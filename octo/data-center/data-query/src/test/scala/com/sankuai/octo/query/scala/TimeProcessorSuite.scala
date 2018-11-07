package com.sankuai.octo.query.scala

import com.sankuai.octo.statistic.helper.TimeProcessor
import com.sankuai.octo.statistic.model.StatRange
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{BeforeAndAfter, FunSuite}

@RunWith(classOf[JUnitRunner])
class TimeProcessorSuite extends FunSuite with BeforeAndAfter {
  test("times") {
    println(TimeProcessor.getTimeSerie(1449741599, 1449744359, StatRange.Minute))
    println(TimeProcessor.getTimeSerie(1449706260, 1449741659, StatRange.Hour))
    println(TimeProcessor.getTimeSerie(1449483060, 1449742260, StatRange.Day))
    val now = System.currentTimeMillis()

    assert(TimeProcessor.getMinuteStartMs(now) / 1000 == TimeProcessor.getMinuteStart((now / 1000).toInt))

  }
}
