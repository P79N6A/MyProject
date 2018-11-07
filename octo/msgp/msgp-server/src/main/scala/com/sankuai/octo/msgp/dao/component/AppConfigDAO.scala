package com.sankuai.octo.msgp.dao.component

import com.sankuai.msgp.common.config.DbConnection
import com.sankuai.msgp.common.config.db.msgp.Tables._
import play.api.libs.json.Json

import scala.slick.driver.MySQLDriver.simple._
import scala.slick.jdbc.{StaticQuery => Q}
import scala.slick.lifted.CanBeQueryCondition

/**
  * Created by yves on 16/11/21.
  */
object AppConfigDAO {

  private val db = DbConnection.getPool()

  case class AppConfigDomain(business: String, owt: String, pdl: String, base: String,
                             groupId: String, artifactId: String, version: String, action: String)
  implicit val appConfigDomainReads = Json.reads[AppConfigDomain]
  implicit val appConfigDomainWrites = Json.writes[AppConfigDomain]

  case class AppConfigExtDomain(appConfigId: Int, business: String, owt: String, pdl: String, app: String)
  implicit val appConfigExtDomainReads = Json.reads[AppConfigExtDomain]
  implicit val appConfigExtDomainWrites = Json.writes[AppConfigExtDomain]


  case class AppConfigSimple(groupId: String = "", artifactId: String = "", version: String = "", action: String = "")

  def insertOrUpdate(appConfig: AppConfigDomain) = {
    db withSession {
      implicit session: Session =>
        val statement = AppConfig.filter { x =>
          x.base === appConfig.base &&
          x.business === appConfig.business &&
          x.owt === appConfig.owt &&
            x.pdl === appConfig.pdl &&
            x.groupId === appConfig.groupId &&
            x.artifactId === appConfig.artifactId
        }
        if (statement.exists.run) {
          AppConfig.map(x => (x.version, x.action)).update(appConfig.version, appConfig.action)
        } else {
          AppConfig += AppConfigRow(0, appConfig.business, appConfig.owt, appConfig.pdl, appConfig.base,
            appConfig.groupId, appConfig.artifactId, appConfig.version, appConfig.action)
        }
    }
  }

  def batchInsert(appConfigs: List[AppConfigDomain]) = {
    db withSession {
      implicit session: Session =>
        appConfigs.foreach {
          appConfig =>
            insertOrUpdate(appConfig)
        }
    }
  }

  def getBlackListConfig(business: String, owt: String, pdl: String, base: String) = {
    db withSession {
      implicit session: Session =>
        AppConfig.filter { x =>
          x.business === business &&
          x.owt === owt &&
            x.pdl === pdl &&
            x.base === base
        }.list
    }
  }

  def getAllBlackListConfig = {
    db withSession {
      implicit session: Session =>
        AppConfig.list
    }
  }

  def getRichConfig(groupId: Option[String], artifactId: Option[String], base: Option[String], business: Option[String], owt: Option[String], pdl: Option[String], action: Option[String]) = {
    db withSession {
      implicit session: Session =>
        val statement = AppConfig.optionFilter(base)(_.base === _)
          .optionFilter(business)(_.business === _)
          .optionFilter(owt)(_.owt === _)
          .optionFilter(pdl)(_.pdl === _)
          .optionFilter(action)(_.action === _)
          .optionFilter(groupId)(_.groupId === _)
          .optionFilter(artifactId)(_.artifactId === _)
        statement.list
    }
  }

  def insertWhiteListConfig(whiteList: AppConfigExtDomain) = {
    db withSession {
      implicit session: Session =>
        AppConfigExt += AppConfigExtRow(0, whiteList.appConfigId, whiteList.business, whiteList.owt, whiteList.pdl, whiteList.app)
    }
  }

  def getWhiteListConfig(appConfigId: Int) = {
    db withSession {
      implicit session: Session =>
       AppConfigExt.filter(_.appConfigId === appConfigId).list
    }
  }

  def deleteBlackListConfig(config: AppConfigDomain) = {
    db withSession {
      implicit session: Session =>
        val statement =
          AppConfig.filter { x =>
            x.base === config.base &&
              x.business === config.business &&
              x.owt === config.owt &&
              x.pdl === config.pdl &&
              x.groupId === config.groupId &&
              x.artifactId === config.artifactId &&
            x.action === config.action
          }
        if (statement.exists.run) {
          val appConfigId = statement.list.head.id
          //删除白名单配置
          AppConfigExt.filter(_.appConfigId === appConfigId).delete

          //删除黑名单
          statement.delete
        }
    }
  }

  def deleteWhiteListConfig(whiteList: AppConfigExtDomain) = {
    db withSession {
      implicit session: Session =>
        val statement =
          AppConfigExt.filter{x=>
            x.appConfigId === whiteList.appConfigId &&
            x.business === whiteList.business &&
            x.owt === whiteList.owt &&
            x.pdl === whiteList.pdl &&
            x.app === whiteList.app
          }
        if (statement.exists.run) {
          statement.delete
        }
    }
  }

  implicit class QueryHelper[T, E](query: Query[T, E, Seq]) {
    def optionFilter[X, R: CanBeQueryCondition](name: Option[X])(f: (T, X) => R) =
      name.map(v => query.withFilter(f(_, v))).getOrElse(query)
  }
}
