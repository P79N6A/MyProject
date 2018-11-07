package com.sankuai.octo.mnsc.utils

import com.sankuai.octo.mnsc.model.Path

object zkCommon {
  private val pre = mnscCommon.rootPre

  def getProtocolPath(appkey: String, env: String, protocol: String) = {
    if ("thrift" == protocol) {
      s"$pre/$env/$appkey/${Path.provider}"

    } else if ("http" == protocol) {
      s"$pre/$env/$appkey/${Path.providerHttp}"
    } else {
      s"$pre/$env/$appkey/providers/$protocol"
    }
  }
}
