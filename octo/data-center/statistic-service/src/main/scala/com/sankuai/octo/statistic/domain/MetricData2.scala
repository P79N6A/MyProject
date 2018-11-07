package com.sankuai.octo.statistic.domain

/**
  *
  * @param timeStatus  时间状态
  * @param costToCount cost 到count的映射map
  */
case class MetricData2(timeStatus: TimeStatus, costToCount: Map[Int, Int])