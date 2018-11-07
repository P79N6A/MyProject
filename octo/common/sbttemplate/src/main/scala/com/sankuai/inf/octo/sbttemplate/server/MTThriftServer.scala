package com.sankuai.inf.octo.sbttemplate.server

/**
 * Created by chenxi on 9/11/15.
 */

import com.meituan.service.mobile.mtthrift.proxy.ThriftServerPublisher
import com.sankuai.inf.octo.scalatemplate.HelloService

object MTThriftServer {

  def start = {
    val serverPublisher = new ThriftServerPublisher()
    serverPublisher.setAppKey("com.sankuai.inf.octo.sbttemplate")
    serverPublisher.setServiceImpl(new MTThriftServerImpl())
    serverPublisher.setServiceInterface(new HelloService().getClass)
    serverPublisher.setClusterManager("OCTO")
    serverPublisher.setDaemon(true)
    serverPublisher.setPort(12345)

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