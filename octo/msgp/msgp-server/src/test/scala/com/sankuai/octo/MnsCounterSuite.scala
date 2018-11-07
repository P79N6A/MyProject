package com.sankuai.octo

;


import java.util.concurrent.atomic.AtomicInteger

import com.sankuai.msgp.common.utils.HttpUtil
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{BeforeAndAfter, FunSuite}
import play.api.libs.json.{JsArray, Json}


/**
 * 统计接入mns但是没有服务节点的服务，但是有调用的服务
 */
@RunWith(classOf[JUnitRunner])
class MnsCounterSuite extends FunSuite with BeforeAndAfter {

  val hostname = "octo.sankuai.com"

  test("appkey") {
    val appkeys = getAppkey
//    val appkeys = List("com.sankuai.inf.msgp")
    println(s"服务总数${appkeys.size}")
    val provider_count  = new AtomicInteger(0);
    val depend_count  = new AtomicInteger(0);
    appkeys.par.foreach {
      appkey =>
        val count = getProviderCounter(appkey)
        if(count==0){
          //获取服务上下游
          val dependCounts = getDepends(appkey)
          println(s"depend $appkey:$dependCounts")
          if(dependCounts>0){
            depend_count.incrementAndGet()
          }
        }else{
          provider_count.incrementAndGet()
        }

    }

    println(s"有服务提供者服务总数${provider_count},无服务提供者总数${appkeys.size-provider_count.get()}")
    println(s"无上下游依赖总数${appkeys.size-provider_count.get-depend_count.get}，有上下有依赖总数${depend_count.get}")

  }

  test("privider"){
    val appkeys = List("com.sankuai.inf.msgp","com.sankuai.inf.data.statistic","com.sankuai.inf.logCollector")
    appkeys.foreach{
      appkey=>
        val count = getProviderCounter(appkey)
        println(count)
    }

  }

  def getAppkey(): List[String] = {
    val url = s"http://$hostname/api/apps"
    val params = new java.util.HashMap[String, String]()
    val data = HttpUtil.httpGetRequest(url, params)
    (Json.parse(data) \ "data").as[List[String]]
  }

  def getProviderCounter(appkey: String): Int = {
    val url = s"http://$hostname/api/provider"
    val params = new java.util.HashMap[String, String]()
    params.put("appkey",appkey)
    val envs = List("1","2","3")
    val types = List("1","2")
    val counts = envs.flatMap {
      env =>
        types.map {
          typee =>
            params.put("type", typee)
            params.put("env", env)
            params.put("stauts", "2")
            val data = HttpUtil.httpGetRequest(url, params)
            try {
              (Json.parse(data) \ "data").asInstanceOf[JsArray].value.size
            }
            catch {
              case e: Exception =>
                println(url)
                0
            }
        }
    }
    counts.sum
  }

  def getDepends(appkey: String): Int ={
    val url = s"http://$hostname/api/service/depends"
    val sources = List("client", "server")

    val params = new java.util.HashMap[String, String]()
    params.put("appkey", appkey)
    params.put("env", "prod")
    val counts = sources.map {
      source =>
        params.put("source", source)
        val data = HttpUtil.httpGetRequest(url, params)
        (Json.parse(data) \ "data").as[List[String]].size
    }
    counts.sum
  }

}
