package com.sankuai.octo.statistic.util

import java.nio.charset.StandardCharsets.UTF_8
import java.util.concurrent.atomic.AtomicInteger

import com.meituan.service.hbase.impl.{MTAsyncHBaseClient, StatisticalFutureCallback}
import com.sankuai.octo.statistic.constant.Constants
import com.sankuai.octo.statistic.helper.HBaseHelper._
import com.sankuai.octo.statistic.helper.{HBaseHelper, Serializer, TimeProcessor, TimeRange}
import com.sankuai.octo.statistic.model._
import org.apache.hadoop.hbase.client._
import org.apache.hadoop.hbase.util.Bytes
import org.slf4j.LoggerFactory
import org.springframework.util.CollectionUtils

import scala.collection.JavaConversions._
import scala.collection.JavaConverters._
import scala.collection.mutable.ArrayBuffer

object HBaseClient {
  private val logger = LoggerFactory.getLogger(this.getClass)
  private val client = new MTAsyncHBaseClient

  //  仅仅用于占位,使用到异步的读写
  private def dummyCallback(size:Int,start:Long) = try {
    new StatisticalFutureCallback[Void] {
      override def onFailureImpl(t: Throwable): Unit = logger.error(s"hbase client failure,put size:$size, put cost ${System.currentTimeMillis() - start} ms", t)

      override def onSuccessImpl(result: Void): Unit = ()
    }
  } catch {
    case e: Exception =>
      logger.error("dummyCallback init fail", e)
      throw new RuntimeException("dummyCallback init fail")
  }

  private var readCounterMap = constructCounterMap()
  private var writeCounterMap = constructCounterMap()

  private val TIMEOUT_MS = 5000L

  case class HBaseQueryResult(timestamp: Int, count: Long, successCount: Long, exceptionCount: Long, timeoutCount: Long, dropCount: Long,
                              HTTP2XXCount: Long, HTTP3XXCount: Long, HTTP4XXCount: Long, HTTP5XXCount: Long,
                              qps: Double, tp50: Double, tp90: Double, tp99: Double, costMax: Double, responseTag: ResponseTag, infraName: String)

  case class ResponseTag(spanname: String, localhost: String, remoteAppkey: String, remoteHost: String)

