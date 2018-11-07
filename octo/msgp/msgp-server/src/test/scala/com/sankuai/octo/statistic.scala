package com.sankuai.octo

import java.util.concurrent.TimeUnit

import com.sankuai.msgp.common.utils.helper.{HttpHelper, JsonHelper}
import dispatch.url
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{BeforeAndAfter, FunSuite}
import play.api.libs.json.Json

import scala.concurrent.duration.Duration

@RunWith(classOf[JUnitRunner])
class Statistic extends FunSuite with BeforeAndAfter {

  case class AlarmData(endpoint: String)

  implicit val var3 = Json.reads[AlarmData]
  implicit val var4 = Json.writes[AlarmData]

  test("error") {
    val request = "http://n.falcon.sankuai.com/alarm/data/api/get_alarm_history/"

    val ret = List(1472918400, 1473004800, 1473091200, 1473177600, 1473264000, 1473350400, 1473436800).flatMap {
      time =>
        val postReq = url(request).POST << JsonHelper.jsonStr(Map("start_time" -> time,
          "end_time" -> (time + 86400),
          "node" -> "",
          "endpoint" -> "",
          "metric" -> "sg.custom.error.status",
          "receiver" -> "",
          "limit" -> 0))
        val ret = HttpHelper.execute(postReq)(Duration.create(100, TimeUnit.SECONDS)).getOrElse("")

        val list = (Json.parse(ret) \ "data").validate[List[AlarmData]].fold({ error =>
          List()
        }, {
          value => value
        })

        println(list.length)
        Thread.sleep(200)
        list
    }

    println(ret.map(_.endpoint).length)
    println(ret.map(_.endpoint).distinct.length)
  }
}
