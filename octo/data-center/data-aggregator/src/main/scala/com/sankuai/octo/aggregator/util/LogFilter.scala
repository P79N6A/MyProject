package com.sankuai.octo.aggregator.util

import com.sankuai.meituan.config.listener.IConfigChangeListener
import org.slf4j.LoggerFactory

object LogFilter {
  private val logger = LoggerFactory.getLogger(this.getClass)
  private val config = MyProxy.mcc
  private val APPKEY_LOG_LIST = "AppKeyLogList"

  private var appKeyLogSet = {
    try {
      val value = config.get(APPKEY_LOG_LIST, "")
      constructList(value)
    } catch {
      case e: Exception =>
        logger.error("appKeyLogSet init failed", e)
        Set[String]()
    }
  }

  def needLog(appKey: String): Boolean = {
    appKeyLogSet.contains(appKey)
  }

  private def constructList(value: String) = {
    if (value == null || value == "") {
      Set[String]()
    } else {
      value.split(",").toSet
    }
  }
  {
    config.addListener(APPKEY_LOG_LIST, new IConfigChangeListener {
      override def changed(key: String, oldValue: String, newValue: String): Unit = {
        logger.info(s"AppKeyLogList changed,oldValue:$oldValue,newValue:$newValue")
        appKeyLogSet = constructList(newValue)
      }
    })
  }
}
