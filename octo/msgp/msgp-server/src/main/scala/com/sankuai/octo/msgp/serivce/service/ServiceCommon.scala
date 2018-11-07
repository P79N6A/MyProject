package com.sankuai.octo.msgp.serivce.service

import java.util
import java.util.Date
import java.util.concurrent.{Executors, TimeUnit}
import javax.servlet.http.Cookie

import com.fasterxml.jackson.databind.ObjectMapper
import com.sankuai.inf.octo.mns.util.ProcessInfoUtil
import com.sankuai.meituan.auth.util.UserUtils
import com.sankuai.meituan.auth.vo.{User => MUser}
import com.sankuai.meituan.borp.vo.ActionType
import com.sankuai.msgp.common.config.MsgpConfig
import com.sankuai.msgp.common.config.db.msgp.Tables.AppkeyAuth2Row
import com.sankuai.msgp.common.model.{User => _, _}
import com.sankuai.msgp.common.model.Path
import com.sankuai.msgp.common.service.org.{BusinessOwtService, OrgSerivce, UserService}
import com.sankuai.msgp.common.utils.client.Messager.{Alarm, MODE, mail, xm}
import com.sankuai.msgp.common.utils.client.{BorpClient, Messager, TairClient}
import com.sankuai.msgp.common.utils.helper.{AuthorityHelper, CommonHelper, JsonHelper}
import com.sankuai.msgp.common.utils.{HttpUtil, StringUtil, UserUtil}
import com.sankuai.octo.msgp.dao.appkey.AppkeyDescDao
import com.sankuai.octo.msgp.domain.{AppkeyDesc, _}
import com.sankuai.octo.msgp.model._
import com.sankuai.octo.msgp.serivce.AppkeyAuth
import com.sankuai.octo.msgp.serivce.monitor.MonitorConfig
import com.sankuai.octo.msgp.serivce.subscribe.{AppkeySubscribe, MonitorSubscribe, ReportSubscribe}
import com.sankuai.octo.msgp.utils._
import com.sankuai.octo.msgp.dao.monitor.{MonitorDAO, ProviderTriggerDao}
import com.sankuai.octo.msgp.dao.subscribe.AppkeySubscribeDAO
import com.sankuai.octo.msgp.serivce.service.ServiceConfig.timeout
import com.sankuai.octo.msgp.service.mq.OctoOwnersMafka
import com.sankuai.octo.msgp.utils.client._
import com.sankuai.octo.mworth.util.DateTimeUtil
import dispatch._
import org.apache.commons.lang.StringUtils
import org.joda.time.DateTime
import org.slf4j.{Logger, LoggerFactory}
import play.api.libs.json.{JsError, JsObject, JsString, JsValue, Json}

import scala.collection.JavaConverters._
import scala.collection.parallel.ForkJoinTaskSupport
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.concurrent.duration.Duration
import scala.util.parsing.json.JSONObject


// TODO 移除enabled标识
object ServiceCommon {
  val LOG: Logger = LoggerFactory.getLogger(ServiceCommon.getClass)

  val LIST_DESC_KEY = "MSGP_APPKEY_LIST"
  val OWNER_LOWER_LIMIT = 2
  val OWNER_UPPER_LIMIT = 12
  val sankuaiPath = Path.sankuaiPath
  val prodPath = List(sankuaiPath, Env.prod).mkString("/")
  val stagePath = List(sankuaiPath, Env.stage).mkString("/")
  val testPath = List(sankuaiPath, Env.test).mkString("/")
  val rootPaths = List(prodPath, stagePath, testPath)
  val subPaths = Path.values.map(_.toString).toList
  val env_desc = if (CommonHelper.isOffline) {
    "线下"
  } else {
    "线上"
  }
  val OCTO_URL = if (CommonHelper.isOffline) {
    "http://octo.test.sankuai.com"
  } else {
    "http://octo.sankuai.com"
  }

  private val timeout = Duration.create(20L, TimeUnit.SECONDS)
  private val alarmUserList = List("yangjie17", "shuchao02", "huixiangbo", "zhangyun16")
  private val adminList = List("caojiguang", "yangrui08", "zhangyun16")

