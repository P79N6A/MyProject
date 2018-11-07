/*
package com.sankuai.octo.statistic.metric

import java.io.{ByteArrayInputStream, ByteArrayOutputStream}
import java.text.DecimalFormat
import java.util.concurrent.locks.{ReentrantLock, ReentrantReadWriteLock}
import java.util.concurrent.{Executors, TimeUnit}

import com.meituan.mtrace.thrift.model.StatusCode
import com.meituan.service.mobile.mtthrift.util.ProcessInfoUtil
import com.sankuai.octo.statistic.constant.Constants
import com.sankuai.octo.statistic.helper.DailyMetricHelper
import com.sankuai.octo.statistic.metrics.{MetricRegistry, SimpleCountHistogram, Snapshot}
import com.sankuai.octo.statistic.model._
import com.sankuai.octo.statistic.util.{CronScheduler, ExecutionContextFactory, StatThreadFactory, tair}
import com.sankuai.octo.statistic.{StatConstants, metrics}
import org.joda.time.DateTime
import org.quartz._
import org.slf4j.LoggerFactory

import scala.collection.JavaConversions._
import scala.collection.JavaConverters._
import scala.collection.concurrent.TrieMap
import scala.concurrent.{Future, blocking}

object DailyMetricProcessor {

  private val logger = LoggerFactory.getLogger(DailyMetricProcessor.getClass)

  // name format:appKey|spanName|dayStart
  private[metric] val registryMap = TrieMap[String, MetricRegistry]()
  private val scheduler = Executors.newScheduledThreadPool(2, StatThreadFactory.threadFactory(this.getClass))

  private val registryMapLock = new ReentrantLock()

  //  减小互斥的碰撞
  private val locks = (1 to 31).map(_ => new ReentrantReadWriteLock())

  private def selectLock(appKey: String) = {
    locks(Math.abs(appKey.hashCode % locks.size))
  }

  private implicit lazy val ec = ExecutionContextFactory.build(Runtime.getRuntime.availableProcessors)

  // 定时同步histogram至tair,避免host shutdown问题
  private val timerTask = new Runnable {
    override def run(): Unit = {
      if (registryMap.nonEmpty) {
        logger.debug(s"begin run sync to tair")
        val start = System.currentTimeMillis()
        try {
          registryMap.foreach {
            entry =>
              val lock = selectLock(entry._1)
              val writeLock = lock.writeLock()
              writeLock.lock()
              try {
                entry._2.getMetrics.foreach {
                  x =>
                    val key = x._1
                    val value = x._2
                    //  sync data to tair
                    syncData(key, value)
                    //  sync histogram to tair
                    syncHistogramToTair(key, value)
                }
              } finally {
                writeLock.unlock()
              }
          }
          logger.debug(s"end run sync metrics to tair,time: ${System.currentTimeMillis() - start} ms,size: ${registryMap.size}")
        } catch {
          case e: Exception => logger.error("sync to tair Fail", e)
        }
      }
    }
  }
  //定时对本地维护的histogram进行version check,将不通过的remove
  private val versionCheckTask = new Runnable {

    override def run(): Unit = {
      try {
        registryMap.foreach(entry => {
          val registry = entry._2
          registry.getMetrics.foreach {
            metric =>
              if (!versionCheck(metric._1, metric._2)) {
                logger.warn(s"versionCheck Fail,delete invalid metric,name:${metric._1}")
                registry.remove(metric._1)
              }
          }
          if (registry.getMetrics.isEmpty) {
            registryMap.remove(entry._1)
          }
        })
      } catch {
        case e: Exception => logger.error("version check Fail", e)
      }
    }

    /**
     *
     * @param name metric name
     * @param metric registry中的metric
     * @return true通过检查,否则false
     */
    private def versionCheck(name: String, metric: metrics.Metric): Boolean = {
      toGram(metric) match {
        case Some(histogram) =>
          val tairKey = getTairHistogramKey(name)
          tair.getValue(tairKey) match {
            case Some(bytes) =>
              val tairHistogram = new SimpleCountHistogram()
              tairHistogram.init(new ByteArrayInputStream(bytes))
              histogram.newerThan(tairHistogram)
            case None => true
          }
        case None =>

          //类型不一致,检查失败
          false
      }
    }

  }

  {
    scheduler.scheduleAtFixedRate(timerTask, 60, 60, TimeUnit.SECONDS)
    scheduler.scheduleAtFixedRate(versionCheckTask, 30, 30, TimeUnit.SECONDS)

    // daily update,将每天的数据落地,滚动到下一天
    scheduleDailyJob()

    // 在jvm退出时优雅关闭
    Runtime.getRuntime.addShutdownHook(new Thread() {
      override def run(): Unit = {
        scheduler.submit(timerTask)
        scheduler.shutdown()
        scheduler.awaitTermination(10, TimeUnit.SECONDS)
      }
    })

  }

  def update(metric: Metric) {
    metric.data.foreach {
      metricData => updateMetricData(metric.key, metricData)
    }
  }

  private def isServerSource(statSource: StatSource): Boolean = {
    statSource match {
      case StatSource.Server | StatSource.ServerDrop | StatSource.ServerSlow | StatSource.ServerFailure => true
      case _ => false
    }
  }

  private[metric] def syncData(name: String, metric: metrics.Metric) {
    toGram(metric) match {
      case Some(histogram) =>
        try {
          val data = asDayStat(name, histogram)
          //从本机的metric将数据同步至tair中
          Future {
            blocking {
              val metricKey = DailyMetricHelper.dailyMetricTairKey(name, StatConstants.ENVIRONMENT)
              logger.debug("syncToTair metricKey:{}", metricKey)
              processSyncData(metricKey, data)
            }
          }
        } catch {
          case e: Exception => logger.error(s"DailyMetricProcessor syncData failed name: $name", e)
        }
      case None =>
    }
  }

  private def processSyncData(data: (String, AnyRef)) {
    tair.putAsync(data._1, data._2)
  }

  private def syncHistogramToTair(name: String, metric: metrics.Metric) {
    toGram(metric).foreach { histogram =>
      histogram.incrVersion()
      val tairKey = getTairHistogramKey(name)
      val stream = new ByteArrayOutputStream()
      histogram.dump(stream)
      logger.debug("syncHistogramToTair,tairKey:{}", tairKey)
      Future {
        val bytes = stream.toByteArray
        blocking {
          processSyncHistogram((tairKey, bytes, 86400))
        }
      }
    }
  }

  private def processSyncHistogram(data: (String, Array[Byte], Int)) {
    tair.putAsync(data._1, data._2, data._3)
  }

  private def updateMetricData(metricKey: MetricKey, metricData: MetricData) {
    val dayStart = DailyMetricHelper.dayStart(metricData.start)
    val source = metricKey.source
    val status = if (metricData.status == null) {
      StatusCode.SUCCESS
    } else {
      metricData.status
    }

    if (isServerSource(source)) {
      val spanMetricName = s"${metricKey.appkey}|${metricKey.spanname}|$dayStart"
      val spanAllMetricName = s"${metricKey.appkey}|${Constants.ALL}|$dayStart"
      val spanHistogram = getOrCreate(metricKey.appkey, spanMetricName)
      val spanAllHistogram = getOrCreate(metricKey.appkey, spanAllMetricName)

      val lock = selectLock(metricKey.appkey)
      val readLock = lock.readLock()
      readLock.lock()
      try {
        spanHistogram.update(metricData.cost, metricData.count, status)
        spanAllHistogram.update(metricData.cost, metricData.count, status)
      } finally {
        readLock.unlock()
      }
    } else {
      val spanMetricName = s"${metricKey.appkey}|${metricKey.spanname}|$dayStart|${source.toString.toLowerCase()}"
      val spanAllMetricName = s"${metricKey.appkey}|${Constants.ALL}|$dayStart|${source.toString.toLowerCase()}"
      val spanHistogram = getOrCreate(metricKey.appkey, spanMetricName)
      val spanAllHistogram = getOrCreate(metricKey.appkey, spanAllMetricName)

      val lock = selectLock(metricKey.appkey)
      val readLock = lock.readLock()
      readLock.lock()
      try {
        spanHistogram.update(metricData.cost, metricData.count, status)
        spanAllHistogram.update(metricData.cost, metricData.count, status)
      } finally {
        readLock.unlock()
      }
    }
  }

  private def getOrCreate(appKey: String, name: String) = {
    // double check
      val gram = getByName(appKey, name)
      if (gram == null) {
        val tairVal = getHistogramFromTair(name)
        registryMapLock.lock()
        try {
          if (getByName(appKey, name) == null) {
            tairVal match {
              case Some(histogram) =>
                register(appKey, name, histogram)
              case None =>
                createAndRegister(appKey, name)
            }
          }
        } catch {
          case e: Exception => logger.error("getOrCreate failed", e)
            createAndRegister(appKey, name)
        } finally {
          registryMapLock.unlock()
        }
        getByName(appKey, name)
      } else {
        gram
      }
  }

  private def getByName(appKey: String, name: String) = {
    registryMap.get(appKey) match {
      case Some(registry) =>
        val gram = registry.getMetrics.get(name)
        gram.asInstanceOf[SimpleCountHistogram]
      case None => null
    }
  }

  /**
   *
   * @param name ,metric name
   * @return tair中反查的对象
   */
  private def getHistogramFromTair(name: String) = {
    val tairKey = getTairHistogramKey(name)
    logger.debug(s"getFromTair,tairKey:$tairKey")
    tair.getValue(tairKey) match {
      case Some(bytes) =>
        val histogram = new SimpleCountHistogram()
        histogram.init(new ByteArrayInputStream(bytes))
        Some(histogram)
      case None => None
    }
  }

  private def createAndRegister(appKey: String, name: String) {
    logger.debug(s"create appkey:$appKey,name:$name")
    register(appKey, name, new SimpleCountHistogram())
  }

  private def register(appKey: String, name: String, histogram: SimpleCountHistogram) {
    try {
      val registry = registryMap.getOrElseUpdate(appKey, new MetricRegistry)
      if (!registry.getMetrics.containsKey(name)) {
        logger.debug(s"register appkey:$appKey,name:$name")
        registry.register(name, histogram)
      }
    } catch {
      case e: Exception => logger.error(s"register conflict:appKey:$appKey,name:$name", e)
    }
  }

  private def debug(name: String, metric: metrics.Metric) {
    val histogram = metric.asInstanceOf[SimpleCountHistogram]
    val rtt = histogram.getSnapshot
    logger.debug(s"$name ${histogram.getCount} ${rtt.getMin} ${rtt.getMedian} ${rtt.getValue(0.90)} ${rtt.getValue(0.95)} ${rtt.getValue(0.99)} ${rtt.getMax}")
  }

  private def toGram(metric: metrics.Metric): Option[SimpleCountHistogram] = {
    metric match {
      case gram: SimpleCountHistogram => Some(gram)
      case _ => None
    }
  }

  private def asDayStat(name: String, histogram: SimpleCountHistogram) = {
    val snap = histogram.getSnapshot
    val count = histogram.getCount
    val data = asStatData(snap)
    val keys = name.split("\\|")
    val appKey = keys(0)
    val spanName = keys(1)
    val dayStartSeconds = keys(2).toInt
    data.setCount(count)
    data.setSuccessCount(histogram.getSuccessCount)
    data.setExceptionCount(histogram.getExceptionCount)
    data.setTimeoutCount(histogram.getTimeoutCount)
    data.setDropCount(histogram.getDropCount)
    //覆盖跨天的情况
    val now = System.currentTimeMillis()
    var timeRange = now / 1000L - dayStartSeconds
    if (timeRange > 86400) {
      timeRange = 86400
    }
    val df: DecimalFormat = new DecimalFormat("#.###")
    data.setQps(df.format(count.toDouble / timeRange).toDouble)
    data.setAppkey(appKey)
    data.setTs(dayStartSeconds)
    data.setEnv(StatConstants.env)
    data.setRange(StatRange.Day)
    data.setGroup(StatGroup.Span)
    data.setTags(Map(Constants.SPAN_NAME -> spanName).asJava)

    data.setUpdateTime(now)
    data.setUpdateFrom(ProcessInfoUtil.getLocalIpV4FromLocalCache)

    data
  }

  private def asStatData(snap: Snapshot) = {
    val df: DecimalFormat = new DecimalFormat("#.###")
    val data = new StatData()
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
    data
  }

  private def getTairHistogramKey(name: String) = s"${StatConstants.ENVIRONMENT}|daily|stat|simple|histogram|$name"

  private def scheduleDailyJob() = {

    val job: JobDetail = JobBuilder.newJob(classOf[DailyUpdateJob]).build()
    val trigger: Trigger = TriggerBuilder.newTrigger().startNow()
      .withSchedule(CronScheduleBuilder.dailyAtHourAndMinute(0, 5)).build()
    CronScheduler.scheduleJob(job, trigger)
  }
}

@DisallowConcurrentExecution
class DailyUpdateJob extends Job {

  private val logger = LoggerFactory.getLogger(this.getClass)

  @throws(classOf[JobExecutionException])
  override def execute(ctx: JobExecutionContext) {
    logger.info("DailyUpdateJob Start!")
    // 取得昨天的时间
    val now = DateTime.now()
    val startOfYesterday = now.minusDays(1).withTimeAtStartOfDay()
    val yesterdayStartSecond = startOfYesterday.getMillis.toInt / 1000

    // daily update metrics
    DailyMetricProcessor.registryMap.foreach(entry => {
      val registry = entry._2
      registry.getMetrics.foreach(metric => {
        val keys = metric._1.split("\\|")
        val metricTime = keys(2).toInt
        if (metricTime == yesterdayStartSecond) {
          // 落地到tair中,并将昨天的数据从metric移除
          DailyMetricProcessor.syncData(metric._1, metric._2)
          registry.remove(metric._1)
        }
      })
    })

  }

}
*/
