package com.sankuai.octo.aggregator.store

import java.lang

import com.meituan.mtrace.hbase.{CloudTableStoreService, Env}
import com.sankuai.octo.aggregator.util.MyProxy
import org.slf4j.LoggerFactory

import scala.collection.JavaConversions._

class TableStoreService(val threshold: Double) {
  private val cloudTableStoreService = new CloudTableStoreService(TableStoreService.env)

  private def init() = {
    cloudTableStoreService.setThreshold(threshold)
    cloudTableStoreService
  }

  def store(bytes: Array[Byte]): Unit = {
    cloudTableStoreService.store(bytes)
  }

  def getTraceIdsByServiceName(serviceName: String, endTs: Long, limit: Int): List[lang.Long] = {
    cloudTableStoreService.getTraceIdsByServiceName(serviceName, endTs, limit, TableStoreService.env).toList
  }

  def setDegree(threshold: Double) = {
    cloudTableStoreService.setThreshold(threshold)
  }
}

object TableStoreService {
  private val logger = LoggerFactory.getLogger(this.getClass)
  private val config = MyProxy.mcc

  private val env: Env = try {
    val value = config.get("environment", "prod")
    logger.info("environment:{}", value)
    value.toLowerCase match {
      case "prod" => Env.Prod
      case "stage" => Env.Stage
      case "test" => Env.Test
      case _ => Env.Prod
    }
  } catch {
    case e: Exception => logger.error("TableStoreService get env failed", e)
      Env.Prod
  }

  def apply(threshold: Double) = {
    new TableStoreService(threshold).init()
  }
}
