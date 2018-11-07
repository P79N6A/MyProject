package com.sankuai.octo.mworth.service

import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.{ConcurrentLinkedQueue, Executors, TimeUnit}

import com.sankuai.msgp.common.service.org.{BusinessOwtService, OpsService}
import com.sankuai.msgp.common.utils.StringUtil
import com.sankuai.octo.msgp.serivce.service.ServiceCommon
import com.sankuai.octo.mworth.common.model.{OperationSourceType, WorthEvent}
import com.sankuai.octo.mworth.dao.worthEvent
import com.sankuai.octo.mworth.dao.worthEvent.WEvent
import org.slf4j.LoggerFactory

import scala.collection.concurrent.TrieMap

/**
 * Created by zava on 15/12/7.
 */
object mWorthEventService {
  private val logger = LoggerFactory.getLogger(this.getClass)

  private val scheduler = Executors.newSingleThreadScheduledExecutor()

  private val queue = new ConcurrentLinkedQueue[WorthEvent]()

  private val userBusiness = TrieMap[String, Int]()
  private val appkeyOwtMap = TrieMap[String, String]()

  val taskcount = new AtomicInteger()

  private val timerTask = new Runnable {

    override def run(): Unit = {
      if (!queue.isEmpty) {
        try {
          val seq = (1 to 100).flatMap(_ => {
            Option(queue.poll())
          })
          if (seq.nonEmpty) {
            try {
              val start = System.currentTimeMillis()
              val insertList = seq.map(event => eventToWEvent(event)).toList
              worthEvent.batchInsert(insertList)
              logger.info(s" batch insert event size:${insertList.size},time:${System.currentTimeMillis() - start}")
            } catch {
              case e: Exception => logger.error("batch insert event Fail", e)
            }
            if (taskcount.getAndIncrement() % 100 == 0) {
              appkeyOwtMap.clear()
              userBusiness.clear()
              taskcount.set(0)
            }
          }
        } catch {
          case e: Exception => logger.error("batch insert event Fail", e)
        }
      }
    }
  }

  private def eventToWEvent(worthEvent: WorthEvent): WEvent = {
    val business = if (OperationSourceType.HUMAN == worthEvent.getOperationSourceType) {
      getBusiness(worthEvent.getOperationSource)
    } else {
      -1
    }
    val appkeyOwt = getAppkeyOwt(worthEvent.getTargetAppkey)
    WEvent(0L, worthEvent.getProject, worthEvent.getModel,
      worthEvent.getFunctionName, worthEvent.getFunctionDesc, worthEvent.getOperationSourceType.getType,
      business, worthEvent.getOperationSource, worthEvent.getTargetAppkey,
      appkeyOwt, worthEvent.getSignid,
      worthEvent.getStartTime, worthEvent.getEndTime, worthEvent.getCreateTime.getTime
    )
  }

  def getBusiness(username: String): Int = {
    userBusiness.getOrElseUpdate(username, getUserBusiness(username))
  }

  def getAppkeyOwt(appkey: String): String = {
    if (StringUtil.isNotBlank(appkey) && ServiceCommon.exist(appkey)) {
      appkeyOwtMap.getOrElseUpdate(appkey, ServiceCommon.desc(appkey).owt.getOrElse(""))
    } else {
      ""
    }
  }

  def getUserBusiness(username: String): Int = {
    val owts = OpsService.getOwtsbyUsername(username)
    if (owts.size() > 0)
      BusinessOwtService.getBusiness(owts.get(0))
    else
      100
  }

  {
    scheduler.scheduleAtFixedRate(timerTask, 5, 30, TimeUnit.SECONDS)

    /** 在jvm退出时优雅关闭 */
    Runtime.getRuntime.addShutdownHook(new Thread() {
      override def run(): Unit = {
        scheduler.submit(timerTask)
        scheduler.shutdown()
        scheduler.awaitTermination(10, TimeUnit.SECONDS)
      }
    })
  }

  def put(worthEvent: WorthEvent) = queue.offer(worthEvent)
}


