package com.sankuai.octo.msgp.serivce.data

import java.util.concurrent.{Executors, TimeUnit}

import com.sankuai.msgp.common.config.db.msgp.Tables._
import com.sankuai.msgp.common.model.Env
import com.sankuai.octo.msgp.dao.appkey.AppkeyDescDao
import com.sankuai.octo.msgp.dao.perf.PerfDayDao
import com.sankuai.octo.msgp.dao.self.OctoJobDao
import com.sankuai.octo.msgp.model.MScheduler
import com.sankuai.octo.msgp.serivce.service.ServiceDesc
import org.joda.time.DateTime
import org.quartz._
import org.quartz.impl.StdSchedulerFactory
import org.slf4j.{Logger, LoggerFactory}

import scala.collection.JavaConverters._
import scala.collection.parallel.ForkJoinTaskSupport
import scala.concurrent.{ExecutionContext, Future}

object Kpi {
  private val LOG: Logger = LoggerFactory.getLogger(Kpi.getClass)

  private val mode = "kpi"
  private val tags = "spanname:*,localhost:all"
  private val scheduler = Executors.newScheduledThreadPool(2)

  private implicit val ec = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(2))

  private val threadPool = new ForkJoinTaskSupport(new scala.concurrent.forkjoin.ForkJoinPool(4))

  // data-query产出tags
  def spannameList(appkey: String, source: String = "server", env: String = Env.prod.toString) = {
    //查找过去两天接口的聚合
    val start: Int = (new DateTime().minusDays(2).getMillis / 1000).toInt
    val end: Int = (new DateTime().getMillis / 1000).toInt
    val tags = DataQuery.tags(appkey, start, end, env, source)
    tags.spannameList.asJava
  }

  def syncDay2(apps: java.util.List[String], dateTime: DateTime) = {
    syncDay(apps.asScala.toList, dateTime)
  }

  def syncDay(apps: List[String], dateTime: DateTime) = {
    val appList = if (apps == null || apps.isEmpty) {
      ServiceDesc.appsName()
    } else {
      apps
    }
    Future {
      val appPar = appList.par
      appPar.tasksupport = threadPool
      appPar.foreach {
        key =>
          syncDailyKpi(key, "prod", dateTime)
      }
    }
  }


  @DisallowConcurrentExecution
  class CrontabUpdateJob extends Job {
    private val logger = LoggerFactory.getLogger(this.getClass)

    @throws(classOf[JobExecutionException])
    override def execute(ctx: JobExecutionContext) {
      logger.info("data CrontabUpdateJob Start")
      // 取得昨天的时间
      val now = DateTime.now()
      val startOfYesterday = now.minusDays(1).withTimeAtStartOfDay()
      syncDay(ServiceDesc.appsName(), startOfYesterday)
    }
  }

  // 每天02:00计算client端可用率到数据库中
  def startCrontab = {
    try {
      val cronScheduler = new StdSchedulerFactory().getScheduler
      cronScheduler.start()
      val job = JobBuilder.newJob(classOf[CrontabUpdateJob]).build()
      val trigger = TriggerBuilder.newTrigger().startNow()
        .withSchedule(CronScheduleBuilder.dailyAtHourAndMinute(0, 30)).build()
      cronScheduler.scheduleJob(job, trigger)
      // 在jvm退出时优雅关闭
      Runtime.getRuntime.addShutdownHook(new Thread() {
        override def run(): Unit = {
          cronScheduler.shutdown(true)
        }
      })
    } catch {
      case e: Exception => LOG.error(s"data startCrontab failed $e")
    }
  }

  // 定时同步天粒度kpi数据
  def startSyncDay = {
    val now = System.currentTimeMillis() / 1000
    val init = 60 - (now % 60)
    LOG.info(s"init perf on $now with delay $init")
    scheduler.scheduleAtFixedRate(new Runnable {
      def run(): Unit = {
        val start = DateTime.now()
        val dateTime = start.withTimeAtStartOfDay()
        syncPerfDay(dateTime)
        val end = DateTime.now()
        OctoJobDao.insertCost(SchedulerCostRow(0, MScheduler.dataSyncSchedule.toString, start.getMillis, end.getMillis))
      }
    }, init, 30 * 60, TimeUnit.SECONDS)
  }

  def syncPerfDay(dateTime: DateTime) = {
    syncDay(ServiceDesc.appsName(), dateTime)
  }

  // 将数据中心的天粒度数据同步到perf_day表中
  def syncDailyKpi(appkey: String, env: String, dateTime: DateTime) = {
    val resultOpt = DataQuery.getDailyStatistic(appkey, env, dateTime)
    if (resultOpt.isDefined) {
      val ts = (dateTime.getMillis / 1000).toInt
      val ret = resultOpt.get.asScala.map {
        x =>
          val tags = x.getTags
          val appkeyCategory = AppkeyDescDao.getCategory(appkey)
          val tag_spanname = tags.getSpanname
          val spanname = if (tag_spanname.length > 255) {
            tag_spanname.substring(0, 255)
          } else {
            tag_spanname
          }
          PerfDayRow(0L, ts, mode, this.tags, appkey, appkeyCategory, spanname, "all", "all", "all", 0,
            x.getCount, Some(x.getQps), Some(x.getCost50), Some(x.getCost90), Some(x.getCost95), Some(x.getCost99), Some(x.getCostMax))
      }.toList
      PerfDayDao.updateOrInsertPerfDay(ret)
    } else {
      LOG.error(s"syncDailyKpi parse dailyKpi failed,")
    }
  }


  def startJob = {
    startCrontab
    startSyncDay
  }
}
