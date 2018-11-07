package com.sankuai.octo.msgp.serivce

import com.sankuai.msgp.common.config.DbConnection
import com.sankuai.msgp.common.config.db.msgp.Tables.{UserShortcut, _}
import com.sankuai.msgp.common.utils.client.TairClient
import com.sankuai.octo.msgp.model.DashboardDomain.{Dashboard, Shortcut}
import org.slf4j.{Logger, LoggerFactory}
import play.api.libs.json.Json

import scala.collection.JavaConverters._
import scala.slick.driver.MySQLDriver.simple._

object Setting {
  val LOG: Logger = LoggerFactory.getLogger(Setting.getClass)
  private val db = DbConnection.getPool()

  def getDashboard(login: String): Dashboard = {
    try {
      val key = s"$login.dashboard"
      TairClient.get(key).getOrElse(Dashboard("/"))
      TairClient.getValue(key).fold {
        Dashboard("/")
      } {
        x => Json.parse(x).validate[Dashboard].get
      }
    } catch {
      case e: Exception => LOG.error(s"getDashboard exception $e"); Dashboard("/")
    }
  }

  def setDashboard(login: String, json: String): Dashboard = {
    println(json)
    val dashboard = Json.parse(json).validate[Dashboard].get
    setDashboard(login, dashboard)
  }

  def setDashboard(login: String, dashboard: Dashboard): Dashboard = {
    val key = s"$login.dashboard"
    TairClient.put(key, dashboard)
    dashboard
  }

  /** using by postHandle */
  def getShortcutList(login: String) = {
    getUSR(login).map(
      x =>
        Map("id" -> x.id.toString,
          "title" -> x.title,
          "url" -> x.url,
          "appkey" -> x.appkey).asJava
    ).asJava
  }

  def getShortcuts(login: String): List[Shortcut] = {
    val key = s"$login.shortcuts"
    TairClient.getValue(key).fold {
      List[Shortcut]()
    } {
      x => Json.parse(x).validate[List[Shortcut]].get
    }
  }

  def addShortcut(login: String, json: String) = {
    LOG.info(json)
    val shortcut = Json.parse(json).validate[Shortcut].get
    val userSR = UserShortcutRow(0, login, shortcut.title, shortcut.url, shortcut.appkey)
    insertUSR(userSR)
    getUSR(login)
  }

  def delShortcut(login: String, id: Int) = {
    deleteUSR(id)
    getUSR(login)
  }

  def insertUSR(userSR: UserShortcutRow) = {
    db withSession {
      implicit session: Session =>
        val id = (UserShortcut returning UserShortcut.map(_.id)) += userSR
        id
    }
  }

  def getUSR(login: String) = {
    db withSession {
      implicit session: Session =>
        UserShortcut.filter(x => x.login === login).list
    }
  }

  def deleteUSR(id: Int) = {
    db withSession {
      implicit session: Session =>
        val statement = UserShortcut.filter(x => x.id === id.toLong)
        statement.delete
    }
  }
}
