package com.sankuai.msgp.common.model

import com.sankuai.msgp.common.config.db.msgp.Tables.{AppkeyDescRow, AppkeyProviderRow}
import com.sankuai.msgp.common.service.org.{BusinessOwtService, OpsService}
import com.sankuai.msgp.common.utils.helper.JsonHelper
import com.sankuai.sgagent.thrift.model.SGService
import org.apache.commons.lang3.StringUtils
import org.apache.zookeeper.data.Stat
import org.slf4j.{Logger, LoggerFactory}
import play.api.libs.json.Json.JsValueWrapper
import play.api.libs.json._

import scala.collection.JavaConverters._

object ServiceModels {
  val LOG: Logger = LoggerFactory.getLogger(ServiceModels.getClass)

  case class User(id: Int, login: String, name: String) {
    def equal(user: User): Boolean = {
      user.id == this.id && user.login == this.login && user.name == this.name
    }
  }

  case class ServiceDetail(unifiedProto: Int)

  //用于api注册服务
  case class DescSimple(name: Option[String] = Some(""), appkey: String, baseApp: Option[String] = Some(""), owners: List[String], observers: List[String], intro: String, category: Option[String] = Some(""),
                        business: Option[Int] = Some(0), group: Option[String] = Some(""),
                        base: Option[Int] = Some(0), owt: Option[String] = Some(""), pdl: Option[String] = Some(""),
                        level: Option[Int], tags: String, regLimit: Int = 0, createTime: Option[Long]) {
    def toRich(owners: List[User], observers: Option[List[User]] = Some(List())): Desc = Desc(name.getOrElse(""), appkey, baseApp, owners, observers, intro, category.getOrElse(""),
      business, group, base, owt, pdl,
      Some(level.getOrElse(0)), tags, regLimit, Some(createTime.getOrElse(0)))
  }

  case class Desc(name: String, appkey: String, baseApp: Option[String] = Some(""), owners: List[User], observers: Option[List[User]] = Some(List()), intro: String, category: String,
                  business: Option[Int] = Some(0), group: Option[String] = Some(""), base: Option[Int] = Some(0), owt: Option[String] = Some(""), pdl: Option[String] = Some(""),
                  level: Option[Int] = Some(0), tags: String, regLimit: Int = 0, createTime: Option[Long] = Some(0)) {
    def ownerId: String = owners.map(_.id).mkString(",")

    def owner: String = owners.map(x => x.name + "(" + x.login + ")").mkString(",")

    def ownerString: String = owners.map(x => x.name).mkString(",")

    def observer: String = {
      if (None != observers) {
        observers.get.map(x => x.name + "(" + x.login + ")").mkString(",")
      } else {
        ""
      }

    }

    def observerId: String = {
      if (None != observers) {
        observers.get.map(_.id).mkString(",")
      } else {
        ""
      }
    }

    def toAppkeyDescRow: AppkeyDescRow = AppkeyDescRow(0L, name, base.getOrElse(0), appkey, baseApp.getOrElse(""), JsonHelper.jsonStr(owners), JsonHelper.jsonStr(observers.getOrElse(List[User]())),
      pdl.getOrElse(""), owt.getOrElse(""), intro, tags, business.getOrElse(0), category, regLimit, createTime.getOrElse(0L))

    def toRich: DescRich = {
      val base_str = if (base.getOrElse(0) == 0) {
        Base.meituan.getName
      } else {
        Base.dianping.getName
      }
      val owtBussiness = BusinessOwtService.getBusiness(base_str, owt.getOrElse(""))
      DescRich(name, appkey, baseApp, owners, observers, intro, category, Some(business.getOrElse(0)),
        owtBussiness,
        group.getOrElse(""), base, owt, pdl,
        Some(level.getOrElse(0)), Level.apply(level.getOrElse(0)).toString, tags, regLimit, createTime)
    }
  }

  case class DescRich(name: String, appkey: String, baseApp: Option[String] = Some(""), owners: List[User], observers: Option[List[User]] = Some(List()), intro: String, category: String,
                      business: Option[Int], businessName: String, group: String, base: Option[Int] = Some(0), owt: Option[String] = Some(""), pdl: Option[String] = Some(""),
                      level: Option[Int], levelName: String, tags: String, regLimit: Int = 0, createTime: Option[Long])

