package com.sankuai.octo.msgp.serivce.overload

import com.sankuai.msgp.common.config.MsgpConfig
import com.sankuai.octo.msgp.model.LogCollectorResult
import com.sankuai.octo.statistic.util.ExecutionContextFactory
import dispatch._
import org.slf4j.LoggerFactory
import play.api.libs.json._

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, duration}

object LogCollectorService {

  private val LOG = LoggerFactory.getLogger(LogCollectorService.getClass)

  private implicit val ec = ExecutionContextFactory.build(Runtime.getRuntime.availableProcessors)

  private implicit val timeout = Duration.create(3L, duration.SECONDS)

  val ONE_SECOND_IN_MS = 1000
  var logURL = MsgpConfig.get("log-url", "http://query.octo.test.sankuai.info/")

  def getCurrentQPS(appkey: String, method: String, env: String, timestamp: Long, periodInSeconds: Int) = {
    var rv = Map[String, Double]()
    val spanName = if (method == null) "all" else method
    val end = timestamp / ONE_SECOND_IN_MS
    val start = end - periodInSeconds
    LOG.info("request for qps: " + mergePerfQuery(appkey, spanName, env, start.toString, end.toString))
    val result = getHttpContent(mergePerfQuery(appkey, spanName, env, start.toString, end.toString))
    result.consumer2QpsList.foreach { consumer =>
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
    val feature = Http(url(logURL + URL) > as.String)
    Json.parse(Await.result(feature, timeout)).as[List[LogCollectorResult]]
  }

  def getHttpContent(URL: String) = {
    val feature = Http(url(logURL + URL) > as.String)
    Json.parse(Await.result(feature, timeout)).as[LogCollectorResult]
  }

}
