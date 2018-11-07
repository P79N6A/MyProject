package com.sankuai.octo.mnsc.dataCache

import com.sankuai.octo.mnsc.model.Env
import com.sankuai.octo.mnsc.model.service.{CacheValue, ProviderNode}
import com.sankuai.octo.mnsc.remote.zk
import com.sankuai.octo.mnsc.utils.mnscCommon
import com.sankuai.sgagent.thrift.model.SGService
import org.apache.commons.lang.StringUtils
import org.joda.time.DateTime
import org.slf4j.LoggerFactory
import play.api.libs.json.Json

import scala.collection.JavaConverters._


/**
  * Created by lhmily on 11/23/2016.
  */
abstract class providerBaseCache {
  private val LOG = LoggerFactory.getLogger(this.getClass)

  def updatePathCache(appkey: String, env: String, providerPath: String, isPar: Boolean = false) = {
    val path = s"${mnscCommon.rootPre}/$env/$appkey/$providerPath"
    try {
      val nodeList = zk.client().getChildren.forPath(path).asScala
      val parList = if (isPar) {
        nodeList.par
      } else {
        nodeList
      }

      val ipToApp = getIpInfoCache()
      parList.foreach(
        node => {
          val nodePath = s"$path/$node"
          val data = zk.client.getData.forPath(nodePath)
          val dataUTF8 = if (null == data) "" else new String(data, "utf-8")
          if (dataUTF8.nonEmpty) {
            Json.parse(dataUTF8).validate[ProviderNode].asOpt match {
              case Some(x) =>
                val service = com.sankuai.octo.mnsc.model.service.ProviderNode2SGService(x)

                val v = if (ipToApp.keySet.contains(service.ip)) {
                  ipToApp(service.ip) + service.appkey
                } else {
                  scala.collection.Set[String](service.appkey)
                }

                ipToApp.update(service.ip, v)
                getPathCache()(s"${appkey}|${env}") += (s"${service.ip}:${service.port}" -> service)

              case _ => //do nothing
            }
          }
        }
      )
    } catch {
      case e: Exception =>
        LOG.error(s"fail to update path cache, appkey=$appkey env=$env", e)
        null
    }
  }

  def updateProviderCache(providerVersion: String, appkey: String, env: String, providerPath: String, isPar: Boolean = false) = {
    val path = s"${mnscCommon.rootPre}/$env/$appkey/$providerPath"
    if (StringUtils.isNotEmpty(providerVersion)) {
      try {
        val nodeList = zk.client().getChildren.forPath(path).asScala
        val result = scala.collection.mutable.ArrayBuffer[SGService]()
        val parList = if (isPar) {
          nodeList.par
        } else {
          nodeList
        }

        val ipToAppkey = getIpInfoCache()
        parList.foreach(
          node => {
            val nodePath = s"$path/$node"
            val data = zk.client.getData.forPath(nodePath)
            val dataUTF8 = if (null == data) "" else new String(data, "utf-8")
            if (dataUTF8.nonEmpty) {
              Json.parse(dataUTF8).validate[ProviderNode].asOpt match {
                case Some(x) =>
                  val service = com.sankuai.octo.mnsc.model.service.ProviderNode2SGService(x)
                  result.synchronized {
                    result += service
                  }

                  if (service.appkey != "com.sankuai.inf.sg_agent"
                    && service.appkey != "com.sankuai.inf.kms_agent"
                    && service.ip.nonEmpty) {
                    val v = if (ipToAppkey.keySet.contains(service.ip)) {
                      ipToAppkey(service.ip) + service.appkey
                    } else {
                      scala.collection.Set[String](service.appkey)
                    }

                    ipToAppkey.update(service.ip, v)
                  }
                case _ => //do nothing
              }
            }
          }
        )
        val currentCache = getCache(appkey)
        val cacheValue = CacheValue(providerVersion, result.toList)
        currentCache.update(getCacheKey(appkey, env), cacheValue)
        setIsUpdate(true)
        cacheValue
      } catch {
        case e: Exception =>
          LOG.error(s"fail to update cache, appkey=$appkey env=$env", e)
          null
      }
    } else {
      null
    }
  }

  protected def getCache(appkey: String): scala.collection.concurrent.Map[String, CacheValue]

  protected def getCache(): scala.collection.concurrent.Map[String, CacheValue]

  protected def getPathCache(): scala.collection.concurrent.Map[String, scala.collection.concurrent.Map[String, SGService]]

  //mutable.set可能存在潜在的原子性问题，不确定，故全量替换set
  protected def getIpInfoCache(): scala.collection.concurrent.Map[String, scala.collection.Set[String]]

  protected def getCacheKey(appkey: String, env: String) = s"$appkey|$env"

  //若在providerCache中发现不存在的appkey，删除之
  def deleteNonexistentAppKey() = {
    val cache = getCache()
    val newAppkeys = mnscCommon.allApp()
    val cacheAppkeys = getAppkeysByCacheKeySet(cache.keySet)
    LOG.info(s"[deleteNonexistentAppKey] providerCache=${cache.keySet.size} cacheAppkeys=${cacheAppkeys.size} newAppkeys=${newAppkeys.size}")

    cacheAppkeys.toList.filter(!newAppkeys.contains(_)).foreach {
      appkey =>
        Env.values.foreach {
          env =>
            cache.remove(s"$appkey|$env")
            LOG.info(s"[deleteNonexistentAppKey] providerCache delete $appkey|$env")
        }
    }
  }

