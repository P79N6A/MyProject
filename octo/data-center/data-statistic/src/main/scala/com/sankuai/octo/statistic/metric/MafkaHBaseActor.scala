/*
package com.sankuai.octo.statistic.metric

import akka.actor.{Actor, ActorLogging, Props}
import com.sankuai.octo.statistic.model.StatData
import com.sankuai.octo.statistic.util.common

/**
  * Created by wujinwu on 16/5/28.
  */
class MafkaHBaseActor extends Actor with ActorLogging {

  private val perfProducer = context.actorOf(Props[PerfProducerActor](), "perfProducer")

  private val perfConsumer = context.actorOf(Props[PerfConsumerActor](), "perfConsumer")

  override def receive: Receive = {
    if (!common.isOffline) {
      onlineReceive
    } else {
      offlineReceive
    }
  }

  private val onlineReceive: Receive = {
    case statData: StatData =>
      perfProducer ! statData
  }

  private val offlineReceive: Receive = {
    case statData: StatData => //  ignore
  }
}
*/
