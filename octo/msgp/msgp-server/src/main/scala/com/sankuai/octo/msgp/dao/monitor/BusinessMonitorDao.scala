package com.sankuai.octo.msgp.dao.monitor

import com.sankuai.meituan.auth.util.UserUtils
import com.sankuai.msgp.common.config.DbConnection
import com.sankuai.msgp.common.utils.helper.JsonHelper
import com.sankuai.msgp.common.config.db.msgp.Tables._
import com.sankuai.msgp.common.service.org.{OpsService, OrgSerivce}
import com.sankuai.octo.msgp.dao.kpi.BusinessDashDao
import com.sankuai.octo.msgp.model.SubStatus
import org.slf4j.LoggerFactory
import play.api.libs.json.Json

import scala.slick.driver.MySQLDriver.simple._

object BusinessMonitorDAO {
  private val db = DbConnection.getPool()
  private val logger = LoggerFactory.getLogger(this.getClass)

  def wrapperBusinessMonitorRow(id: Long, screenId: Long, strategy: Int, desc: String, threshold: Int, duration: Int) = {
    BusinessMonitorRow(id, screenId, strategy, desc, threshold, duration)
  }

  // 获取需要监控的指标
  def getAllBusinessMonitor = {
    db withSession {
      implicit session: Session =>
        BusinessMonitor.join(AppScreen).on(_.screenId === _.id).list
    }
  }

  def insert(row: BusinessMonitorRow): Option[Long] = {
    db withSession {
      implicit session: Session =>
        (BusinessMonitor returning BusinessMonitor.map(_.id)).insertOrUpdate(row)
    }
  }

  def get(screenId: Long, strategy: Int) = {
    db withSession {
      implicit session: Session =>
        BusinessMonitor.filter(x => x.screenId === screenId && x.strategy === strategy).firstOption
    }
  }

  def get(screenId: Long) = {
    db withSession {
      implicit session: Session =>
        val list = BusinessMonitor.filter(_.screenId === screenId).list
        list.map { item =>
          val subscribeOpt = getSubscribeWithId(item.id)
          val subscribe = if (subscribeOpt.nonEmpty) {
            val subscribe = subscribeOpt.get
            Map("xm" -> subscribe.xm,
              "sms" -> subscribe.sms,
              "email" -> subscribe.email)
          } else {
            Map("xm" -> 1,
              "sms" -> 1,
              "email" -> 1)
          }
          Map("businessMonitor" -> item,
            "subscribe" -> subscribe)
        }
    }
  }

  def delete(id: Long) = {
    db withSession {
      implicit session: Session =>
        BusinessMonitor.filter(_.id === id).delete
    }
  }

  /**
   * @param businessMonitorId 告警项ID
   * @param mode 订阅渠道：xm、sms、email
   * @param newStatus 订阅状态: 0 Sub、1 UnSub
   */
  case class SubscribeUpdateRequest(businessMonitorId: Long, mode: String, newStatus: Int)

  implicit val subscribeReads = Json.reads[SubscribeUpdateRequest]
  implicit val subscribeWrites = Json.writes[SubscribeUpdateRequest]

  def subscribe(json: String) = {
    try {
      val user = UserUtils.getUser
      val request = Json.parse(json).validate[SubscribeUpdateRequest].get
      insertOrUpdateSubscribe(request.businessMonitorId, user.getId, user.getLogin, user.getName, request.mode, request.newStatus)
      JsonHelper.dataJson(true)
    } catch {
      case e: Exception => logger.error("", e)
        JsonHelper.errorJson(e.getMessage)
    }
  }

  def getSubscribeWithId(businessMonitorId: Long) = {
    db withSession {
      implicit session: Session =>
        try {
          val user = UserUtils.getUser
          BusinessTriggerSubscribe.filter(x => x.businessMonitorId === businessMonitorId && x.userId === user.getId).list.headOption
        } catch {
          case e: Exception => logger.error("", e)
            None
        }
    }
  }

  def getSubscribe(businessMonitorId: Long) = {
    db withSession {
      implicit session: Session =>
        BusinessTriggerSubscribe.filter(x => x.businessMonitorId === businessMonitorId).list
    }
  }

