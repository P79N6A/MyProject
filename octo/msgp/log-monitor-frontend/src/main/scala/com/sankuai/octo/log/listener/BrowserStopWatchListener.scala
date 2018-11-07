package com.sankuai.octo.log.listener

import akka.actor.ActorRef
import com.corundumstudio.socketio.listener.DataListener
import com.corundumstudio.socketio.{AckRequest, SocketIOClient}
import com.sankuai.octo.log.actor.WatcherMapper.StopWatch
import com.sankuai.octo.log.event.BrowserWatchData
import org.slf4j.LoggerFactory

class BrowserStopWatchListener(watcherMapper: ActorRef) extends DataListener[BrowserWatchData] {

  private val logger = LoggerFactory.getLogger(this.getClass)

  @throws(classOf[Exception])
  override def onData(client: SocketIOClient, data: BrowserWatchData, ackSender: AckRequest): Unit = {
    logger.info("StopWatch,client:{}", client)
    watcherMapper ! StopWatch(client)
  }
}
