package com.sankuai.octo.msgp.serivce.data

import java.util.Date
import java.util.concurrent.TimeUnit

import com.sankuai.msgp.common.utils.StringUtil
import com.sankuai.msgp.common.utils.helper.{CommonHelper, JsonHelper}
import com.sankuai.octo.msgp.domain.{AppsKpiReq, KpiReq}
import com.sankuai.octo.mworth.util.DateTimeUtil
import com.sankuai.octo.statistic.constant.Constants
import org.joda.time.{DateTime, DateTimeZone}
import org.slf4j.LoggerFactory

import scala.collection.JavaConverters._
import scala.concurrent.duration.Duration

/**
  * Created by yves on 16/9/19.
  *
  * 提供对外的性能数据查询接口
  */
object PublicQuery {
  private implicit val timeout = Duration.create(30L, TimeUnit.SECONDS)

  private val logger = LoggerFactory.getLogger(this.getClass)

  private val percentFormatStr = "%.4f"

  case class HostCount(appkey: String, localHost: String, count: Double)

  case class DestinationData(remoteAppkey: String, spanname: String, callCount: List[Double], successCount: List[Double], failPercent: List[String], qPS: List[Double], timePoint: List[String],
                             tp50: List[Double], tp90: List[Double], tp99: List[Double])

  case class AppnameKpi(appkey: String, spanname: String, count: List[Double],
                        exceptionCount: List[Double],
                        qps: List[Double], tp50: List[Double], tp90: List[Double], tp99: List[Double], time: List[String])

  def getHostCount(appkey: String, env: String = "prod", ts: Long) = {
    val role = "server"
    val group = "spanLocalHost"
    val spanname = "all"
    val localhost = "*"
    val remoteAppkey = "all"
    val remoteHost = "all"
    val unitHour = "Hour"
    val protocol = "thrift"
    val dataSource = "hbase"

    val startOfDate = new DateTime(ts, DateTimeZone.forID("Asia/Shanghai")).withTimeAtStartOfDay()
    val start = (startOfDate.getMillis / 1000).toInt
    val end = (startOfDate.plusDays(1).getMillis / 1000).toInt

    val recordOpt = DataQuery.getDataRecord(appkey, start, end, protocol, role, null, env, unitHour, group, spanname, localhost,
      remoteAppkey, remoteHost, dataSource)

    val result = if (recordOpt.nonEmpty) {
      recordOpt.get.map { record =>
        val localHost = record.tags.localhost.getOrElse("")
        val count = record.count.map(_.y.getOrElse(0D)).sum
        HostCount(appkey, localHost, count)
      }
    } else {
      List[HostCount]()
    }
    result
  }

  def getDestinationData(kpiReq: KpiReq) = {
    val appkey = kpiReq.getAppkey
    val env = kpiReq.getEnv
    val start = kpiReq.getStart
    val end = kpiReq.getEnd
    val remoteAppkey = kpiReq.getRemoteAppkey
    val spanname = kpiReq.getSpanname
    val role = "client"
    val group = "SpanRemoteApp"
    val localhost = "all"
    val remoteHost = "all"
    val unitHour = "Hour"
    val unitMinute = "Minute"
    val protocol = "thrift"
    val dataSource = "hbase"

    val startTime = (start.getTime / 1000).toInt
    val endTime = (end.getTime / 1000).toInt

    val unit = if ((endTime - startTime) >= Constants.ONE_DAY_SECONDS) unitHour else unitMinute

    val recordOpt = DataQuery.getDataRecord(appkey, startTime, endTime, protocol, role, null, env, unit, group, spanname, localhost,
      remoteAppkey, remoteHost, dataSource)

    val dest = if (recordOpt.isDefined) {
      recordOpt.get.map { record =>
        val callCount = record.count.map(_.y.getOrElse(0D))
        val successCount = record.successCount.map(_.y.getOrElse(0D))
        val failPercent = (callCount zip successCount).map {
          case (total, success) =>
            if (total != 0) {
              s"${percentFormatStr.format((total - success) / total * 100)}%"
            } else {
              "0.0000%"
            }
        }
        val timePoint = record.count.map(_.x.getOrElse(""))
        val qPS = record.qps.map(_.y.getOrElse(0D))
        val tp50 = record.tp90.map(_.y.getOrElse(0D))
        val tp90 = record.tp90.map(_.y.getOrElse(0D))
        val tp99 = record.tp99.map(_.y.getOrElse(0D))

        DestinationData(record.tags.remoteApp.getOrElse(""), record.tags.spanname.getOrElse(""), callCount, successCount, failPercent, qPS, timePoint, tp50, tp90, tp99)
      }
    } else {
      List[DestinationData]()
    }
    dest
  }

