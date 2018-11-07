package com.sankuai.octo.msgp.dao.common

/**
  * Created by yves on 16/10/25.
  */

import com.sankuai.msgp.common.config.DbConnection
import com.sankuai.msgp.common.config.db.msgp.Tables._

import scala.slick.driver.MySQLDriver.simple._
import scala.slick.jdbc.{StaticQuery => Q}

object BannerDAO {
  private val db = DbConnection.getPool()

  case class BannerDomain(messageType: Int, messageTitle: String, messageBody: String, operator: String, expired: Boolean, create_time: Long = 0)

  def deleteMessage(record: BannerDomain) = {
    db withSession {
      implicit session: Session =>
        val statement = Banner.filter { x =>
          x.messageType === record.messageType &&
            x.messageTitle === record.messageTitle &&
            x.messageBody === record.messageBody
        }
        statement.delete
    }
  }

  def updateMessage(record: BannerDomain) = {
    db withSession {
      implicit session: Session =>
        val statement = Banner.filter { x =>
          x.messageType === record.messageType &&
            x.messageTitle === record.messageTitle &&
            x.messageBody === record.messageBody
        }
        statement.map(x => x.expired).update(true)
    }
  }

  def insertMessage(record: BannerDomain) = {
    db withSession {
      implicit session: Session =>
        val statement = Banner.filter { x =>
          x.messageType === record.messageType &&
            x.messageTitle === record.messageTitle &&
            x.messageBody === record.messageBody
        }
        if (!statement.exists.run) {
          Banner += BannerRow(0, record.messageType, record.messageTitle, record.messageBody, record.operator, record.expired, record.create_time)
        }
    }
  }

  def getValidMessage = {
    db withSession {
      implicit session: Session =>
        Banner.filter(_.expired === false).list
    }
  }

  def getAllMessage = {
    db withSession {
      implicit session: Session =>
        Banner.list
    }
  }
}
