package com.sankuai.octo.spark

import com.sankuai.octo.spark.domain.ModuleInvokeKey
import com.sankuai.octo.statistic.domain.{Instance, InstanceKey}
import com.sankuai.octo.statistic.metrics.SimpleCountHistogram
import com.sankuai.octo.statistic.model._


/**
  * Created by wujinwu on 16/2/2.
  */
object Calculator {

  def calculate(moduleInvokeKey: ModuleInvokeKey, group: StatGroup, tags: Map[String, String], costToCount: Map[Int, Long])
               (range: StatRange) = {
    val localAppKey = moduleInvokeKey.localAppKey
    val env = StatEnv.getInstance(moduleInvokeKey.envStr)
    val source = moduleInvokeKey._type match {
      case 0 => StatSource.Client
      case _ => StatSource.Server
    }
    val key = InstanceKey(localAppKey, moduleInvokeKey.ts, env, source, range, group, PerfProtocolType.THRIFT, tags)
    val instance = updateInstance(key, costToCount)
    instance.export()
  }

  def calculate(moduleInvokeKey: ModuleInvokeKey, groups: Seq[StatGroup], tags: Map[String, String], costToCount: Map[Int, Long])
               (range: StatRange) = {
    val localAppKey = moduleInvokeKey.localAppKey
    val env = StatEnv.getInstance(moduleInvokeKey.envStr)
    val source = moduleInvokeKey._type match {
      case 0 => StatSource.Client
      case _ => StatSource.Server
    }
    groups.map { group =>
      val key = InstanceKey(localAppKey, moduleInvokeKey.ts, env, source, range, group, PerfProtocolType.THRIFT, tags)
      val instance = updateInstance(key, costToCount)
      instance.export()
    }

  }

  /**
    * 按tag维度拆分后，基于MetricManager的接口实现
    *
    * @param key         需要更新的InstanceKey
    * @param costToCount cost -> count
    */
  private def updateInstance(key: InstanceKey, costToCount: Map[Int, Long]) = {
    val instance = getInstance(key)
    costToCount.foreach { case (cost, count) =>
      instance.update(cost, count)
    }
    instance
  }

  /**
    *
    * @param  key instanceKey
    * @return 直接创建Instance并返回,不需要缓存,离线运算中Instance并无共享
    */
  def getInstance(key: InstanceKey): Instance = {
    new Instance(key, new SimpleCountHistogram())
  }

}
