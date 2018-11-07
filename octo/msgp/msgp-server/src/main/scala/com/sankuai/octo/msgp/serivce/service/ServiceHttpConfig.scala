package com.sankuai.octo.msgp.serivce.service

import com.sankuai.meituan.borp.vo.ActionType
import com.sankuai.msgp.common.model.{EntityType, Env, Path, ServiceModels}
import com.sankuai.msgp.common.utils.client.BorpClient
import com.sankuai.msgp.common.utils.{HttpUtil, StringUtil}
import com.sankuai.msgp.common.utils.helper.JsonHelper
import com.sankuai.octo.msgp.utils.client.ZkHlbClient
import org.slf4j.{Logger, LoggerFactory}
import play.api.libs.json.{JsError, Json}

/**
  * Created by zhangjinlu on 16/5/7.
  */
object ServiceHttpConfig {
  val LOG: Logger = LoggerFactory.getLogger(ServiceHttpConfig.getClass)

  val defaultTCPParams = "check interval=3000 rise=2 fall=3 timeout=1500 type=tcp;"
  val defaultHTTPParams = "check interval=3000 rise=2 fall=3 timeout=1500 type=http; check_http_send \"GET / HTTP/1.0\\r\\n\\r\\n\";"
  val defaultCustomizedRise = 2
  val defaultCustomizedFall = 3
  val defaultCustomizedInterval = 3000
  val defaultCustomizedTimeout = 1500
  val defaultCustomizedCheckHttpExpectAlive = "http_2xx http_3xx"
  val defaultCentraCheckType = "tcp"
  val defaultCentraHttpSend = ""

  val defaultLoadBalanceType = "WRR"
  val defaultSlowStart = 0
  val defaultSlowStartTime = "60s"
  val sankuaiPath = "/mns/sankuai"

  case class ErrMsg(code: Int = SUCCESS, msg: String = "")

  val SUCCESS = 0
  val FAILURE = -1

  def getCurrTimestamp(): Long = {
    System.currentTimeMillis() / 1000
  }

  def getDefaultHealthCheckConfig(appkey: String): ServiceModels.HealthCheckConfig = {
    val currTime = Option(getCurrTimestamp())
    ServiceModels.HealthCheckConfig(appkey, 0, Option("defaultTCP"), Option(defaultTCPParams),
      defaultCentraCheckType, defaultCentraHttpSend, None, currTime, currTime, None)
  }

  def getHealthCheckConfig(appkey: String, envId: Int): ServiceModels.HealthCheckConfig = {
    val sharedConfigData = getSharedHttpConfig(appkey, envId)
    val healthCheckIssue = sharedConfigData.health_check_issue
    ServiceModels.HealthCheckConfig(appkey, healthCheckIssue.is_health_check, healthCheckIssue.health_check_type,
      healthCheckIssue.health_check, healthCheckIssue.centra_check_type, healthCheckIssue.centra_http_send,
      healthCheckIssue.customized_params,
      sharedConfigData.createTime, sharedConfigData.updateTime, sharedConfigData.reserved)
  }

  def getDefaultDomainConfig(appkey: String): ServiceModels.DomainConfig = {
    val currTime = Option(getCurrTimestamp())
    ServiceModels.DomainConfig(appkey, "", "/", currTime, currTime, None)
  }

  def getDomainConfig(appkey: String, envId: Int): ServiceModels.DomainConfig = {
    val sharedConfigData = getSharedHttpConfig(appkey, envId)
    val domainIssue = sharedConfigData.domain_issue
    ServiceModels.DomainConfig(appkey, domainIssue.domain_name, domainIssue.domain_location,
      sharedConfigData.createTime, sharedConfigData.updateTime, sharedConfigData.reserved)
  }

  def getDefaultSlowStartConfig(appkey: String): ServiceModels.SlowStartConfig = {
    val currTime = Option(getCurrTimestamp())
    ServiceModels.SlowStartConfig(appkey, defaultSlowStart,defaultSlowStartTime, currTime, currTime, None)
  }

