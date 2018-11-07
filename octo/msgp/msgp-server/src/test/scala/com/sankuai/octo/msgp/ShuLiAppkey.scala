package com.sankuai.octo.msgp

import java.util.concurrent.TimeUnit

import com.sankuai.msgp.common.model.ServiceModels.Desc
import dispatch.Defaults._
import dispatch.{Http, as, url}
import play.api.libs.json.Json

import scala.concurrent.Await
import scala.concurrent.duration.Duration

object ShuLiAppkey {
  val appStr = ""
  val apps = appStr.split(",").sorted
  implicit val timeout = Duration.create(20000, TimeUnit.MILLISECONDS)

  val urlStr = "http://octo.sankuai.com/api/appkeyDesc/"

  def getDesc() = {
    apps.foreach { x =>
      val getReq = url(urlStr + x).GET

      val text = try {
        val future = Http(getReq OK as.String)
        Await.result(future, timeout)
      } catch {
        case e: Exception =>
          ""
      }

      (Json.parse(text) \ "data").validate[Desc].fold({
        error =>
          ""
      }, {
        x =>
          println(s"${x.appkey} === ${x.owt.getOrElse("")} - ${x.pdl.getOrElse("")} === ${x.owner} === ${x.intro}")
      })
    }
  }

  def main(args: Array[String]) {
    getDesc
  }
}
