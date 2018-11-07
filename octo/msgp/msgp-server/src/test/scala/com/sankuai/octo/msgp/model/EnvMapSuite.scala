package com.sankuai.octo.msgp.model

import com.sankuai.msgp.common.utils.helper.CommonHelper
import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class EnvMapSuite extends FunSuite {

  test("EnvMap.isValid") {
    if (CommonHelper.isOffline) {
      // test offline
      val validList = List("dev", "Dev", "DEV", "ppe", "test")
      val invalidList = List("prod", "pRod", "staging", "asdfs", "")
      assert(validList.forall(EnvMap.isValid))
      assert(invalidList.foldLeft(false)((result, item) => (!result) && (!EnvMap.isValid(item))))
      assert(!EnvMap.isValid(null))
    } else {
      //test online

      val validList = List("prod", "Prod", "PROD", "staging")
      val invalidList = List("dev", "Dev", "ppe", "test", "")
      assert(validList.forall(EnvMap.isValid))
      assert(invalidList.foldLeft(false)((result, item) => (!result) && (!EnvMap.isValid(item))))
    }
  }
}
