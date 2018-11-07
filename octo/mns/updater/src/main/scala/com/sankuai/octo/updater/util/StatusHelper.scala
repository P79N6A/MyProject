package com.sankuai.octo.updater.util

import com.sankuai.octo.scanner.model.Provider
import com.sankuai.octo.updater.thrift.ProviderStatus
import com.sankuai.octo.util.HeartbeatDetector
import com.sankuai.sgagent.thrift.model.fb_status
import org.slf4j.LoggerFactory

/**
 * Created by wujinwu on 16/6/8.
 */
object StatusHelper {

  private val logger = LoggerFactory.getLogger(this.getClass)

  private implicit val ec = ExecutionContextFactory.build(Runtime.getRuntime.availableProcessors())

  def checkStatusAndUpdate(providerPath: String, status: ProviderStatus) = {
    val providerJson = ScanUtils.getProviderJsonInZK(providerPath)
    if (null == providerJson || providerJson.isEmpty) {
      logger.warn("get from zk failed, json is empty, path = " + providerPath)
    } else if (providerJson.getIntValue("status") == fb_status.STOPPED.getValue) {
      logger.warn("get from zk failed, path = {},status STOPPED", providerPath)
    } else {
      // construct provider from json
      val provider = Provider.json2Provider(providerJson)
      provider.setServerPath(providerPath)
      provider.setProvidersDir(getProviderDir(providerPath))
      var validatedStatus = fb_status.ALIVE;
      if (provider.getHeartbeatSupport == 2 || provider.getHeartbeatSupport == 3)
        validatedStatus = HeartbeatDetector.detect(provider)
      else
        validatedStatus = Detector.detect(provider)

      if (validatedStatus.getValue != provider.getStatus && validatedStatus.getValue == status.getValue) {
        ScanUtils.updateServerStatus(validatedStatus.getValue, provider.getStatus, provider, providerJson)
      }
    }

  }

  def getProviderDir(providerPath: String) = {
    val index = providerPath.lastIndexOf("/")
    providerPath.substring(0, index)
  }
}
