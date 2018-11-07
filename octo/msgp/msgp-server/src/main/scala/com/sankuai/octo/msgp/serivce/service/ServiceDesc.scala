package com.sankuai.octo.msgp.serivce.service

import java.util.concurrent.{CountDownLatch, Executors}

import com.sankuai.meituan.auth.util.UserUtils
import com.sankuai.meituan.borp.vo.ActionType
import com.sankuai.msgp.common.config.MsgpConfig
import com.sankuai.msgp.common.config.db.msgp.Tables.{AppkeyAbandonedRow, AppkeyDescRow}
import com.sankuai.msgp.common.model._
import com.sankuai.msgp.common.service.hulk.HulkApiService
import com.sankuai.msgp.common.service.org.{OpsService, SsoService}
import com.sankuai.msgp.common.utils.client.{BorpClient, Messager}
import com.sankuai.msgp.common.utils.helper.JsonHelper
import com.sankuai.octo.msgp.dao.appkey.{AppkeyAbandonedDao, AppkeyDescDao, AppkeyProviderDao}
import com.sankuai.octo.msgp.serivce.AppkeyAuth
import com.sankuai.octo.msgp.serivce.monitor.MonitorConfig
import com.sankuai.octo.msgp.utils.client.ZkClient
import org.apache.commons.lang3.StringUtils
import org.joda.time.DateTime
import org.slf4j.{Logger, LoggerFactory}
import play.api.libs.json.Json

import scala.collection.JavaConverters._
import scala.collection.mutable.ListBuffer
import scala.collection.parallel.ForkJoinTaskSupport

/**
  * Created by zava on 16/5/10.
  * 对服务的编辑修改查询
  */
object ServiceDesc {
  val LOG: Logger = LoggerFactory.getLogger(ServiceDesc.getClass)
  private val taskThreadPool = new ForkJoinTaskSupport(new scala.concurrent.forkjoin.ForkJoinPool(4))

  private val POOL_SIZE = 10
  val executorthreadPool = Executors.newFixedThreadPool(POOL_SIZE)

  //校验是否可以删除
  case class AppKeyCheck(deleted: Boolean, message: String)

  val sankuaiPath = "/mns/sankuai"
  val prodPath = List(sankuaiPath, Env.prod).mkString("/")
  val stagePath = List(sankuaiPath, Env.stage).mkString("/")
  val testPath = List(sankuaiPath, Env.test).mkString("/")
  val rootPaths = List(prodPath, stagePath, testPath)
  val MAX_SIZE = 300

  /**
    * 删除节点
    *
    * @param appkey
    * @return
    */
  def delete(appkey: String, username: String) = {
    try {
      val blockDeletion = MsgpConfig.get("block.delete.appkey", "true").toBoolean
      if (blockDeletion) {
        JsonHelper.dataJson("删除成功")
      } else {
        val deleteChecked = deleteCheck(appkey)
        if (deleteChecked.deleted) {
          val serviceDesc = ServiceCommon.desc(appkey)
          rootPaths.foreach(rootPath => {
            ZkClient.deleteWithChildren(List(rootPath, appkey).mkString("/"))
          })
          ServiceConfig.delSpace(appkey)
          AppkeyAuth.delete(appkey)

          //记录被删除的Appkey描述信息
          val appkeyDescRowOpt = AppkeyDescDao.get(appkey)
          appkeyDescRowOpt match {
            case Some(desc) =>
              AppkeyAbandonedDao.insert(AppkeyAbandonedRow(0, desc.name, desc.base, desc.appkey, desc.baseapp, desc.owners,
                desc.observers, desc.pdl, desc.owt, desc.intro, desc.tags, desc.business, desc.category, desc.reglimit, username,
                System.currentTimeMillis() / 1000))
            case None => None
          }
          AppkeyDescDao.delete(appkey)

          //记录操作日志，构造特殊键供查询
          BorpClient.saveOpt(actionType = ActionType.DELETE.getIndex, entityId = appkey, entityType = EntityType.deleteServer, oldValue = Json.toJson(serviceDesc).toString)
          sendDelete(serviceDesc.appkey)
          //通知ops
          val opsAppkey = OpsService.getOpsAppkey(appkey)
          for (appkeySrv <- opsAppkey.data) {
            if (appkeySrv.appkey.contains(appkey)) {
              val del_appkeySrv = appkeySrv.copy(appkey = List(appkey))
              OpsService.deleteOpsAppkey(del_appkeySrv)
            }
          }
          MonitorConfig.deleteProviderTrigger(appkey)
          //删除服务提供者
          AppkeyProviderDao.delete(appkey)
          LOG.info(s"删除服务$appkey")
          JsonHelper.dataJson("删除成功")
        } else {
          JsonHelper.errorJson(deleteChecked.message)
        }
      }
    } catch {
      case e: Exception =>
        LOG.error(s"service delete failed ${appkey}", e)
        JsonHelper.errorJson("删除失败")
    }
  }

