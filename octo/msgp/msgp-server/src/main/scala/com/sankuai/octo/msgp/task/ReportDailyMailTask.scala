package com.sankuai.octo.msgp.task

import java.sql.Date
import java.util
import java.util.concurrent.TimeUnit

import com.meituan.jmonitor.JMonitor
import com.sankuai.meituan.config.listener.IConfigChangeListener
import com.sankuai.msgp.common.config.MsgpConfig
import com.sankuai.msgp.common.service.org.OrgSerivce
import com.sankuai.msgp.common.utils.{MailTask, MailUtil, StringUtil}
import com.sankuai.msgp.common.utils.client.Messager.{mail, xm}
import com.sankuai.msgp.common.utils.helper.TaskTimeHelper
import com.sankuai.octo.msgp.serivce.service.ServiceCommon
import com.sankuai.octo.msgp.serivce.servicerep.ServiceDailyReport
import com.sankuai.octo.msgp.serivce.subscribe.AppkeySubscribe
import com.sankuai.octo.msgp.dao.report.ReportDailyMailDao
import com.sankuai.octo.msgp.domain.report.{DailyReportWrapper, NonstandardAppkey}
import com.sankuai.octo.msgp.serivce.DomService
import com.sankuai.octo.msgp.utils._
import com.sankuai.octo.msgp.utils.client.FreeMarkerClient
import com.sankuai.octo.mworth.util.DateTimeUtil
import com.sankuai.octo.mworth.utils.CronScheduler
import com.sankuai.octo.statistic.util.ExecutionContextFactory
import org.joda.time.DateTime
import org.quartz._
import org.slf4j.LoggerFactory

import scala.collection.JavaConverters._
import scala.concurrent._
import scala.concurrent.duration.Duration
import scala.reflect.io.File

object ReportDailyMailTask {

  //屏蔽报警列表
  var dailyReportDisablelist: List[String] = {
    val value = MsgpConfig.get("dailyReport.mail.disablelist", "")
    value.split(",").map(_.trim).toList
  }

  {
    MsgpConfig.addListener("dailyReport.mail.disablelist", new IConfigChangeListener() {
      def changed(key: String, oldValue: String, newValue: String) = {
        logger.info("dailyReport.mail.disablelist", newValue)
        dailyReportDisablelist = newValue.split(",").map(_.trim).toList
      }
    })
  }

  private[task] val logger = LoggerFactory.getLogger(this.getClass)
  private implicit val ec = ExecutionContextFactory.build(4)

  def init(): Unit = {
    val job = JobBuilder.newJob(classOf[ReportDailyMailJob]).build()
    val trigger: Trigger = TriggerBuilder.newTrigger().startNow()
      .withSchedule(CronScheduleBuilder.dailyAtHourAndMinute(8, 0)).build()
    CronScheduler.scheduleJob(job, trigger)
  }

  def asyncMail(username: String, mailname: String, appkeyList: List[String], start: Int, end: Int): Unit = {
    JMonitor.kpiForCount("daily.report")
    val startDate = new java.sql.Date(start * 1000L)
    val yesterday = new Date(startDate.getTime - DateTimeUtil.DAY_TIME)
    val dateOfLastWeek = new Date(startDate.getTime - 7 * 86400000)

    val future = Future.traverse(appkeyList) { appkey =>
      Future {
        blocking {
          //读取统计信息
          ServiceDailyReport.getAppReportData(username, appkey, startDate, yesterday, dateOfLastWeek)
        }
      }
    }

    future.foreach { futureData =>
      val day = new DateTime(startDate.getTime).toString(DateTimeUtil.DATE_DAY_FORMAT)
      val abnormalAppkeyGroups = ServiceDailyReport.getNonstandardAppkey(username)
      var dailyData = List[DailyReportWrapper]()
      if (futureData.nonEmpty) {
        val filterData = futureData.filter(_.getMainData.getCount.getValue > 0)
        dailyData = filterData.sortBy(-_.getMainData.getCount.getValue)
      }
      logger.info(s"$username,$mailname,apppkeyData size ${dailyData.size}")
      val data = new util.HashMap[String, Object]()

      val start = new DateTime(startDate.getTime).toString(DateTimeUtil.DATE_TIME_FORMAT)
      val end = new DateTime(startDate.getTime).plusDays(1).toString(DateTimeUtil.DATE_TIME_FORMAT)

      val hostUrl = ServiceCommon.OCTO_URL

      if (dailyData.nonEmpty || !abnormalAppkeyGroups.isEmpty) {
        data.put("appDailyList", dailyData.asJava)
        data.put("nonstandardAppkeyList", abnormalAppkeyGroups)
        data.put("hostUrl", hostUrl)
        data.put("start", start)
        data.put("end", end)
        data.put("username", username)
        data.put("day", day)
        val body = FreeMarkerClient.getInstance.processTemplateIntoString("dailyReportEmail.ftl", data)
        //获取发邮件的人的信息
        val users = getMailAddressee(username, mailname)
        val mailAddress = users.map(_.getEmail).distinct.toSeq
        if (StringUtil.isNotBlank(body) && mailAddress.nonEmpty) {
          logger.info(s"OCTO服务质量报表 mail to : $mailAddress")
          //          mail.notifySend(mailAddress, "OCTO服务质量报表", body)
          MailUtil.getInstance().submit(new MailTask("OCTO服务质量报表", body, mailAddress.toSet.asJava))
          ReportDailyMailDao.sendMail(username, startDate)
        }

        //统计异常日志数大于100的服务数量
        val errorLogCountOverThreshold = dailyData.filter {
          dailyRecord =>
            val mainData = dailyRecord.getMainData
            mainData.getErrorCount.getValue > 100000
        }.map(_.getAppkey)

        //发日报的时候发大象通知
        val xmNotificationInReportDaily = MsgpConfig.get("xmNotificationInReportDaily", "0").toInt
        if (xmNotificationInReportDaily == 1 && !abnormalAppkeyGroups.isEmpty || errorLogCountOverThreshold.nonEmpty) {
          val abnormalAppkeys = abnormalAppkeyGroups.asScala.flatMap {
            appkeys =>
              appkeys.asInstanceOf[util.List[NonstandardAppkey]].asScala
          }.map(_.getAppkey).distinct.toList
          doAppkeyXmNotification(users.map(_.getLogin).distinct.toSeq, abnormalAppkeys, errorLogCountOverThreshold)
        }
      }
    }
  }

