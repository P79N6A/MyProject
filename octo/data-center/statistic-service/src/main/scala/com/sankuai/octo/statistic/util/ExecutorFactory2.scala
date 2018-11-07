package com.sankuai.octo.statistic.util

import java.util.concurrent.RejectedExecutionException

import org.slf4j.LoggerFactory

import scala.concurrent.{blocking, _}

class ExecutorFactory2[T, U](private val processor: (T => U),
                             private val name: String = "ExecutorFactory",
                             private val parallelism: Int = 1,
                             private val block: Boolean = false) {

  import ExecutorFactory2.logger

  private implicit lazy val executionContext = ExecutionContextFactory.build(parallelism)
  private val failCallback: PartialFunction[Throwable, Unit] = {
    case e: RejectedExecutionException => logger.error(s"$name RejectedExecution", e)
    case e: Exception => logger.error("execute failed", e)
  }

  def execute(t: T) = {
    val future = Future {
      block match {
        case true =>
          blocking {
            processor(t)
          }
        case false => processor(t)
      }
    }
    future.onFailure(failCallback)
    future
  }
}

object ExecutorFactory2 {
  private[util] val logger = LoggerFactory.getLogger(this.getClass)

  def apply[T, U](processor: (T => U)) = {
    val res = new ExecutorFactory2[T, U](processor)
    res
  }

  def apply[T, U](processor: (T => U), name: String) = {
    val res = new ExecutorFactory2[T, U](processor, name)
    res
  }

  def apply[T, U](processor: (T => U), name: String, parallelism: Int) = {
    val res = new ExecutorFactory2[T, U](processor, name, parallelism)
    res
  }

  def apply[T, U](processor: (T => U), name: String, parallelism: Int, block: Boolean) = {
    val res = new ExecutorFactory2[T, U](processor, name, parallelism, block)
    res
  }
}
