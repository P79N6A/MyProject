package com.sankuai.octo

import org.scalatest.{BeforeAndAfterEach, Suite}
import org.springframework.test.context.web.WebAppConfiguration
import org.springframework.test.context.{ActiveProfiles, ContextConfiguration, TestContextManager}

@ContextConfiguration(locations = Array(
  "classpath*:webmvc-config.xml",
  "classpath*:applicationContext.xml",
  "classpath*:applicationContext-*.xml",
  "classpath*:mybatis*.xml"))
@WebAppConfiguration
@ActiveProfiles(Array("test"))
trait SpringTest extends BeforeAndAfterEach { this: Suite =>

  override def beforeEach(): Unit = {
    new TestContextManager(classOf[SpringTest]).prepareTestInstance(this)
    super.beforeEach()
  }
}