  def getSlowStartConfig(appkey: String, envId: Int): ServiceModels.SlowStartConfig = {
    val sharedConfigData = getSharedHttpConfig(appkey, envId)
    val slowStartIssue = sharedConfigData.slow_start_issue
    var is_slow_start = defaultSlowStart
    var slow_start_value =defaultSlowStartTime
    if (slowStartIssue.isEmpty) {
        is_slow_start = defaultSlowStart
        slow_start_value =defaultSlowStartTime

    }else{

        is_slow_start = slowStartIssue.get.is_slow_start
        slow_start_value =slowStartIssue.get.slow_start_value
    }


    ServiceModels.SlowStartConfig(appkey,is_slow_start,slow_start_value,
      sharedConfigData.createTime, sharedConfigData.updateTime, sharedConfigData.reserved)
  }

  def getDefaultLoadBalanceConfig(appkey: String): ServiceModels.LoadBalanceConfig = {
    val currTime = Option(getCurrTimestamp())
    ServiceModels.LoadBalanceConfig(appkey, defaultLoadBalanceType, "", currTime, currTime, None)
  }

  def getLoadBalanceConfig(appkey: String, envId: Int): ServiceModels.LoadBalanceConfig = {
    val sharedConfigData = getSharedHttpConfig(appkey, envId)
    val loadBalanceIssue = sharedConfigData.load_balance_issue
    ServiceModels.LoadBalanceConfig(appkey, loadBalanceIssue.load_balance_type, loadBalanceIssue.load_balance_value,
      sharedConfigData.createTime, sharedConfigData.updateTime, sharedConfigData.reserved)
  }

  def getDefaultSharedHttpConfig(appkey: String): ServiceModels.SharedHttpConfig = {
    val currTime = Option(getCurrTimestamp())

    val defaultHttpConfig = getDefaultHealthCheckConfig(appkey)
    val defaultHealthCheckIssue = ServiceModels.HealthCheckIssue(defaultHttpConfig.is_health_check, defaultHttpConfig.health_check_type,
      defaultHttpConfig.health_check, defaultHttpConfig.centra_check_type,
      defaultHttpConfig.centra_http_send, defaultHttpConfig.customized_params)

    val defaultDomainConfig = getDefaultDomainConfig(appkey)
    val defaultDomainIssue = ServiceModels.DomainIssue(defaultDomainConfig.domain_name, defaultDomainConfig.domain_location)

    val defaultSlowStartConfig = getDefaultSlowStartConfig(appkey)
    val defaultSlowStartIssue = Option(ServiceModels.SlowStartIssue(defaultSlowStartConfig.is_slow_start,defaultSlowStartConfig.slow_start_value))

    val defaultLoadBalanceConfig = getDefaultLoadBalanceConfig(appkey)
    val defaultLoadBalanceIssue = ServiceModels.LoadBalanceIssue(defaultLoadBalanceConfig.load_balance_type, defaultLoadBalanceConfig.load_balance_value)

    ServiceModels.SharedHttpConfig(appkey, defaultHealthCheckIssue, defaultDomainIssue, defaultLoadBalanceIssue, defaultSlowStartIssue,currTime, currTime, None, None)
  }

  // healthCheckConfig domainConfig loadBalanceConfig共享一份数据sharedHttpConfig
  def getSharedHttpConfig(appkey: String, envId: Int): ServiceModels.SharedHttpConfig = {
    val path = s"$sankuaiPath/${Env.apply(envId)}/$appkey/${Path.sharedHttpConfig}"
    if (ZkHlbClient.exist(path)) {
      val data = ZkHlbClient.getData(path)
      LOG.debug("getSharedHttpConfig: zk data: " + data)
      if (data.isEmpty || data == "\"\"" ) {
        // 只有两个引号时也作为空字符串对待
        // zk节点没有数据时返回默认数据
        LOG.error("getSharedHttpConfig: zk has no data for path: " + path + ". Return default data.")
        getDefaultSharedHttpConfig(appkey)
      } else {
        Json.parse(data).validate[ServiceModels.SharedHttpConfig].get
      }
    } else {
      // zk节点不存在时返回默认数据
      LOG.error("getSharedHttpConfig: zk has no path: " + path + ". Return default data")
      getDefaultSharedHttpConfig(appkey)
    }
  }

