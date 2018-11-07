package com.sankuai.octo.mnsc.dataCache

import java.util.concurrent.{ConcurrentHashMap, Executors, TimeUnit}

import com.sankuai.octo.mnsc.model.httpProperties._
import com.sankuai.octo.mnsc.model.{Env, Path}
import com.sankuai.octo.mnsc.remote.zk
import com.sankuai.octo.mnsc.utils.mnscCommon
import org.apache.commons.lang3.StringUtils
import org.joda.time.DateTime
import org.slf4j.{Logger, LoggerFactory}
import play.api.libs.json.Json

import scala.collection.JavaConverters._

/**
  * Created by zhangjinlu on 15/10/10.
  */
object httpPropertiesDataCache {
  private val LOG: Logger = LoggerFactory.getLogger(httpPropertiesDataCache.getClass)

  private val httpPropertiesCache = new ConcurrentHashMap[String, PropertiesValue]() asScala

  private var pullCount4HttpProperties = 0

  private val scheduler = Executors.newScheduledThreadPool(1)

  def getHttpPropertiesCacheByAppKey(appkey: String, env: String) = {
    val properties = httpPropertiesCache.get(getCacheKey(appkey, env))
    properties match {
      case Some(value) => properties
      case None =>
        LOG.warn(s"httpPropertiesCache don't exist $appkey|$env")
        getHttpPropertiesFromZk(appkey, env)
        httpPropertiesCache.get(getCacheKey(appkey, env))
    }
  }

  private def getCacheKey(appkey: String, env: String) = s"$appkey|$env"

  def jsonProp2PropertiesValue(prop: HttpProperties, version: String) = {

    val currentProperty = new scala.collection.mutable.HashMap[String, String]()
    if (prop.health_check_issue.isDefined && 1 == prop.health_check_issue.get.is_health_check) {
      val item = prop.health_check_issue.get
      if (item.health_check.isDefined) {
        currentProperty.update("health_check", item.health_check.get)
      }
      currentProperty.update("centra_check_type", item.centra_check_type)
      if (item.centra_http_send.isDefined) {
        currentProperty.update("centra_http_send", item.centra_http_send.get)
      }
    }

    if (prop.domain_issue.isDefined) {
      val item = prop.domain_issue.get
      if (StringUtils.isNotEmpty(item.domain_name)) {
        currentProperty.update("domain_name", item.domain_name)
        if (StringUtils.isNotEmpty(item.domain_location)) {
          currentProperty.update("domain_location", item.domain_location)
        }
      }
    }
    if (prop.load_balance_issue.isDefined) {
      val item = prop.load_balance_issue.get
      currentProperty.update("load_balance_type", item.load_balance_type)
      currentProperty.update("load_balance_value", item.load_balance_value)
    }
    if (prop.slow_start_issue.isDefined) {
      val item = prop.slow_start_issue.get
      currentProperty.update("is_slow_start", String.valueOf(item.is_slow_start))
      currentProperty.update("slow_start_value", item.slow_start_value)
    } 

    if(prop.extend_issue.isDefined){
      val item = prop.extend_issue.get
      if(item.keepalive.isDefined){
        currentProperty.update("keepalive",String.valueOf(item.keepalive.get))
      }
      if(item.keepalive_timeout.isDefined){
        currentProperty.update("keepalive_timeout",String.valueOf(item.keepalive_timeout.get))

      }
    }

    PropertiesValue(version, currentProperty.toMap)
  }


  //根据appKey及环境env从ZK获取http_properties节点数据
  def getHttpPropertiesFromZk(appkey: String, env: String) = {
    val path = s"${mnscCommon.rootPre}/$env/$appkey/${Path.httpProperties}"
    try {
      val (version, _) = zk.getNodeVersion(path)
      val data = zk.client.getData.forPath(path)
      val dataUTF8 = if (null == data) "" else new String(data, "utf-8")
      if (dataUTF8.nonEmpty) {
        Json.parse(dataUTF8).validate[HttpProperties].asOpt match {
          case Some(x) =>
            val propertiesVal = jsonProp2PropertiesValue(x, version)
            httpPropertiesCache.update(getCacheKey(appkey, env), propertiesVal)
            propertiesVal
          case _ =>
            LOG.error(s"get path $path node data invalid")
        }
      }
    } catch {
      case e: Exception => LOG.error(s"get path $path or Json.validate ${e.getMessage}")
    }
  }

