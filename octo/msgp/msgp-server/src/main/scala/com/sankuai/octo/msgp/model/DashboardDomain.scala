package com.sankuai.octo.msgp.model

import java.util.concurrent.atomic.AtomicInteger

import com.sankuai.octo.msgp.dao.perf.PerfDayDao.{CategoryPerfCount, RequestCount}
import play.api.libs.json.Json

import scala.collection.immutable.Map

object DashboardDomain {

  case class Shortcut(id: Option[String], title: String, url: String, appkey: String)

  case class Dashboard(url: String)

  case class Overview(serviceCount: Int, serviceByBusiness: Map[String, Int], instanceCount: Int,
                      instanceByStatus: Map[String, Int], instanceByDC: Map[String, Int],
                      requestCountToday: String, requestCount: RequestCount, serviceGBLocation: Map[String, Int],
                      serviceGBType: Map[String, Int])


  implicit val categoryPerfCountReads = Json.reads[CategoryPerfCount]
  implicit val categoryPerfCountWrites = Json.writes[CategoryPerfCount]

  implicit val dayCategoryPerfCountReads = Json.reads[RequestCount]
  implicit val dayCategoryPerfCountWrites = Json.writes[RequestCount]

  implicit val overviewReads = Json.reads[Overview]
  implicit val overviewWrites = Json.writes[Overview]


  case class Menu(id: Int, title: String, url: String, menus: List[Menu])

  object Menu {
    val idOp = new AtomicInteger()

    def apply(title: String) = new Menu(idOp.incrementAndGet(), title, "", List())

    def apply(title: String, menus: List[Menu]) = new Menu(idOp.incrementAndGet(), title, "", menus)

    def apply(title: String, url: String) = new Menu(idOp.incrementAndGet(), title, url, List())

    def apply(title: String, url: String, menus: List[Menu]) = new Menu(idOp.incrementAndGet(), title, url, menus)

    def apply(id: Int, title: String, url: String) = new Menu(id, title, url, List())
  }

  implicit val shortcutReads = Json.reads[Shortcut]
  implicit val shortcutWrites = Json.writes[Shortcut]
  implicit val dashReads = Json.reads[Dashboard]
  implicit val dashWrites = Json.writes[Dashboard]
}
