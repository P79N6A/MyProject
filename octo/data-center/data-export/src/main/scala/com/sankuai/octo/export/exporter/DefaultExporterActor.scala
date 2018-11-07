package com.sankuai.octo.export.exporter

import akka.actor.{Actor, ActorLogging, Props, Status}
import com.sankuai.octo.export.StatConstants
import com.sankuai.octo.statistic.model._
import org.slf4j.LoggerFactory

import scala.compat.Platform._

class DefaultExporterActor extends Actor with ActorLogging {

  import DefaultExporterActor._

  private val falconExporter = context.actorOf(Props[FalconExporterActor](), "falconExporterActor")
  private val tagExporter = context.actorOf(Props[TagActor]().withDispatcher("custom.tag-dispatcher"), "tagActor")
  private val hbaseExporterActor = context.actorOf(Props[HBaseExporterActor](), "hbaseExporterActor")

  override def receive: Receive = {
    case statData: StatData => doExportStatData(statData)

    case Status.Failure(ex) => failAction(ex)
  }


  private def doExportStatData(statData: StatData): Unit = {
    hbaseExporterActor ! statData
    //  数据导出至 Falcon
    statData.getRange match {
      case StatRange.Minute =>
        //  从导出的stat提取出tag信息
        tagExporter ! statData

        //  分钟级别数据目前导出到Falcon
        //  判断是否需要导出至falcon
        if (expired(statData) && judgeByGroup(statData)) {
          falconExporter ! statData
        }
      //  测量log的timeout跟数据冗余度关系
      //        printLog(statData)
      case _ =>
    }
  }

  private def failAction(ex: Throwable) = {
    //    log.error("statData fail", ex)
    logger.error("statData fail", ex)
  }

  /**
   *
   * @param statData 统计数据
   * @return 判断是否需要导出至falcon
   */
  private def judgeByGroup(statData: StatData) = {
    statData.getGroup match {
      case StatGroup.SpanLocalHost | StatGroup.SpanRemoteApp | StatGroup.Span => true
      case _ => false
    }
  }

  /**
   *
   * @param statData 要导出的statData
   * @return 如果数据已经过期返回 true,否则 false
   */
  private def expired(statData: StatData): Boolean = {
    val nowTime = (System.currentTimeMillis() / 1000L).toInt
    (nowTime - statData.getTs) >= statData.getRange.getLifetime
  }


  /*
    private def printLog(statData: StatData) = {
      //  添加log测量数据的多次复写的冗余度
      def dataTimeout = config.get("data_timeout", "90").toInt
      if (logger.isDebugEnabled) {
        val now = (System.currentTimeMillis() / 1000L).toInt
        if (now - statData.getTs > dataTimeout) {
          logger.debug(s"now:$now,${statData.getAppkey},${statData.getTs},${statData.getEnv}," +
            s"${statData.getSource},${statData.getRange},${statData.getGroup},${statData.getTags},${statData.getCount}")
        }
      }
    }
  */

}

object DefaultExporterActor {

  private val logger = LoggerFactory.getLogger(this.getClass)

}
