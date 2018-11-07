package com.sankuai.octo.aggregator.util

import org.scalatest.FunSuite

class ConvertSuite extends FunSuite {

  test("convert") {
    println(Convert.ipToInt("192.168.3.2"))

//    println(com.meituan.mtrace.Convert.ipToInt("192.168.3.2"))


    println(Convert.intToIp(-1062731006))

//    println(com.meituan.mtrace.Convert.intToIp(-1062731006))



    println(Convert.ipToInt("127.0.3.2"))

//    println(com.meituan.mtrace.Convert.ipToInt("127.0.3.2"))


    println(Convert.intToIp(2130707202))

//    println(com.meituan.mtrace.Convert.intToIp(2130707202))
  }
}
