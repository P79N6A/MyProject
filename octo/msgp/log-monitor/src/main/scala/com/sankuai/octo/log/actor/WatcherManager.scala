package com.sankuai.octo.log.actor

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.cluster.pubsub.DistributedPubSub
import akka.cluster.pubsub.DistributedPubSubMediator.Subscribe
import akka.event.LoggingReceive
import com.sankuai.octo.log.Protocol.{ClientUUID, WatchInfo}
import com.sankuai.octo.log.actor.IndicatorListener.{Indicator, IndicatorMsg}
import com.sankuai.octo.log.actor.Watcher.{Deregister, Register}
import com.sankuai.octo.log.constant.RTLogConstant._


/**
  * Created by wujinwu on 16/4/21.
  */
class WatcherManager extends Actor with ActorLogging {

  import WatcherManager._

  private val mediator = DistributedPubSub(context.system).mediator
  private val logSender = context.actorOf(Props[LogSender](), "logSender")
  //  启动性能监控actor
  private val indicatorListener = context.actorOf(Props[IndicatorListener](), "indicatorListener")

  mediator ! Subscribe(UUID_TOPIC, self)
  private var infoToWatcherMap = Map[WatchInfo, ActorRef]()

  override def receive: Receive = LoggingReceive {

    case RegisterClientUUID(clientUUID, watchInfo) => startWatchAction(clientUUID, watchInfo)

    case DeregisterClientUUID(clientUUID) => stopWatchAction(clientUUID)

    case WatcherDead(info) => removeInfo(info)

    case IndicatorMsg => indicatorListener ! getIndicator

  }

  private def startWatchAction(clientUUID: ClientUUID, watchInfo: WatchInfo) = {
    //  add user to new Info mapping
    registerUserToWatcher(clientUUID, watchInfo)

  }

  private def registerUserToWatcher(clientUUID: ClientUUID, info: WatchInfo) = {
    val watcher = infoToWatcherMap.getOrElse(info, createWatcher(info))
    watcher ! Register(clientUUID)
  }

  private def createWatcher(watchInfo: WatchInfo) = {
    val watcher = context.actorOf(Props(classOf[Watcher], watchInfo, logSender))
    infoToWatcherMap += watchInfo -> watcher
    watcher
  }

  private def getIndicator = {
    val watchInfos = infoToWatcherMap.keySet
    val appkeys = watchInfos.map { watchInfo =>
      watchInfo.appkey
    }

    log.info("online appkeys:{}", appkeys)

    val connectionCount = watchInfos.size
    Indicator(appkeys, connectionCount)
  }

  private def stopWatchAction(clientUUID: ClientUUID) = {
    deregisterUserFromWatcher(clientUUID)
  }

  private def deregisterUserFromWatcher(clientUUID: ClientUUID) = {
    infoToWatcherMap.foreach { case (_, watcher) =>
      // deregister User From Watcher
      watcher ! Deregister(clientUUID)
    }

  }

  private def removeInfo(watchInfo: WatchInfo) = {
    infoToWatcherMap -= watchInfo
  }

}

object WatcherManager {

  case class RegisterClientUUID(clientUUID: ClientUUID, watchInfo: WatchInfo)

  case class DeregisterClientUUID(clientUUID: ClientUUID)

  case class WatcherDead(watchInfo: WatchInfo)

}
