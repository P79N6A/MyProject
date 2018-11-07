package com.sankuai.octo.aggregator

import com.sankuai.octo.parser.{Metric, Type}
import org.scalatest.FunSuite

import scala.collection.JavaConverters._
import scala.util.Random

/**
  * Created by wujinwu on 15/11/11.
  */
class senderTest extends FunSuite {

  test("sender") {
    val token = "5559fd5f8ee440bb1bbc91d8"
    val key = "clientCount"
    val value: Double = 10
    val metric = new Metric(token, Type.COUNTER, key, value)
    val tags = Map("spanname" -> "test2", "localhost" -> "xxx", "remoteApp" -> "test1", "remoteHost" -> "172.30.8.161", "status" -> "0")
    metric.setTags(tags.asJava)
    while (true) {
      try {
        val value = Random.nextInt(10).toDouble
        metric.setValue(if (value < 0) -value else value)
        sender.asyncSend(metric)
        Thread.sleep(5)
      } catch {
        case e: Exception => println(e)
      }
    }
    val list = List()
    list.grouped(100).foreach(println)
  }
}
