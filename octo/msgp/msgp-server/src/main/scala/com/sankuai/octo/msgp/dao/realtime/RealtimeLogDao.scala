package com.sankuai.octo.msgp.dao.realtime

import com.sankuai.msgp.common.config.DbConnection
import com.sankuai.msgp.common.config.db.msgp.Tables._

import scala.slick.driver.MySQLDriver.simple._

object RealtimeLogDao {

  private val db = DbConnection.getPool()

  def insert(domain: RealtimeLogDomain) = {
    db withSession {
      implicit session: Session =>
        val row = RealtimeLogRow(0, domain.appkey, domain.logPath, domain.createTime)
        RealtimeLog.insertOrUpdate(row)
    }
  }


  def get(appkey: String): Option[RealtimeLogRow] = {
    db withSession {
      implicit session: Session =>
        RealtimeLog.filter(_.appkey === appkey).firstOption
    }
  }

  case class RealtimeLogDomain(appkey: String, logPath: String, createTime: Long)

}
