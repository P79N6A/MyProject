/*
package com.sankuai.octo.statistic.exporter

import java.util.concurrent.{Executors, TimeUnit}

import com.sankuai.octo.statistic.constant.Constants
import com.sankuai.octo.statistic.helper.{TagHelper, TimeProcessor}
import com.sankuai.octo.statistic.mafka.consumer.PerfConsumer
import com.sankuai.octo.statistic.mafka.producer.PerfProducer
import com.sankuai.octo.statistic.model._
import com.sankuai.octo.statistic.util._
import org.joda.time.DateTime
import org.quartz._
import org.slf4j.LoggerFactory

import scala.collection.concurrent.TrieMap

// 默认数据输出方式：可能存储到hbase、同时在es中构建索引
// TODO：是否通过接口上报给query来实现？
object DefaultExporterProxy extends AbstractExporter {
  private val logger = LoggerFactory.getLogger(this.getClass)

  private val falconExporter = FalconExporter

  private val tagScheduler = Executors.newSingleThreadScheduledExecutor(StatThreadFactory.threadFactory(this.getClass))

  val tagExporter = ExecutorFactory(putTagToTair, "DefaultExporterProxy.tagExporter")

  val processoreNum = Runtime.getRuntime.availableProcessors()

  val statExporter = ExecutorFactory(doExportStatData, "DefaultExporterProxy.statExporter", processoreNum, processoreNum * 3)

  private[exporter] var tags = TrieMap[TagKey, Tag]()

  {
    // actions all here!!
    val tagTimerTask = new Runnable {
      override def run(): Unit = {
        if (tags.nonEmpty) {
          //  定期将tag写入到tair
          tags.foreach {
            entry =>
              try {
                tagExporter.submit(entry)
              } catch {
                case e: Exception => logger.error("tagTimerTask fail", e)
              }
          }
        }
      }

    }

    tagScheduler.scheduleAtFixedRate(tagTimerTask, 60, 60, TimeUnit.SECONDS)

    // daily update
    scheduleDailyJob()

    //  初始化 mafka consumer
    //  初始化 mafka consumer
    if (!common.isOffline) {
      //  数据导出至mafka,异步执行
      logger.debug("online,send to mq ")
      PerfConsumer
      PerfProducer
    }

    // 在jvm退出时优雅关闭
    Runtime.getRuntime.addShutdownHook(new Thread() {
      override def run(): Unit = {
        tagScheduler.submit(tagTimerTask)
        tagScheduler.shutdown()
        tagScheduler.awaitTermination(10, TimeUnit.SECONDS)
      }
    })

  }

  private def putTagToTair(entry: (TagKey, Tag)) = {
    TagHelper.putTagToTair(entry._1, entry._2)
  }

  override def export(statData: StatData): Unit = {
    statExporter.submit(statData)
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

  /**
   *
   * @param statData 统计数据
   * @return 判断是否需要导出至falcon
   */
  private def judgeByGroup(statData: StatData) = {
    statData.getGroup match {
      case StatGroup.SpanLocalHost | StatGroup.SpanRemoteApp | StatGroup.Span => true
      case _ => false
    }
  }

  private def getSource(statData: StatData) = {
    statData.getSource match {
      case StatSource.Client | StatSource.ClientDrop | StatSource.ClientSlow | StatSource.ClientFailure => StatSource.Client
      case StatSource.Server | StatSource.ServerDrop | StatSource.ServerSlow | StatSource.ServerFailure => StatSource.Server
      case StatSource.RemoteClient | StatSource.RemoteClientDrop | StatSource.RemoteClientSlow | StatSource.RemoteClientFailure => StatSource.RemoteClient
    }
  }

  private def addStatToTag(tag: Tag, statData: StatData) {
    if (!tag.spannames.contains(statData.getTags.get(Constants.SPAN_NAME))) {
      tag.spannames += statData.getTags.get(Constants.SPAN_NAME)
    }
    if (!tag.localHosts.contains(statData.getTags.get(Constants.LOCAL_HOST))) {
      tag.localHosts += statData.getTags.get(Constants.LOCAL_HOST)
    }
    if (!tag.remoteAppKeys.contains(statData.getTags.get(Constants.REMOTE_APPKEY))) {
      tag.remoteAppKeys += statData.getTags.get(Constants.REMOTE_APPKEY)
    }
    if (!tag.remoteHosts.contains(statData.getTags.get(Constants.REMOTE_HOST))) {
      tag.remoteHosts += statData.getTags.get(Constants.REMOTE_HOST)
    }
  }

  private def getTagByKey(key: TagKey) = {
    tags.getOrElseUpdate(key, {
      //  从tair中回复数据,若无则新建
      TagHelper.getTagBytesByKey(key) match {
        case Some(bytes) => TagHelper.asTag(bytes)
        case None => Tag()
      }
    })
  }

  private def scheduleDailyJob() {

    val job: JobDetail = JobBuilder.newJob(classOf[TagDailyUpdateJob]).build()
    val trigger: Trigger = TriggerBuilder.newTrigger().startNow()
      .withSchedule(CronScheduleBuilder.dailyAtHourAndMinute(0, 5)).build()

    CronScheduler.scheduleJob(job, trigger)
  }

  /**
   *
   * @param statData 要导出的statData
   * @return 如果数据已经过期返回 true,否则 false
   */
  private def expired(statData: StatData): Boolean = {
    val nowTime = (System.currentTimeMillis() / 1000L).toInt
    (nowTime - statData.getTs) >= statData.getRange.getLifetime
  }

  private def doExportStatData(statData: StatData): Unit = {
    if (!common.isOffline) {
      //  数据导出至mafka,异步执行
      logger.debug("online,send to mq ")
      PerfProducer.sendToMq(statData)
    }

    //  数据导出至 Falcon
    statData.getRange match {
      case StatRange.Minute =>
        //  从导出的stat提取出tag信息
        addToTag(statData)

        //  分钟级别数据目前导出到Falcon
        //  判断是否需要导出至falcon
        if (expired(statData) && judgeByGroup(statData)) {
          falconExporter.export(statData)
        }
        //  测量log的timeout跟数据冗余度关系
        printLog(statData)
      case _ =>
    }

  }

  private def printLog(statData: StatData) = {
    //  添加log测量数据的多次复写的冗余度
    def dataTimeout = config.get("data_timeout", "90").toInt
    if (logger.isDebugEnabled) {
      val now = (System.currentTimeMillis() / 1000L).toInt
      if (now - statData.getTs > dataTimeout) {
        logger.debug(s"now:$now,${statData.getAppkey},${statData.getTs},${statData.getEnv}," +
          s"${statData.getSource},${statData.getRange},${statData.getGroup},${statData.getTags},${statData.getCount}")
      }
    }
  }

}


@DisallowConcurrentExecution
class TagDailyUpdateJob extends Job {

  private val logger = LoggerFactory.getLogger(this.getClass)

  @throws(classOf[JobExecutionException])
  override def execute(ctx: JobExecutionContext) {
    logger.info("TagDailyUpdateJob Start!")
    // 取得昨天的时间
    val now = DateTime.now()
    val startOfToday = now.withTimeAtStartOfDay()
    val todayTs = (startOfToday.getMillis / 1000L).toInt

    // daily update tagsval
    DefaultExporterProxy.tags.withFilter(_._1.ts < todayTs).foreach(entry => {
      TagHelper.putTagToTair(entry._1, entry._2)
      //  将历史的tag数据remove
      DefaultExporterProxy.tags.remove(entry._1)
    })
  }

}*/
