package com.sankuai.octo.msgp.dao.monitor

import com.sankuai.msgp.common.config.DbConnection
import com.sankuai.msgp.common.config.db.msgp.Tables._

import scala.slick.driver.MySQLDriver.simple._

object MonitorCountDAO {
  private val db = DbConnection.getPool()

  def getByBusinessMonitorId(businessMonitorId: Long) = {
    db withSession {
      implicit session: Session =>
        BusinessMonitorCount.filter(_.businessMonitorId === businessMonitorId).list.headOption
    }
  }

  def update(row: BusinessMonitorCountRow) = {
    db withSession {
      implicit session: Session =>
        BusinessMonitorCount.filter(_.businessMonitorId === row.businessMonitorId).update(row)
    }
  }

  def insert(row: BusinessMonitorCountRow) = {
    db withSession {
      implicit session: Session =>
        (BusinessMonitorCount returning BusinessMonitorCount.map(_.id)).insert(row)
    }
  }

  def main(args: Array[String]) {
    update(BusinessMonitorCountRow(1, 1, 2))
  }
}
