package com.sankuai.octo.spark

import com.sankuai.octo.spark.Helper._
import com.sankuai.octo.spark.domain.{ModuleInvokeAllKey, ModuleInvokeKey}
import com.sankuai.octo.statistic.model.{StatEnv, StatGroup, StatRange}
import org.scalatest.FunSuite

import scala.collection.immutable

/**
  * Created by wujinwu on 16/3/7.
  */
class OctoDataCenterTest extends FunSuite {

  test("testMerge") {
    val map1 = Map("a" -> Map(1 -> 2L, 3 -> 4L), "b" -> Map(1 -> 2L, 3 -> 4L))
    val map2 = Map("c" -> Map(2 -> 4L, 3 -> 5L), "b" -> Map(1 -> 3L, 3 -> 5L))
    val map = Helper.mergeSingleKeyMap(map1, map2)

    println(map)
    println(StatEnv.Prod.toString)
    println(122318312.hashCode())
  }

  test("testSpark") {
    val spanRdd = Array[(ModuleInvokeKey, Map[String, Map[Int, Long]])](
      new ModuleInvokeKey("com.sankuai.inf.logCollector", Helper.getDayStart(System.currentTimeMillis()), 1, "Prod")
        -> Map("span1" -> Map(1 -> 2L, 2 -> 3L, 3 -> 4L), "span2" -> Map(1 -> 2L, 2 -> 3L, 3 -> 4L)),
      new ModuleInvokeKey("com.sankuai.inf.statistic", Helper.getDayStart(System.currentTimeMillis()), 0, "Prod")
        -> Map("span2" -> Map(3 -> 2L, 2 -> 3L, 3 -> 4L), "span3" -> Map(1 -> 2L, 2 -> 3L, 3 -> 4L)))

    val spanHive = spanRdd.flatMap { case (minuteKey, resultMap) =>
      resultMap.toSeq.flatMap { case (tuple, costToCount) =>
        val tags = getTags(spanname = tuple)
        val statDataList = Calculator.calculate(minuteKey, immutable.Seq(StatGroup.Span, StatGroup.SpanRemoteApp, StatGroup.SpanLocalHost,
          StatGroup.SpanRemoteHost), tags, costToCount)(StatRange.Minute)
        statDataList.map { statData =>
          transformStatDataToHiveData(statData)
        }
      }
    }
    spanHive.foreach { item =>
      println(item._1)
      println(item._2)
    }

    val v1 = ModuleInvokeKey("com.sankuai.inf.logCollector", Helper.getDayStart(System.currentTimeMillis()), 1, "Prod")
    val v2 = ModuleInvokeKey("com.sankuai.inf.logCollector", Helper.getDayStart(System.currentTimeMillis()), 1, "Prod")

    println(v1 == v2)
    println(v1 == null)

    val v3 = ModuleInvokeAllKey("com.sankuai.inf.logCollector", Helper.getDayStart(System.currentTimeMillis()), 1, "Prod", "span1", "localhost1", "remoteApp1", "remoteHost1")
    val v4 = ModuleInvokeAllKey("com.sankuai.inf.logCollector", Helper.getDayStart(System.currentTimeMillis()), 1, "Prod",
      "span1", "localhost1", "remoteApp1", "remoteHost2")

    println(v3 == v4)
    println(v3 == null)
    val tableName = "testTable"
    val dt = "20160301"
    println(s"INSERT OVERWRITE TABLE mart_inf.$tableName PARTITION(dt=$dt) " +
      s" SELECT rowkey,count,qps,tp50,tp90,tp95,tp99,cost_max,cost_data FROM $tableName")
  }
}
