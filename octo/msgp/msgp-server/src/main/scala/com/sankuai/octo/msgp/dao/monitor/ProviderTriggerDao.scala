package com.sankuai.octo.msgp.dao.monitor

import com.sankuai.msgp.common.config.DbConnection
import com.sankuai.msgp.common.model.{EntityType, SubscribeStatus}
import com.sankuai.msgp.common.config.db.msgp.Tables._
import com.sankuai.msgp.common.utils.client.BorpClient
import com.sankuai.msgp.common.model.MonitorModels.ProviderTrigger
import com.sankuai.octo.msgp.model.SubStatus
import com.sankuai.octo.msgp.model.SubStatus.SubStatus
import org.slf4j.{Logger, LoggerFactory}
import play.api.libs.json.Json

import scala.slick.driver.MySQLDriver.simple._

/**
  * Created by zava on 16/7/28.
  */
object ProviderTriggerDao {

  val LOG: Logger = LoggerFactory.getLogger(ProviderTriggerDao.getClass)
  private val db = DbConnection.getPool()

  def getTriggers(appkey: String) = {
    db withSession {
      implicit session: Session =>
        val triggers = AppkeyProviderTrigger.filter(x => x.appkey === appkey).list
        triggers
    }
  }

  def getTrigger(appkey: String, item: String) = {
    db withSession {
      implicit session: Session =>
        AppkeyProviderTrigger.filter(x => x.appkey === appkey && x.item === item).firstOption
    }
  }

  def exitTrigger(appkey: String, trigger: ProviderTrigger) = {
    db withSession {
      implicit session: Session =>
        val statement = AppkeyProviderTrigger.filter(x => x.appkey === appkey && x.item === trigger.item && x.function === trigger.function)
        statement.exists.run
    }
  }


  def insertOrUpdateTrigger(appkey: String, trigger: ProviderTrigger) = {
    db withSession {
      implicit session: Session =>
        val statement = AppkeyProviderTrigger.filter(x => x.appkey === appkey && x.item === trigger.item && x.function === trigger.function)
        if (statement.exists.run) {
          //update
          val oldTriggers = statement.list
          if (oldTriggers.nonEmpty) {
            val oldTrigger = ProviderTrigger(oldTriggers(0).item, oldTriggers(0).itemDesc, oldTriggers(0).function, oldTriggers(0).functionDesc, oldTriggers(0).threshold)
            BorpClient.saveOpt(actionType = 2, entityId = appkey, entityType = EntityType.updateTrigger, oldValue = Json.toJson(oldTrigger).toString, newValue = Json.toJson(trigger).toString)
            statement.map(x => (x.function, x.functionDesc, x.threshold)).update(trigger.function, trigger.functionDesc, trigger.threshold)
          }
        } else {
          //insert
          AppkeyProviderTrigger += AppkeyProviderTriggerRow(0, appkey, trigger.item, trigger.itemDesc, trigger.function, trigger.functionDesc, trigger.threshold)
          BorpClient.saveOpt(actionType = 1, entityId = appkey, entityType = EntityType.createTrigger, newValue = Json.toJson(trigger).toString)
        }
    }
  }


  // 删除监控项
  def deleteTrigger(appkey: String, id: Long) = {
    db withSession {
      implicit session: Session =>
        val statement = AppkeyProviderTrigger.filter(x => x.id === id)
        val oldTriggers = statement.list
        if (oldTriggers.nonEmpty) {
          val oldTrigger = ProviderTrigger(oldTriggers(0).item, oldTriggers(0).itemDesc, oldTriggers(0).function, oldTriggers(0).functionDesc, oldTriggers(0).threshold)
          statement.delete
          BorpClient.saveOpt(actionType = 3, entityId = appkey, entityType = EntityType.deleteTrigger, oldValue = Json.toJson(oldTrigger).toString)
        }
    }
  }

  def deleteTrigger(appkey: String) = {
    db withSession {
      implicit session: Session =>
        val statement = AppkeyProviderTrigger.filter(x => x.appkey === appkey)
        val oldTriggers = statement.list
        if (oldTriggers.nonEmpty) {
          val oldTrigger = ProviderTrigger(oldTriggers(0).item, oldTriggers(0).itemDesc, oldTriggers(0).function, oldTriggers(0).functionDesc, oldTriggers(0).threshold)
          statement.delete
          BorpClient.saveOpt(actionType = 3, entityId = appkey, entityType = EntityType.deleteTrigger, oldValue = Json.toJson(oldTrigger).toString)
        }
    }
  }