  private def getPath(appkey: String, env: String) = s"${mnscCommon.rootPre}/$env/$appkey/${Path.httpProperties}"

  //定时任务弱同步pull http-properties
  def renewAllHttpProperties() = {
    val apps = mnscCommon.allApp().par
    apps.foreach {
      appkey =>
        Env.values.foreach {
          env =>
            val path = getPath(appkey, env.toString)
            val nodeState = zk.getNodeState(path)
            if (null == nodeState) {
              LOG.error(s"renewAllHttpProperties failed, $path don't exist")
            } else {
              val version = zk.getNodeVersion(nodeState)
              val propertiesVal = httpPropertiesCache.get(getCacheKey(appkey, env.toString))
              propertiesVal match {
                case Some(value) =>
                  if (version != value.version) {
                    getHttpPropertiesFromZk(appkey, env.toString)
                  }
                case None =>
                  getHttpPropertiesFromZk(appkey, env.toString)
              }
            }
        }
    }
  }

  //定时任务强同步pull provider
  def renewAllHttpPropertiesForce(isPar: Boolean) = {
    val apps = mnscCommon.allAppkeys(isPar)
    val start = System.currentTimeMillis
    Env.values.foreach {
      env =>
        apps.foreach {
          appkey =>
            getHttpPropertiesFromZk(appkey, env.toString)
        }
    }
    val end = System.currentTimeMillis
    LOG.info(s"renewAllHttpPropertiesForce--> apps.length=${apps.length}  cost ${end - start}")

  }

  //强同步和弱同步，每隔20秒执行弱同步，每隔forceBorder * 20秒执行强同步
  def doRenew() = {
    val now = System.currentTimeMillis() / mnscCommon.initDelay4HttpProperties
    val init = 60 - (now % 60)
    LOG.info(s"init doRenew on $now with delay $init")
    scheduler.scheduleWithFixedDelay(new Runnable {
      def run(): Unit = {
        try {
          if (0 == pullCount4HttpProperties) {
            LOG.info(s"renewAllHttpPropertiesForce start. count=${pullCount4HttpProperties + 1}")
            val start = new DateTime().getMillis
            renewAllHttpPropertiesForce(true)
            val end = new DateTime().getMillis
            LOG.info(s"renewAllHttpPropertiesForce cost ${end - start}")

            deleteNonexistentAppKey()
          } else {
            LOG.info(s"renewAllHttpProperties start. count=${pullCount4HttpProperties + 1}")
            val start = new DateTime().getMillis
            renewAllHttpProperties()
            val end = new DateTime().getMillis
            LOG.info(s"renewAllHttpProperties cost ${end - start}")
          }
          pullCount4HttpProperties = (pullCount4HttpProperties + 1) % mnscCommon.forceBorder4HttpProperties
        } catch {
          case e: Exception => LOG.error(s"renew localCache fail $e")
        }
      }
    }, init, mnscCommon.renewInterval4HttpProperties * 2, TimeUnit.SECONDS)
  }

  //若在providerCache中发现不存在的appkey，删除之
  def deleteNonexistentAppKey() = {
    val newAppkeys = mnscCommon.allApp()
    val cacheAppkeys = httpPropertiesCache.keySet.map(x => x.stripSuffix("|prod").stripSuffix("|stage").stripSuffix("|test"))
    LOG.info(s"delete appkey. httpPropertiesCache=${httpPropertiesCache.keySet.size} cacheAppkeys=${cacheAppkeys.size} newAppkeys=${newAppkeys.size}")

    cacheAppkeys.filter(!newAppkeys.contains(_)).foreach {
      appkey =>
        Env.values.foreach {
          env =>
            httpPropertiesCache.remove(s"$appkey|$env")
            LOG.info(s"[deleteNonexistentAppKey] httpPropertiesCache delete $appkey|$env")
        }
    }
  }

  //watcher触发执行动作
  def mnscWatcherAction(appkey: String, version: String, env: String) = {
    LOG.info(s"mnscWatcherAction triggered. appkey-version-env = $appkey-$version-$env")
    val properties = httpPropertiesCache.get(s"$appkey|$env")
    properties match {
      case Some(value) =>
        if (version != value.version) {
          getHttpPropertiesFromZk(appkey, env)
        }
      case None =>
        LOG.error(s"httpPropertiesCache don't exist $appkey|$env")
        getHttpPropertiesFromZk(appkey, env)
    }
  }


}
