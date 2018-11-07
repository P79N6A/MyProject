package com.sankuai.octo.msgp.task

import com.sankuai.msgp.common.utils.StringUtil
import com.sankuai.octo.mworth.util.DateTimeUtil
import com.sankuai.octo.mworth.utils.CronScheduler
import org.joda.time.DateTime
import org.quartz.{CronScheduleBuilder, JobBuilder, Trigger, TriggerBuilder}
import org.scalatest.FunSuite

/**
  * Created by wujinwu on 16/3/10.
  */
class DependTimerTaskTest extends FunSuite {

  test("testCron") {
    val job = JobBuilder.newJob(classOf[DailyDependJob]).build()
    val trigger: Trigger = TriggerBuilder.newTrigger().startNow()
      .withSchedule(CronScheduleBuilder.cronSchedule("*/5 * * * * ?")).build()
    CronScheduler.scheduleJob(job, trigger)
    while (true) {
      Thread.sleep(10000)
    }
  }
  test("calculate"){
    val job = new DailyDependJob()
    job.calculate()
  }
  private def getDate(day: String): DateTime = {
    var time: Long = System.currentTimeMillis
    if (StringUtil.isNotBlank(day)) {
      time = DateTimeUtil.parse(day, DateTimeUtil.DATE_DAY_FORMAT).getTime
    }
    new DateTime(time)
  }

}
