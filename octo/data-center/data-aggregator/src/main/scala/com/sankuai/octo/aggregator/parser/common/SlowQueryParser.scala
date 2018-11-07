package com.sankuai.octo.aggregator.parser.common

import com.sankuai.octo.aggregator.perf
import com.sankuai.octo.aggregator.thrift.model.TraceThresholdLogList
import com.sankuai.octo.statistic.helper.Serializer
import com.sankuai.octo.statistic.util.ExecutorFactory
import org.slf4j.LoggerFactory

object SlowQueryParser {
  private val logger = LoggerFactory.getLogger(SlowQueryParser.getClass)

  val asyncSlowLogParser = new ExecutorFactory(parseSlowLog, "CommonLogParser.asyncSlowLogParser", 2, 8, 20000)

  def processSlowQuery(logContent: Array[Byte]): Unit = {
    val logList = Serializer.toObject(logContent, classOf[TraceThresholdLogList])
    logger.debug("receive threshold logs: {}", logList.getLogsSize)
    asyncSlowLogParser.submit(logList)
  }

  private def parseSlowLog(list: TraceThresholdLogList) {
    perf.procesSlowQuery(list)
  }
}
