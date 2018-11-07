package com.sankuai.octo.aggregator.store

import com.meituan.service.hbase.config.Config
import org.scalatest.FunSuite

/**
 * Created by wujinwu on 15/10/22.
 */
class TableStoreServiceTest extends FunSuite {

  test("testStore") {
    val config = Config.loadFromFile()
    println(config.getHBaseClientConf.get("hbase.zookeeper.quorum"))
  }

  test("testHBaseGet") {
//    val list = TableStoreService.getTraceIdsByServiceName("com.sankuai.inf.logCollector", System.currentTimeMillis() / 1000L, 1000)
//    list.foreach(println)
  }
}
