package com.sankuai.octo.msgp.serivce.subscribe

import com.sankuai.meituan.auth.vo.User
import com.sankuai.msgp.common.config.DbConnection
import com.sankuai.msgp.common.model.SubscribeStatus
import com.sankuai.octo.msgp.dao.subscribe.AppkeySubscribeDAO
import com.sankuai.octo.msgp.dao.subscribe.AppkeySubscribeDAO.AppkeySubscribeDomain
import org.slf4j.{Logger, LoggerFactory}

/**
  * Created by yves on 17/4/21.
  *
  * 统一的订阅, 包括服务日报、周报、节点报警、配置报警等
  */


object AppkeySubscribe {

  val LOG: Logger = LoggerFactory.getLogger(AppkeySubscribe.getClass)


  def getSubscribeForDailyReport(username: String) = {
    AppkeySubscribeDAO.getSubscribedDailyReport(username, SubscribeStatus.subscribed.getStatus.toByte)
  }

  def getSubscribeForWeeklyReport(username: String) = {
    AppkeySubscribeDAO.getSubscribedWeeklyReport(username, SubscribeStatus.subscribed.getStatus.toByte)
  }

  def getSubscribedUserForReport(reportType: String = "daily") = {
    AppkeySubscribeDAO.getSubscribedUserForReport(reportType)
  }

  def updateSubscribe(appkey:String, newOwners: List[String], oldOwners: List[String]) = {
    val removeList = oldOwners.toSet.diff(newOwners.toSet).toList
    if(removeList.nonEmpty){
      removeList.foreach{
        username=>
          ReportSubscribe.cancelReportSubscribe(username,appkey)
//          MonitorSubscribe.cancelMonitorSubscribe(username,appkey)
      }
    }
    val addList = newOwners.toSet.diff(oldOwners.toSet).toList
    if(addList.nonEmpty){
      addList.foreach{
        username=>
          ReportSubscribe.addReportSubscribe(username,appkey)
      }
    }
//    if(oldOwners.length != newOwners.length){
//      if(oldOwners.length > newOwners.length){
//        val removedOwners = oldOwners.filterNot(x=> newOwners.contains(x))
//        //取消订阅
//        removedOwners.foreach{
//          username=>
//            ReportSubscribe.cancelReportSubscribe(username, appkey)
//            MonitorSubscribe.cancelMonitorSubscribe(username, appkey)
//        }
//      }else {
//        val addedOwners = newOwners.filterNot(x=> oldOwners.contains(x))
//        //增加订阅
//        addedOwners.foreach{
//          username=>
//            ReportSubscribe.addReportSubscribe(username, appkey)
//            //TODO 把性能监控订阅接入appkey_subscribe
//        }
//      }
//    }
  }
}
