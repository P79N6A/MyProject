package com.sankuai.octo.mworth.utils

import java.util.concurrent.{TimeUnit, ConcurrentLinkedQueue, Executors}

import org.slf4j.{LoggerFactory, Logger}

/**
 * Created by zava on 15/12/4.
 */
class AsyncProcessor [T] private(private val workerCount: Int = 5, private val processor: (T => Unit)) {
  val executor = Executors.newFixedThreadPool(workerCount)
  val queue = new ConcurrentLinkedQueue[T]()
  private val LOG: Logger = LoggerFactory.getLogger(this.getClass)

  def put(data: T) = {
    queue.offer(data)
  }

  private def init() {
    for (id <- 1 to workerCount) executor.submit(runner(id))
    Runtime.getRuntime.addShutdownHook(new Thread() {
      override def run() {
        try {
          executor.shutdown()
          executor.awaitTermination(10, TimeUnit.SECONDS)
        } catch {
          case e: Exception => LOG.error("", e)
        }
      }
    })
  }

  private def runner(id: Int) = new Runnable {
    val rid = id

    override def run(): Unit = {
      while (!executor.isShutdown || !queue.isEmpty) {
        try {
          val data = queue.poll()
          if (data != null) {
            processor(data)
          } else {
            Thread.sleep(10)
          }
        } catch {
          case e: Exception => LOG.warn(s"process failed", e)
        }
      }
    }
  }
}

object AsyncProcessor {
  def apply[T](processorMethod: (T => Unit)) = {
    val res = new AsyncProcessor[T](processor = processorMethod)
    res.init()
    res
  }

  def apply[T](workerCount: Int, processor: (T => Unit)) = {
    val res = new AsyncProcessor[T](workerCount, processor)
    res.init()
    res
  }

}