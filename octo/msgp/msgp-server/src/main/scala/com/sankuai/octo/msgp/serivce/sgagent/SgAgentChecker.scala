package com.sankuai.octo.msgp.serivce.sgagent

import java.util.Arrays
import java.util.concurrent.{Callable, Executors, TimeUnit}

import com.alibaba.fastjson.{JSON, JSONArray, JSONObject}
import com.google.common.cache.{CacheBuilder, CacheLoader}
import com.google.common.util.concurrent.ListenableFutureTask
import com.sankuai.inf.octo.mns.util.ProcessInfoUtil
import com.sankuai.msgp.common.config.db.msgp.Tables
import com.sankuai.msgp.common.model.{Env, Page, ServiceModels}
import com.sankuai.msgp.common.utils.helper.{CommonHelper, JsonHelper}
import com.sankuai.octo.config.model.{ConfigFile, ConfigGroups, file_param_t}
import com.sankuai.octo.msgp.serivce.graph.ServiceModel.AppPerf
import com.sankuai.octo.msgp.serivce.service.AppkeyProviderService
import com.sankuai.octo.msgp.serivce.service.ServiceCommon
import com.sankuai.octo.msgp.dao.appkey.AppkeyProviderDao
import com.sankuai.msgp.common.config.db.msgp.Tables.SchedulerCostRow
import com.sankuai.msgp.common.service.org.OpsService
import com.sankuai.msgp.common.utils.client.{Messager, TairClient}
import com.sankuai.octo.msgp.dao.self.{HttpAuthDao, OctoJobDao}
import com.sankuai.octo.msgp.model.{Echart, MScheduler}
import com.sankuai.msgp.common.utils.client.Messager.{Alarm, MODE}
import com.sankuai.octo.msgp.serivce.data.DataQuery.{getDailyStatisticFormatted}

import com.sankuai.octo.msgp.utils.client.MnsCacheClient
import com.sankuai.octo.statistic.util.ExecutionContextFactory
import com.sankuai.sgagent.thrift.model.{SGAgent, proc_conf_param_t}
import dispatch.{Http, as, url, _}
import org.apache.commons.lang3.StringUtils
import org.apache.thrift.protocol.{TBinaryProtocol, TProtocol}
import org.apache.thrift.transport.{TFramedTransport, TSocket, TTransport}
import org.joda.time.{DateTime, LocalDate}
import org.slf4j.{Logger, LoggerFactory}
import play.api.libs.json.{Json}
import java.text.DateFormat
import java.text.SimpleDateFormat

import scala.collection.JavaConverters._
import scala.collection.mutable.ListBuffer
import scala.collection.parallel.ForkJoinTaskSupport
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, duration}

object SgAgentChecker {
  private val LOG: Logger = LoggerFactory.getLogger(SgAgentChecker.getClass)
  private val appkey = "com.sankuai.inf.sg_agent"

  case class ProvideGroupByVersion(version: String, providerList: List[ServiceModels.ProviderNode])

  //一个用来执行sg_agent重复注册,一个用来执行刷新缓存,其他用来reload中任务调度
  private val scheduler = Executors.newScheduledThreadPool(6)
  private val expiredTime = 10
  private implicit val ec = ExecutionContextFactory.build(Runtime.getRuntime.availableProcessors)

  private val threadPool = new ForkJoinTaskSupport(new scala.concurrent.forkjoin.ForkJoinPool(16))

  private implicit val timeout = Duration.create(60L, duration.SECONDS)

  private val sgAgentCheckUrl = "http://sgagent.sankuai.com/check"

  private val sgAgentRepairUrl = if (CommonHelper.isOffline) {
    "http://sgagent.office.sankuai.com/repair"
  } else {
    "http://sgagent.sankuai.com/repair"
  }
  val SG_AGENT_PIE = "mspg_sgagent_pie_data"

