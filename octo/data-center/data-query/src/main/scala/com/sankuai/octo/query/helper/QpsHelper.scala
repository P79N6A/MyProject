package com.sankuai.octo.query.helper

import java.util
import java.util.Collections

import com.sankuai.octo.query.domain.QpsQueryResult
import com.sankuai.octo.query.domain.QpsQueryResult.Consumer2Qps
import com.sankuai.octo.query.falconData.FalconHistoryData
import com.sankuai.octo.statistic.constant.Constants
import com.sankuai.octo.statistic.helper.{DailyMetricHelper, TagHelper}
import com.sankuai.octo.statistic.model._
import org.slf4j.LoggerFactory
import org.springframework.util.StringUtils

import scala.collection.JavaConversions._

object QpsHelper {

  private val logger = LoggerFactory.getLogger(this.getClass)

  /**
   *
   * @param provider 服务提供者的appKey
   * @param spanName 方法名
   * @param startSecond 查询起始时间,至少是当前时间的5min前
   * @param endSecond 查询起始时间,至少是当前时间的5min前
   * @param environment 环境标识
   * @return 正常与异常drop流量的聚合
   */
  def queryClientQps(provider: String, spanName: String, startSecond: Int, endSecond: Int,
                     environment: String) = {
    queryQps(environment, provider, spanName, startSecond, endSecond, StatSource.Server)
  }

  def queryClientDropQps(provider: String, spanName: String, startSecond: Int, endSecond: Int,
                         environment: String): QpsQueryResult = {
    queryQps(environment, provider, spanName, startSecond, endSecond, StatSource.ServerDrop)
  }

  private def queryQps(env: String, provider: String, spanName: String, start: Int, end: Int, statSource: StatSource): QpsQueryResult = {
    logger.info(s"provider:$provider,spanName:$spanName,start:$start,end:$end,env:$env,statSource:$statSource")
    if (!checkPrecondition(provider, spanName, start, end, env)) {
      return constructEmptyResult(provider, spanName)
    }
    try {
      /** 走falcon查询,source为Server时查询的qps信息是全部的流量(即包含正常的跟Server端drop的)
        * source 为ServerDrop时查询的是Server端drop的流量 */
      val statEnv = StatEnv.getInstance(env)
      val recordList = FalconHistoryData.historyDataFromFalcon(provider, start, end, statEnv,
        statSource, StatGroup.SpanRemoteApp, spanName, remoteAppkey = "*")
      val list = recordList.filter(_.tags.remoteApp.get != Constants.ALL).filter(_.qps.exists(_.y.isDefined))
      if (list.isEmpty) {
        //  这种情况下,对应appKey的数据不存在,直接返回
        constructEmptyResult(provider, spanName)
      } else {
        val normalQpsMap = list.map(item => {
          val alist = item.qps.filter(_.y.isDefined)
          val avg = alist.map(_.y.get).sum / alist.size
          (item.tags.remoteApp.get, avg)
        })
        val consumer2QpsList = normalQpsMap.map({ case (consumer, qpsAvg) => new Consumer2Qps(consumer, qpsAvg) })

        new QpsQueryResult(provider, spanName, consumer2QpsList.toList.sortBy(-_.getQpsAvg))
      }
    } catch {
      case e: Exception =>
        logger.error(s"queryQps Fail $provider $spanName $start $end $env", e)
        constructEmptyResult(provider, spanName)
    }
  }

  private def checkPrecondition(providerAppKey: String, spanName: String, startSecond: Int,
                                endSecond: Int, environment: String): Boolean = {
    logger.info(s"$providerAppKey,$spanName,$startSecond,$endSecond,$environment")
    if (!StatEnv.isValid(environment)) {
      logger.error("checkPrecondition Fail,environment illegal,environment:{}", environment)
      return false
    } else if (!StringUtils.hasText(providerAppKey) || !StringUtils.hasText(spanName)) {
      logger.error(s"checkPrecondition Fail,providerAppKey or spanName is empty! provider:$providerAppKey,spanName:$spanName")
      return false
    }
    if (startSecond > endSecond) {
      logger.error(s"checkPrecondition Fail,time range illegal!,start:$startSecond,end:$endSecond")
      false
    } else {
      true
    }
  }

  private def constructEmptyResult(providerAppKey: String, spanName: String) = {
    new QpsQueryResult(providerAppKey, spanName, Collections.emptyList())
  }


  def queryProviderSpanToConsumer(providerAppKey: String, spanName: String, environment: String): util.List[String] = {
    if (!StringUtils.hasText(providerAppKey) || !StringUtils.hasText(spanName) || !StatEnv.isValid(environment)) {
      Collections.emptyList()
    } else {
      try {
        /** 查询provider所有的consumer,即将spanName作为"all"处理 */
        //  "all"统一走分钟级别的Tag查询
        val statEnv = StatEnv.getInstance(environment)
        val option = TagHelper.getDailyTag(providerAppKey, DailyMetricHelper.dayStart(), statEnv, StatSource.Server)
        option match {
          case Some(tag) =>
            if (tag.remoteAppKeys.isEmpty) {
              Collections.emptyList()
            } else {
              tag.remoteAppKeys.filter(_ != Constants.ALL).toList.sorted
            }
          case None => Collections.emptyList()
        }
      } catch {
        case e: Exception =>
          logger.error(s"queryProviderSpan Fail,$providerAppKey,$spanName,$environment", e)
          new util.ArrayList[String]()
      }
    }
  }

  /**
    *
    * @param providerAppKey 服务提供者
    * @param environment 环境标识
    * @return provider对应的spanname集合,包含"all"
    */
  def queryProvider(providerAppKey: String, environment: String): util.List[String] = {
    if (!StringUtils.hasText(providerAppKey) || !StringUtils.hasText(environment) || !StatEnv.isValid(environment)) {
      List(Constants.ALL)
    } else {
      try {
        //  统一走分钟级别的Tag查询
        val statEnv = StatEnv.getInstance(environment)
        val option = TagHelper.getDailyTag(providerAppKey, DailyMetricHelper.dayStart(), statEnv, StatSource.Server)
        option match {
          case Some(tag) =>
            if (tag.spannames.isEmpty) {
              List(Constants.ALL)
            } else {
              Constants.ALL :: tag.spannames.filter(_ != Constants.ALL).toList.sorted
            }
          case None => List(Constants.ALL)
        }
      } catch {
        case e: Exception =>
          logger.error(s"queryProviderSpan Fail,$providerAppKey,$environment", e)
          List(Constants.ALL)
      }
    }
  }

}