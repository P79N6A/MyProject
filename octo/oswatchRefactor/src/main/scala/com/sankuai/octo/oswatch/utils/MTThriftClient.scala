package com.sankuai.octo.oswatch.utils

import com.meituan.service.mobile.mtthrift.client.pool.MTThriftPoolConfig
import com.meituan.service.mobile.mtthrift.proxy.ThriftClientProxy

object MTThriftClient {
  def getClient[T, F](remoteAppkey: String, t: T) = {
    val cp = cookClientProxy(t)
    cp.setAppKey(Common.appkey)
    cp.setRemoteAppkey(remoteAppkey)

    tryThenCatch[Option[F]]{
      cp.afterPropertiesSet()
      Some(cp.getObject.asInstanceOf[F])
    }(_ => None)
  }

  def tryThenCatch[T](tryProcessor: => T)(catchProcessor: Exception => T): T =
    try tryProcessor catch {case e: Exception => catchProcessor(e)}

  def multiTryThenCatch[T](tryProcessor: => T)(catchProcessor: Exception => T): T =
    try tryProcessor catch {case e: Exception => tryThenCatch(tryProcessor)(catchProcessor)}

  private def cookClientProxy[T](t: T) = {
    val thriftClientProxy = new ThriftClientProxy()
    thriftClientProxy.setServiceInterface(t.getClass)
    thriftClientProxy.setTimeout(25000)
    thriftClientProxy.setClusterManager(Common.clusterManager)

    val mtThriftPoolConfig = new MTThriftPoolConfig()
    mtThriftPoolConfig.setMaxActive(5)
    mtThriftPoolConfig.setMaxIdle(2)
    mtThriftPoolConfig.setMinIdle(1)
    mtThriftPoolConfig.setMaxWait(3000)
    mtThriftPoolConfig.setTestOnBorrow(false)

    thriftClientProxy.setMtThriftPoolConfig(mtThriftPoolConfig)
    thriftClientProxy
  }
}
