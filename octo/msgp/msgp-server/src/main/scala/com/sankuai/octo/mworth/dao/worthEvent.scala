package com.sankuai.octo.mworth.dao

import java.util.Date

import com.sankuai.msgp.common.config.DbConnection
import com.sankuai.msgp.common.model.Page
import com.sankuai.octo.mworth.db.Tables._
import org.slf4j.{Logger, LoggerFactory}

import scala.slick.driver.MySQLDriver.simple._
import scala.slick.jdbc.GetResult
import scala.slick.jdbc.StaticQuery.interpolation
import scala.slick.lifted.CanBeQueryCondition

/**
  * Created by zava on 15/11/30.
  */
object worthEvent {
  val LOG: Logger = LoggerFactory.getLogger(worthEvent.getClass)

  private val db = DbConnection.getWorthPool()

  case class WEvent(id: Long, project: String, model: String, functionName: String, functionDesc: String,
                    operationSourceType: Int, business: Int = -1, operationSource: String, targetAppkey: String,
                    appkeyOwt: String, signid: String, startTime: Long, endTime: Long, createTime: Long)

  case class WEventCount(username: String, project: String, model: String, count: Int)

  implicit val getWorthEventCountResult = GetResult(r => WEventCount(r.<<, r.<<, r.<<, r.<<))

  case class WEventBusinessUserOwtModelCount(business: Int, username: String, appkeyOwt: String, project: String, model: String, count: Int)

  implicit val getWorthBusinessUserOwtModelCountResult = GetResult(r => WEventBusinessUserOwtModelCount(r.<<, r.<<, r.<<, r.<<, r.<<, r.<<))

  case class WEventOwtAppkeyModelCount(owt: String, appkey: String, project: String, model: String, count: Int)

  implicit val getWorthOwtAppkeyModelCountResult = GetResult(r => WEventOwtAppkeyModelCount(r.<<, r.<<, r.<<, r.<<, r.<<))

  case class WEventModelMethodCount(project: String, model: String, functionName: String, count: Int)

  implicit val getWorthModelMethodCountResult = GetResult(r => WEventModelMethodCount(r.<<, r.<<, r.<<, r.<<))

  case class WEventDailyCount(username: String, module: String, functionDesc: String, count: Int)

  implicit val getWorthEventDailyCountResult = GetResult(r => WEventDailyCount(r.<<, r.<<, r.<<, r.<<))


  def search(project: Option[String], model: Option[String], functionName: Option[String], targetAppkey: Option[String], fromTime: Date, toTime: Date, page: Page) = {
    db withSession {
      implicit session: Session =>
        val statement = WorthEvent.optionFilter(project)(_.project === _)
          .optionFilter(functionName)(_.functionName === _)
          .optionFilter(targetAppkey)(_.targetAppkey === _)
          .filter(x => x.createTime > fromTime.getTime && x.createTime < toTime.getTime)
        val limit = page.getPageSize
        val offset = page.getStart
        val count = statement.length.run
        page.setTotalCount(count)
        statement.drop(offset).take(limit).list
    }
  }

  def querySign(project: String, functionName: String, signid: Option[String]) = {
    db withSession {
      implicit session: Session =>
        WorthEvent.filter(x => x.project === project
          && x.functionName === functionName)
          .optionFilter(signid)(_.signid === _).sortBy(x => x.id.desc).list
    }
  }


  def insert(wEvent: WEvent): Long = {
    db withSession {
      implicit session: Session =>
        (WorthEvent returning WorthEvent.map(_.id)) +=
          WorthEventRow(0, wEvent.project, wEvent.model, wEvent.functionName, wEvent.functionDesc,
            wEvent.operationSourceType, wEvent.business, wEvent.operationSource,
            Some(wEvent.targetAppkey), Some(wEvent.appkeyOwt), Some(wEvent.signid), Some(wEvent.startTime),
            Some(wEvent.endTime), wEvent.createTime)
    }
  }

