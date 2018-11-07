package com.sankuai.octo.log.actor

import akka.actor.SupervisorStrategy._
import akka.actor.{ActorRef, FSM, OneForOneStrategy, Props, Terminated}
import com.sankuai.octo.log.Protocol._
import com.sankuai.octo.log.actor.Connector.{Buffer, ConnectorError, ConnectorException}
import com.sankuai.octo.log.actor.Watcher.{WatcherData, WatcherState}
import com.sankuai.octo.log.actor.WatcherManager.WatcherDead
import com.sankuai.octo.log.constant.RTLogConstant.TIMEOUT

/**
  * Created by wujinwu on 16/4/21.
  */
class Watcher(watchInfo: WatchInfo, logSender: ActorRef) extends FSM[WatcherState, WatcherData] {

  import Watcher._

  override val supervisorStrategy = OneForOneStrategy(maxNrOfRetries = 3, withinTimeRange = TIMEOUT) {
    case ex: ConnectorException =>
      ex.error match {
        case ConnectorError.Network => Restart
        case _ =>
          notifyUserStopping(stateData, ex.msg)
          Stop
      }
    case t => super.supervisorStrategy.decider.applyOrElse(t, (_: Any) => Escalate)
  }
  private val watcherManager = context.parent
  private val connector = context.actorOf(Props(classOf[Connector], watchInfo))
  //  监控子节点的stop
  context.watch(connector)

  {
    startWith(Idle, Subscribers())

    when(Idle) {
      case Event(Register(user), sub@Subscribers(u)) =>
        goto(Active) using sub.copy(users = u + user)

      case Event(Deregister(user), sub@Subscribers(u)) =>
        // ignore
        stay()

      case Event(Buffer(queue), _) =>
        // ignore
        stay()

      case Event(StateTimeout, _) =>
        log.info("Watcher Timeout close,watchInfo:{}", watchInfo)
        stop()

    }

    when(Active) {
      case Event(Register(user), sub@Subscribers(u)) =>
        stay() using sub.copy(users = u + user)

      case Event(Deregister(ClientUUID(clientIp, "*")), sub@Subscribers(u)) =>
        //  解除注册clientIp所有的uuid
        val newUsers = u.filterNot(_.clientIp == clientIp)
        changeStateByUsers(newUsers, sub)

      case Event(Deregister(user), sub@Subscribers(u)) =>
        val newUsers = u - user
        changeStateByUsers(newUsers, sub)

      case Event(Buffer(queue), sub@Subscribers(u)) =>

        val innerMsg = constructLogInnerMsg(u, 0, watchInfo, queue.mkString(""))
        logSender ! innerMsg
        stay()
    }
    whenUnhandled {
      // common code for both states
      case Event(e: Exception, _) =>
        log.info("Exception", e)
        stay()

      case Event(Terminated(con), _) =>
        log.info("watcher stop,watch info:{}", watchInfo)
        notifyUserStopping(stateData)
        stop()
    }

    onTransition {
      case _ -> Idle => setTimer("idle_timeout", StateTimeout, TIMEOUT)

      case _ -> Active => cancelTimer("idle_timeout")
    }

    initialize()
  }


  @throws(classOf[Exception])
  override def preStart(): Unit = {
    setTimer("idle_timeout", StateTimeout, TIMEOUT)
    super.preStart()
  }

  override def postStop(): Unit = {

    cancelTimer("idle_timeout")
    watcherManager ! WatcherDead(watchInfo)
    super.postStop()
  }

  private def notifyUserStopping(data: WatcherData, info: String = "connection closed") = {
    val sub = data.asInstanceOf[Subscribers]
    val users = sub.users
    if (users.nonEmpty) {
      val innerMsg = constructLogInnerMsg(users, 1, watchInfo, info)
      logSender ! innerMsg
    }
  }

  private def changeStateByUsers(newUsers: Set[ClientUUID], sub: Subscribers): State = {
    if (newUsers.isEmpty) {
      goto(Idle) using Subscribers()
    } else {
      stay() using sub.copy(users = newUsers)
    }
  }

}

object Watcher {

  private def constructLogInnerMsg(users: Set[ClientUUID], `type`: Int, watchInfo: WatchInfo, content: String) = {

    val clientIps = users.map(_.clientIp)
    val result = new LogResult(`type`, watchInfo.host, content)
    val msg = LogInnerMsg(clientIps, watchInfo, result)
    msg

  }

  sealed trait WatcherState

  sealed trait WatcherData

  final case class Subscribers(users: Set[ClientUUID] = Set()) extends WatcherData

  //  received msg
  case class Register(clientUUID: ClientUUID)

  case class Deregister(clientUUID: ClientUUID)

  case object Idle extends WatcherState

  case object Active extends WatcherState

}
