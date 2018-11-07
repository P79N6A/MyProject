package com.sankuai.octo.service

import com.sankuai.meituan.config.MtConfigClient
import com.sankuai.meituan.config.exception.MtConfigException
import org.slf4j.{Logger, LoggerFactory}

object config {
  private final val LOG: Logger = LoggerFactory.getLogger(config.getClass)

  val instance = {
    val client: MtConfigClient = new MtConfigClient
    try {
      // 配置所在路径
      client.setNodeName("scanner")
      // 可选，配置同步轮询间隔，单位：秒，默认：100秒
      client.setPullPeriod(100)
      // 可选，扫描注解根目录，默认全部扫描
      client.setScanBasePackage("com.sankuai")
      client.init
    } catch {
      case e: MtConfigException => {
        LOG.error("init MtConfigClient (" + client.getNodeName + ") failed", e)
      }
    }
    LOG.info("MtConfigClient ({}) init", client.getNodeName)
    client
  }

  def get(key: String, default: String = null) = {
    val value = instance.getValue(key)
    if (value != null) value else default
  }
}