  def getTriggerSubs(businessMonitorId: Long) = {
    val subs = getSubscribe(businessMonitorId)
    if (subs.nonEmpty) {
      val xmUserIds = subs.filter(_.xm == SubStatus.Sub.id.toByte).map(_.userId).toList
      val smsUserIds = subs.filter(_.sms == SubStatus.Sub.id.toByte).map(_.userId).toList
      val emailUserIds = subs.filter(_.email == SubStatus.Sub.id.toByte).map(_.userId).toList
      Some(xmUserIds, smsUserIds, emailUserIds)
    } else {
      None
    }
  }

  def insertOrUpdateSubscribe(businessMonitorId: Long, userId: Int, userLogin: String, userName: String, mode: String, newStatus: Int) {
    db withSession {
      implicit session: Session =>
        val statement = BusinessTriggerSubscribe.filter(x => x.businessMonitorId === businessMonitorId && x.userId === userId)
        if (statement.exists.run) {
          //update
          mode match {
            case "xm" =>
              statement.map(x => x.xm).update(newStatus.toByte)
            case "sms" =>
              statement.map(x => x.sms).update(newStatus.toByte)
            case "email" =>
              statement.map(x => x.email).update(newStatus.toByte)
            case _ =>
              logger.error(s"illegal mode $mode from $userLogin ")
          }
        } else {
          //insert
          mode match {
            case "xm" =>
              BusinessTriggerSubscribe += BusinessTriggerSubscribeRow(0, businessMonitorId, userId, userLogin, userName, newStatus.toByte, SubStatus.UnSub.id.toByte, SubStatus.UnSub.id.toByte)
            case "sms" =>
              BusinessTriggerSubscribe += BusinessTriggerSubscribeRow(0, businessMonitorId, userId, userLogin, userName, SubStatus.UnSub.id.toByte, newStatus.toByte, SubStatus.UnSub.id.toByte)
            case "email" =>
              BusinessTriggerSubscribe += BusinessTriggerSubscribeRow(0, businessMonitorId, userId, userLogin, userName, SubStatus.UnSub.id.toByte, SubStatus.UnSub.id.toByte, newStatus.toByte)
            case _ =>
              logger.error(s"illegal mode mode from $userLogin")
          }
        }
    }
  }

  def addDefaultMonitor(screenId: Long) = {
    try {
      val user = UserUtils.getUser
      // 增加默认报警
      insert(BusinessMonitorDAO.wrapperBusinessMonitorRow(0, screenId, 1, "下降百分比(比基线)", 20, 3))
      val id1 = get(screenId, 1)
      if (id1.nonEmpty) {
        insertOrUpdateSubscribe(id1.get.id, user.getId, user.getLogin, user.getName, "xm", 0)
      }

      insert(BusinessMonitorDAO.wrapperBusinessMonitorRow(0, screenId, 3, "上升百分比(比基线)", 20, 3))
      val id2 = get(screenId, 3)
      if (id2.nonEmpty) {
        insertOrUpdateSubscribe(id2.get.id, user.getId, user.getLogin, user.getName, "xm", 0)
      }
    } catch {
      case e: Exception => logger.error("BusinessMonitorDao insertOrUpdateSubscribe failed", e)
        e.getMessage
    }
  }

  def syncBusinessMonitor() = {
    val owts = OpsService.getStreeServiceOwt
    val all = BusinessDashDao.getAll
    all.foreach { item =>
      val owt = s"meituan.${item.owt}"
      val screenId = item.screenId
      val headOpt = owts.find(_.key == owt)
      if (headOpt.nonEmpty) {
        val logins = headOpt.get.op_admin.split(",")
        logins.foreach { login =>
          val userOpt = OrgSerivce.employee(login)
          if (userOpt.nonEmpty) {
            val user = userOpt.get

            // 增加默认报警
            insert(BusinessMonitorDAO.wrapperBusinessMonitorRow(0, screenId, 1, "下降百分比(比基线)", 20, 3))
            val id1 = get(screenId, 1)
            if (id1.nonEmpty) {
              insertOrUpdateSubscribe(id1.get.id, user.getId, user.getLogin, user.getName, "xm", 0)
            }

            insert(BusinessMonitorDAO.wrapperBusinessMonitorRow(0, screenId, 3, "上升百分比(比基线)", 20, 3))
            val id2 = get(screenId, 3)
            if (id2.nonEmpty) {
              insertOrUpdateSubscribe(id2.get.id, user.getId, user.getLogin, user.getName, "xm", 0)
            }

          }
        }
      }
    }
  }

  def main(args: Array[String]) {
    println(OrgSerivce.employee("wangyanzhao"))
    syncBusinessMonitor
  }
}
