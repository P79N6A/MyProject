package com.sankuai.octo.msgp.task

import com.sankuai.msgp.common.utils.helper.TaskTimeHelper
import org.joda.time.format.DateTimeFormat
import org.joda.time.{DateTime, Days}
import org.scalatest.FunSuite

class TaskTimeHelperTest extends FunSuite {

  test("testGetStartEnd") {
    val res = TaskTimeHelper.getStartEnd()
    println(res)
  }
  test("getYesterDayStartEnd") {
    val res = TaskTimeHelper.getYesterDayStartEnd()
    println(res)
  }
  test("testMonday") {
    val now = DateTime.now()
    val dtf = DateTimeFormat.forPattern("yyyyMMdd")
    val a = DateTime.parse("20160329", dtf)
    println(Days.daysBetween(now, a).getDays)

    println(TaskTimeHelper.getMondayDate((System.currentTimeMillis() / 1000L).toInt))
  }

  test("getmail"){
   val mails =  ReportDailyMailTask.getMailAddressee("wangyanzhao","")
    println(mails)
  }
}
