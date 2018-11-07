package com.sankuai.octo.msgp.task

import java.util.concurrent.{Executors, TimeUnit}

import com.sankuai.msgp.common.model.Business
import com.sankuai.octo.msgp.serivce.component.{ComponentHelper, TrendService}
import org.joda.time.DateTime
import org.quartz.impl.StdSchedulerFactory
import org.quartz.{Job, JobExecutionContext, JobExecutionException, _}
import org.slf4j.LoggerFactory

/**
  * Created by yves on 16/8/10.
  */
object ComponentDailyTask {
  private val scheduler = Executors.newSingleThreadScheduledExecutor()
  private val logger = LoggerFactory.getLogger(this.getClass)
  private val cronScheduler = new StdSchedulerFactory().getScheduler

  def uploadTrendEvent(dateOfYesterday: java.sql.Date) = {
    TrendService.uploadDailyTrend(dateOfYesterday)
  }

  def deleteDeprecatedDependencies() = {
    ComponentHelper.deleteDeprecatedDependencies()
  }

  def deleteInvalidArtifact() = {
    ComponentHelper.deleteInvalidArtifact()
  }

  def updateBusiness() = {
    ComponentHelper.updateBusiness(Business.other.toString, "")
  }

  def start {
    // Event Daily Counter
    scheduleDailyBackupJob()
    // 在jvm退出时优雅关闭
    Runtime.getRuntime.addShutdownHook(new Thread() {
      override def run(): Unit = {
        scheduler.shutdown()
        scheduler.awaitTermination(10, TimeUnit.SECONDS)
      }
    })
  }

  private def scheduleDailyBackupJob() {
    val job: JobDetail = JobBuilder.newJob(classOf[ComponentDailyJob]).build()
    val trigger: Trigger = TriggerBuilder.newTrigger().startNow()
      .withSchedule(CronScheduleBuilder.dailyAtHourAndMinute(0, 30)).build()
    try {
      cronScheduler.scheduleJob(job, trigger)
    } catch {
      case e: Exception => logger.error(s"scheduleJob Fail,job:$job,trigger:$trigger")
    }
  }
}

class ComponentDailyJob extends Job {

  private val logger = LoggerFactory.getLogger(this.getClass)

  @throws(classOf[JobExecutionException])
  override def execute(ctx: JobExecutionContext) {
    logger.info("uploadTrendDailyJob Start!")
    val currentTime = DateTime.now()
    val yesterday = currentTime.minusDays(1)
    val dateOfYesterday = new java.sql.Date(yesterday.withTimeAtStartOfDay().getMillis)
    //上传组件使用趋势
    ComponentDailyTask.uploadTrendEvent(dateOfYesterday)
    //清理废弃依赖
    ComponentDailyTask.deleteDeprecatedDependencies()
    //清理费独立发布项
    ComponentDailyTask.deleteInvalidArtifact()

    ComponentDailyTask.updateBusiness()
  }
}
