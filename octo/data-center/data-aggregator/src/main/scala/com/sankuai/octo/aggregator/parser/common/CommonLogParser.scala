package com.sankuai.octo.aggregator.parser.common

import com.meituan.mtrace.thrift.model.Constants.{TRACE_COMPRESS_DATA_LIST, TRACE_DATA_LIST}
import com.sankuai.octo.aggregator.thrift.model.CommonLog
import com.sankuai.octo.aggregator.util.{FunctionSwitch, Threshold}
import com.sankuai.octo.statistic.constant.Constants
import com.sankuai.octo.statistic.util.ExecutorFactory
import org.slf4j.LoggerFactory

object CommonLogParser {
  private val logger = LoggerFactory.getLogger(this.getClass)
  val asyncCommLogParser = new ExecutorFactory(parseCommonLog, "CommonLogParser.asyncCommLogParser", 6, 8, 20000)

  def putCommonLog(log: CommonLog) {
    asyncCommLogParser.submit(log)
  }

  private def parseCommonLog(log: CommonLog) {
    logger.debug(s"log:$log")
    val start = System.currentTimeMillis()
    try {
      val content = log.getContent
      log.getCmd match {
        case Constants.TRACE_THRESHOLD_LOG =>
          if (FunctionSwitch.slowQueryParserSwitch == FunctionSwitch.SWITCH_ON) {
            SlowQueryParser.processSlowQuery(content)
          }
        case Constants.DROP_REQUEST_LOG =>
          DropParser.processDrop(content)
        case Constants.MTRACE_LOG_LIST =>
          MtraceParser.processMtraceChain(content)
        case TRACE_DATA_LIST =>
          MtraceParser.processMtraceTotal(content)
        case TRACE_COMPRESS_DATA_LIST =>
          MtraceParser.processCompress(content)
        case _ => logger.debug(s"cmd:${log.getCmd},extend:${log.getExtend}")
      }
    } catch {
      case e: Exception => logger.error(s"parseCommonLog Fail,cmd:${log.getCmd}", e)
    }
    val end = System.currentTimeMillis()
    if (end - start > Threshold.MIDDLE) {
      logger.warn(s"parseCommonLog overtime ${end - start} $log")
    }
  }
}
