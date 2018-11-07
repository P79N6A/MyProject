package com.sankuai.octo.msgp.serivce.service

import java.util.Collections

import com.sankuai.meituan.borp.vo.ActionType
import com.sankuai.msgp.common.model.EntityType
import com.sankuai.msgp.common.utils.HttpUtil
import com.sankuai.msgp.common.utils.client.BorpClient
import com.sankuai.msgp.common.utils.helper.{CommonHelper, JsonHelper}
import com.sankuai.octo.msgp.dao.appkey.AppkeyDescDao
import com.sankuai.octo.msgp.model.EnvMap
import com.sankuai.octo.msgp.service.auth.AppkeyAuthService
import org.slf4j.{Logger, LoggerFactory}
import play.api.libs.json.Json

import scala.collection.JavaConversions._
import scala.collection.JavaConverters._

/**
  * Created by yves on 17/2/7.
  * 服务鉴权
  */
object ServiceAuth {

  private final val SPECIAL_DEPARTMENT = Set("pay", "conch")

  private val LOGGER: Logger = LoggerFactory.getLogger(ServiceAuth.getClass)

  private final val KMS_HOST_ONLINE_COMMON = "http://kms.sankuai.com"
  private final val KMS_HOST_ONLINE_PAY = "http://kms.pay.sankuai.com"
  private final val KMS_HOST_OFFLINE = "http://kms.test.sankuai.com"
  //private final val KMS_HOST_OFFLINE = "http://10.4.242.104:8999"
  private final val CLIENT_TO_REQUEST_KMS = "kms"
  private final val SECRET_TO_REQUEST_KMS = "fbfddd8ecb1454bcb7205ad3ac1793e2"

  private final val isOffline = CommonHelper.isOffline

  /**
    * 获取服务白名单
    *
    * @param appkey 服务
    */
  def getAppkeyWhiteList(appkey: String, env: String) = {
    val kmsHost = getKmsHost(appkey)
    val url = s"$kmsHost/api/admin/auth/whitelist"
    val paramMap = Map("appkey" -> appkey, "env" -> env)
    try {
      val text = HttpUtil.httpGetRequestWithBasicAuthorization(url, Map[String, String](),
        paramMap, CLIENT_TO_REQUEST_KMS, SECRET_TO_REQUEST_KMS)
      val whiteListOpt = (Json.parse(text) \ "result").asOpt[List[String]]
      whiteListOpt match {
        case Some(whiteList) =>
          JsonHelper.dataJson(whiteList)
        case None =>
          JsonHelper.errorJson("获取服务白名单失败")
      }
    } catch {
      case e: Exception =>
        LOGGER.error(s"Get authorization whitelist of $appkey failed.  $e")
        JsonHelper.errorJson("获取服务白名单失败")
    }
  }

  case class AppkeyWhiteList(appkey: String, whitelist: List[String])
  implicit val appkeyWhiteListReads = Json.reads[AppkeyWhiteList]
  implicit val appkeyWhiteListWrites = Json.writes[AppkeyWhiteList]


  /**
    * 更新服务白名单
    *
    * @param appkey  服务
    * @param allWhiteList  所有服务白名单
    */
  def updateAppkeyWhiteList(appkey: String, allWhiteList: java.util.List[String], env: String) = {
    val appkeyWhiteList = AppkeyWhiteList(appkey, allWhiteList.asScala.toList)
    val appkeyWhiteListJson = JsonHelper.jsonStr(appkeyWhiteList)
    val kmsHost = getKmsHost(appkey)
    val url = s"$kmsHost/api/admin/auth/whitelist?env=$env"

    try {
      val text = HttpUtil.httpPostRequestWithBasicAuthorization(url, appkeyWhiteListJson, CLIENT_TO_REQUEST_KMS, SECRET_TO_REQUEST_KMS)
      val whiteListOpt = (Json.parse(text) \ "result").asOpt[List[String]]
      whiteListOpt match {
        case Some(whiteList) => {
          try {
            BorpClient.saveOpt(actionType = ActionType.UPDATE.getIndex, entityId = appkey,
              entityType = EntityType.updateAppkeyAuthWhiteList,
              fieldName = EnvMap.getAliasEnv(Integer.valueOf(env)),
              newValue = whiteList.asJava.toString)
          } catch {
            case e: Exception => LOGGER.error("保存服务白名单更新操作记录失败", e)
          }
          JsonHelper.dataJson(whiteList)
        }
        case None =>
          JsonHelper.errorJson("更新服务白名单失败")
      }
    } catch {
      case e: Exception =>
        LOGGER.error(s"Update authorization whitelist of $appkey failed.  $e")
        JsonHelper.errorJson("更新服务白名单失败")
    }
  }


