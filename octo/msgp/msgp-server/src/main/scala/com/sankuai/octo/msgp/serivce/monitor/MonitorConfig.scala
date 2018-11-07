package com.sankuai.octo.msgp.serivce.monitor

import java.util.concurrent.Executors

import com.sankuai.meituan.auth.util.UserUtils
import com.sankuai.msgp.common.config.MsgpConfig
import com.sankuai.msgp.common.config.db.msgp.Tables
import com.sankuai.msgp.common.model.{MonitorModels, ServiceModels}
import com.sankuai.msgp.common.utils.helper.{CommonHelper, HttpHelper, JsonHelper}
import com.sankuai.octo.msgp.dao.monitor.{MonitorDAO, MonitorTriggerDAO, ProviderTriggerDao}
import com.sankuai.octo.msgp.domain.UserSubscribeMonitor
import com.sankuai.msgp.common.model.MonitorModels._
import com.sankuai.octo.msgp.model.SubStatus
import com.sankuai.octo.msgp.serivce.data.Kpi
import com.sankuai.octo.msgp.serivce.service.ServiceCommon
import com.sankuai.octo.msgp.serivce.subscribe.MonitorSubscribe
import dispatch.url
import org.slf4j.{Logger, LoggerFactory}
import play.api.libs.json.Json

import scala.collection.JavaConverters._
import scala.concurrent.{ExecutionContext, Future}

object MonitorConfig {
  val LOG: Logger = LoggerFactory.getLogger(MonitorConfig.getClass)

  private val mtraceUrl = if (CommonHelper.isOffline) {
    "http://mtrace.inf.dev.sankuai.com"
  } else {
    "http://mtrace.sankuai.com"
  }

  def triggerItems() = {
    MonitorModels.triggerItems()
  }

  def providerTriggerItems() = {
    MonitorModels.providerTriggerItems()
  }

  def triggerFunctions() = {
    MonitorModels.triggerFunctions()
  }

  def triggerOptions() = {
    MonitorModels.triggerOptions()
  }

  def getSpans(appkey: String, side: String, env: String) = {
    Kpi.spannameList(appkey, side, env).asScala.filter(_ != "*").asJava
  }

  def getTriggers(appkey: String) = {
    MonitorTriggerDAO.getTriggers(appkey)
  }


  case class ConfiguredStatus(span: String, isConfigured: Boolean)

  case class UnconfiguredSpans(appkey: String, env: String, server: List[ConfiguredStatus], client: List[ConfiguredStatus])

  /**
    * 得到未配置报警的接口列表
    *
    * @param appkey
    * @param env
    */
  def getUnconfiguredSpans(appkey: String, env: String) = {
    val ownSpans = getSpans(appkey, "server", env).asScala
    val externSpans = getSpans(appkey, "client", env).asScala
    val triggerSpans = MonitorTriggerDAO.getTriggers(appkey).map(_.spanname)

    val ownSpansUnconfigured = ownSpans.map {
      ownSpan =>
        val configured = triggerSpans.contains(ownSpan)
        ConfiguredStatus(ownSpan, configured)
    }

    val externSpansUnconfigured = externSpans.map {
      externSpan =>
        val configured = triggerSpans.contains(externSpan)
        ConfiguredStatus(externSpan, configured)
    }

    UnconfiguredSpans(appkey, env, ownSpansUnconfigured.toList, externSpansUnconfigured.toList)
  }

  def getTriggersWithSubStatus(appkey: String) = {
    val triggers = getTriggers(appkey)
    // add subscribe status
    val user = UserUtils.getUser
    val subscribes = MonitorDAO.getSubscribes(appkey, user.getId).sortBy(-_.triggerId)
    val triggerWithSubStatus = triggers.map {
      x =>
        val subOption = subscribes.find(sub => sub.triggerId == x.id || sub.triggerId == 0)
        if (subOption.isDefined) {
          AppkeyTriggerWithSubStatus(x.id, x.appkey, x.side, x.spanname, x.item, x.itemDesc, x.duration,
            x.function, x.functionDesc, x.threshold, x.createTime, subOption.get.xm, subOption.get.sms, subOption.get.email)
        } else {
          AppkeyTriggerWithSubStatus(x.id, x.appkey, x.side, x.spanname, x.item, x.itemDesc, x.duration,
            x.function, x.functionDesc, x.threshold, x.createTime, SubStatus.UnSub.id, SubStatus.UnSub.id, SubStatus.UnSub.id)
        }
    }
    triggerWithSubStatus
  }

