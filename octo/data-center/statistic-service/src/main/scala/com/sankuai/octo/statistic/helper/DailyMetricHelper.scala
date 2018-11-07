package com.sankuai.octo.statistic.helper

import com.sankuai.octo.statistic.model.{StatData, Tags}
import org.joda.time.{DateTime, DateTimeZone}


/**
 * Created by wujinwu on 15/9/8.
 */
object DailyMetricHelper {

  /**
   *
   * @param timestamp ms级别的时间戳
   * @return 当天的起始时间,s为单位;
   */
  def dayStart(timestamp: Long = System.currentTimeMillis()): Int = {
    val dateTime = new DateTime(timestamp, DateTimeZone.forID("Asia/Shanghai"))
    val startOfDay = dateTime.withTimeAtStartOfDay()
    (startOfDay.getMillis / 1000L).toInt
  }


  /**
   *
   * @param name format :appKey|spanName|timestamp of current Date start
   *             e.g com.sankuai.msgp|testMethod|1441468800
   * @param env 环境
   * @return 每天的metric查询的tair key
   */
  def dailyMetricTairKey(name: String, env: String) = s"$env|daily|stat|$name"

  /**
   *
   * @param appKey 应用的appKey
   * @param spanName 方法名
   * @param timeStamp 某一天起始时间的秒级时间戳
   * @param env 环境
   * @return  每天的metric查询的tair key
   */

  def dailyMetricTairKey(appKey: String, spanName: String, timeStamp: Long, env: String, source: String) =
    if(null == source || source == "server") {
      s"$env|daily|stat|$appKey|$spanName|$timeStamp"
    } else {
      s"$env|daily|stat|$appKey|$spanName|$timeStamp|${source.toLowerCase()}"
    }

  /**
   *
   * @param appKey 应用的appKey
   * @param timeStamp 某一天起始时间的秒级时间戳
   * @param env 环境
   * @return  每天的tags查询的tair key
   */
  def dailyTagsTairKey(appKey: String, timeStamp: Int = dayStart(), env: String) = {
    s"$env|daily|tags|$appKey|$timeStamp"
  }

  /**
   *
   * @param bytes 字节流
   * @return 转译的对象
   */
  def asDailyStat(bytes: Array[Byte]) = {
    api.bytesToObject(bytes, classOf[StatData])
  }

  /**
   *
   * @param bytes 字节流
   * @return 转译的对象
   */
  @deprecated
  def asDailyTags(bytes: Array[Byte]) = api.bytesToObject(bytes, classOf[Tags])

}
