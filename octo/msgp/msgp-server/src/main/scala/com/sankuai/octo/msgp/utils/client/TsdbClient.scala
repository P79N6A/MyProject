package com.sankuai.octo.msgp.utils.client

import com.sankuai.msgp.common.utils.helper.JsonHelper
import com.sankuai.msgp.common.config.db.msgp.Tables.AppScreenRow
import com.sankuai.octo.statistic.util.ExecutionContextFactory
import dispatch._
import org.slf4j.LoggerFactory
import play.api.libs.json.Json

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, duration}

object TsdbClient {
  private val logger = LoggerFactory.getLogger(this.getClass)
  private implicit val ec = ExecutionContextFactory.build(20)
  private implicit val timeout = Duration.create(5L, duration.SECONDS)
  private val TSDBUrlStr = "http://tsdb.falcon.sankuai.com"
  private val TSDBQueryUrl = url(s"$TSDBUrlStr/api/query/").POST.setHeader("Content-Type", "application/json;charset=utf-8")
  private val oneDaySeconds = 24 * 3600

  case class FalconTSDBResponse(metric: String, tags: Map[String, String], aggregateTags: List[String], dps: Map[String, Option[Double]])

  implicit val loadReads = Json.reads[FalconTSDBResponse]
  implicit val loadWrites = Json.writes[FalconTSDBResponse]

  case class Query(metric: String, tags: Map[String, String], aggregator: String)

  case class FalconTSDBParam(queries: List[Query], start: String, end: String)

  def get(param: FalconTSDBParam) = {
    try {
      val feature = Http(TSDBQueryUrl << JsonHelper.jsonStr(param) > as.String)
      val ret = Await.result(feature, timeout)
      Json.parse(ret).validate[List[FalconTSDBResponse]].getOrElse(List())
    } catch {
      case e: Exception =>
        logger.error(s"get falcon tsdb data failed param $param", e)
        List()
    }
  }

  def wrapperQuery(metric: String, tags: Map[String, String], aggregator: String) = {
    if (aggregator.equalsIgnoreCase("sum")) {
      Query(metric, tags, "zimsum")
    } else {
      Query(metric, tags, aggregator)
    }
  }

  def getBaseData(start: Int, end: Int, rows: List[AppScreenRow]) = {
    val queries = rows.map { appScreenRow =>
      val (metric, tags) = getMetricAndTags(appScreenRow.metric)
      wrapperQuery(s"$metric.base.${appScreenRow.sampleMode}", tags, "sum")
    }

    // 先取基线，如果没有再取昨天的值，基线存储在一台机器上
    val baseResponses = TsdbClient.get(FalconTSDBParam(queries, start.toString, end.toString))
    if (baseResponses.nonEmpty) {
      baseResponses
    } else {
      // base data取过去一天的数据，昨天的值分布在多台机器
      val queries = rows.map { appScreenRow =>
        val (metric, tags) = getMetricAndTags(appScreenRow.metric)
        wrapperQuery(metric, tags, appScreenRow.sampleMode)
      }
      val baseResponsesYesterday = TsdbClient.get(FalconTSDBParam(queries, (start - oneDaySeconds).toString, (end - oneDaySeconds).toString))
      if (baseResponsesYesterday.nonEmpty) {
        baseResponsesYesterday.map { item =>
          val dps = item.dps.map {
            item =>
              (item._1.toInt + oneDaySeconds).toString -> item._2
          }
          FalconTSDBResponse(item.metric, item.tags, item.aggregateTags, dps)
        }
      } else {
        List()
      }
    }
  }

  def getMetricAndTags(metric: String) = {
    val tmp = metric.split("/")
    val tagsRet = tmp.tail.flatMap { item =>
      val tags = item.split(",")
      tags.map { tag =>
        val tmp = tag.split("=")
        tmp(0) -> tmp.applyOrElse(1, List("", ""))
      }
    }.toMap
    (tmp(0), tagsRet)
  }

  def main(args: Array[String]) = {
    //    val data = get(FalconTSDBParam(List(Query("kpi.logCollector.minuter.in.count", Map(), "sum")), "1465978260", "1465978440"))
    //    println(data)
    //
    //    val data2 = get(FalconTSDBParam(List(Query("kpi.logCollector.minuter.in.count", Map(), "avg")), "1465978260", "1465978440"))
    //    println(data2)

    val data3 = get(FalconTSDBParam(List(wrapperQuery("kpi.logCollector.minuter.sendMetrics.failed.count", Map(), "sum")), "2m-ago", ""))
    println(data3)
  }
}
