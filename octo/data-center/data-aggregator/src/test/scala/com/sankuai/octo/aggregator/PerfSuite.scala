package com.sankuai.octo.aggregator


import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{BeforeAndAfter, FunSuite}

@RunWith(classOf[JUnitRunner])
class PerfSuite extends FunSuite with BeforeAndAfter {

  test("perf create & get") {
    val id = perf.getToken("com.sankuai.pay.paytair")
    println(id)
  }
}
