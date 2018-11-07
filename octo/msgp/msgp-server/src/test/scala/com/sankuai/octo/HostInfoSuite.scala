package com.sankuai.octo

import com.sankuai.msgp.common.utils.DateTimeUtil
import com.sankuai.octo.msgp.serivce.data.FalconQuery
import org.joda.time.{DateTime, LocalDate}
import com.sankuai.msgp.common.utils.helper.JsonHelper
import com.sankuai.octo.msgp.serivce.service.ServiceHost
import com.sankuai.octo.msgp.serivce.sgagent.SgAgentChecker
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{BeforeAndAfter, FunSuite}
import scala.util.parsing.json.JSON

/**
 * Created by lhmily on 12/16/2015.
 */
@RunWith(classOf[JUnitRunner])
class HostInfoSuite extends FunSuite with BeforeAndAfter {
  test("hostinfo") {
    //println(ServiceHost.getHostInfo("10.4.245.3"))
    println(ServiceHost.getHostInfo("10.20.61.147"))
  }

  test("getPerformanceOfHTTP") {
    val date = new DateTime("2018-2-17").withTimeAtStartOfDay()
    val s = SgAgentChecker.getPerformanceOfHTTP("com.sankuai.cos.mtconfig", date)
    SgAgentChecker.getDailyAvailability("com.sankuai.cos.mtconfig","mcc")
    println(s)
  }
  case class HostCount(host: String, count: Double, total: Double)
  test("getFalcon") {
    val date = DateTimeUtil.parse("2017-04-08 14:00:00", DateTimeUtil.DATE_TIME_FORMAT)
    val start = (date.getTime / 1000).toInt
    val end = start + 4 * 60 * 60
    val hosts = List("set-gh-cos-mtconfig-test02")
    val data = FalconQuery.getMetrics(start, end, hosts, "MnsInvoker.getConfig.success.percent")

    val hostDatas = data.map {
      falconData =>
        val values = falconData.Values.getOrElse(List())
        if (values.nonEmpty) {
          val value = values.maxBy(_.value)
          val total = values.map(_.value.getOrElse(0.0)).sum
          val hostname = falconData.endpoint.getOrElse("")
          HostCount(hostname, value.value.getOrElse(0.0), total)
        } else {
          HostCount("", 0, 0)
        }
    }
    val sortDatas = hostDatas.sortBy(-_.count)
    println("====order by max====== ")
    sortDatas.foreach(x => println(s"${x.host}\t${x.count}\t${x.total}"))
  }

    test("getMccDynamicData") {
      val res = "{\"ret\":0,\"msg\":\"success\",\"data\":{\"d1\":\"d4\",\"key13\":\"1314\",\"hawktest\":\"#1#2\",\"key0\":\"q\",\"c\":\"cdev\",\"hawktest2\":\"#1#2#!!\",\"beeapp_ios_download_url\":\"itms-services://?action=download-manifest&url=https://mss.sankuai.com/v1/mss_58cc5fe0bd5a4d8b909e48a322de2da1/test/moma-3.8.0-380.plist\\ntms-services://?action=download-manifest\",\"testKey1\":\"testValue2\"},\"version\":\"2881924903435\"}";
      JSON.parseFull(res) match {
        case Some(map: Map[String, Any]) => {
          val dataMap = map.get("data")
          val jsonString = JsonHelper.jsonStr("{}")
          println(jsonString)
        }
        case _ => "获取失败"
      }
      println(res)
    }

    test("getResultWhenIPNOTAvailable") {
      val r = ServiceHost.getResultWhenIPNOTAvailable("10.124.1.100")//  10.69.198.145
      println(r)
    }
}
