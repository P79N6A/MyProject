package com.sankuai.octo

import com.sankuai.octo.msgp.serivce.AppkeyAlias
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{BeforeAndAfter, FunSuite}

@RunWith(classOf[JUnitRunner])
class AppkeyAliasSuite extends FunSuite with BeforeAndAfter {
  test("appkeyAlias"){
    println(AppkeyAlias.aliasAppkey("dddss"))
  }
}
