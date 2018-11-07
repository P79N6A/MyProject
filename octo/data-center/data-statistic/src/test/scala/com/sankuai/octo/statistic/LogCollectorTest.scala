package com.sankuai.octo.statistic

import com.sankuai.octo.statistic.processor.ExportService
import com.sankuai.octo.statistic.util.config
import org.scalatest.{BeforeAndAfter, FunSuite, Matchers}
import org.springframework.test.context.web.WebAppConfiguration
import org.springframework.test.context.{ContextConfiguration, TestContextManager}

/**
 * Created by wujinwu on 15/7/21.
 */

@ContextConfiguration(
  locations = Array("classpath:applicationContext.xml")
  //  , loader = classOf[AnnotationConfigContextLoader]
)
@WebAppConfiguration
class LogCollectorTest extends FunSuite
//with SpringTestContextManagement
with Matchers with BeforeAndAfter {

//  new TestContextManager(this.getClass).prepareTestInstance(this)

  before {
    Bootstrap.init()
  }

  test("config") {
    val export = config.get("export.list", "")
    val router = CollectorConfig.get("statistic.allocate", "")
    println(s"export:$export")
    println(s"router:$router")
  }
test("appkeyconfig"){
  println(ExportService.isExportAppkey("appkey1"))
  (1 to 10).foreach { i =>
    Thread.sleep(100)
    println(ExportService.isExportAppkey("appkey1"))
  }

}

}