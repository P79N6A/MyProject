package com.sankuai.msgp.common.model

object Status extends Enumeration {
  type Status = Value
  val DEAD = Value(0, "未启动")
  val STARTING = Value(1, "启动中")
  val ALIVE = Value(2, "正常")
  val STOPPING = Value(3, "关闭中")
  val STOPPED = Value(4, "禁用")
  val WARNING = Value(5, "异常")
}