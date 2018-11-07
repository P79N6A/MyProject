package com.sankuai.octo

import com.sankuai.octo.msgp.serivce.service.{ServiceCommon, ServiceProvider}
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{BeforeAndAfter, FunSuite}

@RunWith(classOf[JUnitRunner])
class serviceCommonSuite extends FunSuite with BeforeAndAfter {
  test("apidesc") {
    val apiDesc = ServiceCommon.apiDesc("com.sankuai.cos.mtconfig")
    println(apiDesc)
  }

  test("getAliveRatio") {
    ServiceProvider.getAliveRatio("com.sankuai.inf.msgp", 1481385600)
  }

  test("keyword") {
    val data = ServiceCommon.search("msgp")
    println(data)
  }
  test("checknode"){
    val checkNodes = ServiceCommon.checkMnsNode()
    println(checkNodes)
  }
}
