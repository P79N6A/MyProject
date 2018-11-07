package com.sankuai.octo.msgp.dao.component

import com.sankuai.msgp.common.config.DbConnection
import com.sankuai.msgp.common.config.db.msgp.Tables._
import com.sankuai.msgp.common.model.Base
import com.sankuai.msgp.common.utils.StringUtil
import com.sankuai.msgp.common.utils.helper.SqlParser
import org.slf4j.{Logger, LoggerFactory}
import play.api.libs.json.Json

import scala.slick.driver.MySQLDriver.simple._
import scala.slick.jdbc.StaticQuery.interpolation
import scala.slick.jdbc.{GetResult, StaticQuery => Q}

/**
  * Created by yves on 16/8/8.
  */
object ComponentDAO {
  private final val LOG: Logger = LoggerFactory.getLogger(ComponentDAO.getClass)
  private val db = DbConnection.getPool()

  case class SimpleArtifact(groupId: String, artifactId: String, version: String)

  implicit val artifactReads = Json.reads[SimpleArtifact]
  implicit val artifactWrites = Json.writes[SimpleArtifact]

  case class AppDescDomain(business: String, owt: String, pdl: String, app: String, appkey: String, base: String, groupId: String, artifactId: String, version: String, packaging: String)

  implicit val getAppDescDomainResult = GetResult(r => AppDescDomain(r.<<, r.<<, r.<<, r.<<, r.<<, r.<<, r.<<, r.<<, r.<<, r.<<))

  implicit val appDescDomainReads = Json.reads[AppDescDomain]
  implicit val appDescDomainWrites = Json.writes[AppDescDomain]

  implicit val getSimpleArtifactResult = GetResult(r => SimpleArtifact(r.<<, r.<<, r.<<))

  case class ComponentVersionCount(version: String, count: Int)

  implicit val getComponentVersionResult = GetResult(r => ComponentVersionCount(r.<<, r.<<))

  case class ComponentVersionDetails(base: String, business: String, owt: String, pdl: String, appkey: String, appGroupId: String, appArtifactId: String, version: String)

  implicit val getComponentVersionUserCountResult = GetResult(r => ComponentVersionDetails(r.<<, r.<<, r.<<, r.<<, r.<<, r.<<, r.<<, r.<<))

  case class ComponentDetails(base: String, business: String, owt: String, pdl: String, appkey: String, appGroupId: String, appArtifactId: String, groupId: String, artifactId: String, version: String, uploadTime: Long)

  implicit val getComponentDetailsResult = GetResult(r => ComponentDetails(r.<<, r.<<, r.<<, r.<<, r.<<, r.<<, r.<<, r.<<, r.<<, r.<<, r.<<))

  case class AppkeyDependency(appkey: String, app: String, groupId: String, artifactId: String, version: String)

  implicit val getAppkeyDependencyResult = GetResult(r => AppkeyDependency(r.<<, r.<<, r.<<, r.<<, r.<<))

  case class DependencyUpload(appDesc: AppDescDomain, artifacts: List[SimpleArtifact])

  implicit val dependencyReads = Json.reads[DependencyUpload]
  implicit val dependencyWrites = Json.writes[DependencyUpload]


  def insertOrUpdate(appDesc: AppDescDomain, artifact: SimpleArtifact, category: String, now: Long) = {
    db withSession {
      implicit session: Session =>
        val statement = AppDependency.filter(
          x =>
            x.appGroupId === appDesc.groupId && x.appArtifactId === appDesc.artifactId
              && x.groupId === artifact.groupId && x.artifactId === artifact.artifactId)
        if (statement.exists.run) {
          // update
          statement.map(x => (x.base, x.business, x.owt, x.pdl, x.app, x.appkey, x.appVersion, x.version, x.uploadTime))
            .update(appDesc.base, appDesc.business, appDesc.owt, appDesc.pdl, appDesc.app,
              appDesc.appkey, appDesc.version, artifact.version, now)
        } else {
          //insert
          AppDependency += AppDependencyRow(0, appDesc.base, appDesc.business, appDesc.owt, appDesc.pdl,
            appDesc.app, appDesc.groupId, appDesc.artifactId, appDesc.version, appDesc.appkey,
            artifact.groupId, artifact.artifactId, artifact.version, category, now, now)
        }
    }
  }