  /**
    * httpconfig保存单独的zk
    *
    * @param appkey
    * @param envId
    * @param data
    */
  def saveSharedHttpConfig(appkey: String, envId: Int, data: ServiceModels.SharedHttpConfig) {
    val path = s"$sankuaiPath/${Env.apply(envId)}/$appkey/${Path.sharedHttpConfig}"
    if (ZkHlbClient.exist(path)) {
      ZkHlbClient.setData(path, Json.prettyPrint(Json.toJson(data)))
    } else {
      // zk节点不存在时创建新节点
      LOG.info("saveSharedHttpConfig: zk has no path: " + path + ". Create path with data.")
      ZkHlbClient.create(path, Json.prettyPrint(Json.toJson(data)))
    }
  }


  def addHealthCheckConfig(appkey: String, envId: Int, data: String) = {
    Json.parse(data).validate[ServiceModels.HealthCheckConfig].fold({
      error =>
        LOG.info("[addHealthCheckConfig] " + error.toString)
        JsonHelper.errorJson("参数错误: " + JsError.toFlatJson(error).toString())
    }, {
      config => {
        if (config.appkey.equalsIgnoreCase(appkey)) {
          val ipAndPort = ServiceProvider.getOneProvider(appkey, envId)
          if (StringUtil.isBlank(ipAndPort)) {
            JsonHelper.errorJson("可用服务列表为空，请确认")
          } else {
            val centra_http_send = config.centra_http_send
            val url = "http://" + ipAndPort + centra_http_send
            val statusCode = HttpUtil.getUrlCode(url)
            if (statusCode >= 200 && statusCode < 300) {
              val configFixed = if (centra_http_send.equalsIgnoreCase("/monitor/alive")) {
                config.copy(health_check_type = Option("defaultUniform"))
              } else {
                config.copy(health_check_type = Option("customized"))
              }
              updateHealthCheckConfig(appkey, envId, configFixed)
              JsonHelper.dataJson("健康检查设置成功")
            } else if (statusCode >= 300 && statusCode < 400) {
              JsonHelper.errorJson("接口请去掉sso配置")
            } else {
              JsonHelper.errorJson("接口url检测失败, 请重试")
            }
            val configFixed = if (centra_http_send.equalsIgnoreCase("/monitor/alive")) {
              config.copy(health_check_type = Option("defaultUniform"))
            } else {
              config.copy(health_check_type = Option("customized"))
            }
            updateHealthCheckConfig(appkey, envId, configFixed)
            JsonHelper.dataJson("健康检查设置成功")
          }
        } else {
          JsonHelper.dataJson("appkey不允许修改")
        }
      }
    })
  }


  def updateHealthCheckConfig(appkey: String, envId: Int, data: String): String = {
    Json.parse(data).validate[ServiceModels.HealthCheckConfig].fold({
      error =>
        LOG.info("[updateHealthCheckConfig] " + error.toString)
        JsonHelper.errorJson("内部异常: " + JsError.toFlatJson(error).toString())
    }, {
      x => {
        if (x.appkey.equalsIgnoreCase(appkey)) {
          updateHealthCheckConfig(appkey, envId, x)
          JsonHelper.dataJson(x)
        } else {
          JsonHelper.dataJson("appkey不允许修改")
        }
      }
    })
  }

