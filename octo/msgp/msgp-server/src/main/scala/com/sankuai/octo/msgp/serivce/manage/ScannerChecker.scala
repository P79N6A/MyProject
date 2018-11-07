package com.sankuai.octo.msgp.serivce.manage

import java.util.Date
import java.util.concurrent.{Executors, TimeUnit}

import com.sankuai.meituan.auth.vo.User
import com.sankuai.meituan.config.listener.IConfigChangeListener
import com.sankuai.msgp.common.config.db.msgp.Tables._
import com.sankuai.msgp.common.config.{DbConnection, MsgpConfig}
import com.sankuai.msgp.common.model.{EntityType, Env, Page}
import com.sankuai.msgp.common.utils.client.Messager.{Alarm, MODE, xm}
import com.sankuai.msgp.common.utils.client.{BorpClient, Messager, TairClient}
import com.sankuai.msgp.common.utils.helper.CommonHelper
import com.sankuai.octo.msgp.dao.report.ReportDailyDao
import com.sankuai.octo.msgp.dao.self.OctoJobDao
import com.sankuai.octo.msgp.serivce.service
import com.sankuai.octo.msgp.task.MonitorProviderTask
import com.sankuai.octo.statistic.util.ExecutorFactory
import org.joda.time.DateTime
import org.quartz._
import org.quartz.impl.StdSchedulerFactory
import org.slf4j.{Logger, LoggerFactory}
import play.api.libs.json.Json

import scala.collection.JavaConversions._
import scala.slick.driver.MySQLDriver.simple._

object ScannerChecker {
  private val LOG: Logger = LoggerFactory.getLogger(ScannerChecker.getClass)
  private val db = DbConnection.getPool()
  private val userId = -1024
  private val appkeys = List("com.sankuai.octo.scanner", "com.sankuai.inf.octo.scannermaster")

  //scanner扫描存在主从两个节点，当发生主从切换时会出现对两个节点上报的记录都报警的情况，这里需要排除对从节点的检查
  //mcc配置里保存了scanner各个主节点的ip，当scanner主节点发生变化时需要修改mcc配置项确保正确报警
  private val scannerMasterIpsKey = "scanner.master.ip"
  private var scannerMasterIps:List[String] = {
    MsgpConfig.addListener(scannerMasterIpsKey, new IConfigChangeListener {
      override def changed(key: String, oldValue: String, newValue: String): Unit = {
        scannerMasterIps = newValue.split(",").toList
      }
    })

    MsgpConfig.get(scannerMasterIpsKey, "10.4.213.136,10.69.32.65,10.32.61.170,10.12.74.72").split(",").toList
  }

  case class SLog(appkey: String, time: Long, level: Int, category: String, content: String, identifier: String,
                  roundTime: Option[Int], providerCount: Option[Int], weight: Option[Int], newWeight: Option[Int],
                  status: Option[Int], newStatus: Option[Int])

  implicit val logReads = Json.reads[SLog]
  implicit val logWrites = Json.writes[SLog]

  //用于消费日志队列的线程
  val exe = ExecutorFactory(doJob, "scannerChecker-job", 4, 20, 20000)

  //用于scanner报警的executor
  val monitorExe = Executors.newScheduledThreadPool(1)

  def ownerIdList = service.ServiceCommon.desc(appkeys(1)).owners.map(x => x.id).distinct

  /** appkeyNotify用来记录“appkey+side+item”对应的历史报警频率信息 */
  val appkeyNotify = scala.collection.concurrent.TrieMap[String, NotifyInf]()

  /**
    * 上报数据异常时间统计
    */
  private var alarmBeginTime: Long = 0
  private var isDuringAlarmPeriod: Boolean = false
  private val abnormalTimeOfScanner = "abnormalTimeOfScanner"
  private val dailyAbnormalTimeOfScanner = "dailyAbnormalTimeOfScanner"
  private val DAY_TIME: Long = 86400000l
  private val ratioFormat = "%.4f"
  private val availabilityAppkey = List("com.sankuai.inf.octo.scannerdetector", "com.sankuai.inf.octo.scannerupdater")
  private val scheduler = Executors.newSingleThreadScheduledExecutor()
  private val cronScheduler = new StdSchedulerFactory().getScheduler
  private var scannerAlarm: Boolean = {
    MsgpConfig.addListener("msgp.scanner.alarm", new IConfigChangeListener {
      override def changed(key: String, oldValue: String, newValue: String): Unit = {
        scannerAlarm = "true".equals(newValue)
      }
    })
    val valueInMcc = MsgpConfig.get("msgp.scanner.alarm", "true")
    "true".equals(valueInMcc)
  }