  def sendDelete(appkey: String) {
    val user = if (UserUtils.getUser == null) "系统" else UserUtils.getUser.getName
    val misID = if (UserUtils.getUser == null) "系统" else UserUtils.getUser.getLogin
    val onlineOfflineMsg = ServiceCommon.env_desc
    val now = DateTime.now().toString("yyyy-MM-dd HH:mm:ss")
    val msg = s"$now \n服务删除($onlineOfflineMsg)\n服务标识：$appkey \n删除者：$user($misID)"
    val alertUserSeq = Seq("caojiguang@meituan.com", "zhangyun16@meituan.com")
    Messager.mail.send(Seq("zhangyun16@meituan.com"), s"服务删除：$appkey", msg)
    Messager.xm.send(alertUserSeq, msg)
  }

  /**
    * 删除服务校验，不能有服务提供者，hulk容器
    *
    * @param appkey
    * @return
    */
  private def deleteCheck(appkey: String): AppKeyCheck = {
    var appKeyCheck = AppKeyCheck(true, "可以删除")
    val hasProvider = AppkeyProviderService.hasProvider(appkey)
    if (hasProvider) {
      appKeyCheck = AppKeyCheck(false, "服务还有提供者，无法删除")
    } else {
      val hulkCheck = HulkApiService.checkDeleteAppkey(appkey)
      if (hulkCheck.isDefined && hulkCheck.getOrElse(HulkApiService.HulkAppKeyCheck(0, "")).errorCode == 1) {
        appKeyCheck = AppKeyCheck(false, "服务还有Docker容器配置，无法删除")
      }
    }
    appKeyCheck
  }

  /**
    *
    * 添加关注者
    */
  def observer(appkey: String, user: com.sankuai.meituan.auth.vo.User): String = {
    try {
      val oldDesc = ServiceCommon.desc(appkey)
      //api不能修改服务负责人
      val observersOpt = oldDesc.observers
      var observers: List[ServiceModels.User] = List()
      val employee = SsoService.getUser(user.getLogin)
      for (emp <- employee) {
        observers = observersOpt.getOrElse(List()) :+ ServiceModels.User(emp.getId, emp.getLogin, emp.getName)
      }

      val newDesc = oldDesc.copy(observers = Some(observers))
      ServiceCommon.saveAuth(oldDesc,newDesc)
      ServiceCommon.updateDesc(appkey, newDesc)
      BorpClient.saveOpt(user = user, actionType = ActionType.UPDATE.getIndex, entityId = appkey, entityType = EntityType.updateServer,
        oldValue = Json.toJson(oldDesc).toString, newValue = Json.toJson(newDesc).toString)

      JsonHelper.dataJson(newDesc)
    } catch {
      case e: Exception => LOG.error("", e)
        JsonHelper.dataJson("服务订阅异常")
    }
  }


  /** 只获取appkey的名字，其他信息不获取 */
  def appsName() = {
    AppkeyDescDao.apps()
  }


