package com.sankuai.octo.service

import java.util.concurrent.{ForkJoinPool, TimeUnit}

import scala.concurrent.ExecutionContext

object ExecutionContextFactory {

  def build(parallelism: Int) = {
    val forkJoinPool = new ForkJoinPool(parallelism,
      ForkJoinPool.defaultForkJoinWorkerThreadFactory, null, true)
    // add shutdown hook
    Runtime.getRuntime.addShutdownHook(new Thread() {
      override def run(): Unit = {
        forkJoinPool.shutdown()
        forkJoinPool.awaitTermination(10, TimeUnit.SECONDS)
      }
    })
    ExecutionContext.fromExecutor(forkJoinPool)
  }

}