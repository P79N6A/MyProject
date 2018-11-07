package com.sankuai.octo.mnsc.utils

import com.sankuai.inf.octo.mns.util.{IpUtil, ProcessInfoUtil}
import org.apache.commons.lang.StringUtils
import scala.collection.JavaConverters._


/**
  * Created by lhmily on 01/14/2016.
  */
object ipCommon {
  private final val ipRegex = "^(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|[1-9])\\." +
    "(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)\\." +
    "(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)\\." +
    "(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)$"

  private final val shProdIpPreOnline = List("10.1.", "10.3.", "10.6.", "10.101.")
  private final val shProdIpPreOffline = List("10.2.", "10.66.")
  private final val shIpPreOffice = List("10.128.", "172.24.")
  private final val shanghaiIP = List(shProdIpPreOnline, shProdIpPreOffline, shIpPreOffice).flatten



  def getPrefixOfIP(ip: String) = {
    val ips = ip.split("\\.")
    val prefix = List(ips(0), ips(1)).mkString(".")
    s"$prefix."
  }

  def checkIP(ip: String) = {
    if (StringUtils.isBlank(ip)) {
      false
    } else {
      ip.trim.matches(ipRegex)
    }
  }

  private def isStartWith(list: List[String], ip: String) = {
    list.foldLeft(false) {
      (ret, item) =>
        ret || ip.startsWith(item)
    }
  }

  def isShangHai(ip: String) = {
    val idcs = ProcessInfoUtil.getIdcInfo(List(ip).asJava)
    val isSH = null != idcs && idcs.containsKey(ip) && "shanghai".equalsIgnoreCase(idcs.get(ip).getRegion)
    isSH || isStartWith(shanghaiIP, ip)
  }


}
