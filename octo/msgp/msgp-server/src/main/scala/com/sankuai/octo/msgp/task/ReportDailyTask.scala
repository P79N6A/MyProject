package com.sankuai.octo.msgp.task

import java.util.concurrent.{Executors, TimeUnit}

import com.sankuai.inf.octo.mns.util.ProcessInfoUtil
import com.sankuai.msgp.common.model.Page
import com.sankuai.msgp.common.utils.StringUtil
import com.sankuai.msgp.common.utils.helper.{CommonHelper, TaskTimeHelper}
import com.sankuai.octo.msgp.serivce.data.DataQuery
import com.sankuai.octo.msgp.serivce.data.DataQuery.{DataRecord, DataRecordMerged, Point}
import com.sankuai.octo.msgp.serivce.monitor.MonitorEvent
import com.sankuai.octo.msgp.serivce.service.{AppkeyProviderService, ServiceCommon}
import com.sankuai.octo.msgp.dao.availability.AvailabilityDao
import com.sankuai.octo.msgp.dao.report.ReportDailyDao.ReportDailyDomain
import com.sankuai.octo.msgp.dao.report.ReportDailyMailDao.ReportDailyMailDomain
import com.sankuai.octo.msgp.dao.report.{ReportDailyDao, ReportDailyMailDao, ReportDailyStatusDAO}
import com.sankuai.octo.msgp.utils._
import com.sankuai.octo.msgp.utils.helper.ReportHelper
import com.sankuai.octo.mworth.utils.CronScheduler
import com.sankuai.octo.statistic.constant.Constants
import com.sankuai.octo.statistic.model.DailyRecord
import org.joda.time.DateTime
import org.quartz._
import org.slf4j.LoggerFactory

import scala.collection.JavaConverters._
import scala.collection.parallel.ForkJoinTaskSupport
import scala.concurrent.duration.Duration
import scala.concurrent.{ExecutionContext, Future}


object ReportDailyTask {

  val timeout = Duration.create(60L, TimeUnit.SECONDS)

  private[task] val logger = LoggerFactory.getLogger(this.getClass)

