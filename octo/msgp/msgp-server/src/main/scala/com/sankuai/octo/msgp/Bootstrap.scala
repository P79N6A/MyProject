package com.sankuai.octo.msgp

import com.sankuai.msgp.common.config.MsgpConfig
import com.sankuai.msgp.common.service.hulk.HulkApiService
import com.sankuai.msgp.common.service.org.OpsService
import com.sankuai.msgp.common.utils.helper.CommonHelper
import com.sankuai.octo.msgp.serivce.data._
import com.sankuai.octo.msgp.serivce.graph.ViewCache
import com.sankuai.octo.msgp.serivce.manage.{AgentAvailability, ScannerChecker}
import com.sankuai.octo.msgp.serivce.monitor.{MonitorTrigger, MonitorTriggerLeader}
import com.sankuai.octo.msgp.serivce.service.{AppkeyDescService, ServiceConfig}
import com.sankuai.octo.msgp.serivce.{DashboardService, DomService}
import com.sankuai.octo.msgp.service.cutFlow.CutFlowService
import com.sankuai.octo.msgp.task.{MonitorScheduleTask, _}
import com.sankuai.octo.mworth.service.worthCountService
import com.sankuai.octo.mworth.task.{worthEventCountDailyTask, worthEventTask}
import org.slf4j.{Logger, LoggerFactory}

/**
  * 定时任务调度类
  * 1、区分线上、线下执行不同的调度任务
  * 2、通过动态配置指定一个主机执行调度任务，解决多机部署问题
  *
  * @return
  */
class Bootstrap {
  val LOG: Logger = LoggerFactory.getLogger(classOf[Bootstrap])
  val hostKey = "msgp.task.host"

  //注意：定时任务通过MCC配置实现，如果msgp部署机器调整，需要确认定时任务是否执行
  def init() = {
    val taskHost = if (CommonHelper.isOffline) {
      MsgpConfig.get(hostKey, "10.4.254.140")
    } else {
      MsgpConfig.get(hostKey, "10.5.203.31")
    }
    val localHost = CommonHelper.getLocalIp

    LOG.info(s"taskHost is $taskHost")
    LOG.info(s"localHost is $localHost")
    //多机计算日报
    ReportDailyTask.init
    DomService.init
    //多机计算性能报警
    MonitorTrigger.start
    //刷新owt 与 business
    OpsService.refreshOwtBG

    //执行任务调度
    if (taskHost == localHost) {
      //刷新ip
      OpsService.start()
      HulkApiService.start()
      //刷新首页
      DashboardService.refresh
      //刷新服务提供者
      AppkeyDescService.refresh
      ViewCache.start
      Kpi.startJob

      MonitorTriggerLeader.start()
      ScannerChecker.startScannerCheckerTask()
      AgentAvailability.checkJob()
      ServiceConfig.checkMCC()

      worthEventTask.start
      worthEventCountDailyTask.start
      worthCountService.start

      //组件趋势
      ComponentDailyTask.start

      //业务线周报
      QpsPeakTimerTask.init()
      IdcTrafficTimerTask.init()
      QpsTimerTask.init()
      DependTimerTask.init()

      //异常日志
      ErrorLogTimerTask.init()

      //服务治理日报Leader
      ReportDailyLeaderTask.init()

      //error定时任务
      ErrorQuery.start()

      //服务可用率
      AvailabilityTask.start

      //业务指标报警
//      MonitorScheduleTask.start

      //一键截流检查
      //ServiceCutFlow.start()
      CutFlowService.start()

      MonitorProviderTask.init()
    }
    //线上任务机器发邮件，发送服务提供者报警
    if (!CommonHelper.isOffline && taskHost == localHost) {
      ReportDailyMailTask.init()
      ReportWeeklyMailTask.init()
    }
  }
}
