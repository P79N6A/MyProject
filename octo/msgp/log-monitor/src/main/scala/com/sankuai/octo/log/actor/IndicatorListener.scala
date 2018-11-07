package com.sankuai.octo.log.actor

import java.util.concurrent.TimeUnit._

import akka.actor.{Actor, ActorLogging}
import com.meituan.jmonitor.JMonitor
import com.sankuai.octo.statistic.util.ExecutionContextFactory

import scala.concurrent.Future
import scala.concurrent.duration.Duration

/**
  * Created by wujinwu on 16/5/31.
  */
class IndicatorListener extends Actor with ActorLogging {

  import IndicatorListener._

  private val watcherManager = context.parent

  private val task = {
    val interval = Duration(30, SECONDS)
    context.system.scheduler.schedule(interval, interval, watcherManager, IndicatorMsg)(ec)
  }
  private val jmonitorTask = {
    val interval = Duration(30, SECONDS)
    context.system.scheduler.schedule(interval, interval, self, SetJmonitorValue)(ec)
  }


  @throws(classOf[Exception])
  override def postStop(): Unit = {
    task.cancel()
    jmonitorTask.cancel()

  }


  override def receive: Receive = {
    case Indicator(msg_appkeys, msg_connectionCount) => indicatorAction(msg_appkeys, msg_connectionCount)

    case SetJmonitorValue => setJmonitorAction()

  }

  private def indicatorAction(msg_appkeys: Set[String], msg_connectionCount: Int) = {
    current_appkey_count = msg_appkeys.size
    current_connection_count = msg_connectionCount
    log.info("current online appkeys:{}", msg_appkeys)
    log.info("current log_io connection count:{}", msg_connectionCount)
  }

  private def setJmonitorAction(): Unit = {
    Future {
      JMonitor.setCount("kpi.appkey", current_appkey_count)
      JMonitor.setCount("kpi.connection", current_connection_count)

    }(ec)

  }

}

object IndicatorListener {

  private val ec = ExecutionContextFactory.build(1)

  /** 记录瞬态的业务指标 */
  //  当前活跃的appkey 列表
  private var current_appkey_count: Int = 0
  //  当前与log_io的连接数
  private var current_connection_count: Int = 0

  case class Indicator(appkeys: Set[String], connectionCount: Int)

  case object IndicatorMsg

  case object SetJmonitorValue

}
