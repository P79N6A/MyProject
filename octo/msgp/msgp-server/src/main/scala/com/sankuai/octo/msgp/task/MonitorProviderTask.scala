package com.sankuai.octo.msgp.task

import java.util.concurrent.{Executors, TimeUnit}

import com.sankuai.meituan.config.listener.IConfigChangeListener
import com.sankuai.msgp.common.config.MsgpConfig
import com.sankuai.msgp.common.model.{Env, Status}
import com.sankuai.msgp.common.config.db.msgp.Tables.{AppkeyProviderTriggerRow, ProviderTriggerCountRow}
import com.sankuai.octo.msgp.dao.monitor.{ProviderTriggerCountDao, ProviderTriggerDao}
import com.sankuai.msgp.common.model.Status.Status
import com.sankuai.msgp.common.utils.MessageFalcon.FalconStatus
import com.sankuai.msgp.common.utils.{FalconTask, FalconUtil, MessageFalcon}
import com.sankuai.msgp.common.utils.client.TairClient
import com.sankuai.msgp.common.utils.helper.CommonHelper
import com.sankuai.octo.msgp.serivce.manage.ScannerChecker.SLog
import com.sankuai.octo.msgp.serivce.monitor.MonitorConfig
import com.sankuai.octo.msgp.serivce.service.{AppkeyProviderService, ServiceCommon}
import com.sankuai.msgp.common.utils.client.Messager.{Alarm, MODE}
import com.sankuai.msgp.common.utils.client.Messager
import com.sankuai.octo.msgp.service.mq.OctoTriggersEvent
import com.sankuai.octo.statistic.util.{ExecutorFactory, StatThreadFactory}
import org.slf4j.LoggerFactory

import scala.collection.JavaConversions._
import scala.collection.mutable
import scala.collection.parallel.ForkJoinTaskSupport

/**
  * 定时监控服务的节点状态
  */
object MonitorProviderTask {
  private val logger = LoggerFactory.getLogger(this.getClass)

  private val checkScheduler = Executors.newSingleThreadScheduledExecutor(StatThreadFactory.threadFactory("MonitorProviderJob-check"))
  private val eventScheduler = ExecutorFactory(doEventNotify, "MonitorProvider-Event-job", 1, 3, 2000)

  val domain_url = ServiceCommon.OCTO_URL

  val env_desc = ServiceCommon.env_desc

  var singleAppkey: Option[String] = None

  var isSendToFalcon = MsgpConfig.get("trigger_send_to_falcon", "true").toBoolean

  {
    MsgpConfig.addListener("trigger_send_to_falcon", new IConfigChangeListener() {
      def changed(key: String, oldValue: String, newValue: String) = {
        logger.info(s"trigger_send_to_falcon change : $newValue")
        isSendToFalcon = newValue.toBoolean
      }
    })
  }

  private val MONITOR_WINDOW = 300

  def init(): Unit = {
    if (!CommonHelper.isOffline) {
      logger.info("启动服务节点状态检查")
      checkScheduler.scheduleAtFixedRate(eventTask, 1, 5, TimeUnit.MINUTES)
    } else {
      logger.info("启动线下定制服务节点状态检查")
      checkScheduler.scheduleAtFixedRate(singleEventTask, 1, 5, TimeUnit.MINUTES)
    }
  }

  /**
    *
    */
  def checkAppkeyStatus(): Unit = {
    val start = System.currentTimeMillis()
    logger.info("服务节点状态检查开始")
    val appsPar = ServiceCommon.apps.par
    appsPar.tasksupport = new ForkJoinTaskSupport((new scala.concurrent.forkjoin.ForkJoinPool(8)))
    appsPar.foreach {
      app =>
        checkAppkey(app)
    }
    logger.info(s"服务节点状态检查结束,耗时${System.currentTimeMillis() - start}")
  }


  def checkAppkey(appkey: String) = {
    //这里的计算要注意，活跃的节点数量和活跃节点比例的计算方式是 活跃节点数/非禁用节点数，禁用节点比例计算方式是 禁用节点数/节点总数
    val allProviders = AppkeyProviderService.getProviderNodes(appkey)
    val providers = allProviders.filter(_.status != Status.STOPPED.id)
    val aliveSize = providers.count(_.status == Status.ALIVE.id)
    val size = allProviders.size
    val aliveRatio = if (providers.size > 0) {
      aliveSize * 100 / providers.size
    } else {
      0
    }
    val forbiddenSize = allProviders.filter(_.status == Status.STOPPED.id).size
    val forbiddenRatio = if (size > 0) {
      forbiddenSize * 100 / size
    } else {
      0
    }
    if (size > 0) {
      logger.debug(s"appkey ${appkey} size:${providers.size},aliveSize ${aliveSize}")
      ProviderTriggerDao.getTriggers(appkey).foreach {
        x =>
          checkItem(appkey, x, size, aliveSize, aliveRatio, forbiddenSize, forbiddenRatio)
      }
    }
  }

