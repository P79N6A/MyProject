package com.sankuai.octo.dashboard

import com.sankuai.octo.msgp.serivce.falcon.AlarmQuery
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{BeforeAndAfter, FunSuite}

/**
 * Created by zava on 16/5/11.
 */
@RunWith(classOf[JUnitRunner])
class AlarmQuerySuite extends FunSuite with BeforeAndAfter {
  test("allAlarm") {
   val list =  AlarmQuery.owtAlarmCount("corp=meituan&owt=tair",1462979784)
    println(list)
  }

  test("query") {
//   val list =  AlarmQuery.querySrvAlarm("corp=meituan&owt=dba",3600)
   val list =  AlarmQuery.querySrvAlarm("corp=meituan&owt=dba",3600*6)
    println(list)
  }
}
