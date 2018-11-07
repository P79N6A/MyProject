package com.sankuai.octo.msgp.serivce.data

import com.sankuai.octo.statistic.model.{DataRecord, DataRecordMerged, Point, ResponseTag}

import scala.collection.JavaConverters._

/**
  * Created by yves on 17/6/21.
  *
  * 暂时做一下转换, 原因是scala case class使用的太多
  * 需要逐步替换为java pojo
  */
object QueryHelper {

  private val fourDecimalFormatter = "%.4f"

  def toScalaDataRecordMerged(list: java.util.List[DataRecordMerged]) = {
    list.asScala.map {
      raw =>

        val failCount = raw.getExceptionCount + raw.getTimeoutCount + raw.getDropCount +
          raw.getHttp4XXCount + raw.getHttp5XXCount
        val failCountPer = if (raw.getCount != 0) {
          s"${fourDecimalFormatter.format(failCount.toDouble / raw.getCount * 100)}%"
        } else {
          "0.0000%"
        }

        DataQuery.DataRecordMerged(
          raw.getAppkey,
          toScalaTag(raw.getTags),
          raw.getCount.toDouble,
          raw.getSuccessCount.toDouble,
          raw.getExceptionCount.toDouble,
          raw.getTimeoutCount.toDouble,
          raw.getDropCount.toDouble,
          raw.getQps.toInt,
          raw.getTp50.toInt,
          raw.getTp90.toInt,
          raw.getTp99.toInt,
          raw.getTp999.toInt,
          raw.getTp9999.toInt,
          raw.getTp99999.toInt,
          raw.getTp999999.toInt,
          raw.getDropQps.toInt,
          failCount,
          failCountPer,
          raw.getHttp2XXCount.toDouble,
          raw.getHttp3XXCount.toDouble,
          raw.getHttp4XXCount.toDouble,
          raw.getHttp5XXCount.toDouble
        )
    }.toList
  }


  def toScalaDataRecord(list: java.util.List[DataRecord]) = {
    list.asScala.map {
      raw =>
        DataQuery.DataRecord(raw.getAppkey,
          toScalaTag(raw.getTags),
          toScalaPointList(raw.getCount),
          toScalaPointList(raw.getSuccessCount),
          toScalaPointList(raw.getExceptionCount),
          toScalaPointList(raw.getTimeoutCount),
          toScalaPointList(raw.getDropCount),
          toScalaPointList(raw.getQps),
          toScalaPointList(raw.getTp50),
          toScalaPointList(raw.getTp90),
          toScalaPointList(raw.getTp99),
          toScalaPointList(raw.getTp999),
          toScalaPointList(raw.getTp9999),
          toScalaPointList(raw.getTp99999),
          toScalaPointList(raw.getTp999999),
          toScalaPointList(raw.getDropQps),
          toScalaPointList(raw.getHttp2XXCount),
          toScalaPointList(raw.getHttp3XXCount),
          toScalaPointList(raw.getHttp4XXCount),
          toScalaPointList(raw.getHttp5XXCount))
    }.toList
  }

  def toScalaTag(tag: ResponseTag) = {
    DataQuery.ResponseTag(Some(tag.getProtocolType), Some(tag.getRole), Some(tag.getEnv),
      Some(tag.getGroup), Some(tag.getSpanname), Some(tag.getLocalhost),
      Some(tag.getRemoteApp), Some(tag.getRemoteHost))
  }

  def toScalaPointList(points: java.util.List[Point]) = {
    points.asScala.map(x => toScalaPoint(x)).toList
  }

  def toScalaPoint(point: Point) = {
    DataQuery.Point(Some(point.getX), Some(point.getY), Some(point.getTs))
  }

}
