package com.sankuai.octo.msgp.serivce.monitor.business

import com.sankuai.msgp.common.config.db.msgp.Tables.{AppScreenRow, BusinessMonitorRow}
import com.sankuai.octo.msgp.dao.kpi.AppScreenDao
import com.sankuai.octo.msgp.utils.client.TsdbClient
import com.sankuai.octo.msgp.utils.client.TsdbClient.{FalconTSDBParam, Query, _}
import com.sankuai.octo.statistic.util.ExecutorFactory
import org.joda.time.DateTime
import org.slf4j.LoggerFactory

import scala.collection.mutable

/**
 * 基线值是每天定时计算的，所以报警存在空档期
 */
object KpiMonitorModel {
  private val logger = LoggerFactory.getLogger(this.getClass)
  val baseCache = mutable.Map[String, Map[String, Option[Double]]]()
  private val asyncSyncBase = new ExecutorFactory(updateBaseCache, "business.asyncSyncBase", 16, 50, 2000)

  // 同步所有的基线值
  def syncBase = {
    val rows = AppScreenDao.get

    val now = new DateTime()
    val start = (now.withTimeAtStartOfDay().getMillis / 1000).toInt
    val end = start + 24 * 60 * 60 - 1

    rows.foreach { row =>
      submitUpdateBase((row, start, end))
    }
  }

  def submitUpdateBase(param: (AppScreenRow, Int, Int)) = {
    asyncSyncBase.submit(param)
  }

  def updateBaseCache(param: (AppScreenRow, Int, Int)): Unit = {
    // 没有才更新
    val row = param._1
    val start = param._2
    val end = param._3
    if (baseCache.get(row.metric).isEmpty) {
      val baseResponses = TsdbClient.getBaseData(start, end, List(row))
      if (baseResponses.nonEmpty) {
        baseCache.getOrElseUpdate(row.metric, baseResponses(0).dps)
      }
    }
  }

  // 清空基线值
  def emptyBase = {
    baseCache.empty
  }

  // 获取最后一个上报的值
  def getLastValue(queries: List[Query]) = {
    val start = "2m-ago"
    val data = TsdbClient.get(FalconTSDBParam(queries, start, ""))
    if (data.nonEmpty) {
      data(0).dps.toList.sortBy(_._1)
    } else {
      List()
    }
  }

  def getBaseData(metric: String) = {
    baseCache.get(metric)
  }

  def getNowBase(trigger: (BusinessMonitorRow, AppScreenRow)) = {
    var ret = (None: Option[Double], None: Option[Double], None: Option[String])
    val (metric, tags) = TsdbClient.getMetricAndTags(trigger._2.metric)
    if(tags.nonEmpty){
      val nowData = getLastValue(List(wrapperQuery(metric, tags, trigger._2.sampleMode)))
      val baseData = getBaseData(trigger._2.metric)
      if (nowData.nonEmpty && baseData.nonEmpty) {
        val time = nowData(0)._1
        val nowValue = nowData(0)._2
        val tmp = baseData.get.get(time)
        if (tmp.nonEmpty) {
          val baseValue = tmp.get
          ret = (nowValue, baseValue, Some(time))
        }
      }
    }
    ret
  }
}
