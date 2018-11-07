package com.sankuai.octo.msgp.serivce.falcon

import java.util.concurrent.TimeUnit

import dispatch.Defaults._
import dispatch._
import org.slf4j.LoggerFactory
import play.api.libs.json.Json

import scala.concurrent.Await
import scala.concurrent.duration.Duration

object AlarmQuery {
  private val logger = LoggerFactory.getLogger(this.getClass)

  private implicit val timeout = Duration.create(20000L, TimeUnit.MILLISECONDS)

  private val alarmUrl = "http://p.falcon.sankuai.com/"
  private val alarmSearchUrl = s"$alarmUrl/api/alarm/search/"
  private val alarmAllUrl = s"$alarmUrl/api/alarm/all/"


  case class AlarmData(ActionId: Int, counter: String, currentStep: Int, endpoint: String, expressionId: Int, func: String, grpname: String,
                       id: String, leftValue: String, link: String, maxStep: Int, metric: String, note: String, operator: String,
                       priority: Int, rightValue: String, status: String, strategyId: Int, templateId: Int, timestamp: Int, tplname: String)


  case class AlarmData2(Config_link: String, Counter: String, Dashboard_link: String,
                        Duration: String, Func_and_value: String, Grp_name: String,
                        Priority_and_step: String, Solved_link_id: String, Timestamp: Int,
                        Tpl_name: String, alarm_service: String){
  }



  implicit val alarmDataReads = Json.reads[AlarmData]
  implicit val alarmDataWrites = Json.writes[AlarmData]

  implicit val alarmData2Reads = Json.reads[AlarmData2]
  implicit val alarmData2Writes = Json.writes[AlarmData2]

  case class Data(alarm_data: List[AlarmData])

  case class Data2(alarm_data: List[AlarmData2])

  implicit val dataReads = Json.reads[Data]
  implicit val dataWrites = Json.writes[Data]

  implicit val data2Reads = Json.reads[Data2]
  implicit val data2Writes = Json.writes[Data2]

  case class AlarmMSG(msg: String, ok: Boolean, data: Data)

  case class AlarmMSG2(msg: String, ok: Boolean, data: Data2)

  implicit val alarmMSGReads = Json.reads[AlarmMSG]
  implicit val alarmMSGWrites = Json.writes[AlarmMSG]

  implicit val alarmMSG2Reads = Json.reads[AlarmMSG2]
  implicit val alarmMSG2Writes = Json.writes[AlarmMSG2]

  //部门的报警数
  case class OwtData(owt: String, count: Int, alarm_data: List[AlarmData])

  case class Srv(owt: String, srv: String)

  //status:0正常,1报警
  case class SrvData(owt: String, srv: String, count: Int, alarm_data: List[AlarmData2], status: Boolean = true)

  /**
   * 获取所有的报警
   */
  def allAlarm(nodeSearch: String, endTime: Int) = {
    val postReq = url(alarmAllUrl).POST << Map("node_search" -> nodeSearch, "end_time" -> endTime.toString)
    try {
      val future = Http(postReq OK as.String)
      val text = Await.result(future, timeout)
      Json.parse(text).validate[AlarmMSG].asOpt
    } catch {
      case e: Exception =>
        logger.error(s"get allAlarm failed nodeSearch:$nodeSearch endTime:$endTime", e)
        None
    }
  }

  /**
   * 拆分报警
   * 1:mysql总量报警
   * 2:各个部门的报警
   */

  def owtAlarmCount(nodeSearch: String, endTime: Int): List[OwtData] = {
    val retOpt = allAlarm(nodeSearch, endTime)
    if (retOpt.nonEmpty) {
      val groupbyOwt = retOpt.get.data.alarm_data.filter(_.tplname.contains("MYSQL")).groupBy {
        data =>
          val grpname = data.grpname.split("&")
          val pdl = grpname.filter(_.contains("pdl")).head.split("=").last
          pdl
      }
      val countList = groupbyOwt.map { case (owt, list) =>
        val size = list.size

        OwtData(owt, size, list)
      }.toList.sortBy(-_.count)
      val allCount = countList.map(_.count).sum
      OwtData("all", allCount, List()) :: countList
    } else {
      List(OwtData("all", 0, List()))
    }
  }

  def query(nodeSearch: String = "", duration: String = "", endpoint: String = "", metric: String = "") = {
    val postReq = url(alarmSearchUrl).POST << Map("node_search" -> nodeSearch
      , "duration" -> duration.toString
      , "endpoint" -> endpoint
      , "metric" -> metric
    )
    try {
      val future = Http(postReq OK as.String)
      val text = Await.result(future, timeout)
      Json.parse(text).validate[AlarmMSG2].asOpt
    } catch {
      case e: Exception =>
        logger.error(s"get allAlarm failed nodeSearch:$nodeSearch duration:$duration", e)
        None
    }
  }

  /**
   * 拆分查询报警
   * 1:mysql总量报警
   * 2:各个部门,srv的报警
   */

  def querySrvAlarm(nodeSearch: String, duration: Int): List[SrvData] = {
    val retOpt = query(nodeSearch, duration.toString)
    if (retOpt.nonEmpty) {
      val groupbyOwt = retOpt.get.data.alarm_data.filter{alarm_data=>alarm_data.Tpl_name.contains("MYSQL") && !alarm_data.Priority_and_step.contains("P3")}.groupBy {
        data =>
          val grpname = data.Grp_name.split("&")
          val pdl = grpname.filter(_.contains("pdl")).head.split("=").last
          val srv = grpname.filter(_.contains("srv")).head.split("=").last
          Srv(pdl, srv)
      }
      val countList = groupbyOwt.map { case (srv, list) =>
        val size = list.size
        val status = getStatus(list)
        SrvData(srv.owt, srv.srv, size, list, status)
      }.toList.sortBy(-_.count)
      countList
    } else {
      List()
    }
  }

  private def getStatus(data: List[AlarmData2]) = {
    val groupByPri = data.groupBy {
      priorityData =>
        val priority = priorityData.Priority_and_step.substring(1, 3)
        priority
    }
    val p0Size = groupByPri.getOrElse("P0", List()).size
    val p1Size = groupByPri.getOrElse("P1", List()).size
    //P0,P1告警,P2~P3不报警
    if (p0Size + p1Size>0) {
      false
    }else {
      true
    }
  }
}
