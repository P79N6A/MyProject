package com.sankuai.octo.aggregator.util

import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.{Executors, TimeUnit}

import com.sankuai.octo.statistic.constant.Constants
import com.sankuai.octo.statistic.helper.DailyMetricHelper
import com.sankuai.octo.statistic.util.StatThreadFactory
import org.joda.time.DateTime

/**
  * Created by wujinwu on 15/12/30.
  */
object LogMetricCounter {

  val perfHourCounter = new AtomicLong()

  val perfDayCounter = new AtomicLong()

  private val scheduler = Executors.newSingleThreadScheduledExecutor(StatThreadFactory.threadFactory(this.getClass))

  private val task = new Runnable {
    private var hourStart = getHourStart()
    private var dayStart = DailyMetricHelper.dayStart()

    override def run(): Unit = {
      val time = (System.currentTimeMillis() / 1000L).toInt
      if (time - hourStart > Constants.ONE_HOUR_SECONDS) {
        hourStart = getHourStart()
        perfHourCounter.set(0)
      }
      if (time - dayStart > Constants.ONE_DAY_SECONDS) {
        dayStart = DailyMetricHelper.dayStart()
        perfDayCounter.set(0)
      }
    }
  }

  scheduler.scheduleAtFixedRate(task, 1, 1, TimeUnit.MINUTES)

  def incrCounter() = {
    perfHourCounter.incrementAndGet()
    perfDayCounter.incrementAndGet()
  }
  
  private def getHourStart(ts: Long = System.currentTimeMillis()) = {
    val dateTime = new DateTime(ts)
    (dateTime.withMinuteOfHour(0).withSecondOfMinute(0).getMillis / 1000L).toInt
  }


}
