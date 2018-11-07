package com.sankuai.octo

import com.sankuai.msgp.common.utils.client.Messager
import com.sankuai.msgp.common.utils.client.Messager.Alarm
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{BeforeAndAfter, FunSuite}

@RunWith(classOf[JUnitRunner])
class MessagerSuite extends FunSuite with BeforeAndAfter {

  test("send") {
    val list = List("hanjiancheng","digger")
    val alarm = Alarm(s"OCTO报警：测试", "测试报警发送", "", "")
    Messager.sendXMAlarm(list, alarm)
  }

}
