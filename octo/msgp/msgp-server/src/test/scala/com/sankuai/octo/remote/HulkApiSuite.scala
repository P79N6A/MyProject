package com.sankuai.octo.remote

import com.sankuai.msgp.common.service.hulk.HulkApiService
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{BeforeAndAfter, FunSuite}

@RunWith(classOf[JUnitRunner])
class HulkApiSuite extends FunSuite with BeforeAndAfter {
  test("getName") {
    HulkApiService.start()
    Thread.sleep(5000)
    println(HulkApiService.ip2Host("10.4.247.95"))
    println(HulkApiService.host2ip("set-zone0-com-meituan-mtrace-web-1887"))
  }

  test("ip2name") {
    println(HulkApiService.ipname(List("10.4.247.95","10.32.217.234")))
  }
  test("checkDeleteAppkey"){
    val data = HulkApiService.checkDeleteAppkey("com.sankuai.inf.msgp")
    println(data)
  }
}
