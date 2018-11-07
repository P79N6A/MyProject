package com.sankuai.octo.mnsc.dataCache

import java.util.concurrent.{ConcurrentHashMap, Executors, TimeUnit}

import com.sankuai.octo.mnsc.model.service.CacheValue
import com.sankuai.octo.mnsc.model.{Appkeys, Env, Path, ServerType}
import com.sankuai.octo.mnsc.remote.zk
import com.sankuai.octo.mnsc.utils.mnscCommon
import com.sankuai.sgagent.thrift.model.SGService
import org.slf4j.LoggerFactory

import scala.collection.JavaConverters._
import scala.collection.concurrent.Map

/**
  * ZK provider节点下数据缓存
  */
object appProviderDataCache extends providerBaseCache {
  private val LOG = LoggerFactory.getLogger(appProviderDataCache.getClass)

  //服务的provider缓存，key为"appkey|env"
  private val providerCache = new ConcurrentHashMap[String, CacheValue]() asScala

  /*sg_agent缓存*/
  private val agentCache = new ConcurrentHashMap[String, CacheValue]() asScala

  private val pathCache = new ConcurrentHashMap[String, scala.collection.concurrent.Map[String, SGService]]() asScala

  private val ipInfoCache = new ConcurrentHashMap[String, scala.collection.Set[String]]() asScala

  private val agentAppkeyList = Appkeys.noCacheAppkeys

  private var pullCount4Provider = 0

  private val scheduler = Executors.newScheduledThreadPool(2)

  //private val pathCacheScheduler = Executors.newScheduledThreadPool(1)


  /*def getProviderCache(appkey: String, env: String, isSearchZK: Boolean) = {
    if (Appkeys.largeAppkeys.contains(appkey)) {
      val watcherPath = s"${mnscCommon.rootPre}/$env/$appkey/${Path.provider}"
      val (providerVersion, _) = zk.getNodeVersion(watcherPath)
      Some(CacheValue(providerVersion, pathCache(s"$appkey|$env").values.toList))
    } else {
      val cache = getCache(appkey)
      val providers = cache.get(getCacheKey(appkey, env))
      if (providers.isEmpty && isSearchZK) {
        LOG.debug(s"providerCache don't exist $appkey|$env")
        val (version, _) = zk.getNodeVersion(s"${mnscCommon.rootPre}/$env/$appkey/${Path.provider.toString}")
        updateProviderCache(version, appkey, env, Path.provider.toString)
        cache.get(getCacheKey(appkey, env))

      } else {
        providers
      }
    }
  }*/

  def getPathData(appkey: String, env: String) = {
    val watcherPath = s"${mnscCommon.rootPre}/$env/$appkey/${Path.provider}"
    val (providerVersion, _) = zk.getNodeVersion(watcherPath)
    val cacheValue = CacheValue(providerVersion, pathCache(s"$appkey|$env").values.toList)

    getCache(appkey).update(getCacheKey(appkey, env), cacheValue)
    cacheValue
  }

  //获取provider cache
  def getProviderCache(appkey: String, env: String, isSearchZK: Boolean) = {
    val cache = getCache(appkey)
    val providers = cache.get(getCacheKey(appkey, env))
    if (providers.isEmpty && isSearchZK) {
      LOG.debug(s"providerCache don't exist $appkey|$env")
      val (version, _) = zk.getNodeVersion(s"${mnscCommon.rootPre}/$env/$appkey/${Path.provider.toString}")
      updateProviderCache(version, appkey, env, Path.provider.toString)
      cache.get(getCacheKey(appkey, env))
    } else {
      providers
    }
  }

  override protected def getCache(appkey: String) = {
    if (agentAppkeyList.contains(appkey)) {
      agentCache
    } else {
      providerCache
    }
  }

  def getPathCache(): Map[String, scala.collection.concurrent.Map[String, SGService]] = pathCache

  override protected def getCache(): Map[String, CacheValue] = providerCache

  override protected def getIpInfoCache(): Map[String, scala.collection.Set[String]] = ipInfoCache

