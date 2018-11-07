package com.sankuai.octo.msgp.task

import java.util.concurrent.TimeUnit

import com.sankuai.msgp.common.utils.helper.{CommonHelper, TaskTimeHelper}
import com.sankuai.octo.msgp.serivce.data.DataQuery
import com.sankuai.octo.msgp.dao.report.ReportIdcTrafficDao
import com.sankuai.octo.msgp.dao.report.ReportIdcTrafficDao.ReportIdcTrafficDomain
import com.sankuai.octo.msgp.task.QpsPeakTimerTask.logger
import com.sankuai.octo.msgp.utils.helper.ReportHelper
import com.sankuai.octo.msgp.utils.helper.ReportHelper._
import com.sankuai.octo.mworth.utils.CronScheduler
import com.sankuai.octo.statistic.util.ExecutionContextFactory
import org.quartz._
import org.slf4j.LoggerFactory

import scala.concurrent._
import scala.concurrent.duration.{Duration, FiniteDuration}

object IdcTrafficTimerTask {

  private[task] val logger = LoggerFactory.getLogger(this.getClass)

  private implicit val ec = ExecutionContextFactory.build(4)

  def init(): Unit = {
    val job = JobBuilder.newJob(classOf[DailyIdcTrafficJob]).build()
    val trigger = TriggerBuilder.newTrigger().startNow()
      .withSchedule(CronScheduleBuilder.dailyAtHourAndMinute(4, 30)).build()
    CronScheduler.scheduleJob(job, trigger)
  }

  /**
    *
    * @param owtToAppkeyList owt -> appkey list
    * @param start           起始时间
    * @param end             终止时间
    * @return
    */
  def asyncComputeIdcTraffic(owtToAppkeyList: (String, List[String]), start: Int, end: Int) = {

    val env = "prod"
    val role = "server"
    val group = "spanLocalHost"
    val spanname = "all"
    val localhost = "*"
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
          val recordOpt = DataQuery.getDataRecord(appkey, start, end, protocol, role, "", env, unit, group, spanname, localhost,
            remoteAppkey, remoteHost, dataSource)
          recordOpt.flatMap { records =>
            //  过滤掉localhost非法的情况
            val filterList = records.filterNot { record =>
              record.tags.localhost.isEmpty || record.tags.localhost.get.isEmpty || record.tags.localhost.get == "all"
            }
            if (filterList.nonEmpty) {
              //  根据idc分组
              val resList = filterList.groupBy { record =>
                CommonHelper.ip2IDC(record.tags.localhost.get)
              }.map { case (idc, recordByIdc) =>
                //  对idc下多个localhost,分别求得localhost的traffic,求和
                val idcTraffic = recordByIdc.map { localhostRecord =>
                  val localhostTraffic = localhostRecord.count.map {
                    _.y.getOrElse(0D).toLong
                  }.sum
                  localhostTraffic
                }.sum
                val idcHostCount = recordByIdc.size
                ReportIdcTrafficDomain(owt, appkey, idc, idcHostCount, idcTraffic, TaskTimeHelper.getMondayDate(start), System.currentTimeMillis())
              }.toSeq
              Some(resList)
            } else {
              logger.info(s"$appkey filterList.nonEmpty: ${filterList.nonEmpty}")
              None
            }
          }
        }
      }
    }
    // batch insert
    future.foreach { list =>
      val resList = list.flatten
      resList.foreach { seq =>
        batchInsertIdcTrafficDomains(seq)
      }
    }
  }

  private def batchInsertIdcTrafficDomains(resList: Seq[ReportIdcTrafficDomain]) = {
    if (resList != null && resList.nonEmpty) {
      try {
        ReportIdcTrafficDao.batchInsert(resList)
      } catch {
        case e: Exception =>
          logger.error("batchInsertIdcTrafficDomains fail", e)
      }
    }
  }

  def getIdcTraffic(owt: String, weekDay: java.sql.Date, limit: Int) = {
    val idcTraffics = ReportIdcTrafficDao.query(owt, weekDay)
    val groupByAppKey = idcTraffics.groupBy(_.appkey)
    val allAppKeyCountMap = groupByAppKey.map { case (appkey, list) =>
      val totalCount = list.map(_.idcCount).sum
      (appkey, totalCount)
    }
    val appKeyCountMap = allAppKeyCountMap.toSeq.sortBy(-_._2).take(limit)
    val resList = appKeyCountMap.flatMap { case (appkey, count) =>
      val list = groupByAppKey(appkey)
      val items = CommonHelper.IDC_SET.map { idc =>
        list.find(_.idc == idc) match {
          case Some(v) => IdcTrafficDesc(appkey, idc, Some(IdcAndHost(v.idcCount, v.hostCount)))
          case None => IdcTrafficDesc(appkey, idc, None)
        }
      }
      items
    }
    resList
  }

  def calcIDCbyHand(timeMillis: Long = System.currentTimeMillis()): Unit = {
    val (start, end) = TaskTimeHelper.getStartEnd(timeMillis)

    logger.info(s"DailyIdcTrafficJob begin,start:$start,end:$end")

    //  获取业务线 -> appkey list
    val owtAppkeyMap = ReportHelper.getOwtToAppkeyMap

    owtAppkeyMap.foreach { entry =>
      IdcTrafficTimerTask.asyncComputeIdcTraffic(entry, start, end)
    }
  }

}

@DisallowConcurrentExecution
class DailyIdcTrafficJob extends Job {

  import IdcTrafficTimerTask.logger

  private implicit val timeout = Duration.create(20L, TimeUnit.SECONDS)

  @throws(classOf[JobExecutionException])
  override def execute(ctx: JobExecutionContext): Unit = {
    calculate()
  }

  def calculate(timeMillis: Long = System.currentTimeMillis()): Unit = {
    val (start, end) = TaskTimeHelper.getStartEnd(timeMillis)

    logger.info(s"DailyIdcTrafficJob begin,start:$start,end:$end")

    //  获取业务线 -> appkey list
    val owtAppkeyMap = ReportHelper.getOwtToAppkeyMap

    owtAppkeyMap.foreach { entry =>
      IdcTrafficTimerTask.asyncComputeIdcTraffic(entry, start, end)
    }
  }
}

case class IdcAndHost(idcCount: Long, hostCount: Int)

case class IdcTrafficDesc(appkey: String, idc: String, idcAndHost: Option[IdcAndHost])