  /** NotifyInf报警频率的类，
    * count：已报警次数，
    * firstTimestamp：连续报警区间的首个报警的时间，
    * lastTimestamp：上一次报警的时间 */
  case class NotifyInf(var count: Int, var firstTimestamp: Long, var lastTimestamp: Long)

  def report(text: String): Unit = {
    exe.submit(text)
  }

  def doJob(text: String): Unit = {
    val logData = doReport(text)
    if (logData != null) {
      //线上更新数据库
      if(!CommonHelper.isOffline){
        inOrUpLog(List(logData))
      }
    }
  }

  def doReport(text: String) = {
    try {
      val jsonStr = Json.parse(text)
      val logData = jsonStr.validate[SLog].get
      LOG.debug(s"ScannerReport value is $logData")
      var result: ScannerLogRow = null
      val category = logData.category
      category match {
        //任务调度 qps低直接插入
        case "RoundTime" =>
          val cost = logData.roundTime.get
          OctoJobDao.inOrUpJob(OctoJobRow(0, logData.appkey, logData.identifier, "RoundTime", logData.time, cost, logData.content))

        case "RoundTimeSlowProvider" =>
          val cost = logData.roundTime.get
          val providerCount = logData.providerCount.get
          OctoJobDao.inOrUpJob(OctoJobRow(0, logData.appkey, logData.identifier, "RoundTimeSlowProvider", logData.time, cost, providerCount.toString))

        //borp 内部实现为异步，直接处理保证实时性
        case "UpdateWeight" | "UpdateStatus" | "DeleteProvider" =>
          val identifier = logData.identifier.split("\\|")
          LOG.debug(identifier.toString)
          val env = identifier.apply(0).split(":").apply(1).toInt
          val appkey = identifier.apply(1).split(":").apply(1)
          val ip = identifier.apply(2).split(":").apply(1)
          val port = identifier.apply(3).split(":").apply(1)
          val fieldName = try {
            s"${Env.apply(env).toString} $ip:$port"
          } catch {
            case e: Exception => "unknown"
          }
          LOG.debug(s"scanner log fieldName: $fieldName")
          val user = new User() //标记特殊的操作者scanner
          user.setId(userId)
          user.setName(appkey)

          if (category == "UpdateWeight") {
            val oldValue = logData.weight.get.toString
            val newValue = logData.newWeight.get.toString
            BorpClient.saveOpt(user = user, actionType = 2, entityId = appkey, entityType = EntityType.updateWeight, fieldName = fieldName, oldValue = oldValue, newValue = newValue)
          } else if (category == "UpdateStatus") {
            val oldValue = logData.status.get.toString
            val newValue = logData.newStatus.get.toString
            BorpClient.saveOpt(user = user, actionType = 2, entityId = appkey, entityType = EntityType.updateStatus, fieldName = fieldName, oldValue = oldValue, newValue = newValue)
            MonitorProviderTask.eventNotify(logData)
          } else if (category == "DeleteProvider") {
            BorpClient.saveOpt(user = user, actionType = 2, entityId = appkey, entityType = EntityType.deleteProvider, fieldName = fieldName, oldValue = "", newValue = "")
          }

        //区分种类存储scanner日志
        case "DuplicateRegistry" =>
          val provider = logData.identifier
          val content = logData.content
          if (checkHowNotify(provider) && content.contains("com.sankuai.inf.sg_agent")) {
            val message = "节点重复注册：" + logData.content
            xm.send(Seq("yangjie17@meituan.com", "shuchao02@meituan.com", "huixiangbo@meituan.com"), message)
          }
          //          }
          result = ScannerLogRow(0, logData.appkey, 0, provider, category, logData.content, logData.time)

        case "ConnectSlow" | "ConnectFailed" =>
          val identifier = logData.identifier.split("\\|")
          val env = identifier.apply(0).split(":").apply(1).toInt
          val appkey = identifier.apply(1).split(":").apply(1)
          val ip = identifier.apply(2).split(":").apply(1)
          val port = identifier.apply(3).split(":").apply(1)
          val provider = s"$ip:$port"
          result = ScannerLogRow(0, appkey, env, provider, category, logData.content, logData.time)
      }
      result
    } catch {
      case e: Exception => LOG.error(s"insertLog error $e text", e); null
    }
  }

