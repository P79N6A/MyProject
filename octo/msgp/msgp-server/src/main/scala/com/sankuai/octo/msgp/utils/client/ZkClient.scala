package com.sankuai.octo.msgp.utils.client

import java.util

import com.sankuai.meituan.config.listener.IConfigChangeListener
import com.sankuai.msgp.common.config.MsgpConfig
import com.sankuai.msgp.common.utils.helper.CommonHelper
import com.sankuai.octo.msgp.service.zk.ZkConnectionStateListener
import org.apache.curator.framework.CuratorFrameworkFactory
import org.apache.curator.retry.ExponentialBackoffRetry
import org.apache.zookeeper.data.Stat
import org.slf4j.{Logger, LoggerFactory}

import scala.collection.JavaConverters._

object ZkClient {
  private val LOG: Logger = LoggerFactory.getLogger(ZkClient.getClass)

  private var zkClient = getZk

  {
    zkListener()
  }

  def zkListener() = {
    MsgpConfig.addListener("zk.host", new IConfigChangeListener() {
      override def changed(key: String, oldValue: String, newValue: String): Unit = {
        println(s"changed key $key ,oldValue:$oldValue ,newValue:$newValue")
        zkClient = getZk
      }
    })
  }

  def client = zkClient

  def getZk = {
    val connectString = url
    val superClient = CuratorFrameworkFactory.builder.connectString(connectString).retryPolicy(new ExponentialBackoffRetry(3000, 5)).build()
    LOG.info("start zk client with " + connectString)
    val path = "/octotest"
    superClient.getConnectionStateListenable.addListener(new ZkConnectionStateListener(path, connectString))
    superClient.start()
    superClient
  }


  private def url: String = {
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

  /**
    * 需要捕获异常 使用setDataWithEx
    * @param path
    * @param data
    */
  @deprecated
  def setData(path: String, data: String) {
    try {
      if(path.contains("/desc")){
        LOG.info(s"##zkclient setData has write desc path = $path and data = $data")
      }
      val zkNode = client.checkExists().forPath(path)
      if (zkNode.isInstanceOf[Stat]) {
        val currentVersion = zkNode.getVersion
        client.setData().withVersion(currentVersion).forPath(path, data.getBytes("utf-8"))
      } else {
        client.setData().forPath(path, data.getBytes("utf-8"))
      }
    } catch {
      case e: Exception =>
        LOG.error(s"saveData path:$path, data:$data", e)
    }
  }

  def setDataWithEx(path: String,data: String) {
    if(path.contains("/desc")){
      LOG.info(s"##zkclient setDataWithEx has write desc path = $path and data = $data")
    }
    val zkNode = client.checkExists().forPath(path)
    if (zkNode.isInstanceOf[Stat]) {
      val currentVersion = zkNode.getVersion
      client.setData().withVersion(currentVersion).forPath(path, data.getBytes("utf-8"))
    } else {
      client.setData().forPath(path, data.getBytes("utf-8"))
    }
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
    client.checkExists().forPath(path.trim) != null
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
