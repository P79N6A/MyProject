package com.sankuai.octo.mnsc.dataCache

import java.util.concurrent.{ConcurrentHashMap, Executors, TimeUnit}

import com.sankuai.octo.mnsc.dataCache.httpPropertiesDataCache.{LOG, getHttpPropertiesFromZk, httpPropertiesCache}
import com.sankuai.octo.mnsc.model.Env
import com.sankuai.octo.mnsc.remote.zk
import com.sankuai.octo.mnsc.utils.mnscCommon
import org.joda.time.DateTime
import org.slf4j.{Logger, LoggerFactory}

import scala.collection.JavaConverters._
import com.sankuai.octo.mnsc.idl.thrift.model.{Constants, HttpGroup, groupNode}
import com.sankuai.octo.mnsc.model.httpGroup.httpGroup
import play.api.libs.json._

import scala.collection.concurrent.TrieMap
import scala.util.parsing.json.JSON
import play.api.libs.json

/**
  * Created by zhoufeng on 16/8/21.
  */
object httpGroupDataCache {
  private val LOG: Logger = LoggerFactory.getLogger(httpGroupDataCache.getClass)
  private val pre = mnscCommon.rootPre
  private val groupPre = mnscCommon.httpGroupPathPre
  private val unknownValue = "unknown"
  //http group缓存
  private val groupCache = new ConcurrentHashMap[String, TrieMap[String, TrieMap[String, HttpGroup]]]() asScala

  private val scheduler = Executors.newScheduledThreadPool(1)

  def getGroupCache(appkey: String, env: String) = {
    val apkGroup = groupCache.get(s"$env|$appkey")
    apkGroup match {
      case Some(value) => apkGroup
      case None =>
        LOG.debug(s"appkey: $appkey doesn't have http groups")
        //getGroupStr(appkey, env)
        groupCache.get(s"$env|$appkey")
    }
  }

  def getGroupStr(appkey: String, env: String) = {
    val path = s"$pre/$env/$appkey$groupPre"
    try {
      val groups = TrieMap[String, HttpGroup]()
      if (!zk.exist(path)) {
        groups
      } else {
        //对比zk version，如变化则更新
        val (zkVersion, _) = zk.getNodeVersion(path)
        val groupItems = groupCache.get(s"$env|$appkey")
        groupItems match {
          case Some(versionMap: TrieMap[String, TrieMap[String, HttpGroup]]) =>{
            val cacheVersion = versionMap.keys.toList.head
            //比较version，若无变化直接返回
            if (zk.versionCompare(cacheVersion, zkVersion, true, (arg1: Long, arg2: Long) => arg1 == arg2)) {
              groupItems.get(cacheVersion)
            } else {
              //version变化，则更新
              updateGroupCache(appkey, env, path, groups, zkVersion);
            }
          }
          case None => {
            // cache无该条数据，则更新
            updateGroupCache(appkey, env, path, groups, zkVersion);
          }
          case other => LOG.warn("Unknown data structure")
        }
        groups
      }
    } catch {
      case e: Exception => LOG.error(s"get path $path fail ${e.getMessage}")
    }
  }

  def updateGroupCache(appkey: String, env: String, path: String, groups: TrieMap[String, HttpGroup], zkVersion: String) = {
    val groupList = zk.children(path).toList
    groupList.foreach {
      groupName =>
        val group = JSON.parseFull(zk.getData(s"$path/$groupName"))
        group match {
          case Some(map: Map[String, Any]) => {
            val httpGroupData = new HttpGroup()
            httpGroupData.groupName = map.getOrElse("group_name", unknownValue).toString
            httpGroupData.appkey = map.getOrElse("appkey", unknownValue).toString
            val groupNodeList = map.get("server") match {
              case Some(serverList: List[Map[String, Any]]) =>
                serverList.map(node => new groupNode(node.getOrElse("ip", unknownValue).toString, node.getOrElse("port", 0).toString.toDouble.toInt))
              case None =>
                LOG.warn("Unknown data structure")
                List[groupNode]()
            }
            httpGroupData.server = groupNodeList.asJava
            groups.update(groupName, httpGroupData)
            groupCache.update(s"$env|$appkey", scala.collection.concurrent.TrieMap(zkVersion -> groups))
          }
          case None => LOG.warn("Unknown data structure")
        }
    }
  }

  def getAllGroups(env: String) = {
    groupCache.filter(x => x._1.startsWith(s"$env|"))
  }

  def renewAllGroups(isPar: Boolean) = {
    val apps = mnscCommon.allAppkeys(isPar)

    val start = new DateTime().getMillis
    Env.values.foreach {
      env =>
        apps.foreach {
          appkey =>
            getGroupStr(appkey, env.toString)
        }
    }
    val end = new DateTime().getMillis
    LOG.info(s"renewAllGroups--> apps.length=${apps.length}  cost ${end - start}")

  }

  def doRenew() = {
    val now = System.currentTimeMillis() / mnscCommon.initDelay4HttpProperties
    val init = 60 - (now % 60)
    LOG.info(s"init doRenew on $now with delay $init")
    scheduler.scheduleWithFixedDelay(new Runnable {
      def run(): Unit = {
        try {
          LOG.info(s"renewAllUpstream start.")
          val start = new DateTime().getMillis
          renewAllGroups(true)
          val end = new DateTime().getMillis
          LOG.info(s"renewAllGroup cost ${end - start}")
        } catch {
          case e: Exception => LOG.error(s"renew localCache fail $e")
        }
      }
    }, init, 20, TimeUnit.SECONDS)
  }

  //若在providerCache中发现不存在的appkey，删除之
  def deleteNonexistentAppKey() = {
    val newAppkeys = mnscCommon.allApp()
    val cacheAppkeys = groupCache.keySet.map(x => x.stripSuffix("|prod").stripSuffix("|stage").stripSuffix("|test"))
    LOG.info(s"delete appkey. groupCache=${groupCache.keySet.size} cacheAppkeys=${cacheAppkeys.size} newAppkeys=${newAppkeys.size}")
    cacheAppkeys.filter(!newAppkeys.contains(_)).foreach {
      appkey =>
        Env.values.foreach {
          env =>
            groupCache.remove(s"$appkey|$env")
            LOG.info(s"[deleteNonexistentAppKey] groupCache delete $appkey|$env")
        }
    }
  }
  //watcher触发执行动作
  def mnscWatcherAction(appkey: String, env: String) = {
    LOG.info(s"mnscWatcherAction triggered. appkey-env = $appkey-$env")
    val apkGroup = groupCache.get(s"$env|$appkey")
    apkGroup match {
      case Some(value) =>
          getGroupStr(appkey, env)
      case None =>
        LOG.debug(s"appkey: $appkey doesn't have http groups")
        //getGroupStr(appkey, env)
        getGroupStr(appkey, env)
    }
  }

}


