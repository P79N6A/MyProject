package com.sankuai.octo.aggregator

import org.apache.commons.validator.routines.InetAddressValidator
import org.slf4j.{Logger, LoggerFactory}

import scala.util.matching.Regex

object IpValidator {
  private val LOG: Logger = LoggerFactory.getLogger(IpValidator.getClass)

  private val validator = new InetAddressValidator()

  val ipPattern = new Regex( """^([0-9]{1,3}[\.]){3}[0-9]{1,3}$""")

  def filterExternalIp(originalIp: String): String = {
    val ip = originalIp.replaceAll("thrift://", "").replaceAll("http://", "")
    if (originalIp.startsWith("127.0.0.1") || originalIp.startsWith("10.") || originalIp.startsWith("192.168.")
      || originalIp.startsWith("172.26.") || originalIp.startsWith("172.27.")) {
      originalIp
    } else if (validator.isValidInet4Address(ip)) {
      "external"
    } else {
      LOG.info("ip :{}", originalIp)
      "external"
    }
  }
}
