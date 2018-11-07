package com.sankuai.octo

import java.io.ByteArrayInputStream
import java.text.DecimalFormat
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.{Executors, TimeUnit}

import com.meituan.mtrace.thrift.model.StatusCode
import com.sankuai.octo.statistic.constant.Constants
import com.sankuai.octo.statistic.domain.{Instance, InstanceKey}
import com.sankuai.octo.statistic.metrics.{SimpleCountHistogram, SnapShotPrinter}
import com.sankuai.octo.statistic.model._
import org.junit.Assert
import org.scalatest.FunSuite

import scala.util.Random

/**
 * Created by wujinwu on 15/9/24.
 */
class InstanceTest extends FunSuite {

  test("update") {
    // 计算qps要考虑到超出时间范围
    val timeRange: Int = (System.currentTimeMillis() / 1000L).toInt - (System.currentTimeMillis() / 1000L).toInt
    //    timeRange = if (timeRange > key.range.getTimeRange) key.range.getTimeRange else timeRange

    val df: DecimalFormat = new DecimalFormat("#.###")
    //    println(df.format(1.toDouble / timeRange).toDouble)

    val instance = getInstance()
    val start = System.currentTimeMillis()
    val gram = new SimpleCountHistogram()
    (1 to 100000).foreach(i => {
      val cost = getRandomCost(i)
      gram.update(cost)
      instance.update(new MetricData(start, 1, cost, StatusCode.SUCCESS))

    })

    val simpleHistogram = instance.histogram
    val simpleSnapshot = simpleHistogram.getSnapshot

    val snapshot = gram.getSnapshot
    assert(gram.getCount == simpleHistogram.getCount)

    SnapShotPrinter.print(snapshot)

    Assert.assertEquals(snapshot.getMax, simpleSnapshot.getMax)
    Assert.assertEquals(snapshot.getMin, simpleSnapshot.getMin)
    Assert.assertEquals(snapshot.getMean, simpleSnapshot.getMean, 0.01F)
    Assert.assertEquals(snapshot.get999thPercentile, simpleSnapshot.get999thPercentile, 0.01F)
    Assert.assertEquals(snapshot.get99thPercentile, simpleSnapshot.get99thPercentile, 0.01F)
    Assert.assertEquals(snapshot.get98thPercentile, simpleSnapshot.get98thPercentile, 0.01F)
    Assert.assertEquals(snapshot.get75thPercentile, simpleSnapshot.get75thPercentile, 0.01F)
    Assert.assertEquals(snapshot.getMedian, simpleSnapshot.getMedian, 0.01F)
    Assert.assertEquals(snapshot.getStdDev, simpleSnapshot.getStdDev, 0.1F)
  }

  test("threadUpdate") {
    val instance = getInstance()
    val gram = getHistogrm()
    val poolExecutor = Executors.newFixedThreadPool(1000)
    val start = System.currentTimeMillis()
    (1 to 100000).foreach(i => {
      val cost = getRandomCost(i)
      poolExecutor.submit(new Runnable {
        override def run(): Unit = {
          instance.update(new MetricData(start, 1, cost, StatusCode.SUCCESS))
        }
      })
    })
    println("耗时:" + (System.currentTimeMillis() - start))
    TimeUnit.SECONDS.sleep(1)
    poolExecutor.shutdown()
    while (!poolExecutor.isTerminated) {
      try {
        TimeUnit.SECONDS.sleep(1)
      }
      catch {
        case e: InterruptedException => e.printStackTrace()
      }
    }

    val simpleHistogram = instance.histogram
    val simpleSnapshot = simpleHistogram.getSnapshot

    val snapshot = gram.getSnapshot

    SnapShotPrinter.print(snapshot)
    println(s"count: ${gram.getCount}")

    SnapShotPrinter.print(simpleSnapshot)
    println(s"instacne count: ${simpleHistogram.getCount}")


    assert(gram.getCount == simpleHistogram.getCount)

    Assert.assertEquals(snapshot.getMax, simpleSnapshot.getMax, 500)
    Assert.assertEquals(snapshot.getMin, simpleSnapshot.getMin)
    Assert.assertEquals(snapshot.getMean, simpleSnapshot.getMean, 2.0F)
    Assert.assertEquals(snapshot.get999thPercentile, simpleSnapshot.get999thPercentile, 1.0F)
    Assert.assertEquals(snapshot.get99thPercentile, simpleSnapshot.get99thPercentile, 5.0F)
    Assert.assertEquals(snapshot.get98thPercentile, simpleSnapshot.get98thPercentile, 5.01F)
    Assert.assertEquals(snapshot.get75thPercentile, simpleSnapshot.get75thPercentile, 5.01F)
    Assert.assertEquals(snapshot.getMedian, simpleSnapshot.getMedian, 1.0F)
    Assert.assertEquals(snapshot.getStdDev, simpleSnapshot.getStdDev, 1.0F)

  }
  test("export") {
    val count = new AtomicLong(0)
    val ts = (System.currentTimeMillis() / 1000 / 60 * 60).toInt
    val stat = exportStatData(count)
    val qps = stat.getCount.toDouble / (System.currentTimeMillis() / 1000L - ts)
    Assert.assertEquals(qps, stat.getQps, 0.001F)
    assert(stat.getCount == count.longValue())
    println(stat)
  }

