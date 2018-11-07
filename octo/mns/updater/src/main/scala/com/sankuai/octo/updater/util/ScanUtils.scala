package com.sankuai.octo.updater.util

import java.nio.charset.StandardCharsets.UTF_8

import com.alibaba.fastjson.{JSON, JSONObject}
import com.sankuai.meituan.config.listener.IConfigChangeListener
import com.sankuai.octo.scanner.model.Provider
import com.sankuai.octo.scanner.model.report.UpdateStatusReport
import com.sankuai.octo.util.SendReport
import org.slf4j.LoggerFactory

/**
  * Created by jiguang on 14-9-29.
  */
object ScanUtils {
  private val logger = LoggerFactory.getLogger(this.getClass)

  private val UPDATER_SWITCH = "updaterSwitch"

  @volatile
  private var switch = config.get(UPDATER_SWITCH, "false").toBoolean

  config.addListener(UPDATER_SWITCH, new IConfigChangeListener {
    override def changed(key: String, oldValue: String, newValue: String): Unit = {
      logger.info(s"key:$key,oldValue:$oldValue,newValue:$newValue")
      switch = newValue.toBoolean
    }
  })


  def getProviderJsonInZK(serverPath: String): JSONObject = {
    ScanUtils.bytes2JO(Common.zkClient.get(serverPath))
  }

  def updateServerStatus(validatedStatus: Int, status: Int, provider: Provider, jo: JSONObject) {
    val strLog = "update node status:" + provider.getServerPath + " status, from " + status + " to " + validatedStatus
    logger.info(strLog)
    jo.put("status", validatedStatus)
    jo.put("lastUpdateTime", System.currentTimeMillis / 1000)
    if (switch) {
      Common.zkClient.update(provider.getServerPath, JSON.toJSONBytes(jo), false)
    }
    updateClusterTime(provider.getProvidersDir)
    SendReport.send(new UpdateStatusReport(0, "UpdateStatus", strLog, status, validatedStatus, provider.getIdentifierString))
  }

  def updateClusterTime(providersDir: String) {
    val clusterJO: JSONObject = ScanUtils.bytes2JO(Common.zkClient.get(providersDir))
    val lastUpdateTime: Long = System.currentTimeMillis / 1000
    clusterJO.put("lastUpdateTime", lastUpdateTime)
    if (switch) {
      Common.zkClient.update(providersDir, JSON.toJSONBytes(clusterJO), false)
    }
  }

  def bytes2JO(bytes: Array[Byte]): JSONObject = {
    if (null == bytes) {
      return null
    }
    val serverInfo = new String(bytes, UTF_8)
    try {
      val jo = JSON.parseObject(serverInfo)
      jo
    } catch {
      case e: Exception =>
        logger.error("Exception ", e)
        null
    }

  }
}
