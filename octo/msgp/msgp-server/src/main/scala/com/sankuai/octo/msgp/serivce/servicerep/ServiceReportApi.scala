package com.sankuai.octo.msgp.serivce.servicerep

import java.util.concurrent.Executors

import com.sankuai.msgp.common.utils.StringUtil
import com.sankuai.octo.msgp.dao.report._
import com.sankuai.octo.mworth.util.DateTimeUtil
import org.joda.time.DateTime

import scala.concurrent.ExecutionContext


/**
 * 提供外部查询日报/周报信息的api
 */
object ServiceReportApi {
  private implicit val ec = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(8))
  private val fourDecimalFormatter = "%.4f"

  // appkey ,top90,调用量,qps,服务节点数,单机QPS
  case class PerfData(appkey: String, startDay: String, endDay: String,
                      count: Long, hostCount: Int,
                      qps: Double, tp90: Int,
                      maxQps: Double, minQps: Double,
                      avgHostQps: Double, maxHostQps: Double,
                      errorCount: Long, errorRadio: Double)

  case class SimpleIdcCount(idc: String, count: Long, hostCount: Int)


  def kpi(appkey: String, day: String, unit: String): PerfData = {
    val week = if (unit.equals("week")) {
      true
    } else {
      false
    }
    val dataTime = getDate(day, week)
    if (week) {
      kpi(appkey, dataTime)
    } else {
      //天粒度统计
      val currentDate = new java.sql.Date(dataTime.withTimeAtStartOfDay().getMillis)
      val dailyDataOpt = ReportDailyDao.getSpanData(appkey, "all", currentDate)
      if (dailyDataOpt.isDefined) {
        val dailyData = dailyDataOpt.get
        val hostCount =  dailyData.hostCount
        val minQps = 0.0 //业务方暂时不需要, report_daily应该保存

        PerfData(appkey, day, day, dailyData.count, hostCount, dailyData.qps, dailyData.tp90, dailyData.topQps, minQps, dailyData.avgHostQps, dailyData.topHostQps, dailyData.errorCount, dailyData.errorCount / dailyData.count)
      } else {
        PerfData(appkey, day, day, 0, 0, 0.0, 0, 0.0, 0.0, 0.0, 0.0, 0L, 0.0)
      }
    }
  }

  def kpi(appkey: String, day: DateTime): PerfData = {
    val date = new java.sql.Date(day.getMillis)
    //获取qps
    val qpsOpt = getQps(appkey, date)
    val errorOpt = getError(appkey, date)
    val peakOpt = getPeak(appkey, date)
    val weekDay = getStrWeekDay(day)
    val qps = qpsOpt match {
      case Some(qps) =>
        (qps.count, qps.qps, qps.tp90)
      case None =>
        (0L, 0.0, 0)
    }
    val peak = peakOpt match {
      case Some(peak) =>
        (peak.hostCount, peak.maxHourQps,
          peak.minHourQps, peak.avgHostQps, peak.maxHostQps)
      case None =>
        (0, 0.0, 0.0, 0.0, 0.0)
    }
    val error = errorOpt match {
      case Some(error) =>
        (error.errorCount, error.ratio)
      case None =>
        (0L, 0.0)
    }
    PerfData(appkey, weekDay.head, weekDay.last, qps._1, peak._1, qps._2, qps._3,
      peak._2, peak._3, peak._4, peak._5, error._1, error._2)
  }


  /**
   * 获取业务线一周的kpi
   * @param owt
   * @param day
   * @param limit
   */
  def getWeeklyKpi(owt: String, day: String, limit: Int) = {
    val startDay = getDate(day, true)
    val dayRanges = getWeekDay(startDay).toList
    val kpiListMissingQps = ServiceReport.getAvailability(owt, startDay, limit)
    val qpsList = ServiceReport.getQps(owt, startDay, Integer.MAX_VALUE).flatten
    val completeKpiList = kpiListMissingQps.map{
      x=>
        val appkey = x.head.appkey
        val relevantQpsItems = qpsList.filter(x=> x.appkey.equalsIgnoreCase(appkey)).sortBy(_.ts)
        x.map{
          kpi=>
            val correspondingItem = relevantQpsItems.filter(_.ts == kpi.ts)
            if(correspondingItem.nonEmpty){
              kpi.copy(qps = correspondingItem.head.qps)
            }else{
              kpi.copy(qps = "NaN")
            }
        }

    }
    completeKpiList
  }


  def getQps(appkey: String, day: java.sql.Date) = {
    ReportQpsDao.get(appkey, day)
  }


  def getError(appkey: String, day: java.sql.Date) = {
    ReportErrorLogDao.get(appkey, day)
  }

  def getPeak(appkey: String, startDay: java.sql.Date) = {
    ReportQpsPeakDao.get(appkey, startDay)

  }

  /**
   * idc 流量分布
   */
  def idc(appkey: String, weekStartDay: DateTime) = {
    val sqlDate = new java.sql.Date(weekStartDay.getMillis)
    val list = ReportIdcTrafficDao.get(appkey, sqlDate).map {
      traffic =>
        SimpleIdcCount(traffic.idc, traffic.idcCount, traffic.hostCount)
    }
    Map("appkey" -> appkey, "idcCounts" -> list)
  }


  private def getDate(day: String, week: Boolean = false): DateTime = {
    var time: Long = System.currentTimeMillis
    if (StringUtil.isNotBlank(day)) {
      time = DateTimeUtil.parse(day, DateTimeUtil.DATE_DAY_FORMAT).getTime
    }
    val date: DateTime = new DateTime(time)
    if (week) {
      date.withDayOfWeek(1)
    } else {
      date
    }
  }


  def getWeekDay(date: DateTime, days: Int = 6) = {
    val start = date.withTimeAtStartOfDay().withDayOfWeek(1)
    (0 to days).map {
      d =>
        val day = start.plusDays(d)
        day
    }
  }

  def getStrWeekDay(date: DateTime, days: Int = 6) = {
    val start = date.withTimeAtStartOfDay().withDayOfWeek(1)
    (0 to days).map {
      d =>
        val day = start.plusDays(d)
        DateTimeUtil.format(day.toDate, DateTimeUtil.DATE_DAY_FORMAT)
    }
  }
}
