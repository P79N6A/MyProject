package com.sankuai.msgp.task

import com.sankuai.msgp.common.utils.ReportHelper
import org.scalatest.{BeforeAndAfter, FunSuite}

/**
  * Created by yves on 17/1/11.
  */
class ReportHelperSuite extends FunSuite with BeforeAndAfter {

  test("apps"){
    val owtDescMap = ReportHelper.getOwtToDescMap
    if(owtDescMap.nonEmpty) println("owtDescMap is not empty.") else println("owtDescMap is empty.")
    Thread.sleep(10000)
  }
}
