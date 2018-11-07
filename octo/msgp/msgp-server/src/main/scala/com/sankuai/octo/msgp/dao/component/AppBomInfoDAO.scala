package com.sankuai.octo.msgp.dao.component

import com.sankuai.msgp.common.config.DbConnection
import com.sankuai.msgp.common.config.db.msgp.Tables._
import com.sankuai.octo.msgp.dao.component.ComponentDAO.{AppDescDomain, SimpleArtifact}
import play.api.libs.json.Json

import scala.slick.driver.MySQLDriver.simple._
import scala.slick.jdbc.{StaticQuery => Q}

/**
  * Created by yves on 16/11/16.
  */
object AppBomInfoDAO {

  private val db = DbConnection.getPool()

  case class AppBomInfoDomain(business: String, owt: String, pdl: String,
                              app: String, appkey: String, base: String, appGroupId: String, appArtifactId: String, appVersion: String, appPackaging: String,
                              infBomUsed: Int, infBomVersion: String, xmdBomUsed: Int, xmdBomVersion: String,
                              createTime: Long, uploadTime: Long)

  implicit val appBomInfoDomainReads = Json.reads[AppBomInfoDomain]
  implicit val appBomInfoDomainWrites = Json.writes[AppBomInfoDomain]

  case class AppBomInfoUpload(appDesc: AppDescDomain,
                              infBomUsed: Int, infBomVersion: String,
                              xmdBomUsed: Int, xmdBomVersion: String)

  implicit val appBomInfoUploadReads = Json.reads[AppBomInfoUpload]
  implicit val appBomInfoUploadWrites = Json.writes[AppBomInfoUpload]

  def insertOrUpdate(bomInfo: AppBomInfoDomain) = {
    db withSession {
      implicit session: Session =>
        val now = System.currentTimeMillis()
        val statement = AppBom.filter(
          x =>
            x.appGroupId === bomInfo.appGroupId && x.appArtifactId === bomInfo.appArtifactId)
        if (statement.exists.run) {
          // update
          statement.map(x => (x.business, x.owt, x.pdl, x.app, x.appkey, x.base, x.appVersion, x.appPackaging, x.infBomUsed, x.infBomVersion, x.xmdBomUsed, x.xmdBomVersion, x.uploadTime))
            .update(bomInfo.business, bomInfo.owt, bomInfo.pdl, bomInfo.app, bomInfo.appkey, bomInfo.base, bomInfo.appVersion, bomInfo.appPackaging, bomInfo.infBomUsed, bomInfo.infBomVersion, bomInfo.xmdBomUsed, bomInfo.xmdBomVersion, now)
        } else {
          //insert
          AppBom += AppBomRow(0, bomInfo.business, bomInfo.owt, bomInfo.pdl,
            bomInfo.app, bomInfo.appkey, bomInfo.base, bomInfo.appGroupId, bomInfo.appArtifactId, bomInfo.appVersion, bomInfo.appPackaging,
            bomInfo.infBomUsed, bomInfo.infBomVersion, bomInfo.xmdBomUsed, bomInfo.xmdBomVersion,
            now, now)
        }
    }
  }

  def getAllApp = {
    db withSession {
      implicit session: Session =>
        AppBom.list.map{
          x=>
            SimpleArtifact(x.appGroupId, x.appArtifactId, x.appVersion)
        }.distinct
    }
  }

  def deleteArtifact(artifact: SimpleArtifact) = {
    db withSession {
      implicit session: Session =>
        val statement = AppBom.filter{x=>
          x.appGroupId === artifact.groupId &&
            x.appArtifactId === artifact.artifactId
        }
        if(statement.exists.run){
          statement.delete
        }
    }
  }
}
