package com.sankuai.octo.msgp.model


/**
  * Created by lhmily on 06/30/2016.
  */
object ProviderNodeSortEle extends Enumeration{
  type ProviderNodeSortEle = Value
  val hostnameAsc = Value(1)
  val IPAsc = Value(2)
  val portAsc = Value(3)
  val roleAsc = Value(4)
  val versionAsc = Value(5)
  val fweightAsc = Value(6)
  val statusAsc = Value(7)
  val lastUpdateTimeAsc = Value(8)
  val hostnameDesc = Value(-1)
  val IPDesc = Value(-2)
  val portDesc = Value(-3)
  val roleDesc = Value(-4)
  val versionDesc = Value(-5)
  val fweightDesc = Value(-6)
  val statusDesc = Value(-7)
  val lastUpdateTimeDesc = Value(-8)
}
