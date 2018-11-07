package com.sankuai.octo.dashboard

import java.util.Date
import java.util.concurrent.TimeUnit

import com.sankuai.octo.msgp.serivce.servicerep.ServiceKpiReport
import com.sankuai.octo.mworth.util.DateTimeUtil
import org.scalatest.FunSuite

import scala.concurrent.duration.Duration

/**
 * Created by zava on 16/5/24.
 */
class ServiceKpiReportSuite extends FunSuite{
  test("query") {
    val list =  ServiceKpiReport.kpi("com.sankuai.dataapp.recsys.useridmap",new Date(1464343800*1000L),new Date(1464344340*1000L),"","","")
    println(list)
  }
  test("perfKpi") {
    val start = DateTimeUtil.parse("2016-06-02 17:50:00",DateTimeUtil.DATE_TIME_FORMAT).getTime
    val start_time = (start/1000 /60*60).toInt- 180
    val list =  ServiceKpiReport.perfKpi("com.sankuai.mobile.group.sinai.spec",start_time-300,start_time+60,time=start_time)
    println(list)

    val day_start = DateTimeUtil.parse("2016-06-01 17:50:00",DateTimeUtil.DATE_TIME_FORMAT).getTime
    val day_start_time = (day_start/1000 /60*60).toInt- 180
    val day_list =  ServiceKpiReport.perfKpi("com.sankuai.mobile.group.sinai.spec",day_start_time-300,day_start_time+60,time=day_start_time)
    println(day_list)

    val week_start = DateTimeUtil.parse("2016-05-26 17:50:00",DateTimeUtil.DATE_TIME_FORMAT).getTime
    val week_start_time = (week_start/1000 /60*60).toInt- 180
    val week_list =  ServiceKpiReport.perfKpi("com.sankuai.mobile.group.sinai.spec",week_start_time-300,week_start_time+60,time=week_start_time)
    println(week_list)
  }
  test("source") {
    val day_start = DateTimeUtil.parse("2016-06-01 17:50:00",DateTimeUtil.DATE_TIME_FORMAT).getTime
    val day_start_time = (day_start/1000 /60*60).toInt- 180
    val list =  ServiceKpiReport.perfKpi("com.sankuai.inf.logCollector",day_start_time-300,day_start_time+60,day_start_time,"server","spanLocalHost","all")
    val list1 =  ServiceKpiReport.perfKpi("com.sankuai.inf.logCollector",day_start_time-300,day_start_time+60,day_start_time,"server","SpanRemoteApp","*")
    val list2 =  ServiceKpiReport.perfKpi("com.sankuai.inf.logCollector",day_start_time-300,day_start_time+60,day_start_time,"client","SpanRemoteApp","*")
    println(list)
    println(list1)
    println(list2)
  }
  test("timeout"){
    val timeout = Duration.create(30, TimeUnit.SECONDS)

     println(timeout)
  }
}
