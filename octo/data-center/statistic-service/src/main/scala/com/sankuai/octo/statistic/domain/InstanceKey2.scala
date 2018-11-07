package com.sankuai.octo.statistic.domain

import com.sankuai.octo.statistic.model.{PerfProtocolType, StatEnv, StatSource}

/**
  * Created by wujinwu on 16/5/24.
  */
case class InstanceKey2(appKey: String, env: StatEnv, source: StatSource,
                        perfProtocolType: PerfProtocolType)