package com.sankuai.octo.aggregator

import com.sankuai.octo.aggregator.util.MyProxy
import dispatch._
import org.slf4j.{Logger, LoggerFactory}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, duration}

object http {
  private val LOG: Logger = LoggerFactory.getLogger(http.getClass)
  private val config = MyProxy.mcc

  implicit val timeout = Duration.create(5, duration.SECONDS)
  implicit val stringParser = {
    text: String => Some(text)
  }

  def execute[T](req: Req, parser: String => Option[T] = stringParser)
                (implicit duration: Duration = timeout): Option[T] = {
    try {
      val start = System.currentTimeMillis()
      val text = Await.result(Http(req > as.String), duration)
      val end = System.currentTimeMillis()
      if (config.get("http.debug", "false") == "true") {
        LOG.info(s"call ${req.url} cost ${end - start} return $text")
      }
      parser(text)
    } catch {
      case e: Exception =>
        LOG.error(s"$e for ${req.url}")
        None
    }
  }

  def syncCall[T](future: Future[String], parser: String => Option[T])
                 (implicit duration: Duration = timeout): Option[T] = {
    try {
      val text = Await.result(future, duration)
      LOG.debug(s"$text")
      parser(text)
    } catch {
      case e: Exception =>
        LOG.error(s"$e")
        None
    }
  }

  def main(args: Array[String]) {
//    val req = url("http://octo.sankuai.com/api/monitor/alive")
//    val ret = execute(req, {
//      text =>
//        (Json.parse(text) \ "status").asOpt[String]
//    })
//    println(ret)
//    println(execute(req))
  }
}
