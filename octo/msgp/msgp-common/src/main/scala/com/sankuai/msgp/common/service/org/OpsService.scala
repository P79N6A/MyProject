package com.sankuai.msgp.common.service.org

import java.net.URLEncoder
import java.util
import java.util.Collections
import java.util.concurrent.{Executors, TimeUnit}

import com.google.common.cache.{CacheBuilder, CacheLoader}
import com.sankuai.inf.octo.mns.util.ProcessInfoUtil
import com.sankuai.msgp.common.model.{Env, Pdl}
import com.sankuai.msgp.common.model.ServiceModels.User
import com.sankuai.msgp.common.service.appkey.AppkeyDescService
import com.sankuai.msgp.common.service.hulk.HulkApiService
import com.sankuai.msgp.common.utils.ExecutionContextFactory
import com.sankuai.msgp.common.utils.client.TairClient
import com.sankuai.msgp.common.utils.helper.{HttpHelper, JsonHelper}
import dispatch._
import org.apache.commons.lang3.StringUtils
import org.slf4j.{Logger, LoggerFactory}
import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._
import play.api.libs.json._

import scala.collection.JavaConversions._
import scala.collection.JavaConverters._
import scala.collection.concurrent.TrieMap
import scala.collection.mutable.Set
import scala.collection.parallel.ForkJoinTaskSupport
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, duration}

object OpsService {
  private val LOG: Logger = LoggerFactory.getLogger(OpsService.getClass)
  private val opsHost = "http://ops.sankuai.com"
  private val token = "Bearer 6e0f033b45a278d2a6cad32940de88c9b4bd5725"

  private implicit val ec = ExecutionContextFactory.build(Runtime.getRuntime.availableProcessors)

  private val threadPool = new ForkJoinTaskSupport(new scala.concurrent.forkjoin.ForkJoinPool(8))

  private implicit val timeout = Duration.create(30L, duration.SECONDS)

  private val owtpdl = TrieMap[String, util.ArrayList[Pdl]]()
  //owt与business的对应关系
  var owtBusiness = TrieMap[String, TreeData]()
  //business 集合
  var businessGroup = Set[String]()

  private val pdlOwner = TrieMap[Pdl, List[User]]()

  private val scheduler = Executors.newScheduledThreadPool(3)

  case class UserOwts(owts: List[String])

  implicit val userOwtsReads = Json.reads[UserOwts]
  implicit val userOwtsWrites = Json.writes[UserOwts]

  case class ReqIps(ips: List[String])

  case class OpsSrv(corp: String, owt: String, pdl: String, srv: String)

  case class AppkeySrv(appkey: List[String], owt: String, pdl: String, srv: String)

  implicit val appkeySrvReads = Json.reads[AppkeySrv]
  implicit val appkeySrvWrites = Json.writes[AppkeySrv]

  case class OptAppkey(code: Int, msg: Option[String], data: Option[AppkeySrv])

  implicit val optAppkeyReads = Json.reads[OptAppkey]
  implicit val optAppkeyWrites = Json.writes[OptAppkey]

  case class OpsHost(ip_lan: String, env: String)

  implicit val opsHostReads = Json.reads[OpsHost]
  implicit val opsHostWrites = Json.writes[OpsHost]

  val hostnameTagCache = CacheBuilder.newBuilder().maximumSize(5000L).expireAfterWrite(20L, TimeUnit.SECONDS)
    .build(new CacheLoader[String, String]() {
      def load(hostname: String) = {
        hostTag(hostname)
      }
    })

  private val ip2hostCache = CacheBuilder.newBuilder().maximumSize(2000L).expireAfterWrite(20L, TimeUnit.SECONDS)
    .build(new CacheLoader[String, String]() {
      def load(ip: String) = {
        val time = System.currentTimeMillis()
        val data = TairClient.get(ipkey(ip)).getOrElse(HulkApiService.ip2Host(ip))
        val cost = System.currentTimeMillis() - time
        if (cost > 1000) {
          LOG.error(s"tair get cost $cost,key: ${ipkey(ip)}")
        }
        data
      }
    })

