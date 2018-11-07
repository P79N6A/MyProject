package com.sankuai.octo.oswatch

import java.net.NetworkInterface
import org.slf4j.{LoggerFactory, Logger}
import scala.collection.JavaConverters._

object common {
  private final val LOG: Logger = LoggerFactory.getLogger(common.getClass)

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
