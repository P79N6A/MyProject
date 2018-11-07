package com.sankuai.octo.aggregator

import java.util.concurrent.ConcurrentHashMap

import com.sankuai.meituan.config.listener.IConfigChangeListener
import com.sankuai.octo.aggregator.thrift.model.{SGModuleInvokeInfo, TraceThresholdLog, TraceThresholdLogList}
import com.sankuai.octo.aggregator.util.MyProxy
import com.sankuai.octo.parser.{Metric, Type}
import com.sankuai.octo.statistic.util.common
import dispatch.url
import org.apache.commons.lang.StringUtils
import org.slf4j.{Logger, LoggerFactory}
import play.api.libs.json.Json

import scala.collection.JavaConversions._
import scala.util.Random

object perf {
  private final val LOG: Logger = LoggerFactory.getLogger(perf.getClass)
  private final val projectMap = new ConcurrentHashMap[String, String]
  private final val AUTH_TOKEN: String = "5385cd607707a16953441ded"

  private final val ONLINE_HOSTS = "parser.performance.sankuai.com"
  private final val OFFLINE_HOSTS = "10.4.238.164"

  private final val defaultPerfHosts = if (common.isOffline) OFFLINE_HOSTS else ONLINE_HOSTS

  private val config = MyProxy.mcc

  var perfHosts = {
    try {
      val hosts = config.get("perf.hosts", defaultPerfHosts)
      if (StringUtils.isNotBlank(hosts)) {
        hosts.split(",").toList
      } else {
        defaultPerfHosts.split(",").toList
      }
    } catch {
      case e: Exception =>
        LOG.error("perfHosts init fail", e)
        defaultPerfHosts.split(",").toList
    }
  }

  private val clientList = perfHosts.map(initClient)

  def initClient(host: String) = {
    LOG.info(s"init perf client $host")
    new BatchStatsDClient("", host, 8889)
  }

  def selectClient() = {
    clientList.apply(Random.nextInt(clientList.size))
  }

  def host = {
    if (common.isOffline) {
      "http://10.4.233.218:8087"
    } else {
      "http://performance.sankuai.com"
    }
  }

  var init = false
  var apps = Set[String]()

  def initApp() = {
    apps = config.get("disable.apps", "").split(",").toSet
    config.addListener("disable.apps", new IConfigChangeListener() {
      def changed(key: String, oldValue: String, newValue: String) = {
        LOG.info(s"change $key $oldValue $newValue")
        apps = newValue.split(",").toSet
      }
    })
    init = true
    apps
  }

  def disableApps = if (init) apps else initApp()

  def isBlock(log: SGModuleInvokeInfo) = {
    (StringUtils.isNotBlank(log.getLocalAppKey) && disableApps.contains(log.getLocalAppKey)) ||
      (StringUtils.isNotBlank(log.getRemoteAppKey) && disableApps.contains(log.getRemoteAppKey)) ||
      (StringUtils.isNotBlank(log.getRemoteAppKey) && log.getLocalAppKey == "{octo.key}")
  }

  def procesSlowQuery(logList: TraceThresholdLogList) = {
    logList.getLogs.foreach {
      log =>
        if (StringUtils.isNotEmpty(log.getLocalAppKey)) {
          val keyPrefix = envPrefix + specName(log.getType) + "SlowQuery"
          val tags = generateTags(log)
          val localToken = getToken(log.getLocalAppKey)
          localToken.map {
            token =>
              val counterMetric = new Metric(token, Type.COUNTER, s"${keyPrefix}Count", log.getCount)
              counterMetric.setTags(tags)
              sender.asyncSend(counterMetric)
              val costMetric = new Metric(token, Type.TIMER, s"${keyPrefix}Cost", log.getCost)
              costMetric.setTags(tags)
              sender.asyncSend(costMetric)
          }
        }
        if (StringUtils.isNotBlank(log.getRemoteAppKey)) {
          val remoteKeyPrefix = envPrefix + "Remote" + specName(log.getType) + "SlowQuery"
          val tags = generateTags(log)
          val localToken = getToken(log.getRemoteAppKey)
          localToken.map {
            token =>
              val remoteCounterMetric = new Metric(token, Type.COUNTER, s"${remoteKeyPrefix}Count", log.getCount)
              remoteCounterMetric.setTags(tags)
              sender.asyncSend(remoteCounterMetric)
              val remoteCostMetric = new Metric(token, Type.TIMER, s"${remoteKeyPrefix}Cost", log.getCost)
              remoteCostMetric.setTags(tags)
              sender.asyncSend(remoteCostMetric)
          }
        }
    }
  }

