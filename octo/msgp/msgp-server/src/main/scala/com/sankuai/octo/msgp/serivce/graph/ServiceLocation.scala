package com.sankuai.octo.msgp.serivce.graph

import com.sankuai.msgp.common.config.DbConnection
import com.sankuai.msgp.common.utils.helper.JsonHelper
import com.sankuai.msgp.common.config.db.msgp.Tables._
import com.sankuai.octo.msgp.serivce.graph.ServiceModel._
import org.slf4j.{Logger, LoggerFactory}
import play.api.libs.json.{JsError, Json}

import scala.slick.driver.MySQLDriver.simple._

object ServiceLocation {
  private val LOG: Logger = LoggerFactory.getLogger(ServiceLocation.getClass)
  private val db = DbConnection.getPool()

  def getAppGraph(graphId: Int) = {
    db withSession {
      implicit session: Session =>
        val result = AppGraph.filter(self => self.graphId === graphId).list
        result
    }
  }

  def getAppGraph(graphId: Int, appkey: String) = {
    db withSession {
      implicit session: Session =>
        AppGraph.filter(x => x.graphId === graphId && x.appkey === appkey).firstOption
    }
  }

  def updateAppAxis(data: String) = {
    Json.parse(data).validate[ServerGraph].fold({
      error => LOG.info(error.toString)
        JsonHelper.errorJson("内部异常: " + JsError.toFlatJson(error).toString())
    }, {
      self =>
        insertOrUpdateAppAxis(self.graphId, self.list)
        JsonHelper.dataJson(self)
    })
  }

  def insertOrUpdateAppAxis(graphId: Int, appAxis: List[AppAxis]) = {
    db withSession {
      implicit session: Session =>
        appAxis.foreach {
          app =>
            val statement = AppGraph.filter(self => (self.graphId === graphId && self.appkey === app.appkey)).map(self => (self.x, self.y))
            if (statement.exists.run) {
              statement.update(app.x.toInt, app.y.toInt)
            } else {
              AppGraph += AppGraphRow(0, graphId, app.appkey, app.x.toInt, app.y.toInt)
            }
        }
    }
  }
}
