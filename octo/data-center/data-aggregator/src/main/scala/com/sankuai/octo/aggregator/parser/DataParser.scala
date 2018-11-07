package com.sankuai.octo.aggregator.parser

import com.meituan.mtrace.thrift.model.{ThriftSpan, ThriftSpanList}
import com.sankuai.octo.aggregator.IpValidator
import com.sankuai.octo.aggregator.util.{Convert, LogFilter, RestfulFilter, Threshold}
import com.sankuai.octo.statistic.constant.Constants
import com.sankuai.octo.statistic.model.{MetricData, MetricKey, PerfProtocolType, StatSource}
import com.sankuai.octo.statistic.util.ExecutorFactory
import org.apache.commons.lang3.StringUtils
import org.slf4j.LoggerFactory

import scala.collection.JavaConversions._

object DataParser {
  private val logger = LoggerFactory.getLogger(this.getClass)

  val asyncTraceLogParser = new ExecutorFactory(parseTraceLog, "DataParser.asyncTraceLogParser", 6, 16, 20000)

  case class TraceLog(log: ThriftSpanList, count: Int)

  def putTraceLog(traceLog: TraceLog) = asyncTraceLogParser.submit(traceLog)

  /**
    * 对于uploadModuleInvoke数据，spans大小只有1，设置count即可
    * 对于commonlog数据，count都是1
    */
  private def parseTraceLog(traceLog: TraceLog) {

    //  根据白名单,判断是否将日志打印
    if (logger.isDebugEnabled()) {
      traceLog.log.spans.foreach { spanLog =>
        if (LogFilter.needLog(spanLog.local.appKey)) {
          logger.debug("info:{}", spanLog)
        }
      }
    }
    val start = System.currentTimeMillis()
    val thriftSpanList = traceLog.log
    val count = traceLog.count
    thriftSpanList.spans.foreach {
      thriftSpan =>
        val local = thriftSpan.getLocal
        val remote = thriftSpan.getRemote

        val appkey = local.getAppKey
        val remoteAppkey = remote.getAppKey
        val spanname = thriftSpan.getSpanName
        if (!"${octo_hotel_admin_rest_appkey}".equalsIgnoreCase(appkey) &&
          !"FacebookService.getStatus".equalsIgnoreCase(spanname) &&
          !"${octo_hotel_admin_rest_appkey}".equalsIgnoreCase(remoteAppkey)) {
          MetricParser.put(constructMetric(thriftSpan, count))
        }
    }
    val end = System.currentTimeMillis()
    if (end - start > Threshold.BIG) {
      logger.warn(s"parseTraceLog overtime ${end - start}")
    }
  }

  def constructMetric(info: ThriftSpan, count: Int) = {
    val local = info.getLocal
    val localIp = IpValidator.filterExternalIp(Convert.intToIp(local.getIp))

    val remote = info.getRemote
    val remoteIp = IpValidator.filterExternalIp(Convert.intToIp(remote.getIp))

    // 当被commonLogParse调用时并没有被preProcessor
    val appkey = if (StringUtils.isBlank(local.getAppKey)) Constants.UNKNOWN_SERVICE else local.getAppKey
    val localhost = if (StringUtils.isBlank(localIp)) Constants.UNKNOWN_HOST else localIp
    val remoteAppkey = if (StringUtils.isBlank(remote.getAppKey)) Constants.UNKNOWN_SERVICE else remote.getAppKey
    val remoteHost = if (StringUtils.isBlank(remoteIp)) Constants.UNKNOWN_HOST else remoteIp
    val spanname = if (StringUtils.isBlank(info.getSpanName)) Constants.UNKNOWN_METHOD else RestfulFilter.cleanSpanName(info.getSpanName)


    val source = if (info.isClientSide) StatSource.Client else StatSource.Server
    // MetricKey的作用是聚合一部分数据，减少带宽？
    val key = new MetricKey(appkey, spanname, localhost, remoteAppkey, remoteHost, source,
      PerfProtocolType.getInstance(info.getType), if(StringUtils.isBlank(info.getInfraName)) "mtthrift" else info.getInfraName)
    val data = new MetricData(info.getStart, count, info.getDuration, info.getStatus)
    MetricStruct(key, data)
  }
}