/*
package com.sankuai.octo.statistic.metric

import java.util.concurrent.atomic.{AtomicInteger, AtomicLong}
import java.util.concurrent.{ConcurrentHashMap, CountDownLatch, Executors, TimeUnit}

import com.meituan.mtrace.thrift.model.StatusCode
import com.sankuai.octo.statistic.constant.Constants
import com.sankuai.octo.statistic.domain.{Instance, InstanceKey}
import com.sankuai.octo.statistic.metrics.SimpleCountHistogram
import com.sankuai.octo.statistic.model._
import org.junit.Assert
import org.scalatest.FunSuite

import scala.collection.concurrent.TrieMap
import scala.util.Random

/**
 * Created by zava on 15/9/24.
 */
class MetricManagerSuite extends FunSuite {

  test("getInstance") {
    val appkeys = Array("com.sankuai.inf.test1.logCollector", "com.sankuai.inf.test2.logCollector",
      "com.sankuai.inf.test3.logCollector", "com.sankuai.inf.test4.logCollector")
    val spanLocalHost = "192.168.1."
    val spanLocalHosts = new Array[String](10)
    (0 to 9).foreach {
      i =>
        spanLocalHosts(i) = spanLocalHost + (i + 1)
    }

    //分钟的开始
    val ts = (System.currentTimeMillis() / 1000).toInt / 60 * 60
    var tagsMap = Map(Constants.SPAN_NAME -> "hello1")
    val mapInstance = TrieMap[InstanceKey, Instance]()
    for (appkey <- appkeys) {
      for (spanLocalHost <- spanLocalHosts) {
        tagsMap += (Constants.LOCAL_HOST -> spanLocalHost)
        val instanceKey = new InstanceKey(appkey, ts, StatEnv.Prod, StatSource.Server, StatRange.Minute, StatGroup.Span, PerfProtocolType.THRIFT, tagsMap)
        val instance = MetricManager.getInstance(instanceKey)
        //        println(s"instancekey:${instance.key} histogram :${instance.histogram}")
        mapInstance.put(instanceKey, instance)
      }
    }
    for (appkey <- appkeys) {
      for (spanLocalHost <- spanLocalHosts) {
        tagsMap += (Constants.LOCAL_HOST -> spanLocalHost)
        val instanceKey = new InstanceKey(appkey, ts, StatEnv.Prod, StatSource.Server, StatRange.Minute, StatGroup.Span, PerfProtocolType.THRIFT, tagsMap)
        val instance = MetricManager.getInstance(instanceKey)
        val mapValue = mapInstance.get(instanceKey)
        mapValue match {
          case Some(mapValue) => Assert.assertEquals(mapValue, instance)
          case None => print(s"instancekey: $instanceKey error")
        }

      }
    }
  }

  test("get time cost") {
    val appkeys = Array("com.sankuai.inf.test1.logCollector", "com.sankuai.inf.test2.logCollector",
      "com.sankuai.inf.test3.logCollector", "com.sankuai.inf.test4.logCollector")
    val spanLocalHost = "192.168.1."
    val spanLocalHosts = new Array[String](10)
    (0 to 9).foreach {
      i =>
        spanLocalHosts(i) = spanLocalHost + (i + 1)
    }

    var tagsMap = Map(Constants.SPAN_NAME -> "hello1")
    val mapInstance = new ConcurrentHashMap[InstanceKey, Instance]()
    //val mapInstance = TrieMap[InstanceKey, Instance]()
    (1 to 25000).foreach {
      x =>
        appkeys.foreach {
          appkey =>
            spanLocalHosts.foreach {
              spanLocalHost =>
                tagsMap += (Constants.LOCAL_HOST -> spanLocalHost)
                val instanceKey = new InstanceKey(appkey, Random.nextInt(1000000), StatEnv.Prod, StatSource.Server, StatRange.Minute, StatGroup.Span, PerfProtocolType.THRIFT, tagsMap)
                val instance = new Instance(instanceKey, new SimpleCountHistogram())
                mapInstance.put(instanceKey, instance)
            }
        }
    }
    println(mapInstance.size)
    val start = System.currentTimeMillis()
    (1 to 1000000).foreach {
      x =>
        appkeys.foreach {
          appkey =>
            spanLocalHosts.foreach {
              spanLocalHost =>
                tagsMap += (Constants.LOCAL_HOST -> spanLocalHost)
                val instanceKey = new InstanceKey(appkey, Random.nextInt(1000000), StatEnv.Prod, StatSource.Server, StatRange.Minute, StatGroup.Span, PerfProtocolType.THRIFT, tagsMap)
                mapInstance.get(instanceKey)
            }
        }
    }
    val end = System.currentTimeMillis()
    println(end - start)
  }

  test("TrieMap remove") {
    val counter = new AtomicLong(0)
    val map = new TrieMap[Int, Int]()
    val poolExecutor = Executors.newFixedThreadPool(100)
    (1 to 50).foreach {
      _ =>
        poolExecutor.submit(new Runnable {
          override def run(): Unit = {
            while (true) {
              try {
                map.put(Random.nextInt(10000), Random.nextInt(100000))
                counter.incrementAndGet()
              } catch {
                case e: Exception =>
                  println(e)
              }
            }
          }
        })
    }
    (1 to 30).foreach {
      _ =>
        poolExecutor.submit(new Runnable {
          override def run(): Unit = {
            while (true) {
              try {
                map.get(Random.nextInt(10000))
                counter.incrementAndGet()
              } catch {
                case e: Exception =>
                  println(e)
              }
            }
          }
        })
    }
    (1 to 10).foreach {
      _ =>
        poolExecutor.submit(new Runnable {
          override def run(): Unit = {
            while (true) {
              try {
                map.remove(Random.nextInt(10000))
                counter.incrementAndGet()
              } catch {
                case e: Exception =>
                  println(e)
              }
            }
          }
        })
    }
    (1 to 10).foreach {
      _ =>
        poolExecutor.submit(new Runnable {
          override def run(): Unit = {
            while (true) {
              try {
                val itr = map.iterator
                while (itr.hasNext) {
                  val key = itr.next()._1
                  if (key < 5000) {
                    counter.incrementAndGet()
                    map.remove(key)
                  }
                }
              } catch {
                case e: Exception =>
                  println(e)
              }
            }
          }
        })
    }
    Thread.sleep(5000)
    println(counter.incrementAndGet() + " " + map.size)
  }

