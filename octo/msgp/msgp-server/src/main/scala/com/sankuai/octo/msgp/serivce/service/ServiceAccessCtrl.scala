package com.sankuai.octo.msgp.serivce.service

import java.io.IOException

import com.sankuai.meituan.auth.util.UserUtils
import com.sankuai.msgp.common.model.Env
import com.sankuai.msgp.common.utils.helper.JsonHelper
import com.sankuai.octo.msgp.utils.client.ZkClient
import org.apache.zookeeper.KeeperException
import org.apache.zookeeper.KeeperException.NoNodeException
import org.slf4j.{Logger, LoggerFactory}
import play.api.libs.json.Json

/**
 * Created by lhmily on 09/28/2015.
 */
object ServiceAccessCtrl {
  val LOG: Logger = LoggerFactory.getLogger(ServiceAccessCtrl.getClass)
  val sankuaiPath = "/mns/sankuai"
  val prodPath = List(sankuaiPath, Env.prod).mkString("/")
  val stagePath = List(sankuaiPath, Env.stage).mkString("/")
  val testPath = List(sankuaiPath, Env.test).mkString("/")

  case class AccessData(user: Option[String], updateTime: Option[Long], status: Int, ips: List[String])

  implicit val accessDataRead = Json.reads[AccessData]
  implicit val accessDataWrite = Json.writes[AccessData]


  @throws(classOf[NoNodeException])
  @throws(classOf[IllegalArgumentException])
  @throws(classOf[IOException])
  def getAccessData(appkey: String, env: Int, tag: Int) = {
    if (env <= 0 || env > 3) throw new IllegalArgumentException("env range is [1,3] ")
    if (tag < 0 || tag > 1) throw new IllegalArgumentException("tag must be 0 or 1")
    val envDes = Env(env).toString
    val childNode = if (0 == tag) "consumer" else "provider"
    val path = List(sankuaiPath, envDes, appkey, "auth", childNode).mkString("/")
    if (!ZkClient.exist(path)) throw KeeperException.create(KeeperException.Code.NONODE, path)
    try {
      JsonHelper.toObject(ZkClient.getData(path), classOf[AccessData])
    } catch {
      case _: Throwable => throw new IOException("illegal zk data")
    }
  }

  def saveRegistryData(appkey: String, env: Int, provider_consumer: String, json: String): Boolean = {
    Json.parse(json).validate[AccessData].fold({
      error => LOG.info(error.toString)
        return false
    }, {
      x =>
        val envDes = Env(env).toString
        val currentTime = System.currentTimeMillis() / 1000
        val user = UserUtils.getUser
        val misID = user.getLogin
        val userName = user.getName
        val data = x.copy(user = Option(s"$userName($misID)"), updateTime = Option(currentTime))
        val path = s"$sankuaiPath/$envDes/$appkey/auth/$provider_consumer"
        val data_str = Json.prettyPrint(Json.toJson(data))
        try {
          if (!ZkClient.exist(path)) {
            ZkClient.create(path, data_str)
          } else {
            ZkClient.setData(path, data_str)
          }
        } catch {
          case _: Exception => return false
        }
        return true
    })
  }
}
