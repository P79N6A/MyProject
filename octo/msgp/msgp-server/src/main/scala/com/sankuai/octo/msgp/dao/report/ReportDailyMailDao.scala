package com.sankuai.octo.msgp.dao.report

import com.sankuai.msgp.common.config.DbConnection
import com.sankuai.msgp.common.config.db.msgp.Tables._

import scala.slick.driver.MySQLDriver.simple._
import scala.slick.jdbc.StaticQuery.interpolation

/**
 * Created by zava on 16/4/25.
 * 日报
 */
object ReportDailyMailDao {
  private val db = DbConnection.getPool()

  def batchInsert(list: Seq[ReportDailyMailDomain]) = {
    db withSession {
      implicit session: Session =>
        val seq = list.map(domain => {
          ReportDailyMailRow(0, domain.appkey, domain.username, domain.day,
            domain.status, domain.sendTime,domain.createTime)
        })
        seq.foreach(ReportDailyMail.insertOrUpdate)
    }
  }
  def insert(domain: ReportDailyMailDomain) = {
    db withSession {
      implicit session: Session =>
        val row = ReportDailyMailRow(0, domain.appkey, domain.username, domain.day,
          domain.status, domain.sendTime,domain.createTime)
        ReportDailyMail.insertOrUpdate(row)
    }
  }
  /**
   * 获取需要给谁发邮件
   */
  def userlist(day: java.sql.Date) = {
    db withSession {
      implicit session: Session =>
        sql"select  DISTINCT username from report_daily_mail where  day  =  ${day}   ".as[String].list
    }
  }
  def userAppkey(username:String,day: java.sql.Date) ={
    db withSession {
      implicit session: Session =>
        ReportDailyMail.filter(_.username === username).filter(_.day === day).list
    }
  }


  def sendMail(username:String,day: java.sql.Date) ={
    db withSession {
      implicit session: Session =>
        val statement = ReportDailyMail.filter(x => x.username === username && x.day === day)
        // 插入or修改全局订阅状态
        if (statement.exists.run) {
          statement.map(x => (x.status, x.sendTime)).update(1, System.currentTimeMillis())
        }
    }
  }

  def readMail(username:String,day: java.sql.Date) ={
    db withSession {
      implicit session: Session =>
        val statement = ReportDailyMail.filter(x => x.username === username && x.day === day)
        // 插入or修改全局订阅状态
        if (statement.exists.run) {
          statement.map(x => (x.status, x.sendTime)).update(2, System.currentTimeMillis())
        }
    }
  }



  case class ReportDailyMailDomain(appkey: String, username: String,
                                   day: java.sql.Date,
                                   status: Int = 0,
                                   sendTime: Long = 0L,
                                   createTime: Long = 0L)

}
