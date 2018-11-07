package com.sankuai.octo.query.selfData

import java.util.Locale
import java.util.concurrent.TimeUnit

import com.sankuai.octo.query.helper.QueryCondition
import com.sankuai.octo.query.model.HistoryData.{DataRecord, Point, ResponseTag}
import com.sankuai.octo.statistic.constant.Constants
import com.sankuai.octo.statistic.helper.{HBaseHelper, TimeProcessor}
import com.sankuai.octo.statistic.model._
import com.sankuai.octo.statistic.util.{ExecutionContextFactory, HBaseClient}
import org.joda.time.DateTime
import org.slf4j.LoggerFactory
import org.springframework.util.StringUtils

import scala.collection.mutable.ListBuffer
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future, blocking}

object HbaseData {

  private val logger = LoggerFactory.getLogger(this.getClass)

  private implicit val ec = ExecutionContextFactory.build(Runtime.getRuntime.availableProcessors)

  private val timeout = Duration(20, TimeUnit.SECONDS)

  def historyData(appkey: String, protocolType: String, role: String, dataType: String, env: String, unit: String, condition: QueryCondition,
                  start: Int, end: Int, sortKey: String) = {
    val statRange =
      if (StringUtils.hasText(unit)) {
        //  保留显式指定时间范围
        StatRange.getInstance(unit)
      } else {
        //  通过时间跨度计算查询的时间单位
        val diff = Math.abs(end - start)
        if (diff < 12 * Constants.ONE_HOUR_SECONDS) {
          //  12小时内以分钟为单位
          StatRange.Minute
        } else if (diff < 7 * Constants.ONE_DAY_SECONDS) {
          //  12小时至7天以小时为单位
          StatRange.Hour
        } else {
          //  7天以上以天为单位
          //todo 目前天粒度并没有完全覆盖,暂时以小时为单位
          StatRange.Hour
        }
      }

    val perfProtocol = PerfProtocolType.getInstance(protocolType)
    val perfDataType = PerfDataType.getInstance(dataType)
    val statEnv = StatEnv.getInstance(env)
    val statGroup = condition.statGroup
    val perfRole = PerfRole.getInstance(role)
    val spanname = condition.spanname
    val localhost = condition.localhost
    val remoteHost = condition.remoteHost
    val remoteAppkey = condition.remoteAppkey

    val timeRange = TimeProcessor.getTimeRange(start, end, statRange)

    //  高层次的抽象,单个get可以抽象为时间范围为[ts,ts]的scan查询,统一scan
    val timeRangeList = HBaseHelper.splitTimeRange(timeRange, statRange)

    val futureList = Future.traverse(timeRangeList) { timeRange =>
      Future {
        val scan = HBaseClient.generateTimeRangeHBaseScan(appkey, perfProtocol, perfDataType, statEnv, statGroup, timeRange,
          Some(spanname), Some(localhost), Some(remoteAppkey), Some(remoteHost))
        logger.debug("scan :{}", scan)
        blocking {
          HBaseClient.scan(scan, perfRole, statRange, statGroup)
        }
      }
    }

    val res = futureList.map { list =>
      val queryResults = list.flatten
      // 根据tags分类
      val tmp = queryResults.groupBy(_.responseTag).map { x =>
        val tag = x._1
        val list = x._2.sortBy(_.timestamp) //将并行集合转换成非并行集合
        val infraName = if(list.length >= 1) list.head.infraName else PerfProtocolType.THRIFT.toString
        val responseTag = ResponseTag(protocolType, role, dataType, env, statRange.toString, statGroup.toString,
          Some(tag.spanname), Some(tag.localhost), Some(tag.remoteAppkey), Some(tag.remoteHost), Some(infraName))
        val count, successCount, exceptionCount, timeoutCount, dropCount,
        HTTP2XXCount, HTTP3XXCount, HTTP4XXCount, HTTP5XXCount,
        qps, tp50, tp90, tp99, dropQps = ListBuffer[Point]()
        list.foreach { data =>
          val date = new DateTime(data.timestamp * 1000L).toString("YYYY MM-dd HH:mm, EEE", Locale.US)
          count.append(Point(Some(date), Some(data.count.toDouble), Some(data.timestamp)))
          successCount.append(Point(Some(date), Some(data.successCount.toDouble), Some(data.timestamp)))
          exceptionCount.append(Point(Some(date), Some(data.exceptionCount.toDouble), Some(data.timestamp)))
          timeoutCount.append(Point(Some(date), Some(data.timeoutCount.toDouble), Some(data.timestamp)))
          dropCount.append(Point(Some(date), Some(data.dropCount.toDouble), Some(data.timestamp)))

          HTTP2XXCount.append(Point(Some(date), Some(data.HTTP2XXCount.toDouble), Some(data.timestamp)))
          HTTP3XXCount.append(Point(Some(date), Some(data.HTTP3XXCount.toDouble), Some(data.timestamp)))
          HTTP4XXCount.append(Point(Some(date), Some(data.HTTP4XXCount.toDouble), Some(data.timestamp)))
          HTTP5XXCount.append(Point(Some(date), Some(data.HTTP5XXCount.toDouble), Some(data.timestamp)))

          qps.append(Point(Some(date), Some(data.qps), Some(data.timestamp)))
          tp50.append(Point(Some(date), Some(data.tp50), Some(data.timestamp)))
          tp90.append(Point(Some(date), Some(data.tp90), Some(data.timestamp)))
          tp99.append(Point(Some(date), Some(data.tp99), Some(data.timestamp)))
          dropQps.append(Point(Some(date), Some(data.dropCount / 60), Some(data.timestamp)))
        }
        val ret = DataRecord(appkey, responseTag, count.toList, successCount.toList, exceptionCount.toList, timeoutCount.toList, dropCount.toList,
          HTTP2XXCount.toList, HTTP3XXCount.toList, HTTP4XXCount.toList, HTTP5XXCount.toList,
          qps.toList, tp50.toList, tp90.toList, tp99.toList, dropQps.toList)
        ret
      }.toSeq.filter(_.count.nonEmpty)
      sortDataRecordsByKey(tmp, sortKey)
    }.recover {
      case e: Exception =>
        logger.error(s"historyData fail,appkey:$appkey,unit:${statRange.toString},start:$start,end:$end", e)
        Seq()
    }
    try {
      Await.result(res, timeout)
    } catch {
      case e: Exception =>
        logger.error(s"historyData fail,appkey:$appkey,unit:${statRange.toString},start:$start,end:$end", e)
        Seq()
    }

  }

  private def sortDataRecordsByKey(list: Seq[DataRecord], sortKey: String) = {
    val retList = sortKey match {
      case "qps" => list.sortBy(-_.qps.map(_.y.getOrElse(0.0)).sum)
      case "tp50" => list.sortBy(-_.tp50.map(_.y.getOrElse(0.0)).sum)
      case "tp90" => list.sortBy(-_.tp90.map(_.y.getOrElse(0.0)).sum)
      case "tp99" => list.sortBy(-_.tp99.map(_.y.getOrElse(0.0)).sum)
      case _ => list.sortBy(-_.qps.map(_.y.getOrElse(0.0)).sum)
    }
    retList
  }
}
