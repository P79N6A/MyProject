package com.sankuai.octo

import java.io.IOException

import com.sankuai.octo.msgp.serivce.service.ServiceAccessCtrl
import org.apache.zookeeper.KeeperException.NoNodeException
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{BeforeAndAfter, FunSuite}

/**
 * Created by lhmily on 09/29/2015.
 */
@RunWith(classOf[JUnitRunner])
class AccessCtrlSuite extends FunSuite with BeforeAndAfter {
  test("getAccessData") {
    try {
      //serviceAccessCtrl.getAccessData("com.sankuai.inf.sg_agent", 3, 0)
      ServiceAccessCtrl.getAccessData("com.sankuai.inf.sg_agent", 4, 0)
    } catch {
      case e: NoNodeException => println(e.getMessage)
      case e: IllegalArgumentException=>println(e.getMessage)
      case e: IOException => println(e.getMessage)
    }

  }
}
