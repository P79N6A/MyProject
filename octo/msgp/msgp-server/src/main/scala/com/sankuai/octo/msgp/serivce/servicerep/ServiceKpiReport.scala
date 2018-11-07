package com.sankuai.octo.msgp.serivce.servicerep

import java.util.Date
import java.util.concurrent.TimeUnit

import com.sankuai.octo.msgp.serivce.data.DataQuery.Point
import com.sankuai.octo.msgp.serivce.data.{DataQuery, ErrorQuery}
import com.sankuai.octo.msgp.serivce.monitor.MonitorEvent
import com.sankuai.octo.msgp.utils.helper.ReportHelper

import scala.collection.parallel.ForkJoinTaskSupport
import scala.concurrent.duration.Duration

/**
 * Created by zava on 16/5/24.
 * 服务的kpi 报告
 * 可以指定时间端查询
 *
 * 目标:灵活的获取服务的运营状态
 * count,tp90,报错,业务状态,等关键指标信息,
 */
object ServiceKpiReport {
  val timeout = Duration.create(60L, TimeUnit.SECONDS)

  private val tasksupport = new ForkJoinTaskSupport(new scala.concurrent.forkjoin.ForkJoinPool(6))

  case class KpiData(appkey: String, node: String, count: Long, tp90: Int, tp50: Int,
                     errorCount: Int, falconCount: Int, octoCount: Int, qps: Int = 0,
                     tp50Point: Option[Point] = None, qpsPoint: Option[Point] = None)

  private val env = "prod"
  private val role = "server"
  private val client = "client"
  private val group = "spanLocalHost"
  private val spanname = "all"
  private val localhost = "all"
  private val remoteAppkey = "all"
  private val remoteHost = "all"
  private val protocol = "thrift"
  private val dataSource = "hbase"

  def kpi(appkey: String, start: Date, end: Date, role: String = role, group: String, remoteAppkey: String) = {
    val start_time = (start.getTime / 1000).toInt
    val end_time = (end.getTime / 1000).toInt

    val kpiDatas = perfKpi(appkey, start_time, end_time, 0, role, "all", group, remoteAppkey)
    val falconErrors = ErrorQuery.getAppkeyFalconAlarm(appkey, start_time, end_time)
    val falconCount = if (falconErrors.nonEmpty) {
      val name = falconErrors.head.Grp_name
      (name, falconErrors.size)
    } else {
      ("", 0)
    }
    val kpiDataPar = kpiDatas.par
    kpiDataPar.tasksupport = tasksupport
    val perfKpiDatas = kpiDataPar.map {
      kpiData =>
        val appkey = kpiData.appkey
        val errorCount = ReportHelper.getDayErrorCount(appkey, start_time, end_time)
        val perfAlertCount = MonitorEvent.getEventCount(appkey, "", start_time * 1000L, end_time * 1000L)
        val data = kpiData.copy(node = falconCount._1, errorCount = errorCount.toInt, falconCount = falconCount._2, octoCount = perfAlertCount)
        data
    }
    perfKpiDatas.toList
  }


  def perfKpi(appkey: String, start: Int, end: Int, time: Int = 0, role: String = role, spanname:String = spanname,group: String = group, remoteAppkey: String = remoteAppkey) = {
    val result = DataQuery.getDataRecord(appkey, start, end, protocol, role, null, env, null, group, spanname, localhost,
      remoteAppkey, remoteHost, dataSource)
    //  parse result & store
    val dataResult= result.getOrElse(List())
    val dataResultPar = dataResult.par
    dataResultPar.tasksupport = tasksupport
    dataResultPar.map{
        v =>
          val appkey = if (group.equals("SpanRemoteApp")) {
            v.tags.remoteApp.getOrElse("")
          } else {
            v.appkey
          }
          calculatePerf(appkey, v, time)
    }
  }

  def calculatePerf(appkey: String, dataRecord: DataQuery.DataRecord, time: Int = 0) = {
    val allCountList = dataRecord.count.filter(c => c.y.nonEmpty && c.y.get.toLong != 0)
    val allCounts = allCountList.map(_.y.getOrElse(0.0))

    val top90List = dataRecord.tp90.filter(c => c.y.nonEmpty && c.y.get.toLong != 0)
    val top50List = dataRecord.tp50.filter(c => c.y.nonEmpty && c.y.get.toLong != 0)
    val qpsList = dataRecord.qps.filter(c => c.y.nonEmpty && c.y.get.toLong != 0)

    val top90s = top90List.map(_.y.getOrElse(0.0))
    val top50s = top50List.map(_.y.getOrElse(0.0))
    val qpss = qpsList.map(_.y.getOrElse(0.0))

    val count = allCounts.sum

    val tp90 = if (top90s.isEmpty) {
      0
    } else {
      (top90s.sum / top90s.size).toInt
    }

    val tp50 = if (top50s.isEmpty) {
      0
    } else {
      (top50s.sum / top50s.size).toInt
    }

    val qps = if (qpss.isEmpty) {
      0
    } else {
      (qpss.sum / qpss.size).toInt
    }
    val tp50Point = dataRecord.tp50.filter(_.ts.getOrElse(1) == time).headOption
    val qpsPoint = dataRecord.qps.filter(_.ts.getOrElse(1) == time).headOption
    KpiData(appkey, "", count.toLong, tp90, tp50, 0, 0, 0, qps, tp50Point, qpsPoint)
  }

}

