package com.sankuai.octo.msgp.dao.monitor

import com.sankuai.msgp.common.config.DbConnection
import com.sankuai.msgp.common.config.db.msgp.Tables._

import scala.slick.driver.MySQLDriver.simple._

/**
  * Created by nero on 2018/4/10
  */
object ProviderTriggerCountDao {

  private val db = DbConnection.getPool()

  def getPorviderMonitorId(appkeyTriggerId: Long) = {
    db withSession {
      implicit session: Session =>
        ProviderTriggerCount.filter(_.triggerId === appkeyTriggerId).list.headOption
    }
  }

  def update(row: ProviderTriggerCountRow) = {
    db withSession {
      implicit session: Session =>
        ProviderTriggerCount.filter(_.triggerId === row.triggerId).update(row)
    }
  }

  def insert(row: ProviderTriggerCountRow) = {
    db withSession {
      implicit session: Session =>
        (ProviderTriggerCount returning ProviderTriggerCount.map(_.id)).insert(row)
    }
  }

  def getProviderTriggerCount() = {
    db withSession {
      implicit session: Session =>
        ProviderTriggerCount.map(x => x.triggerId).list.distinct
    }
  }

}
