package com.sankuai.octo.statistic.metric

import java.util.concurrent.TimeUnit._

import akka.actor.{Actor, ActorLogging, ActorRef}
import akka.pattern.pipe
import com.meituan.jmonitor.JMonitor
import com.sankuai.octo.statistic.domain._
import com.sankuai.octo.statistic.helper.TimeProcessor
import com.sankuai.octo.statistic.metrics.SimpleCountHistogram2
import com.sankuai.octo.statistic.model._
import com.sankuai.octo.statistic.processor.ExportService
import com.sankuai.octo.statistic.util.ExecutionContextFactory
import com.sankuai.octo.statistic.util.TagUtil._

import scala.collection.mutable
import scala.concurrent.duration.Duration
import scala.concurrent.{ExecutionContextExecutor, Future}

class GroupRangeActor(key: InstanceKey2, groupRange: StatGroupRange, exporter: ActorRef) extends Actor with ActorLogging {

  import com.sankuai.octo.statistic.metric.GroupRangeActor._

  private val group = groupRange.group

  private val range = groupRange.range

  private val exportInstanceTask = {
    //  根据时间粒度确定导出时间间隔
    val interval = range match {
      case StatRange.Day => Duration(1, HOURS)
      case StatRange.Hour => Duration(30, MINUTES)
      case StatRange.Minute => Duration(80, SECONDS)
    }
    context.system.scheduler.schedule(interval, interval, self, ExportInstance)
  }
  private var map = mutable.Map[GroupKey, Instance2]()

  @throws[Exception](classOf[Exception])
  override def postStop(): Unit = {
    //  故障时导出数据
    exportAction()
    exportInstanceTask.cancel()
    super.postStop()
  }

  override def receive: Receive = {

    case metric: Metric2 => processMetric(metric)

    case ExportInstance => exportAction()

  }

  private def processMetric(metric: Metric2) = {
    val tags = constructGroupToTag(metric, group)
    tags.foreach { tag =>
      metric.data.foreach { metricData =>
        val minuteTs = metricData.timeStatus.minuteTs
        val ts = range match {
          case StatRange.Day => TimeProcessor.getDayStart(minuteTs)
          case StatRange.Hour => TimeProcessor.getHourStart(minuteTs)
          case StatRange.Minute => minuteTs
        }
        val groupKey = GroupKey(ts, range, group, tag)
        updateInstance(key, groupKey, metricData)
      }
    }
  }

  private def exportAction() = {
    map.foreach { case (groupKey, instance) =>
      Future {
        val appkey = instance.appkey
        if (isExportAppkey(appkey)) {
          JMonitor.kpiForCount("statistic.groupRange.count")
          val hashCode = instance.getGroupKey.statTag.hashCode().abs
          ExportService(appkey, hashCode).sendGroupRangeData(asInstance3(instance))
        } else {
          instance.export()
        }
      }.pipeTo(exporter)
      //  删除已过期数据,避免内存泄露
      handleExpire(groupKey)
    }
  }

  private def asInstance3(instance: Instance2): Instance3 = {
    instance.updateCreateTime(System.currentTimeMillis())
    val instanceKey2 = instance.getInstanceKey
    val groupKey2 = instance.getGroupKey
    val statTag2 = groupKey2.statTag
    val infraName = statTag2.infraName
    val statTag: StatTag3 = new StatTag3(statTag2.spanname, statTag2.localHost, statTag2.remoteHost, statTag2.remoteAppKey, infraName)
    val key: InstanceKey3 = new InstanceKey3(instanceKey2.appKey, instanceKey2.env, instanceKey2.source, instanceKey2.perfProtocolType)
    val groupKey: GroupKey3 = new GroupKey3(groupKey2.ts, groupKey2.range, groupKey2.group, statTag)
    val histogram: SimpleCountHistogram3 = asHistogram3(instance.getHistogram)
    new Instance3(key, groupKey, histogram)
  }

  private def asHistogram3(histogram: SimpleCountHistogram2): SimpleCountHistogram3 = {
    val reservoir2 = histogram.getReservoir
    val reservoir3 = new SimpleCountReservoir3(reservoir2.getMax, reservoir2.getValues)
    new SimpleCountHistogram3(histogram.getCount, histogram.getSuccessCount,
      histogram.getExceptionCount, histogram.getTimeoutCount, histogram.getDropCount,
      histogram.getHTTP2XXCount, histogram.getHTTP3XXCount, histogram.getHTTP4XXCount, histogram.getHTTP5XXCount,
      histogram.getVersion, histogram.getCreateTime, histogram.getUpdateTime, reservoir3)
  }

