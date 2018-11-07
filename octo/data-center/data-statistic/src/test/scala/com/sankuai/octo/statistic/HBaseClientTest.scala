package com.sankuai.octo.statistic

import java.util.concurrent.Executors

import org.scalatest.FunSuite

/**
  * Created by wujinwu on 15/11/27.
  */
class HBaseClientTest extends FunSuite {


  test("testHBase") {
    //    HBasePressureTest.pressureTest()

    val es = Executors.newFixedThreadPool(5)
    (1 to 100000).foreach(_ => {
      val r = new Runnable {
        override def run(): Unit = Thread.sleep(10000)
      }
      es.submit(r)
    })
    Thread.sleep(10000)
    es.shutdownNow()
    //    es.awaitTermination(10,TimeUnit.SECONDS)

  }

}
