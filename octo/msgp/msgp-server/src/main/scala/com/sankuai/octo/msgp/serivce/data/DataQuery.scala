package com.sankuai.octo.msgp.serivce.data

import java.util.Date
import java.util.concurrent.TimeUnit

import com.sankuai.msgp.common.config.MsgpConfig
import com.sankuai.msgp.common.config.db.msgp.Tables
import com.sankuai.msgp.common.model.Env
import com.sankuai.msgp.common.service.org.OpsService
import com.sankuai.msgp.common.utils.{DataQueryClient, HttpUtil, StringUtil}
import com.sankuai.msgp.common.utils.helper.{CommonHelper, JsonHelper}
import org.joda.time.LocalDate
import com.sankuai.octo.msgp.dao.availability.AvailabilityDao
import com.sankuai.octo.msgp.domain.IpPortHostname
import com.sankuai.octo.msgp.serivce.graph.ServiceModel.AppCall
import com.sankuai.octo.msgp.serivce.service.ServiceCommon
import com.sankuai.octo.mworth.util.DateTimeUtil
import com.sankuai.octo.statistic.constant.Constants
import com.sankuai.octo.statistic.util.ExecutionContextFactory
import dispatch._
import org.apache.commons.lang.StringUtils
import org.joda.time.format.DateTimeFormat
import org.joda.time.{DateTime, Days}
import org.slf4j.{Logger, LoggerFactory}
import play.api.libs.json.{Json, Reads}

import scala.collection.JavaConversions._
import scala.collection.JavaConverters._
import scala.collection.mutable.ListBuffer
import scala.collection.parallel.ForkJoinTaskSupport
import scala.concurrent.Await
import scala.concurrent.duration.{Duration, FiniteDuration}

object DataQuery {
  private val logger: Logger = LoggerFactory.getLogger(DataQuery.getClass)

  private implicit val ec = ExecutionContextFactory.build(Runtime.getRuntime.availableProcessors)

  private val taskSuppertPool = new ForkJoinTaskSupport(new scala.concurrent.forkjoin.ForkJoinPool(8))

  private val formatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss")
  private implicit val timeout = Duration.create(30L, TimeUnit.SECONDS)
  private val DAY_TIME: Long = 86400000L
  val POINT = List(Point(None, Some(0.0), Some(0)))
  private val threeDecimalFormatter = "%.3f"
  private val fourDecimalFormatter = "%.4f"


  //是否使用线上环境进行调试
  val useOnlineConfig = false

  val hostUrl = if (CommonHelper.isOffline && !useOnlineConfig) "http://query.octo.test.sankuai.info" else "http://data.octo.vip.sankuai.com"
  val queryDataSource = "hbase"

  case class LocalHostData(localHosts: java.util.List[IpPortHostname], idcLocalHosts: java.util.Map[String, java.util.List[String]], spannames: java.util.List[String])

  case class RemoteSpannameData(remoteAppKeys: java.util.List[String], spannames: java.util.List[String])


  class DailyData(val appkey: String = "", val spanname: String = "", val localhost: String = "", val remoteApp: String = "", val remoteHost: String = "", val count: Long = 0L,
                  val successCount: Long = 0L, val successCountPer: String = "",
                  val exceptionCount: Long = 0L, val exceptionCountPer: String = "",
                  val timeoutCount: Long = 0L, val timeoutCountPer: String = "",
                  val dropCount: Long = 0L, val dropCountPer: String = "",
                  val http2XXCount: Long = 0L, val http2XXCountPer: String = "",
                  val http3XXCount: Long = 0L, val http3XXCountPer: String = "",
                  val http4XXCount: Long = 0L, val http4XXCountPer: String = "",
                  val http5XXCount: Long = 0L, val http5XXCountPer: String = "",
                  val qps: Double = 0, val upper50: Double = 0, val upper90: Double = 0, val upper95: Double = 0,
                  val upper99: Double = 0, val upper999: Double = 0, val upper9999: Double = 0, val upper99999: Double = 0,
                  val upper999999: Double = 0, val upper: Double = 0, val costMean: Double = 0)

  case class DailyDataWithAvailability(appkey: String = "", spanname: String = "", localhost: String = "", remoteApp: String = "", remoteHost: String = "",
                                       count: Long = 0L, failCount: Long = 0L, successCount: Long = 0L, successCountPer: String = "",
                                       countClient: Long = 0L, failCountClient: Long = 0L, successCountClient: Long = 0L, successCountClientPer: String = "",
                                       failureDetails: FailureDetails,
                                       qps: Double = 0, costMean: Double = 0, topPercentile: TopPercentile, upper: Double = 0)
  case class TopPercentile(upper50: Double = 0, upper90: Double = 0, upper95: Double = 0, upper99: Double = 0, upper999: Double = 0, upper9999: Double = 0, upper99999: Double = 0, upper999999: Double = 0)

  case class DailyAvailabilityDetails(appkey: String = "", spanName: String = "", remoteApp: String = "", count: Long = 0L, successCount: Long = 0L, successCountPer: String = "",
                                      failCount: Long = 0L, failCountPer: String = "",
                                      exceptionCount: Long = 0L, exceptionCountPer: String = "",
                                      timeoutCount: Long = 0L, timeoutCountPer: String = "",
                                      dropCount: Long = 0L, dropCountPer: String = "")

  case class FailureDetails(exceptionCount: Long = 0L, timeoutCount: Long = 0L, dropCount: Long = 0L,
                            http4XXCount: Long = 0L, http5XXCount: Long = 0L)

  case class DailyAvailabilityTrend(dates: List[String], successRatioBasedClient: List[Double])

  case class DailyAvailabilityTrend2(dates: List[String], successRatio: List[BigDecimal])

  case class DailyAvailabilityAllData(dates: List[String], availabilityBasedClient: List[String], countBasedClient: List[Long], successCountBasedClient: List[Long], exceptionCountBasedClient: List[Long], timeoutCountBasedClient: List[Long],
                                      dropCountBasedClient: List[Long], http4xxCountBasedClient: List[Long], http5xxCountBasedClient: List[Long])

  case class DailyCountTrend(dates: List[String], countBasedServer: List[Double], countBasedClient: List[Double])

  case class DailyKPITrend(dates: List[String], qps: List[Double], tp50: List[Double], tp90: List[Double], tp95: List[Double], tp99: List[Double])

  case class DailyKPI(dates: List[String], availabilityTrend: DailyAvailabilityTrend, countTrend: DailyCountTrend, kpiTrend: DailyKPITrend)

  case class HostDetail(appkey: String, localHostDesc: IpPortHostname, remoteHostDesc: IpPortHostname,
                        count: Long, successCount: Long, http2XXCount: Long, http3XXCount: Long, failureDetails: FailureDetails,
                        qps: Double, tp50: Double, tp90: Double, tp99: Double, tp999: Double, tp9999: Double, tp99999: Double, tp999999: Double)

  case class SecondData(appkey: String, ip: String, start: Int, end: Int, qpsList: java.util.List[Integer]);

