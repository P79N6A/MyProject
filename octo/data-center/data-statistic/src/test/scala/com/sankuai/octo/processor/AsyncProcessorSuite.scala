package com.sankuai.octo.processor

import java.util.concurrent.{Executors, TimeUnit}

import com.sankuai.octo.statistic.model.StatRange
import com.sankuai.octo.statistic.processor.AsyncProcessor
import com.sankuai.octo.statistic.util.StatThreadFactory
import org.scalatest.FunSuite

import scala.util.Random

/**
 * Created by wujinwu on 15/9/1.
 */
class AsyncProcessorSuite extends FunSuite {

  test("testRange") {
    val ranges = Seq(StatRange.values().filter(_ != StatRange.Day): _*)
    println(ranges.getClass)
  }
  test("AsyncProcessor") {
    val func = {
      x: String => {
        Thread.sleep(1)
        println(x)
      }
    }
    val ap1 = AsyncProcessor(100, func)
    for (i <- 1 to 10000) {
      ap1.put(Random.nextString(10))
    }

    val ap2 = AsyncProcessor({
      x: Int => println(x)
    })
    ap2.put(32424)

    def p(data: Double) = println(data)

    val ap3 = AsyncProcessor(p)
    ap3.put(3243.222)
    ap3.put(1.222)
    Thread.sleep(1000)
  }

  test("sleep") {
    val exe = Executors.newFixedThreadPool(Runtime.getRuntime.availableProcessors() * 100)
    (1 to Runtime.getRuntime.availableProcessors() * 100).foreach(_ => {
      val r = new Runnable {
        override def run(): Unit = Thread.sleep(10)
      }
      exe.submit(r)
    })
    while (true)
      ()
  }

  test("pool") {
    val scheduler = Executors.newScheduledThreadPool(2, StatThreadFactory.threadFactory(this.getClass))
    scheduler.scheduleAtFixedRate(new Runnable {
      def run(): Unit = {
        print("xxx")
      }
    }, 1, 1, TimeUnit.SECONDS)
    Thread.sleep(100000)
  }
}
