package com.sankuai.octo.mnsc.test

import java.util.concurrent.atomic.AtomicInteger

import com.sankuai.octo.mnsc.dataCache._
import com.sankuai.octo.mnsc.remote.zk
import com.sankuai.octo.mnsc.service.mnscService
import com.sankuai.octo.mnsc.utils.mnscCommon
import com.sankuai.octo.mnsc.zkWatcher.httpPropertiesWatcher
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{BeforeAndAfter, FunSuite}
import play.api.libs.json.Json
import scala.collection.JavaConverters._


@RunWith(classOf[JUnitRunner])
class mnscDataCacheSuite extends FunSuite with BeforeAndAfter {

  test("tet getMNSCacheByAppkeys") {
    val list = List("com.sankuai.cos.mtconfig", "com.sankuai.inf.mnsc")

    val ret = mnscService.getMNSCacheByAppkeys(list.asJava, "thrift")
    println(ret)
  }


  test("get desc cache") {
    val count: AtomicInteger = new AtomicInteger(0)
    appDescDataCache.doDescRenew()
    while (count.incrementAndGet() <= 10) {
      val allDescData = appDescDataCache.getDescDataAll
      //allDescData.keys.foreach(
      //  x => println("[BEAUG] "+x+" ==> bizCode:" + allDescData(x).business.toString)
      //)
      Thread.sleep(5000)
    }
  }

  test("get provider cache") {
    val count: AtomicInteger = new AtomicInteger(0)
    println("[BEAUG]Gonna call doRenew")
    appProviderDataCache.doRenew()
    while (count.incrementAndGet() <= 20) {
      //cacheValue
      val prodProviderData = mnscService.getMnsc("com.sankuai.cos.mtconfig", "0", "prod")
      //      appProviderDataCache.getProviderCache("com.sankuai.sfsdfsa", "prod")
      //      println("[BEAUG] getProviderCache--> prod v= "+ prodProviderData.get.version)
      Thread.sleep(2000)
    }
  }

  test("version compare") {
    println("")

  }


  test("get provider list with version") {
    val appkey = "com.sankuai.inf.octo.oswatch"
    //    val appkey1 = "com.sankuai.inf.mnsc"
    val env = "prod"
    appProviderDataCache.doRenew()
    while (true) {
      println("===========================")
      println(mnscService.getMnsc(appkey, "", env))
      //      println(mnscService.getMnsc(appkey1, "", env))
      Thread.sleep(4000)
    }
  }

  test("TEST get provider-http list & TEST watcher") {
    val appkey1 = "com.saikuai.it.saas.bpm"
    val appkey2 = "com.sankuai.inf.jinluTestHTTP";
    val env = "prod"
    appProviderHttpDataCache.doRenew()
    while (true) {
      println("===========================")
      println(appProviderHttpDataCache.getProviderHttpCache(appkey1, env,false))
      println(appProviderHttpDataCache.getProviderHttpCache(appkey2, env,false))
      println(mnscService.getMNSCache4HLB(appkey1, "", env))
      println(mnscService.getMNSCache4HLB(appkey2, "", env))
      Thread.sleep(4000)
    }
  }

  test("get http-properties with version") {
    httpPropertiesDataCache.doRenew()
    appDescDataCache.doDescRenew()
    httpPropertiesWatcher.initWatcher()
    val appkey = "com.sankuai.inf.jinluTestHTTP";
    val env = "stage"
    val bizCode = 1

    while (true) {
      println("\n===========================")
      println("【1】 httpPropertiesDataCache.getHttpPropertiesCacheByAppKey")
      println(httpPropertiesDataCache.getHttpPropertiesCacheByAppKey(appkey, env))
      println("【2】 mnscService.getHttpPropertiesByAppkey")
      println(mnscService.getHttpPropertiesByAppkey(appkey, env))
      println("【3】 mnscService.getHttpPropertiesByBusinessLine")
      println(mnscService.getHttpPropertiesByBusinessLine(bizCode, env))
      Thread.sleep(4000)
    }
  }


  test("http group service test") {
    httpGroupDataCache.getGroupStr("com.sankuai.hlb.rt", "prod")
    println(mnscService.getAllGroups("prod"))
    println()
  }


  test("HLBC server update test") {
    val path = "/hlbc/prod/server-block/hlbcTest.cache_update_test"
    val content = "{\"server_name\":\"hlbcTest.cache_update_test01\",\"business\":11,\"server_block_cmd\":\"\",\"location_list\":[{\"location_path\":\"/\",\"location_block_cmd\":\" proxy_set_header Host $host; set $upstream_name hbnb_fe_hoteltest_mei_com;\",\"proxy_pass\":\"$upstream_name\"}]}"
    //zk.create(path, content)
    //zk.deleteWithChildren(path)

  }

  test("HLB group test") {
    val env = "prod"
    val appkey = "com.sankuai.hlb.rt"
    println("group data: ")
    println(httpGroupDataCache.getGroupStr(appkey, env))
    //println(zk.children("/mns/sankuai/prod/com.sankuai.hlb.rt/groups/http"))
    println(mnscService.getHlbGroupByAppkey(appkey, env))
  }

    test("all Evict Appkey") {
      httpPropertiesDataCache.doRenew()
      appDescDataCache.doDescRenew()
      appProviderHttpDataCache.doRenew()

      while (true) {
        Thread.sleep(4000)
        println(s"\n============= ${mnscCommon.allApp().length} ==============")
        println("【1】 httpPropertiesDataCache *_*")
        println(httpPropertiesDataCache.deleteNonexistentAppKey())
        println("【2】 appDescDataCache *_*")
        println(appDescDataCache.deleteNonexistentAppKey())
        println("【3】 appProviderHttpDataCache *_*")
        println(appProviderHttpDataCache.deleteNonexistentAppKey())
      }
    }
}