  def getProviderTriggersWithSubStatus(appkey: String) = {
    val triggers = ProviderTriggerDao.getTriggers(appkey)
    // add subscribe status
    val user = UserUtils.getUser
    val subscribes = ProviderTriggerDao.getSubscribes(appkey, user.getId).sortBy(-_.triggerId)
    val triggerWithSubStatus = triggers.map {
      x =>
        val subOption = subscribes.find(sub => sub.triggerId == x.id || sub.triggerId == 0)
        if (subOption.isDefined) {
          AppkeyProviderTriggerWithSubStatus(x.id, x.appkey, x.item, x.itemDesc,
            x.function, x.functionDesc, x.threshold, x.createTime, subOption.get.xm, subOption.get.sms, subOption.get.email)
        } else {
          AppkeyProviderTriggerWithSubStatus(x.id, x.appkey, x.item, x.itemDesc,
            x.function, x.functionDesc, x.threshold, x.createTime, SubStatus.UnSub.id, SubStatus.UnSub.id, SubStatus.UnSub.id)
        }
    }
    triggerWithSubStatus
  }

  def updateTrigger(appkey: String, json: String) = {
    try {
      val trigger = Json.parse(json).validate[Trigger].get
      MonitorTriggerDAO.insertOrUpdateTrigger(appkey, trigger)
      //给人服务负责人订阅上
      addTrigger(appkey, trigger)
    }
    catch {
      case e: Exception => LOG.error(s"when validate[Trigger].get", e)
    }
  }

  /**
    * 批量增加性能报警
    *
    * @param appkey
    * @param json
    */
  def batchUpdateTrigger(appkey: String, json: String) = {
    try {
      val triggers = Json.parse(json).validate[List[Trigger]]
      if (triggers.isSuccess) {
        triggers.get.foreach {
          trigger =>
            MonitorTriggerDAO.insertOrUpdateTrigger(appkey, trigger)
            //给人服务负责人订阅上
            addTrigger(appkey, trigger)
        }
      }
    }
    catch {
      case e: Exception => LOG.error(s"when validate[Trigger].get", e)
    }
  }

  /**
    * 获取配置项限额
    *
    * @param appkey
    * @param itemDesc
    * @param side
    * @return
    */
  def getItemDescCount(appkey: String, itemDesc: String, side: String) = {
    //已有的配额
    val existedCount = MonitorTriggerDAO.getItemDescCount(appkey, itemDesc, side)

    val unlimitedAppkeys = getItemUnlimitedWhiteList
    val configuredCount = unlimitedAppkeys.getOrElse(appkey, 40)
    (existedCount, configuredCount)
  }

  def getItemUnlimitedWhiteList = {
    MsgpConfig.get("trigger.unlimited").split(",").map {
      x =>
        val appkeyConfig = x.split(":")
        if (appkeyConfig.size == 2) {
          appkeyConfig.apply(0) -> appkeyConfig.apply(1)
        } else {
          "" -> 0
        }
    }.toMap
  }

  //mtrace 拓扑节点
  case class MtraceTopologyNode(appkey: String, methodName: String)

  implicit val mtraceTopologyNodeReads = Json.reads[MtraceTopologyNode]
  implicit val mtraceTopologyNodeWrites = Json.writes[MtraceTopologyNode]

  //mtrace 拓扑图
  case class MtraceTopology(childrens: List[MtraceTopologyNode], node: MtraceTopologyNode)

  implicit val mtraceTopologyReads = Json.reads[MtraceTopology]
  implicit val mtraceTopologyWrites = Json.writes[MtraceTopology]

  /**
    * 获取服务接口的外部接口
    *
    * @param appkey appkey
    * @param span   服务接口
    */

  def getCoreClientSpan(appkey: String, span: String) = {
    val request = s"$mtraceUrl/topology?appkey=$appkey&methodName=$span"
    try {
      val text = HttpHelper.execute(url(request), text =>
        Json.parse(text).validate[List[MtraceTopology]].asOpt)
      text match {
        case Some(data) =>
          val correspondingNode = data.filter(x => x.node.appkey.equalsIgnoreCase(appkey) && x.node.methodName.equalsIgnoreCase(span))
          if (correspondingNode.nonEmpty) {
            correspondingNode.flatMap(_.childrens)
          } else {
            List()
          }
        case None =>
          List()
      }
    } catch {
      case e: Exception =>
        LOG.error("get mtrace topology failed", e)
        List()
    }
  }

