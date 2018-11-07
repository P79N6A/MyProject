package com.sankuai.octo.msgp.serivce.manage

import java.util.concurrent.{Executors, TimeUnit}

import com.sankuai.msgp.common.config.{DbConnection, MsgpConfig}
import com.sankuai.msgp.common.model.{Env, ServiceModels}
import com.sankuai.msgp.common.utils.client.Messager.xm
import com.sankuai.msgp.common.utils.helper.JsonHelper
import com.sankuai.msgp.common.config.db.msgp.Tables._
import com.sankuai.msgp.common.service.org.OpsService
import com.sankuai.octo.msgp.model.Appkeys
import com.sankuai.octo.msgp.serivce.service.{AppkeyProviderService, ServiceCommon}
import com.sankuai.octo.msgp.serivce.sgagent.SgAgentService
import com.sankuai.msgp.common.utils.client.Messager.{Alarm, MODE}
import com.sankuai.msgp.common.utils.client.Messager
import com.sankuai.octo.msgp.utils.client.MnsCacheClient
import com.sankuai.sgagent.thrift.model.{ProtocolRequest, SGAgent, SGService}
import org.apache.commons.lang3.ArrayUtils
import org.apache.curator.framework.{CuratorFramework, CuratorFrameworkFactory}
import org.apache.curator.retry.RetryOneTime
import org.apache.thrift.TException
import org.apache.thrift.protocol.{TBinaryProtocol, TProtocol}
import org.apache.thrift.transport.{TFramedTransport, TSocket, TTransport}
import org.joda.time.DateTime
import org.slf4j.{Logger, LoggerFactory}
import play.api.libs.json.Json

import scala.collection.JavaConverters._
import scala.slick.driver.MySQLDriver.simple._

/**
 * 在sg_agent发版后，可以通过指定某些sg_agent执行getServerList接口测试可用率。
 * 使用原生thrift方式调用指定ip:port的sg_agent的getServiceList接口.
 */
object AgentAvailability {
  private val LOG: Logger = LoggerFactory.getLogger(AgentAvailability.getClass)
  private val ownerIdList = List(61338, 2080175, 59048)
  private val modes = Seq(MODE.XM)
  private val monitorExe = Executors.newScheduledThreadPool(2)
  private val db = DbConnection.getPool()
  private val alarmUserList = List("yangjie17", "shuchao02", "huixiangbo")

  private val CHECK_APPKEY = "com.sankuai.sentinel.sgagent"
  private val CHECK_IP = "111.111.111.111"
  private val CHECK_PORT = 1111

  case class row(id: Long, name: String, protocol: String, providers: String, apps: String, status: Int, time: Long)

  implicit val fr = Json.reads[row]
  implicit val fw = Json.writes[row]

  case class SimpleProvider(name: String, ip: String)

  /**
   * appkeyNotify用来记录“appkey+side+item”对应的历史报警频率信息
   */
  val appkeyNotify = scala.collection.mutable.Map[String, NotifyInf]()

  case class NotifyInf(var count: Int, var firstTimestamp: Long, var lastTimestamp: Long)

  def check(ipPort: String, protocol: String, apps: List[String]) = {
    var transport: TTransport = null
    val ip = ipPort.split(":").apply(0)
    val port = ipPort.split(":").apply(1).toInt
    try {
      val timeout = 3000
      transport = new TFramedTransport(new TSocket(ip, port, timeout), 16384000)
      val tProtocol: TProtocol = new TBinaryProtocol(transport)
      val agent = new SGAgent.Client(tProtocol)
      transport.open

      apps.foreach {
        x =>
          val req = new ProtocolRequest()
          req.setRemoteAppkey(x).setLocalAppkey("com.sankuai.inf.msgp").setProtocol(protocol)
          val serviceList = getServiceList(req, agent, x, 0)
          if (serviceList.isEmpty) {
            //报警
            val name = s"${ipPort}_${x}"
            if (checkHowNotify(name)) {
              val message = s"获取服务列表失败：${name}"
              val alarm = Alarm(null, message, null)
              Messager.sendAlarm(ownerIdList, alarm, modes)
            }
          }
      }
    } catch {
      case e: Exception =>
        LOG.warn(s"$ipPort ${e.getMessage}")
        Some(ipPort)
    } finally {
      if (transport != null) {
        try {
          transport.close()
        } catch {
          case e: Exception => LOG.error(s"$ipPort fail ${e.getMessage}")
        }
      }
    }
  }

