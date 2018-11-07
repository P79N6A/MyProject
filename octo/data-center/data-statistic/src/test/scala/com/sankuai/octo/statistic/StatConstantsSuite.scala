package com.sankuai.octo.statistic

import org.scalatest.{BeforeAndAfter, Matchers, FunSuite}
import org.springframework.test.context.{TestContextManager, ContextConfiguration}
import org.springframework.test.context.web.WebAppConfiguration

@ContextConfiguration(
  locations = Array("classpath:applicationContext.xml")
)
@WebAppConfiguration
class StatConstantsSuite extends FunSuite with BeforeAndAfter{

//  new TestContextManager(this.getClass).prepareTestInstance(this)

  before {
    Bootstrap.init()
  }
    test("isExportHost"){
      val appkeys = List("waimai_api","com.sankuai.inf.msgp")
      appkeys.foreach{
        appkey=>
          println(StatConstants.isExportHost(appkey))
      }

    }
}
