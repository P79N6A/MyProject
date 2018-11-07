package com.sankuai.octo.msgp.serivce.servicerep

import java.sql.Date

import com.sankuai.msgp.common.utils.StringUtil
import com.sankuai.octo.msgp.serivce.subscribe.AppkeySubscribe
import com.sankuai.octo.msgp.dao.report.ReportDailyDao
import com.sankuai.octo.msgp.domain.report.{DailyReportItem, DailyReportWrapper, PerformanceData}
import com.sankuai.octo.msgp.serivce.DomService
import com.sankuai.octo.mworth.util.DateTimeUtil

import scala.collection.JavaConverters._
import scala.collection.mutable.ListBuffer
import scala.collection.parallel.ForkJoinTaskSupport

/**
  * Created by yves on 16/10/22.
  * 服务日报
  */
object ServiceDailyReport {

  private val dailyReportThreadPool = new ForkJoinTaskSupport(new scala.concurrent.forkjoin.ForkJoinPool(4))

  case class DailyAppkeyData(owt: String = "", appkey: String = "", spanname: String = "",
                             count: Double = 0, successRatio: Double = 0.0, aliveRatio: String = "", errorCount: Double = 0L, perfAlertCount: Double = 0L,
                             qps: Double = 0, topQps: Double = 0, avgHostQps: Double = 0, topHostQps: Double = 0,
                             tp50: Double = 0, tp90: Double = 0, tp95: Double = 0, tp99: Double = 0, tp999: Double = 0,
                             isLoadBalance: Int = 0)

  def getDailyReport(username: String, startDate: Date) = {
    val yesterday = new Date(startDate.getTime - 86400000)
    val dateOfLastWeek = new Date(startDate.getTime - 7 * 86400000)
    val list = AppkeySubscribe.getSubscribeForDailyReport(username)
    val listPar = list.par
    listPar.tasksupport = dailyReportThreadPool
    //TODO 1. 前一天没有计算出来数据的appkey,同比和环比应该设为0 2. 对于周末和工作日,制定更好的同环比阈值
    val result = listPar.map {
      appkey =>
        getAppReportData(username, appkey, startDate, yesterday, dateOfLastWeek)
    }.toList.filter(_.getMainData.getCount.getValue != 0).sortBy(-_.getMainData.getCount.getValue)
    result.asJava
  }

  def getAllKpiAppkey() ={
    val yesterday = new Date(System.currentTimeMillis() - 86400000)
    ReportDailyDao.getAllAppkey(yesterday)
  }

  def getNonstandardAppkey(username: String)={
    val allNonstandardAppkey = DomService.getNonstandardAppkey(username).toList.distinct

    val groupSize = 3
    val groupNumber = allNonstandardAppkey.size / groupSize + 1
    val groupedAppkeys = (0 until groupNumber).map{
      index=>
        allNonstandardAppkey.slice(index * groupSize, (index + 1) * groupSize).asJava
    }
    if(groupedAppkeys.size == 1 && groupedAppkeys.head.size() == 0){
      List().asJava
    }else{
      groupedAppkeys.asJava
    }
  }

