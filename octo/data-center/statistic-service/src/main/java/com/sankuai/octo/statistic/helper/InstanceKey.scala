package com.sankuai.octo.statistic.helper

import com.sankuai.octo.statistic.model._

class InstanceKey(val appKey: String, val ts: Int, val env: StatEnv, val source: StatSource, val range: StatRange, val group: StatGroup,
                  val perfProtocolType: PerfProtocolType, val tags: Map[String, String]) {

  override def equals(other: Any): Boolean = other match {
    case that: InstanceKey =>
      (that canEqual this) &&
        appKey == that.appKey &&
        ts == that.ts &&
        env == that.env &&
        source == that.source &&
        range == that.range &&
        group == that.group &&
        perfProtocolType == that.perfProtocolType &&
        tags == that.tags
    case _ => false
  }

  def canEqual(other: Any): Boolean = other.isInstanceOf[InstanceKey]

  override def hashCode(): Int = {
    val state = Seq(appKey, ts, env, source, range, group, perfProtocolType, tags)
    state.map(_.hashCode()).foldLeft(0)((a, b) => 31 * a + b)
  }
}