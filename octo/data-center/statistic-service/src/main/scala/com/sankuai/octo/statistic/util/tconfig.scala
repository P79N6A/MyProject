package com.sankuai.octo.statistic.util

import com.sankuai.meituan.config.MtConfigClient
import com.sankuai.meituan.config.listener.IConfigChangeListener
import org.slf4j.LoggerFactory

trait tconfig {

  private val logger = LoggerFactory.getLogger(this.getClass)

  private val client = new MtConfigClient()

  def get(key: String, default: String = null) = {
    try {
      val value = client.getValue(key)
      if (value != null) value else default
    } catch {
      case e: Exception => logger.error("get config failed...", e)
        default
    }
  }

  /**
   * 初始化方法
   * @param appkey appkey
   * @param model 模型
   * @param id id
   * @param scanBasePackage 扫描基本路径
   */
  def init(appkey: String, model: String = "v2", id: String, scanBasePackage: String = "com.sankuai"): Unit = {
    client.setAppkey(appkey)
    //1.0.0及后面版本使用
    client.setModel(model)
    client.setId(id)
    //可选，扫描注解的根目录，默认全部扫描
    client.setScanBasePackage(scanBasePackage)

    try {
      // 初始化client
      client.init()
    } catch {
      case e: Exception =>
        logger.error(s"init MtConfigClient (${client.getNodeName}) failed", e)
        //  初始化不成功,快速失败
        throw new RuntimeException(e)
    }
  }

  def addListener(key: String, listener: IConfigChangeListener) = {
    client.addListener(key, listener)
  }

  def setValue(key: String, value: String) = {
    client.setValue(key, value)
  }

}
