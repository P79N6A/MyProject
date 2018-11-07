package com.sankuai.octo.log

import akka.actor.ActorRef
import com.sankuai.octo.log.Protocol.WatchCmd.WatchCmd

/**
  * Created by wujinwu on 16/6/6.
  */
object Protocol {

  case class RegisterClient(clientIp: String, client: ActorRef)

  case class WatchInfo(appkey: String, filePath: String, filterWords: Set[String], host: String)

  case class ClientUUID(clientIp: String, uuid: String)

  case class ClientUUIDWatchInfo(clientUUID: ClientUUID, watchInfos: Set[WatchInfo])

  case class WatchAction(cmd: WatchCmd, clientUUIDWatchInfo: ClientUUIDWatchInfo)

  /**
    *
    * @param `type`  前端结果的类型,0 表示成功,非0表示异常
    * @param host    查询的LogAgent的主机名
    * @param content 内容
    */
  case class LogResult(`type`: Int, host: String, content: String)

  case class LogMsg(info: WatchInfo, result: LogResult)

  case class LogInnerMsg(clientIps: Set[String], info: WatchInfo, result: LogResult)

  object WatchCmd extends Enumeration {
    type WatchCmd = Value
    val Start, Stop = Value
  }

}
