package com.sankuai.msgp.common.dao.appkey

import com.sankuai.msgp.common.config.DbConnection
import com.sankuai.msgp.common.config.db.msgp.Tables._

import scala.slick.driver.MySQLDriver.simple._

object AppkeyAliasDao {
  private val db = DbConnection.getPool()

  /**
    * 获取所有appkey
    * @return
    */
  def getAllAppkeyAlias = {
    db withSession {
      implicit session: Session =>
        AppAlias.list.map(_.errorLogAppkey).distinct
    }
  }

  def getAppkeyByAlias(alias: String) = {
    db withSession {
      implicit session: Session =>
        val appkey = AppAlias.filter(x => x.errorLogAppkey === alias).map(_.appkey).first
        appkey
    }
  }
}
