package com.sankuai.octo.msgp.appkey

import java.net.URI
import java.util
import java.util.regex.Pattern

import com.sankuai.meituan.borp.impl.BorpServiceImpl
import com.sankuai.meituan.borp.vo.{Action, BorpRequest}
import com.sankuai.msgp.common.model.ServiceModels.{AppkeyTs, Desc, DescSimple, User}
import com.sankuai.msgp.common.model.{EntityType, Env, Path, Pdl}
import com.sankuai.msgp.common.service.org.{OpsService, OrgSerivce, SsoService}
import com.sankuai.msgp.common.utils.helper.{CommonHelper, JsonHelper}
import com.sankuai.octo.msgp.dao.appkey.AppkeyDescDao
import com.sankuai.octo.msgp.domain.AppkeyReg
import com.sankuai.octo.msgp.serivce.service.{ServiceCommon, ServiceDesc, ServiceGroup, ServiceHttpConfig}
import com.sankuai.octo.msgp.utils.Auth
import com.sankuai.octo.msgp.utils.client.{ZkClient, ZkHlbClient}
import com.sankuai.octo.mworth.util.DateTimeUtil
import dispatch.{Http, as, url}
import org.apache.commons.lang3.StringUtils
import org.apache.http.HttpEntity
import org.apache.http.client.CookieStore
import org.apache.http.client.config.{CookieSpecs, RequestConfig}
import org.apache.http.client.methods.{CloseableHttpResponse, HttpGet}
import org.apache.http.impl.client.{BasicCookieStore, CloseableHttpClient, HttpClients}
import org.apache.http.impl.cookie.BasicClientCookie
import org.apache.http.util.EntityUtils
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{BeforeAndAfter, FunSuite}
import org.slf4j.{Logger, LoggerFactory}
import play.api.libs.json.{JsArray, Json}

import scala.collection.JavaConverters._
import scala.collection.mutable.ListBuffer
import scala.collection.parallel.ForkJoinTaskSupport
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, duration}

/**
 * Created by zava on 16/1/22.
 * 读取所有appkey的服务列表
 * 根据服务列表获取hostname
 * 通过hostname 获取 服务树标签信息
 * http://ops.sankuai.com/api/stree/host/tag?host=dx-inf-octo-log23
 *
 */
@RunWith(classOf[JUnitRunner])
class ServiceDescSuite extends FunSuite with BeforeAndAfter {
  private val taskSuppertPool = new ForkJoinTaskSupport(new scala.concurrent.forkjoin.ForkJoinPool(8))
  val LOG: Logger = LoggerFactory.getLogger("ServiceOwtSuite")
  val ssoid = "2815cb0925*c4af5a999867ebe5e70b0"
  implicit val timeout = Duration.create(60L, duration.SECONDS)
  //  val hostname = "octo.test.sankuai.info"
//    val hostname = "localhost:8080"
  val hostname = "octo.sankuai.com"


  test("restoreAppKey") {
    //获取所有的线上的appkey
    val descRichList = getAppkey
    var count = 0
    descRichList.foreach {
      descRich =>
        count = count + 1
        println(s"#$count-${descRichList.length}")
        val business = getBorpBusiness(descRich.appkey)
        business match {
          case Some(busGroup) =>
            println(s"changed,${descRich.appkey},${business}")
            busGroup.business match {
              case None =>
              case Some(_) =>
                if (!descRich.business.equals(busGroup.business) || !descRich.group.equals(busGroup.group)) {
                  updateBusGroup(descRich, busGroup)
                }
            }

          case None =>
            println(s"nochang,${descRich.appkey}")
        }
    }
  }

  private val appkey = "msgp"
  private val secret = "b535efb74b52d3d202cb96d2e239b454"
  private val scannerApp = "com.sankuai.octo.scanner"
  private val borpHostUrl = {
    "http://release.borp.test.sankuai.info"
    //          "http://api.borp-in.sankuai.com"
  }
  private val borpService = new BorpServiceImpl(borpHostUrl, appkey, secret)

  case class BusGroup(business: Option[Int] = Some(0), group: Option[String] = Some(""))

