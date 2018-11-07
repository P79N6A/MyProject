package com.sankuai.octo.msgp.serivce.sgagent

import com.sankuai.meituan.auth.util.UserUtils
import com.sankuai.meituan.borp.vo.ActionType
import com.sankuai.msgp.common.config.{DbConnection, MsgpConfig}
import com.sankuai.msgp.common.config.db.msgp.Tables._
import com.sankuai.msgp.common.model._
import com.sankuai.msgp.common.service.org.OpsService
import com.sankuai.msgp.common.utils.client.{BorpClient, Messager}
import com.sankuai.msgp.common.utils.helper.{CommonHelper, JsonHelper}
import com.sankuai.octo.msgp.model._
import com.sankuai.octo.msgp.serivce.service.{AppkeyProviderService, ServiceCommon}
import com.sankuai.msgp.common.utils.client.Messager.{Alarm, MODE, xm}
import com.sankuai.octo.msgp.utils.client.ZkClient
import com.sankuai.sgagent.thrift.model.SGAgent
import org.apache.thrift.protocol.{TBinaryProtocol, TProtocol}
import org.apache.thrift.transport.{TFramedTransport, TSocket, TTransport}
import org.joda.time.DateTime
import org.slf4j.LoggerFactory
import play.api.libs.json.Json

import scala.collection.JavaConverters._
import scala.collection.mutable.ArrayBuffer
import scala.slick.driver.MySQLDriver.simple._

object SgAgentSwitchEnv {
  private val LOG = LoggerFactory.getLogger(SgAgentSwitchEnv.getClass)
  private val db = DbConnection.getPool
  private val sankuaiPath = "/mns/sankuai"

  case class ErrMsg(code: Int = SUCCESS, msg: String = "")

  case class SwitchenvItem(id: Long, applyMisid: Option[String] = None, applyTime: Option[Long] = None, ip: Option[String] = None, oldEnv: Option[String] = None, newEnv: Option[String] = None, comfirmMisid: Option[String] = None, comfirmTime: Option[Long] = None, flag: Option[Int] = Some(0), note: Option[String] = Some(""), cluster: String = "", hostName: String = "")

  //  case class hostInfo(ip:String,hostName:String,env:String,provider:List[ProviderNode])

  val SUCCESS = 0
  val FAILURE = -1

  def getApplyListByUser(page: Page) = {
    val user = UserUtils.getUser
    val misID = user.getLogin
    db withSession {
      implicit session: Session =>
        page.setTotalCount(Switchenv.filter(_.applyMisid === misID).length.run)
        val list = Switchenv.filter(_.applyMisid === misID).sortBy(_.applyTime.desc).drop(page.getStart).take(page.getPageSize).list
        val res = list.map {
          x =>
            x.copy(newEnv = Some(EnvMap.getAliasEnv(x.newEnv.getOrElse(""))), oldEnv = Some(EnvMap.getAliasEnv(x.oldEnv.getOrElse(""))))
        }
        res.asJava
    }
  }

  private def checkIPForApply(ip: String, newEnv: String): ErrMsg = {
    if (!CommonHelper.isOffline) {
      //线上情况，则检测其在OPS上的TAG
      val checkOpsStr = MsgpConfig.get("switchEnv.checkOpsCluster", "false").trim
      if ("true" equals checkOpsStr) {
        //启用OPS检测
        val opsEnv = OpsService.getHostEnvTag(ip)
        opsEnv match {
          case x: String =>

            /**
             * 以下逻辑实现：
             * 1.stag机器(包含stage和staging)只能切换成stage
             * 2.test机器只能切换成test
             * 3.其他不限制。
             * 【NOTE】在环境切换审核时，可以拒绝。
             */
            if (x.contains("stag") && (!newEnv.equals("stage"))) {
              return ErrMsg(FAILURE, s"该主机的tag为$opsEnv,不能将其切换成$newEnv")
            }
            if (x.contains("test") && (!newEnv.equals("test"))) {
              return ErrMsg(FAILURE, s"该主机的tag为$opsEnv,不能将其切换成$newEnv")
            }
          case None =>
            return ErrMsg(FAILURE, "查询不到该主机的相关信息")
        }
      }

      val checkISAuth = MsgpConfig.get("switchEnv.checkIPInProvider", "false")
      if ("true" equals checkISAuth) {
        if (!isIPAuth(ip)) {
          return ErrMsg(FAILURE, "对不起，您无权限切换该主机的环境")
        }
      }
    }

    val checkenv = checkIPEnv(ip, newEnv)
    if (FAILURE == checkenv.code) {
      return checkenv
    }
    ErrMsg()
  }

