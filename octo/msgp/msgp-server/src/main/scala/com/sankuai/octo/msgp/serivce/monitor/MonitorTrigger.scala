package com.sankuai.octo.msgp.serivce.monitor

import java.util.concurrent.{Executors, TimeUnit}

import com.sankuai.inf.octo.mns.util.ProcessInfoUtil
import com.sankuai.meituan.config.listener.IConfigChangeListener
import com.sankuai.msgp.common.config.MsgpConfig
import com.sankuai.msgp.common.config.db.msgp.Tables._
import com.sankuai.msgp.common.model.Page
import com.sankuai.msgp.common.utils.{FalconTask, FalconUtil, MessageFalcon}
import com.sankuai.msgp.common.utils.client.Messager.{Alarm, MODE}
import com.sankuai.msgp.common.utils.client.{Messager, TairClient}
import com.sankuai.octo.msgp.dao.monitor.{AppkeyTriggerCountDAO, MonitorTriggerDAO}
import com.sankuai.octo.msgp.model.{MScheduler, TriggerStatus}
import com.sankuai.octo.msgp.serivce.data.DataQuery
import com.sankuai.octo.msgp.serivce.data.DataQuery.{DataSeries, Point}
import com.sankuai.octo.msgp.serivce.service.{AppkeyProviderService, ServiceCommon}
import com.sankuai.octo.msgp.serivce.servicerep.ServiceKpiReport
import com.sankuai.octo.msgp.serivce.servicerep.ServiceKpiReport.KpiData
import com.sankuai.octo.msgp.service.mq.OctoTriggersEvent
import org.joda.time.{DateTime, LocalDate}
import org.slf4j.{Logger, LoggerFactory}

import scala.collection.mutable
import scala.collection.parallel.ForkJoinTaskSupport

object MonitorTrigger {
  val logger: Logger = LoggerFactory.getLogger(MonitorTrigger.getClass)

  //qps_minute
  case class MonitorData(count: Double, qps: Double, cost_50: Double, cost_90: Double, cost_99: Double,
                         perfKpi: Option[KpiData] = None)

  case class CompareData(threshold_value: Double, value: Double = 0, cValue: Double = 0,
                         avgThreshold_value: Double = 0, avgValue: Double = 0, avgCValue: Double = 0)

  var isSendToFalcon = MsgpConfig.get("trigger_send_to_falcon", "true").toBoolean

  {
    MsgpConfig.addListener("trigger_send_to_falcon", new IConfigChangeListener() {
      def changed(key: String, oldValue: String, newValue: String) = {
        logger.info(s"trigger_send_to_falcon change : $newValue")
        isSendToFalcon = newValue.toBoolean
      }
    })
  }

  //本地分钟粒度缓存
  //  val minuteBuffer = scala.collection.mutable.Map[String,Option[FalconLastData]]()
  //同比缓存
  //  val compareBuffer = scala.collection.mutable.Map[String,KpiData]()

  // appkeyNotify用来记录“appkey+side+item”对应的历史报警频率信息
  //  val appkeyNotify = mutable.Map[String, NotifyInf]()

  //qps的报警阈值
  private val Threshold_QPS = MsgpConfig.get("threshold_qps", "100").toInt
  private val Threshold_TP = MsgpConfig.get("threshold_tp", "100").toInt

  val scheduler = Executors.newScheduledThreadPool(3)
  private val threadPool = new ForkJoinTaskSupport(new scala.concurrent.forkjoin.ForkJoinPool(20))

  var MONITOR_OUTLIER_CHECK_DISABLED: Boolean = {
    val value = MsgpConfig.get("monitor.outlier.check.disabled", "false")
    value.toBoolean
  }

  {
    MsgpConfig.addListener("monitor.outlier.check.disabled", new IConfigChangeListener() {
      def changed(key: String, oldValue: String, newValue: String) = {
        logger.info("monitor.outlier.check.disabled", newValue)
        MONITOR_OUTLIER_CHECK_DISABLED = newValue.toBoolean
      }
    })
  }