  def getBorpBusiness(appkey: String): Option[BusGroup] = {
    val start = DateTimeUtil.parse("2016-02-01 00:20:20", DateTimeUtil.DATE_TIME_FORMAT)
    val end = DateTimeUtil.parse("2016-02-03 20:20:20", DateTimeUtil.DATE_TIME_FORMAT)
    val borpRequest = BorpRequest.builder.
      mustEq("operatorId", "64137").
      mustEq("appkey", "msgp").
      mustEq("entityType", "服务概要修改").
      mustEq("entityId", appkey).
      beginDate(start).
      endDate(end).
      from(0).
      size(20).build
    val borpResponse = borpService.getBorpResponse(borpRequest, classOf[Action])
    if (null == borpResponse) {
      return None
    }
    val actions = borpResponse.getResult.asScala.toList
    if (actions.size > 0) {
      val first_Action = actions.sortWith((x, nextX) => x.getAddTime.compareTo(nextX.getAddTime) < 0).head
      val details = borpService.getDetailByActionId(first_Action.getActionId).asScala.toList
      if (details.size > 0) {
        val first_detail = details.head;
        val business = EntityType.withName(first_detail.getEntityType) match {
          case EntityType.updateServer =>
            val oldObj = Json.parse(first_detail.getOldValue).validate[Desc].asOpt.get
            val newObj = Json.parse(first_detail.getNewValue).validate[Desc].asOpt.get
            if (!oldObj.business.equals(newObj.business) || !oldObj.group.equals(newObj.group)) {
              println(s"部门从${oldObj.business}修改为${newObj.business}")
              Some(BusGroup(oldObj.business, oldObj.group))
            } else {
              None
            }
          case _ =>
            println(s"修改无关,${first_detail.getActionId},${first_detail.getDetailId}")
            None
        }
        return business;
      }
    }
    return None;
  }

  test("updatebase") {
    var count = 0;
    val descRichList = getAppkey
    descRichList.par.foreach {
      descRich =>
        count = count + 1;
        if (matchChinese(descRich.owt.getOrElse("")) && descRich.appkey.contains("-")) {
          val base = descRich.base.getOrElse(0)
          if (base == 0) {
            val simpleDesc = Desc(descRich.name, descRich.appkey, Some(descRich.appkey), descRich.owners, descRich.observers, descRich.intro, descRich.category,
              descRich.business, descRich.group, Some(1), descRich.owt, descRich.pdl, descRich.level, descRich.tags, descRich.regLimit, descRich.createTime)
            updateService(descRich.appkey, JsonHelper.jsonStr(simpleDesc));
          }

        }
    }
  }

  def matchChinese(matchStr: String) = {
    val regEx = "[\u4E00-\u9FA5]";
    val p = Pattern.compile(regEx);
    val matcher = p.matcher(matchStr)
    val finder = matcher.find()
    finder
  }

  /**
   * 维护服务负责人信息
   * desc: 部分RD已经离职从服务负责人里删除
   */
  test("updateOwner") {
    val descRichList = getAppkey.filter(_.owt.getOrElse("").equals("waimai"))
    val descPar = descRichList.par
    var count = 0;
    descPar.tasksupport = taskSuppertPool
    descPar.foreach {
//    descRichList.foreach {
      descRich =>
        count = count + 1;
        updateOwner(descRich)
        println(s"total：${descRichList.size};count:${count};appkey:${descRich.appkey}")
    }
  }

  private def updateOwner(descRich: Desc) = {
    //判定服务负责人是否离职了
    var isOut = false
    var owners: List[User] = descRich.owners.filter {
      owner =>
        val employeeInfo = OrgSerivce.employee(owner.login)
        val user_Opt = SsoService.getUser(owner.login)
        employeeInfo match {
          case Some(eInfo) =>
            if (eInfo.getStatus.toInt == 1) {
              println(s"${descRich.appkey}，已经离职，${owner.login}")
              isOut = true
              false
            } else {
              true
            }
          case None =>
            if (user_Opt.isEmpty) {
              println(s"${owner.login}，无此人，${descRich.appkey}")
              false
            } else {
              true
            }
        }
    }.distinct

    val observers: Option[List[User]] = descRich.observers match {
      case Some(observers) =>
        val newOb = observers.filter {
          obser =>
            val employeeInfo = OrgSerivce.employee(obser.login)
            val user_Opt = SsoService.getUser(obser.login)
            employeeInfo match {
              case Some(eInfo) =>
                if (eInfo.getStatus.toInt == 1) {
                  println(s"${obser.login}，已经离职，${descRich.appkey}")
                  isOut = true
                  false
                } else if (owners.contains(obser)) {
                  false
                } else {
                  true
                }
              case None =>
                if (user_Opt.isEmpty) {
                  println(s"${obser.login}，没有详情，${descRich.appkey}")
                  false
                } else {
                  true
                }
            }
        }.distinct
        Some(newOb)
      case None =>
        None
    }

    if (!isOut) {
      if (owners.size != descRich.owners.size) {
        isOut = true
      }
      if (!isOut && observers.getOrElse(List()).size != descRich.observers.getOrElse(List()).size) {
        isOut = true
      }
    }

    val owt_pdl = (descRich.owt, descRich.pdl)
    //    println(s"appkey:${descRich.appkey},owner$owners}")
    if (owners.size == 0) {
      //获取服务树的负责人
      try {
        owners = getOpsRdAdmin(descRich.appkey)
        isOut = true
      }
      catch {
        case e: Exception => println(s"获取上级失败，${descRich.appkey}")
          owners = descRich.owners
      }
    }

    if (isOut||descRich.owt.getOrElse("").equals("waimai")) {
      val owner_strs = owners.map(_.login)
      val observer_strs: List[String] = observers match {
        case Some(obs) =>
          obs.map(_.login)
        case None =>
          List[String]()
      }

      val simpleDesc = DescSimple(Some(descRich.name), descRich.appkey, Some(descRich.appkey), owner_strs, observer_strs, descRich.intro,Some(descRich.category),
        descRich.business, descRich.group, descRich.base, descRich.owt, descRich.pdl, descRich.level, descRich.tags, descRich.regLimit, descRich.createTime)

      val map = Map("data" -> simpleDesc, "username" -> "tangye03");
      updateService(descRich.appkey, JsonHelper.jsonStr(map))
    }
  }