  /**
    * 获取每个appkey的各项指标数据/同比/环比
    *
    * @param username       查询人
    * @param appkey         appkey
    * @param startDate      待查询时间
    * @param yesterday      前一天
    * @param dateOfLastWeek 上周同一天
    * @return
    */
  def getAppReportData(username: String, appkey: String, startDate: Date, yesterday: Date, dateOfLastWeek: Date) = {
    //从数据库取数据,分为 appkey数据和接口数据
    val currentDataList = getAppDataDaily(username, appkey, startDate)
    val yesterdayDataList = getAppDataDaily(username, appkey, yesterday)
    val lastWeekDataList = getAppDataDaily(username, appkey, dateOfLastWeek)

    val owt = currentDataList.headOption.map(_.owt).getOrElse("")

    //计算同环比
    val comparedItems = currentDataList.map {
      currentData =>
        val spanname = currentData.spanname
        val yesterdayData = yesterdayDataList.find(x => x.spanname == spanname).getOrElse(DailyAppkeyData(spanname = spanname))
        val lastWeekData = lastWeekDataList.find(x => x.spanname == spanname).getOrElse(DailyAppkeyData(spanname = spanname))
        val yComparedResult = getComparedData(currentData, yesterdayData)
        val wComparedResult = getComparedData(currentData, lastWeekData)
        getMergedData(appkey, spanname, currentData, yComparedResult, wComparedResult)
    }
    //概要数据
    val mainData = comparedItems.find(_.getSpanname.equalsIgnoreCase("all")).getOrElse(new DailyReportItem())
    //分接口统计的数据
    val spannameData = comparedItems.filterNot(_.getSpanname.equalsIgnoreCase("all")).sortBy(-_.getCount.getValue)
    new DailyReportWrapper(appkey, owt, mainData, spannameData.asJava)
  }

  /**
    * 获得appkey下所有接口的性能数据
    *
    * @param username
    * @param appkey
    * @param date
    * @return
    */
  def getAppDataDaily(username: String, appkey: String, date: Date) = {
    val dailyData = ReportDailyDao.getAppkeyData(appkey, date).filterNot { x => StringUtil.isBlank(x.spanname) }
    if (dailyData.nonEmpty) {
      dailyData.map { data =>
        DailyAppkeyData(data.owt, data.appkey, data.spanname, data.count, data.successRatio.getOrElse(BigDecimal(100.0000)).doubleValue(),
          data.aliveRatio, data.errorCount, data.perfAlertCount,
          data.qps, data.topQps, data.avgHostQps, data.topHostQps,
          data.tp50, data.tp90, data.tp95, data.tp99, data.tp999,
          data.isLoadBalance)
      }
    } else {
      List(DailyAppkeyData(appkey))
    }
  }

  /**
    * 比较
    *
    * @param comparedData   待比较数据
    * @param referencedData 参考数据
    * @return
    */
  def getComparedData(comparedData: DailyAppkeyData, referencedData: DailyAppkeyData) = {
    val count = calculateGrowth(Boolean.box(false), comparedData.count, referencedData.count)
    val qps = calculateGrowth(Boolean.box(false), comparedData.qps, referencedData.qps)
    val successRatio = calculateGrowth(Boolean.box(false), comparedData.successRatio, referencedData.successRatio)
    val topQps = calculateGrowth(Boolean.box(false), comparedData.topQps, referencedData.topQps)
    val topHostQps = calculateGrowth(Boolean.box(false), comparedData.topHostQps, referencedData.topHostQps)
    val avgHostQps = calculateGrowth(Boolean.box(false), comparedData.avgHostQps, referencedData.avgHostQps)
    val tp50 = calculateGrowth(Boolean.box(false), comparedData.tp50, referencedData.tp50)
    val tp90 = calculateGrowth(Boolean.box(false), comparedData.tp90, referencedData.tp90)
    val tp95 = calculateGrowth(Boolean.box(false), comparedData.tp95, referencedData.tp95)
    val tp99 = calculateGrowth(Boolean.box(false), comparedData.tp99, referencedData.tp99)
    val tp999 = calculateGrowth(Boolean.box(false), comparedData.tp999, referencedData.tp999)
    val errorCount = calculateGrowth(Boolean.box(true), comparedData.errorCount, referencedData.errorCount)
    val perfAlertCount = calculateGrowth(Boolean.box(true), comparedData.perfAlertCount, referencedData.perfAlertCount)

    val comparison = referencedData.copy(count = count, errorCount = errorCount,
      successRatio = successRatio, perfAlertCount = perfAlertCount,
      qps = qps, topHostQps = topHostQps, avgHostQps = avgHostQps,
      topQps = topQps, tp50 = tp50, tp90 = tp90, tp95 = tp95, tp99 = tp99, tp999 = tp999)

    val list = ListBuffer[Boolean]()

    if (qps > 20.0 && comparedData.qps > 1500) {
      list.append(false)
    }

    if (comparedData.successRatio < 99.0) {
      list.append(false)
    }

    /*if (comparedData.isLoadBalance == 1) {
      list.append(false)
    }*/

    //注释掉判断指标, 与前端保持一致
    /*if (topHostQps > 20.0 && comparedData.topHostQps > 120) {
      list.append(false)
    }*/

//    if (tp50 > 20.0 && comparedData.tp50 > 100) {
//      list.append(false)
//    }

    if (tp90 > 20.0 && comparedData.tp90 > 200) {
      list.append(false)
    }
    /*
    if (tp95 > 20.0 && comparedData.tp95 > 300) {
      list.append(false)
    }

    if (tp99 > 20.0 && comparedData.tp99 > 400) {
      list.append(false)
    }*/

//    if (tp999 > 20.0 && comparedData.tp999 > 500) {
//      list.append(false)
//    }

    if (errorCount > 20.0 && comparedData.errorCount > 50) {
      list.append(false)
    }

//    if (perfAlertCount > 20.0 && comparedData.perfAlertCount > 5) {
//      list.append(false)
//    }

    val status = list.isEmpty
    (comparison, status)
  }