  def getServiceList(protocolRequest: ProtocolRequest, agent: SGAgent.Client, appkey: String, count: Int): List[SGService] = {
    val response = agent.getServiceListByProtocol(protocolRequest)
    val serviceList = if (null != response && response.getErrcode == 0 && null != response.getServicelist) {
      response.getServicelist.asScala.toList
    } else {
      List()
    }
    LOG.debug(s"第${count}次${appkey}的服务列表大小是：${serviceList.size}，列表是：${serviceList}")
    if (serviceList.isEmpty && count < 2) {
      getServiceList(protocolRequest, agent, appkey, count + 1)
    }
    serviceList
  }

  def getAgentProvider(envId: Int) = {
    val appkey = "com.sankuai.inf.sg_agent"
    AppkeyProviderService.providerNode(appkey, envId).map {
      x =>
        try {
          SimpleProvider(OpsService.ipToHost(x.split(":").apply(0)), x)
        } catch {
          case e: Exception =>
            LOG.info(s"getAgentProvider ${e.getMessage}")
            SimpleProvider(x, x)
        }
    }
  }

  /**
   * 1：调用sg_agent接口注册一个指定IP, 400ms（可配置）后然后getServiceList获取，判断成功失败
   * 2：删除这个IP,  400ms（可配置）然后getServiceList获取，判断成功失败
   */

  def checkAgent() = {
    LOG.info(s"执行哨兵服务检查任务开始")
    val start = System.currentTimeMillis
    val mnsc = MnsCacheClient.getInstance
    Env.values.foreach {
      env =>
        try {
          val resp = mnsc.getMNSCache(Appkeys.sgsentinel.toString, "0", env.toString)
          if (resp.code == com.sankuai.octo.mnsc.idl.thrift.model.Constants.SUCCESS) {
            resp.defaultMNSCache.asScala.foreach { x => checkSentineAgent(x, env.id) }
          }
        }
        catch {
          case e: TException =>
            LOG.error(s"获取哨兵服务节点（${ServiceCommon.env_desc}）失败", e)
        }
    }
    LOG.info(s"执行哨兵服务检查任务完成,耗时${System.currentTimeMillis - start}")
  }

  def checkSentineAgent(sgService: SGService, env: Int) = {
    //初始化，清空服务提供者
    val sleep_time = MsgpConfig.get("check.sentinel.sleep", "5000").toLong
    val ip = sgService.getIp
    deleteSentinelProvider(env)
    registerSentinelThriftService(ip)
    if (!checkSentinelProvider(ip, true)) {
      sendCheckAlarm(s"服务节点注册校验（${ServiceCommon.env_desc}） \n注册节点失败,appkey:$CHECK_APPKEY,哨兵: ${ip},hostname:${OpsService.ipToHost(ip)},环境: ${Env.apply(env)} ")
    } else {
      LOG.info(s"哨兵 ${ip} 注册节点成功")
    }
    deleteSentinelProvider(env)
    Thread.sleep(sleep_time)
    if (checkSentinelProvider(ip, false)) {
      sendCheckAlarm(s"服务节点删除校验（${ServiceCommon.env_desc}） \n删除节点失败,appkey:$CHECK_APPKEY ,哨兵: ${ip},hostname:${OpsService.ipToHost(ip)},环境: ${Env.apply(env)}")
    } else {
      LOG.info(s"哨兵: ${ip},hostname:${OpsService.ipToHost(ip)},环境: ${Env.apply(env)} 删除节点成功")
    }
  }

  /**
   * 注册一个服务
   */
  def registerSentinelThriftService(ip: String) {
    try {
      SgAgentService.registerProvider(ip, CHECK_APPKEY, CHECK_IP, CHECK_PORT)
    }
    catch {
      case e: TException =>
        LOG.error(s"服务节点注册校验（${ServiceCommon.env_desc}） \n$CHECK_APPKEY,哨兵 ${ip} 注册节点失败", e)
        sendCheckAlarm(s"服务节点注册校验（${ServiceCommon.env_desc}） \n$CHECK_APPKEY,哨兵 ${ip} 注册节点失败")
    }
  }

