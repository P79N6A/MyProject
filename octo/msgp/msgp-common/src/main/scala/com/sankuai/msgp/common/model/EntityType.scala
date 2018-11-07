package com.sankuai.msgp.common.model


object EntityType extends Enumeration{
  type entityTypeEnum = Value
  //msgp操作
  val registerServer = Value("服务注册")
  val updateServer = Value("服务概要修改")
  val deleteServer = Value("删除服务")
  val increaseProvider = Value("增加提供者")
  val msgpDelProvider = Value("删除提供者")
  val updateProvider = Value("更新提供者")

  val createCellar = Value("创建cellar节点")

  val updateSession = Value("启用停用会话")

  val createTrigger = Value("添加报警")
  val updateTrigger = Value("编辑报警")
  val deleteTrigger = Value("删除报警")

  //scanner操作
  val updateWeight = Value("调整权重")
  val updateStatus = Value("调整状态")
  val deleteProvider = Value("删除节点")

  //主机操作
  val applySwitch = Value("申请主机环境切换")
  val doSwitchEnv = Value("执行主机环境切换")
  val updateAndRestartAgent = Value("修改agent配置并且重启agent")
  val switchEnvDelProvider = Value("环境切换删除节点")
  val switchEnvNewProvider = Value("环境切换增加节点")

  //MCC操作
  val sgconfigMigration = Value("sgconfig数据迁移")

  //机器下线
  val shutdownMachine = Value("机器下线")
  val disableProvider = Value("节点禁用")


  val deleteGroup = Value("删除服务分组")
  val createGroup = Value("创建分组")
  val updateGroup = Value("编辑分组")

  val deleteHttpGroup = Value("删除http服务分组")
  val createHttpGroup = Value("创建http分组")
  val updateHttpGroup = Value("编辑http分组")

  //http相关配置
  val updateSlowStart = Value("调整慢启动策略")
  val updateHealthCheckConfig = Value("调整健康检查策略")
  val updateLoadBalanceConfig = Value("调整负载均衡策略")
  val updateDomainConfig = Value("调整域名映射策略")

  // 异常日志相关配置
  val errorLogStart = Value("异常日志-启动")
  val errorLogStop = Value("异常日志-停止")
  val errorLogAddMonitor = Value("异常日志-添加日志监控")
  val errorLogStartAlarm = Value("异常日志-启动报警")
  val errorLogStopAlarm = Value("异常日志-停止报警")
  val errorLogRestartAlarm = Value("异常日志-重启报警")
  val errorLogAddFilter = Value("异常日志-增加日志过滤器")
  val errorLogUpdateFilter = Value("异常日志-修改日志过滤器")
  val errorLogDeleteFilter = Value("异常日志-删除日志过滤器")
  val errorLogSortFilter = Value("异常日志-过滤器排序")
  val errorLogEnableFilter = Value("异常日志-启用某条过滤器")
  val errorLogDisableFilter = Value("异常日志-禁用某条过滤器")

  // Mtthrift
  val mtthriftInvoke = Value("Mtthrift-Http接口调用")

  val MESSAGE = Value("发送通知")

  val cellSwitch = Value("调整set-cell状态")

  //弹性相关配置
  val manualScaleOut = Value("扩容")
  val manualScaleIn = Value("缩容")
  val manualScaleOutError = Value("一键扩容-错误")
  val manualScaleInError = Value("一键缩容-错误")
  val unifiedPolicyAdd = Value("监控策略新增")
  val unifiedPolicyUpdate = Value("监控策略更新")
  val unifiedPolicyScaleOut = Value("统一监控扩容")
  val unifiedPolicyScaleIn = Value("统一监控缩容")
  val periodPolicyAdd = Value("周期策略新增")
  val periodPolicyUpdate = Value("周期策略更新")
  val periodPolicyScaleOut = Value("周期策略扩容")
  val periodPolicyScaleIn = Value("周期策略缩容")
  val groupOperationAdd = Value("分组创建")
  val groupOperationUpdate = Value("分组更新")

  //服务鉴权更新
  val updateAppkeyAuthWhiteList = Value("更新服务鉴权白名单")
  val updateAppkeyAuth = Value("更新服务鉴权配置")
  val updateSpanAuth = Value("更新接口鉴权配置")

}