  def getOpsRdAdmin(appkey: String) = {
    val tagOpt = OpsService.getAppTag(appkey)
    if (tagOpt.isDefined) {
      OpsService.getRDAdmin(tagOpt.get).asScala.toList.flatMap { username =>
        val empOpt = OrgSerivce.employee(username)
        if (empOpt.isDefined) {
          val emp = empOpt.get
          Some(User(emp.getId, emp.getLogin, emp.getName))
        } else {
          None
        }
      }
    } else {
      List()
    }
  }

  test("initOwt") {
    //获取所有的线上的appkey
    val descRichList = getAppkey;
    var count = 0;
    descRichList.foreach {
      descRich =>
        count = count + 1;
        println(s"#$count-${descRichList.length}")
        descRich.owt match {
          case Some(owt) =>
            if (owt.equals("inf")) {
              updateOwt(descRich)
            }
          case None =>
            updateOwt(descRich)
        }
    }
  }

  test("updateOwtby") {
    val descRichList = getAppkey;
    val userPdlMap = new util.HashMap[String, util.List[String]]()
    val descList = new util.ArrayList[Desc]()
    descRichList.foreach {
      descRich =>
        descRich.owt match {
          case Some(owt) =>
            descRich.owners.foreach {
              owner =>
                if (!userPdlMap.containsKey(owner.login)) {
                  val owts = OpsService.getOwtsbyUsername(owner.login)
                  userPdlMap.put(owner.login, owts)
                }
            }
            if (StringUtils.isBlank(owt)) {
              descList.add(descRich);
              println(s"no owt ${descRich.appkey}")
            }
          case None =>
            descList.add(descRich);
            println(s"no owt ${descRich.appkey}")
        }
    }

    descList.asScala.foreach {
      descRich =>
        if (descRich.owners.size < 1) {
          println(s"no owner ${descRich.appkey}")
        } else {
          val user = descRich.owners.apply(0);
          val userOwts = userPdlMap.get(user.login)
          if (null != userOwts && userOwts.size() > 1) {
            userOwts.asScala.foreach {
              owt =>
                if (descRich.appkey.contains(s".${owt}.")) {
                  if (!owt.equals(descRich.owt.get)) {
                    println(s"appkey,owt ${descRich.appkey},${owt}")
                    //                    updateOwt(descRich,owt)
                  }
                }
            }
          } else if (null == userOwts) {
            println(s"no owners,owt ${descRich.appkey}")
          }
        }
    }
  }

  test("owt2") {
    val appkeys = "com.sankuai.meishi.mtes.importer,com.meituan.ia.capi.wx,com.sankuai.meishi.shike,com.sankuai.octo.sanjianke,com.sankuai.banma.auth,com.sankuai.hlb.check,com.meituan.movie.mmdb.event.databus.show,com.sankuai.waimai.bizaudit,com.sankuai.lvyou.meilv.virgo,com.sankuai.hotel.tower.poi,com.sankuai.meishi.api,com.sankuai.train.basedata.search,com.sankuai.hbdata.richard.demo,com.sankuai.merchant.cp,com.sankuai.meilv.grouptravel,com.sankuai.it.family,com.meituan.xg.activity.poi,com.sankuai.travel.campaign,com.meituan.xg.activity.config,com.sankuai.hbdata.bootcamp,com.sankuai.travel.pandora.ruleengine,com.sankuai.gct.information"
    appkeys.split(",").foreach {
      appkey =>
        val descRich = ServiceCommon.desc(appkey)
        val pdlData = getPdl(descRich.appkey)
        updateDesc(descRich, pdlData)
    }
  }

