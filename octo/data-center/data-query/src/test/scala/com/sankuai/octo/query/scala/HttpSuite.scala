package com.sankuai.octo.query.scala

import java.util.concurrent.TimeUnit

import com.ning.http.client.ProxyServer
import dispatch.{Http, as, url}
import org.scalatest.FunSuite

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration

/**
  * Created by wujinwu on 16/3/15.
  */
class HttpSuite extends FunSuite {
  test("testHttp") {
    val testHost = "10.32.92.43:8960"
    val testUrl = s"http://$testHost/api/history/data"
    val req = url(testUrl).setProxyServer(new ProxyServer("10.32.140.181", 80)) <<? Map("appkey" -> "com.sankuai.inf.mnsc",
      "start" -> "1460995200", "end" -> "1460995200", "protocolType" -> "thrift", "role" -> "server", "dataType" -> "all",
      "env" -> "prod", "unit" -> "Day", "group" -> "spanLocalHost", "spanname" -> "all", "localhost" -> "*", "dataSource" -> "hbase")
    val future = Http(req OK as.String).mapTo[String]
    println(Await.result(future, Duration.create(20, TimeUnit.SECONDS)))
    println("7.630828374E9".toDouble.toLong)
  }

  test("testInstance") {
    val testHost = "10.32.94.103:8950"
    val testUrl = s"http://$testHost/instance/dump"
    val req = url(testUrl).setProxyServer(new ProxyServer("10.32.140.181", 80))
    val future = Http(req OK as.String).mapTo[String]
    println(Await.result(future, Duration.create(20, TimeUnit.SECONDS)))
  }


}
