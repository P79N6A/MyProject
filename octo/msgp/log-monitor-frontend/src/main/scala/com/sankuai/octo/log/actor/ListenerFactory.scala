package com.sankuai.octo.log.actor

import akka.actor.{Actor, ActorLogging, ActorRef}
import com.corundumstudio.socketio.listener.DataListener
import com.sankuai.octo.log.constant.WatchEvent
import com.sankuai.octo.log.constant.WatchEvent.WatchEvent
import com.sankuai.octo.log.event.BrowserWatchData
import com.sankuai.octo.log.listener.{BrowserStartWatchListener, BrowserStopWatchListener}

class ListenerFactory(watcherMapper: ActorRef) extends Actor with ActorLogging {

  private var listeners = Map[WatchEvent, DataListener[BrowserWatchData]]()

  override def receive: Receive = {
    case watchEvent: WatchEvent =>
      val listener = listeners.getOrElse(watchEvent, create(watchEvent))
      sender() ! listener
  }

  private def create(event: WatchEvent) = {
    val newListener = event match {
      case WatchEvent.BROWSER_START_WATCH => new BrowserStartWatchListener(watcherMapper)
      case WatchEvent.BROWSER_STOP_WATCH => new BrowserStopWatchListener(watcherMapper)
    }
    listeners += event -> newListener
    newListener
  }
}
