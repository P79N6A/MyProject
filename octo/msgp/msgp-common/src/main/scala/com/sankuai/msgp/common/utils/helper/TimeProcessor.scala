package com.sankuai.msgp.common.utils.helper

import org.joda.time.{DateTime, DateTimeZone}

object TimeProcessor {
  private val minute = 60
  private val hour = 60 * 60
  private val day = 60 * 60 * 24

  // 获取一个时间戳开始分钟的时间戳
  def getMinuteStart(ts: Int) = {
    ts / 60 * 60
  }

  // 获取一个时间戳开始分钟的时间戳
  def getMinuteStartMs(ts: Long) = {
    ts / 60000 * 60000
  }


  // 获取一个时间戳开始小时的时间戳
  def getHourStart(ts: Int) = {
    val dateTime = new DateTime(ts * 1000L, DateTimeZone.forID("Asia/Shanghai"))
    (dateTime.withMinuteOfHour(0).withSecondOfMinute(0).getMillis / 1000).toInt
  }

  // 获取一个时间戳开始天的时间戳
  def getDayStart(ts: Int) = {
    val dateTime = new DateTime(ts * 1000L, DateTimeZone.forID("Asia/Shanghai"))
    (dateTime.withTimeAtStartOfDay().getMillis / 1000).toInt
  }

  def getDayEnd(ts: Int) = {
    val dateTime = new DateTime(ts * 1000L, DateTimeZone.forID("Asia/Shanghai"))
    (dateTime.withTimeAtStartOfDay().minusDays(-1).getMillis / 1000).toInt - 1
  }

  def getDateTimeFormatStr(ts: Int, pattern: String) = {
    val dateTime = new DateTime(ts * 1000L, DateTimeZone.forID("Asia/Shanghai"))
    dateTime.toString(pattern)
  }

  def getMonthLastDay(ts: Int) = {
    val dateTime = new DateTime(ts * 1000L, DateTimeZone.forID("Asia/Shanghai"))
    val monthLastDay = dateTime.dayOfMonth().withMaximumValue()
    monthLastDay
  }

  def getDayLastHour(dateTime: DateTime) = {
    val dayLastHour = dateTime.hourOfDay().withMaximumValue()
    dayLastHour
  }

  def getDayLastMinute(dateTime: DateTime) = {
    val dayLastMinute = dateTime.minuteOfDay().withMaximumValue()
    dayLastMinute
  }
}


