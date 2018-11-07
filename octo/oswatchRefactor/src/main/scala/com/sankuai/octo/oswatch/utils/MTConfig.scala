package com.sankuai.octo.oswatch.utils

import com.sankuai.meituan.config.MtConfigClient
import org.slf4j.{LoggerFactory, Logger}

/**
 * Created by dreamblossom on 15/9/29.
 */
object MTConfig {
  private final val LOG: Logger = LoggerFactory.getLogger(MTConfig.getClass)

  private val client = new MtConfigClient()
  private val instance = {
    client.setAppkey("com.sankuai.inf.octo.oswatch")
    //1.0.0及后面版本使用
    client.setModel("v2")
    client.setEnv("prod")
    client.setId("defaultClient")
    //可选，扫描注解的根目录，默认全部扫描
    client.setScanBasePackage("com.sankuai.meituan")

    try {
      // 初始化client
      client.init()
    } catch {
      case e: Exception => LOG.error("init MtConfigClient (" + client.getNodeName + ") failed", e);
    }
    client
  }

  def get(key: String, default: String = null) = {
    try {
      val value = instance.getValue(key)
      if (value != null) value else default
    } catch {
      case e: Exception => LOG.error(s"client get ${key} failed", e)
        default
    }
  }

  //  def main(args: Array[String]): Unit = {
  //    println(get("admins","default"))
  //  }
}
