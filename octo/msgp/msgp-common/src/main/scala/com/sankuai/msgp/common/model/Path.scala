package com.sankuai.msgp.common.model

object Path extends Enumeration {
  type Path = Value
  val desc = Value("desc")
  val provider = Value("provider")
  val consumer = Value("consumer")
  val route = Value("route")
  val routeHttp = Value("route-http")
  val config = Value("config")
  val quota = Value("quota")
  val auth = Value("auth")
  val providerHttp = Value("provider-http")
  val sharedHttpConfig = Value("http-properties")
  val sankuaiPath = "/mns/sankuai"
}