  /**
    *
    * @param zeroIgnored 计算同环比时, 前期数据是否可以是0
    * @param comparedData 待比较数据
    * @param referencedData 参考数据(昨日 Or 上周同期)
    * @return
    */
  def calculateGrowth(zeroIgnored: Boolean, comparedData: Double, referencedData: Double): Double = {
    if (referencedData > 0) {
      (comparedData - referencedData) / referencedData * 100.0
    } else if (referencedData == 0 && comparedData > 0) {
      //若前期数据为0, 且指标同环比允许为前期数据为0
      if(zeroIgnored){
        100.0
      }else{
        0.142857
      }
    } else {
      0.0
    }
  }

  def getMergedData(appkey: String, spanname: String, currentData: DailyAppkeyData, yComparedResult: (DailyAppkeyData, Boolean), wComparedResult: (DailyAppkeyData, Boolean)) = {
    val status = yComparedResult._2 && wComparedResult._2
    val yComparedData = yComparedResult._1
    val wComparedData = wComparedResult._1
    val countPerformanceData = new PerformanceData("count", currentData.count, yComparedData.count, wComparedData.count)
    val qpsPerformanceData = new PerformanceData("qps", currentData.qps, yComparedData.qps, wComparedData.qps)
    val tp50PerformanceData = new PerformanceData("tp50", currentData.tp50, yComparedData.tp50, wComparedData.tp50)
    val tp90PerformanceData = new PerformanceData("tp90", currentData.tp90, yComparedData.tp90, wComparedData.tp90)
    val tp95PerformanceData = new PerformanceData("tp95", currentData.tp95, yComparedData.tp95, wComparedData.tp95)
    val tp99PerformanceData = new PerformanceData("tp99", currentData.tp99, yComparedData.tp99, wComparedData.tp99)
    val tp999PerformanceData = new PerformanceData("tp999", currentData.tp999, yComparedData.tp999, wComparedData.tp999)
    val errorCountPerformanceData = new PerformanceData("errorCount", currentData.errorCount, yComparedData.errorCount, wComparedData.errorCount)
    val perfAlertPerformanceData = new PerformanceData("perfAlert", currentData.perfAlertCount, yComparedData.perfAlertCount, wComparedData.perfAlertCount)
    val isLoadBalance = currentData.isLoadBalance

    new DailyReportItem(appkey, spanname, status, currentData.successRatio, currentData.aliveRatio, countPerformanceData, qpsPerformanceData
      , tp50PerformanceData, tp90PerformanceData, tp95PerformanceData, tp99PerformanceData, tp999PerformanceData,
      errorCountPerformanceData, perfAlertPerformanceData, isLoadBalance)
  }
}
