package com.sankuai.octo.mworth.dao

import java.sql
import java.util.Date

import com.sankuai.msgp.common.config.DbConnection
import com.sankuai.msgp.common.model.{Business, Page}
import com.sankuai.msgp.common.utils.StringUtil
import com.sankuai.octo.mworth.db.Tables._
import com.sankuai.octo.mworth.service.mWorthEventService
import com.sankuai.octo.mworth.task.worthEventTask
import com.sankuai.octo.mworth.util.DateTimeUtil
import org.joda.time.{DateTime, Days}
import org.slf4j.{Logger, LoggerFactory}

import scala.slick.driver.MySQLDriver.simple._
import scala.slick.jdbc.GetResult
import scala.slick.jdbc.StaticQuery.interpolation
import scala.slick.lifted.CanBeQueryCondition


/**
 * Created by zava on 15/11/30.
 */
object worthEventCount {
  val LOG: Logger = LoggerFactory.getLogger(worthEventCount.getClass)

  private val db = DbConnection.getWorthPool()
  private val dayMillis = 86400000

  case class WEventCount(id: Long, business: Int = 100, username: String, owt: String, appkeyOwt: String, day: java.sql.Date,
                         dtype: Int, project: String, model: String,
                         count: Int, createTime: Long)

  case class WEventUserCount(business: String, username: String, count: Int)

  case class WEventOwtCount(business: String, owt: String, count: Int)

  case class WEventModelCount(business: String, model: String, count: Int)

  case class WEventOwtModelCount(owt: String, model: String, count: Int)


  implicit val getWorthEventUserCountResult = GetResult(r => WEventUserCount(r.<<, r.<<, r.<<))
  implicit val getWorthEventOwtCountResult = GetResult(r => WEventOwtCount(r.<<, r.<<, r.<<))
  implicit val getWorthEvenModelCountResult = GetResult(r => WEventModelCount(r.<<, r.<<, r.<<))


  def insert(wEventCount: WEventCount): Long = {
    db withSession {
      implicit session: Session =>
        (WorthEventCount returning WorthEventCount.map(_.id)) +=
          WorthEventCountRow(0, wEventCount.business, wEventCount.username, wEventCount.appkeyOwt,
            wEventCount.project, wEventCount.model, wEventCount.owt, wEventCount.day,
            wEventCount.dtype, wEventCount.count, wEventCount.createTime)
    }
  }

  def batchInsert(list: List[WEventCount]) = {
    db withSession {
      implicit session: Session =>
        val seq = list.map(wEventCount => {
          WorthEventCountRow(0, wEventCount.business, wEventCount.username, wEventCount.appkeyOwt,
            wEventCount.project, wEventCount.model, wEventCount.owt, wEventCount.day,
            wEventCount.dtype, wEventCount.count, wEventCount.createTime)
        })
        WorthEventCount ++= seq
    }
  }


  def queryUsername(business: Integer, username: String, day: java.sql.Date, dtype: Int, page: Page): List[WEventUserCount] = {
    val list = db withSession {
      implicit session: Session =>
        val limit = page.getStart
        val offset = page.getPageSize
        val total =
          if (null != business && StringUtil.isNotBlank(username)) {
            val bus_val = business.intValue()
            sql"select  count(DISTINCT business,username) from worth_event_count where  business = ${bus_val} and username=${username}  and day = ${day} and dtype= ${dtype}".as[Int].first
          }
          else if (null != business && StringUtil.isBlank(username)) {
            val bus_val = business.intValue()
            sql"select  count(DISTINCT business,username) from worth_event_count where  business = ${bus_val}  and day = ${day} and dtype= ${dtype}".as[Int].first
          }
          else if (null == business && StringUtil.isNotBlank(username)) {
            sql"select  count(DISTINCT business,username) from worth_event_count where   username=${username} and day = ${day} and dtype= ${dtype}".as[Int].first
          } else {
            sql"select  count(DISTINCT business,username) from worth_event_count where  day = ${day} and dtype= ${dtype}".as[Int].first
          }

        page.setTotalCount(total)
        if (null != business && StringUtil.isNotBlank(username)) {
          val bus_val = business.intValue()
          sql"select  business,username,sum(count) as  count from worth_event_count where  business = ${bus_val} and username=${username} and day = ${day} and dtype= ${dtype}  GROUP BY business,username order by  count desc limit  ${limit},${offset} ".as[WEventUserCount].list
        } else if (null != business && StringUtil.isBlank(username)) {
          val bus_val = business.intValue()
          sql"select  business,username,sum(count) as  count from worth_event_count where  business = ${bus_val}  and day = ${day} and dtype= ${dtype} GROUP BY business,username order by  count desc limit  ${limit},${offset}  ".as[WEventUserCount].list
        } else if (null == business && StringUtil.isNotBlank(username)) {
          sql"select  business,username,sum(count) as  count from worth_event_count where  username=${username} and day = ${day} and dtype= ${dtype} GROUP BY business,username order by  count desc  limit  ${limit},${offset}  ".as[WEventUserCount].list
        }
        else {
          sql"select  business,username,sum(count) as  count from worth_event_count where  day = ${day} and dtype= ${dtype}  GROUP BY business,username order by  count desc limit  ${limit},${offset} ".as[WEventUserCount].list
        }
    }
    list.map {
      orgowt =>
        WEventUserCount(Business.getBusinessNameById(Integer.valueOf(orgowt.business)), orgowt.username, orgowt.count)
    }
  }


