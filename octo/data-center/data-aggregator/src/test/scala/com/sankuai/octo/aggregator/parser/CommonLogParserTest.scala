package com.sankuai.octo.aggregator.parser

import java.nio.ByteBuffer
import com.sankuai.octo.aggregator.parser.common.CommonLogParser
import com.sankuai.octo.aggregator.thrift.model.{CommonLog, DropRequest, DropRequestList}
import com.sankuai.octo.statistic.constant.Constants
import org.apache.thrift.TSerializer
import org.apache.thrift.protocol.TBinaryProtocol
import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner
import org.springframework.test.context.ContextConfiguration

import scala.collection.JavaConversions._

/**
  * Created by wujinwu on 15/11/9.
  */
@RunWith(classOf[JUnitRunner])
@ContextConfiguration(locations = Array("classpath:applicationContext.xml"))
class CommonLogParserTest extends FunSuite {

  test("putDropMetric") {
    val list = new DropRequestList(List(new DropRequest("testAppKey", "192.168.1.1", "testRemoteAppkey", "testSpanname", System.currentTimeMillis(), 100, 0)))
    val serializer = new TSerializer(new TBinaryProtocol.Factory())
    val bytes = serializer.serialize(list)
    while (true)
      CommonLogParser.putCommonLog(new CommonLog(Constants.DROP_REQUEST_LOG, ByteBuffer.wrap(bytes)))
  }
}
