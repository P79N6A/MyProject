package com.sankuai.octo.oswatch.service

import com.sankuai.octo.oswatch.utils.{Common, MTConfig}
import play.api.libs.json.Json

import scala.collection.mutable.{Map => MutableMap}
import scalaj.http.Http
import com.typesafe.scalalogging.LazyLogging

/**
 * Created by xintao on 15/11/10.
 */
object FalconService extends LazyLogging {
  val cpuCounterStr = "docker.cpu.percent/type=docker_container"
  val memCounterStr = "docker.mem.percent/type=docker_container"
  val netCounterStr = "docker.net.rx_bytes/type=docker_container"

  case class EndpointCounter(endpoint: String, counter: String)

  implicit val endpointCounterRead = Json.reads[EndpointCounter]
  implicit val endpointCounterWrite = Json.writes[EndpointCounter]

  case class DataSeries(timestamp: Option[Int], value: Option[Double])

  implicit val dataSeriesRead = Json.reads[DataSeries]
  implicit val dataSeriesWrite = Json.writes[DataSeries]

  case class FalconHistoryDataResponse(endpoint: Option[String], counter: Option[String], Values: Option[List[DataSeries]], dstype: Option[String], step: Option[Int])

  implicit val falconHistoryDataResponseRead = Json.reads[FalconHistoryDataResponse]
  implicit val falconHistoryDataResponseWrite = Json.writes[FalconHistoryDataResponse]

  case class FalconLastDataResponse(endpoint: Option[String], counter: Option[String], value: Option[DataSeries])

  implicit val falconLastDataResponseRead = Json.reads[FalconLastDataResponse]
  implicit val falconLastDataResponseWrite = Json.writes[FalconLastDataResponse]

  case class ContainerInf(containerName: String, cpu: Double, mem: Double, net: Double)

  implicit val containerInfRead = Json.reads[ContainerInf]
  implicit val containerInfWrite = Json.writes[ContainerInf]

  def queryHistoryData(start: Long, end: Long, endpointCounters: List[EndpointCounter], cf: String = "AVERAGE") = {
    val falconHistoryParam = Json.toJson(
      Map(
        "start" -> Json.toJson(start),
        "end" -> Json.toJson(end),
        "cf" -> Json.toJson(cf),
        "endpoint_counters" -> Json.toJson(endpointCounters)) //.map(Json.toJson(_))
    )
    // println(falconHistoryParam.toString())
    val falconHistoryQueryURL = Common.falconURL + "/graph/history"
    val response = Http(falconHistoryQueryURL).postData(falconHistoryParam.toString()).header("Content-Type", "application/json").header("Charset", "UTF-8").execute()
    // Logger.info(response.body)
    Json.parse(response.body.toString).validate[List[FalconHistoryDataResponse]].fold({ error =>
      logger.error(s"FalconHistoryDataResponse parse failed $error")
      None
    }, {
      value =>
        //  println(value)
        Some(value)
    })
  }

  def queryLastData(endpointCounters: List[EndpointCounter]) = {
    //    println("endpointCounters:"+endpointCounters)
    val falconLastQueryURL = Common.falconURL + "/graph/last"
    val response = Http(falconLastQueryURL).postData(Json.toJson(endpointCounters).toString()).header("Content-Type", "application/json").header("Charset", "UTF-8").execute()
    // Logger.info(response.body.toString)
    Json.parse(response.body.toString).validate[List[FalconLastDataResponse]].fold({ error =>
      logger.error(s"FalconLastDataResponse parse failed $error")
      None
    }, {
      value =>
        //        println("queryLastData:"+value)
        Some(value)
    })
  }