  def queryOwt(business: Integer, owt: String, day: java.sql.Date, dtype: Int, page: Page): List[WEventOwtCount] = {
    val list = db withSession {
      implicit session: Session =>
        val limit = page.getStart
        val offset = page.getPageSize
        val total =
          if (null != business && StringUtil.isNotBlank(owt)) {
            val bus_val = business.intValue()
            sql"select  count(DISTINCT business,appkey_owt) from worth_event_count where  business = ${bus_val} and appkey_owt=${owt}  and day = ${day} and dtype= ${dtype}".as[Int].first
          }
          else if (null != business && StringUtil.isBlank(owt)) {
            val bus_val = business.intValue()
            sql"select  count(DISTINCT business,appkey_owt) from worth_event_count where  business = ${bus_val}  and day = ${day} and dtype= ${dtype}".as[Int].first
          }
          else if (null == business && StringUtil.isNotBlank(owt)) {
            sql"select  count(DISTINCT business,appkey_owt) from worth_event_count where   appkey_owt=${owt} and day = ${day} and dtype= ${dtype}".as[Int].first
          } else {
            sql"select  count(DISTINCT business,appkey_owt) from worth_event_count where  day = ${day} and dtype= ${dtype}".as[Int].first
          }

        page.setTotalCount(total)
        if (null != business && StringUtil.isNotBlank(owt)) {
          val bus_val = business.intValue()
          sql"select  business,appkey_owt as owt,sum(count) as  count  from worth_event_count where  business = ${bus_val} and appkey_owt=${owt} and day = ${day} and dtype= ${dtype} GROUP BY business,appkey_owt order by  count desc  limit  ${limit},${offset} ".as[WEventOwtCount].list
        } else if (null != business && StringUtil.isBlank(owt)) {
          val bus_val = business.intValue()
          sql"select  business,appkey_owt as owt,sum(count) as  count from worth_event_count where  business = ${bus_val}  and day = ${day} and dtype= ${dtype} GROUP BY business,appkey_owt order by  count desc limit  ${limit},${offset}  ".as[WEventOwtCount].list
        } else if (null == business && StringUtil.isNotBlank(owt)) {
          sql"select  business,appkey_owt as owt,sum(count) as  count from worth_event_count where  appkey_owt=${owt} and day = ${day} and dtype= ${dtype} GROUP BY business,appkey_owt order by  count  desc limit  ${limit},${offset}  ".as[WEventOwtCount].list
        } else {
          sql"select  business,appkey_owt as owt,sum(count) as  count from worth_event_count where  day = ${day} and dtype= ${dtype}   GROUP BY business,appkey_owt order by  count desc limit  ${limit},${offset} ".as[WEventOwtCount].list
        }
    }
    list.map {
      orgowt =>
        WEventOwtCount(Business.getBusinessNameById(Integer.valueOf(orgowt.business)), orgowt.owt, orgowt.count)
    }
  }

