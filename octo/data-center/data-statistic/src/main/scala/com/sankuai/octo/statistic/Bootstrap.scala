package com.sankuai.octo.statistic

import com.meituan.jmonitor.JMonitorAgent
import com.meituan.jmonitor.config.JMonitorConfig
import com.sankuai.octo.statistic.util.{TairParam, config, tair}
import org.slf4j.LoggerFactory

/**
  * Created by wujinwu on 16/6/2.
  */
object Bootstrap {

  private val logger = LoggerFactory.getLogger(this.getClass)

  private val localAppKey = "com.sankuai.inf.data.statistic"
  private val collectorAppKey = "com.sankuai.inf.logCollector"


  def init() = {
    try {
      config.init(localAppKey, "v2", "logStatistic", "com.sankuai")
      CollectorConfig.init(collectorAppKey, "v2", "logCollector", "com.sankuai")
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
    //  初始化jmonitor
    initJMonitor()
  }

  private def initJMonitor() = {
    val config = getJMonitorConfig
    JMonitorAgent.initJMonitorAgent(config)
    JMonitorAgent.getJMonitorAgent.start()
  }

  private def getJMonitorConfig = {
    val config = JMonitorConfig.loadFromFile("jmonitor.properties")
    config
  }

}