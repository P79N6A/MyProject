package com.sankuai.octo.statistic.util

import java.util.concurrent.atomic.AtomicInteger

import com.sankuai.octo.statistic.constant.Constants
import com.sankuai.octo.statistic.domain.StatTag
import com.sankuai.octo.statistic.helper.api
import com.sankuai.octo.statistic.model.{StatData, StatGroup, StatRange}
import dispatch._

import scala.collection.immutable.TreeMap

object Falcon {
  private val falconHost = "http://transfer.falcon.vip.sankuai.com:6060/api/push"
  private val falconDevHost = "http://10.5.233.113:6060/api/push"
  private val writeCounter = new AtomicInteger(0)

  private implicit lazy val ec = ExecutionContextFactory.build()

  val req = if (common.isOffline) {
    url(falconDevHost)
  } else {
    url(falconHost)
  }

  case class FalconData(endpoint: String,
                        metric: String,
                        timestamp: Long,
                        value: Double,
                        tags: String = "",
                        step: Int = 60,
                        counterType: String = "GAUGE")

  def send(data: Iterable[FalconData]) {
    writeCounter.incrementAndGet()
    val postReq = req.POST << api.jsonStr(data)
    Http(postReq > as.String)
  }

  def statToFalconData(stat: StatData) = {
    // 先强制只允许给falcon上报分钟粒度数据
    require(StatRange.Minute == stat.getRange)
    require(stat.getTags != null)

    //防止写入endpoint为all的错误信息
    if (stat.getGroup == StatGroup.SpanLocalHost && stat.getTags.localHost.equalsIgnoreCase(Constants.ALL)) {
      Seq()
    } else {
      //  endpoint包含标识一个指标的信息
      val endpoint = stat.getGroup match {
        case StatGroup.SpanLocalHost => stat.getTags.localHost
        case StatGroup.Span | StatGroup.SpanRemoteApp | StatGroup.SpanRemoteHost | StatGroup.LocalHostRemoteHost
             | StatGroup.LocalHostRemoteApp | StatGroup.RemoteAppRemoteHost => stat.getAppkey
      }
      //  根据相应的查询维度,将endpoint中以外的信息包含进后缀中,便以前端处理
      val metricSuffix = stat.getGroup match {
        case StatGroup.Span => stat.getTags.spanname
        case StatGroup.SpanLocalHost => s"${stat.getAppkey}_${stat.getTags.spanname}"
        case StatGroup.SpanRemoteApp => stat.getTags.remoteAppKey + "_" + stat.getTags.spanname
        case StatGroup.SpanRemoteHost => stat.getTags.remoteHost + "_" + stat.getTags.spanname
        case StatGroup.LocalHostRemoteHost => s"${stat.getTags.remoteHost}_${stat.getTags.localHost}"
        case StatGroup.LocalHostRemoteApp => s"${stat.getTags.remoteAppKey}_${stat.getTags.localHost}"
        case StatGroup.RemoteAppRemoteHost => s"${stat.getTags.remoteAppKey}_${stat.getTags.remoteHost}"
      }
      val metricPrefix = s"${stat.getEnv}_${stat.getSource}_$metricSuffix"

      val tagsStr = getTagsStr(stat.getTags)

      val countMetric = metricPrefix + "_count"
      val successCountMetric = metricPrefix + "_successCount"
      val exceptionCountMetric = metricPrefix + "_exceptionCount"
      val timeoutCountMetric = metricPrefix + "_timeoutCount"
      val dropCountMetric = metricPrefix + "_dropCount"
      val qpsMetric = metricPrefix + "_qps"
      val meanMetric = metricPrefix + "_mean"
      val tp50Metric = metricPrefix + "_tp50"
      val tp90Metric = metricPrefix + "_tp90"
      val tp95Metric = metricPrefix + "_tp95"
      val tp99Metric = metricPrefix + "_tp99"

      val count = FalconData(endpoint, countMetric, stat.getTs, stat.getCount, tagsStr)
      val successCount = FalconData(endpoint, successCountMetric, stat.getTs, stat.getSuccessCount, tagsStr)
      val exceptionCount = FalconData(endpoint, exceptionCountMetric, stat.getTs, stat.getExceptionCount, tagsStr)
      val timeoutCount = FalconData(endpoint, timeoutCountMetric, stat.getTs, stat.getTimeoutCount, tagsStr)
      val dropCount = FalconData(endpoint, dropCountMetric, stat.getTs, stat.getDropCount, tagsStr)
      val qps = FalconData(endpoint, qpsMetric, stat.getTs, stat.getQps, tagsStr)
      val mean = FalconData(endpoint, meanMetric, stat.getTs, stat.getCostMean, tagsStr)
      val tp50 = FalconData(endpoint, tp50Metric, stat.getTs, stat.getCost50, tagsStr)
      val tp90 = FalconData(endpoint, tp90Metric, stat.getTs, stat.getCost90, tagsStr)
      val tp95 = FalconData(endpoint, tp95Metric, stat.getTs, stat.getCost95, tagsStr)
      val tp99 = FalconData(endpoint, tp99Metric, stat.getTs, stat.getCost99, tagsStr)
      Seq(count, successCount, exceptionCount, timeoutCount, dropCount, qps, mean, tp50, tp90, tp95, tp99)
    }
  }

  def getAndResetWriteCount() = {
    val count = writeCounter.get()
    writeCounter.set(0)
    count
  }

  private def getTagsStr(tag: StatTag) = {
    val map = Map(Constants.SPAN_NAME -> tag.spanname,
      Constants.LOCAL_HOST -> tag.localHost,
      Constants.REMOTE_HOST -> tag.remoteHost,
      Constants.REMOTE_APPKEY -> tag.remoteAppKey)
    TreeMap(map.toSeq: _*).map(x => s"${x._1}=${x._2}").mkString(",")
  }
}
