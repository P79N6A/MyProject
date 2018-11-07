package com.sankuai.octo.spark.domain

/**
  * Created by wujinwu on 16/3/13.
  */
case class HiveModuleData(rowkey: Array[Byte], count: Long, qps: Double, tp50: Double,
                          tp90: Double, tp95: Double, tp99: Double, cost_max: Double, cost_data: Array[Byte])
