package com.sankuai.octo.processor

import com.sankuai.octo.statistic.metrics.SimpleCountHistogram2
import org.scalatest.FunSuite

class DailyHistogramProcessorSuite extends FunSuite {
  test("processHistogram") {
    val gram1 = new SimpleCountHistogram2()
    val gram2 = new SimpleCountHistogram2()

    updateHistogram(gram1)
    updateHistogram(gram2)
  }




  def updateHistogram(gram: SimpleCountHistogram2) {
    (1 to 10000).foreach{
      x =>
      val cost = (Math.random * 20).toInt
      gram.update(cost)
    }
  }
}
