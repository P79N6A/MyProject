package com.sankuai.octo

import com.sankuai.octo.msgp.serivce.service.{AppkeyProviderService, ServiceConsumer}
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{BeforeAndAfter, FunSuite}
import org.springframework.test.context.{ContextConfiguration, TestContextManager}

/**
  * Created by yves on 16/10/19.
  */

@RunWith(classOf[JUnitRunner])
@ContextConfiguration(locations = Array("classpath*:applicationContext.xml",
  "classpath*:applicationContext-*.xml",
  "classpath*:webmvc-config.xml",
  "classpath*:mybatis*.xml"))
class serviceConsumerSuite extends FunSuite with BeforeAndAfter {
  new TestContextManager(this.getClass).prepareTestInstance(this)

  test("getUnknownServiceListCache") {
    val result = ServiceConsumer.getUnknownServiceListCache("", "yesterday", "*", true)
    Thread.sleep(10000000)
  }

  test("getNodeTypeByIp") {
    val result = AppkeyProviderService.getNodeTypeByIp("10.4.245.36")
  }

  test("getAllIpOfThriftNode") {
    val result = AppkeyProviderService.getAllIpOfThriftNode("mysql")
  }
}
