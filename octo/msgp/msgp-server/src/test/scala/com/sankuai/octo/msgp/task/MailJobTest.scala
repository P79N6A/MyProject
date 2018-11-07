package com.sankuai.octo.msgp.task

import com.sankuai.octo.msgp.utils.FreeMarkerTemplate
import org.scalatest.FunSuite
import org.springframework.test.context.{TestContextManager, ContextConfiguration}

@ContextConfiguration(locations = Array("classpath*:applicationContext.xml",
  "classpath*:webmvc-config.xml",
  "classpath*:applicationContext-*.xml",
  "classpath*:mybatis*.xml"))
class MailJobTest  extends FunSuite {
  new TestContextManager(this.getClass).prepareTestInstance(this)

  test("mailJob") {
    val job = new ReportDailyMailJob()
    job.calculate()
    while (true) {
      Thread.sleep(10000)
    }
  }
  test("mailweeklyJob"){
    val job = new ReportWeeklyMailJob()
    job.calculate()
    while (true) {
      Thread.sleep(10000)
    }
  }

}
