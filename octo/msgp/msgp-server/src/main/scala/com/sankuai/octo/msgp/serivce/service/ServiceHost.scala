package com.sankuai.octo.msgp.serivce.service

import java.util

import com.sankuai.inf.octo.mns.util.ProcessInfoUtil
import com.sankuai.msgp.common.model.Env
import com.sankuai.msgp.common.service.org.OpsService
import com.sankuai.msgp.common.utils.helper.CommonHelper
import com.sankuai.msgp.common.model.Path
import com.sankuai.octo.msgp.domain.HostInfo
import com.sankuai.octo.msgp.serivce.data.DataQuery
import com.sankuai.octo.msgp.serivce.sgagent.SgAgentSwitchEnv
import com.sankuai.octo.msgp.utils.client.ZkClient
import org.joda.time.DateTime
import org.slf4j.{Logger, LoggerFactory}

import scala.collection.JavaConverters._
import scala.util.parsing.json.JSON
import com.sankuai.octo.msgp.serivce.sgagent.SgAgentChecker
/**
  * Created by lhmily on 12/16/2015.
  */
object ServiceHost {
  val LOG: Logger = LoggerFactory.getLogger(ServiceHost.getClass)

  private val sankuaiPath = "/mns/sankuai"

  val hostInfoContactList = List(
    "请联系杨杰(yangjie17)、惠向波(huixiangbo)安装。",
    "请联系杨杰(yangjie17)、惠向波(huixiangbo)更新。",
    "请找杨杰(yangjie17)、惠向波(huixiangbo)解决。",
    "请找对应SRE解决。",
    "请找对应octo负责人解决。"
  )
  val emptyMsg = "--"
  val defaultValue = "无法获取"
  val errorEnv = "none"
  val falseValue = "false"
  val unknownFile = "未知文件"
  val successMsg  = "success"
  val failValue = "Fail"
  val spaceinput = "\n                             "

