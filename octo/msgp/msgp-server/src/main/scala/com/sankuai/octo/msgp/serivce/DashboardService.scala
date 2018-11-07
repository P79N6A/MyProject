package com.sankuai.octo.msgp.serivce

import java.io.{FileInputStream, FileOutputStream, ObjectInputStream, ObjectOutputStream}
import java.util.concurrent.{Executors, TimeUnit}

import com.sankuai.meituan.auth.util.UserUtils
import com.sankuai.msgp.common.config.DbConnection
import com.sankuai.msgp.common.config.db.msgp.Tables.SchedulerCostRow
import com.sankuai.msgp.common.model.{Business, Page, ServiceModels, Status}
import com.sankuai.msgp.common.utils.helper.{AuthorityHelper, CommonHelper}
import com.sankuai.octo.msgp.dao.appkey.AppkeyDescDao
import com.sankuai.octo.msgp.dao.perf.PerfDayDao
import com.sankuai.octo.msgp.dao.self.OctoJobDao
import com.sankuai.octo.msgp.model.DashboardDomain.{Menu, Overview}
import com.sankuai.octo.msgp.model._
import com.sankuai.octo.msgp.serivce.graph.ViewDefine
import com.sankuai.octo.msgp.serivce.service.AppkeyProviderService
import com.sankuai.octo.msgp.serivce.sgagent.SgAgentChecker
import com.sankuai.octo.msgp.service.s3.S3Service
import com.sankuai.octo.statistic.util.ExecutionContextFactory
import org.joda.time.DateTime
import org.slf4j.{Logger, LoggerFactory}

import scala.collection.JavaConverters._
import scala.concurrent.Future

object DashboardService {
  private val LOG: Logger = LoggerFactory.getLogger(DashboardService.getClass)

  private val db = DbConnection.getPool()
  private val tags = "spanname:*,localhost:all"
  private val logCollector = "com.sankuai.inf.logCollector"
  private val tairDefaultCacheKey = "msgp.defaultdash2"
  private val tairInstanceCacheKey = "msgp.instanceCache"

  private val localInstanceListFile = "/opt/logs/DashboardinstanceList"
  //  private val localInstanceListFile = "/Users/nero/proj/DashboardinstanceList"
  private val localOverviewFile = "/opt/logs/OverviewFile"
  //  private val localOverviewFile = "/Users/nero/proj/OverviewFile"


  private val scheduler = Executors.newScheduledThreadPool(2)
  private implicit val ec = ExecutionContextFactory.build(2)

  private val s3Service = new S3Service()

  def getOverviewCache = {
    val dashData = getDefaultDash()
    dashData
    //    if (dashData.isDefined) {
    //      dashData.get
    //    } else {
    //      defaultDash
    //    }
  }

  def refresh = {

    scheduler.scheduleAtFixedRate(new Runnable {
      def run(): Unit = {
        try {
          val start = DateTime.now
          // 更新首页缓存
          defaultDash
          val end = DateTime.now
          OctoJobDao.insertCost(SchedulerCostRow(0, MScheduler.dashboardOverviewRefresh.toString, start.getMillis, end.getMillis))
          val time = end.getMillis - start.getMillis
          LOG.info(s"end refresh defaultDash $time")
        } catch {
          case e: Exception => LOG.error(s"dashboard refresh OverviewCache failed", e)
        }
      }
    }, 5, 600, TimeUnit.SECONDS)

    //定时刷新sg_agent version分布
    //    scheduler.scheduleAtFixedRate(new Runnable {
    //      def run(): Unit = {
    //        try {
    //          val start = System.currentTimeMillis()
    //          //刷新 sg_check 任务
    //          checkSgAgent
    //          val time = System.currentTimeMillis() - start
    //          LOG.info(s"end refresh sg_agent check $time")
    //        } catch {
    //          case e: Exception => LOG.error(s"dashboard refresh OverviewCache failed", e)
    //        }
    //      }
    //    }, 5, 600, TimeUnit.SECONDS)
  }

