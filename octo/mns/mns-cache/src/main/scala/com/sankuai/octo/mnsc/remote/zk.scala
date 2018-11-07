package com.sankuai.octo.mnsc.remote

import java.util

import com.sankuai.inf.octo.mns.util.ProcessInfoUtil
import com.sankuai.octo.mnsc.utils.{config, mnscCommon}
import org.apache.commons.lang.StringUtils
import org.apache.curator.framework.CuratorFrameworkFactory
import org.apache.curator.framework.api.CuratorWatcher
import org.apache.curator.retry.RetryUntilElapsed
import org.apache.zookeeper.data._
import org.slf4j.{Logger, LoggerFactory}
import scala.collection.JavaConverters._
import scala.util.control.Breaks


object zk {
  private val LOG: Logger = LoggerFactory.getLogger(zk.getClass)
  private val singleHostCount = mnscCommon.singleHostCount4ZK
  private val superAuth = "super:P!vxQr6w0d8o*f`"
  private var INDEX = 0
  private val zkList = (1 to singleHostCount).flatMap(x => getZk).toList
  //仅使用一条连接操作watcher，避免多条连接互相注册，导致实利数量增加
  private val zkWatcher = getWatcherZk
  val zkPathCache = getWatcherZk
  private val zkCount = zkList.length
  private val zkClientLock = new Object()


  private val backupZKUrl = if (ProcessInfoUtil.isLocalHostOnline) {
    //online
    "10.32.145.200:2181"
  } else {
    //offline
    "10.4.245.245:2181"
  }


  private def getZk = {
    getURLCn2.map {
      host =>
        val superClient = CuratorFrameworkFactory.builder.connectString(host).retryPolicy(new RetryUntilElapsed(3000, 2000)).build()
        LOG.info("start zk client with " + host)
        superClient.start()
        superClient
    }
  }

  private def getWatcherZk = {
      val superClient = CuratorFrameworkFactory.builder.connectString(url).retryPolicy(new RetryUntilElapsed(3000, 2000)).build()
      superClient.start()
      superClient
  }

  private def getURLCn2 = {
    val urlsArr = url.split(",")
    val ret = new util.ArrayList[String]
    var x = 0
    while (x < urlsArr.length) {
      var y = x + 1
      while (y < urlsArr.length) {
        ret.add(s"${urlsArr(x)},${urlsArr(y)}")
        y += 1
      }
      x += 1
    }
    if (ret.isEmpty) {
      ret.add(url)
    }
    ret.asScala
  }

  def client() = {
    val loop = new Breaks
    var curIndex = getZkIndex()
    var ret = zkList(curIndex)
    loop.breakable {
      for (i <- 1 to zkCount) {
        ret = zkList(curIndex)
        if (!ret.getZookeeperClient.isConnected) {
          curIndex = getNextIndex(curIndex)
        } else {
          loop.break()
        }
      }
    }
    ret
  }

  /*def clientWatcher() = {
    val loop = new Breaks
    var curIndex = getZkWatcherIndex()
    var ret = zkWatcherList(curIndex)
    loop.breakable {
      for (i <- 1 to zkWatcherCount) {
        ret = zkWatcherList(curIndex)
        if (!ret.getZookeeperClient.isConnected) {
          curIndex = getNextWatcherIndex(curIndex)
        } else {
          loop.break()
        }
      }
    }
    ret
  }*/

  private def getNextIndex(curIndex: Int) = (curIndex + 1) % zkCount

  private def getZkIndex() = zkClientLock.synchronized {
    INDEX = getNextIndex(INDEX)
    INDEX
  }

  /*private def getNextWatcherIndex(curIndex: Int) = (curIndex + 1) % zkWatcherCount
  private def getZkWatcherIndex() = zkWatcherClientLock.synchronized {
    INDEXWatcher = getNextWatcherIndex(INDEXWatcher)
    INDEXWatcher
  }*/

  private def url: String = {
    val localip = ProcessInfoUtil.getLocalIpV4
    val zkKey = try {
      val idcList = ProcessInfoUtil.getIdcInfo(List(localip).asJava)
      if (null == idcList) {
        "zk.host"
      } else {
        val idcInfo = idcList.get(localip)
        if (null == idcInfo) {
          "zk.host"
        } else {
          if ("shanghai".equalsIgnoreCase(idcInfo.region)) {
            "sh.zk.host"
          } else {
            "zk.host"
          }
        }
      }
    } catch {
      case e: Exception =>
        LOG.error("failed to parse the idc info of localip.", e)
        "zk.host"
    }

    val configUrl = config.get(zkKey)
    if (StringUtils.isEmpty(configUrl)) backupZKUrl else configUrl
  }

