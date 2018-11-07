package com.sankuai.octo.msgp.serivce.service

import java.util.concurrent.{Executors, TimeUnit}

import com.sankuai.msgp.common.model.Env
import com.sankuai.msgp.common.service.org.OpsService
import com.sankuai.msgp.common.utils.StringUtil
import com.sankuai.msgp.common.utils.client.TairClient
import com.sankuai.msgp.common.utils.helper.CommonHelper
import com.sankuai.octo.msgp.model.Perf.{Consumer, ConsumerOutline}
import com.sankuai.octo.msgp.serivce.data.DataQuery
import org.joda.time.DateTime
import org.slf4j.LoggerFactory
import play.api.libs.json.Json

import scala.collection.JavaConverters._
import scala.collection.parallel.ForkJoinTaskSupport
import scala.concurrent.ExecutionContext
import scala.concurrent.duration.Duration

object ServiceConsumer {
  private val logger = LoggerFactory.getLogger(this.getClass)
  private val ALL = "all"
  private implicit val timeout = Duration.create(20L, TimeUnit.SECONDS)
  private val role = "server"
  private val group = "RemoteAppRemoteHost"
  private val spanname = ALL
  private val localhost = ALL
  private val protocol = "thrift"
  private val dataSource = "hbase"
  private val unknown_service_list_merged_key = "unknown_service_list_merged"
  private val unknown_service_list_key = "unknown_service_list"