  def defaultDash = {
    LOG.info("begin refesh dashboard")
    val serviceList = service.ServiceCommon.listService
    val businessMap = serviceList.groupBy(x => Business.getBusinessNameById(x.business.getOrElse(100)).toString).map(x => (x._1, x._2.length))
    val instanceList = serviceList.filter(x => x.appkey != "com.sankuai.inf.sg_agent" && x.appkey != "com.sankuai.inf.kms_agent")
      .flatMap(x => AppkeyProviderService.provider(x.appkey))
    putDefaultInstance(instanceList)
    val statusMap = instanceList.groupBy(x => Status.apply(x.status).toString).map(x => (x._1, x._2.length))
    val instanceByIDC = {
      instanceList.groupBy(node => IdcName.getNameByIdc(CommonHelper.ip2IDC(node.ip))).map(x => (x._1, x._2.length))
    }
    val (requestCountToday, dayCategoryPerfCount) = PerfDayDao.getReqCount
    val serviceGBLocation = AppkeyDescDao.groupByLocation
    val serviceGBType = AppkeyDescDao.groupByType

    val overview = Overview(serviceList.length, businessMap, instanceList.length, statusMap, instanceByIDC,
      requestCountToday, dayCategoryPerfCount, serviceGBLocation, serviceGBType)
    putDefaultDash(overview)
    overview
  }

  private def putDefaultDash(overview: Overview) = {
    //    TairClient.put(tairDefaultCacheKey, overview)
    SerializeToFile(overview, localOverviewFile)
    LOG.info(s"###s3Service is $s3Service")
    s3Service.uploadDashboard(localOverviewFile)
  }

  private def putDefaultInstance(instanceList: List[ServiceModels.ProviderNode]) = {
    //    TairClient.put(tairInstanceCacheKey, instanceList)
    SerializeToFile(instanceList, localInstanceListFile)
    LOG.info(s"###s3Service is $s3Service")
    s3Service.uploadDashboard(localInstanceListFile)
  }

  private def SerializeToFile[T](list: T, path: String) = {
    val bos = new FileOutputStream(path)
    val oos = new ObjectOutputStream(bos)
    oos.writeObject(list)
    oos.close()
  }

  def deserialize[T](path: String): T = {
    val bis = new FileInputStream(path)
    val ois = new ObjectInputStream(bis)
    ois.readObject.asInstanceOf[T]
  }

  private def getDefaultDash() = {
    s3Service.downloadDashboard(localOverviewFile);
    val result = deserialize[Overview](localOverviewFile);
    LOG.info("####look the type _res " + result.isInstanceOf[Overview])
    result
    //    val result = TairClient.get(tairDefaultCacheKey)
    //    try {
    //      result.flatMap {
    //        text =>
    //          Json.parse(text).validate[Overview].asOpt
    //      }
    //    }
    //    catch {
    //      case e: Exception =>
    //        LOG.error(s"getDefaultDash  fail", e)
    //        None
    //    }
  }

  private def getDefaultInstance() = {
    s3Service.downloadDashboard(localInstanceListFile);
    val result = deserialize[List[ServiceModels.ProviderNode]](localInstanceListFile)
    LOG.info("####look the type _res" + result.isInstanceOf[List[ServiceModels.ProviderNode]])
    result
    //    val result = TairClient.get(tairInstanceCacheKey)
    //    try {
    //      result.flatMap {
    //        text =>
    //          Json.parse(text).validate[List[ServiceModels.ProviderNode]].asOpt
    //      }
    //    }
    //    catch {
    //      case e: Exception =>
    //        LOG.error(s"getDefaultInstance  fail", e)
    //        None
    //    }
  }


  def checkSgAgent() = {
    val envs = List(1, 2, 3)
    val regions = List("all", "beijing", "shanghai", "hulk")
    val appkey = "com.sankuai.inf.sg_agent"
    envs.foreach {
      env =>
        regions.foreach {
          region =>
            SgAgentChecker.checkSGVersion(appkey, env, region)
        }
    }
  }

  def refreshCache() = {
    Future {
      defaultDash
    }
  }

  val date = new DateTime().toString("yyyy-MM-dd ")
  val timeStart = date + "00:00:00"
  val timeEnd = date + "23:59:59"