  /**
    * 获取服务鉴权数据
    *
    * @param appkey
    */
  def getAppkeyAuthList(appkey: String, env: String) = {
    val kmsHost = getKmsHost(appkey)
    val url = s"$kmsHost/api/admin/auth/appkey/token"
    val paramMap = Map("appkey" -> appkey, "env" -> env)
    try {
      val text = HttpUtil.httpGetRequestWithBasicAuthorization(url, Map[String, String](),
        paramMap, CLIENT_TO_REQUEST_KMS, SECRET_TO_REQUEST_KMS)
      val appkeyAuthListOpt = (Json.parse(text) \ "result").asOpt[Map[String, String]]
      appkeyAuthListOpt match {
        case Some(appkeyAuthList) => JsonHelper.dataJson(appkeyAuthList.keys.toList)
        case None =>
          JsonHelper.errorJson("获取服务鉴权数据失败")
      }
    } catch {
      case e: Exception =>
        LOGGER.error(s"Get appkey authorization of $appkey failed.  $e")
        JsonHelper.errorJson("获取服务鉴权数据失败")
    }
  }

  def getAuthenticatedClientAppkey(appkey: String, env: String): java.util.List[String] = {
    val kmsHost = getKmsHost(appkey)
    val url = s"$kmsHost/api/admin/auth/appkey/token"
    val paramMap = Map("appkey" -> appkey, "env" -> env)
    try {
      val text = HttpUtil.httpGetRequestWithBasicAuthorization(url, Map[String, String](),
        paramMap, CLIENT_TO_REQUEST_KMS, SECRET_TO_REQUEST_KMS)
      val appkeyAuthListOpt = (Json.parse(text) \ "result").asOpt[Map[String, String]]
      appkeyAuthListOpt match {
        case Some(appkeyAuthList) => appkeyAuthList.keys.toList.asJava
        case None => List.empty[String].asJava
      }
    } catch {
      case e: Exception => List.empty[String].asJava
    }
  }


  case class AppkeyAuthList(appkey: String, appkeyTokens: List[String])
  implicit val appkeyAuthListReads = Json.reads[AppkeyAuthList]
  implicit val appkeyAuthListWrites = Json.writes[AppkeyAuthList]

  /**
    * 更新服务鉴权数据
    *
    * @param appkey
    * @param allAuthList
    */
  def updateAppkeyAuthList(appkey: String, allAuthList: java.util.List[String], env: String) = {
    val appkeyAuthList = AppkeyAuthList(appkey, allAuthList.asScala.toList)
    val appkeyAuthListJson = JsonHelper.jsonStr(appkeyAuthList)
    val kmsHost = getKmsHost(appkey)
    val url = s"$kmsHost/api/admin/auth/appkey/token?env=$env"

    try {
      val text = HttpUtil.httpPostRequestWithBasicAuthorization(url, appkeyAuthListJson, CLIENT_TO_REQUEST_KMS, SECRET_TO_REQUEST_KMS)
      val appkeyAuthListOpt = (Json.parse(text) \ "result").asOpt[List[String]]
      appkeyAuthListOpt match {
        case Some(authList) =>
          try {
            BorpClient.saveOpt(actionType = ActionType.UPDATE.getIndex, entityId = appkey,
              entityType = EntityType.updateAppkeyAuth, fieldName = EnvMap.getAliasEnv(Integer.valueOf(env)),
              newValue = authList.asJava.toString)
          } catch {
            case e: Exception => LOGGER.error("保存服务鉴权更新操作记录失败", e)
          }
          AppkeyAuthService.updateAppkeyAuthList(appkey, env, allAuthList)
          JsonHelper.dataJson(authList)
        case None =>
          JsonHelper.errorJson("更新服务鉴权数据失败")
      }
    } catch {
      case e: Exception =>
        LOGGER.error(s"Update appkey authorization of $appkey failed.  $e")
        JsonHelper.errorJson("更新服务鉴权数据失败")
    }
  }


  case class SpanAuthList(appkey: String, interfaceTokens: Map[String, List[String]])
  implicit val spanAuthListReads = Json.reads[SpanAuthList]
  implicit val spanAuthListWrites = Json.writes[SpanAuthList]