  /**
   * 校验服务是否存在
   *
   * @return true 存在节点，false 不存在节点
   */
  def checkSentinelProvider(ip: String, expect: Boolean) = {
    try {
      val serviceList = SgAgentService.getServiceList(ip, Appkeys.msgp.toString, CHECK_APPKEY)
      val checkProvdier = serviceList.filter { x => x.getIp.equals(CHECK_IP) && x.getPort == CHECK_PORT }
      checkProvdier.nonEmpty
    }
    catch {
      case e: Exception =>
        LOG.error(s"sgAgent  get service error  哨兵:$ip ", e)
        !expect
    }
  }

  /**
   * 删除服务节点
   */
  def deleteSentinelProvider(env: Int) = {
    val providerNode = ServiceModels.ProviderEdit(appkey = CHECK_APPKEY, ip = CHECK_IP,
      port = CHECK_PORT, env = env, weight = None, fweight = None, status = None, extend = None, enabled = None, role = None)
    AppkeyProviderService.delProviderByType(CHECK_APPKEY, 1, providerNode)
  }


  def searchProvider(keyword: String, envId: Int) = {
    //获取所有的provider
    val list = getAgentProvider(envId)
    //做filter
    def f(x: String) = {
      keyword.split(" ").filter(_ != "").foldLeft(true) {
        (result, self) =>
          result && x.contains(self)
      }
    }
    list.filter(x => f(x.name))
  }

  def checkJob() = {
    monitorExe.scheduleAtFixedRate(new Runnable {
      override def run(): Unit = {
        val start = System.currentTimeMillis
        val checkList = getCheckJob()
        checkList.foreach {
          x => x.providers.split(",").toList.foreach {
            self => check(self,x.protocol, x.apps.split(",").toList)
          }
        }
        selfCheckMnsZk()
        LOG.info(s"agentAvailability costs: ${(System.currentTimeMillis() - start)/1000}s." )
      }
    }, 10, 480, TimeUnit.SECONDS)

    monitorExe.scheduleAtFixedRate(new Runnable {
      override def run(): Unit = {
        try {
          val check_switch = MsgpConfig.get("check.sentinel.switch", "true").toBoolean
          if (check_switch) {
            checkAgent()
          }
        }
        catch {
          case e: Exception =>
            LOG.error("check.sentinel失败", e)
        }
      }
    }, 10, 480, TimeUnit.SECONDS)
  }

  private def sendCheckAlarm(msg: String) = {
    val now = DateTime.now().toString("yyyy-MM-dd HH:mm:ss")
    xm.send(alarmUserList, s"$now \n $msg")
  }

  private def selfCheckMnsZk() = {
    val zkStr = MsgpConfig.get("selfCheckMnsZk", "")
    val zkArr = zkStr.split(",")
    if (ArrayUtils.isNotEmpty(zkArr)) zkArr.filter(_.contains(":")).foreach {
      zkUrl =>
        var client: CuratorFramework = null
        try {
          client = CuratorFrameworkFactory.builder.connectString(zkUrl).retryPolicy(new RetryOneTime(500)).build()
          client.start()
          if (null == client.checkExists().forPath("/mns")) {
            sendCheckAlarm(s"Mns-zk(${ServiceCommon.env_desc})\n the checking for the path of /mns is error. $zkUrl")
          }
        } catch {
          case _: Exception =>
            //ignore error
            sendCheckAlarm(s"Mns-zk(${ServiceCommon.env_desc})\n open error. $zkUrl")
        } finally {
          if (null != client) {
            try {
              client.close()
            } catch {
              case _: Exception =>
                LOG.error("cannot close the zk client, {}", zkUrl)
            }
          }
        }
    }
  }

  def addCheck(json: String) = {
    try {
      val checker = Json.parse(json).validate[row].asOpt
      checker match {
        case Some(row) => addCheckJob(AgentCheckerRow(row.id, row.name, row.protocol, row.providers, row.apps, row.status, row.time))
        case None => JsonHelper.errorJson(s"增加失败")
      }
    } catch {
      case e: Exception => JsonHelper.errorJson(s"增加失败，${e.getMessage}")
    }
  }

  def updateCheck(json: String) = {
    try {
      val checker = Json.parse(json).validate[row].asOpt
      checker match {
        case Some(row) => updateCheckJob(AgentCheckerRow(row.id, row.name, row.protocol, row.providers, row.apps, row.status, row.time))
        case None => JsonHelper.errorJson(s"更新失败")
      }
    } catch {
      case e: Exception => JsonHelper.errorJson(s"更新失败，${e.getMessage}")
    }
  }

