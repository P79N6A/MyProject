package com.sankuai.octo.log.actor

import java.util.concurrent.TimeUnit._

import akka.actor.{Actor, ActorLogging, Cancellable}
import com.meituan.jmonitor.JMonitor
import com.sankuai.octo.log.utils.IndicatorHelper
import com.sankuai.octo.log.utils.IndicatorHelper.{Appkeys, UserAccessCount, UserNames}
import com.sankuai.octo.statistic.util.ExecutionContextFactory
import org.joda.time.DateTime

import scala.concurrent.Future
import scala.concurrent.duration.Duration

/**
  * Created by wujinwu on 16/5/31.
  */
class FrontendIndicatorListener extends Actor with ActorLogging {

  import FrontendIndicatorListener._

  private var cleanTask: Cancellable = null
  private var jmonitorTask: Cancellable = null

  @throws(classOf[Exception])
  override def preStart(): Unit = {

    cleanTask = {
      val now = DateTime.now()
      val nowSecond = (now.getMillis / 1000L).toInt
      val tomorrowSecond = (now.plusDays(1).withTimeAtStartOfDay().withMinuteOfHour(1).getMillis / 1000).toInt

      val initialDelay = tomorrowSecond - nowSecond
      context.system.scheduler.schedule(Duration(initialDelay, SECONDS), Duration(1, DAYS), self, Clean)(ec)
    }

    jmonitorTask = {
      val interval = Duration(30, SECONDS)
      context.system.scheduler.schedule(interval, interval, self, SetJmonitorValue)(ec)
    }

    initDataFromTair()

  }

  private def initDataFromTair() = {
    //  初始化 appkey跟用户列表
    IndicatorHelper.getUserNames.foreach { item =>
      FrontendIndicatorListener.daily_userNames = item
    }(ec)

    IndicatorHelper.getUserAccessCount.foreach { item =>
      FrontendIndicatorListener.daily_user_access_count = item
    }(ec)

    IndicatorHelper.getAppkeys.foreach { item =>
      FrontendIndicatorListener.daily_appkeys = item
    }(ec)
  }

  @throws(classOf[Exception])
  override def postStop(): Unit = {
    //    task.cancel()
    cleanTask.cancel()
    jmonitorTask.cancel()

    putDataToTair()
  }

  private def putDataToTair() = {
    //  存储业务指标至tair
    IndicatorHelper.putAppkeys(daily_appkeys)
    IndicatorHelper.putUserNames(daily_userNames)
    IndicatorHelper.putUserAccessCount(daily_user_access_count)
  }

  override def receive: Receive = {

    case UserWatch(userName, appkey) => userWatchAction(userName, appkey)

    case Clean => cleanAction()

    case SetJmonitorValue => setJmonitorAction()

  }

  private def cleanAction() = {
    daily_userNames = UserNames()
    daily_user_access_count = UserAccessCount()
    daily_appkeys = Appkeys()
  }

  private def userWatchAction(userName: String, appkey: String) = {
    daily_userNames = daily_userNames.copy(set = daily_userNames.set + userName)
    daily_user_access_count = daily_user_access_count.copy(count = daily_user_access_count.count + 1)
    daily_appkeys = daily_appkeys.copy(set = daily_appkeys.set + appkey)

  }

  private def setJmonitorAction(): Unit = {
    Future {
      JMonitor.setCount("kpi.user.access", daily_user_access_count.count)
      JMonitor.setCount("kpi.distinct.user", daily_userNames.set.size)

      log.info("daily userNames:{}", daily_userNames.set)
      log.info("daily user access count:{}", daily_user_access_count.count)
      log.info("daily appkeys:{}", daily_appkeys.set)

    }(ec)

  }


}

object FrontendIndicatorListener {

  private val ec = ExecutionContextFactory.build(1)
  /** 记录一天内累加业务指标 */
  private var daily_userNames = UserNames()
  private var daily_appkeys = Appkeys()
  private var daily_user_access_count = UserAccessCount()

  case class UserWatch(userName: String, appkey: String)

  case object Clean

  case object SetJmonitorValue

}