  def appskpi(appsKpiReq: AppsKpiReq) = {
    val start = appsKpiReq.getStart
    val end = appsKpiReq.getEnd

    val unit = if (end.getTime - start.getTime > 86400000) {
      "hour"
    } else {
      "minute"
    }
    val int_start = (start.getTime / 1000).toInt
    val int_end = (end.getTime / 1000).toInt
    val data = appsKpiReq.getSpanList.asScala.map {
      appspan =>
        val dataRecordsOpt = DataQuery.getDataRecord(appspan.getAppkey, int_start, int_end,
          "", "server", "", "prod", unit, "spanLocalhost", appspan.getSpanname, "all", "all", "all", "hbase")
        val dataRecords = dataRecordsOpt.getOrElse(List())
        dataRecords.flatMap {
          dataRecord =>
            val count = dataRecord.count.map(_.y.getOrElse(0.0))
            val exceptionCount = dataRecord.exceptionCount.map(_.y.getOrElse(0.0))
            val qps = dataRecord.qps.map(_.y.getOrElse(0.0))
            val tp50 = dataRecord.tp50.map(_.y.getOrElse(0.0))
            val tp90 = dataRecord.tp90.map(_.y.getOrElse(0.0))
            val tp99 = dataRecord.tp99.map(_.y.getOrElse(0.0))
            val time = dataRecord.count.map {
              x =>
                val ts = x.ts
                ts match {
                  case Some(second) =>
                    DateTimeUtil.format(new Date(second * 1000L), DateTimeUtil.DATE_TIME_FORMAT)
                  case None =>
                    ""
                }
            }
            Some(AppnameKpi(dataRecord.appkey, dataRecord.tags.spanname.getOrElse("all"), count, exceptionCount, qps, tp50, tp90, tp99, time))
        }
    }
    data
  }

  /**
    * 提供给Hulk的接口
    *
    * @param appkey
    * @param spanname
    * @param env
    * @param start 单位为秒
    * @param end
    * @return
    */
  def getQPS(appkey: String, spanname: String, env: String, start: Long, end: Long, idc: java.util.List[String]) = {
    val role = "server"
    val group = "span"
    val localhost = "*"
    val remoteAppkey = "all"
    val remoteHost = "all"
    val protocol = "thrift"
    val dataSource = "hbase"
    val _spanname = if (StringUtil.isBlank(spanname)) "all" else spanname

    val idcSet = idc.asScala.map(_.toUpperCase).toSet

    if (_spanname.equalsIgnoreCase("*")) {
      -1.0
    } else {
      val result = DataQuery.getDataRecord(appkey, start.toInt, end.toInt, protocol, role, null, env, null, group, _spanname, localhost,
        remoteAppkey, remoteHost, dataSource)
      if (result.isDefined) {
        val records = result.get
        records.flatMap { item =>
          val qps = item.qps
          if (qps.nonEmpty) {
            val ip = item.tags.localhost.getOrElse("unknownHost")
            // 过滤相应机房的机器
            if (idcSet.isEmpty || idcSet.contains(CommonHelper.ip2IDC(ip))) {
              Some(Map("host" -> ip, "qps" -> qps))
            } else {
              logger.info(s"ip: $ip, idc: ${CommonHelper.ip2IDC(ip)}")
              None
            }
          } else {
            None
          }
        }
      } else {
        //返回错误
        List()
      }
    }
  }

  def getAverageQps(appkey: String, spanname: String, env: String, ts: Long, days: Int = 1) = {
    val role = "server"
    val group = "spanLocalhost"
    val localhost = "*"
    val remoteAppkey = "all"
    val remoteHost = "all"
    val unitMinute = "Minute"
    val protocol = "thrift"
    val dataSource = "hbase"
    val _spanname = if (StringUtil.isBlank(spanname)) "all" else spanname

    if (_spanname.equalsIgnoreCase("*")) {
      -1.0
    } else {
      val start = ts - days * DateTimeUtil.DAY_TIME / 1000l
      val end = start + 31

      val resultOpt = DataQuery.getDataRecord(appkey, start.toInt, end.toInt, protocol, role, null, env, unitMinute, group, _spanname, localhost,
        remoteAppkey, remoteHost, dataSource)
      val averageQps = if (resultOpt.isDefined) {
        val records = resultOpt.getOrElse(List())
        val qpsList = if (records.nonEmpty) {
          val recordsExcludedAll = records.filter {
            record =>
              record.tags.spanname.get.equalsIgnoreCase(_spanname) &&
                record.tags.localhost.nonEmpty &&
                !record.tags.localhost.get.equalsIgnoreCase("all") &&
                !record.tags.localhost.get.equalsIgnoreCase("external") &&
                record.qps.nonEmpty
          }
          if (recordsExcludedAll.nonEmpty) {
            recordsExcludedAll.map {
              record =>
                record.qps.head
            }
          } else {
            List()
          }
        } else {
          List()
        }
        if (qpsList.nonEmpty) {
          qpsList.map { qps => qps.y.get }.sum / qpsList.size
        } else {
          0.0
        }
      } else {
        //返回错误
        -1.0
      }
      averageQps
    }
  }


  case class DailyPerformance(appkey: String = "", countServer: Long = 0L, countClient: Long = 0L, successRatio: String = "", qps: Double = 0)

  /**
    * 获取当天服务简单的调用数据
    * QA团队使用
    *
    * @param appkey 服务名
    * @param env    环境
    * @param ts     时间戳, 数据为时间戳下当天开始到当天结束
    * @param source
    * @return
    */
  def getDailyData(appkey: String, env: String, ts: Long, source: String = "server") = {
    val startOfDate = new DateTime(ts * 1000, DateTimeZone.forID("Asia/Shanghai")).withTimeAtStartOfDay()
    try {
      val result = DataQuery.getDailyPerformance(appkey, Constants.ALL, env, startOfDate)
      if (result.nonEmpty) {
        val record = result.head
        JsonHelper.dataJson(Some(DailyPerformance(appkey, record.count, record.countClient, record.successCountClientPer, record.qps))
        )
      } else {
        JsonHelper.dataJson("performance data is empty.")
      }
    } catch {
      case e: Exception =>
        logger.error(s"parse dailyKpi failed", e)
        JsonHelper.errorJson("get performance data failed.")
    }
  }
}
