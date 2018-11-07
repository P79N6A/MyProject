package com.sankuai.octo.statistic.metric.actor

import java.util.concurrent.TimeUnit._

import akka.actor.{Actor, ActorLogging, Props}
import com.meituan.mtrace.thrift.model.StatusCode
import com.sankuai.octo.statistic.domain._
import com.sankuai.octo.statistic.helper.TimeProcessor
import com.sankuai.octo.statistic.metric.AppKeyReceiveCountActor.AppKeyMetricCode
import com.sankuai.octo.statistic.metric.{AppKeyListActor, AppKeyReceiveCountActor, MetricProcessor2}
import com.sankuai.octo.statistic.model.{Metric, MetricKey, PerfProtocolType, StatSource}
import com.sankuai.octo.statistic.util.ExecutionContextFactory
import org.apache.commons.lang3.StringUtils

import scala.collection.mutable
import scala.concurrent.ExecutionContextExecutor
import scala.concurrent.duration.Duration

/**
 * Created by wujinwu on 16/5/20.
 */
class MetricPreProcessor extends Actor with ActorLogging {

  import MetricPreProcessor._

  private val metricProcessor2 = context.actorOf(Props[MetricProcessor2](), "metricProcessor2")
  private val appKeyListActor = context.actorOf(Props[AppKeyListActor](), "appKeyListActor")
  private val appKeyReceiveCountActor = context.actorOf(Props[AppKeyReceiveCountActor](), "appKeyReceiveCountActor")

  private val exportMetricTask = {
    val interval = Duration(5, SECONDS)
    context.system.scheduler.schedule(interval, interval, self, ExportMetric)
  }
  private var buffer = mutable.Map[MetricKey2, mutable.Map[TimeStatus, mutable.Map[Int, Int]]]()

  @scala.throws[Exception](classOf[Exception])
  override def postStop(): Unit = {
    exportMetricAction()
    exportMetricTask.cancel()
    super.postStop()
  }

  private var count = 0

  override def receive: Receive = {
    //  兼容旧RPC接口
    case metric: Metric => preprocessMetric(handleMetric(metric))

    case ExportMetric => exportMetricAction()
    //  新的akka netty接口
    case metrics: Metrics => preprocessNewMetrics(metrics)
  }

  private def preprocessNewMetrics(metrics: Metrics) = {
    val nowSeconds = (System.currentTimeMillis() / 1000L).toInt
    val newMetrics = metrics.metrics.map { metric =>
      val newDataList = metric.data.filter { data =>
        nowSeconds - data.timeStatus.minuteTs <= 100
      }
      if (newDataList.size != metric.data.size) {
        Metric2(metric.key, newDataList)
      } else {
        metric
      }
    }
    metricProcessor2 ! Metrics(newMetrics)
  }

  private def preprocessMetric(metric: Metric) = {
    //  发送统计数据
    metricStat(metric)

    filterAndInsertToBuffer(metric)

    count += 1
    if (count > 500000) {
      //  采样获取队列长度,优化
      log.info(s"receive metric ${metric.key} data size:${metric.data}")
      count = 0
    }
  }

  private def filterAndInsertToBuffer(metric: Metric) = {
    val metricKey2 = getCacheMetricKey(metric.key)
    val metricKeyMap = buffer.getOrElseUpdate(metricKey2, mutable.Map())
    val nowSeconds = (System.currentTimeMillis() / 1000L).toInt
    val iter = metric.data.iterator()
    while (iter.hasNext) {
      val metricData = iter.next()
      val minuteTs = TimeProcessor.getMinuteStart((metricData.start / 1000L).toInt)
      //  超过时间边界的性能数据,drop,避免污染HBase数据
      if (nowSeconds - minuteTs <= 100) {
        val status = if (metricData.status == null) {
          StatusCode.SUCCESS
        } else {
          metricData.status
        }
        val timeStatus = TimeStatus(minuteTs, status)
        val cost2CountMap = metricKeyMap.getOrElseUpdate(timeStatus, mutable.Map())
        val oldCount = cost2CountMap.getOrElse(metricData.cost, 0)
        cost2CountMap += metricData.cost -> (oldCount + metricData.count)
      }
    }

  }

  private def metricStat(metric: Metric) = {
    val appkey = metric.key.appkey
    appKeyListActor ! appkey
    val code = metric.key.hashCode()
    appKeyReceiveCountActor ! new AppKeyMetricCode(appkey, code)
  }

  private def exportMetricAction() = {
    //  根据时间维度划分数据,通过range指定
    if (buffer.nonEmpty) {
      exportMetrics(buffer)
      buffer = mutable.Map()
    }
  }

  private def exportMetrics(buffer: mutable.Map[MetricKey2, mutable.Map[TimeStatus, mutable.Map[Int, Int]]]) = {
    val metrics = transformMetrics(buffer)
    metricProcessor2 ! metrics
  }

  private def transformMetrics(buffer: mutable.Map[MetricKey2, mutable.Map[TimeStatus, mutable.Map[Int, Int]]]) = {
    val newMetrics = buffer.map { case (metricKey2, timeStatusMap) =>
      val dataList = timeStatusMap.map { case (timeStatus, cost2CountMap) =>
        MetricData2(timeStatus, cost2CountMap.toMap)
      }
      Metric2(metricKey2, dataList)
    }
    Metrics(newMetrics)
  }


}

object MetricPreProcessor {

  private implicit lazy val ec: ExecutionContextExecutor = ExecutionContextFactory.build(1)

  case object ExportMetric

  private var metricKeyMap = Map[MetricKey, MetricKey2]()

  private def getCacheMetricKey(key: MetricKey) = {
    metricKeyMap.getOrElse(key, createKey(key))
  }

  private def createKey(key: MetricKey) = {
    val metricKey2 = MetricKey2(key.appkey, key.spanname, key.localHost,
      key.remoteAppKey, key.remoteHost, key.source, key.perfProtocolType, key.infraName)
    metricKeyMap += key -> metricKey2
    metricKey2
  }

  private def handleMetric(metric: Metric) = {
    //  清理数据类型
    if (metric.key.perfProtocolType == null) {
      metric.key.perfProtocolType = PerfProtocolType.THRIFT
    }

    //  统一数据来源
    unifySource(metric.key)
    //  驻留关键字段
    internMetric(metric)

    metric
  }

  private def unifySource(key: MetricKey): Unit = {
    //  source统一为client or server
    val source = key.source match {
      case StatSource.Server | StatSource.ServerDrop | StatSource.ServerSlow | StatSource.ServerFailure => StatSource.Server
      case _ => StatSource.Client
    }
    key.source = source
  }

  /**
   * 将频繁出现的字符串缓存至常量池,避免重复占用内存
   *
   * @param metric metric
   */
  private def internMetric(metric: Metric): Unit = {
    metric.key.appkey = metric.key.appkey.intern()
    metric.key.spanname = metric.key.spanname.intern()
    metric.key.localHost = metric.key.localHost.intern()
    metric.key.remoteAppKey = metric.key.remoteAppKey.intern()
    metric.key.remoteHost = metric.key.remoteHost.intern()
    metric.key.infraName = if (StringUtils.isBlank(metric.key.infraName) || metric.key.infraName.equals("N/A")) {
      "mtthrift"
    } else {
      metric.key.infraName
    }.intern()
  }

}