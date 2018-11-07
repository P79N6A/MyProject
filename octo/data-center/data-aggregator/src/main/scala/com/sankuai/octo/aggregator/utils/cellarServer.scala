package com.sankuai.octo.aggregator.utils

import dispatch.url
import org.slf4j.LoggerFactory
import play.api.libs.json.Json

object cellarServer {
  private val LOGGER = LoggerFactory.getLogger(this.getClass)

  private val cellarHost = "http://cellar.sankuai.com"

  case class CellarAppkey(cfgServerAppkeys: List[String], groupAppkeys: List[String])

  implicit val misIdIPsReads = Json.reads[CellarAppkey]
  implicit val misIdIPsWrites = Json.writes[CellarAppkey]

  def cellarAppkeys() = {
    try {
      val request = s"$cellarHost/info/ds/allCellarAppkey/get"
      val msg = http.execute(url(request), text =>
        Json.parse(text).validate[CellarAppkey].asOpt)
      msg
    } catch {
      case e: Exception => LOGGER.error(s"get cellarAppkeys fail", e)
        None
    }
  }
}