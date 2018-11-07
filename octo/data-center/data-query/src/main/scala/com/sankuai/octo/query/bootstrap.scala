package com.sankuai.octo.query

import com.meituan.mtrace.Tracer
import com.sankuai.octo.statistic.util.{TairParam, config, tair}

class bootstrap {

  def init() = {
    Tracer.setThreshold("com.sankuai.inf.logCollector", 20)
    Tracer.setThreshold("com.sankuai.inf.data.statistic", 20)
    Tracer.setThreshold("com.sankuai.fe.mta.parser", 50)

    //  初始化MCC
    val localAppKey = "com.sankuai.inf.data.query"
    config.init(localAppKey, "v2", "defaultClient", "com.sankuai")
    //  初始化tair
    tair.init(TairParam.master(), TairParam.slave(), TairParam.group(),
      localAppKey, TairParam.remoteAppKey(), TairParam.area())
  }
}
