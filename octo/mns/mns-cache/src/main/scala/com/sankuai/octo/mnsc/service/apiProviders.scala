package com.sankuai.octo.mnsc.service

import scala.collection.JavaConverters._
import com.sankuai.octo.mnsc.dataCache.appProvidersCommCache
import com.sankuai.octo.mnsc.model.{Env, service}
import com.sankuai.octo.mnsc.model.service.{Provider, ProviderNode}
import com.sankuai.octo.mnsc.remote.zk
import com.sankuai.octo.mnsc.utils.{api, zkCommon}
import com.sankuai.sgagent.thrift.model.SGService
import org.apache.commons.lang3.StringUtils
import org.slf4j.{Logger, LoggerFactory}
import play.api.libs.json.Json

/**
  * Created by lhmily on 01/13/2016.
  */
object apiProviders {
  private val LOG: Logger = LoggerFactory.getLogger(apiService.getClass)


  def getProviders(appkey: String, env: Int, protocol: String) = {
    val services = appProvidersCommCache.getProviderCache(appkey, env, protocol)
    if (services.Providers.isEmpty) api.errorJson(404, s"can't find appkey:$appkey env:$env protocol:$protocol") else api.dataJson(200, Map {
      "serviceList" -> services.Providers
    })
  }

  def postProviders(providers: java.util.List[SGService]) = {
    providers.asScala.map {
      svr =>
        if (null == svr.getVersion) {
          // version cannot be null
          svr.setVersion("")
        }
        val item = service.SGService2ProviderNode(svr)
        val protocol = getProtocol(item)
        val providerPath = zkCommon.getProtocolPath(item.appkey, Env(item.env).toString, protocol)
        val nodePath = s"${providerPath}/${item.ip}:${item.port}"
        val nodeData = Json.prettyPrint(Json.toJson(item))
        val provider = Provider(item.appkey, item.lastUpdateTime)
        val providerData = Json.prettyPrint(Json.toJson(provider))
        if (!zk.exist(providerPath)) {
          val msg = Map("appkey" -> item.appkey, "ip" -> item.ip, "port" -> item.port)
          Map("ret" -> 404, "msg" -> msg)
        }
        else if (zk.exist(nodePath)) {
          zk.client.inTransaction().setData().forPath(nodePath, nodeData.getBytes("utf-8")).and().setData().forPath(providerPath, providerData.getBytes("utf-8")).and().commit()
          Map("ret" -> 200, "msg" -> "success")
        }
        else {
          zk.client.inTransaction().create().forPath(nodePath, nodeData.getBytes("utf-8")).and().setData().forPath(providerPath, providerData.getBytes("utf-8")).and().commit()
          Map("ret" -> 200, "msg" -> "success")
        }
    }
  }


  def deleteProviders(providers: java.util.List[SGService]) = {
    providers.asScala.map {
      svr =>
        val item = service.SGService2ProviderNode(svr)
        val protocol = getProtocol(item)
        val providerPath = zkCommon.getProtocolPath(item.appkey, Env(item.env).toString, protocol)
        val nodePath = s"${providerPath}/${item.ip}:${item.port}"
        val nodeData = Json.prettyPrint(Json.toJson(item))
        val provider = Provider(item.appkey, item.lastUpdateTime)
        val providerData = Json.prettyPrint(Json.toJson(provider))
        if (!zk.exist(providerPath) || !zk.exist(nodePath)) {
          val msg = Map("appkey" -> item.appkey, "ip" -> item.ip, "port" -> item.port)
          Map("ret" -> 404, "msg" -> msg)
        }
        else {
          zk.client.inTransaction().delete().forPath(nodePath).and().setData().forPath(providerPath, providerData.getBytes("utf-8")).and().commit()
          Map("ret" -> 200, "msg" -> "success")
        }
    }
  }

  def getProtocol(item: ProviderNode) = {
    item.protocol.getOrElse(if (Some(1) == item.serverType) "http" else "thrift")
  }

}