  // enabled 0 启用 1 停用
  case class ProviderNode(id: Option[String], name: Option[String], appkey: String, version: String,
                          ip: String, port: Int, weight: Int, fweight: Option[Double], status: Int, enabled: Option[Int] = Some(0),
                          role: Int, env: Int, lastUpdateTime: Long, extend: String,
                          serverType: Option[Int] = Some(0), protocol: Option[String] = Some(""), groupInfo: Option[String] = Some(""),swimlane: Option[String] = Some(""), cell: Option[String] = Some(""),
                          serviceInfo: Option[Map[String, ServiceDetail]], heartbeatSupport: Option[Int] = Some(0)) {
    def toRich = ProviderRich(id, name, appkey, version, ip, port, weight, status, Status.apply(status).toString, enabled.getOrElse(0), role, env, lastUpdateTime, extend)

    def toAppkeyProvider = AppkeyProviderRow(0L, appkey, version, ip, ip, port, `type` = "thrift", weight, fweight.getOrElse(0.0), status, enabled.getOrElse(0), role, env, lastUpdateTime, extend, serverType.getOrElse(0),
      protocol.getOrElse(""), swimlane.getOrElse(""), JsonHelper.jsonStr(serviceInfo.getOrElse(Map[String, ServiceDetail]())), heartbeatSupport.getOrElse(0), "OTHER")

    override def toString = try {
      List(id.getOrElse(""), name.getOrElse(""), version, ip, s"port:${port}", s"weight:${weight}", Status.apply(status), if (role == 0) "主用" else if (role == 1) "备机" else "未知").mkString("|~|")
    } catch {
      case e: Exception => ""
    }


    def toEdit = ProviderEdit(appkey, ip, port, env, Some(weight), fweight, Some(status), enabled, Some(role), groupInfo,swimlane, cell, Some(extend))

    def toSimpe = ProviderSimple(appkey, ip, port, weight, status, Status.apply(status).toString, enabled.getOrElse(0), env, swimlane.getOrElse(""))

    def toNodeDesc = ProviderNodeDesc(name, appkey, version, ip, port, weight, fweight, status, Status.apply(status).toString, enabled, role, env, lastUpdateTime, extend, serverType,
      protocol, groupInfo, swimlane, cell, serviceInfo, heartbeatSupport)

  }

  case class ProviderEdit(appkey: String, ip: String, port: Int, env: Int, weight: Option[Int], fweight: Option[Double], status: Option[Int],
                          enabled: Option[Int] = Some(0), role: Option[Int],groupInfo: Option[String] = Some(""), swimlane: Option[String] = Some(""), cell: Option[String] = Some(""), extend: Option[String])

  case class ProviderSimple(appkey: String, ip: String, port: Int, weight: Int, status: Int, statusDesc: String,
                            enabled: Int, env: Int, swimlane: String)

  case class ProviderRich(id: Option[String], name: Option[String], appkey: String, version: String,
                          ip: String, port: Int, weight: Int, status: Int, statusDesc: String,
                          enabled: Int, role: Int, env: Int, lastUpdateTime: Long,
                          extend: String)

  case class ProviderNodeDesc(name: Option[String], appkey: String, version: String,
                              ip: String, port: Int, weight: Int, fweight: Option[Double], status: Int, statusDesc: String, enabled: Option[Int] = Some(0),
                              role: Int, env: Int, lastUpdateTime: Long, extend: String,
                              serverType: Option[Int] = Some(0), protocol: Option[String] = Some(""),groupInfo: Option[String] = Some(""),swimlane: Option[String] = Some(""), cell: Option[String] = Some(""),
                              serviceInfo: Option[Map[String, ServiceDetail]], heartbeatSupport: Option[Int] = Some(0))

  case class ProviderWithZkNode(providerNode: ProviderNode, zkNode: Stat)

  case class AppkeyTs(appkey: String, lastUpdateTime: Long)

  case class ProviderHost(ip: String, port: Int, localhost: String, hostname: String)

  case class ProviderStatus(ip: String, status: Map[String, Boolean])

  case class ProviderProtocolStatus(ip: String, protocol: String, status: Boolean)

  case class ProviderNodeProtocolStatus(ip: String, port: Int, service: String, status: Int)


  implicit val providerStatusReads = Json.reads[ProviderStatus]
  implicit val providerStatusWrites = Json.writes[ProviderStatus]

  case class AppkeyProvider(appkey: String, ips: List[ProviderHost])


  case class AppkeyIps(appkey: String, ips: List[String])

