package com.sankuai.octo.msgp.dao.kpi

import com.sankuai.msgp.common.config.DbConnection
import com.sankuai.msgp.common.utils.helper.JsonHelper
import com.sankuai.msgp.common.config.db.msgp.Tables._
import com.sankuai.octo.msgp.serivce.monitor.business.KpiMonitorModel
import org.joda.time.DateTime

import scala.collection.JavaConverters._
import scala.slick.driver.MySQLDriver.simple._

object AppScreenDao {
  private val db = DbConnection.getPool()

  def wrapper(id: Long = 0, category: String, appkey: String, endpoint: String, serverNode: String,
              title: String, metric: String, sampleMode: String, auth: Int, updateTime: Long) = {
    AppScreenRow(id, category, appkey, Some(endpoint), serverNode, title, metric, sampleMode, auth, updateTime)
  }

  def insertOrUpdate(row: AppScreenRow): Option[Long] = {
    db withSession {
      implicit session: Session =>
        // 同步报警基线
        val now = new DateTime()
        val start = (now.withTimeAtStartOfDay().getMillis / 1000).toInt
        val end = start + 24 * 60 * 60 - 1
        KpiMonitorModel.submitUpdateBase((row, start, end))
        (AppScreen returning AppScreen.map(_.id)).insertOrUpdate(row)
    }
  }

  def insert(row: AppScreenRow) = {
    db withSession {
      implicit session: Session =>
        // 同步报警基线
        val now = new DateTime()
        val start = (now.withTimeAtStartOfDay().getMillis / 1000).toInt
        val end = start + 24 * 60 * 60 - 1
        KpiMonitorModel.submitUpdateBase((row, start, end))
        (AppScreen returning AppScreen.map(_.id)).insert(row)
    }
  }

  def wrapperInsertOrUpdate(row: AppScreenRow) = {
    val authorisedScreens = ScreenAuthDao.denied(List(row))._1
    if (authorisedScreens.nonEmpty) {
      JsonHelper.dataJson(insertOrUpdate(row))
    } else {
      JsonHelper.errorJson(s"对指标 ${row.appkey} ${row.metric} 无权限")
    }
  }

  def get(appkey: String, id: Long) = {
    db withSession {
      implicit session: Session =>
        if (id != 0) {
          AppScreen.filter(_.id === id).list
        } else {
          AppScreen.filter(_.appkey === appkey).list
        }
    }
  }

  def get(id: Long) = {
    db withSession {
      implicit session: Session =>
        AppScreen.filter(_.id === id).list
    }
  }

  def get(ids: List[Long]) = {
    db withSession {
      implicit session: Session =>
        AppScreen.filter(_.id inSet ids).list
    }
  }

  def get = {
    db withSession {
      implicit session: Session =>
        AppScreen.list.sortBy(_.title)
    }
  }

  def getFrontend() = {
    get.asJava
  }

  def getKpiMetrics = {
    get.filter(_.metric.startsWith("kpi."))
  }

  def getNotKpiMetrics = {
    val list = BusinessDashDao.getAllIds
    get(list).filterNot(_.metric.startsWith("kpi."))
  }

  def authGet(appkey: String, id: Long) = {
    val tmp = get(appkey, id)

    val (authorisedScreens, unauthorisedScreens) = ScreenAuthDao.denied(tmp)
    Map("authorisedScreens" -> authorisedScreens, "unauthorisedScreens" -> unauthorisedScreens)
  }

  def authGet(ids: List[Long]): List[AppScreenRow] = {
    val tmp = db withSession {
      implicit session: Session =>
        AppScreen.filter(_.id inSet ids).list
    }
    val ret = ScreenAuthDao.denied(tmp)
    ret._1
  }

  def delete(id: Long) = {
    db withSession {
      implicit session: Session =>
        AppScreen.filter(_.id === id).delete
        BusinessDashDao.delete(id)
    }
  }

  def dashDelete(id: Long) = {
    db withSession {
      implicit session: Session =>
        val appScreen = AppScreen.filter(_.id === id).list.headOption
        if (appScreen.nonEmpty) {
          if (appScreen.get.category != "appkey") {
            AppScreen.filter(_.id === id).delete
          }
          BusinessDashDao.delete(id)
        }
    }
  }
}
