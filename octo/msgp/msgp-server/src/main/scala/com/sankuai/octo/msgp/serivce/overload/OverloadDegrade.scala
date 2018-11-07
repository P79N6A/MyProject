package com.sankuai.octo.msgp.serivce.overload

import java.util.concurrent.ConcurrentHashMap

import com.sankuai.msgp.common.config.MsgpConfig
import com.sankuai.msgp.common.model.Env
import com.sankuai.msgp.common.utils.client.Messager
import com.sankuai.msgp.common.utils.helper.{CommonHelper, JsonHelper}
import com.sankuai.octo.msgp.model.Quota.JsonDegradeAction
import com.sankuai.octo.msgp.model.{OctoEnv, WatchResult}
import com.sankuai.octo.msgp.serivce.service
import com.sankuai.octo.msgp.serivce.service.ServiceQuota
import com.sankuai.msgp.common.utils.client.Messager.{Alarm, MODE}
import com.sankuai.octo.msgp.utils.client.ZkClient
import com.sankuai.octo.oswatch.thrift.data._
import org.slf4j.{Logger, LoggerFactory}
import play.api.libs.json.{JsError, JsValue, Json}

import scala.collection.JavaConverters._
import scala.collection.mutable.{Map => MMap}
import scala.compat.Platform

object OverloadDegrade {
  val logger: Logger = LoggerFactory.getLogger(OverloadDegrade.getClass)

  val ONE_SECOND_IN_MS = 1000
  val zkPathParentTemplate = "/mns/sankuai/%s/%s/quota"
  val zkPathTemplate = "/mns/sankuai/%s/%s/quota/%s"
  val LAST_UPDATE_TIME = "lastUpdateTime"
  var alarmIntervalInMinutes: Int = MsgpConfig.get("alarmIntervalInMinutes", "30").trim.toInt
  var continuousAlarmNum: Int = MsgpConfig.get("continuousAlarmNum", "4").trim.toInt
  //qps 查询最小时间间隔 ，若过低查询结果返回空 存在问题
  var qpsQueryMinInternal: Int = MsgpConfig.get("qpsQueryMinInternal", "30").trim.toInt

  case class ResponseJson(errorCode: Int, oswatchId: Long, monitorTypeValue: Double)

  implicit val JsonResponseReader = Json.reads[ResponseJson]
  implicit val JsonResponseWriter = Json.writes[ResponseJson]

  var lastCheckTimeMap = new ConcurrentHashMap[String, Long]()
  var lastAlarmTimeMap = new ConcurrentHashMap[String, Long]()

  def overloadDegradeActionRun(json: String) = {
    Json.parse(json).validate[ResponseJson].fold({
      error => logger.info(error.toString)
        JsonHelper.errorJson("内部异常: " + JsError.toFlatJson(error).toString())
    }, { x =>
      //oswatchId => getConsumerList
      ServiceQuota.getQuotaByoswatchId(x.oswatchId) match {
        case None =>
          //无配置 ＝> 无操作
          JsonHelper.dataJson(s"The quota ${x.oswatchId} not exist")
        case Some(providerQuota) =>

          for (i <- 1 to 2) {
            //产生降级行为
            val res = watch(providerQuota)
            //写入ZK
            check(providerQuota, res._1, res._2)
            Thread sleep providerQuota.watchPeriodInSeconds
          }
          JsonHelper.dataJson("ok")
      }
    })
  }

