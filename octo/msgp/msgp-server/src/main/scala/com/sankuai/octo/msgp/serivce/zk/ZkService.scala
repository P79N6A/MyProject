package com.sankuai.octo.msgp.serivce.zk

import com.sankuai.msgp.common.model.ServiceModels
import com.sankuai.octo.msgp.utils.client.ZkClient
import org.slf4j.{Logger, LoggerFactory}
import play.api.libs.json.Json

import scala.collection.JavaConverters._

/**
 * 通过zk获取服务提供者列表
 */
object ZkService {
  private val LOG: Logger = LoggerFactory.getLogger(ZkService.getClass)

  def getProviderByPath(providerPath: String) = {
    val providerNodeList = ZkClient.children(providerPath).asScala.sorted
    if (providerNodeList.isEmpty) {
      List()
    } else {
      providerNodeList.flatMap { node => {
        try {
          val data = ZkClient.getData(s"$providerPath/$node")
          Json.parse(data).validate[ServiceModels.ProviderNode].asOpt.map { x => x }
        } catch {
          case e: Exception => LOG.error(s"获取服务节点失败,providerPath:$providerPath", e);
            None
        }
      }
      }.toList
    }
  }
}
