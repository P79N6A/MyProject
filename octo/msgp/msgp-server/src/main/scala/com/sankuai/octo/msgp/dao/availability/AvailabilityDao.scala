package com.sankuai.octo.msgp.dao.availability

import com.sankuai.msgp.common.config.DbConnection
import com.sankuai.msgp.common.config.db.msgp.Tables._
import org.joda.time.DateTime

import scala.collection.JavaConversions._
import scala.slick.driver.MySQLDriver.simple._
import scala.slick.jdbc.{StaticQuery => Q}

object AvailabilityDao {
  private val db = DbConnection.getPool()

  def batchInsertAvailability(rows: List[AvailabilityDayReportRow]) = {
    db withSession {
      implicit session: Session =>
        rows.foreach {
          row =>
            AvailabilityDayReport.insertOrUpdate(row)
        }
    }
  }

  def fetchAvailability(appkeys: java.util.List[String], day: DateTime) = {
    db withSession {
      implicit session: Session =>
        val ts = (day.withTimeAtStartOfDay().getMillis / 1000).toInt
        AvailabilityDayReport.filter(x => (x.appkey inSet appkeys) && x.ts === ts).list
    }
  }

  def fetchAvailabilitySingle(appkey: String, spanname: String, day: DateTime) = {
    db withSession {
      implicit session: Session =>
        val ts = (day.withTimeAtStartOfDay().getMillis / 1000).toInt
        AvailabilityDayReport.filter(x => x.appkey === appkey  && x.spanname === spanname && x.ts === ts).firstOption
    }
  }

  def fetchAvailabilityMerged(appkey: String, day: DateTime) = {
    db withSession {
      implicit session: Session =>
        val ts = (day.withTimeAtStartOfDay().getMillis / 1000).toInt
        AvailabilityDayReport.filter(x => x.appkey === appkey  && x.spanname === "all" && x.ts === ts).firstOption
    }
  }

  def fetchAvailabilityTrend(appkey: String, spanname: String, day: DateTime) = {
    db withSession {
      implicit session: Session =>
        val ts = (day.withTimeAtStartOfDay().minusDays(30).getMillis / 1000).toInt
        AvailabilityDayReport.filter(x => x.appkey === appkey  && x.spanname === spanname && x.ts >= ts).list
    }
  }

  def fetchWeeklyAvailability(appkeys: List[String], spanname: String, start: DateTime, end: DateTime) = {
    db withSession {
      implicit session: Session =>
        val start_ts = (start.getMillis / 1000).toInt
        val end_ts = (end.getMillis / 1000).toInt
        AvailabilityDayReport.filter(x => (x.appkey inSet appkeys)  && x.spanname === spanname && x.ts >= start_ts && x.ts <= end_ts).list
    }
  }

  def fetchDailyAvailability(appkey: String, spanname: String, start: DateTime, end: DateTime) = {
    db withSession {
      implicit session: Session =>
        val start_ts = (start.getMillis / 1000).toInt
        val end_ts = (end.getMillis / 1000).toInt
        AvailabilityDayReport.filter(x => x.appkey === appkey  && x.spanname === spanname && x.ts >= start_ts && x.ts <= end_ts).list
    }
  }

  def batchInsertAvailabilityDetails(rows: List[AvailabilityDayDetailRow]) = {
    db withSession {
      implicit session: Session =>
        rows.foreach {
          row =>
            AvailabilityDayDetail.insertOrUpdate(row)
        }
    }
  }

  def fetchAvailabilityDetails(appkey: String, spanName: String, day: DateTime) = {
    db withSession {
      implicit session: Session =>
        val ts = (day.withTimeAtStartOfDay().getMillis / 1000).toInt
        if (spanName.equalsIgnoreCase("all")) {
          AvailabilityDayDetail.filter(x => x.appkey === appkey && x.spanname =!= spanName && x.ts === ts && x.failureCount > 0L).list
        } else {
          AvailabilityDayDetail.filter(x => x.appkey === appkey && x.spanname === spanName && x.ts === ts && x.failureCount > 0L).list
        }
    }
  }

  /***
    * 获取当天有效的Appkey&Spanname
    * @param ts 当天实际
    * @return
    */
  def fetchDailySpans(ts: Long) ={
    db withSession {
      implicit session: Session =>
        AvailabilityDayReport.filter{x=> x.ts === ts.toInt}.list
    }
  }
}
