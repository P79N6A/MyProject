package com.sankuai.octo.export

import com.sankuai.meituan.config.listener.IConfigChangeListener
import com.sankuai.octo.statistic.model.StatEnv
import com.sankuai.octo.statistic.util.config
import org.slf4j.LoggerFactory

object StatConstants {
  private val logger = LoggerFactory.getLogger(StatConstants.getClass)

  // "prod" or "stage" or "test"
  val ENVIRONMENT: String = {
    val value = config.get("environment", "prod")
    logger.info("environment:{}", value)
    value
  }

  val env: StatEnv = {
    ENVIRONMENT match {
      case "prod" => StatEnv.Prod
      case "stage" => StatEnv.Stage
      case "test" => StatEnv.Test
      case _ => StatEnv.Prod
    }
  }

  //  每一个待发送的数据列表长度
  val groupLength = 1500
}
