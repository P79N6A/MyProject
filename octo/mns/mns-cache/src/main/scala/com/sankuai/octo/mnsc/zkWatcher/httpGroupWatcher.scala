package com.sankuai.octo.mnsc.zkWatcher

import com.sankuai.octo.mnsc.dataCache.httpGroupDataCache
import com.sankuai.octo.mnsc.model.Env
import com.sankuai.octo.mnsc.remote.zk
import com.sankuai.octo.mnsc.utils.mnscCommon
import org.apache.curator.framework.api.CuratorWatcher
import org.apache.zookeeper.WatchedEvent
import org.apache.zookeeper.Watcher.Event.{EventType, KeeperState}
import org.slf4j.{Logger, LoggerFactory}


object httpGroupWatcher {
  private val LOG: Logger = LoggerFactory.getLogger(httpGroupWatcher.getClass)
  private val pre = mnscCommon.rootPre
  private val prodPath = s"$pre/${Env.prod}"
  private var apps = mnscCommon.allApp()
  private val groupPre = mnscCommon.httpGroupPathPre

  //watch 节点下的服务节点是否有变更
  class httpGroupWatcher(appkey: String, env: String) extends CuratorWatcher {
    def process(event: WatchedEvent): Unit = {
      LOG.info(s"httpGroupWatcher event $event for appKey|env = $appkey|$env")

      event.getType match {
        case EventType.NodeDataChanged =>
          httpGroupDataCache.mnscWatcherAction(appkey, env)
        case _ => //do nothing
      }

      /*避免reconnect时watcher instance 指数级增加，导致的OOM问题；
        同时，加快新session中Watcher的playback速度，提高mnsc在网络抖动后自愈的速度
       */
      if(EventType.NodeDataChanged == event.getType || KeeperState.Expired == event.getState){
        registryHttpGroupWatcher(appkey, env)
      }
    }
  }

  def registryHttpGroupWatcher(appkey: String, env: String) = {
    val groupWatcherPath = s"$pre/$env/$appkey$groupPre" //   /mns/sankuai/env/appkey/groups/http
    zk.addDataWatcher(groupWatcherPath, new httpGroupWatcher(appkey, env.toString))
  }

  //watch是否有appKey的新增或删除
  class appkeyWatcher(path: String) extends CuratorWatcher {
    def process(event: WatchedEvent): Unit = {
      LOG.info(s"appkeyWatcher event $event for path=$path")

      event.getType match {
        case EventType.NodeChildrenChanged =>
          val newApps = mnscCommon.allApp()
          LOG.info(s"new apps $newApps")
          httpGroupDataCache.deleteNonexistentAppKey()
          newApps.filter(!apps.contains(_)).foreach {
            appkey =>
              Env.values.foreach {
                env => registryHttpGroupWatcher(appkey, env.toString)
              }
          }
          apps = newApps
        case _ => //do nothing
      }

      /*避免reconnect时watcher instance 指数级增加，导致的OOM问题；
        同时，加快新session中Watcher的playback速度，提高mnsc在网络抖动后自愈的速度
       */
      if(EventType.NodeDataChanged == event.getType ||
        EventType.NodeChildrenChanged == event.getType ||
        KeeperState.Expired == event.getState){
        registAppkeyWatcher()
      }
    }
  }

  def registAppkeyWatcher() = {
    val watcherPath = prodPath
    LOG.info(s"Conna call zk.client.getChildren.usingWatcher for path=$watcherPath")
    zk.addChildrenWatcher(watcherPath, new appkeyWatcher(watcherPath))
  }

  def initWatcher() = {
    registAppkeyWatcher()
    apps.par.foreach {
      appkey =>
        LOG.info("init httpGroup watcher, appkey = {}", appkey)
        Env.values.foreach {
          env =>
            registryHttpGroupWatcher(appkey, env.toString)
        }
    }
  }
}
