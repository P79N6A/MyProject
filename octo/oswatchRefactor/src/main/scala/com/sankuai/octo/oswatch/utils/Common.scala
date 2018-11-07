package com.sankuai.octo.oswatch.utils

import java.net.NetworkInterface
import scala.collection.JavaConverters._
import org.slf4j.{LoggerFactory, Logger}

/**
 * Created by dreamblossom on 15/9/29.
 */
object Common {
  private final val LOG: Logger = LoggerFactory.getLogger(Common.getClass)

  final val appkey = "com.sankuai.inf.octo.oswatch"
  final val port = 9111
  final val clusterManager = "OCTO"
  final val ONE_SECOND_IN_MS = 1000
  final val harborAppkey = "com.sankuai.inf.hulk.harbor"

  //默认线下
  var logURL = MTConfig.get("log-url","http://query.octo.test.sankuai.info")
  var msgpURL = MTConfig.get("msgp-url","http://octo.test.sankuai.info")
  val falconURL = MTConfig.get("falcon-url","http://10.4.243.29:9966") // 线上http://query.falcon.vip.sankuai.com:9966
  // loaderActor load monitorPolicy 时间间隔 秒
  var checkInterval=MTConfig.get("check-interval","3").trim.toDouble
  // WatcherActor 在等待actorExitPeriod个轮询周期未收到消息后退出
  var actorExitPeriod = MTConfig.get("actor-exit-period","2").trim.toInt
  // http查询超时时间 秒
  var httpTimeoutInterval = MTConfig.get("http-timeout-interval","3").trim.toInt
  // qps 查询区间 秒
  var qpsMinWatchPeriodInSecond = MTConfig.get("qps-min-watch-interval-inSecond","60").trim.toInt
  // http 请求失败轮训次数
  var httpFailQueryTimes = MTConfig.get("http-fail-queryTimes","3").trim.toInt
  // http请求失败轮训时间间隔 秒
  var httpFailQueryWaitTime = MTConfig.get("http-fail-query-waitTime","2").trim.toInt
  //actorSelection 超时时间
  var actorSelectionTimeout = MTConfig.get("actorSelection-timeout","1").trim.toInt

  def getLocalIp() = {
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
}
