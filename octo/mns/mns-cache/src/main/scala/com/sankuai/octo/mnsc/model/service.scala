package com.sankuai.octo.mnsc.model

import com.sankuai.sgagent.thrift.model.SGService
import org.apache.commons.lang3.StringUtils
import play.api.libs.json.Json.JsValueWrapper
import play.api.libs.json._

import scala.collection.JavaConverters._


object service {

  case class ServiceDetail(unifiedProto: Int)


  // enabled 0 启用 1 停用   trace 0 关闭 1 开启
  case class ProviderNode(appkey: String, version: String, ip: String, port: Int,
                          weight: Int, fweight: Option[Double], status: Int, role: Int, env: Int,
                          lastUpdateTime: Long, extend: String, serverType: Option[Int] = Some(0), protocol: Option[String] = Some(""),
                          serviceInfo: Option[Map[String, ServiceDetail]], heartbeatSupport: Option[Int] = Some(0),
                          swimlane: Option[String], cell: Option[String], groupInfo: Option[String]) {
    override def toString = try {
      val envir = Env(env);
      s"""{"appkey":"${appkey}","version":"${version}","ip":"${ip},"port":${port},"weight":${weight},"fweight":${fweight},"status:":${status},"role":$role,"envir":"${envir}","lastUpdateTime":${lastUpdateTime},"extend":"${extend}","serverType":$serverType,"protocol":"${protocol},"serviceInfo":"${serviceInfo}","heartbeatSupport":"${heartbeatSupport}","swimlane":"${swimlane}", "cell":"${cell}", "groupInfo":"${groupInfo}"}"""
    } catch {
      case e: Exception => ""
    }
  }


  case class AppkeyTs(appkey: String, lastUpdateTime: Long)

  implicit val ApppkeyTsReads = Json.reads[AppkeyTs]
  implicit val ApppkeyTsWrites = Json.writes[AppkeyTs]

  implicit val serviceDetailRepads = Json.reads[ServiceDetail]
  implicit val serviceDetailWrites = Json.writes[ServiceDetail]


  implicit val mapRepads: Reads[Map[String, ServiceDetail]] = new Reads[Map[String, ServiceDetail]] {
    override def reads(json: JsValue): JsResult[Map[String, ServiceDetail]] = JsSuccess {
      json.as[JsObject].value.map {
        case (k, v) => (k, ServiceDetail(
          (v \ "unifiedProto").as[Int]
        ))
      }.toMap
    }
  }
  implicit val mapWrites: Writes[Map[String, ServiceDetail]] = new Writes[Map[String, ServiceDetail]] {
    def writes(map: Map[String, ServiceDetail]): JsValue =
      Json.obj(map.map { case (s, o) =>
        val ret: (String, JsValueWrapper) = s -> Json.toJson(o)
        ret
      }.toSeq: _*)
  }


  case class ProviderDel(appkey: String, protocol: String, ip: String, prot: Int, env: Int)

  case class Provider(appkey: String, lastUpdateTime: Long)

  implicit val providerNodeReads = Json.reads[ProviderNode]
  implicit val providerNodeWrites = Json.writes[ProviderNode]

  implicit val providerReads = Json.reads[Provider]
  implicit val providerWrites = Json.writes[Provider]

  implicit val providerDelReads = Json.reads[ProviderDel]
  implicit val providerDelWrites = Json.writes[ProviderDel]


  case class NodeState(mtime: Long, cversion: Long, version: Long)

  implicit val nodeStateR = Json.reads[NodeState]
  implicit val nodeStateW = Json.writes[NodeState]

  case class CacheValue(version: String, SGServices: List[SGService])

  case class CacheData(version: String, Providers: List[ProviderNode], lastGetTime: Long = System.currentTimeMillis() / 1000)

  // version mtime|cversion|version

  def ProviderNode2SGService(node: ProviderNode) = {

    val service = new SGService()
    service.setAppkey(node.appkey)
      .setVersion(node.version)
      .setIp(node.ip)
      .setPort(node.port)
      .setWeight(node.weight)
      .setStatus(node.status)
      .setRole(node.role)
      .setEnvir(node.env)
      .setLastUpdateTime(node.lastUpdateTime.toInt)
      .setExtend(node.extend)
      .setFweight(node.fweight.getOrElse(0))
      .setServerType(node.serverType.getOrElse(0))
      .setProtocol(node.protocol.getOrElse(""))
      .setServiceInfo(ProviderInfo2Info(node).asJava)
      .setHeartbeatSupport(node.heartbeatSupport.getOrElse(0).toByte)
      .setSwimlane(node.swimlane.getOrElse(""))
      .setCell(node.cell.getOrElse(""))
      .setGroupInfo(node.groupInfo.getOrElse(""))
  }


  def SGService2ProviderNode(service: SGService) = {
    val serviceInfo = if (null == service.serviceInfo) {
      Some(Map[String, ServiceDetail]())
    } else {
      Some(SGServiceInfo2Info(service))
    }
    val protocolTemp = if (StringUtils.isEmpty(service.protocol)) None else Some(service.protocol)
    val swimlaneTemp = if (StringUtils.isEmpty(service.swimlane)) None else Some(service.swimlane)
    val cellTemp = if(StringUtils.isEmpty(service.cell)) None else Some(service.cell)
    val extendStr = if(null == service.extend) "" else service.extend
    val groupInfoTemp = if (StringUtils.isEmpty(service.groupInfo)) None else  Some(service.groupInfo)

    ProviderNode(service.appkey,
      service.version,
      service.ip,
      service.port,
      service.weight,
      Some(service.fweight),
      service.status,
      service.role,
      service.envir,
      service.lastUpdateTime,
      extendStr,
      Some(service.serverType),
      protocolTemp,
      serviceInfo = serviceInfo,
      heartbeatSupport = Some(service.heartbeatSupport & 0xff),
      swimlane = swimlaneTemp,
      cell = cellTemp,
      groupInfo = groupInfoTemp
    )
  }

  private def ProviderInfo2Info(node: ProviderNode) = {
    //    com.sankuai.sgagent.thrift.model.ServiceDetail
    val info = node.serviceInfo.getOrElse(Map[String, ServiceDetail]())
    info.map {
      item =>
        val value = new com.sankuai.sgagent.thrift.model.ServiceDetail()
        value.setUnifiedProto(1 == item._2.unifiedProto)
        (item._1 -> value)
    }

  }

  private def SGServiceInfo2Info(service: SGService) = {
    val serviceInfo = service.serviceInfo.asScala
    val info = scala.collection.mutable.Map[String, ServiceDetail]()
    serviceInfo.foreach { e =>
      info.put(e._1, ServiceDetail(if (e._2.isUnifiedProto) 1 else 0))
    }
    info.toMap
  }
}
