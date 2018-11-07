package com.sankuai.msgp.common.model

object Env extends Enumeration {
  type Env = Value
  val test = Value(1)
  val stage = Value(2)
  val prod = Value(3)
}
