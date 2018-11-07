package com.sankuai.msgp.common.config

import com.sankuai.meituan.config.MtConfigClient
import com.sankuai.meituan.config.listener.IConfigChangeListener
import org.slf4j.{Logger, LoggerFactory}


/**
  * Created by liangchen on 2018/3/21.
  */
object MnsapiConfig {
  private final val LOG: Logger = LoggerFactory.getLogger(MsgpConfig.getClass)

  private val client = new MtConfigClient()


  private val instance = {
    client.setAppkey("com.sankuai.inf.octo.mnsapi")
    //1.0.0及后面版本使用
    client.setModel("v2")
    client.setEnv("prod")
    client.setId("com.sankuai.inf.octo.mnsapi")
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

  def get(key: String, default: String = "") = {
    try {
      val value = instance.getValue(key)
      if (value != null) value else default
    } catch {
      case e: Exception => LOG.error(s"client get $key failed", e)
        default
    }
  }

  def set(key: String, value: String = null) = {
    try {
      instance.setValue(key, value)
    } catch {
      case e: Exception => LOG.error(s"client set $key value $value failed", e)
    }
  }

  def addListener(key: String,configChangeListener : IConfigChangeListener) ={
    instance.addListener(key,configChangeListener)
  }



  def main(args: Array[String]): Unit = {
    println(get("task.execute.host"))
  }
}