  def getHostInfo(ip: String) = {
    var result = ""
    var errList = List[String]()
    val isOnlineIp = ProcessInfoUtil.isOnlineHost(ip)
    val isOnlineEnv = !CommonHelper.isOffline
    if (isOnlineEnv && !isOnlineIp) {
      //线下环境的ip在线上使用
      result += "线下环境IP请在线下环境查询\n"
      val hostInfoObj = new HostInfo("线下环境IP请在线下环境查询", result, errList.asJava)
      hostInfoObj
    } else if (!isOnlineEnv && isOnlineIp) {
      //线上环境的ip在线下使用
      result += "线上环境IP请在线上环境查询\n"
      val hostInfoObj = new HostInfo("线上环境IP请在线上环境查询", result, errList.asJava)
      hostInfoObj
    } else {
      val res = JSON.parseFull(SgAgentChecker.sgagentHealthCheck(if (null != ip) {
        ip
      } else {
        ""
      }))
      res match {
        case Some(map: Map[String, Any]) => {
          // hostname
          val hostName = getHostName(ip, map)

          // idc
          val idc = getIDC(ip)

          //判断接口是否返回错误信息
          val err  = getValueWithDefault(map, "err", successMsg)
          //判断接口是否能返回正确的数据
          if (!successMsg.equals(err)) {
            result = getResultWhenIPNOTAvailable(ip)
            val envFixedErr = getEnvWhenError(ip, isOnlineEnv)
            val hostInfoObj = new HostInfo(ip, hostName, envFixedErr, idc, err, result, errList.asJava)
            hostInfoObj
          } else {
            //正常情况的返回信息
            // os
            val osVersion = getValueWithDefault(map, "os_version", defaultValue)
            val osStartTime = getValueWithDefault(map, "os_start_time", defaultValue)

            // 获取健康检查接口的文件完整性信息
            val fileIntegrity = getValueWithDefault(map, "file_integrity", falseValue)
            val fileLoss = map.getOrElse("file_loss", unknownFile).toString
            val fileRes = if ("true".equals(fileIntegrity) || "none".equals(fileLoss) || fileLoss.isEmpty) {
              "文件完整"
            } else {
              if (unknownFile.equals(fileLoss)) {
                result += s"未知文件丢失，${hostInfoContactList(3)}$spaceinput"
                errList = "file_res" +: errList
              } else {
                result += s"$fileLoss 丢失，${hostInfoContactList(3)}$spaceinput"
                errList = "file_res" +: errList
              }
              val fileIntegrityText = if ("true".equals(fileIntegrity)) "完整" else "不完整"
              val fileLossText = if ("".equals(fileLoss)) unknownFile else fileLoss

              s"文件$fileIntegrityText，缺失文件 = $fileLossText"
            }

            // 获取健康检查接口的环境信息（appenv+octoenv）
            val appenv = getValueWithDefault(map, "appenv", errorEnv)
            val env = if ( "prod".equals(appenv) || "product".equals(appenv)) {
              "prod"
            } else if ("staging".equals(appenv) || "stage".equals(appenv))  {
              "staging"
            } else if ("ppe".equals(appenv) || "beta".equals(appenv)) {
              "ppe"
            } else if ("dev".equals(appenv)) {
              "dev"
            } else if ("test".equals(appenv) || "qa".equals(appenv)) {
              "test"
            } else{
              val appenvText = if (errorEnv.equals(appenv)) {
                "未设置"
              } else {
                " = " + appenv
              }
              result += s"机器环境配置错误，appenv$appenvText，请找对应SRE修复$spaceinput"
              errList = "env" +: errList
              s"appenv$appenvText"
            }

            // 获取健康检查接口的sgagent安装信息
            val installed = getValueWithDefault(map, "installed", defaultValue)
            val sgAgentInstalled = if (installed.startsWith("cplugin")) {
              val version = getValueWithDefault(map, "sgagent_version", "未知版本")
              val versionIsNewest = getValueWithDefault(map, "version_is_newest", falseValue).toString
              if ("true".equals(versionIsNewest)) {
                "已安装了最新版本的sg_agent"
              } else if ("未知版本".equals(version)) {
                result += s"未检测出sg_agent版本，${hostInfoContactList(3)}$spaceinput"
                errList = "sg_agent_installed" +: errList
                s"安装信息 = $installed，版本信息 = $version（不是最新版本）"
              } else {
                result += s"已安装的sg_agent版本为$version，不是最新版本，${hostInfoContactList(3)}$spaceinput"
                errList = "sg_agent_installed" +: errList
                s"安装信息 = $installed，版本信息 = $version（不是最新版本）"
              }
            } else {
              result += s"未安装sg_agent，${hostInfoContactList(3)}$spaceinput"
              errList = "sg_agent_installed" +: errList
              val installedText = if (defaultValue.equals(installed)) "未安装sg_agent" else s"安装异常信息=$installed"
              s"$installedText"
            }

            //获取健康检查接口的puppet安装信息
            val puppetConfigSgagent = getValueWithDefault(map, "puppet_config_sg_agent", falseValue)
            val puppetRes = if ("true".equals(puppetConfigSgagent)) {
              val puppetRunError = map.getOrElse("puppet_run_error", -1)
              if (0 == puppetRunError) {
                "puppet正常运行"
              } else {
                result += s"puppet运行异常，${hostInfoContactList(3)}$spaceinput"
                errList = "puppet_res" +: errList
                s"puppet = $puppetConfigSgagent，puppet运行失败信息 = $puppetRunError"
              }
            } else {
              result += s"puppet未配置sg_agent，${hostInfoContactList(3)}$spaceinput"
              errList = "puppet_res" +: errList
              s"puppet = $puppetConfigSgagent"
            }

            //获取健康检查接口的rpc信息
            val getConfig = getValueWithDefault(map, "getConfig", failValue)
            val getServiceList = getValueWithDefault(map, "getServiceList",failValue)
            val rpcRes = s"获取配置 = $getConfig; 获取服务列表 = $getServiceList"
            if (failValue.equals(getConfig) && failValue.equals(getServiceList)) {
              result += s"RPC请求检查失败，无法获取配置和获取服务列表。$spaceinput"
              errList = "rpc_res" +: errList
            } else if (failValue.equals(getConfig)) {
              result += s"RPC请求检查失败，无法获取配置。$spaceinput"
              errList = "rpc_res" +: errList
            } else if (failValue.equals(getServiceList)) {
              result += s"RPC请求检查失败，无法获取服务列表。$spaceinput"
              errList = "rpc_res" +: errList
            }

            //获取磁盘状态信息
            val sysResource = getValueWithDefault(map, "sys_resource", defaultValue)
            val sysResourceRes = if ("normal".equals(sysResource)) {
              "正常"
            } else {
              val msg = JSON.parseFull(sysResource) match {
                case Some(diskMap: Map[String, Any]) =>
                  val path = getValueWithDefault(diskMap, "fulldisk", "")
                  if(path.isEmpty) {
                    "磁盘空间已满"
                  } else {
                    s"分区\'$path\'的磁盘空间已满"
                  }
                case _ => s"磁盘异常信息= $sysResource"
              }
              result += msg
              errList = "sys_resource_res" +: errList
              msg
            }

            //获取网段信息
            val ipVlan = getValueWithDefault(map, "ip_vlan", defaultValue)
            val ipVlanRes = if (ipVlan.equals(ip)) {
              "正常"
            } else {
              result += s"$ip 主机的网段不在网段列表$spaceinput"
              errList = "ipvlan_res" +: errList
              s"$ip 主机的网段不在网段列表中"
            }

            // puppet
            if (result.isEmpty) result = "一切正常"

            //其他信息展示
            //sg_agent日志统计
            val sgagentLog = getSgagentLog(map)

            //cplugin运行状态
            var cpluginRunningRes = getCpluginRunningResult(map)

            //sg_Agent运行状态
            var sgagentRunningRes = getSgagentRunningResult(map)

            // moniter
            val monitor = getValueWithDefault(map, "monitor", defaultValue)

            result = if (result.lastIndexOf("\n") > 0) result.substring(0, result.lastIndexOf("\n")) else result
            val hostInfoObj = new HostInfo(ip, hostName, env, idc, osVersion, osStartTime, fileRes,
              sgAgentInstalled, puppetRes, sgagentLog, rpcRes, cpluginRunningRes,
              sgagentRunningRes, monitor, sysResourceRes, ipVlanRes, "", result, errList.asJava)
            hostInfoObj
          }
        }
        case _ => {
          val hostInfoObj = new HostInfo(emptyMsg, "未知主机", emptyMsg, emptyMsg, emptyMsg, "暂时无法登陆该主机", errList.asJava)
          hostInfoObj
        }
      }

    }
  }

