package com.sankuai.octo.graph

import com.sankuai.msgp.common.utils.helper.JsonHelper
import com.sankuai.octo.msgp.serivce.data.DataQuery
import com.sankuai.octo.msgp.serivce.graph.{ServiceView, ViewCache}
import com.sankuai.octo.msgp.serivce.service
import org.joda.time.DateTime
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{BeforeAndAfter, FunSuite}

@RunWith(classOf[JUnitRunner])
class ServiceViewSuite extends FunSuite with BeforeAndAfter {

  test("idcInfo") {
    println(JsonHelper.dataJson(ServiceView.getIdcInfo(100,"all")))
  }

  test("updateGraphDataIDC") {
    val services = service.ServiceCommon.listService
     ViewCache.updateGraphDataIDC(100,services)
  }


  test("new apps") {
    println(JsonHelper.dataJson(ServiceView.getNewApps(2000, 7)))
  }

  test("worst perf apis") {
    println(JsonHelper.dataJson(ServiceView.getPerfWorstAPI(100, 20)))
  }

  test("new spannames") {
    val startTime = new DateTime()
    println(JsonHelper.dataJson(ServiceView.getNewSpannames(100)))
  }

  //修改DataQuery获取线上数据验证
  test("getAppCallIDCFromDataCenter") {
    val appkey = "com.sankuai.inf.mnsc"
    println(JsonHelper.dataJson(DataQuery.getAppCallIDCFromDataCenter(appkey, "server")))
  }
}