  /**
    * 获取接口鉴权数据
    *
    * @param appkey
    */
  def getSpanAuthList(appkey: String, env: String) = {
    val kmsHost = getKmsHost(appkey)
    val url = s"$kmsHost/api/admin/auth/interface/token"
    val paramMap = Map("appkey" -> appkey, "env" -> env)
    try {
      val text = HttpUtil.httpGetRequestWithBasicAuthorization(url, Map[String, String](),
        paramMap, CLIENT_TO_REQUEST_KMS, SECRET_TO_REQUEST_KMS)
      val spanAuthListOpt = (Json.parse(text) \ "result").asOpt[Map[String, Map[String, String]]]
      spanAuthListOpt match {
        case Some(spanAuthList) =>
          //避免返回token
          val interfaceTokens = spanAuthList.map{
            case(interface, tokens)=>
              interface -> tokens.keys.toList
          }
          JsonHelper.dataJson(SpanAuthList(appkey, interfaceTokens))
        case None =>
          JsonHelper.errorJson("获取接口鉴权数据失败")
      }
    } catch {
      case e: Exception =>
        LOGGER.error(s"Get appkey authorization of $appkey failed.  $e")
        JsonHelper.errorJson("获取接口鉴权数据失败")
    }
  }

  def getSpanAuthMap(appkey: String, env: String): java.util.Map[String, java.util.List[String]] = {
    val kmsHost = getKmsHost(appkey)
    val url = s"$kmsHost/api/admin/auth/interface/token"
    val paramMap = Map("appkey" -> appkey, "env" -> env)
    try {
      val text = HttpUtil.httpGetRequestWithBasicAuthorization(url, Map[String, String](),
        paramMap, CLIENT_TO_REQUEST_KMS, SECRET_TO_REQUEST_KMS)
      val spanAuthListOpt = (Json.parse(text) \ "result").asOpt[Map[String, Map[String, String]]]
      spanAuthListOpt match {
        case Some(spanAuthList) =>
          //避免返回token
          spanAuthList.map({
            case(interface, tokens)=>
              interface -> tokens.keys.toList
          }).mapValues(_.asJava).asJava
        case None =>
          Collections.emptyMap()
      }
    } catch {
      case e: Exception =>
        LOGGER.error(s"Get appkey authorization of $appkey failed.  $e")
        Collections.emptyMap()
    }
  }

  /**
    * 更新接口鉴权数据
    *
    * @param json
    */
  def updateSpanAuthList(json: String) = {
    val request = Json.parse(json)
    val spanAuthDataOpt = (request \ "spanAuthData").asOpt[SpanAuthList]
    val env = (request \ "env").as[String]
    spanAuthDataOpt match {
      case Some(data) =>
        val appkey = data.appkey
        val kmsHost = getKmsHost(appkey)
        val url = s"$kmsHost/api/admin/auth/interface/token?env=$env"
        try {
          val spanAuthListJson = Json.toJson(data).toString()
          val text = HttpUtil.httpPostRequestWithBasicAuthorization(url, spanAuthListJson, CLIENT_TO_REQUEST_KMS, SECRET_TO_REQUEST_KMS)
          val spanAuthListOpt = (Json.parse(text) \ "result").asOpt[Map[String, List[String]]]
          spanAuthListOpt match {
            case Some(authList) =>
              val authMap = authList.mapValues(_.asJava)
              try {
                BorpClient.saveOpt(actionType = ActionType.UPDATE.getIndex, entityId = appkey,
                  entityType = EntityType.updateSpanAuth, fieldName = EnvMap.getAliasEnv(Integer.valueOf(env)),
                  newValue = JsonHelper.jsonStr(authMap))
              } catch {
                case e: Exception => LOGGER.error("保存服务鉴权更新操作记录失败", e)
              }
              AppkeyAuthService.updateSpanAuth(appkey, env, authMap)
              JsonHelper.dataJson(authList)
            case None =>
              JsonHelper.errorJson("更新接口鉴权数据失败")
          }
        } catch {
          case e: Exception =>
            LOGGER.error(s"Update appkey authorization of $appkey failed.  $e")
            JsonHelper.errorJson("更新接口鉴权数据失败")
        }
      case None =>
        LOGGER.error(s"Update span authorization failed. data: $json.")
        JsonHelper.errorJson("更新接口鉴权数据失败")
    }
  }

  def isSensitiveAppkey(appkey: String) = {
    val appkeyDescOpt = AppkeyDescDao.get(appkey)
    val owtOpt = appkeyDescOpt match {
      case None => None
      case Some(appkeyDesc) => Some(appkeyDesc.owt)
    }
    if(owtOpt.isDefined && SPECIAL_DEPARTMENT.contains(owtOpt.get)){
      true
    }else{
      false
    }
  }

  def getKmsHost(appkey: String) = {
    isOffline match {
      case true => KMS_HOST_OFFLINE
      case false =>
        val isSensitive = isSensitiveAppkey(appkey)
        isSensitive match {
          case true=> KMS_HOST_ONLINE_PAY
          case false=> KMS_HOST_ONLINE_COMMON
        }
    }
  }

  def getAllAppkeys = {
    ServiceCommon.apps().asScala
  }

  def searchAppkey(keyword: String) = {
    ServiceCommon.search(keyword).map(_.appkey)
  }
}