  /**
    * 删除一个发布项所有的依赖
    * artifact = App artifact是Maven中的标准用法
    *
    * @param artifact 发布项
    * @return 无
    */
  def deleteArtifact(artifact: SimpleArtifact) = {
    db withSession {
      implicit session: Session =>
        val statement = AppDependency.filter { x =>
          x.appGroupId === artifact.groupId &&
            x.appArtifactId === artifact.artifactId
        }
        if (statement.exists.run) {
          statement.delete
        }
    }
  }

  def deleteItem(id: Long) = {
    db withSession {
      implicit session: Session =>
        val statement = AppDependency.filter { x =>
          x.id === id
        }
        if (statement.exists.run) {
          statement.delete
        }
    }
  }

  /**
    * 检测发布项是否被作为依赖使用
    *
    * @param artifact 发布项
    * @return 有效依赖记录
    */
  def isDependency(artifact: SimpleArtifact) = {
    db withSession {
      implicit session: Session =>
        AppDependency.filter { x =>
          x.groupId === artifact.groupId &&
            x.artifactId === artifact.artifactId
        }.list.headOption
    }
  }

  /**
    * 根据关键字获取groupId
    *
    */

  def getGroupIdByKeyword(keyword: String) = {
    val keywordReg = s"%$keyword%"
    db withSession {
      implicit session: Session =>
        AppDependency.filter(_.groupId like keywordReg).list.map(_.groupId)
    }
  }

  /**
    * 根据关键字获取artifactId
    *
    */
  def getArtifactIdByKeyword(groupId: String, keyword: String) = {
    val keywordReg = s"%$keyword%"
    db withSession {
      implicit session: Session =>
        AppDependency.filter(_.groupId === groupId).filter(_.artifactId like keywordReg).list.map(_.artifactId)
    }
  }

  /**
    * 查询组件在app_dependency中存在的所有版本
    *
    */
  def getVersion(groupId: String, artifactId: String) = {
    db withSession {
      implicit session: Session =>
        val statement = AppDependency.filter {
          x =>
            x.groupId === groupId && x.artifactId === artifactId
        }
        statement.map(_.version).list.distinct
    }
  }

  def getOwt(business: String) = {
    db withSession {
      implicit session: Session =>
        val statement = AppDependency.filter(_.business === business)
        statement.map(_.owt).list.distinct
    }
  }

  def getAllOwt = {
    db withSession {
      implicit session: Session =>
        AppDependency.map(_.owt).list.distinct
    }
  }

  def getPdl(owt: String) = {
    db withSession {
      implicit session: Session =>
        val statement = AppDependency.filter(_.owt === owt)
        statement.map(_.pdl).list.distinct
    }
  }

  def getApp(owt: String, pdl: String) = {
    db withSession {
      implicit session: Session =>
        val statement = AppDependency.filter {
          x =>
            x.owt === owt && x.pdl === pdl
        }
        statement.map(_.app).list.distinct
    }
  }


  def getUploadTime(appGroupId: String, appArtifactId: String) = {
    db withSession {
      implicit session: Session =>
        AppDependency.filter { x =>
          x.appGroupId === appGroupId && x.appArtifactId === appArtifactId
        }.map(_.uploadTime).list.distinct
    }
  }

  /**
    * 获得所有应用及其上传的时间
    *
    */
  def getAppAndUploadTime = {
    db withSession {
      implicit session: Session =>
        AppDependency.map(x => (x.appGroupId, x.appArtifactId, x.uploadTime)).list.distinct
    }
  }

  /**
    * 删除未使用的组件
    *
    */
  def deleteDeprecatedDependencies(appGroupId: String, appArtifactId: String, oldUploadTimeList: List[Long]) = {
    db withSession {
      implicit session: Session =>
        val statement = AppDependency.filter { x =>
          x.appGroupId === appGroupId && x.appArtifactId === appArtifactId &&
            (x.uploadTime inSet oldUploadTimeList)
        }
        if (statement.exists.run) {
          statement.delete
        }
    }
  }

