package com.sankuai.octo.updater.actor

import akka.actor.{Actor, ActorLogging, Props}
import com.sankuai.octo.updater.actor.StatusChecker.StatusInfo
import com.sankuai.octo.updater.thrift.ProviderStatus

/**
  * Created by wujinwu on 16/6/8.
  */
class StatusChecker extends Actor with ActorLogging {
  override def receive: Receive = {
    case info: StatusInfo =>
      val actor = context.actorOf(Props[StatusUpdater]().withDispatcher("updater-dispatcher"))
      actor ! info
  }
}

object StatusChecker {

  case class StatusInfo(providerPath: String, status: ProviderStatus)

}