  def startScannerCheckerTask() = {
    startScannerMonitor()
    startCountAbnormalTime()
  }

  def getScannerAvailability = {
    val lastDate = new java.sql.Date(new DateTime(System.currentTimeMillis()).withTimeAtStartOfDay().minusDays(1).getMillis)
    val otherSuccessRatio = availabilityAppkey.map {
      appkey =>
        val dataOpt = ReportDailyDao.getSpanData(appkey, "all", lastDate)
        if (dataOpt.isEmpty) {
          100.0000
        } else {
          dataOpt.get.successRatio.getOrElse(BigDecimal(100.0000)).doubleValue()
        }
    }
    val abnormalTimeOfScannerCount = TairClient.get(dailyAbnormalTimeOfScanner).getOrElse("0").toLong
    val masterSuccessRatio = ((DAY_TIME - abnormalTimeOfScannerCount).toDouble / DAY_TIME) * 100
    val successRatio = masterSuccessRatio +: otherSuccessRatio
    val scannerAvailability = s"${ratioFormat.format(successRatio.product / Math.pow(100, successRatio.length - 1))}%"
    scannerAvailability +: successRatio.map(x => s"${ratioFormat.format(x)}%")
  }

  def startCountAbnormalTime() = {
    val job: JobDetail = JobBuilder.newJob(classOf[updateAbnormalTimeWorker]).build()
    val trigger: Trigger = TriggerBuilder.newTrigger().startNow()
      .withSchedule(CronScheduleBuilder.dailyAtHourAndMinute(0, 30)).build()
    try {
      cronScheduler.scheduleJob(job, trigger)
    } catch {
      case e: Exception => LOG.info(s"Schedule CountAbnormalTime Fail,job:$job,trigger:$trigger")
    }
    Runtime.getRuntime.addShutdownHook(new Thread() {
      override def run(): Unit = {
        scheduler.shutdown()
        scheduler.awaitTermination(10, TimeUnit.SECONDS)
      }
    })
  }

  def updateAbnormalTime() = {
    val oldValue = TairClient.get(abnormalTimeOfScanner).getOrElse("0").toLong
    TairClient.put(abnormalTimeOfScanner, "0")
    TairClient.put(dailyAbnormalTimeOfScanner, s"$oldValue")
  }

  //每隔1分钟扫描scanner roundTime日志，发现scanner停止上报之后，报警
  def startScannerMonitor() = {
    monitorExe.scheduleAtFixedRate(new Runnable {
      override def run(): Unit = {
        //获取roundTime日志
        //根据时间戳判断是否需要报警
        //TODO 全局锁
        val result = getJobLog(appkeys)
        val now = new DateTime().getMillis
        val timeLimit = 1000 * 3 * 60
        result.filter(x => x.job == "RoundTime")
            .filter(x => scannerMasterIps.contains(x.identifier)).foreach {
          x =>
            if (now - x.stime > timeLimit) {
              //报警
              if (!isDuringAlarmPeriod) {
                //首次发现上报异常
                isDuringAlarmPeriod = true
                alarmBeginTime = now
              }

              val id = x.identifier + x.job
              val modes = Seq(MODE.XM)
              if (checkHowNotify(id) && scannerAlarm) {
                //以provider作为唯一命名
                val message = "scanner停止扫描：" + id
                val alarm = Alarm("", message, null)
                Messager.sendAlarm(ownerIdList, alarm, modes)
              }
            } else {
              if (isDuringAlarmPeriod) {
                //上报异常已解除
                val oldValue = TairClient.get(abnormalTimeOfScanner).getOrElse("0").toLong
                val newValue = now - alarmBeginTime + oldValue
                TairClient.put(abnormalTimeOfScanner, s"$newValue")
                isDuringAlarmPeriod = false
              }
            }
        }
      }
    }, 10, 60, TimeUnit.SECONDS)
  }