  def updateDesc(descRich: Desc, pdlData: Option[Pdl]) {
    val desc = pdlData match {
      case Some(pdl) =>
        descRich.copy(owt = Some(pdl.getOwt), pdl = Some(pdl.getPdl))
      case None =>
        descRich.copy(owt = Some(""), pdl = Some(""))
    }
    val owners: List[String] = desc.owners.filter(user => (!user.login.equals("tangye03"))).map(_.login)
    val observers: List[String] = desc.observers match {
      case Some(obs) =>
        obs.map(_.login)
      case None =>
        List[String]()
    }
    val simpleDesc = DescSimple(Some(desc.name), desc.appkey, Some(desc.appkey), owners, observers, desc.intro, Some(desc.category),
      desc.business, desc.group, desc.base, desc.owt, desc.pdl, desc.level, desc.tags, desc.regLimit, desc.createTime)
    val map = Map("data" -> simpleDesc, "username" -> "tangye03");
    updateService(desc.appkey, JsonHelper.jsonStr(map));
  }

  /**
   * 初始化 owt 的权限
   * pre:所有的服务已经绑定了owt和pdl
   * 1:获取目前的所有的 服务列表信息
   * 2:创建 插入语句
   */
  test("initowtauth") {
    val descRichList = getAppkey;
    descRichList.foreach {
      descRich =>
        descRich.owt match {
          case Some(owt) =>
            //user_id,appkey,level,name
            println(s"+owt,(0,'${descRich.appkey}',${Auth.Level.READ.getValue},'$owt') ")
          case None =>
            println(s"-owt,${descRich.appkey}")
        }
    }
  }
  test("registryService") {
    val json = "{\"username\": \"yangrui08\", \"data\": {\"appkey\": \"com.sankuai.hbdata.bi.tl1\", \"owners\": [\"yangrui08\"], \"pdl\": \"bi\", \"tags\": \"\", \"observers\": [], \"intro\": \"\", \"owt\": \"hbdata\"}}"
    val appkeyReg = JsonHelper.toObject(json, classOf[AppkeyReg])
    println(appkeyReg.toString)
    val data = appkeyReg.getData
    val appkey = data.getAppkey
    (0 to 10).foreach {
      x =>
        data.setAppkey(appkey + "-" + x)
        val json = JsonHelper.jsonStr(appkeyReg)
        println(json)
        registryService(json)

    }
    (0 to 10).foreach {
      x =>
        val data = ServiceDesc.delete(appkey + "-" + x, "tangye03")
        println(data)
    }
  }


  test("reglimit") {
    val descRichList = getAppkey;
    val regList = descRichList.filter(_.regLimit == 1)
    regList.foreach {
      regdesc =>
        val owners: List[String] = regdesc.owners.map(_.login)
        val observers: List[String] = regdesc.observers match {
          case Some(obs) =>
            obs.map(_.login)
          case None =>
            List[String]()
        }
        val simpleDesc = DescSimple(Some(regdesc.name), regdesc.appkey, Some(regdesc.appkey), owners, observers, regdesc.intro, Some(regdesc.category),
          regdesc.business, regdesc.group, regdesc.base, regdesc.owt, regdesc.pdl, regdesc.level, regdesc.tags, 1, regdesc.createTime)
        val map = Map("data" -> simpleDesc, "username" -> "yangrui08");
        updateService(regdesc.appkey, JsonHelper.jsonStr(map));
    }
    //    registryService(regList)
  }

  def updateService(appkey: String, json: String): Unit = {
    val url = s"http://$hostname/api/service/registry"
    val postReq = addHeaderAndCookie(url).addHeader("Content-Type", "application/json;charset=utf-8").POST << json
    val feature = Http(postReq > as.String)
    val content = Await.result(feature, timeout)
    println(println(s"$appkey,$json,$content"))
  }

  def registryService(json: String): Unit = {
//    val url = s"http://octo.sankuai.com/service/registry"
//    val postReq = addHeaderAndCookie(url).addHeader("Content-Type", "application/json;charset=utf-8").POST << json
//    val feature = Http(postReq > as.String)
//    val content = Await.result(feature, timeout)
//    println(println(s"$appkey,$json,$content"))
    val appkeyReg = JsonHelper.toObject(json, classOf[AppkeyReg])
    ServiceCommon.saveService(appkeyReg,new Array[javax.servlet.http.Cookie](1))
  }

