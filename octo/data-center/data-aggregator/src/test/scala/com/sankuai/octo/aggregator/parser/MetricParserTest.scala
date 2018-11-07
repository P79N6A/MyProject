package com.sankuai.octo.aggregator.parser

import java.util
import java.util.UUID

import com.meituan.mtrace.thrift.model.StatusCode
import com.sankuai.octo.aggregator.MafkaService
import com.sankuai.octo.aggregator.thrift.LogCollectorServiceImpl
import com.sankuai.octo.aggregator.thrift.model.SGModuleInvokeInfo
import com.sankuai.octo.statistic.model.{MetricData, MetricKey, PerfProtocolType, StatSource}
import com.sankuai.octo.statistic.util.HessianSerializer
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{BeforeAndAfter, FunSuite}

import scala.util.Random

/**
 * Created by wujinwu on 15/9/6.
 */
@RunWith(classOf[JUnitRunner])
class MetricParserTest extends FunSuite with BeforeAndAfter {

  test("MetricParser") {
    val s = new LogCollectorServiceImpl()
    for (i <- 0 to 1000000) {
      MetricParser.put(DataParser.constructMetric(s.thriftSpanTransfer(randomInvokeInfo), 1))
      Thread.sleep(10)
    }
    Thread.sleep(1000000)
  }

  test("MafkaService") {
    println(MafkaService.isSentToMafka("com.sankuai.inf.msgp"))
  }

  test("testSendMetrics") {
    //val statisticService = StatisticServiceProxy("com.sankuai.inf.logCollector")
    val mafkaService = MafkaService.getMafkaProducer("waimai_api_test")
    val appkeys = Array("waimai_api_test")
    //    val appkeys = Array("com.sankuai.inf.test4.logCollector","com.sankuai.inf.test5.logCollector","com.sankuai.inf.test6.logCollector")
    val spans = Array("testapi.span1", "testapi.span2", "testapi.span3", "testapi.span4", "testapi.span5")
    (1 to 200).foreach { _ =>
       for (appkey <- appkeys) {
         for (span <- spans) {
           (1 to 200).foreach { _ =>
             val testMetric = new com.sankuai.octo.statistic.model.Metric(new MetricKey(appkey, span, "localHost",
               "remoteAppKey", "remoteHost", StatSource.Server, PerfProtocolType.THRIFT, PerfProtocolType.THRIFT.toString), getRandomMetrics)
             //statisticService.sendMetrics(List(testMetric))
             val message = HessianSerializer.serialize(testMetric)
             mafkaService.sendAsyncMessage(message)
           }
         }
       }
     }
  }

  def getRandomMetrics: util.ArrayList[MetricData] = {
    val list = new util.ArrayList[MetricData]()
    list.add(new MetricData(System.currentTimeMillis(), 1, 100, StatusCode.SUCCESS))
    list.add(new MetricData(System.currentTimeMillis(), 1, 50, StatusCode.SUCCESS))
    list.add(new MetricData(System.currentTimeMillis(), 1, 5, StatusCode.SUCCESS))
    list
  }

  def randomInvokeInfo: SGModuleInvokeInfo = {
    val strs = Array[String]("com.sankuai.inf1.logCollector", "com.sankuai.waimai1.poi", "com.sankuai.waimai.poi1.cview")

    val info: SGModuleInvokeInfo = new SGModuleInvokeInfo
    info.setTraceId(UUID.randomUUID().toString)
    info.setSpanName(strs(Random.nextInt(strs.length)))
    info.setLocalAppKey(strs(Random.nextInt(strs.length)))
    info.setLocalHost("192.168.1.111")
    info.setRemoteAppKey("com.sankuai.tair.waimai.cbase")
    info.setRemoteHost("192.168.2.222")
    info.setType(Random.nextInt(2))
    info.setStatus(0)
    info.setCount(1)
    info.setStart(System.currentTimeMillis())
    info.setCost(Random.nextInt(20) + 20)
    info
  }
}
