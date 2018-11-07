package com.sankuai.octo.msgp.task

import java.util
import java.util.concurrent.TimeUnit

import com.meituan.jmonitor.JMonitor
import com.sankuai.msgp.common.config.MsgpConfig
import com.sankuai.msgp.common.service.org.OrgSerivce
import com.sankuai.msgp.common.utils.{MailTask, MailUtil, StringUtil}
import com.sankuai.octo.msgp.serivce.service.ServiceCommon
import com.sankuai.octo.msgp.serivce.servicerep.ServiceWeeklyReport
import com.sankuai.octo.msgp.serivce.subscribe.AppkeySubscribe
import com.sankuai.octo.msgp.dao.report.ReportDailyMailDao
import com.sankuai.msgp.common.utils.client.Messager.mail
import com.sankuai.octo.msgp.task.ReportWeeklyMailTask.mailCondition
import com.sankuai.octo.msgp.utils._
import com.sankuai.octo.msgp.utils.client.FreeMarkerClient
import com.sankuai.octo.mworth.utils.CronScheduler
import com.sankuai.octo.statistic.util.ExecutionContextFactory
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import org.quartz._
import org.slf4j.LoggerFactory

import scala.collection.JavaConverters._
import scala.concurrent.duration.Duration

object ReportWeeklyMailTask {

  private[task] val logger = LoggerFactory.getLogger(this.getClass)
  private implicit val ec = ExecutionContextFactory.build(4)

  case class mailCondition(start: java.sql.Date, end: java.sql.Date, weekdays: List[String],
                           data: util.Map[String, Object])

  def init(): Unit = {
    val job = JobBuilder.newJob(classOf[ReportWeeklyMailJob]).build()
    val trigger: Trigger = TriggerBuilder.newTrigger().startNow()
      .withSchedule(CronScheduleBuilder.cronSchedule("0 0 8 ? * MON")).build()
    CronScheduler.scheduleJob(job, trigger)
  }

  def asyncMail(username: String, mailname: String, appkeyList: List[String], condition: mailCondition): Unit = {
    JMonitor.kpiForCount("weekly.report")
    val map = ServiceWeeklyReport.getWeeklyData(username, appkeyList, condition.start, condition.end, condition.weekdays)
    if (map.getOrElse("week_data", List()).nonEmpty) {
      val data = condition.data
      data.put("weeklyList", map.getOrElse("week_data", List()).asJava)
      data.put("days", map.getOrElse("xAxis", List()).asJava)
      data.put("username", username)
      val body = FreeMarkerClient.getInstance.processTemplateIntoString("weeklyReportEmail.ftl", data)
      //获取发邮件的人的信息
      val mails = getSendMail(username, mailname)
      if (StringUtil.isNotBlank(body) && mails.nonEmpty) {
        logger.info(s"OCTO服务质量周报 mail to : ${mails}")
//        mail.notifySend(mails, "OCTO服务质量周报", body)
        MailUtil.getInstance().submit(new MailTask("OCTO服务质量周报",body,mails.toSet.asJava))
        ReportDailyMailDao.sendMail(username, condition.start)
      }
    }else{
      logger.info(s"$username's weekly report is empty")
    }
  }

  def getSendMail(username: String, mailname: String): Seq[String] = {
    val sendName = if (StringUtil.isBlank(mailname)) {
      username
    } else {
      mailname + ",yangrui08"
    }

    val employes = sendName.split(",").flatMap {
      name =>
        OrgSerivce.employee(name)
    }
    employes.map(_.getEmail).distinct.toSeq
  }
}


@DisallowConcurrentExecution
class ReportWeeklyMailJob extends Job {

  import com.sankuai.octo.msgp.task.ReportWeeklyMailTask.logger

  private implicit val timeout = Duration.create(20L, TimeUnit.SECONDS)


  @throws(classOf[JobExecutionException])
  override def execute(ctx: JobExecutionContext): Unit = {
    calculate()
  }

  def calculate(timeMillis: Long = System.currentTimeMillis()): Unit = {

    val dailyMail = MsgpConfig.get("daily.mail", "false").toBoolean
    if (dailyMail) {
      val datetime = new DateTime(timeMillis).plusDays(-1)
      val users = AppkeySubscribe.getSubscribedUserForReport("weekly")
      logger.info(s"sendmail size ${users.size} ")
      val condition = getMailCondition(datetime)
      users.foreach { case (username) =>
        val appkeys = AppkeySubscribe.getSubscribeForDailyReport(username)
        if (appkeys.nonEmpty) {
          logger.info(s"sendmail ,username:$username, appkeys size ${appkeys.size}")
          ReportWeeklyMailTask.asyncMail(username, "", appkeys, condition)
        }
      }
      logger.info(s"ReportWeeklyMailJob end")
    } else {
      logger.info(s"ReportWeeklyMailJob stop")
    }

  }

  def calculate(username: String, mailname: String, timeMillis: Long) = {

    logger.info(s"send weekly mail ,username:$username,mailname:$mailname")
    //  获取业务线 -> appkey list
    val appkeys = AppkeySubscribe.getSubscribeForDailyReport(username)
    if (appkeys.nonEmpty) {
      val datetime = new DateTime(timeMillis)
      val condition = getMailCondition(datetime)
      ReportWeeklyMailTask.asyncMail(username, mailname, appkeys, condition)
    }
  }

  private def getMailCondition(datetime: DateTime) = {
    val day = datetime.toString(DateTimeFormat.forPattern("yyyy-MM-dd"))
    val dateRange = ServiceWeeklyReport.getWeekRange(day)
    val startDateString = dateRange.get("start")
    val endDateString = dateRange.get("end")
    val datePattern = DateTimeFormat.forPattern("yyyy-MM-dd")
    val startDateTime = DateTime.parse(startDateString, datePattern).withTimeAtStartOfDay()
    val endDateTime = DateTime.parse(endDateString, datePattern).withTimeAtStartOfDay()
    val validDateTime = DateTime.now().withTimeAtStartOfDay().minusDays(1)

    val weekly_end_dateTime = if (validDateTime.isBefore(endDateTime.getMillis)) {
      validDateTime
    } else {
      endDateTime
    }
    val weekdays = ServiceWeeklyReport.getWeekdays(weekly_end_dateTime)
    val map = new util.HashMap[String, Object]()
    val hostUrl = ServiceCommon.OCTO_URL
    map.put("hostUrl", hostUrl)
    map.put("start", startDateString)
    map.put("end", endDateString)
    map.put("day", day)
    val stat_date = new java.sql.Date(startDateTime.getMillis)
    val end_date = new java.sql.Date(weekly_end_dateTime.getMillis)
    mailCondition(stat_date, end_date, weekdays, map)
  }


}