  implicit val ec = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(4))

  private def getTaskSupport(count: Int) = if (count > 16) {
    new ForkJoinTaskSupport(new scala.concurrent.forkjoin.ForkJoinPool(16))
  } else if (count > 0) {
    new ForkJoinTaskSupport(new scala.concurrent.forkjoin.ForkJoinPool(count))
  } else {
    new ForkJoinTaskSupport(new scala.concurrent.forkjoin.ForkJoinPool(1))
  }

  case class ErrMsg(code: Int = SUCCESS, msg: String = "")

  val SUCCESS = 0
  val FAILURE = -1

  def exist(appkey: String): Boolean = {
    rootPaths.foldLeft(false) {
      (result, rootPath) =>
        val exitTemp = ZkClient.children(rootPath).asScala.foldLeft(false) {
          (ret, x) =>
            ret || x.equalsIgnoreCase(appkey)
        }
        result || exitTemp
    }
  }


  //检查三个环境下provider-http节点是否存在，不存在则创建
  def correctnessCheck4Http(): Boolean = {
    rootPaths.foreach(rootPath => {
      // 获取appKey List
      ServiceDesc.appsName().toList.foreach(
        appKey => {
          val providerHttpExist = ZkClient.exist(s"$rootPath/$appKey/${Path.providerHttp}")
          if (providerHttpExist) {
            val data = ZkClient.getData(s"$rootPath/$appKey/${Path.providerHttp}")
            LOG.info(s"[correctnessCheck4Http] 节点数据：$rootPath/$appKey/${Path.providerHttp}  Data: " + data.toString)
          } else {
            val providerData = ServiceModels.AppkeyTs(appKey, System.currentTimeMillis() / 1000)
            LOG.info(s"[correctnessCheck4Http] 创建节点：$rootPath/$appKey/${Path.providerHttp}  Data: " + providerData.toString)
            ZkClient.create(s"$rootPath/$appKey/${Path.providerHttp}", Json.prettyPrint(Json.toJson(providerData)))
          }
        }
      )
    })
    true
  }


  def initService(desc: ServiceModels.Desc): String = {
    // TODO use Env not rootPaths
    var result = ""
    try {
      rootPaths.foreach(rootPath => {
        // 根目录
        ZkClient.createWithParent(List(rootPath, desc.appkey).mkString("/"))
        // 服务子目录：desc,provider,consumer,route,config,quota,auth
        subPaths.foreach(subPath => ZkClient.client.create().creatingParentsIfNeeded().forPath(List(rootPath, desc.appkey, subPath).mkString("/")))
        // 设置desc信息
        if (!rootPath.equals(prodPath) && !CommonHelper.isOffline) {
          val regDesc = desc.copy(regLimit = 0)
          ZkClient.setDataWithEx(List(rootPath, desc.appkey, Path.desc).mkString("/"), Json.prettyPrint(Json.toJson(regDesc)))
        } else {
          //强制开启主机验证
          val regDesc = desc.copy(regLimit = 1)
          ZkClient.setDataWithEx(List(rootPath, desc.appkey, Path.desc).mkString("/"), Json.prettyPrint(Json.toJson(regDesc)))
        }
        // 设置provider信息
        val provider = ServiceModels.AppkeyTs(desc.appkey, System.currentTimeMillis() / 1000)
        ZkClient.setDataWithEx(List(rootPath, desc.appkey, Path.provider).mkString("/"), Json.prettyPrint(Json.toJson(provider)))
        // 设置provider-http信息，Node Data保持与provider一致
        ZkClient.setDataWithEx(List(rootPath, desc.appkey, Path.providerHttp).mkString("/"), Json.prettyPrint(Json.toJson(provider)))
        // 设置http-properties默认信息
        val defaultSharedHttpConfig = ServiceHttpConfig.getDefaultSharedHttpConfig(desc.appkey)
        ZkHlbClient.setDataWithEx(List(rootPath, desc.appkey, Path.sharedHttpConfig).mkString("/"), Json.prettyPrint(Json.toJson(defaultSharedHttpConfig)))
      })
      Env.values.foreach(env => {
        // 设置默认route信息
        if (CommonHelper.isOffline) {
          ServiceGroup.doDefaultGroup(desc.appkey, env.id, "disable")
          ServiceGroup.doDefaultMultiCenterGroup(desc.appkey, env.id, "disable")
        } else {
          ServiceGroup.doDefaultGroup(desc.appkey, env.id, "enable")
          ServiceGroup.doDefaultMultiCenterGroup(desc.appkey, env.id, "disable")
        }
        // 设置默认route-http信息
        ServiceGroup.initHttpGroup(desc.appkey, env.id)
      })
    } catch {
      case e: Exception => LOG.error(s"initService error appkey:$desc.appkey", e)
        result = "zk写入异常"
    }
    //写入数据库
    if (result.equals("")) {
      val regDesc = desc.copy(regLimit = 1)
      AppkeyDescDao.insert(regDesc.toAppkeyDescRow)
    }
    result
  }

  /**
    * 检查所有的appkey列表，返回节点确实的appkey
    */
  def checkMnsNode() = {
    rootPaths.flatMap { rootPath =>
      val childrens = ZkClient.children(rootPath).asScala
      val appkeyPar = childrens.par
      appkeyPar.tasksupport = getTaskSupport(16)
      childrens.flatMap {
        appkey =>
          val path = s"$rootPath/$appkey"
          val app_childen = ZkClient.children(path)
          val size = app_childen.size()
          if (size < 10) {
            println(s"$path,size:$size,children:$app_childen")
            Some(path)
          } else {
            None
          }
      }.toList
    }.asJava
  }

  def apps(): java.util.List[String] = {
    AppkeyDescDao.appsbyowt(None).asJava
  }

  def loadByUser(user_id: Int) = {
    AppkeyDescDao.appsbyuser(user_id)
  }


  def appsByOwt(owt: String) = {
    AppkeyDescDao.appsbyowt(Some(owt))
  }

  def appsByUser(): java.util.List[String] = {
    val user = UserUtils.getUser
    loadByUser(user.getId).asJava
  }

  def listService: List[ServiceModels.Desc] = {
    ServiceFilter.serviceTotal(new Page(1, Integer.MAX_VALUE))
  }


  def filterServiceListByPerm(list: List[ServiceModels.Desc], pdled: Boolean = true, user: MUser = UserUtils.getUser) = {
    val hasAuthApps = AppkeyAuth.getAppkeysByAuth(user, pdled)
    if (AuthorityHelper.isAdmin(user.getLogin)) {
      list
    } else {
      val listPar = list.par
      listPar.tasksupport = getTaskSupport(list.size)
      listPar.filter {
        x =>
          hasAuthApps.contains(x.appkey) // || AppkeyAuth.isOwtAdmin(x, user.getLogin)
      }.toList
    }
  }

  def listServiceByUser(pdled: Boolean = true): List[ServiceModels.Desc] = {
    filterServiceListByPerm(listService, pdled).sortBy(x => (-x.createTime.getOrElse(0L), x.appkey))
  }

  /**
    * 只提供给open api使用
    *
    * @param login
    * @return
    */

  def serviceByUser(login: String): List[ServiceModels.Desc] = {
    val list = ZkClient.children(prodPath).asScala.map(desc).sortBy(x => (-x.createTime.getOrElse(0L), x.appkey)).toList
    (if (AuthorityHelper.isAdmin(login)) {
      list
    } else {
      list.filter {
        x =>
          x.owners.map(_.login).contains(login)
      }
    }).sortBy(_.appkey)
  }

  def appsByUser(login: String): List[String] = serviceByUser(login).map(_.appkey)

  def owtByUser(user: com.sankuai.meituan.auth.vo.User) = {
    val appkeyPar = getAppkeysByUserId(user.getId).par
    appkeyPar.tasksupport = getTaskSupport(8)
    val list = appkeyPar.map {
      appkey =>
        desc(appkey)
    }.groupBy(_.owt).map {
      kv =>
        if (StringUtil.isBlank(kv._1.getOrElse(""))) {
          (kv._1, 0)
        } else {
          (kv._1, kv._2.length)
        }
    }.toList.sortBy(-_._2)
    if (list.isEmpty) {
      ""
    } else {
      list.head._1.getOrElse("")
    }
  }

  def searchRich(keyword: String, page: Page): List[ServiceModels.DescRich] = {
    search(keyword, page).map(_.toRich).sortBy(x => (-x.createTime.getOrElse(0L), x.appkey))
  }

  def search(keyword: String, page: Page): List[ServiceModels.Desc] = {
    val list = AppkeyDescDao.search(keyword, page)
    ServiceFilter.toDescs(list)
  }

  def search(keyword: String): List[ServiceModels.Desc] = {
    val list = listService
    list.filter {
      x =>
        keyword.split("\\s+").forall(
          key =>
            List(x.appkey, x.tags, x.intro, x.owner).foldLeft(false) {
              (matched, s) => matched || s.toLowerCase.contains(key.toLowerCase)
            })
    }.sortBy(x => (-x.createTime.getOrElse(0L), x.appkey))
  }

  def listService(page: Page, pdled: Boolean): List[ServiceModels.Desc] = {
    val list = listServiceByUser(pdled)
    page.setTotalCount(list.length)
    list.slice(page.getStart, page.getStart + page.getPageSize)
  }

  def listServiceRich(page: Page, pdled: Boolean): List[ServiceModels.DescRich] = {
    listService(page, pdled).map(_.toRich).toList.sortBy(x => (-x.createTime.getOrElse(0L), x.appkey))
  }

  def desc(appkey: String): ServiceModels.Desc = {
    val appDesc = AppkeyDescDao.get(appkey)
    if (appDesc.isDefined) {
      val result = ServiceFilter.toDesc(appDesc.get)
      //TODO 将线上所有的createtime为0的都设置成1414803600L（2014-11-01 09:00:00）
      if (result.createTime == Some(0))
        result.copy(createTime = Some(1414803600L))
      else
        result
    } else {
      LOG.info(s"appkey $appkey 不存在")
      ServiceModels.Desc(name = "", appkey = appkey, owners = List[ServiceModels.User](), intro = "", category = "", tags = "")
    }
  }

  def ownerStrByAppkey(appkey: String) = {
    val appDesc = AppkeyDescDao.get(appkey)
    if (appDesc.isDefined) {
      val desc = ServiceFilter.toDesc(appDesc.get)
      val list = desc.owners.map {
        x =>
          s"${x.name}(${x.login})"
      }.toList
      list.mkString(",")
    } else {
      ""
    }
  }

  def apiDesc(appkey: String): ServiceModels.Desc = {
    val data = desc(appkey)
    data
    //根据服务节点信息获取http,thrift
    //    val countlist = AppkeyProviderService.getProviderCount(appkey)
    //    var is_thrift = false;
    //    var is_http = false;
    //    countlist.foreach {
    //      counter =>
    //        if (counter._1.equals("thrift")) {
    //          is_thrift = true
    //        }
    //        else if (counter._1.equals("http")) {
    //          is_http = true
    //        }
    //    }
    //    val category = if (is_http && is_thrift) {
    //      "thrift,http"
    //    } else if (is_thrift) {
    //      "thrift"
    //    } else if (is_http) {
    //      "http"
    //    } else {
    //      ""
    //    }
    //    val apiDesc = data.copy(category = category)
    //    apiDesc
  }

  def isOwnerLogin(appkey: String, user: String) = {
    desc(appkey).owners.map(_.login).contains(user)
  }

  def isObserverLogin(appkey: String, user: String) = {
    desc(appkey).observers match {
      case Some(desc) =>
        desc.map(_.login).contains(user)
      case None =>
        false
    }
  }

  def getOwnersLogin(appkey: String) = {
    desc(appkey).owners.take(3).foldLeft(List[Object]()) {
      (result, self) =>
        result :+ Map("name" -> self.name, "login" -> self.login).asJava
    }.asJava
  }

  def getOwnersLoginWhole(appkey: String) = {
    desc(appkey).owners.map(_.login)
  }

  def getOwersId(appkey: String) = {
    desc(appkey).owners.map(_.id)
  }

  //写入数据库和zk
  def updateDesc(appkey: String, desc: ServiceModels.Desc): String = {
    LOG.info(s"##updateDesc appkey = $appkey , desc = $desc")
    var result = "";
    try {
      rootPaths.foreach(rootPath => {
        val descPath = List(rootPath, appkey, Path.desc).mkString("/")
        if (!rootPath.equals(prodPath) && !CommonHelper.isOffline) {
          val regDesc = desc.copy(regLimit = 0)
          ZkClient.setDataWithEx(descPath, Json.prettyPrint(Json.toJson(regDesc)))
        } else {
          ZkClient.setDataWithEx(descPath, Json.prettyPrint(buildZKJson(desc, descPath)))
        }
      })
    }
    catch {
      case e: Exception => LOG.error(s"appkey update error,$appkey,desc:${desc}", e)
        result = "zk写入失败"
    }
    if (result.equals("")) {
      AppkeyDescDao.insert(desc.toAppkeyDescRow)
    }
    result
  }

  def buildZKJson(desc: ServiceModels.Desc, descPath: String) = {
    val descZkStr = ZkClient.getData(descPath)
    LOG.info(s"desc json will change json= $descZkStr")
    val returnStr = if (descZkStr.contains("\"cell\"")) {
      val cellData = Json.parse(descZkStr).\("cell")
      val standardStr = Json.toJson(desc)
      //这里不处理会导致false带多个双引号
      addJsonKV(standardStr, "cell", cellData.asInstanceOf[JsString].value)
    } else {
      Json.toJson(desc)
    }
    LOG.info(s"desc json change result json= $returnStr")
    returnStr
  }

  def updateCellStatus(appkey: String, cellStatus: Boolean) = {
    LOG.info(s"## $appkey change cell status to $cellStatus")
    val descPath = List(prodPath, appkey, Path.desc).mkString("/")
    val oldDesc = ZkClient.getData(descPath)
    val descJson = Json.parse(oldDesc)
    var newDesc = if (oldDesc.contains("\"cell\"")) {
      addJsonKV(removeJsonKV(descJson, "cell"), "cell", cellStatus.toString)
    } else {
      addJsonKV(descJson, "cell", cellStatus.toString)
    }
    ZkClient.setDataWithEx(descPath, Json.prettyPrint(newDesc))
    BorpClient.saveOpt(actionType = ActionType.UPDATE.getIndex, entityId = appkey, entityType = EntityType.cellSwitch, newValue = cellStatus.toString)
  }

  def addJsonKV(jsValue: JsValue, key: String, value: String) = {
    val result = jsValue.as[JsObject] + (key, JsString(value))
    result
  }

  def removeJsonKV(jsValue: JsValue, key: String) = {
    val result = jsValue.as[JsObject] - (key)
    result
  }

  def isCellOpen(appkey: String) = {
    val descPath = List(prodPath, appkey, Path.desc).mkString("/")
    val descStr = ZkClient.getData(descPath)
    val result = if (descStr.contains("\"cell\"")) {
      Json.parse(descStr).\("cell").asInstanceOf[JsString].value.toBoolean
    } else {
      false
    }
    result
  }


  def getAppkeysByUserId(userId: Long) = AppkeyAuth.getAppkeysByUserId(userId).distinct

  def getIllKeyword() = MsgpConfig.get("registryIllKey", "").split(",").toList


  /**
    * 提供给hulk的删除接口
    *
    * @param shutDownIPs
    * @return
    */
  def batchDelProviderByIPs(shutDownIPs: ShutDownIPs) = {
    val user = UserService.bindUser(shutDownIPs.getUsername)
    LOG.info(s"hulk delete ips delete user : $user")
    val ips = shutDownIPs.getIps.asScala.toList
    //    val result = doShutdownIpsBy(shutDownIPs.getUsername, ips,shutDownIPs.getAppkey)
    val envs = Env.values.map(_.toString).toList
    val deleteIps = ips.filter(CommonHelper.checkIP).distinct
    val t1 = System.currentTimeMillis()
    val appkeyIps = getAppkeyByIp(deleteIps)
    val t2 = System.currentTimeMillis()
    LOG.info(s"###getAppkeyByIp cost time : ${t2 - t1} ms")
    val shutDownIp = appkeyIps.map {
      ip_appkeys =>
        val ip = ip_appkeys._1
        val appkeys = ip_appkeys._2
        //        if(appkeys.isEmpty){
        appkeys.add(shutDownIPs.getAppkey)
        //        }
        val list = doShutdownIpsByAppkeys(appkeys.asScala.toList, ip, envs, shutDownIPs.getUsername)
        ServiceModels.IpShutDown(ip, list)
    }
    val t3 = System.currentTimeMillis()
    LOG.info(s"###del appkey provider cost time : ${t3 - t2} ms")
    doShutdownAgent(ips, shutDownIPs.getUsername)
    val t4 = System.currentTimeMillis()
    LOG.info(s"###del sg agent provider cost time : ${t4 - t3} ms")
    JsonHelper.dataJson(shutDownIp)
  }


  /**
    * 校验用户名
    * 关闭机器节点，从所有 服务里，删除该机器
    *
    * @param req
    * @return
    */
  def doShutdownIps(req: ServiceModels.usernameIPs): List[ServiceModels.IpShutDown] = {
    UserService.bindUser(req.username)
    doShutdownAgent(req.ips, req.username)
    val data = doShutdownIpsBy(req.username, req.ips)
    val shutdownFalse = data.filter(_.appkeyShutDown.exists(_.shutdown == false))
    shutdownFalse

  }

  def doShutdownIpsBy(username: String, ips: List[String]) = {
    val envs = Env.values.map(_.toString).toList
    val deleteIps = ips.filter(CommonHelper.checkIP).distinct
    val appkeyIps = getAppkeyByIp(deleteIps)
    LOG.info(s"doShutdownIpsBy appkeyIps : $appkeyIps")
    val shutDownIp = appkeyIps.map {
      ip_appkeys =>
        val ip = ip_appkeys._1
        val appkeys = ip_appkeys._2
        val list = doShutdownIpsByAppkeys(appkeys.asScala.toList, ip, envs, username)
        ServiceModels.IpShutDown(ip, list)
    }
    shutDownIp
  }

  def doShutdownIpsByAppkeys(appkeys: List[String], deleteIp: String, envs: List[String], username: String): List[ServiceModels.AppkeyShutDown] = {
    if (appkeys.nonEmpty) {
      val time = System.currentTimeMillis()
      val appkeyPar = appkeys.par
      appkeyPar.tasksupport = getTaskSupport(appkeys.size)
      val data = appkeyPar.map {
        appkey =>
          val all_shutdown = envs.map {
            env =>
              val thriftDelete = doDelProvider(appkey, env.toString, true, username, deleteIp)
              val httpDelete = doDelProvider(appkey, env.toString, false, username, deleteIp)
              thriftDelete && httpDelete
          }
          ServiceModels.AppkeyShutDown(appkey, !all_shutdown.exists(_ == false))
      }.toList
      LOG.info(s"shutdown appkeys ${appkeys},ip:${deleteIp} envs:${envs},cost time:${System.currentTimeMillis() - time}")
      data
    } else {
      List[ServiceModels.AppkeyShutDown]()
    }
  }

  private def doShutdownAgent(deleteIps: List[String], username: String) = {
    val envs = Env.values.map(_.toString).toList
    val agent_appkeys = List(Appkeys.sgagent.toString, Appkeys.kmsagent.toString)
    val providerTypes = List(Path.provider, Path.providerHttp)
    agent_appkeys.foreach {
      appkey =>
        providerTypes.foreach {
          providerType =>
            val providerTypeStr = providerType.toString
            envs.foreach {
              env =>
                val providerPath = s"${Path.sankuaiPath}/$env/$appkey/$providerTypeStr"
                val deleteProviderPaths = deleteIps.map {
                  deleteIp =>
                    val port = ServicePorts.getPort(appkey)
                    s"$providerPath/$deleteIp:$port"
                }
                doDelProviderPaths(appkey, providerPath, deleteProviderPaths, username, false)
            }
        }
    }
  }


  def doDisableIpsByAppkeys(appkeys: List[String], deleteIps: List[String], envs: List[String], username: String) {
    if (appkeys.nonEmpty) {
      val time = System.currentTimeMillis()
      val appkeyPar = appkeys.par
      appkeyPar.tasksupport = getTaskSupport(appkeys.size)
      appkeyPar.foreach {
        appkey =>
          envs.foreach {
            env =>
              doDisableProviders(appkey, env.toString, true, username, deleteIps)
              doDisableProviders(appkey, env.toString, false, username, deleteIps)
          }
      }
      LOG.info(s"shutdown ip cost time:${System.currentTimeMillis() - time}")
    }
  }

  def doDisableIpsBy(username: String, ips: List[String]) = {
    val envs = Env.values.map(_.toString).toList
    val deleteIps = ips.filter(CommonHelper.checkIP).distinct
    val appkeyIps = getAppkeyByIp(deleteIps)
    appkeyIps.foreach {
      ip_appkeys =>
        val ip = ip_appkeys._1
        val appkeys = ip_appkeys._2
        doDisableIpsByAppkeys(appkeys.asScala.toList, List(ip), envs, username)
    }
  }


  private def doDelProvider(appkey: String, env: String, isThrift: Boolean, username: String, deleteIp: String) = {
    val providerPathStr = if (isThrift) {
      Path.provider
    } else {
      Path.providerHttp
    }

    val proctolStr = if (isThrift) {
      "thrift"
    } else {
      "http"
    }

    val providerPath = s"${Path.sankuaiPath}/$env/$appkey/$providerPathStr"
    val providers = ZkClient.children(providerPath)
    val providersPar = if (providers.isEmpty) {
      providers.asScala
    } else {
      val providersPar = providers.asScala.par
      providersPar.tasksupport = getTaskSupport(providers.size)
      providersPar
    }
    val deleteProviderPaths = providersPar.flatMap {
      ipPort =>
        if (ipPort.contains(s"$deleteIp:")) {
          Some(s"$providerPath/$ipPort")
        } else {
          None
        }
    }.toList
    LOG.info(s"doDelProvider appkey:$appkey,providerPath : $providerPath,username : $username")
    doDelProviderPaths(appkey, providerPath, deleteProviderPaths, username, true)
  }

  /**
    *
    * @param appkey              删除的appkey
    * @param providerPath        服务提供者节点的路径 ~/appkey/provider ,~/appkey/provider-http
    * @param deleteProviderPaths 服务提供者具体节点的路劲~/appkey/provider/ip:port
    * @param username            删除人
    * @param updateProviderPath  是否更新父级节点
    * @throws java.lang.Exception
    */
  private def doDelProviderPaths(appkey: String, providerPath: String, deleteProviderPaths: List[String], username: String, updateProviderPath: Boolean): Boolean = {
    try {
      var isEdit = false
      deleteProviderPaths.foreach {
        deleteProviderPath =>
          if (ZkClient.exist(deleteProviderPath)) {
            isEdit = true
            LOG.info(s"begin delProvider appkey:$appkey, path : $deleteProviderPaths")
            ZkClient.deleteProvider(deleteProviderPath)
            val msg = s"deletePath=$deleteProviderPath, operator=${username}"
            LOG.info(s"after delProvider appkey:$appkey,path : $deleteProviderPaths")
            BorpClient.saveOpt(actionType = ActionType.DELETE.getIndex, entityId = appkey, entityType = EntityType.shutdownMachine, newValue = msg)
          }
      }
      if (isEdit && updateProviderPath) {
        //更新下父节点的信息
        val apppkeyTs = ServiceModels.AppkeyTs(appkey, System.currentTimeMillis() / 1000)
        val providerData = Json.prettyPrint(Json.toJson(apppkeyTs))
        if (ZkClient.exist(providerPath)) {
          ZkClient.setData(providerPath, providerData)
        }
      }
      true
    }
    catch {
      case e: Exception =>
        LOG.error("机器下线异常", e)
        if (e.getMessage.contains("NoNode")) {
          true
        } else {
          val eventTime = DateTimeUtil.format(new Date(), DateTimeUtil.DATE_TIME_FORMAT);
          val message = s"OCTO服务节点下线(${env_desc})\n描述：机器下线异常," +
            s" appkey:${appkey}\ndeleteProviderPaths:${deleteProviderPaths}\n操作时间:$eventTime\n操作用户:${username}\n异常原因:${e.getMessage}"
          //方便以后处理添加报警人
          //        val owners = AppkeyAuth.getAppkeyOwner(appkey).flatMap {
          //          id =>
          //            val employee = OrgSerivce.employee(id.toInt)
          //            if (null == employee) {
          //              None
          //            } else {
          //              Some(employee.getLogin)
          //            }
          //        }
          val userList = List("zhangyun16")
          Messager.xm.send(userList, message)
          false
        }
    }
  }


  def batchDisableProviderByIPs(json: String) = {
    Json.parse(json).validate[ServiceModels.usernameIPs].fold({
      error =>
        LOG.error(error.toString)
        JsonHelper.errorJson("内部异常: " + JsError.toFlatJson(error).toString())
    }, {
      obj =>
        val userName = if (StringUtils.isNotEmpty(obj.username)) {
          obj.username
        } else {
          "unkown"
        }
        val ips = if (null == obj.ips) {
          List[String]()
        } else {
          obj.ips
        }
        if (ips.nonEmpty) {
          doDisableIps(ServiceModels.usernameIPs(userName, ips))
        }
        JsonHelper.dataJson("disable successfully")
    })
  }


  /**
    * 校验用户名
    * 关闭机器节点，从所有 服务里，删除该机器
    *
    * @param req
    * @return
    */
  def doDisableIps(req: ServiceModels.usernameIPs) = {
    UserService.bindUser(req.username)
    doDisableIpsBy(req.username, req.ips)
  }

  private def doDisableProviders(appkey: String, env: String, isThrift: Boolean, username: String, disableIps: List[String]) = {
    val providerPathStr = if (isThrift) {
      Path.provider
    } else {
      Path.providerHttp
    }

    val thrift_http = if (isThrift) {
      1
    } else {
      0
    }
    val proctolStr = if (isThrift) {
      "thrift"
    } else {
      "http"
    }

    val providerPath = s"${Path.sankuaiPath}/$env/$appkey/$providerPathStr"
    val providers = ZkClient.children(providerPath)
    val providersPar = if (providers.isEmpty) {
      providers.asScala
    } else {
      val providersPar = providers.asScala.par
      providersPar.tasksupport = getTaskSupport(providers.size)
      providersPar
    }
    val updateProviderPaths = providersPar.flatMap {
      ipPort =>
        disableIps.flatMap {
          disableIp =>
            if (ipPort.contains(s"$disableIp:")) {
              Some(s"$providerPath/$ipPort")
            } else {
              None
            }
        }
    }.toList
    doDisableProviderPaths(appkey, thrift_http, providerPath, updateProviderPaths, username)
  }

  /**
    *
    * @param appkey   禁用的appkey
    * @param username 禁用人
    * @throws java.lang.Exception
    */
  private def doDisableProviderPaths(appkey: String, thrift_http: Int, providerPath: String, disableProviderPaths: List[String], username: String) = {

    disableProviderPaths.foreach {
      disableProviderPath =>
        try {
          if (ZkClient.exist(disableProviderPath)) {
            val providerNode = AppkeyProviderService.getProviderNode(disableProviderPath).get
            val newStatus = Status.STOPPED.id
            val providerNodeEdit = providerNode.copy(enabled = Some(1), status = newStatus)
            val node = s"${providerNode.ip}:${providerNode.port}"
            AppkeyProviderService.updateProviderByType(appkey, thrift_http, node, providerNodeEdit)
            val msg = s"diablePath=$providerPath, operator=${username}"
            BorpClient.saveOpt(actionType = ActionType.UPDATE.getIndex, entityId = appkey, entityType = EntityType.disableProvider, newValue = msg)
          }
        }
        catch {
          case e: Exception =>
            LOG.error("禁用服务节点失败,节点不存在", e)
            sendDisableMessag(appkey, disableProviderPath)
        }
    }


  }

  private def sendDisableMessag(appkey: String, disableProviderPath: String): Unit = {
    val message = s"${env_desc}环境，禁用服务节点失败, appkey:${appkey}\n disableProviderPath:${disableProviderPath}"
    val owners = AppkeyAuth.getAppkeyOwner(appkey).flatMap {
      id =>
        val employee = OrgSerivce.employee(id.toInt)
        if (null == employee) {
          None
        } else {
          Some(employee.getLogin)
        }
    }
    Messager.xm.send(alarmUserList ::: owners, message)
  }


  def batchDelProviderByIPs(json: String) = {
    Json.parse(json).validate[ServiceModels.usernameIPs].fold({
      error =>
        LOG.error(error.toString)
        JsonHelper.errorJson("内部异常: " + JsError.toFlatJson(error).toString())
    }, {
      obj =>
        val userName = if (StringUtils.isNotEmpty(obj.username)) {
          obj.username
        } else {
          "unkown"
        }
        val ips = if (null == obj.ips) {
          List[String]()
        } else {
          obj.ips
        }
        if (ips.nonEmpty) {
          val shutDownList = doShutdownIps(ServiceModels.usernameIPs(userName, ips))
          if (shutDownList.nonEmpty) {
            JsonHelper.errorDataJson(shutDownList)
          } else {
            JsonHelper.dataJson("deleted successfully")
          }
        } else {
          JsonHelper.dataJson("deleted successfully")
        }

    })
  }

  def getServiceByIp(ips: String, status: Integer) = {
    val ip_list = ips.split(",").toList
    val ip_appkeys = getAppkeyByIp(ip_list)
    val filter_appkeys = if (status != null) {
      val data = ip_appkeys.map {
        x =>
          val ip = x._1
          val appkeys = x._2
          val filter_appkey = appkeys.asScala.filter {
            appkey =>
              filterService(appkey, ip, status)
          }
          (ip, filter_appkey.toList.asJava)
      }
      data
    } else {
      ip_appkeys
    }
    filter_appkeys.map {
      x => new IpAppkeys(x._1, x._2)
    }.asJava
  }

  def filterService(appkey: String, ip: String, status: Integer): Boolean = {
    val list = AppkeyProviderService.getProvdierBy(appkey, ip, status)
    list.nonEmpty
  }

  def delProviderByIP(ip: String, misid: String) = {
    if (StringUtils.isEmpty(ip)) {
      JsonHelper.errorJson("ip 不能为空")
    } else if (StringUtils.isEmpty(misid)) {
      JsonHelper.errorJson("misid 不能为空")
    } else {
      try {
        doShutdownIps(ServiceModels.usernameIPs(misid, List(ip)))
      }
      catch {
        case e: Exception =>
          LOG.error("删除服务节点失败ip:{},misid:{}", ip, misid, e)
          JsonHelper.errorJson("删除失败")
      }
      JsonHelper.dataJson("deleted successfully")
    }
  }


  def updateServiceUser(appkeyUser: AppkeyUser): String = {
    val user = UserService.bindUser(appkeyUser.getUsername)

    val desc = ServiceCommon.desc(appkeyUser.getAppkey)
    if (desc.owners.isEmpty) {
      JsonHelper.errorJson(s"appkey,${appkeyUser.getAppkey}不存在")
    } else {
      val str_owners = appkeyUser.getOwners.asScala
      var owners: List[ServiceModels.User] = List()
      str_owners.foreach {
        str_owner =>
          val employee = OrgSerivce.employee(str_owner)
          for (emp <- employee) {
            owners = owners :+ ServiceModels.User(emp.getId, emp.getLogin, emp.getName)
          }
      }
      if (owners.isEmpty) {
        owners = owners :+ ServiceModels.User(user.getId, user.getLogin, user.getName)
      }
      //      val megerObservers = desc.owners ::: desc.observers.getOrElse(List())
      val desc_edit = desc.copy(owners = owners)
      saveService(user, desc_edit, new Array[Cookie](1), false)
    }
  }

  def removeLeftUsers(appkeyUser: AppkeyUser): String = {
    val user = UserService.bindUser(appkeyUser.getUsername)
    val desc = ServiceCommon.desc(appkeyUser.getAppkey)
    if (desc.owners.length > 2 && (desc.owners.length - appkeyUser.getOwners.size()) > 1) {
      val newDesc = desc.copy(owners = desc.owners.filter(x => !appkeyUser.getOwners.contains(x.login)))
      saveService(user, newDesc, new Array[Cookie](1), false)
    } else {
      JsonHelper.errorJson("服务负责人数量小于2人，或者删除后小于1人，禁止删除!")
    }
  }

  /**
    * 暴露给前端接口,
    * 如果服务提供者为空,设置注册人为服务负责人
    */
  def saveService(appkeyReg: AppkeyReg, cookies: Array[Cookie]): String = {
    val cookies2 = if (cookies.length == 0) {
      val cookies3 = new Array[Cookie](1)
      val ssoid = MsgpConfig.get("configSsoid", "d722c5a90bfa401f8aeb5b7c1ec115fc")
      cookies3(0) = getCookie("ssoid", ssoid)
      cookies3
    } else {
      cookies
    }
    //    val cookies2 = new Array[Cookie](1)
    LOG.info(s"api注册服务,${appkeyReg.toString}")
    //    if(exist(appkeyReg.getData.getAppkey)){
    //      var result = "重复注册！"
    //      LOG.info(s"api注册，重复注册,${appkeyReg.toString}")
    //      result
    //    }else{
    val username = appkeyReg.getUsername
    //有时候会比较慢
    val user = UserService.bindUser(username)
    val simpleData = appkeyReg.getData
    val str_owners = simpleData.getOwners.asScala
    val str_observers = simpleData.getObservers.asScala
    var owners: List[ServiceModels.User] = List()
    str_owners.foreach {
      str_owner =>
        val employee = OrgSerivce.employee(str_owner)
        for (emp <- employee) {
          owners = owners :+ ServiceModels.User(emp.getId, emp.getLogin, emp.getName)
        }
    }
    if (owners.isEmpty) {
      owners = owners :+ ServiceModels.User(user.getId, user.getLogin, user.getName)
    }
    var observers: List[ServiceModels.User] = List()
    str_observers.foreach {
      str_observer =>
        val employee = OrgSerivce.employee(str_observer)
        for (emp <- employee) {
          observers = observers :+ ServiceModels.User(emp.getId, emp.getLogin, emp.getName)
        }
    }
    val baseApp = if (simpleData.getBase == Base.dianping.getId) {
      s"com.dianping.${
        simpleData.getAppkey.trim
      }".replace("-", ".")
    } else {
      simpleData.getAppkey.trim
    }
    val desc = toDesc(simpleData, owners, observers, baseApp)
    val edit_desc = if (simpleData.getBase == Base.dianping.getId) {
      desc.copy(business = Some(Business.shanghai.getId))
    } else {
      desc
    }
    try {
      val result = saveService(user, edit_desc, cookies2, true)
      //线上的同步到线下,多次重试
      if (!CommonHelper.isOffline) {
        syncRegAppkey(appkeyReg, 0)
      }
      result
    }
    catch {
      case e: Exception =>
        LOG.error("注册失败", e)
        Messager.xm.send(Seq("caojiguang@meituan.com", "tagye03@meituan.com", "yangrui08@meituan.com", "zhangyun16@meituan.com"),
          s"appkey注册失败\n${appkeyReg.toString}, localhost:${CommonHelper.getLocalIp}")
        throw e
    }
    //    }
  }

  private def syncRegAppkey(appkeyReg: AppkeyReg, count: Int) {
    val simpleDesc = appkeyReg.getData
    //增加线下注册强制开启主机验证的功能
    simpleDesc.setRegLimit(1)
    val reg = JsonHelper.jsonStr(appkeyReg)
    val data = HttpUtil.httpPostRequest("http://octo.test.sankuai.com/api/service/registry", reg)
    if (!JsonHelper.isValidJson(data) && count < 3) {
      LOG.info(s"offline registy error data is $data")
      Messager.sendSingleMessage("zhangyun16", Alarm("线下注册5xx error", s"注意${ProcessInfoUtil.getLocalIpV4}节点存在注册失败，登陆查看", ""), Seq(MODE.XM))
      syncRegAppkey(appkeyReg, count + 1)
    }
    LOG.info(s"线下同步$data")
  }


  private def toDesc(simpleDesc: SimpleDesc, owners: List[ServiceModels.User], observers: List[ServiceModels.User], baseApp: String) = {
    ServiceModels.Desc("", simpleDesc.getAppkey, Some(baseApp), owners, Some(observers), simpleDesc.getIntro, "",
      Some(0), Some(""), Some(simpleDesc.getBase), Some(simpleDesc.getOwt), Some(simpleDesc.getPdl),
      Some(0), simpleDesc.getTags, simpleDesc.getRegLimit, Some(0))
  }


  /**
    * 保存服务,registry,updateDesc 方法
    */
  def saveService(user: com.sankuai.meituan.auth.vo.User, appkeyDesc: AppkeyDesc, cookies: Array[Cookie]): String = {
    val owners = appkeyDesc.getOwners.asScala.map {
      x =>
        ServiceModels.User(x.getId, x.getLogin, x.getName)
    }.toList
    val observers = appkeyDesc.getObservers.asScala.map {
      x =>
        ServiceModels.User(x.getId, x.getLogin, x.getName)
    }.toList
    val baseApp = if (StringUtils.isNotBlank(appkeyDesc.getBaseApp)) {
      Some(appkeyDesc.getBaseApp)
    } else {
      None
    }
    val desc = ServiceModels.Desc(appkeyDesc.getName, appkeyDesc.getAppkey, baseApp,
      owners, Some(observers),
      appkeyDesc.getIntro, appkeyDesc.getCategory,
      Some(appkeyDesc.getBusiness), Some(appkeyDesc.getGroup),
      Some(appkeyDesc.getBase), Some(appkeyDesc.getOwt), Some(appkeyDesc.getPdl),
      Some(appkeyDesc.getLevel), appkeyDesc.getTags, appkeyDesc.getRegLimit, Some(appkeyDesc.getCreateTime))
    if ((desc.owners.size < OWNER_LOWER_LIMIT || desc.owners.size > OWNER_UPPER_LIMIT) && !CommonHelper.isOffline) {
      JsonHelper.errorJson("服务负责人数量介于2～12人之间，请确认数量")
    } else if (desc.owners.size < OWNER_LOWER_LIMIT && CommonHelper.isOffline) {
      JsonHelper.errorJson("服务负责人数量大于2人，请确认数量")
    } else {
      saveService(user, desc, cookies, false)
    }
  }

  def saveService(user: com.sankuai.meituan.auth.vo.User, desc: ServiceModels.Desc, cookies: Array[Cookie]): String = {
    saveService(user, desc, cookies, false)
  }

  private def appkeyIsValid(appkey: String, isApi: Boolean = false) = {
    val exp_w_d_dot = "^\\b[A-Za-z][A-Za-z0-9.]*$"
    val exp_prefix = "^com\\.(sankuai|meituan|dianping)\\..*$"

    if (StringUtils.isEmpty(appkey)) {
      ErrMsg(FAILURE, "appkey can not be empty.")
    } else if (!appkey.matches(exp_w_d_dot)) {
      ErrMsg(FAILURE, "appkey must start with a letter, and can only contain letter, number and dot.")
    } else if (!appkey.matches(exp_prefix)) {
      ErrMsg(FAILURE, "appkey must start with com.sankuai or com.meituan")
    } else if (appkey.contains("..") || appkey.endsWith(".")) {
      ErrMsg(FAILURE, "The key between two dots can not be empty, and appkey must not end with a dot.")
    }
    else if (!isApi) {
      val illKeys = getIllKeyword()
      illKeys.foldLeft(ErrMsg()) {
        (ret, item) =>
          if (appkey.contains(item)) {
            ErrMsg(FAILURE, s"appkey can not contain $item")
          } else {
            ret
          }
      }
    } else {
      ErrMsg()
    }
  }

  /*
    删除服务负责人
  *
   */
  def deleteOwnerDesc(user: com.sankuai.meituan.auth.vo.User) = {
    val list = getAppkeysByUserId(user.getId)
    LOG.info("员工离职，清理服务负责人，user:" + user + ",list:" + list)
    list.foreach {
      appkey =>
        val oldDesc = desc(appkey)
        val owners = oldDesc.owners.filter {
          owner =>
            !owner.login.equals(user.getLogin)
        }
        val newOwners = if (owners.isEmpty) {
          //删除服务负责人
          AppkeyAuth.delete(appkey, Auth.Level.ADMIN.getValue)
          //获取leader
          try {
            val day = DateTimeUtil.format(new Date(), DateTimeUtil.DATE_DAY_FORMAT)
            val headerId = OrgSerivce.getDirectHeader(user.getId, day)
            val header = OrgSerivce.employee(headerId)
            val use_head = ServiceModels.User(header.getId, header.getLogin, header.getName)
            List(use_head)
          } catch {
            case e: Exception =>
              LOG.error(s"get leader error appkey is $appkey and the old owner is ${user.getLogin}")
              val use_head = ServiceModels.User(91037, "inf.octo", "octo(inf.octo)")
              List(use_head)
          }
        } else {
          owners
        }
        val obsers = oldDesc.observers.getOrElse(List()).filter {
          obser =>
            !obser.login.equals(user.getLogin)
        }
        val new_desc = oldDesc.copy(owners = newOwners, observers = Some(obsers))
        saveAuth(oldDesc, new_desc)
        updateDesc(appkey, new_desc)
        ReportSubscribe.cancelReportSubscribe(user.getLogin, appkey)
        MonitorDAO.deleteTriggerSubscribeByLogin(user.getLogin)
        ProviderTriggerDao.deleteTriggerSubscribe(user.getId)
        BorpClient.saveOpt(user = user, actionType = ActionType.UPDATE.getIndex, entityId = new_desc.appkey, entityType = EntityType.updateServer,
          oldValue = Json.toJson(oldDesc).toString, newValue = Json.toJson(new_desc).toString)
    }

  }


  /**
    * 不允许api接口，修改服务负责人
    */
  def saveService(user: com.sankuai.meituan.auth.vo.User, desc: ServiceModels.Desc, cookies: Array[Cookie], isApi: Boolean): String = {
    val serviceDesc = desc.copy(
      appkey = desc.appkey.trim,
      baseApp = if (desc.baseApp.isEmpty) {
        Some(desc.appkey.trim)
      } else {
        desc.baseApp
      }
    )
    exist(serviceDesc.appkey) match {
      case true =>
        if (serviceDesc.owners.isEmpty) {
          LOG.error(s"appkey:${serviceDesc.appkey},服务负责人为空")
          JsonHelper.errorJson("服务负责人为空")
        } else {
          val oldDesc = ServiceCommon.desc(serviceDesc.appkey)
          val desc =
            serviceDesc.copy(business = if (serviceDesc.business.getOrElse(0) != Business.other.getId) {
              serviceDesc.business
            } else {
              oldDesc.business
            }, group = oldDesc.group, createTime = oldDesc.createTime,
              owners = if (serviceDesc.owt.getOrElse("").equals("waimai") && !serviceDesc.owners.contains(ServiceModels.User(2052137, "digger", "digger(digger)"))) {
                ServiceModels.User(2052137, "digger", "digger(digger)") :: serviceDesc.owners
              } else {
                serviceDesc.owners
              }
            )
          if (regLimitCheck(serviceDesc, oldDesc, user)) {
            //返回接结果
            var result: String = "";
            //更新服务描述
            result = updateDesc(desc.appkey, desc) //TODO zk判断是否成功，不成功抛出不写数据库 返回错误信息
            //设置权限
            if (result.equals("")) {
              saveAuth(oldDesc, desc)
              //订阅更新
              AppkeySubscribe.updateSubscribe(serviceDesc.appkey, serviceDesc.owners.map(_.login), oldDesc.owners.map(_.login))
              //强制验证主机列表通知
              regLimitAlarm(serviceDesc, oldDesc, user)
              BorpClient.saveOpt(user = user, actionType = ActionType.UPDATE.getIndex, entityId = desc.appkey, entityType = EntityType.updateServer,
                oldValue = Json.toJson(oldDesc).toString, newValue = Json.toJson(desc).toString)
              val descjson = JsonHelper.jsonStr(desc)
              OctoOwnersMafka.getInstance().sendAsyncMessage(descjson)
              JsonHelper.dataJson(desc)
            } else {
              JsonHelper.errorJson(result)
            }
          } else {
            JsonHelper.errorJson("非OCTO管理员和SRE, 禁止更改主机列表验证选项")
          }
        }
      case false =>
        val business = serviceDesc.business.getOrElse(BusinessOwtService.getBusiness(serviceDesc.owt.getOrElse("")))
        val oldDesc = ServiceModels.Desc(name = "", appkey = serviceDesc.appkey, owners = List[ServiceModels.User](), intro = "", category = "", tags = "")
        val desc = if (serviceDesc.owt.getOrElse("").equals("waimai")) {
          val diggerOwners = serviceDesc.owners ::: List(ServiceModels.User(2052137, "digger", "digger(digger)"))
          serviceDesc.copy(owners = diggerOwners, business = Some(business), createTime = Some(System.currentTimeMillis() / 1000))
        } else {
          serviceDesc.copy(business = Some(business), createTime = Some(System.currentTimeMillis() / 1000))
        }
        var result = initService(desc)
        if (result.equals("")) {
          saveAuth(oldDesc, desc)
          LOG.info(s"服务注册,${
            desc.appkey
          }")
          //订阅日报
          AppkeySubscribe.updateSubscribe(serviceDesc.appkey, desc.owners.map(_.login), List())

          BorpClient.saveOpt(user = user, actionType = ActionType.INSERT.getIndex, entityId = desc.appkey,
            entityType = EntityType.registerServer, newValue = Json.toJson(desc).toString)
          //异步添加空间到mtConfig
          ServiceConfig.addSpace(desc.appkey)
          // 发送注册通知邮件
          LOG.info(s"发送服务注册信息,${desc.appkey}")
          sendRegistry(desc)
          val descjson = JsonHelper.jsonStr(desc)
          OctoOwnersMafka.getInstance.sendAsyncMessage(descjson)
          JsonHelper.dataJson(desc)
        } else {
          JsonHelper.errorJson(result)
        }
    }
  }

  def sendRegistry(desc: ServiceModels.Desc) {
    //耗时较长所以加了异步
    Future {
      val user = if (UserUtils.getUser == null) "服务树" else UserUtils.getUser.getName
      val misID = if (UserUtils.getUser == null) "服务树" else UserUtils.getUser.getLogin
      //      val owners = desc.owners.map(x => s"${x.login}@meituan.com")
      //      val sysUsers = Seq("tangye03@meituan.com", "yangrui08@meituan.com")
      val domain = if (CommonHelper.isOffline) "octo.test.sankuai.com" else "octo.sankuai.com"
      val url = s"http://$domain/service/detail?appkey=${desc.appkey}"
      val onlineOfflineMsg = ServiceCommon.env_desc
      val now = DateTime.now().toString("yyyy-MM-dd HH:mm:ss")
      val msg = s"$now \n新服务注册($onlineOfflineMsg)\n服务标识：[${desc.appkey}|$url]\n描述：${desc.intro} \n注册人：$user($misID)，处理节点：${ProcessInfoUtil.getLocalIpV4}"
      Messager.mail.send(Seq("caojiguang@meituan.com","zhangyun16@meituan.com"), s"新服务注册：${desc.appkey}", msg)
    }(ec)
  }

  def regLimitAlarm(newDesc: ServiceModels.Desc, oldDesc: ServiceModels.Desc, user: com.sankuai.meituan.auth.vo.User) = {
    val newRegStatus = newDesc.regLimit
    val oldRegStatus = oldDesc.regLimit
    if (newRegStatus != oldRegStatus) {
      //管理员才可以修改
      if (oldRegStatus == 0 && newRegStatus == 1) {
        //开启了注册限制
        val usernames = oldDesc.owners.map(_.login)
        val message = s"为规范appkey的使用，已开启${newDesc.appkey}的强制验证主机列表功能，此功能对appkey已有provider节点无影响，" +
          s"若appkey在[OCTO日报异常服务统计|http://octo.sankuai.com/repservice/daily]中，请按照wiki及时修复，否则可能影响服务扩容([详细说明|https://123.sankuai.com/km/page/14790229])\n" +
          s"如有疑问，请咨询OCTO技术支持(infocto)"
        val alarm = Alarm("强制验证主机列表", message)
        Messager.sendXMAlarm(usernames, alarm)
      }
    }
  }

  def regLimitCheck(newDesc: ServiceModels.Desc, oldDesc: ServiceModels.Desc, user: com.sankuai.meituan.auth.vo.User) = {
    val newRegStatus = newDesc.regLimit
    val oldRegStatus = oldDesc.regLimit
    if (newRegStatus != oldRegStatus) {
      //管理员才可以修改
      if (AuthorityHelper.isAdmin(user.getLogin) || CommonHelper.isOffline) {
        true
      } else {
        false
      }
    } else {
      true
    }
  }

  def saveAuth(oldDesc: ServiceModels.Desc, desc: ServiceModels.Desc): Boolean = {
    //添加负责人权限
    var removeList = oldDesc.owners.toSet.diff(desc.owners.toSet).toList
    if (!removeList.isEmpty) {
      AppkeyAuth.delete(desc.appkey, removeList, Auth.Level.ADMIN.getValue)
      MonitorConfig.deleteProviderTrigger(desc.appkey, removeList)
      MonitorConfig.deletePerformanceTrigger(desc.appkey, removeList)
    }
    var addList = desc.owners.toSet.diff(oldDesc.owners.toSet).toList
    if (!addList.isEmpty) {
      val owners = if ("waimai".equals(desc.owt.getOrElse(""))) {
        ServiceModels.User(2052137, "digger", "digger(digger)") :: addList
      } else {
        addList
      }
      AppkeyAuth.insertAuth(desc.appkey, Auth.Level.ADMIN.getValue, addList)
      MonitorConfig.addProviderTrigger(desc.appkey, addList)
      MonitorConfig.addPerformanceTrigger(desc.appkey, addList)
      //订阅性能报警
    }

    //添加观察者权限
    //    AppkeyAuth.delete(desc.appkey, Auth.Level.OBSERVER.getValue)
    for (observers <- desc.observers) {
      val old_observers = oldDesc.observers.getOrElse().asInstanceOf[List[ServiceModels.User]]
      var removeList = old_observers.toSet.diff(observers.toSet).toList
      if (!removeList.isEmpty) {
        AppkeyAuth.delete(desc.appkey, removeList, Auth.Level.OBSERVER.getValue)
      }
      var addList = observers.toSet.diff(old_observers.toSet).toList
      if (!addList.isEmpty) {
        AppkeyAuth.insertAuth(desc.appkey, Auth.Level.OBSERVER.getValue, addList)
        //        MonitorConfig.addProviderTrigger(desc.appkey, addList)
      }

    }
    //添加部门权限
    AppkeyAuth.deleteOwt(desc.appkey, Auth.Level.OBSERVER.getValue)
    for (owt <- desc.owt) {
      AppkeyAuth.insertAuth(desc.appkey, Auth.Level.OBSERVER.getValue, List(ServiceModels.User(0, "owt", owt)))
    }
    true
  }


  def getCookie(name: String, value: String): Cookie = {
    val cookie: Cookie = new Cookie(name, value)
    cookie.setDomain("octo.sankuai.com")
    cookie.setVersion(0)
    cookie.setDomain("octo.sankuai.com")
    cookie.setPath("/")
    cookie
  }

  private def getSendUser(appkey: String): Seq[String] = {
    val desc = ServiceDesc.zkDesc(appkey)
    val owners = desc.owners
    owners.map(_.login)
  }

  def sendStatusMessage(appkey: String, message: String): Unit = {
    val users = getSendUser(appkey)
    Messager.xm.send(users, message)
  }

  def sendInfoToAdmin(message: String): Unit = {
    Messager.xm.send(adminList, message)
  }

  /**
    * 通过ip 获取相关的appkey
    *
    * @param ips
    * @return
    */
  def getAppkeyByIp(ips: List[String]) = {
    val mnsc = MnsCacheClient.getInstance
    val res = ips.map {
      ip =>
        try {
          val resp = mnsc.getAppkeyListByIP(ip)
          LOG.info(s"getAppkeyByIp resp : $resp")
          if (resp.code == com.sankuai.octo.mnsc.idl.thrift.model.Constants.SUCCESS) {
            (ip, resp.getAppKeyList)
          } else {
            (ip, new util.ArrayList[String]())
          }
        }
        catch {
          case e: Exception =>
            LOG.error(s"get getAppkeyByIp   $ip failed ", e)
            (ip, new util.ArrayList[String]())
        }
    }
    res
  }


  /*
 * 创建cellar 节点
 * 路径 /mns/sankuai/prod/${appkey}/providers/cellar
 */
  def createCellar(cellarNode: CellarNode) = {
    val appkey = cellarNode.getAppkey
    try {
      rootPaths.foreach(rootPath => {
        // 根目录
        val cellars = List(CellarPath.cellar_providers, CellarPath.cellar_routes)
        cellars.foreach {
          x =>
            val cellarPathExist = ZkClient.exist(s"$rootPath/$appkey/${x}")
            val providerData = ServiceModels.AppkeyTs(appkey, System.currentTimeMillis() / 1000)
            if (!cellarPathExist) {
              val msg = s"[create cellar node] 创建节点：$rootPath/$appkey/${x} Data: " + providerData.toString
              LOG.info(msg)
              ZkClient.create(s"$rootPath/$appkey/${x}", Json.prettyPrint(Json.toJson(providerData)))
              BorpClient.saveOpt(actionType = ActionType.INSERT.getIndex, entityId = appkey, entityType = EntityType.createCellar, newValue = msg)
            }
        }
      })
    }
    catch {
      case e: Exception => LOG.error(s"appkey create cellar error,$appkey", e)
        throw e
    }
  }

  private val SESSION_APPKEY_PREFIX: String = "OCTO_APPKEY_SESSION_"

  def getSessionAppkey: String = {
    val userId = UserUtil.getCurrentUserId
    var appkey: String = ""
    try {
      if (userId > 0) {
        appkey = TairClient.get(SESSION_APPKEY_PREFIX + userId).getOrElse("")
      }
    }
    catch {
      case e: Exception => {
        LOG.error("获取session 失败" + userId, e)
      }
    }
    return appkey
  }

  def putSessionAppkey(appkey: String) {
    if (StringUtil.isBlank(appkey)) {
      return
    }
    val userId = UserUtil.getCurrentUserId
    try {
      if (userId > 0) {
        TairClient.put(SESSION_APPKEY_PREFIX + userId, appkey)
      }
    }
    catch {
      case e: Exception => {
        LOG.error("添加session 失败" + userId, e)
      }
    }
  }

  def getDefaultAppkey(apps: java.util.List[String], appkey: String) = {
    var defaultAppkey = ""
    if (StringUtil.isBlank(appkey)) {
      defaultAppkey = getSessionAppkey
    } else {
      defaultAppkey = appkey
    }
    if (StringUtil.isBlank(defaultAppkey)) {
      defaultAppkey = if (apps.isEmpty) "" else apps.get(0)
    }
    defaultAppkey
  }

  def refreshOwtPdl() = {
    val apps = AppkeyDescDao.appkeys(-1, "-", "-")
    LOG.info(s"update no owt and pdl data size : ${apps.size}")
    var count = 0
    apps.foreach {
      appkey =>
        val app = AppkeyDescDao.get(appkey).get
        val owtpdl = getRemoteOwtPdl(appkey)
        val new_desc = app.copy(owt = owtpdl._1, pdl = owtpdl._2);
        AppkeyDescDao.insert(new_desc)
        count += 1
        LOG.info(s"do app: ${app.appkey} owt and pdl data is $owtpdl, now count is $count")
    }
    LOG.info("updated no owt and pdl data")
  }

  def getRemoteOwtPdl(appkey: String) = {
    try {
      val urlString = s"http://api.cmdb.dp/api/v0.1/cmdb?projects=$appkey&type=bu,product"
      val getReq = url(urlString)
      val feature = Http(getReq > as.String)
      val content = Await.result(feature, timeout)
      val owt = Json.parse(content).\("results").\(appkey).\("bu").\("ci_eng_name").asOpt[String].getOrElse("-") //owt
      val pdl = Json.parse(content).\("results").\(appkey).\("product").\("product_en_name").asOpt[String].getOrElse("-") //pdl
      (owt, pdl)
    } catch {
      case e: Exception => LOG.error(s"getRemoteOwtPdl error,$appkey", e)
        ("-", "-")
    }
  }

  //取消单个服务负责人或者关注人权限
  def convergenceOwnerObserver(user: com.sankuai.meituan.auth.vo.User, appkey: String, role: String) = {
    val desc = ServiceCommon.desc(appkey);
    val newDesc = if ("owner".equalsIgnoreCase(role)) {
      desc.copy(owners = desc.owners.filter(x => x.login != user.getLogin))
    } else {
      desc.copy(observers = Some(desc.observers.get.filter(x => x.login != user.getLogin)))
    }
    saveService(user, newDesc, new Array[Cookie](1), false)
  }


  /**
    * 取消一些离职人员的报警订阅
    */
  def clearSubscribe() = {
    val list = ProviderTriggerDao.getSubscribeUserDistinct();
    var count = 0
    LOG.info(s"###distinct username count : ${list.size}")
    list.foreach {
      u =>
        OrgSerivce.employee(u) match {
          case Some(e) =>
            if (e.getStatus == 1) {
              count += 1
              LOG.info(s"left staff : $e")
              ProviderTriggerDao.deleteTriggerSubscribe(e.getId)
              MonitorDAO.deleteTriggerSubscribeByLogin(e.getLogin)
            }
          case None =>
            LOG.info(s"None staff : $u")
        }
    }
    LOG.info(s"###left user count : $count")
  }

  def clearObservers() = {
    val clearList = "baishaohua,chenbaisheng,dingjianlin,fanfan.yang.sh,fenglong,gaoyuxun,guoyingbo,hongdan,huangxiaolu,huguochao,hujun06,huxia,liubiqian,liucheng06,lixiaoguang04,lixiaotong,lubin.shi,pengjunyu,puxiaoxiao,qiaoxin,shijiashuo,sunfeifei,sunshengzhi02,tangweizhong,wangjing25,wangpo,wuxiaoyu,xieyixuan,xufanghua,yangfan32,yangshengcai,zhangchi11,zhangkaisheng,zhangmengyao03,zhangshaoxiong,zhaoqian10,zhulei07,zuopucun"
    val list = clearList.split(",")
    list.foreach {
      u =>
        val employee = OrgSerivce.employee(u).get
        var user: com.sankuai.meituan.auth.vo.User = new com.sankuai.meituan.auth.vo.User
        user.setId(employee.getId)
        user.setLogin(employee.getLogin)
        user.setName(employee.getName)
        val appkeyList = AppkeyAuth.getAppkeyByObservers(user)
        var count = 0
        val appkeyListPar = appkeyList.par
        appkeyListPar.tasksupport = new ForkJoinTaskSupport(new scala.concurrent.forkjoin.ForkJoinPool(16))
        appkeyListPar.foreach {
          appkey =>
            convergenceOwnerObserver(user, appkey, "observers")
            count += 1
            LOG.info(s"当前处理：$u,需要处理的总数是 ${appkeyList.size},当前处理次数是：$count")
        }
    }
  }

}
