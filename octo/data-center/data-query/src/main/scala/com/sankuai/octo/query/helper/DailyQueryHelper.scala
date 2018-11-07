package com.sankuai.octo.query.helper

import java.util.Collections

import com.sankuai.octo.statistic.helper.{DailyMetricHelper, TagHelper}
import com.sankuai.octo.statistic.model._
import com.sankuai.octo.statistic.util.tair
import org.slf4j.LoggerFactory

import scala.collection.JavaConverters._

/**
  * Created by wujinwu on 16/1/8.
  */
object DailyQueryHelper {

  private val logger = LoggerFactory.getLogger(this.getClass)

  def dailyMetrics(appkey: String, ts: Int, env: String, source: String): java.util.List[StatData] = {
    // tags
    val statSource = if(source == "client") {
      StatSource.Client
    } else {
      StatSource.Server
    }
    val tagKey = TagHelper.getTagKey(appkey, ts, env, statSource)
    TagHelper.getTagBytesByKey(tagKey) match {
      case Some(tagsBytes) =>
        val tag = TagHelper.asTag(tagsBytes)
        logger.debug("spannames:{}", tag.spannames)
        val statDataList = tag.spannames.flatMap {
          spanName =>
            val key = DailyMetricHelper.dailyMetricTairKey(appkey, spanName, ts, env, source)
            logger.debug("key:{}", key)
            val value = tair.getValue(key)
            value.map(statBytes => {
              val stat = DailyMetricHelper.asDailyStat(statBytes)
              logger.debug("stat:{}", stat)
              stat
            })
        }
        logger.debug("statData:{}", statDataList)

        /** 根据调用count倒序排列 */
        statDataList.toSeq.sortBy(-_.getCount).asJava
      case None =>
        Collections.emptyList()
    }
  }
}
