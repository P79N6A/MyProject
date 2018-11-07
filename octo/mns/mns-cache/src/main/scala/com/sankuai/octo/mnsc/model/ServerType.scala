package com.sankuai.octo.mnsc.model

/**
 * Created by lhmily on 01/04/2016.
 */
object ServerType extends Enumeration {
  type  ServerType= Value
  val thrift = Value(0,"provider")
  val http = Value(1,"provider-http")
}
