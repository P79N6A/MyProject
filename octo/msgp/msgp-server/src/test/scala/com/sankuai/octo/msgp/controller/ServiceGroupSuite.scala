package com.sankuai.octo.msgp.controller

import com.sankuai.msgp.common.model.Page
import com.sankuai.octo.msgp.serivce.service.ServiceGroup
import org.scalatest.{BeforeAndAfter, FunSuite}

class ServiceGroupSuite extends FunSuite with BeforeAndAfter {

  test("group") {
    val page = new Page(1,20)
    val data  = ServiceGroup.group("com.sankuai.inf.data.statistic",3,page)
    println(data)
  }

}
