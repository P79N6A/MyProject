package com.sankuai.octo.msgp.dao.report

import com.sankuai.msgp.common.config.DbConnection
import com.sankuai.msgp.common.config.db.msgp.Tables._

import scala.slick.driver.MySQLDriver.simple._

object ReportDependDao {

  private val db = DbConnection.getPool()

  def batchInsert(list: Seq[ReportDependDomain]) = {
    db withSession {
      implicit session: Session =>
        val seq = list.map(domain => {
          ReportDependRow(0, domain.business, domain.owt, domain.appkey, domain.`type`, domain.count, domain.start_day, domain.create_time)
        })
        seq.foreach(ReportDepend.insertOrUpdate)
    }
  }


  def query(owt: String, `type`: Boolean, weekDay: java.sql.Date, limit: Int) = {
    db withSession {
      implicit session: Session =>
        ReportDepend.filter(_.owt === owt).filter(_.startDay === weekDay).filter(_.`type` === `type`)
          .sortBy(_.count.desc).take(limit).list
    }
  }

  def get(appkey: String, `type`: Boolean, weekDay: java.sql.Date) = {
    db withSession {
      implicit session: Session =>
        ReportDepend.filter(_.appkey === appkey).filter(_.startDay === weekDay).filter(_.`type` === `type`).firstOption

    }
  }

  case class ReportDependDomain(business: Int, owt: String, appkey: String, `type`: Boolean, count: Long, start_day: java.sql.Date, create_time: Long)

}
