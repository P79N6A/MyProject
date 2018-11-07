package com.sankuai.octo.msgp.task

import java.util.concurrent.TimeUnit

import com.sankuai.msgp.common.utils.helper.TaskTimeHelper
import com.sankuai.octo.msgp.dao.report.ReportQpsPeakDao
import com.sankuai.octo.msgp.dao.report.ReportQpsPeakDao.ReportQpsPeakDomain
import com.sankuai.octo.msgp.serivce.data.DataQuery
import com.sankuai.octo.msgp.serivce.data.DataQuery.Point
import com.sankuai.octo.msgp.utils.helper.ReportHelper
import com.sankuai.octo.mworth.utils.CronScheduler
import com.sankuai.octo.statistic.constant.Constants
import com.sankuai.octo.statistic.util.ExecutionContextFactory
import org.quartz._
import org.slf4j.LoggerFactory

import scala.concurrent._
import scala.concurrent.duration.Duration

object QpsPeakTimerTask {

  private implicit val ec = ExecutionContextFactory.build(4)
  private implicit val timeout = Duration.create(20L, TimeUnit.SECONDS)

  private[task] val logger = LoggerFactory.getLogger(this.getClass)

  def init(): Unit = {
    val job = JobBuilder.newJob(classOf[DailyQpsPeakJob]).build()
    val trigger = TriggerBuilder.newTrigger().startNow()
      .withSchedule(CronScheduleBuilder.dailyAtHourAndMinute(5, 30)).build()
    CronScheduler.scheduleJob(job, trigger)
  }


