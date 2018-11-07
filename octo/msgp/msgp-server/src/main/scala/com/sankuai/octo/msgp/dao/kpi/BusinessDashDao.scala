package com.sankuai.octo.msgp.dao.kpi

import com.sankuai.msgp.common.config.DbConnection
import com.sankuai.msgp.common.config.db.msgp.Tables._
import com.sankuai.octo.msgp.dao.monitor.BusinessMonitorDAO
import org.joda.time.DateTime
import org.slf4j.LoggerFactory

import scala.collection.JavaConverters._
import scala.slick.driver.MySQLDriver.simple._

object BusinessDashDao {
  private val db = DbConnection.getPool()
  private val logger = LoggerFactory.getLogger(this.getClass)

  def wrapper(id: Long = 0, category: String, title: String, owt: String, metricId: Long,
              endpoint: String, serverNode: String, metric: String, sampleMode: String) = {
    BusinessDashRow(id, owt, metricId)
  }

  def addMetricAndDash(id: Long = 0, category: String, title: String, owt: String, screenId: Long,
                       endpoint: String, serverNode: String, metric: String, sampleMode: String) = {
    var newScreenId = screenId
    if (category == "appkey") {
      insertOrUpdate(BusinessDashRow(id, owt, screenId))
    } else {
      // 存储到AppScreen
      newScreenId = AppScreenDao.insert(AppScreenRow(id, category, "", Some(endpoint), serverNode, title, metric, sampleMode, 0, new DateTime().getMillis))
      // 存储到BusinessDash
      insertOrUpdate(BusinessDashRow(id, owt, newScreenId))
    }
    BusinessMonitorDAO.addDefaultMonitor(newScreenId)
  }

  def insertOrUpdate(row: BusinessDashRow): Option[Long] = {
    db withSession {
      implicit session: Session =>
        (BusinessDash returning BusinessDash.map(_.id)).insertOrUpdate(row)
    }
  }

  def getDash() = {
    val dashs = db withSession {
      implicit session: Session =>
        BusinessDash.list
    }
    // TODO with auth strategy
    dashs.map(_.owt).toSet.toList.asJava
  }

  def get(owt: String, screenId: Long) = {
    db withSession {
      implicit session: Session =>
        if (screenId != 0) {
          BusinessDash.filter(x => x.owt === owt && x.screenId === screenId).map(_.screenId).list
        } else {
          BusinessDash.filter(_.owt === owt).map(_.screenId).list
        }
    }
  }

  def get(screenId: Long) = {
    db withSession {
      implicit session: Session =>
        BusinessDash.filter(x => x.screenId === screenId).list.headOption
    }
  }

  def getAllIds = {
    db withSession {
      implicit session: Session =>
        BusinessDash.filter(_.screenId =!= 0L).map(_.screenId).list
    }
  }

  def getAll = {
    db withSession {
      implicit session: Session =>
        BusinessDash.filter(_.screenId =!= 0L).list
    }
  }

  def delete(owt: String, metricId: Long) = {
    db withSession {
      implicit session: Session =>
        BusinessDash.filter { x => x.owt === owt && x.screenId === metricId}.delete
    }
  }

  def delete(metricId: Long) = {
    db withSession {
      implicit session: Session =>
        BusinessDash.filter { x => x.screenId === metricId}.delete
    }
  }
}
