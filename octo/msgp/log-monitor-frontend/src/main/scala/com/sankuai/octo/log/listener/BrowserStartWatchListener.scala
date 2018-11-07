package com.sankuai.octo.log.listener

import akka.actor.ActorRef
import com.corundumstudio.socketio.listener.DataListener
import com.corundumstudio.socketio.{AckRequest, SocketIOClient}
import com.sankuai.octo.log.actor.WatcherMapper.{StartWatch, WatchParam}
import com.sankuai.octo.log.constant.RTLogConstant
import com.sankuai.octo.log.event.BrowserWatchData
import dispatch.Defaults.executor
import dispatch.{Http, as, url}
import org.slf4j.LoggerFactory

class BrowserStartWatchListener(watcherMapper: ActorRef) extends DataListener[BrowserWatchData] {


  private val logger = LoggerFactory.getLogger(this.getClass)

  private val req = url(s"${RTLogConstant.MSGP_SERVER}/api/realtime/update_path").POST

  @throws(classOf[Exception])
  override def onData(client: SocketIOClient, data: BrowserWatchData, ackSender: AckRequest): Unit = {
    logger.info(s"rtLog,user:${data.userName},appkey:${data.appkey}")
    if (data.hosts != null && data.hosts.nonEmpty) {
      val filterWords = data.filter.split(",").map(_.trim).toSet
      val watchParam = WatchParam(data.userName, data.appkey, data.filePath, filterWords, data.hosts)
      watcherMapper ! StartWatch(client, watchParam)

      //  更新appkey的logPath
      val postRequest = req << Map("appkey" -> data.appkey, "logPath" -> data.filePath)
      logger.info("req:{}", postRequest.toRequest.toString)
      Http(postRequest > as.String).mapTo[String]
    }
  }
}