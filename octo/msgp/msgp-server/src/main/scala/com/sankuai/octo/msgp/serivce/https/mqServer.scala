package com.sankuai.octo.msgp.serivce.https

import com.sankuai.msgp.common.utils.helper.{CommonHelper, HttpHelper}
import dispatch.url
import org.slf4j.LoggerFactory
import play.api.libs.json.Json

object mqServer {
  private val LOGGER = LoggerFactory.getLogger(this.getClass)

  private val mqHost = if (CommonHelper.isOffline) {
    "http://mq.test.sankuai.com"
  } else {
    "http://api.mtmq.vip.sankuai.com"
  }

  case class MqAppkey(appkey: String)

  implicit val reads = Json.reads[MqAppkey]
  implicit val writes = Json.writes[MqAppkey]

  def mqAppkeys() = {
    try {
        val request = s"$mqHost/api/appkeylist"
      val msg = HttpHelper.execute(url(request).POST, text =>
        (Json.parse(text) \ "data").validate[List[MqAppkey]].asOpt)
      msg
    } catch {
      case e: Exception => LOGGER.error(s"get mqAppkeys fail", e)
        None
    }
  }

  def main(args: Array[String]) {
    println(mqAppkeys())
  }
}
