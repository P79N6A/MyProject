package com.sankuai.octo.mworth.service

import java.util
import java.util.concurrent.{Executors, TimeUnit}

import com.sankuai.msgp.common.model.Page
import com.sankuai.octo.mworth.dao.worthValue.WValue
import com.sankuai.octo.mworth.dao.{worthEvent, worthValue}
import com.sankuai.octo.mworth.db.Tables.{WorthConfigRow, WorthEventRow}
import com.sankuai.octo.mworth.model.MWorthConfig
import com.sankuai.octo.mworth.utils.CronScheduler
import org.apache.commons.lang3.StringUtils
import org.joda.time.DateTime
import org.quartz.{DisallowConcurrentExecution, Job, JobExecutionContext, JobExecutionException, _}
import org.slf4j.LoggerFactory

import scala.collection.JavaConverters._
import scala.collection.mutable

/**
 * Created by zava on 15/12/2.
 * 1:获取事件
 * 2:根据事件获取服务的定义
 * 3:获取服务的配置
 * 4:计算服务价值
 */
object worthCountService {
  private val logger = LoggerFactory.getLogger(worthCountService.getClass)

  private val scheduler = Executors.newSingleThreadScheduledExecutor()

  private val defaultTime = 30

  val signMap = mutable.HashMap[String, WorthEventRow]()

  def count(date: DateTime) = {
    val yesterday = date.minusDays(1)
    val yesterdayStart = yesterday.withTimeAtStartOfDay().toDate
    val eneOfYesterday = date.withTimeAtStartOfDay().toDate

    val page = new Page(1, 1000)
    val list = worthEvent.search(None, None, None, None, yesterdayStart, eneOfYesterday, page)
    worthCountService.countEvent(list)
    for (pageNo <- 2 to page.getTotalPageCount) {
      page.setPageNo(pageNo)
      val list = worthEvent.search(None, None, None, None, yesterdayStart, eneOfYesterday, page)
      worthCountService.countEvent(list)
    }
  }

  def countEvent(list: List[WorthEventRow]) {
    if (list.isEmpty) {
      return
    }
    val valueList = new util.ArrayList[WValue]()
    for (event <- list) {
      countTime(event) match {
        case Some(time) =>
          val functionOpt = getFunctionKey(event)
          functionOpt match {
            case Some(funtion) =>
              val functionId = funtion.id
              val worthConfig = getWortConfig(functionId, event)
              if (None != worthConfig) {
                val worth = worthConfig.get.worth;
                val totalWorth = worth * time.toInt
                val primitiveTotalWorth = worthConfig.get.primitiveCostTime * worth
                valueList.add(WValue(0L, functionId, event.business, event.appkeyOwt, event.project, funtion.model,
                  event.functionDesc, worth, totalWorth,
                  primitiveTotalWorth, time, event.createTime, System.currentTimeMillis, false))
              }
            case _ =>
              None
          }
        case _ =>
          None
      }
    }
    val start = System.currentTimeMillis();
    if (!valueList.isEmpty) {
      worthValue.batchInsert(valueList.asScala.toList)
    }
    logger.info(s" batch insert event size:${valueList.size},time:${System.currentTimeMillis() - start}")
  }

  def count(list: List[WorthEventRow], config: MWorthConfig): Unit = {
    val valueList = new util.ArrayList[WValue]()
    for (event <- list) {
      countTime(event) match {
        case Some(time) =>
          var totalWorth = 0;
          var worth = 0;
          var primitiveTotalWorth = 0;
          if (None != config) {
            worth = config.getWorth
            totalWorth = config.getWorth * time.toInt
            primitiveTotalWorth = config.getPrimitiveCostTime * time.toInt
          }
          getFunctionKey(event) match {
            case Some(funtion) =>
              valueList.add(WValue(0L, config.getFunctionId, event.business, event.appkeyOwt, event.project, funtion.model,
                event.functionDesc, worth, totalWorth,
                primitiveTotalWorth, time, event.createTime, System.currentTimeMillis, false))
            case _ =>
              None
          }
        case _ =>
          None
      }
    }
    val start = System.currentTimeMillis();
    if (valueList.size() > 0) {
      worthValue.batchInsert(valueList.asScala.toList)
    }
    logger.info(s" batch insert event size:${valueList.size},time:${System.currentTimeMillis() - start}")
  }

  //计算耗时,单位 秒
  def countTime(worthEventRow: WorthEventRow): Option[Long] = {
    val value = worthEventRow.signid match {
      case Some(signid) => //有上下文的数据
        if (StringUtils.isBlank(signid)) {
          Some(worthEventRow.endTime.getOrElse(0L) - worthEventRow.startTime.getOrElse(0l))
        }
        else if (worthEventRow.endTime == None) {
          signMap.put(signid, worthEventRow)
          None
        } else {
          val startEvent = signMap.get(signid)
          if (startEvent != None) {
            Some(worthEventRow.endTime.getOrElse(0L) - startEvent.get.startTime.getOrElse(0l))
          } else {
            val startEvents = worthEvent.querySign(worthEventRow.project, worthEventRow.functionName, worthEventRow.signid)
            if (startEvents.nonEmpty) {
              val startEvent = startEvents(0)
              Some(worthEventRow.endTime.getOrElse(0L) - startEvent.startTime.getOrElse(0l))
            } else {
              Some(1L)
            }
          }
        }
      case None =>
        Some(worthEventRow.endTime.getOrElse(0L) - worthEventRow.startTime.getOrElse(0l))
    }
    value match {
      case Some(time) =>
         Some(time / 1000 + defaultTime)
      case _ =>
        Some(1L)
    }
  }

  def getFunctionKey(worthEvent: WorthEventRow) = {
    val key = s"${worthEvent.project}|${worthEvent.model}|${worthEvent.functionDesc}"
    mWorthFunctionService.get(key)
  }

  def getWortConfig(funtionId: Long, worthEvent: WorthEventRow): Option[WorthConfigRow] = {
    val appkey = worthEvent.targetAppkey.getOrElse("")
    var config = mWorthConfigService.get(s"$funtionId|$appkey")
    if (config == None) {
      config = mWorthConfigService.get(s"$funtionId")
    }
    config
  }

  def start {
    // Event Daily Counter
    scheduleDailyJob()
    // 在jvm退出时优雅关闭
    Runtime.getRuntime.addShutdownHook(new Thread() {
      override def run(): Unit = {
        scheduler.shutdown()
        scheduler.awaitTermination(10, TimeUnit.SECONDS)
      }
    })

  }

  private def scheduleDailyJob() {
    val job: JobDetail = JobBuilder.newJob(classOf[EventDailyCounterJob]).build()
    val trigger: Trigger = TriggerBuilder.newTrigger().startNow()
      .withSchedule(CronScheduleBuilder.dailyAtHourAndMinute(0, 5)).build()
    CronScheduler.scheduleJob(job, trigger)
  }

}

@DisallowConcurrentExecution
class EventDailyCounterJob extends Job {

  private val logger = LoggerFactory.getLogger(this.getClass)

  @throws(classOf[JobExecutionException])
  override def execute(ctx: JobExecutionContext) {
    logger.info("EventDailyCounterJob Start!")

    //TODO 如果事件跨天如何解决
    worthCountService.signMap.clear()

    // 取得昨天的时间
    val now = DateTime.now()
    val yesterday = now.minusDays(1)
    worthCountService.count(yesterday)
  }
}
