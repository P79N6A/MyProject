package com.sankuai.octo.msgp.model

object TriggerStatus extends Enumeration {
  type TriggerStatus =  Value
  val UNFINISHED = Value(0, "未完成")
  val FINISHED = Value(1, "已完成")
  val TRIGGER_FAILED = Value(2, "监控失败")
  val MISSING_DATA = Value(9, "缺失有效数据")
}