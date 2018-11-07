package com.sankuai.octo.export.histogram

import java.util.concurrent.TimeUnit._
import java.util.concurrent.atomic.AtomicInteger

import akka.actor.{Actor, ActorLogging, ActorRef}
import akka.pattern.pipe
import com.meituan.jmonitor.JMonitor
import com.sankuai.octo.statistic.domain._
import com.sankuai.octo.statistic.model.StatRange
import com.sankuai.octo.statistic.util.ExecutionContextFactory
import org.slf4j.LoggerFactory

import scala.collection.mutable
import scala.concurrent.duration.Duration
import scala.concurrent.{ExecutionContextExecutor, Future}

/**
 * 处理分钟，小时维度的数据
 */
class GroupRangeHistogramProcessor(groupRange: StatGroupRange, exporter: ActorRef) extends Actor with ActorLogging {

  import GroupRangeHistogramProcessor._

  private val map = mutable.Map[TimeGroupKey, Instance2]()

  private val range = groupRange.range

  //  根据时间粒度确定导出时间间隔
  val interval = range match {
    case StatRange.Hour => Duration(30, MINUTES)
    case StatRange.Minute => Duration(80, SECONDS)
  }
  private val exportInstanceTask = {
    context.system.scheduler.schedule(interval, interval, self, ExportInstance)
  }

  @throws[Exception](classOf[Exception])
  override def postStop(): Unit = {
    //  故障时导出数据
    exportAction()
    exportInstanceTask.cancel()
    super.postStop()
  }

  override def receive: Receive = {
    case instance: Instance2 => processInstance2(instance)
    case ExportInstance => exportAction()
  }

  val mapExecutionContext = ExecutionContextFactory.build(20)

  private def exportAction() = {
    logger.debug(s"export GroupRangeHistogram start size:${map.size}")
    if (map.nonEmpty) {
      JMonitor.kpiForCount("dataExport.range.histogram.total", map.size)
      map.foreach { case (timeGroupKey, instance2) =>
        Future {
          val data = instance2.export()
          val tags = timeGroupKey.getGroupKey.statTag
          if (tags.spanname.equals("all") && tags.localHost.equals("all")
            && tags.remoteHost.equals("all") && tags.remoteAppKey.equals("all")) {
            log.info(s"export:${timeGroupKey} ,${data.toString}")
          }
          data
        }(mapExecutionContext).pipeTo(exporter)

        map -= timeGroupKey

      }
      JMonitor.kpiForCount("dataExport.range.histogram_export", map.size)
      logger.debug(s"export GroupRangeHistogram end,size:${map.size}")
    }
  }

  private def processInstance2(instance: Instance2) = {
    val createTime = instance.getHistogram.getCreateTime
    val int_create_time = (createTime / 1000).toInt
    val timeSeq = (int_create_time / interval.toSeconds).toInt
    val groupKey = instance.getGroupKey
    //避免多次传入
    val nowSeconds = (System.currentTimeMillis() / 1000L).toInt
    if (nowSeconds - groupKey.ts - range.getTimeRange <= interval.toSeconds * 3) {
      val instanceKey = instance.getInstanceKey
      val timeGroupKey = new TimeGroupKey(timeSeq, groupKey, instanceKey)
      val instance2 = map.get(timeGroupKey)
      val newInstance2 = if (instance2.isDefined) {
        mergeInstance2(instance2.get, instance)
      } else {
        instance
      }
      map.put(timeGroupKey, newInstance2)
      val tags = groupKey.statTag
      if (tags.spanname.equals("all") && tags.localHost.equals("all")
        && tags.remoteHost.equals("all") && tags.remoteAppKey.equals("all")) {
        log.info(s"${instance.getClientIp},timeGroupKey:${timeGroupKey},groupKey:${groupKey},data:${instance.export()}")
      }
    }

  }

  private def mergeInstance2(instance2Old: Instance2, instance2: Instance2): Instance2 = {
    val histogram2 = instance2Old.getHistogram.merge(instance2.getHistogram)
    new Instance2(instance2Old.getInstanceKey, instance2Old.getGroupKey, histogram2)
  }




}

object GroupRangeHistogramProcessor {

  private val logger = LoggerFactory.getLogger(this.getClass)
  private implicit lazy val ec: ExecutionContextExecutor = ExecutionContextFactory.build(1)

  case object ExportInstance

}