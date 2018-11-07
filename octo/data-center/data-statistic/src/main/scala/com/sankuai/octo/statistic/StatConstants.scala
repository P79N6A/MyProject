package com.sankuai.octo.statistic

import com.sankuai.meituan.config.listener.IConfigChangeListener
import com.sankuai.octo.statistic.model.StatEnv
import com.sankuai.octo.statistic.util.config
import org.apache.commons.lang3.StringUtils
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

  val METRIC_LIMIT = {
    if (MafkaConfig.isTaskHost) {
      "mafka_metric_limit"
    } else {
      "metric_limit"
    }
  }
  //  每一个待发送的数据列表长度
  val groupLength = 1500

  var metricLimit: Int = {
    try {
      val value = config.get(METRIC_LIMIT, "20000")
      logger.info(METRIC_LIMIT + ":{}", value)
      config.addListener(METRIC_LIMIT, new IConfigChangeListener() {
        def changed(key: String, oldValue: String, newValue: String) = {
          logger.info(METRIC_LIMIT + ":{}", newValue)
          metricLimit = newValue.toInt
        }
      })
      value.toInt
    }
    catch {
      case e: Exception =>
        logger.error(s"初始化失败$METRIC_LIMIT", e)
        2000000
    }
  }
  var debug_limit: Boolean = {
    val value = config.get("debug_limit", "true")
    logger.info("debug_limit:{}", value)
    config.addListener("debug_limit", new IConfigChangeListener() {
      def changed(key: String, oldValue: String, newValue: String) = {
        logger.info("debug_limit:{}", newValue)
        debug_limit = newValue.toBoolean
      }
    })
    value.toBoolean
  }

  var alias_appkey: Boolean = {
    val value = config.get("alias_appkey", "true")
    logger.info("alias_appkey:{}", value)
    config.addListener("alias_appkey", new IConfigChangeListener() {
      def changed(key: String, oldValue: String, newValue: String) = {
        logger.info("alias_appkey:{}", newValue)
        alias_appkey = newValue.toBoolean
      }
    })
    value.toBoolean
  }

  var mafka_export: Map[String, Boolean] = {
    val value = config.get("mafka.export", "waimai_api:false")
    logger.info("mafka.export:{}", value)
    config.addListener("mafka.export", new IConfigChangeListener() {
      def changed(key: String, oldValue: String, newValue: String) = {
        logger.info("mafka.export:{}", newValue)
        mafka_export = initMafkaExportConfig(newValue)
      }
    })
    initMafkaExportConfig(value)

  }

  private def initMafkaExportConfig(value: String) = {
    if (StringUtils.isNotBlank(value)) {
      try {
        val consumerConfigs = value.split(";")
        consumerConfigs.map {
          config =>
            val configArray = config.split(":")
            (configArray.apply(0), configArray.apply(1).toBoolean)
        }.toMap
      }
      catch {
        case e: Exception =>
          logger.error(s"initMafkaExportConfig 失败 ${value}", e)
          Map[String,Boolean]()
      }
    } else {
      Map[String,Boolean]()
    }
  }

  var mafakMessageLimit: Int = {
    val value = config.get("mafka.message.limit", "10000000")
    config.addListener("mafka.message.limit", new IConfigChangeListener() {
      def changed(key: String, oldValue: String, newValue: String) = {
        logger.info("mafka.message.limit:{}", newValue)
        mafakMessageLimit = newValue.toInt
      }
    })
    value.toInt
  }

  var mafkaMessageSleep: Int = {
    val value = config.get("mafka.message.sleep", "1")
    config.addListener("mafka.message.sleep", new IConfigChangeListener() {
      def changed(key: String, oldValue: String, newValue: String) = {
        logger.info("mafka.message.sleep:{}", newValue)
        mafkaMessageSleep = newValue.toInt
      }
    })
    value.toInt
  }

  def isExportHost(appkey: String): Boolean = {
    if (!mafka_export.contains(appkey)) {
      true
    } else {
      val m_export = mafka_export.getOrElse(appkey, false)
      if (m_export ) {
        if(MafkaConfig.isTaskHost){
          true
        }else{
          false
        }
      }else{
        false
      }
    }
  }

}
