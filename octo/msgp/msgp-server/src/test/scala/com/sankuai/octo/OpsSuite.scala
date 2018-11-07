package com.sankuai.octo

import com.sankuai.msgp.common.model.Pdl
import com.sankuai.msgp.common.service.org.OpsService.OpsSrv
import com.sankuai.msgp.common.service.org.{BusinessOwtService, OpsService}
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{BeforeAndAfter, FunSuite}

import scala.collection.JavaConversions._
import scala.collection.mutable
import scala.concurrent.ops

/**
  * Created by zava on 16/1/12.
  */
@RunWith(classOf[JUnitRunner])
class OpsSuite extends FunSuite with BeforeAndAfter {
  before {
    //        ops.refresh()
    //     HulkApi.refresh()
  }
  test("ip2nameMap") {
    println(OpsService.ip2nameMap())
  }
  test("allservice") {
    println(OpsService.allservice())
  }
  test("iptohost") {
    //    ops.refresh()
    //    HulkApi.refresh()
    println(OpsService.ipToHost("10.32.120.139"))
    println(OpsService.ipToHost("10.32.120.135"))
    println(OpsService.ipToHost("10.12.47.134"))
    println(OpsService.ipToHost("10.5.238.230"))
    println(OpsService.ipToHost("10.64.24.177"))
    println(OpsService.ipToHost("10.64.14.210"))
    println(OpsService.ipToHost("10.4.243.201"))
    println(OpsService.ipToHost("10.4.233.103"))
  }
  test("tairget") {

    val list = "10.66.33.54,10.66.33.136,10.66.70.119,10.66.71.171,10.124.6.30,10.20.60.146,10.20.61.145,10.2.13.27,10.66.40.115,10.66.45.194,10.124.1.52,10.66.15.143,10.124.5.219,10.2.9.148,10.66.35.169,10.66.36.51,10.66.45.140,172.24.125.39,10.2.29.14,10.66.36.122,10.66.63.110,10.5.240.43,10.5.236.112,10.5.240.238,172.27.7.203,10.20.60.148,10.20.61.146,10.20.61.148,10.4.242.208,10.5.233.96,172.18.129.30,172.18.129.99,172.18.153.40,172.18.158.196,192.168.56.2,172.27.5.211,10.20.60.147,172.18.139.122,172.18.139.202,10.4.242.174,10.4.237.109,10.4.239.97,192.168.2.1,10.5.240.191,10.4.234.155,10.4.239.28,10.5.240.192,10.4.254.229,10.4.246.24,172.18.181.214,192.168.140.30,10.4.236.230,10.20.63.251,10.20.61.214,10.4.254.58,10.5.245.60,10.4.238.32,10.4.244.151,10.5.236.37,192.168.10.101,10.4.244.131,10.4.242.119,10.4.242.120,10.4.233.68,10.4.235.154,10.4.236.221,10.4.239.19,172.30.8.81,172.27.2.246,172.25.121.29,10.4.228.131,10.4.230.156,10.4.240.228,10.5.235.223,172.25.121.46,172.25.121.90,192.168.99.1,192.168.7.1,172.25.121.146,172.18.167.239,10.4.242.204,10.4.232.23,10.4.232.24,10.5.240.207,10.4.240.210,10.4.242.112,10.5.234.196,10.5.242.232,10.4.232.3,10.4.232.38,10.4.232.4,10.4.232.41,10.4.241.125,172.28.60.72,172.28.61.119,172.28.63.189,172.30.13.65,172.30.2.195,172.30.7.195,192.168.230.14"
    val time = System.currentTimeMillis()
    list.split(",").foreach { x =>
      println(OpsService.ipToHost(x))
    }
    println(System.currentTimeMillis() - time)
  }
  test("pdlList") {
    //    val arr = (Json.parse("{\"code\":200,\"data\":[\"corp=meituan&owt=web&pdl=sms\"," +
    //      "\"corp=meituan&owt=cloud&pdl=zone\",\"corp=meituan&owt=bp&pdl=data\"]}") \ "data").asInstanceOf[JsArray].value.toSeq
    //    arr.foreach(println(_))
    //    println(ops.refreshPdl())
    //      println(ops.owtList())
    println(OpsService.refreshPdls())
    OpsService.pdlList("dba").foreach(pdl => print(s"${pdl.getPdl},"))
    println()
    OpsService.pdlList("").foreach(pdl => print(s"${pdl.getPdl},"))
    println()
    OpsService.pdlList("inf").foreach(pdl => print(s"${pdl.getPdl},"))
    println()
  }