  private def updateRecordForApply(ip: String, oldEnv: String, newEnv: String): ErrMsg = {
    val currentTime = System.currentTimeMillis

    val user = UserUtils.getUser
    val misID = user.getLogin
    val row = SwitchenvRow(0, Some(misID), Some(currentTime), Some(ip), Some(oldEnv), Some(newEnv))
    db withSession {
      implicit session: Session =>

        var isExist = false
        var updateID: Long = 1L
        val otherMis = new ArrayBuffer[String]

        val list = Switchenv.filter { x => (x.ip === ip) && (x.oldEnv === oldEnv) && (x.flag === 0) }.list
        list.foreach {
          y =>
            if (!isExist) {
              if (y.applyMisid.get == misID) {
                if (y.newEnv.get == newEnv) {
                  return ErrMsg(FAILURE, "您已经申请过该主机环境切换，请等待审核")
                } else {
                  updateID = y.id
                  isExist = true
                }
              } else {
                otherMis += y.applyMisid.get
              }
            }
        }

        if (isExist) {
          Switchenv.filter(_.id === updateID).map(x => (x.newEnv, x.applyTime)).update(Some(newEnv), Some(currentTime))
          BorpClient.saveOpt(actionType = ActionType.INSERT.getIndex, entityId = ip, entityType = EntityType.applySwitch, newValue = newEnv, oldValue = oldEnv)
          sendApplyXM(ip, oldEnv, newEnv)
          return ErrMsg(SUCCESS, "申请成功")
        }
        if (otherMis.nonEmpty) {
          return ErrMsg(FAILURE, "该主机已经被(" + otherMis.mkString(",") + ")申请过环境切换")
        }
        Switchenv += row
    }
    sendApplyXM(ip, oldEnv, newEnv)
    ErrMsg(SUCCESS, "申请成功")
  }


  private def sendApplyXM(ip: String, oldEnv: String, newEnv: String) = {
    val user = if (UserUtils.getUser == null) "系统" else UserUtils.getUser.getName
    val misID = if (UserUtils.getUser == null) "系统" else UserUtils.getUser.getLogin
    val alertUserSeq = Seq("yangjie17@meituan.com", "zhangcan02@meituan.com", "zhangxi@meituan.com", "zhangchi11@meituan.com")
    val domain = if (CommonHelper.isOffline) "octo.test.sankuai.com" else "octo.sankuai.com"
    val url = s"http://$domain/manage/agent/tabNav#agentSwitchEnv"
    val onlineOfflineMsg = ServiceCommon.env_desc
    val now = DateTime.now().toString("yyyy-MM-dd HH:mm:ss")
    val msg = s"$now \n新环境切换申请($onlineOfflineMsg)\n主机：${ip}\n环境变更：$oldEnv -> $newEnv \n 申请人：$user($misID) \n 审核：[ACK|${url}]"
    xm.send(alertUserSeq, msg)
  }

  def applySwitchEnv(ip: String, newEnv: String): String = {
    val checkResult = checkIPForApply(ip, newEnv)
    if (FAILURE == checkResult.code) {
      return JsonHelper.errorJson(checkResult.msg)
    }
    val oldEnv = getCurrentEnvBySgAgent(ip)
    val updateRet = updateRecordForApply(ip, oldEnv, newEnv)
    updateRet.code match {
      case SUCCESS =>
        //线下环境自助切换
        updateIpEnv(ip)
        JsonHelper.dataJson(updateRet.msg)
      case FAILURE =>
        JsonHelper.errorJson(updateRet.msg)
    }
  }

