package com.sankuai.octo.msgp.model

/**
  * Created by yves on 16/12/6.
  */
object ComponentAction extends Enumeration {
  type ComponentAction = Value
  val WARNING = Value(0, "warning")
  val REQUIRED_WARNING = Value(1, "requiredWarning")
  val BROKEN = Value(2, "broken")
  val REQUIRED_BROKEN = Value(3, "requiredBroken")
}
