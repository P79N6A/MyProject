package com.sankuai.octo.statistic.helper

import java.nio.charset.StandardCharsets._

import com.sankuai.octo.statistic.model.{PerfDataType, PerfProtocolType, StatEnv, StatGroup}
import org.apache.hadoop.hbase.util.Bytes
import org.scalatest.FunSuite

/**
  * Created by wujinwu on 16/3/4.
  */
class HBaseHelperTest extends FunSuite {

  test("testGenerateRowKeyPrefix") {
    val rowKeyPrefix = HBaseHelper.generateRowKeyPrefix("com.sankuai.inf.logCollector", PerfProtocolType.THRIFT, PerfDataType.ALL, StatEnv.Prod, StatGroup.Span,
      (System.currentTimeMillis() / 1000L).toInt)
    val str = new String(rowKeyPrefix, UTF_8).replaceAll("\\|", "\\\\|").replaceAll("\\.", "\\\\.")
    println(str)
  }
  test("testParseDouble") {
    val d = 100L
    val bytes = Bytes.toBytes(d)
    //    val bytes = d.toString.getBytes(UTF_8)
    println(HBaseHelper.parseLong(bytes))
  }

}
