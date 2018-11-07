package com.sankuai.octo.msgp.serivce.servicerep

import java.util.concurrent.Executors

import com.sankuai.msgp.common.model.ServiceModels
import com.sankuai.msgp.common.utils.StringUtil
import com.sankuai.msgp.common.utils.helper.TaskTimeHelper
import com.sankuai.octo.msgp.serivce.service.ServiceCommon
import com.sankuai.octo.msgp.serivce.subscribe.{AppkeySubscribe, ReportSubscribe}
import com.sankuai.octo.msgp.dao.availability.AvailabilityDao
import com.sankuai.octo.msgp.dao.report._
import com.sankuai.octo.msgp.task._
import com.sankuai.octo.msgp.utils.helper.ReportHelper
import com.sankuai.octo.mworth.util.DateTimeUtil
import org.joda.time.DateTime
import org.slf4j.LoggerFactory

import scala.collection.JavaConverters._
import scala.collection.mutable.ListBuffer
import scala.collection.parallel.ForkJoinTaskSupport
import scala.concurrent.{ExecutionContext, Future}
import com.sankuai.octo.statistic.util.ExecutionContextFactory

/**
 * Created by zava on 16/3/10.
  *
 */
object ServiceReport {
  private val logger = LoggerFactory.getLogger(ServiceReport.getClass)

  private implicit val taskSupport = new ForkJoinTaskSupport((new scala.concurrent.forkjoin.ForkJoinPool(2)))

