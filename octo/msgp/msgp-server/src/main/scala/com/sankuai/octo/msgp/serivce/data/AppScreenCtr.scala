package com.sankuai.octo.msgp.serivce.data

import com.sankuai.msgp.common.config.db.msgp.Tables.AppScreenRow
import com.sankuai.msgp.common.service.org.OpsService
import com.sankuai.octo.msgp.dao.kpi.AppScreenDao
import com.sankuai.octo.msgp.dao.service.ServiceProviderDAO
import com.sankuai.octo.msgp.utils.client.TsdbClient
import com.sankuai.octo.msgp.utils.client.TsdbClient.{FalconTSDBParam, wrapperQuery}
import org.joda.time.DateTime
import org.slf4j.LoggerFactory

import scala.collection.mutable
import scala.util.Random

object AppScreenCtr {
  private val logger = LoggerFactory.getLogger(this.getClass)
  private val formatStr = "%.3f"

  def getScreen(start: Int, end: Int, id: Long) = {
    try {
      val list = AppScreenDao.get(id)
      val providers = getEndpoints(list)
      val ret = list.map {
        appScreenRow =>
          val (result, flag) = getFromFalconTSDB(start, end, appScreenRow)
          if (flag) {
            getFromFalcon(start, end, providers, appScreenRow)
          } else {
            result
          }
      }
      ret
    } catch {
      case e: Exception =>
        logger.info("AppScreen getScreen failed", e)
        List(Map())
    }
  }

  def getEndpoints(list: List[AppScreenRow]) = {
    if (list.nonEmpty) {
      val row = list(0)
      val category = row.category
      if (category == "appkey") {
        ServiceProviderDAO.providerList(row.appkey).map(OpsService.ipToHost)
      } else if (category == "endpoint") {
        row.endpoint.getOrElse("").split(",").distinct.toList
      } else {
        // 通过serverNode获取所有主机
        OpsService.getServerNodeHosts(row.serverNode)
      }
    } else {
      List()
    }
  }

  def getFromFalcon(start: Int, end: Int, providers: List[String], appScreenRow: AppScreenRow) = {
    val title = appScreenRow.title
    val result = mutable.Map[String, Any](
      "id" -> appScreenRow.id,
      "xAxis" -> List[String](),
      "series" -> List(Map[String, Any]()),
      "title" -> title
    )
    val tmp = FalconQuery.getMetrics(start, end, providers, appScreenRow.metric).toList
    if (tmp.nonEmpty) {
      val data = if (appScreenRow.sampleMode == "avg") {
        FalconQuery.avgByEndpoint(tmp).toList.sortBy(_._1)
      } else {
        FalconQuery.sumByEndpoint(tmp).toList.sortBy(_._1)
      }
      val xAxis = data.map { item =>
        new DateTime(item._1 * 1000L).toString("MM-dd HH:mm")
      }
      result.update("xAxis", xAxis)
      result.update("series", List(Map("name" -> "当前值", "data" -> data.map(_._2))))
    }
    result
  }

  def getFromFalconTSDB(start: Int, end: Int, appScreenRow: AppScreenRow) = {
    val title = appScreenRow.title
    val result = mutable.Map[String, Any](
      "id" -> appScreenRow.id,
      "xAxis" -> List[String](),
      "series" -> List(Map[String, Any]()),
      "title" -> title
    )
    var needQueryFalcon = true
    val (metric, tags) = TsdbClient.getMetricAndTags(appScreenRow.metric)
    val nowResponses = TsdbClient.get(FalconTSDBParam(List(wrapperQuery(metric, tags, appScreenRow.sampleMode)), start.toString, end.toString))
    if (nowResponses.nonEmpty) {
      needQueryFalcon = false
      // now data
      val nowData = mutable.Map(nowResponses(0).dps.toSeq: _*)

      // 先取基线，如果没有再取昨天的值，基线存储在一台机器上
      val baseResponses = TsdbClient.getBaseData(start, end, List(appScreenRow))
      val baseData = if (baseResponses.nonEmpty) {
        mutable.Map(baseResponses(0).dps.toSeq: _*)
      } else {
        mutable.Map[String, Option[Double]]()
      }

      val xAxis = nowData.keys ++ baseData.keys
      xAxis.foreach { x =>
        nowData.getOrElseUpdate(x, None)
        baseData.getOrElseUpdate(x, None)
      }
      val nowDataRet = nowData.toList.sortBy(_._1).map { item => getValue(item._2)}
      val baseDataRet = baseData.toList.sortBy(_._1).map { item => getValue(item._2)}

      result.update("xAxis", xAxis.toList.sorted.map { item => getDateTimeStr(item.toInt)})
      if (appScreenRow.auth == 1) {
        val ratio = Random.nextFloat() + 1.1
        result.update("series", List(Map("name" -> "当前值", "data" -> wrapperData(nowDataRet, ratio)),
          Map("name" -> "基线值", "data" -> wrapperData(baseDataRet, ratio))))
      } else {
        result.update("series", List(Map("name" -> "当前值", "data" -> nowDataRet),
          Map("name" -> "基线值", "data" -> baseDataRet)))
      }
    }
    (result, needQueryFalcon)
  }

  def wrapperData(data: List[Option[Double]], ratio: Double) = {
    data.map { item =>
      if (item.nonEmpty) {
        Some(item.get * ratio)
      } else {
        Some(None)
      }
    }
  }

  def getValue(value: Option[Double]) = {
    if (value.nonEmpty) {
      Some(formatStr.format(value.get).toDouble)
    } else {
      None
    }
  }

  def getDateTimeStr(time: Int) = {
    new DateTime(time * 1000L).toString("MM-dd HH:mm")
  }
}
