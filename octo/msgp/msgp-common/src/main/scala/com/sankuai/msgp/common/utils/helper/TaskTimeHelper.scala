package com.sankuai.msgp.common.utils.helper

import java.sql.Date

import org.joda.time.{DateTime, DateTimeConstants}

object TaskTimeHelper {

  def getStartEnd(timeMillis: Long = System.currentTimeMillis()) = {
    val dateTime = new DateTime(timeMillis)
    if (dateTime.getDayOfWeek == DateTimeConstants.MONDAY) {
      //  计算上周
      val left = TimeProcessor.getDayStart((dateTime.minusWeeks(1).getMillis / 1000).toInt)
       val right = TimeProcessor.getDayStart((dateTime.getMillis / 1000).toInt) - 1
       (left, right)
    } else {
      val monday = dateTime.withDayOfWeek(DateTimeConstants.MONDAY)
       val left = TimeProcessor.getDayStart((monday.getMillis / 1000).toInt)
       val right = TimeProcessor.getDayStart((dateTime.getMillis / 1000).toInt) - 1
      (left, right)
    }
  }

  def getMondayDate(ts: Int) = {
    val monday = new DateTime(ts * 1000L).withDayOfWeek(DateTimeConstants.MONDAY)
    new Date(monday.getMillis)
  }

  def getLastMondayDate(ts: Int) = {
    val lastMonday = new DateTime(ts * 1000L).minusWeeks(1).withDayOfWeek(DateTimeConstants.MONDAY)
    new Date(lastMonday.getMillis)
  }
  def getYesterDayStartEnd(timeMillis: Long = System.currentTimeMillis()) = {
    val dateTime = new DateTime(timeMillis)
    val yesterDay = dateTime.minusDays(1)
      //  计算上周
      val left = TimeProcessor.getDayStart((yesterDay.getMillis / 1000).toInt)
      val right = TimeProcessor.getDayEnd((yesterDay.getMillis / 1000).toInt)
      (left, right)
  }
}
