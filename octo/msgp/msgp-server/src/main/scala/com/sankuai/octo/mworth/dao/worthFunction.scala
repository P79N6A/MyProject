package com.sankuai.octo.mworth.dao

import java.util.Date

import com.sankuai.msgp.common.config.DbConnection
import com.sankuai.msgp.common.model.Page
import com.sankuai.octo.mworth.db.Tables._
import com.sankuai.octo.mworth.model.MWorthFunction
import org.slf4j.{Logger, LoggerFactory}

import scala.slick.driver.MySQLDriver.simple._
import scala.slick.lifted.CanBeQueryCondition

/**
 * Created by zava on 15/11/30.
 */
object worthFunction {
  val LOG: Logger = LoggerFactory.getLogger(worthFunction.getClass)
  private val DELETED_TYPE_DEL = true

  private val db = DbConnection.getWorthPool()

  def search(project: String, model: String, functionName: String, fromTime: Date, toTime: Date, page: Page): List[WorthFunctionRow] = {
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

    search(projectOpt, modelOpt, functionNameOpt, fromTime, toTime, page)
  }

  def search(project: Option[String], model: Option[String], functionName: Option[String], fromTime: Date, toTime: Date, page: Page): List[WorthFunctionRow] = {
    db withSession {
      implicit session: Session =>
        val statement = WorthFunction.optionFilter(project)(_.project === _)
          .optionFilter(model)(_.model === _)
          .optionFilter(functionName)(_.functionName === _)
          .filter(x => x.createTime > fromTime.getTime && x.createTime < toTime.getTime && x.deleted === !DELETED_TYPE_DEL)
        val limit = page.getPageSize
        val offset = page.getStart
        val count = statement.length.run
        page.setTotalCount(count)
        statement.drop(offset).take(limit).list
    }
  }

  def query(project: Option[String], model: Option[String], functionName: Option[String]) = {
    db withSession {
      implicit session: Session =>
        WorthFunction.
          optionFilter(project)(_.project === _)
          .optionFilter(model)(_.model === _)
          .optionFilter(functionName)(_.functionName === _)
           .filter(x => x.deleted === !DELETED_TYPE_DEL).list
    }
  }

  def get(id: Long)= {
    db withSession {
      implicit session: Session =>
        val statement = WorthFunction.filter(x =>
          x.id === id
        )
        statement.list
    }
  }

  def insert(wFunction: MWorthFunction): Long = {
    db withSession {
      implicit session: Session =>
        (WorthFunction returning WorthFunction.map(_.id)) +=
          WorthFunctionRow(0, wFunction.getProject, wFunction.getModel, wFunction.getFunctionName,
            wFunction.getFunctionDesc, wFunction.getFunctionType,
            wFunction.getCreateTime.getTime, wFunction.isDeleted)
    }
  }

  def save(wFunction: MWorthFunction): Long = {
    db withSession {
      implicit session: Session =>
        val statement = wFunction.getId match {
          case y: java.lang.Long =>
            WorthFunction.filter(x =>
              x.id === wFunction.getId.longValue() &&
                x.deleted === wFunction.isDeleted
            )
          case null =>
            WorthFunction.filter(x =>
              x.project === wFunction.getProject &&
                x.model === wFunction.getModel &&
                x.functionName === wFunction.getFunctionName &&
                x.deleted === wFunction.isDeleted
            )
        }

        if (statement.exists.run) {
          statement.map(x => (x.project, x.model, x.functionName, x.functionType, x.functionDesc, x.deleted)).
            update(wFunction.getProject, wFunction.getModel, wFunction.getFunctionName,
              wFunction.getFunctionType, wFunction.getFunctionDesc, wFunction.isDeleted)
        } else {
          val id = (WorthFunction returning WorthFunction.map(_.id)) +=
            WorthFunctionRow(0, wFunction.getProject, wFunction.getModel, wFunction.getFunctionName,
              wFunction.getFunctionDesc, wFunction.getFunctionType,
              wFunction.getCreateTime.getTime, wFunction.isDeleted)
          id
        }

    }
  }

  def delete(id: Long) = {
    db withSession {
      implicit session: Session =>
        val statement = WorthFunction.filter(x =>
          x.id === id
        )
        if (statement.exists.run) {
          statement.map(x => (x.deleted)).update(DELETED_TYPE_DEL)
        } else {
          1L
        }
    }
  }



  implicit class QueryHelper[T, E](query: Query[T, E, Seq]) {
    def optionFilter[X, R: CanBeQueryCondition](name: Option[X])(f: (T, X) => R) =
      name.map(v => query.withFilter(f(_, v))).getOrElse(query)
  }

}