  def keywords() = {
    config.get("parser.keywords", "waimai.order,logCollector").split(",").toSet
  }

  private def generateTags(log: TraceThresholdLog) = {
    val tags = Map("spanname" -> (if (log.getSpanName == null) "" else log.getSpanName), "localhost" -> (if (log.getLocalHost == null) "" else log.getLocalHost),
      "remoteApp" -> (if (log.getRemoteAppKey == null) "" else log.getRemoteAppKey), "remoteHost" -> (if (log.getRemoteHost == null) "" else log.getRemoteHost),
      "status" -> (if (log.getStatus.toString == null) "" else log.getStatus.toString))
    tags
  }

  def useTcp(appkey: String) = {
    config.get("parser.useTcp", "false") == "true" || envPrefix != "" || (appkey != null && keywords().exists(x => appkey.contains(x)))
  }

  def isDebug = {
    "true".equalsIgnoreCase(config.get("debug"))
  }

  def formatCountByLocal(log: SGModuleInvokeInfo): String = {
    val ret = getToken(log.getLocalAppKey).map[String] {
      id =>
        val metric = envPrefix + specName(log.getType) + "Count"
        val span = log.getSpanName
        val host = log.getLocalHost
        val remoteApp = log.getRemoteAppKey
        val remoteHost = log.getRemoteHost
        val status = log.getStatus
        val text = s"$id.$metric._t_spanname.$span._t_localhost.$host._t_remoteApp.$remoteApp._t_remoteHost.$remoteHost._t_status.$status"
        if (isDebug) {
          LOG.info(text)
        }
        text
    }
    ret.getOrElse("")
  }

  def formatCountByRemote(log: SGModuleInvokeInfo): String = {
    val ret = getToken(log.getRemoteAppKey).map[String] {
      id =>
        val metric = envPrefix + "Remote" + specName(log.getType) + "Count"
        val span = log.getSpanName
        val host = log.getRemoteHost
        val remoteApp = log.getLocalAppKey
        val remoteHost = log.getLocalHost
        val status = log.getStatus
        val text = s"$id.$metric._t_spanname.$span._t_localhost.$host._t_remoteApp.$remoteApp._t_remoteHost.$remoteHost._t_status.$status"
        if (isDebug) {
          LOG.info(text)
        }
        text
    }
    ret.getOrElse("")
  }

  val testIps = Set("10.32.33.153", "10.64.13.254")
  val stageIps = Set("10.32.33.152", "10.64.22.248")

  val envPrefix = {
    if (testIps.contains(common.getLocalIp)) "test"
    else if (stageIps.contains(common.getLocalIp)) "stage"
    else ""
  }

  def formatCostByLocal(log: SGModuleInvokeInfo): String = {
    val ret = getToken(log.getLocalAppKey).map {
      id =>
        val metric = envPrefix + specName(log.getType) + "Cost"
        val span = log.getSpanName
        val host = log.getLocalHost
        val remoteApp = log.getRemoteAppKey
        val remoteHost = log.getRemoteHost
        val status = log.getStatus
        val text = s"$id.$metric._t_spanname.$span._t_localhost.$host._t_remoteApp.$remoteApp._t_remoteHost.$remoteHost._t_status.$status"
        if (isDebug) {
          LOG.info(text)
        }
        text
    }
    ret.getOrElse("")
  }

