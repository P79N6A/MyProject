package com.sankuai.octo.msgp.controller

import com.sankuai.octo.msgp.serivce.service.AppkeyProviderService
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{BeforeAndAfter, FunSuite}
import org.springframework.test.context.{ContextConfiguration, TestContextManager}

/**
 * Created by zava on 16/8/16.
 */
@RunWith(classOf[JUnitRunner])
@ContextConfiguration(locations = Array("classpath*:applicationContext.xml",
  "classpath*:applicationContext-*.xml",
  "classpath*:webmvc-config.xml",
  "classpath*:mybatis*.xml"))
class thriftCheckerSuite extends FunSuite with BeforeAndAfter {
  new TestContextManager(this.getClass).prepareTestInstance(this)

  test("checkApp") {
    val data = AppkeyProviderService.appsCache.get("original")
    println(data)
  }
}
