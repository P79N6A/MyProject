package com.sankuai.octo.msgp.controller

//import com.sankuai.octo.msgp.serivce.service.ServiceCutFlow
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{BeforeAndAfter, FunSuite}
import org.springframework.test.context.{ContextConfiguration, TestContextManager}

@RunWith(classOf[JUnitRunner])
@ContextConfiguration(locations = Array("classpath*:applicationContext.xml",
  "classpath*:applicationContext-*.xml",
  "classpath*:webmvc-config.xml",
  "classpath*:mybatis*.xml"))
class TestCutFlow extends FunSuite with BeforeAndAfter {
  new TestContextManager(this.getClass).prepareTestInstance(this)

  test("checkAll") {
    //CutFlowService.monitorCutFlow();
    //ServiceCutFlow.monitorCutFlow()
  }
}