  private val refreshDailyThreadPool = new ForkJoinTaskSupport((new scala.concurrent.forkjoin.ForkJoinPool(2)))
  private implicit val ec = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(2))

  def init(): Unit = {
    val job = JobBuilder.newJob(classOf[ReportDailyJob]).build()
    val trigger: Trigger = TriggerBuilder.newTrigger().startNow()
      .withSchedule(CronScheduleBuilder.dailyAtHourAndMinute(3, 30)).build()
    CronScheduler.scheduleJob(job, trigger)
  }

  //手动刷新数据用
  def computeReportManually(owt: String, appkey: String, start: Int, end: Int) = {
    val currentDate = new java.sql.Date(start * 1000L)
    try {
      computeReportDaily(owt, appkey, "*", start, end)
      logger.info(s"appkey :$appkey, start:$start")
      ReportDailyStatusDAO.delete(appkey, currentDate)
    }
    catch {
      case e: Exception =>
        logger.error(s"computeReport Maunually error,appkey :$appkey, start:$start", e)
    }
  }

  /**
    *
    * @param start 起始时间
    * @param end   终止时间
    * @return
    */
  def computeOneReport(owt: String, appkey: String, spanNameFlag: String, start: Int, end: Int, day: java.sql.Date): Unit = {
    try {
      computeReportDaily(owt, appkey, spanNameFlag, start, end)
      if (spanNameFlag.equalsIgnoreCase(Constants.ALL)) {
        logger.info(s"asyncComputeDaily successfully while computing all spanname. $owt,$appkey")
        ReportDailyStatusDAO.updateStatus(appkey, day, 1)
      } else {
        ReportDailyStatusDAO.updateStatus(appkey, day, 2)
        logger.info(s"asyncComputeDaily successfully while computing * spanname. $owt,$appkey")
      }
    }
    catch {
      case e: Exception =>
        if (spanNameFlag.equalsIgnoreCase(Constants.ALL)) {
          logger.error(s"asyncComputeDaily failed while computing all spanname. appkey $appkey", e)
        } else {
          logger.error(s"asyncComputeDaily failed while computing * spanname. appkey $appkey", e)
        }
    }
  }


  /**
    * 统计appkey下所有spanname的性能数据
    * isSucess ? 调用 DataQuery 为异常的时候，认为计算失败，需要重新计算
    *
    * @param owt
    * @param appkey
    * @param spanNameFlag * 或者 all, 当传入all, 计算all接口的数据;
    *                     传入*, 计算全部数据;
    *                     传入"", 则计算除all之外所有接口
    * @param start
    * @param end
    * @return
    */
  def computeReportDaily(owt: String, appkey: String, spanNameFlag: String, start: Int, end: Int): Unit = {
    val currentDate = new java.sql.Date(start * 1000L)
    val currentDateTime = new DateTime(start * 1000L)

    val realSpan = if (spanNameFlag.equalsIgnoreCase(Constants.ALL)) Constants.ALL else "*"
    val recordsOption = DataQuery.getDataRecordMerged(appkey, start, end, "thrift", "server", "", "prod", "Hour", "span", realSpan, Constants.ALL,
      Constants.ALL, Constants.ALL, "hbase")

    var spanRecords = recordsOption.getOrElse(List[DataRecordMerged]()).filterNot { record =>
      record.tags.spanname.isEmpty || record.tags.spanname.get.isEmpty
    }

    //当传入的spanNameFlag是"", 则不需要计算ALL接口
    spanRecords = if (StringUtil.isBlank(spanNameFlag)) {
      spanRecords.filterNot(_.tags.spanname.getOrElse("").equalsIgnoreCase(Constants.ALL))
    } else {
      spanRecords
    }

    if (spanRecords.nonEmpty) {
      //warn: 从tair获取的天粒度数据 数据可能为空
      val dailyData = getDailyData(appkey, currentDateTime)
      val spanPar = spanRecords.par
      spanPar.tasksupport = refreshDailyThreadPool
      spanPar.foreach {
        record =>
          val spanDailyReportOption = computeReportWithSpan(owt, appkey, record.tags.spanname.get, start, end,
            currentDate, dailyData)
          //写入日报表
          spanDailyReportOption match {
            case Some(spanDailyReport) =>
              ReportDailyDao.batchInsert(List(spanDailyReport))
            case None =>
          }
      }
      //写入日报邮件表
      val desc = ServiceCommon.desc(appkey)
      if (desc.owners.nonEmpty) {
        val mails = desc.owners.map {
          owner =>
            ReportDailyMailDomain(appkey, owner.login, currentDate, 0, 0, System.currentTimeMillis())
        }
        ReportDailyMailDao.batchInsert(mails)
      }
    } else {
      val day = new java.sql.Date(start * 1000L)
      ReportDailyStatusDAO.updateStatus(appkey, day, -1)
      logger.error(s"appkey: $appkey data is empty, owt: $owt")
    }
  }

  /**
    * 统计单个spanname的性能数据
    *
    * @param owt
    * @param appkey
    * @param spanname
    * @param start
    * @param end
    */
  def computeReportWithSpan(owt: String, appkey: String, spanname: String, start: Int, end: Int,
                            currentDate: java.sql.Date, dailyData: List[DailyRecord]) = {

    val currentDateTime = new DateTime(start * 1000L)

    val localhostRecordsSpanNotAllOption = DataQuery.getDataRecord(appkey, start, end, "thrift", "server", "", "prod", "Hour", "spanLocalhost", spanname, "*",
      Constants.ALL, Constants.ALL, "hbase")
    val localhostRecordsSpanAllOption = DataQuery.getDataRecord(appkey, start, end, "thrift", "server", "", "prod", "Hour", "spanLocalhost", spanname, Constants.ALL,
      Constants.ALL, Constants.ALL, "hbase")
    val localhostRecords = localhostRecordsSpanAllOption.getOrElse(List[DataRecord]()) ::: localhostRecordsSpanAllOption.getOrElse(List[DataRecord]())

    localhostRecordsSpanAllOption match {
      case Some(localhostRecordsSpanAll) =>
        if (localhostRecordsSpanAll.nonEmpty) {
          val record = localhostRecordsSpanAll.head
          val allCountPointList = record.count.filter(c => c.y.nonEmpty && c.y.get != 0.0)

          // success count这样处理原因是最初设计数据中心字段时,successCount用作了thrift的成功调用数, 后来加入了http字段
          // 因此真实的successCount需要把这两块分别加起来
          val thriftSuccessPointList = record.successCount.filter(c => c.y.nonEmpty && c.y.get != 0.0)
          val httpSuccessPointList = record.HTTP2XXCount.filter(c => c.y.nonEmpty && c.y.get != 0.0) ::: record.HTTP3XXCount.filter(c => c.y.nonEmpty && c.y.get != 0.0)
          val allSuccessPointList = thriftSuccessPointList ::: httpSuccessPointList

          val allCountList = allCountPointList.map(_.y.get)
          val allSuccessCountList = allSuccessPointList.map(_.y.get)

          if (allCountList.nonEmpty && localhostRecordsSpanNotAllOption.nonEmpty
            && localhostRecordsSpanNotAllOption.get.nonEmpty) {
            //if (allCounts.nonEmpty && nonAllRecords.nonEmpty) {
            val appKeyTotalTraffic = allCountList.sum
            val appKeyTotalSuccessTraffic = allSuccessCountList.sum
            val (maxCountPoint, minCountPoint) = getMaxAndMin(allCountPointList)
            val maxStart = maxCountPoint.ts.get
            val maxEnd = maxStart + Constants.ONE_HOUR_SECONDS
            val minStart = minCountPoint.ts.get
            val minEnd = minStart + Constants.ONE_HOUR_SECONDS

            //获取该spanname下的天粒度数据
            val dailySpanData = dailyData.find(x => x.getTags.getSpanname.equalsIgnoreCase(spanname)).getOrElse(new DailyRecord)
            val dailyQps = dailySpanData.getQps.toInt
            val successRadio = getSuccessRadio(appkey, spanname, currentDateTime, appKeyTotalTraffic, appKeyTotalSuccessTraffic)

            //TODO天粒度数据不可用, 统计出的hostCount有问题
            val hostCount = getHostCount(localhostRecordsSpanNotAllOption.get, appkey, spanname, start, end)

            val isLoadBalance = getLoadBalance(Some(localhostRecords), hostCount, appKeyTotalTraffic)
            val avgHostQps = dailyQps / hostCount
            val maxQps = getMaxQps(appkey, spanname, maxStart, maxEnd, maxCountPoint)
            val minQps = getMinQps(appkey, spanname, minStart, minEnd, minCountPoint)

            //小时粒度的maxHostQps
            val allQpsList = localhostRecordsSpanNotAllOption.get.flatMap(_.qps).filter(c => c.y.nonEmpty && c.y.get != 0.0)

            val (maxHostQpsPoint, _) = getMaxAndMin(allQpsList)
            //分钟粒度的maxHostQps
            val maxHostQps = getMaxHostQps(appkey, spanname, maxStart, maxEnd, maxHostQpsPoint)

            //errorCount只统计到了appkey粒度
            val errorCount = if (spanname.equalsIgnoreCase("all")) ReportHelper.getDayErrorCount(appkey, start, end) else 0L

            val perfAlertCount = MonitorEvent.getEventCount(appkey, spanname, start * 1000L, end * 1000L)

            Some(ReportDailyDomain(owt, appkey, spanname, dailySpanData.getCount, successRadio, "NaN, NaN", dailyQps, maxQps, avgHostQps, maxHostQps,
              dailySpanData.getCost999.toInt, dailySpanData.getCost99.toInt, dailySpanData.getCost95.toInt, dailySpanData.getCost90.toInt,
              dailySpanData.getCost50.toInt, errorCount, perfAlertCount, isLoadBalance, currentDate, System.currentTimeMillis(), hostCount, minQps))

          } else {
            Some(ReportDailyDomain(owt, appkey, spanname, 0L, 0.0, "",
              0.0, 0.0, 0.0, 0.0,
              0, 0, 0, 0, 0, 0, 0, 0,
              currentDate, System.currentTimeMillis()))
          }
        } else {
          logger.error(s"localhostRecordsSpanAll non value: appkey $appkey")
          None
        }
      case None => None
    }
  }


  private def getIdcCount(dayOpt: Option[List[DataRecord]]) = {
    dayOpt.flatMap { records =>
      //  过滤掉localhost非法的情况
      val filterList = records.filterNot { record =>
        record.tags.localhost.isEmpty || record.tags.localhost.get.isEmpty || record.tags.localhost.get == "all"
      }
      //  根据idc分组
      val idcMap = filterList.groupBy { record =>
        CommonHelper.ip2IDC(record.tags.localhost.get)
      }
      val resList = idcMap.toSeq.map {
        case (idc, recordByIdc) =>
          //  对idc下多个localhost,分别求得localhost的traffic,求和
          val idcTraffic = recordByIdc.map {
            localhostRecord =>
              val localhostTraffic = localhostRecord.count.map {
                _.y.getOrElse(0D).toLong
              }.sum
              localhostTraffic
          }.sum
          val idcHostCount = recordByIdc.size
          //得到统计的结果
          (idcHostCount, idcTraffic)
      }

      if (resList.nonEmpty) {
        Some(resList)
      } else {
        None
      }
    }
  }

  private def getHostCount(nonAllRecords: List[DataQuery.DataRecord], appkey: String, spanname: String, start: Int, end: Int) = {
    //  分钟级别的host count数据不可靠,用离线天数据替换
    val flag = nonAllRecords.size == 1 || nonAllRecords.exists { item => item.tags.localhost.get == "0.0.0.0" }
    if (flag) {
      val dayResOption = DataQuery.getDataRecord(appkey, start, end, "thrift", "server", "", "prod", "Day", "spanLocalhost",
        spanname, "*", "all", "all", "hbase")
      dayResOption match {
        case Some(dayData) =>
          val (_, dayNonAll) = dayData.filterNot { record =>
            record.tags.localhost.isEmpty || record.tags.localhost.get.isEmpty
          }.partition(_.tags.localhost.get == Constants.ALL)
          if (dayNonAll.isEmpty) {
            //  小时及天都无法得出准确主机数
            1
          } else {
            dayNonAll.size
          }
        case None => nonAllRecords.size
      }
    } else {
      nonAllRecords.size
    }
  }

  private def getMaxAndMin(values: List[Point]) = {
    if (values.isEmpty) {
      (DataQuery.Point(Some(""), Some(0.0), Some(0)), DataQuery.Point(Some(""), Some(0.0), Some(0)))
    } else {
      var max = values.head
      var min = values.head
      values.foreach { value =>
        if (value.y.get > max.y.get) {
          max = value
        } else if (value.y.get < min.y.get) {
          min = value
        } else {
          ()
        }
      }
      (max, min)
    }
  }

  private def getSuccessRadio(appkey: String, _spanname: String, dateTime: DateTime, count: Double, successCount: Double) = {
    val availableCountOpt = AvailabilityDao.fetchAvailabilitySingle(appkey, _spanname, dateTime)
    val successRadio = if (availableCountOpt.isDefined) {
      availableCountOpt.get.successCountPer.getOrElse(BigDecimal(-1.0)).doubleValue()
    } else {
      -1.0
    }

    if (successRadio < 0) {
      if (successCount == 0) {
        100.0
      } else {
        successCount / count * 100
      }
    } else {
      successRadio
    }
  }

  private def getCount(appkey: String, spanname: String, start: Int, end: Int) = {
    val dataOpt = DataQuery.getDataRecord(appkey, start, end, "thrift", "server", "", "prod", "Minute", "spanLocalhost", spanname, localhost = Constants.ALL,
      "all", "all", "hbase")
    val countOpt = dataOpt.flatMap { list =>
      list.headOption.map { record =>
        record.count.filter(c => c.y.nonEmpty && c.y.get.toLong != 0).map(_.y.get)
      }
    }
    countOpt
  }

  private def getMaxQps(appkey: String, _spanname: String, start: Int, end: Int, max: DataQuery.Point) = {
    val countOpt = getCount(appkey, _spanname, start, end)
    val appKeyMinuteMaxQps = countOpt match {
      case Some(thisCount) => thisCount.max / Constants.ONE_MINUTE_SECONDS

      case None =>
        //  分钟级数据max计算错误时降级
        max.y.get / Constants.ONE_HOUR_SECONDS
    }
    appKeyMinuteMaxQps
  }

  private def getMinQps(appkey: String, _spanname: String, start: Int, end: Int, min: DataQuery.Point) = {
    val countOpt = getCount(appkey, _spanname, start, end)
    val appKeyMinuteQps = countOpt match {
      case Some(thisData) => thisData.min / Constants.ONE_MINUTE_SECONDS

      case None =>
        //  分钟级数据max计算错误时降级
        min.y.get / Constants.ONE_HOUR_SECONDS
    }
    appKeyMinuteQps
  }

  def getMaxHostQps(appkey: String, spanname: String, start: Int, end: Int, max: DataQuery.Point) = {
    val dataOpt = DataQuery.getDataRecord(appkey, start, end, "thrift", "server", "", "prod", "Minute", "spanLocalhost", spanname, "*",
      "all", "all", "hbase")
    val qpsOpt = dataOpt.flatMap { list =>
      Some(list.flatMap(_.qps))
    }
    val maxHostQps = qpsOpt match {
      case Some(qpsList) => if (qpsList.nonEmpty) {
        qpsList.map(_.y.getOrElse(0D)).max
      } else {
        0D
      }
      case None => max.y.getOrElse(0D)
    }
    maxHostQps
  }


  /**
    * 从tair中获取天粒度数据
    *
    * @param appkey
    * @param day
    * @return
    */
  private def getDailyData(appkey: String, day: DateTime) = {
    val dayOpt = DataQuery.getDailyStatistic(appkey, "prod", day)
    val result = dayOpt match {
      case Some(data) =>
        data
      case None =>
        logger.error(s"daily data is empty: $appkey")
        List(new DailyRecord).asJava
    }
    result.asScala.toList
  }

  private def getLoadBalance(drOption: Option[List[DataRecord]], hostCount: Int, appKeyTotalTraffic: Double) = {
    val idcCounts = getIdcCount(drOption)
    val idcRadio = idcCounts.map {
      item =>
        item.map {
          idcitem =>
            val hostRadio = idcitem._1.toDouble / hostCount
            val trafficRadio = idcitem._2.toDouble / appKeyTotalTraffic
            if (hostRadio / trafficRadio > 1.5f || trafficRadio / hostRadio > 1.5f) {
              false
            } else {
              true
            }
        }.toList
    }
    val falseCount = idcRadio.flatMap {
      list =>
        Some(list)
    }.getOrElse(List()).count(_.equals(false))

    if (falseCount > 0) {
      1
    } else {
      0
    }
  }


  /**
    * 若可用率计算失败,而日报数据计算完成,则调用此方法
    * 更新日报中对应的可用率数据
    *
    * @param ts
    * @return
    */
  def fillAvailability(ts: Long) = {
    Future {
      val dateTime = new DateTime(ts * 1000L)
      val dailySpans = AvailabilityDao.fetchDailySpans(ts)
      val dailySpansPar = dailySpans.par
      dailySpansPar.foreach {
        item =>
          val appkey = item.appkey
          val span = item.spanname
          val availableCountOpt = AvailabilityDao.fetchAvailabilitySingle(appkey, span, dateTime)
          if (availableCountOpt.isDefined) {
            val availability = availableCountOpt.get.successCountPer.getOrElse(BigDecimal(-1.0)).doubleValue()
            val day = new java.sql.Date(ts * 1000L)
            ReportDailyDao.updateAvailability(appkey, span, day, availability)
          }
      }
    }
  }
}