  def refreshOwtBG = {
    scheduler.scheduleAtFixedRate(new Runnable {
      def run(): Unit = {
        try {
          refreshOwt
        } catch {
          case e: Exception => LOG.error(s"refresh owt bussiness fail", e)
        }
      }
    }, 0, 60, TimeUnit.MINUTES)

    scheduler.scheduleAtFixedRate(new Runnable {
      def run(): Unit = {
        try {
          refreshPdls()
        } catch {
          case e: Exception => LOG.error(s"refresh owt pdl fail $e")
        }
      }
    }, 0, 60, TimeUnit.MINUTES)
  }

  def start() {
    scheduler.scheduleAtFixedRate(new Runnable {
      def run(): Unit = {
        try {
          refresh()
        } catch {
          case e: Exception => LOG.error(s"refresh ip2hosts fail", e)
        }
      }
    }, 0, 3, TimeUnit.MINUTES)
  }

  def ipToHost(ip: String) = {
    val host = ip2hostCache.get(ip)
    host
  }

  def ipToRealtimeHost(ip: String) = {
    ProcessInfoUtil.getHostName(ip)
  }

  def host2ip(host: String) = {
    TairClient.get(hostkey(host)).getOrElse(HulkApiService.host2ip(host))
  }

  private def ipkey(ip: String) = {
    s"ops.ip2host.${ip}"
  }

  private def hostkey(host: String) = {
    s"ops.host2ip.${host}"
  }

  def restart(host: String) = {
    try {
      val request = s"$opsHost/api/host/srvcmd/?service=/service/sg_agent&host=$host"
      val msg = HttpHelper.execute(url(request).setHeader("Authorization", token),
        text => (Json.parse(text) \ "msg" \ "msg").asOpt[String])
      msg.isDefined && msg.get == "succ"
    } catch {
      case e: Exception => LOG.error(s"restart $host fail $e")
        false
    }
  }

  def ip2nameMap() = {
    val request = s"$opsHost/api/host/ips"
    try {
      HttpHelper.execute(url(request).setHeader("Authorization", token),
        text => (Json.parse(text) \ "data").asOpt[Map[String, String]])
    } catch {
      case e: Exception =>
        LOG.error(s"ip2nameMap $request fail $e")
        None
    }
  }

  def refresh() = {
    val time = System.currentTimeMillis()
    val timeOut = (expiredTime * 70).toInt
    val ipnamemap = ip2nameMap()
    val map = ipnamemap.getOrElse(Map())
    val mapPar = map.par
    mapPar.tasksupport = threadPool
    mapPar.foreach {
      case (ip, name) =>
        if (StringUtils.isNotBlank(ip) && StringUtils.isNotBlank(name)) {
          TairClient.put(ipkey(ip), name, timeOut)
          TairClient.put(hostkey(name), ip, timeOut)
        }
    }
    LOG.info(s"refresh ip2host cost ${System.currentTimeMillis() - time}")
  }

  //刷新所有的pdl
  def refreshPdls() = {
    try {
      val request = s"$opsHost/api/stree/paths?schema=corp-owt-pdl&format=json"
      val data = HttpHelper.execute(url(request).setHeader("Authorization", token))
      val json = (Json.parse(data.get.toString) \ "data").asInstanceOf[JsArray].value
      json.map {
        path =>
          val arr_path = path.asOpt[String].get.split("&")
          if (arr_path.length == 2) {
            val owt = arr_path.apply(1).split("=").apply(1)
            refreshPdl(new Pdl(owt, ""))
          }
          if (arr_path.length == 3) {
            val owt = arr_path.apply(1).split("=").apply(1)
            val pdl = arr_path.apply(2).split("=").apply(1)
            refreshPdl(new Pdl(owt, pdl))
          }
      }
    } catch {
      case e: Exception => LOG.error(s"获取 pdl失败 $e")
        None
    }
  }

  def allservice() = {
    try {
      val request = s"$opsHost/api/stree/paths?schema=corp-owt-pdl-srv&format=json"
      val data = HttpHelper.execute(url(request).setHeader("Authorization", token))
      val json = (Json.parse(data.get.toString) \ "data").asInstanceOf[JsArray].value
      val result = json.map {
        path =>
          val arr_path = path.asOpt[String].get.split("&")
          if (arr_path.length == 4) {
            val corp = arr_path.apply(0).split("=").apply(1)
            val owt = arr_path.apply(1).split("=").apply(1)
            val pdl = arr_path.apply(2).split("=").apply(1)
            val srv = arr_path.apply(3).split("=").apply(1)
            Some(OpsSrv(corp, owt, pdl, srv))
          } else {
            None
          }
      }.filter(_ != None).toList
      result
    } catch {
      case e: Exception => LOG.error(s"获取 pdl失败 $e")
        List()
    }
  }

