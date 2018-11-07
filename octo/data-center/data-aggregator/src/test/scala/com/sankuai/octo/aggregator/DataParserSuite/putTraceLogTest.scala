package com.sankuai.octo.aggregator.DataParserSuite

import com.sankuai.octo.aggregator.parser.DataParser
import com.sankuai.octo.aggregator.thrift.LogCollectorServiceImpl
import com.sankuai.octo.aggregator.thrift.model.SGModuleInvokeInfo
import com.sankuai.octo.aggregator.{StatSuite, IpValidator}
import org.apache.commons.lang.math.RandomUtils
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{BeforeAndAfter, FunSuite}

@RunWith(classOf[JUnitRunner])
class putTraceLogTest extends FunSuite with BeforeAndAfter {

  test("constructMetric") {
    val a = new StatSuite
    val s = new LogCollectorServiceImpl
    println(DataParser.constructMetric(s.thriftSpanTransfer(a.randomInvokeInfo), 1))
  }

  private def randomInvokeInfo = {
    val info: SGModuleInvokeInfo = new SGModuleInvokeInfo
    info.setSpanName("TestController.test")
    info.setLocalAppKey("com.sankuai.inf.logCollector")
    info.setLocalHost("10.12.47.134") // cq-inf-octo-logstat01
    info.setRemoteAppKey("test")
    info.setRemoteHost("10.4.28.104") // yf-inf-octo-mnsc03
    info.setStatus(0)
    info.setCount(1)
    info.setType(1)
    info.setCost(RandomUtils.nextInt(20) + 20)
    info
  }
}