@DisallowConcurrentExecution
class ReportDailyJob extends Job {

  import ReportDailyTask.logger

  private implicit val timeout = Duration.create(30L, TimeUnit.SECONDS)

  @throws(classOf[JobExecutionException])
  override def execute(ctx: JobExecutionContext): Unit = {
    calculate()
  }

  /*  获取业务线 -> appkey list
    * 1:获取自己的ip,读取服务节点列表为启动的节点
    * 2：领取任务，算法为，获取服务提供者列表 count，找到自己的位置 index，
    * 3：from  count_appkey/count * index ，to: count_appkey/count * （index+1） -1
    * 4：读取列表，并更新 为 计算状态
    * 5:计算列表，计算成功后删除数据，
    * 6:如果是异常 设置为504
    */
  def calculate(timeMillis: Long = System.currentTimeMillis()): Unit = {
    val (start, end) = TaskTimeHelper.getYesterDayStartEnd(timeMillis)
    val day = new java.sql.Date(start * 1000L)
    logger.info(s"ReportDailyJob begin, From: $start, to: $end")
    val owtAppkeys = getAppkey(day)
    //先计算ALL接口数据, 保证报表能正常发送
    val calculateSpanAllBegin = System.currentTimeMillis()
    calculateAppkeys((start, end), day, owtAppkeys, Constants.ALL)
    logger.info(s"calculate all cost ${(System.currentTimeMillis() - calculateSpanAllBegin) / 1000}s")

    val calculateSpanNonAllBegin = System.currentTimeMillis()
    //再计算除了ALL接口之外的接口数据, 提供给用户查询
    calculateAppkeys((start, end), day, owtAppkeys, "")
    logger.info(s"calculate non-all cost ${(System.currentTimeMillis() - calculateSpanNonAllBegin) / 1000}s")
  }

