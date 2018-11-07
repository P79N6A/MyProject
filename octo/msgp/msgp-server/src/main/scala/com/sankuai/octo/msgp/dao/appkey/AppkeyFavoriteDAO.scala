package com.sankuai.octo.msgp.dao.appkey

import com.sankuai.msgp.common.config.DbConnection
import com.sankuai.msgp.common.config.db.msgp.Tables._
import com.sankuai.msgp.common.model.ServiceModels

import scala.slick.driver.MySQLDriver.simple._
import scala.slick.jdbc.{StaticQuery => Q}

/**
  * Created by yves on 17/3/21.
  */
object AppkeyFavoriteDao {
  private val db = DbConnection.getPool()

  def getAppkeys(user: ServiceModels.User) ={
    db withSession{
      implicit session: Session =>
        AppkeyFavorite.filter(_.username === user.login).map(_.appkey).list
    }
  }

  def batchInsert(user: ServiceModels.User, appkeys: List[String]) ={
    db withSession{
      implicit session: Session =>
       appkeys.foreach{
          appkey =>
            AppkeyFavorite.insertOrUpdate(AppkeyFavoriteRow(0, user.login, appkey))
        }
    }
  }

  def delete(user: ServiceModels.User, appkey: String) ={
    db withSession{
      implicit session: Session =>
        val statement = AppkeyFavorite.filter{x=>
          x.username === user.login &&
            x.appkey === appkey}
        if (statement.exists.run) {
          statement.delete
        }
    }
  }
}
