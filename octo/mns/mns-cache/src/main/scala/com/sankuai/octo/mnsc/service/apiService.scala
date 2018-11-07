package com.sankuai.octo.mnsc.service

import java.text.SimpleDateFormat
import java.util.Date

import com.sankuai.inf.octo.mns.util.{IpUtil, ProcessInfoUtil}
import com.sankuai.octo.idc.model.Idc
import com.sankuai.octo.mnsc.dataCache.appProviderDataCache
import com.sankuai.octo.mnsc.model.{Appkeys, Env, Path, service}
import com.sankuai.octo.mnsc.model.service.{CacheValue, ProviderNode}
import com.sankuai.octo.mnsc.remote.zk
import com.sankuai.octo.mnsc.utils.{api, ipCommon, mnscCommon}
import com.sankuai.sgagent.thrift.model.{SGService, fb_status}
import org.apache.commons.lang.StringUtils
import org.slf4j.{Logger, LoggerFactory}
import play.api.libs.json.Json

import scala.collection.JavaConverters._

/**
  * Created by lhmily on 01/13/2016.
  */
object apiService {
  private val LOG: Logger = LoggerFactory.getLogger(apiService.getClass)

  private def getTimeMillis(time: String) = {
    val ret = try {
      val dateFormat = new SimpleDateFormat("yy-MM-dd HH:mm:ss");
      val dayFormat = new SimpleDateFormat("yy-MM-dd");
      val curDate = dateFormat.parse(dayFormat.format(new Date()) + " " + time);
      curDate.getTime()
    } catch {
      case e: Exception =>
        //ignore
        0l
    }
    ret
  }

  private def getSamePrefixIP(list: List[SGService], ip: String) = {
    val defaultIdc = new Idc()
    defaultIdc.setIdc("")

    val ipIdc = IpUtil.getIdcInfoFromLocal(List(ip).asJava).asScala.getOrElse(ip, defaultIdc).getIdc
    if (ipIdc.nonEmpty) {
      list.filter(x => IpUtil.getIdcInfoFromLocal(List(x.ip).asJava).asScala.getOrElse(x.ip, defaultIdc).getIdc == ipIdc)
    } else {
      List()
    }
  }

  private def isListAlive(list: List[SGService]) = {
    list.foldLeft(false) {
      (ret, item) =>
        ret || (item.status == fb_status.ALIVE.getValue)
    }
  }

  private def handleServiceList(list: List[SGService], ip: String) = {
    val isSH = ipCommon.isShangHai(ip)
    val regionList = list.filter(x => ipCommon.isShangHai(x.ip) == isSH)

    val sameIDCList = getSamePrefixIP(regionList, ip)
    if (isListAlive(sameIDCList)) {
      sameIDCList
    } else if (isListAlive(regionList)) {
      regionList
    } else {
      list
    }
  }

  private def isContainMacTag(str: String) = {
    val lowerCaseStr = StringUtils.lowerCase(str)
    StringUtils.contains(lowerCaseStr, "macbook") || StringUtils.contains(lowerCaseStr, "mac.local")
  }

  private def isContainOfflineTag(str: String) = {
    val lowerCaseStr = StringUtils.lowerCase(str)
    isContainMacTag(str) || StringUtils.contains(lowerCaseStr, ".corp.sankuai.com") || StringUtils.contains(lowerCaseStr, ".office.mos")
  }


  private def isContainMacIp(ip: String) = {
    ip.startsWith("172.18.") || ip.startsWith("172.30.") || ip.startsWith("192.168.") || ip.startsWith("172.24.")
  }

  private def isOfflineRequest(host: String, hostName: String, ip: String) = {
    isContainOfflineTag(hostName) || isContainOfflineTag(host) || isContainMacIp(ip)
  }

  private def isMacRequest(host: String, hostName: String, ip: String) = {
    isContainMacTag(hostName) || isContainMacTag(host) || isContainMacIp(ip)
  }

  def getServiceList(appkey: String, env: String, host: String, hostName: String, ip: String) = {
    if (ProcessInfoUtil.isLocalHostOnline && isOfflineRequest(host, hostName, ip)) {
      api.errorJsonArgInvalid("invalid parameters, offline cannot access online.")
    } else {
      val services = appProviderDataCache.getProviderCache(appkey, env, false).getOrElse(CacheValue("", List()))
      //TODO 结合host hostName来做过滤
      val list = handleServiceList(services.SGServices, ip)
      val retList = if (list.isEmpty) {
        List()
      } else {
        list.map {
          item =>
            service.SGService2ProviderNode(item)
        }
      }
      val retCode = if (retList.isEmpty) 404 else 200
      api.dataJson(retCode, Map("serviceList" -> retList))
    }

  }

  def getSgagentList(ip: String) = {
    val nodes = Env.values.map {
      env =>
        val path = s"${
          mnscCommon.rootPre
        }/$env/${
          Appkeys.sgAgent
        }/${
          Path.provider
        }/$ip:${
          Appkeys.sgAgentPort
        }"
        if (zk.exist(path)) {
          val data = zk.getData(path)
          Json.parse(data).validate[ProviderNode].asOpt
        } else {
          None
        }
    }
    val retList = nodes.flatMap(item => item)
    val retCode = if (retList.isEmpty) 404 else 200
    api.dataJson(retCode, Map("serviceList" -> retList))
  }
}
