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
object worthModelCount {
  val LOG: Logger = LoggerFactory.getLogger(worthEventCount.getClass)

  private val db = DbConnection.getWorthPool()
  private val dayMillis = 86400000

  case class WModelModelCount(id: Long, project: String, model: String, functionName: String,
                         day: java.sql.Date,dtype: Int, count: Int, createTime: Long)

  case class WModelCount(model: String, count: Int)

  case class WModelMethodCount(model: String,functionName:String, count: Int)

  implicit val getWModelCountResult = GetResult(r => WModelCount(r.<<, r.<<))

  implicit val getWModelMethodCountResult = GetResult(r => WModelMethodCount(r.<<, r.<<, r.<<))

  def insert(wModelCount: WModelModelCount): Long = {
    db withSession {
      implicit session: Session =>
        (WorthModelCount returning WorthModelCount.map(_.id)) +=
          WorthModelCountRow(0, wModelCount.functionName, wModelCount.project,wModelCount.model, wModelCount.day,
            wModelCount.dtype, wModelCount.count, wModelCount.createTime)
    }
  }

  def batchInsert(list: List[WModelModelCount]) = {
    db withSession {
      implicit session: Session =>
        val seq = list.map(wModelCount => {
          WorthModelCountRow(0, wModelCount.functionName, wModelCount.project,wModelCount.model, wModelCount.day,
            wModelCount.dtype, wModelCount.count, wModelCount.createTime)
        })
        WorthModelCount ++= seq
    }
  }


  def queryMethod(model: String, functionName: String, day: java.sql.Date, dtype: Int, page: Page) = {
    val sqlDay = new java.sql.Date(day.getTime());
    db withSession {
      implicit session: Session =>
        val limit = page.getStart
        val offset = page.getPageSize
        val total = if (StringUtil.isBlank(functionName) && StringUtil.isBlank(model) ) {
          sql"select  count(DISTINCT model,function_name) from worth_model_count where   day = ${day} and dtype= ${dtype}".as[Int].first
        }
        else if (StringUtil.isBlank(functionName) && StringUtil.isNotBlank(model) ) {
          sql"select  count(DISTINCT model,function_name) from worth_model_count where model=${model} and day = ${day} and dtype= ${dtype}".as[Int].first
        }
        else if (StringUtil.isNotBlank(functionName) && StringUtil.isBlank(model) ) {
          sql"select  count(DISTINCT model,function_name) from worth_model_count where function_name=${functionName} and day = ${day} and dtype= ${dtype}".as[Int].first
        }
        else{
          1
        }
        page.setTotalCount(total)
        if (StringUtil.isBlank(functionName) && StringUtil.isBlank(model) ) {
          sql"select model,function_name as functionName,sum(count) as count from worth_model_count where day = ${day} and dtype= ${dtype}  GROUP BY model,function_name  order by  count desc limit  ${limit},${offset} ".as[WModelMethodCount].list
        }else if (StringUtil.isBlank(functionName) && StringUtil.isNotBlank(model) ) {
          sql"select  model,function_name as functionName,sum(count) as count from worth_model_count where model=${model} and   day = ${day} and dtype= ${dtype}  GROUP BY model,function_name  order by  count desc limit  ${limit},${offset} ".as[WModelMethodCount].list
        }
        else if (StringUtil.isNotBlank(functionName) && StringUtil.isBlank(model) ) {
          sql"select  model,function_name as functionName,sum(count) as count from worth_model_count where function_name=${functionName}  and   day = ${day} and dtype= ${dtype}  GROUP BY model,function_name  order by  count desc limit  ${limit},${offset} ".as[WModelMethodCount].list
        }
        else{
          sql"select  model,function_name as functionName,sum(count) as count from worth_model_count where model = ${model}  and function_name=${functionName}  and  day = ${day} and dtype= ${dtype}  GROUP BY model,function_name order by  count desc limit  ${limit},${offset}".as[WModelMethodCount].list
        }
    }
  }

  def delete(day:java.sql.Date,dtype:Int) ={
    db withSession {
      implicit session: Session =>
        val statement = WorthModelCount.filter(x => x.day === day && x.dtype === dtype)
        statement.delete
    }
  }


}
