package com.sankuai.octo.statistic.processor

import com.meituan.service.mobile.mtthrift.proxy.ThriftClientProxy
import com.sankuai.meituan.config.listener.IConfigChangeListener
import com.sankuai.octo.statistic.util.config
import com.sankuai.octo.statistic.service.LogExportService
import org.apache.commons.lang3.StringUtils
import org.slf4j.LoggerFactory

import scala.collection.concurrent.TrieMap

object ExportService {
  private val LOG = LoggerFactory.getLogger(ExportService.getClass)
  //statistic服务的map
  private val serverMap: TrieMap[String, LogExportService] = TrieMap()

  private val LOCAL_APPKEY = "com.sankuai.inf.data.statistic"
  private val REMOTE_APPKEY = "com.sankuai.inf.data.export"
  private val EXPORT_LIST = "export.list"


  //指定appkey、host的map，key是appkey，value是ip
  private var exportMap: Map[String, Array[String]] = constructExport(getExportList)

  val PORT = 8970

  {
    try {
      //指定定host构建
      config.addListener(EXPORT_LIST, new IConfigChangeListener() {
        def changed(key: String, oldValue: String, newValue: String) = {
          LOG.error(s"StatisticService export.list change $key oldValue: $oldValue newValue: $newValue")
          exportMap = constructExport(newValue)
        }
      })
      LOG.info("addListener success")
    }
    catch {
      case e: Exception => LOG.error("addListener failed", e)
    }
  }

  def isExportAppkey(appkey: String): Boolean = {
    exportMap.contains(appkey)
  }

  def getIpByAppkey(appkey: String) = {
    exportMap.getOrElse(appkey, Array[String]())
  }

  def apply(paramAppkey: String) = {
    val ips = getIpByAppkey(paramAppkey)
    val ip = ips.apply(0)
    serverMap.getOrElseUpdate(ip, constructService(ip))
  }

  def apply(paramAppkey: String, groupType: Int) = {
    val ips = getIpByAppkey(paramAppkey)
    val index = groupType % ips.size
    val ip = ips.apply(index)
    serverMap.getOrElseUpdate(ip, constructService(ip))
  }

  //构建mtthrift client proxy
  private def constructService(ip: String): LogExportService = {
    try {
      val proxy: ThriftClientProxy = new ThriftClientProxy
      proxy.setAppKey(LOCAL_APPKEY)
      proxy.setRemoteAppkey(REMOTE_APPKEY)
      proxy.setServiceInterface(classOf[LogExportService])
      proxy.setServerIpPorts(s"$ip:$PORT")
      proxy.afterPropertiesSet()
      proxy.setTimeout(3000)
      val client: LogExportService = proxy.getObject.asInstanceOf[LogExportService]
      client
    } catch {
      case e: Exception =>
        LOG.error("LogExportService init fail", e)
        throw new RuntimeException("LogExportService init fail", e)
    }
  }


  //获取MCC的指定appkey到host
  private def getExportList = {
    config.get(EXPORT_LIST, "")
  }

  //构建指定的host的map
  def constructExport(value: String) = {
    if (StringUtils.isBlank(value)) {
      Map[String, Array[String]]()
    } else {
      try {
        value.split(";").map {
          x =>
            val tmp = x.split(":")
            val key = tmp(0)
            val value = tmp(1).split(",")
            key -> value
        }.toMap
      } catch {
        case e: Exception => LOG.error(s"$value 不符合格式，例子：" +
          s"com.sankuai.inf.logCollector,com.sankuai.inf.mnsc:10.4.232.74;com.sankuai.inf.msgp:10.4.232.76,value :${value}", e)
          Map[String, Array[String]]()
      }
    }
  }

}