  def updateProviderTrigger(appkey: String, json: String) = {
    try {
      val trigger = Json.parse(json).validate[ProviderTrigger].get
      ProviderTriggerDao.insertOrUpdateTrigger(appkey, trigger)
    }
    catch {
      case e: Exception => LOG.error(s"when validate[Trigger].get", e)
    }
  }

  def deleteTrigger(appkey: String, id: Long) = {
    MonitorTriggerDAO.deleteTrigger(appkey, id)
    // 同时删除订阅
    MonitorDAO.deleteTriggerSubscribe(appkey, id)
  }

  def deleteProviderTrigger(appkey: String, id: Long) = {
    ProviderTriggerDao.deleteTrigger(appkey, id)
    // 同时删除订阅
    ProviderTriggerDao.deleteTriggerSubscribe(appkey, id)
  }

  /**
    * 删除所有相关的服务
    *
    * @param appkey
    * @return
    */
  def deleteProviderTrigger(appkey: String) = {
    ProviderTriggerDao.deleteTrigger(appkey)
    // 同时删除订阅
    ProviderTriggerDao.deleteTriggerSubscribe(appkey)
  }

  def batchSubscribe(appkey: String, json: String) = {
    try {
      val user = UserUtils.getUser
      val request = Json.parse(json).validate[BatchSubscribeUpdateRequest].get
      val xmStatus = if (request.xm) SubStatus.Sub else SubStatus.UnSub
      val smsStatus = if (request.sms) SubStatus.Sub else SubStatus.UnSub
      val emailStatus = if (request.email) SubStatus.Sub else SubStatus.UnSub
      MonitorDAO.insertOrUpdateAllSubscribe(request.appkey, user.getId, user.getLogin, user.getName, xmStatus, smsStatus, emailStatus)
      JsonHelper.dataJson(true)
    } catch {
      case e: Exception => LOG.error("", e)
        JsonHelper.errorJson(e.getMessage)
    }
  }


  def subscribe(appkey: String, json: String) = {
    try {
      val user = UserUtils.getUser
      val request = Json.parse(json).validate[SubscribeUpdateRequest].get
      MonitorDAO.insertOrUpdateSubscribe(request.appkey, request.triggerId, user.getId, user.getLogin, user.getName, request.mode, request.newStatus)
      JsonHelper.dataJson(true)
    } catch {
      case e: Exception => LOG.error("", e)
        JsonHelper.errorJson(e.getMessage)
    }
  }

  def providerSubscribe(appkey: String, json: String) = {
    try {
      val user = UserUtils.getUser
      val request = Json.parse(json).validate[SubscribeUpdateRequest].get
      ProviderTriggerDao.insertOrUpdateSubscribe(request.appkey, request.triggerId, user.getId, user.getLogin, user.getName, request.mode, request.newStatus)
      JsonHelper.dataJson(true)
    } catch {
      case e: Exception => LOG.error("", e)
        JsonHelper.errorJson(e.getMessage)
    }
  }

  def providerBatchSubscribe(appkey: String, json: String) = {
    try {
      val user = UserUtils.getUser
      val request = Json.parse(json).validate[BatchSubscribeUpdateRequest].get
      val xmStatus = if (request.xm) SubStatus.Sub else SubStatus.UnSub
      val smsStatus = if (request.sms) SubStatus.Sub else SubStatus.UnSub
      val emailStatus = if (request.email) SubStatus.Sub else SubStatus.UnSub
      ProviderTriggerDao.insertOrUpdateAllSubscribe(request.appkey, user.getId, user.getLogin, user.getName, xmStatus, smsStatus, emailStatus)
      JsonHelper.dataJson(true)
    } catch {
      case e: Exception => LOG.error("", e)
        JsonHelper.errorJson(e.getMessage)
    }
  }

  def subscribe2(userSubscribeMonitor: UserSubscribeMonitor) = {
    try {
      MonitorDAO.insertOrUpdateSubscribe(userSubscribeMonitor.getAppkey, userSubscribeMonitor.getTriggerId, userSubscribeMonitor.getUserId, userSubscribeMonitor.getUserLogin,
        userSubscribeMonitor.getUserName, userSubscribeMonitor.getMode, userSubscribeMonitor.getNewStatus)
      JsonHelper.dataJson(true)
    } catch {
      case e: Exception => LOG.error("", e)
        JsonHelper.errorJson(e.getMessage)
    }
  }

