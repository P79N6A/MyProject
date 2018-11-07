package com.sankuai.octo.msgp.dao.monitor

import com.sankuai.msgp.common.config.DbConnection
import com.sankuai.msgp.common.config.db.msgp.Tables._

import scala.slick.driver.MySQLDriver.simple._

object AppkeyTriggerCountDAO {
  private val db = DbConnection.getPool()

  def getByBusinessMonitorId(appkeyTriggerId: Long) = {
    db withSession {
      implicit session: Session =>
        AppkeyTriggerCount.filter(_.appkeyTriggerId === appkeyTriggerId).list.headOption
    }
  }

  def update(row: AppkeyTriggerCountRow) = {
    db withSession {
      implicit session: Session =>
        AppkeyTriggerCount.filter(_.appkeyTriggerId === row.appkeyTriggerId).update(row)
    }
  }

  def insert(row: AppkeyTriggerCountRow) = {
    db withSession {
      implicit session: Session =>
        (AppkeyTriggerCount returning AppkeyTriggerCount.map(_.id)).insert(row)
    }
  }
}
