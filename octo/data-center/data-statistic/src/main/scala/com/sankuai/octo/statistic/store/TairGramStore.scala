/*
package com.sankuai.octo.statistic.store

import java.io.{ByteArrayInputStream, ByteArrayOutputStream}

import com.sankuai.octo.statistic.metrics.SimpleCountHistogram
import com.sankuai.octo.statistic.model.StatRange
import com.sankuai.octo.statistic.util.tair
import org.slf4j.LoggerFactory

object TairGramStore extends AbstractGramStore {
  private val logger = LoggerFactory.getLogger(this.getClass)

  def getDayGram(name: String): Option[SimpleCountHistogram] = {
    val key = s"gram|${StatRange.Day}|$name"
    getGram(key)
  }

  def getMinuteGram(name: String): Option[SimpleCountHistogram] = {
    val key = s"gram|${StatRange.Minute}|$name"
    getGram(key)
  }

  private def getGram(key: String): Option[SimpleCountHistogram] = {
    val gram = tair.getValue(key).map {
      bytes =>
        val histogram = new SimpleCountHistogram()
        histogram.init(new ByteArrayInputStream(bytes))
        histogram
    }
    logger.debug(s"getGram $key $gram")
    gram
  }

  def updateDayGram(name: String, histogram: SimpleCountHistogram) {
    updateGram(s"gram|${StatRange.Day}|$name", histogram, 86400 * 2)
  }

  def updateMinuteGram(name: String, histogram: SimpleCountHistogram) {
    updateGram(s"gram|${StatRange.Minute}|$name", histogram, 600)
  }

  private def updateGram(key: String, histogram: SimpleCountHistogram, expire: Int) {
    logger.debug(s"updateGram $key $histogram")
    val stream = new ByteArrayOutputStream()
    histogram.dump(stream)
    tair.put(key, stream.toByteArray, expire)
  }

  override def syncGram(key: String, gramBytes: Array[Byte]): Unit = ???
}
*/
