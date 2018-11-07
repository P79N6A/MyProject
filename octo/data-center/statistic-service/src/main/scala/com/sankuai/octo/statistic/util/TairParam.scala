package com.sankuai.octo.statistic.util

/**
  * Created by wujinwu on 16/1/8.
  */
object TairParam {

  def master(masterKey: String = "tair.master"): String = {
    config.get(masterKey, "10.64.23.204:5198")
  }

  def slave(slaveKey: String = "tair.slave"): String = {
    config.get(slaveKey, "")
  }

  def group(groupKey: String = "tair.group"): String = {
    config.get(groupKey, "group_1")
  }

  def area(areaKey: String = "tair.area"): Short = {
    config.get(areaKey, 5.toString).toShort
  }

  def remoteAppKey(remoteKey: String = "tair.remoteAppKey") = {
    config.get(remoteKey, "")
  }


}
