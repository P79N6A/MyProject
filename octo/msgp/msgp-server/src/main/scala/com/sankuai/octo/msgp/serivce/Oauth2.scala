package com.sankuai.octo.msgp.serivce

import java.util.UUID
import java.util.regex.Pattern

import com.sankuai.meituan.auth.vo.User
import com.sankuai.msgp.common.config.DbConnection
import com.sankuai.msgp.common.utils.helper.{CommonHelper, JsonHelper}
import com.sankuai.msgp.common.config.db.msgp.Tables.{Oauth2Client, Oauth2ClientRow, Oauth2Token, _}
import com.sankuai.octo.msgp.serivce.service.ServiceCommon
import org.apache.commons.lang.StringUtils
import org.joda.time.DateTime
import org.slf4j.{Logger, LoggerFactory}
import play.api.libs.json.Json

import scala.slick.driver.MySQLDriver.simple._

object Oauth2 {
  val LOG: Logger = LoggerFactory.getLogger(Oauth2.getClass)
  private val db = DbConnection.getPool()

  case class Authorize(response_type: String, client_id: String, redirect_uri: String, scope: List[String])
  implicit val AuthorizeReads = Json.reads[Authorize]
  implicit val AuthorizeWrites = Json.writes[Authorize]

  def token(json: String, user: User):String = {
    try {
      val authorize = Json.parse(json).validate[Authorize].get
      token( authorize.response_type,authorize.client_id, authorize.redirect_uri, user, authorize.scope)
    }
    catch {
      case  e : Exception => return JsonHelper.errorJson("wrong post parameters")
    }
  }

  def token(responseType: String, clientId: String, redirectUri: String, user: User, scope: List[String]):String = {
    if (responseType == "code") {
      return Oauth2.getCode(clientId, redirectUri, user, scope)
    }
    else if (responseType == "token") {
      return Oauth2.getToken(clientId, redirectUri, user, scope)
    }
    else {
      val url = s"${redirectUri}?error=wrong redirect_type"
      return JsonHelper.dataJson(url)
    }
  }

  /** implicit模式下直接返回token,采用Fragment模式*/
  def getToken(clientId: String, redirectUri: String, user: User, scope: List[String]):String = {
    val clientList = getClientByClientId(clientId)
    if(clientList.isEmpty) {
      val url = s"${redirectUri}#error=wrong client_id"
      return JsonHelper.dataJson(url)
    }
    val client = clientList(0)
    val verify = checkRedirectUri(redirectUri, clientList)
    if(verify) {
      val token = UUID.randomUUID().toString
      val scopeStr = scope.mkString(",")
      insertOauth2Scope( scope.map( x => Oauth2TokenRow(0, "", token, user.getId, clientId, x) ).toList )
      val url = s"${redirectUri}#access_token=${token}&scope=${scopeStr}"
      return JsonHelper.dataJson(url)
    } else {
      val url = s"${redirectUri}#error=wrong redirect_uri"
      return JsonHelper.dataJson(url)
    }
  }

  /** code模式下返回code，直接放在url参数中*/
  def getCode(clientId: String, redirectUri: String, user: User, scope: List[String]):String = {
    val clientList = getClientByClientId(clientId)
    if(clientList.isEmpty) {
      val url = s"${redirectUri}?error=wrong client_id"
      return JsonHelper.dataJson(url)
    }
    val client = clientList(0)
    val verify = checkRedirectUri(redirectUri, clientList)
    if(verify) {
      val code = UUID.randomUUID().toString
      val createTime = DateTime.now().getMillis
      insertOauth2Scope( scope.map( x => Oauth2TokenRow(0, code, "", user.getId, clientId, x, createTime) ).toList )
      val url = s"${redirectUri}?code=${code}"
      return JsonHelper.dataJson(url)
    } else {
      val url = s"${redirectUri}?error=wrong redirect_uri"
      return JsonHelper.dataJson(url)
    }
  }

