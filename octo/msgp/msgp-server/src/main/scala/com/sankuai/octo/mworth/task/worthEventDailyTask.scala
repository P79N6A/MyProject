package com.sankuai.octo.mworth.task

import java.util.concurrent.{Executors, TimeUnit}

import com.sankuai.msgp.common.model.Business
import com.sankuai.msgp.common.service.org.{BusinessOwtService, OpsService, OrgSerivce}
import com.sankuai.octo.mworth.dao.worthEventCountDaily.WEventBusinessDescCount
import com.sankuai.octo.mworth.dao.{worthEvent, worthEventCountDaily}
import com.sankuai.octo.mworth.service.mWorthDailyService
import com.sankuai.octo.mworth.utils.CronScheduler
import org.joda.time.DateTime
import org.quartz.{CronScheduleBuilder, Trigger, TriggerBuilder, _}
import org.slf4j.LoggerFactory

/**
  * Created by yves on 16/7/6.
  * 备份每天的Event数据到数据库
  */
object worthEventCountDailyTask {

  private val logger = LoggerFactory.getLogger(worthEventCountDailyTask.getClass)

  private val scheduler = Executors.newSingleThreadScheduledExecutor()

  def countEvent(start: Long, end: Long): Unit = {
    logger.info("Backup Job of Worth Start")
    val day = new java.sql.Date(start)
    val dailyList = worthEvent.countBusinessDescDaily(start, end)
    val WEventOwtDescCountList = dailyList.map {
      element =>
        val posName = OrgSerivce.getEmployeePosName(element.username)
        val posId = OrgSerivce.getEmployeePosID(element.username)
        val orgId = OrgSerivce.getEmployeeOrgId(element.username)
        //val business = findBusinessByOrg(orgId)  Org变动后需要调整,故不使用org接口,而是使用ops接口
        val business = findBusinessByUsername(element.username)
        val orgName = OrgSerivce.getEmployeeOrgName(element.username)
        WEventBusinessDescCount(business, element.username, element.module, element.functionDesc,
          posName, posId, orgId, orgName, element.count, day);
    }
    worthEventCountDaily.batchInsert(WEventOwtDescCountList)
    logger.info(s"Backup Job of Worth End, data size is ${WEventOwtDescCountList.size}")
  }

  def countPeak = {
    mWorthDailyService.savePeak
  }

  def findBusinessByOrg(orgId: Int): Int = {
    val topOrgName = OrgSerivce.getTopOrgName(orgId)
    Business.values.find(_.toString.equalsIgnoreCase(topOrgName)).getOrElse(Business.other).getId
  }

  def findBusinessByUsername(username: String) = {
    val owtList = OpsService.getOwtsbyUsername(username)
    if(owtList.isEmpty) {
      100
    }else{
      BusinessOwtService.getBusiness(owtList.get(0))
    }
  }

  def start {
    // Event Daily Counter
    scheduleDailyBackupJob()
    // 在jvm退出时优雅关闭
    Runtime.getRuntime.addShutdownHook(new Thread() {
      override def run(): Unit = {
        scheduler.shutdown()
        scheduler.awaitTermination(10, TimeUnit.SECONDS)
      }
    })

  }

  private def scheduleDailyBackupJob() {
    val job: JobDetail = JobBuilder.newJob(classOf[worthEventCountDailyJob]).build()
    val trigger: Trigger = TriggerBuilder.newTrigger().startNow()
      .withSchedule(CronScheduleBuilder.dailyAtHourAndMinute(0, 30)).build()
    CronScheduler.scheduleJob(job, trigger)
  }

}

class worthEventCountDailyJob extends Job {

  private val logger = LoggerFactory.getLogger(this.getClass)

  @throws(classOf[JobExecutionException])
  override def execute(ctx: JobExecutionContext) {
    logger.info("DailyBackupJob Start!")
    val currentTime = DateTime.now()
    val yesterday = currentTime.minusDays(1)
    val startOfYesterday = yesterday.withTimeAtStartOfDay().getMillis
    val endOfYesterday = currentTime.withTimeAtStartOfDay().getMillis
    worthEventCountDailyTask.countEvent(startOfYesterday, endOfYesterday)
    worthEventCountDailyTask.countPeak
  }
}
