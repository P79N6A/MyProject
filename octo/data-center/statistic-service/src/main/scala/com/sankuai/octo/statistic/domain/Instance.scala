package com.sankuai.octo.statistic.domain

import java.io.ByteArrayOutputStream
import java.text.DecimalFormat

import com.meituan.mtrace.thrift.model.StatusCode
import com.meituan.service.mobile.mtthrift.util.ProcessInfoUtil
import com.sankuai.octo.statistic.metrics.SimpleCountHistogram
import com.sankuai.octo.statistic.model._
import com.sankuai.sgagent.thrift.model.PerfCostDataList
import org.slf4j.LoggerFactory
import org.springframework.util.CollectionUtils

/**
 *
 * @param key 对应维度的key
 * @param histogram  维护对应key的直方图
 */
class Instance(val key: InstanceKey, val histogram: SimpleCountHistogram) extends Serializable {

  import Instance.logger

  /**
   *
   * @param data 更新的metricData
   */
  def update(data: MetricData): Unit = {
    // 全量之后，每条的日志的count是1，兼容旧版idl
    if (data.status != null) {
      update(data.cost, data.count, data.status)
    } else {
      update(data.cost, data.count, StatusCode.SUCCESS)
    }
  }

  def update(cost: Int, count: Long, status: StatusCode = StatusCode.SUCCESS): Unit = {
    histogram.update(cost, count, status)
  }

  /**
   *
   * @return 从histogram数据提取出StatData
   */
  def export(): StatData = {
    val statData = asStatData(histogram)
    statData
  }

  /**
   *
   * @param histogram 直方图
   * @return 直方图对应的统计数据
   */
  private def asStatData(histogram: SimpleCountHistogram) = {
    val statData = new StatData()

    // 填充维度信息 */
    statData.setCount(histogram.getCount)
    statData.setSuccessCount(histogram.getSuccessCount)
    statData.setExceptionCount(histogram.getExceptionCount)
    statData.setTimeoutCount(histogram.getTimeoutCount)
    statData.setDropCount(histogram.getDropCount)

    statData.setHTTP2XXCount(histogram.getHTTP2XXCount)
    statData.setHTTP3XXCount(histogram.getHTTP3XXCount)
    statData.setHTTP4XXCount(histogram.getHTTP4XXCount)
    statData.setHTTP5XXCount(histogram.getHTTP5XXCount)

    statData.setAppkey(key.appKey)
    statData.setTs(key.ts)
    statData.setEnv(key.env)
    statData.setSource(key.source)
    statData.setRange(key.range)
    statData.setGroup(key.group)
    statData.setPerfProtocolType(key.perfProtocolType)
    //    statData.setTags(key.tags)

    val nowMs = System.currentTimeMillis

    // 填充追踪信息
    statData.setUpdateTime(nowMs)
    statData.setUpdateFrom(ProcessInfoUtil.getLocalIpV4FromLocalCache)


    // 计算qps要考虑到超出时间范围
    var timeRange: Int = (nowMs / 1000L).toInt - key.ts
    timeRange = if (timeRange > key.range.getTimeRange) {
      key.range.getTimeRange
    } else if (timeRange == 0) {
      //  避免分母为0,出现 商 为无穷数
      1
    } else {
      timeRange
    }

    val df: DecimalFormat = new DecimalFormat("#.###")
    statData.setQps(df.format(statData.getCount.toDouble / timeRange).toDouble)

    // 计算cost
    val snap = histogram.getSnapshot
    try {
      statData.setCost50(df.format(snap.getMedian).toDouble)
      statData.setCost75(df.format(snap.get75thPercentile).toDouble)
      statData.setCost90(df.format(snap.getValue(0.90)).toDouble)
      statData.setCost95(df.format(snap.get95thPercentile).toDouble)
      statData.setCost98(df.format(snap.get98thPercentile).toDouble)
      statData.setCost99(df.format(snap.get99thPercentile).toDouble)
      statData.setCost999(df.format(snap.get999thPercentile).toDouble)
      statData.setCostMin(df.format(snap.getMin).toDouble)
      statData.setCostMean(df.format(snap.getMean).toDouble)
      statData.setCostMax(df.format(snap.getMax).toDouble)
      statData.getRange match {
        case StatRange.Minute => //  不需要导出详细cost->count信息
        case _ =>
          val costDataList = snap.getCostDataList
          if (!CollectionUtils.isEmpty(costDataList)) {
            statData.setCostData(new PerfCostDataList(costDataList))
          }
      }

    } catch {
      case e: Exception => logger.error(s"asStatData fail:snap:$snap", e)
    }
    statData
  }

  /**
   *
   * @return histogram序列化字节流
   */
  def serialize(): ByteArrayOutputStream = {
    val stream = new ByteArrayOutputStream()
    histogram.dump(stream)
    stream
  }

  def size(): Int = {
    histogram.size()
  }

}

object Instance {
  private val logger = LoggerFactory.getLogger(this.getClass)
}
