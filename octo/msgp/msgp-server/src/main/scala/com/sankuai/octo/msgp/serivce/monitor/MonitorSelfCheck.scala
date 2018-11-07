package com.sankuai.octo.msgp.serivce.monitor

import com.meituan.jmonitor.JMonitor
import com.sankuai.octo.msgp.dao.monitor.MonitorTriggerDAO
import com.sankuai.octo.msgp.model.TriggerStatus

/**
  * Created by yves on 16/12/6.
  * 执行一系列的自检操作
  */
object MonitorSelfCheck {

  /**
    * 上报每一轮的状态信息
    */
  def uploadMonitorStatus(createTime: Long) = {
    //获得上一轮监控没有完成的配置项数目
    val unfinishedCount = MonitorTriggerDAO.getStatusCount(createTime, TriggerStatus.UNFINISHED)
    val failedCount = MonitorTriggerDAO.getStatusCount(createTime, TriggerStatus.TRIGGER_FAILED)
    val emptyDataCount = MonitorTriggerDAO.getStatusCount(createTime, TriggerStatus.MISSING_DATA)
    val totalCount = MonitorTriggerDAO.getAllTriggersCount(createTime)
    JMonitor.addNum("monitor.trigger.unfinished", unfinishedCount)
    JMonitor.addNum("monitor.trigger.failed", failedCount)
    JMonitor.addNum("monitor.trigger.emptyData", emptyDataCount)
    JMonitor.addNum("monitor.trigger.total", totalCount)
  }
}
