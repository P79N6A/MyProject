package com.sankuai.octo.msgp.dao.report

import com.sankuai.msgp.common.config.DbConnection
import com.sankuai.msgp.common.config.db.msgp.Tables._

import scala.slick.driver.MySQLDriver.simple._

/**
  * Created by wujinwu on 16/3/10.
  */
object ReportErrorLogDao {

  private val db = DbConnection.getPool()


  def batchInsert(list: Seq[ReportErrorlogDomain]) = {
    db withSession {
      implicit session: Session =>
        val seq = list.map(domain => {
          ReportErrorlogRow(0, domain.owt, domain.appkey, domain.count, domain.errorCount,
            domain.ratio,  domain.start_day, domain.create_time)
        })
        seq.foreach(ReportErrorlog.insertOrUpdate)
    }
  }
  def get(appkey: String,weekDay:java.sql.Date) = {
    db withSession {
      implicit session: Session =>
        ReportErrorlog.filter(_.appkey === appkey).filter(_.startDay === weekDay).firstOption
    }
  }

  /**
   * 查询 最高qps
   * ,qps最高的 tp
   *  目的获取 appkey 顺序
   */
  def query(owt: String,weekDay:java.sql.Date, limit: Int) = {
    db withSession {
      implicit session: Session =>
        ReportErrorlog.filter(_.owt === owt).filter(_.startDay === weekDay).sortBy(_.count.desc)
          .sortBy(_.ratio.desc).take(limit).list
    }
  }


  case class ReportErrorlogDomain(owt: String, appkey: String, count: Long,  errorCount: Long,ratio: Double,
                                  start_day: java.sql.Date, create_time: Long)

}
