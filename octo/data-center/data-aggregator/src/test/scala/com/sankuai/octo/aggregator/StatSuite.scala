package com.sankuai.octo.aggregator

import java.util.UUID

import com.sankuai.octo.aggregator.thrift.model.SGModuleInvokeInfo
import org.apache.commons.lang.math.RandomUtils
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{BeforeAndAfter, FunSuite}

@RunWith(classOf[JUnitRunner])
class StatSuite extends FunSuite with BeforeAndAfter {

  test("stat") {
    (1 to 10000).foreach {
      x =>
//        MetricParser.put(DataParser.constructMetric(randomInvokeInfo))
    }
    Thread.sleep(10000)
  }

  def randomInvokeInfo: SGModuleInvokeInfo = {
    val info: SGModuleInvokeInfo = new SGModuleInvokeInfo
    info.setTraceId(UUID.randomUUID().toString)
    info.setSpanName("hello")
    info.setLocalAppKey("com.sankuai.inf.test2")
    info.setLocalHost("192.168.1.111")
    info.setRemoteAppKey("com.sankuai.inf.msgp")
    info.setRemoteHost("192.168.2.222")
    info.setType(1)
    info.setStatus(0)
    info.setCount(Integer.MAX_VALUE)
    info.setStart(System.currentTimeMillis())
    info.setCost(RandomUtils.nextInt(20) + 20)
    info
  }
}