  test("owtbyusername") {
    println(OpsService.getOwtsbyUsername("hanjiancheng"))
  }

  test("pdlowner") {
    //    println(ops.saveOwtOwner(new Pdl("meishi"),List("zhengjingchao")))
    //    ops.refreshPdls

    //         println(ops.getOwner(new Pdl("meishi")))
    //        println(ops.getOwtOwner(new Pdl("inf","borp")))
    println(OpsService.getAppSre(new Pdl("inf", "borp")))
    //    val pdlList = List(new Pdl("inf","borp"),new Pdl("inf","octo"))
    //    println(ops.getPdlOwners(pdlList))

  }
  test("getOpsAppkey") {
    println(OpsService.getOpsAppkey("com.sankuai.inf.logCollector"))
    println(OpsService.getOpsAppkey("com.sankuai.inf.logcollector3"))
    println(OpsService.getOpsAppkey("com.sankuai.inf.logcollecto2"))
    println(OpsService.getOpsAppkey("com.sankuai.inf.data.statistic"))
    println(OpsService.getOpsAppkey("com.sankuai.inf.mnsc"))
  }
  test("getRdAdmin") {
    //    println(ops.getOwtAdmin("waimai"))
    OpsService.owtList.foreach {
      owt =>
        val admin = OpsService.getOwtAdmin(owt)
        println(s"${owt},${admin}")

    }
  }

  test("getOpsAppkeys") {
    val opsSrv = OpsSrv("meituan", "inf", "image", "imgsrv")
    val data = OpsService.getOpsAppkeys(opsSrv)
    println(data)
  }


  test("getRDAdmin") {
    val result = OpsService.getRDAdmin("corp=meituan&owt=waimai&pdl=m&srv=crmpanda&cluster=prod")
    print(result)
  }

  test("owt_bg") {
    OpsService.refreshOwt
    val data = OpsService.owtBusiness.get("meituan.inf")
    println(data)
    println(OpsService.owtBusiness.get("dianping.ota"))
    OpsService.owtBusiness.foreach {
      case (key, value) =>
        println(s"$key,$value")
    }
    OpsService.businessGroup.foreach {
      value =>
        println(s"$value")
    }
  }

  test("getOwt") {
    OpsService.refreshOwt
    val owts = BusinessOwtService.getOwtList("dianping", "酒店旅游事业群")
    println(owts)
  }

  test("getStreeServiceOwt") {
    val data = OpsService.getStreeServiceOwt
    println(data)
  }

  test("getAppTag") {
    val tagOpt = OpsService.getAppkeyTag("com.sankuai.inf.msgp")
    if (tagOpt.isDefined) {
      val tag = tagOpt.get
      val aa = tag.replace("corp=", "").replace("&owt=", ".").replace("&pdl=", ".").replace("&srv=", ".")
      println(aa)
      val rds = OpsService.getRDAdmin(tag)
      println(rds)
    }
  }

  test("getAlarmAdmin") {
    println(OpsService.getSrvAlarmAdmin("dianping.dzu.dz_qd.dz-static-ci-web"))
    println(OpsService.getSrvAlarmAdmin("meituan.inf.octo.msgp"))
    println(OpsService.getSrvAlarmAdmin("meituan.inf.octo.statistic"))
    println(OpsService.getSrvAlarmAdmin("meituan.inf.borp.borp"))
    println(OpsService.getSrvAlarmAdmin("meituan.inf.borp.hive"))
    println(OpsService.getSrvAlarmAdmin("meituan.inf.cargo.poiopportal"))
  }
  test("opt") {
    val alarm_admin = OpsService.getAppkeyAlarmAdmin("com.sankuai.inf.msgp")
    var xm_admin = mutable.HashSet()
    val set = xm_admin ++ alarm_admin.getOrElse(Seq()) ++ List("hanjiancheng", "yangrui08", "tangye03")
    println(set)
  }
}