  /** 与客户端后台交互*/
  def getAccessToken(grantType: String, code: String, clientId: String, clientSecret: String, redirectUri: String): String = {
    if(grantType != "authorization_code") {
      return JsonHelper.errorJson("wrong authorization_code")
    }

    val clientList = getClientByClientId(clientId)
    if(clientList.isEmpty) {
      return JsonHelper.errorJson("wrong client_id")
    }

    val client = clientList(0)
    if(client.secret != clientSecret) {
      return JsonHelper.errorJson("wrong client_secret")
    }

    val verify = checkRedirectUri(redirectUri, clientList)

    if(!verify) {
      return JsonHelper.errorJson("wrong redirect_uri")
    } else {
      val oauth2 = getAuth2ByCode(code)
      if(oauth2.isEmpty) {
        return JsonHelper.errorJson("wrong code or code expired or code had been used")
      } else {
        val now = DateTime.now.getMillis
        val expired = 10 * 60 * 1000
        if(now - oauth2(0).codeCreatetime > expired) {
          deleteToken(code)
          return JsonHelper.errorJson("code expired")
        } else {
          val scope = oauth2.map(_.scope).toList.mkString(",")
          val token = UUID.randomUUID().toString
          updateToken(code, token)
          return JsonHelper.dataJson(Map("access_token" -> token, "scope" -> scope))
        }
      }
    }
  }

  /** 校验注册uri和回调uri是否一致 */
  def checkRedirectUri(redirectUri: String, client: List[Oauth2ClientRow]):Boolean = {
    /** 获取注册时的uri */
    val clientUri = client.map(_.uri)

    /** 获取回调uri */
    val pattern = Pattern.compile("(.+?)://(.+?)/")
    val matcher = pattern.matcher(redirectUri)
    val domain = if (matcher.find) { matcher.group(2) } else { "" }

    if( clientUri.isEmpty ||  StringUtils.isBlank(domain)) {
      return true
    }
    if(clientUri.contains(domain)) {
      return true
    } else {
      return false
    }
  }

  def checkTokenAppkey(accessToken: String, appkey: String) = {
    val oauth2 = getAuth2ByToken(accessToken)
    if( oauth2.isEmpty ) {
      (false, JsonHelper.errorJson("wrong access_token"))
    } else if(!oauth2.map(_.scope).contains(appkey)) {
      (false, JsonHelper.errorJson(s"${appkey} permission denied"))
    } else {
      (true, JsonHelper.errorJson(""))
    }
  }

  def getDescByToken(accessToken: String, appkey: String) = {
    //根据Token获取可访问的appkey
    val (verify, result) = checkTokenAppkey(accessToken, appkey)
    if(verify) {
      JsonHelper.dataJson(CommonHelper.toJavaMap(ServiceCommon.desc(appkey)))
    } else {
      result
    }
  }

  def getClientByClientId(clientId: String) = {
    var result = List[Oauth2ClientRow]()
    val start = new DateTime().getMillis
    db withSession {
      implicit session: Session =>
        val client = Oauth2Client.filter(x => x.clientId === clientId).list
        result = client
    }
    val end = new DateTime().getMillis
    LOG.info(s"getClientByClientId cost ${end - start}")
    result
  }

  def getAuth2ByCode(code: String) = {
    db withSession {
      implicit session: Session =>
        /** status等于0表示code未被使用过*/
        val oauth2 = Oauth2Token.filter(x => x.code === code && x.status === 0).list
        oauth2
    }
  }

  def getAuth2ByToken(token: String) = {
    db withSession {
      implicit session: Session =>
        val oauth2 = Oauth2Token.filter(x => x.token === token).list
        oauth2
    }
  }

  def insertOauth2Scope(list: List[Oauth2TokenRow]) = {
    db withSession {
      implicit session: Session =>
        Oauth2Token ++= list
    }
  }

  def deleteToken(code: String) = {
    db withSession {
      implicit session: Session =>
        Oauth2Token.filter(x => x.code === code).delete
    }
  }

  def updateToken(code: String, token: String) = {
    db withSession {
      implicit session: Session =>
        Oauth2Token.filter(x => x.code === code).map( x => (x.token, x.status)).update(token, 1)
    }
  }

  def main(args: Array[String]) {
    val clientId = "com.sankuai.inf.kms"
  }
}
