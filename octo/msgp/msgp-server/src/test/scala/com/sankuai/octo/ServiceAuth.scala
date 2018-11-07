package com.sankuai.octo

import com.sankuai.octo.msgp.serivce.service.ServiceAuth
import org.scalatest.{BeforeAndAfter, FunSuite}

import scala.collection.JavaConverters._

/**
  * Created by yves on 17/2/9.
  */
class ServiceAuth extends FunSuite with BeforeAndAfter{

  test("getAppkeyWhiteList"){
    val result = ServiceAuth.getAppkeyWhiteList("com.sankuai.zava.test1", "3")
    println(result)
  }

  test("updateAppkeyWhiteList"){
    val whiteList = List("com.sankuai.inf.octo.clientA").asJava
    val result = ServiceAuth.updateAppkeyWhiteList("com.meituan.hotel.notify", whiteList, "3")
    println(result)
  }

  test("getAppkeyAuthList"){
    val result = ServiceAuth.getAppkeyAuthList("com.meituan.hotel.notify", "3")
    println(result)
  }

  test("updateAppkeyAuthList"){
    //val authList = List("com.sankuai.inf.octo.clientB", "com.sankuai.inf.octo.clientC", "com.sankuai.inf.octo.clientA", "com.sankuai.inf.octo.clientD")
    val authList = List("com.sankuai.inf.octo.clientC", "com.sankuai.inf.octo.clientA", "com.sankuai.inf.octo.clientD").asJava
    val result = ServiceAuth.updateAppkeyAuthList("com.meituan.hotel.notify", authList, "3")
    println(result)
  }

  test("getSpanAuthList"){
    val result = ServiceAuth.getSpanAuthList("com.meituan.hotel.notify", "3")
    println(result)
  }
}
