package com.sankuai.octo.msgp.model

import play.api.libs.json._

/**
 * Created by dreamblossom on 15/9/30.
 */
case class LogCollectorResult(providerAppKey: String, consumer2QpsList: List[ConsumerResult], spanName: String)

object LogCollectorResult {
  implicit val logCollectorResultReader = Json.reads[LogCollectorResult]
  implicit val logCollectorResultWriter = Json.writes[LogCollectorResult]
}

