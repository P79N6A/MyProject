package com.sankuai.inf.octo.sbttemplate.client

import com.meituan.mtrace.thrift.TException
import com.meituan.service.mobile.mtthrift.client.pool.MTThriftPoolConfig
import com.meituan.service.mobile.mtthrift.proxy.ThriftClientProxy
import com.sankuai.inf.octo.scalatemplate.HelloService
import com.sankuai.inf.octo.scalatemplate.HelloService.{Iface => HSI}

/**
 * Created by chenxi on 9/15/15.
 */

object MTThriftClient {

  def getClient() = {
    val cp = cookClientProxy
    cp.setAppKey("com.sankuai.inf.octo.sbttemplate")
    cp.setRemoteAppkey("com.sankuai.inf.octo.sbttemplate")

    tryThenCatch[Option[HSI]]{
      cp.afterPropertiesSet()
      Some(cp.getObject.asInstanceOf[HelloService.Iface])
    }(_ => None)
  }

  def main(args: Array[String]) {
    val mtClient = MTThriftClient.getClient()

    (0 until 100).foreach{_ =>
      tryThenCatch[Option[String]](mtClient.map(_.hi()))(e => Some(e.getMessage)).map(println)
      Thread sleep 1000
    }
  }

  private def tryThenCatch[T](tryProcessor: => T)(catchProcessor: Exception => T): T =
    try tryProcessor catch {case e: Exception => catchProcessor(e)}

  private def cookClientProxy = {
    val thriftClientProxy = new ThriftClientProxy()
    thriftClientProxy.setServiceInterface(new HelloService().getClass)
    thriftClientProxy.setTimeout(5000)
    thriftClientProxy.setClusterManager("OCTO")

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
