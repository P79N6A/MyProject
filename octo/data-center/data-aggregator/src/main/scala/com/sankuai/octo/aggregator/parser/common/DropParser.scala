package com.sankuai.octo.aggregator.parser.common

import com.meituan.mtrace.thrift.model.StatusCode
import com.sankuai.octo.aggregator.parser.{MetricParser, MetricStruct}
import com.sankuai.octo.aggregator.thrift.model.DropRequestList
import com.sankuai.octo.statistic.helper.Serializer
import com.sankuai.octo.statistic.model.{MetricData, MetricKey, PerfProtocolType, StatSource}
import org.slf4j.LoggerFactory

import scala.collection.JavaConversions._

object DropParser {
  private val logger = LoggerFactory.getLogger(DropParser.getClass)

  def processDrop(logContent: Array[Byte]): Unit = {
    val dropRequestList = Serializer.toObject(logContent, classOf[DropRequestList])
    logger.debug("receive drop requests : {}", dropRequestList.getRequestsSize)
    putDropMetric(dropRequestList)
  }

  private def putDropMetric(dropRequestList: DropRequestList): Unit = {
    dropRequestList.getRequests.foreach(entry => {
      logger.debug(s"appkey:${entry.appkey},spanname:${entry.getSpanname},host:${entry.getHost}," +
        s"getRemoteAppkey:${entry.getRemoteAppkey}")
      /** entry.getAppkey 是client appKey
      entry.getRemoteAppkey 是Server appKey */
      val key = new MetricKey(entry.getRemoteAppkey, entry.getSpanname, entry.getHost, entry.getAppkey
        , "", StatSource.ServerDrop, PerfProtocolType.THRIFT, PerfProtocolType.THRIFT.toString)

      (1 to entry.getCount.toInt).foreach { _ =>
        val data = new MetricData(entry.getStart, 1, 0, StatusCode.DROP)
        MetricParser.put(MetricStruct(key, data))
      }
    })
  }
}
