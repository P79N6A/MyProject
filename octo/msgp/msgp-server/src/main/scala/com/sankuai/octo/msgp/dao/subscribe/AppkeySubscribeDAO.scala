package com.sankuai.octo.msgp.dao.subscribe

import java.util.Date

import com.sankuai.msgp.common.config.DbConnection
import com.sankuai.msgp.common.model.SubscribeStatus
import com.sankuai.msgp.common.config.db.msgp.Tables._
import com.sankuai.octo.msgp.model.SubsStatus

import scala.slick.driver.MySQLDriver.simple._

/**
  * Created by yves on 17/4/21.
  */
object AppkeySubscribeDAO {
  private val db = DbConnection.getPool()

  def getSubscribedDailyReport(username: String, reportSubscribed: Byte) = {
    db withSession {
      implicit session: Session =>
        AppkeySubscribe.filter(x =>
          x.username === username &&
            x.dailyReport === reportSubscribed
        ).list.map(_.appkey)
    }
  }

  def getSubscribedWeeklyReport(username: String, reportSubscribed: Byte) = {
    db withSession {
      implicit session: Session =>
        AppkeySubscribe.filter(x =>
          x.username === username &&
            x.weeklyReport === reportSubscribed
        ).list.map(_.appkey)
    }
  }

  def getSubscribedStatusByUser(username: String) = {
    db withSession {
      implicit session: Session =>
        AppkeySubscribe.filter(x =>
          x.username === username && (x.weeklyReport === SubscribeStatus.subscribed.getStatus.toByte
            || x.dailyReport == SubscribeStatus.subscribed.getStatus.toByte)
        ).list
    }
  }

  def getSubscribedUserForReport(reportType: String) = {
    db withSession {
      implicit session: Session =>
        reportType match {
          case "daily" =>
            AppkeySubscribe.filter(x => x.dailyReport === SubscribeStatus.subscribed.getStatus.toByte)
              .list.map(_.username).distinct
          case "weekly" =>
            AppkeySubscribe.filter(x => x.weeklyReport === SubscribeStatus.subscribed.getStatus.toByte)
              .list.map(_.username).distinct
        }
    }
  }

  def insertOrUpdateReport(domain: AppkeySubscribeDomain) = {
    db withSession {
      implicit session: Session =>

        val statement = AppkeySubscribe.filter { x =>
          x.username === domain.username &&
            x.appkey === domain.appkey
        }
        if (statement.exists.run) {
          statement.map(x => (x.dailyReport, x.weeklyReport, x.updateTime))
            .update(domain.dailyReport, domain.weeklyReport, domain.update_time)
        } else {
          val row = AppkeySubscribeRow(0, domain.username, domain.appkey, domain.dailyReport, domain.weeklyReport,
            domain.create_time, domain.update_time)
          AppkeySubscribe.insert(row)
        }
    }
  }

  def batchInsertOrUpdate(list: Seq[AppkeySubscribeDomain]) = {
    db withSession {
      implicit session: Session =>
        val seq = list.map(domain => {
          AppkeySubscribeRow(0, domain.username, domain.appkey, domain.dailyReport, domain.weeklyReport,
            domain.create_time, domain.update_time)
        })
        seq.foreach(AppkeySubscribe.insertOrUpdate)
    }
  }

  case class AppkeySubscribeDomain(username: String, appkey: String, dailyReport: Byte, weeklyReport: Byte,
                                   create_time: Long, update_time: Long)

  //clear

  def getAllSubsribedUser() = {
    db withSession {
      implicit session: Session =>
        AppkeySubscribe.filter(x => x.dailyReport === SubscribeStatus.subscribed.getStatus.toByte || x.weeklyReport === SubscribeStatus.subscribed.getStatus.toByte)
          .list.map(_.username).distinct
    }
  }

  def updateReport(user: String) = {
    db withSession {
      implicit session: Session =>
        val statement = AppkeySubscribe.filter {
          x =>
            x.username === user
        }
        if (statement.exists.run) {
          statement.map(x => (x.dailyReport, x.weeklyReport, x.updateTime))
            .update(SubscribeStatus.not_subscribed.getStatus.toByte, SubscribeStatus.not_subscribed.getStatus.toByte, new Date().getTime / 1000)
        }
    }
  }

}
