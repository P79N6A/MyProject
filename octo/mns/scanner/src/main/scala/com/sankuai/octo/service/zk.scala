package com.sankuai.octo.service

import com.sankuai.octo.scanner.Common
import com.sankuai.octo.scanner.util.ScanUtils
import org.apache.curator.framework.api.ACLProvider
import org.apache.curator.framework.api.transaction.{CuratorTransaction, CuratorTransactionFinal}
import org.apache.curator.framework.{CuratorFramework, CuratorFrameworkFactory}
import org.apache.curator.retry.RetryUntilElapsed
import org.apache.zookeeper.ZooDefs.Perms
import org.apache.zookeeper.data._
import org.apache.zookeeper.server.auth.DigestAuthenticationProvider
import org.slf4j.{Logger, LoggerFactory}

import scala.collection.JavaConverters._

object zk {
  private val LOG: Logger = LoggerFactory.getLogger(zk.getClass)

  private var zkClient: Option[CuratorFramework] = None

  private def superAuth = "super:P!vxQr6w0d8o*f`"

  private def superAclProvider = {
    val superAcls = List(new ACL(Perms.ALL, new Id("digest", DigestAuthenticationProvider.generateDigest(superAuth)))).asJava
    new ACLProvider {
      def getDefaultAcl: java.util.List[ACL] = superAcls

      def getAclForPath(path: String): java.util.List[ACL] = superAcls
    }
  }

  def client = {
    if (zkClient.isEmpty) {
      var zkUrl = url;
      if (Common.isOnline && ScanUtils.hostIpPrefix.startsWith(Common.gqIpPrefix)) {
        zkUrl = Common.gqZkObserverAddress
      }
      val superClient = CuratorFrameworkFactory.builder.connectString(zkUrl).retryPolicy(new RetryUntilElapsed(3000, 2000)).build()
      superClient.start()
      zkClient = Some(superClient)
    }
    zkClient.get
  }

  private val url = config.get("zk.host", "10.4.245.244:2181,10.4.245.245:2181,10.4.245.246:2181")

  def createWithParent(path: String) {
    zk.client.create().creatingParentsIfNeeded().forPath(path)
  }

  def create(path: String, data: String) {
    zk.client.create().creatingParentsIfNeeded().forPath(path, data.getBytes("utf-8"))
  }

  def getData(path: String): java.lang.String = {
    val data = client.getData.forPath(path)
    if (data == null) "" else new String(data, "utf-8")
  }

  def setDataInTransaction(path: String, data: String, transaction: CuratorTransaction): CuratorTransactionFinal = {
    transaction.setData().forPath(path, data.getBytes("utf-8")).and()
  }

  def setData(path: String, data: String) {
    try {
      client.setData().forPath(path, data.getBytes("utf-8"))
    } catch {
      case e: Exception => LOG.info(f"catch exception $e")
    }
  }

  def children(path: String): java.util.List[String] = {
    client.getChildren.forPath(path)
  }

  def exist(path: String): Boolean = {
    client.checkExists().forPath(path) != null
  }

  def deleteWithChildren(path: String) {
    try {
      client.delete().deletingChildrenIfNeeded().forPath(path)
    } catch {
      case e: Exception => LOG.error(s"delete $path catch $e")
    }
  }
}
