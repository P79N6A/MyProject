package com.sankuai.octo.msgp.data

import com.sankuai.octo.msgp.serivce.data.DaPan
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{BeforeAndAfter, FunSuite}

/**
 * Created by zava on 16/3/31.
 */

@RunWith(classOf[JUnitRunner])
class DaPanSuite extends FunSuite with BeforeAndAfter {

  test("hystrixData") {
    println(DaPan.hystrixData("inf",System.currentTimeMillis()-60000,System.currentTimeMillis()))
  }
  test("hostCount") {
    println(DaPan.getHostCount("com.sankuai.inf.mnsc"))
    println(DaPan.getHostCount("waimai_api"))
  }


}