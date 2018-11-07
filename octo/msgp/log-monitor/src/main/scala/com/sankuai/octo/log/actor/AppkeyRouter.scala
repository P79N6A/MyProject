package com.sankuai.octo.log.actor

import akka.actor.{Actor, ActorLogging}
import akka.cluster.client.ClusterClientReceptionist
import akka.cluster.pubsub.DistributedPubSub
import akka.cluster.pubsub.DistributedPubSubMediator.Publish
import akka.cluster.routing.{ClusterRouterGroup, ClusterRouterGroupSettings}
import akka.routing.ConsistentHashingGroup
import akka.routing.ConsistentHashingRouter.ConsistentHashMapping
import com.sankuai.octo.log.Protocol.{RegisterClient, WatchAction, WatchCmd}
import com.sankuai.octo.log.actor.WatcherManager.{DeregisterClientUUID, RegisterClientUUID}
import com.sankuai.octo.log.constant.RTLogConstant._

/**
  * Created by wujinwu on 16/6/3.
  */
class AppkeyRouter extends Actor with ActorLogging {

  private val mediator = DistributedPubSub(context.system).mediator
  ClusterClientReceptionist(context.system).registerService(self)

  private val hashMapping: ConsistentHashMapping = {
    case RegisterClientUUID(_, info) => info.appkey.hashCode
  }

  private val router = context.actorOf(
    ClusterRouterGroup(ConsistentHashingGroup(Nil, 3, hashMapping), ClusterRouterGroupSettings(
      totalInstances = 100, routeesPaths = List("/user/watcherManager"),
      allowLocalRoutees = true, useRole = None)).props(),
    name = "router")

  override def receive: Receive = {
    case msg: RegisterClient =>
      mediator ! Publish(CLIENT_TOPIC, msg)

    case WatchAction(cmd@WatchCmd.Start, clientUUIDWatchInfo) =>
      //  注册watcher
      val clientUUID = clientUUIDWatchInfo.clientUUID
      clientUUIDWatchInfo.watchInfos.foreach { info =>
        val registerUuid = RegisterClientUUID(clientUUID, info)
        router ! registerUuid
      }
    case WatchAction(cmd@WatchCmd.Stop, clientUUIDWatchInfo) =>
      //  注销 watcher
      val clientUUID = clientUUIDWatchInfo.clientUUID
      val msg = DeregisterClientUUID(clientUUID)
      mediator ! Publish(UUID_TOPIC, msg)

  }
}
