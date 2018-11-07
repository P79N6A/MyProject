package com.sankuai.octo.query.scala

import com.sankuai.octo.query.falconData.FalconLastData
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{BeforeAndAfter, FunSuite}

@RunWith(classOf[JUnitRunner])
class FalconLastDataSuite extends FunSuite with BeforeAndAfter {

  test("historyData") {
    val appkey1 = "com.sankuai.inf.logCollector"
    val env = "prod"
    val source = "server"
    val group = "span"
    val spanname = "all"

    println(FalconLastData.lastData(appkey1, env, source, group, spanname))
  }
}