  /**
   * 根据查询的StatGroup,构造scan,查询参数中至少有一个"*"
   * 如StatGroup.SpanLocalHost  ,spanName 与 localHost至少一个为"*"
   *
   * @param appKey       appKey
   * @param protocolType 协议
   * @param dataType     数据类型
   * @param statEnv      环境
   * @param statGroup    查询维度
   * @param timestamps   时间戳,(start,end),start ,end必须要处于同一个month内,由调用方保证
   * @param spanName     方法名
   * @param localHost    本地 主机
   * @param remoteAppKey 远程appKey
   * @param remoteHost   远程主机
   * @return Scan对象
   */
  def generateTimeRangeHBaseScan(appKey: String, protocolType: PerfProtocolType, dataType: PerfDataType,
                                 statEnv: StatEnv, statGroup: StatGroup, timestamps: TimeRange,
                                 spanName: Option[String] = None, localHost: Option[String] = None,
                                 remoteAppKey: Option[String] = None, remoteHost: Option[String] = None): Scan = {

    //  scan start point & end point
    val startRowKeyPrefix = HBaseHelper.generateRowKeyPrefix(appKey, protocolType, dataType, statEnv, statGroup, timestamps.start)

    val stopRowKeyPrefix = HBaseHelper.generateRowKeyPrefix(appKey, protocolType, dataType, statEnv, statGroup, timestamps.end + 1)

    val month = TimeProcessor.getDateTimeFormatStr(timestamps.start, "yyyyMM")
    //  filter prefix without timestamp
    val filterPrefix = HBaseHelper.generateRowKeyPrefixWithoutTs(appKey, protocolType, dataType, statEnv, statGroup, month)
    val scan = new Scan()
    scan.addColumn(COLUMN_FAMILY, COUNT_COLUMN)
    // TODO: 可用性指标可能错误
    scan.addColumn(COLUMN_FAMILY, SUCCESS_COUNT_COLUMN)
    scan.addColumn(COLUMN_FAMILY, EXCEPTION_COUNT_COLUMN)
    scan.addColumn(COLUMN_FAMILY, TIMEOUT_COUNT_COLUMN)
    scan.addColumn(COLUMN_FAMILY, DROP_COUNT_COLUMN)

    scan.addColumn(COLUMN_FAMILY, HTTP_2XX_COUNT_COLUMN)
    scan.addColumn(COLUMN_FAMILY, HTTP_3XX_COUNT_COLUMN)
    scan.addColumn(COLUMN_FAMILY, HTTP_4XX_COUNT_COLUMN)
    scan.addColumn(COLUMN_FAMILY, HTTP_5XX_COUNT_COLUMN)


    scan.addColumn(COLUMN_FAMILY, QPS_COLUMN)
    scan.addColumn(COLUMN_FAMILY, TP50_COLUMN)
    scan.addColumn(COLUMN_FAMILY, TP90_COLUMN)
    scan.addColumn(COLUMN_FAMILY, TP95_COLUMN)
    scan.addColumn(COLUMN_FAMILY, TP99_COLUMN)
    scan.addColumn(COLUMN_FAMILY, COST_MAX_COLUMN_NEW)
    scan.addColumn(COLUMN_FAMILY, COST_MAX_COLUMN)
    scan.addColumn(COLUMN_FAMILY, INFRA_NAME_COLUMN)

    //  根据条件计算filter
    val filter = statGroup match {
      case StatGroup.SpanLocalHost => constructTimeRangeFilter(filterPrefix, spanName, localHost, StatGroup.SpanLocalHost)
      case StatGroup.SpanRemoteApp => constructTimeRangeFilter(filterPrefix, spanName, remoteAppKey, StatGroup.SpanRemoteApp)
      case StatGroup.Span => constructTimeRangeFilter(filterPrefix, spanName, None, StatGroup.Span)
      case StatGroup.SpanRemoteHost => constructTimeRangeFilter(filterPrefix, spanName, remoteHost, StatGroup.SpanRemoteHost)
      case StatGroup.LocalHostRemoteHost => constructTimeRangeFilter(filterPrefix, localHost, remoteHost, StatGroup.LocalHostRemoteHost)
      case StatGroup.LocalHostRemoteApp => constructTimeRangeFilter(filterPrefix, localHost, remoteAppKey, StatGroup.LocalHostRemoteApp)
      case StatGroup.RemoteAppRemoteHost => constructTimeRangeFilter(filterPrefix, remoteAppKey, remoteHost, StatGroup.RemoteAppRemoteHost)
    }
    scan.setStartRow(startRowKeyPrefix)

    scan.setStopRow(stopRowKeyPrefix)

    scan.setFilter(filter)

    scan

  }