  def refreshOwt = {
    val treeDatas = getStreeServiceOwt
    val tmp_owtBusiness = TrieMap[String, TreeData]()
    val tmp_businessGroup = Set[String]()
    treeDatas.foreach {
      treeData =>
        tmp_owtBusiness.put(treeData.key, treeData)
        //        if (treeData.key.startsWith("meituan")) {
        tmp_businessGroup.add(treeData.business_group.getOrElse("其他"))
      //        }
    }
    owtBusiness = tmp_owtBusiness
    businessGroup = tmp_businessGroup
  }

  def refreshPdl(owtPdl: Pdl): Boolean = {
    val pdllist = owtpdl.getOrElseUpdate(owtPdl.getOwt, new util.ArrayList[Pdl]())
    try {
      owtpdl.put(owtPdl.getOwt, pdllist)
      val owners = owtOwnerCache.get(owtPdl)
      pdlOwner.put(owtPdl, owners)
      //      owtPdl.setOwners(owners.asJava)
      val index = pdllist.indexOf(owtPdl)
      if (index > -1) {
        pdllist.set(index, owtPdl)
        true
      } else {
        pdllist.add(owtPdl)
      }

    } catch {
      case e: Exception => LOG.error(s"refreshPdl失败 $owtPdl")
        false
    }
  }

  def getHostEnvTag(ip: String) = {
    try {
      val name = ipToHost(ip)
      val request = s"$opsHost/api/stree/host/tag?host=$name"
      val data = HttpHelper.execute(url(request).setHeader("Authorization", token), text =>
        (Json.parse(text) \ "data").asOpt[List[String]])
      if (data.isDefined && data.nonEmpty) {
        data.get(0).split("&").filter(_.contains("cluster")).toList(0).split("=").toList(1)
      } else {
        None
      }
    } catch {
      case e: Exception => LOG.error(s"can't get $ip env tag", e)
        None
    }
  }


  def pdlList(owt: String): util.List[Pdl] = {
    if (StringUtils.isNotBlank(owt)) {
      owtpdl.getOrElse(owt, new util.ArrayList[Pdl]())
    } else {
      owtpdl.flatMap(_._2.asScala).toList.asJava
    }
  }

  def getOwtsbyUsername(username: String): util.ArrayList[String] = {
    try {
      val request = s"$opsHost/api/v0.2/users/$username/owts"
      val data = HttpHelper.execute(url(request).setHeader("Authorization", token))
      if (data.isDefined) {
        val userOwtOpt = Json.parse(data.get).asOpt[UserOwts]
        userOwtOpt match {
          case Some(userOwts) =>
            val owts = userOwts.owts.map { x =>
              val strs = x.split("\\.")
              if (strs.size == 2) {
                strs(1)
              } else {
                strs(0)
              }
            }
            new java.util.ArrayList[String](owts.asJava)
          case None =>
            LOG.warn(s"getOwtsbyUsername $username to UserOwts fail")
            new util.ArrayList[String]()
        }
      } else {
        new util.ArrayList[String]()
      }
    } catch {
      case e: Exception => LOG.error(s"getOwtsbyUsername $username", e)
        new util.ArrayList[String]()
    }
  }

  def owtList(): util.List[String] = {
    if (owtpdl.isEmpty) {
      refreshPdls()
    }
    owtpdl.keys.toList.asJava
  }