  /**
   * 通过 sgagent 获取ip对应的当前环境
    *
    * @return 返回给定IP的环境[prod,stage,test]，否则抛出异常Exception
   */
  @throws(classOf[Exception])
  def getCurrentEnvBySgAgent(ip: String): String = {
    var transport: TTransport = null
    try {
      val timeout = 3000
      transport = new TFramedTransport(new TSocket(ip, 5266, timeout), 16384000)
      val protocol: TProtocol = new TBinaryProtocol(transport)
      val agent = new SGAgent.Client(protocol)
      transport.open
      val env = agent.getEnv
      Env.apply(env).toString
    } catch {
      case e: Exception =>
        LOG.error(s"get env error $ip", e)
        throw e
    } finally {
      if (null != transport) {
        try {
          transport.close()
        } catch {
          case e: Exception =>
            LOG.error(s"Failed to close $ip thrift connection", e)
        }
      }
    }
  }


  /**
   * 判定当前用户是否有权限申请切换改IP
   * 获取该用户下所有appkey,并遍历所有的ip，找到IP
    *
    * @param ip
   * @return
   */
  private def isIPAuth(ip: String): Boolean = {
    val appkeyList = ServiceCommon.appsByUser()
    Env.values.foreach {
      x =>
        appkeyList.asScala.foreach {
          y =>
            AppkeyProviderService.providerNode(y.toString, x.toString).foreach {
              z =>
                if (z.contains(ip)) {
                  return true
                }
            }
        }
    }
    false
  }

  def deleteApply(id: String) = {
    db withSession {
      implicit session: Session =>
        val statement = Switchenv.filter(_.id === id.toLong)
        if (statement.exists.run) {
          statement.delete
          JsonHelper.dataJson("删除成功")
        } else {
          JsonHelper.errorJson("删除失败")
        }
    }
  }

  private def checkIPEnv(ip: String, newEnv: String): ErrMsg = {
    var currentEnv = ""
    try {
      currentEnv = getCurrentEnvBySgAgent(ip)
      if (currentEnv equals newEnv) {
        val env = EnvMap.getAliasEnv(newEnv)
        return ErrMsg(FAILURE, s"该主机当前环境已经是$env")
      }
    } catch {
      case e: Exception =>
        return ErrMsg(FAILURE, "无法查询当前主机的环境信息")
    }
    ErrMsg(SUCCESS, s"新环境${EnvMap.getAliasEnv(newEnv)},老环境${EnvMap.getAliasEnv(currentEnv)}")
  }

  /**
   * 跟新 ip 的 服务环境
   * 1：首先改变sg_agent的环境
   * 2：其次改变对应机器的环境
    *
    * @return
   */
  def updateIpEnv(ip: String): String = {
    val oldEnv = getCurrentEnvBySgAgent(ip)
    val switchEnvOpt = getSwitchEnv(ip, oldEnv)
    if (!switchEnvOpt.isDefined) return JsonHelper.errorJson("数据库无申请记录")
    val switchItem = switchEnvOpt.get

    val id = switchItem.id
    val newEnv = switchItem.newEnv.get
    val updated = updateAndRestartAgent(ip, newEnv, oldEnv)
    if (updated) {
      val doSwitch = doSwitchEnv(id, ip, newEnv, oldEnv)
      if (FAILURE == doSwitch.code) {
        //审核失败
        return JsonHelper.errorJson(doSwitch.msg)
      }
      JsonHelper.dataJson(s"跟新环境成功")
    } else {
      JsonHelper.errorJson(s"更新或者重启agent失败,请联系${getSwtichAuthList}")
    }

  }

