package com.sankuai.octo.msgp.dao.report

import com.sankuai.msgp.common.config.DbConnection
import com.sankuai.msgp.common.config.db.msgp.Tables._
import org.slf4j.{Logger, LoggerFactory}

import scala.slick.driver.MySQLDriver.simple._

/**
  * Created by wujinwu on 16/3/10.
  */
object ReportQpsDao {

  private val logger: Logger = LoggerFactory.getLogger(this.getClass)
  private val db = DbConnection.getPool()

  def batchInsert(list: Seq[ReportQpsDomain]) = {
    db withSession {
      implicit session: Session =>
        val seq = list.map(domain => {
          ReportQpsRow(0, domain.owt, domain.appkey, domain.count, domain.qps,
            domain.tp90, domain.week_qps, domain.week_tp90, domain.start_day, domain.create_time)
        })
        seq.foreach(ReportQps.insertOrUpdate)
    }
  }

  def get(appkey: String, mondayOfWeek: java.sql.Date) = {
    db withSession {
      implicit session: Session =>
        ReportQps.filter(_.appkey === appkey).filter(_.startDay === mondayOfWeek).firstOption
    }
  }

  /**
   * 查询 最高qps
   * ,qps最高的 tp
   *  目的获取 appkey 顺序
   */
  def queryTopQps(owt: String, mondayOfWeek: java.sql.Date, limit: Int) = {
    db withSession {
      implicit session: Session =>
        ReportQps.filter(_.owt === owt).filter(_.startDay === mondayOfWeek)
          .sortBy(_.count.desc).take(limit).list
    }
  }

  /**
   * 查询性能最差的 tp
   */
  def queryTopTp(owt: String,weekDay:java.sql.Date, limit: Int) = {
    db withSession {
      implicit session: Session =>
        ReportQps.filter(_.owt === owt).filter(_.startDay === weekDay).
          sortBy(_.tp90.desc).take(limit).list
    }
  }

  case class ReportQpsDomain(owt: String, appkey: String, count: Long, qps: Double, tp90: Int, week_qps: String, week_tp90: String, start_day: java.sql.Date, create_time: Long)

}
