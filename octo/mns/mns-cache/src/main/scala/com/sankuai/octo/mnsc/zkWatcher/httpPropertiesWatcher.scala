package com.sankuai.octo.mnsc.zkWatcher

import com.sankuai.octo.mnsc.dataCache.httpPropertiesDataCache
import com.sankuai.octo.mnsc.model.{Env, Path}
import com.sankuai.octo.mnsc.remote.zk
import com.sankuai.octo.mnsc.utils.mnscCommon
import org.apache.curator.framework.api.CuratorWatcher
import org.apache.zookeeper.WatchedEvent
import org.apache.zookeeper.Watcher.Event.{EventType, KeeperState}
import org.slf4j.{Logger, LoggerFactory}

/**
  * Created by zhangjinlu on 15/10/14.
  */
object httpPropertiesWatcher {
  private val LOG: Logger = LoggerFactory.getLogger(httpPropertiesWatcher.getClass)
  private val pre = mnscCommon.rootPre
  private val prodPath = s"$pre/${Env.prod}"
  private var apps = mnscCommon.allApp()

  //watch provider节点下的服务节点是否有变更
  class HttpPropertiesWatcher(appkey: String, env: String) extends CuratorWatcher {
    def process(event: WatchedEvent): Unit = {
      LOG.info(s"HttpPropertiesWatcher event $event for appKey|env = $appkey|$env")

      event.getType match {
        case EventType.NodeDataChanged =>
          val watcherPath = event.getPath
          val (version, _) = zk.getNodeVersion(watcherPath)
          httpPropertiesDataCache.mnscWatcherAction(appkey, version, env)

        case _ => //do nothing
      }

      /*避免reconnect时watcher instance 指数级增加，导致的OOM问题；
        同时，加快新session中Watcher的playback速度，提高mnsc在网络抖动后自愈的速度
       */
      if(EventType.NodeDataChanged == event.getType || KeeperState.Expired == event.getState){
        registryHttpPropertiesWatcher(appkey, env)
      }
    }
  }

  def registryHttpPropertiesWatcher(appkey: String, env: String) = {
    val propertiesWatcherPath = s"$pre/$env/$appkey/${Path.httpProperties}"
    zk.addDataWatcher(propertiesWatcherPath, new HttpPropertiesWatcher(appkey, env.toString))
  }

  //watch是否有appKey的新增或删除
  class appkeyWatcher(path: String) extends CuratorWatcher {
    def process(event: WatchedEvent): Unit = {
      LOG.info(s"appkeyWatcher event $event for path=$path")

      event.getType match {
        case EventType.NodeChildrenChanged =>
          val newApps = mnscCommon.allApp()
          LOG.info(s"new apps $newApps")
          httpPropertiesDataCache.deleteNonexistentAppKey()
          newApps.filter(!apps.contains(_)).foreach {
            appkey =>
              Env.values.foreach {
                env => registryHttpPropertiesWatcher(appkey, env.toString)
              }
          }
          apps = newApps
        case _ => //do nothing
      }

      /* 避免reconnect时watcher instance 指数级增加，导致的OOM问题；
         同时，加快新session中Watcher的playback速度，提高mnsc在网络抖动后自愈的速度 */
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
        LOG.info("init httpProperties watcher, appkey = {}", appkey)
        Env.values.foreach {
          env =>
            registryHttpPropertiesWatcher(appkey, env.toString)
        }
    }
  }
}