  def getMailAddressee(username: String, mailname: String) = {
    val sendName = if (StringUtil.isBlank(mailname)) {
      username
    } else {
      mailname + ",yangrui08,tangye03"
    }

    val employes = sendName.split(",").flatMap {
      name =>
        OrgSerivce.employee(name)
    }
    employes
  }

  /**
    * 异常appkey通知
    *
    * @param abnormalAppkeys 异常appkey
    *
    */
  def doAppkeyXmNotification(usernames: Seq[String], abnormalAppkeys: List[String], errorLogCountOverThreshold: List[String]) = {
    val buf = scala.collection.mutable.ListBuffer.empty[String]
    if (abnormalAppkeys.nonEmpty) {
      val abnormalAppkeyLimitedStr = abnormalAppkeys.take(5).zipWithIndex.map {
        case (item, index) =>
          s"${index + 1}, $item"
      }.mkString("\n")
      buf += s"[存在异常的服务(${abnormalAppkeys.size}个服务)]\n$abnormalAppkeyLimitedStr"
      if (abnormalAppkeys.size > 5) {
        buf += s"\n......"
      }
      buf += s"\n"
    }
    if (errorLogCountOverThreshold.nonEmpty) {

      val errorlogAppkeyLimitedStr = errorLogCountOverThreshold.take(5).zipWithIndex.map {
        case (item, index) =>
          s"${index + 1}, $item"
      }.mkString("\n")
      buf += s"[异常日志数过多(${errorLogCountOverThreshold.size}个服务)]\n$errorlogAppkeyLimitedStr"
      if (errorLogCountOverThreshold.size > 5) {
        buf += s"\n......"
      }
      buf += s"\n"
    }
    val messageContent = buf.mkString("")

    val message = s"OCTO服务使用异常提醒\n" +
      messageContent +
      s"完整的服务名列表及处理方法, [请点此链接查看|http://octo.sankuai.com/repservice/daily]\n" +
      s"如有更多疑问, 请咨询OCTO技术支持"

    xm.send(usernames, message)
  }

  def main(args: Array[String]): Unit = {
    val xx: ReportDailyMailJob = new ReportDailyMailJob
    xx.calculate("zhangyun16", "zhangyun16", 1527498189000l)
  }
}


@DisallowConcurrentExecution
class ReportDailyMailJob extends Job {

  import com.sankuai.octo.msgp.task.ReportDailyMailTask.logger

  private implicit val timeout = Duration.create(20L, TimeUnit.SECONDS)


  @throws(classOf[JobExecutionException])
  override def execute(ctx: JobExecutionContext): Unit = {
    calculate()
  }

  def calculate(timeMillis: Long = System.currentTimeMillis()): Unit = {
    val (start, end) = TaskTimeHelper.getYesterDayStartEnd(timeMillis)
    val dailyMail = MsgpConfig.get("daily.mail", "false").toBoolean
    if (dailyMail) {
      logger.info(s"ReportDailyMailJob begin,start:$start,end:$end")
      DomService.clearData()
      val users = AppkeySubscribe.getSubscribedUserForReport("daily")
      logger.info(s"sendmail size ${users.size} ")
      users.foreach { case (username) =>
        val appkeys = AppkeySubscribe.getSubscribeForDailyReport(username)
        if (appkeys.nonEmpty) {
          logger.info(s"sendmail ,username:$username, appkeys size ${appkeys.size}")
          if (!ReportDailyMailTask.dailyReportDisablelist.contains(username)) {
            ReportDailyMailTask.asyncMail(username, "", appkeys, start, end)
          }
        }
      }
      logger.info(s"ReportDailyMailJob end")
    } else {
      logger.info(s"ReportDailyMailJob stop")
    }

  }

  def calculate(username: String, mailname: String, timeMillis: Long) = {
    val (start, end) = TaskTimeHelper.getYesterDayStartEnd(timeMillis)

    logger.info(s"sendmail ,username:$username,mailname:$mailname")
    //  获取业务线 -> appkey list
    val appkeys = AppkeySubscribe.getSubscribeForDailyReport(username)
    if (appkeys.nonEmpty) {
      ReportDailyMailTask.asyncMail(username, mailname, appkeys.toList, start, end)
    }
  }

}
