package com.sankuai.octo.msgp

import com.sankuai.msgp.common.service.org.{SsoService, UserService}
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{BeforeAndAfter, FunSuite}


@RunWith(classOf[JUnitRunner])
class UserServiceSuite extends FunSuite with BeforeAndAfter {

  test("get user") {
    UserService.bindUser("plus")
  }
  test("get userid") {
    println(SsoService.getUserByIds("64137"))
  }
}