package com.sankuai.octo.mnsc.utils


import com.sankuai.octo.mnsc.model.Appkeys
import com.sankuai.octo.mnsc.remote.zk
import org.apache.commons.lang3.StringUtils
import org.slf4j.{Logger, LoggerFactory}

import scala.collection.JavaConverters._


object mnscCommon {
  private val LOG: Logger = LoggerFactory.getLogger(mnscCommon.getClass)

  // sg_agent's appkey
  // root path for MNS in ZK
  val rootPre = "/mns/sankuai"

  val httpGroupPathPre = "/groups/http"

  //provider线程第一执行delay时间模值
  val initDelay4Provider = 100
  //provider弱同步时间间隔
  val renewInterval4Provider = 20
  //20秒
  //provider强同步时间间隔为renewInterval4Provider * forceBorder4Provider
  val forceBorder4Provider = 120

  //provider线程第一执行delay时间模值
  val initDelay4ProviderHttp = 1000
  //provider-http弱同步时间间隔
  val renewInterval4ProviderHttp = 20
  //20秒
  //provider-http强同步时间间隔renewInterval4ProviderHttp * forceBorder4ProviderHttp
  val forceBorder4ProviderHttp = 120


  //provider线程第一执行delay时间模值
  val initDelay4HttpProperties = 3000
  //http-properties弱同步时间间隔
  val renewInterval4HttpProperties = 20
  //20秒
  //http-properties强同步时间间隔renewInterval4HttpProperties * forceBorder4HttpProperties
  val forceBorder4HttpProperties = 120

  //group cache同步时间间隔
  val renewHttpGroups = 10 * 60

  //provider线程第一执行delay时间模值
  val initDelay4Desc = 9000
  //desc全量同步时间间隔
  val renewInterval4Desc = 5 * 60 //5分钟


  //每个zk节点建立的zkClient数量； mns-zk对单台主机的连接数做了限制，并且线上和线下连接数限制不同
  val singleHostCount4ZK = 2

  //节点数超过mutlThreadSize时才采用多线程的方式读取
  val mutlThreadSize = 20

  private var appkeys = List[String]()

  private val appkeysPath = s"$rootPre/prod"

  private def getAppFromZK() = {
    val testAppkeys = System.getProperty("mnscCacheLoadAppkeys4Test")
    val remoteAppkeys =if(StringUtils.isNotEmpty(testAppkeys)){
      testAppkeys.trim.split(",").toList
    }else{
      zk.children(appkeysPath).toList
    }

    if (remoteAppkeys.nonEmpty) {
      appkeys = remoteAppkeys
    }
    appkeys
  }

  //get all appKey list except sg_agent and kms_agent
  def allApp() = {
    getAppFromZK()
    appkeys.filter(!Appkeys.noCacheAppkeys.contains(_))
  }

  def ancestorWatchApp() = {
    allApp().filter(!Appkeys.largeAppkeys.contains(_))
  }

  def childrenWatchApp() = {
    Appkeys.largeAppkeys
  }

  def allAppkeysList() = {
    getAppFromZK().asJava
  }

  def allAppkeys(isPar: Boolean) = {
    if (isPar) {
      mnscCommon.allApp().par
    } else {
      mnscCommon.allApp()
    }
  }
}