  //定时appkey任务
  def start() {
    val now = System.currentTimeMillis()
    //得到距离整分钟的秒数
    val init = (60000l - (now % 60000l)) / 1000l
    //延迟5秒开始执行
    scheduler.scheduleAtFixedRate(new Runnable {
      def run(): Unit = {
        try {
          logger.info(s"new round in trigger is ${System.currentTimeMillis() / 1000l}")
          //Tair中存储的秒数
          val startOfRoundOption = TairClient.get(MonitorTriggerLeader.startOfEveryRoundOfMonitor)
          //Trigger中开始的秒数
          val currentStartOfMinute = System.currentTimeMillis() / 1000L / 60L * 60L
          //获得准确的开始秒数
          val currentCreateTime = if (startOfRoundOption.isDefined) {
            startOfRoundOption.get.toLong
          } else {
            currentStartOfMinute
          }
          logger.info(s"currentCreateTime in trigger is $currentCreateTime")
          startMonitorAction(currentCreateTime)
        } catch {
          case e: Exception =>
            logger.error("this round has exception ", e);
        }
      }
    }, init + 5l, 60l, TimeUnit.SECONDS)
  }

  /**
    * 定时appkey任务
    * createTime = 秒
    */
  def startMonitorAction(createTime: Long) {
    logger.debug(s"init monitor  $createTime ")
    val start = System.currentTimeMillis
    val triggerList = getTriggers(createTime)
    if (triggerList.nonEmpty) {
      logger.info(s"monitor trigger size ${triggerList.size}")
      doMonitorAction(triggerList, createTime)
      val end = System.currentTimeMillis
      logger.info(s"${MScheduler.monitorSchedule.toString},start:$start,end: $end, trigger time：$createTime")
    } else {
      logger.error("monitor trigger size is empty.")
    }
  }

  //获得本机需要计算的Trigger Item
  def getTriggers(createTime: Long) = {
    val triggerCount = MonitorTriggerDAO.getAllTriggersCount(createTime)
    logger.info(s"triggerCount:  ${triggerCount}")
    val allAliveNodeList = getMsgpProviderList
    logger.info(s"all alive modes of msgp: ${allAliveNodeList.size}")
    val (fragmentStart, fragmentSize) = getFragmentRange(triggerCount, allAliveNodeList)
    logger.info(s"fragmentStart: $fragmentStart, fragmentSize $fragmentSize")
    MonitorTriggerDAO.getTriggersFragment(fragmentStart, fragmentSize, createTime)
  }

  //获得MSGP存活的节点
  def getMsgpProviderList = {
    val nodeList = AppkeyProviderService.getProviderByType("com.sankuai.inf.msgp", 2, "prod", "", 2, new Page(0, 100), -8)
    nodeList.map(_.ip).sorted
  }


  //计算对应的分片大小
  def getFragmentRange(appkeyCount: Int, msgpProviders: List[String]) = {
    val ip = ProcessInfoUtil.getLocalIpV4
    val hostCount = msgpProviders.size
    val index = msgpProviders.indexOf(ip)
    logger.info(s"monitortrigger getFragmentRange ip:$ip, host_count:$hostCount,index: $index")
    if (index < 0) {
      (0, 0)
    } else {
      val pageSize = appkeyCount / hostCount
      val fragmentStart = appkeyCount / hostCount * index
      val fragmentSize = if (index == hostCount - 1) {
        appkeyCount - fragmentStart
      } else {
        pageSize
      }
      (fragmentStart, fragmentSize)
    }
  }

  //执行监控操作
  def doMonitorAction(triggerList: List[AppkeyTriggerRow], createTime: Long) = {
    val triggerListPar = triggerList.par
    triggerListPar.tasksupport = threadPool
    triggerListPar.foreach {
      trigger =>
        doMonitorTriggerAction(trigger, createTime)
    }
  }