  def updateBusinessWithCorrectValue(errorBusiness: String, correctBusiness: String) = {
    db withSession {
      implicit session: Session =>
        val dependencyStatement = AppDependency.filter { x =>
          x.business === errorBusiness
        }
        if (dependencyStatement.exists.run) {
          dependencyStatement.map(_.business).update(correctBusiness)
        }

        val trendStatement = AppTrend.filter { x =>
          x.business === errorBusiness
        }
        if (trendStatement.exists.run) {
          trendStatement.map(_.business).update(correctBusiness)
        }

        val activenessStatement = AppActiveness.filter { x =>
          x.business === errorBusiness
        }
        if (activenessStatement.exists.run) {
          activenessStatement.map(_.business).update(correctBusiness)
        }

        val bomStatement = AppBom.filter { x =>
          x.business === errorBusiness
        }
        if (bomStatement.exists.run) {
          bomStatement.map(_.business).update(correctBusiness)
        }
    }
  }

  def updateBusiness(owts: List[String], correctBusiness: String) = {
    db withSession {
      implicit session: Session =>
        val dependencyStatement = AppDependency.filter { x =>
          x.owt inSet owts
        }
        if (dependencyStatement.exists.run) {
          dependencyStatement.map(_.business).update(correctBusiness)
        }

        val trendStatement = AppTrend.filter { x =>
          x.owt inSet owts
        }
        if (trendStatement.exists.run) {
          trendStatement.map(_.business).update(correctBusiness)
        }

        val activenessStatement = AppActiveness.filter { x =>
          x.owt inSet owts
        }
        if (activenessStatement.exists.run) {
          activenessStatement.map(_.business).update(correctBusiness)
        }

        val bomStatement = AppBom.filter { x =>
          x.owt inSet owts
        }
        if (bomStatement.exists.run) {
          bomStatement.map(_.business).update(correctBusiness)
        }
    }
  }

  /**
    * 查询线上组件版本分布
    *
    */
  def getComponentVersionCount(groupId: String, artifactId: String, base: String, business: String, owt: String, pdl: String) = {
    //根据base不同选择不同的查询范围
    val parameterMap = if (base.equalsIgnoreCase(Base.all.getName)) {
      Map("business" -> SqlParser.ValueExpress(business, "="), "owt" -> SqlParser.ValueExpress(owt, "="), "pdl" -> SqlParser.ValueExpress(pdl, "="),
        "group_id" -> SqlParser.ValueExpress(groupId, "="), "artifact_id" -> SqlParser.ValueExpress(artifactId, "="))
    } else {
      Map("base" -> SqlParser.ValueExpress(base, "="), "business" -> SqlParser.ValueExpress(business, "="), "owt" -> SqlParser.ValueExpress(owt, "="), "pdl" -> SqlParser.ValueExpress(pdl, "="),
        "group_id" -> SqlParser.ValueExpress(groupId, "="), "artifact_id" -> SqlParser.ValueExpress(artifactId, "="))
    }
    val prefixSQL = "SELECT version, count(*) FROM app_dependency WHERE 1 = 1"
    val suffixSQL = " GROUP BY version"
    val sqlStr = SqlParser.sqlParser(prefixSQL, parameterMap, suffixSQL)
    db withSession {
      implicit session: Session =>
        sql"""#$sqlStr""".as[ComponentVersionCount].list
    }
  }


  /**
    * 根据组件具体版本,查询使用情况
    *
    */

  def getComponentVersionDetails(groupId: String, artifactId: String, version: String, base: String, business: String, owt: String, pdl: String) = {
    //根据base不同选择不同的查询范围
    val parameterMap = if (base.equalsIgnoreCase(Base.all.getName)) {
      Map("business" -> SqlParser.ValueExpress(business, "="), "owt" -> SqlParser.ValueExpress(owt, "="), "pdl" -> SqlParser.ValueExpress(pdl, "="),
        "group_id" -> SqlParser.ValueExpress(groupId, "="), "artifact_id" -> SqlParser.ValueExpress(artifactId, "="), "version" -> SqlParser.ValueExpress(version, "="))
    } else {
      Map("base" -> SqlParser.ValueExpress(base, "="), "business" -> SqlParser.ValueExpress(business, "="), "owt" -> SqlParser.ValueExpress(owt, "="), "pdl" -> SqlParser.ValueExpress(pdl, "="),
        "group_id" -> SqlParser.ValueExpress(groupId, "="), "artifact_id" -> SqlParser.ValueExpress(artifactId, "="), "version" -> SqlParser.ValueExpress(version, "="))
    }
    val prefixSQL = "SELECT base, business, owt, pdl, appkey, app_group_id as appGroupId, app_artifact_id as appArtifactId, version FROM app_dependency WHERE 1 = 1"
    val suffixSQL = " GROUP BY app_group_id, app_artifact_id"
    val sqlStr = SqlParser.sqlParser(prefixSQL, parameterMap, suffixSQL)
    db withSession {
      implicit session: Session =>
        sql"""#$sqlStr""".as[ComponentVersionDetails].list
    }
  }

