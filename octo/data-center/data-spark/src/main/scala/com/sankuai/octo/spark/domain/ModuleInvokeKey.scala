package com.sankuai.octo.spark.domain

/**
  * Created by wujinwu on 16/3/9.
  */
case class ModuleInvokeKey(localAppKey: String, ts: Int, _type: Int, envStr: String) {

  override def equals(other: Any): Boolean = {
    if (other != null) {
      if (other.isInstanceOf[ModuleInvokeKey]) {
        val that = other.asInstanceOf[ModuleInvokeKey]
        localAppKey == that.localAppKey && ts == that.ts &&
          _type == that._type && envStr == that.envStr
      } else {
        false
      }
    } else {
      false
    }
  }

  override def hashCode(): Int = {
    val state = Seq(localAppKey, ts, _type, envStr)
    state.map(_.hashCode()).foldLeft(0)((a, b) => 31 * a + b)
  }
}

case class ModuleInvokeAllKey(localAppKey: String, ts: Int, _type: Int, envStr: String,
                              spanName: String, localHost: String, remoteAppKey: String,
                              remoteHost: String) {

  override def equals(other: Any): Boolean = {
    if (other != null) {
      if (other.isInstanceOf[ModuleInvokeAllKey]) {
        val that = other.asInstanceOf[ModuleInvokeAllKey]
        localAppKey == that.localAppKey && ts == that.ts &&
          _type == that._type && envStr == that.envStr &&
          spanName == that.spanName && localHost == that.localHost &&
          remoteAppKey == that.remoteAppKey && remoteHost == that.remoteHost
      } else {
        false
      }
    } else {
      false
    }
  }

  override def hashCode(): Int = {
    val state = Seq(localAppKey, ts, _type, envStr, spanName, localHost, remoteAppKey, remoteHost)
    state.map(_.hashCode()).foldLeft(0)((a, b) => 31 * a + b)
  }
}