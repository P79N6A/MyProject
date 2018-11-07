package com.sankuai.octo.updater.actor

import akka.actor.{Actor, ActorLogging}
import com.sankuai.octo.updater.actor.StatusChecker.StatusInfo
import com.sankuai.octo.updater.util.StatusHelper

/**
  * Created by wujinwu on 16/6/8.
  */
class StatusUpdater extends Actor with ActorLogging {
  override def receive: Receive = {
    case StatusInfo(providerPath, status) =>
      StatusHelper.checkStatusAndUpdate(providerPath, status)
  }
}
