package com.sankuai.octo

import com.sankuai.octo.msgp.serivce.data
import com.sankuai.octo.msgp.serivce.data.DataQuery
import com.sankuai.octo.msgp.task.AvailabilityTask
import org.joda.time.{DateTime, LocalDate}
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{BeforeAndAfter, FunSuite}

@RunWith(classOf[JUnitRunner])
class DataSuite extends FunSuite with BeforeAndAfter {

  test("spanname") {
    println(data.Kpi.spannameList("mobile.sievetrip"))
    val ts = ((System.currentTimeMillis() - 7 * 86400 * 1000)/1000).toInt
    println(ts)
  }

  test("date") {
    val date = DateTime.now()
    println(date)
    val ts = date.toLocalDate.toDateTimeAtStartOfDay.getMillis
    println(ts)
  }



  test("syncDay logCollector") {
    val appkey = "com.sankuai.inf.logCollector"
    val dateTime = LocalDate.now().toDateTimeAtStartOfDay
    data.Kpi.syncDailyKpi(appkey, "prod", dateTime)
  }

  test("start startSyncDay") {
    data.Kpi.startJob
    while(true) {
      Thread.sleep(1000)
    }
  }

  test("getAvailabilityDetails") {
    val date = new DateTime("2017-03-10").withTimeAtStartOfDay()
    //val result = Availability.insertAvailabilityManually(date)com.sankuai.inf.msgp

    val result = AvailabilityTask.getAvailability("com.sankuai.recsys.recsys.poimodelservice ", "prod", date)
    Thread.sleep(600000)
  }

  test("saveAvailability") {
    val date = new DateTime("2017-06-23").withTimeAtStartOfDay()
    //val result = Availability.insertAvailabilityManually(date)
    val appkeys = List("com.sankuai.inf.mnsc")
    val result = AvailabilityTask.saveAvailability(appkeys, date)
    Thread.sleep(600000)
  }

  test("dailyStatWithAvailability") {
    val date = new DateTime("2016-09-27").withTimeAtStartOfDay()
    val result = DataQuery.getDailyPerformance("com.sankuai.pay.bankgw.payroute", "", "prod", date)
    Thread.sleep(600000)
  }
}
