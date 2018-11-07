package com.sankuai.octo.msgp.serivce.monitor.business

import com.sankuai.meituan.config.listener.IConfigChangeListener
import com.sankuai.msgp.common.config.MsgpConfig
import com.sankuai.msgp.common.config.db.msgp.Tables._
import com.sankuai.msgp.common.service.org.OrgSerivce
import com.sankuai.msgp.common.utils.MessageFalcon
import com.sankuai.msgp.common.utils.MessageFalcon.FalconStatus
import com.sankuai.octo.msgp.dao.kpi.BusinessDashDao
import com.sankuai.octo.msgp.dao.monitor.{BusinessMonitorDAO, MonitorCountDAO}
import com.sankuai.octo.msgp.serivce.service.ServiceCommon
import com.sankuai.msgp.common.utils.client.Messager
import com.sankuai.msgp.common.utils.client.Messager.{Alarm, MODE}
import com.sankuai.octo.msgp.serivce.monitor.MonitorTrigger.logger
import org.joda.time.DateTime
import org.slf4j.LoggerFactory

import scala.collection.mutable
import scala.collection.parallel.ForkJoinTaskSupport

object KpiMonitor {
  private val logger = LoggerFactory.getLogger(this.getClass)
  private val formatStr = "%.1f"
  val taskSuppertPool = new ForkJoinTaskSupport(new scala.concurrent.forkjoin.ForkJoinPool(8))
  //  private val kpiNotify = mutable.Map[String, NotifyInf]()

  var isSendToFalcon = MsgpConfig.get("trigger_send_to_falcon", "true").toBoolean

  {
    MsgpConfig.addListener("trigger_send_to_falcon", new IConfigChangeListener() {
      def changed(key: String, oldValue: String, newValue: String) = {
        logger.info(s"trigger_send_to_falcon change : $newValue")
        isSendToFalcon = newValue.toBoolean
      }
    })
  }

  def doMonitor(trigger: (BusinessMonitorRow, AppScreenRow), nowValue: Double, baseValue: Double, time: String,
                triggerSubs: (List[Int], List[Int], List[Int])) = {
    val businessMonitorRow = trigger._1
    val (triggered, dValue) = compareNowBase(baseValue, nowValue, businessMonitorRow.threshold, businessMonitorRow.strategy)
    val history = MonitorCountDAO.getByBusinessMonitorId(businessMonitorRow.id)
    // 触发阈值，判断是否触发连续阈值，未考虑宕机时历史报警清理问题
    if (triggered) {
      val message = getAlarmDesc(trigger, nowValue, baseValue, dValue)
      val alarm = Alarm(s"OCTO报警：业务指标", message, null, null)
      if (history.nonEmpty) {
        val row = history.get
        if (row.count >= businessMonitorRow.duration) {
          // 报警
          businessAlarm(triggerSubs, alarm, trigger._2.appkey)
        }
        // 更新持续次数
        MonitorCountDAO.update(row.copy(count = row.count + 1))
      } else {
        // 插入持续次数
        MonitorCountDAO.insert(BusinessMonitorCountRow(0, businessMonitorRow.id, 1))
        if (1 >= businessMonitorRow.duration) {
          // 报警
          businessAlarm(triggerSubs, alarm, trigger._2.appkey)
        }
      }
    } else {
      if (history.nonEmpty) {
        val row = history.get
        MonitorCountDAO.update(row.copy(count = 0))
      }
    }
  }

  def businessAlarm(triggerSubs: (List[Int], List[Int], List[Int]), alarm: Alarm, appkey: String) = {
    try {
      if (isSendToFalcon) {
        val employees = triggerSubs._1.map(self => OrgSerivce.employee(self)).filter(_ != null)
        val admins = employees.map(e => e.getLogin)
        MessageFalcon.sendMessage(appkey, alarm.subject, alarm.content, s"$appkey 业务报警", admins.mkString(","), FalconStatus.PROBLEM.getName, true, "", "")
      } else {
        Messager.sendAlarm(triggerSubs._1, alarm, Seq(MODE.XM))
      }
      Messager.sendAlarm(triggerSubs._2, alarm, Seq(MODE.SMS))
      Messager.sendAlarm(triggerSubs._3, alarm, Seq(MODE.MAIL))
    }
    catch {
      case e: Exception =>
        logger.error(s"businessAlarm error : triggerSubs ${triggerSubs} alarm,${alarm}", e);
    }
  }