  /**
    * 根据不同的type获取不同功能的查询的起止时间
    *
    * @param _type
    */
  def getStartAndEndByType(_type: String, start: String, end: String) = {
    var startString = start
    var endString = end
    if (StringUtil.isBlank(start)) {
      _type match {
        case "operation" =>
          startString = DateTimeUtil.format(new Date(System.currentTimeMillis - 4 * 60 * 60 * 1000 - 60 * 1000), "yyyy-MM-dd HH:mm:00")
        case "stream" =>
          startString = DateTimeUtil.format(new Date(System.currentTimeMillis - 11 * 60 * 1000), "yyyy-MM-dd HH:mm:00")
        case _ =>
          startString = DateTimeUtil.format(new Date(System.currentTimeMillis - 61 * 60 * 1000), "yyyy-MM-dd HH:mm:00")
      }
    } else {
      startString = DateTimeUtil.format(DateTime.parse(start, DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss")).toDate, "yyyy-MM-dd HH:mm:00")
    }

    if (StringUtil.isBlank(end)) {
      endString = DateTimeUtil.format(new Date(System.currentTimeMillis - 60 * 1000), "yyyy-MM-dd HH:mm:00")
    } else {
      endString = DateTimeUtil.format(DateTime.parse(end, DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss")).toDate,
        "yyyy-MM-dd HH:mm:00")
    }
    List(startString, endString).asJava
  }


  //性能指标页面查询
  @throws(classOf[Exception])
  def getDailyPerformance(appkey: String, spanname: String, env: String, dateTime: DateTime, source: String = "server") = {
    val currentDate = new DateTime().withTimeAtStartOfDay()
    val allDailyData = getDailyStatisticFormatted(appkey, env, dateTime, source)
    val dailyData = if (StringUtil.isBlank(spanname)) {
      allDailyData
    } else {
      allDailyData.filter(_.spanname.equalsIgnoreCase(Constants.ALL))
    }

    val dailyReport = if (env == Env.prod.toString && source == "server") {
      if (StringUtil.isBlank(spanname)) {
        AvailabilityDao.fetchAvailability(List(appkey), dateTime)
      } else {
        val singleOpt = AvailabilityDao.fetchAvailabilitySingle(appkey, spanname, dateTime)
        if (singleOpt.isDefined) {
          List(singleOpt.get)
        } else {
          List()
        }
      }
    } else {
      List()
    }

    if (dailyData.nonEmpty) {
      dailyData.map { x =>
        val spanname = x.spanname
        val appkey = x.appkey

        //注: spaname为all的数据且服务为http/thrift混合型,才会存在下面两种http和thrift相加
        //thrif服务: x.exceptionCount + x.timeoutCount + x.dropCount
        //http服务: x.http4XXCount + x.http5XXCount
        val count = x.count
        val failCount = x.exceptionCount + x.timeoutCount + x.dropCount + x.http4XXCount + x.http5XXCount
        val successCount = x.successCount
        val successCountPer = x.successCountPer

        val clientCountData = if (dateTime.isBefore(currentDate.getMillis) && env == Env.prod.toString && source == "server") {
          val list = dailyReport.filter(x => x.spanname.equalsIgnoreCase(spanname))
          if (list.nonEmpty) {
            val data = list.head
            val countClient = data.count
            val failCountClient = data.exceptionCount + data.timeoutCount + data.dropCount + data.http4xxCount + data.http5xxCount

            val successCountClient = data.successCount
            val ratioAsBigDecimal = data.successCountPer.getOrElse(BigDecimal(0.0))
            val successCountClientPer = if (ratioAsBigDecimal.doubleValue() == 0.0) {
              "0%"
            } else if (ratioAsBigDecimal.doubleValue() == 100.0) {
              "100%"
            } else {
              s"${fourDecimalFormatter.format(ratioAsBigDecimal)}%"
            }
            (countClient, failCountClient, successCountClient, successCountClientPer)
          } else {
            (0L, 0L, 0L, "0%")
          }
        } else {
          //其他情况: 1, prod下 当天数据; 2, 非prod下当天或者非当天数据。 这两种情况都使用server端统计的结果
          //服务端的successCount和successCountPer是准确的
          (count, failCount, successCount, successCountPer)
        }

        //从客户端查不到错误详情的时候,可以参考从服务端统计的数据
        val failureDetails = FailureDetails(x.exceptionCount, x.timeoutCount, x.dropCount, x.http4XXCount, x.http5XXCount)

        val topPercentile = TopPercentile(x.upper50, x.upper90, x.upper95, x.upper99, x.upper999, x.upper9999, x.upper99999, x.upper999999)

        DailyDataWithAvailability(appkey, spanname, x.localhost, x.remoteApp, x.remoteHost,
          count, failCount, successCount, successCountPer,
          clientCountData._1, clientCountData._2, clientCountData._3, clientCountData._4, failureDetails,
          x.qps, x.costMean, topPercentile, x.upper)
      }
    } else {
      List[DailyDataWithAvailability]()
    }
  }

  /**
    * 根据spanname获取接口的客户端调用情况
    *
    * @param appkey
    * @param spanName
    * @param env
    * @param dateTime
    * @return
    */
  def getAvailabilityDetails(appkey: String, spanName: String, env: String, dateTime: DateTime) = {
    val result = AvailabilityDao.fetchAvailabilityDetails(appkey, spanName, dateTime)
    result.sortWith((s, t) => s.failureCount.compareTo(t.failureCount) > 0)
  }

  /**
    *
    * 获取appkey的可用性趋势数据(Client端统计)
    *
    * @param appkey
    * @param dateTime
    */
  def getAvailabilityTrend(appkey: String, spanname: String, dateTime: DateTime) = {
    val result = AvailabilityDao.fetchAvailabilityTrend(appkey, spanname, dateTime).groupBy(_.ts)
    val end = dateTime.withTimeAtStartOfDay().getMillis
    val start = dateTime.withTimeAtStartOfDay().minusDays(30).getMillis
    val trendData = (start to end by DAY_TIME).map {
      ts =>
        val dailyData = result.getOrElse((ts / 1000).toInt, List[Tables.AvailabilityDayReport#TableElementType]())
        val date = new DateTime(ts).toString("yyyy-MM-dd")
        val successRatio = if (dailyData.isEmpty || CommonHelper.isOffline) {
          BigDecimal(100.0)
        } else {
          dailyData.head.successCountPer.get
        }
        (date, successRatio)
    }.toList
    val dateList = trendData.map(_._1)
    val successRatioList = trendData.map(_._2)
    DailyAvailabilityTrend2(dateList, successRatioList)
  }


  /**
    * 从tair中获取实时天粒度数据(Thrift接口)
    *
    * @param appkey
    * @param env
    * @param dateTime
    * @param source
    * @return
    */
  def getDailyStatistic(appkey: String, env: String, dateTime: DateTime, source: String = "server") = {
    val result = DataQueryClient.queryDailyData(appkey, (dateTime.getMillis / 1000).toInt, env, source)
    if (result != null) {
      Some(result.getRecordList)
    } else {
      None
    }
  }

  def getDailyStatisticFormatted(appkey: String, env: String, dateTime: DateTime, source: String = "server") = {
    val dataOpt = getDailyStatistic(appkey, env, dateTime, source)
    dataOpt match {
      case Some(records) =>
        records.map {
          record =>

            val tags = record.getTags
            val count = record.getCount
            //TODO 注: 此处的successCount指的是thrift统计的successCount, 应修改Query接口
            val successCount = record.getSuccessCount
            val exceptionCount = record.getExceptionCount
            val timeoutCount = record.getTimeoutCount
            val dropCount = record.getDropCount

            var successCountPer, exceptionCountPer, timeoutCountPer, dropCountPer = "0.0000%"

            val http2XXCount = record.getHTTP2XXCount
            val http3XXCount = record.getHTTP3XXCount
            val http4XXCount = record.getHTTP4XXCount
            val http5XXCount = record.getHTTP5XXCount

            var http2XXCountPer, http3XXCountPer, http4XXCountPer, http5XXCountPer = "0.0000%"

            val realSuccessCount = successCount + http2XXCount + http3XXCount
            if (count != 0) {
              successCountPer = if (realSuccessCount == count) {
                "100%"
              } else if (realSuccessCount == 0) {
                "0%"
              } else {
                s"${fourDecimalFormatter.format(realSuccessCount.toDouble / count * 100)}%"
              }
              exceptionCountPer = s"${fourDecimalFormatter.format(exceptionCount.toDouble / count * 100)}%"
              timeoutCountPer = s"${fourDecimalFormatter.format(timeoutCount.toDouble / count * 100)}%"
              dropCountPer = s"${fourDecimalFormatter.format(dropCount.toDouble / count * 100)}%"

              http2XXCountPer = s"${fourDecimalFormatter.format(http2XXCount.toDouble / count * 100)}%"
              http3XXCountPer = s"${fourDecimalFormatter.format(http3XXCount.toDouble / count * 100)}%"
              http4XXCountPer = s"${fourDecimalFormatter.format(http4XXCount.toDouble / count * 100)}%"
              http5XXCountPer = s"${fourDecimalFormatter.format(http5XXCount.toDouble / count * 100)}%"
            }

            new DailyData(appkey, tags.getSpanname, tags.getLocalHost,
              tags.getRemoteAppKey, tags.getRemoteHost, count,
              successCount, successCountPer,
              exceptionCount, exceptionCountPer,
              timeoutCount, timeoutCountPer,
              dropCount, dropCountPer,
              http2XXCount, http2XXCountPer,
              http3XXCount, http3XXCountPer,
              http4XXCount, http4XXCountPer,
              http5XXCount, http5XXCountPer,
              record.getQps.toInt, record.getCost50.toInt, record.getCost90.toInt, record.getCost95.toInt, record.getCost99.toInt, record.getCost999.toInt, record.getCost9999.toInt, record.getCost99999.toInt, record.getCost999999.toInt,
              record.getCostMax.toInt, record.getCostMean.toInt)
        }
      case None =>
        logger.error(s"get dailyKpi failed")
        throw new Exception(s"get dailyKpi failed")
    }
  }

  def getHostDetails(appkey: String, start: Int, end: Int, protocolType: String, role: String, dataType: String, env: String, unit: String, group: String,
                     spanname: String, localhost: String, remoteAppkey: String, remoteHost: String, dataSource: String) = {

    val data = getDataRecord(appkey, start, end, protocolType, role, dataType, env, unit, group, spanname, localhost, remoteAppkey, remoteHost, dataSource)
    if (data.nonEmpty) {
      val mergedRecords = mergeData(data.get)
      val localHosts = mergedRecords.map(_.tags.localhost).map(_.getOrElse("all"))
      val localHostsDesc = ip2HostDesc(localHosts)
      val remoteHosts = mergedRecords.map(_.tags.remoteHost).map(_.getOrElse("all"))
      val remoteHostsDesc = ip2HostDesc(remoteHosts)

      val hostDetails = mergedRecords.zipWithIndex.map { case (record, index) =>
        val failureDetails = FailureDetails(record.exceptionCount.toLong, record.timeoutCount.toLong, record.dropCount.toLong, record.HTTP4XXCount.toLong, record.HTTP5XXCount.toLong)
        HostDetail(record.appkey, localHostsDesc.apply(index), remoteHostsDesc.apply(index),
          record.count.toLong, record.successCount.toLong, record.HTTP2XXCount.toLong, record.HTTP3XXCount.toLong, failureDetails,
          record.qps, record.tp50, record.tp90, record.tp99, record.tp999, record.tp9999, record.tp99999, record.tp999999)
      }
      JsonHelper.dataJson(hostDetails)
    } else {
      JsonHelper.errorJson("get host details failed")
    }
  }

  /**
    * 主机分析
    *
    * @param appkey
    * @param start
    * @param end
    * @param protocolType
    * @param role
    * @param dataType
    * @param env
    * @param unit
    * @param group
    * @param spanname
    * @param localhost
    * @param remoteAppkey
    * @param remoteHost
    * @param dataSource
    * @param idc
    * @param idcLocalHosts
    * @return
    */
  def getHistoryStatisticByHost(appkey: String, start: Int, end: Int, protocolType: String, role: String, dataType: String, env: String, unit: String, group: String,
                                spanname: String, localhost: String, remoteAppkey: String, remoteHost: String, dataSource: String, idc: String, idcLocalHosts: String) = {

    val recordList = if (StringUtils.isBlank(idc)) {
      getDataRecord(appkey, start, end, protocolType, role, dataType, env, unit, group, spanname, localhost, remoteAppkey, remoteHost, dataSource).getOrElse(List())
    } else {
      // 有idc的情况 all yf,dx等参数
      if (localhost == "*" && idc != "all") {
        //具体机房
        val localhosts = idcLocalHosts.split(",").toList.filter {
          x =>
            x != "all" && x != "*"
        }
        getHistoryStatisticByHostList(appkey, start, end, protocolType, role, dataType, env, unit, group, spanname, localhosts, remoteAppkey, remoteHost, dataSource, localhost)
      } else {
        getDataRecord(appkey, start, end, protocolType, role, dataType, env, unit, group, spanname, localhost, remoteAppkey, remoteHost, dataSource).getOrElse(List())
      }
    }
    recordList
  }


  def getHistoryStatisticMergedByHost(appkey: String, start: Int, end: Int, protocolType: String, role: String, dataType: String, env: String, unit: String, group: String,
                                      spanname: String, localhost: String, remoteAppkey: String, remoteHost: String, dataSource: String, idc: String, idcLocalHosts: String) = {
    val recordList = if (StringUtils.isBlank(idc)) {
      getDataRecordMerged(appkey, start, end, protocolType, role, dataType, env, unit, group, spanname, localhost, remoteAppkey, remoteHost, dataSource).getOrElse(List())
    } else {
      // 有idc的情况 all yf,dx等参数
      if (localhost == "*" && idc != "all") {
        //具体机房
        val localhosts = idcLocalHosts.split(",").toList.filter {
          x =>
            x != "all" && x != "*"
        }
        val result = getHistoryStatisticByHostList(appkey, start, end, protocolType, role, dataType, env, unit, group, spanname, localhosts, remoteAppkey, remoteHost, dataSource, localhost)
        mergeData(result)
      } else {
        getDataRecordMerged(appkey, start, end, protocolType, role, dataType, env, unit, group, spanname, localhost, remoteAppkey, remoteHost, dataSource).getOrElse(List())
      }
    }
    recordList
  }

  /**
    * 多线程获取
    *
    * @param appkey
    * @param start
    * @param end
    * @param protocolType
    * @param role
    * @param dataType
    * @param env
    * @param unit
    * @param group
    * @param spanname
    * @param localhosts
    * @param remoteAppkey
    * @param remoteHost
    * @param dataSource
    * @param localhost
    * @return
    */
  def getHistoryStatisticByHostList(appkey: String, start: Int, end: Int, protocolType: String, role: String, dataType: String, env: String, unit: String,
                                    group: String, spanname: String, localhosts: List[String], remoteAppkey: String, remoteHost: String, dataSource: String, localhost: String) = {
    val localHostPar = localhosts.par
    localHostPar.tasksupport = taskSuppertPool
    val allLocalData = localHostPar.flatMap {
      ip =>
        val result = getDataRecord(appkey, start, end, protocolType, role, dataType, env, unit, group, spanname, ip, remoteAppkey, remoteHost, dataSource)
        result.getOrElse(List())
    }.toList
    localhost match {
      case "*" =>
        //构成一个新的 列表
        allLocalData
      case _ =>
        //拼接成一个新的对象
        if (allLocalData.isEmpty) {
          allLocalData
        } else {
          val tags = allLocalData.head.tags.copy(localhost = Some("all"))
          val count = ListBuffer[Point]()
          val tp50 = ListBuffer[Point]()
          val tp90 = ListBuffer[Point]()
          val tp99 = ListBuffer[Point]()
          val tp999 = ListBuffer[Point]()
          val tp9999 = ListBuffer[Point]()
          val tp99999 = ListBuffer[Point]()
          val tp999999 = ListBuffer[Point]()
          val qps = ListBuffer[Point]()
          val dropQps = ListBuffer[Point]()
          val successCount = ListBuffer[Point]()
          val exceptionCount = ListBuffer[Point]()
          val timeoutCount = ListBuffer[Point]()
          val dropCount = ListBuffer[Point]()
          allLocalData.foreach {
            x =>
              count.appendAll(x.count)
              successCount.appendAll(x.successCount)
              exceptionCount.appendAll(x.exceptionCount)
              timeoutCount.appendAll(x.timeoutCount)
              dropCount.appendAll(x.dropCount)
              qps.appendAll(x.qps)
              dropQps.appendAll(x.dropQps)
              val localtp50 = x.tp50.indices.map {
                i =>
                  val tp = x.tp50.apply(i).y.getOrElse(0.0) * x.count.apply(i).y.getOrElse(0.0)
                  x.tp50.apply(i).copy(y = Some(tp))
              }
              val localtp90 = x.tp90.indices.map {
                i =>
                  val tp = x.tp90.apply(i).y.getOrElse(0.0) * x.count.apply(i).y.getOrElse(0.0)
                  x.tp90.apply(i).copy(y = Some(tp))
              }
              val localtp99 = x.tp99.indices.map {
                i =>
                  val tp = x.tp99.apply(i).y.getOrElse(0.0) * x.count.apply(i).y.getOrElse(0.0)
                  x.tp99.apply(i).copy(y = Some(tp))
              }
              val localtp999 = x.tp999.indices.map {
                i =>
                  val tp = x.tp999.apply(i).y.getOrElse(0.0) * x.count.apply(i).y.getOrElse(0.0)
                  x.tp999.apply(i).copy(y = Some(tp))
              }
              val localtp9999 = x.tp9999.indices.map {
                i =>
                  val tp = x.tp9999.apply(i).y.getOrElse(0.0) * x.count.apply(i).y.getOrElse(0.0)
                  x.tp9999.apply(i).copy(y = Some(tp))
              }
              val localtp99999 = x.tp99999.indices.map {
                i =>
                  val tp = x.tp99999.apply(i).y.getOrElse(0.0) * x.count.apply(i).y.getOrElse(0.0)
                  x.tp99999.apply(i).copy(y = Some(tp))
              }
              val localtp999999 = x.tp999999.indices.map {
                i =>
                  val tp = x.tp999999.apply(i).y.getOrElse(0.0) * x.count.apply(i).y.getOrElse(0.0)
                  x.tp999999.apply(i).copy(y = Some(tp))
              }

              tp50.appendAll(localtp50)
              tp90.appendAll(localtp90)
              tp99.appendAll(localtp99)
              tp999.appendAll(localtp999)
              tp9999.appendAll(localtp9999)
              tp99999.appendAll(localtp99999)
              tp999999.appendAll(localtp999999)
          }

          val record = DataRecord(appkey, tags, getPoints(count), getPoints(successCount), getPoints(exceptionCount), getPoints(timeoutCount),
            getPoints(dropCount), getPoints(qps), getTpPoints(tp50, count), getTpPoints(tp90, count),
            getTpPoints(tp99, count), getTpPoints(tp999, count), getTpPoints(tp9999, count), getTpPoints(tp99999, count),
            getTpPoints(tp999999, count),getPoints(dropQps))
          List(record)
        }
    }
  }

  def getPoints(points: ListBuffer[Point]): List[Point] = {
    val list = points.groupBy(_.ts).map {
      x =>
        val list = x._2
        val c = list.map(_.y.getOrElse(0.0)).sum
        list.head.copy(y = Some(c))
    }.toList
    list
  }

  def getTpPoints(points: ListBuffer[Point], cpoints: ListBuffer[Point]): List[Point] = {
    val cMap = cpoints.groupBy(_.ts)
    val list = points.groupBy(_.ts).map {
      x =>
        val list = x._2
        val topY = list.map(_.y.getOrElse(0.0)).sum
        val countY = cMap.getOrElse(x._1, ListBuffer[Point]()).map(_.y.getOrElse(0.0)).sum
        val y = if (countY == 0) {
          0
        } else {
          (topY / countY).toInt.toDouble
        }
        list.head.copy(y = Some(y))
    }.toList
    list
  }

  def getHistoryStatisticMerged(appkey: String, start: Int, end: Int, protocolType: String, role: String, dataType: String, env: String, unit: String, group: String,
                                spanname: String, localhost: String, remoteAppkey: String, remoteHost: String, dataSource: String, merge: Boolean) = {
    if (merge) {
      val result = getDataRecordMerged(appkey, start, end, protocolType, role, dataType, env, unit, group, spanname, localhost, remoteAppkey, remoteHost, dataSource)
      if (result.isDefined) {
        JsonHelper.dataJson(result.get)
      } else {
        JsonHelper.errorJson("get dailyKpi failed")
      }
    } else {
      val result = getDataRecord(appkey, start, end, protocolType, role, dataType, env, unit, group, spanname, localhost, remoteAppkey, remoteHost, dataSource)
      if (result.isDefined) {
        JsonHelper.dataJson(result.get)
      } else {
        JsonHelper.errorJson("get dailyKpi failed")
      }
    }
  }


  /**
    * Thrift接口
    *
    * @param appkey
    * @param start
    * @param end
    * @param protocolType
    * @param role
    * @param dataType
    * @param env
    * @param unit
    * @param group
    * @param spanname
    * @param localhost
    * @param remoteAppkey
    * @param remoteHost
    * @param dataSource
    */
  def getDataRecord(appkey: String, start: Int, end: Int, protocolType: String, role: String, dataType: String, env: String, unit: String,
                    group: String, spanname: String, localhost: String, remoteAppkey: String, remoteHost: String, dataSource: String) = {
    val result = DataQueryClient.queryHistoryData(appkey, start, end, protocolType, role,
      dataType, env, unit, spanname, localhost, remoteAppkey, remoteHost, "", dataSource)
    if (result != null) {
      Some(QueryHelper.toScalaDataRecord(result))
    } else {
      None
    }
  }

  def getDataRecordMerged(appkey: String, start: Int, end: Int, protocolType: String, role: String, dataType: String, env: String, unit: String,
                          group: String, spanname: String, localhost: String, remoteAppkey: String, remoteHost: String, dataSource: String) = {
    val result = DataQueryClient.queryHistoryDataMerged(appkey, start, end, protocolType, role,
      dataType, env, unit, spanname, localhost, remoteAppkey, remoteHost, "", dataSource)
    if (result != null) {
      Some(QueryHelper.toScalaDataRecordMerged(result))
    } else {
      None
    }
  }

  /**
    * 秒级指标
    *
    */
  def getSecondLevelData(appkey: String, start: Int, end: Int, env: String, ip:String) = {
    val result = DataQueryClient.querySecondLevelData(appkey, start, end, env, ip)

    if(result != null) {
      val res = ListBuffer[SecondData]()

      val ipToQpsMap = result.getIpToSecondQpsMap().asScala
      ipToQpsMap.keys.foreach(ip => {
        val secondData = SecondData(appkey, ip, start, end, ipToQpsMap(ip))
        res.append(secondData)
      })
      Some(res.toList)
    } else {
      None
    }
  }




  /**
    * 用于性能指标 - 趋势
    * 由于离线数据的问题，性能指标的日趋势通过tair获取
    * TODO 目前dailyDataBasedClient中的调用次数count是服务器端的调用次数，但不是从tair获取，所以和dailyKPIsBasedServer的count不一致，之后需要改成客户端的调用次数数据
    *
    */
  def getDailyKpiTrends(appkey: String, spanname: String, date: String, env: String, source: String) = {
    //start end 转为时间
    val formatter = DateTimeFormat.forPattern("yyyy-MM-dd")
    val endTime = formatter.parseLocalDate(date)
    val startTime = endTime.minusDays(14)
    val days = Days.daysBetween(startTime, endTime).getDays
    val dayPar = (0 until days).par
    dayPar.tasksupport = taskSuppertPool
    val dailyKPIsBasedServer = dayPar.map {
      x =>
        val dataTime = startTime.plusDays(x)
        val date = dataTime.toDateTimeAtStartOfDay
        val data = getDailyStatisticFormatted(appkey, env, date, source).filter(_.spanname == spanname)
        (date.getMillis / 1000, data.headOption)
    }.toList.sortBy(_._1)
    val dailyKPIsBasedClient = AvailabilityDao.fetchDailyAvailability(appkey, spanname, startTime.toDateTimeAtStartOfDay, endTime.toDateTimeAtStartOfDay).groupBy(_.ts)

    val dates = ListBuffer[String]()
    val countBasedServer = ListBuffer[Double]()
    val countBasedClient = ListBuffer[Double]()
    val successRatioBasedClient = ListBuffer[Double]()
    val tp50 = ListBuffer[Double]()
    val tp90 = ListBuffer[Double]()
    val tp95 = ListBuffer[Double]()
    val tp99 = ListBuffer[Double]()
    val qps = ListBuffer[Double]()

    dailyKPIsBasedServer.foreach {
      dailyKpi =>
        val ts = dailyKpi._1
        val dailyDataBasedServer = dailyKpi._2.getOrElse(new DailyData())
        val dailyDataBasedClient = dailyKPIsBasedClient.getOrElse(ts.toInt, List[Tables.AvailabilityDayReport#TableElementType]())
        val date = new DateTime(ts * 1000L).toString("yyyy-MM-dd")
        dates.append(date)

        countBasedServer.append(dailyDataBasedServer.count)
        val successCountPercentStr = dailyDataBasedServer.successCountPer
        val successRatioBasedServerValue = if (successCountPercentStr.length == 0) 0.0 else successCountPercentStr.substring(0, successCountPercentStr.length - 1).toDouble
        if (dailyDataBasedClient.isEmpty) {
          countBasedClient.append(dailyDataBasedServer.count)
          successRatioBasedClient.append(successRatioBasedServerValue)
        } else {
          countBasedClient.append(dailyDataBasedClient.head.count.toDouble)
          successRatioBasedClient.append(dailyDataBasedClient.head.successCountPer.get.toDouble)
        }

        tp50.append(dailyDataBasedServer.upper50)
        tp90.append(dailyDataBasedServer.upper90)
        tp95.append(dailyDataBasedServer.upper95)
        tp99.append(dailyDataBasedServer.upper99)
        qps.append(dailyDataBasedServer.qps)
    }

    val countTrend = DailyCountTrend(dates.toList, countBasedServer.toList, countBasedClient.toList)
    val kpiTrend = DailyKPITrend(dates.toList, qps.toList, tp50.toList, tp90.toList, tp95.toList, tp99.toList)
    val availabilityTrend = DailyAvailabilityTrend(dates.toList, successRatioBasedClient.toList)
    DailyKPI(dates.toList, availabilityTrend, countTrend, kpiTrend)
  }

  /**
    * 用于数据总览中的可用率数据
    * 得到all接口的客户端可用率数据
    *
    */
  def getDailyAvailability(appkey: String, spanname: String, env: String, source: String) = {
    val endTime = LocalDate.now()
    val startTime = endTime.minusDays(182)
    val dates = ListBuffer[String]()
    dates.append(startTime.toString())
    var Time = startTime
    for (a <- 1 to 181) {
      val oneDayAfter = Time.plusDays(1)
      dates.append(oneDayAfter.toString())
      Time = oneDayAfter
    }
    val dailyKPIsBasedClient = AvailabilityDao.fetchDailyAvailability(appkey, spanname, startTime.toDateTimeAtStartOfDay, endTime.toDateTimeAtStartOfDay).groupBy(_.ts)
    val availabilityBasedClient = ListBuffer[String]()
    val countBasedClient = ListBuffer[Long]()
    val successCountBasedClient = ListBuffer[Long]()
    val exceptionCountBasedClient = ListBuffer[Long]()
    val timeoutCountBasedClient = ListBuffer[Long]()
    val dropCountBasedClient = ListBuffer[Long]()
    val http4xxCountBasedClient = ListBuffer[Long]()
    val http5xxCountBasedClient = ListBuffer[Long]()

    dates.foreach {
      eachDay =>
        val time: Long = new DateTime(eachDay).getMillis()
        val timeMillis: Long = time / 1000
        val dailyDataBasedClient = dailyKPIsBasedClient.getOrElse(timeMillis.toInt, List[Tables.AvailabilityDayReport#TableElementType]())

        if (dailyDataBasedClient.isEmpty) {
          availabilityBasedClient.append("-")
          countBasedClient.append(0)
          successCountBasedClient.append(0)
          exceptionCountBasedClient.append(0)
          timeoutCountBasedClient.append(0)
          dropCountBasedClient.append(0)
          http4xxCountBasedClient.append(0)
          http5xxCountBasedClient.append(0)
        } else {
          availabilityBasedClient.append(dailyDataBasedClient.head.successCountPer.get.toString())
          countBasedClient.append(dailyDataBasedClient.head.count)
          successCountBasedClient.append(dailyDataBasedClient.head.successCount)
          exceptionCountBasedClient.append(dailyDataBasedClient.head.exceptionCount)
          timeoutCountBasedClient.append(dailyDataBasedClient.head.timeoutCount)
          dropCountBasedClient.append(dailyDataBasedClient.head.dropCount)
          http4xxCountBasedClient.append(dailyDataBasedClient.head.http4xxCount)
          http5xxCountBasedClient.append(dailyDataBasedClient.head.http5xxCount)
        }
    }
    DailyAvailabilityAllData(dates.toList, availabilityBasedClient.toList, countBasedClient.toList, successCountBasedClient.toList, exceptionCountBasedClient.toList,
      timeoutCountBasedClient.toList, dropCountBasedClient.toList, http4xxCountBasedClient.toList, http5xxCountBasedClient.toList)
  }

  implicit val var1 = Json.reads[MetricsTags]
  implicit val var2 = Json.writes[MetricsTags]


  /**
    * 获取服务tag
    *
    * @param appkey
    * @param start
    * @param end
    * @param env
    * @param source
    * @return
    */
  def tags(appkey: String, start: Int, end: Int, env: String, source: String) = {
    try {
      val tagQueryResult = DataQueryClient.queryTag(appkey, start, end, env, source)
      MetricsTags(tagQueryResult.getSpannames.asScala.toList,
        tagQueryResult.getLocalHosts.asScala.toList, tagQueryResult.getRemoteAppKeys.asScala.toList,
        tagQueryResult.getRemoteHosts.asScala.toList)
    } catch {
      case e: Exception =>
        logger.error(s"get tags error", e)
        MetricsTags(List(), List(), List(), List())
    }
  }

  /**
    *
    * TODO:remoteHostname 有可能很多导致无法查询成功eg:waimai_api
    */
  case class TagsWithHostname(spannames: List[String], localHosts: List[String], remoteAppKeys: List[String],
                              remoteHosts: List[String], localHostname: List[IpPortHostname],
                              remoteHostname: List[IpPortHostname], idcLocalHosts: Map[String, List[String]])

  def ip2HostDesc(ips: List[String]) = {
    val ipsPar = ips.par
    ipsPar.tasksupport = taskSuppertPool
    ipsPar.map {
      x =>
        if (x == "*" || x == "all") {
          new IpPortHostname(x, x, "", x, x)
        } else {
          val hostname = OpsService.ipToHost(x)
          new IpPortHostname(x, x, "", hostname, s"$hostname($x)")
        }
    }.toList
  }

  private def ip2Idc(ips: List[String]) = {
    val map = scala.collection.mutable.Map[String, List[String]]()
    //这里要禁止这种par的并行操作，否则下面的map存在并发写问题
//    val ipsPar = ips.par
//    ipsPar.tasksupport = taskSuppertPool
    ips.foreach {
      x =>
        if (x != "*" && x != "all") {
          val idc = CommonHelper.ip2IDC(x)
          val list = map.getOrElse(idc, List[String]("*", "all"))
          val list2 = list.::(x)
          map.put(idc, list2)
        }
    }
    map.put("all", ips)
    map.toMap
  }

  def getAppLocalhost(appkey: String, env: String, source: String, start: Int, end: Int) = {
    val metricTags = tags(appkey, start, end, env, source)
    val localHostname = ip2HostDesc(metricTags.localHostList)
    val idcLocalHosts = ip2Idc(metricTags.localHostList)

    val spannames = metricTags.spannameList

    val localhost = localHostname.filter {
      hostname =>
        hostname.getIp != "*" && hostname.getIp != "all"
    }

    val hosts = localhost
    val idcMap = new java.util.LinkedHashMap[String, java.util.List[String]]()
    idcLocalHosts.foreach {
      idcHost =>
        idcMap.put(idcHost._1, idcHost._2.asJava)
    }
    LocalHostData(hosts, idcMap, spannames.asJava)
  }


  /**
    * 获得与服务相关的RemoteAppkey
    * 可能给是调用方, 也可能是被调用方
    *
    * @param appkey
    * @param env
    * @param source client or server
    * @return
    */
  def getAppRemoteAppkey(appkey: String, env: String, source: String) = {
    val start = (new DateTime().minusDays(7).getMillis / 1000).toInt
    val end = (new DateTime().getMillis / 1000).toInt
    val metricTags = tags(appkey, start, end, env, source)
    val spannames = metricTags.spannames.map {
      spanname =>
        spanname.replaceAll("\\n", "").replaceAll("'", "\\\\'").replaceAll("\\s{2,}", " ")
    }
    RemoteSpannameData(metricTags.remoteAppKeys.asJava, spannames.asJava)
  }

  def getTagsAndHostname(appkey: String, start: Int, end: Int, env: String, source: String) = {
    val metricTags = tags(appkey, start, end, env, source)
    TagsWithHostname(metricTags.spannameList, metricTags.localHostList,
      metricTags.remoteAppList, metricTags.remoteHostList, ip2HostDesc(metricTags.localHostList),
      ip2HostDesc(metricTags.remoteHostList), ip2Idc(metricTags.localHostList))
  }

  case class DataSeries(timestamp: Int = 0, value: Double = 0.0)

  implicit val var3 = Json.reads[DataSeries]
  implicit val var4 = Json.writes[DataSeries]

  case class FalconLastData(count: Option[DataSeries], qps: Option[DataSeries], cost_50: Option[DataSeries],
                            cost_90: Option[DataSeries], cost_95: Option[DataSeries], cost_99: Option[DataSeries],
                            qps_drop: Option[DataSeries])

  implicit val var5 = Json.reads[FalconLastData]
  implicit val var6 = Json.writes[FalconLastData]

  case class FalconDataResponse(value: DataSeries, counter: String, endpoint: String)

  implicit val FalconDataResponseRead = Json.reads[FalconDataResponse]
  implicit val FalconDataResponseWrite = Json.writes[FalconDataResponse]

  def lastData(appkey: String, env: String, source: String, spanname: String, group: String = "span") = {
    val now = DateTime.now().minusMinutes(3)
    val start = (now.getMillis / 1000).toInt
    val end = start
    val data = getDataRecord(appkey, start, end, "", source, "", env,
      "Minute", group, spanname, "all", "all", "", "")
    try {
      if (data.nonEmpty && data.get.nonEmpty) {
        val hbaseData = data.get.head
        val last_count = if (hbaseData.count.nonEmpty) {
          val count = hbaseData.count.head
          Some(DataSeries(count.ts.getOrElse(0), count.y.getOrElse(0.0)))
        } else {
          None
        }
        val last_qps = if (hbaseData.qps.nonEmpty) {
          val qps = hbaseData.qps.head
          Some(DataSeries(qps.ts.getOrElse(0), qps.y.getOrElse(0.0)))
        } else {
          None
        }
        val tp50 = hbaseData.tp50.head
        val tp90 = hbaseData.tp90.head
        val tp99 = hbaseData.tp99.head
        Some(FalconLastData(last_count, last_qps,
          Some(DataSeries(tp50.ts.getOrElse(0), tp50.y.getOrElse(0.0))),
          Some(DataSeries(tp90.ts.getOrElse(0), tp90.y.getOrElse(0.0))),
          None,
          Some(DataSeries(tp99.ts.getOrElse(0), tp99.y.getOrElse(0.0))),
          None))
      } else {
        None
      }
    }
    catch {
      case e: Exception =>
        logger.error(s"last data :$appkey,env:$env,source:$source,spanname:$spanname,data:$data,error", e)
        None
    }
  }

//  def main(args: Array[String]): Unit = {
//    val appkey = "com.sankuai.waimai.bizauth"
//    val start = "2017-11-14 20:30:00"
//    val end = "2017-11-14 22:00:00"
//    val protocolType = "thrift"
//    val role = "server"
//    val dataType = "1"
//    val env = "prod"
//    val unit = "Minute"
//    val group="span"
//    val spanname = "Certification.validateWithWmPoiId"
//    val localhost = "all"
//    val remoteAppkey = "all"
//    val remoteHost = "all"
////    println(HbaseData(appkey,start,end,protocolType,role,dataType,env,unit,group,spanname,localhost,remoteAppkey,remoteHost))
////| 14553 | com.sankuai.waimai.bizauth | server | Certification.validateWithWmPoiId           | compare.counters.$appkey.$sideCount.week | QPS(分钟粒度)同比       | >        | 大于          |        15 |        1 |           0 |
//    val trigger = AppkeyTriggerRow(14553,"com.sankuai.waimai.bizauth","server","Certification.validateWithWmPoiId","compare.counters.$appkey.$sideCount.week","QPS(分钟粒度)同比",">","大于",15,1,0)
//    val start_time = 1510664100 - 300
//    val end_time = 1510664100 + 60
//    val perfKpi = KpiData("com.sankuai.waimai.bizauth","",13290727l,4,3,0,0,0,31643,Some(Point(Some("2017 11-14 20:55, Tue"),Some(3.0),Some(1510664100))),Some(Point(Some("2017 11-14 20:55, Tue"),Some(31333.3),Some(1510664100))))
//    val result = HbaseData(appkey,"2017-11-07 20:45:00".toString,"2017-11-07 20:55:00".toString,protocolType,role,dataType,env,unit,group,spanname,localhost,remoteAppkey,remoteHost)
////    println(result)
//    val monitorData = MonitorData(0, 31643, 0, 0, 0,Some(perfKpi))
//    val qpsList = List(22613.333,22519.367,22157.317,22289.3,22138.5,22559.033,22281.9,22328.517,22289.167,22271.883,22256.633)
//    val rr = if (qpsList.size < 5 || !isValidArray(qpsList)) {
//      true
//    } else {
//      val baseQps = monitorData.qps
//      val meanQps = getMean(qpsList)
//      if (baseQps > meanQps * 0.5) {
//        false
//      } else {
//        val allQps = qpsList :+ baseQps
//        !isValidArray(allQps)
//      }
//    }
//    println(rr)
//  }

  def HbaseData(appkey: String, start: String, end: String, protocolType: String, role: String, dataType: String, env: String,
                unit: String, group: String, spanname: String, localhost: String, remoteAppkey: String, remoteHost: String) = {
    val formatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss")
    val startTime = formatter.parseDateTime(start).getMillis / 1000
    val endTime = formatter.parseDateTime(end).getMillis / 1000
    val urlStr = s"$hostUrl/api/hbase/data"
    val getReq = url(urlStr).GET <<? Map("appkey" -> appkey
      , "start" -> startTime.toString
      , "end" -> endTime.toString
      , "protocolType" -> protocolType
      , "role" -> role
      , "dataType" -> dataType
      , "env" -> env
      , "unit" -> unit
      , "group" -> group
      , "spanname" -> spanname
      , "localhost" -> localhost
      , "remoteAppkey" -> remoteAppkey
      , "remoteHost" -> remoteHost)
    try {
      val future = Http(getReq OK as.String)
      val text = Await.result(future, timeout)
      text
    } catch {
      case e: Exception =>
        logger.error(s"get HbaseData error", e)
        JsonHelper.errorJson("get HbaseData failed")
    }
  }

  case class Point(x: Option[String] = Option(""), y: Option[Double] = Option(0.0), ts: Option[Int] = Option(0))

  implicit val pointReads = Json.reads[Point]
  implicit val pointWrites = Json.writes[Point]

  def validateAll(start: String, end: String, role: String = "client", rate: Double = 0.5f) = {
    val apps = ServiceCommon.apps()
    //val apps = List("mobile.groupdeal", "com.sankuai.inf.logCollector", "com.sankuai.inf.data.statistic")
    val notEvenApps = apps.par.flatMap {
      appkey =>
        val result = validateFlow(appkey, start, end, role, rate)
        Map(appkey -> result)
    }.toBuffer.filter(x => x._2.nonEmpty).toMap
    logger.info(s"notEvenApps = $notEvenApps")
    notEvenApps
  }

  def validateFlow(appkey: String, start: String, end: String, role: String = "client", rate: Double = 0.5f, threhold: Double = 10.0f) = {
    val text = role match {
      case "client" =>
        DataQuery.HbaseData(appkey, start, end, "thrift", "client", null, "prod", "hour", "SpanRemoteHost", "all", null, null, "*")
      case "server" =>
        DataQuery.HbaseData(appkey, start, end, "thrift", "server", null, "prod", "hour", "SpanLocalHost", "all", "*", null, null)
    }
    val data = DataQuery.toDataRecord(text)
    logger.debug(s"$data")
    logger.info(s"${data.get.size}")
    val qpsMap = data.getOrElse(List()).flatMap {
      r =>
        val host = role match {
          case "client" =>
            OpsService.ipToHost(r.tags.remoteHost.get)
          case "server" =>
            OpsService.ipToHost(r.tags.localhost.get)
        }
        Map(host -> r.qps.head.y)
    }.toMap.groupBy(x => x._1.replaceAll("\\d", ""))
    val result = qpsMap.flatMap {
      x =>
        val prefix = x._1
        val values = x._2.values.filter(_.nonEmpty)
        val size = values.size
        val mean = values.map(_.get).sum / size
        if (mean > threhold) {
          Map(prefix -> (Map(s"mean-$size" -> mean) ++ x._2.filter(d => Math.abs(d._2.get - mean) > mean * rate)))
        } else {
          Map(prefix -> Map[String, Double]())
        }
    }.filter(x => x._2.size > 1)
    if (result.nonEmpty) {
      logger.info(s"result = $result")
    }
    result
  }

  case class ResponseTag(protocolType: Option[String], role: Option[String], env: Option[String], group: Option[String], spanname: Option[String], localhost: Option[String], remoteApp: Option[String], remoteHost: Option[String])

  implicit val responseTagRead = Json.reads[ResponseTag]
  implicit val responseTagWrite = Json.writes[ResponseTag]

  /*注: successCount 指的是thrift的successCount, http服务的应为HTTP2XXCount + HTTP3XXCount, 而一个服务总的应该是 http+thrift */
  case class DataRecord(appkey: String, tags: ResponseTag, count: List[Point], successCount: List[Point] = List(), exceptionCount: List[Point] = List(),
                        timeoutCount: List[Point] = List(), dropCount: List[Point] = List(), qps: List[Point],
                        tp50: List[Point], tp90: List[Point], tp99: List[Point], tp999: List[Point], tp9999: List[Point], tp99999: List[Point], tp999999: List[Point],
                        dropQps: List[Point], HTTP2XXCount: List[Point] = List(), HTTP3XXCount: List[Point] = List(), HTTP4XXCount: List[Point] = List(), HTTP5XXCount: List[Point] = List())

  implicit val recordReads = Json.reads[DataRecord]
  implicit val recordWrites = Json.writes[DataRecord]

  def toDataRecord(text: String) = {
    (Json.parse(text) \ "data").asOpt[List[DataRecord]]
  }

  def recordIsSuccess(text: String) = {
    (Json.parse(text) \ "isSuccess").asOpt[Boolean]
  }


  case class MetricsTags(spannames: List[String], localHosts: List[String], remoteAppKeys: List[String], remoteHosts: List[String]) {
    def spannameJavaList = spannameList.asJava

    def localHostJavaList = localHostList.asJava

    def remoteAppJavaList = remoteAppList.asJava

    def remoteAppList = if (remoteAppKeys.nonEmpty) "*" +: remoteAppKeys.sortBy(x => x.toLowerCase) else remoteAppKeys

    def remoteHostJavaList = remoteHostList.asJava

    def toSortedMetrics = {
      MetricsTags(this.spannameList, this.localHostList, this.remoteAppList, this.remoteHostList)
    }

    def spannameList = if (spannames.nonEmpty) "*" +: spannames.sortBy(x => x.toLowerCase) else spannames

    def localHostList = if (localHosts.nonEmpty) "*" +: localHosts.sortBy(x => x.toLowerCase) else localHosts

    def remoteHostList = if (remoteHosts.nonEmpty) "*" +: remoteHosts.sortBy(x => x.toLowerCase) else remoteHosts
  }

  def getAppCallIDCFromDataCenter(appkey: String, role: String) = {
    val now = DateTime.now().minusMinutes(2)
    val start = (now.getMillis / 1000).toInt
    val end = start
    val group = "LocalHostRemoteApp"
    val localhost = "*"
    val remoteApp = "*"
    val dataSource = ""
    val text = DataQuery.getDataRecord(appkey, start, end, null, role, null, Env.prod.toString,
      null, group, null, localhost, remoteApp, null, dataSource)
    val idc_data = text.getOrElse(List())

    val all_text = DataQuery.getDataRecord(appkey, start, end, null, role, null, Env.prod.toString,
      null, group, null, "all", remoteApp, null, dataSource)
    val all_data = all_text.getOrElse(List())

    val data = idc_data ::: all_data
    val dataIDC = groupByIDC(data)
    val idcData = getIDCAppCall(dataIDC)
    idcData
  }

  private def groupByIDC(data: List[DataRecord]) = {
    // 剔除remoteApp为all时重复
    val tmpData = data.filter { x =>
      val remoteApp = x.tags.remoteApp.getOrElse("")
      !"all".equals(remoteApp)
    }
    tmpData.groupBy { x =>
      val ip = x.tags.localhost.getOrElse("")
      if (StringUtils.isEmpty(ip)) {
        "other"
      } else if ("all".equals(ip)) {
        "all"
      } else {
        CommonHelper.ip2IDC(ip)
      }
    }
  }

  private def getIDCAppCall(dataIDC: Map[String, List[DataQuery.DataRecord]]) = {
    //计算机房内调用情况
    dataIDC.map {
      x =>
        val idc = x._1
        val datas = x._2
        val appCalls = datas.groupBy(_.tags.remoteApp.getOrElse("all")).map {
          y =>
            val remoteApp = y._1
            val num = y._2.length
            var count = 0.0
            var qps = 0.0
            var tp50 = 0.0
            var tp90 = 0.0
            var tp99 = 0.0
            y._2.foreach {
              record =>
                count = count + record.count.applyOrElse(0, POINT).y.getOrElse(0D)
                qps = qps + record.qps.applyOrElse(0, POINT).y.getOrElse(0D)
                tp50 = tp50 + record.tp50.applyOrElse(0, POINT).y.getOrElse(0D)
                tp90 = tp90 + record.tp90.applyOrElse(0, POINT).y.getOrElse(0D)
                tp99 = tp99 + record.tp99.applyOrElse(0, POINT).y.getOrElse(0D)
            }
            //数据中心查询接口没有返回tp95数据，故使用tp90代替
            AppCall(remoteApp, count.toLong, threeDecimalFormatter.format(qps / num).toDouble, threeDecimalFormatter.format(tp50 / num).toDouble,
              threeDecimalFormatter.format(tp90 / num).toDouble, threeDecimalFormatter.format(tp90 / num).toDouble, threeDecimalFormatter.format(tp99 / num).toDouble)
        }.toList
        idc -> appCalls
    }
  }

  def getValue(list: List[DataQuery.Point]) = {
    list.head.y.getOrElse(0.0)
  }

  def apiPerf(appkey: String, spanname: String, time: Int) = {
    val appkeys = MsgpConfig.get("waimai.appkeys", "").split(",")
    if (appkeys.contains(appkey)) {
      val dataOpt = getDataRecord(appkey, time, time, null, "server", null, "prod", "minute", "span", spanname, "all", null, null, "hbase")
      val datas = dataOpt.getOrElse(List())
      if (datas.isEmpty) {
        JsonHelper.errorDataJson("can't get performance data!")
      } else {
        val data = datas(0)
        JsonHelper.dataJson(Map("appkey" -> appkey
          , "spanname" -> spanname
          , "tp50" -> getValue(data.tp50)
          , "tp90" -> getValue(data.tp90)
          , "tp99" -> getValue(data.tp99)))
      }
    } else {
      JsonHelper.errorDataJson("not allow this appkey")
    }
  }

  def falconQuery(endpoints: List[String], counters: List[String]) = {
    val urlStr = s"$hostUrl/api/falcon/query"
    val params = endpoints.map { x => ("endpoints", x) } ++ counters.map { x => ("counters", x) }
    val getReq = url(urlStr).GET <<? params
    try {
      val future = Http(getReq OK as.String)
      val result = Await.result(future, timeout)
      Json.parse(result).validate[List[FalconDataResponse]].fold({ error =>
        logger.error(s"FalconLastDataResponse parse failed $error"); None
      }, {
        value => Some(value)
      })
    } catch {
      case e: Exception => logger.error(s"DataQuery falconQuery failed,params:$params", e)
        None
    }
  }

  case class DataRecordMerged(appkey: String, tags: ResponseTag, count: Double, successCount: Double,
                              exceptionCount: Double, timeoutCount: Double, dropCount: Double,
                              qps: Double, tp50: Double, tp90: Double, tp99: Double, tp999: Double, tp9999: Double, tp99999: Double, tp999999: Double,dropQps: Double, failCount: Double, failCountPer: String,
                              HTTP2XXCount: Double, HTTP3XXCount: Double, HTTP4XXCount: Double, HTTP5XXCount: Double)

  //implicit val DataRecordMergedReads = Json.reads[DataRecordMerged]
  //implicit val DataRecordMergedWrites = Json.writes[DataRecordMerged]

  def mergeData(list: List[DataRecord]) = {
    list.map { data =>
      val count = pointsSum(data.count)
      val successCount = pointsSum(data.successCount)
      val exceptionCount = pointsSum(data.exceptionCount)
      val timeoutCount = pointsSum(data.timeoutCount)
      val dropCount = pointsSum(data.dropCount)

      val HTTP2XXCount = pointsSum(data.HTTP2XXCount)
      val HTTP3XXCount = pointsSum(data.HTTP3XXCount)
      val HTTP4XXCount = pointsSum(data.HTTP4XXCount)
      val HTTP5XXCount = pointsSum(data.HTTP5XXCount)

      val qps = pointsAvg(data.qps).toInt
      val tp50 = pointsAvg(data.tp50).toInt
      val tp90 = pointsAvg(data.tp90).toInt
      val tp99 = pointsAvg(data.tp99).toInt
      val tp999 = pointsAvg(data.tp99).toInt
      val tp9999 = pointsAvg(data.tp99).toInt
      val tp99999 = pointsAvg(data.tp99).toInt
      val tp999999 = pointsAvg(data.tp99).toInt
      val dropQps = pointsAvg(data.dropQps).toInt

      val failCount = exceptionCount + timeoutCount + dropCount + HTTP4XXCount + HTTP5XXCount
      val failCountPer = if (count != 0) {
        s"${fourDecimalFormatter.format(failCount.toDouble / count * 100)}%"
      } else {
        "0.0000%"
      }

      DataRecordMerged(data.appkey, data.tags, count, successCount, exceptionCount,
        timeoutCount, dropCount, qps, tp50, tp90, tp99, tp999, tp9999, tp99999, tp999999, dropQps, failCount, failCountPer,
        HTTP2XXCount, HTTP3XXCount, HTTP4XXCount, HTTP5XXCount)
    }
  }

  def pointsSum(points: List[Point]) = {
    points.flatMap(_.y).sum
  }

  def pointsAvg(points: List[Point]) = {
    val tmp = points.flatMap(_.y)
    if (tmp.isEmpty) {
      0.0
    } else {
      threeDecimalFormatter.format(tmp.sum / tmp.length).toDouble
    }
  }
}
