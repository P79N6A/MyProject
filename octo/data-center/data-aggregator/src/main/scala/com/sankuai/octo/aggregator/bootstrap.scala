package com.sankuai.octo.aggregator

import com.meituan.mtrace.Tracer
import com.sankuai.octo.aggregator.utils.ConvertAppkey
import com.sankuai.octo.statistic.util.common
import org.slf4j.LoggerFactory

class bootstrap {
  private val logger = LoggerFactory.getLogger(classOf[bootstrap])

  def init() = {
    try {
      Tracer.setThreshold("com.sankuai.inf.logCollector", 20)
      Tracer.setThreshold("com.sankuai.inf.data.statistic", 20)
      Tracer.setThreshold("com.sankuai.fe.mta.parser", 50)

      ConvertAppkey.start()
    }
    catch {
      case e: Exception => logger.error("bootstrap.init failed", e)
    }
  }
}
