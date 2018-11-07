package com.sankuai.octo.export.instance

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import com.sankuai.octo.export.exporter.DefaultExporterActor
import com.sankuai.octo.export.histogram.GroupRangeHistogramProcessor
import com.sankuai.octo.statistic.domain._
import org.slf4j.LoggerFactory

import scala.collection.mutable

class InstanceManager extends Actor with ActorLogging {

  private val exporter = context.actorOf(Props[DefaultExporterActor](), "defaultExporterActor")

  private val instanceGroupRangeMap = mutable.Map[InstanceHistogramKey, ActorRef]()

  override def receive: Receive = {
    case instance: Instance2 => update(instance)
  }

  def update(instance: Instance2) = {
    val groupKey = instance.getGroupKey
    val groupRange = StatGroupRange(groupKey.group, groupKey.range)
    val histogramKey = InstanceHistogramKey(instance.getInstanceKey, groupRange,groupKey.statTag.localHost)
    val actor = instanceGroupRangeMap.getOrElseUpdate(histogramKey, createGroupRangeHistogramActor(groupRange))
    actor ! instance

  }

  private def createGroupRangeHistogramActor(groupRange:StatGroupRange) = {
    val gActor = context.actorOf(Props(classOf[GroupRangeHistogramProcessor], groupRange, exporter).withDispatcher("custom.group-dispatcher"))
    gActor
  }

}

object InstanceManager {
  private val logger = LoggerFactory.getLogger(this.getClass)
}
