/*
package com.sankuai.octo.statistic.metric

import com.sankuai.octo.processor.MinuteMetricProcessorSuite
import org.joda.time.DateTime
import org.scalatest.FunSuite

/**
  * Created by wujinwu on 16/5/19.
  */
class MetricProcessorTest extends FunSuite {
  test("testProcessor") {
    val rand = new MinuteMetricProcessorSuite
    val appkey = "testAppkey"
    val spans = Array("testapi.span1", "testapi.span2", "testapi.span3", "testapi.span4", "testapi.span5")
    val now = DateTime.now()
    val tomorrowSecond = (now.plusDays(1).withTimeAtStartOfDay().withMinuteOfHour(5).getMillis / 1000).toInt
    println(tomorrowSecond)
    val nowSecond = (now.getMillis / 1000L).toInt
    val initialDelay = tomorrowSecond - nowSecond
    println(initialDelay)
    for (span <- spans) {
      (1 to 200000000).foreach { _ =>
        val testMetric = rand.randomMetric()
        MetricProcessor.putMetric(testMetric)
        Thread.sleep(10)
      }
    }
  }
}
*/
