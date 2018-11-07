/*
package com.sankuai.octo.statistic.exporter

import java.util.concurrent.{ConcurrentLinkedQueue, Executors, TimeUnit}

import com.sankuai.octo.statistic.helper.PerfHelper
import com.sankuai.octo.statistic.model._
import com.sankuai.octo.statistic.util.{ExecutorFactory, HBaseClient, StatThreadFactory}
import org.slf4j.LoggerFactory

/** 将数据输出至HBase,内部执行了异步操作
  * Created by wujinwu on 15/11/26.
  */
//
object HBaseExporter extends AbstractExporter {

  private val logger = LoggerFactory.getLogger(this.getClass)

  private val executor = Executors.newSingleThreadExecutor(StatThreadFactory.threadFactory(this.getClass))

  val queue = new ConcurrentLinkedQueue[StatData]()

  val processoreNum = Runtime.getRuntime.availableProcessors()

  val putExecutor = ExecutorFactory(putByRange, "hbase-putExecutor", processoreNum, processoreNum * 2)

  {
    //  职责分离,只需要一个线程不断pull queue,写入动作委托给其他线程
    val task = new Runnable {
      override def run(): Unit = {
        while (!executor.isShutdown || !queue.isEmpty) {
          val seq = (1 to 2000).flatMap(_ => {
            val item = queue.poll()
            if (item == null) {
              Thread.sleep(1)
              None
            } else {
              Some(item)
            }
          })
          try {
            putExecutor.submit(seq)
          } catch {
            case e: Exception => logger.error("putToHBase Fail", e)
          }
        }
      }
    }
    executor.submit(task)
    //  优雅关闭队列消费者
    Runtime.getRuntime.addShutdownHook(new Thread() {
      override def run(): Unit = {
        try {
          while (!queue.isEmpty) {
            //  等待消费者消耗完队列中消息
            Thread.sleep(10)
          }
          executor.shutdown()
          executor.awaitTermination(10, TimeUnit.SECONDS)
        } catch {
          case e: Exception => logger.error("shutdown fail", e)
        }
      }
    })

  }


  override def export(statData: StatData): Unit = putToQueue(statData)

  private def putToQueue(statData: StatData) = queue.offer(statData)


  private def putByRange(statDataSeq: Seq[StatData]): Unit = {
    try {
      //  根据时间维度划分数据,通过range指定
      if (statDataSeq.nonEmpty) {
        statDataSeq.groupBy(_.getRange).foreach {
          case (range, seqByRange) => HBaseClient.putPerfDataList(seqByRange.map(PerfHelper.statDataToPerfData), range)
        }
      }
    } catch {
      case e: Exception => logger.error("HBaseExporter putByRange", e.getMessage)
    }
  }
}
*/
