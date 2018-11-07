/*
package com.sankuai.octo.statistic.metric

import java.util.concurrent.{ConcurrentHashMap, Executors, TimeUnit}

import com.sankuai.octo.statistic.domain.{Instance, InstanceKey}
import com.sankuai.octo.statistic.exporter.{AbstractExporter, DefaultExporterProxy}
import com.sankuai.octo.statistic.metrics.SimpleCountHistogram
import com.sankuai.octo.statistic.model.StatRange
import com.sankuai.octo.statistic.util.StatThreadFactory
import org.slf4j.LoggerFactory

/**
  * 1:负责创建指定的Instance
  * 2:定时清理过期的Instance
  * 3:提供数据导出
  */
object MetricManager {
  private val logger = LoggerFactory.getLogger(MetricManager.getClass)

  val hourInstanceMap = new ConcurrentHashMap[InstanceKey, Instance]()

  val minuteInstanceMap = new ConcurrentHashMap[InstanceKey, Instance]()

  private var exporter: AbstractExporter = DefaultExporterProxy

  private val minuteScheduler = Executors.newSingleThreadScheduledExecutor(StatThreadFactory.threadFactory("MetricManager-minute"))

  private val hourScheduler = Executors.newSingleThreadScheduledExecutor(StatThreadFactory.threadFactory("MetricManager-hour"))

  /**
    *
    * @param key 实例key
    * @return 对应统计实例
    *         没有就创建
    */
  //TODO 天粒度的数据 需要通过反序列化信息还原
  def getInstance(key: InstanceKey): Instance = {
    key.range match {
      case StatRange.Day => throw new RuntimeException("impossible")
      case range =>
        //  实时计算小时与分钟
        getInstanceByRange(range, key)
    }

  }


  private def getInstanceByRange(range: StatRange, key: InstanceKey) = {
    val map = range match {
      case StatRange.Day => throw new RuntimeException("impossible")
      case StatRange.Hour => hourInstanceMap
      case StatRange.Minute => minuteInstanceMap
    }
    val instance = map.get(key)
    if (instance == null) {
      val tmp = new Instance(key, new SimpleCountHistogram())
      val ret = map.putIfAbsent(key, tmp)
      if (ret == null) {
        //  没有并发冲突
        tmp
      } else {
        //  并发冲突,返回之前的
        ret
      }
    } else {
      instance
    }
  }

  /**
    * 维护Gram及计算好的数据，定期通过初始化好的Exporter导出
    */
  def exportMinute(): Unit = {
    val nowTime = (System.currentTimeMillis() / 1000).toInt
    val itr = minuteInstanceMap.entrySet().iterator()
    while (itr.hasNext) {
      try {
        val entry = itr.next()
        val instanceKey = entry.getKey
        val instance = entry.getValue
        val expiredBool = expired(instanceKey, nowTime)
        if (expiredBool) {
          itr.remove()
        }
        val statData = instance.export()
        exporter.export(statData)
      } catch {
        case e: Exception => logger.error("export Fail", e)
      }
    }
  }

  /**
    * 维护Gram及计算好的数据，定期通过初始化好的Exporter导出
    */
  def exportHour(): Unit = {
    val nowTime = (System.currentTimeMillis() / 1000).toInt
    val itr = hourInstanceMap.entrySet().iterator()
    while (itr.hasNext) {
      try {
        val entry = itr.next()
        val instanceKey = entry.getKey
        val instance = entry.getValue
        val expiredBool = expired(instanceKey, nowTime)
        if (expiredBool) {
          itr.remove()
        }
        val statData = instance.export()
        exporter.export(statData)
      } catch {
        case e: Exception => logger.error("export Fail", e)
      }
    }
  }


  /**
    *
    * @param exporter 数据导出者
    *                 可通过接口设置对应的数据Exporter列表，定期将计算结果输出 List<StatData>  -> List<Exporter>
    */
  def setExporter(exporter: AbstractExporter) {
    this.exporter = exporter
  }

  //定时任务维护分钟级 instanceMap
  private val minuteTask = new Runnable {
    override def run(): Unit = {
      exportMinute()
    }
  }

  //定时任务维护小时级 instanceMap
  private val hourTask = new Runnable {
    override def run(): Unit = {
      exportHour()
    }
  }


  /**
    *
    * @param key     要导出的key
    * @param nowTime 现在的时间,单位 秒
    * @return
    */
  private def expired(key: InstanceKey, nowTime: Int): Boolean = (nowTime - key.ts) > key.range.getLifetime

  private def exportData(instance: Instance): Unit = {
    val statData = instance.export()
    exporter.export(statData)
  }

  {
    minuteScheduler.scheduleAtFixedRate(minuteTask, 1, 1, TimeUnit.MINUTES)

    hourScheduler.scheduleAtFixedRate(hourTask, 5, 20, TimeUnit.MINUTES)

    // 在jvm退出时优雅关闭
    Runtime.getRuntime.addShutdownHook(new Thread() {
      override def run(): Unit = {
        minuteScheduler.submit(minuteTask)
        minuteScheduler.shutdown()
        minuteScheduler.awaitTermination(20, TimeUnit.SECONDS)

        hourScheduler.submit(hourTask)
        hourScheduler.shutdown()
        hourScheduler.awaitTermination(20, TimeUnit.SECONDS)

      }
    })
  }
}

*/
