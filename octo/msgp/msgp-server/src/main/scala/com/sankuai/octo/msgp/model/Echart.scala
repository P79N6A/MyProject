package com.sankuai.octo.msgp.model

import play.api.libs.json.Json

object Echart {

  case class PieSeriesData(name: String, value: Int)
  case class Pie(legend: List[String], series: List[PieSeriesData])

  implicit val pieSeriesDataReads = Json.reads[PieSeriesData]
  implicit val pieSeriesDataWrites = Json.writes[PieSeriesData]
  implicit val pieReads = Json.reads[Pie]
  implicit val pieWrites = Json.writes[Pie]


}
