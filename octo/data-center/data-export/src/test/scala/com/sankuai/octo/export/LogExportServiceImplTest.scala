package com.sankuai.octo.export

;

import com.sankuai.octo.processor.MinuteMetricProcessorSuite
import com.sankuai.octo.statistic.domain._
import com.sankuai.octo.statistic.metrics.SimpleCountHistogram2
import com.sankuai.octo.statistic.model._
import com.sankuai.octo.statistic.service.LogExportService
import org.scalatest.{BeforeAndAfter, FunSuite, Matchers}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.web.WebAppConfiguration
import org.springframework.test.context.{ContextConfiguration, TestContextManager}

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
class LogExportServiceImplTest extends FunSuite with SpringTestContextManagement with Matchers with BeforeAndAfter {

  new TestContextManager(this.getClass).prepareTestInstance(this)
  @Autowired
  val logExportService: LogExportService = null

  before {
    Bootstrap.init()
  }

  test("sendDailyData") {
    val suite = new MinuteMetricProcessorSuite()
    val appkey = "com.sankuai.inf.octo.errorlog"
    val spanname = "spanname"
    val name = s"$appkey|$spanname|1492704000"
    val map = (1 to 10).map {
      i =>
        Integer.valueOf(i) -> java.lang.Long.valueOf(Random.nextInt(10 * i))
    }.toMap
    (1 to 10).foreach { i =>

      val reservoir3 = new SimpleCountReservoir3(1000, map.asJava)
      val histogram3 = new SimpleCountHistogram3(1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, reservoir3)
      histogram3.createTime = 1493112300L
      logExportService.sendDailyData(appkey, name, histogram3)
      Thread.sleep(100)
    }
    Thread.sleep(3000000)
  }

  test("sendGroupRangeData") {
    val suite = new MinuteMetricProcessorSuite()
    val appkey = "com.sankuai.inf.octo.errorlog"
    val spanname = "spanname"
  val now = System.currentTimeMillis()
    (1 to 10).foreach { i =>
      val map = (1 to 10).map {
        i =>
          Integer.valueOf(i) -> java.lang.Long.valueOf(Random.nextInt(10 * i))
      }.toMap
      val name = s"$appkey|$spanname|1492704000"
      val reservoir3 = new SimpleCountReservoir3(1000, map.asJava)
      val histogram3 = new SimpleCountHistogram3(1, 1, 1, 1, 1, 1, 1, 1, 1, 1, now, now, reservoir3)
      val instanceKey: InstanceKey3 = new InstanceKey3(appkey, StatEnv.Prod, StatSource.Server, PerfProtocolType.THRIFT)
      val statTag = new StatTag3("spanname", "127.0.0.1", "127.0.0.1", "com.sankuai.inf.octo.errorlog.mock", "")
       StatRange.values().filter(_ != StatRange.Day).foreach{
        rang =>
          val groupKey = new GroupKey3(1492753080, rang, StatGroup.Span, statTag)
          val instance = new Instance3(instanceKey, groupKey, histogram3)
          logExportService.sendGroupRangeData(instance)
      }
    }
    Thread.sleep(300000)
  }


}