  def getContainerInf(containerList: List[String]) = {
    //    println("containerList:"+ containerList)
    val endpointCounterList = containerList.foldLeft(List[EndpointCounter]()) { (ls, tmp) =>
      ls.::(EndpointCounter(tmp, cpuCounterStr))
        .::(EndpointCounter(tmp, memCounterStr))
        .::(EndpointCounter(tmp, netCounterStr))
    }

    val containerInfMap = MutableMap[String, ContainerInf]()

    val containerInfList = queryLastData(endpointCounterList) match {
      case Some(res) => res
      case None => List()
    }
    containerInfList.filter(_.endpoint.isDefined).foreach(endPointCounterInf =>
      if (containerInfMap.contains(endPointCounterInf.endpoint.get)) {
        containerInfMap.get(endPointCounterInf.endpoint.get) match {
          case Some(oldValue) =>
            val newValue = endPointCounterInf.counter.get match {
              case FalconService.cpuCounterStr =>
                ContainerInf(oldValue.containerName,
                  endPointCounterInf.value match {
                    case Some(dataseries) =>
                      dataseries.value.getOrElse(0.0)
                    case None =>
                      0.0
                  },
                  oldValue.mem,
                  oldValue.net)
              case FalconService.memCounterStr =>
                ContainerInf(oldValue.containerName,
                  oldValue.cpu,
                  endPointCounterInf.value match {
                    case Some(dataseries) =>
                      dataseries.value.getOrElse(0.0)
                    case None =>
                      0.0
                  },
                  oldValue.net)
              case FalconService.netCounterStr =>
                ContainerInf(oldValue.containerName,
                  oldValue.cpu,
                  oldValue.mem,
                  endPointCounterInf.value match {
                    case Some(dataseries) =>
                      dataseries.value.getOrElse(0.0)
                    case None =>
                      0.0
                  })
            }
            //  println("newValue:" + newValue)
            containerInfMap.update(endPointCounterInf.endpoint.get, newValue)
          case None => //无此情况
        }
      } else {
        val newValue = endPointCounterInf.counter.get match {
          case FalconService.cpuCounterStr =>
            ContainerInf(endPointCounterInf.endpoint.getOrElse(""),
              endPointCounterInf.value match {
                case Some(dataseries) =>
                  dataseries.value.getOrElse(0.0)
                case None =>
                  0.0
              },
              0.0,
              0.0)
          case FalconService.memCounterStr =>
            ContainerInf(endPointCounterInf.endpoint.getOrElse(""),
              0.0,
              endPointCounterInf.value match {
                case Some(dataseries) =>
                  dataseries.value.getOrElse(0.0)
                case None =>
                  0.0
              },
              0.0)
          case FalconService.netCounterStr =>
            ContainerInf(endPointCounterInf.endpoint.getOrElse(""),
              0.0,
              0.0,
              endPointCounterInf.value match {
                case Some(dataseries) =>
                  dataseries.value.getOrElse(0.0)
                case None =>
                  0.0
              })
        }
        //  println("newValue:" + newValue)
        containerInfMap += (endPointCounterInf.endpoint.getOrElse("") -> newValue)
      })
    //    println("containerInfMap:")
    //    println(containerInfMap)
    containerInfMap.toMap
  }

  //  def main(args: Array[String]) {
  //    val endPointList = List(
  //            EndpointCounter("yf-docker-test01", "docker.cpu.periods/dhost=yf-cloud-docker-host02,type=docker_container"),
  //            EndpointCounter("yf-docker-test01", "cpu.idle"),
  //            EndpointCounter("yf-docker-test01", "mem.memused"),
  //      EndpointCounter("set-zone0-com.sankuai.inf.hulk.test.provider-43-app", "cpu.idle"),
  //            EndpointCounter("set-zone0-com.sankuai.inf.hulk.test.provider-43-app", "docker.cpu.usage_in_kernelmode"),
  //      EndpointCounter("set-zone0-com.sankuai.inf.hulk.test.provider-43-app", "docker.cpu.usage_in_kernelmode/dhost=yf-docker-test03,type=docker_container"))
  //
  //    queryHistoryData(1447230600, 1447230840, endPointList, "AVERAGE")
  //    queryLastData(endPointList)
  //    val containerList = List("set-zone0-com.sankuai.inf.sg_agent-11-sg_agent_bin_dev","set-zone0-com.sankuai.inf.hulk.test.provider-44-app")
  //    print(getContainerInf(containerList))
  //  }
}
