package com.sankuai.octo.statistic.util

import java.util.concurrent.{ForkJoinPool, TimeUnit}

import scala.concurrent.ExecutionContext

/**
  * Created by wujinwu on 16/3/28.
  */
object ExecutionContextFactory {

  def build(parallelism: Int = Runtime.getRuntime.availableProcessors()) = {
    val forkJoinPool = new ForkJoinPool(parallelism,
      ForkJoinPool.defaultForkJoinWorkerThreadFactory, null, true)
    Runtime.getRuntime.addShutdownHook(new Thread() {
      override def run(): Unit = {
        forkJoinPool.shutdown()
        forkJoinPool.awaitTermination(10, TimeUnit.SECONDS)
      }
    })
    ExecutionContext.fromExecutor(forkJoinPool)
  }

}
