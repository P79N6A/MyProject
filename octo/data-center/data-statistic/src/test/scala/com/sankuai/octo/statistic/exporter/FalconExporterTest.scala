package com.sankuai.octo.statistic.exporter

import java.util.concurrent.{Executors, TimeUnit}

import com.sankuai.octo.statistic.util.StatThreadFactory
import org.scalatest.FunSuite

/**
  * Created by wujinwu on 16/5/19.
  */
class FalconExporterTest extends FunSuite {
  test("test") {
    val workerCount = 5
    val executor = Executors.newScheduledThreadPool(workerCount, StatThreadFactory.threadFactory(this.getClass))
    val task = new Runnable {
      override def run(): Unit =
        Thread.sleep(10)
    }
    (1 to workerCount).foreach(_ => executor.scheduleAtFixedRate(task, 1, 10, TimeUnit.MILLISECONDS))
    while (true)
      ()
  }

}
