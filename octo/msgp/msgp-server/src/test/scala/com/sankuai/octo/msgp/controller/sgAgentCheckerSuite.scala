package com.sankuai.octo.msgp.controller

import com.sankuai.octo.msgp.serivce.sgagent.SgAgentChecker
import org.scalatest.FunSuite
import org.springframework.test.context.{ContextConfiguration, TestContextManager}

@ContextConfiguration(locations = Array(
  "classpath*:applicationContext.xml",
  "classpath*:applicationContext-rabbitmq.xml",
  "classpath*:applicationContext-ehcache.xml",
  "classpath*:applicationContext-thrift.xml",
  "classpath*:mybatis*.xml"
  ))
class sgAgentCheckerSuite  extends FunSuite {
  new TestContextManager(this.getClass).prepareTestInstance(this)

  test("SGAVersionCheck") {
    val appkey = "com.sankuai.inf.sg_agent"
    val envId = 3
    val region = "undefined"
    val data = SgAgentChecker.SGAVersionCheck(appkey, envId, region)
    println(data)

  }

}
