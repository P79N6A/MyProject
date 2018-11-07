package com.sankuai.octo.msgp.task

import com.sankuai.msgp.common.utils.StringUtil
import com.sankuai.octo.mworth.util.DateTimeUtil
import com.sankuai.octo.mworth.utils.CronScheduler
import org.joda.time.DateTime
import org.quartz.{CronScheduleBuilder, JobBuilder, TriggerBuilder}
import org.scalatest.FunSuite

/**
  * Created by wujinwu on 16/3/11.
  */
class IdcTrafficTimerTaskTest extends FunSuite {

  test("testCron") {
    val job = JobBuilder.newJob(classOf[DailyIdcTrafficJob]).build()
    val trigger = TriggerBuilder.newTrigger().startNow()
      .withSchedule(CronScheduleBuilder.cronSchedule("*/5 * * * * ?")).build()
    CronScheduler.scheduleJob(job, trigger)
    while (true) {
      Thread.sleep(10000)
    }
  }
  test("calculate"){
    val job = new DailyIdcTrafficJob()
    val day = getDate("2016-03-07")
    job.calculate(day.getMillis + 7*86400*1000)
  }
  private def getDate(day: String): DateTime = {
    var time: Long = System.currentTimeMillis
    if (StringUtil.isNotBlank(day)) {
      time = DateTimeUtil.parse(day, DateTimeUtil.DATE_DAY_FORMAT).getTime
    }
    return new DateTime(time)
  }
}
