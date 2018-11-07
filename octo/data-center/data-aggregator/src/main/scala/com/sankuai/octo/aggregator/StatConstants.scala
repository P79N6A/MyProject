package com.sankuai.octo.aggregator

import com.sankuai.meituan.config.listener.IConfigChangeListener
import com.sankuai.octo.statistic.util.config
import org.slf4j.LoggerFactory

object StatConstants {
  private val logger = LoggerFactory.getLogger(StatConstants.getClass)

  val seed = new java.util.Random()

  def drop() : Boolean = {
    seed.nextInt(100) < dropRatio
  }

  var dropRatio: Int = {
    val value = config.get("drop_ratio", "110")
    logger.warn("drop_ratio:{}", value)
    config.addListener("drop_ratio", new IConfigChangeListener() {
      def changed(key: String, oldValue: String, newValue: String) = {
        logger.warn("drop_ratio:{}", newValue)
        dropRatio = newValue.toInt
      }
    })
    value.toInt
  }

}
