package com.sankuai.octo.statistic.cache

import java.util.concurrent.{ConcurrentSkipListSet, TimeUnit}

import com.google.common.cache.{CacheBuilder, CacheLoader}
import com.sankuai.octo.statistic.helper.Serializer
import com.sankuai.sgagent.thrift.model.DropRequest
import org.scalatest.FunSuite

import scala.util.Random

/**
 * Created by wujinwu on 15/11/2.
 */
class CacheTest extends FunSuite {
  test("CacheTest") {
    val qpsCache = CacheBuilder.newBuilder().expireAfterWrite(1L, TimeUnit.MINUTES).build[String,String](new  CacheLoader[String,String] {
    override def load(key: String): String = if(key == "a") "aVal" else throw new RuntimeException
  })
    try {
      val b = qpsCache.get("b")
    } catch {
      case e: Exception =>
    }
  }
  test("null") {
    (1 to 100).foreach(_ => {
      val list: Seq[String] = if (Random.nextBoolean()) {
        Seq("aaaaaaa")
      } else {
        null
      }
      if (null == list) {
        println("!!!!!!!!!!!!")
      }
    })
  }
  test("CacheSerial") {

    val drop = new DropRequest("aaaaaaaa", "aaaaaaaa", "aaaaaaaa", "aaaaaaaa", 1, 2, 3)

    val bytes = Serializer.toBytes(drop)
    val obj = Serializer.toObject(bytes, classOf[DropRequest])
    println(obj)
    //    obj.foreach(println)

    Map(1 -> "", 2 -> "abs").flatMap(entry => {
      if (entry._2.isEmpty)
        Option(null)
      else {
        Option(entry._1, entry._2)
      }
    }).foreach(println)
  }
  test("testSet") {
    val set = new ConcurrentSkipListSet[String]()
    val start = System.currentTimeMillis()
    (1 to 10000000).foreach { _ =>
      val s = new String("a")
      set.add(s)
    }
    println(System.currentTimeMillis() - start)
    val start2 = System.currentTimeMillis()
    var nset = Set[Int]()

    (1 to 10000000).foreach { _ =>
      val set2 = nset + Random.nextInt(10)
      println(set2 eq nset)
      nset = set2

    }
    println(System.currentTimeMillis() - start2)
  }

}
