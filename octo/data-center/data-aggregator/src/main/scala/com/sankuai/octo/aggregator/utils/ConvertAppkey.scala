package com.sankuai.octo.aggregator.utils

import java.util.concurrent.{Executors, TimeUnit}

import org.slf4j.LoggerFactory


/**
 * 通过 client 统计 remote appkey的性能数据appkey
 */
object ConvertAppkey {
  private val logger = LoggerFactory.getLogger(this.getClass)

  private var appKeyConvertSet = Set[String]()
  private val scheduler = Executors.newScheduledThreadPool(1)

  def start(): Unit = {
    scheduler.scheduleAtFixedRate(new Runnable {
      def run(): Unit = {
        try {
          logger.info(s"刷新 tair appKeyConvert" )
          refresh()
          logger.info(s"tair appKeyConvert size: ${appKeyConvertSet.size}" )
        } catch {
          case e: Exception => logger.error(s"refresh cellar groupAppkeys fail $e")
        }
      }
    }, 0l, 30l, TimeUnit.MINUTES)
  }


  def needConvert(appKey: String): Boolean = {
    appKeyConvertSet.contains(appKey)
  }


  def refresh(): Unit = {
    val cellarRet = cellarServer.cellarAppkeys()
    logger.info(s"cellar data ${cellarRet}")
    appKeyConvertSet = if (cellarRet.nonEmpty) {
      cellarRet.get.groupAppkeys.toSet
    } else {
      Set[String]()
    }
  }

}