  def insertOrUpdateAllSubscribe(appkey: String, userId: Int, userLogin: String, userName: String, xm: SubStatus, sms: SubStatus, email: SubStatus) {
    db withSession {
      implicit session: Session =>
        val statement = ProviderTriggerSubscribe.filter(x => x.appkey === appkey && x.triggerId === 0L && x.userId === userId)
        // 插入or修改全局订阅状态
        if (statement.exists.run) {
          //update
          statement.map(x => (x.xm, x.sms, x.email)).update(xm.id.toByte, sms.id.toByte, email.id.toByte)
        } else {
          //insert
          ProviderTriggerSubscribe += ProviderTriggerSubscribeRow(0, appkey, 0L, userId, userLogin, userName, xm.id.toByte, sms.id.toByte, email.id.toByte)
        }
        // 修改其它单独配置的状态
        ProviderTriggerSubscribe.filter(x => x.appkey === appkey && x.userId === userId).map(x => (x.xm, x.sms, x.email)).update(xm.id.toByte, sms.id.toByte, email.id.toByte)
    }
  }

  def insertOrUpdateSubscribe(appkey: String, triggerId: Long, userId: Int, userLogin: String, userName: String, mode: String, newStatus: Int) {
    db withSession {
      implicit session: Session =>
        val statement = ProviderTriggerSubscribe.filter(x => x.appkey === appkey && x.triggerId === triggerId && x.userId === userId)
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
              LOG.error(s"illegal mode ${mode} from $userLogin $appkey")
          }
        } else {
          //insert
          mode match {
            case "xm" =>
              ProviderTriggerSubscribe += ProviderTriggerSubscribeRow(0, appkey, triggerId, userId, userLogin, userName, newStatus.toByte, SubStatus.UnSub.id.toByte, SubStatus.UnSub.id.toByte)
            case "sms" =>
              ProviderTriggerSubscribe += ProviderTriggerSubscribeRow(0, appkey, triggerId, userId, userLogin, userName, SubStatus.UnSub.id.toByte, newStatus.toByte, SubStatus.UnSub.id.toByte)
            case "email" =>
              ProviderTriggerSubscribe += ProviderTriggerSubscribeRow(0, appkey, triggerId, userId, userLogin, userName, SubStatus.UnSub.id.toByte, SubStatus.UnSub.id.toByte, newStatus.toByte)
            case _ =>
              LOG.error(s"illegal mode ${mode} from $userLogin $appkey")
          }
        }
    }
  }

  def deleteTriggerSubscribe(appkey: String, triggerId: Long) = {
    db withSession {
      implicit session: Session =>
        ProviderTriggerSubscribe.filter(x => x.appkey === appkey && x.triggerId === triggerId).delete
    }
  }

  def deleteTriggerSubscribe(appkey: String) = {
    db withSession {
      implicit session: Session =>
        ProviderTriggerSubscribe.filter(x => x.appkey === appkey).delete
    }
  }


  def deleteTriggerSubscribe(userId: Int) = {
    db withSession {
      implicit session: Session =>
        ProviderTriggerSubscribe.filter(x => x.userId === userId).delete
    }
  }

  def deleteTriggerSubscribe(appkey: String, userId: Int): Unit = {
    db withSession {
      implicit session: Session =>
        ProviderTriggerSubscribe.filter(x => x.userId === userId && x.appkey === appkey).delete
    }
  }

  def getSubscribes(appkey: String, userId: Int) = {
    db withSession {
      implicit session: Session =>
        ProviderTriggerSubscribe.filter(x => x.appkey === appkey && x.userId === userId).list
    }
  }

  def getSubScribe(appkey: String, triggerId: Long) = {
    db withSession {
      implicit session: Session =>
        ProviderTriggerSubscribe.filter(x => x.appkey === appkey && (x.triggerId === triggerId || x.triggerId === 0L)).list
    }
  }

  def getSubScribeByUser(userLogin: String) = {
    db withSession {
      implicit session: Session =>
        ProviderTriggerSubscribe.filter(x => x.userLogin === userLogin && (x.xm === 0.toByte
          || x.sms === 0.toByte || x.email === 0.toByte)).list
    }
  }

  def getSubscribeUserDistinct() = {
    db withSession {
      implicit session: Session =>
        ProviderTriggerSubscribe.map(x => x.userLogin).list.distinct
    }
  }


}
