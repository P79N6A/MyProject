package com.sankuai.octo.aggregator

import java.util.concurrent.CountDownLatch

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{BeforeAndAfter, FunSuite}

import scala.util.Random

@RunWith(classOf[JUnitRunner])
class MonitorSuite extends FunSuite with BeforeAndAfter {

  def randomInt(max: Int = 100) = Random.nextInt(max)

  test("monitor") {
    val num = 5
    val countdownLatch = new CountDownLatch(num)
    (1 to num).foreach {
      tid =>
        new Thread() {
          override def run() {
            (1 to 300).foreach {
              x =>
                //monitor.update("mtupm", System.currentTimeMillis(), randomInt(10), randomInt(100))
                Thread.sleep(1000)
            }
            countdownLatch.countDown()
          }
        }.start()
    }
    countdownLatch.await()
  }
}
