package com.sankuai.octo.msgp.task

import java.sql.Date
import java.text.DecimalFormat
import java.util.concurrent.TimeUnit

import com.sankuai.msgp.common.model.ServiceModels
import com.sankuai.msgp.common.utils.helper.TaskTimeHelper
import com.sankuai.octo.msgp.serivce.data.DataQuery
import com.sankuai.octo.msgp.dao.report.ReportErrorLogDao
import com.sankuai.octo.msgp.dao.report.ReportErrorLogDao.ReportErrorlogDomain
import com.sankuai.octo.msgp.serivce.AppkeyAlias
import com.sankuai.octo.msgp.serivce.other.LogServiceClient
import com.sankuai.octo.msgp.utils.helper.ReportHelper
import com.sankuai.octo.mworth.utils.CronScheduler
import com.sankuai.octo.statistic.util.ExecutionContextFactory
import org.joda.time.DateTime
import org.quartz._
import org.slf4j.LoggerFactory

import scala.concurrent._
import scala.concurrent.duration.Duration


object ErrorLogTimerTask {

  private implicit val ec = ExecutionContextFactory.build(4)

  private[task] val logger = LoggerFactory.getLogger(this.getClass)


  var logService = LogServiceClient.getInstance

  private val df = new DecimalFormat("#.###")

  def init(): Unit = {
    val job = JobBuilder.newJob(classOf[DailyErrorJob]).build()
    val trigger: Trigger = TriggerBuilder.newTrigger().startNow()
      .withSchedule(CronScheduleBuilder.dailyAtHourAndMinute(2, 0)).build()
    CronScheduler.scheduleJob(job, trigger)
  }

  def asyncComputeErrorLog(apps: List[ServiceModels.Desc], start: Int, end: Int) = {
    val future = Future.traverse(apps) { app =>
      Future {
        blocking {
          val count = getDaysCount(app.appkey, start, end)
          val errorCount = getDayErrorCount(app.appkey, start, end)
          val radio = if (count > 0) {
            val value = df.format(errorCount.toDouble * 10000 / count)
            value.toDouble
          } else {
            0.0
          }
          ReportErrorLogDao.ReportErrorlogDomain(app.owt.getOrElse(""), app.appkey, count, errorCount, radio, new Date(start * 1000L), System.currentTimeMillis())
        }
      }.recover {
        case e: Exception =>
          logger.error(s"appkey ErrorLogTimerTask:${app.appkey},exception,", e)
          ReportErrorLogDao.ReportErrorlogDomain(app.owt.getOrElse(""), app.appkey, 0, 0, 0, new Date(start * 1000L), System.currentTimeMillis())
      }
    }
    //  batch insert
    future.foreach { list =>
      batchInsertErrorDomains(list)
    }

  }

  private def batchInsertErrorDomains(resList: List[ReportErrorlogDomain]) = {
    if (resList != null && resList.nonEmpty) {
      try {
        ReportErrorLogDao.batchInsert(resList)
      } catch {
        case e: Exception =>
          logger.error("batchInsertErrorDomains fail", e)
      }
    }
  }

  private def getDayErrorCount(appkey: String, start: Int, end: Int): Long = {
    val aliasAppkey = AppkeyAlias.aliasAppkey(appkey)
    val errorCount = logService.getErrorCount(aliasAppkey, new Date(start * 1000L), new Date(end * 1000L))
    if (null == errorCount) {
      0l
    } else {
      errorCount.toLong
    }
  }

  private def getDaysCount(appkey: String, start: Int, end: Int) = {
    val env = "prod"
    val role = "server"
    val group = "span"
    val spanname = "all"
    val localhost = "all"
    val remoteAppkey = "all"
    val remoteHost = "all"
    val unit = "Day"
    val protocol = "thrift"
    val dataSource = "hbase"

    val drOption = DataQuery.getDataRecord(appkey, start, end, protocol, role, null, env, unit, group, spanname,
      localhost, remoteAppkey, remoteHost, dataSource)
    //  parse result & store
    val list = drOption.flatMap { records =>
      records.headOption.map { record =>
        val totalCount = record.count.map(_.y.getOrElse(0D).toLong).sum
        totalCount
      }
    }
    list.getOrElse(0l)
  }

  def getErrorLog(owt: String, weekDay: Date, limit: Int) = {
    ReportErrorLogDao.query(owt, weekDay, limit)
  }

  private def getWeekDay(date: DateTime, days: Int = 6) = {
    val start = date.withTimeAtStartOfDay().withDayOfWeek(1)
    (0 to days).map {
      d =>
        val day = start.plusDays(d)
        day
    }.toList
  }


}

@DisallowConcurrentExecution
class DailyErrorJob extends Job {

  import ErrorLogTimerTask.logger

  //  获取业务线 -> appkey list
  private val owtToDescMap = ReportHelper.getOwtToDescMap

  @throws(classOf[JobExecutionException])
  override def execute(ctx: JobExecutionContext): Unit = {
    calculate()
  }

  def calculate(timeMillis: Long = System.currentTimeMillis()): Unit = {
    val (start, end) = TaskTimeHelper.getStartEnd(timeMillis)

    logger.info(s"DailyErrorJob begin,start:$start,end:$end")

    //  计算业务线服务最近7日errorlog
    owtToDescMap.foreach { case (owt, apps) =>
      ErrorLogTimerTask.asyncComputeErrorLog(apps, start, end)

    }
  }


}
