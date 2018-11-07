package com.sankuai.octo

import java.io.{FileInputStream, FileOutputStream, ObjectInputStream, ObjectOutputStream}

import com.sankuai.msgp.common.utils.helper.{CommonHelper, JsonHelper}
import com.sankuai.octo.msgp.dao.perf.PerfDayDao.{CategoryPerfCount, RequestCount}
import com.sankuai.octo.msgp.model.DashboardDomain.Overview
import com.sankuai.octo.msgp.model.IdcName
import com.sankuai.octo.msgp.serivce.data.ResourceQuery
import com.sankuai.octo.msgp.serivce.{DashboardService, service}
import com.sankuai.octo.msgp.serivce.service.{AppkeyDescService, AppkeyProviderService}
import com.sankuai.octo.msgp.service.s3.S3Service
import org.joda.time.DateTime
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{BeforeAndAfter, FunSuite}

import scala.collection.immutable.Map

@RunWith(classOf[JUnitRunner])
class dashboardSuite extends FunSuite with BeforeAndAfter {


  test("dashboard") {
    val start = new DateTime().getMillis
    val value = JsonHelper.jsonStr(DashboardService.defaultDash)
    val end = new DateTime().getMillis
    println(s"overview size is ${value.size}")
    println(s"cost    ${end - start}")

    // 经测试首页数据量在4-5kb，很小，适合内存缓存
  }
  test("idc"){
    val serviceList = service.ServiceCommon.listService
    val instanceList = serviceList.filter(x => x.appkey != "com.sankuai.inf.sg_agent" && x.appkey != "com.sankuai.inf.kms_agent")
      .flatMap(x => AppkeyProviderService.provider(x.appkey))
    val idcMap = instanceList.groupBy(node => IdcName.getNameByIdc(CommonHelper.ip2IDC(node.ip)))
    val map = idcMap.filter(_._1=="其他")
    map.values.foreach(println)
  }
  test("save providerNode"){
//    val serviceList = service.serviceCommon.listService
//    dashboardService.saveService(serviceList)
    AppkeyDescService.saveServiceProvider(List("com.sankuai.cos.mtconfig"))
  }
  test("getOverviewCache"){
    println(DashboardService.defaultDash)
    println(DashboardService.getOverviewCache)
  }

  test("getUtilizationRate"){
    val result = ResourceQuery.getUtilizationRate("com.sankuai.inf.msgp")
    println(result)
  }

  test("read local file"){
    val m = Map[String,Int]("xx"->1)
    val o = Overview(1, m, 2, m, m, "day", RequestCount(List(),List(),List[CategoryPerfCount]()), m, m)
//    val _res = deserialize[Overview]("/Users/nero/proj/OverviewFile")
//    println(_res.isInstanceOf[Overview])
//    SerializeInstanceListToFile(o,"/Users/nero/proj/ddddd")
    val _res = deserialize[Overview]("/Users/nero/proj/OverviewFile")
    println(_res)
  }

  def deserialize[T](path: String): T = {
    val bis = new FileInputStream(path)
    val ois = new ObjectInputStream(bis)
    ois.readObject.asInstanceOf[T]
  }

  private def SerializeInstanceListToFile[T](list: T,path: String) = {
    val bos = new FileOutputStream(path)
    val oos = new ObjectOutputStream(bos)
    oos.writeObject(list)
    oos.close()
  }
}