  implicit val ec = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(8))

  private val fourDecimalFormatter = "%.4f"

  case class DependCount(desc: ServiceModels.Desc, count: Int)

  case class DependValue(appkey: String, intro: String, count: Int)

  // appkey ,top90,调用量,qps,服务节点数,单机QPS
  case class PerfData(qps: Double, tp90: Int, count: Long, providerd: Int = 0, hostQps: Double = 0)

  case class ErrorCount(appkey: String, intro: String, count: Long, errorCount: Long, ratio: String)

  case class Kpi(ts: Long, appkey: String, intro: String, availability: String = "", qps: String = "", count: Long = 0L, tp90: String = "")

  case class SimpleIdcCount(idc: String, count: Long, hostCount: Int)

  case class IdcCount(appkey: String, intro: String, totalCount: Long, simpleIdcCounts: List[SimpleIdcCount])

  case class QpsPeak(appkey: String, intro: String, count: Long, hostCount: Int, avgQps: Double,
                     maxHourQps: Double, minHourQps: Double, avgHostQps: Double, maxHourHostQps: Double,
                     maxHostQps: Double)


  def getAvailability(owt: String, startDay: DateTime, limit: Int) = {
  //目前线下的可用率计算是错误的, 在此屏蔽
    val appkeys = ReportHelper.getAppkeyByOwt(owt)
    val dayRanges = getWeekDay(startDay).toList
    val tsRanges = dayRanges.map(_.getMillis / 1000)
    val availabilities = AvailabilityDao.fetchWeeklyAvailability(appkeys, "all", dayRanges.head, dayRanges.last).groupBy(_.appkey)
    val result = availabilities.map {
      case (appkey, lists) =>
        val desc = ServiceCommon.desc(appkey)
        tsRanges.map {
          ts =>
            val dailyKpiOpt = lists.find(_.ts == ts)
            if (dailyKpiOpt.isDefined) {
              val dailyKpi = dailyKpiOpt.get
              val count = dailyKpi.count
              val successCountPer = s"${fourDecimalFormatter.format(dailyKpi.successCountPer.getOrElse(0.0))}%"
              Kpi(ts, appkey, desc.intro, availability = successCountPer, count = count)
            } else {
              Kpi(ts, appkey, desc.intro, availability = "N/A", count = 0)
            }
        }
    }.toList
    //按照总量排序
    result.sortBy(x => -x.map(_.count).sum)
  }

  def getQps(owt: String, startDay: DateTime, limit: Int): List[List[Kpi]] = {
    val dayRanges = getWeekDay(startDay).toList
    val tsRanges = dayRanges.map(_.getMillis / 1000)
    val topApp = ReportQpsDao.queryTopQps(owt, new java.sql.Date(startDay.getMillis), limit)
    topApp.map {
      app =>
        val desc = ServiceCommon.desc(app.appkey)
        val list = app.weekQps.split(",").zipWithIndex.map {
          case(qps, index)=>
            if ("-1".equals(qps)) {
              Kpi(tsRanges.apply(index), app.appkey, desc.intro, qps = "")
            } else {
              Kpi(tsRanges.apply(index), app.appkey, desc.intro, qps = qps)
            }
        }.toList

        if (list.length >= 7) {
          list.take(7)
        } else {
          val len = 7 - list.size
          val lastTimestamp = tsRanges.last
          val nlist = (0 until len).map{
            index=>
              Kpi(lastTimestamp + index * DateTimeUtil.DAY_TIME / 1000l, app.appkey, desc.intro, qps = "")
          }
          val list2 = list ++ nlist
          list2
        }
    }
  }

  def getQpstp(owt: String, startDay: DateTime, limit: Int): List[List[Kpi]] = {
    val dayRanges = getWeekDay(startDay).toList
    val tsRanges = dayRanges.map(_.getMillis / 1000)
    val topApp = ReportQpsDao.queryTopQps(owt, new java.sql.Date(startDay.getMillis), limit)
    topApp.map {
      app =>
        val desc = ServiceCommon.desc(app.appkey)
        val list = app.weekTp90.split(",").zipWithIndex.map {
          case(tp90, index) =>
            if ("-1".equals(tp90)) {
              Kpi(tsRanges.apply(index), app.appkey, desc.intro, tp90 = "")
            } else {
              Kpi(tsRanges.apply(index), app.appkey, desc.intro, tp90 = tp90)
            }
        }.toList

        if (list.length >= 7) {
          list.take(7)
        } else {
          val len = 7 - list.size
          val lastTimestamp = tsRanges.last
          val nlist = (0 until len).map{
            index=>
              Kpi(lastTimestamp + index * DateTimeUtil.DAY_TIME / 1000l, app.appkey, desc.intro, qps = "")
          }
          val list2 = list ++ nlist
          list2
        }
    }
  }

  def getToptp(owt: String, startDay: DateTime, limit: Int): List[List[Kpi]] = {
    val dayRanges = getWeekDay(startDay).toList
    val tsRanges = dayRanges.map(_.getMillis / 1000)
    val topApp = ReportQpsDao.queryTopTp(owt, new java.sql.Date(startDay.getMillis), limit)
    topApp.map {
      app =>
        val desc = ServiceCommon.desc(app.appkey)
        val list = app.weekTp90.split(",").zipWithIndex.map {
          case(tp90, index) =>
            if ("-1".equals(tp90)) {
              Kpi(tsRanges.apply(index), app.appkey, desc.intro, tp90 = "")
            } else {
              Kpi(tsRanges.apply(index), app.appkey, desc.intro, tp90 = tp90)
            }
        }.toList

        if (list.length >= 7) {
          list.take(7)
        } else {
          val len = 7 - list.size
          val lastTimestamp = tsRanges.last
          val nlist = (0 until len).map{
            index=>
              Kpi(lastTimestamp + index * DateTimeUtil.DAY_TIME / 1000l, app.appkey, desc.intro, qps = "")
          }
          val list2 = list ++ nlist
          list2
        }
    }
  }

  def getDepend(appkey: String, `type`: Boolean, day: DateTime) = {
    val dependOpt = ReportDependDao.get(appkey, `type`, new java.sql.Date(day.getMillis))
    dependOpt match {
      case Some(depend) =>
        DependValue(appkey, "", depend.count.toInt)
      case None =>
        DependValue(appkey, "", 0)
    }
  }

  def getDepend(owt: String, `type`: Boolean, day: DateTime, limit: Int) = {
    ReportDependDao.query(owt, `type`, new java.sql.Date(day.getMillis), limit).map {
      depend =>
        val desc = ServiceCommon.desc(depend.appkey)
        DependValue(depend.appkey, desc.intro, depend.count.toInt)
    }
  }

  def getError(owt: String, day: DateTime, limit: Int) = {
    ReportErrorLogDao.query(owt, new java.sql.Date(day.getMillis), limit).map {
      error =>
        val desc = ServiceCommon.desc(error.appkey)
        ErrorCount(error.appkey, desc.intro, error.count, error.errorCount, f"${error.ratio}%.2f")
    }
  }

  /**
   * idc 流量分布
   */
  def getIdc(owt: String, weekStartDay: DateTime, limit: Int) = {
    val sqlDate = new java.sql.Date(weekStartDay.getMillis)
    val topApp = ReportIdcTrafficDao.sumlist(owt, sqlDate, limit)
    val idcHead = ListBuffer[String]()
    val appsTrafficMap = scala.collection.mutable.Map[String, ListBuffer[SimpleIdcCount]]()
    ReportIdcTrafficDao.query(topApp.map(_.appkey), sqlDate).foreach {
      idcTraffic =>
        val idcName = if(idcTraffic.idc.equals("unknown")){
          "other"
        }else{
          idcTraffic.idc
        }
        if (!idcHead.contains(idcTraffic.idc)) {
          idcHead.append(idcName)
        }
        val sic = SimpleIdcCount(idcName, idcTraffic.idcCount, idcTraffic.hostCount)
        val sicList = appsTrafficMap.getOrElseUpdate(idcTraffic.appkey, ListBuffer[SimpleIdcCount]())
        sicList.append(sic)
        appsTrafficMap.put(idcTraffic.appkey, sicList)
    }
    val topCountIdc = topApp.map {
      app =>
        val listIdcCount = appsTrafficMap.getOrElse(app.appkey, ListBuffer[SimpleIdcCount]())
        val simpleIdcCounts = idcHead.map {
          idc =>
            val list = listIdcCount.filter(_.idc.equals(idc))
            if (list.nonEmpty) {
              list.head
            } else {
              SimpleIdcCount(idc, 0, 0)
            }
        }.toList
        val desc = ServiceCommon.desc(app.appkey)
        IdcCount(app.appkey, desc.intro, app.count, simpleIdcCounts)
    }
    Map("idcHead" -> idcHead, "idcTraffics" -> topCountIdc)
  }

  def getQpspeak(owt: String, startDay: DateTime, limit: Int) = {
    val weekDay = getWeekDay(startDay)
    ReportQpsPeakDao.query(owt, new java.sql.Date(startDay.getMillis), limit).map {
      peak =>
        val desc = ServiceCommon.desc(peak.appkey)
        QpsPeak(peak.appkey, desc.intro, peak.count, peak.hostCount, peak.avgQps, peak.maxHourQps, peak.minHourQps, peak.avgHostQps, peak.maxHourHostQps, peak.maxHostQps)
    }
  }


  def refresh(startDay: DateTime, jobName: String) = {
    //把时间加7天
    val startTime = startDay.getMillis + 7 * 86400 * 1000
    val job = jobName match {
      case "qps" =>
        val job = new DailyQpsJob()
        Future {
          job.calculate(startTime)
        }
      case "idc" =>
        val job = new DailyIdcTrafficJob()
        Future {
          job.calculate(startTime)
        }

      case "peak" =>
        val job = new DailyQpsPeakJob()
        Future {
          job.calculate(startTime)
        }
      case "server" =>
        val job = new DailyDependJob()
        Future {
          job.calculate(startTime)
        }
      case "error" =>
        val job = new DailyErrorJob()
        Future {
          job.calculate(startTime)
        }
      case _ =>
    }
  }

  def getWeekDay(date: DateTime, days: Int = 6) = {
    val start = date.withTimeAtStartOfDay().withDayOfWeek(1)
    (0 to days).map {
      d =>
        val day = start.plusDays(d)
        day
    }
  }

  def getStrWeekDay(date: DateTime, days: Int = 6) = {
    val start = date.withTimeAtStartOfDay().withDayOfWeek(1)
    (0 to days).map {
      d =>
        val day = start.plusDays(d)
        DateTimeUtil.format(day.toDate, DateTimeUtil.DATE_DAY_FORMAT)
    }
  }


  def readMail(username: String, day: java.sql.Date): Unit = {
    ReportDailyMailDao.readMail(username, day)
  }

  def mail(username: String, mailname: String, day: java.sql.Date, mailType: Int): Unit = {
    mailType match {
      case 0 =>
        val job = new ReportDailyMailJob()
        job.calculate(username, mailname, day.getTime)
      case 1 =>
        val job = new ReportWeeklyMailJob()
        job.calculate(username, mailname, day.getTime)
    }
  }

  def getUser(username: String) = {
    AppkeySubscribe.getSubscribeForDailyReport(username)
  }


  def mailAll(day: java.sql.Date): Unit = {
    Future {
      val job = new ReportDailyMailJob()
      job.calculate(day.getTime)
    }
  }

  /**
   * 重新设置状态表
 *
   * @param day
   */
  def refreshDailyStatus(day: java.sql.Date) = {
    ReportDailyStatusDAO.delete(day)
    val job = new ReportDailyStatusResetJob
    job.calculate(day.getTime)
  }

  /**
   * 手动刷新没有计算成功的
 *
   * @param day
   */
  def refreshNotComputed(day: java.sql.Date, owt: String) = {
    Future {
      val job = new ReportDailyRefreshJob
      if(StringUtil.isNotBlank(owt)){
        job.calculate(day.getTime, owt)
      }else{
        job.calculate(day.getTime)
      }
    }
  }

  /**
   * 刷新日报数据
 *
   * @param day
   * @param owt
   * @param appkey
   * @return
   */
  def refreshDaily(day: java.sql.Date, owt: String, appkey: String) = {
    val (start, end) = TaskTimeHelper.getYesterDayStartEnd(day.getTime)
    Future {
      if (StringUtil.isNotBlank(appkey)) {
        logger.info(s"appkey :$appkey,day:$day")
        val app = ServiceCommon.desc(appkey)
        ReportDailyTask.computeReportManually(app.owt.getOrElse(""), appkey, start, end)
      } else if (StringUtil.isNotBlank(owt)) {
        logger.info(s"owt :$owt,day:$day")
        val owtAppkeyPar = ReportHelper.getAppkeyByOwt(owt).par
        owtAppkeyPar.tasksupport = taskSupport
        owtAppkeyPar.foreach {
          x =>
            ReportDailyTask.computeReportManually(owt, x, start, end)
        }
      } else {
        logger.info(s"job,day:$day")
        val job = new ReportDailyJob()
        job.calculate(day.getTime)
      }
    }
  }

}
