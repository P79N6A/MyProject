package com.sankuai.octo.statistic.util

import java.util.concurrent._

import org.slf4j.{Logger, LoggerFactory}

class ExecutorFactory[T](private val processor: (T => Unit),
                         private val name: String = "ExecutorFactory",
                         private val minWorker: Int = 1,
                         private val maxWorker: Int = 5,
                         private val maxSize: Int = 2000000,
                         private val keepAliveTime: Long = 30) {
  val executor = new ThreadPoolExecutor(minWorker, maxWorker, keepAliveTime, TimeUnit.SECONDS,
    new LinkedBlockingQueue[Runnable](maxSize), StatThreadFactory.threadFactory(name))
  private val LOG: Logger = LoggerFactory.getLogger(this.getClass)

  def submit(t: T) {
    val task = new Callable[Unit] {
      def call(): Unit = {
        processor(t)
      }
    }
    try {
      executor.submit(task)
    } catch {
      case e: RejectedExecutionException =>
        LOG.error(s"$name RejectedExecution,msg:{}", e.getMessage)
      case e: Exception =>
        LOG.error("submit failed,msg:{}", e.getMessage)
    }
  }

  private def init() {
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
}

object ExecutorFactory {
  def apply[T](processor: (T => Unit)) = {
    val res = new ExecutorFactory[T](processor)
    res.init()
    res
  }

  def apply[T](processor: (T => Unit), name: String) = {
    val res = new ExecutorFactory[T](processor, name)
    res.init()
    res
  }

  def apply[T](processor: (T => Unit), name: String, minWorker: Int, maxWorker: Int) = {
    val res = new ExecutorFactory[T](processor, name, minWorker, maxWorker)
    res.init()
    res
  }

  def apply[T](processor: (T => Unit), name: String, minWorker: Int, maxWorker: Int, maxSize: Int) = {
    val res = new ExecutorFactory[T](processor, name, minWorker, maxWorker, maxSize)
    res.init()
    res
  }

  def apply[T](processor: (T => Unit), name: String, minWorker: Int, maxWorker: Int, maxSize: Int, keepAliveTime: Long) = {
    val res = new ExecutorFactory[T](processor, name, minWorker, maxWorker, maxSize, keepAliveTime)
    res.init()
    res
  }
}