  def exitAppkey(appkey: String): Boolean = {
    val url = s"http://octo.test.sankuai.com/api/service/$appkey/exist"
    val postReq = addHeaderAndCookie(url).addHeader("Content-Type", "application/json;charset=utf-8").GET
    val feature = Http(postReq > as.String)
    val content = Await.result(feature, timeout)
    val exit = (Json.parse(content) \ "data").as[Boolean]
    exit
  }

  def getAppkey(): List[Desc] = {
    val url = s"http://$hostname/service/filter?business=-1&type=4&pageNo=1&pageSize=20000"
    //    val url = s"http://octo.sankuai.com/service/filter?business=-1&type=4&pageNo=1&pageSize=20000"
    val content = httpGet(url)
    val appkeyDescs = (Json.parse(content) \ "data").asInstanceOf[JsArray].value.toSeq
    val list = ListBuffer[Desc]()
    appkeyDescs.foreach {
      appkeyDesc =>
        Json.parse(appkeyDesc.toString()).validate[Desc].fold({ error =>
          println(error)
          None
        }, {
          value => list.append(value)
        })
    }
    list.toList
  }


  def httpGet(urlStr: String) = {
    var result: String = ""
    try {
      val httpget: HttpGet = new HttpGet(urlStr)
      val cookieStore: CookieStore = new BasicCookieStore
      val cookie: BasicClientCookie = new BasicClientCookie("ssoid", ssoid)
      cookie.setVersion(0)
      val uri: URI = httpget.getURI
      cookie.setDomain(uri.getHost)
      cookie.setPath("/")
      cookieStore.addCookie(cookie)
      val httpclient: CloseableHttpClient = HttpClients.custom.setDefaultCookieStore(cookieStore).build
      val requestConfig: RequestConfig = RequestConfig.custom.setSocketTimeout(20000).setConnectTimeout(20000).setCookieSpec(CookieSpecs.STANDARD).build
      System.out.println("Executing request " + httpget.getRequestLine)
      httpget.setConfig(requestConfig)
      val response: CloseableHttpResponse = httpclient.execute(httpget)
      try {
        val entity: HttpEntity = response.getEntity
        if (entity != null) {
          result = EntityUtils.toString(response.getEntity)
        }
      } finally {
        response.close
        httpclient.close
      }
    }
    catch {
      case e: Exception => {
        e.printStackTrace
      }
    }
    result
  }

  def updateOwt(descRich: Desc) = {
    val pdlData = getPdl(descRich.appkey)
    //判定线下有没有,有的话更新
    for (pdl <- pdlData) {
      val oldPdl = descRich.pdl
      if (!pdl.getPdl.equals(oldPdl.getOrElse(""))) {
        val desc = descRich.copy(owt = Some(pdl.getOwt), pdl = Some(pdl.getPdl))
        val owners: List[String] = oldPdl.equals("octo") match {
          case false =>
            desc.owners.filter(user => (!user.login.equals("yangrui08"))).map(_.login)
          case true =>
            desc.owners.map(_.login)
        }

        val observers: List[String] = desc.observers match {
          case Some(obs) =>
            obs.map(_.login)
          case None =>
            List[String]()
        }
        val simpleDesc = DescSimple(Some(desc.name), desc.appkey, Some(desc.appkey), owners, observers, desc.intro, Some(desc.category),
          desc.business, desc.group, desc.base, desc.owt, desc.pdl, desc.level, desc.tags, desc.regLimit, desc.createTime)
        val map = Map("data" -> simpleDesc, "username" -> "yangrui08");
        updateService(desc.appkey, JsonHelper.jsonStr(map));
      }
    }
  }


  def updateOwt(descRich: Desc, owt: String) = {
    //判定线下有没有,有的话更新
    val desc = descRich.copy(owt = Some(owt))
    val owners: List[String] = desc.owners.map(_.login)
    val observers: List[String] = desc.observers match {
      case Some(obs) =>
        obs.map(_.login)
      case None =>
        List[String]()
    }
    val simpleDesc = DescSimple(Some(desc.name), desc.appkey, Some(desc.appkey), owners, observers, desc.intro, Some(desc.category),
      desc.business, desc.group, desc.base, desc.owt, desc.pdl, desc.level, desc.tags, 0, desc.createTime)
    val map = Map("data" -> simpleDesc, "username" -> "yangrui08");
    updateService(desc.appkey, JsonHelper.jsonStr(map));
  }

