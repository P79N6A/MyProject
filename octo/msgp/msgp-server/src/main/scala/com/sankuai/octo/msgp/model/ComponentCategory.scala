package com.sankuai.octo.msgp.model

/**
  * Created by yves on 16/9/5.
  */
object ComponentCategory extends Enumeration {
  type ComponentCategory = Value

  val web_framework = Value(0, "WEB框架")
  val web_container = Value(1, "WEB容器")
  val json = Value(2, "JSON")
  val kv = Value(3, "KV")
  val mq = Value(4, "MQ")
  val orm = Value(5, "ORM")
  val database_relevant = Value(6, "数据库连接相关")
  val log = Value(7, "LOG")
  val monitor = Value(8, "监控")
  val http = Value(9, "HTTP服务")
  val octo = Value(10, "OCTO")
  val bom = Value(11, "BOM")
  val others = Value(100, "其他")

  val categoryNameMap = Map(
    "web_framework" -> "WEB框架",
    "web_container" -> "WEB容器",
    "json" -> "JSON",
    "mq" -> "MQ",
    "kv" -> "KV",
    "orm" -> "ORM",
    "database_relevant" -> "数据库连接相关",
    "log" -> "LOG",
    "monitor" -> "监控",
    "http" -> "HTTP服务",
    "octo" -> "OCTO",
    "bom" -> "BOM",
    "others" -> "其他"
  )

  val variableMap = Map(
    web_framework -> "web_framework",
    web_container -> "web_container",
    json -> "json",
    mq -> "mq",
    kv -> "kv",
    orm -> "orm",
    database_relevant -> "database_relevant",
    log -> "log",
    monitor -> "monitor",
    http -> "http",
    octo -> "octo",
    bom -> "bom",
    others -> "others"
  )

  def getCategoryName(variable: String) = {
    categoryNameMap.getOrElse(variable, "其他")
  }

  def getCategoryVariableName(category: ComponentCategory) = {
    variableMap.getOrElse(category, "others")
  }
}
