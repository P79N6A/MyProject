package com.sankuai.octo.query.model

object HistoryData {

  // 绘图需要的点坐标
  case class Point(x: Option[String], y: Option[Double], ts: Option[Int])

  case class ResponseTag(protocolType: String, role: String, dataType: String, env: String, unit: String, group: String, spanname: Option[String], localhost: Option[String], remoteApp: Option[String], remoteHost: Option[String], infraName: Option[String])

  case class DataRecord(appkey: String, tags: ResponseTag, count: List[Point], successCount: List[Point] = List(), exceptionCount: List[Point] = List(), timeoutCount: List[Point] = List(), dropCount: List[Point] = List(),
                        HTTP2XXCount: List[Point] = List(), HTTP3XXCount: List[Point] = List(), HTTP4XXCount: List[Point] = List(), HTTP5XXCount: List[Point] = List(),
                        qps: List[Point], tp50: List[Point], tp90: List[Point], tp99: List[Point], dropQps: List[Point])

}
