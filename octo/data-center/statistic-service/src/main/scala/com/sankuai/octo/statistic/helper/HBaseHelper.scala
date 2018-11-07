package com.sankuai.octo.statistic.helper

import java.nio.charset.StandardCharsets._
import java.util.concurrent.TimeUnit

import com.google.common.cache.{CacheBuilder, CacheLoader}
import com.sankuai.octo.statistic.constant.Constants
import com.sankuai.octo.statistic.model._
import org.apache.commons.codec.digest.DigestUtils
import org.apache.commons.lang3.ArrayUtils
import org.apache.hadoop.hbase.filter.{CompareFilter, RegexStringComparator, RowFilter}
import org.apache.hadoop.hbase.util.Bytes
import org.joda.time.{DateTime, DateTimeZone}

/**
 * Created by wujinwu on 16/2/26.
 */
object HBaseHelper {

  // hbase表名的映射关系
  private val TABLE_NAME_MAP = Map(
    PerfRole.SERVER ->
      Map(StatRange.Day -> "octolog_serv_perf_day_index",
        StatRange.Hour -> "octolog.serv.perf.hour.index",
        StatRange.Minute -> "octolog.serv.perf.min.index"),
    PerfRole.CLIENT ->
      Map(StatRange.Day -> "octolog_cli_perf_day_index",
        StatRange.Hour -> "octolog.cli.perf.hour.index",
        StatRange.Minute -> "octolog.cli.perf.min.index"))

  //  HBase常量统一放置
  val COLUMN_FAMILY = "D".getBytes(UTF_8)

  val COUNT_COLUMN = "count".getBytes(UTF_8)

  val SUCCESS_COUNT_COLUMN = "success_count".getBytes(UTF_8)

  val EXCEPTION_COUNT_COLUMN = "exception_count".getBytes(UTF_8)

  val TIMEOUT_COUNT_COLUMN = "timeout_count".getBytes(UTF_8)

  val DROP_COUNT_COLUMN = "drop_count".getBytes(UTF_8)

  val HTTP_2XX_COUNT_COLUMN = "http2xx_count".getBytes(UTF_8)

  val HTTP_3XX_COUNT_COLUMN = "http3xx_count".getBytes(UTF_8)

  val HTTP_4XX_COUNT_COLUMN = "http4xx_count".getBytes(UTF_8)

  val HTTP_5XX_COUNT_COLUMN = "http5xx_count".getBytes(UTF_8)

  val QPS_COLUMN = "qps".getBytes(UTF_8)

  val TP50_COLUMN = "tp50".getBytes(UTF_8)

  val TP90_COLUMN = "tp90".getBytes(UTF_8)

  val TP95_COLUMN = "tp95".getBytes(UTF_8)

  val TP99_COLUMN = "tp99".getBytes(UTF_8)

  val COST_MAX_COLUMN = "costMax".getBytes(UTF_8)

  val COST_MAX_COLUMN_NEW = "cost_max".getBytes(UTF_8)

  val COST_DATA_COLUMN = "costData".getBytes(UTF_8)

  val COST_DATA_COLUMN_NEW = "cost_data".getBytes(UTF_8)

  val INFRA_NAME_COLUMN = "infra_name".getBytes(UTF_8)

  private val rowFilters = CacheBuilder.newBuilder().maximumSize(2000).expireAfterAccess(10l, TimeUnit.MINUTES).build(
    new CacheLoader[String, RowFilter]() {
      override def load(key: String): RowFilter = {
        new RowFilter(CompareFilter.CompareOp.EQUAL, new RegexStringComparator(key))
      }
    })


  def getTableName(role: PerfRole, statRange: StatRange): String = {
    TABLE_NAME_MAP(role)(statRange)
  }

  def generateRowKey(perfData: PerfData): Array[Byte] = {
    val spanName = Option(perfData.getTags.spanname)
    val localHost = Option(perfData.getTags.localHost)
    val remoteAppKey = Option(perfData.getTags.remoteAppKey)
    val remoteHost = Option(perfData.getTags.remoteHost)

    generateRowKey(perfData.getAppkey, perfData.getPerfProtocolType, perfData.getDataType,
      perfData.getEnv, perfData.getGroup, perfData.getTs, spanName, localHost, remoteAppKey, remoteHost)
  }

  def generateRowKey(appKey: String, protocolType: PerfProtocolType, dataType: PerfDataType,
                     statEnv: StatEnv, statGroup: StatGroup, timestamp: Int,
                     spanName: Option[String] = None, localHost: Option[String] = None,
                     remoteAppKey: Option[String] = None, remoteHost: Option[String] = None): Array[Byte] = {
    val dateTime = new DateTime(timestamp * 1000L, DateTimeZone.forID("Asia/Shanghai"))
    val month = dateTime.toString("yyyyMM")
    val prefix = protocolType.getType.toString + statEnv + appKey + dataType.getType + statGroup.getType

    val suffix = statGroup match {
      case StatGroup.SpanLocalHost => s"${spanName.get}|${localHost.get}"
      case StatGroup.SpanRemoteApp => s"${spanName.get}|${remoteAppKey.get}"
      case StatGroup.Span => s"${spanName.get}"
      case StatGroup.SpanRemoteHost => s"${spanName.get}|${remoteHost.get}"
      case StatGroup.LocalHostRemoteHost => s"${localHost.get}|${remoteHost.get}"
      case StatGroup.LocalHostRemoteApp => s"${localHost.get}|${remoteAppKey.get}"
      case StatGroup.RemoteAppRemoteHost => s"${remoteAppKey.get}|${remoteHost.get}"
    }
    val rowkeyStr = s"${DigestUtils.md5Hex(prefix + month)}|${protocolType.getType}|$statEnv|$appKey|${dataType.getType}|${statGroup.getType}" + "|" +
      s"$timestamp|$suffix"
    rowkeyStr.getBytes(UTF_8)
  }


