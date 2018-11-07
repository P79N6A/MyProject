package com.sankuai.octo.msgp.model

import play.api.libs.json.Json

import scala.collection.JavaConverters._
import scala.collection.mutable.MutableList

//TODO: 该类应被废弃， 其中仅定义了三个model，应做迁移合并
object Perf {

  case class MetricsTags(spanname: List[String], localhost: List[String], remoteApp: List[String], remoteHost: List[String], status: Option[List[String]]) {
    def spannameList: java.util.List[String] = ("*" +: "all" +: spanname.filter(_ != "all").sortWith(_.toLowerCase < _.toLowerCase)).asJava

    def localHostList: java.util.List[String] = ("*" +: "all" +: localhost.filter(_ != "all").sortWith(_.toLowerCase < _.toLowerCase)).asJava

    def remoteAppList: java.util.List[String] = ("*" +: "all" +: remoteApp.filter(_ != "all").sortWith(_.toLowerCase < _.toLowerCase)).asJava

    def remoteHostList: java.util.List[String] = ("*" +: "all" +: remoteHost.filter(_ != "all").sortWith(_.toLowerCase < _.toLowerCase)).asJava
  }

  implicit val metricsTagsReads = Json.reads[MetricsTags]
  implicit val metricsTagsWrites = Json.writes[MetricsTags]

  case class Consumer(ip: String, host: String, tag: String, appkey: String, time: String, count: Double)

  case class ConsumerOutline(appList: List[String], appCount: List[Double], hostList: List[String], hostCount: List[Double])

}
