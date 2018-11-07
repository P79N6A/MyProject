package com.sankuai.octo.statistic.model

/**
  * Created by wujinwu on 15/11/4.
  * 用于在query与stat模块之间进行交互的数据结构
  */
case class QpsKey(provider: String, spanName: String, consumer: String)

case class QpsData(unixTime: Int, count: Long)
