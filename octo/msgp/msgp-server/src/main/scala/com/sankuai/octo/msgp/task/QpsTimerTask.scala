package com.sankuai.octo.msgp.task

import java.text.DecimalFormat
import java.util.Date
import java.util.concurrent.TimeUnit

import com.sankuai.msgp.common.utils.helper.TaskTimeHelper
import com.sankuai.octo.msgp.serivce.data.DataQuery
import com.sankuai.octo.msgp.serivce.data.DataQuery.Point
import com.sankuai.octo.msgp.dao.report.ReportQpsDao
import com.sankuai.octo.msgp.dao.report.ReportQpsDao.ReportQpsDomain
import com.sankuai.octo.msgp.utils.helper.ReportHelper
import com.sankuai.octo.mworth.util.DateTimeUtil
import com.sankuai.octo.mworth.utils.CronScheduler
import com.sankuai.octo.statistic.constant.Constants
import com.sankuai.octo.statistic.helper.TimeProcessor
import com.sankuai.octo.statistic.model.StatRange
import com.sankuai.octo.statistic.util.ExecutionContextFactory
import org.quartz._
import org.slf4j.LoggerFactory

import scala.concurrent.duration.Duration
import scala.concurrent.{Future, _}

object QpsTimerTask {

  private implicit val timeout = Duration.create(20L, TimeUnit.SECONDS)
  private implicit val ec = ExecutionContextFactory.build(4)


  private[task] val logger = LoggerFactory.getLogger(this.getClass)

  private val df = new DecimalFormat("#.###")

  def init(): Unit = {
    val job = JobBuilder.newJob(classOf[DailyQpsJob]).build()
    val trigger: Trigger = TriggerBuilder.newTrigger().startNow()
      .withSchedule(CronScheduleBuilder.dailyAtHourAndMinute(5, 10)).build()
    CronScheduler.scheduleJob(job, trigger)
  }

  /**
    *
    * @param owtToAppkeyList owt -> appkey list
    * @param start           起始时间
    * @param end             终止时间
    * @return
    */
  def asyncComputeQps(owtToAppkeyList: (String, List[String]), start: Int, end: Int): Unit = {

    val timeSeries = TimeProcessor.getTimeSerie(start, end, StatRange.Day)
    val env = "prod"
    val role = "server"
    val group = "span"
    val spanname = "all"
    val localhost = "all"
    val remoteAppkey = "all"
    val remoteHost = "all"
    val unit = "Hour"
    val protocol = "thrift"
    val dataSource = "hbase"

    val owt = owtToAppkeyList._1
    val appkeyList = owtToAppkeyList._2
    //  async compute
    val future = Future.traverse(appkeyList) { appkey =>
      Future {
        blocking {
          val recordOpt = DataQuery.getDataRecord(appkey, start, end, protocol, role, "", env, unit, group, spanname,
            localhost, remoteAppkey, remoteHost, dataSource)
          recordOpt.flatMap { records =>
            records.headOption.map { record =>

              /** 天粒度数据不可用, 需要聚合小时粒度数据 */
              val hourPointList = (record.count, record.qps, record.tp90).zipped.toList
              val dailyPointList = timeSeries.map { time =>
                val dailyPoints = hourPointList.filter {
                  case (countPoint, _, _) =>
                    countPoint.ts.getOrElse(0) >= time && countPoint.ts.getOrElse(0) < time + Constants.ONE_DAY_SECONDS
                }
                val dailyCount = dailyPoints.map(_._1.y.getOrElse(0D)).sum
                val dailyTP90 = dailyPoints.map(_._3.y.getOrElse(0D)).sum.toInt
                val day = DateTimeUtil.format(new Date(time * 1000L), DateTimeUtil.DATE_TIME_FORMAT)

                val dailyCountPoint = DataQuery.Point(Some(day), Some(dailyCount), Some(time))
                val dailyTP90Point = DataQuery.Point(Some(day), Some(dailyTP90), Some(time))

                (dailyCountPoint, dailyTP90Point)
              }

              val countList = dailyPointList.map(_._1).toList
              val tp90List = dailyPointList.map(_._2).toList

              val size = countList.size
              val totalCount = countList.map(_.y.getOrElse(0D).toLong).sum
              val avgQps = totalCount.toDouble / (size * Constants.ONE_DAY_SECONDS)
              val tp90 = ((countList zip tp90List).map { case (countPoint, tp90Point) =>
                countPoint.y.getOrElse(0D) * tp90Point.y.getOrElse(0D)
              }.sum / totalCount).toInt
              val week_qps = getWeekQps(countList, timeSeries)
              val week_tp90 = getWeekTp90(tp90List, timeSeries)
              ReportQpsDao.ReportQpsDomain(owt, appkey, totalCount, avgQps, tp90, week_qps, week_tp90,
                TaskTimeHelper.getMondayDate(start), System.currentTimeMillis())
            }
          }
        }
      }
    }
    // batch insert
    future.foreach { list =>
      val resList: List[ReportQpsDomain] = list.flatten
      batchInsertQpsDomains(resList)
    }
  }

  private def batchInsertQpsDomains(resList: List[ReportQpsDomain]) = {
    if (resList != null && resList.nonEmpty) {
      try {
        ReportQpsDao.batchInsert(resList)
      } catch {
        case e: Exception =>
          logger.error("batchInsertQpsDomains fail", e)
      }
    }
  }

  private def getWeekQps(countList: List[Point], timeSeries: Seq[Int]) = {
    val valueSet = timeSeries.map { time =>
      val opt = countList.find { count =>
        count.ts.nonEmpty && count.ts.get == time
      }
      opt match {
        case Some(v) => v.y.getOrElse(-1D)
        case None => -1D
      }
    }
    valueSet.map { x =>
      val qps = x / 60 / 60 / 24
      df.format(qps)
    }.mkString(",")
  }

  private def getWeekTp90(tp90List: List[Point], timeSeries: Seq[Int]) = {
    val valueSet = timeSeries.map { time =>
      val opt = tp90List.find { tp90 =>
        tp90.ts.nonEmpty && tp90.ts.get == time
      }
      opt match {
        case Some(v) => v.y.getOrElse(-1D).toInt
        case None => -1
      }
    }
    valueSet.mkString(",")
  }

}

@DisallowConcurrentExecution
class DailyQpsJob extends Job {

  import QpsTimerTask.logger

  private implicit val timeout = Duration.create(20L, TimeUnit.SECONDS)


  @throws(classOf[JobExecutionException])
  override def execute(ctx: JobExecutionContext): Unit = {
    calculate()
  }

  def calculate(timeMillis: Long = System.currentTimeMillis()): Unit = {
    val (start, end) = TaskTimeHelper.getStartEnd(timeMillis)

    logger.info(s"DailyQpsJob begin,start:$start,end:$end")

    //  获取业务线 -> appkey list
    val owtAppkeyMap = ReportHelper.getOwtToAppkeyMap

    //  计算业务线服务最近7日QPS TOP10
    owtAppkeyMap.foreach { entry =>
      QpsTimerTask.asyncComputeQps(entry, start, end)
    }
  }

}