  private def constructTimeRangeFilter(rowKeyPrefix: Array[Byte], left: Option[String], right: Option[String], statGroup: StatGroup) = {
    val rowKeyPrefixStr = new String(rowKeyPrefix, UTF_8).replaceAll("\\|", "\\\\|").replaceAll("\\.", "\\\\.")
    val prefix = s"$rowKeyPrefixStr\\|.+\\|"
    statGroup match {
      //  对只有一个参数的特殊处理
      case StatGroup.Span =>
        if (left.get == "*") {
          val regex = s"$prefix.+"
          val rowFilter = HBaseHelper.getRowFilter(regex)
          rowFilter
        } else {
          //  span name为具体值
          val suffix = left.get.replaceAll("\\|", "\\\\|").replaceAll("\\.", "\\\\.")
          val regex = s"$prefix$suffix$$"
          val rowFilter = HBaseHelper.getRowFilter(regex)
          rowFilter
        }
      case _ =>
        //  多个参数
        if (left.get == "*" && right.get == "*") {
          val regex = s"$prefix.+\\|.+"
          val rowFilter = HBaseHelper.getRowFilter(regex)
          rowFilter
        } else if (left.get == "*") {
          //  右边为具体值
          val rightStr = right.get.replaceAll("\\|", "\\\\|").replaceAll("\\.", "\\\\.")
          val regex = s"$prefix.+\\|$rightStr"
          val rowFilter = HBaseHelper.getRowFilter(regex)
          rowFilter
        } else if (right.get == "*") {
          //  right 为"*",左边为具体值
          val leftStr = left.get.replaceAll("\\|", "\\\\|").replaceAll("\\.", "\\\\.")
          val regex = s"$prefix$leftStr\\|.+"
          val rowFilter = HBaseHelper.getRowFilter(regex)
          rowFilter
        } else {
          //  左右均为具体值
          val leftStr = left.get.replaceAll("\\|", "\\\\|").replaceAll("\\.", "\\\\.")
          val rightStr = right.get.replaceAll("\\|", "\\\\|").replaceAll("\\.", "\\\\.")
          val regex = s"$prefix$leftStr\\|$rightStr$$"
          val rowFilter = HBaseHelper.getRowFilter(regex)
          rowFilter
        }
    }
  }

  /**
   *
   * @param perfDataList 需要写入的 perf Data列表
   * @param statRange    分钟,小时,天的维度,statDataList必须处于同一时间维度
   */
  def putPerfDataList(perfDataList: Iterable[PerfData], statRange: StatRange = StatRange.Minute): Unit = {
    if (perfDataList.size != 0) {
      //  划分出不同端的集合
      val (serverList, clientList) = perfDataList.partition(_.getRole == PerfRole.SERVER)

      if (serverList.size != 0) {
        val tableName = HBaseHelper.getTableName(PerfRole.SERVER, statRange)
        putPerfList(serverList, tableName)
      }
      if (clientList.size != 0) {
        val tableName = HBaseHelper.getTableName(PerfRole.CLIENT, statRange)
        putPerfList(clientList, tableName)
      }
    }
  }

  /**
   *
   * @param scan      scan请求
   * @param role      用于构造表名
   * @param statRange 用于构造表名
   * @param statGroup 用于计数
   * @return
   */
  def scan(scan: Scan, role: PerfRole, statRange: StatRange, statGroup: StatGroup): Seq[HBaseQueryResult] = {
    def traceReadCount(): Unit = {
      val counter = readCounterMap(statRange)(statGroup)
      counter.incrementAndGet()
    }
    try {
      traceReadCount()
      val tableName = HBaseHelper.getTableName(role, statRange)
      logger.debug("tableName:{}", tableName)
      val scanner = client.getScanner(tableName, scan)
      val iterator = scanner.iterator()
      val array = ArrayBuffer[HBaseQueryResult]()
      while (iterator.hasNext) {
        val result = iterator.next()
        try {
          array += queryResultTransform(result)
        } catch {
          case e: Exception => logger.error(s"queryResultTransform fail,result:$result", e)
        }
      }
      array
    } catch {
      case e: Exception =>
        logger.error(s"scan Fail,param:$scan", e)
        Seq()
    }
  }

  private def putPerfList(dataList: Iterable[PerfData], tableName: String): Unit = {
    if (dataList.size != 0) {
      traceWriteCount(dataList)
      //  根据相应端取得相应表名
      val putList = dataList.map(perfToHBasePut).toList
      val start = System.currentTimeMillis()
      //  指定callback不为空,使用了HBaseClient的Async特性
      try {
        client.put(tableName, putList, TIMEOUT_MS, dummyCallback(putList.size,System.currentTimeMillis()))
        } catch {
        case e: Exception => logger.error("putPerfList fail", e)
      }
      logger.debug(s"put cost ${System.currentTimeMillis() - start} ms,length:${putList.size}")
    }

  }

