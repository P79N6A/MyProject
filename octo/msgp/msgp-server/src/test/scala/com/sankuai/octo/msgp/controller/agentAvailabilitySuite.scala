package com.sankuai.octo.msgp.controller

import com.sankuai.octo.msgp.serivce.manage.AgentAvailability
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{BeforeAndAfter, FunSuite}

@RunWith(classOf[JUnitRunner])
//@WebAppConfiguration
//@ContextConfiguration(locations = Array("classpath*:applicationContext.xml",
//  "classpath*:applicationContext-*.xml",
//  "classpath*:webmvc-config.xml",
//  "classpath*:mybatis*.xml"))
class agentAvailabilitySuite extends FunSuite with BeforeAndAfter {
//  new TestContextManager(this.getClass).prepareTestInstance(this)
  test("check sg_agent") {
    AgentAvailability.checkAgent()
    Thread.sleep(10000L)
    println("校验完成")
  }

  test("checkJob") {
    AgentAvailability.checkJob()
    Thread.sleep(10000L)
    println("校验完成")
  }
}
