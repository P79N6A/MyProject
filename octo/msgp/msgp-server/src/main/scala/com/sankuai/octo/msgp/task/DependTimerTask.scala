package com.sankuai.octo.msgp.task

import java.sql.Date
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

import com.sankuai.msgp.common.model.ServiceModels
import com.sankuai.msgp.common.utils.helper.TaskTimeHelper
import com.sankuai.octo.msgp.serivce.data.DataQuery
import com.sankuai.octo.msgp.dao.report.ReportDependDao
import com.sankuai.octo.msgp.dao.report.ReportDependDao.ReportDependDomain
import com.sankuai.octo.msgp.utils.helper.ReportHelper
import com.sankuai.octo.mworth.utils.CronScheduler
import com.sankuai.octo.statistic.constant.Constants
import com.sankuai.octo.statistic.util.ExecutionContextFactory
import org.quartz._
import org.slf4j.LoggerFactory

import scala.concurrent._
import scala.concurrent.duration.Duration


object DependTimerTask {

  implicit val executionContext = ExecutionContextFactory.build(Runtime.getRuntime.availableProcessors() * 8)

  val SERVER = "server"

  val CLIENT = "client"

  private[task] val logger = LoggerFactory.getLogger(this.getClass)

  private val digitsPattern = Pattern.compile(".*\\d+.*")

  private val sourceMap = Map(SERVER -> false, CLIENT -> true)

  def init(): Unit = {
    val job = JobBuilder.newJob(classOf[DailyDependJob]).build()
    val trigger: Trigger = TriggerBuilder.newTrigger().startNow()
      .withSchedule(CronScheduleBuilder.dailyAtHourAndMinute(3, 0)).build()
    CronScheduler.scheduleJob(job, trigger)
  }

  def asyncComputeDependency(apps: List[ServiceModels.Desc], start: Int, end: Int, source: String) = {
    //  async compute
    val future = Future.traverse(apps) { app =>
      Future {
        blocking {
          val count = DependTimerTask.getServiceDependencyCount(app.appkey, start, end, source)
          ReportDependDao.ReportDependDomain(app.business.getOrElse(100), app.owt.getOrElse(""), app.appkey,
            sourceMap.getOrElse(source, false), count,
            TaskTimeHelper.getMondayDate(start), System.currentTimeMillis())
        }
      }
    }
    //  batch insert
    future.foreach { list =>
      batchInsertDependDomains(list)
    }

  }

  private def batchInsertDependDomains(resList: List[ReportDependDomain]) = {
    if (resList != null && resList.nonEmpty) {
      try {
        ReportDependDao.batchInsert(resList)
      } catch {
        case e: Exception =>
          logger.error("batchInsertErrorDomains fail", e)
      }
    }
  }

  /**
    * 获取服务依赖,或 被依赖的数目
    *
    * @param appKey 服务appkey
    * @param start  起始时间
    * @param end    终止时间
    * @param source client or server
    */
  def getServiceDependencyCount(appKey: String, start: Int, end: Int, source: String) = {
    val env = "prod"
    val metricsTags = DataQuery.tags(appKey, start, end, env, source)
    val count = if (metricsTags.remoteAppKeys.nonEmpty) {
      metricsTags.remoteAppKeys.filter(_ != Constants.ALL).count(hasNoDigit)
    } else {
      0
    }
    count
  }

  private def hasNoDigit(content: String) = {
    val m = digitsPattern.matcher(content)
    !m.matches()

  }

  def getDependency(owt: String, source: String, weekDay: Date, limit: Int) = {
    ReportDependDao.query(owt, sourceMap.getOrElse(source, false), weekDay, limit)
  }


}

@DisallowConcurrentExecution
class DailyDependJob extends Job {

  import DependTimerTask.logger

  private implicit val timeout = Duration.create(20L, TimeUnit.SECONDS)

  //  获取业务线 -> appkey list
  private val owtDescMap = ReportHelper.getOwtToDescMap

  @throws(classOf[JobExecutionException])
  override def execute(ctx: JobExecutionContext): Unit = {
    calculate()
  }

  def calculate(timeMillis: Long = System.currentTimeMillis()): Unit = {
    val (start, end) = TaskTimeHelper.getStartEnd(timeMillis)

    logger.info(s"DailyDependJob begin,start:$start,end:$end")

    calculate(start, end, "server")
    calculate(start, end, "client")
  }

  def calculate(start: Int, end: Int, source: String) {

    //  计算业务线服务最近7日依赖关系
    owtDescMap.foreach { case (owt, apps) =>
      DependTimerTask.asyncComputeDependency(apps, start, end, source)
      }
  }
}
