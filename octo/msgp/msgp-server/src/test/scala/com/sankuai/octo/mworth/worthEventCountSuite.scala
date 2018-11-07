package com.sankuai.octo.mworth

import java.sql.Date

import com.sankuai.msgp.common.model.Page
import com.sankuai.octo.mworth.dao.worthEventCount
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{BeforeAndAfter, FunSuite}

/**
 * Created by zava on 16/2/15.
 */

@RunWith(classOf[JUnitRunner])
class worthEventCountSuite extends FunSuite with BeforeAndAfter {
  val day = new Date(System.currentTimeMillis() - 2*86400 * 1000L)
  val dtype = 0
  val page = new Page(1,10)

  test("queryUserme") {
    println(worthEventCount.queryUsername(1,"caojiguang",day, dtype, page))
    println(worthEventCount.queryUsername(1,"",day, dtype, page))
    println(worthEventCount.queryUsername(null,"caojiguang",day, dtype, page))
    println(worthEventCount.queryUsername(null,"",day, dtype, page))
    println(page.getTotalCount)
  }

  test("queryOwt") {
    println(worthEventCount.queryOwt(1,"hotel",day, dtype, page))
    println(worthEventCount.queryOwt(1,"",day, dtype, page))
    println(worthEventCount.queryOwt(null,"hotel",day, dtype, page))
    println(worthEventCount.queryOwt(null,"",day, dtype, page))
    println(page.getTotalCount)

  }

  test("queryModel") {
    val day = new Date(System.currentTimeMillis() - 6*86400 * 1000L)
    println(worthEventCount.queryModel(1,"MNC",day, dtype, page))
    println(worthEventCount.queryModel(1,"",day, dtype, page))
    println(worthEventCount.queryModel(null,"MNC",day, dtype, page))
    println(worthEventCount.queryModel(null,"",day, dtype, page))
    println(page.getTotalCount)
  }

  test("refrehBusiness") {
    worthEventCount.refreshBusiness();
  }
  test("refreshOwt") {
    worthEventCount.refreshOwt();
  }

  test("refresh") {
    val day = new Date(System.currentTimeMillis() - 8*86400 * 1000L)
    val dtype = 0
    worthEventCount.refresh(day,dtype);
  }

}