  def getData(path: String): java.lang.String = {
    try {
      val data = client.getData.forPath(path)
      if (data == null) "" else new String(data, "utf-8")
    } catch {
      case e: Exception => {
        LOG.error(s"function zk.getData exception.", e)
        ""
      }
    }
  }

  def children(path: String) = {
    try {
        client.getChildren.forPath(path).asScala
    } catch {
      case e: Exception => {
        LOG.error("function zk.children exception.", e)
        List[String]()
      }
    }
  }

  def exist(path: String): Boolean = {
    try {
      client.checkExists().forPath(path) != null
    } catch {
      case e: Exception => {
        LOG.error("function zk.exist exception.", e)
        false
      }
    }
  }

  /*def existWatcher(path: String): Boolean = {
    try {
      //用client进行探测，而非clientWatcher，避免同步操作阻塞addWatcher
      //同时就算client发起重建session操作，也不会销毁clientWatcher保存的watchers
      //如果链接已经断开，返回true，表示节点存在；避免watcher丢失
      //因此，mnsc只能针对进程级网络恢复；无法针对线程zk_client级别进行恢复；受到架构限制。
      !client.getZookeeperClient.isConnected || client.checkExists().forPath(path) != null
    } catch {
      case e: Exception => {
        LOG.error("clientWatcher function zk.exist exception.", e)
        false
      }
    }
  }*/

  private def addWatcher(path: String, watcher: CuratorWatcher, isData: Boolean) = {
      try {
        if (isData) {
          zkWatcher.getData.usingWatcher(watcher).inBackground().forPath(path)
        } else {
          zkWatcher.getChildren.usingWatcher(watcher).inBackground().forPath(path)
        }
        true
      } catch {
        case e: Exception =>
          LOG.error("fail to add watcher.", e)
          false
      }
  }

  def addDataWatcher(path: String, watcher: CuratorWatcher) = {
    addWatcher(path, watcher, true)
  }

  def addChildrenWatcher(path: String, watcher: CuratorWatcher) = {
    addWatcher(path, watcher, false)
  }

  def deleteWithChildren(path: String) {
    try {
      client.delete().deletingChildrenIfNeeded().forPath(path)
    } catch {
      case e: Exception => LOG.error(s"delete $path catch", e)
    }
  }

  def getNodeState(path: String) = {
    try {
      client.checkExists().forPath(path)
    } catch {
      case e: Exception => {
        LOG.error(s"function zk.getNodeState exception.", e)
        null
      }
    }
  }

  def getNodeVersion(path: String) = {
    val nodeState = getNodeState(path)
    if (null != nodeState) {
      (s"${nodeState.getMtime}|${nodeState.getCversion}|${nodeState.getVersion}", nodeState.getMtime)
    } else {

      (null, -101l)
    }
  }

  def getNodeVersion(nodeState: Stat) = {
    if (null != nodeState) {
      s"${nodeState.getMtime}|${nodeState.getCversion}|${nodeState.getVersion}"
    } else {
      null
    }
  }

  def getNodeVersionIsZkConnected(path: String) = {
    try {
      val currentZk = client()
      if (currentZk.getZookeeperClient.getZooKeeper.getState.isConnected) {
        getNodeVersion(client().checkExists().forPath(path))
      } else {
        null
      }
    } catch {
      case e: Exception =>
        //ignore error
        null
    }
  }

  def versionCompare(inputVersion: String, cacheVersion: String, defaultValue: Boolean, f: (Long, Long) => Boolean) = {
    if (null == inputVersion || null == cacheVersion) {
      defaultValue
    } else {
      val inputVersionArray = inputVersion.split("\\|")
      val cacheVersionArray = cacheVersion.split("\\|")
      if (inputVersionArray.length == 3 && cacheVersionArray.length == 3) {
        f(inputVersionArray(2).toLong, cacheVersionArray(2).toLong)
      } else {
        defaultValue
      }
    }

  }
}