   def getResultWhenIPNOTAvailable(ip: String) = {
    val opsRes = OpsService.getOwnerByIp(ip)
     if (opsRes.isEmpty) {
       "健康检查失败，该主机不在服务树上。"
     } else {
       s"健康检查失败，暂时无法登陆该主机，${hostInfoContactList(3)}"
     }
  }

  /**
    * 获取健康检查接口的hostname信息
    * @param ip
    * @param map
    * @return
    */
  private def getHostName(ip: String, map: Map[String, Any]) = {
    val host = OpsService.ipToHost(ip)
    val hostName = if (host.equals(ip)) {
      getValueWithDefault(map, "hostname", defaultValue)
    } else {
      host
    }
    hostName
  }

  /**
    * 获取健康检查接口的idc信息
    * @param ip
    * @return
    */
  private def getIDC(ip: String) = {
    val idcTemp = CommonHelper.ip2IDC(ip)
    val idc = if ("other".equals(idcTemp)) {
      "unknown"
    } else {
      idcTemp
    }
    idc
  }

  /**
    * 当健康检查接口返回错误信息时，获取环境信息
    * @param ip
    * @param isOnlineEnv
    * @return
    */
  private def getEnvWhenError(ip: String, isOnlineEnv: Boolean) = {
    try{
      val envErr = SgAgentSwitchEnv.getCurrentEnvBySgAgent(ip)
      val envFixedErr = if (!isOnlineEnv) {
        envErr match
        {
          case "prod" => "dev"
          case "stage" => "ppe"
          case "test" => "test"
          case _ => envErr
        }
      } else {
        envErr
      }
      envFixedErr
    } catch {
      case e: Exception =>
        LOG.error(s"get env error $ip", e)
        s"环境获取失败"
    }
  }