  def checkItem(appkey: String, trigger: AppkeyProviderTriggerRow, size: Int, aliveSize: Int, aliveRatio: Int, forbiddenSize: Int, forbiddenRatio: Int) = {
    //    logger.info(s"begin checkItem $appkey,$trigger,$size,$aliveSize,$aliveRatio")
    val threshold_value = trigger.item match {
      case x if x.equals("active_perf") =>
        aliveRatio
      case x if x.equals("active_count") =>
        aliveSize
      case x if x.equals("active_forbidden") =>
        forbiddenRatio
      case _ => //minute_change
        size
    }
    if (!trigger.item.equals("minute_change")) {
      val function = trigger.function
      function match {
        case ">" =>
          if (threshold_value >= trigger.threshold) {
            //记录下状态
            notifyAndRecordTrigger(appkey, trigger, size, aliveSize, aliveRatio, forbiddenSize, forbiddenRatio)
          } else {
            //检查是否符合恢复的条件并且发送恢复通知
            checkTriggerStatusAndRecoveryTrigger(appkey, trigger, size, aliveSize, aliveRatio, forbiddenSize, forbiddenRatio)
          }
        case "<" =>
          if (threshold_value <= trigger.threshold) {
            //记录下状态
            notifyAndRecordTrigger(appkey, trigger, size, aliveSize, aliveRatio, forbiddenSize, forbiddenRatio)
          } else {
            //检查是否符合恢复的条件并且发送恢复通知
            checkTriggerStatusAndRecoveryTrigger(appkey, trigger, size, aliveSize, aliveRatio, forbiddenSize, forbiddenRatio)
          }
      }
    }
  }

  /**
    * 添加报警记录以及报警
    *
    * @param appkey
    * @param trigger
    * @param size       服务节点总数（非禁用）
    * @param aliveSize  活跃节点数
    * @param aliveRatio 活跃节点比例
    */
  def notifyAndRecordTrigger(appkey: String, trigger: AppkeyProviderTriggerRow, size: Int, aliveSize: Int, aliveRatio: Int, forbiddenSize: Int, forbiddenRatio: Int) = {
    val triggerHistory = ProviderTriggerCountDao.getPorviderMonitorId(trigger.id)
    if (triggerHistory.nonEmpty) {
      val row = triggerHistory.get
      ProviderTriggerCountDao.update(row.copy(count = row.count + 1))
    } else {
      ProviderTriggerCountDao.insert(ProviderTriggerCountRow(0, trigger.id, 1))
    }
    logger.info(s"begin notify $appkey,$trigger,$size,$aliveSize,$aliveRatio")
    if (trigger.item.equals("active_forbidden")) {
      notifyAppForbiddenProvider(appkey, trigger, size, forbiddenSize, forbiddenRatio)
    } else {
      notifyAppProvider(appkey, trigger, size, aliveSize, aliveRatio)
    }
  }

  /**
    * 判断恢复通知
    *
    * @param appkey
    * @param trigger
    * @param size
    * @param aliveSize
    * @param aliveRatio
    * @return
    */
  def checkTriggerStatusAndRecoveryTrigger(appkey: String, trigger: AppkeyProviderTriggerRow, size: Int, aliveSize: Int, aliveRatio: Int, forbiddenSize: Int, forbiddenRatio: Int) = {
    val triggerHistory = ProviderTriggerCountDao.getPorviderMonitorId(trigger.id)
    if (triggerHistory.nonEmpty) {
      val row = triggerHistory.get
      if (row.count >= 1) {
        if (trigger.item.equals("active_forbidden")) {
          notifyAppForbiddenProviderRecovery(appkey, trigger, size, forbiddenSize, forbiddenRatio)
        } else {
          notifyAppProviderRecovery(appkey, trigger, size, aliveSize, aliveRatio)
        }
      }
      ProviderTriggerCountDao.update(row.copy(count = 0))
    }
  }

  /**
    * 节点频率变化通知
    *
    * @param appkey
    * @param ipportenv
    * @param trigger
    * @param count
    */
  def checkItem(appkey: String, ipportenv: String, trigger: AppkeyProviderTriggerRow, count: Int) = {
    val function = trigger.function
    function match {
      case ">" =>
        if (count > trigger.threshold) {
          notifyAppEvent(appkey, ipportenv, trigger, count)
        }
      case "<" =>
        if (count < trigger.threshold) {
          notifyAppEvent(appkey, ipportenv, trigger, count)
        }
    }

  }

