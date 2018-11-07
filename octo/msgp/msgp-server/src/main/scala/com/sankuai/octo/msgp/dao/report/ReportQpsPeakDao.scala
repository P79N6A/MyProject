package com.sankuai.octo.msgp.dao.report

import com.sankuai.msgp.common.config.DbConnection
import com.sankuai.msgp.common.config.db.msgp.Tables._
import org.slf4j.{Logger, LoggerFactory}

import scala.slick.driver.MySQLDriver.simple._

/**
  * Created by wujinwu on 16/3/11.
  */
object ReportQpsPeakDao {

  private val logger: Logger = LoggerFactory.getLogger(this.getClass)
  private val db = DbConnection.getPool()


  def insert(domain: ReportQpsPeakDomain) = {
    db withSession {
      implicit session: Session =>
        val row = ReportQpsPeakRow(0, domain.owt, domain.appkey, domain.count, domain.host_count,
          domain.avg_qps, domain.max_minute_qps, domain.min_minute_qps, domain.avg_host_qps,
          domain.max_hour_host_qps, domain.max_host_qps, domain.start_day, domain.create_time)
        ReportQpsPeak.insertOrUpdate(row)
    }
  }

  def batchInsert(list: Seq[ReportQpsPeakDomain]) = {
    db withSession {
      implicit session: Session =>
        val seq = list.map(domain => {
          ReportQpsPeakRow(0, domain.owt, domain.appkey, domain.count, domain.host_count,
            domain.avg_qps, domain.max_minute_qps, domain.min_minute_qps, domain.avg_host_qps,
            domain.max_hour_host_qps, domain.max_host_qps, domain.start_day, domain.create_time)
        })
        seq.foreach(ReportQpsPeak.insertOrUpdate)
    }
  }

  /**
    * 查询 最高qps
    * ,qps最高的 tp
    * 目的获取 appkey 顺序
    */
  def query(owt: String, weekDay: java.sql.Date, limit: Int) = {
    db withSession {
      implicit session: Session =>
        ReportQpsPeak.filter(_.owt === owt).filter(_.startDay === weekDay).sortBy(_.avgQps.desc)
          .take(limit).list
    }
  }
  /**
   *
   */
  def get(appkey: String, weekDay: java.sql.Date) = {
    db withSession {
      implicit session: Session =>
        ReportQpsPeak.filter(_.appkey === appkey).filter(_.startDay === weekDay).firstOption
    }
  }

  case class ReportQpsPeakDomain(owt: String, appkey: String, count: Long, host_count: Int, avg_qps: Double,
                                 max_minute_qps: Double, min_minute_qps: Double, avg_host_qps: Double, max_hour_host_qps: Double,
                                 max_host_qps: Double, start_day: java.sql.Date, create_time: Long)

}
