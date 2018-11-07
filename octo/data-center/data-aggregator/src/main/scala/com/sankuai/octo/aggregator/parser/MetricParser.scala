package com.sankuai.octo.aggregator.parser

import java.util.concurrent.{ConcurrentLinkedQueue, Executors, TimeUnit}

import com.meituan.jmonitor.JMonitor
import com.sankuai.octo.aggregator.MafkaService
import com.sankuai.octo.aggregator.processor.StatisticService
import com.sankuai.octo.aggregator.utils.ConvertAppkey
import com.sankuai.octo.statistic.model._
import com.sankuai.octo.statistic.util.{ExecutorFactory, HessianSerializer, StatThreadFactory}
import org.slf4j.LoggerFactory

import scala.collection.JavaConversions._

object MetricParser {
  val asyncMetricSender = new ExecutorFactory(sendMetrics, "MetricParser.asyncMetricSender", 8, 200, 400)
  private val logger = LoggerFactory.getLogger(MetricParser.getClass)
  private val scheduler = Executors.newSingleThreadScheduledExecutor(StatThreadFactory.threadFactory(this.getClass))

  private final val METRIC_LENGTH = 100

  private var listCache = new ConcurrentLinkedQueue[MetricStruct]()

  private var errorCount = 0

  private val timerTask = new Runnable {

    override def run(): Unit = {
      /** 定时任务只负责构造发送数据,控制发送长度由其他处理器控制,解耦 */
      if (listCache.nonEmpty) {
        try {
          var tmpList = List[MetricStruct]()
          listCache.synchronized {
            tmpList = listCache.toList
            listCache = new ConcurrentLinkedQueue[MetricStruct]()
          }

          val tmpData = tmpList.groupBy(_.key).map {
            x => x._1 -> x._2.map(_.data)
          }.toList

          // 根据appKey聚合数据
          tmpData.groupBy(_._1.appkey).foreach(entry => {
            logger.debug(s"appkey:${entry._1},size:${entry._2.size}")
            val list = entry._2
            if (list != null && list.nonEmpty) {
              putMetrics(list)
            }
          })
        } catch {
          case e: Exception => logger.error("MetricParser putMetric Fail", e)
        }
      }
    }

    private def putMetrics(metricList: List[(MetricKey, List[MetricData])]): Unit = {
      val metrics = metricList.flatMap(tuple => {
        val list = tuple._2.toList
        list.grouped(METRIC_LENGTH).toList.flatMap(subList => {
          val list = subList.filter(_ != null)
          if (list.isEmpty) {
            None
          } else {
            Some(new Metric(tuple._1, list))
          }
        })
      })
      logger.debug(s"putMetrics ,size:${metrics.size}")
      try {
        if (metrics != null && metrics.nonEmpty) {
          asyncMetricSender.submit(metrics)
        }
      } catch {
        case e: Exception => logger.error(s"putMetrics Fail,metricList:$metricList", e)
      }
    }
  }

  {
    // 频率较低，保证数据经过聚合
    scheduler.scheduleAtFixedRate(timerTask, 5, 5, TimeUnit.SECONDS)

    /** 在jvm退出时优雅关闭 */
    Runtime.getRuntime.addShutdownHook(new Thread() {
      override def run(): Unit = {
        scheduler.submit(timerTask)
        scheduler.shutdown()
        scheduler.awaitTermination(10, TimeUnit.SECONDS)
      }
    })
  }

  def put(metricStruct: MetricStruct) {
    if (metricStruct.data != null) {
      listCache.offer(metricStruct)
      val metricKey = metricStruct.key
      if(ConvertAppkey.needConvert(metricKey.remoteAppKey) && metricKey.source.equals(StatSource.Client)){
        val key = new MetricKey(metricKey.remoteAppKey, metricKey.spanname, metricKey.remoteHost, metricKey.appkey, metricKey.localHost, StatSource.Server, metricKey.perfProtocolType, metricKey.infraName)
        val data = metricStruct.data
        val converMetricStruct = MetricStruct(key, data)
        logger.debug(s"conver metricstruct ${key} ")
        listCache.offer(converMetricStruct)
      }
    }
  }

  def sendMetrics(metrics: List[Metric]) {
    //避免一次发送长度过大,分割成子列表发送
    if (metrics != null && metrics.nonEmpty) {
      val appkey = metrics.head.key.appkey
      metrics.grouped(METRIC_LENGTH).toList.foreach(subList => {
        try {
          JMonitor.kpiForCount("logCollector.minute.out")
          //双写
          if(MafkaService.isSentToMafka(appkey)){
            val message = HessianSerializer.serialize(new MetricList("hessian", subList))
            MafkaService.getMafkaProducer(appkey).sendAsyncMessage(message)
          }
          StatisticService(appkey).sendMetrics(subList)
        } catch {
          case e: Exception =>
            errorCount += 1
            if(errorCount == 100){
              logger.error(s"send metrics failed. ${StatisticService.getStatisticServerList(appkey)}", e)
              errorCount = 0
            }
            JMonitor.kpiForCount("logCollector.minute.sendMetrics.failed")
        }
      })
    }
  }

  def listCacheLen() = {
    listCache.size()
  }
}

case class MetricStruct(key: MetricKey, data: MetricData)
