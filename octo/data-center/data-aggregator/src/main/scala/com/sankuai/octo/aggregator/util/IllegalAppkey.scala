package com.sankuai.octo.aggregator.util

object IllegalAppkey {
  private val SG_AGENT = "com.sankuai.inf.sg_agent"

  def illegal(appkey: String) = {
    if (appkey.equalsIgnoreCase(SG_AGENT)
      || appkey.startsWith("\"")
      || appkey.startsWith("{")
      || appkey.startsWith("$")
      || appkey.contains("appkey")
      || appkey.contains("appKey")) {
      true
    } else {
      false
    }
  }
}