  /**
    * 查询使用组件的App
    *
    */
  def getRealUser(groupId: String, artifactId: String, versions: List[String], base: String, business: String, owt: String, pdl: String) = {
    val suffixSQL = if (versions.length == 1 && StringUtil.isBlank(versions.head)) {
      s" GROUP BY app_group_id, app_artifact_id"
    } else {
      val versionListString = versions.map("'" + _ + "'").mkString(",")
      s" AND version in ($versionListString) GROUP BY app_group_id, app_artifact_id"
    }
    //根据base不同选择不同的查询范围
    val parameterMap = if (base.equalsIgnoreCase(Base.all.getName)) {
      Map("business" -> SqlParser.ValueExpress(business, "="), "owt" -> SqlParser.ValueExpress(owt, "="), "pdl" -> SqlParser.ValueExpress(pdl, "="),
        "group_id" -> SqlParser.ValueExpress(groupId, "="), "artifact_id" -> SqlParser.ValueExpress(artifactId, "="))
    } else {
      Map("base" -> SqlParser.ValueExpress(base, "="), "business" -> SqlParser.ValueExpress(business, "="), "owt" -> SqlParser.ValueExpress(owt, "="), "pdl" -> SqlParser.ValueExpress(pdl, "="),
        "group_id" -> SqlParser.ValueExpress(groupId, "="), "artifact_id" -> SqlParser.ValueExpress(artifactId, "="))
    }
    val prefixSQL = "SELECT business, owt, pdl, app, appkey, base, app_group_id as groupId, app_artifact_id as artifactId, '' as version, '' as packaging FROM app_dependency WHERE 1 = 1"
    val sqlStr = SqlParser.sqlParser(prefixSQL, parameterMap, suffixSQL)
    db withSession {
      implicit session: Session =>
        sql"""#$sqlStr""".as[AppDescDomain].list
    }
  }

  /**
    * 查询所有App
    *
    */
  def getAllUser(base: String, business: String, owt: String, pdl: String) = {
    val parameterMap = if (base.equalsIgnoreCase(Base.all.getName)) {
      Map("business" -> SqlParser.ValueExpress(business, "="), "owt" -> SqlParser.ValueExpress(owt, "="), "pdl" -> SqlParser.ValueExpress(pdl, "="))
    } else {
      Map("base" -> SqlParser.ValueExpress(base, "="), "business" -> SqlParser.ValueExpress(business, "="), "owt" -> SqlParser.ValueExpress(owt, "="), "pdl" -> SqlParser.ValueExpress(pdl, "="))
    }
    val prefixSQL = "SELECT business, owt, pdl, app, appkey, base, app_group_id as groupId, app_artifact_id as artifactId, '' as version, '' as packaging FROM app_dependency WHERE 1 = 1"
    val suffixSQL = " GROUP BY app_group_id, app_artifact_id"
    val sqlStr = SqlParser.sqlParser(prefixSQL, parameterMap, suffixSQL)
    db withSession {
      implicit session: Session =>
        sql"""#$sqlStr""".as[AppDescDomain].list
    }
  }

