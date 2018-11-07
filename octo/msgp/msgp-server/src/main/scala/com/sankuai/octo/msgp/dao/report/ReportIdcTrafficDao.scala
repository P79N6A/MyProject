package com.sankuai.octo.msgp.dao.report

import com.sankuai.msgp.common.config.DbConnection
import com.sankuai.msgp.common.config.db.msgp.Tables._
import org.slf4j.{Logger, LoggerFactory}

import scala.slick.driver.MySQLDriver.simple._
import scala.slick.jdbc.GetResult
import scala.slick.jdbc.StaticQuery.interpolation

/**
  * Created by wujinwu on 16/3/11.
  */
object ReportIdcTrafficDao {

  private val logger: Logger = LoggerFactory.getLogger(this.getClass)
  private val db = DbConnection.getPool()

  case class AppkeyCount(appkey: String,  count: Long)

  implicit val getAppkeyCountResult = GetResult(r => AppkeyCount(r.<<, r.<<))

  def batchInsert(list: Seq[ReportIdcTrafficDomain]) = {
    db withSession {
      implicit session: Session =>
        val seq = list.map(domain => {
          ReportIdcTrafficRow(0, domain.owt, domain.appkey, domain.idc, domain.host_count,
            domain.idc_count, domain.start_day, domain.create_time)
        })
        seq.foreach(ReportIdcTraffic.insertOrUpdate)
    }
  }

  /**
   * 查询 最高qps
   * ,qps最高的 tp
   * 目的获取 appkey 顺序
   */
  def query(appkeys:List[String],weekDay: java.sql.Date) = {
    db withSession {
      implicit session: Session =>
        ReportIdcTraffic.filter(_.startDay === weekDay).filter(_.appkey inSet appkeys).list
    }
  }

  def query(owt: String, weekDay: java.sql.Date) = {
    db withSession {
      implicit session: Session =>
        ReportIdcTraffic.filter(_.owt === owt).filter(_.startDay === weekDay).list
    }
  }


  def sumlist(owt: String, weekDay: java.sql.Date, limit: Int) = {
    db withSession {
      implicit session: Session =>
        sql"select appkey ,sum(idc_count) as count from report_idc_traffic where  owt  =  ${owt}  and start_day= ${weekDay} GROUP  by appkey order by count desc ".as[AppkeyCount].list
    }
  }
  def get(appkey: String, weekDay: java.sql.Date) = {
    db withSession {
      implicit session: Session =>
        ReportIdcTraffic.filter(_.appkey === appkey).filter(_.startDay === weekDay).list
    }
  }

  case class ReportIdcTrafficDomain(owt: String, appkey: String, idc: String, host_count: Int, idc_count: Long, start_day: java.sql.Date, create_time: Long)

}
