package com.sankuai.octo.aggregator.processor

import java.util.ArrayList

import com.meituan.mtrace.thrift.model.StatusCode
import com.sankuai.octo.aggregator.parser.MetricParser
import com.sankuai.octo.statistic.model._
import org.scalatest.FunSuite

class StatisticServiceTest extends FunSuite {

  test("getStatisticServerList") {
    val appkeys = List("appkey1", "com.sankuai.inf.logCollector", "waimai_api")
    appkeys.foreach {
      appkey =>
        println(StatisticService.getStatisticServerList(appkey))
    }
  }

  test("testSendMetrics") {
    val appkeys = List("com.sankuai.inf.octo.errorlog")
    (1 to 300).foreach {
      i =>
        appkeys.foreach {
          appkey =>
            val aaa = (1 to 300).map {
              x =>
                val list = new ArrayList[MetricData]()
                list.add(new MetricData(System.currentTimeMillis(), 1, 100, StatusCode.SUCCESS))
                val metric = new Metric(new MetricKey(appkey, "spanname", "localHost",
                  "remoteAppKey", "remoteHost", StatSource.Server, PerfProtocolType.THRIFT, PerfProtocolType.THRIFT.toString), list)
                metric
            }.toList
            MetricParser.sendMetrics(aaa)
        }
        Thread.sleep(1000)
    }

  }
}