  def doMonitorTriggerAction(trigger: AppkeyTriggerRow, createTime: Long) = {
    val appkey = trigger.appkey
    val monitorDataOpt = getMonitorData(appkey, trigger, createTime)
    try {
      // data-query数据源
      val updateTime = System.currentTimeMillis() / 1000L
      monitorDataOpt match {
        case Some(monitorData) =>
          doTriggerItem(appkey, trigger, monitorData)
          //完成后设置为完成
          MonitorTriggerDAO.updateTriggerStatus(trigger.id, createTime, updateTime, TriggerStatus.FINISHED)
        case None =>
          MonitorTriggerDAO.updateTriggerStatus(trigger.id, createTime, updateTime, TriggerStatus.MISSING_DATA)
      }
    } catch {
      case e: Exception =>
        val updateTime = System.currentTimeMillis() / 1000L
        MonitorTriggerDAO.updateTriggerStatus(trigger.id, createTime, updateTime, TriggerStatus.TRIGGER_FAILED)
        logger.error(s"when trigger appkey $appkey,trigger $trigger,monitorDataOpt $monitorDataOpt", e)
    }
  }


  def getMonitorData(appkey: String, trigger: AppkeyTriggerRow, createTime: Long) = {
    val env = "prod"
    try {
      trigger.item match {
        case x if x.startsWith("today") =>
          val date = new LocalDate().toDateTimeAtStartOfDay
          val dailyData = DataQuery.getDailyStatisticFormatted(appkey, env, date, trigger.side)
          val dayDatas = dailyData.filter(_.spanname == trigger.spanname)
          if (dayDatas.nonEmpty) {
            val dayData = dayDatas.head
            Some(MonitorData(dayData.count, dayData.qps, dayData.upper50, dayData.upper90, dayData.upper99))
          } else {
            None
          }
        case x if x.startsWith("minute") =>
          val lastData = DataQuery.lastData(appkey, env, trigger.side, trigger.spanname)
          lastData match {
            case None =>
              None
            case Some(minuterData) =>
              // 上一次上报距离现在超过八分钟，不再报警
              val now = (new DateTime().getMillis / 1000).toInt
              if ((now - minuterData.count.getOrElse(DataSeries(0, 0)).timestamp) > 8 * 60) {
                None
              } else {
                Some(MonitorData(minuterData.count.getOrElse(DataSeries(0, 0)).value, minuterData.qps.getOrElse(DataSeries(0, 0)).value,
                  minuterData.cost_50.getOrElse(DataSeries(0, 0)).value,
                  minuterData.cost_90.getOrElse(DataSeries(0, 0)).value,
                  minuterData.cost_99.getOrElse(DataSeries(0, 0)).value))
              }
          }

        case x if x.startsWith("compare") =>
          //这里减180s 是为了配合数据中心数据落地
          val start_time = (System.currentTimeMillis() / 1000 / 60 * 60).toInt - 180
          val perfKpi = getAveragePerformance(appkey, trigger, start_time)
          //修复一下同比qps报警计算的问题
          Some(MonitorData(perfKpi.count, perfKpi.qps, perfKpi.tp50, perfKpi.tp90, 0, Some(perfKpi)))
        case _ => None
      }
    }
    catch {
      case e: Exception =>
        logger.error(s"getMonitorData error appkey $appkey,trigger $trigger ,exception", e)
        None
    }
  }

  /**
    * 获取一段时间内的平均性能指标值
    *
    * @param appkey
    * @param trigger
    * @param time
    * @return
    */
  def getAveragePerformance(appkey: String, trigger: AppkeyTriggerRow, time: Int) = {
    val list = ServiceKpiReport.perfKpi(appkey, time - 300, time + 60, time, trigger.side, trigger.spanname)
    if (list.nonEmpty) {
      list.head
    } else {
      KpiData(appkey, "", 0L, 0, 0, 0, 0, 0)
    }
  }

  /**
    * 计算同比环比
    * 1：需要考虑平均的 同比环比
    * 2：需要考虑当前时间点的同比环比
    */
  def compareValue(perfKpi: KpiData, appkey: String, trigger: AppkeyTriggerRow, time: Int): CompareData = {
    val cPerfKpi = getAveragePerformance(appkey, trigger, time)
    if (trigger.item.contains("upper_50")) {
      compareTPValue(perfKpi, cPerfKpi)
    } else {
      compareQPSValue(perfKpi, cPerfKpi)
    }
  }