  def getTriggerSubs(appkey: String, triggerId: Long) = {
    val subs = MonitorDAO.getSubScribe(appkey, triggerId)
    val xmUserIds = subs.filter(_.xm == SubStatus.Sub.id.toByte).map(_.userLogin).toList
    val smsUserIds = subs.filter(_.sms == SubStatus.Sub.id.toByte).map(_.userId).toList
    val emailUserIds = subs.filter(_.email == SubStatus.Sub.id.toByte).map(_.userId).toList
    (xmUserIds, smsUserIds, emailUserIds)
  }

  def getProviderTriggerSubs(appkey: String, triggerId: Long) = {
    val subs = ProviderTriggerDao.getSubScribe(appkey, triggerId)
    //此处的处理是为了兼容老数据订阅只插入一条0的情况，如果取消订阅会插入一条带triggerid的记录，过滤是为了保留特定triggerid取消状态的记录
    val relSubs = subs.groupBy(_.userLogin).flatMap {
      case (userLogin, triggerList) =>
        if (triggerList.size >= 2) {
          triggerList.find(_.triggerId != 0)
        } else {
          Some(triggerList.head)
        }
    }.toList
//    val unSubs = subs.filter(x => (x.xm != SubStatus.Sub.id.toByte || x.sms != SubStatus.Sub.id.toByte || x.email != SubStatus.Sub.id.toByte) && x.triggerId == triggerId).map(_.userLogin).toList
    val xmUserIds = relSubs.filter(x => x.xm == SubStatus.Sub.id.toByte).map(_.userLogin).toList.distinct
    val smsUserIds = relSubs.filter(x => x.sms == SubStatus.Sub.id.toByte).map(_.userId).toList.distinct
    val emailUserIds = relSubs.filter(x => x.email == SubStatus.Sub.id.toByte).map(_.userId).toList.distinct
    (xmUserIds, smsUserIds, emailUserIds)
  }



  /**
    * 1:给appkey 添加默认服务节点订阅
    * 2:服务负责人默认添加上订阅
    */
  def addProviderTrigger(appkey: String, users: List[ServiceModels.User]) = {
    val triggers = List(
      ProviderTrigger("active_perf", "活跃节点比例", "<", "小于", 50),
      ProviderTrigger("minute_change", "一分钟状态变化频率", ">", "大于", 3))
    triggers.foreach {
      trigger =>
        if (!ProviderTriggerDao.exitTrigger(appkey, trigger)) {
          ProviderTriggerDao.insertOrUpdateTrigger(appkey, trigger)
        }
    }
    /*默认订阅所有的权限，
    * TODO 手动取消的如何避免重复加上？
    * 服务负责人被删除了，是否取消订阅
    */
    users.foreach {
      user =>
        //服务节点状态报警
        ProviderTriggerDao.insertOrUpdateAllSubscribe(appkey, user.id, user.login, user.name, SubStatus.Sub, SubStatus.UnSub, SubStatus.UnSub)
    }
    LOG.info(s"${appkey}添加服务节点报警事件")
  }

  /**
    * 添加相关人员的性能报警订阅
    *
    * @param appkey
    * @param users
    */
  def addPerformanceTrigger(appkey: String, users: List[ServiceModels.User]) = {
    val perf_triggers = MonitorTriggerDAO.getTriggers(appkey)
    users.foreach {
      user =>
        perf_triggers.foreach {
          perf_trigger =>
            MonitorDAO.defaultSubscribe(appkey, perf_trigger.id, user)
        }
    }
  }

  def deleteProviderTrigger(appkey: String, users: List[ServiceModels.User]) = {
    val prov_triggers = ProviderTriggerDao.getTriggers(appkey)
    prov_triggers.foreach {
      trigger =>
        users.foreach {
          user =>
            ProviderTriggerDao.deleteTriggerSubscribe(appkey, user.id)
        }
    }
  }

  def deletePerformanceTrigger(appkey: String, users: List[ServiceModels.User]) = {
    users.foreach {
      user =>
        MonitorDAO.deleteTriggerSubscribe(appkey, user.login)
    }
  }

  /**
    * 添加报警
    *
    * @param appkey
    * @param trigger
    */
  def addTrigger(appkey: String, trigger: Trigger) = {
    val desc = ServiceCommon.desc(appkey)
    val users = desc.owners
    val triggerDomain = MonitorTriggerDAO.getTrigger(appkey, trigger)
    if (triggerDomain.isDefined) {
      val trggerId = triggerDomain.get.id
      users.foreach {
        user =>
          MonitorDAO.defaultSubscribe(appkey, trggerId, user)
      }
    }
  }
}
