package com.sankuai.octo.msgp

import com.sankuai.msgp.common.model.Status
import com.sankuai.octo.msgp.task.MonitorProviderTask
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{BeforeAndAfter, FunSuite}

/**
 * Created by zava on 16/6/29.
 */

@RunWith(classOf[JUnitRunner])
class StateEventServiceSuite extends FunSuite with BeforeAndAfter {

  test("put") {
    val sta = List(0,2)
    (100 to 1 by -1).foreach{
      x=>
      val s1 = (Math.random()*10%2).toInt
      val s2 = (s1+1)%2
      val statusEvent = MonitorProviderTask.StateEvent("com.sankuai.inf.logcollect",s"127.0.0.${x%10}","8801",Status.apply(sta(s1)),
        Status.apply(sta(s2)),(System.currentTimeMillis()/1000 - x*6).toInt)
    }

    Thread.sleep(10000000)
  }

}
