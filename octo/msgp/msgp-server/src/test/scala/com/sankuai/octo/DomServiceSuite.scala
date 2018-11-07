package com.sankuai.octo

import com.sankuai.octo.msgp.domain.report.NonstandardAppkey
import com.sankuai.octo.msgp.serivce.DomService
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{BeforeAndAfter, FunSuite}

@RunWith(classOf[JUnitRunner])
class DomServiceSuite extends FunSuite with BeforeAndAfter {

  test("user_Nonstandard_appkey"){
    println(DomService.refreshNonstandardAppkeys)
    println(DomService.getNonstandardAppkey("zhangchi11"))
    println(DomService.getNonstandardAppkey("zhaobo04"))
  }

  test("distinct"){
    val aa = List(new NonstandardAppkey("business", "appkey1", List(1,2,3).toArray,"1,2,3"),
      new NonstandardAppkey("business", "appkey1", List(1,2,3).toArray,"1,2,3"),
      new NonstandardAppkey("business", "appkey1", List(1,2,3).toArray,"1,2,3"),
      new NonstandardAppkey("business", "appkey2", List(1,2,3).toArray,"1,2,3"))
    println(aa.distinct.length)

    val abnormalAppkeysSize = 2
    val  appkeysTobeDeletedSize = 1
    val errorLogCountOverThreshold = 0
  }
}
