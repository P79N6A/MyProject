package com.sankuai.octo.updater.util

import java.util.Properties

import com.meituan.service.mobile.zkclient.MtZookeeperClient
import com.sankuai.inf.octo.mns.ProcessInfoUtil


object Common {
  val isOnline = ProcessInfoUtil.isLocalHostOnline
  val longTimeOutInMills: Int = if (isOnline) 150 else 200
  val appkey: String = "com.sankuai.inf.octo.scannerupdater"
  val vbarAsRead: String = "\\|"
  val vbar: String = "|"
  val colon: String = ":"

  val zkClient: MtZookeeperClient = {
    val zkUrl =
      try {
        val classloader = Thread.currentThread().getContextClassLoader
        val is = classloader.getResourceAsStream("config.properties")
        val prop = new Properties()
        prop.load(is)
        prop.getProperty("zkUrl")
      } catch {
        case e: Exception =>
          e.printStackTrace()
          sys.exit(1)
      }
    new MtZookeeperClient(zkUrl, 30000, true)
  }

}