  //给业务线添加负责人
  def saveOwtOwner(pdl: Pdl, usernames: util.List[String]): String = {
    val request = url(s"$opsHost/api/stree/tag/node/").setHeader("Authorization", token)
    val path = if (StringUtils.isBlank(pdl.getPdl)) {
      s"corp=meituan&owt=${pdl.getOwt}"
    } else {
      s"corp=meituan&owt=${pdl.getOwt}&pdl=${pdl.getPdl}"
    }

    val params = Map("path" -> path, "key" -> "owner_users", "value" -> usernames.asScala.filter(StringUtils.isNotBlank))
    val future = Http(request.POST << JsonHelper.jsonStr(params) > as.String)
    val result = Await.result(future, timeout)
    (Json.parse(result) \ "msg").asOpt[String].get match {
      case "OK" =>
        refreshPdl(pdl)
        "OK"
      case msg =>
        throw new Exception(msg)
    }

  }

  def isOpsTreeHost(appkey: String, ip: String, env: String) = {
    val request = url(s"$opsHost/api/v0.2/appkeys/$appkey/hosts").setHeader("Authorization", token)
    val data = HttpHelper.execute(request)
    if (data.isDefined) {
      (Json.parse(data.get) \ "hosts").asOpt[List[OpsHost]] match {
        case None =>
          false
        case x =>
          val res = x.get.foldLeft(false) {
            (result, item) =>
              result || (item.ip_lan.equals(ip) && item.env.equals(env))
          }
          res
      }
    }else{
      false
    }
  }

  def getOwtOwners(list: util.List[Pdl]): Map[Pdl, util.List[User]] = {
    list.foldLeft(Map.empty[Pdl, util.List[User]]) {
      (result, pdl) =>
        val owner = pdlOwner.getOrElse(pdl, List[User]())
        result + (pdl -> owner)
    }
  }

  val expiredTime = 60L
  val owtOwnerCache = CacheBuilder.newBuilder().expireAfterWrite(expiredTime, TimeUnit.MINUTES)
    .build(new CacheLoader[Pdl, List[User]]() {
      def load(pdl: Pdl) = {
        List[User]()
      }
    })

  val srvAppkeyCache = CacheBuilder.newBuilder().expireAfterWrite(expiredTime, TimeUnit.MINUTES)
    .build(new CacheLoader[OpsSrv, List[String]]() {
      def load(opsSrv: OpsSrv) = {
        try {
          getOpsAppkeys(opsSrv)
        } catch {
          case e: Exception => LOG.error(s"owt ${opsSrv.owt},${opsSrv.pdl},${opsSrv.srv} $e")
            List[String]()
        }
      }
    })

  //获取负责人,优先获取业务线服务人,没有获取部门SRE负责人
  def getOwner(pdl: Pdl): List[User] = {
    val owners = getOwtOwner(pdl) match {
      case Some(owner) if owner.nonEmpty =>
        owner
      case Some(owner) if owner.isEmpty =>
        getOwtSre(pdl.getOwt).asScala.toList
      case None =>
        getOwtSre(pdl.getOwt).asScala.toList
    }
    owners.map {
      owner =>
        getUser(owner).getOrElse(User(0, "", ""))
    }
  }

  def getUser(owner: String): Option[User] = {
    SsoService.getUser(owner) match {
      case Some(emp) =>
        Some(User(emp.getId, emp.getLogin, emp.getName))
      case _ =>
        None
    }
  }

  //获取业务线负责人
  def getOwtOwner(pdl: Pdl): Option[List[String]] = {
    try {
      val path = if (StringUtils.isBlank(pdl.getPdl)) {
        s"corp=meituan&owt=${pdl.getOwt}"
      } else {
        s"corp=meituan&owt=${pdl.getOwt}&pdl=${pdl.getPdl}"
      }
      val request = s"$opsHost/api/stree/tag/node/?format=json&$path&key=owner_users"
      HttpHelper.execute(url(request).setHeader("Authorization", token), text =>
        (Json.parse(text) \ "data").asOpt[List[String]])
    } catch {
      case e: Exception => LOG.error(s"getOwtOwner $pdl", e)
        None
    }
  }