  /**
    *
    * @param appkey
    * @param trigger
    * @param size
    * @param forbiddenSize
    * @param forbiddenRatio
    */
  def notifyAppForbiddenProvider(appkey: String, trigger: AppkeyProviderTriggerRow, size: Int, forbiddenSize: Int, forbiddenRatio: Int) = {
    val url = s"${domain_url}/service/detail?appkey=${appkey}#supplier"
    val message = s"OCTO服务节点监控(${env_desc})\nappkey:[${appkey}|$url]\n内容:服务节点:共${size}个节点,${forbiddenSize}个节点被禁用, 占比${forbiddenRatio}%\n[报警使用说明|https://123.sankuai.com/km/page/28326952] [报警订阅管理|${domain_url}/monitor/provider/config?appkey=${appkey}]"
    val alarm = Alarm(s"OCTO报警：${trigger.itemDesc}", message, "", url)
    notifyApp(appkey, trigger, alarm, FalconStatus.PROBLEM.getName, false, forbiddenSize.toString)
  }

  /**
    *
    * @param appkey
    * @param trigger
    * @param size
    * @param forbiddenSize
    * @param forbiddenRatio
    */
  def notifyAppForbiddenProviderRecovery(appkey: String, trigger: AppkeyProviderTriggerRow, size: Int, forbiddenSize: Int, forbiddenRatio: Int) = {
    val url = s"${domain_url}/service/detail?appkey=${appkey}#supplier"
    val message = s"OCTO服务节点监控(${env_desc})\nappkey:[${appkey}|$url]\n内容:服务节点:共${size}个节点,${forbiddenSize}个节点被禁用, 占比${forbiddenRatio}%, 已经恢复正常\n[报警使用说明|https://123.sankuai.com/km/page/28326952] [报警订阅管理|${domain_url}/monitor/provider/config?appkey=${appkey}]"
    val alarm = Alarm(s"OCTO报警：${trigger.itemDesc}", message, "", url)
    notifyApp(appkey, trigger, alarm, FalconStatus.OK.getName, false, forbiddenSize.toString)
  }

  /**
    * 组织节点监控报警文案
    *
    * @param appkey
    * @param trigger
    * @param size
    * @param aliveSize
    * @param aliveRatio
    */
  def notifyAppProvider(appkey: String, trigger: AppkeyProviderTriggerRow, size: Int, aliveSize: Int, aliveRatio: Int) = {
    val url = s"${domain_url}/service/detail?appkey=${appkey}#supplier"
    val message = s"OCTO服务节点监控(${env_desc})\nappkey:[${appkey}|$url]\n内容:服务节点:共${size}个节点,${aliveSize}个启动, 占比${aliveRatio}%\n[报警使用说明|https://123.sankuai.com/km/page/28326952] [报警订阅管理|${domain_url}/monitor/provider/config?appkey=${appkey}]"
    val alarm = Alarm(s"OCTO报警：${trigger.itemDesc}", message, "", url)
    notifyApp(appkey, trigger, alarm, FalconStatus.PROBLEM.getName, false, aliveSize.toString)
  }

  /**
    * 组织节点监控报警恢复的文案
    *
    * @param appkey
    * @param trigger
    * @param size
    * @param aliveSize
    * @param aliveRatio
    */
  def notifyAppProviderRecovery(appkey: String, trigger: AppkeyProviderTriggerRow, size: Int, aliveSize: Int, aliveRatio: Int) = {
    val url = s"${domain_url}/service/detail?appkey=${appkey}#supplier"
    val message = s"OCTO服务节点监控(${env_desc})\nappkey:[${appkey}|$url]\n内容:服务节点:共${size}个节点,${aliveSize}个启动, 占比${aliveRatio}%, 已经恢复正常\n[报警使用说明|https://123.sankuai.com/km/page/28326952] [报警订阅管理|${domain_url}/monitor/provider/config?appkey=${appkey}]"
    val alarm = Alarm(s"OCTO报警：${trigger.itemDesc}", message, "", url)
    notifyApp(appkey, trigger, alarm, FalconStatus.OK.getName, false, aliveSize.toString)
  }

  /**
    * 组织一下节点频率变化通知文案
    *
    * @param appkey
    * @param ipportenv
    * @param trigger
    * @param count
    */
  def notifyAppEvent(appkey: String, ipportenv: String, trigger: AppkeyProviderTriggerRow, count: Int): Unit = {
    val url = s"${domain_url}/service/detail?appkey=${appkey}#supplier"
    val message = s"OCTO服务节点监控(${env_desc})\nappkey: [${appkey}|$url] \n内容:服务节点${ipportenv}每分钟变更${count}次。\n[报警使用说明|https://123.sankuai.com/km/page/28326952] [报警订阅管理|${domain_url}/monitor/provider/config?appkey=${appkey}]"
    val alarm = Alarm(s"OCTO报警：${trigger.itemDesc}", message, "", url)
    notifyApp(appkey, trigger, alarm, FalconStatus.PROBLEM.getName, true, count.toString)
  }

