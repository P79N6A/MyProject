package com.sankuai.octo

import com.sankuai.msgp.common.model.Business
import com.sankuai.msgp.common.service.org.{BusinessOwtService, OpsService}
import com.sankuai.octo.msgp.serivce.component.ComponentHelper
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{BeforeAndAfter, FunSuite}

/**
 * Created by zava on 16/2/19.
 */
@RunWith(classOf[JUnitRunner])
class BusinessOwtSuite extends FunSuite with BeforeAndAfter {

  test("owtbusiness") {
    println(BusinessOwtService.getBusiness("it"))
    println(BusinessOwtService.getBusiness("bp"))
    println(BusinessOwtService.getBusiness("waimai"))
    println(BusinessOwtService.getBusiness("inf"))
  }

  test("getBusinessId") {
    println(Business.getBusinessIdByName("智能餐厅部"))
    println(Business.getBusinessIdByName("其他"))
    println(Business.getBusinessIdByName("技术工程及基础数据平台"))
  }

  test("getBusiness"){
    OpsService.refreshOwt
    val result = BusinessOwtService.getBusiness("", "movie")
    println(result)
  }

  test("updateBusiness"){
    val result = ComponentHelper.updateBusiness(Business.other.toString, "")
    println(result)
  }

  test("genBusiness"){

  }
}