  def generateRowKeyPrefix(appKey: String, protocolType: PerfProtocolType, dataType: PerfDataType,
                           statEnv: StatEnv, statGroup: StatGroup, timestamp: Int): Array[Byte] = {
    val dateTime = new DateTime(timestamp * 1000L)
    val month = dateTime.toString("yyyyMM")
    val prefix = protocolType.getType.toString + statEnv + appKey + dataType.getType + statGroup.getType
    val rowkeyPrefixStr = s"${DigestUtils.md5Hex(prefix + month)}|${protocolType.getType}|$statEnv|$appKey|${dataType.getType}|${statGroup.getType}" + "|" +
      s"$timestamp"
    rowkeyPrefixStr.getBytes(UTF_8)
  }

  def generateRowKeyPrefixWithoutTs(appKey: String, protocolType: PerfProtocolType, dataType: PerfDataType,
                                    statEnv: StatEnv, statGroup: StatGroup, month: String): Array[Byte] = {
    val prefix = protocolType.getType.toString + statEnv + appKey + dataType.getType + statGroup.getType
    val rowkeyPrefixStr = s"${DigestUtils.md5Hex(prefix + month)}|${protocolType.getType}|$statEnv|$appKey|${dataType.getType}|${statGroup.getType}"
    rowkeyPrefixStr.getBytes(UTF_8)
  }


  /**
   * helper method,适配原生的Double转换为8字节数组 or hive ETL导出的按照String转换的double;
   *
   * @param bytes 字节流
   */
  def parseDouble(bytes: Array[Byte]) = {
    if (ArrayUtils.isEmpty(bytes)) {
      0D
    } else {
      try {
        new String(bytes, UTF_8).toDouble
      } catch {
        case e: NumberFormatException =>
          try {
            Bytes.toDouble(bytes)
          } catch {
            case _: Throwable => 0D
          }
        case _: Throwable => 0D
      }
    }

  }

  /**
   * helper method,适配原生的Long 转换为8字节数组 or hive ETL导出的按照String转换的Long;
   *
   * @param bytes 字节流
   */
  def parseLong(bytes: Array[Byte]) = {
    if (ArrayUtils.isEmpty(bytes)) {
      0L
    } else {
      try {
        new String(bytes, UTF_8).toLong
      } catch {
        case e: NumberFormatException =>
          try {
            Bytes.toLong(bytes)
          } catch {
            case _: Throwable => 0L
          }
        case _: Throwable => 0L
      }
    }

  }

  def parseString(bytes: Array[Byte]) = {
    if (ArrayUtils.isEmpty(bytes)) {
      ""
    } else {
      try {
        new String(bytes, UTF_8)
      } catch {
        case e: NumberFormatException =>
          try {
            Bytes.toString(bytes)
          } catch {
            case _: Throwable => ""
          }
        case _: Throwable => ""

      }
    }

  }

  /**
   *
   * @param timeRange 可能跨月份的时间范围
   * @return 拆分成每一个月的TimeRange集合
   */
  def splitTimeRange(timeRange: TimeRange, statRange: StatRange): Seq[TimeRange] = {
    if (timeRange.start == timeRange.end) {
      //  同一天
      Seq(timeRange)
    } else {
      val startMonth = TimeProcessor.getDateTimeFormatStr(timeRange.start, "yyyyMM")
      val endMonth = TimeProcessor.getDateTimeFormatStr(timeRange.end, "yyyyMM")
      if (startMonth == endMonth) {
        //  处于同一个月份,不需要拆分
        Seq(timeRange)
      } else {
        //  递归拆分,根据StatRange拆分
        val thisMonthLastDay = TimeProcessor.getMonthLastDay(timeRange.start)
        statRange match {
          case StatRange.Day =>
            val thisMonthEndTs = (thisMonthLastDay.getMillis / 1000L).toInt
            val newStart = thisMonthEndTs + Constants.ONE_DAY_SECONDS
            Seq(TimeRange(timeRange.start, thisMonthEndTs)) ++: splitTimeRange(TimeRange(newStart, timeRange.end), StatRange.Day)

          case StatRange.Hour =>
            val endHourTs = (TimeProcessor.getDayLastHour(thisMonthLastDay).getMillis / 1000L).toInt
            val newStart = endHourTs + Constants.ONE_HOUR_SECONDS
            Seq(TimeRange(timeRange.start, endHourTs)) ++: splitTimeRange(TimeRange(newStart, timeRange.end), StatRange.Hour)

          case StatRange.Minute =>
            val endMinuteTs = (TimeProcessor.getDayLastMinute(thisMonthLastDay).getMillis / 1000L).toInt
            val newStart = endMinuteTs + Constants.ONE_MINUTE_SECONDS
            Seq(TimeRange(timeRange.start, endMinuteTs)) ++: splitTimeRange(TimeRange(newStart, timeRange.end), StatRange.Minute)
        }

      }
    }

  }

  def getRowFilter(regex: String) = {
    rowFilters.getUnchecked(regex)
  }

}
