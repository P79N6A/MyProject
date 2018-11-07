package com.sankuai.octo.msgp.serivce.data

import java.util.Calendar

import com.sankuai.octo.msgp.serivce.data.DataQuery.{Point, ResponseTag}
import org.joda.time.format.DateTimeFormat
import org.slf4j.{Logger, LoggerFactory}
import play.api.libs.json.Json

/**
  * Created by zava on 15/12/21.
  */
object DataCompare {
  private val LOG: Logger = LoggerFactory.getLogger(DataCompare.getClass)

  case class CPoint(x: String, y: Option[Double])

  case class SimpleDataRecord(tags: ResponseTag, count: List[CPoint], tp50: List[CPoint], avgQps: Double)

  //dCount 差值
  case class DataCompare4DataQuery(tags: ResponseTag, qps: List[CPoint], compare_qps: List[CPoint], tp50: List[CPoint], compare_tp50: List[CPoint], dQps: Double)

  case class CompareData(tags: ResponseTag, qps: List[Point], tp50: List[Point])

  implicit val pointReads = Json.reads[Point]
  implicit val pointWrites = Json.writes[Point]

  implicit val compareDataReads = Json.reads[CompareData]
  implicit val compareDataWrites = Json.writes[CompareData]

  /**
    * 0: 根据ctype 获取 对比的时间
    * 同比: 和上一周的 同一天对比
    * 环比: 和前一天对比
    * 1:获取 falcon的数据
    * 2:按照 小时 并聚合
    * 3:按照 count 排序后输出
    */
  def dailyCompare(appkey: String, start: String, end: String, env: String, unit: String, source: String, group: String,
                   spanname: String, localhost: String, remoteAppkey: String, remoteHost: String, ctype: Int) = {
    val formatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss")
    val startTime = formatter.parseDateTime(start)
    val endTime = formatter.parseDateTime(end)

    val ctypeMillis = if (ctype == 0) {
      86400
    } else {
      86400 * 7
    }
    val yesterdayStartTimeSecond = (startTime.getMillis / 1000 - ctypeMillis).toInt
    val yesterdayEndTimeSecond = (endTime.getMillis / 1000 - ctypeMillis).toInt

    val currentRecordOpt = DataQuery.getDataRecord(appkey, (startTime.getMillis / 1000).toInt, (endTime.getMillis / 1000).toInt, null, source, null, env, unit, group, spanname, localhost, remoteAppkey, remoteHost, "hbase")
    val yesterdayRecordOpt = DataQuery.getDataRecord(appkey, yesterdayStartTimeSecond, yesterdayEndTimeSecond, null, source, null, env, unit, group, spanname, localhost, remoteAppkey, remoteHost, "hbase")

    val currentRecord = currentRecordOpt.getOrElse(List())
    val yesterdayRecord = yesterdayRecordOpt.getOrElse(List())

    val dayMap = hbaseData2SimpleData(unit, currentRecord)
    val compareDayMap = hbaseData2SimpleData(unit, yesterdayRecord)

    compareSimpleData(dayMap, compareDayMap)
  }


  private def hbaseData2SimpleData(unit: String, records: List[DataQuery.DataRecord]) = {
    if (records.isEmpty) {
      Map[ResponseTag, SimpleDataRecord]()
    } else {
      val compareDataList = records.map {
        x =>
          CompareData(x.tags, x.qps, x.tp50)
      }
      val calendar = Calendar.getInstance()
      compareDataList.map {
        x =>
          val avgQps = x.qps.map(_.y.getOrElse(0.0)).sum / x.qps.length
          val qps = x.qps.map {
            q =>
              calendar.setTimeInMillis(q.ts.get * 1000l)
              if (unit.equals("Minute")) {
                CPoint(s"${q.ts.getOrElse(0)}", q.y)
              } else {
                CPoint(s"${calendar.get(Calendar.HOUR_OF_DAY)}", q.y)
              }
          }
          val tp50 = x.tp50.map {
            tp =>
              calendar.setTimeInMillis(tp.ts.get * 1000l)
              if (unit.equals("Minute")) {
                CPoint(s"${tp.ts.getOrElse(0)}", tp.y)
              } else {
                CPoint(s"${calendar.get(Calendar.HOUR_OF_DAY)}", tp.y)
              }
          }
          val simpleDataRecord = SimpleDataRecord(x.tags, qps.sortBy(_.x.toInt), tp50.sortBy(_.x.toInt), avgQps)
          x.tags -> simpleDataRecord
      }.toMap
    }
  }

  private def compareSimpleData(dayMap: Map[ResponseTag, SimpleDataRecord], compareDayMap: Map[ResponseTag, SimpleDataRecord]) = {
    val data = dayMap.map { x =>
      val tags = x._1
      val cSimpleData = compareDayMap.getOrElse(tags, SimpleDataRecord(tags, List(), List(), 0))
      val simpleData = x._2
      DataCompare4DataQuery(tags, simpleData.count, cSimpleData.count, simpleData.tp50, cSimpleData.tp50,
        math.abs(simpleData.avgQps - cSimpleData.avgQps))
    }
    val list = data.toList
    list.sortBy(-_.dQps)
  }
}
