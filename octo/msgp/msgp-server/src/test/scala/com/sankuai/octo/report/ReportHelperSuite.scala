package com.sankuai.octo.report

import com.sankuai.msgp.common.service.org.OrgSerivce
import com.sankuai.octo.msgp.serivce.subscribe.AppkeySubscribe
import com.sankuai.octo.msgp.utils.helper.ReportHelper
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{BeforeAndAfter, FunSuite}

@RunWith(classOf[JUnitRunner])
//@ContextConfiguration(locations = Array("classpath*:applicationContext*.xml",
//  "classpath*:webmvc-config.xml",
//  "classpath*:mybatis*.xml"))
//@WebAppConfiguration
class ReportHelperSuite extends FunSuite with BeforeAndAfter {
//  new TestContextManager(this.getClass).prepareTestInstance(this)

  test("errorCount") {
    val count =  ReportHelper.errorCount(1500179520,1500179580)
    println(count)
  }

  test("get") {
    println(AppkeySubscribe.getSubscribeForDailyReport("yangrui08"))
  }

  test("org"){
    val heads = OrgSerivce.getHeadList(57704)
    println(heads)
  }

}

