package com.sankuai.msgp.common.model

import com.sankuai.msgp.common.utils.helper.CommonHelper
import play.api.libs.json.Json

import scala.collection.JavaConverters._

object MonitorModels {

  /**
   * 触发器
   * @param item  取值 all.cost.mean
   * @param function 比较器
   * @param threshold 阈值
   */
  case class Trigger(side: String, spanname: String, item: String, itemDesc: String, duration: Int, function: String, functionDesc: String, threshold: Int)

  implicit val triggerReads = Json.reads[Trigger]
  implicit val triggerWrites = Json.writes[Trigger]

  case class ProviderTrigger(item: String, itemDesc: String,function: String, functionDesc: String, threshold: Int)
  implicit val providerTriggerReads = Json.reads[ProviderTrigger]
  implicit val providerTriggerWrites = Json.writes[ProviderTrigger]

  case class TriggerOld(item: String, itemDesc: String, function: String, functionDesc: String, threshold: Int)

  implicit val triggerOldReads = Json.reads[TriggerOld]
  implicit val triggerOldWrites = Json.writes[TriggerOld]

  case class Item(name: String, desc: String)

  val itemList = List(
    Item("compare.$appkey.$sideCount.upper_50.week", "50%耗时(分钟粒度)同比"),
    Item("compare.$appkey.$sideCount.upper_50.day", "50%耗时(分钟粒度)环比"),
    Item("minute.timers.$appkey.$sideCost.upper_50", "50%耗时(分钟粒度)"),

    Item("minute.timers.$appkey.$sideCost.upper_90", "90%耗时(分钟粒度)"),
    Item("minute.timers.$appkey.$sideCost.upper_99", "99%耗时(分钟粒度)"),

    Item("minute.counters.$appkey.$sideCount", "QPS(分钟粒度)"),
    Item("compare.counters.$appkey.$sideCount.week", "QPS(分钟粒度)同比"),
    Item("compare.counters.$appkey.$sideCount.day", "QPS(分钟粒度)环比"),

    Item("today.timers.$appkey.$sideCost.upper_50", "50%耗时(今日)"),
    Item("today.timers.$appkey.$sideCost.upper_90", "90%耗时(今日)"),
    Item("today.timers.$appkey.$sideCost.upper_99", "99%耗时(今日)"),
    Item("today.counters.$appkey.$sideCount", "总次数(今日)")
  )

  val providerItemList = List(
    Item("active_count", "活跃节点数量"),
    Item("active_perf", "活跃节点比例"),
    Item("active_forbidden", "禁用节点比例"),
    Item("minute_change", "一分钟状态变化频率")
    )

  def triggerItems() = {
    itemList.map(CommonHelper.toJavaMap).asJava
  }
  def providerTriggerItems() = {
    providerItemList.map(CommonHelper.toJavaMap).asJava
  }

  case class Function(name: String, desc: String)

  def triggerFunctions() = {
    List(Function(">", "大于"), Function("<", "小于")).map(CommonHelper.toJavaMap).asJava
  }

  def triggerOptions() = {
    Map("items" -> triggerItems(), "functions" -> triggerFunctions())
  }

  case class AppkeyTriggerWithSubStatus(id: Long, appkey: String, side: String, spanname: String, item: String,
                                        itemDesc: String, duration: Int, function: String, functionDesc: String, threshold: Int,
                                        createTime: Long, xm: Int, sms: Int, email: Int)


  implicit val appkeyTriggerWithSubStatusReads = Json.reads[AppkeyTriggerWithSubStatus]
  implicit val appkeyTriggerWithSubStatusWrites = Json.writes[AppkeyTriggerWithSubStatus]


  case class AppkeyProviderTriggerWithSubStatus(id: Long, appkey: String,item: String,
                                        itemDesc: String, function: String, functionDesc: String, threshold: Int,
                                        createTime: Long, xm: Int, sms: Int, email: Int)


  implicit val appkeyProviderTriggerWithSubStatusReads = Json.reads[AppkeyProviderTriggerWithSubStatus]
  implicit val appkeyProviderTriggerWithSubStatusWrites = Json.writes[AppkeyProviderTriggerWithSubStatus]


  /**
   * @param appkey 服务标识
   * @param xm 大象订阅状态:true|false
   * @param sms 大象订阅状态:true|false
   * @param email 大象订阅状态:true|false
   */
  case class BatchSubscribeUpdateRequest(appkey: String, xm: Boolean, sms: Boolean, email: Boolean)

  implicit val batchSubscribeReads = Json.reads[BatchSubscribeUpdateRequest]
  implicit val batchSubscribeWrites = Json.writes[BatchSubscribeUpdateRequest]

  /**
   * @param appkey 服务标识
   * @param triggerId 告警项ID
   * @param mode 订阅渠道：xm、sms、email
   * @param newStatus 订阅状态: 0 Sub、1 UnSub
   */
  case class SubscribeUpdateRequest(appkey: String, triggerId: Long, mode: String, newStatus: Int)

  implicit val subscribeReads = Json.reads[SubscribeUpdateRequest]
  implicit val subscribeWrites = Json.writes[SubscribeUpdateRequest]
}
