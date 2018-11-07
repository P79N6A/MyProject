package com.sankuai.octo.msgp

import com.sankuai.octo.msgp.dao.perf.PerfDayDao
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{BeforeAndAfter, FunSuite}

/**
 * Created by zava on 16/8/26.
 */
@RunWith(classOf[JUnitRunner])
class PerfDayDaoSuite extends FunSuite with BeforeAndAfter {

  test("getReqCount") {
    val reqCount = PerfDayDao.getReqCount
    println(reqCount)
  }

}