package com.sankuai.octo.statistic.processor

import com.meituan.service.mobile.mtthrift.proxy.ThriftClientProxy
import com.sankuai.octo.statistic.ConsistentHashLoadBalancer
import com.sankuai.octo.statistic.service.{LogExportService, LogStatisticService}
import org.slf4j.LoggerFactory

import scala.collection.mutable


class ExportServiceProxy(val appkey: String) {

  private val logger = LoggerFactory.getLogger(this.getClass)

  private def init(): LogExportService = {
    try {
      val proxy: ThriftClientProxy = new ThriftClientProxy
      proxy.setAppKey(appkey)
      proxy.setRemoteAppkey("com.sankuai.inf.data.statistic")
      proxy.setClusterManager("OCTO")
      proxy.setServiceInterface(classOf[LogExportService])
      proxy.setTimeout(10000)
      proxy.afterPropertiesSet()
      val client: LogExportService = proxy.getObject.asInstanceOf[LogExportService]
      client
    } catch {
      case e: Exception =>
        logger.error("StatisticServiceProxy init fail", e)
        throw new RuntimeException("StatisticServiceProxy init fail", e)
    }
  }
}

object ExportServiceProxy {
  val proxyMap = mutable.HashMap[String, LogExportService]()

  def apply(appkey: String): LogExportService = {
    require(appkey != null)
    proxyMap.getOrElseUpdate(appkey, new ExportServiceProxy(appkey).init())
  }
}