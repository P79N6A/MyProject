package com.sankuai.octo.query.scala

import com.sankuai.octo.query.falconData.FalconHistoryData
import com.sankuai.octo.query.helper.{QueryCondition, QueryHelper}
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{BeforeAndAfter, FunSuite}

@RunWith(classOf[JUnitRunner])
class FalconHistoryDataSuite extends FunSuite with BeforeAndAfter {

  test("historyData") {
    val appkey1 = "octo.sankuai.inf.octo.overloadTestClient"
    val appkey2 = "octo.sankuai.inf.octo.overloadTestClient"
    val env = "prod"
    val start = (System.currentTimeMillis() / 1000 - 50 * 60).toInt
    val end = (System.currentTimeMillis() / 1000 - 5 * 60).toInt
    val source = "server"
    val spanname = "all"
    val localHost = "all"
    val remoteAppkey = "all"
    val remoteHost = "all"
    val queryCondition: QueryCondition = QueryHelper.transformQueryCondition(spanname, localHost, remoteHost, remoteAppkey)
    println(FalconHistoryData.historyData(appkey1, env, start, end, source, queryCondition, "qps"))
    println(FalconHistoryData.historyData(appkey2, env, start, end, source, queryCondition, "qps"))
  }

  test("historyData_remoteApp") {
    val appkey1 = "com.sankuai.inf.logCollector"
    val appkey2 = "com.sankuai.inf.mnsc"
    val env = "prod"
    val start = 1446034077
    val end = 1446034377
    val source = "server"
    val spanname = "all"
    val localHost = "all"
    val remoteAppkey = "*"
    val remoteHost = "all"
    val queryCondition: QueryCondition = QueryHelper.transformQueryCondition(spanname, localHost, remoteHost, remoteAppkey)
    println(FalconHistoryData.historyData(appkey1, env, start, end, source, queryCondition, "qps"))
    println(FalconHistoryData.historyData(appkey2, env, start, end, source, queryCondition, "qps"))
    Thread.sleep(5000)
  }

  test("com.sankuai.hotel.bizapp") {
    val appkey = "com.sankuai.hotel.bizapp"
    val env = "prod"
    val start = 1445930454
    val end = 1445931354
    val source = "server"
    val spanname = "all"
    val localHost = "all"
    val remoteAppkey = "*"
    val remoteHost = "all"
    val queryCondition: QueryCondition = QueryHelper.transformQueryCondition(spanname, localHost, remoteHost, remoteAppkey)
    println(FalconHistoryData.historyData(appkey, env, start, end, source, queryCondition, "qps"))
  }

  test("com.sankuai.inf.logCollector") {
    val appkey = "com.sankuai.inf.logCollector"
    val env = "prod"
    val start = 1446048000
    val end = 1446307200
    val source = "server"
    val spanname = "all"
    val localHost = "all"
    val remoteAppkey = "all"
    val remoteHost = "all"
    val queryCondition: QueryCondition = QueryHelper.transformQueryCondition(spanname, localHost, remoteHost, remoteAppkey)
    println(FalconHistoryData.historyData(appkey, env, start, end, source, queryCondition, "qps"))
  }
}
