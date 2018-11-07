package com.sankuai.octo.msgp.model

import play.api.libs.json.Json

/**
 * Created by dreamblossom on 15/9/30.
 */
case class ConsumerResult(consumerAppKey: String, qpsAvg: Double)

object ConsumerResult {
  implicit val consumerResultReader = Json.reads[ConsumerResult]
  implicit val consumerResultWriter = Json.writes[ConsumerResult]
}

