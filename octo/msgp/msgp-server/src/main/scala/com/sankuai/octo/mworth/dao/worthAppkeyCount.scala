package com.sankuai.octo.mworth.dao

import com.sankuai.msgp.common.config.DbConnection
import com.sankuai.msgp.common.model.Page
import com.sankuai.msgp.common.utils.StringUtil
import com.sankuai.octo.mworth.db.Tables._
import org.slf4j.{Logger, LoggerFactory}

import scala.slick.driver.MySQLDriver.simple._
import scala.slick.jdbc.GetResult
import scala.slick.jdbc.StaticQuery.interpolation


/**
 * Created by zava on 15/11/30.
 */
object worthAppkeyCount {
  val LOG: Logger = LoggerFactory.getLogger(worthEventCount.getClass)

  private val db = DbConnection.getWorthPool()
  private val dayMillis = 86400000

  case class WAppkeyCount(id: Long, owt: String, appkey: String, model: String, day: java.sql.Date,
                          dtype: Int, count: Int, createTime: Long)

  case class WOwtCount(owt: String, appkey: String, count: Int)

  implicit val getWOwtCountResult = GetResult(r => WOwtCount(r.<<, r.<<, r.<<))

  case class WOwtModelCount(owt: String, model: String, count: Int)

  implicit val getWOwtModelCountResult = GetResult(r => WOwtModelCount(r.<<, r.<<, r.<<))

  def insert(wAppkeyCount: WAppkeyCount): Long = {
    db withSession {
      implicit session: Session =>
        (WorthAppkeyCount returning WorthAppkeyCount.map(_.id)) +=
          WorthAppkeyCountRow(0, wAppkeyCount.owt, wAppkeyCount.appkey, wAppkeyCount.model, wAppkeyCount.day,
            wAppkeyCount.dtype, wAppkeyCount.count, wAppkeyCount.createTime)
    }
  }

  def batchInsert(list: List[WAppkeyCount]) = {
    db withSession {
      implicit session: Session =>
        val seq = list.map(wAppkeyCount => {
          WorthAppkeyCountRow(0, wAppkeyCount.owt, wAppkeyCount.appkey, wAppkeyCount.model, wAppkeyCount.day,
            wAppkeyCount.dtype, wAppkeyCount.count, wAppkeyCount.createTime)
        })
        WorthAppkeyCount ++= seq
    }
  }


  def query(owt: String, appkey: String, day: java.sql.Date, dtype: Int, page: Page): List[WOwtCount] = {
    db withSession {
      implicit session: Session =>
        val limit = page.getStart
        val offset = page.getPageSize
        val total = if (StringUtil.isBlank(owt) && StringUtil.isBlank(appkey)) {
          sql"select  count(DISTINCT owt,appkey) from worth_appkey_count where  day = ${day} and dtype= ${dtype}".as[Int].first
        } else if (StringUtil.isNotBlank(owt) && StringUtil.isBlank(appkey)) {
          sql"select  count(DISTINCT owt,appkey) from worth_appkey_count where owt = ${owt} and  day = ${day} and dtype= ${dtype}".as[Int].first
        } else if (StringUtil.isBlank(owt) && StringUtil.isNotBlank(appkey)) {
          sql"select  count(DISTINCT owt,appkey) from worth_appkey_count where appkey = ${appkey} and  day = ${day} and dtype= ${dtype}".as[Int].first
        }
        else {
          sql"select  count(DISTINCT owt,appkey) from worth_appkey_count where owt = ${owt} and appkey = ${appkey} and  day = ${day} and dtype= ${dtype}".as[Int].first
        }
        page.setTotalCount(total)
        if (StringUtil.isBlank(owt) && StringUtil.isBlank(appkey)) {
          sql"select  owt,appkey,sum(count) as count from worth_appkey_count where length(appkey)>0 and day = ${day} and dtype= ${dtype}  GROUP BY owt,appkey order by  count desc  limit  ${limit},${offset} ".as[WOwtCount].list
        } else if (StringUtil.isNotBlank(owt) && StringUtil.isBlank(appkey)) {
          sql"select  owt,appkey,sum(count) as count from worth_appkey_count where length(appkey)>0 and  owt = ${owt} and day = ${day} and dtype= ${dtype} and owt=${owt}  GROUP BY owt,appkey order by  count desc limit  ${limit},${offset} ".as[WOwtCount].list
        } else if (StringUtil.isBlank(owt) && StringUtil.isNotBlank(appkey)) {
          sql"select  owt,appkey,sum(count) as count from worth_appkey_count where length(appkey)>0 and  appkey = ${appkey} and  day = ${day} and dtype= ${dtype}  GROUP BY owt,appkey order by  count desc limit  ${limit},${offset}".as[WOwtCount].list
        }
        else {
          sql"select  owt,appkey,sum(count) as count from worth_appkey_count where length(appkey)>0 and  owt = ${owt} and appkey = ${appkey} and  day = ${day} and dtype= ${dtype}  GROUP BY owt,appkey order by  count desc limit  ${limit},${offset}".as[WOwtCount].list
        }
    }
  }

