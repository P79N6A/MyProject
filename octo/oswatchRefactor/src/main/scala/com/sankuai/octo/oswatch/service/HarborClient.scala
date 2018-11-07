package com.sankuai.octo.oswatch.service

import com.sankuai.hulk.harbor.thrift.data.{EnvType, ErrorCode, InstancesQuery}
import com.sankuai.hulk.harbor.thrift.service.HarborService
import com.sankuai.octo.oswatch.utils.{Common, MTThriftClient}
import com.typesafe.scalalogging.LazyLogging
import scala.collection.JavaConverters._

/**
 * Created by xintao on 15/11/18.
 */
object HarborClient extends LazyLogging{
  val client = MTThriftClient.getClient[HarborService, HarborService.Iface](Common.harborAppkey, new HarborService)

  def getSGContainerName(appkey: String, idc: String, env: Int) = {
    MTThriftClient.multiTryThenCatch(client.map(_.doInstancesQuery(new InstancesQuery(appkey).setIdc(idc).setEnv(EnvType.findByValue(env)))) match {
      case None =>
        logger.error("HarborClient client get None.")
        None
      case Some(res) =>
        res.code match {
          case ErrorCode.OK =>
            Some(res.instances.asScala.map(_.containerName).toList)
          case _ =>
            logger.error(s"getInstances($appkey, $idc, $env): $res")
            None
        }
    }) { e =>
      logger.error(e.toString)
      None
    }
  }

}
