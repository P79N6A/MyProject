package com.sankuai.octo.msgp.controller

import com.sankuai.octo.msgp.serivce.data.PortraitService
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{BeforeAndAfter, FunSuite}

//@RunWith(classOf[JUnitRunner])
//class TestPortraitService extends FunSuite with BeforeAndAfter {
//  val appkey1 = "com.sankuai.inf.logCollector"
//  val appkey2 = "com.sankuai.inf.mnsc"
//  test("TestPortraitService") {
//    println(PortraitService.extendTag(List(appkey1, appkey2)))
//  }
//}


@RunWith(classOf[JUnitRunner])
class TestPortraitService extends FunSuite with BeforeAndAfter {
  val appkey1 = "com.sankuai.inf.logCollector"
  val appkey2 = "com.sankuai.inf.mnsc"
  val appkey3 = "com.sankuai.inf.mnsc"
  val appkey4 = "com.sankuai.inf.octo.scannerdetector"
  val appkey5 = "com.sankuai.bi.ocean.config"
  test("TestPortraitService") {
    println(PortraitService.changeFormatTestApi())
  }
}