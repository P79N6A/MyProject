package com.sankuai.octo.msgp.serivce.data

import java.util.concurrent.{Executors, TimeUnit}

import com.sankuai.msgp.common.service.org.OpsService
import com.sankuai.msgp.common.utils.client.TairClient
import com.sankuai.octo.msgp.dao.report.ErrorDashBoardDao
import com.sankuai.octo.msgp.dao.report.ErrorDashBoardDao.{ErrorDashBoardCount, ErrorDashBoardDomain}
import com.sankuai.octo.msgp.serivce.falcon.AlarmQuery
import com.sankuai.octo.msgp.serivce.falcon.AlarmQuery.AlarmData2
import com.sankuai.octo.msgp.serivce.monitor.MonitorEvent
import com.sankuai.octo.msgp.serivce.service.ServiceCommon
import com.sankuai.octo.msgp.utils.helper.ReportHelper
import org.apache.commons.lang3.StringUtils
import org.slf4j.{Logger, LoggerFactory}
import play.api.libs.json.Json

import scala.concurrent.duration.Duration

/**
 * 1:获取errorLog 异常数
 * 2:获取falcon异常数量
 * 3:获取Octo报警数
 */
object ErrorQuery {
  private val logger: Logger = LoggerFactory.getLogger(ErrorQuery.getClass)

  private implicit val timeout = Duration.create(20000L, TimeUnit.MILLISECONDS)

  case class ErrorData(owt: String, appkey: String, node: String, errorCount: Int, falconCount: Int, octoCount: Int)

  implicit val errorDataReads = Json.reads[ErrorData]
  implicit val errorDataWrites = Json.writes[ErrorData]

  private val scheduler = Executors.newScheduledThreadPool(1)

  private val TAIR_PREFIX = "MSGP_ERROR_DASH_"
  private val ERROR_DASHBOARD_TIME = "error_dashboard_time"

  /**
   * 每分钟统计下异常数量
   */
  def start() {
    scheduler.scheduleAtFixedRate(new Runnable {
      def run(): Unit = {
        try {
          val now = (System.currentTimeMillis() / 1000 / 60 * 60).toInt
          val errorTime = TairClient.get(ERROR_DASHBOARD_TIME).getOrElse(now.toString).toInt
          refreshErrorDashBoard(errorTime, now)
          TairClient.put(ERROR_DASHBOARD_TIME, now.toString)
        } catch {
          case e: Exception => logger.error(s"refresh errorCount fail $e")
        }
      }
    }, 0, 1, TimeUnit.MINUTES)
  }

  def refreshErrorDashBoard(start: Int, end: Int): Unit = {
    val count = (end - start) / 60
    (1 to count).foreach {
      index =>
        val start_time = start + (index - 1) * 60;
        val end_time = start + index * 60;
        try {
          logger.info(s"refresh errorCount $start_time $end_time")
          calculationData(start_time, end_time)
        }
        catch {
          case e: Exception => logger.error(s"refreshError ${start}, ${end} fail", e)
        }
    }
  }


  case class AppkeyNode(appkey: String, node: String)

  case class AppkeyAlarmData(appkeyNode: AppkeyNode, alarmData: AlarmData2)

  case class AppkeyAlarmCount(appkeyNode: AppkeyNode, count: Int, alarm_data: List[AlarmData2])


  def getAppkeyFalconAlarm(appkey: String = "", start: Int, end: Int): List[AlarmData2] = {
    val optAppkey = OpsService.getOpsAppkey(appkey)
    val node = optAppkey.data match {
      case Some(appkeySrv) =>
        s"corp=meituan&owt=${appkeySrv.owt}&pdl=${appkeySrv.pdl}&srv=${appkeySrv.srv}&cluster=prod"
      case None =>
        ""
    }
    if (StringUtils.isNotBlank(node)) {
      val retOpt = AlarmQuery.query(node, (end - start).toString)
      if (retOpt.nonEmpty) {
        retOpt.get.data.alarm_data
      } else {
        List()
      }
    } else {
      List()
    }
  }

  def getFalconAlarm(start: Int, end: Int): List[AppkeyAlarmCount] = {
    val retOpt = AlarmQuery.query("", (end - start).toString)
    if (retOpt.nonEmpty) {
      val appAlarmData = retOpt.get.data.alarm_data.filter {
        alarm_data =>
          !alarm_data.Tpl_name.contains("MYSQL") && !alarm_data.Priority_and_step.contains("P3") && alarm_data.Grp_name.contains("cluster=prod")
      }.flatMap {
        data =>
          val grpname = data.Grp_name
          val appkeyNodes = getNodeAppkey(grpname)
          appkeyNodes.map {
            appkeyNode =>
              AppkeyAlarmData(appkeyNode, data)
          }
      }
      val appkeyFalconData = appAlarmData.groupBy(_.appkeyNode).map {
        groupData =>
          val alarm_datas = groupData._2.map(_.alarmData)
          AppkeyAlarmCount(groupData._1, groupData._2.size, alarm_datas)
      }
      appkeyFalconData.toList.sortBy(-_.count)
    } else {
      List()
    }
  }

