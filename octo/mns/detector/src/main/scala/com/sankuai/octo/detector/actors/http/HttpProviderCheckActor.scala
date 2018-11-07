package com.sankuai.octo.detector.actors.http

import com.sankuai.octo.updater.thrift.ProviderStatus
import org.apache.http.concurrent.FutureCallback;
import java.util.concurrent.atomic.AtomicInteger

import akka.actor.Actor
import com.alibaba.fastjson.JSONObject
import com.sankuai.octo.Common
import com.sankuai.octo.detector.actors.detector
import com.sankuai.octo.detector.{ScanServiceImpl, UpdaterServiceClient}
import com.sankuai.octo.falcon.FalconItem
import com.sankuai.octo.model.Provider
import com.sankuai.octo.util.{AsyncHttpClientUtils, ScanUtils}
import com.sankuai.sgagent.thrift.model.fb_status
import org.apache.http.HttpResponse
import org.apache.http.client.methods.HttpGet
import org.slf4j.{Logger, LoggerFactory}

/**
 * Copyright (C) 2015 Meituan
 * All rights reserved
 * User: gaosheng
 * Date: 16-7-4
 * Time: 下午5:07
 */
class HttpProviderCheckActor(env: String, appKey: String, providerPath: String,
                             providersDir: String, scanRoundCounter: AtomicInteger) extends Actor {

  private val LOG: Logger = LoggerFactory.getLogger(HttpProviderCheckActor.this.getClass)

  var unReachableBeginTime = 0L
  var aliveBeginTime = 0L
  var validatedStatus = fb_status.ALIVE
  var provider: Provider = null
  var providerJson: JSONObject = null
  var targetWeight: Int = 10
  var isSlowStarting: Boolean = false
  var statusInfo: String = "ALIVE"
  var round: Int = -1;
  var checkUrl: String = ""


  def receive = {
    case httpCheckMessage: HttpCheckMessage => {
      round = httpCheckMessage.getRound
      checkUrl = httpCheckMessage.getCheckUrl
      statusInfo = "ALIVE"
      FalconItem.providerTotalNum.incrementAndGet
      providerJson = ScanUtils.getProviderJsonInZK(providerPath)
      if (null == providerJson || providerJson.isEmpty) {
        LOG.warn("get from zk failed, json is empty, path = " + providerPath);
      } else if (providerJson.getIntValue("status") == fb_status.STOPPED.getValue) {
        statusInfo = "STOPPED"
        LOG.info(s"round=$round HttpProviderCheckActor $providerPath $statusInfo")
      } else {
        provider = Provider.json2Provider(providerJson)
        provider.setServerPath(providerPath)
        provider.setProvidersDir(providersDir)

        val request = new HttpGet("http://" + provider.getIp + ":" + provider.getPort + checkUrl);
        val callback = new FutureCallback[HttpResponse] {

          override def cancelled(): Unit = {

          }

          override def completed(response: HttpResponse): Unit = {
            val status = response.getStatusLine.getStatusCode
            if (status >= 200 && status < 400) {
              validatedStatus = fb_status.ALIVE;
            } else {
              validatedStatus = fb_status.DEAD;
            }
            handler(validatedStatus, status)

          }

          override def failed(e: Exception): Unit = {
            validatedStatus = fb_status.DEAD;
            handler(validatedStatus, -1)
          }

        }
        AsyncHttpClientUtils.getInstance().execute(request, callback)
      }
    }
  }

  def handler(validatedStatus: fb_status, status: Int): Unit = {
    if (validatedStatus.getValue != provider.getStatus) {
      if (validatedStatus.equals(fb_status.DEAD)) {
        FalconItem.providerFailNum.incrementAndGet
        if (!ScanServiceImpl.emergencySwitch && !ScanServiceImpl.fusing) {
          UpdaterServiceClient.userDefinedHttpDoubleCheck(providerPath, ProviderStatus.DEAD, checkUrl);
        }
      } else if (validatedStatus.equals(fb_status.ALIVE)) {
        UpdaterServiceClient.userDefinedHttpDoubleCheck(providerPath, ProviderStatus.ALIVE, checkUrl);
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

    LOG.info(s"round=$round  HttpProviderCheckActor $providerPath $statusInfo $status")
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