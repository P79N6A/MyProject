package com.sankuai.octo.msgp.dao.monitor

import com.sankuai.msgp.common.config.DbConnection
import com.sankuai.msgp.common.model.EntityType
import com.sankuai.msgp.common.config.db.msgp.Tables.{AppkeyTriggerRow, _}
import com.sankuai.msgp.common.utils.client.BorpClient
import com.sankuai.msgp.common.model.MonitorModels.Trigger
import com.sankuai.octo.msgp.model.TriggerStatus
import com.sankuai.octo.msgp.model.TriggerStatus.TriggerStatus
import com.sankuai.octo.msgp.serivce.monitor.MonitorConfig
import org.slf4j.{Logger, LoggerFactory}
import play.api.libs.json.Json

import scala.slick.driver.MySQLDriver.simple._


/**
  * Created by yves on 16/12/1.
  * 性能报警的持久层
  */
object MonitorTriggerDAO {

  val LOG: Logger = LoggerFactory.getLogger(MonitorConfig.getClass)
  private val db = DbConnection.getPool()

  def batchInsertTriggerStatus(createTime: Long) = {
    db withSession {
      implicit session: Session =>
        val triggerIdList = AppkeyTrigger.map(_.id).list
        val triggerStatusRowList = triggerIdList.map {
          triggerId =>
            AppkeyTriggerStatusRow(0, triggerId, TriggerStatus.UNFINISHED.id, createTime, createTime)
        }
        val transaction_limit = 100
        (0 to (triggerStatusRowList.length / transaction_limit + 1)).foreach {
          x =>
            val subList = triggerStatusRowList.slice(x * transaction_limit, (x + 1) * transaction_limit)
            if (subList.length > 0) {
              LOG.info(s"###batchInsert size ${subList.size}")
              AppkeyTriggerStatus ++= subList
            }
        }
      //        AppkeyTriggerStatus ++= triggerStatusRowList
    }
  }


  def updateTriggerStatus(triggerId: Long, createTime: Long, updateTime: Long, status: TriggerStatus) = {
    db withSession {
      implicit session: Session =>
        val statement = AppkeyTriggerStatus.filter { x =>
          x.triggerId === triggerId && x.createTime === createTime
        }
        if (statement.exists.run) {
          statement.map(x => (x.triggerStatus, x.updateTime)).update(status.id, updateTime)
        }
    }
  }

  /**
    * 删除一端时间的trigger status item
    *
    * @param createTime 删除截止时间, 此时间之前的均删除
    * @return
    */
  def deleteTriggerStatus(createTime: Long) = {
    db withSession {
      implicit session: Session =>
        val statement = AppkeyTriggerStatus.filter { x =>
          x.createTime < createTime
        }
        if (statement.exists.run) {
          statement.delete
        }
    }
  }

  def getItemDescCount(appkey: String, itemDesc: String, side: String) = {
    db withSession {
      implicit session: Session =>
        AppkeyTrigger.filter {
          x =>
            x.appkey === appkey &&
              x.side === side &&
              x.itemDesc === itemDesc
        }.list.length
    }
  }

  /**
    * 获得每一轮中不同状态的数量
    *
    * @param createTime
    * @param status
    * @return
    */
  def getStatusCount(createTime: Long, status: TriggerStatus) = {
    db withSession {
      implicit session: Session =>
        AppkeyTriggerStatus.filter { x =>
          x.createTime === createTime &&
            x.triggerStatus === status.id
        }.length.run
    }
  }

  def getAllTriggersCount(createTime: Long) = {
    db withSession {
      implicit session: Session =>
        AppkeyTriggerStatus.filter(_.createTime === createTime).length.run
    }
  }

  def getTriggersFragment(fragmentStart: Int, fragmentSize: Int, createTime: Long) = {
    db withSession {
      implicit session: Session =>
        val triggerIdList = AppkeyTriggerStatus.filter { x =>
          x.createTime === createTime
        }.list.map(_.triggerId).slice(fragmentStart, fragmentStart + fragmentSize)
        AppkeyTrigger.filter(x => x.id inSet triggerIdList).list
    }
  }

  def getTriggers(appkey: String) = {
    db withSession {
      implicit session: Session =>
        val triggers = AppkeyTrigger.filter(x => x.appkey === appkey).list
        triggers
    }
  }

  def insertOrUpdateTrigger(appkey: String, trigger: Trigger) = {
    db withSession {
      implicit session: Session =>
        val statement = AppkeyTrigger.filter(x => x.appkey === appkey && x.side === trigger.side && x.spanname === trigger.spanname && x.item === trigger.item && x.function === trigger.function)
        if (statement.exists.run) {
          //update
          val oldTriggers = statement.list
          val oldTrigger = Trigger(oldTriggers(0).side, oldTriggers(0).spanname, oldTriggers(0).item, oldTriggers(0).itemDesc, oldTriggers(0).duration, oldTriggers(0).function, oldTriggers(0).functionDesc, oldTriggers(0).threshold)
          BorpClient.saveOpt(actionType = 2, entityId = appkey, entityType = EntityType.updateTrigger, oldValue = Json.toJson(oldTrigger).toString, newValue = Json.toJson(trigger).toString)
          statement.map(x => (x.duration, x.function, x.functionDesc, x.threshold)).update(trigger.duration, trigger.function, trigger.functionDesc, trigger.threshold)
        } else {
          //insert
          AppkeyTrigger += AppkeyTriggerRow(0, appkey, trigger.side, trigger.spanname, trigger.item, trigger.itemDesc, trigger.function, trigger.functionDesc, trigger.threshold, trigger.duration)
          BorpClient.saveOpt(actionType = 1, entityId = appkey, entityType = EntityType.createTrigger, newValue = Json.toJson(trigger).toString)
        }
    }
  }

  def getTrigger(appkey: String, trigger: Trigger) = {
    db withSession {
      implicit session: Session =>
        val statement = AppkeyTrigger.filter(x => x.appkey === appkey && x.side === trigger.side && x.spanname === trigger.spanname && x.item === trigger.item && x.function === trigger.function)
        statement.firstOption
    }
  }

  // 删除监控项
  def deleteTrigger(appkey: String, id: Long) = {
    db withSession {
      implicit session: Session =>
        val statement = AppkeyTrigger.filter(x => x.id === id)
        val oldTriggers = statement.list
        val oldTrigger = Trigger(oldTriggers(0).side, oldTriggers(0).spanname, oldTriggers(0).item, oldTriggers(0).itemDesc, oldTriggers(0).duration, oldTriggers(0).function, oldTriggers(0).functionDesc, oldTriggers(0).threshold)
        statement.delete
        BorpClient.saveOpt(actionType = 3, entityId = appkey, entityType = EntityType.deleteTrigger, oldValue = Json.toJson(oldTrigger).toString)
    }
  }
}
