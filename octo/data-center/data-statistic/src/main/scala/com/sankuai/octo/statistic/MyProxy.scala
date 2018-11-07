/*
package com.sankuai.octo.statistic

import com.sankuai.octo.statistic.util.{TairParam, config, tair}
import org.slf4j.LoggerFactory

object MyProxy {
  private val logger = LoggerFactory.getLogger(this.getClass)
  private val localAppKey = "com.sankuai.inf.data.statistic"

  {
    try {
      config.init(localAppKey, "v2", "defaultClient", "com.sankuai")
    } catch {
      case e: Exception => logger.error("config.init failed", e)
    }

    // tair的初始化还依赖config。。。
    try {
      tair.init(TairParam.master(), TairParam.slave(), TairParam.group(),
        localAppKey, TairParam.remoteAppKey(), TairParam.area())
    } catch {
      case e: Exception => logger.error("config.init failed", e)
    }
  }

  val mcc = config
  val kv = tair
}
*/
