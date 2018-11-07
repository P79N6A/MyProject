/*
package com.sankuai.octo.statistic.util

import com.sankuai.octo.statistic.metric.MetricManager
import com.sankuai.octo.statistic.model.{StatGroup, StatRange}
import scala.collection.JavaConverters._

object InstanceDumpHelper {

  def getSnapshot = {
    val keys = MetricManager.hourInstanceMap.values.asScala.groupBy(_.key.appKey).map {
      case (appKey, appKeyList) =>
        val ranges = appKeyList.groupBy(_.key.range).map {
          case (range, rangeList) =>
            val groups = rangeList.groupBy(_.key.group).map {
              case (group, groupList) => InstanceGroupInfo(group, groupList.map(_.key.tags))
            }
            InstanceRangeInfo(range, groups)
        }
        InstanceKeyInfo(appKey, ranges)
    }
    InstanceDump(keys)
  }

  /**
    *
    * @return 测试使用
    */
  def randomInstanceDump = {
    val ranges = StatRange.values().map { range =>
      val groups = StatGroup.values().map { group =>
        InstanceGroupInfo(group, Seq(Map("localhost" -> "host1")))
      }
      InstanceRangeInfo(range, groups)
    }
    val snapshot = InstanceKeyInfo("testAppkey", ranges)
    InstanceDump(Seq(snapshot))
  }

}


/**
  *
  * @param keys (appKey -> (StatRange -> (StatGroup -> instance tags)))
  */
case class InstanceDump(keys: Iterable[InstanceKeyInfo])

case class InstanceGroupInfo(group: StatGroup, tags: Iterable[Map[String, String]])

case class InstanceRangeInfo(range: StatRange, groups: Iterable[InstanceGroupInfo])

case class InstanceKeyInfo(appKey: String, ranges: Iterable[InstanceRangeInfo])
*/
