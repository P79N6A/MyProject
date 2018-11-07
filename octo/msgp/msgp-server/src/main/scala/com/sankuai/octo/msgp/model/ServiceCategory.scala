package com.sankuai.octo.msgp.model

/**
 * Created by zava on 16/8/29.
 */
object ServiceCategory extends Enumeration {
  type ServiceCategory = Value
  val HTTP = Value("http")
  val THRIFT = Value("thrift")
  val BOTH = Value("thrift,http")
}