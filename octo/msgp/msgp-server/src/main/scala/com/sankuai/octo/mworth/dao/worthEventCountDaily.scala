package com.sankuai.octo.mworth.dao

import java.sql.Date

import com.sankuai.msgp.common.config.DbConnection
import com.sankuai.msgp.common.service.org.OrgSerivce
import com.sankuai.msgp.common.utils.StringUtil
import com.sankuai.octo.mworth.common.model.Worth.Model
import com.sankuai.octo.mworth.db.Tables._
import com.sankuai.octo.mworth.util.DateTimeUtil
import org.joda.time.DateTime
import org.slf4j.{Logger, LoggerFactory}

import scala.slick.driver.MySQLDriver.simple._
import scala.slick.jdbc.{GetResult, StaticQuery => Q}
import com.sankuai.octo.mworth.model.CountType
import com.sankuai.octo.mworth.task.worthEventCountDailyTask

import scala.slick.jdbc.StaticQuery.interpolation

/**
  * Created by yves on 16/7/6.
  */
object worthEventCountDaily {
  val LOG: Logger = LoggerFactory.getLogger(worthEvent.getClass)

  private val db = DbConnection.getWorthPool()

  case class WEventBusinessDescCount(business: Int, username: String, module: String, functionDesc: String,
                                     posName: String, posId: Int, orgId: Int, orgName: String, count: Int, day: Date)

  implicit val getWEventBusinessDescCountResult = GetResult(r => WEventBusinessDescCount(r.<<, r.<<, r.<<, r.<<, r.<<, r.<<, r.<<, r.<<, r.<<, r.<<))

  //接口查詢所用

  case class WDailyCount(date: Date, count: Int)

  implicit val getWDailyCountResult = GetResult(r => WDailyCount(r.<<, r.<<))

  case class WBusinessCount(business: Int, username: String, date: Date, count: Int)

  implicit val getWBusinessCountResult = GetResult(r => WBusinessCount(r.<<, r.<<, r.<<, r.<<))

  case class WUserCount(business: Int, date: Date, count: Int)

  implicit val getWUserCountResult = GetResult(r => WUserCount(r.<<, r.<<, r.<<))

  case class WModuleCount(module: String, functionDesc: String, date: Date, count: Int)

  implicit val getWModuleCountResult = GetResult(r => WModuleCount(r.<<, r.<<, r.<<, r.<<))

  case class WFreeCount(business: Int, username: String, module: String, functionDesc: String, count: Int, day: Date)

  implicit val getWFreeCountResult = GetResult(r => WFreeCount(r.<<, r.<<, r.<<, r.<<, r.<<, r.<<))

  case class WCoverageCount(orgId: Int, date: Date, count: Int)

  implicit val getWCoverageCountResult = GetResult(r => WCoverageCount(r.<<, r.<<, r.<<))

  def insert(event: WEventBusinessDescCount) = {
    db withSession {
      implicit session: Session =>
        (WorthBusinessDescCount returning WorthBusinessDescCount.map(_.id)) +=
          WorthBusinessDescCountRow(0, event.business, event.username, event.module, event.functionDesc,
            event.posId, event.posName, event.orgId, event.orgName, event.count, event.day)
    }
  }

  def batchInsert(list: List[WEventBusinessDescCount]) = {
    db withSession {
      implicit session: Session =>
        val seq = list.map(element => {
          WorthBusinessDescCountRow(0, element.business, element.username, element.module,
            element.functionDesc, element.posId, element.posName, element.orgId, element.orgName, element.count, element.day)
        })
        WorthBusinessDescCount ++= seq
    }
  }

  //刷新每天数据
  def refresh(date: Date) = {
    val startTime = new DateTime(date.getTime).withTimeAtStartOfDay()
    //先删除
    worthEventCountDaily.delete(date)
    worthEventCountDailyTask.countEvent(startTime.getMillis, startTime.getMillis + DateTimeUtil.DAY_TIME)
    worthEventCountDailyTask.countPeak
  }

  def delete(date: Date) = {
    db withSession {
      implicit session: Session =>
        val statement = WorthBusinessDescCount.filter { x => x.day === date }
        statement.delete
    }
  }

  def getUvCount(date: Date, orgId: Int) = {
    db withSession {
      implicit session: Session =>
        sql"SELECT count(*) from worth_business_desc_count where posid = ${orgId}".as[String].list
    }
  }


  def getUserList = {
    db withSession {
      implicit session: Session =>
        sql"SELECT DISTINCT username from worth_business_desc_count".as[String].list
    }
  }

  def updateOrgInfo(posId: List[Int], posName: List[String], orgId: List[Int], orgName: List[String], userList: List[String]) = {
    db withSession {
      implicit session: Session =>
        userList.foreach {
          name =>
            val statement = WorthBusinessDescCount.filter(_.username === name)
              .map(row => (row.posid, row.posname, row.orgid, row.orgname))
            val index = userList.indexOf(name)
            statement.update((posId.apply(index), posName.apply(index), orgId.apply(index), orgName.apply(index)))
        }
    }
  }

