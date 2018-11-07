package com.sankuai.octo

import com.sankuai.octo.statistic.util.CronScheduler
import org.quartz._
import org.scalatest.FunSuite

/**
 * Created by wujinwu on 15/9/23.
 */
class CronSchedulerTest extends FunSuite {

  test("testScheduleJob") {
    val cron = "*/1 * * * * ? "
    val job: JobDetail = JobBuilder.newJob(classOf[TestJob]).build()
    val trigger: Trigger = TriggerBuilder.newTrigger().startNow()
      .withSchedule(CronScheduleBuilder.cronSchedule(cron)).build()
    (1 to 10).foreach(_ => CronScheduler.scheduleJob(job, trigger))
    Thread.sleep(100000)
  }

}

@DisallowConcurrentExecution
class TestJob extends Job {
  override def execute(context: JobExecutionContext): Unit = {
    println(s"tname:${Thread.currentThread().getName} hello world")
  }
}