package com.sankuai.octo.statistic.model

import com.meituan.mtrace.thrift.model.StatusCode

/**
  * Created by wujinwu on 16/5/6.
  */
case class MetricGroupKey(ts: Long, cost: Int, status: StatusCode)
