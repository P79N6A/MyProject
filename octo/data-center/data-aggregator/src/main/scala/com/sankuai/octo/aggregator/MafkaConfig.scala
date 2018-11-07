package com.sankuai.octo.aggregator

import java.util.concurrent.ConcurrentHashMap

import com.sankuai.meituan.config.listener.IConfigChangeListener
import com.sankuai.octo.aggregator.util.MyProxy
import com.sankuai.octo.queue.MetricProducer
import org.apache.commons.lang3.StringUtils
import org.slf4j.LoggerFactory

object MafkaConfig {
  private val logger = LoggerFactory.getLogger(StatConstants.getClass)

  val MAFKA_APPKEY_TOPIC: String = "mafka.appkey.topic"
  val config = MyProxy.mcc

  val mafkaService = new ConcurrentHashMap[String, MetricProducer]
  var appkeys: Set[String] = Set[String]()

  var appkeyTopic: Map[String, String] = {
    val value = config.get(MAFKA_APPKEY_TOPIC, "")
    config.addListener(MAFKA_APPKEY_TOPIC, new IConfigChangeListener() {
      @Override
      def changed(key: String, oldValue: String, newValue: String) = {
        logger.info(key + "value changed,new value" + newValue)
        appkeyTopic = initAppkeyTopic(newValue)
      }
    })
    initAppkeyTopic(value)
  }

  def initAppkeyTopic(value: String): Map[String, String] = {
    mafkaService.clear()
    if (StringUtils.isNotBlank(value)) {
      val topicConfigs = value.split(";")
      topicConfigs.map {
        topicConfig =>
          val configMap = topicConfig.split(":")
          (configMap(0), configMap(1))
      }.toMap
    } else {
      Map[String, String]()
    }
  }

  def getMafkaProducer(appkey: String): MetricProducer = {
    val topic: String = appkeyTopic.apply(appkey)
    if (mafkaService.containsKey(topic)) {
      mafkaService.get(topic)
    }
    else {
      val metricProducer: MetricProducer = new MetricProducer(topic)
      mafkaService.put(topic, metricProducer)
      metricProducer
    }
  }

  def isSentToMafka(appkey: String): Boolean = {
    appkeys.contains(appkey)
  }

  def getAppkeys: Set[String] = {
    appkeys
  }

  def getAppkeyTopic: Map[String, String] = {
    appkeyTopic
  }
}
