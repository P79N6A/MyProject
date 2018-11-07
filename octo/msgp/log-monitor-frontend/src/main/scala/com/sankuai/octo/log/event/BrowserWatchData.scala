package com.sankuai.octo.log.event


class BrowserWatchData(val userName: String, val appkey: String, val hosts: Set[String], val filePath: String, val filter: String) {


  override def equals(other: Any): Boolean = other match {
    case that: BrowserWatchData =>
      (that canEqual this) &&
        userName == that.userName &&
        appkey == that.appkey &&
        hosts == that.hosts &&
        filePath == that.filePath &&
        filter == that.filter
    case _ => false
  }

  def canEqual(other: Any): Boolean = other.isInstanceOf[BrowserWatchData]

  override def toString = s"BrowserWatchData($userName, $appkey, $hosts, $filePath, $filter, ${hashCode()})"

  override def hashCode(): Int = {
    val state = Seq(userName, appkey, hosts, filePath, filter)
    state.map(_.hashCode()).foldLeft(0)((a, b) => 31 * a + b)
  }
}
