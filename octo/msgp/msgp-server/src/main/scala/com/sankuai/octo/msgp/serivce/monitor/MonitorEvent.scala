package com.sankuai.octo.msgp.serivce.monitor

import com.sankuai.meituan.auth.util.UserUtils
import com.sankuai.meituan.auth.vo.User
import com.sankuai.msgp.common.model.{MonitorModels, Page, ServiceModels}
import com.sankuai.msgp.common.config.db.msgp.Tables.EventRow
import com.sankuai.octo.msgp.dao.monitor.MonitorDAO
import org.slf4j.{Logger, LoggerFactory}

object MonitorEvent {
  val LOG: Logger = LoggerFactory.getLogger(MonitorEvent.getClass)

  // 记录报警事件,并返回这个报警在数据库中的ID
  def insertEvent(event: EventRow) = {
    MonitorDAO.insertEvent(event)
  }

  // do ack for event
  def ackEvent(eventId: Long) {
    val user: User = UserUtils.getUser
    val ackUser = s"${user.getName}(${user.getLogin})"
    MonitorDAO.ackEvent(eventId, ackUser)
  }

  def getEventCount(appkey: String, spanname: String, startTime: Long, endTime: Long)={
    MonitorDAO.getEventCount(appkey, spanname, startTime,endTime)
  }

  def eventCount(startTime: Long, endTime: Long) ={
    MonitorDAO.eventCount(startTime,endTime)
  }
  // get events
  def getEvents(appkey: String, startTime: Long, endTime: Long, page: Page) = {
    val eventList = MonitorDAO.getEvents(appkey, startTime, endTime, page)
    val list = eventList.map(x => x.copy(side = {
      if (x.side == "server") "服务接口" else "外部接口"
    }, item = MonitorModels.itemList.find(_.name == x.item).getOrElse(MonitorModels.Item("", "")).desc))
    list
  }
}
