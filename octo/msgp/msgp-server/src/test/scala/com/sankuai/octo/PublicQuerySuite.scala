package com.sankuai.octo

import com.sankuai.octo.msgp.serivce.data.PublicQuery
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{BeforeAndAfter, FunSuite}


/**
  * Created by yves on 16/11/7.
  */
@RunWith(classOf[JUnitRunner])
class PublicQuerySuite extends FunSuite with BeforeAndAfter {

//  test("getQPS"){
//    val testTimes = 1000
//    val startSeconds = new DateTime().getMillis
//    val qpsList = (0 to testTimes).map{
//      num=>
//        //val ts = (startSeconds / 1000l) - num * 60
//        val end = new DateTime().getMillis / 1000l
//        val start = end - 60
//        val qps = PublicQuery.getQPS("com.meituan.pic.imageproc.start", "all", "prod", end, 60)
//        println("(start: " + start +", end: " + end + ")-> qps: " + qps)
//        qps
//    }.toList
//    val endSeconds = new DateTime().getMillis
//    val errorCount = qpsList.count(_ == -1 )
//    println("error count is: " + errorCount +", success query rate is: " + ((testTimes - errorCount) / testTimes) * 100 + "%")
//    println("average query time is: " + (endSeconds - startSeconds) / testTimes + "ms")
//  }

  test("getAverageQps") {
    val ts = System.currentTimeMillis()/1000l - 60*5
    val days = 2
    val avgQps = PublicQuery.getAverageQps("com.sankuai.inf.mnsc", "all", "prod", ts, days)
    println(avgQps)
    Thread.sleep(60000)
  }

  test("getDailyData"){
    val result = PublicQuery.getDailyData("com.sankuai.inf.msgp", "prod", 1505729526)
    println(result)
  }
}
