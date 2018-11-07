package com.sankuai.octo.msgp.serivce.other

import java.util.concurrent.TimeUnit
import java.util.{List => JList}

import com.google.common.cache.{CacheBuilder, CacheLoader}
import com.meituan.mtrace.{Endpoint, Tracer}
import com.sankuai.msgp.common.config.MsgpConfig
import com.sankuai.msgp.common.service.org.{OpsService, OrgSerivce}
import com.sankuai.msgp.common.utils.helper.CommonHelper
import com.sankuai.octo.msgp.model.Perf._
import com.sankuai.octo.msgp.serivce.data.DataQuery
import com.sankuai.octo.msgp.serivce.graph.ServiceModel.AppCall
import com.sankuai.octo.statistic.util.ExecutionContextFactory
import dispatch._
import org.joda.time.DateTime
import org.slf4j.{Logger, LoggerFactory}
import play.api.libs.json.{JsArray, Json}

import scala.collection.JavaConverters._
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, duration}

object PerfApi {
  val LOG: Logger = LoggerFactory.getLogger(OrgSerivce.getClass)

  private implicit val ec = ExecutionContextFactory.build(Runtime.getRuntime.availableProcessors)

  val host = {
    if (CommonHelper.isOffline && "true" != System.getProperty("test")) {
      "http://10.4.233.218:8087"
    } else {
      "http://performance.sankuai.com"
    }
  }

  def apiQuery = host + "/api/query?access_token=5385cd607707a16953441ded&"

  def metricsQuery = host + "/api/metrics"

  def token = "access_token=5385cd607707a16953441ded"

  val format = "yyyy/MM/dd-HH:mm:ss"

  implicit val timeout = Duration.create(50L, duration.SECONDS)

  def syncHttp(request: String, span: String): String = {
    val tracer = Tracer.getClientTracer
    tracer.startNewSpan(span, CommonHelper.localPoint())
    tracer.setClientSent(new Endpoint("perf", "", 80))
    try {
      val feature = Http(url(request) > as.String)
      val content = Await.result(feature, timeout)
      content
    } catch {
      case e: Exception =>
        LOG.error(s"traceHttp $request", e)
        """{"status":200,"data":[],"metrics":[]}"""
    } finally {
      tracer.setClientReceived(0)
    }
  }

  val tagsCache = CacheBuilder.newBuilder().expireAfterWrite(30l, TimeUnit.MINUTES)
    .build(new CacheLoader[String, MetricsTags]() {
      def load(request: String): MetricsTags = {
        _tags(request)
      }
    })

  def queryTags(appkey: String, source: String = "server"): MetricsTags = {
    val key = appkey.toLowerCase
    val request = metricsQuery + s"/counters.$key.$source" + s"Count?$token"
    //_tags(request)

    tagsCache.get(request)
  }

  // search consumer of appkey(provider)
  def queryConsumer(appkey: String, source: String = "server"): JList[String] = {
    val key = appkey.toLowerCase
    val request = metricsQuery + s"/counters.$key.$source" + s"Count?$token"
    val content = syncHttp(request, "apiTags")
    try {
      Json.parse(content).\("tags").\("remoteApp").asInstanceOf[JsArray].value.map(_.validate[String].get).toList.asJava
    } catch {
      case e: Exception =>
        LOG.error(s"queryConsumer $request", e)
        List[String]().asJava
    }
  }

  def _tags(request: String): MetricsTags = {
    try {
      LOG.info(request)
      if (MsgpConfig.get("perf.tags", "true") == "true") {
        val content = syncHttp(request, "apiTags")

        val tracer = Tracer.getClientTracer
        tracer.startNewSpan("queryTags", CommonHelper.localPoint())
        tracer.setClientSent(new Endpoint("perf", "", 80))
        (Json.parse(content) \ "tags").validate[MetricsTags].fold({
          error =>
            tracer.setClientReceived(415)
            LOG.error(s"$error")
            MetricsTags(List(), List(), List(), List(), Some(List()))
        }, {
          tags =>
            tracer.setClientReceived(200)
            tags
        })
      } else {
        MetricsTags(List(), List(), List(), List(), Some(List()))
      }
    } catch {
      case e: Exception =>
        LOG.error(s"queryTags $request", e)
        MetricsTags(List(), List(), List(), List(), Some(List()))
    }
  }

  def consumerNode(appkey: String): Map[String, List[String]] = {
    val defaultNode = Map("ips" -> List(), "appkeys" -> List())
    try {
      val tags = queryTags(appkey)
      val tracer = Tracer.getClientTracer
      tracer.startNewSpan("consumerNode", CommonHelper.localPoint())
      tracer.setClientSent(new Endpoint("perf", "", 80))
      val node = Map("ips" -> tags.remoteHost.filter(x => x != "all" && x != "external").map(OpsService.ipToHost),
        "appkeys" -> tags.remoteApp.filter(x => x != "all" && x != "unknownService"))
      tracer.setClientReceived(200)
      node
    } catch {
      case e: Exception =>
        LOG.error(s"consumerNode $appkey", e)
        defaultNode
    }
  }

  private def getValue(list: List[DataQuery.Point]) = {
    list(0).y.getOrElse(0.0)
  }

  /**
    * 根据idc获取两个appkey之间的调用关系，fromAppkey->toAppkey
    *
    * @param idc  因为perf平台暂时不支持spanname=*,localhost=*,remoteApp=app 三个维度的查询，因此只能先使用全部机房的数据
    * @param unit default 1m
    */
  def getInvokeDescByIDC(fromAppkey: String, toAppkey: String, idc: String = "all", unit: String = "1m") = {
    val now = DateTime.now()
    val end = (now.minusMinutes(2).getMillis / 1000).toInt

    //TODO，按机房划分

    val recordsOpt = DataQuery.getDataRecord(fromAppkey, end, end, "thrift", "client",
      null, "prod", "minuter", "SpanRemoteApp", "*", "all", toAppkey, "all", "hbase")

    val records = recordsOpt.getOrElse(List())
    if (records.isEmpty) {
      List()
    } else {
      records.map { data =>
        AppCall(data.tags.spanname.getOrElse("unknown"), getValue(data.count).toLong, getValue(data.qps),
          getValue(data.tp50), getValue(data.tp90), getValue(data.tp90), getValue(data.tp99))
      }
    }
  }
}
