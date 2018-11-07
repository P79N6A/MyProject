package com.sankuai.octo.statistic

import com.sankuai.octo.processor.MinuteMetricProcessorSuite
import com.sankuai.octo.statistic.impl.LogStatisticServiceImpl
import com.sankuai.octo.statistic.service.LogStatisticService
import org.scalatest.{BeforeAndAfter, Matchers, BeforeAndAfterAll, FunSuite}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.web.WebAppConfiguration
import org.springframework.test.context.{TestContextManager, ContextConfiguration}
import org.springframework.test.context.support.AnnotationConfigContextLoader

import scala.collection.JavaConverters._
import scala.util.Random

/**
 * Created by wujinwu on 15/7/21.
 */

@ContextConfiguration(
  locations = Array("classpath:applicationContext.xml")
//  , loader = classOf[AnnotationConfigContextLoader]
)
@WebAppConfiguration
class LogStatisticServiceImplTest extends FunSuite with SpringTestContextManagement with Matchers with BeforeAndAfter{

  new TestContextManager(this.getClass).prepareTestInstance(this)
  @Autowired
  val logStatisticService: LogStatisticService= null

  before {
    Bootstrap.init()

  }

  test("testImpl") {
    val suite = new MinuteMetricProcessorSuite()
    (1 to 1000000).foreach { i =>
      val metrics = (1 to 10).map { j =>
        suite.randomMetric()
      }
      val javaArg = metrics.asJava
      logStatisticService.sendMetrics(javaArg)
//      Thread.sleep(1000)
    }
  }

  test("dropmetric"){
    val logStatisticServiceImpl =   logStatisticService.asInstanceOf[LogStatisticServiceImpl]
    (1 to 1000000).foreach { i =>
//      val data = logStatisticServiceImpl.dropMetric(Random.nextInt(i))
//      println(data)
//      Thread.sleep(10)
    }
  }
}