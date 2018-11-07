package com.sankuai.octo.oswatch.service

import com.sankuai.octo.oswatch.db.Tables.OswatchMonitorPolicyRow
import com.sankuai.octo.oswatch.utils._
import com.sankuai.octo.oswatch.model._
import com.sankuai.octo.oswatch.thrift.data._
import org.slf4j._
import play.api.libs.json._

import dispatch._, Defaults._
import play.api.libs.json.Json

import scala.concurrent.{Await, duration}
import scala.concurrent.duration.Duration
import scalaj.http._


/**
 * Created by dreamblossom on 15/9/30.
 */
object HttpService {
  val LOG: Logger = LoggerFactory.getLogger(HttpService.getClass)
  implicit val timeout = Duration.create(100, duration.SECONDS)

  def getAliveNode(monitorPolicy: OswatchMonitorPolicyRow ) = {
   // val queryURL = Common.msgpURL + "/api/zk/service/alive" + "?env=" + monitorPolicy.env + "&appkey=" + monitorPolicy.appkey)
  //  val feature = Http(queryURL > as.String)
    println("getAliveNode is called ")
    val queryURL = Common.msgpURL + "/api/zk/providerForOverload/aliveNode"
    val response: HttpResponse[String] = scalaj.http.Http(queryURL).param("env",monitorPolicy.env.toString).param("appkey",monitorPolicy.appkey).param("providerCountSwitch",monitorPolicy.providerCountSwitch.toString).asString
    (Json.parse(response.body) \ "count").as[Int]
  }

  def tellRegister(responseUrl:String,monitorPolicyId: Long ,monitorTypeValue:Double) = {
    println("tellRegister is called ")
    val httpRequest = url(responseUrl).addHeader("Content-Type", "application/json;charset=utf-8").POST << new ResponseJson(ErrorCode.ACTIVE, monitorPolicyId, monitorTypeValue).toString
    val feature = dispatch.Http(httpRequest > as.String)
    LOG.info("response:"+ Await.result(feature, timeout))
//    val response=Http(responseUrl).postData(Json.toJson(new ResponseJson(ErrorCode.ACTIVE, monitorPolicyId, monitorTypeValue)).toString)
//      .header("Content-Type", "application/json")
//      .header("Charset", "UTF-8")
//      .asString
//     LOG.info("response:"+response)
  }
}
