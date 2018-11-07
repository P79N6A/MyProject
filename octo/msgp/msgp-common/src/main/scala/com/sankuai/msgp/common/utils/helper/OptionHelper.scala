package com.sankuai.msgp.common.utils.helper

object OptionHelper {

  def defaultInt(op : Option[Int]) = op.getOrElse(0)

  def defaultLong(op : Option[Long]) = op.getOrElse(0L)

  def defaultString(op : Option[String]) = op.getOrElse("")
}