  def notifyApp(appkey: String, trigger: AppkeyProviderTriggerRow, alarm: Alarm, status: String, isRecovery: Boolean, value: String): Unit = {
    val start = System.currentTimeMillis()
    val i_start = (start / 1000).toInt
    val tairKey = s"${appkey}_MonitorProviderJob"
    val tair_time = TairClient.get(tairKey).getOrElse("0").toInt
    logger.info(s"appkey: $appkey,i_start: $i_start,tair_time is $tair_time, trigger : $trigger ")
    if (i_start - tair_time >= MONITOR_WINDOW) {
      TairClient.put(tairKey, s"${i_start}", MONITOR_WINDOW)
      val triggerSubs = MonitorConfig.getProviderTriggerSubs(appkey, trigger.id)
      //      val alarm_admin = OpsService.getAppkeyAlarmAdmin(appkey)
      val xm_admin = mutable.HashSet()
      val xm_admin_set = xm_admin ++ triggerSubs._1
      if (isSendToFalcon) {
        logger.info("privider trigger send falcon {}", alarm)
        MessageFalcon.sendMessage(appkey, s"OCTO服务节点监控(${env_desc})", alarm.content, s"$appkey 服务节点", xm_admin_set.mkString(","), status, isRecovery, trigger.itemDesc, value)
        OctoTriggersEvent.getInstance.sendMQ(appkey, s"OCTO服务节点监控(${env_desc})", s"$appkey 服务节点", alarm.content, xm_admin_set.mkString(","), status);
      } else {
        Messager.sendXMAlarm(xm_admin_set.toSeq, alarm)
      }
      Messager.sendAlarm(triggerSubs._2, alarm, Seq(MODE.SMS))
      Messager.sendAlarm(triggerSubs._3, alarm, Seq(MODE.MAIL))
    }
  }


  //定时任务判定是否发通知
  private val eventTask = new Runnable {
    override def run(): Unit = {
      checkAppkeyStatus
    }
  }

  private val singleEventTask = new Runnable {
    override def run(): Unit = {
      singleAppkey = TairClient.get("offline_provider_trigger")
      logger.info(s"get singleAppkey $singleAppkey")
      singleAppkey match {
        case Some(x) =>
          val appkeys = x.split(",").toList
          appkeys.foreach {
            appkey =>
              try {
                if (!appkey.isEmpty) {
                  checkAppkey(appkey)
                }
              } catch {
                case e: Exception =>
                  logger.error("single provider calc error", e)
              }

          }
        case None =>
          logger.info("None appkey need offline check")
      }
    }
  }


  {
    // 在jvm退出时优雅关闭
    Runtime.getRuntime.addShutdownHook(new Thread() {
      override def run(): Unit = {
        //        checkScheduler.submit(eventTask)
        checkScheduler.shutdown()
        checkScheduler.awaitTermination(20, TimeUnit.SECONDS)
      }
    })
  }

  def eventNotify(logData: SLog): Unit = {
    if (!CommonHelper.isOffline) {
      eventScheduler.submit(logData)
    } else {
      singleAppkey match {
        case Some(x) =>
          if (x.contains(logData.appkey)) {
            eventScheduler.submit(logData)
          }
        case None =>
      }
    }
  }

  def doEventNotify(logData: SLog): Unit = {
    val identifier = logData.identifier.split("\\|")
    val appkey = identifier.apply(1).split(":").apply(1)
    val env = identifier.apply(0).split(":").apply(1).toInt
    val ip = identifier.apply(2).split(":").apply(1)
    val port = identifier.apply(3).split(":").apply(1)
    val category = logData.category
    if (category == "UpdateStatus") {
      val tairKey = s"Event_MonitorProviderJob_${appkey}_${env}_${ip}:${port}"
      val data = TairClient.incr(tairKey, 1, 60)
      if (data > 2) {
        val trigger = ProviderTriggerDao.getTrigger(appkey, "minute_change")
        if (trigger.isDefined) {
          val envValue = Env.apply(env)
          val ipport = s"${envValue}环境${ip}:${port}"
          checkItem(appkey, ipport, trigger.get, data)
        }
      }
    }
  }

  case class StateEvent(appkey: String, ip: String, port: String, oldStatus: Status, status: Status, time: Int, count: Int = 1, preEvent: StateEvent = null)

  case class MessageOperation(delete: Boolean, send: Boolean)

}