  test("concurrent remove") {
    val counter = new AtomicLong(0)
    val map = new ConcurrentHashMap[Int, Int]()
    val poolExecutor = Executors.newFixedThreadPool(100)
    (1 to 50).foreach {
      _ =>
        poolExecutor.submit(new Runnable {
          override def run(): Unit = {
            while (true) {
              try {
                map.put(Random.nextInt(10000), Random.nextInt(100000))
                counter.incrementAndGet()
              } catch {
                case e: Exception =>
                  println(e)
              }
            }
          }
        })
    }
    (1 to 30).foreach {
      _ =>
        poolExecutor.submit(new Runnable {
          override def run(): Unit = {
            while (true) {
              try {
                map.get(Random.nextInt(10000))
                counter.incrementAndGet()
              } catch {
                case e: Exception =>
                  println(e)
              }
            }
          }
        })
    }
    (1 to 10).foreach {
      _ =>
        poolExecutor.submit(new Runnable {
          override def run(): Unit = {
            while (true) {
              try {
                map.remove(Random.nextInt(10000))
                counter.incrementAndGet()
              } catch {
                case e: Exception =>
                  println(e)
              }
            }
          }
        })
    }
    (1 to 10).foreach {
      _ =>
        poolExecutor.submit(new Runnable {
          override def run(): Unit = {
            while (true) {
              try {
                val itr = map.entrySet().iterator()
                while (itr.hasNext) {
                  val key = itr.next().getKey
                  if (key < 5000) {
                    counter.incrementAndGet()
                    itr.remove()
                  }
                }
              } catch {
                case e: Exception =>
                  println(e)
              }
            }
          }
        })
    }
    Thread.sleep(5000)
    println(counter.incrementAndGet() + " " + map.size())
  }

  //多线程同时访问 获取instance 最终得到一个
  test("threadGetInstance") {
    val appkey = "com.sankuai.inf.test1.logCollector"
    val ts = (System.currentTimeMillis() / 1000).toInt / 60 * 60
    val tagsMap = Map(Constants.SPAN_NAME -> "hello1")
    val begin = new CountDownLatch(1)
    val counter = new AtomicInteger
    val poolExecutor = Executors.newFixedThreadPool(100)
    (1 to 10).foreach {
      _ =>
        poolExecutor.submit(new Runnable {
          override def run(): Unit = {
            begin.await()
            counter.incrementAndGet()
            val instanceKey = new InstanceKey(appkey, ts, StatEnv.Prod, StatSource.Server, StatRange.Minute, StatGroup.Span, PerfProtocolType.THRIFT, tagsMap)
            val instance = MetricManager.getInstance(instanceKey)
            println(s"instancekey:${instance.key} histogram :${instance.histogram}")
          }
        })

    }
    begin.countDown()

    poolExecutor.shutdown()
    while (!poolExecutor.isTerminated) {
      try {
        TimeUnit.SECONDS.sleep(5)
      }
      catch {
        case e: InterruptedException => e.printStackTrace()
      }
    }
  }
  //正常的数据,和过期的数据
  test("export") {
    initMetricManager()
    MetricManager.exportMinute()
  }

  private def initMetricManager(): Unit = {
    val appkey = "com.sankuai.inf.test1.logCollector"
    val ts_array = Array((System.currentTimeMillis() / 1000).toInt / 60 * 60,
      (System.currentTimeMillis() / 1000 - 180).toInt / 60 * 60,
      (System.currentTimeMillis() / 1000 - 240).toInt / 60 * 60,
      (System.currentTimeMillis() / 1000 - 300).toInt / 60 * 60)
    var i = 0
    for (ts <- ts_array) {
      val tagsMap = Map(Constants.SPAN_NAME -> ("hello" + i))
      val instanceKey = new InstanceKey(appkey, ts, StatEnv.Prod, StatSource.Server, StatRange.Minute, StatGroup.Span, PerfProtocolType.THRIFT, tagsMap)
      val instance = MetricManager.getInstance(instanceKey)
      (1 to 100000).foreach(i => {
        val cost = getRandomCost(i)
        instance.update(new MetricData(System.currentTimeMillis(), 1, cost, StatusCode.SUCCESS))
      })
      i += 1
    }
  }

  private def getRandomCost(i: Int): Int = {
    var cost = 0
    if (i % 9999 == 0) {
      //极少数 在30s 以上
      cost = Random.nextInt(1000) + 30000
    } else if (i % 999 == 0) {
      //少部分在 40~50ms
      cost = Random.nextInt(10)
    } else if (i % 2 == 0) {
      //50% 在20~30ms
      cost = Random.nextInt(10) + 20
    } else if (i % 3 == 0) {
      //少部分在 40~50ms
      cost = Random.nextInt(10) + 40
    } else {
      //剩下的随机分布在1s~2s之间
      cost = Random.nextInt(1000) + 1000
    }
    cost
  }
}*/
