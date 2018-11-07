package com.sankuai.octo.oswatch.service


import com.sankuai.octo.oswatch.utils.Common
import com.sankuai.octo.oswatch.model.LogCollectorResult
import org.slf4j.{LoggerFactory, Logger}
import play.api.libs.json._
//import dispatch._, Defaults._
import scalaj.http._
import scala.concurrent.{duration, Await}
import scala.concurrent.duration.Duration
import scala.collection.mutable.Map

/**
 * Created by dreamblossom on 15/9/30.
 */
object LogCollectorService {

  def getCurrentQPS(appkey: String, method: String, env: String, timestamp: Long, periodInSeconds: Int) = {
    val rv = Map[String, Double]()
    val spanName = if (method == null) "all" else method
    val end = timestamp / Common.ONE_SECOND_IN_MS
    val start = end - periodInSeconds
    val result = getHttpContent(mergePerfQuery(appkey, spanName, env, start.toString, end.toString))
    result.consumer2QpsList.foreach{ consumer =>
      rv += (consumer.consumerAppKey -> consumer.qpsAvg)
    }
    rv
  }

  def mergePerfQuery(appkey: String, spanName: String, env: String, start: String, end: String) = {
    "/api/query?provider=" + appkey +
      "&spanname=" + spanName +
      "&env=" + env +
      "&start=" + start +
      "&end=" + end
  }

  def getHttpContentList(URL: String) = {
    // val feature = Http(url(Common.logURL + URL) > as.String)
    // Json.parse(Await.result(feature, timeout)).as[List[LogCollectorResult]]
    println("request: " + Common.logURL + URL)
    val response: HttpResponse[String] = Http(Common.logURL + URL).option(HttpOptions.readTimeout(Common.httpTimeoutInterval * Common.ONE_SECOND_IN_MS))
      .asString
    println("response: " + response)
    (Json.parse(response.body)).as[List[LogCollectorResult]]
  }

  def getHttpContent(URL: String) = {
    println("request: " + Common.logURL + URL)
    val response: HttpResponse[String] = Http(Common.logURL + URL).option(HttpOptions.readTimeout(Common.httpTimeoutInterval*Common.ONE_SECOND_IN_MS))
      .asString
    println("response: " + response)
    //val feature = Http(url(Common.logURL + URL) > as.String)
    (Json.parse(response.body)).as[LogCollectorResult]
  }
}