  def compareTPValue(perfKpi: KpiData, cPerfKpi: KpiData): CompareData = {
    val pointTpOption = perfKpi.tp50Point.getOrElse(Point(Some(""), Some(0.0), Some(0))).y
    val pointTp = if (pointTpOption.isDefined) {
      pointTpOption.get
    } else {
      0.0
    }
    val cPointTpOption = cPerfKpi.tp50Point.getOrElse(Point(Some(""), Some(0.0), Some(0))).y
    val cPointTp = if (cPointTpOption.isDefined) {
      cPointTpOption.get
    } else {
      0.0
    }

    if (cPerfKpi.qps > Threshold_QPS && perfKpi.qps > Threshold_QPS
      && cPerfKpi.tp50 > Threshold_TP && perfKpi.tp50 > Threshold_TP
      && pointTp > Threshold_TP && cPointTp > Threshold_TP
    ) {

      val pointTpCompare = if (cPointTp == 0) {
        0
      } else {
        (pointTp - cPointTp) / cPointTp * 100
      }

      val tpCompare = if (cPerfKpi.tp50 == 0) {
        0
      } else {
        (perfKpi.tp50 - cPerfKpi.tp50).toDouble / cPerfKpi.tp50 * 100
      }
      CompareData(Math.abs(pointTpCompare), pointTp, cPointTp, Math.abs(tpCompare), perfKpi.tp50, cPerfKpi.tp50)
    } else {
      CompareData(0, pointTp, cPointTp)
    }
  }

  def compareQPSValue(perfKpi: KpiData, cPerfKpi: KpiData): CompareData = {
    val pointQpsOption = perfKpi.qpsPoint.getOrElse(Point(Some(""), Some(1.0), Some(0))).y
    val pointQps = if (pointQpsOption.isDefined) {
      pointQpsOption.get
    } else {
      1.0
    }
    val cPointQpsOption = cPerfKpi.qpsPoint.getOrElse(Point(Some(""), Some(1.0), Some(0))).y
    val cPointQps = if (cPointQpsOption.isDefined) {
      cPointQpsOption.get
    } else {
      1.0
    }

    if (cPerfKpi.qps > 10 && perfKpi.qps > 10
      && cPerfKpi.qps > Threshold_QPS && perfKpi.qps > Threshold_QPS
      && pointQps > Threshold_QPS && cPointQps > Threshold_QPS
    ) {
      val pointQpsCompare = if (cPointQps == 0) {
        0
      } else {
        (pointQps - cPointQps) / cPointQps * 100
      }

      val qpsCompare = if (cPerfKpi.qps == 0) {
        0
      } else {
        (perfKpi.qps - cPerfKpi.qps).toDouble / cPerfKpi.qps * 100
      }
      CompareData(Math.abs(pointQpsCompare), pointQps, cPointQps, Math.abs(qpsCompare), perfKpi.qps, cPerfKpi.qps)
    } else {
      CompareData(0, pointQps, cPointQps)
    }
  }

