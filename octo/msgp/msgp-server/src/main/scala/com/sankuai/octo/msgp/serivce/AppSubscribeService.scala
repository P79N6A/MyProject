package com.sankuai.octo.msgp.serivce

import com.sankuai.msgp.common.config.DbConnection
import com.sankuai.msgp.common.config.db.msgp.Tables.{AppSubscribe, _}
import com.sankuai.msgp.common.model.Page
import org.apache.commons.lang3.StringUtils
import org.slf4j.{Logger, LoggerFactory}

import scala.slick.driver.MySQLDriver.simple._
import scala.slick.lifted.CanBeQueryCondition

/**
 * Created by zava on 15/11/11.
  *
 */
object AppSubscribeService {
  val LOG: Logger = LoggerFactory.getLogger(AppSubscribeService.getClass)

  private val db = DbConnection.getPool()
  case class AppSubscribeDomain(id: Long, appkey: String, username: String, userId: Long)

  /**
  *  支持
   *  查询一个用户 订阅的appkey
   *  查询一个 appkey被那些人订阅
   */
  def query(appkey:Option[String],userId:Option[Long]) = {
    db withSession {
      implicit session: Session =>
        AppSubscribe.optionFilter(userId)(_.userId === _).optionFilter(appkey)(_.appkey === _).list
    }
  }
  def search(appkey:String,userId:Long,page:Page) = {
    val appkeyOpt = if(StringUtils.isEmpty(appkey)) {
      None
    } else {
       Some(s"%$appkey%")
    }
    db withSession {
      implicit session: Session =>
        val statement = AppSubscribe.optionFilter(Some(userId))(_.userId === _)
          .optionFilter(appkeyOpt)(_.appkey like _)
        println(statement.selectStatement)
        val limit = page.getPageSize
        val offset = page.getStart
        val count = statement.length.run
        page.setTotalCount(count)
        statement.drop(offset).take(limit).list
    }
  }
  def insert(subscribe: AppSubscribeDomain) : Long = {
    if(query(Some(subscribe.appkey),Some(subscribe.userId)).isEmpty){
      db withSession {
        implicit session: Session =>
          (AppSubscribe returning AppSubscribe.map(_.id)) += AppSubscribeRow(0, subscribe.appkey,subscribe.username,subscribe.userId)
      }
    }else{
        1L
    }
  }
  def insert(appkey:String,username:String,userId:Long)  : Long = {
    insert(AppSubscribeDomain(0L,appkey,username,userId))
  }

  implicit class QueryHelper[T, E](query: Query[T, E, Seq]) {
    def optionFilter[X, R: CanBeQueryCondition](name: Option[X])(f: (T, X) => R) =
      name.map(v => query.withFilter(f(_, v))).getOrElse(query)
  }

  /**
   *
   * @param appkey
   * @param userId
   * 参数二选一
   * @return
   */
  def delete(appkey:String,userId:java.lang.Long)={
    val userIdOpt = if(null == userId) {
      None
    } else {
      Some(userId.toLong)
    }
    val appkeyOpt = if(StringUtils.isBlank(appkey)) {
      None
    } else {
      Some(appkey)
    }
    if(userIdOpt.isEmpty && appkeyOpt.isEmpty){
       0
    }else{
      db withSession {
        implicit  session: Session =>
          val statement = AppSubscribe.optionFilter(appkeyOpt)(_.appkey === _).optionFilter(userIdOpt)(_.userId === _)
          if(statement.exists.run) {
            statement.delete
          }
      }
    }
  }
}
