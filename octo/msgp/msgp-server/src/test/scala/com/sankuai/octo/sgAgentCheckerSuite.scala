package com.sankuai.octo

import com.sankuai.octo.msgp.serivce.sgagent.SgAgentChecker
import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner
import org.springframework.test.context.{ContextConfiguration, TestContextManager}


@RunWith(classOf[JUnitRunner])
@ContextConfiguration(locations = Array("classpath*:applicationContext.xml",
  "classpath*:applicationContext-*.xml",
  "classpath*:webmvc-config.xml",
  "classpath*:mybatis*.xml"))
class sgAgentCheckerSuite  extends FunSuite {
  new TestContextManager(this.getClass).prepareTestInstance(this)


  test("provideGroupByVersion") {
    val appkey = "com.sankuai.inf.sg_agent"
    val version = "sg_agent-v2.3.0"
    val region = "beijing"
    val data  = SgAgentChecker.provideGroupByVersion(appkey,3,version,region)
    println(data)

  }
}
