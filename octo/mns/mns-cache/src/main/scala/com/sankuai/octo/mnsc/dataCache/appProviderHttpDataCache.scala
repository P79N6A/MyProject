package com.sankuai.octo.mnsc.dataCache

import java.util.concurrent.{ConcurrentHashMap, Executors, TimeUnit}

import com.sankuai.octo.mnsc.model.{Env, Path, ServerType}
import com.sankuai.octo.mnsc.model.service.CacheValue
import com.sankuai.octo.mnsc.remote.zk
import com.sankuai.octo.mnsc.utils.mnscCommon
import com.sankuai.sgagent.thrift.model.SGService
import org.joda.time.DateTime
import org.slf4j.{Logger, LoggerFactory}

import scala.collection.JavaConverters._
import scala.collection.concurrent.Map

/**
  * Created by zhangjinlu on 15/10/10.
  * ZK provider-http节点下数据缓存
  */
object appProviderHttpDataCache extends providerBaseCache {
  private val LOG: Logger = LoggerFactory.getLogger(appProviderHttpDataCache.getClass)
  private val pre = mnscCommon.rootPre

  //服务的provider-http缓存，key为"appkey|env"
  private val providerHttpCache = new ConcurrentHashMap[String, CacheValue]() asScala

  private val pathCache = new ConcurrentHashMap[String, scala.collection.concurrent.Map[String, SGService]]() asScala

  private val appkeysWithNodes = new ConcurrentHashMap[String, scala.collection.Set[String]]() asScala

  private val appkeysWithCell = new ConcurrentHashMap[String, scala.collection.Set[String]]() asScala

  private val ipInfoCache = new ConcurrentHashMap[String, scala.collection.Set[String]]() asScala

  private var pullCount4ProviderHttp = 0

  private val scheduler = Executors.newScheduledThreadPool(1)

  private var isCacheUpdate = true

  //强同步和弱同步，每隔20秒执行弱同步，每隔forceBorder * 20秒执行强同步
  def doRenew() = {
    val now = System.currentTimeMillis() / mnscCommon.initDelay4HttpProperties
    val init = 60 - (now % 60)
    LOG.info(s"init doRenew on $now with delay $init")

    //provider-http定时同步任务
    scheduler.scheduleWithFixedDelay(new Runnable {
      def run(): Unit = {
        try {
          if (0 == pullCount4ProviderHttp) {
            //For provider-http
            LOG.info("renewAllProviderHTTPForce start")
            val startProviderHTTP = new DateTime().getMillis
            renewAllProviderForce(Path.providerHttp.toString, true)
            val endProviderHTTP = new DateTime().getMillis
            LOG.info(s"renewAllProviderHTTPForce cost ${endProviderHTTP - startProviderHTTP}")
            //逐出被删除的appKey
            deleteNonexistentAppKey()
          } else {
            LOG.info("renewAllProviderHTTP start")
            val startProviderHTTP = new DateTime().getMillis
            renewAllProvider(providerHttpCache, Path.providerHttp.toString)
            val endProviderHTTP = new DateTime().getMillis
            LOG.info(s"renewAllProviderHTTP cost ${endProviderHTTP - startProviderHTTP}")
          }
          updateAppkeysWithNodes(false)
          pullCount4ProviderHttp = (pullCount4ProviderHttp + 1) % mnscCommon.forceBorder4ProviderHttp
        } catch {
          case e: Exception => LOG.error(s"renew localCache fail.", e)
        }
      }
    }, init, mnscCommon.renewInterval4ProviderHttp * 6, TimeUnit.SECONDS)

    scheduler.scheduleWithFixedDelay(new Runnable {
      def run(): Unit = {
        try {
          LOG.info(s"renewHTTP ip to appkeys map start.")
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
                      providerHttpCache.keySet.contains(key) && providerHttpCache(key).SGServices.exists(_.ip == item._1)
                  }
                  if (!isExists) {
                    removeAppkey += app
                  }
              }
              val newCache = item._2 -- removeAppkey
              ipInfoCache.update(item._1, newCache)
          }
          val endProvider = System.currentTimeMillis()
          LOG.info(s"renewHTTP ip to appkeys map start cost ${endProvider - startProvider}")
        } catch {
          case e: Exception => LOG.error(s"renewHTTP ip to appkeys map start fail.", e)
        }
      }
    }, init, 10, TimeUnit.MINUTES)
  }


  private def updateAppkeysWithNodes(isForce: Boolean) = {
    if (isCacheUpdate || isForce) {
      Env.values.foreach {
        env =>
          val validCache = providerHttpCache.filter(_._2.SGServices.nonEmpty)
          val newAppkeys = validCache.keySet.filter(_.contains(s"|$env")).map(_.split("\\|")(0))
          appkeysWithNodes.update(env.toString, newAppkeys)
          //把set化 + 泳道的appkey都吐给hlb
          val cellAppkeys = validCache.filter(_._2.SGServices.exists(x => Option(x.getCell).getOrElse("").nonEmpty || Option(x.getSwimlane).getOrElse("").nonEmpty))
              .keySet.filter(_.contains(s"|$env")).map(_.split("\\|")(0))
          appkeysWithCell.update(env.toString, cellAppkeys)
      }
      isCacheUpdate = false
    }
  }

  //获取provider-http cache
  def getProviderHttpCache(appkey: String, env: String, isSearchZK: Boolean) = {
    val providers = providerHttpCache.get(getCacheKey(appkey, env))
    if (providers.isEmpty && isSearchZK) {
      LOG.debug(s"providerHttpCache don't exist $appkey|$env")
      val (version, _) = zk.getNodeVersion(s"${mnscCommon.rootPre}/$env/$appkey/${Path.providerHttp.toString}")
      updateProviderCache(version, appkey, env, Path.providerHttp.toString)
      providerHttpCache.get(s"$appkey|$env")
    } else {
      providers
    }
  }

  def getProvidersByIP(ip: String) = {
    val list = providerHttpCache.flatMap(_._2.SGServices.filter(_.ip.equals(ip)))
    list.foreach(_.setServerType(ServerType.http.id))
    list.toList
  }

  def getAppkeysWithProviders(env: String) = {
   val cache = appkeysWithNodes.get(env)
    if(cache.isEmpty){
      updateAppkeysWithNodes(true)
      appkeysWithNodes(env)
    }else{
      cache.get
    }
  }

  def getAppkeysWithCell(env: String) = {
    val cache = appkeysWithCell.get(env)
    if(cache.isEmpty){
      updateAppkeysWithNodes(true)
      appkeysWithCell(env)
    }else{
      cache.get
    }
  }

  def initPathCache(key: String) = {
    getPathCache() += (key -> (new ConcurrentHashMap[String, SGService]() asScala))
  }

  override protected def getPathCache(): Map[String, scala.collection.concurrent.Map[String, SGService]] = pathCache

  override protected def getCache(appkey: String): Map[String, CacheValue] = providerHttpCache

  override protected def getCache() = providerHttpCache

  override protected def getIpInfoCache(): Map[String, scala.collection.Set[String]] = ipInfoCache

  override protected def setIsUpdate(isUpdate: Boolean): Unit = {
    isCacheUpdate = isUpdate
  }
}