  def batchInsert(list: List[WEvent]) = {
    db withSession {
      implicit session: Session =>
        val seq = list.map(wEvent => {
          WorthEventRow(0, wEvent.project, wEvent.model, wEvent.functionName, wEvent.functionDesc,
            wEvent.operationSourceType, wEvent.business, wEvent.operationSource,
            Some(wEvent.targetAppkey), Some(wEvent.appkeyOwt), Some(wEvent.signid), Some(wEvent.startTime),
            Some(wEvent.endTime), wEvent.createTime)
        })
        WorthEvent ++= seq
    }
  }

  /**
    *
    * 统计指定时间段的 用户访问次数
    *
    */
  def countUserModel(start: Long, end: Long) = {
    db withSession {
      implicit session: Session =>
        sql"select operation_source as username,project,  model ,count(1) from worth_event where  create_time  BETWEEN  ${start} and ${end}  and operation_source_type = 0  GROUP BY operation_source ,model".as[WEventCount].list
    }
  }

  def countBusinessUserOwtModel(start: Long, end: Long) = {
    db withSession {
      implicit session: Session =>
        sql"select business, operation_source as username, appkey_owt,project,  model ,count(1) from worth_event where  create_time  BETWEEN  ${start} and ${end}  and operation_source_type = 0  GROUP BY business,username,appkey_owt ,model".as[WEventBusinessUserOwtModelCount].list
    }
  }

  def countOwtAppkeyModel(start: Long, end: Long) = {
    db withSession {
      implicit session: Session =>
        sql"select appkey_owt, target_appkey as appkey , project,  model ,count(1) from worth_event where  create_time  BETWEEN  ${start} and ${end}  and operation_source_type = 0  GROUP BY appkey_owt,appkey,model".as[WEventOwtAppkeyModelCount].list
    }
  }

  def countModelMethod(start: Long, end: Long) = {
    db withSession {
      implicit session: Session =>
        sql"select  project,  model ,function_desc,count(1) from worth_event where  create_time  BETWEEN  ${start} and ${end}  and operation_source_type = 0  GROUP BY model ,function_desc".as[WEventModelMethodCount].list
    }
  }

  /**
    *
    * 统计每一天的 owt-functionDesc
    *
    */
  def countBusinessDescDaily(start: Long, end: Long) = {
    db withSession {
      implicit session: Session =>
        sql"select operation_source as username, model as module, function_desc as functionDesc ,count(1) as count from worth_event where create_time  BETWEEN  ${start} and ${end} and operation_source_type = 0 group by operation_source, function_desc".as[WEventDailyCount].list
    }
  }

  /**
    * 获取没有部门数据的用户信息
    */
  def getBusinessUser() = {
    db withSession {
      implicit session: Session =>
        sql"select  DISTINCT  operation_source from worth_event where business = -1 and operation_source_type = 0;".as[String].list
    }
  }

  def updateBusiness(username: String, business: Int) = {
    db withSession {
      implicit session: Session =>
        sql" update worth_event set business=${business}  where operation_source = ${username} and operation_source_type = 0;".as[Int].first
    }
  }

  def getTargetAppkey() = {
    db withSession {
      implicit session: Session =>
        sql"select  DISTINCT  target_appkey from worth_event where length(target_appkey)>0 and  appkey_owt is NULL;".as[String].list
    }
  }

  def updateAppkeyOwt(appkey: String, owt: String) = {
    db withSession {
      implicit session: Session =>
        sql" update worth_event set appkey_owt=${owt}  where target_appkey = ${appkey}".as[Int].first
    }
  }

  implicit class QueryHelper[T, E](query: Query[T, E, Seq]) {
    def optionFilter[X, R: CanBeQueryCondition](name: Option[X])(f: (T, X) => R) =
      name.map(v => query.withFilter(f(_, v))).getOrElse(query)
  }

}
