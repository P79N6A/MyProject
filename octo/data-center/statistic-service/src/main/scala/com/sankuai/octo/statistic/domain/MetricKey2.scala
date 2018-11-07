package com.sankuai.octo.statistic.domain

import com.sankuai.octo.statistic.model.{PerfProtocolType, StatSource}

/**
  * Created by wujinwu on 16/6/25.
  */
case class MetricKey2(appkey: String, spanname: String, localHost: String, remoteAppKey: String,
                 remoteHost: String, source: StatSource, perfProtocolType: PerfProtocolType, infraName: String)