  def getAppsVersion(groupId: String, artifactId: String, base: String, business: String, owt: String, pdl: String) = {
    val parameterMap = if (base.equalsIgnoreCase(Base.all.getName)) {
      Map("business" -> SqlParser.ValueExpress(business, "="), "owt" -> SqlParser.ValueExpress(owt, "="), "pdl" -> SqlParser.ValueExpress(pdl, "="),
        "group_id" -> SqlParser.ValueExpress(groupId, "="), "artifact_id" -> SqlParser.ValueExpress(artifactId, "="))
    } else {
      Map("base" -> SqlParser.ValueExpress(base, "="), "business" -> SqlParser.ValueExpress(business, "="), "owt" -> SqlParser.ValueExpress(owt, "="), "pdl" -> SqlParser.ValueExpress(pdl, "="),
        "group_id" -> SqlParser.ValueExpress(groupId, "="), "artifact_id" -> SqlParser.ValueExpress(artifactId, "="))
    }
    val prefixSQL = "SELECT business, owt, pdl, app, appkey, base, app_group_id as groupId, app_artifact_id as artifactId, version, '' as packaging FROM app_dependency WHERE 1 = 1"
    val suffixSQL = " GROUP BY app_group_id, app_artifact_id, version"
    val sqlStr = SqlParser.sqlParser(prefixSQL, parameterMap, suffixSQL)
    LOG.info(s"getAppsVersion $sqlStr")
    db withSession {
      implicit session: Session =>
        sql"""#$sqlStr""".as[AppDescDomain].list
    }
  }

  /**
    * 查询组件详细
    *
    */
  def getDetails(base: String, business: String, owt: String, pdl: String, groupId: String, artifactId: String, version: String) = {
    val parameterMap = if (base.equalsIgnoreCase(Base.all.getName)) {
      Map("business" -> SqlParser.ValueExpress(business, "="), "owt" -> SqlParser.ValueExpress(owt, "="), "pdl" -> SqlParser.ValueExpress(pdl, "="),
        "group_id" -> SqlParser.ValueExpress(s"%$groupId%", "LIKE"), "artifact_id" -> SqlParser.ValueExpress(s"%$artifactId%", "LIKE"), "version" -> SqlParser.ValueExpress(version, "="))
    } else {
      Map("base" -> SqlParser.ValueExpress(base, "="), "business" -> SqlParser.ValueExpress(business, "="), "owt" -> SqlParser.ValueExpress(owt, "="), "pdl" -> SqlParser.ValueExpress(pdl, "="),
        "group_id" -> SqlParser.ValueExpress(s"%$groupId%", "LIKE"), "artifact_id" -> SqlParser.ValueExpress(s"%$artifactId%", "LIKE"), "version" -> SqlParser.ValueExpress(version, "="))
    }
    val prefixSQL = "SELECT base, business, owt, pdl, appkey, app_group_id as appGroupId, app_artifact_id as appArtifactId, group_id AS groupId, artifact_id AS artifactId, version, upload_time as uploadTime FROM app_dependency WHERE 1 = 1"
    val suffixSQL = ""
    val sqlStr = SqlParser.sqlParser(prefixSQL, parameterMap, suffixSQL)
    db withSession {
      implicit session: Session =>
        sql"""#$sqlStr""".as[ComponentDetails].list
    }
  }

  /**
    * 通过Appkey查看对应应用的组件依赖
    *
    */
  def getDetialsByAppkey(appkey: String) = {
    db withSession {
      implicit session: Session =>
        AppDependency.filter(_.appkey === appkey).list
    }
  }


  def getBusinessByOwt(base: String, owt: String, tableName: String) = {
    db withSession {
      implicit session: Session =>
        if (tableName.equalsIgnoreCase("app_dependency")) {
          val statement = AppDependency.filter(_.owt === owt)
          statement.map(_.business).list.distinct
        } else {
          val statement = ComponentCoverage.filter(_.owt === owt)
          statement.map(_.business).list.distinct
        }
    }
  }

  /**
    * 获取使用组件的应用的appkey, 这些组件的版本号位于一个集合中
    *
    */
  def getAppVersionInRange(groupId: String, artifactId: String, versions: List[String]) = {
    db withSession {
      implicit session: Session =>
        val statement = AppDependency.filter {
          x =>
            x.groupId === groupId && x.artifactId === artifactId && x.version.inSet(versions)
        }
        statement.list.map {
          x => AppDescDomain(x.business, x.owt, x.pdl, x.app, x.appkey, x.base, x.appGroupId, x.appArtifactId, x.version, "")
        }.distinct
    }
  }
}