  // 获取指定部门的SRE负责人
  // ops新接口传入参数需要带meituan或dianping前缀, 需要增加关联查询, 较为麻烦
  // 考虑到owt不多, 此处查出全量owt和sre, 根据入参判断获取对应sre
  def getOwtSre(owt: String): util.List[String] = {
    try {
      val request = s"$opsHost/api/v0.2/owts"
      val text = HttpHelper.execute(url(request).setHeader("Authorization", token))
      if (text.isDefined) {
        val sres = (Json.parse(text.get) \ "owts").asInstanceOf[JsArray].value.flatMap { value =>
          val owtKeyStr = (value \ "key").asOpt[String].getOrElse("")
          val tmpStrs = owtKeyStr.split("\\.")
          var owtKey = ""
          if (tmpStrs.size == 2) {
            owtKey = tmpStrs(1)
          } else {
            owtKey = tmpStrs(0)
          }
          if (owt.equals(owtKey)) {
            (value \ "op_admin").asOpt[String].getOrElse("").split(",").toList.asJava
          } else {
            Collections.emptyList[String]
          }
        }
        sres
      } else {
        LOG.warn(s"getOwtSre fail, return message is none")
        Collections.emptyList[String]
      }
    } catch {
      case e: Exception => LOG.error(s"getOwtSre $owt", e)
        Collections.emptyList[String]
    }
  }

  //获取指定部门的SRE负责人
  def getOwtAdmin(owt: String): util.List[String] = {
    try {
      val request = s"$opsHost/api/stree/tag/admin?tag=corp%3Dmeituan%26owt%3D${owt}"
      val data = HttpHelper.execute(url(request).setHeader("Authorization", token), text =>
        (Json.parse(text) \ "data" \ s"corp=meituan&owt=${owt}" \ "rd_admin").asOpt[List[String]])
      data.get.asJava
    } catch {
      case e: Exception => LOG.error(s"getOwtSre $owt", e)
        new util.ArrayList[String]()
    }
  }

  def getAppSre(pdl: Pdl): util.List[String] = {
    try {
      val path = if (StringUtils.isBlank(pdl.getPdl)) {
        s"corp=meituan&owt=${pdl.getOwt}"
      } else {
        s"corp=meituan&owt=${pdl.getOwt}&pdl=${pdl.getPdl}"
      }
      val encode_Path = URLEncoder.encode(path, "UTF8")
      val request = s"$opsHost/api/stree/tag/admin?tag=$encode_Path"
      val data = HttpHelper.execute(url(request).setHeader("Authorization", token), text =>
        (Json.parse(text) \ "data" \ s"$path" \ "op_admin").asOpt[List[String]])
      data.get.asJava
    } catch {
      case e: Exception => LOG.error(s"getAppSre $pdl", e)
        new util.ArrayList[String]()
    }
  }

  def getOwnerByIp(ip: String): util.List[String] = {
    try {
      val request = s"$opsHost/api/v0.2/hosts/$ip/srvs"
      val text = HttpHelper.execute(url(request).setHeader("Authorization", token))
      if (text.isDefined) {
        val admins = new util.ArrayList[String]()
        (Json.parse(text.get) \ "srvs").asInstanceOf[JsArray].value.flatMap { value =>
          val rd_admin = (value \ "rd_admin").asOpt[String].getOrElse("").split(",").toList.asJava
          val op_admin = (value \ "op_admin").asOpt[String].getOrElse("").split(",").toList.asJava
          if (rd_admin.nonEmpty) {
            admins.addAll(rd_admin)
          }
          if (op_admin.nonEmpty) {
            admins.addAll(op_admin)
          }
          admins
        }
      } else {
        LOG.warn(s"getOwnerByIp $ip fail, return message is none")
        Collections.emptyList[String]
      }
    } catch {
      case e: Exception => LOG.error(s"getOwnerByIp: $ip ", e)
        Collections.emptyList[String]
    }
  }

  def hostTag(hostname: String): String = {
    val tags = getHostTag(hostname)
    if (tags.isEmpty) {
      ""
    } else {
      tags.get(0)
    }
  }

  /**
    * 获取指定环境和机房下的主机列表
    *
    * @param env
    * @param idc
    * @return
    */
  def getHostnamesFromEnvAndIdc(env: String, idc: String): util.List[String] = {
    try {
      val request = s"$opsHost/api/v0.2/hosts?q=env:$env,idc:$idc"
      implicit val timeout = Duration.create(1L, duration.HOURS)
      val text = HttpHelper.execute(url(request).setHeader("Authorization", token))(timeout)
      val nameList = new util.ArrayList[String]()
      if (text.isDefined) {
        (Json.parse(text.get) \ "hosts").asInstanceOf[JsArray].value.map { value =>
          val name = (value \ "name").asOpt[String].getOrElse("")
          if (name.nonEmpty) {
            nameList.add(name)
          }
        }
        nameList
      } else {
        LOG.warn(s"getIPsFromEnvAndIdc fail, return message is none")
        Collections.emptyList[String]
      }
    } catch {
      case e: Exception => LOG.error(s"getIPsFromEnvAndIdc env=$env, idc=$idc", e)
        Collections.emptyList[String]
    }
  }

