package com.sankuai.octo.mworth.utils

import org.quartz.impl.StdSchedulerFactory
import org.quartz.{JobDetail, Trigger}
import org.slf4j.LoggerFactory

object CronScheduler {

  private val logger = LoggerFactory.getLogger(this.getClass)

  private val cronScheduler = new StdSchedulerFactory().getScheduler


  cronScheduler.start()
  // 在jvm退出时优雅关闭
  Runtime.getRuntime.addShutdownHook(new Thread() {
    override def run(): Unit = {
      cronScheduler.shutdown(true)
    }
  })

  def scheduleJob(job: JobDetail, trigger: Trigger) {
    try {
      cronScheduler.scheduleJob(job, trigger)
    } catch {
      case e: Exception => logger.error(s"scheduleJob Fail,job:$job,trigger:$trigger")
    }
  }
}