  /**
    * 获取健康检查接口的sgagent日志信息
    * @param map
    * @return
    */
  private def getSgagentLog(map: Map[String, Any]) = {
    val logConnectMtconfigFail = map.getOrElse("log_connect_mtconfig_fail", defaultValue).toString
    val logGetServiceListFail = map.getOrElse("log_getServiceList_fail", defaultValue).toString
    val logRegisterServiceFail = map.getOrElse("log_register_service_fail", defaultValue).toString
    val logZkConnectionLost = map.getOrElse("log_zk_connection_lost", defaultValue).toString
    val logZooConnecting = map.getOrElse("log_zoo_connecting", defaultValue).toString
    val sgagentLog = s"error日志最近5分钟,连接MCC失败 = $logConnectMtconfigFail" +
      s"; 获取服务列表失败 = $logGetServiceListFail" +
      s"; 注册服务失败 = $logRegisterServiceFail" +
      s"; 连接ZK失败 = $logZkConnectionLost" +
      s"; 正在连接ZK = $logZooConnecting"
    sgagentLog
  }

  /**
    * 获取健康检查接口的Cplugin运行状态信息
    * @param map
    * @return
    */
  private def getCpluginRunningResult(map: Map[String, Any]) = {
    val cpluginStartTime = getValueWithDefault(map, "cplugin_start_time", defaultValue)
    val closeWait = getValueWithDefault(map, "close_wait", defaultValue)
    var cpluginRunningRes = s"cplugin启动时间 = $cpluginStartTime; close_wait状态（被动关闭）= $closeWait"
    cpluginRunningRes
  }

  /**
    * 获取健康检查接口的Sgagent运行状态信息
    * @param map
    * @return
    */
  private def getSgagentRunningResult(map: Map[String, Any]) = {
    val sgAgentStartTime = getValueWithDefault(map, "sg_agent_start_time", defaultValue)
    val timeWait = getValueWithDefault(map, "time_wait", defaultValue)
    var sgagentRunningRes = s"sg_agent启动时间 = $sgAgentStartTime; time_wait状态（主动关闭）= $timeWait"
    sgagentRunningRes
  }

  private def getValueWithDefault(map: Map[String, Any], key: String, default: String) = {
    var value = map.getOrElse(key, default).toString
    if (null == value || "".equals(value.trim())) {
      value = default
    }
    value
  }

  private def getThriftAppkeys(ip: String) = {
    Env.values.map {
      curEnv =>
        val servicePath = s"$sankuaiPath/$curEnv"
        val appkeys = ZkClient.children(servicePath).asScala
        val list = appkeys.filter {
          app =>
            val providerStr = Path.provider
            val providerPath = s"$servicePath/$app/$providerStr"
            val providers = ZkClient.children(providerPath)
            providers.asScala.foldLeft(false) {
              (ret, item) =>
                val curIP = ipPort2Ip(item)
                ret || ip.equals(curIP)
            }
        }.toList
        (curEnv.toString -> list)
    }.toMap
  }

  private def ipPort2Ip(ipPort: String): String = {
    try {
      //防止IP解析出错
      ipPort.split(":").apply(0)
    } catch {
      case _: Exception => ""
    }
  }

  def countHost(list: util.List[String]) = {
    val ips = list.asScala.map {
      x =>
        //println(x)
        val ip = x.split(":").head
        ip
    }.toSet
    //println(ips)
    ips.size
  }

  def main(args: Array[String]) {
    println(DateTime.now().minusDays(5))
  }

