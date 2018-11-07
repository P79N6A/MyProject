package com.sankuai.octo.updater.actor

import akka.actor.Actor
import com.alibaba.fastjson.JSONObject
import com.sankuai.octo.scanner.model.Provider
import com.sankuai.octo.updater.thrift.ProviderStatus
import com.sankuai.octo.updater.util.ScanUtils
import com.sankuai.octo.util.AsyncHttpClientUtils
import com.sankuai.sgagent.thrift.model.fb_status
import org.apache.http.HttpResponse
import org.apache.http.client.methods.HttpGet
import org.apache.http.concurrent.FutureCallback
import org.slf4j.{Logger, LoggerFactory}

/**
 * Copyright (C) 2015 Meituan
 * All rights reserved
 * User: gaosheng
 * Date: 16-7-4
 * Time: 下午5:07
 */
class HttpProviderCheckActor(providerPath: String, status: ProviderStatus) extends Actor {

  private val LOG: Logger = LoggerFactory.getLogger(HttpProviderCheckActor.this.getClass)

  var validatedStatus = fb_status.ALIVE
  var provider: Provider = null
  var providerJson: JSONObject = null
  var statusInfo: String = "ALIVE"


  def receive = {
    case checkUrl: String => {
      statusInfo = "ALIVE"
      providerJson = ScanUtils.getProviderJsonInZK(providerPath)
      if (null == providerJson || providerJson.isEmpty) {
        LOG.warn("get from zk failed, json is empty, path = " + providerPath);
      } else if (providerJson.getIntValue("status") == fb_status.STOPPED.getValue) {
        statusInfo = "STOPPED"
      } else {
        provider = Provider.json2Provider(providerJson)
        provider.setServerPath(providerPath)
        provider.setProvidersDir(getProviderDir(providerPath))

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
            handler(validatedStatus)

          }

          override def failed(e: Exception): Unit = {
            validatedStatus = fb_status.DEAD;
            handler(validatedStatus)
          }

        }
        AsyncHttpClientUtils.getInstance().execute(request, callback)
      }
    }
  }

  def handler(validatedStatus: fb_status): Unit = {
    if (validatedStatus.getValue != provider.getStatus && validatedStatus.getValue == status.getValue) {
      ScanUtils.updateServerStatus(validatedStatus.getValue, provider.getStatus, provider, providerJson)
    }
  }

  def getProviderDir(providerPath: String) = {
    val index = providerPath.lastIndexOf("/")
    providerPath.substring(0, index)
  }

}