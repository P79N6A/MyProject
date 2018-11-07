package com.sankuai.octo.updater.util

import org.slf4j.LoggerFactory

/**
  * Created by wujinwu on 16/6/2.
  */
object Bootstrap {

  private val logger = LoggerFactory.getLogger(this.getClass)

  private val localAppKey = Common.appkey

  def init() = {
    try {
      config.init(localAppKey, "v2", "defaultClient", "com.sankuai")
    } catch {
      case e: Exception => logger.error("config.init failed", e)
    }

  }

}