  def updateBusGroup(descRich: Desc, busGroup: BusGroup) = {
    //判定线下有没有,有的话更新
    val desc = descRich.copy(business = busGroup.business, group = busGroup.group)
    val owners: List[String] = desc.owners.map(_.login)
    val observers: List[String] = desc.observers match {
      case Some(obs) =>
        obs.map(_.login)
      case None =>
        List[String]()
    }

    val simpleDesc = DescSimple(Some(desc.name), desc.appkey, Some(desc.appkey), owners, observers, desc.intro, Some(desc.category),
      desc.business, desc.group, desc.base, desc.owt, desc.pdl, desc.level, desc.tags, 0, desc.createTime)
    val map = Map("data" -> simpleDesc, "username" -> "yangrui08");
    updateService(desc.appkey, JsonHelper.jsonStr(map));

    //    val appkeyReg = new AppkeyReg("hanjiancheng",
    //      desc.appkey, owners, observers,
    //      desc.base.getOrElse(0), desc.owt.getOrElse(""), desc.pdl.getOrElse(""), desc.tags, desc.intro)
    //
    //    updateService(desc.appkey, JsonHelper.jsonStr(appkeyReg));

  }

  def getPdl(appkey: String): Option[Pdl] = {
    //    val appkey = appkey
    val url = s"http://$hostname/service/$appkey/provider?type=1&env=3&pageNo=1&pageSize=20"
    val getReq = addHeaderAndCookie(url)
    val feature = Http(getReq > as.String)
    val content = Await.result(feature, timeout)
    val pdlMapCount = new util.HashMap[Pdl, Integer]()
    try {
      val providerNodes = (Json.parse(content) \ "data").asInstanceOf[JsArray].value.toSeq
      val hostnames = providerNodes.map {
        providerNode =>
          (providerNode \ "name").asOpt[String].getOrElse("")
      }

      hostnames.foreach {
        hostname =>
          val tags = OpsService.getHostTag(hostname).asScala
          tags.map {
            tag =>
              val arr_path = tag.split("&")
              var owt = "";
              var pdl = "";
              arr_path.foreach {
                path =>
                  if (path.startsWith("owt=")) {
                    owt = path.substring(4)
                  }
                  else if (path.startsWith("pdl=")) {
                    pdl = path.substring(4)
                  }
              }
              val pdlO = new Pdl(owt, pdl)
              var count = 0;
              if (pdlMapCount.containsKey(pdlO)) {
                count = pdlMapCount.get(pdlO)
              }
              pdlMapCount.put(pdlO, count + 1)
          }
      }

      pdlMapCount.asScala.toList.sortBy(-_._2) foreach {
        case (key, value) =>
          println(key + " = " + value)
      }

    } catch {
      case e: Exception =>
        LOG.error(s"error for ${url}")
        None
    }

    if (pdlMapCount.isEmpty) {
      None
    } else {
      Some(pdlMapCount.asScala.toList(0)._1)
    }
  }

  def addHeaderAndCookie(urlString: String) = {
    var result = url(urlString)
    result = result
//      .addCookie(new Cookie("skmtutc", "anXjmKK6IqOWzqWlwKU2MYHkBKBCvAVjRPLo+XhY3cGN5AblSMZAI/qD8mcxKB0P3mf/Xw7XvfCVRNcjv4/xhA==-n1AR+pA8p31RiXQggI0gPqZfVnE=",
//      "anXjmKK6IqOWzqWlwKU2MYHkBKBCvAVjRPLo+XhY3cGN5AblSMZAI/qD8mcxKB0P3mf/Xw7XvfCVRNcjv4/xhA==-n1AR+pA8p31RiXQggI0gPqZfVnE=", "oct.sankuai.com", "/", -1, 2000, false, true))
//      .addCookie(new Cookie("SID", "570th7p1bt26ksolu59n401164", "570th7p1bt26ksolu59n401164", "oct.sankuai.com", "/", -1, 2000, false, true))
//      .addCookie(new Cookie("misId", "hanjiancheng", "hanjiancheng", "oct.sankuai.com", "/", -1, 2000, false, true))
//      .addCookie(new Cookie("misId.sig", "R6t6zhbvKEhWCv0U1TVz6SuDRQk", "R6t6zhbvKEhWCv0U1TVz6SuDRQk", "oct.sankuai.com", "/", -1, 2000, false, true))
//      .addCookie(new Cookie("userId", "64137", "64137", "oct.sankuai.com", "/", -1, 2000, false, true))
//      .addCookie(new Cookie("userId.sig", "rTbO-Kqsm5A1-tTXf40dHyv7PiY", "rTbO-Kqsm5A1-tTXf40dHyv7PiY", "oct.sankuai.com", "/", -1, 2000, false, true))
//      .addCookie(new Cookie("userName", "%E9%9F%A9%E5%BB%BA%E6%88%90", "%E9%9F%A9%E5%BB%BA%E6%88%90", "oct.sankuai.com", "/", -1, 2000, false, true))
//      .addCookie(new Cookie("userName.sig", "oDdXTbNSdFWkL8A51ojyIOMsgmM", "oDdXTbNSdFWkL8A51ojyIOMsgmM", "oct.sankuai.com", "/", -1, 2000, false, true))
//      .addCookie(new Cookie("__mta", "149770889.1445523886869.1445523886869.1452239177404.2", "149770889.1445523886869.1445523886869.1452239177404.2", "oct.sankuai.com", "/", -1, 2000, false, true))
//      .addCookie(new Cookie("_ga", "GA1.2.1016736437.1445859649", "GA1.2.1016736437.1445859649", "oct.sankuai.com", "/", -1, 2000, false, true))
//      .addCookie(new Cookie("ssoid", ssoid, ssoid, "oct.sankuai.com", "/", -1, 2000, false, true))
    //      .addCookie(new Cookie("JSESSIONID", "4wrse2iy8ptk1uyw3eh3gjsv0", "4wrse2iy8ptk1uyw3eh3gjsv0", "oct.sankuai.com", "/", -1, 2000, false, true))
    result
  }

