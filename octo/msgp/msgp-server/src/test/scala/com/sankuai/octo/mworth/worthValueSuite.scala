package com.sankuai.octo.mworth

import com.sankuai.octo.mworth.dao.worthValue
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{BeforeAndAfter, FunSuite}

/**
 * Created by zava on 16/2/26.
 */
@RunWith(classOf[JUnitRunner])
class worthValueSuite extends FunSuite with BeforeAndAfter {

  test("echart") {
    println(worthValue.echart(5))
  }

}
