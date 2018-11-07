package com.sankuai.octo.service

import java.net.NetworkInterface

import org.slf4j.{Logger, LoggerFactory}

import scala.collection.JavaConverters._

object ScalaCommon {
  private final val LOG: Logger = LoggerFactory.getLogger(ScalaCommon.getClass)

  val localIp = {
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
    LOG.info(s"localIp $ips")
    ips.head
  }

  def isOffline = {
    !localIp.startsWith("10.")
  }
}