  implicit val ec = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(8))

  private val taskSupport = new ForkJoinTaskSupport(new scala.concurrent.forkjoin.ForkJoinPool(Runtime.getRuntime.availableProcessors*2))

  def consumerList(appkey: String, period: String, env: String, remoteApp: String, remoteHost: String): List[Consumer] = {
    val param = transRange(period)
    val start = param._1
    val end = param._2
    val unit = param._3
    val data = DataQuery.getDataRecord(appkey, start, end, protocol, role, null, env,
      unit, group, spanname, localhost, remoteApp, remoteHost, dataSource)
    data match {
      case Some(value) =>
        try {
          val valuePar = value.par
          valuePar.tasksupport =taskSupport
          valuePar.map { x =>
            val ip = x.tags.remoteHost.get
            val remoteApp = x.tags.remoteApp.get
            val count = x.count.map(_.y.getOrElse(0.0)).sum
            val hostname = OpsService.ipToHost(ip)
            val tag = OpsService.hostnameTagCache.get(hostname)
            Consumer(ip, hostname, tag, remoteApp, param._1.toString, count)
          }.toList
        }
        catch {
          case e: Exception =>
            logger.error("consumer consumerList failed", e)
            List()
        }
      case None => List()
    }
  }

  def consumerOutline(appkey: String, period: String, env: String) = {
    val appListResult = consumerList(appkey, period, env, "*", "all").groupBy(_.appkey)
    val appCountMap = appListResult.map {
      app =>
        app._1 -> app._2.map(_.count).sum
    }.toList.sortWith(_._2 > _._2)

    val appList = appCountMap.map(_._1)
    val appCount = appCountMap.map(_._2)

    val hostListResult = consumerList(appkey, period, env, "all", "*").groupBy(_.host)
    val hostCountMap = hostListResult.map {
      host =>
        host._1 -> host._2.map(_.count).sum
    }.toList.sortWith(_._2 > _._2)

    val hostList = hostCountMap.map(_._1)
    val hostCount = hostCountMap.map(_._2)
    ConsumerOutline(appList, appCount, hostList, hostCount)
  }

  def transRange(period: String) = {
    period match {
      case "1m" =>
        val startTime = (new DateTime().minusMinutes(1).getMillis / 1000).toInt
        (startTime, startTime, "minute")
      case "1h" =>
        val startTime = (new DateTime().minusHours(1).getMillis / 1000).toInt
        (startTime, startTime, "minute")
      case "today" =>
        val now = new DateTime()
        val startTime = (now.withTimeAtStartOfDay().getMillis / 1000).toInt
        val endTime = (now.withTimeAtStartOfDay().plusDays(1).minusMinutes(1).getMillis / 1000).toInt
        (startTime, endTime, "hour")
      case "yesterday" =>
        val now = new DateTime()
        val startTime = (now.minusDays(1).withTimeAtStartOfDay().getMillis / 1000).toInt
        val endTime = (now.withTimeAtStartOfDay().minusMinutes(1).getMillis / 1000).toInt
        (startTime, endTime, "hour")
    }
  }


  val expiringTime: Int = 3600000

  // 1 hour

  case class unknownServiceItem(host: String, ip: String, protocol: String, env: String, tag: String, rd: List[String])

  implicit val unknownServiceItemReads = Json.reads[unknownServiceItem]
  implicit val unknownServiceItemWrites = Json.writes[unknownServiceItem]

  case class unknownServiceWrapper(protocol: String, hosts: List[String], ips: List[String], tags: List[String], rds: List[String])

  implicit val unknownServiceWrapperReads = Json.reads[unknownServiceWrapper]
  implicit val unknownServiceWrapperWrites = Json.writes[unknownServiceWrapper]

  case class unknownServiceList(env: String, unknownServices: List[unknownServiceWrapper])

  implicit val unknownServiceListReads = Json.reads[unknownServiceList]
  implicit val unknownServiceListWrites = Json.writes[unknownServiceList]

  def getUnknownService(appkey: String, period: String, env: String, remoteHost: String) = {
    val unknownConsumers = consumerList(appkey, period, env, "unknownService", remoteHost).filter {
      x => !x.ip.equals("external") && !x.host.equals("external")
    }
    if (unknownConsumers.nonEmpty) {
      unknownConsumers.map { item =>
        val hostName = if (CommonHelper.checkIP(item.host)) {
          //若ip未转化为hostName,则手动转换
          OpsService.ipToHost(item.ip)
        } else {
          item.host
        }
        val tag = if (StringUtil.isNotBlank(item.tag)) {
          item.tag
        } else {
          OpsService.hostTag(hostName)
        }
        val rds = OpsService.getRDAdmin(tag).asScala.toList
        val protocol = AppkeyProviderService.getNodeTypeByIp(item.ip)
        unknownServiceItem(hostName, item.ip, protocol, env, tag, rds)
      }
    } else {
      List[unknownServiceItem]()
    }
  }

  def getUnknownServiceList(appkey: String, period: String, remoteHost: String, merge: Boolean) = {
    val allAppkeys = if (StringUtil.isNotBlank(appkey)) {
      List(appkey)
    } else {
      ServiceCommon.listService.map(_.appkey).distinct
    }
    val result = allAppkeys.par.flatMap { _appkey =>
      Env.values.flatMap {
        env =>
          getUnknownService(_appkey, period, env.toString, remoteHost)
      }
    }.toList.distinct
    if (merge) {
      val resultGroupByEnv = result.groupBy(_.env)
      val mergedList = resultGroupByEnv.map {
        case (_env, _list) =>
          val groupedList = _list.groupBy(_.protocol)
          val abc = groupedList.map {
            case (key, itemList) =>
              val distinctedList = itemList.map {
                x =>
                  (x.host, x.ip, x.tag)
              }.distinct
              unknownServiceWrapper(key, distinctedList.map(_._1), distinctedList.map(_._2),
                distinctedList.map(_._3), itemList.flatMap(_.rd).distinct)
          }.toList
          unknownServiceList(_env, abc)
      }.toList
      TairClient.put(unknown_service_list_merged_key, mergedList, expiringTime)
      mergedList
    } else {
      TairClient.put(unknown_service_list_key, result, expiringTime)
      result
    }
  }

  def getUnknownServiceListCache(appkey: String, period: String, remoteHost: String, merge: Boolean) = {
    val cachedData = if (merge) {
      val result = TairClient.get(unknown_service_list_merged_key)
      try {
        val data = result.flatMap {
          text =>
            Json.parse(text).validate[List[unknownServiceList]].asOpt
        }
        data
      }
      catch {
        case e: Exception =>
          logger.error(s"get unknown_service_list_merged fail", e)
          None
      }
    } else {
      val result = TairClient.get(unknown_service_list_key)
      try {
        result.flatMap {
          text =>
            Json.parse(text).validate[List[unknownServiceItem]].asOpt
        }
      }
      catch {
        case e: Exception =>
          logger.error(s"get unknown_service_list fail", e)
          None
      }
    }
    if (cachedData.isDefined) {
      cachedData.get
    } else {
      getUnknownServiceList(appkey, period, remoteHost, merge)
    }
  }
}
