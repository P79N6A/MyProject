package com.sankuai.msgp.task.job

import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

import com.sankuai.msgp.common.config.db.msgp.Tables.{AvailabilityDayDetailRow, AvailabilityDayReportRow}
import com.sankuai.msgp.common.model.Env
import com.sankuai.msgp.common.utils.{DateTimeUtil, StringUtil}
import com.sankuai.octo.statistic.constant.Constants
import com.sankuai.octo.statistic.util.ExecutionContextFactory
import org.joda.time.DateTime
import org.slf4j.LoggerFactory
import play.api.libs.json.Json

import scala.collection.parallel.ForkJoinTaskSupport
import scala.concurrent.duration.{Duration, FiniteDuration}

object AvailabilityJob {
  /*private val logger = LoggerFactory.getLogger(AvailabilityJob.getClass)

  private val timeout: FiniteDuration = Duration.create(20000L, TimeUnit.MILLISECONDS)
  private implicit val ec = ExecutionContextFactory.build(20)
  private val availabilityThreadPool = new ForkJoinTaskSupport(new scala.concurrent.forkjoin.ForkJoinPool(5))

  private val reportItemCount = new AtomicInteger(0)
  private val reportDetailItemCount = new AtomicInteger(0)

  case class ClientCountDetails(appkey: String, count: Long = 0L,
                                successCount: Long = 0L,
                                exceptionCount: Long = 0L,
                                timeoutCount: Long = 0L,
                                dropCount: Long = 0L)

  case class ClientCount(count: Long = 0L,
                         successCount: Long = 0L,
                         exceptionCount: Long = 0L,
                         timeoutCount: Long = 0L,
                         dropCount: Long = 0L,
                         http2XXCount: Long = 0L,
                         http3XXCount: Long = 0L,
                         http4XXCount: Long = 0L,
                         http5XXCount: Long = 0L)

  /**
    * 获取可用率指标
    * 1:获取 appkey 的所有调用方
    * 2:获取appkey 的所有spanname
    * 3:获取基于client端的 appkey * spanname 的可用率指标
    * 4:获取基于 appkey * spanname * remoteAppkey 的可用率指标
    */
  def getAvailability(appkey: String, env: String, dateTime: DateTime) = {
    val startTime = (dateTime.getMillis / 1000).toInt
    val endTime = (dateTime.plusDays(1).minusMillis(1).getMillis / 1000).toInt
    val dataSource = DataQuery.queryDataSource
    val tags = DataQuery.tags(appkey, (dateTime.getMillis / 1000).toInt, (dateTime.getMillis / 1000).toInt, env, "server")

    //TODO 这里不需要过滤unknownService
    //获取所有调用方的appkey, 即remoteAppKey
    val remoteAppkeys = tags.remoteAppKeys.filter {
      appkey =>
        !appkey.equals("*") && !appkey.equals("all") && !appkey.equals("unknownService")
    }
    //获取涉及到的接口
    val spanNames = tags.spannames.filter {
      spanname =>
        !spanname.equals("*") && !spanname.equals("all")
    }
    // 存储 spanname * 基于客户端的统计数据汇总
    val clientCountMap = scala.collection.mutable.Map[String, ClientCount]()
    // 存储 spanname * remoteAppkey * 基于客户端的统计数据汇总
    val clientCountDetailsMap = scala.collection.mutable.Map[String, List[ClientCountDetails]]()
    remoteAppkeys.foreach {
      remoteAppkey =>
        try {
          //TODO 使用存在Tair中的天粒度数据代替
          val thriftRet = DataQuery.getHistoryStatisticMerged(remoteAppkey, startTime, endTime, "thrift", "client", "", env, "", "SpanRemoteApp", "*",
            "", appkey, "", dataSource, merge = true)(timeout)
          if (thriftRet.isDefined) {
            val content = thriftRet.get
            //获取到当前remoteAppkey的调用, 根据spanname分类
            val spanNameMap = content.groupBy(_.tags.spanname.get)
            spanNameMap.foreach {
              case (_spanname, records) =>
                //判断remoteAppkey是否使用了该接口
                if (StringUtil.isNotBlank(_spanname) && spanNames.contains(_spanname)) {
                  if (records.nonEmpty) {
                    val dailyRecord = records.head
                    val oldClientCount = clientCountMap.getOrElse(_spanname, ClientCount())
                    //不断更新存入的数据
                    val newClientCount = oldClientCount.copy(
                      count = oldClientCount.count + dailyRecord.count.toLong,
                      successCount = oldClientCount.successCount + dailyRecord.successCount.toLong,
                      exceptionCount = oldClientCount.exceptionCount + dailyRecord.exceptionCount.toLong,
                      timeoutCount = oldClientCount.timeoutCount + dailyRecord.timeoutCount.toLong,
                      dropCount = oldClientCount.dropCount + dailyRecord.dropCount.toLong
                    )
                    clientCountMap.update(_spanname, newClientCount)

                    //调用详情中,不能累加,需要保存每个spanname的记录
                    val countDetailList = clientCountDetailsMap.getOrElse(_spanname, List[ClientCountDetails]())
                    val details = ClientCountDetails(dailyRecord.appkey, dailyRecord.count.toLong, dailyRecord.successCount.toLong,
                      dailyRecord.exceptionCount.toLong, dailyRecord.timeoutCount.toLong, dailyRecord.dropCount.toLong)
                    clientCountDetailsMap.update(_spanname, details :: countDetailList)
                  }
                }
            }
          }
        } catch {
          case e: Exception =>
            logger.error("getDailyStatistic failed in AvailabilityJob", e)
        }
    }
    /*对于Http接口, DataQuery.getHistoryStatisticMerged是无法基于client获取数据的,因此要将服务本身作为Server去查询数据*/
    val thriftSpan = clientCountMap.keys.toList
    val httpSpan = spanNames.filterNot {
      x =>
        thriftSpan.contains(x)
    }

    /** http的数据从天粒度数中获取,原因是这里不需要计算详情数据clientCountDetailsMap了 */
    try {
      val httpRet = DataQuery.getDailyStatistic(appkey, env, dateTime, "server")
      Json.parse(httpRet).validate[List[StatisticWithHttpCode]].fold({
        error =>
          logger.error(s"parse dailyKpi failed", error)
      }, {
        content =>
          content.foreach {
            dailyRecord =>
              val statData = dailyRecord.statData
              val httpData = dailyRecord.httpCode
              val tags = statData.tags
              val _spaname = tags.getOrElse(Constants.SPAN_NAME, "")

              //没有在客户端统计到的数据,也会在此统计, 如mtconfig中的getMergeData接口
              val successCount = if (statData.successCount != 0) {
                statData.successCount
              } else {
                httpData.http2XXCount + httpData.http3XXCount
              }
              if (StringUtil.isNotBlank(_spaname) && httpSpan.contains(_spaname)) {
                //不断更新存入的数据
                val clientCount = ClientCount(
                  count = statData.count,
                  successCount = successCount, //注意 目前没有区分接口类型的字段,只能如此处理
                  exceptionCount = statData.exceptionCount,
                  timeoutCount = statData.timeoutCount,
                  dropCount = statData.dropCount,
                  http2XXCount = httpData.http2XXCount,
                  http3XXCount = httpData.http3XXCount,
                  http4XXCount = httpData.http4XXCount,
                  http5XXCount = httpData.http5XXCount
                )
                clientCountMap.update(_spaname, clientCount)
              }
          }
      })
    } catch {
      case e: Exception =>
        logger.error("getDailyStatistic failed in AvailabilityJob", e)
    }

    // 最后再次计算all的量
    if (clientCountMap.nonEmpty) {
      val list = clientCountMap.values
      clientCountMap.update("all", ClientCount(count = list.map(_.count).sum, successCount = list.map(_.successCount).sum,
        exceptionCount = list.map(_.exceptionCount).sum, timeoutCount = list.map(_.timeoutCount).sum, dropCount = list.map(_.dropCount).sum,
        http2XXCount = list.map(_.http2XXCount).sum, http3XXCount = list.map(_.http3XXCount).sum,
        http4XXCount = list.map(_.http4XXCount).sum, http5XXCount = list.map(_.http5XXCount).sum))
    }
    (clientCountMap, clientCountDetailsMap)
  }

