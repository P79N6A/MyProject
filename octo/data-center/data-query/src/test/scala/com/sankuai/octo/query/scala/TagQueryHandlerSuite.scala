package com.sankuai.octo.query.scala

import com.sankuai.octo.query.helper.QpsHelper
import com.sankuai.octo.statistic.constant.Constants
import com.sankuai.octo.statistic.helper.DailyMetricHelper
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{BeforeAndAfter, FunSuite}
import org.slf4j.LoggerFactory

import scala.collection.JavaConverters._

@RunWith(classOf[JUnitRunner])
class TagQueryHandlerSuite extends FunSuite with BeforeAndAfter {

  private val logger = LoggerFactory.getLogger(this.getClass)

  test("startDay") {
    val start = System.currentTimeMillis() / 1000
    val oneDay = 24 * 60 * 60
    val end = start + oneDay * 2
    val startDay = DailyMetricHelper.dayStart(start * 1000L)
    val endDay = DailyMetricHelper.dayStart(end * 1000L)
    val count = (endDay - startDay) / oneDay

    (0 to count).foreach { x =>
      val dayStart = startDay + x * oneDay
      logger.info(s"时间:$dayStart")
    }
  }

//  test("tags") {
//    val appkey: String = "TagTestAppkey"
//    val start: Int = (System.currentTimeMillis / 1000 - 86400 * 2).toInt
//    val end: Int = (System.currentTimeMillis / 1000).toInt
//    val tags = TagQueryHandler.tags(appkey,StatEnv.Prod,StatSource.Server,start,end)
//    logger.info(s"tags $tags")
//  }

//  test("tags_client") {
//    val appkey: String = "com.sankuai.inf.logCollector"
//    val start: Int = (System.currentTimeMillis / 1000 - 86400 * 2).toInt
//    val end: Int = (System.currentTimeMillis / 1000).toInt
//    val tags = TagQueryHandler.tags(appkey, StatEnv.Prod, StatSource.Server, start, end)
//    logger.info(s"tags $tags")
//  }
  test("queryProviderSpan") {
    val list = QpsHelper.queryProviderSpanToConsumer("com.sankuai.inf.logCollector", Constants.ALL, "prod")
    list.asScala.foreach(println)
  }

  test("queryProvider") {
    val list = QpsHelper.queryProvider("com.sankuai.inf.logCollector", "prod")
    list.asScala.foreach(println)
  }

  test("queryClientQps") {
    val start = (System.currentTimeMillis() / 1000L - Constants.SIX_MINUTES_SECONDS).toInt
    val end = (System.currentTimeMillis() / 1000L - Constants.FIVE_MINUTES_SECONDS).toInt
    val list = QpsHelper.queryClientQps("com.sankuai.inf.logCollector", "all", start, end, "prod")
    list.getConsumer2QpsList.asScala.foreach(entry => {
      println(entry.getConsumerAppKey)
      println(entry.getQpsAvg)
    })
  }

}
