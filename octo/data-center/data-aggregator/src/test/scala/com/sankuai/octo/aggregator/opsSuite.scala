package com.sankuai.octo.aggregator

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{BeforeAndAfter, FunSuite}

@RunWith(classOf[JUnitRunner])
class opsSuite extends FunSuite with BeforeAndAfter {
  test("filter ip") {

    println(IpValidator.filterExternalIp("10.12.47.134"))
    println(IpValidator.filterExternalIp("192.168.13.11"))
    println(IpValidator.filterExternalIp("172.27.13.11"))
    println(IpValidator.filterExternalIp("172.26.13.11"))
    println(IpValidator.filterExternalIp("127.0.0.1"))
    println(IpValidator.filterExternalIp("119.75.217.109"))

    println(IpValidator.filterExternalIp("222.49.238.174"))
  }
}
