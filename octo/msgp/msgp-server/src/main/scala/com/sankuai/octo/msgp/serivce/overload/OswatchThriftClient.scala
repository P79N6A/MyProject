package com.sankuai.octo.msgp.serivce.overload

import com.meituan.service.mobile.mtthrift.client.pool.MTThriftPoolConfig
import com.meituan.service.mobile.mtthrift.proxy.ThriftClientProxy
import org.slf4j.{Logger, LoggerFactory}

/**
 * Created by dreamblossom on 15/10/6.
 */
object OswatchThriftClient {
  val LOG: Logger = LoggerFactory.getLogger(OswatchThriftClient.getClass)

  def getClient[T, F](remoteAppkey: String, t: T) = {
    val cp = cookClientProxy(t)
    cp.setAppKey("com.sankuai.inf.octo.msgp")
    cp.setRemoteAppkey(remoteAppkey)

    tryThenCatch[Option[F]]{
      cp.afterPropertiesSet()
      Some(cp.getObject.asInstanceOf[F])
    }(_ => None)
  }

  def tryThenCatch[T](tryProcessor: => T)(catchProcessor: Exception => T): T =
    try tryProcessor catch {case e: Exception => catchProcessor(e)}

  private def cookClientProxy[T](t: T) = {
    val thriftClientProxy = new ThriftClientProxy()
    thriftClientProxy.setServiceInterface(t.getClass)
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

//    def main(args: Array[String]) = {
//      def test1() = {
//        val client = getClient[OSWatchService, OSWatchService.Iface]("com.sankuai.inf.octo.oswatch", new OSWatchService)
//        val monitorPolicy = new MonitorPolicy(0L, "com.sankuai.chenxi.test_provider_a",  EnvType.PROD, true, false).setIdc("dx").setQps(60)
//        tryThenCatch({client.foreach(_.addMonitorPolicy(monitorPolicy,"responseUrl"));println("OK")})(e => println(e))
//       //  tryThenCatch({client.foreach(_.delMonitorPolicy(0));println("OK")})(e => println(e))
//      }
//      test1()
//    }
}