  def updateAndRestartAgent(id: Long): String = {
    val switchEnvOpt = getSwitchEnv(id)
    if (!switchEnvOpt.isDefined) return JsonHelper.errorJson("无法连接到数据库")
    val switchItem = switchEnvOpt.get

    val ip = switchItem.ip.get
    val newEnv = switchItem.newEnv.get
    val oldEnv = switchItem.oldEnv.get
    val updated = updateAndRestartAgent(ip, newEnv, oldEnv)
    if (updated) {
      JsonHelper.dataJson(s"重置agent成功，请检查agent")
    } else {
      JsonHelper.errorJson(s"更新或者重启agent失败,请登录主机查询sg_agent.xml配置文件")
    }

  }

  def updateAndRestartAgent(ip: String, newEnv: String, oldEnv: String): Boolean = {
    //重启agent
    var exception: Exception = null
    var transport: TTransport = null
    try {
      val timeout = 3000
      transport = new TFramedTransport(new TSocket(ip, 5266, timeout), 16384000)
      val protocol: TProtocol = new TBinaryProtocol(transport)
      val agent = new SGAgent.Client(protocol)
      transport.open
      //正常流程应该是agent报异常
      agent.switchEnv(newEnv, "agent.octo.sankuai.com")
    } catch {
      case e: Exception =>
        exception = e
    } finally {
      if (null != transport) {
        try {
          transport.close()
        } catch {
          case e: Exception => LOG.error(s"$ip fail ${e.getMessage}")
        }
      }
    }
    //check sg_agent的环境
    Thread.sleep(3000L)
    val assert_env = getCurrentEnvBySgAgent(ip)
    if (assert_env.equals(newEnv)) {
      BorpClient.saveOpt(actionType = ActionType.UPDATE.getIndex, entityId = CommonHelper.SG_AGENT_APPKEY, entityType = EntityType.updateAndRestartAgent, fieldName = ip, newValue = newEnv, oldValue = oldEnv)
      true
    } else {
      LOG.error(s"重启sgagent异常，ip $ip,oldEnv $oldEnv newEnv $newEnv", exception)
      false
    }
  }

  private def ipPort2Ip(ipPort: String): String = {
    try {
      //防止IP解析出错
      ipPort.split(":").apply(0)
    } catch {
      case _: Exception => ""
    }
  }


  /**
   * 更新provider节点，主要是将老节点数据迁移到新环境中，如果新节点已经存在，则只删除老节点。
   */
   def updateProviders(ip: String, newEnv: String, oldEnv: String): ErrMsg = {
    val onOffLine = ServiceCommon.env_desc
    //删除的老的zk节点,并将数据迁移到新节点中
    val user = UserUtils.getUser
    try {
      val allServiceList = ServiceCommon.getAppkeyByIp(List(ip))
      val appkeys = allServiceList.apply(0)._2.asScala.toList
      val toList = new ArrayBuffer[Int]()
      val currentEnvpath = s"$sankuaiPath/$oldEnv"
      val agentKeys = List(Appkeys.sgagent.toString, Appkeys.kmsagent.toString)
      val new_appkeys = appkeys ::: agentKeys
      new_appkeys.foreach {
        x =>
          //x为appkey
          val isExist1 = handleUpdateProviders(x, 1, ip, newEnv, oldEnv)
          val isExist2 =
            if (agentKeys.contains(x)) {
              false
            } else {
              handleUpdateProviders(x, 0, ip, newEnv, oldEnv)
            }
          if (isExist1 || isExist2) {
            try {
              val dataPath = List(currentEnvpath, x, Path.desc).mkString("/")
              val serviceDesc = Json.parse(ZkClient.getData(dataPath)).validate[ServiceModels.Desc].get
              val users = serviceDesc.owners
              toList.clear()
              users.foreach {
                u =>
                  toList += u.id
              }
              toList += user.getId
              Messager.sendAlarm(toList, Alarm(null, s"环境切换($onOffLine)\n主机：$ip \n备注：该主机的环境已切换为$newEnv ，如果服务$x 出现问题，请登录该主机并重启服务。", null), Seq(MODE.XM))
            } catch {
              case e: Exception =>
                Messager.sendAlarm(List(user.getId), Alarm(null, s"环境切换($onOffLine)\n主机：$ip \n备注：该主机的环境已切换为$newEnv 。但无法获$x 服务的负责人，请通知该主机的相关人员", null), Seq(MODE.XM))
            }
          }
      }
    } catch {
      case e: Exception =>
        val retMsg = s"主机$ip 的环境已切换为$newEnv 。但在更新该主机的provider节点时失败，请通知该主机的相关人员"
        val msg = s"环境切换($onOffLine)\n主机：$ip \n备注：$retMsg"
        Messager.sendAlarm(List(user.getId), Alarm(null, msg, null), Seq(MODE.XM))
        return ErrMsg(FAILURE, retMsg)
    }
    ErrMsg()
  }

