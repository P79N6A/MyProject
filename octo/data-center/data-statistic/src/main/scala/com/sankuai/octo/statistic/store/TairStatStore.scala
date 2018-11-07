/*
package com.sankuai.octo.statistic.store

import com.meituan.service.mobile.mtthrift.util.ProcessInfoUtil
import com.sankuai.octo.statistic.constant.Constants
import com.sankuai.octo.statistic.helper.api
import com.sankuai.octo.statistic.model._
import com.sankuai.octo.statistic.util.tair
import org.apache.thrift.protocol.TBinaryProtocol
import org.apache.thrift.{TDeserializer, TSerializer}
import org.slf4j.LoggerFactory

abstract class TairStatStore extends AbstractStatStore {
  private val logger = LoggerFactory.getLogger(classOf[TairStatStore])
  val serializer = new TSerializer(new TBinaryProtocol.Factory())
  val deserializer = new TDeserializer(new TBinaryProtocol.Factory())

  def getStat(appkey: String, ts: Int, postfix: String,
              env: StatEnv, source: StatSource,
              range: StatRange, group: StatGroup): Option[StatData] = {
    val key = s"stat|$env|$source|$range|$group|$appkey|$ts|$postfix"
    val data = tair.getValue(key).map {
      bytes =>
        api.bytesToObject(bytes, classOf[StatData])
    }
    logger.debug(s"getStat $key $data")
    data
  }

  def updateStat(data: StatData) {
    if (data.getEnv == null || data.getSource == null || data.getRange == null || data.getGroup == null ||
      data.getAppkey == null || data.getTags == null || data.getTags.isEmpty) {
      logger.error(s"illegal stat data $data")
    }
    val prefix = s"stat|${data.getEnv}|${data.getSource}|${data.getRange}|${data.getGroup}|${data.getAppkey}|${data.getTs}"
    val postfix = data.getGroup match {
      case StatGroup.Span => data.getTags.get(Constants.SPAN_NAME)
      case StatGroup.SpanLocalHost => s"${data.getTags.get(Constants.SPAN_NAME)}|${data.getTags.get(Constants.LOCAL_HOST)}"
      case StatGroup.SpanRemoteApp => s"${data.getTags.get(Constants.SPAN_NAME)}|${data.getTags.get(Constants.REMOTE_APPKEY)}"
      case StatGroup.SpanRemoteHost => s"${data.getTags.get(Constants.SPAN_NAME)}|${data.getTags.get(Constants.REMOTE_HOST)}"
      case StatGroup.LocalHostRemoteHost => s"${data.getTags.get(Constants.LOCAL_HOST)}|${data.getTags.get(Constants.REMOTE_HOST)}"
      case StatGroup.LocalHostRemoteApp => s"${data.getTags.get(Constants.LOCAL_HOST)}|${data.getTags.get(Constants.REMOTE_APPKEY)}"
      case StatGroup.RemoteAppRemoteHost => s"${data.getTags.get(Constants.REMOTE_APPKEY)}|${data.getTags.get(Constants.REMOTE_HOST)}"
    }
    val now = System.currentTimeMillis()
    data.setUpdateTime(now)
    data.setUpdateFrom(ProcessInfoUtil.getLocalIpV4FromLocalCache)
    logger.debug(s"updateStat $prefix $postfix $data")
    tair.put(s"$prefix|$postfix", data)
  }
}
*/
