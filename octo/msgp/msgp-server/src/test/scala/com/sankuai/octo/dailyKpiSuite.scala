package com.sankuai.octo

import com.sankuai.msgp.common.utils.helper.JsonHelper
import com.sankuai.octo.msgp.serivce.data.DataQuery
import org.joda.time.format.DateTimeFormat
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{BeforeAndAfter, FunSuite}

@RunWith(classOf[JUnitRunner])
class dailyKpiSuite extends FunSuite with BeforeAndAfter {
  /**
   * 获取天粒度数据
   */
  test("getDailyStat") {
    val appkey = "com.meituan.service.user"
    val env = "prod"
    val formatter = DateTimeFormat.forPattern("yyyy-MM-dd")
    val dateTime = formatter.parseLocalDate("2016-12-27")
    val data = DataQuery.getDailyStatisticFormatted(appkey, env, dateTime.toDateTimeAtStartOfDay,"client")
    println(JsonHelper.dataJson(data.head))
  }
}
