package com.sankuai.octo.mworth

import com.sankuai.octo.mworth.task.worthEventTask
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{BeforeAndAfter, FunSuite}

/**
 * Created by zava on 16/2/16.
 */


@RunWith(classOf[JUnitRunner])
class worthEventTaskSuite extends FunSuite with BeforeAndAfter {
  test("count") {
    println(worthEventTask.count(1455465600000L,1455580800000L))
  }
}