   def handleUpdateProviders(appkey: String, thrift_http: Int, ip: String, newEnv: String, oldEnv: String) = {
    val thrift_http_desc = if (1 == thrift_http) Path.provider else Path.providerHttp
    val onOffLine = ServiceCommon.env_desc
    var isExist = false
    val otherServiceProviderPath = s"$sankuaiPath/$oldEnv/$appkey/$thrift_http_desc"
    val currentTime = System.currentTimeMillis
    val user = UserUtils.getUser
    ZkClient.children(otherServiceProviderPath).asScala.foreach {
      y =>
        //y为ip:port
        val itemIP = ipPort2Ip(y)
        if (itemIP equals ip) {
          try {
            val otherServicePath = s"$otherServiceProviderPath/$y"
            val serviceOldData = Json.parse(ZkClient.getData(otherServicePath)).validate[ServiceModels.ProviderNode].get
            val serviceNewData = serviceOldData.copy(env = Env.withName(newEnv).id, lastUpdateTime = currentTime / 1000)
            //删除老节点
            ZkClient.deleteWithChildren(otherServicePath)
            val fieldName = s"$oldEnv $y"
            BorpClient.saveOpt(actionType = ActionType.DELETE.getIndex, entityId = appkey, entityType = EntityType.switchEnvDelProvider, fieldName = fieldName)
            //将节点数据迁移到新节点的数据

            val newPath = s"$sankuaiPath/$newEnv/$appkey/$thrift_http_desc/$y"

            if (!ZkClient.exist(newPath)) {
              //新节点如果不存在时，则新增
              AppkeyProviderService.addProviderByType(appkey, thrift_http, serviceNewData)
              BorpClient.saveOpt(actionType = ActionType.INSERT.getIndex, entityId = appkey, entityType = EntityType.switchEnvNewProvider, fieldName = s"$newEnv $y")
            }
          } catch {
            case e: Exception =>
              Messager.sendAlarm(List(user.getId), Alarm(null, s"环境切换($onOffLine)\n主机：$ip \n备注：该主机环境环境已切换为$newEnv 。但无法更新该主机的$appkey $y 的OCTO节点，请通知该主机的相关人员", null), Seq(MODE.XM))
          }
          isExist = true
        }
    }
    isExist
  }


  private def doSwitchEnv(id: Long, ip: String, newEnv: String, oldEnv: String): ErrMsg = {
    val onOffLine = ServiceCommon.env_desc
    val msgTitle = s"环境切换($onOffLine)\n主机：$ip \n老环境：$oldEnv \n新环境：$newEnv \n备注："

    //更新用户的服务的provider节点，主要是将老节点数据迁移到新环境中，如果新节点已经存在，则只删除老节点。
    val updateUserProvider = updateProviders(ip, newEnv, oldEnv)
    if (FAILURE == updateUserProvider.code) {
      sendXM(s"$msgTitle 该主机的agent环境已经更新成功，但更新该主机的新老provider节点失败")
      return updateUserProvider
    }

    //更新数据库
    val updateDB = updataSwitchEnvDB(id, 1, s"该主机已经是 $newEnv 环境,建议重启该主机的服务")
    if (FAILURE == updateDB.code) {
      sendXM(s"$msgTitle 该主机的环境已经更新成功，但无法更新数据库")
      return updateDB
    }


    sendXM(s"$msgTitle 环境切换成功")
    ErrMsg()
  }

