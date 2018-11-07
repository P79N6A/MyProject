package com.sankuai.octo.msgp.dao.component

import java.sql.Date

import com.sankuai.msgp.common.config.DbConnection
import com.sankuai.msgp.common.config.db.msgp.Tables._
import com.sankuai.octo.msgp.dao.component.ComponentDAO.SimpleArtifact
import com.sankuai.msgp.common.model.Base
import com.sankuai.msgp.common.utils.helper.SqlParser

import scala.slick.driver.MySQLDriver.simple._
import scala.slick.jdbc.StaticQuery.interpolation
import scala.slick.jdbc.{GetResult, StaticQuery => Q}

/**
  * Created by yves on 16/9/27.
  */


object ActivenessDAO {
  private val db = DbConnection.getPool()

  case class AppActivenessDomain(base: String, business: String, owt: String, pdl: String, app: String, appGroupId: String, appArtifactId: String, appVersion: String, appkey: String, count: Int, date: Date)

  implicit val getAppActivenessDomainResult = GetResult(r => AppActivenessDomain(r.<<, r.<<, r.<<, r.<<, r.<<, r.<<, r.<<, r.<<, r.<<, r.<<, r.<<))


  /**
    * 获取应用的活跃度
    *
    */
  def getAppActiveness(base: String, business: String, owt: String, pdl: String, start: Date, end: Date) = {
    //根据base不同选择不同的查询范围
    val parameterMap = if (base.equalsIgnoreCase(Base.all.getName)) {
      Map("business" -> SqlParser.ValueExpress(business, "="), "owt" -> SqlParser.ValueExpress(owt, "="), "pdl" -> SqlParser.ValueExpress(pdl, "="))
    } else {
      Map("base" -> SqlParser.ValueExpress(base, "="), "business" -> SqlParser.ValueExpress(business, "="), "owt" -> SqlParser.ValueExpress(owt, "="), "pdl" -> SqlParser.ValueExpress(pdl, "="))
    }
    val prefixSQL = s"SELECT base, business, owt, pdl, app, app_group_id as appGroupId, app_artifact_id as appArtifactId, app_version as appVersion, appkey, count, date FROM app_activeness WHERE date BETWEEN '$start' AND '$end'"
    val suffixSQL = ""
    val sqlStr = SqlParser.sqlParser(prefixSQL, parameterMap, suffixSQL)
    db withSession {
      implicit session: Session =>
        sql"""#$sqlStr""".as[AppActivenessDomain].list
    }
  }

  def insertOrUpdate(base: String, business: String, owt: String, pdl: String, app: String, appGroupId: String, appArtifactId: String, appVersion: String, appkey: String, date: Date) = {
    db withSession {
      implicit session: Session =>
        val statement = AppActiveness.filter(x => x.appGroupId === appGroupId && x.appArtifactId === owt && x.date === date)
        if (statement.exists.run) {
          val currentCount = statement.list.map(_.count).headOption.getOrElse(0)
          statement.map(_.count).update(currentCount + 1)
        } else {
          AppActiveness += AppActivenessRow(0, base, business, owt, pdl, app, appGroupId, appArtifactId, appVersion, appkey, 1, date)
        }
    }
  }

  /**
    * 删除一个发布项所有的依赖
    * artifact = App artifact是Maven中的标准用法
    * @param artifact 发布项
    * @return 无
    */
  def deleteArtifact(artifact: SimpleArtifact) = {
    db withSession {
      implicit session: Session =>
        val statement = AppActiveness.filter{x=>
          x.appGroupId === artifact.groupId &&
            x.appArtifactId === artifact.artifactId
        }
        if(statement.exists.run){
          statement.delete
        }
    }
  }
}