  /**
   * 计算errolog 报警
   * 计算octo 报警
   */
  def calculationData(start: Int, end: Int) = {
    val errorCount = ReportHelper.errorCount(start, end)
    val octoList = MonitorEvent.eventCount(start * 1000L, end * 1000L)

    //保存
    val errors = errorCount.flatMap {
      error =>
        try {
          val desc = ServiceCommon.desc(error.appkey)
          val owt = desc.owt.getOrElse("")
          if (StringUtils.isNotBlank(owt)) {
            Some(ErrorDashBoardDomain(desc.owt.getOrElse(""), error.appkey, "", 0, error.logCount, 0, start, System.currentTimeMillis() / 1000))
          } else {
            None
          }
        }
        catch {
          case e: Exception => logger.error(s"calculationData errorCount appkey ${error.appkey}, ${start}, ${end} fail", e)
            None
        }
    }
    ErrorDashBoardDao.batchInsert(errors)

    octoList.foreach {
      octo =>
        try {
          val desc = ServiceCommon.desc(octo.appkey)
          val owt = desc.owt.getOrElse("")
          if (StringUtils.isNotBlank(owt)) {
            val domain = ErrorDashBoardDomain(desc.owt.getOrElse(""), octo.appkey, "", octo.count, 0, 0, start, System.currentTimeMillis() / 1000)
            ErrorDashBoardDao.updateOctoCount(domain)
          }
        } catch {
          case e: Exception => logger.error(s"calculationData octoList appkey ${octo.appkey}, ${start}, ${end} fail", e)
        }
    }
  }

  /**
   * 1：数据库读取 octo报警
   * 2：falcon 报警实时读取
   */

  def queryAlarm(start: Int, end: Int, sort: String, limit: Int = 10) = {
    val falconAlarmData = getFalconAlarm(start, end)
    if (sort.equals("falcon_count asc")) {
      falconAlarmData.sortBy(_.count)
    }
    val list = ErrorDashBoardDao.query(start, end, sort)

    val resultData = if (sort.contains("falcon_count")) {
      val size = if (falconAlarmData.size < limit) {
        falconAlarmData.size
      } else {
        limit
      }
      (0 until size).map {
        i =>
          val falConAlarmCount = falconAlarmData(i)
          val appkey = falConAlarmCount.appkeyNode.appkey
          val desc = ServiceCommon.desc(appkey)
          val octoOpt = list.filter(_.appkey.equals(appkey)).headOption
          val octoCount = if (octoOpt.isDefined) {
            octoOpt.get.octoCount
          } else {
            0
          }
          ErrorDashBoardCount(desc.owt.getOrElse(""), appkey, falConAlarmCount.appkeyNode.node, octoCount, falConAlarmCount.count)
      }
    } else {
      val size = if (list.size < limit) {
        list.size
      } else {
        limit
      }
      (0 until size).map {
        i =>
          val octoAlarmCount = list(i)
          val falconOpt = falconAlarmData.filter(_.appkeyNode.appkey.equals(octoAlarmCount.appkey)).headOption
          val falconCount = if (falconOpt.isDefined) {
            falconOpt.get.count
          } else {
            0
          }
          val newDashCount = octoAlarmCount.copy(falconCount = falconCount)
          newDashCount
      }
    }
    resultData
  }


  private def getNodeAppkey(nodes: String): List[AppkeyNode] = {
    val appkeyNodes = nodes.split(",").flatMap {
      node =>
        getOpsSrvAppkey(node).map {
          appkey =>
            AppkeyNode(appkey, node)
        }
    }
    appkeyNodes.toList
  }

  private def getOpsSrvAppkey(node: String): List[String] = {
    try {
      val arr_node = node.split("&")
      if (arr_node.size < 4) {
        List()
      } else {
        val corp_node = arr_node.apply(0).split("=").apply(1)
        val owt_node = arr_node.apply(1).split("=").apply(1)
        val pdl_node = arr_node.apply(2).split("=").apply(1)
        val srv_node = arr_node.apply(3).split("=").apply(1)
        val oppsSrv = OpsService.OpsSrv(corp_node, owt_node, pdl_node, srv_node)
        OpsService.srvAppkeyCache.get(oppsSrv)
      }
    }
    catch {
      case e: Exception =>
        logger.error(s"get getOpsSrv node ${node}", e)
        List()
    }
  }

}
