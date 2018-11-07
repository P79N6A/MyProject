package com.sankuai.octo.statistic.metric.actor

import java.io.{ByteArrayInputStream, ByteArrayOutputStream}
import java.text.DecimalFormat

import akka.actor.{Actor, ActorLogging}
import akka.event.LoggingReceive
import com.meituan.jmonitor.JMonitor
import com.meituan.mtrace.thrift.model.StatusCode
import com.sankuai.inf.octo.mns.util.ProcessInfoUtil
import com.sankuai.octo.statistic.processor.ExportService
import com.sankuai.octo.statistic.StatConstants
import com.sankuai.octo.statistic.constant.Constants
import com.sankuai.octo.statistic.domain._
import com.sankuai.octo.statistic.helper.{DailyMetricHelper, TimeProcessor}
import com.sankuai.octo.statistic.metric.MetricProcessor2.AppkeySource
import com.sankuai.octo.statistic.metrics._
import com.sankuai.octo.statistic.model.{StatData, StatGroup, StatSource, _}
import com.sankuai.octo.statistic.util.{ExecutionContextFactory, TagUtil, tair}
import org.apache.commons.lang3.StringUtils
import org.joda.time.DateTime
import org.slf4j.LoggerFactory

import scala.collection.mutable
import scala.concurrent._
import scala.concurrent.duration._

class DailyMetricProcessor2(appkeySource: AppkeySource) extends Actor with ActorLogging {

  import DailyMetricProcessor2._

  private var metrics = mutable.Map[String, SimpleCountHistogram2]()

  private val sourceStr = appkeySource.source.toString.toLowerCase()
  private val appkey = appkeySource.appkey

  //  init
  private val versionCheckTask = {
    val interval = Duration(1, MINUTES)
    context.system.scheduler.schedule(interval, interval, self, VersionCheck)
  }
  private val exportTask = {
    val interval = Duration(2, MINUTES)
    context.system.scheduler.schedule(interval, interval, self, Export)
  }

  private val cleanTask = {
    val now = DateTime.now()
    val nowSecond = (now.getMillis / 1000L).toInt
    val tomorrowSecond = (now.plusDays(1).withTimeAtStartOfDay().withMinuteOfHour(5).getMillis / 1000).toInt

    val initialDelay = tomorrowSecond - nowSecond
    context.system.scheduler.schedule(Duration(initialDelay, SECONDS), Duration(1, DAYS), self, Clean)
  }


  @throws(classOf[Exception])
  override def postStop(): Unit = {

    //  故障时导出数据
    syncData()
    versionCheckTask.cancel()
    exportTask.cancel()
    cleanTask.cancel()

    super.postStop()
  }

  override def receive = LoggingReceive {
    case metric: Metric2 => update(metric)

    case Export => syncData()

    case VersionCheck => versionCheckAction()

    case Clean => cleanData()
  }


  private def syncData() = {
    log.debug(s"DailyMetricProcessor syncData ,metric map size:{}", metrics.size)
    if (metrics.nonEmpty && StatConstants.isExportHost(appkeySource.appkey)) {
      try {
        metrics.foreach { case (name, histogram) =>
          //  sync data to tair
          syncDataByName(appkey, name, histogram)
          //  sync histogram to tair
          syncHistogramToTair(appkey, name, histogram)
        }
      } catch {
        case e: Exception => logger.error("sync to tair Fail", e)
      }
    }
  }


  private def versionCheckAction() = {
    try {
      metrics.foreach { case (name, histogram) =>
        if (!versionCheck(appkey, name, histogram)) {
          logger.warn(s"versionCheck Fail,delete invalid metric,name:$name")
          metrics -= name
        }
      }
    } catch {
      case e: Exception => logger.error("version check Fail", e)
    }
  }

  private def cleanData() = {
    log.info(s"DailyMetricProcessor Clean start")
    val dayStart = (DateTime.now().withTimeAtStartOfDay().getMillis / 1000L).toInt
    metrics.foreach { case (name, histogram) =>
      val keys = name.split("\\|")
      val metricTime = keys(2).toInt
      if (metricTime < dayStart) {
        // 落地到tair中,并将历史数据删除
        syncDataByName(appkey, name, histogram)
        metrics -= name
      }
    }

  }

  private def update(metric: Metric2) {
    metric.data.foreach { metricData =>
      updateMetricData(metric.key, metricData)
    }
  }

  private def updateMetricData(metricKey: MetricKey2, metricData: MetricData2) {
    val dayStart = TimeProcessor.getDayStart(metricData.timeStatus.minuteTs)
    val status = if (metricData.timeStatus.status == null) {
      StatusCode.SUCCESS
    } else {
      metricData.timeStatus.status
    }

    val (spanMetricName, spanAllMetricName) = if (isServerSource(appkeySource.source)) {
      val spanMetric = s"${metricKey.appkey}|${metricKey.spanname}|$dayStart"
      val spanAllMetric = s"${metricKey.appkey}|${Constants.ALL}|$dayStart"
      (spanMetric, spanAllMetric)

    } else {
      val spanMetric = s"${metricKey.appkey}|${metricKey.spanname}|$dayStart|$sourceStr"
      val spanAllMetric = s"${metricKey.appkey}|${Constants.ALL}|$dayStart|$sourceStr"
      (spanMetric, spanAllMetric)
    }


    val spanHistogram = getOrCreate(appkey, spanMetricName)
    val spanAllHistogram = getOrCreate(appkey, spanAllMetricName)

    metricData.costToCount.foreach { case (cost, count) =>
      spanHistogram.update(cost, count, status)
      spanAllHistogram.update(cost, count, status)
    }
  }

