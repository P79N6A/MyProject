package com.sankuai.octo.msgp.dao.service

import com.sankuai.msgp.common.model.Env
import com.sankuai.msgp.common.model.Path
import com.sankuai.octo.msgp.utils.client.ZkClient

import scala.collection.JavaConverters._

object ServiceProviderDAO {
  private val sankuaiPath = "/mns/sankuai"

  // 只返回ip list，并merge thrift & http
  def providerList(appkey: String, env: String = Env.prod.toString) = {
    val list = ZkClient.children(List(sankuaiPath, env, appkey, Path.provider).mkString("/")).asScala.toList ++
      ZkClient.children(List(sankuaiPath, env, appkey, Path.providerHttp).mkString("/")).asScala.toList

    list.map(_.split(":").apply(0)).distinct
  }
}
