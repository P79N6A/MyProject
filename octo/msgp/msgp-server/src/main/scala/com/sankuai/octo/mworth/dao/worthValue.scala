package com.sankuai.octo.mworth.dao

import java.util.Date

import com.sankuai.msgp.common.config.DbConnection
import com.sankuai.octo.mworth.db.Tables._
import com.sankuai.octo.mworth.service.worthCountService
import org.joda.time.DateTime
import org.slf4j.{Logger, LoggerFactory}

import scala.collection.JavaConverters._
import scala.collection.concurrent.TrieMap
import scala.collection.mutable.ListBuffer
import scala.slick.driver.MySQLDriver.simple._
import scala.slick.jdbc.GetResult
import scala.slick.jdbc.StaticQuery.interpolation
import scala.slick.lifted.CanBeQueryCondition

/**
 * Created by zava on 15/11/30.
 */
object worthValue {
  val LOG: Logger = LoggerFactory.getLogger(worthEvent.getClass)

  private val db = DbConnection.getWorthPool()

  case class WValue(id: Long, funtionId: Long, business: Int, owt: Option[String], project: String, model: String,
                    functionName: String, worth: Int, totalWorth: Int,
                    primitiveTotalWorth: Int, costTime: Long, worthTime: Long,
                    createTime: Long, deleted: Boolean)

  case class WModelValue(model: String, totalWorth: Int, ptotalWorth: Int)

  implicit val getWModelValueResult = GetResult(r => WModelValue(r.<<, r.<<, r.<<))

  def query(project: String, model: String, functionName: String, targetAppkey: String, fromTime: Date, toTime: Date): List[WorthValueRow] = {
    val projectOpt = project match {
      case value: String => Some(value)
      case null => None
    }

    val modelOpt = model match {
      case value: String => Some(value)
      case null => None
    }
    val functionNameOpt = functionName match {
      case value: String => Some(value)
      case null => None
    }
    val targetAppkeyOpt = targetAppkey match {
      case value: String => Some(value)
      case null => None
    }

    search(projectOpt, modelOpt, functionNameOpt, targetAppkeyOpt, fromTime, toTime)
  }

  private def search(project: Option[String], model: Option[String], functionName: Option[String], targetAppkey: Option[String], fromTime: Date, toTime: Date): List[WorthValueRow] = {
    db withSession {
      implicit session: Session =>
        WorthValue.optionFilter(project)(_.project === _)
          .optionFilter(model)(_.model === _)
          .optionFilter(functionName)(_.functionName === _)
          .filter(x => x.createTime > fromTime.getTime && x.createTime < toTime.getTime).list
    }
  }

  def insert(wValue: WValue): Long = {
    db withSession {
      implicit session: Session =>
        (WorthValue returning WorthValue.map(_.id)) +=
          WorthValueRow(0, wValue.funtionId, wValue.business, wValue.owt.getOrElse(""), wValue.project, wValue.model, wValue.functionName,
            wValue.worth, wValue.totalWorth,
            wValue.primitiveTotalWorth, wValue.costTime, wValue.worthTime, wValue.createTime,
            wValue.deleted)
    }
  }


  def batchInsert(list: List[WValue]) = {
    db withSession {
      implicit session: Session =>
        val seq = list.map(wValue => {
          WorthValueRow(0, wValue.funtionId, wValue.business, wValue.owt.getOrElse(""), wValue.project, wValue.model, wValue.functionName,
            wValue.worth, wValue.totalWorth,
            wValue.primitiveTotalWorth, wValue.costTime, wValue.worthTime, wValue.createTime,
            wValue.deleted)
        })
        WorthValue ++= seq
    }
  }

  def delete(project: Option[String], model: Option[String], functionName: Option[String], targetAppkey: Option[String], fromTime: Date, toTime: Date) = {
    db withSession {
      implicit session: Session =>
        val statement = WorthValue.optionFilter(project)(_.project === _)
          .optionFilter(model)(_.model === _)
          .optionFilter(functionName)(_.functionName === _)
          .filter(x => x.createTime > fromTime.getTime && x.createTime < toTime.getTime)
        if (statement.exists.run) {
          statement.delete
        } else {
          1L
        }
    }
  }

  def delete(fromTime: Date, toTime: Date) = {
    db withSession {
      implicit session: Session =>
        val statement = WorthValue.filter(x => x.worthTime > fromTime.getTime && x.worthTime < toTime.getTime)
        if (statement.exists.run) {
          statement.delete
        } else {
          1L
        }
    }
  }

  /**
   * echart 输出报告
   * 指定部门,输出每一个功能模块的价值报告,
   * 内容包含:1:未接入情况下,实际接入情况,完全接入情况
   *
   */
  def echart(business: Integer) = {
    val qTimes = getQtime()
    val modelValueMap = TrieMap[String, ListBuffer[Int]]()
    for (i <- 0 to qTimes.size - 2) {
      val modelValues = qreport(business.toInt, qTimes(i).getMillis, qTimes(i + 1).getMillis)
      modelValues.foreach {
        modelValue =>
          val list = modelValueMap.getOrElseUpdate(modelValue.model, ListBuffer[Int]())
          list.append(modelValue.totalWorth, modelValue.ptotalWorth, 0)
      }
    }
    modelValueMap.asJava
  }


  /**
   * 查询指定时间 价值报告
   */
  def qreport(business: Int, start: Long, end: Long) = {
    db withSession {
      implicit session: Session =>
        sql"select model ,sum(primitive_total_worth),sum(total_worth) from worth_value where business=${business} and worth_time BETWEEN ${start} and ${end} group by model".as[WModelValue].list
    }
  }

  private def getQtime() = {
    val dateTime = new DateTime().withDayOfMonth(1).withMillisOfDay(0);
    val q1_start = dateTime.withMonthOfYear(1);
    val q2_start = dateTime.withMonthOfYear(4)
    val q3_start = dateTime.withMonthOfYear(7);
    val q4_start = dateTime.withMonthOfYear(10);
    val q4_end = dateTime.plusYears(1).withMonthOfYear(1);
    List(q1_start, q2_start, q3_start, q4_start, q4_end);
  }

  def refresh(day: Date) = {
    val dayTime = new DateTime(day.getTime)
    val start = dayTime.withTimeAtStartOfDay().toDate
    val tomorrow = dayTime.plusDays(1).withTimeAtStartOfDay()
    val end = tomorrow.toDate
    delete(start, end)
    worthCountService.count(tomorrow)
  }

  implicit class QueryHelper[T, E](query: Query[T, E, Seq]) {
    def optionFilter[X, R: CanBeQueryCondition](name: Option[X])(f: (T, X) => R) =
      name.map(v => query.withFilter(f(_, v))).getOrElse(query)
  }
}
