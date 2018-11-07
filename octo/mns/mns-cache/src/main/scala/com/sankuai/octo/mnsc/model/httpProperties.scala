package com.sankuai.octo.mnsc.model

import play.api.libs.json.Json

object httpProperties {

  case class ExtendIssue(keepalive: Option[Int],
                         keepalive_timeout: Option[Int])

  implicit val extendIssueReads = Json.reads[ExtendIssue]
  implicit val extendIssueWrites = Json.writes[ExtendIssue]

  // enabled 0 启用 1 停用   trace 0 关闭 1 开启
  case class HealthCheckIssue(is_health_check: Int,
                              health_check_type: Option[String],
                              health_check: Option[String],
                              centra_check_type: String,
                              centra_http_send: Option[String])

  implicit val healthCheckIssueReads = Json.reads[HealthCheckIssue]
  implicit val healthCheckIssueWrites = Json.writes[HealthCheckIssue]

  case class DomainIssue(domain_name: String, domain_location: String)

  implicit val domainIssueReads = Json.reads[DomainIssue]
  implicit val domainIssueWrites = Json.writes[DomainIssue]

  case class LoadBalanceIssue(load_balance_type: String, load_balance_value: String)

  implicit val loadBalanceIssueReads = Json.reads[LoadBalanceIssue]
  implicit val loadBalanceIssueWrites = Json.writes[LoadBalanceIssue]

  case class SlowStartIssue(is_slow_start: Int, slow_start_value: String)
  implicit val slowStartIssueReads = Json.reads[SlowStartIssue]
  implicit val slowStartIssueWrites = Json.writes[SlowStartIssue]

  case class HttpProperties(name: Option[String],
                            appkey: String,
                            health_check_issue: Option[HealthCheckIssue],
                            domain_issue: Option[DomainIssue],
                            load_balance_issue: Option[LoadBalanceIssue],
			    slow_start_issue: Option[SlowStartIssue],
                            extend_issue: Option[ExtendIssue])

  implicit val httpPropertiesReads = Json.reads[HttpProperties]
  implicit val httpPropertiesWrites = Json.writes[HttpProperties]

  case class PropertiesValue(version: String, Properties: Map[String, String])

}

