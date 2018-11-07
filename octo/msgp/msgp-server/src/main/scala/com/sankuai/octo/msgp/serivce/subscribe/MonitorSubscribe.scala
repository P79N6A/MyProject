package com.sankuai.octo.msgp.serivce.subscribe

import com.sankuai.octo.msgp.dao.monitor.MonitorDAO

/**
  * Created by yves on 17/5/15.
  * 性能监控
  */
object MonitorSubscribe {

  def cancelMonitorSubscribe(username: String, appkey: String) = {
    MonitorDAO.deleteTriggerSubscribe(appkey, username)
  }

}
