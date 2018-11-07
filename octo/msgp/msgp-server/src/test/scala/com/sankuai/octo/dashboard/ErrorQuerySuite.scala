package com.sankuai.octo.dashboard

import com.sankuai.octo.msgp.serivce.data.ErrorQuery
import com.sankuai.octo.msgp.serivce.falcon.AlarmQuery
import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner
import org.springframework.test.context.web.WebAppConfiguration
import org.springframework.test.context.{ContextConfiguration, TestContextManager}

/**
 * Created by zava on 16/5/17.
 */
@RunWith(classOf[JUnitRunner])
@ContextConfiguration(locations = Array("classpath*:applicationContext*.xml",
  "classpath*:webmvc-config.xml",
  "classpath*:mybatis*.xml"))
@WebAppConfiguration
class ErrorQuerySuite extends FunSuite {
  new TestContextManager(this.getClass).prepareTestInstance(this)

  test("refreshErrorDashBoard") {
    val count =  ErrorQuery.refreshErrorDashBoard(1463986140,1463986200)
    println(count)
  }

  test("query") {
    //   val list =  AlarmQuery.querySrvAlarm("corp=meituan&owt=dba",3600)
    val list =  ErrorQuery.queryAlarm(1463743500,1463747040,"errlog_count desc",10)
    println(list)
  }

  test("start") {
    ErrorQuery.start()
  }
  test("count") {
    ErrorQuery.calculationData(1463646720,1463646780)
  }

  test("getAppkeyFalconHistory") {
//    val data = ErrorQuery.getAppkeyFalconHistory("com.sankuai.inf.logCollector",1463898857,1464075257)
//    println(data)
  }
  test("countFalcon") {
    //   val list =  AlarmQuery.querySrvAlarm("corp=meituan&owt=dba",3600)
//    val list =  ErrorQuery.countFalcon(1463646720,1463646780)
//    println(list)
  }
  test("searhFalcon") {
    val data  = AlarmQuery.query(duration = "180")
    println(data)
  }
}