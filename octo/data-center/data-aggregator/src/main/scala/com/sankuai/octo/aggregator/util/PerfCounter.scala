package com.sankuai.octo.aggregator.util

import java.util.concurrent.atomic.AtomicInteger

/**
  * Created by wujinwu on 16/1/8.
  */
object PerfCounter {
  private val writeCounter = new AtomicInteger(0)

  def getAndResetWriteCount() = {
    val count = writeCounter.get()
    writeCounter.set(0)
    count
  }

}
