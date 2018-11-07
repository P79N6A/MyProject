package com.sankuai.octo.msgp.serivce.data

import java.util.concurrent.TimeUnit

import com.meituan.jmonitor.LOG
import com.ning.http.client.ProxyServer
import com.sankuai.msgp.common.utils.helper.JsonHelper
import com.sankuai.octo.statistic.util.{ExecutionContextFactory, common}
import dispatch._
import org.slf4j.LoggerFactory
import play.api.libs.json.Json

import scala.concurrent.Await
import scala.concurrent.duration.Duration

object FalconQuery {
  private val logger = LoggerFactory.getLogger(this.getClass)
  private implicit val ec = ExecutionContextFactory.build(10)

  private val formatStr = "%.3f"
  private val proxy = new ProxyServer("10.32.140.181", 80)
  private val time = Duration.create(15000L, TimeUnit.MILLISECONDS)

  // history
  val historyUrl = if (common.isOffline) {
    url("http://10.5.233.113:9966/graph/history").setProxyServer(proxy)
    //    url("http://query.falcon.vip.sankuai.com:9966/graph/history").setProxyServer(proxy)
  } else {
    url("http://query.falcon.vip.sankuai.com:9966/graph/history")
  }
  val historyPost = historyUrl.POST

  //counter格式为"metric/tags"
  case class EndpointCounter(endpoint: String, counter: String)

  case class DataSeries(timestamp: Option[Int], value: Option[Double])

  implicit val var1 = Json.reads[DataSeries]
  implicit val var2 = Json.writes[DataSeries]

  case class FalconDataResponse(Values: Option[List[DataSeries]], counter: Option[String], dstype: Option[String], endpoint: Option[String], step: Option[Int])

  implicit val var3 = Json.reads[FalconDataResponse]
  implicit val var4 = Json.writes[FalconDataResponse]

  def falconParam(start: Long, end: Long, cf: String = "AVERAGE", endpointCounters: List[EndpointCounter]) = {
    Map(
      "start" -> start,
      "end" -> end,
      "cf" -> cf,
      "endpoint_counters" -> endpointCounters)
  }

  def historyQuery(param: Map[String, Any]) = {
    val paramStr = JsonHelper.jsonStr(param)
    try {
      val future = Http(historyPost << paramStr > as.String)
      future.map { result =>
        Json.parse(result).validate[Seq[FalconDataResponse]].fold({ error =>
          logger.error(s"FalconDataResponse parse failed,param${param}",error)
          Seq()
        }, {
          value => value
        })
      }
    } catch {
      case e: Exception => LOG.error(s"get data from falcon failed, param: $paramStr", e)
        Future {
          Seq()
        }
    }
  }

  def getMetrics(start: Int, end: Int, endpoints: List[String], metric: String) = {
    val endpointCounters = endpoints.map { x =>
      EndpointCounter(x, metric)
    }
    val param = falconParam(start, end, endpointCounters = endpointCounters)
    val tmpList = historyQuery(param)

    try {
      Await.result(tmpList, time)
    } catch {
      case e: Exception =>
        LOG.error("historyDataFromFalcon fail", e)
        List()
    }
  }

  // counter必须保持一致
  def sumByEndpoint(data: List[FalconDataResponse]) = {
    val valueList = data.foldLeft(List[DataSeries]()) { (result, item) =>
      result.toList ++ item.Values.getOrElse(List())
    }.filter(_.timestamp.nonEmpty)
    // group by time
    valueList.groupBy(_.timestamp.get).map { tmp =>
      val canSum = tmp._2.filter(_.value.nonEmpty)
      val sum = if (canSum.nonEmpty) {
        Some(canSum.map(_.value.get).sum)
      } else {
        None
      }
      (tmp._1, sum)
    }.filter(_._1 != 0)
  }

  // counter必须保持一致
  def avgByEndpoint(data: List[FalconDataResponse]) = {
    val endpointCount = data.length
    if (endpointCount == 0) {
      Map[Int, Option[Double]]()
    } else {
      val tmp = sumByEndpoint(data)
      tmp.map { item =>
        val avg = if (item._2.nonEmpty) {
          Some(formatStr.format(item._2.get / endpointCount).toDouble)
        } else {
          None
        }
        item._1 -> avg
      }
    }
  }

  def main(args: Array[String]) {
    print(getMetrics(1490153504, 1490154104, List("dx-waimai-money-service01"), "load.1minPerCPU"))
  }
}