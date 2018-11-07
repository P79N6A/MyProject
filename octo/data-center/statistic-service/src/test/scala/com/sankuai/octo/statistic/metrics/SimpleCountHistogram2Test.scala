package com.sankuai.octo.statistic.metrics

import java.text.DecimalFormat
import java.util.concurrent
import java.util.concurrent.ConcurrentHashMap
import java.util.logging.Logger

import com.meituan.mtrace.thrift.model.StatusCode
import org.scalatest.FunSuite

import scala.collection.JavaConverters._
import scala.util.Random

/**
  * Created by wujinwu on 16/5/28.
  */
class SimpleCountHistogram2Test extends FunSuite {
  test("testSimple") {
    val histogram = new SimpleCountHistogram2
    histogram.update(1, 2, StatusCode.SUCCESS)
    val snap = histogram.getSnapshot


    val df: DecimalFormat = new DecimalFormat("#.###")

    // 计算cost
    println(df.format(snap.getMedian).toDouble)
    println(snap.getMedian)
  }

  def randomInt(max: Int = 100) = Random.nextInt(max)

  test("merge") {
    val cost2Count1 = new ConcurrentHashMap[Integer, java.lang.Long]()
    (0 to 10).toList.foreach{
      x=>
        cost2Count1.put(randomInt(40), java.lang.Long.valueOf(randomInt(100)))
    }
    val max1 =cost2Count1.keys().asScala.max

    val reservoir1 = new SimpleCountReservoir2(max1, cost2Count1)
    val histogram1 = new SimpleCountHistogram2(100,50,10,10,10,2,3,4,5,1,123,123,reservoir1)

    val cost2Count2 = new ConcurrentHashMap[Integer, java.lang.Long]()
    (0 to 10).toList.foreach{
      x=>
        cost2Count2.put(randomInt(30), java.lang.Long.valueOf(randomInt(100)))
    }
    val max2 = cost2Count2.keys().asScala.max

    val reservoir2 = new SimpleCountReservoir2(max2, cost2Count2)
    val histogram2 = new SimpleCountHistogram2(200,100,20,20,20,4,6,8,10,2,123,123,reservoir2)

    val reservoir2Temp = new SimpleCountReservoir2(max2, cost2Count2)
    val histogram2Temp = new SimpleCountHistogram2(200,100,20,20,20,4,6,8,10,2,123,123,reservoir2Temp)

    val histogramMerged = histogram2.merge(histogram1)
    val max3 = histogramMerged.getReservoir.getMax

    println(s"max in historgam1 is: $max1")
    println(s"max in historgam2 is: $max2")
    println(s"max in historgam merged is: $max3")
    assert(max3 == Math.max(max1, max2), "merge reservoir failed")
    assert((histogram1.count + histogram2Temp.count) == histogramMerged.count, "merge count failed")
  }
}
