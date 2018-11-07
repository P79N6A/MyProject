package com.sankuai.octo.mnsc.utils


import com.meituan.mtrace.Endpoint
import com.sankuai.inf.octo.mns.util.ProcessInfoUtil
import org.slf4j.{Logger, LoggerFactory}

import scala.collection.JavaConverters._

object common {
  private final val LOG: Logger = LoggerFactory.getLogger(common.getClass)

  def toList(enum: Enumeration) = enum.values.map {
    x => Map("name" -> x.toString, "value" -> x.id).asJava
  }.toList.sortBy(_.get("value").asInstanceOf[Int]).asJava

  def toPairList(enum: Enumeration) = enum.values.map(x => Pair(x.toString, x.id)).toList.asJava

  def toStringList(enum: Enumeration) = enum.values.map(x => '"' + x.toString + ',' + x.id + '"').toList.asJava

  def toMap(enum: Enumeration) = enum.values.map(x => (x.toString -> x.id)).toMap.asJava

  def toMapById(enum: Enumeration) = enum.values.map(x => (x.id -> x.toString)).toMap.asJava

  def toMap(cc: AnyRef) = {
    (Map[String, Any]() /: cc.getClass.getDeclaredFields) {
      (a, f) =>
        f.setAccessible(true)
        a + (f.getName -> f.get(cc))
    }.toMap
  }

  def toJavaMap(cc: AnyRef) = {
    toMap(cc).asJava
  }

  def notEmpty(s: String) = s != null && !s.trim.isEmpty

  def notNull(s: String) = s != null

  def localPoint() = {
    val ip = ProcessInfoUtil.getLocalIpV4
    new Endpoint("com.sankuai.inf.msgp", ip, 0)
  }

}
