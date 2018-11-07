package com.sankuai.octo.mnsc.zkWatcher

import java.util.concurrent.Executors

import com.sankuai.inf.octo.mns.falcon.FalconCollect
import com.sankuai.octo.mnsc.dataCache.{appDescDataCache, appProviderDataCache, appProviderHttpDataCache}
import com.sankuai.octo.mnsc.model.{Appkeys, Env, Path}
import com.sankuai.octo.mnsc.remote.zk
import com.sankuai.octo.mnsc.utils.mnscCommon
import org.apache.curator.framework.CuratorFramework
import org.apache.curator.framework.api.CuratorWatcher
import org.apache.curator.framework.recipes.cache.{PathChildrenCache, PathChildrenCacheEvent, PathChildrenCacheListener}
import org.apache.zookeeper.WatchedEvent
import org.apache.zookeeper.Watcher.Event.{EventType, KeeperState}
import org.slf4j.{Logger, LoggerFactory}

object appProviderWatcher {
  private val LOG: Logger = LoggerFactory.getLogger(appProviderWatcher.getClass)
  private val pre = mnscCommon.rootPre
  private val prodPath = s"$pre/${Env.prod}"
  private var apps = mnscCommon.allApp()

  private val pathCacheThreadPool = Executors.newFixedThreadPool(3)

  //watch provider节点下的服务节点是否有变更
  class ProviderWatcher(appkey: String, env: String) extends CuratorWatcher {
    def process(event: WatchedEvent): Unit = {
      LOG.info(s"ProviderWatcher event $event for appKey|env = $appkey|$env")

      val watcherPath = s"$pre/$env/$appkey/${Path.provider}"

      event.getType match {
        case EventType.NodeDataChanged =>
          val watcherPath = event.getPath
          val (version, mtime) = zk.getNodeVersion(watcherPath)
          appProviderDataCache.mnscWatcherAction(appkey, env, Path.provider.toString)

          val interval = System.currentTimeMillis - mtime
          LOG.info(s"ProviderWatcher process for appKey|env = $appkey|$env -->  cost ${interval} ms")
          FalconCollect.setMax("MnsCache.ProviderWatcher.cost", "appkey="+appkey, interval)
        case _ => //do nothing
      }

      /*避免reconnect时watcher instance 指数级增加，导致的OOM问题；
        同时，加快新session中Watcher的playback速度，提高mnsc在网络抖动后自愈的速度
       */
      if(EventType.NodeDataChanged == event.getType || KeeperState.Expired == event.getState){
        zk.addDataWatcher(watcherPath, new ProviderWatcher(appkey, env))
      }
    }
  }

  //watch provider-http节点下的服务节点是否有变更
  class ProviderHttpWatcher(appkey: String, env: String) extends CuratorWatcher {
    def process(event: WatchedEvent): Unit = {
      LOG.info(s"ProviderHttpWatcher event $event for appKey|env = $appkey|$env")

      val watcherPath = s"$pre/$env/$appkey/${Path.providerHttp}"

      event.getType match {
        case EventType.NodeDataChanged =>
          val watcherPath = event.getPath
          val (version, mtime) = zk.getNodeVersion(watcherPath)
          appProviderHttpDataCache.mnscWatcherAction(appkey, env, Path.providerHttp.toString)

          val interval = System.currentTimeMillis - mtime
          LOG.info(s"ProviderHttpWatcher process for appKey|env = $appkey|$env -->  cost ${interval} ms")
          FalconCollect.setMax("MnsCache.ProviderHttpWatcher.cost", "appkey="+appkey, interval)
        case _ => //do nothing
      }

      /*避免reconnect时watcher instance 指数级增加，导致的OOM问题；
        同时，加快新session中Watcher的playback速度，提高mnsc在网络抖动后自愈的速度
       */
      if(EventType.NodeDataChanged == event.getType || KeeperState.Expired == event.getState){
        zk.addDataWatcher(watcherPath, new ProviderHttpWatcher(appkey, env))
      }
    }
  }

  //watch是否有appKey的新增或删除
  class appWatcher(path: String) extends CuratorWatcher {
    def process(event: WatchedEvent): Unit = {
      LOG.info(s"appWatcher event $event for path=$path")

      event.getType match {
        case EventType.NodeChildrenChanged =>
          val newApps = mnscCommon.allApp()
          LOG.info(s"new apps $newApps")
          appDescDataCache.mnscAppKeyListChangeWatcherAction()
          appProviderDataCache.deleteNonexistentAppKey()
          appProviderHttpDataCache.deleteNonexistentAppKey()

          newApps.filter(!apps.contains(_)).foreach {
            appkey => registryProviderWatcher4AllEnv(appkey)
          }
          apps = newApps

        case _ => //do nothing
      }

      if(EventType.NodeDataChanged == event.getType ||
        EventType.NodeChildrenChanged == event.getType ||
        KeeperState.Expired == event.getState) {
        zk.addChildrenWatcher(path, new appWatcher(path))
      }

    }
  }

