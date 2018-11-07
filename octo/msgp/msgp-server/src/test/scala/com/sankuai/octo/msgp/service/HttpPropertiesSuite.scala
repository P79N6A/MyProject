package com.sankuai.octo.msgp.service

import com.sankuai.msgp.common.model.Env
import com.sankuai.octo.msgp.serivce.service.ServiceHttpConfig
import com.sankuai.octo.msgp.utils.client.ZkClient
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{BeforeAndAfter, FunSuite}
import play.api.libs.json.Json

import scala.collection.JavaConverters._
import scala.collection.parallel.ForkJoinTaskSupport


@RunWith(classOf[JUnitRunner])
class HttpPropertiesSuite extends FunSuite with BeforeAndAfter {
  val taskSupport = new ForkJoinTaskSupport(new scala.concurrent.forkjoin.ForkJoinPool(16))
  test("Http Propertie") {
    val mns_root = "/mns/sankuai/"
    Env.values.foreach {
      value =>
        val env_path = s"$mns_root${value.toString}"
        val appkeys = ZkClient.children(env_path)
        val appPar = appkeys.asScala.par
        appPar.tasksupport = taskSupport
        appPar.foreach {
          appkey =>
            val pro_path = s"$env_path/$appkey/http-properties"
            val properties = ZkClient.getData(pro_path)
            if (!properties.contains("load_balance_issue")) {
              // 设置http-properties默认信息
              val defaultSharedHttpConfig = ServiceHttpConfig.getDefaultSharedHttpConfig(appkey)
              ZkClient.setData(pro_path, Json.prettyPrint(Json.toJson(defaultSharedHttpConfig)))
            }
        }
    }

  }
}

