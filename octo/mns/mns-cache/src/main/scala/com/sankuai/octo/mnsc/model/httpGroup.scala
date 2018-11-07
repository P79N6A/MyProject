package com.sankuai.octo.mnsc.model

import play.api.libs.json.Json

/**
 * Created by zhoufeng on 16/8/21.
 */
object httpGroup {
  case class httpGroupNode(ip: String, port: Int)

  implicit val groupNodeReads = Json.reads[httpGroupNode]
  implicit val groupNodeWirtes = Json.writes[httpGroupNode]

  case class httpGroup(group_name: String,
                   appkey: String,
                   desc: Option[String],
                   server: List[httpGroupNode])

  implicit val httpGroupReads = Json.reads[httpGroup]
  implicit val httpGroupWrites = Json.writes[httpGroup]
}