  private def traceWriteCount(dataList: Iterable[PerfData]): Unit = {
    dataList.foreach(data => {
      val counter = writeCounterMap(data.getRange)(data.getGroup)
      counter.incrementAndGet()
    })
  }

  private def perfToHBasePut(perfData: PerfData): Put = {
    val rowKey = HBaseHelper.generateRowKey(perfData)
    val put = new Put(rowKey)

    /**
     * Do not write the Mutation to the WAL
     */
    put.setDurability(Durability.SKIP_WAL)

    put.add(COLUMN_FAMILY, COUNT_COLUMN, Bytes.toBytes(perfData.getCount))
    put.add(COLUMN_FAMILY, SUCCESS_COUNT_COLUMN, Bytes.toBytes(perfData.getSuccessCount))
    put.add(COLUMN_FAMILY, EXCEPTION_COUNT_COLUMN, Bytes.toBytes(perfData.getExceptionCount))
    put.add(COLUMN_FAMILY, TIMEOUT_COUNT_COLUMN, Bytes.toBytes(perfData.getTimeoutCount))
    put.add(COLUMN_FAMILY, DROP_COUNT_COLUMN, Bytes.toBytes(perfData.getDropCount))

    put.add(COLUMN_FAMILY, HTTP_2XX_COUNT_COLUMN, Bytes.toBytes(perfData.getHTTP2XXCount))
    put.add(COLUMN_FAMILY, HTTP_3XX_COUNT_COLUMN, Bytes.toBytes(perfData.getHTTP3XXCount))
    put.add(COLUMN_FAMILY, HTTP_4XX_COUNT_COLUMN, Bytes.toBytes(perfData.getHTTP4XXCount))
    put.add(COLUMN_FAMILY, HTTP_5XX_COUNT_COLUMN, Bytes.toBytes(perfData.getHTTP5XXCount))

    put.add(COLUMN_FAMILY, QPS_COLUMN, Bytes.toBytes(perfData.getQps))
    put.add(COLUMN_FAMILY, TP50_COLUMN, Bytes.toBytes(perfData.getCost50))
    put.add(COLUMN_FAMILY, TP90_COLUMN, Bytes.toBytes(perfData.getCost90))
    put.add(COLUMN_FAMILY, TP95_COLUMN, Bytes.toBytes(perfData.getCost95))
    put.add(COLUMN_FAMILY, TP99_COLUMN, Bytes.toBytes(perfData.getCost99))
    put.add(COLUMN_FAMILY, COST_MAX_COLUMN_NEW, Bytes.toBytes(perfData.getCostMax))

    put.add(COLUMN_FAMILY, INFRA_NAME_COLUMN, Bytes.toBytes(perfData.getTags.infraName))

    if (perfData.getCostData != null && !CollectionUtils.isEmpty(perfData.getCostData.getCostDataList)) {
      put.add(COLUMN_FAMILY, COST_DATA_COLUMN_NEW, Serializer.toBytes(perfData.getCostData))
    }
    put
  }

