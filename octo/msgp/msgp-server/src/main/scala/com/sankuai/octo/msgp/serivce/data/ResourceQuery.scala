package com.sankuai.octo.msgp.serivce.data

import java.util.concurrent.TimeUnit

import com.ning.http.client.ProxyServer
import com.sankuai.msgp.common.utils.helper.JsonHelper
import com.sankuai.msgp.common.model.Base
import com.sankuai.msgp.common.service.org.OpsService
import com.sankuai.octo.msgp.serivce.data.DataQuery.DataSeries
import com.sankuai.octo.msgp.serivce.service.ServiceCommon
import com.sankuai.octo.statistic.util.{ExecutionContextFactory, common}
import dispatch.{Http, as, url}
import org.slf4j.{Logger, LoggerFactory}
import play.api.libs.json.Json

import scala.concurrent.Await
import scala.concurrent.duration.Duration

/**
  * Created by yves on 17/3/22.
  */
object ResourceQuery {

  private final val LOGGER: Logger = LoggerFactory.getLogger(ResourceQuery.getClass)
  private final val UTILIZATION_ENDPOINT = "utilization"
  private final val UTILIZATION_LINK_PREFIX = "http://ops.sankuai.com/servicetree"

  private val proxy = new ProxyServer("10.32.140.181", 80)

  private final val falconUrl = if (common.isOffline) {
    "http://10.5.233.113:9966"
  } else {
    "http://query.falcon.vip.sankuai.com:9966"
  }

  private implicit val timeout = Duration.create(5000L, TimeUnit.MILLISECONDS)
  private implicit val ec = ExecutionContextFactory.build(5)


  private final val FALCON_LAST_DATA_API = s"$falconUrl/graph/last"

  private final val UTILIZATION_COUNTER_DAILY_SUFFIX = ".util/unit=d"
  private final val LOW_RATE_COUNTER_DAILY_SUFFIX = ".low.rate/unit=d"


  case class UtilizationWrapper(endpoint: String, counter: String, value: DataSeries)

  implicit val utilizationWrapperReads = Json.reads[UtilizationWrapper]
  implicit val utilizationWrapperWrites = Json.writes[UtilizationWrapper]

  case class SingleParam(endpoint: String, counter: String)

  implicit val singleParamReads = Json.reads[SingleParam]
  implicit val singleParamWrites = Json.writes[SingleParam]

  case class SimpleUtilization(opsLink: String, utilizationRate: Double, lowRate: Double)


  def getUtilizationRate(appkey: String) = {
    if(common.isOffline){
      JsonHelper.dataJson(SimpleUtilization("", -1, -1))
    }else{
      val falconPost = if (common.isOffline) {
        url(FALCON_LAST_DATA_API).setProxyServer(proxy)
      } else {
        url(FALCON_LAST_DATA_API)
      }
      /** 1, 获取tags */
      //TODO: 点评测无数据

      val appkeyDesc = ServiceCommon.desc(appkey)
      if (appkeyDesc.base.isDefined && appkeyDesc.base.get.equals(Base.meituan.getId)) {
        /** 北京侧L */
        val tagOpt = OpsService.getAppkeyTag(appkey)
        tagOpt match {
          case Some(tag) =>
            val app = tag.replace("corp=", "").replace("&owt=", ".").replace("&pdl=", ".").replace("&srv=", ".")
            val utilizationCounter = s"$app$UTILIZATION_COUNTER_DAILY_SUFFIX"
            val lowRateCounter = s"$app$LOW_RATE_COUNTER_DAILY_SUFFIX"
            /** 从falcon取数据 */
            val utilizationPlayLoad = List(SingleParam(UTILIZATION_ENDPOINT, utilizationCounter), SingleParam(UTILIZATION_ENDPOINT, lowRateCounter))
            val utilizationPlayLoadJson = JsonHelper.jsonStr(utilizationPlayLoad)

            try {
              val postRequest = falconPost << utilizationPlayLoadJson
              val future = Http(postRequest OK as.String)
              val text = Await.result(future, timeout)
              LOGGER.info(s"utilization text $text")
              val utilizationOpt = Json.parse(text).asOpt[List[UtilizationWrapper]]
              utilizationOpt match {
                case Some(utilizations) =>
                  val opsLink = s"$UTILIZATION_LINK_PREFIX/$tag/$UTILIZATION_ENDPOINT"
                  JsonHelper.dataJson(SimpleUtilization(opsLink, utilizations.filter(_.counter.equalsIgnoreCase(utilizationCounter)).head.value.value,
                    utilizations.filter(_.counter.equalsIgnoreCase(lowRateCounter)).head.value.value))
                case None =>
                  JsonHelper.errorJson("从Falcon查询资源利用率失败")
              }
            } catch {
              case e: Exception =>
                LOGGER.error(s"get utilization error", e)
                JsonHelper.errorJson("从Falcon查询资源利用率失败")
            }
          case None =>
            JsonHelper.errorJson("查询tags错误")
        }
      } else {
        //上海侧
        JsonHelper.dataJson(SimpleUtilization("", -1, -1))
      }
    }
  }

  def main(args: Array[String]): Unit = {
    getUtilizationRate("com.sankuai.inf.msgp")
  }
}
