package com.sankuai.octo.log.utils

import java.util.concurrent.TimeUnit

import akka.util.Timeout
import com.sankuai.octo.statistic.util.ExecutionContextFactory

/**
  * Created by wujinwu on 16/4/22.
  */
object ReconnectUtil {

  implicit val ec = ExecutionContextFactory.build(Runtime.getRuntime.availableProcessors())

  implicit val timeout = Timeout(5, TimeUnit.SECONDS)

}
