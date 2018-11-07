package com.sankuai.octo.aggregator.util

import org.scalatest.FunSuite

import scala.collection.mutable
import scala.io.Source

/**
  * Created by wujinwu on 15/12/17.
  */
class RestfulFilterTest extends FunSuite {

  test("testCleanSpanName") {
    var count = 0L
    val set = mutable.Set[String]()
    for (line <- Source.fromFile("/Users/wujinwu/testFile").getLines()) {
      set += line
    }
    val start = System.currentTimeMillis()
    (1 to 100000).foreach { _ =>
      for (line <- set) {
        RestfulFilter.cleanSpanName(line)
        count += 1
      }
    }
    println(s"count:$count,cost:${System.currentTimeMillis() - start} ms")
  }

  test("testRegEx") {
    val input: String = "/12/3-4.5/6.1-1/12.22222,1213123213/21312-322.2/4.1-4/21/1-11/1-11/23-412/123-123-23-132-123-1313.2-13.13-12.-12.3-..1-231/view1232141/*,*,*,*,*,*,*,*,*,*,*,*,*,*,*,*,*,*,*,*,*,*,*,*,*,*,*,*,*,*,*,*,*,*,*,*,*,*,*,*,*,*,*,*,*,*,*,*,*,*,*,*,*,*,*,*,*,*,*,*,*,*,*,*,*,*,*,*,*,*,*,*,*,*,*,*,*,*,*"
    val input2: String = "IGroupOrderService.createOrderV1231.231231"
    println(RestfulFilter.cleanSpanName(input))
    println(RestfulFilter.cleanSpanName(input2))
  }

  test("testJessionId"){
    val inputs = List("/login;jsessionid=1brwhynguteew1mbdf9t0t2my","/login;\t\n\njsessionid=1brwhynguteew1mbdf9t0t2my?abc","/login;\t\n\njsessionid=1brwhynguteew1mbdf9t0t2my&dbc")
    inputs.foreach{
      input=>
        println(RestfulFilter.cleanSpanName(input))
    }
  }
}