  test("getRegisterationLimited") {
    println(ServiceDesc.getRegisterationLimited)
  }

  /**
   * 同步appkey
   * &owt=hbdata
   * owt=ia
   * owt=flight
   * owt=fe&pdl=ios
   * owt=travel
   */
  test("sync appkey") {
    val owt_list = List("hbdata", "ia", "flight", "fe", "travel")
    val appkey_list = getAppkey()
    appkey_list.par.foreach {
      desc =>
        if (owt_list.contains(desc.owt.getOrElse("")) && !exitAppkey(desc.appkey)) {
          val owners: List[String] = desc.owners.map(_.login)
          val observers: List[String] = desc.observers match {
            case Some(obs) =>
              obs.map(_.login)
            case None =>
              List[String]()
          }
          val simpleDesc = DescSimple(Some(desc.name), desc.appkey, Some(desc.appkey), owners, observers, desc.intro, Some(desc.category),
            desc.business, desc.group, desc.base, desc.owt, desc.pdl, desc.level, desc.tags, desc.regLimit, desc.createTime)
          val map = Map("data" -> simpleDesc, "username" -> "hanjiancheng");
          updateService(desc.appkey, JsonHelper.jsonStr(map));
        }
    }
  }
  test("exitAppkey") {
    println(exitAppkey("com.sankuai.inf.msgp2"))
  }

  /**
   * 读取zk的数据 同步到数据库
   */

  test("sync db") {
    val prod_path = ServiceCommon.prodPath
    //    val stage_test = List(ServiceCommon.stagePath, ServiceCommon.testPath)
    val list = ZkClient.children(prod_path).asScala.filter(_.equals("com.sankuai.inf.leaf.wen"))
    //    val listPar = list.par
    //    listPar.tasksupport = new ForkJoinTaskSupport(new scala.concurrent.forkjoin.ForkJoinPool(16))
    list.foreach {
      appkey =>
        println(appkey)
        if ("com.sankuai.inf.leaf.wen".equals(appkey.trim)) {
          println("heloo")
        }
        val appkeyDBopt = AppkeyDescDao.get(appkey)
        println(appkeyDBopt)
        if (!appkeyDBopt.isDefined || (appkeyDBopt.isDefined && appkeyDBopt.get.createTime == 0L)) {
          println(appkey)
          val desc = ServiceDesc.zkDesc(appkey)
          AppkeyDescDao.insert(desc.toAppkeyDescRow)
        }


      //        stage_test.foreach {
      //          env_path =>
      //            val env_appkey = s"$env_path/$appkey"
      //            if (!zk.exist(env_appkey)) {
      //              val desc = ServiceDesc.zkDesc(appkey)
      //              println(env_appkey)
      ////              initEnvDesc(env_path, desc, getEnv(env_path).id)
      //            }
      //            ServiceCommon.subPaths.foreach{
      //              sub_path =>
      //                val path = List(env_path, appkey, sub_path).mkString("/")
      //                if(!path.contains("cellar") && !zk.exist(path)){
      //                  println(path)
      //                                if(sub_path == "route-http"){
      //                                  println(appkey)
      //                                  zk.client.create().creatingParentsIfNeeded().forPath(path)
      //                                  ServiceGroup.initHttpGroup(appkey, getEnv(env_path).id)
      //                                }
      //                }
      //            }
      //        }


    }

  }