  def registryProviderWatcher4AllEnv(appkey: String) = {
    Env.values.foreach {
      env =>
        //if (!Appkeys.largeAppkeys.contains(appkey)) {
          val providerWatcherPath = s"$pre/$env/$appkey/${Path.provider}"
          zk.addDataWatcher(providerWatcherPath, new ProviderWatcher(appkey, env.toString))
        //}
        val providerHTTPWatcherPath = s"$pre/$env/$appkey/${Path.providerHttp}"
        zk.addDataWatcher(providerHTTPWatcherPath, new ProviderHttpWatcher(appkey, env.toString))
    }
  }

  def initWatcherApp() = {
    val watcherPath = prodPath
    zk.addChildrenWatcher(watcherPath, new appWatcher(prodPath))
  }

  def initWatcherProvider() = {
    apps.par.foreach {
      appkey =>
        LOG.info(s"init provider watcher, appkey = ${appkey}")
        registryProviderWatcher4AllEnv(appkey)
    }
  }

  def initWatcher() = {
    initWatcherApp()
    initWatcherProvider()

    //pathCache watch for large appkeys
  }

  def initPathCache() = {
    mnscCommon.childrenWatchApp.par.foreach {
      app =>
      Env.values.foreach {
        env =>
          val key = s"${app}|${env}"
          appProviderDataCache.initPathCache(app, env.toString)

          val pathChildrenCache = new PathChildrenCache(zk.zkPathCache, s"$pre/$env/$app/${Path.provider}", true, false, pathCacheThreadPool)
          pathChildrenCache.getListenable.addListener(new PathChildrenCacheListener {
            override def childEvent(curatorFramework: CuratorFramework, pathChildrenCacheEvent: PathChildrenCacheEvent): Unit = {
              val znodeTime = pathChildrenCacheEvent.getData.getStat.getMtime

              val data = pathChildrenCacheEvent.getData.getData
              val dataUTF8 = if (null == data) "" else new String(data, "utf-8")
              pathChildrenCacheEvent.getType match {
                case PathChildrenCacheEvent.Type.CHILD_ADDED =>
                  appProviderDataCache.pathCacheUpdateAction(dataUTF8, key, true)
                case PathChildrenCacheEvent.Type.CHILD_UPDATED =>
                  appProviderDataCache.pathCacheUpdateAction(dataUTF8, key, false)
                case PathChildrenCacheEvent.Type.CHILD_REMOVED =>
                  appProviderDataCache.pathCacheRemoveAction(dataUTF8, key)
                case _ =>
              }
              val interval = System.currentTimeMillis - znodeTime
              LOG.info(s"ProviderPathCache process for appKey|env = $app|$env -->  cost ${interval} ms")
            }
          })

          pathChildrenCache.start()
          //仅支持rpc增量更新，http方式改动太大不做处理

          /*val httpKey = s"${app}|${env}"
          appProviderHttpDataCache.initPathCache(httpKey)

          val httpPathChildrenCache = new PathChildrenCache(zk.zkPathCache, s"$pre/$env/$app/${Path.providerHttp}", true)
          httpPathChildrenCache.getListenable.addListener(new PathChildrenCacheListener {
            override def childEvent(curatorFramework: CuratorFramework, pathChildrenCacheEvent: PathChildrenCacheEvent): Unit = {
              val childPath = pathChildrenCacheEvent.getData.getPath
              val data = pathChildrenCacheEvent.getData.getData
              val dataUTF8 = if (null == data) "" else new String(data, "utf-8")
              pathChildrenCacheEvent.getType match {
                case PathChildrenCacheEvent.Type.CHILD_ADDED =>
                  appProviderHttpDataCache.pathCacheAddAction(dataUTF8, httpKey, true)
                case PathChildrenCacheEvent.Type.CHILD_UPDATED =>
                  appProviderHttpDataCache.pathCacheAddAction(dataUTF8, httpKey, false)
                case PathChildrenCacheEvent.Type.CHILD_REMOVED =>
                  appProviderHttpDataCache.pathCacheRemoveAction(dataUTF8, httpKey)
              }
            }
          })
          httpPathChildrenCache.start()*/
      }
    }
  }
}
