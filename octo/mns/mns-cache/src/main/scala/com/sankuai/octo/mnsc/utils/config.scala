package com.sankuai.octo.mnsc.utils

import com.sankuai.meituan.config.MtConfigClient
import org.slf4j.{Logger, LoggerFactory}

object config {
  private final val LOG: Logger = LoggerFactory.getLogger(config.getClass)

  private val client = new MtConfigClient()

  client.setAppkey(System.getProperty("jetty.appkey"))
  //1.0.0及后面版本使用
  client.setModel("v2")
  client.setId("defaultClient")
  //可选，扫描注解的根目录，默认全部扫描
  client.setScanBasePackage("com.sankuai.meituan")

  try {
    // 初始化client
    client.init()
  } catch {
    case e: Exception => LOG.error("init MtConfigClient (" + client.getNodeName + ") failed", e);
  }

  def get(key: String, default: String = null) = {
    val value = client.getValue(key)
    if (value != null) value else default
  }
}
