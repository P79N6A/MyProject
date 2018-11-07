package com.sankuai.octo.aggregator.util

import com.sankuai.octo.aggregator.utils.ConvertAppkey
import org.scalatest.FunSuite

class ConvertAppkeySuite extends FunSuite {
  test("appkey") {
    ConvertAppkey.start()
    println(ConvertAppkey.needConvert("com.sankuai.tair.waimai.cbase"))
    println(ConvertAppkey.needConvert("com.sankuai.tair.waimai.cbase2"))
    Thread.sleep(10000)
    println(ConvertAppkey.needConvert("com.sankuai.tair.web.msg"))
  }
}