  /**
    * 获取指定环境下的主机列表
    *
    * @param env
    * @return
    */
  def getHostnamesFromEnv(env: String): util.List[String] = {
    try {
      val request = s"$opsHost/api/v0.2/hosts?q=env:$env"
      implicit val timeout = Duration.create(1L, duration.HOURS)
      val text = HttpHelper.execute(url(request).setHeader("Authorization", token))(timeout)
      val nameList = new util.ArrayList[String]()
      if (text.isDefined) {
        (Json.parse(text.get) \ "hosts").asInstanceOf[JsArray].value.map { value =>
          val name = (value \ "name").asOpt[String].getOrElse("")
          if (name.nonEmpty) {
            nameList.add(name)
          }
        }
        nameList
      } else {
        LOG.warn(s"getIPsFromEnv fail, return message is none")
        Collections.emptyList[String]
      }
    } catch {
      case e: Exception => LOG.error(s"getIPsFromEnv env=$env", e)
        Collections.emptyList[String]
    }
  }


  /**
    * 根据ops发布项的名称获得tag
    */

  case class PlusRelation(appkey: List[String], owt: String, pdl: String, srv: String, bind: Int)

  implicit val plusRelationReads = Json.reads[PlusRelation]
  implicit val plusRelationWrites = Json.writes[PlusRelation]

  def getAppkeyRelation(appkey: String) = {
    try {
      val request = s"$opsHost/api/stree/relation/octo?appkey=$appkey"
      val data = HttpHelper.execute(url(request).setHeader("Authorization", token),
        text =>
          (Json.parse(text) \ "data").asOpt[PlusRelation])
      data
    } catch {
      case e: Exception => LOG.error(s"getAppTag $appkey", e)
        None
    }
  }

  def getAppTag(app: String) = {
    try {
      val request = s"$opsHost/api/stree/relation/plus?appkey=$app"
      val data = HttpHelper.execute(url(request).setHeader("Authorization", token),
        text =>
          (Json.parse(text) \ "data").asOpt[PlusRelation])
      if (data.isDefined) {
        val plusRelation = data.get
        Some(s"corp=meituan&owt=${plusRelation.owt}&pdl=${plusRelation.pdl}&srv=${plusRelation.srv}")
      } else {
        None
      }
    } catch {
      case e: Exception => LOG.error(s"getAppTag $app", e)
        None
    }
  }

  /**
    * 根据appkey的名称获得tag
    */
  def getAppkeyTag(appkey: String) = {
    try {
      val request = s"$opsHost/api/stree/relation/octo?appkey=$appkey"
      val data = HttpHelper.execute(url(request).setHeader("Authorization", token),
        text =>
          (Json.parse(text) \ "data").asOpt[PlusRelation])
      if (data.isDefined) {
        val plusRelation = data.get
        Some(s"corp=meituan&owt=${plusRelation.owt}&pdl=${plusRelation.pdl}&srv=${plusRelation.srv}")
      } else {
        None
      }
    } catch {
      case e: Exception => LOG.error(s"getAppkeyTag $appkey", e)
        None
    }
  }

  /**
    * 根据tag获取RD负责人
    *
    * @param tag
    * @return
    */
  def getRDAdmin(tag: String) = {
    val tagAsURL: String = URLEncoder.encode(tag, "UTF8")
    try {
      val request = s"$opsHost/api/stree/tag/admin?format=json&tag=$tagAsURL"
      val data = HttpHelper.execute(url(request).setHeader("Authorization", token), text =>
        (Json.parse(text) \ "data" \ tag \ "rd_admin").asOpt[List[String]])
      data.get.asJava
    } catch {
      case e: Exception => LOG.error(s"getRDAdmin $tagAsURL", e)
        new util.ArrayList[String]()
    }
  }