  def watch(providerQuota: ProviderQuota) = {
    logger.info("watch is called ")
    var actions = List[Option[DegradeAction]]()
    //val logCollectorService = new LogCollectorService(log, logURL)
    val currentTimestamp = Platform.currentTime
    val lastTimestamp = if (lastCheckTimeMap.containsKey(providerQuota.id)) lastCheckTimeMap.get(providerQuota.id) else 0L
    val secondDiff = (currentTimestamp - lastTimestamp) / ONE_SECOND_IN_MS
    logger.info("lastCheckTimeMap secondDiff:" + secondDiff)
    secondDiff < providerQuota.watchPeriodInSeconds match {
      case true =>
        (actions, WatchResult.NOT_AT_TIME)
      case false => {
        lastCheckTimeMap.put(providerQuota.id, currentTimestamp)
        val qpsMap = LogCollectorService.getCurrentQPS(providerQuota.providerAppkey,
          providerQuota.method,
          OctoEnv.getEnv(providerQuota.env),
          currentTimestamp,
          providerQuota.watchPeriodInSeconds > qpsQueryMinInternal match {
            case true => providerQuota.watchPeriodInSeconds
            case false => qpsQueryMinInternal
          })
        val aliveNode = ServiceQuota.countAliveProviderNode(providerQuota.providerAppkey, providerQuota.env, providerQuota.proNumCntSwitch.getValue)
        actions = providerQuota.consumerList.asScala.filter(x => {
          qpsMap.contains(x.consumerAppkey)
        }).map { consumerQuota =>
          val currentQPS = qpsMap.get(consumerQuota.getConsumerAppkey) match {
            case None => 0L
            case Some(qpstmp) => qpstmp
          }
          logger.info("x.consumerAppkey qps:" + currentQPS)
          createDegradeAction(providerQuota, consumerQuota, currentQPS, currentTimestamp, aliveNode)
        }.toList
        (actions, WatchResult.OK)
      }
    }
  }

  def check(providerQuota: ProviderQuota, actions: List[Option[DegradeAction]], watchResult: Int): Unit = {
    logger.info("check is called ")
    watchResult match {
      case WatchResult.NOT_AT_TIME => logger.info("NOT_AT_TIME "); return
      case WatchResult.OK =>
    }
    if (actions.isEmpty) {
      logger.debug("actions is empty")
      removeDegradeNode(providerQuota.env, providerQuota.providerAppkey, providerQuota.method)
    } else {
      providerQuota.status match {
        case DegradeStatus.DISABLE =>
          logger.debug("DegradeStatus is disable")
          removeDegradeNode(providerQuota.env, providerQuota.providerAppkey, providerQuota.method)
        case DegradeStatus.ENABLE =>
          logger.debug("DegradeStatus is enable")
          updateDegradeAction(providerQuota.getEnv, providerQuota.getProviderAppkey, providerQuota.getMethod, actions)
      }
      providerQuota.alarm match {
        case AlarmStatus.DISABLE =>
        case AlarmStatus.ENABLE =>
          for (action <- actions)
            action match {
              case None =>
              case Some(actionTmp) =>
                overloadAlarm(providerQuota, actionTmp, providerQuota.status.getValue)
            }
      }
    }
  }

  private def createDegradeAction(providerQuota: ProviderQuota, consumerQuota: ConsumerQuota, currentQPS: Double, currentTimestamp: Long, aliveNode: Int) = {
    val configQPS = consumerQuota.getQPSRatio * providerQuota.QPSCapacity * aliveNode
    if (configQPS < currentQPS) {
      val degradeRatio = (currentQPS - configQPS) / currentQPS
      Some(genDegradeAction(providerQuota: ProviderQuota, generateDegradeActionId(providerQuota, consumerQuota.consumerAppkey), consumerQuota.getConsumerAppkey, degradeRatio, consumerQuota.getDegradeStrategy, consumerQuota.getDegradeRedirect, currentQPS, currentTimestamp))
    } else {
      None
    }
  }

  private def genDegradeAction(providerQuota: ProviderQuota, actionId: String, cAppkey: String, ratio: Double, strategy: DegradeStrategy, redirect: String, currentQPS: Double, currentTimestamp: Long): DegradeAction = {
    val action: DegradeAction = new DegradeAction
    action.setId(actionId).setConsumerAppkey(cAppkey).setTimestamp(currentTimestamp).setDegradeRatio(ratio).setDegradeStrategy(strategy).setMethod(providerQuota.getMethod).setProviderAppkey(providerQuota.getProviderAppkey).setEnv(providerQuota.getEnv).setConsumerQPS(currentQPS.intValue).setDegradeEnd(providerQuota.getDegradeEnd)
    if (redirect != null) action.setDegradeRedirect(redirect)
    action
  }

  private def generateDegradeActionId(providerQuota: ProviderQuota, cAppkey: String): String = {
    cAppkey + '/' + providerQuota.getMethod + '/' + providerQuota.getId
  }

