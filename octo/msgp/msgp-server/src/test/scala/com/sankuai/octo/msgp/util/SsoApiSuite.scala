package com.sankuai.octo.msgp.util

import com.sankuai.msgp.common.service.org.SsoService
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{BeforeAndAfter, FunSuite}


@RunWith(classOf[JUnitRunner])
class SsoApiSuite extends FunSuite with BeforeAndAfter {

  test("get user") {
    println(SsoService.getUser("plus"))

  }
  test("get userid") {
    println(SsoService.getUserByIds("64137"))
  }
}