  def getRichCheckJob() = {
    try {
      val result = getCheckJob.map {
        x => x.copy(
          providers = x.providers.split(",").map {
            self => OpsService.ipToHost(self.split(":").apply(0))
          }.mkString(",")
        )
      }
      JsonHelper.dataJson(result)
    } catch {
      case e: Exception => JsonHelper.errorJson(s"获取失败，${e.getMessage}")
    }

  }

  def getCheckJob() = {
    db withSession {
      implicit session: Session =>
        AgentChecker.list
    }
  }

  def addCheckJob(checker: AgentCheckerRow) = {
    db withSession {
      implicit session: Session =>
        val statement = AgentChecker.filter(x => x.name === checker.name)
        if (statement.exists.run) {
          JsonHelper.errorJson(s"${checker.name}已存在,请重新命名！")
        } else {
          AgentChecker += checker
          JsonHelper.dataJson(checker)
        }
    }
  }

  def updateCheckJob(checker: AgentCheckerRow) = {
    db withSession {
      implicit session: Session =>
        val statement = AgentChecker.filter(x => x.name === checker.name).map(x => (x.name, x.providers, x.apps, x.status))
        if (statement.exists.run) {
          statement.update(checker.name, checker.providers, checker.apps, checker.status)
          JsonHelper.dataJson(checker)
        } else {
          JsonHelper.errorJson(s"${checker.name}不存在")
        }
    }
  }

  def deleteCheckJob(id: Long) = {
    db withSession {
      implicit session: Session =>
        val statement = AgentChecker.filter(x => x.id === id)
        if (statement.exists.run) {
          statement.delete
          JsonHelper.dataJson(id)
        } else {
          JsonHelper.errorJson(s"要删除的id：${id}不存在")
        }
    }
  }

  def checkHowNotify(name: String) = {
    //TODO 清除旧的name，防止内存泄露
    var ifNotify = false
    val now = System.currentTimeMillis()
    if (appkeyNotify.get(name) == None) {
      ifNotify = true
      appkeyNotify.update(name, NotifyInf(1, now, now))
    } else {
      val notifyInf = appkeyNotify(name)
      /** 根据limit判断是否要发送报警 */
      def checkIfNotify(limit: Long) = {
        if (notifyInf.count < limit) {
          notifyInf.count = notifyInf.count + 1
          true
        } else {
          false
        }
      }
      val limit = 3
      /** 记录报警区间长度，仅当本次报警处于上一个连续报警区间才有意义 */
      val nowFirst = now - notifyInf.firstTimestamp
      /** 记录当前报警与上一个连续报警区间最后一次报警的时间差 */
      val nowLast = now - notifyInf.lastTimestamp
      val fiveMin: Long = 5 * 60 * 1000
      val tenMin: Long = 10 * 60 * 1000
      val thirtyMin: Long = 30 * 60 * 1000

      /** 两次报警时间相差不超过30分钟时，认为两个报警处于一个连续的报警区间 */
      if (nowLast < thirtyMin) {
        //属于连续报警区间
        if (nowFirst <= fiveMin) {
          //前5分钟最多报警3次
          ifNotify = checkIfNotify(limit)
        }
        else if (nowFirst <= tenMin) {
          //前10分钟最多报警5次
          ifNotify = checkIfNotify(limit + 2)
        }
        else if (nowFirst <= thirtyMin) {
          //前30分钟最多报警6次
          ifNotify = checkIfNotify(limit + 3)
        }
        else {
          //连续报警超过30分钟后，报警频率趋于1/30min
          ifNotify = checkIfNotify(limit + 3 + nowFirst / thirtyMin)
        }
      } else {
        //不属于连续报警区间
        ifNotify = true
        notifyInf.count = 1
        notifyInf.firstTimestamp = now
      }

      /** 不论何种情况最后都要更新 */
      notifyInf.lastTimestamp = now
    }
    ifNotify
  }

  def main(args: Array[String]) {
    //    val ipPort = "192.168.22.196:5266"
    //    val apps = List("com.sankuai.waimai.poi", "com.sankuai.inf.msgp")
    //    check(ipPort, List("com.sankuai.waimai.order"))
    //    while(true) {}
    addCheckJob(AgentCheckerRow(0, "test1", "thrift", "ddd", "dd", 1, 999))
  }
}