  case class SrvData(duty_admin: String = "", rd_admin: String = "", op_admin: String = "")

  implicit val srvDataReads: Reads[SrvData] = (
    (JsPath \ "duty_admin").read[String] and
      (JsPath \ "rd_admin").read[String] and
      (JsPath \ "op_admin").read[String]
    ) (SrvData.apply _)


  implicit val srvDataWrites: Writes[SrvData] = Writes { srvData =>
    Json.obj(
      "duty_admin" -> srvData.duty_admin,
      "rd_admin" -> srvData.rd_admin,
      "op_admin" -> srvData.op_admin
    )
  }

  /**
    * 获取srv的报警 负责人
    * 合并值班人信息和服务负责人信息
    *
    * @param srv
    * @return
    */
  def getSrvAlarmAdmin(srv: String) = {
    try {
      val request = s"$opsHost/api/stree/service/srv/$srv"
      val data = HttpHelper.execute(url(request).setHeader("Authorization", token), text =>
        (Json.parse(text) \ "data").asOpt[SrvData])
      if (data.isDefined) {
        val srvData = data.get
        val new_alarm_user = List(srvData.duty_admin, srvData.rd_admin).filter { x => StringUtils.isNotBlank(x) }.mkString(",")
        Some(new_alarm_user)
      } else {
        LOG.error(s"getsrv $srv request $request")
        None
      }
    } catch {
      case e: Exception => LOG.error(s"getsrv $srv", e)
        None
    }
  }

  def getAppkeyAlarmAdmin(appkey: String): Option[Seq[String]] = {
    val desc = AppkeyDescService.getAppkeyDesc(appkey)
    if (desc != null) {
      val base = desc.base
      val srv = if (base.get == 0) {
        val relationOpt = getAppkeyRelation(appkey)
        if (relationOpt.isDefined) {
          val relation = relationOpt.get
          s"meituan.${relation.owt}.${relation.pdl}.${relation.srv}"
        } else {
          ""
        }
      } else if (desc.owt.isDefined && desc.pdl.isDefined) {
        s"dianping.${desc.owt.get}.${desc.pdl.get}.$appkey"
      } else {
        ""
      }
      if (StringUtils.isNotBlank(srv)) {
        val alarmAdmin = getSrvAlarmAdmin(srv)
        Some(alarmAdmin.getOrElse("").split(",").toSeq)
      } else {
        None
      }
    } else {
      None
    }
  }

  def getHostTag(hostname: String): util.List[String] = {
    try {
      val request = s"$opsHost/api/stree/host/tag?format=json&host=$hostname"
      val data = HttpHelper.execute(url(request).setHeader("Authorization", token), text =>
        (Json.parse(text) \ "data" \ hostname).asOpt[List[String]])
      if (data.isDefined) {
        data.get.asJava
      } else {
        LOG.error(s"getHostTag $hostname request $request")
        new util.ArrayList[String]()
      }
    } catch {
      case e: Exception => LOG.error(s"getHostTag $hostname", e)
        new util.ArrayList[String]()
    }
  }

  def getOpsAppkey(appkey: String) = {
    try {
      val request = s"$opsHost/api/srvset/srvocto?appkey=$appkey"
      val data = HttpHelper.execute(url(request).setHeader("Authorization", token), text =>
        Json.parse(text).asOpt[OptAppkey])
      data.get
    } catch {
      case e: Exception => LOG.error(s"getOpsAppkey $appkey", e)
        OptAppkey(404, None, None)
    }

    OptAppkey(404, None, None)
  }

  def ipname(ips: List[String]): Option[Map[String, String]] = {
    val timeOut = (expiredTime * 70).toInt
    val reqIps = ReqIps(ips)
    val request = s"$opsHost/api/host/ip"
    val postReq = url(request).setHeader("Authorization", token).addHeader("Content-Type", "application/json;charset=utf-8").POST << JsonHelper.jsonStr(reqIps)
    try {
      val future = Http(postReq OK as.String)
      val text = Await.result(future, timeout)
      val ipnamesOpt = (Json.parse(text) \ "data").validate[Map[String, String]].asOpt
      if (ipnamesOpt.isDefined) {
        val ipnames = ipnamesOpt.get
        ipnames.foreach {
          case (ip, name) =>
            if (StringUtils.isNotBlank(ip) && StringUtils.isNotBlank(name)) {
              TairClient.put(ipkey(ip), name, timeOut)
              TairClient.put(hostkey(name), ip, timeOut)
            }
        }
        ipnamesOpt
      } else {
        None
      }
    } catch {
      case e: Exception =>
        LOG.error(s"get ip name error", e)
        None
    }
  }

