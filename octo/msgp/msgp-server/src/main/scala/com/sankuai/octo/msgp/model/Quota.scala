package com.sankuai.octo.msgp.model

import play.api.libs.json.Json

/**
 * Created by chenxi on 6/25/15.
 */

object Quota {
  case class JsonDegradeAction(
                                id: String,
                                env: Int,
                                providerAppkey: String,
                                consumerAppkey: String,
                                method: String,
                                degradeRatio: Double,
                                degradeStrategy: Int,
                                timestamp: Long,
                                degradeRedirect: Option[String],
                                degradeEnd: Int,
                                extend: String
                                )

  implicit val degradeReads = Json.reads[JsonDegradeAction]
  implicit val degradeWrites = Json.writes[JsonDegradeAction]
}
