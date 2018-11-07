package com.sankuai.octo.export.exporter

import java.util.concurrent.TimeUnit._

import akka.actor.{Actor, ActorLogging}
import com.sankuai.octo.export.StatConstants
import com.sankuai.octo.export.util.Falcon
import com.sankuai.octo.statistic.model.StatData
import com.sankuai.octo.export.util.Falcon.FalconData
import com.sankuai.octo.statistic.util.{ExecutionContextFactory}
import org.slf4j.LoggerFactory

import scala.collection.mutable.ArrayBuffer
import scala.concurrent.duration.Duration
import scala.concurrent.{Future, ExecutionContextExecutor}

/**
  * Created by wujinwu on 16/5/27.
  */
class FalconExporterActor extends Actor with ActorLogging {

  private var list = ArrayBuffer[StatData]()

  import FalconExporterActor._

  private val exportFalconTask = {
    val interval = Duration(30, SECONDS)
    context.system.scheduler.schedule(interval, interval, self, ExportFalcon)
  }

  @scala.throws[Exception](classOf[Exception])
  override def postStop(): Unit = {
    //  故障时导出数据
    exportFalconAction()
    exportFalconTask.cancel()
    super.postStop()
  }

  override def receive: Receive = {
    case statData: StatData => list += statData
    case ExportFalcon => exportFalconAction()
  }

  private def exportFalconAction() = {
    if (list.size != 0) {
      exportToFalcon(list)
      list = ArrayBuffer()
    }
  }
}

object FalconExporterActor {

  private val logger = LoggerFactory.getLogger(this.getClass)

  private implicit lazy val ec: ExecutionContextExecutor = ExecutionContextFactory.build(1)

  case object ExportFalcon

  val futureExecutionContext =  ExecutionContextFactory.build()
  private def exportToFalcon(list: Iterable[StatData]) = {
    val subLists = list.grouped(StatConstants.groupLength)
    subLists.foreach { sub =>
      Future {
        val sendList = sub.flatMap(statToFalconDataList)
        if (sendList.size != 0) {
          sendToFalcon(sendList)
        }
      }(futureExecutionContext)
    }
  }

  private def statToFalconDataList(statData: StatData) = {
    Falcon.statToFalconData(statData)

  }

  private def sendToFalcon(falconDataList: Iterable[FalconData]): Unit = {
    val start = System.currentTimeMillis()
    Falcon.send(falconDataList)
    logger.debug(s"Falcon send size ${falconDataList.size}, time: ${System.currentTimeMillis() - start}")
  }

}
