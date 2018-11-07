package com.sankuai.octo.aggregator

import java.nio.charset.StandardCharsets.UTF_8

import com.meituan.mtrace.thrift.model.StatusCode
import com.sankuai.octo.statistic.helper.api
import com.sankuai.octo.statistic.model._
import com.sankuai.octo.statistic.util.common
import org.apache.hadoop.hbase.util.Bytes
import org.scalatest.FunSuite

import scala.collection.JavaConversions._

/**
  * Created by wujinwu on 15/11/26.
  */
class CommonTest extends FunSuite {

  test("testIsOffline") {
    val offline = common.isOffline
    println(s"offline:$offline")
  }

  test("testGetLocalIp") {
    val ip = common.getLocalIp
    println(s"localIp:$ip")
  }

  test("testEqual") {
    val start = System.currentTimeMillis()
    (1 to 100000000).foreach { i =>
      Bytes.toBytes(10000L)
    }
    println(System.currentTimeMillis() - start)
    println(System.currentTimeMillis() - start)
  }
  test("MetricTest") {
    val key = new MetricKey("com.sankuai.inf.data.statistic", "LogStatisticService.sendMetrics", "dx-inf-octo-logstat20",
      "com.sankuai.info.data.logCollecotr", "dx-inf-octo-logCollecotr10", StatSource.Server, PerfProtocolType.THRIFT, PerfProtocolType.THRIFT.toString)
    val data = (1 to 100).map(_ => new MetricData(System.currentTimeMillis(),1, Integer.MAX_VALUE, StatusCode.SUCCESS))
    val list = (1 to 100).map(_ => new Metric(key, data))
    val bytes = api.jsonBytes(list)
    println(s"bytes length:${bytes.length}")
  }
  test("string") {
    val str = "com.sankuai.inf.logCollector"
    val s1 = str.intern()
    val bytes = str.getBytes(UTF_8)
    val s2 = new String(bytes)
    val s3 = s2.intern()
    assert(s1 eq s3)
  }
}