  private def sendXM(msg: String) = {
    val user = UserUtils.getUser
    Messager.sendAlarm(List(user.getId), Alarm(null, msg, null), Seq(MODE.XM))
  }


  def comfirmSwitchEnv(id: Long): String = {
    val switchEnvOpt = getSwitchEnv(id)
    if (!switchEnvOpt.isDefined) return JsonHelper.errorJson("无法连接到数据库")
    val switchItem = switchEnvOpt.get
    val ip = switchItem.ip.get
    val newEnv = switchItem.newEnv.get
    val oldEnv = switchItem.oldEnv.get
    val doSwitch = doSwitchEnv(id, ip, newEnv, oldEnv)
    if (FAILURE == doSwitch.code) {
      //审核失败
      return JsonHelper.errorJson(doSwitch.msg)
    }
    JsonHelper.dataJson("审核成功")
  }

  def updataSwitchEnvDB(id: Long, flag: Int, note: String): ErrMsg = {
    val currentTime = System.currentTimeMillis
    val user = UserUtils.getUser
    val misID = user.getLogin
    db withSession {
      implicit session: Session =>

        val ret_update = Switchenv.filter(_.id === id).map(x => (x.comfirmMisid, x.comfirmTime, x.flag, x.note)).update(Some(misID), Some(currentTime), Some(flag), Some(note))
        if (1 != ret_update) {
          return ErrMsg(FAILURE, s"无法更新数据库")
        }
    }

    ErrMsg()
  }

  private def getSwitchEnv(ip: String, oldEnv: String) = {
    db withSession {
      implicit session: Session =>
        Switchenv.filter { x => (x.ip === ip) && (x.oldEnv === oldEnv) && (x.flag === 0) }.firstOption
    }
  }

  /**
   * 根据ID 获取环境切换的申请
   */
  def getSwitchEnv(id: Long) = {
    db withSession {
      implicit session: Session =>
        Switchenv.filter(_.id === id).firstOption
    }
  }

  /**
   *
   * @param flag 0：默认值，表示未审核
   *             1：已审核，并且已经执行了环境切换
   *             2：审核不通过
   * @return
   */
  def getSwitchEnv(flag: Int, searchIP: String, page: Page) = {
    db withSession {
      implicit session: Session =>
        page.setTotalCount(Switchenv.filter(x => (x.flag === flag) && x.ip.like(searchIP)).length.run)
        val retList = Switchenv.filter(x => (x.flag === flag) && x.ip.like(searchIP)).sortBy(_.applyTime.desc).drop(page.getStart).take(page.getPageSize).list
        retList.map {
          x =>
            val cluster = OpsService.getHostEnvTag(x.ip.getOrElse("")) match {
              case s: String => s
              case None => ""
            }
            val hostName = OpsService.ipToHost(x.ip.getOrElse(""))
            SwitchenvItem(x.id, x.applyMisid, x.applyTime, x.ip, x.oldEnv, x.newEnv, x.comfirmMisid, x.comfirmTime, x.flag, x.note, cluster, hostName)
        }

    }
  }

  def deleteSwitchEnv(id: Long) = {
    db withSession {
      implicit session: Session =>
        1 == Switchenv.filter(_.id === id).delete
    }
  }

  def getSwtichAuthList() = {
    val auths = MsgpConfig.get("switch.admin", "yangjie17,zhangcan02,zhangxi,shuchao02,zhangchi11")
    auths.split(",").toList.asJava
  }

}
