package com.sankuai.octo.msgp

import com.sankuai.octo.msgp.dao.monitor.ProviderTriggerDao
import com.sankuai.octo.msgp.task.MonitorProviderTask
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{BeforeAndAfter, FunSuite}

/**
 * Created by zava on 16/7/29.
 */

@RunWith(classOf[JUnitRunner])
class MonitorProviderJobSuite extends FunSuite with BeforeAndAfter {

  test("checkApp") {
    MonitorProviderTask.checkAppkey("com.sankuai.waimai.d.merchantcoupon")
  }
  test("checkItem") {
    val appkey = "com.sankuai.waimai.d.merchantcoupon"
    val size = 14
    val aliveSize = 14
    val aliveRatio = if (size > 0) {
      aliveSize * 100 / size
    } else {
      0
    }
    val triggers = ProviderTriggerDao.getTriggers(appkey)
    triggers.foreach {
      x =>
//        MonitorProviderTask.checkItem(appkey, x, size, aliveSize, aliveRatio)
    }
  }

}