package com.sankuai.octo.msgp.task

import com.sankuai.msgp.common.model.Env
import com.sankuai.octo.msgp.serivce.data.DataQuery
import com.sankuai.octo.msgp.serivce.service.ServiceDesc
import com.sankuai.octo.msgp.dao.availability.AvailabilityDao
import com.sankuai.msgp.common.config.db.msgp.Tables.{AvailabilityDayDetailRow, AvailabilityDayReportRow}
import com.sankuai.msgp.common.utils.StringUtil
import com.sankuai.octo.mworth.util.DateTimeUtil
import com.sankuai.octo.statistic.util.ExecutionContextFactory
import org.joda.time.DateTime
import org.quartz._
import org.quartz.impl.StdSchedulerFactory
import org.slf4j.LoggerFactory

import scala.collection.JavaConverters._
import scala.collection.parallel.ForkJoinTaskSupport
import scala.concurrent.Future

object AvailabilityTask {
  private val logger = LoggerFactory.getLogger(AvailabilityTask.getClass)

  private implicit val ec = ExecutionContextFactory.build(20)
  private val availabilityThreadPool = new ForkJoinTaskSupport(new scala.concurrent.forkjoin.ForkJoinPool(4))

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
          val thriftRet = DataQuery.getDataRecordMerged(remoteAppkey, startTime, endTime, "thrift", "client", "", env, "", "SpanRemoteApp", "*",
            "", appkey, "", dataSource)
          thriftRet match {
            case None =>
              logger.error(s"Fetching data from dataCenterHistoryDataMerged failed")
              throw new Exception(s"Fetching data from dataCenterHistoryDataMerged failed")
            case Some(content) =>
              //获取到当前remoteAppkey的调用, 根据spanname分类
              val spanNameMap = content.groupBy(_.tags.spanname.get)
              spanNameMap.foreach {
                case (_spanname, records) =>
                  //判断remoteAppkey是否使用了该接口
                  if (StringUtil.isNotBlank(_spanname) && spanNames.contains(_spanname)) {
                    if (records.nonEmpty) {
                      val dailyRecord = records.head
                      /** TODO: 此处是为了fix query接口的查询结果导致的统计异常
                        * 具体是: 如果指定remoteAppkey为 appkey1, query接口会返回appkey1,appkey12,appkey123
                        * */
                      val realRemoteAppkey = dailyRecord.tags.remoteApp.getOrElse("")
                      if (realRemoteAppkey.equalsIgnoreCase(appkey)) {
                        val oldClientCount = clientCountMap.getOrElse(_spanname, ClientCount())
                        //不断更新存入的数据
                        val newClientCount = oldClientCount.copy(
                          count = oldClientCount.count + dailyRecord.count.toLong,

                          successCount = oldClientCount.successCount + dailyRecord.successCount.toLong,
                          exceptionCount = oldClientCount.exceptionCount + dailyRecord.exceptionCount.toLong,
                          timeoutCount = oldClientCount.timeoutCount + dailyRecord.timeoutCount.toLong,
                          dropCount = oldClientCount.dropCount + dailyRecord.dropCount.toLong,

                          http2XXCount = oldClientCount.http2XXCount.toLong + dailyRecord.HTTP2XXCount.toLong,
                          http3XXCount = oldClientCount.http3XXCount.toLong + dailyRecord.HTTP3XXCount.toLong,
                          http4XXCount = oldClientCount.http4XXCount.toLong + dailyRecord.HTTP4XXCount.toLong,
                          http5XXCount = oldClientCount.http5XXCount.toLong + dailyRecord.HTTP5XXCount.toLong

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
          }
        } catch {
          case e: Exception =>
            logger.error(s"Fetching data from dataCenterHistoryData2 failed,appkey ${appkey}", e)
        }
    }
    /*对于Http接口, DataQuery.getHistoryStatisticMerged是无法基于client获取数据的,因此要将服务本身作为Server去查询数据*/
    val thriftSpan = clientCountMap.keys.toList
    val httpSpan = spanNames.filterNot {
      x =>
        thriftSpan.contains(x)
    }

    /** http的数据从天粒度数中获取,原因是这里不需要计算详情数据clientCountDetailsMap了 */
    val httpRet = DataQuery.getDailyStatistic(appkey, env, dateTime, "server")
    httpRet match {
      case Some(content) =>
        content.asScala.foreach {
          dailyRecord =>
            val tags = dailyRecord.getTags
            val _spaname = tags.getSpanname

            //没有在客户端统计到的数据,也会在此统计, 如mtconfig中的getMergeData接口
            val successCount = dailyRecord.getSuccessCount

            if (StringUtil.isNotBlank(_spaname) && httpSpan.contains(_spaname)) {
              //不断更新存入的数据
              val clientCount = ClientCount(
                count = dailyRecord.getCount,
                successCount = successCount, //注意 目前没有区分接口类型的字段,只能如此处理
                exceptionCount = dailyRecord.getExceptionCount,
                timeoutCount = dailyRecord.getTimeoutCount,
                dropCount = dailyRecord.getTimeoutCount,
                http2XXCount = dailyRecord.getHTTP2XXCount,
                http3XXCount = dailyRecord.getHTTP3XXCount,
                http4XXCount = dailyRecord.getHTTP4XXCount,
                http5XXCount = dailyRecord.getHTTP5XXCount
              )
              clientCountMap.update(_spaname, clientCount)
            }
        }
      case None =>
        logger.error(s"parse dailyKpi failed")
        throw new Exception(s"parse dailyKpi failed")
    }

    // 最后再次计算all的量
    if (clientCountMap.nonEmpty) {
      val list = clientCountMap.values
      clientCountMap.update("all", ClientCount(count = list.map(_.count).sum, successCount = list.map(_.successCount).sum,
        exceptionCount = list.map(_.exceptionCount).sum, timeoutCount = list.map(_.timeoutCount).sum, dropCount = list.map(_.dropCount).sum,
        http2XXCount = list.map(_.http2XXCount).sum, http3XXCount = list.map(_.http3XXCount).sum,
        http4XXCount = list.map(_.http4XXCount).sum, http5XXCount = list.map(_.http5XXCount).sum))
    }
    if(appkey.equals("com.sankuai.cx.quickpass.member")){
      logger.info(s"### clientCountMap data : $clientCountMap")
      logger.info(s"### clientCountDeatailsMap : $clientCountDetailsMap")
    }
    (clientCountMap, clientCountDetailsMap)
  }

  // 离线计算前一天的可用率
  def saveAvailability(appkeys: List[String], dateTime: DateTime) = {
    val start = new DateTime()
    logger.info(s"all saveAvailability begin ${appkeys} at ${start.toString(DateTimeUtil.DATE_TIME_FORMAT)}")
    val appkeyPar = appkeys.par
    appkeyPar.tasksupport = availabilityThreadPool
    appkeyPar.foreach {
      appkey =>
        try {
          computeAvailability(appkey, dateTime)
        }
        catch {
          case e: Exception =>
            logger.error(s"saveAvailablity failed,appkey ${appkey}", e)
        }
    }
    val end = new DateTime()
    logger.info(s"all saveAvailability end at ${end.toString(DateTimeUtil.DATE_TIME_FORMAT)}")
    logger.info(s"all appkey availability cost ${(end.getMillis - start.getMillis) / 1000}s")
  }

  def computeAvailability(appkey: String, dateTime: DateTime) = {
    val result = getAvailability(appkey, Env.prod.toString, dateTime)
    //得到客户端统计概要
    val availabilities = result._1
    if (availabilities.nonEmpty) {

      val serverStatistic = try {
        DataQuery.getDailyStatisticFormatted(appkey, "prod", dateTime, "server")
      } catch {
        case e: Exception =>
          logger.error(s"DataQuery getDailyStat4Frontend failed,appkey:${appkey}", e)
          List()
      }
      if(appkey.equals("com.sankuai.cx.quickpass.member")){
        logger.info(s"### serverStatistic data : $serverStatistic")
      }
      val serverStatisticMap = serverStatistic.groupBy(_.spanname)

      if(appkey.equals("com.sankuai.cx.quickpass.member")){
        logger.info(s"### serverStatisticMap data : $serverStatisticMap")
      }

      val availabilityList = availabilities.map {
        availability =>
          val spanname = availability._1
          val spanUsable = availability._2

          val serverRecord = serverStatisticMap.get(spanname)

          if(appkey.equals("com.sankuai.cx.quickpass.member")){
            logger.info(s"### serverRecord data : $serverRecord")
          }

          val count = spanUsable.count
          val successCount = spanUsable.successCount + spanUsable.http2XXCount + spanUsable.http3XXCount

          val exceptionCount = spanUsable.exceptionCount
          val timeoutCount = spanUsable.timeoutCount
          val dropCount = spanUsable.dropCount

          var successCountPer, exceptionCountPer, timeoutCountPer, dropCountPer = BigDecimal(0.0)

          val serverCount = if (serverRecord.isDefined) {
            serverRecord.get.head.count
          } else {
            0L
          }

          if (serverCount != 0 && count != 0) {
            //注意可用率计算时,调用量使用服务端的数据
            val errorCount = count - successCount

            val successCountRatio = BigDecimal((serverCount - errorCount.toDouble) / serverCount * 100)
            successCountPer = if (successCountRatio.doubleValue() > 100.0) {
              BigDecimal(100.0)
            } else {
              successCountRatio.setScale(4, BigDecimal.RoundingMode.DOWN)
            }
            exceptionCountPer = BigDecimal(exceptionCount.toDouble / count * 100).setScale(4, BigDecimal.RoundingMode.DOWN)
            timeoutCountPer = BigDecimal(timeoutCount.toDouble / count * 100).setScale(4, BigDecimal.RoundingMode.DOWN)
            dropCountPer = BigDecimal(dropCount.toDouble / count * 100).setScale(4, BigDecimal.RoundingMode.DOWN)

            val costMean = if (serverRecord.isDefined) {
              serverRecord.get.head.costMean
            } else {
              0
            }
            if(appkey.equals("com.sankuai.cx.quickpass.member")){
              logger.info("### calc result : "+AvailabilityDayReportRow(0, (dateTime.getMillis / 1000).toInt, appkey, spanname, count, successCount, Some(successCountPer),
                exceptionCount, Some(exceptionCountPer), timeoutCount, Some(timeoutCountPer), dropCount, Some(dropCountPer),
                spanUsable.http2XXCount, spanUsable.http3XXCount, spanUsable.http4XXCount, spanUsable.http5XXCount, 0, costMean))
            }
            AvailabilityDayReportRow(0, (dateTime.getMillis / 1000).toInt, appkey, spanname, count, successCount, Some(successCountPer),
              exceptionCount, Some(exceptionCountPer), timeoutCount, Some(timeoutCountPer), dropCount, Some(dropCountPer),
              spanUsable.http2XXCount, spanUsable.http3XXCount, spanUsable.http4XXCount, spanUsable.http5XXCount, 0, costMean)
          } else {
            if(appkey.equals("com.sankuai.cx.quickpass.member")){
              logger.info(s"### calc failue serverCount : $serverCount and count : $count")
            }
            AvailabilityDayReportRow(0, 0, "", "", 0l, 0l, Some(0.0), 0l, Some(0.0), 0l, Some(0.0), 0l, Some(0.0), 0, 0, 0, 0, 0, 0.0)
          }
      }.toList

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
                //此处不是可用率
                val successCountRatio = BigDecimal(successCount.toDouble / count * 100)
                successCountPer = if (successCountRatio.doubleValue() > 100.0) {
                  BigDecimal(100.0)
                } else {
                  successCountRatio.setScale(4, BigDecimal.RoundingMode.DOWN)
                }
                exceptionCountPer = BigDecimal(exceptionCount.toDouble / count * 100).setScale(4, BigDecimal.RoundingMode.DOWN)
                timeoutCountPer = BigDecimal(timeoutCount.toDouble / count * 100).setScale(4, BigDecimal.RoundingMode.DOWN)
                dropCountPer = BigDecimal(dropCount.toDouble / count * 100).setScale(4, BigDecimal.RoundingMode.DOWN)
                failureCountPer = BigDecimal(failureCount.toDouble / count * 100).setScale(4, BigDecimal.RoundingMode.DOWN)
              }
              AvailabilityDayDetailRow(0, (dateTime.getMillis / 1000).toInt, appkey, spanname, details.appkey, count, successCount, Some(successCountPer),
                failureCount, Some(failureCountPer), exceptionCount, Some(exceptionCountPer), timeoutCount, Some(timeoutCountPer), dropCount, Some(dropCountPer))
          }
      }.toList
      try {
        AvailabilityDao.batchInsertAvailabilityDetails(availabilityDetailList.filter { x => x.count > 0 && x.spanname.length < 255 })
        AvailabilityDao.batchInsertAvailability(availabilityList.filter { x => x.count > 0 && x.spanname.length < 255 })
      } catch {
        case e: Exception => logger.error("Save availability details failed, appkey is: $appkey", e)
      }
    } else {
      logger.info(s"Availability Data is empty, appkey is: $appkey")
    }
  }

  // 每天2:00同步前一天天粒度数据到数据库中
  def start = {
    try {
      val cronScheduler = new StdSchedulerFactory().getScheduler
      cronScheduler.start()

      val job = JobBuilder.newJob(classOf[AvailabilityDailyJob]).build()
      val trigger = TriggerBuilder.newTrigger().startNow()
        .withSchedule(CronScheduleBuilder.dailyAtHourAndMinute(2, 30)).build()
      cronScheduler.scheduleJob(job, trigger)

      // 在jvm退出时优雅关闭
      Runtime.getRuntime.addShutdownHook(new Thread() {
        override def run(): Unit = {
          cronScheduler.shutdown(true)
        }
      })
    } catch {
      case e: Exception => logger.error(s"data startCrontab failed $e")
    }
  }

  /**
    * 手动计算全部可用率数据
    *
    * @param dateTime
    */
  def insertAllAppkeyAvailability(dateTime: DateTime) = {
    Future {
      val startOfDay = dateTime.withTimeAtStartOfDay()
      AvailabilityTask.saveAvailability(ServiceDesc.appsName().filter(!_.contains("tair")), startOfDay)
    }
  }

  def insertSingleAvailability(appkey: String, dateTime: DateTime) = {
    Future {
      val appkeys = List(appkey)
      val startOfDay = dateTime.withTimeAtStartOfDay()
      AvailabilityTask.saveAvailability(appkeys, startOfDay)
    }
  }

  def main(args: Array[String]) {
    insertAllAppkeyAvailability(new DateTime(1467820800000L))
  }
}

@DisallowConcurrentExecution
class AvailabilityDailyJob extends Job {
  private val logger = LoggerFactory.getLogger(this.getClass)

  @throws(classOf[JobExecutionException])
  override def execute(ctx: JobExecutionContext) {
    logger.info("data CrontabUpdateJob Start")
    // 取得昨天的时间
    val now = DateTime.now()
    val startOfYesterday = now.minusDays(1).withTimeAtStartOfDay()
    AvailabilityTask.saveAvailability(ServiceDesc.appsName().filter(!_.contains("tair")), startOfYesterday)
  }
}
