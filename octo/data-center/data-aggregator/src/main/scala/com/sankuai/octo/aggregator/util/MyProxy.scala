package com.sankuai.octo.aggregator.util

import com.sankuai.octo.statistic.util.config
import org.slf4j.LoggerFactory

object MyProxy {
  private val logger = LoggerFactory.getLogger(this.getClass)
  private val localAppKey = "com.sankuai.inf.logCollector"

  {
    try {
      config.init(localAppKey, "v2", "defaultClient", "com.sankuai")
    } catch {
      case e: Exception => logger.error("config.init failed", e)
    }
  }

  val mcc = config
}