  def updateHealthCheckConfig(appkey: String, envId: Int, data: ServiceModels.HealthCheckConfig) {

    val currTime = Option(getCurrTimestamp())
    val oldData = getHealthCheckConfig(appkey, envId)
    var healthCheck = data.health_check

    // 如果选择默认选项，设置为默认参数
    if (data.health_check_type == Option("defaultTCP")) {
      healthCheck = Option(defaultTCPParams)
    } else if (data.health_check_type == Option("defaultHTTP")) {
      healthCheck = Option(defaultHTTPParams)
    }

    val newData = oldData.copy(is_health_check = data.is_health_check,
      health_check_type = data.health_check_type,
      health_check = healthCheck,
      centra_check_type = data.centra_check_type,
      centra_http_send = data.centra_http_send,
      customized_params = data.customized_params,
      updateTime = currTime)

      var msg = s"${Env.apply(envId)}环境"
      if (data.is_health_check ==1){
          msg =msg +"开启健康检查策略，"+"接口为："+data.centra_http_send
      }else{
          msg =msg +"关闭健康检查策略，"+"接口为："+data.centra_http_send
      }
    BorpClient.saveOpt(actionType = ActionType.INSERT.getIndex, entityId = appkey, entityType = EntityType.updateHealthCheckConfig,newValue=msg)
    saveHealthCheckConfig(appkey, envId, newData)
  }

  def saveHealthCheckConfig(appkey: String, envId: Int, data: ServiceModels.HealthCheckConfig) {
    val currTime = Option(getCurrTimestamp())
    val oldSharedData = getSharedHttpConfig(appkey, envId)
    val newHealthCheckIssue = ServiceModels.HealthCheckIssue(data.is_health_check, data.health_check_type,
      data.health_check, data.centra_check_type,
      data.centra_http_send, data.customized_params)
    val newSharedData = oldSharedData.copy(health_check_issue = newHealthCheckIssue, updateTime = currTime)
    saveSharedHttpConfig(appkey, envId, newSharedData)
  }

  def updateSlowStartConfig(appkey: String, envId: Int, data: String): String = {
    Json.parse(data).validate[ServiceModels.SlowStartConfig].fold({
      error =>
        LOG.info("[updateSlowStartConfig] " + error.toString)
        JsonHelper.errorJson("内部异常: " + JsError.toFlatJson(error).toString())
    }, {
      x => {
        if (x.appkey.equalsIgnoreCase(appkey)) {
          updateSlowStartConfig(appkey, envId, x)
          JsonHelper.dataJson(x)
        } else {
          JsonHelper.dataJson("appkey不允许修改")
        }
      }
    })
  }

  def updateSlowStartConfig(appkey: String, envId: Int, data: ServiceModels.SlowStartConfig) {

    val currTime = Option(getCurrTimestamp())
    val oldData = getSlowStartConfig(appkey, envId)
    val newData = oldData.copy(is_slow_start = data.is_slow_start,
      slow_start_value =data.slow_start_value,
      updateTime = currTime)
    var msg = s"${Env.apply(envId)}环境"
    if (data.is_slow_start ==1){
      msg =msg +"开启慢启动开关，"+"调整时间为："+data.slow_start_value
    }else{
      msg =msg +"关闭慢启动开关，"+"调整时间为："+data.slow_start_value
    }

    BorpClient.saveOpt(actionType = ActionType.INSERT.getIndex, entityId = appkey, entityType = EntityType.updateSlowStart,newValue = msg)

    saveSlowStartConfig(appkey, envId, newData)
  }

  def saveSlowStartConfig(appkey: String, envId: Int, data: ServiceModels.SlowStartConfig) {
    val currTime = Option(getCurrTimestamp())
    val oldSharedData = getSharedHttpConfig(appkey, envId)
    val newSlowStartIssue = ServiceModels.SlowStartIssue(data.is_slow_start,data.slow_start_value)
    val newSharedData = oldSharedData.copy(slow_start_issue = Option(newSlowStartIssue), updateTime = currTime)
    saveSharedHttpConfig(appkey, envId, newSharedData)
  }


