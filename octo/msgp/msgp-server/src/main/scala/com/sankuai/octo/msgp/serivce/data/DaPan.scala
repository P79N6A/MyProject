package com.sankuai.octo.msgp.serivce.data

import java.util.Date
import java.util.concurrent.TimeUnit

import com.sankuai.msgp.common.model.Page
import com.sankuai.octo.msgp.serivce.other.LogServiceClient
import com.sankuai.octo.msgp.serivce.service.{AppkeyProviderService, ServiceFilter}

import scala.collection.JavaConverters._
import scala.collection.parallel.ForkJoinTaskSupport
import scala.concurrent.duration.Duration

object DaPan {
  private val logService = LogServiceClient.getInstance

  private val threadPool = new ForkJoinTaskSupport(new scala.concurrent.forkjoin.ForkJoinPool(8))
  private implicit val timeout = Duration.create(10000L, TimeUnit.MILLISECONDS)

  case class CoreData(name: String,
                      desc: String,
                      currentTime: Long,
                      errorPercentage: Double,
                      errorCount: Int,
                      requestCount: Double,
                      rollingCountTimeout: Double,
                      rollingCountSemaphoreRejected: Double,
                      reportingHosts: Int,
                      latencyExecute: Map[String, Double],
                      latencyTotal: Map[String, Double])

  private def hystrixMap(coreData: CoreData) = {
    Map[String, Any](
      "name" -> coreData.name,
      "desc" -> coreData.desc,
      "currentTime" -> coreData.currentTime,
      "errorPercentage" -> coreData.errorPercentage,
      "errorCount" -> coreData.errorCount,
      "requestCount" -> coreData.requestCount,
      "rollingCountTimeout" -> coreData.rollingCountTimeout,
      "rollingCountSemaphoreRejected" -> coreData.rollingCountSemaphoreRejected,
      "reportingHosts" -> coreData.reportingHosts,
      "latencyExecute" -> coreData.latencyExecute,
      "latencyTotal" -> coreData.latencyTotal,
      "rollingCountSuccess" -> coreData.requestCount,
      "type" -> "HystrixCommand",
      "group" -> "User",
      "isCircuitBreakerOpen" -> false,
      "rollingCountCollapsedRequests" -> 0,
      "rollingCountExceptionsThrown" -> 0,
      "rollingCountFailure" -> 0,
      "rollingCountFallbackFailure" -> 0,
      "rollingCountFallbackRejection" -> 0,
      "rollingCountFallbackSuccess" -> 0,
      "rollingCountResponsesFromCache" -> 12,
      "rollingCountShortCircuited" -> 0,
      "rollingCountThreadPoolRejected" -> 0,
      "currentConcurrentExecutionCount" -> 0,
      "latencyExecute_mean" -> 9,
      "latencyTotal_mean" -> 10,
      "propertyValue_circuitBreakerRequestVolumeThreshold" -> 20,
      "propertyValue_circuitBreakerSleepWindowInMilliseconds" -> 5000,
      "propertyValue_circuitBreakerErrorThresholdPercentage" -> 50,
      "propertyValue_circuitBreakerForceOpen" -> false,
      "propertyValue_circuitBreakerForceClosed" -> false,
      "propertyValue_circuitBreakerEnabled" -> true,
      "propertyValue_executionIsolationStrategy" -> "THREAD",
      "propertyValue_executionIsolationThreadTimeoutInMilliseconds" -> 50,
      "propertyValue_executionIsolationThreadInterruptOnTimeout" -> true,
      "propertyValue_executionIsolationThreadPoolKeyOverride" -> null,
      "propertyValue_executionIsolationSemaphoreMaxConcurrentRequests" -> 10,
      "propertyValue_fallbackIsolationSemaphoreMaxConcurrentRequests" -> 10,
      "propertyValue_metricsRollingStatisticalWindowInMilliseconds" -> 60000,
      "propertyValue_requestCacheEnabled" -> true,
      "propertyValue_requestLogEnabled" -> true)
  }


  def hystrixData(owt: String, start: Long, end: Long) = {
    val appsPar = ServiceFilter.serviceByOwtPdl(owt, null, new Page(1, Integer.MAX_VALUE)).par
    appsPar.tasksupport = new ForkJoinTaskSupport(new scala.concurrent.forkjoin.ForkJoinPool(16))
    val ret = appsPar.map {
      desc =>
        val appkey = desc.appkey
        val hostCount = getHostCount(appkey)
        if(hostCount>0){
          val errorLogCount = logService.getErrorCount(appkey, new Date(start), new Date(end))
          val datas = getPerf(appkey, end)
          val (count, successCount, exceptionCount, timeoutCount, dropCount, tp50, tp90, tp99) = if (datas.isEmpty) {
            (0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0)
          } else {
            val data = datas(0)
            (DataQuery.getValue(data.count), DataQuery.getValue(data.successCount), DataQuery.getValue(data.exceptionCount), DataQuery.getValue(data.timeoutCount),
              DataQuery.getValue(data.dropCount), DataQuery.getValue(data.tp50), DataQuery.getValue(data.tp90), DataQuery.getValue(data.tp99))
          }
          val errorPercentage = if (count != 0) {
            ((errorLogCount / count) * 100).toInt
          } else {
            0
          }
          val latencyMap = Map("50" -> tp50, "90" -> tp90, "99" -> tp99)
          hystrixMap(CoreData(appkey, desc.intro, start, errorPercentage, errorLogCount, count, timeoutCount, dropCount, hostCount, latencyMap, latencyMap)).asJava
        }else{
          val latencyMap = Map("50" -> 0.0, "90" -> 0.0, "99" -> 0.0)
          hystrixMap(CoreData(appkey, desc.intro, start, 0, 0, 0, 0, 0, 0, latencyMap, latencyMap)).asJava
        }
    }.toList
    val filtret = ret
      .filter(_.get("requestCount").asInstanceOf[Number].doubleValue() > 0)
    if (filtret.isEmpty) {
      ret.slice(0, 10).asJava
    } else {
      filtret.asJava
    }
  }

  def getPerf(appkey: String, start: Long) = {
    val time = (start / 1000).toInt - 120
    val dataSource = DataQuery.queryDataSource
    val recordsOpt = DataQuery.getDataRecord(appkey, time, time, null, "server", null, "prod", "minute", "span", "all", "all", null, null, dataSource)
    recordsOpt.getOrElse(List())
  }

  def getHostCount(appkey: String) = {
    val thriftCount = AppkeyProviderService.getProviderNodeCountBy(appkey, "prod", 2, 1)
    val hostCount = if (thriftCount > 0){
      thriftCount
    } else {
      AppkeyProviderService.getProviderNodeCountBy(appkey, "prod", 2, 0)
    }
    hostCount
  }

  def main(args: Array[String]) {
    val start = System.currentTimeMillis()
    (0 to 50).map{
      x=>
        println(getPerf("com.sankuai.inf.msgp", 1498718469000L))
    }
    val end = System.currentTimeMillis()
    println((end - start)/50)
  }
}
