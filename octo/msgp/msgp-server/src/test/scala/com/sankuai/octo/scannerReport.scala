package com.sankuai.octo

import dispatch.Defaults._
import dispatch.{as, url, _}
import org.scalatest.{BeforeAndAfter, FunSuite}

class scannerReport extends FunSuite with BeforeAndAfter {
  test("report") {
    while(true) {
      (1 to 340).foreach{
        x =>
          val redirectUri = "http://localhost:8080/api/scanner/report"
          val postReq = url(redirectUri).POST.setContentType("application/json", "UTF-8")
          val a = postReq << """{"appkey":"com.sankuai.octo.scanner","category":"DuplicateRegistry","level":0,"time":1433395169267,"content":"testwyz","identifier":"ip:192.168.0.1|port:8999","roundTime":50,"providerCount":121,"weight":10,"newWeight":30 }"""
          Http(a > as.String)
          Thread.sleep(10)
      }
      println("=========")
      Thread.sleep(5000)
    }
  }
}
