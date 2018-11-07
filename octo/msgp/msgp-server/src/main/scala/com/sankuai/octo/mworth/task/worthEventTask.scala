package com.sankuai.octo.mworth.task

import java.util.concurrent.{Executors, TimeUnit}

import com.sankuai.msgp.common.utils.StringUtil
import com.sankuai.octo.mworth.dao.worthAppkeyCount.WAppkeyCount
import com.sankuai.octo.mworth.dao.worthEventCount.WEventCount
import com.sankuai.octo.mworth.dao.worthModelCount.WModelModelCount
import com.sankuai.octo.mworth.dao.{worthAppkeyCount, worthEvent, worthEventCount, worthModelCount}
import com.sankuai.octo.mworth.utils.CronScheduler
import org.joda.time.DateTime
import org.quartz.{DisallowConcurrentExecution, Job, JobExecutionContext, JobExecutionException, _}
import org.slf4j.LoggerFactory

/**
 * 1:获取事件
 * 2:统计天,周,月,报告
 */
object worthEventTask {
  private val logger = LoggerFactory.getLogger(worthEventTask.getClass)

  private val scheduler = Executors.newSingleThreadScheduledExecutor()


  /**
   * 1:组织部门维度的报告
   * 2:服务维度的报告   * 3:模块维度报告

   */
  def count(start: Long, end: Long): Unit = {
    val day = new java.sql.Date(start)
    val list = worthEvent.countBusinessUserOwtModel(start, end)
    val wcList = list.map {
      weCount =>
        val appkeyOwt = if(StringUtil.isBlank(weCount.appkeyOwt)){
        "其他"
      }else{
        weCount.appkeyOwt
      }
        WEventCount(0L, weCount.business, weCount.username, "", appkeyOwt, day, 0, weCount.project, weCount.model, weCount.count, System.currentTimeMillis())
    }
    worthEventCount.batchInsert(wcList)

    val owtlist = worthEvent.countOwtAppkeyModel(start, end)
    val owtAppkeyList = owtlist.filter{we=>StringUtil.isNotBlank(we.appkey)}.map {
      weCount =>
        val owt = if(StringUtil.isBlank(weCount.owt)){
          "其他"
        }else{
          weCount.owt
        }
        WAppkeyCount(0L, owt, weCount.appkey, weCount.model, day, 0, weCount.count, System.currentTimeMillis())
    }
    worthAppkeyCount.batchInsert(owtAppkeyList)

    val modellist = worthEvent.countModelMethod(start, end)
    val modeMethodList = modellist.map {
      weCount =>
        WModelModelCount(0L, weCount.project, weCount.model, weCount.functionName, day, 0, weCount.count, System.currentTimeMillis())
    }
    worthModelCount.batchInsert(modeMethodList)
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
    val job: JobDetail = JobBuilder.newJob(classOf[EventDailyEventJob]).build()
    val trigger: Trigger = TriggerBuilder.newTrigger().startNow()
      .withSchedule(CronScheduleBuilder.dailyAtHourAndMinute(0, 5)).build()
    CronScheduler.scheduleJob(job, trigger)
  }

}

@DisallowConcurrentExecution
class EventDailyEventJob extends Job {

  private val logger = LoggerFactory.getLogger(this.getClass)

  @throws(classOf[JobExecutionException])
  override def execute(ctx: JobExecutionContext) {
    logger.info("EventDailyCounterJob Start!")
    // 取得昨天的时间
    val now = DateTime.now()
    val yesterday = now.minusDays(1)
    val yesterdayStart = yesterday.withTimeAtStartOfDay().getMillis
    val eneOfYesterday = now.withTimeAtStartOfDay().getMillis
    worthEventTask.count(yesterdayStart, eneOfYesterday)
  }
}
