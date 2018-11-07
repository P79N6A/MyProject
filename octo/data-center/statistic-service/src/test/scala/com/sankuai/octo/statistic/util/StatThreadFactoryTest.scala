package com.sankuai.octo.statistic.util

import java.util.concurrent.{Executors, TimeUnit}

import org.scalatest.FunSuite

/**
  * Created by wujinwu on 15/12/29.
  */
class StatThreadFactoryTest extends FunSuite {

  test("testThreadFactory") {
    val tf = StatThreadFactory.threadFactory(this.getClass)
    val exe = Executors.newSingleThreadScheduledExecutor(tf)
    exe.scheduleAtFixedRate(new Runnable {
      override def run(): Unit = {
        val t = Thread.currentThread
        println("Simple task is running on " + t.getName + " with priority " + t.getPriority + " " + t.isDaemon)
      }

    }, 1, 1, TimeUnit.SECONDS)
    Thread.sleep(100000)
  }

}