  def exportStatData(count: AtomicLong = new AtomicLong()) = {
    val instance = getInstance()
    val start = System.currentTimeMillis()
    (1 to 100000).foreach(_ => {
      count.addAndGet(1)
      val r = Random.nextInt(1000)
      if (r % 100 == 0) {
        instance.update(new MetricData(start, 1, 5000, StatusCode.SUCCESS))
      } else if (r % 999 == 0) {
        instance.update(new MetricData(start, 1, 999, StatusCode.SUCCESS))
      } else {
        instance.update(new MetricData(start, 1, Random.nextInt(100), StatusCode.SUCCESS))
      }
    })
    val stat = instance.export()
    stat
  }

  test("serialize") {
    val instance = getInstance()
    val start = System.currentTimeMillis()
    (1 to 100000).foreach(i => {
      val cost = getRandomCost(i)
      instance.update(new MetricData(start, 1, cost, StatusCode.SUCCESS))
    })
    val outputStream = instance.serialize()
    val newhistogram = new SimpleCountHistogram
    val histogram = instance.histogram
    newhistogram.init(new ByteArrayInputStream(outputStream.toByteArray))

    val srcSnapshot = histogram.getSnapshot
    val targSnapshot = newhistogram.getSnapshot

    Assert.assertEquals(newhistogram.getCount, histogram.getCount)
    Assert.assertEquals(srcSnapshot.getMax, targSnapshot.getMax)
    Assert.assertEquals(srcSnapshot.getMin, targSnapshot.getMin)
    Assert.assertEquals(srcSnapshot.get75thPercentile, targSnapshot.get75thPercentile, 0.00001f)
    Assert.assertEquals(srcSnapshot.getMedian, targSnapshot.getMedian, 0.00001f)
    Assert.assertEquals(srcSnapshot.get99thPercentile, targSnapshot.get99thPercentile, 0.00001f)
    Assert.assertEquals(srcSnapshot.getStdDev, targSnapshot.getStdDev, 0.00001f)

  }

  private def getInstance(): Instance = {
    val ts = (System.currentTimeMillis() / 1000 / 60 * 60).toInt
    val key = new InstanceKey("testApp", ts, StatEnv.Prod, StatSource.Server, StatRange.Minute, StatGroup.Span, PerfProtocolType.THRIFT,Map(
      Constants.SPAN_NAME -> "testSpan",
      Constants.LOCAL_HOST -> "testlocalHost",
      Constants.REMOTE_APPKEY -> "testRemoteAppKey",
      Constants.REMOTE_HOST -> "testRemoteHost"))
    new Instance(key, new SimpleCountHistogram())
  }

  private def getHistogrm(): SimpleCountHistogram = {
    val gram = new SimpleCountHistogram()
    (1 to 100000).foreach(i => {
      val cost = getRandomCost(i)
      gram.update(cost)
    })
    gram
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


}