  def updateDomainConfig(appkey: String, envId: Int, data: String): String = {
    Json.parse(data).validate[ServiceModels.DomainConfig].fold({
      error => LOG.info(error.toString); JsonHelper.errorJson("内部异常: " + JsError.toFlatJson(error).toString())
    }, {
      x => {
        if (x.appkey.equalsIgnoreCase(appkey)) {
          updateDomainConfig(appkey, envId, x)
          JsonHelper.dataJson(x)
        } else {
          JsonHelper.dataJson("appkey不允许修改")
        }
      }
    })
  }

  def updateDomainConfig(appkey: String, envId: Int, data: ServiceModels.DomainConfig) {
    val currTime = Option(getCurrTimestamp())
    val oldData = getDomainConfig(appkey, envId)
    val newData = oldData.copy(domain_name = data.domain_name, domain_location = data.domain_location, updateTime = currTime)
    saveDomainConfig(appkey, envId, newData)
  }

  def saveDomainConfig(appkey: String, envId: Int, data: ServiceModels.DomainConfig) {
    val currTime = Option(getCurrTimestamp())
    val oldSharedData = getSharedHttpConfig(appkey, envId)
    val newDomainIssue = ServiceModels.DomainIssue(data.domain_name, data.domain_location)
    val newSharedData = oldSharedData.copy(domain_issue = newDomainIssue, updateTime = currTime)
    saveSharedHttpConfig(appkey, envId, newSharedData)
  }


  def updateLoadBalanceConfig(appkey: String, envId: Int, data: String): String = {
    Json.parse(data).validate[ServiceModels.LoadBalanceConfig].fold({
      error => LOG.info(error.toString); JsonHelper.errorJson("内部异常: " + JsError.toFlatJson(error).toString())
    }, {
      x => {
        if (x.appkey.equalsIgnoreCase(appkey)) {
          val oldSharedData = getSharedHttpConfig(appkey, envId)
          //更新route-http
          if (x.load_balance_type == "idc_optimize") {
            ServiceGroup.setHttpIDCOptimize(appkey, envId, true)
            ServiceGroup.setHttpCenterOptimize(appkey, envId, false)
          }else if(x.load_balance_type == "center_optimize"){
            ServiceGroup.setHttpCenterOptimize(appkey,envId, true)
            ServiceGroup.setHttpIDCOptimize(appkey, envId, false)
          } else if (oldSharedData.load_balance_issue.load_balance_type == "idc_optimize") {
            ServiceGroup.setHttpIDCOptimize(appkey, envId, false)
          }else if (oldSharedData.load_balance_issue.load_balance_type == "center_optimize"){
            ServiceGroup.setHttpCenterOptimize(appkey, envId, false)
          }
          updateLoadBalanceConfig(appkey, envId, x)
          JsonHelper.dataJson(x)
        } else {
          JsonHelper.dataJson("appkey不允许修改")
        }
      }
    })
  }

  def updateLoadBalanceConfig(appkey: String, envId: Int, data: ServiceModels.LoadBalanceConfig) {
    val currTime = Option(getCurrTimestamp())
    val oldData = getLoadBalanceConfig(appkey, envId)
    val newData = oldData.copy(load_balance_type = data.load_balance_type, load_balance_value = data.load_balance_value, updateTime = currTime)
    var msg = s"${Env.apply(envId)}环境"+"调整策略为："+data.load_balance_type+data.load_balance_value


    BorpClient.saveOpt(actionType = ActionType.INSERT.getIndex, entityId = appkey, entityType = EntityType.updateLoadBalanceConfig,newValue = msg)
    saveLoadBalanceConfig(appkey, envId, newData)
  }

  def saveLoadBalanceConfig(appkey: String, envId: Int, data: ServiceModels.LoadBalanceConfig) {
    val currTime = Option(getCurrTimestamp())
    val oldSharedData = getSharedHttpConfig(appkey, envId)
    val newLoadBalanceIssue = ServiceModels.LoadBalanceIssue(data.load_balance_type, data.load_balance_value)
    val newSharedData = oldSharedData.copy(load_balance_issue = newLoadBalanceIssue, updateTime = currTime)
    saveSharedHttpConfig(appkey, envId, newSharedData)
  }

}
