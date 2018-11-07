package com.sankuai.octo.statistic.domain

import com.sankuai.octo.statistic.constant.Constants
import com.sankuai.octo.statistic.model._
import org.scalatest.FunSuite

import scala.util.Random
import com.google.common.base.Stopwatch
import java.util.concurrent.TimeUnit

/**
  * Created by wujinwu on 16/1/20.
  */
class InstanceKeyTest extends FunSuite {

  test("testHashCode") {
    val key = new InstanceKey(Random.nextString(10), (System.currentTimeMillis() / 1000).toInt, StatEnv.Prod,
      StatSource.Server, StatRange.Day, StatGroup.Span, PerfProtocolType.THRIFT,Map(
        Constants.SPAN_NAME -> Random.nextString(10),
        Constants.LOCAL_HOST -> Random.nextString(10),
        Constants.REMOTE_APPKEY -> Random.nextString(10),
        Constants.REMOTE_HOST -> Random.nextString(10)))
    //    val key3 = key.clone()
    val key2 = new InstanceKey("abs", (System.currentTimeMillis() / 1000).toInt, StatEnv.Prod,
      StatSource.Server, StatRange.Day, StatGroup.Span, PerfProtocolType.THRIFT, Map(
        Constants.SPAN_NAME -> "abs",
        Constants.LOCAL_HOST -> "abs",
        Constants.REMOTE_APPKEY -> "abs",
        Constants.REMOTE_HOST -> "abs"))
    val key3 = new InstanceKey("abs", (System.currentTimeMillis() / 1000).toInt, StatEnv.Prod,
      StatSource.Server, StatRange.Day, StatGroup.Span, PerfProtocolType.THRIFT, Map(
        Constants.SPAN_NAME -> "abs",
        Constants.LOCAL_HOST -> "abs",
        Constants.REMOTE_APPKEY -> "abs",
        Constants.REMOTE_HOST -> "abs"))
    println(key.hashCode())
    println(key2.hashCode())
    println(key3.hashCode())
    println(key == key2)
    println(key2 == key3)


        val sw = Stopwatch.createStarted()

    //    val map = new ConcurrentHashMap[InstanceKey, InstanceKey2]()
    //    map.put(key, keyTest)
    //    map.put(key2, keyTest2)
    //    println(key.hashCode() == key3.hashCode())
    //    (1 to 100000000).foreach { _ =>
    //      map.get(key)
    //    }
    //    println(s"ms:${sw.stop().elapsed(TimeUnit.MILLISECONDS)}")
    //    val map2 = TrieMap[InstanceKey, InstanceKey2]()
    //    map2.put(key, keyTest)
    //    map2.put(key2, keyTest2)
    //    sw.reset().start()
    //    (1 to 100000000).foreach { _ =>
    //      map2.get(key)
    //    }
    //    println(s"ms:${sw.stop().elapsed(TimeUnit.MILLISECONDS)}")


//        sw.reset().start()
//        (1 to 100000000).foreach { _ =>
//          keyTest.hashCode()
//        }
//        println(s"ms:${sw.stop().elapsed(TimeUnit.MILLISECONDS)}")

        sw.reset().start()
        (1 to 10000000).foreach { _ =>
          key.hashCode() == key2.hashCode()
          key2.hashCode() == key3.hashCode()
        }
        println(s"ms:${sw.stop().elapsed(TimeUnit.MILLISECONDS)}")

    //    sw.reset().start()
    //    (1 to 100000000).foreach { _ =>
    //      keyTest.equals(keyTest2)
    //    }
    //    println(s"ms:${sw.stop().elapsed(TimeUnit.MILLISECONDS)}")
    //
    //    sw.reset().start()
    //    (1 to 100000000).foreach { _ =>
    //      key.equals(key2)
    //    }
    //    println(s"ms:${sw.stop().elapsed(TimeUnit.MILLISECONDS)}")
  }

}
