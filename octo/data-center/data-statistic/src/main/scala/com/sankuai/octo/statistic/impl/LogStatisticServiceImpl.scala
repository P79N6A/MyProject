package com.sankuai.octo.statistic.impl

import java.util
import java.util.concurrent.atomic.AtomicInteger

import akka.actor.{ActorSystem, Props}
import com.meituan.jmonitor.JMonitor
import com.sankuai.octo.log.constant.RTLogConstant
import com.sankuai.octo.statistic.StatConstants
import com.sankuai.octo.statistic.metric.actor.MetricPreProcessor
import com.sankuai.octo.statistic.model._
import com.sankuai.octo.statistic.service.LogStatisticService
import com.typesafe.config.ConfigFactory
import org.apache.thrift.TException
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.util.CollectionUtils

import scala.collection.JavaConverters._

@Service
class LogStatisticServiceImpl extends LogStatisticService {
  private val logger = LoggerFactory.getLogger(this.getClass)
  private val system = startActorSystem()
  private val metricPreProcessor = system.actorOf(Props[MetricPreProcessor]().withDispatcher("custom.metric-dispatcher"), "metricPreProcessor")

  /**
   * 上报metrics
   *
   * @param metrics 上报的metric集合
   */
  @throws(classOf[TException])
  override def sendMetrics(metrics: util.List[Metric]) {
    JMonitor.kpiForCount("statistic.minute.metrics", metrics.size())
    JMonitor.kpiForCount("statistic.minute.metrics.data", metrics.asScala.map(_.data.size()).toList.sum)
    if (!CollectionUtils.isEmpty(metrics)) {
      val iter = metrics.iterator()
      while (iter.hasNext) {
        val metric = iter.next()
        if(StatConstants.alias_appkey){
          if ("waimai_api".equalsIgnoreCase(metric.key.appkey)) {
            metric.key.appkey = "waimai_api3"
          }
          if ("waimai_api".equalsIgnoreCase(metric.key.remoteAppKey)) {
            metric.key.remoteAppKey = "waimai_api3"
          }
        }
        metricPreProcessor ! metric
      }
    }
  }
  /**
   *  从队列里消费数据
   */
  @throws(classOf[TException])
  def sendMetricList(metricList: MetricList) {
    sendMetrics(metricList.getData)
  }

  private def startActorSystem() = {
    val conf = ConfigFactory.parseString("akka.remote.netty.tcp.hostname = " + RTLogConstant.localIP).
      withFallback(ConfigFactory.load())
    val system = ActorSystem("LogStatistic", conf)
    system
  }

  val count = new TimeCount()
  val log_count = new AtomicInteger()
  //当分钟的count值超过阈值的时候考虑降级
  private def dropMetric(size: Int): Boolean = {
    val oldCount = count.getCount.get()
    count.addAndGet(size)
    if (oldCount > StatConstants.metricLimit) {
      if (log_count.incrementAndGet() % 100 == 0) {
        logger.info(s"minuteCount:${count}")
        logger.error(s"统计限流，minuteCount:${count}")
        log_count.set(0)
      }
      if (StatConstants.debug_limit) false else true
    } else {
      false
    }
  }

}