  private def getOrCreate(appkey: String, name: String) = {
    getByName(name) match {
      case Some(histogram) => histogram
      case None =>
        try {
          val tairVal = getHistogramFromTair(appkey, name)
          tairVal match {
            case Some(histogram) =>
              register(name, histogram)
            case None =>
              createAndRegister(name)
          }
        } catch {
          case e: Exception => logger.error("getOrCreate failed", e)
            createAndRegister(name)
        }
        getByName(name).get
    }
  }

  private def getByName(name: String) = {
    metrics.get(name)
  }

  private def createAndRegister(name: String) {
    register(name, new SimpleCountHistogram2())
  }

  private def register(name: String, histogram: SimpleCountHistogram2) = {
    metrics += name -> histogram
  }

}

object DailyMetricProcessor2 {

  //  received msg
  case object Export

  case object VersionCheck

  case object Clean

  private val logger = LoggerFactory.getLogger(this.getClass)

  private implicit lazy val ec: ExecutionContextExecutor = ExecutionContextFactory.build(12)

  private val IP = ProcessInfoUtil.getLocalIpV4

  private def syncDataByName(appkey: String, name: String, histogram: SimpleCountHistogram2) {
    try {
      if (ExportService.isExportAppkey(appkey)) {
        JMonitor.kpiForCount("statistic.daily.count")
        ExportService(appkey).sendDailyData(appkey, name, asHistogram3(histogram))
      } else {
        val data = asDayStat(name, histogram)
        //从本机的metric将数据同步至tair中
        Future {
          blocking {
            val metricKey = DailyMetricHelper.dailyMetricTairKey(name, StatConstants.ENVIRONMENT)
            logger.debug("syncToTair metricKey:{}", metricKey)
            processSyncData(metricKey, data)
          }
        }
      }
    } catch {
      case e: Exception => logger.error(s"DailyMetricProcessor syncData failed name: $name", e)
    }
  }

  private def asHistogram3(histogram: SimpleCountHistogram2): SimpleCountHistogram3 = {
    val reservoir2 = histogram.getReservoir
    val reservoir3 = new SimpleCountReservoir3(reservoir2.getMax, reservoir2.getValues)
    new SimpleCountHistogram3(histogram.getCount, histogram.getSuccessCount,
      histogram.getExceptionCount, histogram.getTimeoutCount, histogram.getDropCount,
      histogram.getHTTP2XXCount, histogram.getHTTP3XXCount, histogram.getHTTP4XXCount, histogram.getHTTP5XXCount,
      histogram.getVersion, System.currentTimeMillis(), histogram.getUpdateTime, reservoir3)
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

  private def processSyncData(data: (String, AnyRef)) {
    tair.putAsync(data._1, data._2)
  }

  /**
   *
   * @param statSource statSource 只可能为client or Server
   * @return true标识Server,false标识client
   */
  private def isServerSource(statSource: StatSource): Boolean = {
    statSource == StatSource.Server
  }

  /**
   *
   * @param name ,metric name
   * @return tair中反查的对象
   */
  private def getHistogramFromTair(appkey: String, name: String) = {
    val tairKey = getTairHistogramKey(appkey, name)
    logger.debug(s"getFromTair,tairKey:$tairKey")
    tair.getValue(tairKey) match {
      case Some(bytes) =>
        val histogram = new SimpleCountHistogram2()
        histogram.init(new ByteArrayInputStream(bytes))
        Some(histogram)
      case None => None
    }
  }

  private def syncHistogramToTair(appkey: String, name: String, histogram: SimpleCountHistogram2) {
    histogram.incrVersion()
    val tairKey = getTairHistogramKey(appkey, name)
    val stream = new ByteArrayOutputStream()
    histogram.dump(stream)
    logger.debug("syncHistogramToTair,tairKey:{}", tairKey)
    Future {
      val bytes = stream.toByteArray
      blocking {
        processSyncHistogram((tairKey, bytes, StatRange.Day.getTimeRange))
      }
    }
  }

  private def processSyncHistogram(data: (String, Array[Byte], Int)) {
    tair.putAsync(data._1, data._2, data._3)
  }

  private def getTairHistogramKey(appkey: String, name: String) = {
    if (isExportAppkey(appkey)) {
      s"${StatConstants.ENVIRONMENT}|$IP|daily|stat|simple|histogram|$name"
    } else {
      s"${StatConstants.ENVIRONMENT}|daily|stat|simple|histogram|$name"
    }
  }

  /**
   *
   * @param name      metric name
   * @param histogram registry中的metric
   * @return true通过检查,否则false
   */
  private def versionCheck(appkey: String, name: String, histogram: SimpleCountHistogram2): Boolean = {
    val tairKey = getTairHistogramKey(appkey, name)
    tair.getValue(tairKey) match {
      case Some(bytes) =>
        val tairHistogram = new SimpleCountHistogram2()
        tairHistogram.init(new ByteArrayInputStream(bytes))
        histogram.newerThan(tairHistogram)
      case None => true
    }
  }
  def isExportAppkey(appkey: String): Boolean = ExportService.isExportAppkey(appkey)
}

