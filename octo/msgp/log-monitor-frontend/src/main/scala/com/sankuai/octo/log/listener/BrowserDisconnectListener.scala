package com.sankuai.octo.log.listener

import akka.actor.ActorRef
import com.corundumstudio.socketio.SocketIOClient
import com.corundumstudio.socketio.listener.DisconnectListener
import com.sankuai.octo.log.actor.WatcherMapper.StopWatch
import org.slf4j.LoggerFactory

/**
  * Created by wujinwu on 16/4/23.
  */
class BrowserDisconnectListener(watcherMapper: ActorRef) extends DisconnectListener {
  private val logger = LoggerFactory.getLogger(this.getClass)

  override def onDisconnect(client: SocketIOClient): Unit = {
    logger.info("StopWatch,client:{}", client)
    watcherMapper ! StopWatch(client)
  }
}
