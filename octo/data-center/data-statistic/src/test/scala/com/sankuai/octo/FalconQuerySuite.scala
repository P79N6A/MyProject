package com.sankuai.octo

import java.util.concurrent.TimeUnit

import com.ning.http.client.ProxyServer
import com.sankuai.octo.statistic.helper.api
import dispatch.Defaults._
import dispatch._
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{BeforeAndAfter, FunSuite}
import org.slf4j.{Logger, LoggerFactory}

import scala.concurrent.Await
import scala.concurrent.duration.Duration

@RunWith(classOf[JUnitRunner])
class FalconQuerySuite extends FunSuite with BeforeAndAfter{
  val LOG: Logger = LoggerFactory.getLogger(classOf[FalconQuerySuite])

  test("falcon query") {
    case class EndpointCounter(endpoint: String, counter: String)

    case class DataSeries(timestamp: Int, value: Double)
    case class FalconDataResponse(Values: Seq[DataSeries], counter: String, dstype: String, endpoint: String, step: Int)

    val proxy = new ProxyServer("10.64.35.229", 80)
    val falconQueryHost = "http://query.falcon.vip.sankuai.com:9966/graph/history"
    val postReq = url(falconQueryHost).POST.setProxyServer(proxy)
    val time  = Duration.create(500, TimeUnit.MILLISECONDS)

    val param =
      Map("start" -> 1442847300,
        "end" -> 1442883300,
        "cf" -> "AVERAGE",
        "endpoint_counters" -> Seq(EndpointCounter("dx-inf-octo-msgp01", "Prod_Server_testMethod0_count/localhost=dx-inf-octo-msgp01,remoteApp=com.sankuai.from0,remoteHost=fromHost00,spanname=testMethod0"), EndpointCounter("dx-inf-octo-msgp01", "Prod_Server_testMethod0_mean/localhost=dx-inf-octo-msgp01,remoteApp=com.sankuai.from0,remoteHost=fromHost00,spanname=testMethod0")))

    val data = try {
      val future = Http(postReq << api.jsonStr(param) > as.String)
      val result = Await.result(future, time)
      Some(api.toObject(result, classOf[Seq[FalconDataResponse]]))
    } catch {
      case e: Exception => LOG.error(s"get data from falcon failed $e"); None
    }

    println(data)
  }
}