  def queryModel(business: Integer, model: String, day: java.sql.Date, dtype: Int, page: Page): List[WEventModelCount] = {
    val list = db withSession {
      implicit session: Session =>
        val limit = page.getStart
        val offset = page.getPageSize
        val total =
          if (null != business && StringUtil.isNotBlank(model)) {
            val bus_val = business.intValue()
            sql"select  count(DISTINCT business,model) from worth_event_count where  business = ${bus_val} and model=${model}  and day = ${day} and dtype= ${dtype}".as[Int].first
          }
          else if (null != business && StringUtil.isBlank(model)) {
            val bus_val = business.intValue()
            sql"select  count(DISTINCT business,model) from worth_event_count where  business = ${bus_val}  and day = ${day} and dtype= ${dtype}".as[Int].first
          }
          else if (null == business && StringUtil.isNotBlank(model)) {
            sql"select  count(DISTINCT business,model) from worth_event_count where   model = ${model} and day = ${day} and dtype= ${dtype}".as[Int].first
          } else {
            sql"select  count(DISTINCT business,model) from worth_event_count where  day = ${day} and dtype= ${dtype}".as[Int].first
          }

        page.setTotalCount(total)
        if (null != business && StringUtil.isNotBlank(model)) {
          val bus_val = business.intValue()
          sql"select  business,model,sum(count) as  count from worth_event_count where  business = ${bus_val} and model=${model} and day = ${day} and dtype= ${dtype}  GROUP BY business,model order by  count desc  limit  ${limit},${offset} ".as[WEventModelCount].list
        } else if (null != business && StringUtil.isBlank(model)) {
          val bus_val = business.intValue()
          sql"select  business,model,sum(count) as  count from worth_event_count where  business = ${bus_val}  and day = ${day} and dtype= ${dtype} GROUP BY business,model order by  count desc limit  ${limit},${offset}  ".as[WEventModelCount].list
        } else if (null == business && StringUtil.isNotBlank(model)) {
          sql"select  business,model,sum(count) as  count from worth_event_count where  model=${model} and day = ${day} and dtype= ${dtype} GROUP BY business,model order by  count desc  limit  ${limit},${offset}  ".as[WEventModelCount].list
        }
        else {
          sql"select  business,model,sum(count) as  count from worth_event_count where  day = ${day} and dtype= ${dtype}  GROUP BY business,model order by  count desc limit  ${limit},${offset} ".as[WEventModelCount].list
        }
    }
    list.map {
      orgowt =>
        WEventModelCount(Business.getBusinessNameById(Integer.valueOf(orgowt.business)), orgowt.model, orgowt.count)
    }
  }

  def modelquery(start: Date, end: Date) = {
    val from = new DateTime(start.getTime)
    val to = new DateTime(end.getTime)
    val days = Days.daysBetween(from, to)
    val xAxis = (0 to days.getDays).map {
      index =>
        DateTimeUtil.format(from.plusDays(index).toDate, DateTimeUtil.DATE_DAY_FORMAT)
    }
    val value = modelReport(start, end)
    Map("xAxis" -> xAxis, "value" -> value)
  }

  def modelReport(start: Date, end: Date, dtype: Int = 0) = {
    val sqlStartDay = new java.sql.Date(start.getTime());
    val sqlEndDay = new java.sql.Date(end.getTime());
    db withSession {
      implicit session: Session =>
        sql"select model,day,count(1) from worth_event_count where  day  BETWEEN  ${sqlStartDay} and ${sqlEndDay}  and dtype = ${dtype}  GROUP BY model ,day order by model, day".as[WEventModelCount].list
    }

  }

  /**
   * 刷新指定日期的统计
   */
  def refresh(day: Date, dtype: Int) {
    val dayTime = new DateTime(day.getTime)
    val start = dayTime.withTimeAtStartOfDay().getMillis
    //清理多个报表的历史数据
    val sqlData = new sql.Date(day.getTime)
    worthEventCount.delete(sqlData,dtype)
    worthAppkeyCount.delete(sqlData,dtype)
    worthModelCount.delete(sqlData,dtype)
    worthEventTask.count(start, start + dayMillis)
  }

  /**
   * 更新没有组织部门的事件用户
   */
  def refreshBusiness() = {
    worthEvent.getBusinessUser().foreach {
      username =>
        val business = mWorthEventService.getBusiness(username)
        worthEvent.updateBusiness(username, business)
    }
  }

  /**
   * 更新没有 owt 的appkey
   */
  def refreshOwt() = {
    worthEvent.getTargetAppkey().foreach {
      appkey =>
        val owt = mWorthEventService.getAppkeyOwt(appkey)
        worthEvent.updateAppkeyOwt(appkey, owt)
    }
  }

  def delete(day:java.sql.Date,dtype:Int) ={
    db withSession {
      implicit session: Session =>
        val statement = WorthEventCount.filter(x => x.day === day && x.dtype === dtype)
        statement.delete
    }
  }

  implicit class QueryHelper[T, E](query: Query[T, E, Seq]) {
    def optionFilter[X, R: CanBeQueryCondition](name: Option[X])(f: (T, X) => R) =
      name.map(v => query.withFilter(f(_, v))).getOrElse(query)
  }

}
