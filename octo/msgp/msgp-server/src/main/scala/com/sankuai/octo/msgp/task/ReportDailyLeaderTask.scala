package com.sankuai.octo.msgp.task

import java.sql.Date

import com.meituan.jmonitor.JMonitor
import com.sankuai.meituan.config.listener.IConfigChangeListener
import com.sankuai.msgp.common.config.MsgpConfig
import com.sankuai.msgp.common.utils.helper.TaskTimeHelper
import com.sankuai.octo.msgp.dao.report.ReportDailyStatusDAO
import com.sankuai.octo.msgp.utils.helper.ReportHelper
import com.sankuai.octo.mworth.utils.CronScheduler
import org.quartz._
import org.slf4j.LoggerFactory


/**
  * 实际计算模块
  */
object ReportDailyLeaderTask {
  private[task] val logger = LoggerFactory.getLogger(this.getClass)

  def init(): Unit = {
    //3点钟将待计算的appkey状态写入表中
    val resetJob = JobBuilder.newJob(classOf[ReportDailyStatusResetJob]).build()
    val resetTrigger: Trigger = TriggerBuilder.newTrigger().startNow()
      .withSchedule(CronScheduleBuilder.dailyAtHourAndMinute(3, 0)).build()
    CronScheduler.scheduleJob(resetJob, resetTrigger)

    //6点钟重新计算未成功的
    val refreshJob = JobBuilder.newJob(classOf[ReportDailyRefreshJob]).build()
    val refreshTrigger: Trigger = TriggerBuilder.newTrigger().startNow()
      .withSchedule(CronScheduleBuilder.dailyAtHourAndMinute(6, 0)).build()
    CronScheduler.scheduleJob(refreshJob, refreshTrigger)
  }
}


/**
  * 将需要计算的appkey写入状态表
  */
@DisallowConcurrentExecution
class ReportDailyStatusResetJob extends Job {

  import ReportDailyLeaderTask.logger

  //不参与日报计算的appkey
  var APPKEY_EXCLUDED_FROME_REPORT_DAILY: Set[String] = {
    val value = MsgpConfig.get("appkey.excluded.from.report.daily","")
    value.split(",").toSet
  }

  {
    MsgpConfig.addListener("appkey.excluded.from.report.daily", new IConfigChangeListener() {
      def changed(key: String, oldValue: String, newValue: String) = {
        logger.info("appkey.excluded.from.report.daily", newValue)
        APPKEY_EXCLUDED_FROME_REPORT_DAILY = newValue.split(",").toSet
      }
    })
  }

  @throws(classOf[JobExecutionException])
  override def execute(ctx: JobExecutionContext): Unit = {
    calculate()
  }

  def calculate(timeMillis: Long = System.currentTimeMillis()): Unit = {
    val (start, _) = TaskTimeHelper.getYesterDayStartEnd(timeMillis)
    logger.info(s"ReportDailyLeaderTask start ")
    //  获取业务线 -> appkey list
    val owtAppkeyMap = ReportHelper.getOwtToAppkeyMap
    val dateOfData = new Date(start.toLong * 1000L)
    // 将appkey写入Status表中
    val appkeyStatus = owtAppkeyMap.flatMap {
      case (owt, appkeyList) =>
        appkeyList.map {
          appkey =>
            ReportDailyStatusDAO.ReportDailyStatusDomain(owt, appkey, 0, dateOfData)
        }
    }.toList
    val shuffledRecords = scala.util.Random.shuffle(appkeyStatus)
    shuffledRecords.filter(x => !APPKEY_EXCLUDED_FROME_REPORT_DAILY.contains(x.appkey))
    ReportDailyStatusDAO.batchInsert(shuffledRecords)

    logger.info(s"ReportDailyLeaderTask: appkey size ${shuffledRecords.size}")
  }
}

/**
  * 重新计算未成功计算的appkey
  */
@DisallowConcurrentExecution
class ReportDailyRefreshJob extends Job {

  import ReportDailyLeaderTask.logger

  @throws(classOf[JobExecutionException])
  override def execute(ctx: JobExecutionContext) = {
    calculate(System.currentTimeMillis())
  }

  def calculate(timeMillis: Long) = {
    val (start, end) = TaskTimeHelper.getYesterDayStartEnd(timeMillis)
    val day = new java.sql.Date(start * 1000L)
    val list = ReportDailyStatusDAO.searchUncomputed(day)
    //上报Falcon
    JMonitor.addNum("report.daily.unfinished", list.size)
    logger.info(s"report.daily.unfinished size : ${list.size} and appkey : $list")
    val reportDailyJob = new ReportDailyJob
    reportDailyJob.calculateAppkeys((start, end), day, list, "*")
  }

  def calculate(timeMillis: Long, owt: String) = {
    val (start, end) = TaskTimeHelper.getYesterDayStartEnd(timeMillis)
    val day = new java.sql.Date(start * 1000L)
    val list = ReportDailyStatusDAO.searchByOwt(day, owt)
    val reportDailyJob = new ReportDailyJob
    reportDailyJob.calculateAppkeys((start, end), day, list, "*")
  }
}





