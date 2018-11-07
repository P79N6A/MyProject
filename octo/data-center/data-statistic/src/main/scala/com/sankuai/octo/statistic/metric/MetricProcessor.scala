/*
package com.sankuai.octo.statistic.metric

import java.util.concurrent.ConcurrentHashMap

import akka.actor.{ActorRef, ActorSystem, Props}
import com.sankuai.octo.statistic.StatConstants
import com.sankuai.octo.statistic.constant.Constants
import com.sankuai.octo.statistic.domain.InstanceKey
import com.sankuai.octo.statistic.helper.DailyMetricHelper
import com.sankuai.octo.statistic.metric.MetricProcessor2.AppkeySource
import com.sankuai.octo.statistic.metric.actor.DailyMetricProcessor2
import com.sankuai.octo.statistic.model._
import com.sankuai.octo.statistic.util.ExecutorFactory
import org.joda.time.DateTime
import org.slf4j.LoggerFactory

import scala.collection.JavaConversions._

object MetricProcessor {

  private val system = ActorSystem("LogStatistic")
  private val dailyProcessorMap = new ConcurrentHashMap[AppkeySource, ActorRef]()

  private def processMetric(metric: Metric) {
    update(metric)
    // 暂时对于 天 粒度特殊处理, 后续聚合后可以删除
    processDailyMetric(metric)
  }

  private val logger = LoggerFactory.getLogger(MetricProcessor.getClass)


  // 队列大小不要设置太大，防止数据堆积影响计算准确性
  val metricExecutor = ExecutorFactory(processMetric, "MetricProcessor", 8, 32, 20000, 15)

  // 去掉天粒度，falcon和hbase均没有用到
  val ranges = StatRange.values().filter(_ != StatRange.Day).toSeq

  def putMetric(metric: Metric) {
    metricExecutor.submit(metric)
  }

  private def processDailyMetric(metric: Metric) = {
    //  source统一为client or server
    val source = metric.key.source match {
      case StatSource.Server | StatSource.ServerDrop | StatSource.ServerSlow | StatSource.ServerFailure => StatSource.Server
      case _ => StatSource.Client
    }
    val appkeySource = AppkeySource(metric.key.appkey, source)

    val dailyProcessor = dailyProcessorMap.get(appkeySource)
    val actorRef = if (dailyProcessor == null) {
      val tmp = system.actorOf(Props(classOf[DailyMetricProcessor2], appkeySource).withDispatcher("custom.daily-dispatcher"))
      val ret = dailyProcessorMap.putIfAbsent(appkeySource, tmp)
      if (ret == null) {
        //  没有并发冲突
        tmp
      } else {
        //  并发冲突,返回之前的
        system.stop(tmp)
        ret
      }
    } else {
      dailyProcessor
    }
    actorRef ! metric
  }

  /**
   * 按tag维度拆分后，基于MetricManager的接口实现
   *
   * @param metric 需要更新的metric
   */
  private def update(metric: Metric) {
    val appkey = metric.key.appkey
    val env = StatConstants.env
    val source = metric.key.source
    val groupToTagMapping = constructGroupToTag(metric.key)

    val perfProtocolType = if (metric.key.perfProtocolType == null) {
      PerfProtocolType.THRIFT
    } else {
      metric.key.perfProtocolType
    }

    ranges.foreach { range =>
      metric.data.foreach { metricData =>
        val ts = range match {
          case StatRange.Day => DailyMetricHelper.dayStart(metricData.start)
          case StatRange.Hour => getHourStart(metricData.start)
          case StatRange.Minute => getMinuteStart(metricData.start)
        }
        groupToTagMapping.foreach { case (group, tags) =>
          updateInstance(metricData, appkey, ts, env, source, range, group, perfProtocolType, tags)
        }
      }
    }
  }

  // 获取一个时间戳开始分钟的时间戳，输入毫秒，输出秒(falcon是秒)
  private def getMinuteStart(ts: Long) = {
    (ts / 1000 / 60 * 60).toInt
  }

  private def getHourStart(ts: Long) = {
    val dateTime = new DateTime(ts)
    (dateTime.withMinuteOfHour(0).withSecondOfMinute(0).getMillis / 1000L).toInt
  }

  private def getTags(spanname: String = Constants.ALL, localHost: String = Constants.ALL,
                      remoteAppKey: String = Constants.ALL, remoteHost: String = Constants.ALL) = {
    Map(Constants.SPAN_NAME -> spanname,
      Constants.LOCAL_HOST -> localHost,
      Constants.REMOTE_APPKEY -> remoteAppKey,
      Constants.REMOTE_HOST -> remoteHost)
  }

  private def updateInstance(metricData: MetricData, appkey: String, ts: Int, env: StatEnv, source: StatSource,
                             range: StatRange, group: StatGroup, perfProtocolType: PerfProtocolType, tags: Map[String, String]) = {
    val instanceKey = new InstanceKey(appkey, ts, env, source, range, group, perfProtocolType, tags)
    val instance = MetricManager.getInstance(instanceKey)
    instance.update(metricData)
  }

  /**
   *
   * @param key metric key
   * @return 构造出查询维度与tags的映射关系
   */
  private def constructGroupToTag(key: MetricKey): Seq[(StatGroup, Map[String, String])] = {
    val spanname = key.spanname
    val localhost = key.localHost
    val remoteAppKey = key.remoteAppKey
    val remoteHost = key.remoteHost

    // Span
    val tagsSpan = getTags(spanname = spanname)
    val tagsAll = getTags()

    // SpanRemoteApp
    val tagsSpanRemoteApp = getTags(spanname = spanname, remoteAppKey = remoteAppKey)
    val tagsSpanAllRemoteApp = getTags(remoteAppKey = remoteAppKey)

    // SpanLocalHost
    val tagsSpanLocalhost = getTags(spanname = spanname, localHost = localhost)
    val tagsSpanAllLocalhost = getTags(localHost = localhost)

    // SpanRemoteHost
    val tagsSpanRemoteHost = getTags(spanname = spanname, remoteHost = remoteHost)
    val tagsSpanAllRemoteHost = getTags(remoteHost = remoteHost)

    //    // LocalHostRemoteHost
    //    val tagsLocalHostRemoteHost = getTags(localHost = localhost, remoteHost = remoteHost)
    //    val tagsLocalHostRemoteHostAll = getTags(localHost = localhost)
    //    val tagsLocalHostAllRemoteHost = getTags(remoteHost = remoteHost)

    // LocalHostRemoteApp
    val tagsLocalHostRemoteApp = getTags(localHost = localhost, remoteAppKey = remoteAppKey)
    val tagsLocalHostRemoteAppAll = getTags(localHost = localhost)
    val tagsLocalHostAllRemoteApp = getTags(remoteAppKey = remoteAppKey)

    // RemoteHostRemoteApp
    val tagsRemoteHostRemoteApp = getTags(remoteAppKey = remoteAppKey, remoteHost = remoteHost)
    val tagsRemoteHostRemoteAppAll = getTags(remoteHost = remoteHost)
    val tagsRemoteHostAllRemoteApp = getTags(remoteAppKey = remoteAppKey)

    var ret = Seq[(StatGroup, Map[String, String])]()

    if (MetricSwitch.isOpen(StatGroup.Span)) {
      ret = ret ++ Seq(
        StatGroup.Span -> tagsSpan,
        StatGroup.Span -> tagsAll)
    }

    if (MetricSwitch.isOpen(StatGroup.SpanRemoteApp)) {
      ret = ret ++ Seq(
        StatGroup.SpanRemoteApp -> tagsSpanRemoteApp,
        StatGroup.SpanRemoteApp -> tagsSpanAllRemoteApp,
        StatGroup.SpanRemoteApp -> tagsSpan,
        StatGroup.SpanRemoteApp -> tagsAll)
    }

    if (MetricSwitch.isOpen(StatGroup.SpanLocalHost)) {
      ret = ret ++ Seq(
        // 在falcon中存储endpoint不能为all，需要在export中特殊处理后
        StatGroup.SpanLocalHost -> tagsSpanLocalhost,
        StatGroup.SpanLocalHost -> tagsSpanAllLocalhost,
        StatGroup.SpanLocalHost -> tagsSpan,
        StatGroup.SpanLocalHost -> tagsAll
      )
    }

    if (MetricSwitch.isOpen(StatGroup.SpanRemoteHost)) {
      ret = ret ++ Seq(
        StatGroup.SpanRemoteHost -> tagsSpanRemoteHost,
        StatGroup.SpanRemoteHost -> tagsSpanAllRemoteHost,
        StatGroup.SpanRemoteHost -> tagsSpan,
        StatGroup.SpanRemoteHost -> tagsAll)
    }

    if (MetricSwitch.isOpen(StatGroup.LocalHostRemoteApp)) {
      ret = ret ++ Seq(
        StatGroup.LocalHostRemoteApp -> tagsLocalHostRemoteApp,
        StatGroup.LocalHostRemoteApp -> tagsLocalHostRemoteAppAll,
        StatGroup.LocalHostRemoteApp -> tagsLocalHostAllRemoteApp,
        StatGroup.LocalHostRemoteApp -> tagsAll
      )
    }

    if (MetricSwitch.isOpen(StatGroup.RemoteAppRemoteHost)) {
      ret = ret ++ Seq(StatGroup.RemoteAppRemoteHost -> tagsRemoteHostRemoteApp,
        StatGroup.RemoteAppRemoteHost -> tagsRemoteHostRemoteAppAll,
        StatGroup.RemoteAppRemoteHost -> tagsRemoteHostAllRemoteApp,
        StatGroup.RemoteAppRemoteHost -> tagsAll)
    }
    ret
  }
}*/
