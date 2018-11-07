package com.sankuai.octo.msgp.utils.client

import java.util

import com.sankuai.meituan.config.listener.IConfigChangeListener
import com.sankuai.msgp.common.config.MsgpConfig
import com.sankuai.msgp.common.utils.helper.CommonHelper
import org.apache.curator.framework.CuratorFrameworkFactory
import org.apache.curator.framework.api.transaction.{CuratorTransaction, CuratorTransactionFinal}
import org.apache.curator.retry.RetryUntilElapsed
import org.slf4j.{Logger, LoggerFactory}

import scala.collection.JavaConverters._

object ZkHlbClient {

  private val LOG: Logger = LoggerFactory.getLogger(ZkClient.getClass)
  val zk_host = "hlb.zk.host"

  private var INDEX = 0

  val singleHostCount = 3
  private var zkList = (1 to singleHostCount).flatMap(x => getZk()).toList

  {
    zkListener()
  }

  def zkListener() = {
    MsgpConfig.addListener(zk_host, new IConfigChangeListener() {
      override def changed(key: String, oldValue: String, newValue: String): Unit = {
        println(s"changed key ${key} ,oldValue:${oldValue} ,newValue:${newValue}")
        zkList = (1 to singleHostCount).flatMap(x => getZk()).toList
      }
    })
  }


  def getZk() = {
    getZKUrl.split(",").map {
      host =>
        val superClient = CuratorFrameworkFactory.builder.connectString(host).retryPolicy(new RetryUntilElapsed(3000, 2000)).build()
        LOG.info("start zk client with " + host)
        superClient.start()
        superClient
    }
  }

  def client = {
    //val index = (new DateTime().getMillis % zkCount).toInt
    var index = 0
    synchronized {
      index = INDEX
      INDEX = (INDEX + 1) % zkList.length
    }
    zkList(index)
  }

  private def getZKUrl: String = {
    val normalStr = if(CommonHelper.isOffline) {
      "10.20.63.234:2181,10.20.60.153:2181,10.20.61.154:2181"
    }else{
      "gh-zookeeper-inf-mns01:2181,gh-zookeeper-inf-mns02:2181,gh-zookeeper-inf-mns03:2181"
    };
    val configUrl = MsgpConfig.get("zk.host",normalStr)
    configUrl
  }

  def createWithParent(path: String) {
    client.create().creatingParentsIfNeeded().forPath(path)
  }

  def create(path: String, data: String) {
    client.create().creatingParentsIfNeeded().forPath(path, data.getBytes("utf-8"))
  }

  def getData(path: String): java.lang.String = {
    val data = client.getData.forPath(path)
    if (data == null) "" else new String(data, "utf-8")
  }

  def setDataInTransaction(path: String, data: String, transaction: CuratorTransaction): CuratorTransactionFinal = {
    transaction.setData().forPath(path, data.getBytes("utf-8")).and()
  }

  /**
    * 需要捕获异常 使用setDataWithEx
    * @param path
    * @param data
    */
  @deprecated
  def setData(path: String, data: String) {
    try {
      client.setData().forPath(path, data.getBytes("utf-8"))
    } catch {
      case e: Exception => LOG.error(s"saveData path:$path,data:$data", e)
    }
  }

 def setDataWithEx(path: String, data: String) {
   client.setData().forPath(path, data.getBytes("utf-8"))
 }

  def children(path: String): java.util.List[String] = {
    try {
      if (exist(path)) {
        client.getChildren.forPath(path)
      } else {
        new util.ArrayList[String]()
      }
    } catch {
      case e: Exception => {
        LOG.error(s"get zk-child from $path catch", e)
        List[String]().asJava
      }
    }
  }

  def exist(path: String): Boolean = {
    client.checkExists().forPath(path) != null
  }

  def deleteWithChildren(path: String) {
    try {
      client.delete().deletingChildrenIfNeeded().forPath(path)
    } catch {
      case e: Exception => LOG.error(s"delete $path catch", e)
    }
  }

  @throws(classOf[Exception])
  def deleteProvider(path: String) {
    client.delete().deletingChildrenIfNeeded().forPath(path)
  }
}