/*
package com.sankuai.octo.statistic.exporter

import java.util.concurrent.{ConcurrentLinkedQueue, Executors, TimeUnit}

import com.sankuai.octo.statistic.model.StatData
import com.sankuai.octo.statistic.util.Falcon.FalconData
import com.sankuai.octo.statistic.util.{Falcon, StatThreadFactory}
import org.slf4j.LoggerFactory

// 基于Falcon将数据输出
object FalconExporter extends AbstractExporter {
  private val logger = LoggerFactory.getLogger(this.getClass)
  private val workerCount = 3
  val queue = new ConcurrentLinkedQueue[StatData]()
  private val executor = Executors.newScheduledThreadPool(workerCount, StatThreadFactory.threadFactory(this.getClass))

  private val task = new Runnable {
    override def run(): Unit = {
      val seq = (1 to 100).flatMap(_ => {
        Option(queue.poll())
      })
      if (seq.nonEmpty) {
        try {
          val sendList = seq.flatMap(statToFalconDataList)
          if (sendList.nonEmpty) {
            sendToFalcon(sendList)
          }
        } catch {
          case e: Exception => logger.error("sendToFalcon Fail", e)
        }
      }
    }
  }

  override def export(statData: StatData): Unit = {
    queue.offer(statData)
  }

  private def statToFalconDataList(statData: StatData) = {
    Falcon.statToFalconData(statData)
  }

  private def sendToFalcon(falconDataList: Seq[FalconData]): Unit = {
    val start = System.currentTimeMillis()
    Falcon.send(falconDataList)
    logger.debug(s"Falcon send size ${falconDataList.size}, time: ${System.currentTimeMillis() - start}, data: ${falconDataList.head}")
  }

  (1 to workerCount).foreach(_ => executor.scheduleAtFixedRate(task, 1, 100, TimeUnit.MILLISECONDS))

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
}*/