  private val sgAgentProviderDesc = CacheBuilder
    .newBuilder()
    .expireAfterWrite(expiredTime, TimeUnit.MINUTES)
    .build(
      new CacheLoader[(String, Int), List[ServiceModels.ProviderNode]]() {
        override def load(key: (String, Int)) = {
          providerDesc(key._1, key._2)
        }

        //供refresh调用
        override def reload(key: (String, Int), value: List[ServiceModels.ProviderNode]) = {
          //异步刷新
          val task = ListenableFutureTask.create(new Callable[List[ServiceModels.ProviderNode]]() {
            def call() = {
              providerDesc(key._1, key._2)
            }
          })
          scheduler.execute(task)
          task
        }
      }
    )

  /** appkeyNotify用来记录“appkey+side+item”对应的历史报警频率信息 */
  val appkeyNotify = scala.collection.mutable.Map[String, NotifyInf]()

  /** NotifyInf报警频率的类，
    * count：已报警次数，
    * firstTimestamp：连续报警区间的首个报警的时间，
    * lastTimestamp：上一次报警的时间 */
  case class NotifyInf(var count: Int, var firstTimestamp: Long, var lastTimestamp: Long)

  def getProvide(appkey: String, envId: Int) = {
    sgAgentProviderDesc.get((appkey, envId))
  }

  def providerDesc(appkey: String, envId: Int) = {
    val list = envId match {
      case 0 =>
        AppkeyProviderService.provider(appkey)
      case _ =>
        AppkeyProviderService.provider(appkey, envId)
    }
    list
  }

  def startScanner() = {
    //启动1分钟一次的sg_agent重复注册
    scheduler.scheduleAtFixedRate(new Runnable {
      def run(): Unit = {
        try {
          val start = DateTime.now
          SGAProviderCheck()
          val end = DateTime.now
          OctoJobDao.insertCost(SchedulerCostRow(0, MScheduler.sgAgentScannerSchedule.toString, start.getMillis, end.getMillis))
        } catch {
          case e: Exception => LOG.error(s"SGAProviderCheck fail $e")
        }
      }
    }, 10, 60, TimeUnit.SECONDS)

    //启动5分钟一次的sg_agent刷新
    scheduler.scheduleAtFixedRate(new Runnable {
      override def run(): Unit = {
        try {
          val start = DateTime.now()
          sgAgentProviderDesc.refresh((appkey, 3))
          sgAgentProviderDesc.refresh((appkey, 2))
          sgAgentProviderDesc.refresh((appkey, 1))
          val end = DateTime.now()
          //insert monitorCost
          OctoJobDao.insertCost(SchedulerCostRow(0, MScheduler.sgAgentProviderRefresh.toString, start.getMillis, end.getMillis))
        } catch {
          case e: Exception => LOG.error(s"SGAProviderCheck fail $e")
        }
      }

    }, 10, 300, TimeUnit.SECONDS)
  }

  //定时扫描sg_agent环境，观察有无节点同时在stage/test/prod环境
  def SGAProviderCheck(appkey: String = "com.sankuai.inf.sg_agent") = {
    LOG.info("begin SGAProviderCheck")
    val test = AppkeyProviderService.providerNode(appkey, Env.test.id).toSet
    val stage = AppkeyProviderService.providerNode(appkey, Env.stage.id).toSet
    val prod = AppkeyProviderService.providerNode(appkey, Env.prod.id).toSet

    val testAndStage = test & stage
    val testAndProd = test & prod
    val stageAndProd = stage & prod

    val ownerIdList = (ServiceCommon.desc(appkey).owners.map(x => x.id) ++ List(41081)).distinct
    val subject = "sg_agent节点重复注册"
    val modes = Seq(MODE.XM)

    if (!testAndStage.isEmpty) {
      val name = "sg_agent_test&stage"
      if (checkHowNotify(name)) {
        val message = "在test和stage重复注册：" + testAndStage.mkString(" ")
        val alarm = Alarm(s"octo报警：$subject", message, null)
        Messager.sendAlarm(ownerIdList, alarm, modes)
      }
    }
    if (!testAndProd.isEmpty) {
      val name = "sg_agent_test&prod"
      if (checkHowNotify(name)) {
        val message = "在test和prod重复注册：" + testAndProd.mkString(" ")
        val alarm = Alarm(s"octo报警：$subject", message, null)
        Messager.sendAlarm(ownerIdList, alarm, modes)
      }
    }
    if (!stageAndProd.isEmpty) {
      val name = "sg_agent_stage&prod"
      if (checkHowNotify(name)) {
        val message = "在stage和prod重复注册：" + stageAndProd.mkString(" ")
        val alarm = Alarm(s"octo报警：$subject", message, null)
        Messager.sendAlarm(ownerIdList, alarm, modes)
      }
    }
    LOG.info(s"finish SGAProviderCheck")
  }

