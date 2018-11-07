package com.sankuai.octo.aggregator.thrift

import java.{lang, util}

import com.facebook.fb303.fb_status
import com.meituan.jmonitor.JMonitor
import com.meituan.mtrace.thrift.model.{Endpoint, StatusCode, ThriftSpan, ThriftSpanList}
import com.sankuai.octo.aggregator.StatConstants
import com.sankuai.octo.aggregator.parser.DataParser
import com.sankuai.octo.aggregator.parser.DataParser.TraceLog
import com.sankuai.octo.aggregator.parser.common.CommonLogParser
import com.sankuai.octo.aggregator.thrift.model._
import com.sankuai.octo.aggregator.thrift.service.LogCollectorService
import com.sankuai.octo.aggregator.util._
import com.sankuai.octo.statistic.constant.Constants
import com.sankuai.octo.statistic.model.PerfProtocolType
import org.apache.commons.lang3.StringUtils
import org.apache.thrift.TException
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

import scala.collection.JavaConversions._

@Service("logCollectorService")
class LogCollectorServiceImpl extends LogCollectorService.Iface {
  private val logger = LoggerFactory.getLogger(this.getClass)

  @throws(classOf[TException])
  override def uploadLog(log: SGLog): Int = {
    0
  }

  @throws(classOf[TException])
  override def uploadModuleInvoke(info: SGModuleInvokeInfo): Int = {
    val start = System.currentTimeMillis()
    //  根据白名单,判断是否将日志打印
    if (LogFilter.needLog(info.getLocalAppKey)) {
      logger.info("info:{}", info)
    }
    LogMetricCounter.incrCounter()
    JMonitor.kpiForCount("logCollector.minuter.in")
    // pre process info to avoid null AppKey
    try {
      if (!IllegalAppkey.illegal(info.getLocalAppKey)) {
        preProcess(info)
        // 将转换后的info发送DataParser
        val spans = List(thriftSpanTransfer(info))
        val thriftSpanList = new ThriftSpanList
        thriftSpanList.setSpans(spans)
        DataParser.putTraceLog(TraceLog(thriftSpanList, info.getCount))
      }
    } catch {
      case e: Exception => logger.error(s"uploadModuleInvoke failed，info ${info}", e)
    }
    val end = System.currentTimeMillis()
    if (end - start > Threshold.SMALL) {
      logger.warn(s"uploadModuleInvoke overtime ${end - start} $info")
    }
    JMonitor.add("moduleInvoke", end - start)
    0
  }

  @throws(classOf[TException])
  override def uploadCommonLog(log: CommonLog) = {
    val start = System.currentTimeMillis()
    if (StatConstants.drop()) {
      try {
        JMonitor.kpiForCount("logCollector.minuter.in")
        logger.debug(s"cmd:${log.getCmd}")
        val start = System.currentTimeMillis()
        CommonLogParser.putCommonLog(log)
        val end = System.currentTimeMillis()
        if (end - start > Threshold.SMALL) {
          logger.warn(s"uploadCommonLog overtime ${end - start} $log")
        }
      } catch {
        case e: Exception => logger.error("uploadModuleInvoke failed", e)
      }
    }
    val end = System.currentTimeMillis()
    JMonitor.add("commonLog", end - start)
    0
  }

  private def preProcess(info: SGModuleInvokeInfo): Unit = {
    if (StringUtils.isBlank(info.getLocalAppKey)) {
      info.setLocalAppKey(Constants.UNKNOWN_SERVICE)
    }
    if (StringUtils.isBlank(info.getLocalHost)) {
      info.setLocalHost(Constants.UNKNOWN_HOST)
    }
    if (StringUtils.isBlank(info.getRemoteAppKey)) {
      info.setRemoteAppKey(Constants.UNKNOWN_SERVICE)
    }
    if (StringUtils.isBlank(info.getRemoteHost)) {
      info.setRemoteHost(Constants.UNKNOWN_HOST)
    }
    if (StringUtils.isBlank(info.getSpanName)) {
      info.setSpanName(Constants.UNKNOWN_METHOD)
    } else {
      info.setSpanName(RestfulFilter.cleanSpanName(info.getSpanName))
    }
  }

  // 将SGModuleInvokeInfo转换成ThriftSpan
  def thriftSpanTransfer(info: SGModuleInvokeInfo) = {
    val local = new Endpoint(Convert.ipToInt(info.getLocalHost), info.getLocalPort.toShort, info.getLocalAppKey)
    val remote = new Endpoint(Convert.ipToInt(info.getRemoteHost), info.getRemotePort.toShort, info.getRemoteAppKey)

    val thriftSpan = new ThriftSpan
    thriftSpan.setSpanName(info.getSpanName)
    thriftSpan.setLocal(local)
    thriftSpan.setRemote(remote)
    thriftSpan.setStart(info.getStart)
    thriftSpan.setDuration(info.getCost)
    // 确认type字段含义
    thriftSpan.setType(PerfProtocolType.THRIFT.toString)
    // 0是否是success
    thriftSpan.setStatus(StatusCode.SUCCESS)

    info.getType match {
      case 1 => thriftSpan.setClientSide(false) //server
      case 0 => thriftSpan.setClientSide(true)
      case _ => thriftSpan.setClientSide(false)
    }

    thriftSpan
  }

  override def getName: String = ???

  override def shutdown(): Unit = ???

  override def getOptions: util.Map[String, String] = ???

  override def reinitialize(): Unit = ???

  override def getCounter(s: String): Long = ???

  override def setOption(s: String, s1: String): Unit = ???

  override def getCounters: util.Map[String, lang.Long] = ???

  override def getStatusDetails: String = ???

  override def getStatus: fb_status = ???

  override def getOption(s: String): String = ???

  override def getVersion: String = ???

  override def getCpuProfile(i: Int): String = ???

  override def aliveSince(): Long = ???
}