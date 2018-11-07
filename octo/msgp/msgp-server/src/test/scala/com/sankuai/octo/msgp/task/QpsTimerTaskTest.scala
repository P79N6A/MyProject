package com.sankuai.octo.msgp.task

import com.sankuai.msgp.common.utils.StringUtil
import com.sankuai.octo.mworth.util.DateTimeUtil
import com.sankuai.octo.mworth.utils.CronScheduler
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import org.quartz.{CronScheduleBuilder, JobBuilder, Trigger, TriggerBuilder}
import org.scalatest.FunSuite
import org.springframework.test.context.{ContextConfiguration, TestContextManager}

/**
  * Created by wujinwu on 16/3/10.
  */
@ContextConfiguration(locations = Array("classpath*:applicationContext.xml",
  "classpath*:webmvc-config.xml",
  "classpath*:applicationContext-*.xml",
  "classpath*:mybatis*.xml"))
class QpsTimerTaskTest extends FunSuite {

  /**
    * Use the TestContextManager, as this caches the contexts so that they aren't rebuilt every test. It is configured from the class annotations.
    */
  new TestContextManager(this.getClass).prepareTestInstance(this)

  test("testCron") {
    val job = JobBuilder.newJob(classOf[DailyQpsJob]).build()
    val trigger: Trigger = TriggerBuilder.newTrigger().startNow()
      .withSchedule(CronScheduleBuilder.cronSchedule("*/5 * * * * ?")).build()
    CronScheduler.scheduleJob(job, trigger)
    while (true) {
      Thread.sleep(10000)
    }
  }
  test("calculate"){
    val job = new DailyQpsJob()
    //    val day = getDate("2016-03-14")
    val dtf = DateTimeFormat.forPattern("yyyyMMdd")
    val time = dtf.parseDateTime("20160411")

    job.calculate(time.getMillis)
    val peakJob = new DailyQpsPeakJob()
    val idcJob = new DailyIdcTrafficJob()
    val errorJob = new DailyErrorJob()
    val dependJob = new DailyDependJob()

    peakJob.calculate(time.getMillis)
    idcJob.calculate(time.getMillis)
    errorJob.calculate(time.getMillis)
    dependJob.calculate(time.getMillis)

    Thread.sleep(100000000)
  }
  private def getDate(day: String): DateTime = {
    var time: Long = System.currentTimeMillis
    if (StringUtil.isNotBlank(day)) {
      time = DateTimeUtil.parse(day, DateTimeUtil.DATE_DAY_FORMAT).getTime
    }
    new DateTime(time)
  }

  test("testPdf") {

//    val job = new WeeklyPdfJob()
//    job.calculate()

  }

}
