package com.sankuai.octo.oswatch.server

/**
 * Created by chenxi on 9/11/15.
 */

import com.meituan.service.mobile.mtthrift.proxy.ThriftServerPublisher
import com.sankuai.octo.oswatch.thrift.service.OSWatchService
import com.sankuai.octo.oswatch.utils.Common

object MTThriftServer {
  val oswatchServiceImpl = new OSWatchServiceImpl()

  def start = {
    oswatchServiceImpl.init()

    val serverPublisher = new ThriftServerPublisher()
    serverPublisher.setAppKey(Common.appkey)
    serverPublisher.setServiceImpl(new OSWatchServiceImpl())
    serverPublisher.setServiceInterface(new OSWatchService().getClass)
    serverPublisher.setClusterManager(Common.clusterManager)
    serverPublisher.setDaemon(true)
    serverPublisher.setPort(Common.port)

    try {
      serverPublisher.publish()
      println("=============================")
      println("           START             ")
      println("=============================")
    } catch {
      case e: Exception => e.printStackTrace()
    }
  }

  def main(args: Array[String]) {
    MTThriftServer.start
  }
}