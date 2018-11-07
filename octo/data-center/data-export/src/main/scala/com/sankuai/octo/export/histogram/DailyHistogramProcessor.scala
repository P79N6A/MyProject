package com.sankuai.octo.export.histogram

import java.text.DecimalFormat

import akka.actor.{Actor, ActorLogging}
import com.meituan.jmonitor.JMonitor
import com.sankuai.inf.octo.mns.util.ProcessInfoUtil
import com.sankuai.octo.export.StatConstants
import com.sankuai.octo.export.util.TagUtil
import com.sankuai.octo.statistic.domain.TimeSpan
import com.sankuai.octo.statistic.helper.DailyMetricHelper
import com.sankuai.octo.statistic.metrics.{SimpleCountHistogram2, Snapshot}
import com.sankuai.octo.statistic.model._
import com.sankuai.octo.statistic.util.{ExecutionContextFactory, tair}
import org.apache.commons.lang3.StringUtils
import org.slf4j.LoggerFactory

import scala.collection.mutable
import scala.concurrent._
import scala.concurrent.duration._

/**
 * 处理天粒度的数据
 */
class DailyHistogramProcessor extends Actor with ActorLogging {

  import DailyHistogramProcessor._

  private val map = mutable.Map[TimeSpan, SimpleCountHistogram2]()
  val interval = Duration(2, MINUTES)
  private val exportTask = {
    context.system.scheduler.schedule(interval, interval, self, Export)
  }

  override def receive: Receive = {
    case histogram: DailyHistogram => processDailyHistogram(histogram)
    case Export => syncData()
  }

  @throws(classOf[Exception])
  override def postStop(): Unit = {
    //  故障时导出数据
    syncData()
    exportTask.cancel()
    super.postStop()
  }

  private def syncData() = {
    if (map.nonEmpty) {
      try {
        JMonitor.kpiForCount("dataExport.daily.histogram", map.size)
        map.foreach { case (timeSpan, histogram) =>
          if (timeSpan.getSpanMetricName.contains("|all")) {
            logger.info(s"${timeSpan.toString}")
          }
          syncDataByName(timeSpan.getSpanMetricName, histogram)
          map -= timeSpan
        }
      } catch {
        case e: Exception => logger.error("sync to tair Fail", e)
      }
    }
  }

  private def processDailyHistogram(dailyHistogram: DailyHistogram) {
    val createTime = dailyHistogram.histogram.getCreateTime
    val timeSeq = (createTime / 1000 / interval.toSeconds).toInt
    val ip_rep = s"${dailyHistogram.clientIp}|"
    val name  = dailyHistogram.name.replace(ip_rep,"")
    val timeSpan = new TimeSpan(timeSeq, name)
    val histogram = map.get(timeSpan)
    if (histogram.isDefined) {
      histogram.get.merge(dailyHistogram.histogram)
    } else {
      map.put(timeSpan, dailyHistogram.histogram)
    }

  }

}

object DailyHistogramProcessor {

  case class DailyHistogram(appkey: String, clientIp: String, name: String, histogram: SimpleCountHistogram2)

  case object Export

  private val logger = LoggerFactory.getLogger(this.getClass)

  private implicit lazy val ec: ExecutionContextExecutor = ExecutionContextFactory.build(20)

  private val IP = ProcessInfoUtil.getLocalIpV4

  private def syncDataByName(name: String, histogram: SimpleCountHistogram2) {
    try {
      val data = asDayStat(name, histogram)
      //从本机的metric将数据同步至tair中
      Future {
        blocking {
          val metricKey = DailyMetricHelper.dailyMetricTairKey(name, StatConstants.ENVIRONMENT)
          //输出调试日志
          if (name.contains("|all")) {
            logger.info(s"syncToTair metricKey:${metricKey},DayStat :${data}")
          }
          //注释掉实际导出
          processSyncData(metricKey, data)
        }
      }
    } catch {
      case e: Exception => logger.error(s"DailyMetricProcessor syncData failed name: $name", e)
    }
  }

  private def processSyncData(data: (String, AnyRef)) {
    tair.putAsync(data._1, data._2)
  }

  private def asDayStat(name: String, histogram: SimpleCountHistogram2) = {
    val snap = histogram.getSnapshot
    val df: DecimalFormat = new DecimalFormat("#.###")
    val data = asStatData(snap, df)
    val count = histogram.getCount
    val keys = name.split("\\|")
    val appKey = keys(0)
    val spanName = keys(1)
    val dayStartSeconds = if (StringUtils.isNumeric(keys(2))) {
      keys(2).toInt
    } else {
      logger.error(s"异常name:$name")
      keys(3).toInt
    }
    data.setCount(count)
    data.setSuccessCount(histogram.getSuccessCount)
    data.setExceptionCount(histogram.getExceptionCount)
    data.setTimeoutCount(histogram.getTimeoutCount)
    data.setDropCount(histogram.getDropCount)

    data.setHTTP2XXCount(histogram.getHTTP2XXCount)
    data.setHTTP3XXCount(histogram.getHTTP3XXCount)
    data.setHTTP4XXCount(histogram.getHTTP4XXCount)
    data.setHTTP5XXCount(histogram.getHTTP5XXCount)

    //覆盖跨天的情况
    val now = System.currentTimeMillis()
    var timeRange = now / 1000L - dayStartSeconds
    if (timeRange > StatRange.Day.getTimeRange) {
      timeRange = StatRange.Day.getTimeRange
    } else if (timeRange == 0) {
      timeRange = 1
    }

    // TODO 暂时不记录tair天粒度数据接口的类型
    data.setQps(df.format(count.toDouble / timeRange).toDouble)
    data.setAppkey(appKey)
    data.setTs(dayStartSeconds)
    data.setEnv(StatConstants.env)
    data.setRange(StatRange.Day)
    data.setGroup(StatGroup.Span)
    data.setTags(TagUtil.getStatTag(spanname = spanName))

    data.setUpdateTime(now)
    data.setUpdateFrom(IP)

    data
  }

  private def asStatData(snap: Snapshot, df: DecimalFormat) = {
    val data = new StatData()
    try {
      data.setCost50(df.format(snap.getMedian).toDouble)
      data.setCost75(df.format(snap.get75thPercentile).toDouble)
      data.setCost90(df.format(snap.getValue(0.90)).toDouble)
      data.setCost95(df.format(snap.get95thPercentile).toDouble)
      data.setCost98(df.format(snap.get98thPercentile).toDouble)
      data.setCost99(df.format(snap.get99thPercentile).toDouble)
      data.setCost999(df.format(snap.get999thPercentile).toDouble)
      data.setCostMin(df.format(snap.getMin).toDouble)
      data.setCostMean(df.format(snap.getMean).toDouble)
      data.setCostMax(df.format(snap.getMax).toDouble)
    } catch {
      case e: Exception => logger.error("asStatData fail", e)
    }
    data
  }

}