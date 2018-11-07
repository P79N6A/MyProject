package com.sankuai.octo.spark

import java.nio.charset.StandardCharsets._

import org.apache.hadoop.hbase.KeyValue
import org.apache.hadoop.hbase.util.Bytes
import org.scalatest.FunSuite

/**
  * Created by wujinwu on 16/2/19.
  */
class HelperTest extends FunSuite {

  test("testParseDate") {
    println(Helper.parseDate(Array("20160130", "20160206")))
    println(Helper.parseDate(Array("20160130")))

    println(Helper.getDayStart(System.currentTimeMillis()))
  }
  test("testSort") {
    val row = "f647ecd30b26045c4bb1de5651d8baa9|0|Stage|com.sankuai.inf.data.statistic|0|0|1458053580|GetRequest|10.4.38.87".getBytes(UTF_8)
    val COLUMN_FAMILY = "D".getBytes(UTF_8)
    val SUCCESS_COUNT_COLUMN = "success_count".getBytes(UTF_8)
    val EXCEPTION_COUNT_COLUMN = "exception_count".getBytes(UTF_8)


    val v1 = new KeyValue(row, COLUMN_FAMILY, SUCCESS_COUNT_COLUMN, Bytes.toBytes(0L))
    val v2 = new KeyValue(row, COLUMN_FAMILY, EXCEPTION_COUNT_COLUMN, Bytes.toBytes(0L))
    println(KeyValue.COMPARATOR.compare(v1, v2))

  }
}
