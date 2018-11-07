package com.sankuai.octo.aggregator.parser.common

import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.{Executors, TimeUnit}

import com.meituan.mtrace.thrift.model.Constants._
import com.meituan.mtrace.thrift.model.ThriftSpanList
import com.sankuai.meituan.config.listener.IConfigChangeListener
import com.sankuai.octo.aggregator.parser.DataParser
import com.sankuai.octo.aggregator.parser.DataParser.TraceLog
import com.sankuai.octo.aggregator.store.TableStoreService
import org.joda.time.DateTime

import com.sankuai.octo.aggregator.util.{FunctionSwitch, LogCompress, LogFilter, MyProxy}
import com.sankuai.octo.statistic.constant.Constants
import com.sankuai.octo.statistic.helper.{DailyMetricHelper, Serializer}
import com.sankuai.octo.statistic.util.StatThreadFactory
import org.slf4j.LoggerFactory
import org.springframework.util.CollectionUtils

import scala.collection.JavaConversions._

object MtraceParser {
  private val logger = LoggerFactory.getLogger(this.getClass)
  private val scheduler = Executors.newSingleThreadScheduledExecutor(StatThreadFactory.threadFactory(this.getClass))
  private val config = MyProxy.mcc
  private val DEGREE_CHAIN = "degree.mtraceChain"
  private val DEGREE_TOTAL = "degree.mtraceTotal"

  private var chainDegree = try {
    config.get(DEGREE_CHAIN, "0.01").toDouble
  } catch {
    case e: Exception => logger.error("mtraceDegree init failed", e)
      0.01
  }
  private val chainStore = TableStoreService(chainDegree)

  private var totalDegree = try {
    config.get(DEGREE_TOTAL, "0.01").toDouble
  } catch {
    case e: Exception => logger.error("mtraceDegree init failed", e)
      0.01
  }
  private val totalStore = TableStoreService(totalDegree)

  val MTraceCount = new AtomicInteger()
  val MTraceErrorCount = new AtomicInteger()

  def processMtraceChain(logContent: Array[Byte]) = {
    //  透明存储mtrace log
    MTraceCount.incrementAndGet()
    try {
      if (FunctionSwitch.mtraceStoreSwitch == FunctionSwitch.SWITCH_ON) {
        chainStore.store(logContent)
      }
    } catch {
      case e: Exception =>
        logger.warn(s"cmd:${Constants.MTRACE_LOG_LIST} error", e)
        MTraceErrorCount.incrementAndGet()
    }
  }

  def processMtraceTotal(logContent: Array[Byte]) = {
    //  新的cmd字段即要透明存储mtrace log，也要进行性能统计
    logger.debug("receive mtrace_log_list length:{}", logContent.length)
    try {
      if (FunctionSwitch.mtraceStoreSwitch == FunctionSwitch.SWITCH_ON) {
        totalStore.store(logContent)
      }
      val thriftSpanList = Serializer.toObject(logContent, classOf[ThriftSpanList])
      printLog(thriftSpanList)
      DataParser.putTraceLog(TraceLog(thriftSpanList, 1))
    } catch {
      case e: Exception =>
        logger.warn(s"cmd:$TRACE_DATA_LIST error", e)
    }
  }

  def processMtraceTotal2(logContent: Array[Byte]) = {
    logger.debug("receive mtrace_log_list length:{}", logContent.length)
    try {
      val thriftSpanList = Serializer.toObject(logContent, classOf[ThriftSpanList])
      printLog(thriftSpanList)
      DataParser.putTraceLog(TraceLog(thriftSpanList, 1))
    } catch {
      case e: Exception =>
        logger.warn(s"cmd:$TRACE_DATA_LIST error", e)
    }
  }

  private def printLog(thriftSpanList: ThriftSpanList) = {
    if (!CollectionUtils.isEmpty(thriftSpanList.spans)) {
      thriftSpanList.spans.foreach { span =>
        if (LogFilter.needLog(span.local.appKey)) {
          logger.debug("span:{}", span)
        }

      }
    }

  }

  def processCompress(logContent: Array[Byte]) = {
    val start = new DateTime().getMillis
    val data = LogCompress.decompress(logContent)
    val end = new DateTime().getMillis
    logger.debug(s"commonLog decompress cost ${end - start}")
    processMtraceTotal2(data)
  }

  private val task = new Runnable {
    var dayStart = DailyMetricHelper.dayStart()

    override def run(): Unit = {
      val time = (System.currentTimeMillis() / 1000L).toInt
      if (time - dayStart > Constants.ONE_DAY_SECONDS) {
        dayStart = DailyMetricHelper.dayStart()
        MTraceCount.set(0)
        MTraceErrorCount.set(0)
      }
    }
  }

  {
    try {
      scheduler.scheduleAtFixedRate(task, 1, 1, TimeUnit.MINUTES)

      config.addListener(DEGREE_CHAIN, new IConfigChangeListener() {
        def changed(key: String, oldValue: String, newValue: String) = {
          logger.info(s"MtraceParser change $key $oldValue $newValue")
          try {
            chainDegree = newValue.toDouble
          } catch {
            case e: Exception => logger.error("MtraceParser change error", e)
          }
        }
      })

      logger.info(s"addListener $DEGREE_CHAIN success")

      config.addListener(DEGREE_TOTAL, new IConfigChangeListener() {
        def changed(key: String, oldValue: String, newValue: String) = {
          logger.info(s"MtraceParser change $key $oldValue $newValue")
          try {
            totalDegree = newValue.toDouble
          } catch {
            case e: Exception => logger.error("MtraceParser change error", e)
          }
        }
      })

      logger.info(s"addListener $DEGREE_TOTAL success")
    } catch {
      case e: Exception => logger.error("MtraceParser static failed", e)
    }
  }
}
