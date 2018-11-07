package com.sankuai.octo

import com.sankuai.octo.msgp.serivce.service.ServiceConfig
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{BeforeAndAfter, FunSuite}
import com.sankuai.octo.msgp.serivce.mcc.MtConfigService
/**
 * Created by liangchen on 17/9/6.
 */
@RunWith(classOf[JUnitRunner])
class ServiceConfigSuite extends FunSuite with BeforeAndAfter {
  test("deleteConfigFile") {
    val res = ServiceConfig.deleteConfigFile("com.sankuai.octo.tmy", 2, "0", "applicationContext.xml")
    println(res)
  }
}