  def getEnv(env_path: String) = {

    if (env_path.contains("stage")) {
      Env.stage
    }
    else if (env_path.contains("test")) {
      Env.test
    }
    else
      Env.prod

  }


  def initEnvDesc(rootPath: String, desc: Desc, env: Int) = {
    ZkClient.createWithParent(List(rootPath, desc.appkey).mkString("/"))
    // 服务子目录：desc,provider,consumer,route,config,quota,auth
    ServiceCommon.subPaths.foreach(subPath => ZkClient.client.create().creatingParentsIfNeeded().forPath(List(rootPath, desc.appkey, subPath).mkString("/")))
    // 设置desc信息

    ZkClient.setData(List(rootPath, desc.appkey, Path.desc).mkString("/"), Json.prettyPrint(Json.toJson(desc)))

    // 设置provider信息
    val provider = AppkeyTs(desc.appkey, System.currentTimeMillis() / 1000)
    ZkClient.setData(List(rootPath, desc.appkey, Path.provider).mkString("/"), Json.prettyPrint(Json.toJson(provider)))
    // 设置provider-http信息，Node Data保持与provider一致
    ZkClient.setData(List(rootPath, desc.appkey, Path.providerHttp).mkString("/"), Json.prettyPrint(Json.toJson(provider)))
    // 设置http-properties默认信息
    val defaultSharedHttpConfig = ServiceHttpConfig.getDefaultSharedHttpConfig(desc.appkey)
    ZkHlbClient.setData(List(rootPath, desc.appkey, Path.sharedHttpConfig).mkString("/"), Json.prettyPrint(Json.toJson(defaultSharedHttpConfig)))

    // 设置默认route信息
    if (CommonHelper.isOffline) {
      ServiceGroup.doDefaultGroup(desc.appkey, env, "disable")
      ServiceGroup.doDefaultMultiCenterGroup(desc.appkey, env, "disable")
    } else {
      ServiceGroup.doDefaultGroup(desc.appkey, env, "enable")
      ServiceGroup.doDefaultMultiCenterGroup(desc.appkey, env, "disable")
    }
    // 设置默认route-http信息
    ServiceGroup.initHttpGroup(desc.appkey, env)
  }

  test("delete appkey") {
    val appkeys = "com.sankuai.flight.eng.flightinfo,com.sankuai.hotel.goods.data.stagingf,com.sankuai.hotel.goods.data.ftec,com.sankuai.hotel.goods.biz.betab,com.sankuai.hotel.goods.open.stagingd,com.sankuai.hotel.goods.open.ftea,com.sankuai.hotel.goods.biz.stagingb,com.sankuai.hotel.goods.open.ftec,com.sankuai.meishi.eagle.stagepub,com.sankuai.travel.osg.mboxes,com.sankuai.sre.ops.influxdb,com.sankuai.hotel.goods.data.fteb,com.sankuai.hotel.goods.biz.fteb,com.sankuai.inf.leaf.wen,com.sankuai.hotel.goods.open.betad,com.sankuai.hotel.goods.data.beta,com.sankuai.hotel.goods.biz.test,com.sankuai.hotel.goods.data.stagingc,com.sankuai.hotel.goods.data.betac,deal-stock-process-service,com.sankuai.hotel.goods.data.dev,com.sankuai.lvyou.thd,search-indexer-wishshop";
    appkeys.split(",").foreach {
      appkey =>
        delProvider(appkey)
        val url = s"http://octo.test.sankuai.com/api/service/$appkey?login=hanjiancheng"
        val delReq = addHeaderAndCookie(url).addHeader("Content-Type", "application/json;charset=utf-8").DELETE
        val feature = Http(delReq > as.String)
        val content = Await.result(feature, timeout)
        val mess = (Json.parse(content) \ "isSuccess").as[Boolean]
        if (!mess) {
          //删除服务提供者节点，然后继续删除
          println(s"$appkey,$content")
        }
    }
  }

  def delProvider(appkey: String) = {
    val url = s"http://octo.test.sankuai.com/api/provider/delete"
    val map = Map("username" -> "hanjiancheng", "appkey" -> appkey)
    val json = JsonHelper.jsonStr(map)
    val delReq = addHeaderAndCookie(url).addHeader("Content-Type", "application/json;charset=utf-8").POST << json
    val feature = Http(delReq > as.String)
    val content = Await.result(feature, timeout)

    //删除服务提供者节点，然后继续删除
    println(s"$appkey,$content")

  }
}


