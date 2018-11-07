package com.sankuai.octo.msgp.serivce.component

import java.util.Date

import com.sankuai.msgp.common.model.Business
import com.sankuai.msgp.common.utils.StringUtil
import com.sankuai.msgp.common.utils.helper.CommonHelper
import com.sankuai.octo.msgp.dao.component.TrendDAO
import com.sankuai.octo.msgp.dao.component.TrendDAO.{ComponentDailyCountUpload, ComponentTrendCount}
import com.sankuai.octo.mworth.util.DateTimeUtil
import com.sankuai.octo.statistic.util.ExecutionContextFactory
import org.joda.time.DateTime

import scala.concurrent.Future

/**
  * Created by yves on 16/9/27.
  *
  * MSGP 中 组件趋势统计
  *
  */
object TrendService {

  case class ComponentTrendElement(name: String, counts: List[Int])

  case class ComponentTrend(trends: List[ComponentTrendElement], dailyTotal: List[Int], dates: List[String])

  private val debug = false

  private implicit val ec = ExecutionContextFactory.build(10)


  /**
    * 查询对应组件的使用趋势
    *
    */
  def getComponentTrend(start: Date, end: Date, base: String, business: String, owt: String, pdl: String, groupId: String, artifactId: String, version: String) = {
    val result = if (CommonHelper.isOffline && !debug) {
      ComponentTrend(List[ComponentTrendElement](), List[Int](), List[String]())
    } else {
      val startTime = new DateTime(start.getTime).withTimeAtStartOfDay()
      val endTime = new DateTime(end.getTime).withTimeAtStartOfDay()
      val day = (endTime.getMillis - startTime.getMillis) / DateTimeUtil.DAY_TIME
      val dates = (day.toInt to 0 by -1).map {
        x =>
          endTime.minusDays(x).toString("yyyy-MM-dd")
      }.toList
      val startDate = new java.sql.Date(startTime.getMillis)
      val endDate = new java.sql.Date(endTime.getMillis)
      val resultTemp = TrendDAO.getComponentTrend(startDate, endDate, base, business, owt, pdl, groupId, artifactId, version)

      val result = if (StringUtil.isBlank(business)) {
        resultTemp.groupBy(_.business)
      } else if (StringUtil.isNotBlank(business) && StringUtil.isBlank(owt) && StringUtil.isBlank(pdl)) {
        resultTemp.groupBy(_.owt)
      } else if (StringUtil.isNotBlank(business) && !StringUtil.isBlank(owt) && StringUtil.isBlank(pdl)) {
        resultTemp.groupBy(_.pdl)
      } else if (StringUtil.isNotBlank(business) && !StringUtil.isBlank(owt) && !StringUtil.isBlank(pdl)) {
        resultTemp.groupBy(_.pdl)
      } else {
        resultTemp.groupBy(_.business)
      }
      val trends = result.map {
        e =>
          val name = e._1
          val dateToData = e._2.groupBy {
            x =>
              DateTimeUtil.format(new Date(x.date.getTime), "yyyy-MM-dd")
          }
          val counts = dates.map {
            date =>
              dateToData.getOrElse(date, List[ComponentTrendCount]()).map(_.coverage).sum
          }
          ComponentTrendElement(name, counts)
      }.toList
      val dateToData = resultTemp.groupBy {
        x =>
          DateTimeUtil.format(new Date(x.date.getTime), "yyyy-MM-dd")
      }
      val dailyTotal = dates.map {
        date =>
          dateToData.getOrElse(date, List[ComponentTrendCount]()).map(_.coverage).sum
      }
      ComponentTrend(trends, dailyTotal, dates)
    }
    result
  }

  /**
    * 每天上传趋势数据
    *
    */
  def uploadDailyTrend(date: Date) = {
    val artifactsUsedFrequently = ComponentHelper.CMPT_FREQUNTLY_USED.map(_._2).toList
    val dailyCount = TrendDAO.getDailyCount(artifactsUsedFrequently)
    val lastDate = new java.sql.Date(date.getTime)
    val ComponentCoverageUploadList = dailyCount.par.map {
      x =>
        ComponentDailyCountUpload(x.base, x.business, x.owt, x.pdl, x.groupId, x.artifactId, x.version, x.realCount, lastDate)
    }.toList
    TrendDAO.batchInsert(ComponentCoverageUploadList)
  }

  /**
    * 手动将数据刷入趋势库
    *
    */
  def refresh(date: Date) = {
    val lastDate = new java.sql.Date(date.getTime)
    Future {
      TrendDAO.delete(lastDate)
      uploadDailyTrend(lastDate)
    }
  }
}
