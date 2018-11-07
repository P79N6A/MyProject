package com.sankuai.octo.msgp.dao.component

import java.sql.Date

import com.sankuai.msgp.common.config.DbConnection
import com.sankuai.msgp.common.config.db.msgp.Tables._
import com.sankuai.msgp.common.utils.StringUtil
import com.sankuai.octo.msgp.dao.component.ComponentDAO.SimpleArtifact

import scala.slick.driver.MySQLDriver.simple._
import scala.slick.jdbc.StaticQuery.interpolation
import scala.slick.jdbc.{GetResult, StaticQuery => Q}


/**
  * Created by yves on 16/9/27.
  */
object TrendDAO {

  case class ComponentTrendCount(business: String, owt: String, pdl: String, coverage: Int, date: Date)

  implicit val getComponentTrendCountResult = GetResult(r => ComponentTrendCount(r.<<, r.<<, r.<<, r.<<, r.<<))

  case class ComponentDailyCount(base: String, business: String, owt: String, pdl: String, groupId: String, artifactId: String, version: String, realCount: Int)

  implicit val getComponentDailyCountResult = GetResult(r => ComponentDailyCount(r.<<, r.<<, r.<<, r.<<, r.<<, r.<<, r.<<, r.<<))

  case class ComponentDailyCountUpload(base: String, business: String, owt: String, pdl: String, groupId: String, artifactId: String, version: String, coverage: Int, date: Date)

  implicit val getComponentDailyCountUploadResult = GetResult(r => ComponentDailyCountUpload(r.<<, r.<<, r.<<, r.<<, r.<<, r.<<, r.<<, r.<<, r.<<))

  private val db = DbConnection.getPool()


  /**
    * 删除ComponentCoverage表中特定时间记录
    *
    */
  def delete(date: Date) = {
    db withSession {
      implicit session: Session =>
        val statement = AppTrend.filter { x => x.date === date }
        statement.delete
    }
  }

  /**
    * 批量上传至ComponentCoverage表
    *
    */
  def batchInsert(list: List[ComponentDailyCountUpload]) = {
    db withSession {
      implicit session: Session =>
        val seq = list.map(element => {
          AppTrendRow(0, element.base, element.business, element.owt, element.pdl, element.groupId,
            element.artifactId, element.version, element.coverage, element.date)
        })
        AppTrend ++= seq
    }
  }

  /**
    * 查询一段时间内的组件使用量趋势
    *
    */

  def getComponentTrend(start: Date, end: Date, base: String, business: String, owt: String, pdl: String, groupId: String, artifactId: String, version: String) = {
    var prefixSQL = ""
    var suffixSQL = ""
    //TODO 这里查的是所有的,base没有起作用,需要添加上
    if (StringUtil.isBlank(business) && StringUtil.isBlank(owt) && StringUtil.isBlank(pdl)) {
      prefixSQL = s"SELECT business, '' as owt, '' as pdl, SUM(coverage), date FROM app_trend WHERE  group_id = '${groupId}' AND artifact_id = '${artifactId}'AND  date BETWEEN '${start}' AND '${end}' "
      suffixSQL = s" GROUP BY date, business"
    } else if (StringUtil.isNotBlank(business) && StringUtil.isBlank(owt) && StringUtil.isBlank(pdl)) {
      prefixSQL = s"SELECT business, owt, '' as pdl, SUM(coverage), date FROM app_trend WHERE  group_id = '${groupId}' AND artifact_id = '${artifactId}' AND business = '${business}' AND date BETWEEN '${start}' AND '${end}'"
      suffixSQL = " GROUP BY date, owt"
    } else if (StringUtil.isNotBlank(business) && !StringUtil.isBlank(owt) && StringUtil.isBlank(pdl)) {
      prefixSQL = s"SELECT business, owt, pdl, SUM(coverage), date FROM app_trend WHERE group_id = '${groupId}' AND artifact_id = '${artifactId}'  and date BETWEEN '${start}' AND '${end}' AND business = '${business}' AND owt = '${owt}'"
      suffixSQL = " GROUP BY date, pdl"
    } else if (StringUtil.isNotBlank(business) && !StringUtil.isBlank(owt) && !StringUtil.isBlank(pdl)) {
      prefixSQL = s"SELECT business, owt, pdl, SUM(coverage), date FROM app_trend WHERE  group_id = '${groupId}' AND artifact_id = '${artifactId}' AND date BETWEEN '${start}' AND '${end}' AND business = '${business}' AND owt = '${owt}' AND pdl = '${pdl}'"
      suffixSQL = " GROUP BY date, pdl"
    } else {
      //异常
      prefixSQL = "SELECT business, '' as owt, '' as pdl, SUM(coverage), date FROM app_trend WHERE  group_id = ${groupId} AND artifact_id = ${artifactId}  AND date BETWEEN ${start} AND ${end} "
      suffixSQL = " GROUP BY date, business"
    }
    val conditionSQL = if (StringUtil.isBlank(version)) "" else s" AND version = '$version'"
    val sqlStr = prefixSQL + conditionSQL + suffixSQL
    db withSession {
      implicit session: Session =>
        sql"""#$sqlStr""".as[ComponentTrendCount].list
    }
  }


  /**
    * 从依赖库获取关键组件的统计信息,目前仅过滤groupId不以"org."开始的
    * 统计business/owt/pdl下应用使用某个组件的次数
    *
    */

  def getDailyCount(artifacts: List[String]) = {
    db withSession {
      implicit session: Session =>
        val statement = AppDependency.filter(_.artifactId inSet artifacts).list.groupBy {
          record =>
            SimpleArtifact(record.groupId, record.artifactId, record.version)
        }
        //每个进行遍历
        statement.flatMap {
          case (dependency, usageOfEveryDependency) =>
            val usageMap = usageOfEveryDependency.groupBy(x => (x.base, x.business, x.owt, x.pdl))
            usageMap.map {
              case (orgInfo, usage) =>
                val count = usage.map(x => (x.appGroupId, x.appArtifactId)).distinct.length
                ComponentDailyCount(orgInfo._1, orgInfo._2, orgInfo._3, orgInfo._4, dependency.groupId, dependency.artifactId, dependency.version, count)
            }.toList
        }.toList
    }
  }
}
