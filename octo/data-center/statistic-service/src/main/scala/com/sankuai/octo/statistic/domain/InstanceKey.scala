package com.sankuai.octo.statistic.domain

import com.sankuai.octo.statistic.model._

/**
  *
  * 多个信息组合唯一标识
  *
  * @param appKey 应用key
  * @param env    环境
  * @param source 来源
  * @param range  范围
  * @param group  分组
  * @param tags   标签分组 {spanname:methodName,  SpanLocalHost :host1},与group相对应
  */
case class InstanceKey(appKey: String, ts: Int, env: StatEnv, source: StatSource, range: StatRange, group: StatGroup,
                       perfProtocolType: PerfProtocolType, tags: Map[String, String]) extends Serializable {
  private lazy val _hash = {
    var result: Int = 0
    result = if (appKey != null) appKey.hashCode else 0
    result = 31 * result + ts
    result = 31 * result + (if (env != null) env.hashCode else 0)
    result = 31 * result + (if (source != null) source.hashCode else 0)
    result = 31 * result + (if (range != null) range.hashCode else 0)
    result = 31 * result + (if (group != null) group.hashCode else 0)
    result = 31 * result + (if (perfProtocolType != null) perfProtocolType.hashCode else 0)
    result = 31 * result + (if (tags != null) tagsHash(tags) else 0)
    result
  }

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

  private def tagsHash(tags: Map[String, String]) = {
    var result: Int = 0
    tags.foreach {
      entry =>
        result = 31 * result + (if (entry._1 != null) entry._1.hashCode else 0)
        result = 31 * result + (if (entry._2 != null) entry._2.hashCode else 0)
    }
    result
  }
}

