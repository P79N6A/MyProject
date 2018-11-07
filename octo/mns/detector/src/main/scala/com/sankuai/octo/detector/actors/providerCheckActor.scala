package com.sankuai.octo.detector.actors

import java.util.concurrent.atomic.AtomicInteger

import akka.actor.Actor
import com.alibaba.fastjson.{JSONObject}
import com.sankuai.octo.Common
import com.sankuai.octo.falcon.FalconItem
import com.sankuai.octo.model.Provider
import com.sankuai.octo.detector._
import com.sankuai.octo.updater.thrift.ProviderStatus
import com.sankuai.octo.util.{HeartbeatDetector, ScanUtils}
import com.sankuai.sgagent.thrift.model.fb_status
import org.slf4j.{Logger, LoggerFactory}


class providerCheckActor(env: String, appKey: String, providerPath: String,
                         providersDir: String, scanRoundCounter: AtomicInteger) extends Actor {

  private val LOG: Logger = LoggerFactory.getLogger(providerCheckActor.this.getClass)

  var unReachableBeginTime = 0L
  var aliveBeginTime = 0L
  var validatedStatus = fb_status.ALIVE
  var provider: Provider = null
  var providerJson: JSONObject = null
  var targetWeight: Int = 10
  var isSlowStarting: Boolean = false
  var statusInfo: String = "ALIVE"
  var detectorType: String = "Telnet"


  def receive = {
    case round => {
      statusInfo = "ALIVE"
      FalconItem.providerTotalNum.incrementAndGet
      //val round = scanRoundCounter.get
      providerJson = ScanUtils.getProviderJsonInZK(providerPath)
      if (null == providerJson || providerJson.isEmpty) {
        LOG.warn("get from zk failed, json is empty, path = " + providerPath);
      } else if (providerJson.getIntValue("status") == fb_status.STOPPED.getValue) {
        statusInfo = "STOPPED"
      } else {
        provider = Provider.json2Provider(providerJson)
        provider.setServerPath(providerPath)
        provider.setProvidersDir(providersDir)
        if (ScanServiceImpl.useScannerHeartbeat && (provider.getHeartbeatSupport() == 2 || provider.getHeartbeatSupport() == 3)) {
          validatedStatus = HeartbeatDetector.detect(provider)
          detectorType = "Heartbeat"
        } else {
          validatedStatus = detector.detect(provider)
          detectorType = "Telnet"
        }

        if (validatedStatus.getValue != provider.getStatus) {
          if (validatedStatus.equals(fb_status.DEAD)) {
            FalconItem.providerFailNum.incrementAndGet
            if (!ScanServiceImpl.emergencySwitch && !ScanServiceImpl.fusing) {
              UpdaterServiceClient.doubleCheck(providerPath, ProviderStatus.DEAD);
            }
          } else if (validatedStatus.equals(fb_status.ALIVE)) {
            UpdaterServiceClient.doubleCheck(providerPath, ProviderStatus.ALIVE);
          }
        }

        if (validatedStatus.equals(fb_status.DEAD)) {
          statusInfo = "DEAD"
          exceptionHandler(provider)
          aliveBeginTime = 0
        } else if (validatedStatus.eq(fb_status.ALIVE)) {
          unReachableBeginTime = 0L
          if (aliveBeginTime == 0)
            aliveBeginTime = System.currentTimeMillis
        }
      }
      LOG.info(s"round=$round $detectorType check provider $providerPath $statusInfo")
    }
  }


  def exceptionHandler(provider: Provider) {

    var exceptionLog = ""
    if (detector.isReachable(provider.getIp)) {
      unReachableBeginTime = 0L
      exceptionLog = s"异常信息:${provider.getExceptionMsg}, 端口异常: $providerPath"
      LOG.warn(exceptionLog)
    } else {
      // ping 不通, 再判断连续 ping 不通的时间（线上：12小时，线下：1小时）, 则删除
      LOG.warn(s"unreachable provider:" + providerPath)
      if (unReachableBeginTime == 0L) {
        unReachableBeginTime = System.currentTimeMillis
      }
      else if ((System.currentTimeMillis - unReachableBeginTime) > Common.unReachableTimeBeforeDelete) {
        if (Common.isOnline) {
          exceptionLog = s"线上机器:连续12小时ping不到该主机,判断机器已下线:$providerPath"
        } else {
          exceptionLog = s"线下机器:连续1小时ping不到该主机,判断机器已下线:$providerPath"
        }
        exceptionLog += " 从mns里删除"
        ScanUtils.deleteUnReachableProvider(provider)
        LOG.warn(exceptionLog)
      }
    }
  }

}