  val menuList = List(
    Menu("Dashboard",
      List(
        Menu("个人主页", "/personal")
        , Menu("订阅中心<span class=\"corner corner-danger\">New!</span>", "/subscribe")
        , Menu("报错大盘", "/error")
//        , Menu("业务大盘", "/business")
        , Menu("数据库大盘", "/database")
        , Menu("缓存大盘", "/tair")
//        , Menu("数据大盘", "/dashboard")
//        , Menu("网络大盘", "/network")

      )),
    Menu("服务详情",
      List(Menu("我的服务", "/service")
        , Menu("服务提供者", "/service/detail?appkey={appkey}#supplier")
        , Menu("服务消费者", "/service/detail?appkey={appkey}#consumer")
        , Menu("服务概要", "/service/detail?appkey={appkey}#outline")
        , Menu("组件依赖", "/service/detail?appkey={appkey}#component")
        , Menu("服务注册", "/service/registry")
        , Menu("值班管理", "/service/detail?appkey={appkey}#oncall")
        , Menu("实时日志", "/realtime/entry?appkey={appkey}")
      )),
    Menu("服务运营",
      List(Menu("配置管理", "/serverOpt/operation?appkey={appkey}#config")
        , Menu("服务分组", "/serverOpt/operation?appkey={appkey}#routes")
        , Menu("Thrift截流", "/serverOpt/operation?appkey={appkey}#thriftCutFlow")
        //, Menu("一键截流", "/serverOpt/operation?appkey={appkey}#cutFlow")
        /*
                , Menu("HTTP截流<span class=\"corner corner-danger\">New!</span>", "/serverOpt/operation?appkey={appkey}#httpCutFlow")
        */
        , Menu("HTTP设置", "/serverOpt/operation?appkey={appkey}#httpConfig")
        , Menu("访问控制", "/serverOpt/operation?appkey={appkey}#accessCtrl")
        , Menu("服务鉴权<span class=\"corner corner-danger\">New!</span>", "/serverOpt/operation?appkey={appkey}#appkeyAuth")
        //  , Menu("弹性伸缩<span class=\"corner corner-danger\">New!</span>", "/serverOpt/operation?appkey={appkey}#hulkPolicy")
        //  , Menu("一键扩容<span class=\"corner corner-danger\">New!</span>", "/serverOpt/operation?appkey={appkey}#manuScaleOut")
        , Menu("弹性伸缩<span class=\"corner corner-danger\">New!</span>", "/serverOpt/operation?appkey={appkey}#hulkOption")
        // , Menu("主机诊断", "/serverOpt/operation?appkey={appkey}#hostManage")//主机管理
        , if (CommonHelper.isOffline) Menu("日志级别调整", s"http://log.inf.dev.sankuai.com/search/index?appkey=") else Menu("日志级别调整", s"http://xmdlog.sankuai.com/search/index?appkey=")
        //        , if (CommonHelper.isOffline) Menu("日志一键降级", s"http://log.inf.dev.sankuai.com/search/degrade?appkey=${ThreadContext.get(appkey)}") else Menu("日志一键降级", s"http://xmdlog.sankuai.com/search/degrade?appkey=${ThreadContext.get(appkey)}")
        , Menu("操作记录", "/serverOpt/operation?appkey={appkey}#syslog")
      )),
    Menu("数据分析",
      List(
        Menu("数据总览", "/data/tabNav?appkey={appkey}&type=dashboard#dashboard")
        , Menu("业务指标", "/data/tabNav?appkey={appkey}&type=operation#operation")
        , Menu("性能指标", "/data/tabNav?appkey={appkey}&type=performance#performance")
        , Menu("来源分析", "/data/tabNav?appkey={appkey}&type=source#source")
        , Menu("去向分析", "/data/tabNav?appkey={appkey}&type=destination#destination")
        , Menu("主机分析", "/data/tabNav?appkey={appkey}&type=host#host")
        , Menu("秒级指标<span class=\"corner corner-danger\">New!</span>", "/data/tabNav?appkey={appkey}&type=secondLevel#secondLevel")
        , Menu("上下游分析", "/data/tabNav?appkey={appkey}&type=stream#stream")
        , if (CommonHelper.isOffline) Menu("调用链分析", s"http://mtrace.inf.dev.sankuai.com/elasticSearch/getTraceIDByAnnotation?appkey={appkey}&methodName=&timeStart=$timeStart&timeEnd=$timeEnd")
        else Menu("调用链分析", s"http://mtrace.inf.sankuai.com/elasticSearch/getTraceIDByAnnotation?appkey={appkey}&methodName=&timeStart=$timeStart&timeEnd=$timeEnd")
        , Menu("标签治理", "/data/tabNav?appkey={appkey}&type=tag#tag")
      )),
    Menu("服务报表",
      List(
        Menu("业务线周报", s"/repservice"),
        Menu("服务治理日报", s"/repservice/daily"),
        Menu("服务治理周报", s"/repservice/weekly"),
        Menu("服务组件治理", s"/component/tabNavExt"),
        if (CommonHelper.isOffline) Menu("调用链周报", s"http://mtrace.sankuai.com/report/view?source=octoReport")
        else Menu("调用链周报", s"http://mtrace.sankuai.com/report/view?source=octoReport")
      )),
    Menu("监控报警",
      List(
        Menu("性能配置报警", "/monitor/config?appkey={appkey}")
        , Menu("性能报警记录", "/monitor/log?appkey={appkey}")
        , Menu("异常日志统计", "/log/report?appkey={appkey}")
        , Menu("异常日志趋势", "/log/trend?appkey={appkey}")
        , Menu("异常监控配置", "/log/configuration/list?appkey={appkey}")
        , Menu("异常过滤器配置", "/log/filter/list?appkey={appkey}")
        , Menu("业务监控配置", "/monitor/business?screenId=")
        , Menu("业务大盘配置", "/monitor/business/dash/config?owt=")
        , Menu("服务节点报警配置", "/monitor/provider/config?appkey={appkey}")
      )
    ),
    Menu("服务视图",
      List(
        Menu(ViewDefine.Graph.waimai.toString, s"/graph/level?id=${ViewDefine.Graph.waimai.id}")
        , Menu(ViewDefine.Graph.waimai_m.toString, s"/graph/level?id=${ViewDefine.Graph.waimai_m.id}")
        , Menu(ViewDefine.Graph.waimai_c.toString, s"/graph/level?id=${ViewDefine.Graph.waimai_c.id}")
        , Menu(ViewDefine.Graph.waimai_b.toString, s"/graph/level?id=${ViewDefine.Graph.waimai_b.id}")
        , Menu(ViewDefine.Graph.banma.toString, s"/graph/level?id=${ViewDefine.Graph.banma.id}")
        , Menu(ViewDefine.Graph.meishi.toString, s"/graph/level?id=${ViewDefine.Graph.meishi.id}")
        , Menu(ViewDefine.Graph.hotel.toString, s"/graph/level?id=${ViewDefine.Graph.hotel.id}")
        , Menu(ViewDefine.Graph.hotel_m.toString, s"/graph/level?id=${ViewDefine.Graph.hotel_m.id}")
        , Menu(ViewDefine.Graph.travel.toString, s"/graph/level?id=${ViewDefine.Graph.travel.id}")
        , Menu(ViewDefine.Graph.movie_group.toString, s"/graph/level?id=${ViewDefine.Graph.movie_group.id}")
        , Menu(ViewDefine.Graph.finance_group.toString, s"/graph/level?id=${ViewDefine.Graph.finance_group.id}")
        , Menu(ViewDefine.Graph.pay_group.toString, s"/graph/level?id=${ViewDefine.Graph.pay_group.id}")
        , Menu(ViewDefine.Graph.car_group.toString, s"/graph/level?id=${ViewDefine.Graph.car_group.id}")
        , Menu(ViewDefine.Graph.sjst_m_group.toString, s"/graph/level?id=${ViewDefine.Graph.sjst_m_group.id}")
        , Menu(ViewDefine.Graph.pay_quickpass.toString, s"/graph/level?id=${ViewDefine.Graph.pay_quickpass.id}")
        , Menu(ViewDefine.Graph.sjst_erp.toString, s"/graph/level?id=${ViewDefine.Graph.sjst_erp.id}")
        , Menu(ViewDefine.Graph.micro_loan.toString, s"/graph/level?id=${ViewDefine.Graph.micro_loan.id}")
        , Menu(ViewDefine.Graph.daocan_c.toString, s"/graph/level?id=${ViewDefine.Graph.daocan_c.id}")
        , Menu(ViewDefine.Graph.daocan_message.toString, s"/graph/level?id=${ViewDefine.Graph.daocan_message.id}")
        , Menu(ViewDefine.Graph.wallet.toString, s"/graph/level?id=${ViewDefine.Graph.wallet.id}")
        , Menu(ViewDefine.Graph.pay_c.toString, s"/graph/level?id=${ViewDefine.Graph.pay_c.id}")
        , Menu(ViewDefine.Graph.pay_b.toString, s"/graph/level?id=${ViewDefine.Graph.pay_b.id}")
        , Menu(ViewDefine.Graph.pay_risk.toString, s"/graph/level?id=${ViewDefine.Graph.pay_risk.id}")
        , Menu(ViewDefine.Graph.lottery.toString, s"/graph/level?id=${ViewDefine.Graph.lottery.id}")
        , Menu(ViewDefine.Graph.ad_business.toString, s"/graph/level?id=${ViewDefine.Graph.ad_business.id}")

      )),
    Menu("用户自检<span class=\"corner corner-danger\">New!</span>",
      List(
        Menu("主机信息", "/checker/userCheck?appkey={appkey}#checkerHostInfo")
        , Menu("服务列表自检", "/checker/userCheck?appkey={appkey}#checkerService")
        , Menu("配置获取自检", "/checker/userCheck?appkey={appkey}#checkerConfig")
        , Menu("Mtthrift自检", "/checker/userCheck?appkey={appkey}#thriftCheck")
      )),
    Menu("文档与帮助",
      List(
        Menu("帮助文档", s"/more/doc")
        //Menu("更新记录", s"/more/update_log")
        //Menu("<hr style=\"margin: 10px 0;\"/><div style=\"text-align: center;\">OCTO客户通知群</div><div style=\"text-align: center;\"><img src=\"/static/img/qr_code_octo2.png\" height=\"200px\" width=\"200px\"/></div>")
      ))
  )

