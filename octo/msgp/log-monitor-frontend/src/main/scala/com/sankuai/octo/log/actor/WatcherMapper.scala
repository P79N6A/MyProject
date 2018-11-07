package com.sankuai.octo.log.actor

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.cluster.client.ClusterClient.Send
import akka.event.LoggingReceive
import com.corundumstudio.socketio.SocketIOClient
import com.sankuai.octo.log.Protocol._
import com.sankuai.octo.log.actor.FrontendIndicatorListener.UserWatch
import com.sankuai.octo.log.constant.RTLogConstant
import com.sankuai.octo.statistic.util.ExecutionContextFactory

import scala.concurrent.duration._


/**
  * Created by wujinwu on 16/4/21.
  */
class WatcherMapper(clusterClient: ActorRef) extends Actor with ActorLogging {

  import WatcherMapper._

  /** The worker register itself periodically to the master, see the <code>registerTask</code>.
    * This has the nice characteristics that master and worker can be started in any order, and
    * in case of master fail over the worker re-register itself to the new master. */


  private val clusterRouter = "/user/router/singleton"
  private val registerTask = {
    val registerInterval = 30 second
    val task = context.system.scheduler.schedule(registerInterval, registerInterval, clusterClient,
      new Send(clusterRouter, RegisterClient(RTLogConstant.localIP, self)))(ec)
    task
  }

  private val heartbeatTask = {
    val interval = 30 second
    val task = context.system.scheduler.schedule(interval, interval, self, HeartBeat)(ec)
    task
  }
  //  启动性能监控actor
  private val frontendIndicatorListener = context.actorOf(Props[FrontendIndicatorListener](), "frontendIndicatorListener")
  private var userToInfoMap = Map[SocketIOClient, Set[WatchInfo]]()
  private var infoToUserMap = Map[WatchInfo, Set[SocketIOClient]]()

  override def receive: Receive = LoggingReceive {
    case StartWatch(user, watchParam) => startWatchAction(user, watchParam)

    case StopWatch(user) => stopWatchAction(user)

    case LogMsg(info, result) => forwardAction(info, result)

    case HeartBeat => heartBeatAction()

  }


  private def startWatchAction(user: SocketIOClient, watchParam: WatchParam) = {
    val watchInfos = generateWatchInfos(watchParam)
    //  add user to new Info mapping
    userToInfoMap += user -> watchInfos
    watchInfos.foreach { info =>
      val set = infoToUserMap.getOrElse(info, Set())
      infoToUserMap += info -> (set + user)
    }
    registerUserToWatcher(user, watchInfos)

    recordWatchAction(watchParam)

  }

  /**
    * 记录用户监控的动作
    *
    * @param watchParam 监控参数
    */
  private def recordWatchAction(watchParam: WatchParam) = {
    frontendIndicatorListener ! UserWatch(watchParam.userName, watchParam.appkey)
  }

  private def stopWatchAction(user: SocketIOClient) = {
    val watchInfoOpt = userToInfoMap.get(user)
    userToInfoMap -= user
    watchInfoOpt.foreach { watchInfos =>
      watchInfos.foreach { info =>
        infoToUserMap.get(info).foreach { users =>
          val newUsers = users - user
          if (newUsers.isEmpty) {
            infoToUserMap -= info
          } else {
            infoToUserMap += info -> newUsers
          }
        }
      }
    }
    deregisterUserFromWatcher(user.getSessionId.toString, watchInfoOpt)
  }

  private def deregisterUserFromWatcher(uuid: String, option: Option[Set[WatchInfo]]) = {
    option.foreach { watchInfos =>
      // deregister User From Watcher
      val clientUUID = ClientUUID(RTLogConstant.localIP, uuid)
      val list = ClientUUIDWatchInfo(clientUUID, watchInfos)
      val cmd = WatchAction(WatchCmd.Stop, list)

      clusterClient ! new Send(clusterRouter, cmd)
    }
  }

  private def forwardAction(info: WatchInfo, result: LogResult) = {
    log.info(s"info:$info,result:$result")
    infoToUserMap.get(info) match {
      case Some(users) =>
        users.foreach { user =>
          user.sendEvent(RTLogConstant.LOG_EVENT_NAME, result)
        }

      case None =>
        //  注销失效的watchInfo
        val infoOpt = Some(Set(info))
        deregisterUserFromWatcher("*", infoOpt)
    }

    handleExceptionMsg(info, result)


  }

  def handleExceptionMsg(info: WatchInfo, result: LogResult) = {
    if (result.`type` != 0) {
      //  清理失效的info
      infoToUserMap.get(info).foreach { clients =>
        clients.foreach { client =>
          userToInfoMap.get(client).foreach { infos =>
            val newInfos = infos - info
            if (newInfos.isEmpty) {
              userToInfoMap -= client
            } else {
              userToInfoMap += client -> newInfos
            }
          }
        }

      }
      //  删除失效的info
      infoToUserMap -= info
    }
  }

  /**
    * 往后端发送有效的监控信息,实现"心跳"功能
    */
  private def heartBeatAction() = {

    userToInfoMap.foreach { case (user, watchInfos) =>
      registerUserToWatcher(user, watchInfos)
    }
  }

  private def registerUserToWatcher(user: SocketIOClient, watchInfos: Set[WatchInfo]) = {

    val uuid = user.getSessionId.toString
    val clientUUID = ClientUUID(RTLogConstant.localIP, uuid)
    val list = ClientUUIDWatchInfo(clientUUID, watchInfos)
    val cmd = WatchAction(WatchCmd.Start, list)

    clusterClient ! new Send(clusterRouter, cmd)
  }

  @throws[Exception](classOf[Exception])
  override def postStop(): Unit = {
    registerTask.cancel()
    heartbeatTask.cancel()
  }


}

object WatcherMapper {

  private implicit val ec = ExecutionContextFactory.build(1)

  private def generateWatchInfos(watchParam: WatchParam) = {
    watchParam.hosts.map { host => WatchInfo(watchParam.appkey, watchParam.filePath, watchParam.filterWords, host) }
  }

  case class WatchParam(userName: String, appkey: String, filePath: String, filterWords: Set[String], hosts: Set[String])


  case class StartWatch(user: SocketIOClient, watchParam: WatchParam)

  case class StopWatch(user: SocketIOClient)

  case object HeartBeat

}
