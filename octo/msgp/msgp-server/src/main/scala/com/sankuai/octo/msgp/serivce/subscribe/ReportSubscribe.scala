package com.sankuai.octo.msgp.serivce.subscribe

import com.sankuai.msgp.common.model.SubscribeStatus
import com.sankuai.octo.msgp.dao.subscribe.AppkeySubscribeDAO
import com.sankuai.octo.msgp.dao.subscribe.AppkeySubscribeDAO.AppkeySubscribeDomain

/**
  * Created by yves on 17/4/21.
  */
object ReportSubscribe {
  /**
    * 批量增加订阅
    *
    * @param username
    * @param appkeyList
    */
  def addReportSubscribe(username: String, appkeyList: List[String]) = {
    val now = System.currentTimeMillis() / 1000
    val subscribeDomains = appkeyList.map{
      appkey=>
        AppkeySubscribeDomain(username, appkey, SubscribeStatus.subscribed.getStatus.toByte,
          SubscribeStatus.subscribed.getStatus.toByte, now, now)
    }
    AppkeySubscribeDAO.batchInsertOrUpdate(subscribeDomains)
  }

  /**
    * 更新日报/周报的订阅状态
    *
    * @param username
    * @param appkey
    * @param status
    * @return
    */
  def updateReportSubscribeStatus(username: String, appkey: String, status: SubscribeStatus) = {
    val now = System.currentTimeMillis() / 1000
    val subscribeDomain =  AppkeySubscribeDomain(username, appkey, status.getStatus.toByte,
      status.getStatus.toByte, now, now)
    AppkeySubscribeDAO.insertOrUpdateReport(subscribeDomain)
  }

  def addReportSubscribe(username: String, appkey: String) = {
    updateReportSubscribeStatus(username, appkey, SubscribeStatus.subscribed)
  }

  def cancelReportSubscribe(username: String, appkey: String) = {
    updateReportSubscribeStatus(username, appkey, SubscribeStatus.not_subscribed)
  }
}
