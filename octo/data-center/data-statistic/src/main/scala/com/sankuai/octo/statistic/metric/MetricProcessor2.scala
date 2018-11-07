package com.sankuai.octo.statistic.metric

import akka.actor.{Actor, ActorLogging, ActorRef, Props, Status}
import com.sankuai.octo.statistic.StatConstants
import com.sankuai.octo.statistic.domain.{InstanceKey2, Metric2, Metrics}
import com.sankuai.octo.statistic.metric.actor.DailyMetricProcessor2
import com.sankuai.octo.statistic.model._

import scala.collection.mutable

/**
 * Created by wujinwu on 16/5/20.
 */
class MetricProcessor2 extends Actor with ActorLogging {

  import MetricProcessor2._

  private val dailyProcessorMap = mutable.Map[AppkeySource, ActorRef]()
  private val metricManagers = mutable.Map[InstanceKey2, ActorRef]()

  private val exporter = context.actorOf(Props[DefaultExporterActor](), "defaultExporterActor")

  override def receive: Receive = {
    case Metrics(metrics) =>

      update(metrics)
      // 暂时对于 天 粒度特殊处理, 后续聚合后可以删除
      processDailyMetric(metrics)

    case Status.Failure(ex) => log.error("MetricProcessor2 recv Failure,msg:{}", ex.getMessage)
  }

  def update(metrics: Iterable[Metric2]) = {
    metrics.foreach { metric =>
      val key = InstanceKey2(metric.key.appkey, StatConstants.env, metric.key.source, metric.key.perfProtocolType)
      val manager = metricManagers.getOrElseUpdate(key, createMetricManager(key))
      manager ! metric
    }
  }

  private def processDailyMetric(metrics: Iterable[Metric2]) = {
    metrics.foreach { metric =>
      //  source统一为client or server
      val source = metric.key.source match {
        case StatSource.Server | StatSource.ServerDrop | StatSource.ServerSlow | StatSource.ServerFailure => StatSource.Server
        case _ => StatSource.Client
      }
      val appkeySource = AppkeySource(metric.key.appkey, source)
      val dailyProcessor = dailyProcessorMap.getOrElseUpdate(appkeySource, createDailyMetricProcessor(appkeySource))
      dailyProcessor ! metric
    }

  }


  private def createDailyMetricProcessor(appkeySource: AppkeySource) = {
    val actorRef = context.actorOf(Props(classOf[DailyMetricProcessor2], appkeySource).withDispatcher("custom.daily-dispatcher"))
    actorRef
  }


  private def createMetricManager(key: InstanceKey2) = {
    val m = context.actorOf(Props(classOf[MetricManager2], key, exporter))
    m
  }
}

object  MetricProcessor2 {

  case class AppkeySource(appkey: String, source: StatSource)

}