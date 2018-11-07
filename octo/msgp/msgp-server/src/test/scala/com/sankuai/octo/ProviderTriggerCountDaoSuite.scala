package com.sankuai.octo

import com.sankuai.msgp.common.config.db.msgp.Tables
import com.sankuai.octo.msgp.dao.monitor.ProviderTriggerCountDao
import org.junit.runner.RunWith
import org.scalatest.{BeforeAndAfter, FunSuite}
import org.scalatest.junit.JUnitRunner

/**
  * Created by nero on 2018/4/19
  */
@RunWith(classOf[JUnitRunner])
class ProviderTriggerCountDaoSuite extends FunSuite with BeforeAndAfter{


  test("run") {
//    ProviderTriggerCountDao.insert(Tables.ProviderTriggerCountRow(0,1,2))
    ProviderTriggerCountDao.update(Tables.ProviderTriggerCountRow(3,1,0))
    println(ProviderTriggerCountDao.getProviderTriggerCount())
    println(ProviderTriggerCountDao.getPorviderMonitorId(1))
  }
}