  def updateDegradeAction(actionJson: String, env: Int, providerAppkey: String, method: String) = {
    logger.info(s"updateDegradeAction $actionJson")
    val zkPath = zkPathTemplate.format(Env.apply(env), providerAppkey, method)
    if (ZkClient.exist(zkPath))
      ZkClient.setData(zkPath, actionJson)
    else
      ZkClient.create(zkPath, actionJson)

    val zkParentPath = zkPathParentTemplate.format(Env.apply(env), providerAppkey, method)
    ZkClient.setData(zkParentPath, Json.obj((LAST_UPDATE_TIME, System.currentTimeMillis())).toString())
  }

  def updateDegradeAction(env: Int, providerAppkey: String, method: String, actions: List[Option[DegradeAction]]): Unit = {
    logger.info(s"updateDegradeAction $actions")
    val actionMap = MMap[(Int, String), Seq[DegradeAction]]()

    //actions to Map, evn+proAppkey is key, action is value
    actions.foreach {
      case None =>
      case Some(degradeAction) =>
        val key = (degradeAction.getEnv, degradeAction.getProviderAppkey)
        val list = actionMap.get(key) match {
          case Some(list) => list :+ degradeAction
          case None => List(degradeAction)
        }

        actionMap.put(key, list)
    }

    //merge degrade actions which have same env & provider app key into ONE json object
    actionMap.foreach { pair =>
      val ((env, providerAppkey), actions) = pair

      val zkPath = zkPathTemplate.format(Env.apply(env), providerAppkey, method)
      val json = getJsonArray(actions)

      if (ZkClient.exist(zkPath))
        ZkClient.setData(zkPath, json.toString())
      else
        ZkClient.create(zkPath, json.toString())
    }

    val zkParentPath = zkPathParentTemplate.format(Env.apply(env), providerAppkey, method)
    ZkClient.setData(zkParentPath, Json.obj((LAST_UPDATE_TIME, System.currentTimeMillis())).toString())
  }

  def checkAlarm(env: Int, providerAppkey: String, method: String) = {
    val zkPath = zkPathTemplate.format(Env.apply(env), providerAppkey, method)
    ZkClient.exist(zkPath)
  }

  def removeDegradeNode(env: Int, providerAppkey: String, method: String): Unit = {
    val path = zkPathTemplate.format(Env.apply(env), providerAppkey, method)
    if (ZkClient.exist(path))
      ZkClient.deleteWithChildren(path)
  }

  private def getJsonArray(actions: Seq[DegradeAction]): JsValue = {
    Json.toJson(actions.map { da =>
      val consumerAppkey = if (da.consumerAppkey == "unknownService") "" else da.consumerAppkey
      JsonDegradeAction(
        id = da.getId,
        env = da.getEnv,
        providerAppkey = da.getProviderAppkey,
        consumerAppkey = consumerAppkey,
        method = da.getMethod,
        degradeRatio = da.getDegradeRatio,
        degradeStrategy = da.getDegradeStrategy.getValue,
        timestamp = da.getTimestamp,
        degradeRedirect = if (da.isSetDegradeRedirect) Some(da.getDegradeRedirect) else None,
        degradeEnd = da.getDegradeEnd.getValue,
        extend = ""
      )
    })
  }

