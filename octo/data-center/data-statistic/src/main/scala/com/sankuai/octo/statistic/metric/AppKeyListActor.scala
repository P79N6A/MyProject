package com.sankuai.octo.statistic.metric

import java.util.concurrent.TimeUnit.MINUTES

import akka.actor.{Actor, ActorLogging}
import com.fasterxml.jackson.annotation.JsonProperty
import com.sankuai.octo.statistic.helper.TimeProcessor
import com.sankuai.octo.statistic.util.ExecutionContextFactory

import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import scala.concurrent.ExecutionContextExecutor
import scala.concurrent.duration.Duration

/**
  * Created by wujinwu on 16/5/15.
  */


class AppKeyListActor extends Actor with ActorLogging {

  import AppKeyListActor._

  private var list = ListBuffer[AppKeyList]()

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
    case appkey: String =>
      val head = list.head
      head.appkeys += appkey
    case Timer =>
      if (list.size >= 2) {
        list = list.dropRight(1)
      }
      val ts = TimeProcessor.getMinuteStart((System.currentTimeMillis() / 1000L).toInt)
      val newElement = new AppKeyList(ts)
      newElement +=: list
      AppKeyListActor.Value = list
  }

  @throws(classOf[Exception])
  override def preStart(): Unit = {
    val ts = TimeProcessor.getMinuteStart((System.currentTimeMillis() / 1000L).toInt)
    val newElement = new AppKeyList(ts)
    newElement +=: list
  }
}


object AppKeyListActor {

  private implicit lazy val ec: ExecutionContextExecutor = ExecutionContextFactory.build(1)
  var Value = ListBuffer[AppKeyList]()

  case class AppKeyList(@JsonProperty("ts") ts: Int, @JsonProperty("appkeys") appkeys: mutable.Set[String] = mutable.Set())


  case object Timer
}