  def getOrgIdList = {
    db withSession {
      implicit session: Session =>
        sql"SELECT DISTINCT orgid from worth_business_desc_count".as[Int].list
    }
  }

  def updateBusinessInfoViaOrg(orgId: List[Int], orgToBussiness: Map[Int, Int]) = {
    db withSession {
      implicit session: Session =>
        orgId.foreach {
          orgid =>
            val statement = WorthBusinessDescCount.filter(_.orgid === orgid)
              .map(row => row.business)
            statement.update(orgToBussiness(orgid))
        }
    }
  }

  def updateBusinessInfoViaOps(userList: List[String], userToBusiness: Map[String, Int]) = {
    db withSession {
      implicit session: Session =>
        userList.foreach {
          username =>
            val statement = WorthBusinessDescCount.filter(_.username === username)
              .map(row => row.business)
            statement.update(userToBusiness(username))
        }
    }
  }

  def queryTotal(start: Date, end: Date, dataType: Int) = {
    db withSession {
      implicit session: Session =>
        if (dataType == CountType.pv.id) {
          sql"select day as date, sum(count) as count from worth_business_desc_count where day BETWEEN  ${start} and ${end} GROUP BY day ORDER BY day ASC".as[WDailyCount].list
        } else {
          sql"select day as date, COUNT(DISTINCT username) as count from worth_business_desc_count where day BETWEEN  ${start} and ${end} GROUP BY day ORDER BY day ASC".as[WDailyCount].list
        }
    }
  }

  def queryMax(dataType: Int, start: Date) = {
    db withSession {
      implicit session: Session =>
        if (dataType == CountType.pv.id) {
          sql"select day as date, sum(count) as count from worth_business_desc_count WHERE day >= ${start} GROUP BY day ORDER BY sum(count) DESC LIMIT 1".as[WDailyCount].list
        } else {
          sql"select day as date, COUNT(DISTINCT username) as count from worth_business_desc_count WHERE day >= ${start} GROUP BY day ORDER BY COUNT(DISTINCT username) DESC LIMIT 1".as[WDailyCount].list
        }
    }
  }


  def queryBusinessTotal(start: Date, end: Date, business: Int, dataType: Int) = {
    db withSession {
      implicit session: Session =>
        if (dataType == CountType.pv.id) {
          if (business >= 0) {
            sql"select business, username, day as date, sum(count) as count from worth_business_desc_count where day BETWEEN  ${start} and ${end} and business = ${business} GROUP BY day, username ORDER BY day ASC".as[WBusinessCount].list
          }
          else {
            sql"select business, '' as username, day as date, sum(count) as count from worth_business_desc_count where day BETWEEN  ${start} and ${end} GROUP BY day, business ORDER BY day ASC".as[WBusinessCount].list
          }
        } else {
          if (business >= 0) {
            sql"select business, '' as username, day as date, COUNT(DISTINCT username) as count from worth_business_desc_count where day BETWEEN  ${start} and ${end} and business = ${business} GROUP BY day ORDER BY day ASC".as[WBusinessCount].list
          } else {
            sql"select business, '' as username, day as date, COUNT(DISTINCT username) as count from worth_business_desc_count where day BETWEEN  ${start} and ${end} GROUP BY day, business ORDER BY day ASC".as[WBusinessCount].list
          }
        }
    }
  }

  def queryUsersTotal(start: Date, end: Date) = {
    db withSession {
      implicit session: Session =>
        sql"select business, day as date, count(DISTINCT username) as count from worth_business_desc_count where day BETWEEN  ${start} and ${end} GROUP BY day, business ORDER BY day ASC".as[WUserCount].list
    }
  }

  def queryModuleTotal(start: Date, end: Date, module: Int, dataType: Int) = {
    db withSession {
      implicit session: Session =>
        if (dataType == CountType.pv.id) {
          if (module >= 0) {
            val moduleString = Model.values().find(_.ordinal() == module).getOrElse(Model.OTHER).getName
            sql"select model as module, function_desc as functionDesc, day as date, sum(count) as count from worth_business_desc_count where day BETWEEN  ${start} and ${end} and model = ${moduleString} GROUP BY day, model, function_desc ORDER BY day ASC".as[WModuleCount].list
          }
          else {
            sql"select model as module, null, day as date, sum(count) as count from worth_business_desc_count where day BETWEEN  ${start} and ${end}  GROUP BY day,model ORDER BY day ASC".as[WModuleCount].list
          }
        } else {
          if (module >= 0) {
            val moduleString = Model.values().find(_.ordinal() == module).getOrElse(Model.OTHER).getName
            sql"select model as module, function_desc as functionDesc, day as date, count(DISTINCT username) as count from worth_business_desc_count where day BETWEEN  ${start} and ${end} and model = ${moduleString} GROUP BY day, model, function_desc ORDER BY day ASC".as[WModuleCount].list
          }
          else {
            sql"select model as module, null, day as date, count(DISTINCT username) as count from worth_business_desc_count where day BETWEEN  ${start} and ${end}  GROUP BY day,model ORDER BY day ASC".as[WModuleCount].list
          }
        }
    }
  }

