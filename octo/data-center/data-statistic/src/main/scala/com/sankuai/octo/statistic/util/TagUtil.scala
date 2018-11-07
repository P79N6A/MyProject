package com.sankuai.octo.statistic.util

import com.sankuai.octo.statistic.constant.Constants
import com.sankuai.octo.statistic.domain.StatTag
import com.sankuai.octo.statistic.model.PerfProtocolType

/**
  * Created by wujinwu on 16/6/23.
  */
object TagUtil {

  def getStatTag(spanname: String = Constants.ALL, localHost: String = Constants.ALL,
                 remoteHost: String = Constants.ALL, remoteAppKey: String = Constants.ALL)
                (implicit infraName: String = PerfProtocolType.THRIFT.toString) = {
    StatTag(spanname, localHost, remoteHost, remoteAppKey, infraName)
  }
}
