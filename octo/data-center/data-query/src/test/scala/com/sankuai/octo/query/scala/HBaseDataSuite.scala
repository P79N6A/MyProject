package com.sankuai.octo.query.scala

import com.sankuai.octo.query.helper.{QueryCondition, QueryHelper}
import com.sankuai.octo.query.selfData.HbaseData
import com.sankuai.octo.statistic.helper.api
import com.sankuai.octo.statistic.model._
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{BeforeAndAfter, FunSuite}

@RunWith(classOf[JUnitRunner])
class HBaseDataSuite extends FunSuite with BeforeAndAfter {

  test("transformQueryCondition") {
    val spanname = "GetRequest"
    val localhost = "all"
    val remoteAppkey = "*"
    val remoteHost = "all"
    val queryCondition: QueryCondition = QueryHelper.transformQueryCondition(spanname, localhost, remoteHost, remoteAppkey)
    println(queryCondition)
  }
  test("history data") {
    val appkey = "com.sankuai.xm.mbox.api"
    val protocolType = PerfProtocolType.THRIFT.toString
    val role = PerfRole.SERVER.toString
    val dataType = PerfDataType.ALL.toString
    val statEnv = StatEnv.Prod.toString
    val spanname = "ImDownloadController.getChatFileTempUrlPath"
    val localhost = "all"
    val remoteAppkey = "all"
    val remoteHost = "all"
    //    println(HbaseData.historyData(appkey, protocolType, role, dataType, statEnv, range, statGroup, start, end, spanname, localhost, remoteAppkey, remoteHost, "qps"))
    //
    //    val stime = new DateTime().getMillis
    //    println(HbaseData.historyData(appkey, protocolType, role, dataType, statEnv, range, statGroup, start, end, spanname, localhost, remoteAppkey, remoteHost, "qps"))
    //    val etime = new DateTime().getMillis
    //    println(s"cost ${etime - stime}")


    val dayStart = 1477540800
    val dayEnd = 1477562400
    val queryCondition: QueryCondition = QueryHelper.transformQueryCondition(spanname, localhost, remoteHost, remoteAppkey)
    println(api.dataJson(HbaseData.historyData(appkey, protocolType, role, dataType, statEnv, StatRange.Minute.toString, queryCondition, dayStart, dayEnd, "qps")))
  }


  test("history hour data") {
    val appkey = "com.sankuai.cos.mtconfig"
    val protocolType = PerfProtocolType.THRIFT.toString
    val role = PerfRole.SERVER.toString
    val dataType = PerfDataType.ALL.toString
    val statEnv = StatEnv.Prod.toString
    val spanname = "*"
    val localhost = "all"
    val remoteAppkey = "all"
    val remoteHost = "all"

    val hourStart = 1488729600
    val hourEnd = 1488772800
    val queryCondition: QueryCondition = QueryHelper.transformQueryCondition(spanname, localhost, remoteHost, remoteAppkey)
    println(api.dataJson(HbaseData.historyData(appkey, protocolType, role, dataType, statEnv, StatRange.Hour.toString, queryCondition, hourStart, hourEnd, "qps")))
  }
}
