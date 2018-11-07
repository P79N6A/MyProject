package com.sankuai.octo.msgp.task

import com.sankuai.octo.msgp.serivce.service.ServiceProvider
import com.sankuai.octo.mworth.util.DateTimeUtil
import com.sankuai.octo.mworth.utils.CronScheduler
import org.junit.runner.RunWith
import org.quartz.{CronScheduleBuilder, JobBuilder, Trigger, TriggerBuilder}
import org.scalatest.junit.JUnitRunner
import org.scalatest.{BeforeAndAfter, FunSuite}
import org.springframework.test.context.web.WebAppConfiguration
import org.springframework.test.context.{ContextConfiguration, TestContextManager}

/**
 * Created by zava on 16/4/26.
 */

@RunWith(classOf[JUnitRunner])
@ContextConfiguration(locations = Array(
  "classpath*:applicationContext*.xml",
  "classpath*:webmvc-config.xml",
  "classpath*:mybatis*.xml"))
@WebAppConfiguration
class ReportDailyTaskTest extends FunSuite with BeforeAndAfter {

  new TestContextManager(this.getClass).prepareTestInstance(this)
  //测试服务
  test("test") {
    val data = ReportDailyTask.computeReportDaily("banma", "com.sankuai.banma.package.admin", "all", 1498492800, 1498665600)
    println(data)
    Thread.sleep(10000)
  }

  test("testCron") {
    val job = JobBuilder.newJob(classOf[ReportDailyJob]).build()
    val trigger: Trigger = TriggerBuilder.newTrigger().startNow()
      .withSchedule(CronScheduleBuilder.cronSchedule("5 * * * * ?")).build()
    CronScheduler.scheduleJob(job, trigger)
    while (true) {
      Thread.sleep(10000)
    }
  }

  test("testDailyMail") {
    val job = new ReportDailyMailJob()
    job.calculate(System.currentTimeMillis())
    Thread.sleep(600000)
  }

  test("testDailyLeader") {
    val job = new ReportDailyStatusResetJob()
    val day = DateTimeUtil.parse("2017-03-11 01:00:00", DateTimeUtil.DATE_TIME_FORMAT)
    job.calculate(day.getTime)
    while (true) {
      Thread.sleep(600000)
    }
  }
  test("testLeaderRefreshDaily") {
    val job = new ReportDailyRefreshJob()
    val day = DateTimeUtil.parse("2016-10-26 01:00:00", DateTimeUtil.DATE_TIME_FORMAT);
    job.calculate(day.getTime)
    while (true) {
      Thread.sleep(600000)
    }
  }
  test("testDaily") {
    val job = new ReportDailyJob()
    val day = DateTimeUtil.parse("2016-10-26 01:00:00", DateTimeUtil.DATE_TIME_FORMAT);
    job.calculate(day.getTime)
    while (true) {
      Thread.sleep(600000)
    }
  }

  test("testMailCron") {
    val job = JobBuilder.newJob(classOf[ReportDailyMailJob]).build()
    val trigger: Trigger = TriggerBuilder.newTrigger().startNow()
      .withSchedule(CronScheduleBuilder.cronSchedule("5 * * * * ?")).build()
    CronScheduler.scheduleJob(job, trigger)
    while (true) {
      Thread.sleep(10000)
    }
  }

  test("provider") {
    val emp = ServiceProvider.getOutlineOfProvider("com.sankuai.inf.msgp", 1, 3, "", -1, null, -8)
    val idcList = emp.idcList
  }

}
