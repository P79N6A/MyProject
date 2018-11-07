package com.sankuai.octo.updater.util

import java.net.InetAddress

import com.sankuai.octo.scanner.model.Provider
import com.sankuai.sgagent.thrift.model.fb_status
import org.apache.thrift.transport.TSocket
import org.slf4j.{Logger, LoggerFactory}


object Detector {
  private val LOG: Logger = LoggerFactory.getLogger(Detector.getClass)

  def detect(provider: Provider): fb_status = {
    var socket: TSocket = null
    try {
      socket = new TSocket(provider.getIp, provider.getPort)
      socket.setTimeout(Common.longTimeOutInMills)
      socket.getSocket.setReuseAddress(true)
      socket.getSocket.setTcpNoDelay(true)
      socket.open()
      fb_status.ALIVE
    } catch {
      case e: Exception =>
        provider.setExceptionMsg(e.getMessage)
        if(Common.isOnline && e.getMessage.contains("SocketTimeoutException")) {
          val status2: fb_status = detectAgain(provider)
          if(status2.equals(fb_status.ALIVE))
            fb_status.ALIVE
          else
            fb_status.DEAD
        } else {
          fb_status.DEAD
        }
    } finally {
      if (socket != null) {
        try {
          socket.close
        } catch {
          case e: Exception => LOG.error(s"close socket exception ${provider.getIpPort}  ${e.getMessage}")
        }
      }
    }
  }

  def detectAgain(provider: Provider): fb_status = {
    var socket: TSocket = null
    try {
      socket = new TSocket(provider.getIp, provider.getPort)
      socket.setTimeout(Common.longTimeOutInMills)
      socket.getSocket.setReuseAddress(true)
      socket.getSocket.setTcpNoDelay(true)
      socket.open
      fb_status.ALIVE
    } catch {
      case e: Exception =>
        provider.setExceptionMsg(e.getMessage)
        fb_status.DEAD
    } finally {
      if (socket != null) {
        try {
          socket.close
        } catch {
          case e: Exception => LOG.error(s"close socket exception ${provider.getIpPort}  ${e.getMessage}")
        }
      }
    }

  }

  def isReachable(ip: String) = {
    try {
      val address = InetAddress.getByName(ip)
      address.isReachable(Common.longTimeOutInMills)
    } catch {
      case e: Exception => false
    }
  }

}
