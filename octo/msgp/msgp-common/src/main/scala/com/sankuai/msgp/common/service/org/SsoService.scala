package com.sankuai.msgp.common.service.org

import com.sankuai.meituan.auth.vo.User
import com.sankuai.msgp.common.utils.HttpUtil
import org.slf4j.{Logger, LoggerFactory}
import play.api.libs.json.Json

import scala.collection.JavaConverters._

object SsoService {
  private val LOG: Logger = LoggerFactory.getLogger(SsoService.getClass)
  val host_url: String = "http://api.sso-in.sankuai.com"
  val clientId: String = "msgp"
  val secret: String = "b535efb74b52d3d202cb96d2e239b454"


  def getUser(mtid: String): Option[User] = {
    val headers = Map()
    val url = s"$host_url/api/userbymtid"
    val params = Map("mtid" -> mtid).asInstanceOf[Map[String, String]].asJava
    val text = HttpUtil.httpGetRequestWithBasicAuthorization(url, null, params, clientId, secret)
    if (text.contains("error")) {
      LOG.error(s"获取用户失败:$mtid,result:$text")
      None
    } else {
      try {
        val id = (Json.parse(text) \ "data" \ "entityId").as[Int]
        val login = (Json.parse(text) \ "data" \ "mtid").as[String]
        val name = (Json.parse(text) \ "data" \ "name").as[String]
        val user = new com.sankuai.meituan.auth.vo.User()
        user.setId(id)
        user.setLogin(login)
        user.setName(name)
        Some(user)
      }
      catch {
        case e: Exception =>
          LOG.info(s"获取用户失败:$mtid,result:$text", e)
          None
      }
    }

  }

  def getUserByIds(useIds: String): String = {
    val headers = Map()
    val url = s"$host_url/api/v2/users"
    val params = Map("ids" -> useIds).asJava
    HttpUtil.httpGetRequestWithBasicAuthorization(url, null, params, clientId, secret)
  }

}
