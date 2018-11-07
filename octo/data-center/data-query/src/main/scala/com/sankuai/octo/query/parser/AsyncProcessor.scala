package com.sankuai.octo.query.parser

import java.util.concurrent.{ConcurrentLinkedQueue, Executors, TimeUnit}

import org.slf4j.{Logger, LoggerFactory}

class AsyncProcessor[T](val processor: (T => Unit)) {
  private val LOG: Logger = LoggerFactory.getLogger(AsyncProcessor.getClass)
  var workerCount = 5
  val executor = Executors.newFixedThreadPool(workerCount)
  val queue = new ConcurrentLinkedQueue[T]()

  def this(count: Int, processor: (T => Unit)) {
    this(processor)
    workerCount = count
  }

  def runner(id: Int) = new Runnable {
    val rid = id

    def run(): Unit = {
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

  def init() {
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

  def put(data: T) = {
    queue.offer(data)
  }
}

object AsyncProcessor {
  def apply[T](processor: (T => Unit)) = new AsyncProcessor[T](processor)

  def apply[T](workerCount: Int, processor: (T => Unit)) = new AsyncProcessor[T](workerCount, processor)

  def main(args: Array[String]) {
    val ap1 = new AsyncProcessor[String](x => {
      Thread.sleep(1)
      println(x)
    })
    for (i <- 1 to 100) {
      ap1.put("xxxx")
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
}
