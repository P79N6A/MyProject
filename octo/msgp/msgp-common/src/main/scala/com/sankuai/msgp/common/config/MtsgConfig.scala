package com.sankuai.msgp.common.config

import java.io.BufferedInputStream

import com.sankuai.meituan.config.MtConfigClient
import com.sankuai.meituan.config.listener.IConfigChangeListener
import org.slf4j.{Logger, LoggerFactory}

object MtsgConfig {
  private final val LOG: Logger = LoggerFactory.getLogger(MtsgConfig.getClass)

  private val client = new MtConfigClient()

  private val instance = {
    client.setAppkey("com.sankuai.octo.mtsg")
    //1.0.0及后面版本使用
    client.setModel("v2")
    client.setEnv("prod")
    client.setId("mtsg")
    //可选，扫描注解的根目录，默认全部扫描
    client.setScanBasePackage("com.sankuai.meituan")

    try {
      // 初始化client
      client.init()
    } catch {
      case e: Exception => LOG.error("init MtConfigClient (" + client.getNodeName + ") failed", e);
    }
    client.addListener("not_exist", new IConfigChangeListener {
      def changed(key: String, oldValue: String, newValue: String): Unit = {
        println(s"$key changed from $oldValue to $newValue")
      }
    })
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

  def printFile(stream: BufferedInputStream) {
    val buffer: Array[Byte] = new Array[Byte](1000)
    stream.read(buffer, 0, 1000)
    val str: String = new String(buffer)
    System.out.println(str)
  }

  def main(args: Array[String]): Unit = {
    println(get("sg.auto.resend.mail.host"))
  }
}