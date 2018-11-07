package com.sankuai.octo.msgp.dao.component

import com.sankuai.msgp.common.config.DbConnection
import com.sankuai.msgp.common.config.db.msgp.Tables._

import scala.slick.driver.MySQLDriver.simple._
import scala.slick.jdbc.{StaticQuery => Q}

/**
  * Created by yves on 16/11/2.
  * 操作 app_admin表, 此表保存了发布项负责人
  */


object AppAdminDAO {

  private val db = DbConnection.getPool()

  def getAdminsUserId(app: String) = {
    db withSession {
      implicit session: Session =>
        AppAdmin.filter { x => x.appName === app }.map(_.adminsId).firstOption
    }
  }

  def getAdminsUsername(app: String) = {
    db withSession {
      implicit session: Session =>
        AppAdmin.filter { x => x.appName === app }.map(_.adminsName).firstOption
    }
  }

  def getAllAdmins = {
    db withSession {
      implicit session: Session =>
        AppAdmin.list.map(_.adminsName).distinct
    }
  }

  def updateUserInfo(appAdmins: AppAdminDomain) = {
    db withSession {
      implicit session: Session =>
        val statement = AppAdmin.filter{x => x.appName === appAdmins.app_name}
        statement.map(_.adminsId).update(appAdmins.admins_id)
    }
  }

  case class AppAdminDomain(app_name: String, admins_name: String, admins_id: String)
}
