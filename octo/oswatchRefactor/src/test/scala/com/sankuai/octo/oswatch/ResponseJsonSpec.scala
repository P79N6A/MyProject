package com.sankuai.octo.oswatch

import com.sankuai.octo.oswatch.db.Tables.OswatchMonitorPolicyRow
import com.sankuai.octo.oswatch.model.ResponseJson
import com.sankuai.octo.oswatch.thrift.data.{EnvType, MonitorPolicy, ErrorCode}
import org.scalatest.{Matchers, FlatSpec}
import play.api.libs.json.Json

/**
 * Created by dreamblossom on 15/10/9.
 */
class ResponseJsonSpec extends FlatSpec with Matchers{
  println(Json.toJson(new ResponseJson(ErrorCode.ACTIVE, 6,30)).toString())
}
