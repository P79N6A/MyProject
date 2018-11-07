package com.sankuai.octo.msgp.dao.kpi

import com.sankuai.meituan.auth.util.UserUtils
import com.sankuai.msgp.common.config.DbConnection
import com.sankuai.msgp.common.config.db.msgp.Tables._
import org.slf4j.LoggerFactory

import scala.slick.driver.MySQLDriver.simple._

object ScreenAuthDao {
  private val db = DbConnection.getPool()
  private val logger = LoggerFactory.getLogger(this.getClass)

  def get(appkeys: List[String]): List[AppScreenAuthRow] = {
    db withSession {
      implicit session: Session =>
        AppScreenAuth.filter(_.appkey inSet appkeys).list
    }
  }

  // return (authorisedScreens, unauthorisedScreens)
  def denied(screens: List[AppScreenRow]) = {
    val unauthorisedScreens = try {
      val login = UserUtils.getUser.getLogin
      val appkeys = screens.map(_.appkey)

      val authMap = ScreenAuthDao.get(appkeys).groupBy { x => (x.appkey, x.metric)}
      var unauthorisedScreens = List[AppScreenRow]()
      screens.foreach {
        screen =>
          if (authMap.contains((screen.appkey, screen.metric))) {
            val loginList = authMap.apply((screen.appkey, screen.metric)).map(_.userLogin)
            if (!loginList.contains(login)) {
              // 没权限
              unauthorisedScreens = unauthorisedScreens :+ screen
            }
          }
      }
      unauthorisedScreens
    } catch {
      case e: Exception => logger.info(s"invoke hasAuth failed", e)
        List()
    }
    val unauthorisedIds = unauthorisedScreens.map(_.id)
    val authorisedScreens = screens.filter { x => !unauthorisedIds.contains(x.id)}
    (authorisedScreens, unauthorisedScreens)
  }
}
