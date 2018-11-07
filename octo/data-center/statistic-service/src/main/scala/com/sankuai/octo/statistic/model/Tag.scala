package com.sankuai.octo.statistic.model

/**
  * Created by wujinwu on 16/1/8.
  */
case class TagKey(appkey: String, ts: Int, env: StatEnv, source: StatSource)

case class Tag(var spannames: Set[String] = Set(), var localHosts: Set[String] = Set(),
               var remoteAppKeys: Set[String] = Set(), var remoteHosts: Set[String] = Set())

case class QueryTag(appkey: String, spannames: Set[String], localHosts: Set[String], remoteAppKeys: Set[String], remoteHosts: Set[String])