  // 向appkey owner发送警报信息
  def doOverloadAlarmAction(action: DegradeAction, degradeStatus: Int) = {
    logger.info("overloadAlarm is called ")
    val degradeRatio = (action.degradeRatio * 10000).floor / 100
    val consumerQuotaQPS = ((1 - action.degradeRatio) * action.consumerQPS).ceil.toInt
    //    val degradeEnd = action.degradeEnd match {
    //      case DegradeEnd.SERVER => "过载保护发生在服务端"
    //      case DegradeEnd.CLIENT => "过载保护发生在客户端"
    //    }
    val msgstrategy = action.degradeStrategy.getValue match {
      case 0 => s"降级策略为Drop,即当前" + degradeRatio + "%的请求将被丢弃."
      case _ => s"降级策略为Customize,即当前" + degradeRatio + "%的请求将被降级,被降级请求会抛出异常,需Owner自己捕获处理." //当前"+n+"%的请求将被导向地址\""+x.degradeRedirect+"\"。"
    }
    val envDes = if (!CommonHelper.isOffline) {
      action.env match {
        case 1 => "线上test环境下,"
        case 2 => "线上stage环境下,"
        case 3 => "线上prod环境下,"
      }
    } else {
      action.env match {
        case 1 => "线下test环境下,"
        case 2 => "线下stage环境下,"
        case 3 => "线下prod环境下,"
      }
    }
    val alarmMessage = degradeStatus match {
      case 0 => //"报警＋降级"
        s"过载警报:\n ${envDes} consumer(${action.consumerAppkey})对provider(${action.providerAppkey})的方法(${action.method})请求的当前QPS为${action.consumerQPS}," +
          s"已超过给定的配额${consumerQuotaQPS},出现过载; ${msgstrategy} 降级开关已打开,该降级将被执行."
      case 1 =>
        s"过载警报:\n ${envDes} consumer(${action.consumerAppkey})对provider(${action.providerAppkey})的方法(${action.method})请求的当前QPS为${action.consumerQPS}," +
          s"已超过给定的配额${consumerQuotaQPS},出现过载; ${msgstrategy} 降级开关未打开,建议RD将其打开或者使用一键截流功能."
    }
    logger.info("msgToProvider: " + alarmMessage)
    cutFlowAlarm(List(action.providerAppkey), alarmMessage)
    logger.info("send to provider")
    cutFlowAlarm(List(action.consumerAppkey), alarmMessage)
    logger.info("send to comsumer")
    JsonHelper.dataJson("ok")
  }


  /**
    * 执行一键截流报警 ,给服务负责人发送报警
    */
  def cutFlowAlarm(appkeys: List[String], alarmMessage: String) {
    try {
      val appkeyIdList = appkeys.flatMap { appkey =>
        service.ServiceCommon.desc(appkey).owners.map(x => x.id).distinct
      }
      logger.info("appkeyIdList: " + appkeyIdList.distinct)
      val appkeyAlarm = Alarm("一键截流报警", alarmMessage, null)
      logger.info("appkeyAlarm: " + appkeyAlarm)
      val modes = Seq(MODE.XM)
      Messager.sendAlarm(appkeyIdList.distinct, appkeyAlarm, modes)
    } catch {
      case e: Exception => logger.error(s"Get $appkeys owner exception")
    }
  }

  def cutFlowAlarm(appkeys: java.util.List[String], alarmMessage: String,flag : Boolean) : Unit = {
    cutFlowAlarm(appkeys.asScala.toList,alarmMessage)
  }

  def overloadAlarm(providerQuota: ProviderQuota, action: DegradeAction, degradeStatus: Int): Unit = {
    val actionKey = makeKey(action)
    val currentTimestamp = Platform.currentTime

    if (lastAlarmTimeMap.containsKey(actionKey)) {
      val lastTimestamp = lastAlarmTimeMap.get(actionKey)
      val timeDiff = (currentTimestamp - lastTimestamp) / ONE_SECOND_IN_MS
      logger.info("lastAlarmTimeMap timeDiff:" + timeDiff)
      //据上次报警>30分钟 报警
      if (timeDiff > alarmIntervalInMinutes * 60) {
        logger.info(">30 minutes, alarm")
        lastAlarmTimeMap.put(actionKey, currentTimestamp)
        doOverloadAlarmAction(action, degradeStatus)
      } else if (timeDiff > providerQuota.watchPeriodInSeconds * continuousAlarmNum) {
        //30分钟以内 且连续报警四次以上 退出 不报警
        logger.info("In 30 minutes,alarm num > 4,no alarm")
        return
      } else {
        logger.info("In 30 minutes,alarm num < 4,alarm")
        doOverloadAlarmAction(action, degradeStatus)
      }
    } else {
      //第一次 报警
      logger.info("first overload")
      lastAlarmTimeMap.put(actionKey, currentTimestamp)
      doOverloadAlarmAction(action, degradeStatus)
    }
  }

  def makeKey(action: DegradeAction) = {
    action.env + '|' + action.consumerAppkey + '|' +
      action.providerAppkey + '|' + action.method
  }

  def getDegradeAction(env: Int, providerAppkey: String, method: String) = {
    val path = zkPathTemplate.format(Env.apply(env), providerAppkey, method)
    if (ZkClient.exist(path)) {
      ZkClient.getData(path)
    } else {
      ""
    }
  }
}