  def formatCostByRemote(log: SGModuleInvokeInfo): String = {
    val ret = getToken(log.getRemoteAppKey).map {
      id =>
        val metric = envPrefix + "Remote" + specName(log.getType) + "Cost"
        val span = log.getSpanName
        val host = log.getRemoteHost
        val remoteApp = log.getLocalAppKey
        val remoteHost = log.getLocalHost
        val status = log.getStatus
        val text = s"$id.$metric._t_spanname.$span._t_localhost.$host._t_remoteApp.$remoteApp._t_remoteHost.$remoteHost._t_status.$status"
        if (isDebug) {
          LOG.info(text)
        }
        text
    }
    ret.getOrElse("")
  }

  private def specName(source: Int): String = {
    if (source == 1) "server" else "client"
  }

  def getToken(appkey: String): Option[String] = {
    if (StringUtils.isBlank(appkey) || appkey == "{octo.key}" || appkey == "\"\"") return None
    val id: String = projectMap.get(appkey)
    if (id == null) {
      LOG.warn(s"can't find $appkey from ${projectMap.size()}")
      projectMap synchronized {
        get(appkey).fold {
          LOG.info(s"create project in perf $appkey")
          create(appkey)
        } {
          currentId =>
            projectMap.put(appkey, currentId)
            Some(currentId)
        }
      }
    } else {
      Some(id)
    }
  }

  case class A(_id: String)

  def sendMetric(log: SGModuleInvokeInfo) = {
    if (StringUtils.isNotEmpty(log.getLocalAppKey)) {
      val keyPrefix = envPrefix + specName(log.getType)
      val tags = generateTags(log)
      val localToken = getToken(log.getLocalAppKey)
      localToken.map {
        token =>
          val counterMetric = new Metric(token, Type.COUNTER, s"${keyPrefix}Count", log.getCount)
          counterMetric.setTags(tags)
          sender.asyncSend(counterMetric)
          val costMetric = new Metric(token, Type.TIMER, s"${keyPrefix}Cost", log.getCost)
          costMetric.setTags(tags)
          sender.asyncSend(costMetric)
      }
    }
  }

  private def generateTags(log: SGModuleInvokeInfo) = {
    val tags = Map("spanname" -> (if (log.getSpanName == null) "" else log.getSpanName), "localhost" -> (if (log.getLocalHost == null) "" else log.getLocalHost),
      "remoteApp" -> (if (log.getRemoteAppKey == null) "" else log.getRemoteAppKey), "remoteHost" -> (if (log.getRemoteHost == null) "" else log.getRemoteHost),
      "status" -> (if (log.getStatus.toString == null) "" else log.getStatus.toString))
    tags
  }

  def create(appkey: String): Option[String] = {
    try {
      val request = url(s"$host/api/projects?access_token=$AUTH_TOKEN")
      val params = Map("name" -> Seq(appkey), "title" -> Seq(appkey), "url" -> Seq(appkey), "type" -> Seq("java"), "category" -> Seq("service"))
      val response = http.execute[String](request.setParameters(params).setContentType("application/x-www-form-urlencoded", "UTF-8").POST)
      LOG.info(s"create $appkey $response")
      response.map(x => (Json.parse(x) \ "_id").as[String])
    } catch {
      case e: Exception => LOG.error(s"create $appkey failed"); None
    }
  }

  def get(appkey: String): Option[String] = {
    try {
      val request = url(s"$host/api/projects/${appkey.toLowerCase}/?access_token=$AUTH_TOKEN")
      val response = http.execute[String](request)
      LOG.debug(s"get $appkey $response")
      response.map(x => (Json.parse(x) \ "_id").as[String])
    } catch {
      case e: Exception => LOG.error(s"get $appkey failed", e); None
    }
  }

}