  /**
    * 区分触发同比.环比
    */
  def doTriggerItem(appkey: String, trigger: AppkeyTriggerRow, monitorData: MonitorData) = {
    val itemDesc = {
      if (trigger.side == "server")
        "服务接口" + trigger.itemDesc
      else
        "外部接口" + trigger.itemDesc
    }
    val threshold = trigger.threshold
    val start_time = (System.currentTimeMillis() / 1000 / 60 * 60).toInt - 180

    val compareData = if (itemDesc.contains("QPS")) {
      /** 考虑到性能数据异常时,QPS的变化较为明显, 因此暂时只做qps异常点检测
        * */
      trigger.item match {
        //QPS(分钟粒度)同比
        case x if x.endsWith("week") =>
          if (MONITOR_OUTLIER_CHECK_DISABLED) {
            compareValue(monitorData.perfKpi.get, appkey, trigger, start_time - 604800)
          } else {
            if (isOutlier(monitorData, "QPS", start_time - 604800 - 600, start_time - 604800, trigger)) {
              CompareData(trigger.threshold, avgThreshold_value = trigger.threshold)
            } else {
              compareValue(monitorData.perfKpi.get, appkey, trigger, start_time - 604800)
            }
          }
        //QPS(分钟粒度)环比
        case x if x.endsWith("day") =>
          if (MONITOR_OUTLIER_CHECK_DISABLED) {
            compareValue(monitorData.perfKpi.get, appkey, trigger, start_time - 86400)
          } else {
            if (isOutlier(monitorData, "QPS", start_time - 86400 - 600, start_time - 86400, trigger)) {
              CompareData(trigger.threshold, avgThreshold_value = trigger.threshold)
            } else {
              compareValue(monitorData.perfKpi.get, appkey, trigger, start_time - 86400)
            }
          }

        //QPS(分钟粒度)
        case x =>
          if (MONITOR_OUTLIER_CHECK_DISABLED) {
            compareValue(monitorData.perfKpi.get, appkey, trigger, start_time - 86400)
          } else {
            //同时校验上周同期和昨日同期数据,若其中一次判定为异常点,则不执行报警检查
            if (isOutlier(monitorData, "QPS", start_time - 86400 - 600, start_time - 86400, trigger) ||
              isOutlier(monitorData, "QPS", start_time - 604800 - 600, start_time - 604800, trigger)) {
              CompareData(trigger.threshold, avgThreshold_value = trigger.threshold)
            } else {
              CompareData(monitorData.qps, avgThreshold_value = monitorData.qps)
            }
          }
      }
    } else {
      trigger.item match {
        //50%耗时(分钟粒度)
        case x if x.endsWith("upper_50") =>
          CompareData(monitorData.cost_50, avgThreshold_value = monitorData.cost_50)
        //90%耗时(分钟粒度)
        case x if x.endsWith("upper_90") =>
          CompareData(monitorData.cost_90, avgThreshold_value = monitorData.cost_90)
        //99%耗时(分钟粒度)
        case x if x.endsWith("upper_99") =>
          CompareData(monitorData.cost_99, avgThreshold_value = monitorData.cost_99)
        //总次数
        case x if x.endsWith("sideCount") =>
          CompareData(monitorData.count, avgThreshold_value = monitorData.count)
        //耗时同比
        case x if x.endsWith("week") =>
          compareValue(monitorData.perfKpi.get, appkey, trigger, start_time - 604800)
        //耗时环比
        case x if x.endsWith("day") =>
          compareValue(monitorData.perfKpi.get, appkey, trigger, start_time - 86400)
        case x =>
          CompareData(monitorData.count, avgThreshold_value = monitorData.count)
      }
    }

    val message = if (trigger.item.contains("compare")) {
      s" 上期值: ${compareData.cValue} 本期值: ${compareData.value} "
    } else {
      compareData.threshold_value.formatted("%.2f")
    }
    val compared = trigger.item.contains("compare")
    //环比、同比，小于 设定值，是不正确的
    //    val function = if (compared) {
    //      ">"
    //    } else {
    //      trigger.function
    //    }
    trigger.function match {
      case ">" =>
        val boolCompare = if (compared) {
          compareData.threshold_value > 0 && compareData.value > compareData.cValue
        } else {
          true
        }

        if (boolCompare && compareData.threshold_value > threshold && compareData.avgThreshold_value > threshold) {
          doTriggerWithDuration(appkey, trigger, message)
        } else {
          //报警恢复通知
          recoveryTrigger(appkey, trigger, message)
          // 删除历史报警
          deleteTriggerCount(trigger)
        }
      case "<" =>
        val boolCompare = if (compared) {
          compareData.threshold_value > 0 && compareData.value < compareData.cValue
        } else {
          true
        }

        if (boolCompare && compareData.threshold_value < threshold && compareData.avgThreshold_value < threshold) {
          doTriggerWithDuration(appkey, trigger, message)
        } else {
          //报警恢复通知
          recoveryTrigger(appkey, trigger, message)
          // 删除历史报警
          deleteTriggerCount(trigger)
        }
    }
  }

