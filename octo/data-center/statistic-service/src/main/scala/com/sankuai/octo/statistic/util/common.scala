package com.sankuai.octo.statistic.util

import com.sankuai.inf.octo.mns.ProcessInfoUtil
import org.slf4j.{Logger, LoggerFactory}

object common {
  private final val logger: Logger = LoggerFactory.getLogger(common.getClass)

  val getLocalIp = {
    // 获取本地内网 IP
    val ip = ProcessInfoUtil.getLocalIpV4()
    logger.info(s"localIp:$ip")
    ip
  }

  val isOffline = {
    val res = !ProcessInfoUtil.isLocalHostOnline
    logger.info(s"isOffline:$res")
    res
  }

}
