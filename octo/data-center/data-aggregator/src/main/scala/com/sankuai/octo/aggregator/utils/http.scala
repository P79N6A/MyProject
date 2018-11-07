package com.sankuai.octo.aggregator.utils

import dispatch._
import org.slf4j.{Logger, LoggerFactory}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, duration}

object http {
  val LOG: Logger = LoggerFactory.getLogger(http.getClass)
  implicit val timeout = Duration.create(30, duration.SECONDS)
  implicit val stringParser = {
    text: String => Some(text)
  }

  def execute[T](req: Req, parser: String => Option[T] = stringParser)
                (implicit duration: Duration = timeout): Option[T] = {
    try {
      val text = Await.result(Http(req > as.String), duration)
      parser(text)
    } catch {
      case e: Exception =>
        LOG.error(s"http client for ${req.url}", e)
        None
    }
  }
}
