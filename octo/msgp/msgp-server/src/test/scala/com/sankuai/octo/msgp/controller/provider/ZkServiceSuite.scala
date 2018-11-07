package com.sankuai.octo.msgp.controller.provider

import com.sankuai.octo.msgp.serivce.zk.ZkService
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{BeforeAndAfter, FunSuite}

@RunWith(classOf[JUnitRunner])
class ZkServiceSuite extends FunSuite with BeforeAndAfter {

  test("getProviderByPath") {
   val list =  ZkService.getProviderByPath("/mns/sankuai/prod/search-arts-distshop/provider")
    println(list)
  }

}