  private def queryResultTransform(result: Result): HBaseQueryResult = {
    val rowKeyBytes = result.getRow
    val rowKey = new String(rowKeyBytes, UTF_8)
    val strArray = rowKey.split("\\|")
    // 这些字段暂时不需要
    //    val protocol = PerfProtocolType.getInstance(strArray(1).toInt)
    //    val env = StatEnv.getInstance(strArray(2))
    //    val appKey = strArray(3)
    //    val dataType = PerfDataType.getInstance(strArray(4).toInt)
    val group = StatGroup.getInstance(strArray(5).toInt)
    val ts = strArray(6).toInt

    val responseTag = group match {
      case StatGroup.SpanLocalHost =>
        ResponseTag(strArray(7), strArray(8), Constants.ALL, Constants.ALL)
      case StatGroup.SpanRemoteApp =>
        ResponseTag(strArray(7), Constants.ALL, strArray(8), Constants.ALL)
      case StatGroup.Span =>
        ResponseTag(strArray(7), Constants.ALL, Constants.ALL, Constants.ALL)
      case StatGroup.SpanRemoteHost =>
        ResponseTag(strArray(7), Constants.ALL, Constants.ALL, strArray(8))
      case StatGroup.LocalHostRemoteHost =>
        ResponseTag(Constants.ALL, strArray(7), Constants.ALL, strArray(8))
      case StatGroup.LocalHostRemoteApp =>
        ResponseTag(Constants.ALL, strArray(7), strArray(8), Constants.ALL)
      case StatGroup.RemoteAppRemoteHost =>
        ResponseTag(Constants.ALL, Constants.ALL, strArray(7), strArray(8))
    }

    try {
      val columnFamily = result.getFamilyMap(COLUMN_FAMILY)
      if (!CollectionUtils.isEmpty(columnFamily)) {
        // 总量count
        val count = parseLong(columnFamily.get(COUNT_COLUMN))
        //  可用性指标
        val successCount = parseLong(columnFamily.get(SUCCESS_COUNT_COLUMN))
        val exceptionCount = parseLong(columnFamily.get(EXCEPTION_COUNT_COLUMN))
        val timeoutCount = parseLong(columnFamily.get(TIMEOUT_COUNT_COLUMN))
        val dropCount = parseLong(columnFamily.get(DROP_COUNT_COLUMN))

        val HTTP2XXCount = parseLong(columnFamily.get(HTTP_2XX_COUNT_COLUMN))
        val HTTP3XXCount = parseLong(columnFamily.get(HTTP_3XX_COUNT_COLUMN))
        val HTTP4XXCount = parseLong(columnFamily.get(HTTP_4XX_COUNT_COLUMN))
        val HTTP5XXCount = parseLong(columnFamily.get(HTTP_5XX_COUNT_COLUMN))

        val qps = parseDouble(columnFamily.get(QPS_COLUMN))
        val tp50 = parseDouble(columnFamily.get(TP50_COLUMN))
        val tp90 = parseDouble(columnFamily.get(TP90_COLUMN))
        val tp99 = parseDouble(columnFamily.get(TP99_COLUMN))

        var cost_max = parseDouble(columnFamily.get(COST_MAX_COLUMN_NEW))
        if (cost_max == 0) {
          cost_max = parseDouble(columnFamily.get(COST_MAX_COLUMN))
        }

        val infraName = parseString(columnFamily.get(INFRA_NAME_COLUMN))

        HBaseQueryResult(ts, count, successCount, exceptionCount, timeoutCount, dropCount, HTTP2XXCount,
          HTTP3XXCount, HTTP4XXCount, HTTP5XXCount, qps, tp50, tp90, tp99, cost_max, responseTag, infraName)
      } else {
        constructDefaultQueryResult(ts, responseTag)
      }
    } catch {
      case e: Exception =>
        logger.warn(s"default parse fail,msg:${e.getMessage}")
        constructDefaultQueryResult(ts, responseTag)
    }
  }

  private def constructDefaultQueryResult(ts: Int, responseTag: ResponseTag) = {
    HBaseQueryResult(ts, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0D, 0D, 0D, 0D, 0D, responseTag, PerfProtocolType.THRIFT.toString)
  }

  def getAndResetReadCount(): java.util.Map[StatRange, java.util.Map[StatGroup, AtomicInteger]] = {
    val res = readCounterMap
    readCounterMap = constructCounterMap()
    res
  }

  def getAndResetWriteCount(): java.util.Map[StatRange, java.util.Map[StatGroup, AtomicInteger]] = {
    val res = writeCounterMap
    writeCounterMap = constructCounterMap()
    res
  }

  def getTimestamp(rowkey: String) = {
    val keyArray = rowkey.split("\\|")
    keyArray.apply(4)
  }

  private def constructCounterMap(): java.util.Map[StatRange, java.util.Map[StatGroup, AtomicInteger]] =
    StatRange.values().map { range => (range, StatGroup.values().map { group => (group, new AtomicInteger())}.toMap.asJava)}.toMap.asJava


}
