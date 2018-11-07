package com.sankuai.octo.msgp.serivce.servicerep


import com.sankuai.msgp.common.utils.StringUtil
import com.sankuai.octo.msgp.serivce.subscribe.AppkeySubscribe
import com.sankuai.octo.msgp.dao.report.ReportDailyDao
import com.sankuai.octo.msgp.dao.report.ReportDailyDao.ReportWeeklyItem
import com.sankuai.octo.msgp.domain.report.{SeriesData, WeekData, WeekTend}
import org.joda.time.format.DateTimeFormat
import org.joda.time.{DateTime, DateTimeConstants}

import scala.collection.JavaConverters._


/**
 * Created by yves on 16/10/22.
 * 服务周报
 */
object ServiceWeeklyReport {

  def getWeeklyTrend(username: String, appkey: String, dataType: String, day: String) = {
    val appkeyList = if (appkey.equalsIgnoreCase("all")) {
      AppkeySubscribe.getSubscribeForWeeklyReport(username)
    }else{
        List(appkey)
    }

    val datePattern = DateTimeFormat.forPattern("yyyy-MM-dd")
    val dateRange = getWeekRange(day)
    val startDateString = dateRange.get("start")
    val endDateString = dateRange.get("end")

    val startDateTime = DateTime.parse(startDateString, datePattern).withTimeAtStartOfDay()
    val endDateTime = DateTime.parse(endDateString, datePattern).withTimeAtStartOfDay()
    val validDateTime = DateTime.now().withTimeAtStartOfDay().minusDays(1)

    val weekly_end_dateTime = if (validDateTime.isBefore(endDateTime.getMillis)) {
      validDateTime
    } else {
      endDateTime
    }

    val weekdays = getWeekdays(weekly_end_dateTime)
    getWeeklyData(username, appkeyList, new java.sql.Date(startDateTime.getMillis), new java.sql.Date(weekly_end_dateTime.getMillis), weekdays)
  }


  def getWeeklyData(username: String, appkeys: List[String], start: java.sql.Date, end: java.sql.Date, weekdays: List[String]) = {

    //获取日报的趋势图,一周的 可用率，QPS，tp50,tp99
    val data = ReportDailyDao.week(appkeys, start, end)
    val appkey_week_tend = data.groupBy(_.appkey).map {
      appkey_data =>
        val appkey = appkey_data._1
        val appkey_weekData = appkey_data._2
        val day_Data = appkey_weekData.groupBy(_.day.toString)
        val week_data = weekdays.map {
          day =>
            val data_list = day_Data.getOrElse(day, List(ReportWeeklyItem(appkey, java.sql.Date.valueOf(day), 0l, 0, 0, 0, 0, 0.0)))
            data_list.head
        }
        val count_trend = new WeekData("当前值", week_data.map(_.count.toDouble: java.lang.Double).asJava)
        val qps_data = week_data.map { x => math.ceil(x.qps): java.lang.Double }.asJava
        val qps_tend = new WeekData("当前值", qps_data)
        val successRatio_tend = new WeekData("当前值", week_data.map {
          x =>
            (x.successRatio * 100).toDouble: java.lang.Double
        }.asJava)
        val tp50_tend = new WeekData("当前值", week_data.map(_.tp50.toDouble: java.lang.Double).asJava)
        val tp90_tend = new WeekData("当前值", week_data.map(_.tp90.toDouble: java.lang.Double).asJava)
        val tp999_tend = new WeekData("当前值", week_data.map(_.tp999.toDouble: java.lang.Double).asJava)

        val seriesData = new SeriesData(List(count_trend).asJava, List(qps_tend).asJava, List(successRatio_tend).asJava, List(tp50_tend).asJava, List(tp90_tend).asJava, List(tp999_tend).asJava)
        new WeekTend(appkey, seriesData)
    }
    Map("xAxis" -> weekdays, "week_data" -> appkey_week_tend)
  }

  def getWeekRange(day: String) = {
    val datePattern = DateTimeFormat.forPattern("yyyy-MM-dd")
    val currentDate = if (StringUtil.isBlank(day)) {
      DateTime.now().minusDays(1)
    } else {
      DateTime.parse(day, datePattern)
    }
    val current = currentDate.toString(datePattern)
    val start = currentDate.withDayOfWeek(DateTimeConstants.MONDAY).toString(datePattern)
    val end = currentDate.withDayOfWeek(DateTimeConstants.SUNDAY).toString(datePattern)
    Map("current" -> current, "start" -> start, "end" -> end).asJava
  }

  def getWeekdays(endTime: DateTime) = {
    val weekly_end_string = endTime.toString(DateTimeFormat.forPattern("yyyy-MM-dd"))
    ServiceReport.getStrWeekDay(endTime, 6).reverse.toList.dropWhile(!_.equalsIgnoreCase(weekly_end_string)).reverse
  }

  def getAppkeyList(username: String) = {
    ("all" :: AppkeySubscribe.getSubscribeForWeeklyReport(username)).asJava
  }
}
