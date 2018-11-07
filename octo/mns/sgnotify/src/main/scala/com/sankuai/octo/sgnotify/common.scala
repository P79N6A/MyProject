package com.sankuai.octo.sgnotify

import org.slf4j.{LoggerFactory, Logger}
import com.sankuai.inf.octo.mns.util.ProcessInfoUtil

object common {
  private final val LOG: Logger = LoggerFactory.getLogger(common.getClass)

  def isOffline = {
    !ProcessInfoUtil.isLocalHostOnline
  }

}
