package com.sankuai.msgp.common.dao.appkey

import com.sankuai.msgp.common.config.DbConnection
import com.sankuai.msgp.common.config.db.msgp.Tables._

import scala.slick.driver.MySQLDriver.simple._

object AppkeyDescDao {
  private val db = DbConnection.getPool()

  def getAppkeyDesc(appkey: String) = {
    db withSession {
      implicit session: Session =>
        val statement = AppkeyDesc.filter(x => x.appkey === appkey)
        statement.firstOption
    }
  }

  /**
    * 获取所有appkey
    * @return
    */
  def getAllAppkey = {
    db withSession {
      implicit session: Session =>
        AppkeyDesc.list.map(_.appkey).distinct
    }
  }
}
