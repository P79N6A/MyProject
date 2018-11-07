package com.sankuai.octo.aggregator

import java.nio.ByteBuffer

import com.sankuai.octo.aggregator.thrift.model.{CommonLog, ErrorLog}
import org.apache.thrift.protocol.TBinaryProtocol
import org.apache.thrift.{TDeserializer, TSerializer}
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{BeforeAndAfter, FunSuite}

@RunWith(classOf[JUnitRunner])
class LogTest extends FunSuite with BeforeAndAfter {

  test("common log") {
    val log = new ErrorLog("msgp", System.currentTimeMillis(), 1, "test", "xxxxx")
    val serializer = new TSerializer(new TBinaryProtocol.Factory())
    val deserializer = new TDeserializer(new TBinaryProtocol.Factory())
    val content = serializer.serialize(log)

    val commonLog = new CommonLog(1, ByteBuffer.wrap(content))
    val newLog = new ErrorLog()
    val bytes = new Array[Byte](commonLog.content.capacity())
    commonLog.content.get(bytes)
    deserializer.deserialize(newLog, bytes)
    println(newLog)
  }

  test("appkeyTopic") {
    val a = MafkaConfig.getMafkaProducer("waimai_api")
    a
  }
}