  def SGAVersionCheck(appkey: String, envId: Int, region: String) = {
    val cacheKey = getCheckKey(appkey, envId, region)
    val cacheData = TairClient.get(cacheKey).getOrElse("")
    if (StringUtils.isNotBlank(cacheData)) {
      Json.parse(cacheData).as[Echart.Pie]
    } else {
      checkSGVersion(appkey, envId, region)
    }
  }

  def checkSGVersion(appkey: String, envId: Int, region: String) = {
    //获取sgAgent所有的服务节点
    val list = try {
      val mnsc = MnsCacheClient.getInstance
      val mnscRet = mnsc.getMNSCache(appkey, "", Env(envId).toString)
      val listTemp = if (com.sankuai.octo.mnsc.idl.thrift.model.Constants.SUCCESS == mnscRet.code) {
        val dataPar = mnscRet.defaultMNSCache.asScala.par
        dataPar.tasksupport = threadPool
        dataPar.map {
          x =>
            ServiceModels.SGService2ProviderNode(x)
        }.toList
      } else {
        List[ServiceModels.ProviderNode]()
      }
      filterSGAgentListByRegion(listTemp, region)
    } catch {
      case e: Exception =>
        LOG.error("Fail to get the detail of the version information.", e)
        List[ServiceModels.ProviderNode]()
    }
    //构造饼状图数据
    val pieSeriesData = list.groupBy(_.version).map(x => Echart.PieSeriesData(x._1, x._2.length)).toList
    val pieDdata = Echart.Pie(pieSeriesData.map(_.name).sortWith(_ < _), pieSeriesData)
    val jsonData = JsonHelper.jsonStr(pieDdata)
    val cacheKey = getCheckKey(appkey, envId, region)
    TairClient.put(cacheKey, jsonData, 600)
    pieDdata
  }

  private def getCheckKey(appkey: String, envId: Int, region: String) = {
    s"$SG_AGENT_PIE-$appkey-$envId-$region"
  }

  private def filterSGAgentListByRegion(list: List[ServiceModels.ProviderNode], region: String) = {
    try {
      if ("hulk".equalsIgnoreCase(region)) {
        list.filter(x => OpsService.ipToHost(x.ip).startsWith("set-"))
      } else if ("shanghai".equalsIgnoreCase(region) || "beijing".equalsIgnoreCase(region)) {
        val regionInfo = ProcessInfoUtil.getIdcInfo(list.map(_.ip).filter(CommonHelper.checkIP).asJava)
        list.filter { x =>
          val isHulk = OpsService.ipToHost(x.ip).startsWith("set-")
          !isHulk
        }.filter {
          x =>
            val idc = regionInfo.get(x.ip)
            if (null == idc) {
              false
            } else {
              region.equalsIgnoreCase(idc.region)
            }
        }
      } else {
        list
      }
    } catch {
      case e: Exception => list
    }
  }



