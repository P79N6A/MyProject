package com.sankuai.octo.statistic.metric

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import com.sankuai.octo.statistic.domain.{InstanceKey2, Metric2, StatGroupRange}
import com.sankuai.octo.statistic.model._

import scala.collection.mutable

/**
  * Created by wujinwu on 16/5/21.
  */
class MetricManager2(key: InstanceKey2, exporter: ActorRef) extends Actor with ActorLogging {

  import MetricManager2._

  private val groupRangeMap = mutable.Map[StatGroupRange, ActorRef]()

  override def receive: Receive = {
    case metric: Metric2 =>
      groups.foreach { group =>
        if (MetricSwitch.isOpen(group)) {
          ranges.foreach { range =>
            val groupRange = getCache(StatGroupRange(group, range))
            val actor = groupRangeMap.getOrElseUpdate(groupRange, createGroupRangeActor(groupRange))
            actor ! metric
          }
        }
      }

  }

  private def createGroupRangeActor(groupRange: StatGroupRange) = {
    val gActor = context.actorOf(Props(classOf[GroupRangeActor], key, groupRange, exporter).withDispatcher("custom.group-dispatcher"))
    gActor
  }

}

object MetricManager2 {

  private val groups = StatGroup.values().toSeq

  private val ranges = StatRange.values().filter(_ != StatRange.Day).toSeq

  private var cacheGroupRange = Map[StatGroupRange, StatGroupRange]()

  private def getCache(key: StatGroupRange) = {
    cacheGroupRange.getOrElse(key, createGroupRange(key))
  }

  private def createGroupRange(key: StatGroupRange) = {
    cacheGroupRange += key -> key
    key
  }

}
