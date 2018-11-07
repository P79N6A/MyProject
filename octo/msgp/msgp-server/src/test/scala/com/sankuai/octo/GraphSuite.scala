package com.sankuai.octo

import com.sankuai.msgp.common.service.org.OpsService
import com.sankuai.octo.msgp.serivce.data.DataQuery
import com.sankuai.octo.msgp.serivce.graph.{ServiceView, ViewCache}
import com.sankuai.octo.msgp.serivce.other.PerfApi
import com.sankuai.octo.msgp.serivce.service.ServiceCommon
import com.sankuai.octo.msgp.serivce.service
import org.joda.time.DateTime
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{BeforeAndAfter, FunSuite}

@RunWith(classOf[JUnitRunner])
class GraphSuite extends FunSuite with BeforeAndAfter {

  test("graph api") {
    //    ViewCache.refreshAppNodeCache()
    //    ViewCache.refreshGraphDataCache()
    //    val waimai = ViewCache.getGraphData(ViewDefine.Graph.waimai.id)
    //    println(waimai)
    //    val banma = ViewCache.getGraphData(ViewDefine.Graph.banma.id)
    //    println(banma)
  }

  test("buildAppNodeCache") {
    OpsService.refresh
    ViewCache.refreshAppNodeCache
  }
  test("refreshGraphDataCache"){
    val appkey = "com.sankuai.movie.mmdb.ugc"
    val desc = ServiceCommon.desc(appkey)
    ViewCache.buildAppNodeByIDC(desc)
    val services = service.ServiceCommon.listService
    println(ViewCache.getAppNodeIDC(appkey))
    ViewCache.updateGraphDataIDC(100,List(desc))
    println(ViewCache.getGraphDataIDC(100, "all"))
  }

  test("getGraphDataIDC"){
    println(ViewCache.getGraphDataIDC(100, "all"))
  }



//  test("getIdcInfo") {
//    //    val ret = ServiceView.getIdcInfo(100, "dx")
//    //    println("外卖:" + ret)
//    val ret = perfapi.getAppCall("com.sankuai.waimai.open", "getInvoke", "1m", "server", "com.sankuai.waimai.poi")
//    println(ret)
//  }

  test("getInokeDesc") {
//    ops.refresh
//    perfapi.getInvokeDescByIDC("com.sankuai.waimai.poi")
//    println(perfapi.getInvokeDescByIDC("com.sankuai.banma.rider","com.meituan.banma.api","dx"))
//    println(perfapi.getInvokeDescByIDC("com.sankuai.banma.rider","com.meituan.banma.api","yf"))
//      println(perfapi.getInvokeDescByIDC("com.sankuai.banma.rider","com.meituan.banma.api","lf"))
//      println( perfapi.getInvokeDescByIDC("com.sankuai.banma.rider","com.meituan.banma.api","cq"))
//      println(perfapi.getInvokeDescByIDC("com.sankuai.banma.rider","com.meituan.banma.api","other"))

//    println(ServiceView.getInvokeDesc("com.sankuai.banma.rider", "com.meituan.banma.api", "dx"))
    println(ServiceView.getInvokeDesc("com.sankuai.inf.msgp", "com.sankuai.inf.mnsc", "dx"))
  }

  test("spanname") {
    val start: Int = (new DateTime().minusDays(2).getMillis / 1000).toInt
    val end: Int = (new DateTime().getMillis / 1000).toInt
    val from = "com.sankuai.waimai.open"
    val to = "com.sankuai.waimai.poi"
    val ret = DataQuery.tags(to, start, end, "prod", "server")
    ret.spannames.foreach(println)
  }

  test("getAppCallByIDC") {
    OpsService.refresh
//    ViewCache.buildAppNodeByIDC(service.desc("com.sankuai.waimai.poi"))
    PerfApi.getInvokeDescByIDC("com.sankuai.banma.admin","com.sankuai.waimai.poi")
  }


}
