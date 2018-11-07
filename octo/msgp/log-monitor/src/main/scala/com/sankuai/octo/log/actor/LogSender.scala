package com.sankuai.octo.log.actor

import akka.actor.{Actor, ActorLogging, ActorRef}
import akka.cluster.pubsub.DistributedPubSub
import akka.cluster.pubsub.DistributedPubSubMediator.Subscribe
import com.sankuai.octo.log.Protocol.{LogInnerMsg, LogMsg, RegisterClient}
import com.sankuai.octo.log.constant.RTLogConstant._

/**
  * Created by wujinwu on 16/6/6.
  */
class LogSender extends Actor with ActorLogging {
  private val mediator = DistributedPubSub(context.system).mediator
  mediator ! Subscribe(CLIENT_TOPIC, self)

  private var clientMap = Map[String, ActorRef]()

  override def receive: Receive = {
    case RegisterClient(clientIp, client) => clientMap += clientIp -> client
    case LogInnerMsg(clientIps, info, result) =>
      val logMsg = LogMsg(info, result)
      log.info(s"recv clientIps :$clientIps")
      clientIps.foreach { clientIp =>
        clientMap.get(clientIp).foreach { actor =>
          actor ! logMsg
        }
      }
  }
}
