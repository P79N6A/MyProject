package com.sankuai.octo.msgp

import com.sankuai.octo.msgp.serivce.service.AppkeyProviderService
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{BeforeAndAfter, FunSuite}



@RunWith(classOf[JUnitRunner])
class AppkeyProviderServiceSuite extends FunSuite with BeforeAndAfter {

  test("get appkeys ") {
    val data = AppkeyProviderService.appkeys(List("10.124.5.28","10.5.234.227"))
    println(data)
  }


  test("provider"){
    val data = AppkeyProviderService.provider("com.sankuai.inf.sg_agent")
    println(data)
  }
  test("proivder_status"){
    val list = AppkeyProviderService.getProviderProtocolStatus("com.sankuai.inf.mnsc","10.5.238.232,10.5.239.227")
    println(list)
  }


}
