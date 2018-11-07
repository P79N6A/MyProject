package com.sankuai.octo.aggregator.util

import com.sankuai.meituan.config.listener.IConfigChangeListener
import org.slf4j.LoggerFactory

object FunctionSwitch {
  private val logger = LoggerFactory.getLogger(this.getClass)
  private val config = MyProxy.mcc
  private val SWITCH_OLD_PARSER = "switch.oldParser"

  val SWITCH_ON = "on"

  val slowQueryParserSwitch = config.get("switch.slowQueryParser", SWITCH_ON)
  var oldParserSwitch = config.get(SWITCH_OLD_PARSER, SWITCH_ON)
  val mtraceStoreSwitch = config.get("switch.mtraceStore", SWITCH_ON)

  {
    try {
      config.addListener(SWITCH_OLD_PARSER, new IConfigChangeListener() {
        def changed(key: String, oldValue: String, newValue: String) = {
          logger.info(s"FunctionSwitch change key => $key oldValue => $oldValue newValue => $newValue")
          oldParserSwitch = newValue
        }
      })

      logger.info(s"FunctionSwitch addListener $SWITCH_OLD_PARSER success")
    }
    catch {
      case e: Exception => logger.error("FunctionSwitch addListener failed", e)
    }
  }
}
