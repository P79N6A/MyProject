package com.sankuai.octo.msgp.serivce.graph

import play.api.libs.json.Json

object ServiceModel {

  object Color extends Enumeration {
    type Color = Value
    val Green = Value(0, "green")
    val Yellow = Value(1, "yellow")
    val Orange = Value(2, "orange")
    val Red = Value(3, "red")

    def hostColor(load: Double) = {
      load match {
        case x if (x < 0.2) => Green.id
        case x if (x >= 0.2 && x < 0.4) => Yellow.id
        case x if (x >= 0.4 && x < 0.6) => Orange.id
        case x if (x >= 0.6) => Red.id
      }
    }

    def timeColor(time: Double) = {
      time match {
        case x if (x < 20) => Green.id
        case x if (x >= 20 && x < 50) => Yellow.id
        case x if (x >= 50 && x < 100) => Orange.id
        case x if (x >= 100) => Red.id
      }
    }

    def qpsCount(qps: Double) = {
      qps match {
        case x if (x < 10) => Green.id
        case x if (x >= 10 && x < 100) => Yellow.id
        case x if (x >= 100 && x < 1000) => Orange.id
        case x if (x >= 1000) => Red.id
      }
    }
  }

  case class AppCall(name: String, count: Long, qps: Double, upper50: Double, upper90: Double, upper95: Double, upper99: Double)

  case class AppNode(name: String, business: Int, level: Int, hosts: Map[String, Int], in: List[AppCall], out: List[AppCall], x: Int, y: Int)

  //hosts的key是idc
  case class AppNodeIDC(name:String,business: Int, level: Int, hosts:Map[String,Map[String, Int]] ,idcInMap:Map[String,List[AppCall]],idcOutMap:Map[String,List[AppCall]],x: Int, y: Int)


  implicit val appCallReads = Json.reads[AppCall]
  implicit val appCallWrites = Json.writes[AppCall]
  implicit val appReads = Json.reads[AppNode]
  implicit val appWrites = Json.writes[AppNode]

  implicit val appNodeIDCReads = Json.reads[AppNodeIDC]
  implicit val appNodeIDCWrites = Json.writes[AppNodeIDC]

  case class AppAxis(appkey: String, x: Double, y: Double)

  case class ServerGraph(graphId: Int, list: List[AppAxis])

  implicit val readsAppAxis = Json.reads[AppAxis]
  implicit val writesAppAxis = Json.writes[AppAxis]
  implicit val readsGraph = Json.reads[ServerGraph]
  implicit val writesGraph = Json.writes[ServerGraph]

  case class GraphData(nodes: List[AppNode], outNodes: List[AppNode], unknownNodes: List[AppNode], auth: Option[String] = Some("read"), business: Option[String] = Some("其他"))

  implicit val readsLevel = Json.reads[GraphData]
  implicit val writesLevel = Json.writes[GraphData]

  case class AppPerf(appkey: String, spanname: String, count: Long, qps: Double, tp50: Double, tp90: Double, tp95: Double, tp99: Double)

  implicit val readsAppPerf = Json.reads[AppPerf]
  implicit val writesAppPerf = Json.writes[AppPerf]
}
