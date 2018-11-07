package com.sankuai.octo.mworth

import com.sankuai.octo.mworth.service.mWorthDailyService
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{BeforeAndAfter, FunSuite}

/**
 * Created by zava on 16/2/28.
 */
@RunWith(classOf[JUnitRunner])
class worthCountServiceSuite extends FunSuite with BeforeAndAfter {

  test("count") {
    // 取得昨天的时间
    mWorthDailyService.updateBusinessInfoViaOps()
    //worthCountService.count(yesterday)
  }
}