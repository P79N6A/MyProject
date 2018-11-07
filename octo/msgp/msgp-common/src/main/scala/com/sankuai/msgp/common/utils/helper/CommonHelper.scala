package com.sankuai.msgp.common.utils.helper

import java.net.NetworkInterface

import com.meituan.mtrace.Endpoint
import com.sankuai.inf.octo.mns.util.ProcessInfoUtil
import com.sankuai.msgp.common.model._
import com.sankuai.msgp.common.service.org.BusinessOwtService
import org.apache.commons.lang3.StringUtils
import org.slf4j.{Logger, LoggerFactory}

import scala.collection.JavaConverters._
import scala.collection.concurrent.TrieMap

object CommonHelper {
  private final val LOG: Logger = LoggerFactory.getLogger(CommonHelper.getClass)

  private val isOnline = ProcessInfoUtil.isLocalHostOnline

  case class Pair(name: String, value: Int)

  val SG_AGENT_APPKEY = "com.sankuai.inf.sg_agent"

  val IDC_SET = Seq("dx", "yf", "cq", "rz", "wj", "jx", "dbc", "other")

  val ipIDCCache = TrieMap[String, String]()


  val EnvDesc = if (isOffline) {
    "线下"
  } else {
    "线上"
  }

  def businessList = Business.values().map {
    x => Map("name" -> x.getName, "value" -> x.getId).asJava
  }.toList.sortBy(_.get("value").asInstanceOf[Int]).asJava

  def businessPairList = Business.values().map {
    x => Pair(x.getName, x.getId)
  }.toList.asJava

  def owtList(business: Int) = {
    BusinessOwtService.getOwtList(business)
  }

  def pdlList(owt: String) = {
    BusinessOwtService.getPdlList(owt)
  }

  def levelList = toList(Level)

  def statusMap = toMapById(Status)

  def envMap = toMapById(Env).asScala.filterKeys(_ != 0).toMap.asJava

  def roleMap = toMapById(Role)

  def toList(enum: Enumeration) = enum.values.map {
    x => Map("name" -> x.toString, "value" -> x.id).asJava
  }.toList.sortBy(_.get("value").asInstanceOf[Int]).asJava

  def toPairList(enum: Enumeration) = enum.values.map(x => Pair(x.toString, x.id)).toList.asJava

  def toStringList(enum: Enumeration) = enum.values.map(x => '"' + x.toString + ',' + x.id + '"').toList.asJava

  def toMap(enum: Enumeration) = enum.values.map(x => x.toString -> x.id).toMap.asJava

  def toMapById(enum: Enumeration) = enum.values.map(x => x.id -> x.toString).toMap.asJava

  def toMap(cc: AnyRef) = {
    (Map[String, Any]() /: cc.getClass.getDeclaredFields) {
      (a, f) =>
        f.setAccessible(true)
        a + (f.getName -> f.get(cc))
    }
  }

  def toJavaMap(cc: AnyRef) = {
    toMap(cc).asJava
  }

  def notEmpty(s: String) = s != null && !s.trim.isEmpty

  def notNull(s: String) = s != null


  def encode(text: String) = {
    scalaj.http.Base64.encodeString(text)
    //Base64.encodeBase64String(text.getBytes("utf-8"))
  }

  def decode(text: String) = {
    scalaj.http.Base64.decodeString(text)
    //new String(Base64.decodeBase64(text), "utf-8")
  }

  val getLocalIp = {
    val netInterfaces = NetworkInterface.getNetworkInterfaces.asScala
    val ips = netInterfaces.flatMap {
      x =>
        x.getInetAddresses.asScala.flatMap {
          ip =>
            if (!ip.isSiteLocalAddress && !ip.isLoopbackAddress && ip.getHostAddress.indexOf(":") == -1) {
              Some(ip.getHostAddress)
            } else if (ip.isSiteLocalAddress && !ip.isLoopbackAddress && ip.getHostAddress.indexOf(":") == -1) {
              Some(ip.getHostAddress)
            } else None
        }.toList
    }.toList
    LOG.info(s"getLocalIp $ips")
    ips.head
  }
  /**
    * true :线下环境
    * false: 线上环境
    */
  val isOffline = !isOnline


  def localPoint() = {
    val ip = getLocalIp
    new Endpoint("com.sankuai.inf.msgp", ip, 0)
  }

  //NOTE 该函数不会对输入参数ip做检测
  def ip2IDC(ip: String) = {
    if (!checkIP(ip)) {
      "other"
    } else {
      try {
        ipIDCCache.get(ip) match {
          case Some(idc) =>
            idc
          case None =>
            val idcInfo = ProcessInfoUtil.getIdcInfo(List(ip).asJava)
            if (idcInfo.containsKey(ip)) {
              idcInfo.asScala.map(x => ipIDCCache.put(x._1, x._2.idc))
              idcInfo.get(ip).getIdc
            } else {
              LOG.error(s"idc list is empty. ip : $ip")
              "other"
            }
        }
      }
      catch {
        case e: Exception =>
          LOG.info(s"stackTrace: ${e.printStackTrace()}")
          LOG.error(s"get idc failed. ip : $ip", e)
          "other"
      }
    }
  }

  def ips2IDCs(ips: java.util.List[String]) = {
    ProcessInfoUtil.getIdcInfo(ips)
  }

  def checkIP(ip: String) = {
    if (StringUtils.isEmpty(ip)) false
    else {
      ip.matches("((?:(?:25[0-5]|2[0-4]\\d|((1\\d{2})|([1-9]?\\d)))\\.){3}(?:25[0-5]|2[0-4]\\d|((1\\d{2})|([1-9]?\\d))))")
    }
  }
}
