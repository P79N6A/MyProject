package com.sankuai.msgp.common.config

import com.sankuai.meituan.config.listener.IConfigChangeListener
import com.sankuai.meituan.config.{FileConfigClient, MtConfigClient}
import org.slf4j.{Logger, LoggerFactory}

import scala.collection.JavaConverters._
import scala.collection.concurrent.TrieMap

object MsgpConfig {
  private final val LOG: Logger = LoggerFactory.getLogger(MsgpConfig.getClass)

  private val client = new MtConfigClient()
  private val AUTHKEY: String = "msgp.api.auth"

  val client_secret = TrieMap[String, String]()

  val fileClient = {
    val client = new FileConfigClient()
    client.setAppkey("com.sankuai.inf.msgp")
    client.init()
    LOG.info(s"init file conf finish")
    client
  }

  private val instance = {
    client.setAppkey("com.sankuai.inf.msgp")
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
    client.addListener("not_exist", new IConfigChangeListener {
      def changed(key: String, oldValue: String, newValue: String): Unit = {
        println(s"$key changed from $oldValue to $newValue")
      }
    })
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

  def getClientSecret(default: String = null) = {
    val data = get(AUTHKEY, default)
    initClientSecret(data)
    client_secret.asJava
  }

  def initClientSecret(apiAuthConfig: String) {
    client_secret.clear()
    val configs = apiAuthConfig.split("\n")
    configs.foreach {
      config =>
        val kv = config.split("=")
        client_secret.put(kv.apply(0), kv.apply(1))
    }
  }

  def main(args: Array[String]): Unit = {
    println(get("admins"))
  }
}
