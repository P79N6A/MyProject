package com.sankuai.octo.sgnotify

import com.sankuai.meituan.config.MtConfigClient
import com.sankuai.meituan.config.exception.MtConfigException
import org.slf4j.{Logger, LoggerFactory}

object config {
  private val LOG: Logger = LoggerFactory.getLogger(config.getClass)

  val instance = {
    val client: MtConfigClient = new MtConfigClient
    try {
      client.setModel("v2")
      client.setAppkey("com.sankuai.inf.sgnotify")
      client.setId("default")
      client.setScanBasePackage("com.sankuai")
      client.init()
    } catch {
      case e: MtConfigException => LOG.error("init MtConfigClient (" + client.getNodeName + ") failed", e)
    }
    LOG.info("MtConfigClient ({}) init", client.getNodeName)
    client
  }

  def get(key: String, default: String = null) = {
    val value = instance.getValue(key)
    if (value != null) value else default
  }

  def main(args: Array[String]) {
    println(config.get("test", "xxx"))
  }
}