  //定时任务强同步pull provider
  def renewAllProviderForce(providerPathStr: String, isPar: Boolean) = {
    val apps = mnscCommon.allAppkeys(isPar)
    /*val apps = if (providerPathStr.endsWith("http")) {
      mnscCommon.allAppkeys(isPar)
    } else {
      mnscCommon.ancestorWatchApp().par
    }*/

    val start = new DateTime().getMillis
    Env.values.foreach {
      env =>
        apps.foreach {
          appkey =>
            val path = s"${mnscCommon.rootPre}/$env/$appkey/$providerPathStr"
            val (version, _) = zk.getNodeVersion(path)
            if (null == version) {
              LOG.error(s"renewAllProviderForce failed, $path don't exist")
            } else {
              updateProviderCache(version, appkey, env.toString, providerPathStr)
            }
        }
    }

    val end = new DateTime().getMillis
    LOG.info(s"renewProviderForce--> path=${providerPathStr} apps.length=${apps.length}  cost ${end - start}")
  }

  def renewAllPathCacheForce(providerPathStr: String, isPar: Boolean) = {
    val apps = mnscCommon.childrenWatchApp().toList.par
    val start = new DateTime().getMillis
    Env.values.foreach {
      env =>
        apps.foreach {
          appkey =>
            updatePathCache(appkey, env.toString, providerPathStr)
        }
    }
    val end = new DateTime().getMillis
    LOG.info(s"renewAllPathCacheForce--> path=${providerPathStr} apps.length=${apps.length}  cost ${end - start}")
  }

  private def getAppkeysByCacheKeySet(appkeySets: scala.collection.Set[String]) = appkeySets.map(x => x.stripSuffix("|prod").stripSuffix("|stage").stripSuffix("|test"))

  //定时任务弱同步pull provider
  protected def renewAllProvider(cache: scala.collection.concurrent.Map[String, CacheValue], providerPathStr: String) = {
    val apps = mnscCommon.allApp().par
    /*val apps = if (providerPathStr.endsWith("http")) {
      mnscCommon.allApp().par
    } else {
      mnscCommon.ancestorWatchApp().par
    }*/

    apps.foreach {
      appkey =>
        Env.values.foreach {
          env =>
            val providers = cache.get(getCacheKey(appkey, env.toString))
            providers match {
              case Some(value) =>
                val path = s"${mnscCommon.rootPre}/$env/$appkey/$providerPathStr"
                val (version, _) = zk.getNodeVersion(path)
                if (null == version) {
                  LOG.warn(s"renewAllProvider failed, $path don't exist")
                } else {
                  if (version != value.version) {
                    updateProviderCache(version, appkey, env.toString, providerPathStr)
                  }
                }
              case None =>
                LOG.debug(s"reload providerCache is empty: $appkey|$env")
            }
        }
    }
  }


  //watcher触发执行动作
  def mnscWatcherAction(appkey: String, env: String, providerPathStr: String) = {
    val providers = getCache(appkey).get(getCacheKey(appkey, env))
    val path = s"${mnscCommon.rootPre}/$env/$appkey/$providerPathStr"
    val (version, _) = zk.getNodeVersion(path)
    providers match {
      case Some(value) =>
        if (version != value.version) {
          updateProviderCache(version, appkey, env, providerPathStr)
        }
      case None =>
        updateProviderCache(version, appkey, env, providerPathStr)
    }
  }

  def pathCacheUpdateAction(data: String, key: String, isAdd: Boolean): Unit = {
    if (data.nonEmpty) {
      Json.parse(data).validate[ProviderNode].asOpt match {
        case Some(x) =>
          val service = com.sankuai.octo.mnsc.model.service.ProviderNode2SGService(x)
          if (service.ip.nonEmpty) {
            val ipToApp = getIpInfoCache()
            val v = if (ipToApp.keySet.contains(service.ip)) {
              ipToApp(service.ip) + service.appkey
            } else {
              scala.collection.Set[String](service.appkey)
            }

            ipToApp.update(service.ip, v)
            if (isAdd) {
              getPathCache()(key) += (s"${service.ip}:${service.port}" -> service)
            } else {
              getPathCache()(key).update(s"${service.ip}:${service.port}", service)
            }
          }
        case _ =>
      }
    }
  }

  def pathCacheRemoveAction(data: String, key: String): Unit = {
    if (data.nonEmpty) {
      Json.parse(data).validate[ProviderNode].asOpt match {
        case Some(x) =>
          val service = com.sankuai.octo.mnsc.model.service.ProviderNode2SGService(x)
          if (service.ip.nonEmpty) {
            getPathCache()(key).remove(s"${service.ip}:${service.port}")
          }
        case _ =>
      }
    }
  }

  def getAppkeyListByIP(ip: String) = {
    val cache = getCache()
    val keySet = cache.filter(_._2.SGServices.foldLeft(false) { (result, item) =>
      result || StringUtils.equals(ip, item.ip)
    }).keySet
    getAppkeysByCacheKeySet(keySet)
  }

  def getAppkeysByIP(ip: String) = {
    getIpInfoCache().getOrElse(ip, Nil)
  }

  protected def setIsUpdate(isUpdate: Boolean) = {

  }
}