  //计算多个appkey的日报
  def calculateAppkeys(startEnd: (Int, Int), day: java.sql.Date, owtappkeys: List[(String, String)], spanNameFlag: String) = {
    val calculateStart = System.currentTimeMillis()
    logger.info(s"ReportDailyJob owtappkeys size ${owtappkeys.size}")
    owtappkeys.foreach { case (owt, appkey) =>
      ReportDailyTask.computeOneReport(owt, appkey, spanNameFlag, startEnd._1, startEnd._2, day)
    }
    val calculateEnd = System.currentTimeMillis()
    logger.info(s"ReportDailyJob end, ${calculateEnd - calculateStart} Elapsed.")
  }

  def getAppkey(day: java.sql.Date) = {
    val count = ReportDailyStatusDAO.count(day)
    val list = getMsgpProviderList
    val (from, size) = getAppkeyRange(count, list)
    ReportDailyStatusDAO.search(from, size, day)
  }

  def getAppkeyRange(appkey_count: Int, msgp_provider: List[String]) = {
    val ip = ProcessInfoUtil.getLocalIpV4
    val host_count = msgp_provider.size
    val index = msgp_provider.indexOf(ip)
    if (index < 0) {
      (0, 0)
    } else {
      val page_size = appkey_count / host_count
      val from = appkey_count / host_count * index
      val size = if (index == host_count - 1) {
        appkey_count - from
      } else {
        page_size
      }
      (from, size)
    }
  }

  def getMsgpProviderList = {
    val list = AppkeyProviderService.getProviderByType("com.sankuai.inf.msgp", 2, "prod", "", 2, new Page(0, 100), -8)
    list.map(_.ip)
  }
}
