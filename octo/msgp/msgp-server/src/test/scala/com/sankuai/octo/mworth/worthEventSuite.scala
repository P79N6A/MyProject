package com.sankuai.octo.mworth

import com.sankuai.octo.mworth.dao.worthEvent
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{BeforeAndAfter, FunSuite}

/**
 * Created by zava on 16/2/15.
 */

@RunWith(classOf[JUnitRunner])
class worthEventSuite extends FunSuite with BeforeAndAfter {
  test("count") {
    println(worthEvent.countUserModel(1455465600000L, 1455580800000L))
  }
  test("getBusinessUser") {
    println(worthEvent.getBusinessUser())
  }

  test("updateBusiness") {
    println(worthEvent.updateBusiness("gaojun",4))
  }
}


