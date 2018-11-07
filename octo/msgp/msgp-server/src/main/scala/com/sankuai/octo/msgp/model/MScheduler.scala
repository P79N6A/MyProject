package com.sankuai.octo.msgp.model

//字符串作为检索数据库的关键字
object MScheduler extends Enumeration{
  type Schedule = Value
  //监控报警任务调度
  val monitorSchedule = Value(0,"monitorSchedule")
  //同步性能监控平台数据
  val dataSyncSchedule = Value(1,"dataSyncSchedule")
  //sg_agent重复注册检测
  val sgAgentScannerSchedule = Value(2,"sgAgentScannerSchedule")
  //sg_agent provider desc刷新
  val sgAgentProviderRefresh = Value(3, "sgAgentProviderRefresh")
  //首页绘图刷新
  val dashboardOverviewRefresh = Value(4, "dashboardOverviewRefresh")

  val appkeyAndProviderRefresh = Value(5, "appkeyAndProviderRefresh")
}