  //强同步和弱同步，每隔20秒执行弱同步，每隔forceBorder * 20秒执行强同步
  def doRenew() = {
    val now = System.currentTimeMillis() / mnscCommon.initDelay4Provider
    val init = 60 - (now % 60)
    LOG.info(s"init doRenew on $now with delay $init")
    //provider定时同步任务
    scheduler.scheduleWithFixedDelay(new Runnable {
      def run(): Unit = {
        try {
          if (0 == pullCount4Provider) {
            LOG.info("renewAllProviderForce start. count = {}", pullCount4Provider + 1)
            val startProvider = System.currentTimeMillis()
            renewAllProviderForce(Path.provider.toString, true)
            val endProvider = System.currentTimeMillis()
            LOG.info("renewAllProviderForce cost {}", endProvider - startProvider)
            //逐出被删除的appKey
            deleteNonexistentAppKey()
          } else {
            LOG.info(s"renewAllProvider start. count = {}", pullCount4Provider + 1)
            val startProvider = System.currentTimeMillis()
            renewAllProvider(providerCache, Path.provider.toString)
            val endProvider = System.currentTimeMillis()
            LOG.info(s"renewAllProvider cost {}", endProvider - startProvider)
          }
          pullCount4Provider = (pullCount4Provider + 1) % mnscCommon.forceBorder4Provider
        } catch {
          case e: Exception => LOG.error(s"renew localCache fail.", e)
        }
      }
    }, init, mnscCommon.renewInterval4Provider * 6, TimeUnit.SECONDS)

    scheduler.scheduleWithFixedDelay(new Runnable {
      def run(): Unit = {
        try {
          LOG.info(s"renew agent list start.")
          val startProvider = System.currentTimeMillis()
          var count = 0
          agentAppkeyList.foreach {
            appkey =>
              Env.values.foreach {
                env =>

                  count +=1
                  if(count >= 1000){
                    Thread.sleep(10000)
                  }
                  val (version,_) = zk.getNodeVersion(s"${mnscCommon.rootPre}/${env.toString}/$appkey/${Path.provider}")
                  val currentCache = agentCache.get(getCacheKey(appkey, env.toString))
                  currentCache match {
                    case Some(cache) =>
                      if (version != cache.version) {
                        updateProviderCache(version, appkey, env.toString, Path.provider.toString, true)
                      }
                    case None =>
                      updateProviderCache(version, appkey, env.toString, Path.provider.toString, true)
                  }
              }
          }
          val endProvider = System.currentTimeMillis()
          LOG.info(s"renewA sgagent cost ${endProvider - startProvider}")
        } catch {
          case e: Exception => LOG.error(s"renew sgagent fail.", e)
        }
      }
    }, init, 45, TimeUnit.MINUTES)

    //移除冗余的appkey，一次全量处理大概300ms；为了保证用户接口高性能的一种权衡
    scheduler.scheduleWithFixedDelay(new Runnable {
      def run(): Unit = {
        try {
          LOG.info(s"renew ip to appkeys map start.")
          val startProvider = System.currentTimeMillis()

          ipInfoCache.foreach {
            item =>
              val removeAppkey = scala.collection.mutable.ArrayBuffer[String]()
              item._2.foreach {
                app =>
                  //检查三个环境对应的appkey的value中都不含有对应的IP，移除之
                  val isExists = Env.values.exists{
                    env =>
                      val key = s"$app|$env"
                      providerCache.keySet.contains(key) && providerCache(key).SGServices.exists(_.ip == item._1)
                  }
                  if (!isExists) {
                    removeAppkey += app
                  }
              }
              val newCache = item._2 -- removeAppkey
              ipInfoCache.update(item._1, newCache)
          }
          val endProvider = System.currentTimeMillis()
          LOG.info(s"renew ip to appkeys map start cost ${endProvider - startProvider}")
        } catch {
          case e: Exception => LOG.error(s"renew ip to appkeys map start fail.", e)
        }
      }
    }, init, 5, TimeUnit.MINUTES)

    scheduler.scheduleWithFixedDelay(new Runnable {
      def run(): Unit = {
        try {
          val startProvider = System.currentTimeMillis()

          pathCache.foreach {
            item =>
              val app = item._1.split("\\|")(0)
              val env = item._1.split("\\|")(1)
              val watcherPath = s"${mnscCommon.rootPre}/$env/$app/${Path.provider}"

              val (providerVersion, _) = zk.getNodeVersion(watcherPath)
              val cacheValue = CacheValue(providerVersion, item._2.values.toList)
              getCache(app).update(getCacheKey(app, env), cacheValue)
          }

          val endProvider = System.currentTimeMillis()
          LOG.info(s"reflect pathCache to old Cache cost ${endProvider - startProvider}")
        } catch {
          case e: Exception => LOG.error(s"reflect pathCache to old Cache failed: ", e)
        }
      }
    }, init, 5, TimeUnit.MINUTES)
  }

  def initPathCache(appkey: String, env: String) = {
    val key = s"${appkey}|${env}"
    getPathCache() += (key -> (new ConcurrentHashMap[String, SGService]() asScala))

    updatePathCache(appkey, env.toString, Path.provider.toString)
  }

  def getProvidersByIP(ip: String) = {
    val list = providerCache.flatMap(_._2.SGServices.filter(_.ip.equals(ip)))
    list.foreach(_.setServerType(ServerType.thrift.id))
    list.toList
  }
}