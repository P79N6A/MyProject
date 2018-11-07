package com.sankuai.octo.report

import com.sankuai.msgp.common.utils.StringUtil
import com.sankuai.octo.msgp.serivce.data.DataQuery
import com.sankuai.octo.msgp.serivce.servicerep.ServiceReport
import com.sankuai.octo.msgp.task.{DailyIdcTrafficJob, ReportDailyMailTask, ReportDailyTask}
import com.sankuai.octo.msgp.utils.helper.ReportHelper
import com.sankuai.octo.mworth.util.DateTimeUtil
import org.joda.time.format.{DateTimeFormat, DateTimeFormatter}
import org.joda.time.{DateTime, LocalDate}
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{BeforeAndAfter, FunSuite}

@RunWith(classOf[JUnitRunner])
class ServiceReportSuite extends FunSuite with BeforeAndAfter {

//  before{
//    service.listService
//  }

  test("user") {
    ReportHelper.refreshUserAppkeyMap
    val appkeys = ServiceReport.getUser("chenxi18")
    println(appkeys)
  }

  test("qps") {
    val day = getDate("2016-03-07")
    val qps = ServiceReport.getQps("inf",day,10)
    println(qps)
  }
  test("idc") {
    val day = getDate("2016-03-07")
    val qps = ServiceReport.getIdc("inf",day,10)
    println(qps)
  }
  test("server") {
    val days = getDays("2016-03-07")
    val deps = ServiceReport.getDepend("waimai",false,days.apply(0),10)
    println(deps)
  }

  test("client") {
    val days = getDays("2016-03-07")
    val deps = ServiceReport.getDepend("waimai",true,days.apply(0),10)
    println(deps)
  }

  test("time"){
    val timeMillis = System.currentTimeMillis();
    val dateTime = new DateTime(timeMillis)
    val (start, end) = if (dateTime.getDayOfWeek == 1) {
      //  计算上周
      val left = (dateTime.minusDays(7).getMillis / 1000).toInt
      val right = (dateTime.minusDays(1).getMillis / 1000).toInt
      (left, right)
    } else {
      val diff: Int = dateTime.getDayOfWeek - 1
      val monday = dateTime.minusDays(diff)
      val left = (monday.getMillis / 1000).toInt
      val right = (dateTime.minusDays(1).getMillis / 1000).toInt
      (left, right)
    }
    println(start)
    println(end)
  }
  test("refresh"){
    val date: DateTime = getDate("2016-02-07")
    ServiceReport.refresh(date, "qps")
    ServiceReport.refresh(date, "idc")
    ServiceReport.refresh(date, "error")
    ServiceReport.refresh(date, "peak")
    ServiceReport.refresh(date, "server")
    Thread.sleep(1000000)
  }

  test("computeAppkeyDaily") {
    val startDate = new DateTime("2017-03-14")
    val endDate = new DateTime("2017-03-15")
    val start = (startDate.withTimeAtStartOfDay().getMillis / 1000 ).toInt
    val end = (endDate.withTimeAtStartOfDay().getMillis / 1000 ).toInt - 1
    val owt = "inf"
    val appkey = "com.sankuai.inf.mnsc"
//    println(ReportDailyTask.computeAppkeyDaily(owt, appkey, start, end))
    println( ReportDailyTask.computeReportDaily(owt,appkey, "", start, end))
//    println( ReportDailyTask.computeAppkeyDaily("inf", "com.sankuai.inf.mnsc", start, end))
    //Thread.sleep(1000000)
  }
  test("maxhost"){
    //http://data.octo.vip.sankuai.com/api/history/data?role=server&remoteAppkey=all
    // &localhost=*&dataSource=hbase&spanname=*&appkey=waimai_i&end=1489402800
    // &unit=Minute&protocolType=thrift&start=1489399200&group=spanLocalhost&remoteHost=all&env=prod
   val appkey = "waimai_i"
    val start =  1489399200
    val end =  1489402800
    val maxPoint = DataQuery.Point(Some("2017 03-13 19:00, Mon"),Some(18.13),Some(1489399200))
   val point = ReportDailyTask.getMaxHostQps(appkey,"all",start,end,maxPoint)
    println(point)
  }

  test("computeIDCCount") {
    val job  = new DailyIdcTrafficJob
    job.calculate()
    Thread.sleep(1000000)
  }

  private val formatter: DateTimeFormatter = DateTimeFormat.forPattern("yyyy-MM-dd")

  private def getDays(day: String, days: Int = 5) = {
    val date = if (day == null) LocalDate.now else formatter.parseLocalDate(day)
    val nowDay = date.toDateTimeAtStartOfDay
    (days until 0 by -1).map {
      d =>
        val day = nowDay.plusDays(-d)
        day
    }
  }

  private def getDate(day: String): DateTime = {
    var time: Long = System.currentTimeMillis
    if (StringUtil.isNotBlank(day)) {
      time = DateTimeUtil.parse(day, DateTimeUtil.DATE_DAY_FORMAT).getTime
    }
    new DateTime(time)
  }

  test("doAppkeyXmNotification") {
    ReportDailyMailTask.doAppkeyXmNotification(Seq("tangye03"), List("appkey1", "appkey2", "appkey2", "appkey2", "appkey2", "appkey2"), List())

  }
}