  def getMenuListByUser = {
    if (AuthorityHelper.hasMangagementAuth(UserUtils.getUser)) {
      menuList :+
        Menu("系统管理",
          List(Menu("OCTO大盘", "/manage/dashboard"),
            Menu("服务画像", "/manage/portrait/portraitTabNav"),
            Menu("已删除服务", "/manage/operation?appkey={appkey}"),
            Menu("系统自检", "/manage/octoSelfCheck"),
            Menu("组件依赖管理", "/component/tabNav"),
            Menu("服务治理管理", "/manage/agent/tabNav?appkey={appkey}#availabilityData"),
            Menu("服务价值统计", "/worth/count/tabNav"),
            Menu("服务价值报告", "/worth/echart"),
            Menu("服务治理覆盖率统计", "/svccover/serviceCoverage")

          ))
    } else {
      menuList
    }
  }

  def menus() = {
    getMenuListByUser.map {
      x =>
        CommonHelper.toMap(x).updated("menus", x.menus.map(CommonHelper.toJavaMap).asJava).asJava
    }.asJava
  }


  def getInstanceCache = {
    getDefaultInstance()
  }

  def getInstanceByIDC(idcName: String, page: Page) = {
    val instanceList = getInstanceCache
    val idc = IdcName.getIdcByName(idcName)
    val instanceResult = if (idc == "OTHER") {
      val knownIdc = IdcName.idcNameMap.keys.toList.filter(_ != "OTHER")
      instanceList.filter {
        x =>
          !knownIdc.contains(CommonHelper.ip2IDC(x.ip))
      }
    } else {
      instanceList.filter {
        x =>
          CommonHelper.ip2IDC(x.ip).equals(idc)
      }
    }
    page.setTotalCount(instanceResult.length)
    instanceResult.slice(page.getStart, page.getStart + page.getPageSize)
  }

  def getInstanceByStatus(status: Int, page: Page) = {
    val instanceList = getInstanceCache
    val instanceResult = instanceList.filter(_.status == status)
    page.setTotalCount(instanceResult.length)
    instanceResult.slice(page.getStart, page.getStart + page.getPageSize)
  }
}