  def queryDetails(date: Date, business: Int, username: String, module: Int, functionDesc: String) = {
    db withSession {
      implicit session: Session =>
        if (business == -1 && module != -1) {
          val moduleString = Model.values().find(_.ordinal() == module).getOrElse(Model.OTHER).getName
          if (functionDesc.equals("选择全部")) {
            sql"select business, username, model as module, function_desc as functionDesc, count,day as date from worth_business_desc_count where day = ${date} and model =  ${moduleString} ORDER BY count DESC".as[WFreeCount].list
          } else {
            sql"select business, username, model as module, function_desc as functionDesc, count,day as date from worth_business_desc_count where day = ${date} and model =  ${moduleString} and function_desc =  ${functionDesc} ORDER BY count DESC".as[WFreeCount].list
          }
        } else if (business != -1 && module == -1) {
          if (StringUtil.isBlank(username)) {
            sql"select business, username, model as module, function_desc as functionDesc, count,day as date from worth_business_desc_count where day = ${date} and business =  ${business} ORDER BY count DESC".as[WFreeCount].list
          } else {
            //查部门下特定用户使用情况(隐含了 部门和模块均为空,但是用户名不为空的情况)
            sql"select business, username, model as module, function_desc as functionDesc,count, day as date from worth_business_desc_count where day = ${date} and username =  ${username} ORDER BY count DESC".as[WFreeCount].list
          }
        } else if (business != -1 && module != -1) {
          val moduleString = Model.values().find(_.ordinal() == module).getOrElse(Model.OTHER).getName
          if (functionDesc.equals("选择全部") && StringUtil.isBlank(username)) {
            sql"select business, username, model as module, function_desc as functionDesc, count, day as date from worth_business_desc_count where day = ${date} and business =  ${business} and model =  ${moduleString} ORDER BY count DESC".as[WFreeCount].list
          } else if (functionDesc.equals("选择全部") && !StringUtil.isBlank(username)) {
            sql"select business, username, model as module, function_desc as functionDesc, count, day as date from worth_business_desc_count where day = ${date} and username =  ${username} and model =  ${moduleString} ORDER BY count DESC".as[WFreeCount].list
          } else if (!functionDesc.equals("选择全部") && StringUtil.isBlank(username)) {
            sql"select business, username, model as module, function_desc as functionDesc, count, day as date from worth_business_desc_count where day = ${date} and business =  ${business} and model =  ${moduleString} and function_desc =  ${functionDesc} ORDER BY count DESC".as[WFreeCount].list
          } else {
            sql"select business, username, model as module, function_desc as functionDesc, count, day as date from worth_business_desc_count where day = ${date} and username =  ${username} and model =  ${moduleString} and function_desc =  ${functionDesc} ORDER BY count DESC".as[WFreeCount].list
          }
        } else {
          //(business == -1 && module == -1) 四大皆空
          sql"select business, username, model as module, function_desc as functionDesc, count, day as date from worth_business_desc_count where day = ${date} ORDER BY count DESC LIMIT 200".as[WFreeCount].list
        }
    }
  }

  def queryFunction(date: Date, module: Int) = {
    db withSession {
      implicit session: Session =>
        val moduleString = Model.values().find(_.ordinal() == module).getOrElse(Model.OTHER).getName
        //获取全部function_desc
        sql"select DISTINCT function_desc from worth_business_desc_count where model = ${moduleString}".as[String].list
    }
  }

  def queryCoverage(start: Date, end: Date, orgNodeList: List[Int], devPosList: List[String]) = {
    db withSession {
      implicit session: Session =>
        val devPosStr = devPosList.map("'" + _ + "'").mkString(",")
        if(orgNodeList.head == 2) {
          val orgNodeStr = orgNodeList.map("'" + _ + "'").mkString(",")
          sql"""select business as orgid, day as date, count(Distinct username) from worth_business_desc_count where day BETWEEN ${start} and ${end}  and posname in (#${devPosStr}) group by day, business""".as[WCoverageCount].list
        }else{
          val orgNodeStr = orgNodeList.map("'" + _ + "'").mkString(",")
          sql"""select orgid, day as date, count(Distinct username) from worth_business_desc_count where day BETWEEN ${start} and ${end} and orgid in (#${orgNodeStr}) and posname in (#${devPosStr}) group by day, orgid""".as[WCoverageCount].list
        }
    }
  }
}