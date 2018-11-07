package com.sankuai.octo.aggregator

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

import scala.collection.JavaConverters._

object counter {
  var metrics = new ConcurrentHashMap[String, AtomicLong]().asScala

  def update(appkey: String, spanname: String, start: Long, count: Int) = {
    val hour = start / 3600000
    val all = metrics.getOrElseUpdate(s"$appkey|$hour", new AtomicLong(0L))
    all.addAndGet(count.toLong)
    val span = metrics.getOrElseUpdate(s"$appkey|$spanname|$hour", new AtomicLong(0L))
    span.addAndGet(count.toLong)
  }

  def clear(hour: Int) = {
    val tmp = metrics.filterKeys(key => key.split("\\|").last.toInt > hour).toMap
    metrics = new ConcurrentHashMap[String, AtomicLong](tmp.asJava).asScala
  }

  def main(args: Array[String]) {
    update("fdsfs", "rwer", System.currentTimeMillis(), 33)
    update("fdsfs", "rwer", System.currentTimeMillis(), 222)
    println(metrics)
    clear((System.currentTimeMillis() / 3600000).toInt)
    println(metrics)
  }
}
