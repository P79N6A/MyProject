package com.sankuai.msgp.common.model

object Role extends Enumeration {
  type Role = Value
  val normal = Value(0)
  val backup = Value(1)
}