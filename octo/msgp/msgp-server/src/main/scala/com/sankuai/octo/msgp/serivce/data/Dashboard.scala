package com.sankuai.octo.msgp.serivce.data

import java.util.concurrent.TimeUnit

import com.sankuai.msgp.common.model.Env
import com.sankuai.msgp.common.service.org.OpsService
import com.sankuai.msgp.common.utils.helper.JsonHelper
import com.sankuai.octo.msgp.dao.service.ServiceProviderDAO
import com.sankuai.octo.msgp.serivce.falcon.AlarmQuery
import com.sankuai.octo.msgp.serivce.monitor.MonitorEvent
import com.sankuai.octo.msgp.serivce.other.LogServiceClient
import org.joda.time.DateTime

import scala.collection.parallel.ForkJoinTaskSupport
import scala.concurrent.duration.Duration

object Dashboard {
  private implicit val timeout = Duration.create(10000L, TimeUnit.MILLISECONDS)
  private val logService = LogServiceClient.getInstance

  private val LOAD = "load.1minPerCPU"
  private val FULL_GC = "jvm.fullgc.count"
  private val GC = "jvm.gc.count"

  private val formatStr = "%.3f"

  private val threadPool = new ForkJoinTaskSupport(new scala.concurrent.forkjoin.ForkJoinPool(6))

  case class KeyMetric(var load: List[DataQuery.FalconDataResponse], var fullGc: List[DataQuery.FalconDataResponse],
                       var gc: List[DataQuery.FalconDataResponse])

  case class AlarmRet(appkey: String, nodeSearch: String, data: Option[AlarmQuery.AlarmMSG], endTime: Int)

  def getAllAlarm(appkey: String, endTime: Int) = {
    val serverTreeOpt = OpsService.getOpsAppkey(appkey)
    if (serverTreeOpt.code == 200 && serverTreeOpt.data.nonEmpty) {
      val serverTree = serverTreeOpt.data.get
      val nodeSearch = s"corp=meituan&owt=${serverTree.owt}&pdl=${serverTree.pdl}&srv=${serverTree.srv}"

      val retOpt = AlarmQuery.allAlarm(nodeSearch, endTime)
      if (retOpt.nonEmpty) {
        JsonHelper.dataJson(AlarmRet(appkey, nodeSearch, retOpt, endTime))
      } else {
        JsonHelper.errorJson("can't get alarm message")
      }
    } else {
      JsonHelper.errorJson("can't get alarm message")
    }
  }


  def queryAlarmCount(nodeSearch: String, duration: Int) = {
    AlarmQuery.querySrvAlarm(nodeSearch, duration)
  }

  def getKeyMetric(appkey: String) = {
    val providersPar = ServiceProviderDAO.providerList(appkey).par
    providersPar.tasksupport = threadPool
    val hosts = providersPar.map(OpsService.ipToHost).toList
    val ret = KeyMetric(List(), List(), List())
    if(hosts.nonEmpty){
      val dataOpt = DataQuery.falconQuery(hosts, List(LOAD, FULL_GC, GC))
      //找出指标排名前五
      dataOpt match {
        case Some(value) =>
          value.groupBy(_.counter).foreach { x =>
            x._1 match {
              case LOAD =>
                ret.load = x._2.sortBy(-_.value.value).slice(0, 5)
              case FULL_GC =>
                ret.fullGc = x._2.sortBy(-_.value.value).slice(0, 5)
              case GC =>
                ret.gc = x._2.sortBy(-_.value.value).slice(0, 5)
            }
          }
        case None =>
      }
    }
    ret
  }

  def getPerfMetric(appkey: String) = {
    val now = new DateTime().getMillis
    val time = now - (120 * 1000)

    val (qps, count, successCount, exceptionCount, timeoutCount, dropCount, tp50, tp90, tp99, qpsPerHost) = perf(appkey, time)

    val tmp = maxQpsOfHost(appkey, time)
    val maxQps = if (tmp.isEmpty) {
      (qpsPerHost, "unknown")
    } else {
      (tmp.get.qps.applyOrElse(0, DataQuery.POINT).y.getOrElse(0.0), OpsService.ipToHost(tmp.get.tags.localhost.getOrElse("")))
    }

    val errorCount = logService.getErrorCount(appkey, new java.util.Date(now - 5 * 60 * 1000), new java.util.Date(now))
    val eventCount = MonitorEvent.getEventCount(appkey, "", now - 5 * 60 * 1000, now)

    Map("qps" -> qps,
      "tp90" -> tp90,
      "qpsPerHost" -> qpsPerHost,
      "maxQps" -> maxQps._1,
      "time" -> time,
      "errorCount" -> errorCount,
      "eventCount" -> eventCount,
      "maxQpsHost" -> maxQps._2
    )
  }

  // 获取性能数据
  def perf(appkey: String, time: Long) = {
    val datas = DaPan.getPerf(appkey, time)
    val (qps, count, successCount, exceptionCount, timeoutCount, dropCount, tp50, tp90, tp99) = if (datas.isEmpty) {
      (0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0)
    } else {
      val data = datas(0)
      (DataQuery.getValue(data.qps), DataQuery.getValue(data.count), DataQuery.getValue(data.successCount), DataQuery.getValue(data.exceptionCount), DataQuery.getValue(data.timeoutCount),
        DataQuery.getValue(data.dropCount), DataQuery.getValue(data.tp50), DataQuery.getValue(data.tp90), DataQuery.getValue(data.tp99))
    }
    val hostCount = DaPan.getHostCount(appkey)
    val qpsPerHost = if (hostCount == 0) {
      qps
    } else {
      qps / hostCount
    }
    (formatStr.format(qps), count, successCount, exceptionCount, timeoutCount, dropCount, tp50, tp90, tp99, formatStr.format(qpsPerHost).toDouble)
  }

  // 获取单机最大qps
  def maxQpsOfHost(appkey: String, time: Long) = {
    val start = (time / 1000).toInt
    val recordsOpt = DataQuery.getDataRecord(appkey, start, start, null, "server", null, Env.prod.toString, "minute", "spanLocalhost", "all", "*", null, null, "hbase")
    val records = recordsOpt.getOrElse(List())

    if (records.nonEmpty) {
      Some(records.filter(_.tags.localhost.getOrElse("all") != "all").maxBy(_.qps.applyOrElse(0, DataQuery.POINT).y.getOrElse(0.0)))
    } else {
      None
    }
  }


  case class FalconMetric(name: String, key: String)

  case class AppScreen(name: String, src: String)

  def main(args: Array[String]) {
    println(perf("com.sankuai.inf.msgp", 1498718469000L))
  }
}