  // 离线计算前一天的可用率
  def saveAvailability(appkeys: List[String], dateTime: DateTime) = {
    val start = new DateTime()
    logger.info(s"all saveAvailability begin  at ${start.toString(DateTimeUtil.DATE_TIME_FORMAT)}")
    val appkeyPar = appkeys.par
    appkeyPar.tasksupport = availabilityThreadPool
    appkeyPar.foreach {
      appkey =>
        val result = getAvailability(appkey, Env.prod.toString, dateTime)
        //得到客户端统计概要
        val availabilities = result._1
        if (availabilities.nonEmpty) {
          val data = try {
            DataQuery.getDailyStatisticFormatted(appkey, "prod", dateTime, "server")
          } catch {
            case e: Exception => logger.error("DataQuery getDailyStat4Frontend failed", e)
              List()
          }
          val availabilityList = availabilities.map {
            availability =>
              val spanname = availability._1
              val spanUsable = availability._2

              val count = spanUsable.count
              val successCount = spanUsable.successCount

              val exceptionCount = spanUsable.exceptionCount
              val timeoutCount = spanUsable.timeoutCount
              val dropCount = spanUsable.dropCount

              var successCountPer, exceptionCountPer, timeoutCountPer, dropCountPer = BigDecimal(0.0)

              if (count != 0) {
                successCountPer = BigDecimal(successCount.toDouble / count * 100).setScale(4, BigDecimal.RoundingMode.DOWN)
                exceptionCountPer = BigDecimal(exceptionCount.toDouble / count * 100).setScale(4, BigDecimal.RoundingMode.DOWN)
                timeoutCountPer = BigDecimal(timeoutCount.toDouble / count * 100).setScale(4, BigDecimal.RoundingMode.DOWN)
                dropCountPer = BigDecimal(dropCount.toDouble / count * 100).setScale(4, BigDecimal.RoundingMode.DOWN)

                val find = data.find(_.spanname == spanname)
                val costMean = if (find.nonEmpty) {
                  find.get.costMean
                } else {
                  0
                }
                AvailabilityDayReportRow(0, (dateTime.getMillis / 1000).toInt, appkey, spanname, count, successCount, Some(successCountPer),
                  exceptionCount, Some(exceptionCountPer), timeoutCount, Some(timeoutCountPer), dropCount, Some(dropCountPer), 0, costMean)
              } else {
                AvailabilityDayReportRow(0, 0, "", "", 0l, 0l, Some(0.0), 0l, Some(0.0), 0l, Some(0.0), 0l, Some(0.0), 0, 0.0)
              }
          }.toList.filter { x => x.count > 0 && x.spanname.length < 255 }

          //得到客户端调用详情 注:是没有all数据的
          val availabilityDetails = result._2
          val availabilityDetailList = availabilityDetails.flatMap {
            availabilityDetail =>
              val spanname = availabilityDetail._1
              val countDetailList = availabilityDetail._2
              if (spanname.length > 255) {
                logger.info(s"Availability: spanname is too long $appkey, $spanname")
              }
              countDetailList.map {
                details =>
                  val count = details.count

                  val successCount = details.successCount
                  val exceptionCount = details.exceptionCount
                  val timeoutCount = details.timeoutCount
                  val dropCount = details.dropCount

                  val failureCount = exceptionCount + timeoutCount + dropCount

                  var successCountPer, failureCountPer, exceptionCountPer, timeoutCountPer, dropCountPer = BigDecimal(0.0)

                  if (count != 0) {
                    successCountPer = BigDecimal(successCount.toDouble / count * 100).setScale(4, BigDecimal.RoundingMode.DOWN)
                    exceptionCountPer = BigDecimal(exceptionCount.toDouble / count * 100).setScale(4, BigDecimal.RoundingMode.DOWN)
                    timeoutCountPer = BigDecimal(timeoutCount.toDouble / count * 100).setScale(4, BigDecimal.RoundingMode.DOWN)
                    dropCountPer = BigDecimal(dropCount.toDouble / count * 100).setScale(4, BigDecimal.RoundingMode.DOWN)
                    failureCountPer = BigDecimal(failureCount.toDouble / count * 100).setScale(4, BigDecimal.RoundingMode.DOWN)
                  }
                  AvailabilityDayDetailRow(0, (dateTime.getMillis / 1000).toInt, appkey, spanname, details.appkey, count, successCount, successCountPer,
                    failureCount, failureCountPer, exceptionCount, exceptionCountPer, timeoutCount, timeoutCountPer, dropCount, dropCountPer)
              }
          }.toList.filter { x => x.count > 0 && x.spanname.length < 255 }

          reportItemCount.addAndGet(availabilityList.size)
          reportDetailItemCount.addAndGet(availabilityDetailList.size)

          try {
            AvailabilityDao.batchInsertAvailabilityDetails(availabilityDetailList)
            AvailabilityDao.batchInsertAvailability(availabilityList)
          } catch {
            case e: Exception => logger.error("Save availability details failed", e)
          }
        } else {
          logger.info(s"Availability Data is empty, appkey is: $appkey")
        }
    }
    val end = new DateTime()
    logger.info(s"availabilityList size: ${reportItemCount.get()}, availabilityDetailList size: ${reportDetailItemCount.get()}")
    logger.info(s"saveAvailability operation end at ${end.toString(DateTimeUtil.DATE_TIME_FORMAT)}")
    logger.info(s"compute all appkey's availability, it costs ${(end.getMillis - start.getMillis) / 1000}s")
  }


  // 每天2:00同步前一天天粒度数据到数据库中
  def start = {
    val now = DateTime.now()
    val startOfYesterday = now.minusDays(1).withTimeAtStartOfDay()
    AvailabilityJob.saveAvailability(ServiceDesc.appsName().filter(!_.contains("tair")), startOfYesterday)
  }*/
}
