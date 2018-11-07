package com.sankuai.octo.oswatch.model

import com.sankuai.octo.oswatch.db.Tables.OswatchMonitorPolicyRow
import com.sankuai.octo.oswatch.thrift.data.ErrorCode
import play.api.libs.json.Json

/**
 * Created by dreamblossom on 15/10/1.
 */
case class ResponseJson(errorCode: Int, oswatchId: Long, monitorTypeValue: Double) {
  def this(errorCode: ErrorCode, oswatchId: Long, monitorTypeValue: Double) = this(
    errorCode.getValue,
    oswatchId,
    monitorTypeValue)
}

object ResponseJson {
  implicit val JsonResponseReader = Json.reads[ResponseJson]
  implicit val JsonResponseWriter = Json.writes[ResponseJson]
}

