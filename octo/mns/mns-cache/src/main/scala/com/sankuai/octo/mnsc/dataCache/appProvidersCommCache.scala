package com.sankuai.octo.mnsc.dataCache

import java.util.concurrent.{ConcurrentHashMap, CountDownLatch, Executors, TimeUnit}

import com.sankuai.octo.mnsc.model.service.{CacheData, ProviderNode}
import com.sankuai.octo.mnsc.model.Env
import com.sankuai.octo.mnsc.remote.zk
import com.sankuai.octo.mnsc.utils.{mnscCommon, zkCommon}
import org.slf4j.{Logger, LoggerFactory}
import play.api.libs.json.Json

import scala.collection.JavaConverters._

/**
  * ZK provider节点下数据缓存
  */
object appProvidersCommCache {
  private val LOG: Logger = LoggerFactory.getLogger(appProvidersCommCache.getClass)

  //服务的provider缓存，key为"appkey|env"
  private val providerCache = new ConcurrentHashMap[String, CacheData]() asScala

  //获取provider cache
  def getProviderCache(appkey: String, env: Int, protocol: String): CacheData = {
    val strEnv = Env(env).toString
    val providers = providerCache.get(s"$appkey|$strEnv|$protocol")
    providers match {
      case Some(value) => {
        val curTime = System.currentTimeMillis() / 1000
        // 每5秒更新一次
        if (10 > curTime - providers.get.lastGetTime) {
          providers.get
        } else {
          LOG.info(s"providerCache $appkey|$strEnv|$protocol is expired")
          getProviderPar(appkey, strEnv, protocol)
        }
      }
      case None =>
        LOG.info(s"providerCache don't exist $appkey|$strEnv|$protocol")
        getProviderPar(appkey, strEnv, protocol)
    }
  }

  def getProviderPar(appkey: String, env: String, protocol: String) = {
    val path = zkCommon.getProtocolPath(appkey, env, protocol)
    //保证version是相对旧的
    val (version, _) = zk.getNodeVersion(path)
    val nodeList = zk.children(path)
    val versionNew = zk.getNodeVersion(path)
    if (version != versionNew) {
      LOG.warn(s"version changed during zk.children($path) from $version to $versionNew")
    }
    val result = scala.collection.mutable.ArrayBuffer[ProviderNode]()
    nodeList.foreach(
      node => {
        val nodePath = s"$path/$node"
        try {
          val data = zk.getData(nodePath)
          Json.parse(data).validate[ProviderNode].asOpt match {
            case Some(x) =>
              result.synchronized {
                result += x
              }
            case _ => //do nothing
          }
        } catch {
          case e: Exception => LOG.error(s"get path $nodePath or Json.validate error ${e.getMessage}", e)
        }
      }
    )
    val cacheData = CacheData(version, result.toList)
    providerCache.update(s"$appkey|$env|$protocol", cacheData)
    cacheData
  }

}