  def checkHowNotify(name: String) = {
    var ifNotify = false
    val now = System.currentTimeMillis()
    if (appkeyNotify.get(name).isEmpty) {
      ifNotify = true
      appkeyNotify.update(name, NotifyInf(1, now, now))
    }
    else {
      val notifyInf = appkeyNotify(name)
      /** 根据limit判断是否要发送报警 */
      def checkIfNotify(limit: Long) = {
        if (notifyInf.count < limit) {
          notifyInf.count = notifyInf.count + 1
          true
        } else {
          false
        }
      }
      val limit = 3
      /** 记录报警区间长度，仅当本次报警处于上一个连续报警区间才有意义 */
      val nowFirst = now - notifyInf.firstTimestamp
      /** 记录当前报警与上一个连续报警区间最后一次报警的时间差 */
      val nowLast = now - notifyInf.lastTimestamp
      val fiveMin: Long = 5 * 60 * 1000
      val tenMin: Long = 10 * 60 * 1000
      val thirtyMin: Long = 30 * 60 * 1000

      /** 两次报警时间相差不超过30分钟时，认为两个报警处于一个连续的报警区间 */
      if (nowLast < thirtyMin) {
        //属于连续报警区间
        if (nowFirst <= fiveMin) {
          //前5分钟最多报警3次
          ifNotify = checkIfNotify(limit)
        }
        else if (nowFirst <= tenMin) {
          //前10分钟最多报警5次
          ifNotify = checkIfNotify(limit + 2)
        }
        else if (nowFirst <= thirtyMin) {
          //前30分钟最多报警6次
          ifNotify = checkIfNotify(limit + 3)
        }
        else {
          //连续报警超过30分钟后，报警频率趋于1/30min
          ifNotify = checkIfNotify(limit + 3 + nowFirst / thirtyMin)
        }
      }
      else {
        //不属于连续报警区间
        ifNotify = true
        notifyInf.count = 1
        notifyInf.firstTimestamp = now
      }

      /** 不论何种情况最后都要更新 */
      notifyInf.lastTimestamp = now
    }
    ifNotify
  }

  //insert or update scanner provider status
  def inOrUpLog(values: List[ScannerLogRow]) = {
    db withSession {
      implicit session: Session =>
        values.foreach {
          value =>
            val statement = ScannerLog.filter(self =>
              self.appkey === value.appkey &&
                self.env === value.env &&
                self.provider === value.provider &&
                self.category === value.category)
            if (statement.exists.run) {
              //update
              statement.map(self => (self.content, self.time)).update(value.content, value.time)
            } else {
              //insert
              ScannerLog += value
            }
        }
    }
  }

  def getJobLog(appkeys: List[String]) = {
    //job运行情况
    db withSession {
      implicit session: Session =>
        val list = OctoJob.filter(self => self.appkey inSet appkeys).list
        list
    }
  }

  def getJobLog(appkeys: java.util.List[String]) = {
    //job运行情况
    db withSession {
      implicit session: Session =>
        val list = OctoJob.filter(self => self.appkey inSet appkeys).list
        list
    }
  }

  def getSLog(page: Page) = {
    //最近的一百条记录
    val count = 100
    val limit = page.getPageSize
    val offset = page.getStart
    db withSession {
      implicit session: Session =>
        val list = ScannerLog.sortBy(x => x.time.desc).take(count).list
        page.setTotalCount(list.length)
        list.slice(offset, offset + limit)
    }
  }

  def getBorpLog(page: Page) = {
    //最近2分钟内的操作
    val now = new DateTime()
    val end = new Date(now.getMillis)
    val start = new Date(now.minusMinutes(2).getMillis)
    BorpClient.getOptLogByOperatorId(userId.toString, start, end, page)
  }

  class updateAbnormalTimeWorker extends Job {
    @throws(classOf[JobExecutionException])
    override def execute(ctx: JobExecutionContext) {
      ScannerChecker.updateAbnormalTime()
    }
  }

}