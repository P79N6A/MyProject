package com.sankuai.octo.msgp

import com.sankuai.octo.msgp.dao.availability.AvailabilityDao
import com.sankuai.octo.mworth.util.DateTimeUtil
import org.joda.time.DateTime
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{BeforeAndAfter, FunSuite}

/**
 * Created by zava on 16/7/31.
 */
@RunWith(classOf[JUnitRunner])
class AvailabilityDaoSuite extends FunSuite with BeforeAndAfter {

  test("checkApp") {
    val appkey = "com.sankuai.waimai.bizorder"
    val date =DateTimeUtil.parse("2016-07-07",DateTimeUtil.DATE_DAY_FORMAT)
    val dateTime = new DateTime(date.getTime)
    val availAble = AvailabilityDao.fetchAvailabilityMerged(appkey, dateTime)
    println(availAble)
  }

}