  def getOpsAppkeys(opsSrv: OpsSrv) = {
    try {
      val request = s"$opsHost/api/stree/relation/octo?owt=${opsSrv.owt}&pdl=${opsSrv.pdl}&srv=${opsSrv.srv}"
      val dataOpt = HttpHelper.execute(url(request).setHeader("Authorization", token), text =>
        Json.parse(text).asOpt[OptAppkey])
      dataOpt match {
        case None => List[String]()
        case Some(data) =>
          data.data match {
            case None => List[String]()
            case Some(optAppkey) => optAppkey.appkey
          }
      }

    } catch {
      case e: Exception => LOG.error(s"getOpsAppkey ${opsSrv.owt},${opsSrv.pdl},${opsSrv.srv}", e)
        List[String]()
    }
    List[String]()
  }

  def deleteOpsAppkey(appkeySrv: AppkeySrv) = {
    val request = s"$opsHost/api/stree/relation/octo"
    val param_data = JsonHelper.jsonStr(appkeySrv)
    val deleteReq = url(request).setContentType("application/json", "UTF-8").setHeader("Authorization", token).DELETE << param_data
    try {
      val future = Http(deleteReq OK as.String)
      val text = Await.result(future, timeout)
      LOG.info(s"delete appkey ${appkeySrv},text $text")
      Json.parse(text).validate[OptAppkey].asOpt
    } catch {
      case e: Exception =>
        LOG.error(s"get delete app failed,${request},${param_data}", e)
        None
    }
  }

  def addOpsAppkey(appkeySrv: AppkeySrv) = {
    val request = s"$opsHost/api/srvset/srvocto"
    val postReq = url(request).setHeader("Authorization", token).POST << JsonHelper.jsonStr(appkeySrv)
    try {
      val future = Http(postReq OK as.String)
      val text = Await.result(future, timeout)
      Json.parse(text).validate[OptAppkey].asOpt
    } catch {
      case e: Exception =>
        LOG.error(s"get delete failed app", e)
        None
    }
  }

  def getServerNodeHosts(serverNode: String) = {
    val request = s"$opsHost/api/stree/tag/host?" + serverNode
    val postReq = url(request).setHeader("Authorization", token).GET
    try {
      val future = Http(postReq OK as.String)
      val text = Await.result(future, timeout)
      (Json.parse(text) \ "data").validate[List[String]].getOrElse(List())
    } catch {
      case e: Exception =>
        LOG.error(s"get host by serverNode failed serverNode:$serverNode", e)
        List()
    }
  }

  case class TreeData(comment: String = "", duty_admin: String = "", key: String = "",
                      id: Int = 0, nginx_cluster: Option[String] = Some(""), name: String = "", create_by: String = "",
                      update_at: Long = 0L, create_at: Long = 0L, user_groups: Option[String] = Some(""), rd_admin: String = "",
                      business_group: Option[String] = Some(""), op_admin: String = "", sre_xm_group: Option[String] = Some(""), ep_admin: String = "")

  implicit val treeDataReads = Json.reads[TreeData]
  implicit val treeDataWrites = Json.writes[TreeData]


  def getStreeServiceOwt = {
    val urlStr = s"$opsHost/api/stree/service/list/owt"
    val getReq = url(urlStr).setHeader("Authorization", token).GET

    try {
      val future = Http(getReq OK as.String)
      val text = Await.result(future, timeout)
      //      val validateResult = (Json.parse(text) \ "data").validate[List[TreeData]]
      (Json.parse(text) \ "data").validate[List[TreeData]].getOrElse(List())
    } catch {
      case e: Exception =>
        LOG.error(s"get stree service list owt failed", e)
        List()
    }
  }
}
