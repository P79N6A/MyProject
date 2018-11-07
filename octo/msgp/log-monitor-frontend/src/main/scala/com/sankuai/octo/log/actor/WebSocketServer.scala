package com.sankuai.octo.log.actor

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.pattern.ask
import com.corundumstudio.socketio.listener.DataListener
import com.corundumstudio.socketio.protocol.JacksonJsonSupport
import com.corundumstudio.socketio.{Configuration, SocketIOServer}
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.meituan.jmonitor.LOG
import com.sankuai.octo.log.constant.WatchEvent.WatchEvent
import com.sankuai.octo.log.constant.{RTLogConstant, WatchEvent}
import com.sankuai.octo.log.event.BrowserWatchData
import com.sankuai.octo.log.listener.BrowserDisconnectListener


/**
  * Created by wujinwu on 16/4/21.
  */
class WebSocketServer(clusterClient: ActorRef) extends Actor with ActorLogging {

  import WebSocketServer._
  import com.sankuai.octo.log.utils.ReconnectUtil.{ec, timeout}

  private val watcherMapper = context.actorOf(Props(classOf[WatcherMapper], clusterClient), "watcherMapper")
  private val listenerFactory = context.actorOf(Props(classOf[ListenerFactory], watcherMapper), "listenerFactory")
  private var server: SocketIOServer = null

  @throws(classOf[Exception])
  override def preStart(): Unit = startServer()

  private def startServer() = {
    val server = new SocketIOServer(buildConfiguration())
    initEvent(server)
    LOG.info("init socketio server")
    server.start()
    this.server = server
  }

  private def initEvent(server: SocketIOServer) = {
    WatchEvent.values.foreach { watchEvent =>
      getListener(watchEvent).foreach { listener =>
        server.addEventListener(watchEvent.toString, classOf[BrowserWatchData], listener)
      }
    }

    //  disconnect listener
    server.addDisconnectListener(new BrowserDisconnectListener(watcherMapper))
  }

  private def getListener(event: WatchEvent) = {

    val future = (listenerFactory ? event).map(_.asInstanceOf[DataListener[BrowserWatchData]])
    future
  }

  @throws(classOf[Exception])
  override def postStop(): Unit = stopServer()

  private def stopServer() = {
    if (server != null) {
      server.stop()
      server = null
    }
  }

  override def receive: Receive = {
    case _ =>
  }

}

object WebSocketServer {

  private def buildConfiguration(): Configuration = {
    val config: Configuration = new Configuration()
    //  性能相关配置
    val processorNum = Runtime.getRuntime.availableProcessors()
    config.setBossThreads(processorNum)
    config.setWorkerThreads(processorNum * 2)
    config.setPreferDirectBuffer(true)
    //  socket相关
    config.getSocketConfig.setReuseAddress(true)
    config.getSocketConfig.setTcpKeepAlive(true)
    config.setJsonSupport(new JacksonJsonSupport(DefaultScalaModule))
    //  网络相关
    config.setPort(RTLogConstant.LOG_SERVER_PORT)
    //  Linux上才开启,提高性能
    //    config.setUseLinuxNativeEpoll(true)
    config
  }

}
