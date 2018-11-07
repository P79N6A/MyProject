package com.sankuai.octo.export.exporter

import java.util.concurrent.TimeUnit.SECONDS

import akka.actor.{Actor, ActorLogging}
import com.sankuai.octo.export.StatConstants
import com.sankuai.octo.statistic.helper.PerfHelper
import com.sankuai.octo.statistic.model._
import com.sankuai.octo.statistic.util.{ExecutionContextFactory, HBaseClient}
import org.slf4j.LoggerFactory

import scala.collection.mutable.ArrayBuffer
import scala.concurrent.duration.Duration
import scala.concurrent.{ExecutionContextExecutor, Future, blocking}

class HBaseExporterActor extends Actor with ActorLogging {

  import com.sankuai.octo.export.exporter.HBaseExporterActor._

  private val exportHBaseTask = {
    val interval = Duration(30, SECONDS)
    context.system.scheduler.schedule(interval, interval, self, ExportHBase)
  }
  private var list = ArrayBuffer[StatData]()

  @scala.throws[Exception](classOf[Exception])
  override def postStop(): Unit = {
    //  故障时导出数据
    exportHBaseAction()
    exportHBaseTask.cancel()
    super.postStop()
  }

  override def receive: Receive = {
    onlineReceive
  }

  private def exportHBaseAction() = {
    //  根据时间维度划分数据,通过range指定
    if (list.size != 0) {
      putByRange(list)
      list = ArrayBuffer()
    }
  }

  private val onlineReceive: Receive = {
    case statData: StatData => list += statData

    case ExportHBase => exportHBaseAction()
  }

  private val offlineReceive: Receive = {
    case statData: StatData => //  ignore

    case ExportHBase => //  ignore

  }

}

object HBaseExporterActor {

  private val logger = LoggerFactory.getLogger(this.getClass)

  private implicit lazy val ec: ExecutionContextExecutor = ExecutionContextFactory.build(1)

  val hbaseExecutionContext = ExecutionContextFactory.build()

  private def putByRange(list: Iterable[StatData]): Unit = {
    //  根据时间维度划分数据,通过range指定
    logger.debug(s"putByRange start,size:{}", list.size)
    val subLists = list.grouped(StatConstants.groupLength)
    subLists.foreach { sub =>
      Future {
        blocking {
          sub.groupBy(_.getRange).foreach { case (range, seqByRange) =>
            HBaseClient.putPerfDataList(seqByRange.map(PerfHelper.statDataToPerfData), range)
          }
        }
      }(hbaseExecutionContext)
    }
  }

  case object ExportHBase


}
