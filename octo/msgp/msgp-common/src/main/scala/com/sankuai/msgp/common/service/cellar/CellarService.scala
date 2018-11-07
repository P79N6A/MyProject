package com.sankuai.msgp.common.service.cellar

import com.sankuai.msgp.common.utils.HttpUtil
import com.sankuai.msgp.common.utils.HttpUtil.RequestType
import org.slf4j.LoggerFactory
import play.api.libs.json.Json

object CellarService {
  private val cellarHost = "http://cellar.vip.sankuai.com"
  private val serverAppkeyUri = "/api/info/ds/serverAppkey"
  private val LOGGER = LoggerFactory.getLogger(this.getClass)
  private val CLIENT_ID = "octo"
  private val CLIENT_SECRET = "8786d9bb-113c-4e72-9fd5-8e031e22"
  case class CellarGroupAppkey(data: Option[List[String]], success: Boolean, message: Option[String])
  implicit val misIdIPsReads = Json.reads[CellarGroupAppkey]
  implicit val misIdIPsWrites = Json.writes[CellarGroupAppkey]
  def cellarAppkeys() = {
    try {
      val url = s"$cellarHost$serverAppkeyUri"
      val response = HttpUtil.httpGetRequest(url, HttpUtil.getBAAuthHeader(CLIENT_ID, CLIENT_SECRET, RequestType.GET, serverAppkeyUri), null)
      val obj = Json.parse(response).asOpt[CellarGroupAppkey]
      if (obj.get.success) {
        obj
      } else {
        None
      }
    } catch {
      case e: Exception => LOGGER.error(s"get cellarGroupAppkeys fail", e)
        None
    }
  }
}
