package com.sankuai.octo.aggregator

import java.util.UUID

import com.sankuai.octo.aggregator.thrift.model.SGModuleInvokeInfo
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{BeforeAndAfter, FunSuite}

import scala.util.Random

@RunWith(classOf[JUnitRunner])
class SessionSuite extends FunSuite with BeforeAndAfter {

  test("byte") {
    val start: Long = System.currentTimeMillis()
    println(start)
    println(start.toBinaryString)
    println(1L.toBinaryString)
    println(1024L.toBinaryString)
    println(1024L.toString)
    println(1024L.toString.getBytes("utf-8"))
  }

  test("perf") {
    val log = randomInvokeInfo
    log.setLocalAppKey("mobile.sinai")
    log.setRemoteAppKey("mobile.sinai")
    println(perf.isBlock(log))
    while (true) {
      Thread.sleep(10000)
      println(log)
      println(perf.isBlock(log))
    }
  }

  def randomInvokeInfo: SGModuleInvokeInfo = {
    val strs = Array[String]("com.sankuai.inf1.logCollector", "com.sankuai.waimai1.poi", "com.sankuai.waimai.poi1.cview")

    val info: SGModuleInvokeInfo = new SGModuleInvokeInfo
    info.setTraceId(UUID.randomUUID().toString)
    info.setSpanName(strs(Random.nextInt(strs.length)))
    info.setLocalAppKey(strs(Random.nextInt(strs.length)))
    info.setLocalHost("192.168.1.111")
    info.setRemoteAppKey("mtupm.test22")
    info.setRemoteHost("192.168.2.222")
    info.setType(Random.nextInt(2))
    info.setStatus(0)
    info.setCount(1)
    info.setStart(System.currentTimeMillis())
    info.setCost(Random.nextInt(20) + 20)
    info
  }
}
