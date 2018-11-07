package com.sankuai.octo.mworth.dao

import java.util.Date

import com.sankuai.msgp.common.config.DbConnection
import com.sankuai.msgp.common.model.Page
import com.sankuai.octo.mworth.db.Tables._
import com.sankuai.octo.mworth.model.MWorthConfig
import org.slf4j.{Logger, LoggerFactory}

import scala.slick.driver.MySQLDriver.simple._
import scala.slick.lifted.CanBeQueryCondition

/**
 * Created by zava on 15/11/30.
 */
object worthConfig {
  val LOG: Logger = LoggerFactory.getLogger(worthConfig.getClass)

  private val db = DbConnection.getWorthPool()

  private val DELETED_TYPE_DEL = true

  //联表查询? project,model,functionname?
  def search(functionId: java.lang.Long, fromTime: Date, toTime: Date, page: Page) = {
    val functionIdOpt = if(functionId==null) {
      None
    } else {
      Some(functionId.longValue())
    }
    db withSession {
      implicit session: Session =>
        val statement = WorthConfig.optionFilter(functionIdOpt)(_.functionId === _)
          .filter(x => x.createTime > fromTime.getTime && x.createTime < toTime.getTime
           && x.deleted===false)
        val limit = page.getPageSize
        val offset = page.getStart
        val count = statement.length.run
        page.setTotalCount(count)
        statement.drop(offset).take(limit).list
    }
  }

  /**
   *
   * @param funtionId 函数id
   * @param targetAppkey 指定appkey
   * @param effectived 是否生效
   * @return
   */
  def query(funtionId: Option[Long], targetAppkey: Option[String], effectived: Option[Boolean]) = {
    db withSession {
      implicit session: Session =>
        WorthConfig.filter(x =>x.deleted === !DELETED_TYPE_DEL)
          .optionFilter(funtionId)(_.functionId === _)
          .optionFilter(targetAppkey)(_.targetAppkey === _)
          .optionFilter(effectived)(_.effectived === _).list
    }
  }

  def save(worthConfig: MWorthConfig) : Long = {
    db withSession {
      implicit session: Session =>
        val statement = WorthConfig.filter(x =>
            x.functionId === worthConfig.getFunctionId.longValue() &&
            x.effectived === worthConfig.isEffectived &&
            x.deleted === !DELETED_TYPE_DEL
        )
        if (statement.exists.run) {
          //存在也是新增一条,之前的数据,失效
          statement.map(_.effectived).update(MWorthConfig.EFFECTIVED_DELETE)
        }
        //insert
        val id = (WorthConfig returning WorthConfig.map(_.id)) +=
          WorthConfigRow(0, worthConfig.getFunctionId, Some(worthConfig.getTargetAppkey), worthConfig.getWorth,
            worthConfig.getPrimitiveCostTime, worthConfig.getFromTime.getTime,
            Some(worthConfig.getToTime.getTime), worthConfig.isCoverd, worthConfig.isEffectived,
            worthConfig.isDeleted,worthConfig.getCreateTime.getTime)
        id
    }
  }

  def delete(id: Long) = {
    db withSession {
      implicit session: Session =>
        val statement = WorthConfig.filter(_.id === id)
        if (statement.exists.run) {
          //存在也是新增一条,之前的数据,失效
          statement.map(_.deleted).update(DELETED_TYPE_DEL)
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
