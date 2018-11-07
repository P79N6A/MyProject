package com.sankuai.octo.msgp.task

import java.util.Date
import java.util.concurrent.Executors

import com.sankuai.msgp.common.model.{Pdl, ServiceModels}
import com.sankuai.msgp.common.service.org.OpsService
import com.sankuai.msgp.common.utils.StringUtil
import com.sankuai.octo.msgp.serivce.monitor.business.{KpiMonitor, KpiMonitorModel}
import com.sankuai.octo.mworth.utils.CronScheduler
import org.quartz._
import org.slf4j.LoggerFactory

import scala.collection.JavaConverters._

object MonitorScheduleTask {
  private val logger = LoggerFactory.getLogger(this.getClass)


  private val scheduledExecutor = Executors.newScheduledThreadPool(1)

  def start = {
    // 启动时同步基线
    KpiMonitorModel.syncBase

    // 定时同步基线
    val syncBaseDataJob = JobBuilder.newJob(classOf[SyncBaseDataJob]).build()
    //每天凌晨10分执行一次
    val syncBaseDataTrigger: Trigger = TriggerBuilder.newTrigger().startNow()
      .withSchedule(CronScheduleBuilder.dailyAtHourAndMinute(0, 10)).build()
    CronScheduler.scheduleJob(syncBaseDataJob, syncBaseDataTrigger)

    // 定时清理基线
    val cleanBaseDataJob = JobBuilder.newJob(classOf[CleanBaseDataJob]).build()
    //每天凌晨执行一次
    val cleanBaseDataTrigger: Trigger = TriggerBuilder.newTrigger().startNow()
      .withSchedule(CronScheduleBuilder.dailyAtHourAndMinute(0, 0)).build()
    CronScheduler.scheduleJob(cleanBaseDataJob, cleanBaseDataTrigger)

    // 执行报警任务
    val allKpiMonitorJob = JobBuilder.newJob(classOf[AllKpiMonitorJob]).build()
    val builder = SimpleScheduleBuilder.simpleSchedule()
      .withIntervalInSeconds(60).repeatForever()
    val allKpiMonitorJobTrigger: Trigger = TriggerBuilder.newTrigger()
      .startAt(new Date(System.currentTimeMillis() + 30 * 1000))
      .withSchedule(builder).build()
    CronScheduler.scheduleJob(allKpiMonitorJob, allKpiMonitorJobTrigger)
  }
   def getAppSre(desc:ServiceModels.Desc) = {
    val owt = desc.owt.getOrElse("")
    val pdl = desc.pdl.getOrElse("")
    if(StringUtil.isNotBlank(owt)){
      val sres = OpsService.getAppSre(new Pdl(owt,pdl)).asScala
      val users = sres.flatMap{
        sre=>
          OpsService.getUser(sre)
      }
      users.toList
    }else{
      List[ServiceModels.User]()
    }

  }
  @DisallowConcurrentExecution
  class SyncBaseDataJob extends Job {
    @throws(classOf[JobExecutionException])
    override def execute(ctx: JobExecutionContext): Unit = {
      KpiMonitorModel.syncBase
    }
  }

  @DisallowConcurrentExecution
  class CleanBaseDataJob extends Job {
    @throws(classOf[JobExecutionException])
    override def execute(ctx: JobExecutionContext): Unit = {
      KpiMonitorModel.emptyBase
    }
  }

  @DisallowConcurrentExecution
  class AllKpiMonitorJob extends Job {
    @throws(classOf[JobExecutionException])
    override def execute(ctx: JobExecutionContext): Unit = {
      KpiMonitor.allKpiMonitor
    }
  }

}
