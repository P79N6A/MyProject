package com.sankuai.octo.statistic.metric

import java.util
import java.util.concurrent.TimeUnit._

import akka.actor.{Actor, ActorLogging}
import com.fasterxml.jackson.annotation.JsonProperty
import com.meituan.jmonitor.JMonitor
import com.sankuai.octo.statistic.helper.TimeProcessor
import com.sankuai.octo.statistic.util.ExecutionContextFactory

import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import scala.concurrent.ExecutionContextExecutor
import scala.concurrent.duration.Duration

/**
 * Created by wujinwu on 16/5/15.
 */

class AppKeyReceiveCountActor extends Actor with ActorLogging {


  import AppKeyReceiveCountActor._


  private var list = ListBuffer[AppKeyReceiveCount2]()

  var appKeyReceiveCount = new AppKeyReceiveCount(TimeProcessor.getMinuteStart((System.currentTimeMillis() / 1000L).toInt))

  private val timerTask = {
    val interval = Duration(1, MINUTES)
    context.system.scheduler.schedule(interval, interval, self, Timer)
  }

  @scala.throws[Exception](classOf[Exception])
  override def postStop(): Unit = {
    timerTask.cancel()
    super.postStop()
  }

  override def receive: Receive = {
    case appKeyMetricCode: AppKeyMetricCode =>
      val metricSet = appKeyReceiveCount.map.getOrElseUpdate(appKeyMetricCode.appkey,new java.util.HashSet[Integer](10000))
      if(!metricSet.contains(appKeyMetricCode.metricCode)){
        metricSet.add(appKeyMetricCode.metricCode)
        JMonitor.kpiForCount("statistic.metric_code", 1)
      }
    case Timer =>
      if (list.size >= 2) {
        list = list.dropRight(1)
      }
      val newResult = appKeyReceiveCount.map.map {
        item =>
          AppKeyCount(item._1, item._2.size())
      }.toList.sortBy(-_.count)
      val newElement = new AppKeyReceiveCount2(appKeyReceiveCount.ts, newResult)
      newElement +=: list
      AppKeyReceiveCountActor.Value = list
      preStart()
  }

  @throws(classOf[Exception])
  override def preStart(): Unit = {
    val ts = TimeProcessor.getMinuteStart((System.currentTimeMillis() / 1000L).toInt)
    appKeyReceiveCount = new AppKeyReceiveCount(ts)

  }

}

object AppKeyReceiveCountActor {

  private implicit lazy val ec: ExecutionContextExecutor = ExecutionContextFactory.build(1)
  var Value = ListBuffer[AppKeyReceiveCount2]()

  case class AppKeyCount(appkey: String, count: Int)

  case class AppKeyMetricCode(appkey: String, metricCode: Int)

  case class AppKeyReceiveCount(@JsonProperty("ts") ts: Int, @JsonProperty("map") map: mutable.Map[String, java.util.HashSet[Integer]] = mutable.Map())

  case class AppKeyReceiveCount2(@JsonProperty("ts") ts: Int, @JsonProperty("list") list: List[AppKeyCount] = List())

  case object Timer

}