  private def handleExpire(groupKey: GroupKey) = {
    if (isExpired(groupKey)) {
      map -= groupKey
    }
  }


  private def updateInstance(key: InstanceKey2, groupKey: GroupKey, metricData: MetricData2) = {
    val instance = map.getOrElseUpdate(groupKey, createInstance(key, groupKey))
    val status = metricData.timeStatus.status
    metricData.costToCount.foreach { case (cost, count) =>
      instance.update(cost, count, status)
    }
  }

  private def createInstance(key: InstanceKey2, groupKey: GroupKey) = {
    val instance = new Instance2(key, groupKey, new SimpleCountHistogram2())
    instance
  }
}

object GroupRangeActor {

  private implicit lazy val ec: ExecutionContextExecutor = ExecutionContextFactory.build(20)

  case object ExportInstance

  /**
   *
   * @param  metric key
   * @return 构造出查询维度与tags的映射关系
   */
  private def constructGroupToTag(metric: Metric2, group: StatGroup): Seq[StatTag] = {
    val key = metric.key
    val spanname = key.spanname
    val localhost = key.localHost
    val remoteAppKey = key.remoteAppKey
    val remoteHost = key.remoteHost

    implicit val infraName = key.infraName

    val groupMappings: Seq[StatTag] = group match {
      case StatGroup.SpanLocalHost =>
        // SpanLocalHost
        val tagsSpanLocalhost = getStatTag(spanname = spanname, localHost = localhost)
        val tagsSpanAllLocalhost = getStatTag(localHost = localhost)
        // 在falcon中存储endpoint不能为all，需要在export中特殊处理后
        Seq(tagsSpanLocalhost,
          tagsSpanAllLocalhost)

      case StatGroup.SpanRemoteApp =>
        // SpanRemoteApp
        val tagsSpanRemoteApp = getStatTag(spanname = spanname, remoteAppKey = remoteAppKey)
        val tagsSpanAllRemoteApp = getStatTag(remoteAppKey = remoteAppKey)
        Seq(tagsSpanRemoteApp,
          tagsSpanAllRemoteApp)

      case StatGroup.Span =>
        val tagsSpan = getStatTag(spanname = spanname)
        val tagsAll = getStatTag()
        Seq(tagsSpan, tagsAll)

      case StatGroup.SpanRemoteHost =>
        // SpanRemoteHost
        val tagsSpanRemoteHost = getStatTag(spanname = spanname, remoteHost = remoteHost)
        val tagsSpanAllRemoteHost = getStatTag(remoteHost = remoteHost)
        Seq(tagsSpanRemoteHost,
          tagsSpanAllRemoteHost)

      case StatGroup.LocalHostRemoteHost =>
        // LocalHostRemoteHost
        val tagsLocalHostRemoteHost = getStatTag(localHost = localhost, remoteHost = remoteHost)
        Seq(tagsLocalHostRemoteHost)
      case StatGroup.LocalHostRemoteApp =>
        // LocalHostRemoteApp
        val tagsLocalHostRemoteApp = getStatTag(localHost = localhost, remoteAppKey = remoteAppKey)
        Seq(tagsLocalHostRemoteApp)
      case StatGroup.RemoteAppRemoteHost =>
        // RemoteHostRemoteApp
        val tagsRemoteHostRemoteApp = getStatTag(remoteAppKey = remoteAppKey, remoteHost = remoteHost)
        Seq(tagsRemoteHostRemoteApp)
    }
    groupMappings
  }


  /**
   *
   * @param groupKey 查询组的key
   * @return 如果数据已经过期返回 true,否则 false
   */
  private def isExpired(groupKey: GroupKey): Boolean = {
    val nowTime = (System.currentTimeMillis() / 1000L).toInt
    (nowTime - groupKey.ts) >= groupKey.range.getLifetime
  }

  def isExportAppkey(appkey: String): Boolean = ExportService.isExportAppkey(appkey)
}
