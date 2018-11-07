package com.sankuai.octo.msgp.task

import com.sankuai.msgp.common.utils.StringUtil
import com.sankuai.msgp.common.utils.helper.TaskTimeHelper
import com.sankuai.octo.SpringTest
import com.sankuai.octo.msgp.serivce.service.ServiceCommon
import com.sankuai.octo.mworth.util.DateTimeUtil
import com.sankuai.octo.mworth.utils.CronScheduler
import org.joda.time.DateTime
import org.junit.runner.RunWith
import org.quartz.{CronScheduleBuilder, JobBuilder, Trigger, TriggerBuilder}
import org.scalatest._
import org.scalatest.junit.JUnitRunner
import org.springframework.test.context.{ActiveProfiles, ContextConfiguration, TestContextManager}

/**
  * Created by wujinwu on 16/3/10.
  */


@RunWith(classOf[JUnitRunner])
class ErrorTimerTaskTest extends FunSuite with SpringTest with Matchers{
  test("testCron") {
    val job = JobBuilder.newJob(classOf[DailyErrorJob]).build()
    val trigger: Trigger = TriggerBuilder.newTrigger().startNow()
      .withSchedule(CronScheduleBuilder.cronSchedule("*/5 * * * * ?")).build()
    CronScheduler.scheduleJob(job, trigger)
    while (true) {
      Thread.sleep(10000)
    }
  }
  test("calculate"){
    val job = new DailyErrorJob()
    val day = getDate("2016-03-14")
    job.calculate(day.getMillis + 7*86400*1000)
  }

  test("asyncComputeErrorLog"){
    val timeMillis =1489290702000L
    val (start, end) = TaskTimeHelper.getStartEnd(timeMillis)
    val list = List("com.meituan.xg.client.d.shop","com.meituan.xg.shopgoods","com.meituan.xg.wms.outboundorder","cip-recwiki-service","com.meituan.xg.shop.storeinfo","com.meituan.xg.shop.credential","com.sankuai.ugc.ugcreviewweb","com.meituan.xg.client.c","com.meituan.fugu.web.statistics","poi-bobo-web")
    val apps = list.map{
      x=>ServiceCommon.desc(x)
    }
    ErrorLogTimerTask.asyncComputeErrorLog(apps, start, end)
    while (true) {
      Thread.sleep(10000)
    }
  }
  private def getDate(day: String): DateTime = {
    var time: Long = System.currentTimeMillis
    if (StringUtil.isNotBlank(day)) {
      time = DateTimeUtil.parse(day, DateTimeUtil.DATE_DAY_FORMAT).getTime
    }
    return new DateTime(time)
  }

}