  case class usernameIPs(username: String, ips: List[String])

  case class AppkeyShutDown(appkey: String, shutdown: Boolean)

  case class IpShutDown(ip: String, appkeyShutDown: List[AppkeyShutDown])

  implicit val appkeyShutDownReads = Json.reads[AppkeyShutDown]
  implicit val appkeyShutDownWrites = Json.writes[AppkeyShutDown]

  implicit val ipShutdownReads = Json.reads[IpShutDown]
  implicit val ipShutdownWrites = Json.writes[IpShutDown]

  implicit val usernameIPsReads = Json.reads[usernameIPs]
  implicit val usernameIPsWrites = Json.writes[usernameIPs]

  implicit val userReads = Json.reads[User]
  implicit val userWrites = Json.writes[User]

  implicit val descRichReads = Json.reads[DescRich]
  implicit val descRichWrites = Json.writes[DescRich]

  implicit val descReads = Json.reads[Desc]
  implicit val descWrites = Json.writes[Desc]

  implicit val descSimpleReads = Json.reads[DescSimple]
  implicit val descSimpleWrites = Json.writes[DescSimple]

  implicit val serviceDetailRepads = Json.reads[ServiceDetail]
  implicit val serviceDetailWrites = Json.writes[ServiceDetail]

  implicit val providerSimpleReads = Json.reads[ProviderSimple]
  implicit val providerSimpleWrites = Json.writes[ProviderSimple]

  implicit val providerNodeDescRepads = Json.reads[ProviderNodeDesc]
  implicit val providerNodeDescWrites = Json.writes[ProviderNodeDesc]

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

  implicit val providerNodeRepads = Json.reads[ProviderNode]
  implicit val providerNodeWrites = Json.writes[ProviderNode]


  implicit val providerHostRepads = Json.reads[ProviderHost]
  implicit val providerHostWrites = Json.writes[ProviderHost]

  implicit val appkeyProviderRepads = Json.reads[AppkeyProvider]
  implicit val appkeyProviderWrites = Json.writes[AppkeyProvider]


  implicit val providerEditReads = Json.reads[ProviderEdit]
  implicit val providerEditWrites = Json.writes[ProviderEdit]

  implicit val ApppkeyTsReads = Json.reads[AppkeyTs]
  implicit val ApppkeyTsWrites = Json.writes[AppkeyTs]

  case class Route(appkey: String, lastUpdateTime: Long)

  implicit val routeReads = Json.reads[Route]
  implicit val routeWrites = Json.writes[Route]

  case class ConsumerGroup(ips: List[String], idcs: Option[List[String]], appkeys: List[String])

  implicit val consumerGroupReads = Json.reads[ConsumerGroup]
  implicit val consumerGroupWrites = Json.writes[ConsumerGroup]

  // priority越高越大; status 0 禁用 1 启用; category 0 自定义分组 1 默认分组
  case class Group(id: Option[String],
                   name: String,
                   category: Option[Int],
                   appkey: String,
                   env: Int,
                   priority: Int,
                   status: Int,
                   consumer: ConsumerGroup,
                   provider: List[String],
                   createTime: Option[Long],
                   updateTime: Option[Long],
                   reserved: String)

  implicit val groupReads = Json.reads[Group]
  implicit val groupWrites = Json.writes[Group]

  case class HealthCheckCustomizedParams(rise: Int,
                                         fall: Int,
                                         interval: Int,
                                         timeout: Int,
                                         check_http_send: String,
                                         check_http_expect_alive: String)

  implicit val HealthCheckCustomizedParamsReads = Json.reads[HealthCheckCustomizedParams]
  implicit val HealthCheckCustomizedParamsWrites = Json.writes[HealthCheckCustomizedParams]

  //HTTP设置相关
  case class HealthCheckConfig(appkey: String,
                               is_health_check: Int, //是否进行健康检查
                               health_check_type: Option[String], //健康检查方案
                               health_check: Option[String], //健康检查CMD，由HLB灌入nginx
                               centra_check_type: String, //Scanner集中健康检查方式
                               centra_http_send: String, //Scanner集中健康检查方式若为"http"，此字段为健康检查请求的uri
                               customized_params: Option[HealthCheckCustomizedParams],
                               createTime: Option[Long],
                               updateTime: Option[Long],
                               reserved: Option[String])

  implicit val HealthCheckConfigReads = Json.reads[HealthCheckConfig]
  implicit val HealthCheckConfigWrites = Json.writes[HealthCheckConfig]