  def doTriggerWithDuration(appkey: String, trigger: AppkeyTriggerRow, message: String) = {
    val history = AppkeyTriggerCountDAO.getByBusinessMonitorId(trigger.id)
    if (history.nonEmpty) {
      val row = history.get
      if (row.count + 1 >= trigger.duration) {
        // 报警
        doTrigger(appkey, trigger, message)
      }
      // 更新持续次数
      AppkeyTriggerCountDAO.update(row.copy(count = row.count + 1))
    } else {
      // 插入持续次数
      AppkeyTriggerCountDAO.insert(AppkeyTriggerCountRow(0, trigger.id, 1))
      if (1 >= trigger.duration) {
        // 报警
        doTrigger(appkey, trigger, message)
      }
    }
  }

  def deleteTriggerCount(trigger: AppkeyTriggerRow) = {
    val history = AppkeyTriggerCountDAO.getByBusinessMonitorId(trigger.id)
    if (history.nonEmpty) {
      val row = history.get
      // 更新持续次数
      AppkeyTriggerCountDAO.update(row.copy(count = 0))
    }
  }

  def recoveryTrigger(appkey: String, trigger: AppkeyTriggerRow, value: String) = {
    val history = AppkeyTriggerCountDAO.getByBusinessMonitorId(trigger.id)
    if (history.nonEmpty) {
      val row = history.get
      if (row.count >= trigger.duration) {
        val side = if (trigger.side == "server") "服务接口" else "外部接口"
        val onlineOfflineMsg = ServiceCommon.env_desc
        val recoveryDesc = if (trigger.functionDesc.equals("大于")) {
          "小于"
        } else {
          "大于"
        }
        val message = if (trigger.item.startsWith("compare")) {
          //对比的情况下展示原始值和现在值
          s"OCTO监控报警($onlineOfflineMsg)\n$appkey$side[${
            trigger.spanname
          }], $value, ${
            trigger.itemDesc
          }${
            recoveryDesc
          }[${
            trigger.threshold
          }%]"
        } else {
          s"OCTO监控报警($onlineOfflineMsg)\n$appkey$side[${
            trigger.spanname
          }] ${
            trigger.itemDesc
          }[$value]${
            recoveryDesc
          }[${
            trigger.threshold
          }]"
        }
        notifyApp(appkey, trigger, 0, message, MessageFalcon.FalconStatus.OK.getName, value)
      }
    }
  }

  def main(args: Array[String]): Unit = {
    val trigger = AppkeyTriggerRow(14716, "com.sankuai.wmarch.sg.api", "server", "BusinessCertification.queryBasicInfo", "compare.timers.$appkey.$sideCost.upper_99", "99%耗时(分钟粒度)", ">", "大于", 2000, 1, 0)
    recoveryTrigger("com.sankuai.wmarch.sg.api", trigger, "1000")
  }

  // 触发报警时，存储报警事件和发送报警
  def doTrigger(appkey: String, trigger: AppkeyTriggerRow, value: String) = {
    val side = if (trigger.side == "server") "服务接口" else "外部接口"
    val onlineOfflineMsg = ServiceCommon.env_desc
    val message = if (trigger.item.startsWith("compare")) {
      //对比的情况下展示原始值和现在值
      s"OCTO监控报警($onlineOfflineMsg)\n$appkey$side[${
        trigger.spanname
      }], $value, ${
        trigger.itemDesc
      }${
        trigger.functionDesc
      }[${
        trigger.threshold
      }%]"
    } else {
      s"OCTO监控报警($onlineOfflineMsg)\n$appkey$side[${
        trigger.spanname
      }] ${
        trigger.itemDesc
      }[$value]${
        trigger.functionDesc
      }[${
        trigger.threshold
      }]"
    }

    //    val ifNotify = MonitorFrequency.checkHowNotify(appkey + trigger.side + trigger.item, appkeyNotify)
    val now = System.currentTimeMillis()
    //    if (ifNotify) {
    val event = EventRow(0, appkey, trigger.side, trigger.spanname, trigger.item, 1, now, 0, now, "", message)
    val eventId = MonitorEvent.insertEvent(event)
    notifyApp(appkey, trigger, eventId, message, MessageFalcon.FalconStatus.PROBLEM.getName, value)
    //    } else {
    //      val event = EventRow(0, appkey, trigger.side, trigger.spanname, trigger.item, 1, now, 1, now, "", message)
    //      MonitorEvent.insertEvent(event)
    //    }
  }

  // 报警通过邮件、大象、短信发送到责任人
  def notifyApp(appkey: String, trigger: AppkeyTriggerRow, eventId: Long, message: String, status: String, value: String) = {
    val subject = trigger.itemDesc
    val domain = ServiceCommon.OCTO_URL
    val dataUrl = if (trigger.item.startsWith("compare")) {
      val _type = if (trigger.item.contains("day")) {
        0
      } else {
        1
      }
      s"$domain/data/performance/span?appkey=$appkey&spanname=${
        trigger.spanname
      }&type=${
        _type
      }&unit=Minute"
    } else if (trigger.side == "server") {
      s"$domain/data/tabNav?appkey=$appkey#source"
    } else {
      s"$domain/data/tabNav?appkey=$appkey#destination"
    }
    //    val ackUrl = s"$domain/monitor/$appkey/ack?side=${
    //      trigger.side
    //    }&spanname=${
    //      trigger.spanname
    //    }&eventId=$eventId"

    val subscribeUrl = s"$domain/monitor/config?appkey=$appkey"
    val content = s"$message\n[查看详情 | $dataUrl] [报警订阅 | $subscribeUrl]"
    val alarm = Alarm(s"OCTO监控报警：$subject", content)
    val triggerSubs = MonitorConfig.getTriggerSubs(appkey, trigger.id)
    //获取服务树的值班人
    //    val alarm_admin = OpsService.getAppkeyAlarmAdmin(appkey)
    val xm_admin = mutable.HashSet()
    val xm_admin_set = xm_admin ++ triggerSubs._1
    if (isSendToFalcon) {
      val now = (System.currentTimeMillis() / 1000).toInt
      logger.info("trigger send falcon {}", alarm)
      MessageFalcon.sendMessage(appkey, s"OCTO监控报警(${ServiceCommon.env_desc})", alarm.content, trigger.spanname, xm_admin_set.mkString(","), status, false, trigger.itemDesc, value)
      OctoTriggersEvent.getInstance().sendMQ(appkey, s"OCTO监控报警(${ServiceCommon.env_desc})", trigger.spanname, alarm.content, xm_admin_set.mkString(","), status);
    } else {
      Messager.sendXMAlarm(xm_admin_set.toSeq, alarm)
    }
    Messager.sendAlarm(triggerSubs._2, alarm, Seq(MODE.SMS))
    Messager.sendAlarm(triggerSubs._3, alarm, Seq(MODE.MAIL))
  }


  /**
    * 判断是否是异常值
    *
    * @param monitorData
    * @param itemType 指标类型 QPS Or TP
    * @param start
    * @param end
    * @param trigger
    * @return
    */
  def isOutlier(monitorData: MonitorData, itemType: String, start: Int, end: Int, trigger: AppkeyTriggerRow) = {
    val recordsOption = DataQuery.getDataRecord(trigger.appkey, start, end, "", trigger.side, "", "prod", "", "span", trigger.spanname,
      "all", "all", "all", "")
    val recordList = recordsOption.getOrElse(List())
    val qpsList = if (recordList.nonEmpty) {
      recordList.head.qps.map(qpsPoint => qpsPoint.y.getOrElse(0.0))
    } else {
      List()
    }
    itemType match {
      case "QPS" =>
        if (qpsList.size < 5 || !isValidArray(qpsList)) {
          true
        } else {
          val baseQps = monitorData.qps
          val meanQps = getMean(qpsList)
          if (baseQps > meanQps * 0.5) {
            false
          } else {
            val allQps = qpsList :+ baseQps
            !isValidArray(allQps)
          }
        }
      case _ => false
    }
  }

  def isValidArray(list: List[Double]) = {
    val mean = getMean(list)
    val stdDev = getStdDev(list, mean)
    stdDev < mean * 0.2
  }

  def getMean(xs: List[Double]): Double = xs match {
    case Nil => 0.0
    case ys => ys.sum / ys.size.toDouble
  }

  def getStdDev(xs: List[Double], avg: Double): Double = xs match {
    case Nil => 0.0
    case ys => math.sqrt((0.0 /: ys) {
      (a, e) => a + math.pow(e - avg, 2.0)
    } / xs.size)
  }
}