  def callCount(appkey: String, date: DateTime = DateTime.now().minusDays(1)) = {
    val stat = DataQuery.getDailyStatisticFormatted(appkey, "prod", date, "server")
    //println(stat)
    val count = if (!stat.isEmpty) stat.filter(_.spanname == "all").head.count else 0
    //println(count)
    count
  }

  def appHosts() = {
    try {
      var thriftAppCount = 0
      var httpAppCount = 0
      var thriftProviderCount = 0
      var httpProviderCount = 0
      var thriftHostCount = 0
      var httpHostCount = 0
      var thriftCall = 0L
      var httpCall = 0L
      var bothAppCount = 0
      var bothCall = 0L
      var noneAppCount = 0
      var noneCall = 0L
      List("prod").foreach {
        curEnv =>
          val servicePath = s"$sankuaiPath/$curEnv"
          val appkeys = ZkClient.children(servicePath).asScala
          appkeys.foreach {
            app =>
              val providerPath = s"$servicePath/$app/${Path.provider.toString}"
              val providers = ZkClient.children(providerPath)
              val providerHttpPath = s"$servicePath/$app/${Path.providerHttp.toString}"
              val httpProviders = ZkClient.children(providerHttpPath)
              val isThrift = (providers != null && !providers.isEmpty && (httpProviders == null || httpProviders.isEmpty))
              val isHttp = (httpProviders != null && !httpProviders.isEmpty && (providers == null || providers.isEmpty))
              val both = (providers != null && !providers.isEmpty && httpProviders != null && !httpProviders.isEmpty)
              val none = (providers == null || providers.isEmpty) && (httpProviders == null || httpProviders.isEmpty)

              val thriftHost = countHost(providers)
              val httpHost = countHost(httpProviders)
              val count = callCount(app)
              if (providers.size() > 100 || httpProviders.size() > 100) {
                LOG.warn(s"big service: $app ${providers.size} $thriftHost ${httpProviders.size} $httpHost $count")
              }
              if (isThrift) {
                LOG.info(s"thrift: $app ${providers.size} $thriftHost ${httpProviders.size} $httpHost $count")
                thriftAppCount = thriftAppCount + 1
                thriftProviderCount = thriftProviderCount + providers.size()
                thriftHostCount = thriftHostCount + thriftHost
                thriftCall = thriftCall + count
              }
              if (isHttp) {
                LOG.info(s"http: $app ${providers.size} $thriftHost ${httpProviders.size} $httpHost $count")
                httpAppCount = httpAppCount + 1
                httpProviderCount = httpProviderCount + httpProviders.size()
                httpHostCount = httpHostCount + httpHost
                httpCall = httpCall + count
              }
              if (both) {
                LOG.info(s"both: $app ${providers.size} $thriftHost ${httpProviders.size} $httpHost $count")
                thriftAppCount = thriftAppCount + 1
                thriftProviderCount = thriftProviderCount + providers.size()
                thriftHostCount = thriftHostCount + thriftHost
                //thriftCall = thriftCall + count

                httpAppCount = httpAppCount + 1
                httpProviderCount = httpProviderCount + httpProviders.size()
                httpHostCount = httpHostCount + httpHost
                //httpCall = httpCall + count

                bothAppCount = bothAppCount + 1
                bothCall = bothCall + count
              }
              if (none) {
                LOG.info(s"none: $app  ${providers.size} $thriftHost ${httpProviders.size} $httpHost $count")
                noneAppCount = noneAppCount + 1
                noneCall = noneCall + count
              }
          }
      }
      LOG.info(s"thrift $thriftAppCount $thriftProviderCount $thriftHostCount $thriftCall")
      LOG.info(s"http $httpAppCount $httpProviderCount $httpHostCount $httpCall")
      LOG.info(s"both $bothAppCount $bothCall")
      LOG.info(s"none $noneAppCount $noneCall")
    } catch {
      case e: Exception => LOG.error("appHosts:", e.getMessage)
    }
  }
}