  /**
    *
    * @param owtToAppkeyList owt -> appkey list
    * @param start           起始时间
    * @param end             终止时间
    * @return
    */
  def asyncComputeQpsPeak(owtToAppkeyList: (String, List[String]), start: Int, end: Int) = {

    //  计算业务线峰值QPS
    val env = "prod"
    val role = "server"
    val group = "spanLocalHost"
    val spanname = "all"
    val localhost = "*"
    val remoteAppkey = "all"
    val remoteHost = "all"
    val unitMinute = "Minute"
    val unitHour = "Hour"
    val unitDay = "Day"
    val protocol = "thrift"
    val dataSource = "hbase"

    val owt = owtToAppkeyList._1
    val appkeyList = owtToAppkeyList._2
    //  async compute
    val future = Future.traverse(appkeyList) { appkey =>
      Future {
        blocking {
          val nonAllRecords = DataQuery.getDataRecord(appkey, start, end, protocol, role, "", env, unitHour, group, spanname, localhost,
            remoteAppkey, remoteHost, dataSource).getOrElse(List[DataQuery.DataRecord]())

          /** 注意: localhost为 * 并不能保证查到的数据包含all 数据, 因此需要重查一遍all数据 */
          val allRecords = DataQuery.getDataRecord(appkey, start, end, protocol, role, "", env, unitHour, group, spanname, "all",
            remoteAppkey, remoteHost, dataSource).getOrElse(List[DataQuery.DataRecord]())

          if (allRecords.nonEmpty && nonAllRecords.nonEmpty) {
            val allCountList = allRecords.head.count.filter(c => c.y.nonEmpty && c.y.get.toLong != 0)
            val allCounts = allCountList.map(_.y.get)
            if (allCounts.nonEmpty) {

              val appKeyTotalTraffic = allCounts.sum
              //  计算服务的平均QPS
              val appKeyAvgQps = appKeyTotalTraffic / (allCounts.size * Constants.ONE_HOUR_SECONDS)
              val (max, min) = getMaxAndMin(allCountList)

              val maxStart = max.ts.get
              val maxEnd = maxStart + Constants.ONE_HOUR_SECONDS
              val minStart = min.ts.get
              val minEnd = minStart + Constants.ONE_HOUR_SECONDS

              val maxOpt = DataQuery.getDataRecord(appkey, maxStart, maxEnd, protocol, role, "", env, unitMinute, group, spanname, localhost = Constants.ALL,
                remoteAppkey, remoteHost, dataSource)
              val minOpt = DataQuery.getDataRecord(appkey, minStart, minEnd, protocol, role, "", env, unitMinute, group, spanname, localhost = Constants.ALL,
                remoteAppkey, remoteHost, dataSource)

              val appKeyMinuteMaxQps = maxOpt.flatMap { list =>
                list.headOption.map { record =>
                  record.count.filter(c => c.y.nonEmpty && c.y.get.toLong != 0).map(_.y.get).max
                }
              } match {
                case Some(thisMax) => thisMax / Constants.ONE_MINUTE_SECONDS
                case None =>
                  //  分钟级数据max计算错误时降级
                  max.y.get / Constants.ONE_HOUR_SECONDS
              }

              val appKeyMinuteMinQps = minOpt.flatMap { list =>
                list.headOption.map { record =>
                  record.count.filter(c => c.y.nonEmpty && c.y.get.toLong != 0).map(_.y.get).min
                }
              } match {
                case Some(thisMin) => thisMin / Constants.ONE_MINUTE_SECONDS
                case None =>
                  //  分钟级数据min计算错误时降级
                  min.y.get / Constants.ONE_HOUR_SECONDS
              }

              //  分钟级别的host count数据不可靠,用离线天数据替换
              val flag = nonAllRecords.size == 1 || nonAllRecords.exists { item => item.tags.localhost.get == "0.0.0.0" }
              //目前(2016/09/19)天粒度数据不可用, 因此hostcount = nonAllRecords.map(_.tags.localhost).distinct.size
              val hostCount =
                if (flag) {
                  //  实时计算出现脏数据,查询离线运算数据
                  logger.info("illegal appkey:{}", appkey)
                  val dayResOption = DataQuery.getDataRecord(appkey, start, end, protocol, role, "", env, unitDay, group,
                    spanname, localhost, remoteAppkey, remoteHost, dataSource)
                  dayResOption match {
                    case Some(dayData) =>
                      val dayNonAll = dayData.filterNot { record =>
                        record.tags.localhost.isEmpty || record.tags.localhost.get.isEmpty
                      }
                      if (dayNonAll.isEmpty) {
                        1
                      } else {
                        dayNonAll.map(_.tags.localhost).distinct.size
                      }
                    case None => nonAllRecords.size
                  }
                } else {
                  nonAllRecords.map(_.tags.localhost).distinct.size
                }

              val hostAvgQps = appKeyAvgQps / hostCount
              val hostMinuteMaxQps = appKeyMinuteMaxQps / hostCount
              // TODO: 该值因为脏数据导致计算出错,后续可修复
              val hostMaxQps = nonAllRecords.map { record =>
                val filterList = record.count.filter { c =>
                  c.y.nonEmpty && c.y.get.toLong != 0
                }.map(_.y.get)
                if (filterList.nonEmpty) {
                  filterList.max
                } else {
                  0
                }
              }.max / Constants.ONE_HOUR_SECONDS
              Some(ReportQpsPeakDomain(owt, appkey, appKeyTotalTraffic.toLong, hostCount, appKeyAvgQps, appKeyMinuteMaxQps, appKeyMinuteMinQps,
                hostAvgQps, hostMinuteMaxQps, hostMaxQps, TaskTimeHelper.getMondayDate(start), System.currentTimeMillis()))
            } else {
              logger.info(s"$appkey allCounts.nonEmpty: ${allCounts.nonEmpty}")
              None
            }
          } else {
            logger.info(s"$appkey allRecords.nonEmpty: ${allRecords.nonEmpty}, nonAllRecords.nonEmpty: ${nonAllRecords.nonEmpty}")
            None
          }
        }
      }
    }

    // batch insert
    future.foreach { list =>
      val resList: List[ReportQpsPeakDomain] = list.flatten
      if (resList != null && resList.nonEmpty) {
        try {
          ReportQpsPeakDao.batchInsert(resList)
        } catch {
          case e: Exception =>
            logger.info("batchInsertQpsPeakDomains fail", e)
        }
      }
    }
  }

  private def getMaxAndMin(values: List[Point]) = {
    var max = values.head
    var min = values.head
    values.foreach { value =>
      if (value.y.get > max.y.get) {
        max = value
      } else if (value.y.get < min.y.get) {
        min = value
      } else {
        ()
      }
    }
    (max, min)
  }


}

@DisallowConcurrentExecution
class DailyQpsPeakJob extends Job {

  import QpsPeakTimerTask.logger

  private implicit val timeout = Duration.create(20L, TimeUnit.SECONDS)

  @throws(classOf[JobExecutionException])
  override def execute(ctx: JobExecutionContext): Unit = {
    calculate()
  }

  def calculate(timeMillis: Long = System.currentTimeMillis()): Unit = {
    val (start, end) = TaskTimeHelper.getStartEnd(timeMillis)
    //  过滤掉数据错误的日期

    logger.info(s"DailyQpsPeakJob begin,start:$start, end:$end")

    //  获取业务线 -> appkey list
    val owtAppkeyMap = ReportHelper.getOwtToAppkeyMap

    owtAppkeyMap.foreach {
      entry =>
        QpsPeakTimerTask.asyncComputeQpsPeak(entry, start, end)
    }
  }
}