  /**
    *
    * 获取所有thirf服务的数据
    */
  def apphosts() = {
    val list = ZkClient.children(prodPath).asScala.filter {
      appkey =>
        !appkey.equals("com.sankuai.inf.sg_agent") && !appkey.equals("com.sankuai.inf.kms_agent")
    }
    val listPar = list.par
    listPar.tasksupport = taskThreadPool
    val providers = listPar.map {
      appkey =>
        val providerPath = List(sankuaiPath, Env.prod, appkey, Path.provider).mkString("/")
        val providerNodeList = ZkClient.children(providerPath).asScala
        val prodNodes = providerNodeList.flatMap { node => {
          try {
            val data = ZkClient.getData(s"$providerPath/$node")
            Json.parse(data).validate[ServiceModels.ProviderNode].asOpt.map { x => x }
          } catch {
            case e: Exception => LOG.error("", e); None
          }
        }
        }.toList
        val ips = prodNodes.filter(_.status == Status.ALIVE.id).map {
          _.ip
        }.distinct
        ServiceModels.AppkeyIps(appkey, ips)
    }.toList
    val data = providers.filter(_.ips.size > 0)
    data
  }


  def exit(appkey: String) = {
    ZkClient.exist(List(prodPath, appkey, Path.desc).mkString("/"))
  }

  def zkDesc(appkey: String): ServiceModels.Desc = {
    try {
      val data = ZkClient.getData(List(prodPath, appkey, Path.desc).mkString("/"))
      LOG.debug(data)
      val result = Json.parse(data).validate[ServiceModels.Desc].get
      //TODO 将线上所有的createtime为0的都设置成1414803600L（2014-11-01 09:00:00）
      if (result.createTime == Some(0))
        result.copy(createTime = Some(1414803600L))
      else
        result
    } catch {
      case e: Exception => LOG.info(s"appkey $appkey 不存在", e)
        ServiceModels.Desc(name = "", appkey = appkey, owners = List[ServiceModels.User](), intro = "", category = "", tags = "")
    }
  }


  def zkListService: List[ServiceModels.Desc] = {
    val appkeyList = ZkClient.children(prodPath).asScala
    val result = scala.collection.mutable.ArrayBuffer[ServiceModels.Desc]()
    val latch = new CountDownLatch(appkeyList.length)
    appkeyList.foreach(
      appkey => {
        executorthreadPool.submit(new Runnable {
          override def run(): Unit = {
            val appDesc = zkDesc(appkey)
            result.synchronized {
              result += appDesc
            }
            latch.countDown()
          }
        })
      }
    )
    latch.await()
    val list = result.toList.sortBy(x => (-x.createTime.getOrElse(0L), x.appkey))
    list
  }

  /**
    * 刷新zk的数据到数据库，
    * 数据库多余的数据删除
    */
  def refresh(appkey: String) = {
    if (StringUtils.isNotBlank(appkey)) {
      val app = zkDesc(appkey)
      if (app.owners.nonEmpty) {
        val app_desc = app.toAppkeyDescRow
        AppkeyDescDao.insert(app_desc)
      } else {
        deleteApp(appkey)
      }
    } else {
      val list = zkListService
      saveService(list)
      val db_appkey_list = AppkeyDescDao.apps()
      db_appkey_list.map {
        appkey =>
          if (!exit(appkey)) {
            deleteApp(appkey)
          }
      }
    }
  }


  def deleteApp(appkey: String) = {
    AppkeyDescDao.delete(appkey)
    AppkeyAuth.delete(appkey)
  }

  /**
    * TODO delete 注册前端双写
    *
    * @param serviceList
    * @return
    */
  def saveService(serviceList: List[ServiceModels.Desc]) = {
    val listBuffer = ListBuffer[AppkeyDescRow]()
    serviceList.foreach {
      x =>
        val desc = x.toAppkeyDescRow
        listBuffer.append(desc)
        if (listBuffer.size > MAX_SIZE) {
          AppkeyDescDao.batchInsert(listBuffer.toList)
          listBuffer.clear()
        }
    }
    if (listBuffer.nonEmpty) {
      AppkeyDescDao.batchInsert(listBuffer.toList)
    }
  }

  /**
    * 获取开启了服务
    *
    * @return
    */
  def getRegisterationLimited = {
    val list = AppkeyDescDao.getRegisterationLimited
    list
  }

  /**
    * 获取服务的appkey和owt
    */
  def getAllAppkeyOwt = AppkeyDescDao.getAllAppkeyOwt

}
