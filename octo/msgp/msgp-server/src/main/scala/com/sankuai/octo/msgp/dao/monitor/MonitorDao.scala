package com.sankuai.octo.msgp.dao.monitor

import com.sankuai.msgp.common.config.DbConnection
import com.sankuai.msgp.common.config.db.msgp.Tables._
import com.sankuai.msgp.common.model.{Page, ServiceModels}
import com.sankuai.msgp.common.utils.StringUtil
import com.sankuai.octo.msgp.model.SubStatus
import com.sankuai.octo.msgp.model.SubStatus.SubStatus
import com.sankuai.octo.msgp.serivce.monitor.MonitorConfig
import com.sankuai.octo.msgp.utils._
import org.slf4j.{Logger, LoggerFactory}

import scala.slick.driver.MySQLDriver.simple._
import scala.slick.jdbc.GetResult
import scala.slick.jdbc.StaticQuery.interpolation

object MonitorDAO {
  val LOG: Logger = LoggerFactory.getLogger(MonitorConfig.getClass)
  private val db = DbConnection.getPool()

  case class MonitorCount(appkey: String, count: Int)

  implicit val getMonitorCountResult = GetResult(r => MonitorCount(r.<<, r.<<))

  // 记录报警事件,并返回这个报警在数据库中的ID
  def insertEvent(event: EventRow) = {
    db withSession {
      implicit session: Session =>
        val id = (Event returning Event.map(_.id)) += event
        id
    }
  }

  // do ack for event
  def ackEvent(eventId: Long, ackUser: String) {
    db withSession {
      implicit session: Session =>
        val now = System.currentTimeMillis()
        val statement = Event.filter(x => x.id === eventId).map(x => (x.status, x.ackTime, x.ackUser))
        val a = statement.first
        if (a._3 != null || a._3 != "") {
          val l = (a._3.split(",") :+ ackUser).distinct.mkString(",")
          statement.update(0, now, l)
        } else {
          statement.update(0, now, ackUser)
        }
    }
  }

  def getAllEventsByItems(appkey: String, startTime: Long, endTime: Long) = {
    db withSession {
      implicit session =>
        val statement = Event.filter(x => x.appkey === appkey && x.createTime > startTime && x.createTime < endTime)
          .sortBy(r => (r.createTime.desc, r.item.asc))
        val itemEvents = statement.map(x => (x.item, x.createTime)).list
        itemEvents
    }
  }

  // get events
  def getEvents(appkey: String, startTime: Long, endTime: Long, page: Page) = {
    db withSession {
      implicit session =>
        val statement = Event.filter(x => x.appkey === appkey && x.createTime > startTime && x.createTime < endTime)
          .sortBy(r => (r.createTime.desc, r.item.asc))
        val limit = page.getPageSize
        val offset = page.getStart
        val count = statement.length.run
        page.setTotalCount(count)
        val eventList = statement.drop(offset).take(limit).list
        eventList
    }
  }

  def getEventCount(appkey: String, spanname: String, startTime: Long, endTime: Long) = {
    db withSession {
      implicit session =>
        val statement = if (StringUtil.isBlank(spanname) || spanname.equalsIgnoreCase("all")) {
          Event.filter(x => x.appkey === appkey && x.createTime > startTime && x.createTime < endTime)
        } else {
          Event.filter(x => x.appkey === appkey && x.spanname === spanname && x.createTime > startTime && x.createTime < endTime)
        }
        statement.length.run
    }
  }