  case class DomainConfig(appkey: String,
                          domain_name: String,
                          domain_location: String,
                          createTime: Option[Long],
                          updateTime: Option[Long],
                          reserved: Option[String])

  implicit val DomainConfigReads = Json.reads[DomainConfig]
  implicit val DomainConfigWrites = Json.writes[DomainConfig]

  case class LoadBalanceConfig(appkey: String,
                               load_balance_type: String,
                               load_balance_value: String,
                               createTime: Option[Long],
                               updateTime: Option[Long],
                               reserved: Option[String])

  implicit val LoadBalanceConfigReads = Json.reads[LoadBalanceConfig]
  implicit val LoadBalanceConfigWrites = Json.writes[LoadBalanceConfig]

  case class SlowStartConfig(appkey: String,
                             is_slow_start: Int,
                             slow_start_value: String,
                             createTime: Option[Long],
                             updateTime: Option[Long],
                             reserved: Option[String])

  implicit val SlowStartConfigReads = Json.reads[SlowStartConfig]
  implicit val SlowStartConfigWrites = Json.writes[SlowStartConfig]

  case class SlowStartIssue(is_slow_start: Int,
                            slow_start_value: String)

  implicit val SlowStartIssueReads = Json.reads[SlowStartIssue]
  implicit val SlowStartIssueWrites = Json.writes[SlowStartIssue]

  case class HealthCheckIssue(is_health_check: Int,
                              health_check_type: Option[String],
                              health_check: Option[String],
                              centra_check_type: String,
                              centra_http_send: String,
                              customized_params: Option[HealthCheckCustomizedParams])

  implicit val HealthCheckIssueReads = Json.reads[HealthCheckIssue]
  implicit val HealthCheckIssueWrites = Json.writes[HealthCheckIssue]

  case class DomainIssue(domain_name: String,
                         domain_location: String)

  implicit val DomainIssueReads = Json.reads[DomainIssue]
  implicit val DomainIssueWrites = Json.writes[DomainIssue]

  case class LoadBalanceIssue(load_balance_type: String,
                              load_balance_value: String)

  implicit val LoadBalanceIssueReads = Json.reads[LoadBalanceIssue]
  implicit val LoadBalanceIssueWrites = Json.writes[LoadBalanceIssue]

  case class SharedHttpConfig(appkey: String,
                              health_check_issue: HealthCheckIssue,
                              domain_issue: DomainIssue,
                              load_balance_issue: LoadBalanceIssue,
                              slow_start_issue: Option[SlowStartIssue],
                              createTime: Option[Long],
                              updateTime: Option[Long],
                              reserved: Option[String],
                              server_port: Option[String])

  implicit val SharedHttpConfigReads = Json.reads[SharedHttpConfig]
  implicit val SharedHttpConfigWrites = Json.writes[SharedHttpConfig]

  case class GroupServerNode(ip: String, port: Int)

  case class HlbGroup(group_name: String, env: String, appkey: String, desc: Option[String], server: List[GroupServerNode])

  case class AddResult(code: Int, msg: String)

  implicit val groupServerNodeReads = Json.reads[GroupServerNode]
  implicit val groupServerNodeWrites = Json.writes[GroupServerNode]

  implicit val hlbGroupReads = Json.reads[HlbGroup]
  implicit val hlbGroupWrites = Json.writes[HlbGroup]

  implicit val addResultReads = Json.reads[AddResult]
  implicit val addResultWrites = Json.writes[AddResult]

  def SGService2ProviderNode(service: SGService) = {
    val providerNode = ProviderNode(None,
      Some(OpsService.ipToHost(service.ip)),
      appkey = service.appkey, service.version,
      ip = service.ip, service.port,
      service.weight, Some(service.fweight), service.status,
      if (service.status == Status.STOPPED.id) Some(1) else Some(0),
      service.role,
      service.envir,
      service.lastUpdateTime.toLong,
      service.extend,
      Some(service.serverType),
      if (StringUtils.isBlank(service.protocol)) Some("") else Some(service.protocol),
      Some(service.groupInfo),
      Some(service.swimlane),
      Some(service.cell), //cell
      if (null == service.serviceInfo) {
        Some(Map[String, ServiceDetail]())
      } else {
        Some(SGServiceInfo2Info(service))
      }
      , Some(service.heartbeatSupport & 0xff)
    )
    providerNode
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
