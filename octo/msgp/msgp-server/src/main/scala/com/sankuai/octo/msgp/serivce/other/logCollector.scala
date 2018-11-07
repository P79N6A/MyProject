package com.sankuai.octo.msgp.serivce.other

import com.sankuai.msgp.common.utils.helper.CommonHelper
import org.slf4j.{Logger, LoggerFactory}
import play.api.libs.json.{JsArray, Json}

import scala.collection.JavaConverters._


/**
 * Created by dreamblossom on 15/8/12.
 */
object logCollector {
  val LOG: Logger = LoggerFactory.getLogger(logCollector.getClass)
  val lcHost = {
    if (CommonHelper.isOffline) {
      "http://query.octo.test.sankuai.info"
    } else {
      "http://data.octo.vip.sankuai.com"
    }
  }

  def getSpannames(appkey: String, env: String = "prod") = {
    val request = lcHost + s"/api/queryProvider?" + s"provider=$appkey&env=$env"
    LOG.info("getSpannames request: " + request)
    val content = PerfApi.syncHttp(request, "apiTags")

    try {
      Json.parse(content).asInstanceOf[JsArray].value.map(_.validate[String].get).toList.asJava
    } catch {
      case e: Exception =>
        LOG.error(s"getSpannames $request", e)
        List[String]().asJava
    }
  }

  def queryConsumer(appkey: String, spanname: String = "all", env: String = "prod") = {
    val request = lcHost + s"/api/queryProviderSpan?" + s"provider=$appkey&spanname=$spanname&env=$env"
    val content = PerfApi.syncHttp(request, "apiTags")
    try {
      Json.parse(content).asInstanceOf[JsArray].value.map(_.validate[String].get).toList
    } catch {
      case e: Exception =>
        LOG.error(s"queryConsumer $request", e)
        List[String]()
    }
  }

  def quotaConsumer(appkey: String, spanname: String = "all", env: String = "prod") = {
    val data = queryConsumer(appkey, spanname, env)
    (List("others") union data).asJava
  }
}
