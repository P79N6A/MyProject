package com.sankuai.octo

import org.apache.thrift.transport.{TSocket, TFramedTransport, TTransport}
import org.apache.thrift.protocol.{TBinaryProtocol, TProtocol}
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{BeforeAndAfter, FunSuite}
import com.sankuai.octo.test.thrift.service.LogCollectorService
import com.sankuai.octo.test.thrift.model.SGLog

@RunWith(classOf[JUnitRunner])
class ThriftTest extends FunSuite with BeforeAndAfter {

  test("reset timeout") {
    val init = System.currentTimeMillis()
    var transport: TTransport = null
    try {
      val timeout = 200
      val host = "172.30.8.162"
      val port = 8600
      val socket = new TSocket(host, port, timeout)
      socket.open
      val send = System.currentTimeMillis()
      socket.setTimeout(3000)
      transport = new TFramedTransport(socket, 16384000)
      val protocol: TProtocol = new TBinaryProtocol(transport)
      val agent = new LogCollectorService.Client(protocol)
      //val config = agent.getServiceList("com.sankuai.inf.msgp", "com.sankuai.inf.test")
      (1 to 100).foreach { x=>
        val config = agent.uploadLog(randomLog(111))
        println(config)
      }
      val recv = System.currentTimeMillis()
      if (send - init > timeout || recv - send > timeout) {
        println(s"warn ${send - init}ms ${recv - send}ms")
      } else {
        println(s"ok ${send - init}ms ${recv - send}ms")
      }
    } catch {
      case e: Exception =>
        val now = System.currentTimeMillis()
        println(s"error ${e.getMessage} ${now - init}")
    } finally {
      if (transport != null) {
        try {
          transport.close()
        } catch {
          case e: Exception => println(s"close ${e.getMessage}")
        }
      }
    }
  }

  private def randomLog(count: Int): SGLog = {
    val log: SGLog = new SGLog
    log.setAppkey("com.sankuai.inf.test.client")
    log.setLevel(count)
    log.setTime(System.currentTimeMillis)
    log.setContent("test" + count)
    return log
  }
}
