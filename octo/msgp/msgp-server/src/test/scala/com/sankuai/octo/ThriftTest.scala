package com.sankuai.octo

import com.sankuai.sgagent.thrift.model.SGAgent
import org.apache.thrift.protocol.{TBinaryProtocol, TProtocol}
import org.apache.thrift.transport.{TFramedTransport, TSocket, TTransport}
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{BeforeAndAfter, FunSuite}

import scala.collection.JavaConverters._

@RunWith(classOf[JUnitRunner])
class ThriftTest extends FunSuite with BeforeAndAfter {

  test("reset timeout") {
    var transport: TTransport = null
    try {
      val init = System.currentTimeMillis()
      val timeout = 10
      val host = "192.168.11.90"
      val port = 5266
      val socket = new TSocket(host, port, timeout)
      socket.open
      socket.setTimeout(2000)
      transport = new TFramedTransport(socket, 16384000)
      val protocol: TProtocol = new TBinaryProtocol(transport)
      val agent = new SGAgent.Client(protocol)
      val send = System.currentTimeMillis()
      val config = agent.getServiceList("com.sankuai.inf.msgp", "com.sankuai.waimai.money")
      config.asScala.foreach(println)
      val recv = System.currentTimeMillis()
      if (send - init > timeout || recv - send > timeout) {
        println(s"warn $config ${send - init}ms ${recv - send}ms")
      } else {
        println(s"$config ${send - init}ms ${recv - send}ms")
      }
    } catch {
      case e: Exception =>
        println(s"error ${e.getMessage}")
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
}
