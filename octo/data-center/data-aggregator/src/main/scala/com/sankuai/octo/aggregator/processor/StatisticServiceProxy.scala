package com.sankuai.octo.aggregator.processor

import com.meituan.service.mobile.mtthrift.proxy.ThriftClientProxy
import com.sankuai.octo.statistic.ConsistentHashLoadBalancer
import com.sankuai.octo.statistic.service.LogStatisticService
import org.slf4j.LoggerFactory

import scala.collection.mutable

class StatisticServiceProxy(val appkey: String) {

  private val logger = LoggerFactory.getLogger(this.getClass)

  private def init(): LogStatisticService = {
    try {
      val proxy: ThriftClientProxy = new ThriftClientProxy
      proxy.setAppKey(appkey)
      proxy.setRemoteAppkey("com.sankuai.inf.data.statistic")
      proxy.setClusterManager("OCTO")
      proxy.setServiceInterface(classOf[LogStatisticService])
      proxy.setTimeout(10000)
      proxy.setUserDefinedBalancer(classOf[ConsistentHashLoadBalancer])
      proxy.afterPropertiesSet()
      val client: LogStatisticService = proxy.getObject.asInstanceOf[LogStatisticService]
      client
    } catch {
      case e: Exception =>
        logger.error("StatisticServiceProxy init fail", e)
        throw new RuntimeException("StatisticServiceProxy init fail", e)
    }
  }
}

object StatisticServiceProxy {
  val proxyMap = mutable.HashMap[String, LogStatisticService]()

  def apply(appkey: String): LogStatisticService = {
    require(appkey != null)
    proxyMap.getOrElseUpdate(appkey, new StatisticServiceProxy(appkey).init())
  }
}
