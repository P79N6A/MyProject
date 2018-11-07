package com.sankuai.octo.msgp.dao.report

import com.sankuai.msgp.common.config.DbConnection
import com.sankuai.msgp.common.config.db.msgp.Tables._

import scala.slick.driver.MySQLDriver.simple._
import scala.slick.jdbc.GetResult
import scala.slick.jdbc.StaticQuery.interpolation

/**
 * Created by zava on 16/4/25.
 * 日报
 */
object ReportDailyDao {
  private val db = DbConnection.getPool()

  case class ReportWeeklyItem(appkey: String, day: java.sql.Date, count: Long, qps: Double, tp50: Int, tp90: Int, tp999: Int, successRatio: Double)

  implicit val getDailyResult = GetResult(r => ReportWeeklyItem(r.<<, r.<<, r.<<, r.<<, r.<<, r.<<, r.<<, r.<<))

  case class ReportDailyDomain(owt: String, appkey: String, spanname: String, count: Long, successRatio: Double = 0.0, aliveRatio: String,
                               qps: Double = 0.0, topQps: Double = 0.0, avgHostQps: Double = 0.0, topHostQps: Double = 0.0,
                               tp999: Int = 0, tp99: Int = 0, tp95: Int = 0, tp90: Int = 0, tp50: Int = 0, errorCount: Long = 0L,
                               perfAlertCount: Long = 0L, isLoadBalance: Int = 0,
                               day: java.sql.Date, createTime: Long = 0L, hostCount: Int = 0,
                               minMinuteQps: Double = 0.0
                                )


  def batchInsert(list: Seq[ReportDailyDomain]) = {
    db withSession {
      implicit session: Session =>
        val seq = list.map(domain => {
          ReportDailyRow(0, domain.owt, domain.appkey, domain.spanname, domain.count, Some(domain.successRatio), domain.aliveRatio, domain.qps, domain.topQps, domain.topHostQps, domain.avgHostQps,
            domain.tp999, domain.tp99, domain.tp95, domain.tp90, domain.tp50, domain.errorCount, domain.perfAlertCount, domain.isLoadBalance,domain.hostCount, domain.day, domain.createTime)
        })
        seq.foreach(ReportDaily.insertOrUpdate)
    }
  }

  def insert(domain: ReportDailyDomain) = {
    db withSession {
      implicit session: Session =>
        val row = ReportDailyRow(0, domain.owt, domain.appkey, domain.spanname, domain.count, Some(domain.successRatio), domain.aliveRatio, domain.qps, domain.topQps, domain.topHostQps, domain.avgHostQps,
          domain.tp999, domain.tp99, domain.tp95, domain.tp90, domain.tp50, domain.errorCount, domain.perfAlertCount,domain.isLoadBalance,domain.hostCount, domain.day, domain.createTime)
        ReportDaily.insertOrUpdate(row)
    }
  }

  /**
   * 获取该appkey下所有spanname的日报数据
   *
   * @param appkey
   * @param day
   */
  def getAppkeyData(appkey: String, day: java.sql.Date) = {
    db withSession {
      implicit session: Session =>
        ReportDaily.filter { x => x.appkey === appkey && x.day === day }.list
    }
  }

  /**
   * 获得单个接口的日报数据
   *
   * @param appkey
   * @param spanname
   * @param day
   * @return
   */
  def getSpanData(appkey: String, spanname: String, day: java.sql.Date) = {
    db withSession {
      implicit session: Session =>
        ReportDaily.filter { x => x.appkey === appkey && x.spanname === spanname && x.day === day }.firstOption
    }
  }


  def week(appkeys: List[String], start: java.sql.Date, end: java.sql.Date) = {
    db withSession {
      val appkey_str = s"'${appkeys.mkString("','")}'"
      implicit session: Session =>
        val sqlStr = s"select appkey,`day`, count, qps, tp50, tp90, tp999, success_ratio from report_daily where appkey in ($appkey_str) and spanname = 'all' and `day` >= '$start' and `day` <= '$end' order by appkey,`day`"
        sql"""#$sqlStr""".as[ReportWeeklyItem].list
    }
  }

  def updateAvailability(appkey: String, spannme: String, day: java.sql.Date, successRatio: Double)={
    db withSession {
      implicit session: Session =>
        val statement = ReportDaily.filter { x =>
          x.appkey === appkey &&
            x.spanname === spannme &&
            x.day === day }
        if(statement.exists.run){
          statement.map(_.successRatio).update(Some(successRatio))
        }
    }
  }

  /**
   * 查询所有有性能数据的appkey
   * @param day
   */
  def getAllAppkey(day: java.sql.Date) ={
    db withSession {
      implicit session: Session =>
        ReportDaily.filter { x => x.day === day && x.spanname==="all" }.map(_.appkey).list.distinct
    }
  }
}
