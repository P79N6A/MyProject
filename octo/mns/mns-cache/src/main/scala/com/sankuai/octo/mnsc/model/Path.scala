package com.sankuai.octo.mnsc.model

object Path extends Enumeration {
  type Path = Value
  val desc = Value("desc")
  val provider = Value("provider")
  val providerHttp = Value("provider-http")
  val consumer = Value("consumer")
  val route = Value("route")
  val config = Value("config")
  val quota = Value("quota")
  val auth = Value("auth")
  val httpProperties = Value("http-properties")
  //val httpGroup = Value("http-group")
}
