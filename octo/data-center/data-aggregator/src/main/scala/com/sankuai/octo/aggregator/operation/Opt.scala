package com.sankuai.octo.aggregator.operation

import com.sankuai.inf.octo.mns.util.ProcessInfoUtil
import com.sankuai.octo.aggregator.processor.StatisticService
import org.slf4j.LoggerFactory
import play.api.libs.json.Json

object Opt {
  private val logger = LoggerFactory.getLogger(this.getClass)

  case class AppkeyStatisticHostLogHost(appkey: String, statisticHosts: List[String], aggregatorHost: String)

  case class Apps(apps: List[String])

  implicit val reads = Json.reads[Apps]
  implicit val writes = Json.writes[Apps]

  /**
   * 获取appkey的statistic处理节点及本机ip
   * @param apps
   * @return
   */
  def statisticHostByAppkey(apps: List[String]) = {
    apps.map { appkey =>
      AppkeyStatisticHostLogHost(appkey, StatisticService.getStatisticServerList(appkey), ProcessInfoUtil.getHostNameInfoByIp)
    }
  }

  def getStatisticNode(json: String) = {
    try {
      Json.parse(json).validate[Apps].fold({
        error =>
          logger.error(s"statisticHostByAppkey failed", error)
          List()
      }, {
        apps =>
          statisticHostByAppkey(apps.apps)
      })
    } catch {
      case e: Exception => logger.error(s"statisticHostByAppkey failed", e)
        List()
    }
  }

}
