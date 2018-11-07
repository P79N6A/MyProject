package com.sankuai.octo.msgp

import com.sankuai.octo.msgp.serivce.service.ServiceProvider
import org.scalatest.{BeforeAndAfter, FunSuite}
import org.springframework.test.context.{ContextConfiguration, TestContextManager}



@ContextConfiguration(locations = Array(
  "classpath*:applicationContext-*.xml"))
class ServiceProviderSuite extends FunSuite with BeforeAndAfter {
  new TestContextManager(this.getClass).prepareTestInstance(this)

  test("getServiceIdc"){
    val appkey = "com.sankuai.inf.data.statistic"
    val data =  ServiceProvider.getServiceIdc(appkey,1)
    println(data)
  }
}
