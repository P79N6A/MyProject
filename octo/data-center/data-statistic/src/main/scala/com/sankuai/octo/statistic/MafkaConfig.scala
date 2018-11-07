package com.sankuai.octo.statistic

import com.sankuai.inf.octo.mns.util.ProcessInfoUtil
import com.sankuai.meituan.config.listener.IConfigChangeListener
import com.sankuai.octo.statistic.util.config
import org.apache.commons.lang3.StringUtils
import org.slf4j.LoggerFactory

/**
 * 1：判定自己是否为任务机器
 */
object MafkaConfig {
  private val logger = LoggerFactory.getLogger(MafkaConfig.getClass)
  val LOCAL_HOST = ProcessInfoUtil.getLocalIpV4

  /**
    * 任务机器到消费组的映射
    */
  var hostConsumerGroup: Map[String, String] = {
    val value = config.get("mafka.host.consumerGroup", "")
    logger.info("hostConsumerGroup:{}", value)
    config.addListener("mafka.host.consumerGroup", new IConfigChangeListener() {
      def changed(key: String, oldValue: String, newValue: String) = {
        logger.info("mafka.host.consumerGroup:{}", newValue)
        hostConsumerGroup= initHostConfig(newValue)
      }
    })
    initHostConfig(value)
  }

  /**
    * 消费组到topic的映射
    */

  var consumerGroupTopic: Map[String, String] = {
    val value = config.get("mafka.consumerGroup.topic", "")
    logger.info("mafka.consumerGroup.topic:{}", value)
    config.addListener("mafka.consumerGroup.topic", new IConfigChangeListener() {
      def changed(key: String, oldValue: String, newValue: String) = {
        logger.info("mafka.consumerGroup.topic:{}", newValue)
        consumerGroupTopic= initConsumerGroupConfig(newValue)
      }
    })
    initConsumerGroupConfig(value)
  }

  private def initHostConfig(value: String) = {
    if (StringUtils.isNotBlank(value)) {
      val hostConfigs = value.split(";")
      hostConfigs.flatMap{
        config =>
          val configArray = config.split(":")
          val hosts = configArray.apply(0).split(",")
          val consumerGroup = configArray.apply(1)
          hosts.map{
            host=>
              (host, consumerGroup)
          }
      }.toMap
    } else {
      Map[String,String]()
    }
  }

  private def initConsumerGroupConfig(value: String) = {
    if (StringUtils.isNotBlank(value)) {
      val consumerConfigs = value.split(";")
      consumerConfigs.map{
        config =>
          val configArray = config.split(":")
          (configArray.apply(0), configArray.apply(1))
      }.toMap
    } else {
      Map[String,String]()
    }
  }

  /**
   * 判断机器是否是任务机器
   *
   * @return 本机是否是任务机器
   */
  def isTaskHost(): Boolean = {
    hostConsumerGroup.contains(LOCAL_HOST)
  }

  /**
    * 获取主机名
    *
    * @return
    */
  def getGroupName(): String = {
    hostConsumerGroup.getOrElse(LOCAL_HOST,"")
  }

  def getTopicName(): String = {
    val consumerGroup = hostConsumerGroup.getOrElse(LOCAL_HOST,"")
    consumerGroupTopic.getOrElse(consumerGroup,"")
  }

}
