package com.sankuai.msgp.common.model

object Level extends Enumeration {
  type Level = Value
  val interface = Value(0, "接入层")
  val logic = Value(1, "业务逻辑层")
  val data = Value(2, "数据层")
}