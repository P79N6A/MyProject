package com.sankuai.octo.processor

import com.meituan.mtrace.thrift.model.StatusCode
import com.sankuai.octo.statistic.model._
import org.scalatest.FunSuite

import scala.collection.JavaConversions._
import scala.util.Random

class MinuteMetricProcessorSuite extends FunSuite {
  // 需要其他接口实现后才能单测
  /*
    test("testMinute") {
      val start = System.currentTimeMillis()
      (1 to 1000).foreach(_ => {
        MetricProcessor.putMetric(randomMetric())
  //      Thread.sleep(10)
        //            start += 1000 * 60
      })
      println(s"耗时,${System.currentTimeMillis()-start}")

      TimeUnit.SECONDS.sleep(60)
    }
  */

  def randomMetric(start: Long = System.currentTimeMillis()) = {
    val spannamePrefix = "testMethod"
    val localHostPrefix = "testHost"
    val remoteAppKeyPrefix = "testRemoteKey"
    val remoteHostPrefix = "testRemoteHost"

    val metricKey = new MetricKey("com.sankuai.inf.testFalcon6", spannamePrefix + Random.nextInt(2),
      localHostPrefix, remoteAppKeyPrefix + Random.nextInt(2),
      remoteHostPrefix, StatSource.Server, PerfProtocolType.THRIFT, PerfProtocolType.THRIFT.toString)
    val metricData = (1 to Random.nextInt(5000)).map(_ => {
      new MetricData(start, 1, Random.nextInt(1000), StatusCode.SUCCESS)
    })
    val metric = new Metric(metricKey, metricData)
    metric
  }

  test("sushuo"){
    val n = 1900000
    val m = 2000000;
    ( n to m).foreach{
     i =>
        var j = 2;
        while (i % j != 0) {
          j =j+1; // 测试2至i的数字是否能被i整除，如不能就自加
        }
        if (j == i)
        //当有被整除的数字时，判断它是不是自身,若是，则说明是素数
        {
          println(i); // 如果是就打印出数字
        }
    }


  }

}