  /** *
    * 获取一段时间的所有异常
    *
    * @param startTime
    * @param endTime
    * @return
    */
  def eventCount(startTime: Long, endTime: Long) = {
    db withSession {
      implicit session =>
        sql"select appkey ,count(*) from event where create_time >= ${
          startTime
        } and create_time < ${
          endTime
        }  group by appkey ".as[MonitorCount].list
    }
  }


  def insertOrUpdateAllSubscribe(appkey: String, userId: Int, userLogin: String, userName: String, xm: SubStatus, sms: SubStatus, email: SubStatus) {
    db withSession {
      implicit session: Session =>
        val statement = TriggerSubscribe.filter(x => x.appkey === appkey && x.triggerId === 0L && x.userId === userId)
        // 插入or修改全局订阅状态
        if (statement.exists.run) {
          //update
          statement.map(x => (x.xm, x.sms, x.email)).update(xm.id.toByte, sms.id.toByte, email.id.toByte)
        } else {
          //insert
          TriggerSubscribe += TriggerSubscribeRow(0, appkey, 0L, userId, userLogin, userName, xm.id.toByte, sms.id.toByte, email.id.toByte)
        }
        // 修改其它单独配置的状态
        TriggerSubscribe.filter(x => x.appkey === appkey && x.userId === userId).map(x => (x.xm, x.sms, x.email)).update(xm.id.toByte, sms.id.toByte, email.id.toByte)
    }
  }

  /**
    * 给用户默认订阅上报警
    * TODO:当用户主动取消订阅时候不在默认修改
    */
  def defaultSubscribe(appkey: String, triggerId: Long, user: ServiceModels.User) {
    db withSession {
      implicit session: Session =>
        val statement = TriggerSubscribe.filter(x => x.appkey === appkey && x.triggerId === triggerId && x.userId === user.id)
        if (statement.exists.run) {
          //更新xm 为订阅
          statement.map(x => (x.xm)).update(SubStatus.Sub.id.toByte)
        } else {
          //帮助订阅
          TriggerSubscribe += TriggerSubscribeRow(0, appkey, triggerId, user.id, user.login, user.name, SubStatus.Sub.id.toByte, SubStatus.UnSub.id.toByte, SubStatus.UnSub.id.toByte)
        }
    }
  }

  def insertOrUpdateSubscribe(appkey: String, triggerId: Long, userId: Int, userLogin: String, userName: String, mode: String, newStatus: Int) {
    db withSession {
      implicit session: Session =>
        val statement = TriggerSubscribe.filter(x => x.appkey === appkey && x.triggerId === triggerId && x.userId === userId)
        if (statement.exists.run) {
          //update
          mode match {
            case "xm" =>
              statement.map(x => (x.xm)).update(newStatus.toByte)
            case "sms" =>
              statement.map(x => (x.sms)).update(newStatus.toByte)
            case "email" =>
              statement.map(x => (x.email)).update(newStatus.toByte)
            case _ =>
              LOG.error(s"illegal mode ${
                mode
              } from $userLogin $appkey")
          }
        } else {
          //insert
          mode match {
            case "xm" =>
              TriggerSubscribe += TriggerSubscribeRow(0, appkey, triggerId, userId, userLogin, userName, newStatus.toByte, SubStatus.UnSub.id.toByte, SubStatus.UnSub.id.toByte)
            case "sms" =>
              TriggerSubscribe += TriggerSubscribeRow(0, appkey, triggerId, userId, userLogin, userName, SubStatus.UnSub.id.toByte, newStatus.toByte, SubStatus.UnSub.id.toByte)
            case "email" =>
              TriggerSubscribe += TriggerSubscribeRow(0, appkey, triggerId, userId, userLogin, userName, SubStatus.UnSub.id.toByte, SubStatus.UnSub.id.toByte, newStatus.toByte)
            case _ =>
              LOG.error(s"illegal mode ${
                mode
              } from $userLogin $appkey")
          }
        }
    }
  }

  def deleteTriggerSubscribe(appkey: String, triggerId: Long) = {
    db withSession {
      implicit session: Session =>
        TriggerSubscribe.filter(x => x.appkey === appkey && x.triggerId === triggerId).delete
    }
  }

  def deleteTriggerSubscribe(appkey: String, userLogin: String) = {
    db withSession {
      implicit session: Session =>
        TriggerSubscribe.filter(x => x.appkey === appkey && x.userLogin === userLogin).delete
    }
  }

  def deleteTriggerSubscribeByLogin(userLogin: String) = {
    db withSession {
      implicit session: Session =>
        TriggerSubscribe.filter(x => x.userLogin === userLogin).delete
    }
  }


  def getSubscribes(appkey: String, userId: Int) = {
    db withSession {
      implicit session: Session =>
        TriggerSubscribe.filter(x => x.appkey === appkey && x.userId === userId).list
    }
  }

  def getSubScribe(appkey: String, triggerId: Long) = {
    db withSession {
      implicit session: Session =>
        TriggerSubscribe.filter(x => x.appkey === appkey && (x.triggerId === triggerId || x.triggerId === 0L)).list
    }
  }

  def getSubScribeByUser(userLogin: String) = {
    db withSession {
      implicit session: Session =>
        TriggerSubscribe.filter(x => x.userLogin === userLogin && (x.xm === 0L.toByte || x.sms === 0L.toByte || x.email === 0L.toByte)).list
    }
  }
}
