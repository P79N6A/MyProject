package com.sankuai.octo.export.exporter

import java.util.concurrent.TimeUnit._

import akka.actor.{Actor, ActorLogging}
import com.sankuai.octo.statistic.helper.{TagHelper, TimeProcessor}
import com.sankuai.octo.statistic.model.{StatData, StatSource, Tag, TagKey}
import com.sankuai.octo.statistic.util.ExecutionContextFactory
import org.joda.time.DateTime

import scala.collection.mutable
import scala.concurrent.duration.Duration
import scala.concurrent.{ExecutionContextExecutor, Future, blocking}

/**
 * Created by wujinwu on 16/5/27.
 */
class TagActor extends Actor with ActorLogging {

  import TagActor._

  private var tags = mutable.Map[TagKey, Tag]()

  private val exportTagTask = {
    val interval = Duration(3, MINUTES)
    context.system.scheduler.schedule(interval, interval, self, ExportTag)
  }

  private val cleanTask = {
    val now = DateTime.now()
    val nowSecond = (now.getMillis / 1000L).toInt
    val tomorrowSecond = (now.plusDays(1).withTimeAtStartOfDay().withMinuteOfHour(5).getMillis / 1000).toInt

    val initialDelay = tomorrowSecond - nowSecond
    context.system.scheduler.schedule(Duration(initialDelay, SECONDS), Duration(1, DAYS), self, Clean)
  }


  @scala.throws[Exception](classOf[Exception])
  override def postStop(): Unit = {
    //  故障时导出数据
    exportTagAction()
    exportTagTask.cancel()
    cleanTask.cancel()
    super.postStop()
  }

  private def exportTagAction() = {
    tags.foreach { case (key, tag) =>
      putTagToTair(key, tag)
    }
  }


  private def addToTag(statData: StatData): Unit = {
    //  将分钟的时间戳转换为当天起始时间的时间戳
    val dayStart = TimeProcessor.getDayStart(statData.getTs)

    //  将client,Server端的source归并,聚合tags
    val source = getSource(statData)
    val key = TagKey(statData.getAppkey, dayStart, statData.getEnv, source)
    val tag = getTagByKey(key)
    addStatToTag(tag, statData)
  }

  private def getSource(statData: StatData) = {
    if (null == statData.getSource) {
      log.error(s"source is null appkey:${statData.getAppkey},set defalult Server")
      StatSource.Server
    } else {
      statData.getSource match {
        case StatSource.Client | StatSource.ClientDrop | StatSource.ClientSlow | StatSource.ClientFailure => StatSource.Client
        case StatSource.Server | StatSource.ServerDrop | StatSource.ServerSlow | StatSource.ServerFailure => StatSource.Server
        case StatSource.RemoteClient | StatSource.RemoteClientDrop | StatSource.RemoteClientSlow | StatSource.RemoteClientFailure => StatSource.RemoteClient
      }
    }
  }

  private def getTagByKey(key: TagKey) = {
    val tag = tags.get(key) match {
      case Some(v) => v
      case None =>
        //  从tair中回复数据,若无则新建
        val newTag = TagHelper.getTagBytesByKey(key) match {
          case Some(bytes) => TagHelper.asTag(bytes)
          case None => Tag()
        }
        tags += key -> newTag
        newTag
    }
    tag
  }

  override def receive: Receive = {
    case statData: StatData => addToTag(statData)

    case ExportTag => exportTagAction()

    case Clean => cleanData()
  }

  private def cleanData() = {
    log.info(s"TagActor Clean start")
    val dayStart = TimeProcessor.getDayStart((System.currentTimeMillis() / 1000L).toInt)
    tags.foreach { case (tagKey, tag) =>
      if (tagKey.ts < dayStart) {
        // 落地到tair中,并将历史数据删除
        putTagToTair(tagKey, tag)
        tags -= tagKey
      }
    }

  }

  private def addStatToTag(tag: Tag, statData: StatData) {
    if (!tag.spannames.contains(statData.getTags.spanname)) {
      tag.spannames += statData.getTags.spanname
    }
    if (!tag.localHosts.contains(statData.getTags.localHost)) {
      tag.localHosts += statData.getTags.localHost
    }
    if (!tag.remoteAppKeys.contains(statData.getTags.remoteAppKey)) {
      tag.remoteAppKeys += statData.getTags.remoteAppKey
    }
    if (!tag.remoteHosts.contains(statData.getTags.remoteHost)) {
      tag.remoteHosts += statData.getTags.remoteHost
    }
  }


}

object TagActor {

  private implicit lazy val ec: ExecutionContextExecutor = ExecutionContextFactory.build(1)

  val hbaseExecutionContext = ExecutionContextFactory.build()

  private def putTagToTair(key: TagKey, tag: Tag) = {
    Future {
      blocking {
        TagHelper.putTagToTair(key, tag)
      }
    }(hbaseExecutionContext)
  }

  case object ExportTag

  case object Clean

}
