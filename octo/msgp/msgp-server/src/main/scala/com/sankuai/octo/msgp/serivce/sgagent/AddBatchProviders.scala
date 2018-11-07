package com.sankuai.octo.msgp.serivce.sgagent

import com.sankuai.meituan.borp.vo.ActionType
import com.sankuai.msgp.common.model.{EntityType, ServiceModels}
import com.sankuai.msgp.common.service.org.OpsService
import com.sankuai.msgp.common.utils.client.BorpClient
import com.sankuai.msgp.common.utils.helper.JsonHelper
import com.sankuai.octo.msgp.serivce.service.AppkeyProviderService
import org.slf4j.{Logger, LoggerFactory}
import play.api.libs.json.{JsError, Json}

/**
 * Created by lhmily on 10/28/2015.
 */
object AddBatchProviders {
  val LOG: Logger = LoggerFactory.getLogger(AddBatchProviders.getClass)

  case class myInputData(id: Option[String], name: Option[String], appkey: String, version: String,
                         ip: String, port: Int, weight: Int, fweight: Option[Double], status: Int, enabled: Int =  0,
                         role: Int, env: Int, lastUpdateTime: Long, trace: Option[Int], extend: String,
                         prefix: String, startNum: Int, endNum: Int)

  implicit val myInputDataReads = Json.reads[myInputData]
  implicit val myInputDataWrites = Json.writes[myInputData]

  def batchProviders(appkey: String, json: String) = {
    var list = scala.collection.mutable.LinkedList[String]()
    Json.parse(json).validate[myInputData].fold({
      error => LOG.info(error.toString)
        JsonHelper.errorJson("内部异常: " + JsError.toFlatJson(error).toString())
    }, {
      x =>
        try {
          val provider = ServiceModels.ProviderNode(x.id, x.name, x.appkey, x.version, x.ip, x.port, x.weight,x.fweight, x.status,Some(x.enabled), x.role, x.env,
            x.lastUpdateTime, x.extend,
            serviceInfo = Some(Map[String, ServiceModels.ServiceDetail]()))
          for (i <- x.startNum to x.endNum) {
            list = list :+ saveBatchProviders(x.prefix, i, provider)
          }
        } catch {
          case e: Exception => LOG.error(f"$e")
            JsonHelper.errorJson(e.getMessage)
        }

    })
    list.toList
  }

  private def saveBatchProviders(prefix: String, num: Int, x: ServiceModels.ProviderNode): String = {
    val currentTime = System.currentTimeMillis() / 1000
    val ip = OpsService.host2ip(converHostName(prefix, num))
    val updated = x.copy(lastUpdateTime = currentTime, ip = ip)

    AppkeyProviderService.addProviderByType(updated.appkey, 1, updated)
    BorpClient.saveOpt(actionType = ActionType.INSERT.getIndex, entityId = updated.appkey, entityType = EntityType.increaseProvider, newValue = Json.toJson(updated).toString)
    updated.ip
  }

  private def converHostName(prefix: String, num: Int): String = prefix + int2Str(num)

  private def int2Str(num: Int): String = if (num < 10) s"0$num" else num.toString


  def hostName2IP(prefix: String, startNum: Int, endNum: Int): List[String] = {
    var list = scala.collection.mutable.LinkedList[String]()
    for (i <- startNum to endNum) {
      val hostName = converHostName(prefix, i)
      val ip = OpsService.host2ip(hostName)
      list = list :+ s"$hostName($ip)"
    }
    list.toList
  }
}
