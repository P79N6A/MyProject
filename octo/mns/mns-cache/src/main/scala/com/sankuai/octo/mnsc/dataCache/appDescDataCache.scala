package com.sankuai.octo.mnsc.dataCache

import java.util.concurrent.{ConcurrentHashMap, Executors, TimeUnit}

import com.sankuai.octo.mnsc.model.desc.descData
import com.sankuai.octo.mnsc.model.{Env, Path}
import com.sankuai.octo.mnsc.remote.zk
import com.sankuai.octo.mnsc.utils.mnscCommon
import org.joda.time.DateTime
import org.slf4j.{Logger, LoggerFactory}
import play.api.libs.json.{JsError, Json}

import scala.collection.JavaConverters._


/**
  * desc节点下的数据
  */
object appDescDataCache {
  private val LOG: Logger = LoggerFactory.getLogger(appDescDataCache.getClass)
  private val pre = mnscCommon.rootPre

  private val scheduler = Executors.newScheduledThreadPool(1)

  //服务的描述信息缓存，key为"appKey"
  private val descCache = new ConcurrentHashMap[String, descData]() asScala

  //读取传入appKey下的desc节点数据,直接存入descCache
  def getDescPar(appkey: String) = {
    val path = s"$pre/${Env.prod}/$appkey/${Path.desc}"
    try {
      val data = zk.getData(path)
      if (data.nonEmpty) {
        Json.parse(data).validate[descData].asOpt match {
          case Some(x) =>
            descCache.update(s"$appkey", x)
            x
          case other =>
            LOG.error(s"get path $path desc node data invalid $other")
        }
      }
    }
    catch {
      case e: Exception => LOG.error(s"get path $path or Json.validate error ${e.getMessage}", e)
    }
  }

  //定时任务强同步pull，更新所有appKey的desc缓存
  def renewAllDescForce(isPar: Boolean) = {
    val apps = mnscCommon.allAppkeys(isPar)
    val start = new DateTime().getMillis
    apps.foreach {
      appkey => getDescPar(appkey)
    }
    val end = new DateTime().getMillis
    LOG.info(s"renewAllDescForce--> apps.length=${apps.length}  cost ${end - start}")

  }

  //每5分钟进行全量更新
  def doDescRenew() = {
    val now = System.currentTimeMillis() / mnscCommon.initDelay4Desc
    val init = 60 - (now % 60)
    LOG.info(s"init doDescRenew on $now with delay $init")
    scheduler.scheduleAtFixedRate(new Runnable {
      def run(): Unit = {
        try {
          //每5分钟拉取每个appkey的最新desc数据
          renewAllDescForce(true)
          //逐出被删除的appKey
          deleteNonexistentAppKey()
        } catch {
          case e: Exception => LOG.error(s"renewAllDescForce fail.", e)
        }
      }
    }, 5, mnscCommon.renewInterval4Desc, TimeUnit.SECONDS)
  }

  //若在descCache中发现不存在的appkey，删除之
  def deleteNonexistentAppKey() = {
    val newAppkeys = mnscCommon.allApp()
    val cacheAppkeys = descCache.keys.toList
    cacheAppkeys.filter(!newAppkeys.contains(_)).foreach {
      appkey =>
        descCache.remove(appkey)
        LOG.info(s"[deleteNonexistentAppKey] descCache delete $appkey")
    }
  }

  //watcher触发执行动作
  def mnscAppKeyListChangeWatcherAction() = {
    renewAllDescForce(true)
    deleteNonexistentAppKey()
  }

  //获取服务描述信息cache
  def getDescDataAll = {
    descCache
  }
}
