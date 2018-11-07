package com.sankuai.octo.msgp.controller

import com.sankuai.octo.msgp.serivce.servicerep.ServiceDailyReport
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{BeforeAndAfter, FunSuite}


@RunWith(classOf[JUnitRunner])
class ServiceDailyReportSuite extends FunSuite with BeforeAndAfter {

  test("getNonstandardAppkey"){
    println(ServiceDailyReport.getNonstandardAppkey("niuyushan"))
  }

}

