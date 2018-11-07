package com.sankuai.octo.msgp.dao.report

import java.sql.Date

import com.sankuai.msgp.common.config.DbConnection
import com.sankuai.msgp.common.config.db.msgp.Tables._

import scala.slick.driver.MySQLDriver.simple._

/**
  * Created by yves on 16/10/31.
  */
object ReportDailyStatusDAO {
  private val db = DbConnection.getPool()

  def batchInsert(list: Seq[ReportDailyStatusDomain]) = {
    db withSession {
      implicit session: Session =>
        val seq = list.map(domain => {
          ReportDailyStatusRow(0, domain.owt, domain.appkey, domain.isComputed, domain.date)
        })
        seq.foreach(ReportDailyStatus.insertOrUpdate)
    }
  }

  def insert(domain: ReportDailyStatusDomain) = {
    db withSession {
      implicit session: Session =>
        val row =
          ReportDailyStatusRow(0, domain.owt, domain.appkey, domain.isComputed, domain.date)
        ReportDailyStatus.insertOrUpdate(row)
    }
  }

  def delete(appkey: String, day: java.sql.Date) = {
    db withSession {
      implicit session: Session =>
        val statement =
          ReportDailyStatus.filter { x =>
            x.appkey === appkey &&
              x.day === day
          }
        statement.delete
    }
  }

  def delete(day: java.sql.Date) = {
    db withSession {
      implicit session: Session =>
        val statement =
          ReportDailyStatus.filter { x =>
            x.day === day
          }
        statement.delete
    }
  }

  def delete(appkes: Set[String], day: java.sql.Date) = {
    db withSession {
      implicit session: Session =>
        val statement =
          ReportDailyStatus.filter { x =>
            (x.appkey inSet appkes) &&
              (x.day === day)
          }
        statement.delete
    }
  }


  def get(owt: String, appkey: String) = {
    db withSession {
      implicit session: Session =>
        ReportDailyStatus.filter { x =>
          x.owt === owt &&
            x.appkey === appkey
        }.list
    }
  }

  def count(day: java.sql.Date): Int = {
    db withSession {
      implicit session: Session =>
        ReportDailyStatus.filter { x =>
          x.day === day
        }.length.run
    }
  }

  def search(from: Int, size: Int, day: java.sql.Date) = {
    db withSession {
      implicit session: Session =>
        val statement = ReportDailyStatus.filter { x =>
          x.day === day
        }.list.map(x => (x.owt, x.appkey))
        statement.slice(from, from + size)
    }
  }

  def search(day: java.sql.Date) = {
    db withSession {
      implicit session: Session =>
        ReportDailyStatus.filter { x =>
          x.day === day
        }.list.map(x => (x.owt, x.appkey))
    }
  }

  def searchUncomputed(day: java.sql.Date) = {
    db withSession {
      implicit session: Session =>
        ReportDailyStatus.filter { x =>
          x.day === day &&
            (x.isComputed === 1 || x.isComputed === 0)
        }.list.map(x => (x.owt, x.appkey))
    }
  }

  def searchByOwt(day: java.sql.Date, owt: String) = {
    db withSession {
      implicit session: Session =>
        ReportDailyStatus.filter { x =>
          x.day === day &&
            x.owt === owt
        }.list.map(x => (x.owt, x.appkey))
    }
  }

  /**
    *
    * @param appkey
    * @param day
    * @param status 0表示未计算， 1表示成功计算all接口， 2表示成功计算all接口和*接口, -1表示appkey性能数据为空
    */
  def updateStatus(appkey: String, day: java.sql.Date, status: Int): Unit = {
    db withSession {
      implicit session: Session =>
        val statement = ReportDailyStatus.filter { x =>
          x.appkey === appkey && x.day === day
        }
        statement.map(_.isComputed).update(status)
    }
  }

  /**
    *
    * @param owt
    * @param appkey
    * @param isComputed 0表示未计算， 1表示成功计算all接口， 2表示成功计算all接口和*接口， -1表示appkey性能数据为空
    * @param date
    */
  case class ReportDailyStatusDomain(owt: String, appkey: String, isComputed: Int, date: Date)

}
