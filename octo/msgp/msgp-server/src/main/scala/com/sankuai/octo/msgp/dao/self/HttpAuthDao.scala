package com.sankuai.octo.msgp.dao.self

import java.util.Date

import com.sankuai.msgp.common.config.DbConnection
import com.sankuai.msgp.common.config.db.msgp.Tables.{MnsapiAuth, _}
import org.slf4j.{Logger, LoggerFactory}

import scala.slick.driver.MySQLDriver.simple._

object HttpAuthDao {
  val LOG: Logger = LoggerFactory.getLogger(HttpAuthDao.getClass)

  private val db = DbConnection.getPool()

  def insertOrUpdateAuth(username: String, token: String, owtPattern: String, appkeyPattern: String) = {
    LOG.info(s"insert HttpAuth value: $username, $token, $owtPattern, $appkeyPattern")
    val updateTime = new java.sql.Timestamp(new Date().getTime)
    db withSession{
      implicit session: Session =>
        val statement = MnsapiAuth.filter(_.username === username)
        if (statement.exists.run) {
          // update
          statement.map(x => (x.username, x.token, x.owtPattern, x.appkeyPattern, x.updateTime))
            .update(username, token, owtPattern, appkeyPattern, updateTime)
        } else {
          MnsapiAuth += MnsapiAuthRow(0, username, token, owtPattern, appkeyPattern, updateTime)
        }

    }
  }

  def getAllItems = {
    db withSession {
      implicit session: Session =>
        MnsapiAuth.list
    }
  }

  def getAllUsernames = {
    db withSession {
      implicit session: Session =>
        MnsapiAuth.map(_.username).list
    }
  }

  def getAuthItem(authID: String) = {
    db withSession{
      implicit session: Session =>
        MnsapiAuth.filter(_.id === authID.toInt).first
    }
  }

  def delete(authID: String) ={
    db withSession{
      implicit session: Session =>
        val statement = MnsapiAuth.filter{x=>
          x.id === authID.toInt}
        if (statement.exists.run) {
          statement.delete
        } else {
          -1
        }
    }
  }
}
