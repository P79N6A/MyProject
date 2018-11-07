package com.sankuai.octo.statistic.domain

import com.meituan.mtrace.thrift.model.StatusCode

/**
  *
  * @param minuteTs 具体某一分钟起始时刻的时间戳
  * @param status   状态码
  */
case class TimeStatus(minuteTs: Int, status: StatusCode)