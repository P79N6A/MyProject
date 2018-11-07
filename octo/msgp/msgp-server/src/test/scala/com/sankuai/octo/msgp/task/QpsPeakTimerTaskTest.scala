package com.sankuai.octo.msgp.task

import com.sankuai.msgp.common.utils.StringUtil
import com.sankuai.octo.mworth.util.DateTimeUtil
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import org.scalatest.FunSuite

/**
  * Created by wujinwu on 16/3/11.
  */
class QpsPeakTimerTaskTest extends FunSuite {


  test("calculate"){
    val job = new DailyQpsPeakJob()
    //    val day = getDate("2016-03-14")
    job.calculate()
    Thread.sleep(100000000)
  }
  private def getDate(day: String): DateTime = {
    var time: Long = System.currentTimeMillis
    if (StringUtil.isNotBlank(day)) {
      time = DateTimeUtil.parse(day, DateTimeUtil.DATE_DAY_FORMAT).getTime
    }
    new DateTime(time)
  }
}
