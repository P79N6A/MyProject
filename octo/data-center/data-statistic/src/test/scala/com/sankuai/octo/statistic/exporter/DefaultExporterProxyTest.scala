package com.sankuai.octo.statistic.exporter

import java.util.concurrent.{Executors, ForkJoinPool, TimeUnit}

import com.sankuai.octo.statistic.util.StatThreadFactory
import org.scalatest.FunSuite

import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by wujinwu on 16/1/18.
  */
class DefaultExporterProxyTest extends FunSuite {

  test("testExport") {
    implicit val ec: ExecutionContext = {
      val forkJoinPool = new ForkJoinPool(2,
        ForkJoinPool.defaultForkJoinWorkerThreadFactory, null, true)
      ExecutionContext.fromExecutor(forkJoinPool)
    }

    val tagScheduler = Executors.newSingleThreadScheduledExecutor(StatThreadFactory.threadFactory(this.getClass))
    val tagTimerTask = new Runnable {
      override def run(): Unit = {
        (1 to 10).foreach { entry =>
          Future {
            println(s"hello $entry,${Thread.currentThread().getName}")
            throw new RuntimeException("aaaaaa")
          }
        }
      }
    }
    tagScheduler.scheduleAtFixedRate(tagTimerTask, 1, 1, TimeUnit.SECONDS)
    while (true) ()
  }

}