  def provideGroupByVersion(appkey: String, envId: Int, version: String, region: String) = {
    val list = AppkeyProviderDao.searchByVersion(appkey,envId,version)
    val data = list.map { node =>
      ServiceModels.ProviderNode(Some(""), Some(node.hostname), node.appkey, node.version,
        node.ip, node.port, node.weight, Some(node.fweight), node.status, Some(node.enabled),
        node.role, node.env, node.lastupdatetime, node.extend,
        Some(node.servertype), Some(node.protocol),Some(""),Some(node.swimlane),Some(""),
        None, Some(node.heartbeatsupport))
    }
    val filterList = filterSGAgentListByRegion(data, region)
    ProvideGroupByVersion(version, filterList)
  }

  def checkHowNotify(name: String) = {
    var ifNotify = false
    val now = System.currentTimeMillis()
    if (appkeyNotify.get(name) == None) {
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


  def getMccFileData(appkey: String, ip: String, fileName: String) = {
    var transport: TTransport = null
    try {
      val timeout = 3000
      transport = new TFramedTransport(new TSocket(ip, 5266, timeout), 16384000)
      val protocol: TProtocol = new TBinaryProtocol(transport)
      val agent = new SGAgent.Client(protocol)
      transport.open

      val fileParam = new file_param_t
      fileParam.setAppkey(appkey)

      val configFileAsParam = new ConfigFile
      configFileAsParam.setFilename(fileName)

      fileParam.setConfigFiles(Arrays.asList(configFileAsParam))

      val fileParamAsResult = agent.getFileConfig(fileParam)
      if (null == fileParamAsResult || fileParamAsResult.getConfigFiles.isEmpty) {
        JsonHelper.dataJson("获取失败，没有该文件")
      } else {
        val files = fileParamAsResult.getConfigFiles
        val file = files.asScala.filter(_.filename.equals(fileName)).map(_.getFilecontent)
        JsonHelper.dataJson(new String(file(0)))
      }
    } catch {
      case e: Exception =>
        JsonHelper.errorJson(s"获取失败")
    } finally {
      if (null != transport) {
        try {
          transport.close()
        } catch {
          case e: Exception => LOG.error(s"Failed to close $ip thrift connection", e)
            JsonHelper.errorJson(JsonHelper.errorJson(s"获取失败"))
        }
      }
    }
  }


  def getMccDynamicData(appkey: String, ip: String, path: String, swimlane: String) = {
    import scala.util.parsing.json.{JSON, JSONObject}
    var transport: TTransport = null
    try {
      val timeout = 3000
      transport = new TFramedTransport(new TSocket(ip, 5266, timeout), 16384000)
      val protocol: TProtocol = new TBinaryProtocol(transport)
      val agent = new SGAgent.Client(protocol)
      transport.open

      val request_param = new proc_conf_param_t()
      request_param.setAppkey(appkey)
      request_param.setEnv("")
      request_param.setPath(path)
      if (!StringUtils.isEmpty(swimlane)) {
        request_param.setSwimlane(swimlane)
      }

      val res = agent.getConfig(request_param)//
      JSON.parseFull(res) match {
        case Some(map: Map[String, Any]) => {
          val dataMap = map.get("data")
          if (null != dataMap) {
            JsonHelper.jsonStr(dataMap)
          } else {
            "没有数据"
          }
        }
        case _ => "获取失败"
      }
    } catch {
      case e: Exception =>
        LOG.warn(s"Failed to get mcc dynamic data from $ip", e)
        JsonHelper.errorJson(s"获取失败")
    } finally {
      if (null != transport) {
        try {
          transport.close()
        } catch {
          case e: Exception => LOG.error(s"Failed to close $ip thrift connection", e)
            JsonHelper.errorJson(JsonHelper.errorJson(s"Failed to get mcc dynamic data from $ip ,${e.getMessage()}"))
        }
      }
    }
  }


  def sgagentHealthCheck(ip: String) = {
    val urlStr = s"$sgAgentCheckUrl/$ip"
    val getReq = url(urlStr)
    val feature = Http(getReq > as.String)
    val content = Await.result(feature, timeout)
    content
  }

  def sgagentInstall(ip: String) = {
    val urlStr = s"$sgAgentRepairUrl/$ip/install"
    val getReq = url(urlStr)
    val feature = Http(getReq > as.String)
    val content = Await.result(feature, timeout)
    content
  }

  def sgagentReinstall(ip: String) = {
    val urlStr = s"$sgAgentRepairUrl/$ip/reinstall"
    val getReq = url(urlStr)
    val feature = Http(getReq > as.String)
    val content = Await.result(feature, timeout)
    content
  }

  def sgagentRestart(ip: String) = {
    val urlStr = s"$sgAgentRepairUrl/$ip/restart"
    val getReq = url(urlStr)
    val feature = Http(getReq > as.String)
    val content = Await.result(feature, timeout)
    content
  }

  case class DailyAvailabilityAllData(dates: List[String], httpSuccessCountPercent: Double, thriftSuccessCountPercent: Double, successCountPercent:Double,
                                      httpAvailabilityBasedClient: List[String], thriftAvailabilityBasedClient: List[String], availabilityBasedClient: List[String])

  def getDailyAvailability(appkey: String, serverType: String) = {
    val endTime = LocalDate.now()
    val startTime = endTime.minusDays(31)
    val dates = ListBuffer[String]()
    dates.append(startTime.toString())
    var sTime = startTime
    for (a <- 1 to 30) {
      val oneDayAfter = sTime.plusDays(1)
      dates.append(oneDayAfter.toString())
      sTime = oneDayAfter
    }

    val availabilityBasedClient = ListBuffer[String]()
    val httpAvailabilityBasedClient = ListBuffer[String]()
    val thriftAvailabilityBasedClient = ListBuffer[String]()

    var successTotal: Double = 0.0
    var httpSuccessTotal: Double = 0.0
    var thriftSuccessTotal: Double = 0.0
    dates.foreach {
      eachDay =>
        val time = new DateTime(eachDay).withTimeAtStartOfDay()
        serverType match {
          case "mcc"=> {
            // 每天，http,thrift和两者平均的可用率
            val httpSuccessCountPerOfDay = getPerformanceOfHTTP(appkey, time)
            val thriftSuccessCountPerOfDay = 100
            val avgSuccessCountPerOfDay = (httpSuccessCountPerOfDay + thriftSuccessCountPerOfDay) / 2

            httpAvailabilityBasedClient.append(httpSuccessCountPerOfDay.toDouble.formatted("%.4f"))
            thriftAvailabilityBasedClient.append(thriftSuccessCountPerOfDay.toDouble.formatted("%.4f"))
            availabilityBasedClient.append(avgSuccessCountPerOfDay.toDouble.formatted("%.4f"))
              // 累计，http,thrift和两者平均的可用率
            httpSuccessTotal = httpSuccessTotal + httpSuccessCountPerOfDay
            thriftSuccessTotal = thriftSuccessTotal + thriftSuccessCountPerOfDay
            successTotal = successTotal + avgSuccessCountPerOfDay
          }
          case "mns" => {
            val avgSuccessCountPerOfDay = 100
            availabilityBasedClient.append(avgSuccessCountPerOfDay.toDouble.formatted("%.4f"))
            successTotal = successTotal + avgSuccessCountPerOfDay
          }
          case _ => {
            val avgSuccessCountPerOfDay = 0
            availabilityBasedClient.append(avgSuccessCountPerOfDay.toDouble.formatted("%.4f"))
            successTotal = successTotal + avgSuccessCountPerOfDay
          }
        }
    }
    //一个月的平均可用率
    val httpSuccessCountPercent = if (dates.nonEmpty) {
      httpSuccessTotal / dates.size
    } else {
      httpSuccessTotal
    }
    val thriftSuccessCountPercent = if (dates.nonEmpty) {
     thriftSuccessTotal / dates.size
    } else {
      thriftSuccessTotal
    }
    val successCountPercent = if (dates.nonEmpty) {
      successTotal / dates.size
    } else {
      successTotal
    }
    DailyAvailabilityAllData(dates.toList, httpSuccessCountPercent, thriftSuccessCountPercent, successCountPercent,
      httpAvailabilityBasedClient.toList, thriftAvailabilityBasedClient.toList, availabilityBasedClient.toList)
  }

  def getPerformanceOfHTTP(appkey: String, dateTime: DateTime): Double = {
    try {
      val env = "prod"
      val source = "server"
      val currentDate = new DateTime().withTimeAtStartOfDay()
      //从tair中获取天粒度数据且格式化
      val allDailyData = getDailyStatisticFormatted(appkey, env, dateTime, source)
      //val allDailyData_test = getDailyStatisticFormatted(appkey, "test", dateTime, source)
      //allDailyData ++= allDailyData_test
      var totalHttpDaliyPerformance: Double = 0.0
      var total = if (allDailyData.nonEmpty) {
        val httpDailyData = allDailyData.filter {
          d => existPrefixOf(d.spanname)
        }
        if (httpDailyData.nonEmpty) {
          httpDailyData.map { x =>
            val successCountPer = x.successCountPer.replace("%", "").toDouble
            totalHttpDaliyPerformance = totalHttpDaliyPerformance + successCountPer.toDouble;
          }
          totalHttpDaliyPerformance / httpDailyData.size
        } else {
          totalHttpDaliyPerformance
        }
      } else {
        totalHttpDaliyPerformance
      }
      total
    } catch {
      case e: Exception =>
        0.0
    }
  }

  def existPrefixOf(source: String): Boolean = {
    val pattern = "Config2Controller|Config2Controller|APIController".r
    pattern.findPrefixOf(source) match {
      case Some(n) => true
      case _ => false
    }
  }

  case class httpAuthItem(id: Option[Int], username: Option[String], token: String, appkey_pattern: String, owt_pattern: String, updateTime: Option[String])
  implicit val jsonHttpAuthItemReads = Json.reads[httpAuthItem]
  implicit val jsonHttpAuthItemWrites = Json.writes[httpAuthItem]

  def saveHttpAuthItem(json: String): Boolean = {
    Json.parse(json).validate[httpAuthItem].fold({ error =>
      LOG.info(error.toString)
      false
    }, {
      x =>
        try{
          //val username = getLoginName()
          HttpAuthDao.insertOrUpdateAuth(x.username.get, x.token, x.owt_pattern, x.appkey_pattern)
          true
        }catch {
          case e: Exception =>
            LOG.error("saveHttpAuthItem failed: {}", e.getMessage)
            false
        }
    })
  }

  def getHttpAuthItems(page: Page) = {
    val sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
    val items = HttpAuthDao.getAllItems
    val list = if (null != items) {
      items.map { x =>
        httpAuthItem(Option(x.id), Option(x.username), x.token, x.appkeyPattern, x.owtPattern, Option(sdf.format(x.updateTime)))
      }.sortBy(_.username)
    } else {
      List()
    }
    page.setTotalCount(list.length)
    list.slice(page.getStart, page.getStart + page.getPageSize)
  }

  def getAuthUsernames() = {
    val sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
    val items = HttpAuthDao.getAllUsernames
    if (null != items) {
      items
    } else {
      List()
    }
  }

  def getHttpAuth(authID: String):httpAuthItem = {
    val sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
    val item = HttpAuthDao.getAuthItem(authID)
    if (null != item) {
      httpAuthItem(Option(item.id), Option(item.username), item.token, item.appkeyPattern, item.owtPattern, Option(sdf.format(item.updateTime)))
    } else {
      null
    }
  }

  def deleteHttpAuth(authID: String) = {
    val sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
    0 == HttpAuthDao.delete(authID)
  }
}