  def getAlarmDesc(trigger: (BusinessMonitorRow, AppScreenRow), nowValue: Double, baseValue: Double, dValue: String) = {
    val businessMonitorRow = trigger._1
    val appScreenRow = trigger._2
    val desc = businessMonitorRow.strategy match {
      case 0 => ("下降值:", "")
      case 1 => ("下降百分比:", "%")
      case 2 => ("上升值:", "")
      case 3 => ("上升百分比:", "%")
      case _ => ("上升百分比:", "%")
    }
    val ret = s"业务指标 ${appScreenRow.title}(${appScreenRow.metric}) 当前值:$nowValue 基线值:$baseValue ${desc._1}$dValue 大于 ${businessMonitorRow.threshold}${desc._2}\n" +
      s"[业务监控配置|${ServiceCommon.OCTO_URL}/monitor/business?screenId=${businessMonitorRow.screenId}] "
    val dash = BusinessDashDao.get(businessMonitorRow.screenId)
    if (dash.nonEmpty) {
      s"$ret[业务大盘|${ServiceCommon.OCTO_URL}/business?owt=${dash.get.owt}]"
    } else {
      ret
    }
  }

  def compareNowBase(base: Double, current: Double, threshold: Double, strategy: Int) = {
    strategy match {
      case 0 => compareBaseValue(base, current, -threshold)
      case 1 => compareBasePercentage(base, current, -threshold)
      case 2 => compareBaseValue(base, current, threshold)
      case 3 => compareBasePercentage(base, current, threshold)
      case _ => compareBasePercentage(base, current, threshold)
    }
  }

  // 比基线值
  def compareBaseValue(base: Double, current: Double, threshold: Double) = {
    val value = current - base
    (incrOrDescCompareThreshold(value, threshold), formatStr.format(value.abs))
  }

  // 比基线百分比
  def compareBasePercentage(base: Double, current: Double, threshold: Double) = {
    val dValue = current - base
    if (base == 0) {
      if (dValue == 0) (false, "0") else (true, "无穷大")
    } else {
      val percentage = formatStr.format(dValue / base * 100).toDouble
      (incrOrDescCompareThreshold(percentage, threshold), s"${percentage.abs}%")
    }
  }

  // 增长、下降是否超出阈值
  def incrOrDescCompareThreshold(value: Double, threshold: Double) = {
    if (threshold > 0) {
      // 比基线增长
      if (value > threshold) true else false
    } else {
      // 比基线减小
      if (value < threshold) true else false
    }
  }

  // 所有报警
  def allKpiMonitor = {
    val triggers = BusinessMonitorDAO.getAllBusinessMonitor
    val triggerPar = triggers.par
    triggerPar.tasksupport = taskSuppertPool
    triggerPar.foreach { trigger =>
      val triggerSubs = BusinessMonitorDAO.getTriggerSubs(trigger._1.id)
      if (triggerSubs.nonEmpty) {
        val (nowValue, baseValue, time) = KpiMonitorModel.getNowBase(trigger)
        if (nowValue.nonEmpty && baseValue.nonEmpty && time.nonEmpty) {
          val timeStr = new DateTime(time.get.toLong * 1000).toString("yyyy-MM-dd HH:mm:ss")
          doMonitor(trigger, formatStr.format(nowValue.get).toDouble, formatStr.format(baseValue.get).toDouble, timeStr, triggerSubs.get)
        }
      }
    }
  }

  def main(args: Array[String]) {
    KpiMonitorModel.syncBase
    while (true) {
      allKpiMonitor
      Thread.sleep(2000)
    }
  }
}
