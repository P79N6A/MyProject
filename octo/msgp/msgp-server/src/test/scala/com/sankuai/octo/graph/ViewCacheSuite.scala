package com.sankuai.octo.graph

import com.sankuai.msgp.common.utils.helper.JsonHelper
import com.sankuai.octo.msgp.serivce.graph.ViewDefine.Graph
import com.sankuai.octo.msgp.serivce.graph.{ServiceView, ViewCache}
import com.sankuai.octo.msgp.serivce.service.ServiceCommon
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{BeforeAndAfter, FunSuite}

/**
 * Created by zava on 16/8/19.
 */

@RunWith(classOf[JUnitRunner])
class ViewCacheSuite extends FunSuite with BeforeAndAfter {
  test("buildAppNodeByIDC") {
    val appkey = "com.sankuai.waimai.money"
    val desc = ServiceCommon.desc(appkey)
    println(JsonHelper.dataJson(ViewCache.buildAppNodeByIDC(desc)))
  }

  test("refreshAppNodeCache") {
    println(JsonHelper.dataJson(ViewCache.refreshAppNodeCache))
  }
  test("updateAppNodeIDC") {
    val appkey = "com.sankuai.waimai.poiquery"
    val desc = ServiceCommon.desc(appkey)
    val nodeIDC = ViewCache.buildAppNodeByIDC(desc)
    println(ViewCache.getAppNodeIDC(appkey))
  }
  test("getGraphDataIDC"){
    println(ViewCache.getGraphDataIDC(100,"all"))
    println(ServiceView.getIdcInfo(100,"all"))
  }

  test("perfWorst"){
    ViewCache.perfWorst()
    Graph.values.foreach{
      x=>
        val data = ViewCache.getperfWorst(x.id)
        println(data)
    }
  }

  test("newSpannames"){
    ViewCache.newSpannames()
    Graph.values.foreach{
      x=>
        val data = ViewCache.getNewSpannames(x.id)
        println(data)
    }

  }

}