  def queryModel(owt: String, model: String, day: java.sql.Date, dtype: Int, page: Page): List[WOwtModelCount] = {
    db withSession {
      implicit session: Session =>
        val limit = page.getStart
        val offset = page.getPageSize
        val total = if (StringUtil.isBlank(owt) && StringUtil.isBlank(model)) {
          sql"select  count(DISTINCT owt,model) from worth_appkey_count where  day = ${day} and dtype= ${dtype}".as[Int].first
        } else if (StringUtil.isNotBlank(owt) && StringUtil.isBlank(model)) {
          sql"select  count(DISTINCT owt,model) from worth_appkey_count where owt = ${owt} and  day = ${day} and dtype= ${dtype}".as[Int].first
        } else if (StringUtil.isBlank(owt) && StringUtil.isNotBlank(model)) {
          sql"select  count(DISTINCT owt,model) from worth_appkey_count where model = ${model} and  day = ${day} and dtype= ${dtype}".as[Int].first
        }
        else {
          sql"select  count(DISTINCT owt,model) from worth_appkey_count where owt = ${owt} and model = ${model} and  day = ${day} and dtype= ${dtype}".as[Int].first
        }
        page.setTotalCount(total)
        if (StringUtil.isBlank(owt) && StringUtil.isBlank(model)) {
          sql"select  owt,model,sum(count) as count from worth_appkey_count where  day = ${day} and dtype= ${dtype}  GROUP BY owt,model  order by  count desc limit  ${limit},${offset} ".as[WOwtModelCount].list
        } else if (StringUtil.isNotBlank(owt) && StringUtil.isBlank(model)) {
          sql"select  owt,model,sum(count) as count from worth_appkey_count where owt = ${owt} and day = ${day} and dtype= ${dtype} GROUP BY owt,model order by  count desc limit  ${limit},${offset} ".as[WOwtModelCount].list
        } else if (StringUtil.isBlank(owt) && StringUtil.isNotBlank(model)) {
          sql"select  owt,model,sum(count) as count from worth_appkey_count where model = ${model} and  day = ${day} and dtype= ${dtype}  GROUP BY owt,model order by  count desc limit  ${limit},${offset}".as[WOwtModelCount].list
        }
        else {
          sql"select  owt,model,sum(count) as count from worth_appkey_count where owt = ${owt} and model = ${model} and  day = ${day} and dtype= ${dtype}  GROUP BY owt,model order by  count desc limit  ${limit},${offset}".as[WOwtModelCount].list
        }
    }
  }
  def delete(day:java.sql.Date,dtype:Int) ={
    db withSession {
      implicit session: Session =>
        val statement = WorthAppkeyCount.filter(x => x.day === day && x.dtype === dtype)
        statement.delete
    }
  }


}
