package com.sankuai.octo

import java.util.concurrent.TimeUnit

import com.sankuai.msgp.common.model.Base
import com.sankuai.msgp.common.model.ServiceModels.{Desc, User}
import com.sankuai.octo.msgp.serivce.service.ServiceCommon
import com.sankuai.octo.msgp.utils.client.ZkClient
import org.apache.curator.framework.CuratorFrameworkFactory
import org.apache.curator.framework.recipes.leader.LeaderLatch
import org.apache.curator.framework.recipes.locks.InterProcessSemaphoreMutex
import org.apache.curator.retry.RetryUntilElapsed
import org.junit.runner.RunWith
import org.scalatest.{BeforeAndAfter, FunSuite}
import org.scalatest.junit.JUnitRunner
import play.api.libs.json.Json

import scala.util.Random

@RunWith(classOf[JUnitRunner])
class ZkSuite extends FunSuite with BeforeAndAfter {

  test("desc") {
    val serviceDesc = Desc("ELB注册代理", "regproxy",Some("regproxy"), List(User(7655, "zhangxi", "张熙")),Some(List(User(64137, "hanjiancheng", "韩建成"))), "...", "http", None, None,Some(Base.dianping.getId),Some("inf"),Some("hlb"), None, "octo,inf")
    val descStr = Json.prettyPrint(Json.toJson(serviceDesc))
    println(descStr)
    val path = List(ServiceCommon.prodPath, "regproxy", "desc").mkString("/")
    if(ZkClient.exist(path)){
      ZkClient.setData(path, descStr)
    }else{
      ZkClient.create(path,descStr)
    }

    val data = ZkClient.getData(path)
    println(data)

    println(Json.prettyPrint(Json.parse(data)))
    val json = Json.parse(data)
    val desc = json.validate[Desc].get
    println(desc)
  }

  test("desc json") {
    val data = "{\"name\":\"ELB注册代理\",\"appkey\":\"regproxy\",\"owners\":[{\"id\":7655,\"login\":\"zhangxi\",\"name\":\"张熙\"}],\"observers\":[{\"id\":64137,\"login\":\"hanjiancheng\",\"name\":\"韩建成\"}],\"intro\":\"...\",\"category\":\"http\",\"owt\":\"inf\",\"pdl\":\"elb\",\"tags\":\"octo,inf\"}"

    val result = Json.parse(data).validate[Desc].get
    println(result)
  }

  test("LeaderLatch") {
    val url = "192.168.12.164:2181,192.168.3.163:2181,172.27.1.130:2181"
    val count = 10
    (1 to count).foreach {
      x =>
        Thread.sleep(x * 2000)
        new Thread() {
          override def run() = {
            val client = CuratorFrameworkFactory.builder.connectString(url).retryPolicy(new RetryUntilElapsed(3000, 2000)).build()
            client.start()
            val latch = new LeaderLatch(client, "/test/latch")
            latch.start()
            latch.await(2, TimeUnit.SECONDS)
            println(s"$x init state ${latch.hasLeadership}")
            var i = 1
            while (true) {
              try {
                if (latch.hasLeadership) {
                  latch.close()
                }
                if (latch.hasLeadership) {
                  println(s"$i $x check state ${latch.hasLeadership}")
                }
                i += 1
                Thread.sleep(2000)
              } catch {
                case e: Exception => println(s"${Thread.currentThread().getId} $e fail")
              }
            }
          }
        }.start()
    }
    Thread.sleep(1000000)
  }

  test("InterProcessSemaphoreMutex") {
    val url = "192.168.12.164:2181,192.168.3.163:2181,172.27.1.130:2181"
    val count = 5
    (1 to count).foreach {
      x =>
        new Thread() {
          override def run() = {
            val client = CuratorFrameworkFactory.builder.connectString(url).retryPolicy(new RetryUntilElapsed(3000, 2000)).build()
            client.start()
            val mutex = new InterProcessSemaphoreMutex(client, "/test/mutex")
            mutex.acquire(5, TimeUnit.SECONDS)
            println(s"$x init state ${mutex.isAcquiredInThisProcess}")
            var i = 1
            try {
              while (true) {
                println(s"$x check state ${mutex.isAcquiredInThisProcess}")
                if (mutex.isAcquiredInThisProcess) {
                  if (i > 10) {
                    client.close()
                    throw new RuntimeException
                  } else if (Random.nextBoolean()) {
                    mutex.release()
                  }
                } else {
                  mutex.acquire(5, TimeUnit.SECONDS)
                }
                i += 1
                Thread.sleep(Random.nextInt(5) * 1000)
              }
            } catch {
              case e: Exception => println(s"$x $e fail")
            }
          }
        }.start()
    }
    Thread.sleep(1000000)
  }

  test("get") {
    val serviceDesc = Desc("ELB注册代理", "regproxy",Some("regproxy"), List(User(7655, "zhangxi", "张熙")),Some(List(User(64137, "hanjiancheng", "韩建成"))), "...", "http", None, None,Some(Base.dianping.getId),Some("inf"),Some("hlb"), None, "octo,inf")
    val descStr = Json.prettyPrint(Json.toJson(serviceDesc))
    println(descStr)
    val path = List(ServiceCommon.prodPath, "regproxy", "desc").mkString("/")
    if(ZkClient.exist(path)){
      ZkClient.setData(path, descStr)
    }else{
      ZkClient.create(path,descStr)
    }
    while(true){
      val data = ZkClient.getData(path)
      println(data)
      Thread.sleep(1000)
    }
  }

  test("delete"){
    //,"search-indexer-poisku","search-arts-poisku","orderdish-shop-config-service","maoyan-show-settlement-job","mercury-shop-web","apollo-data-service","maoyan-show-settlement-mq","mercury-activity-base-service"
    val appkeys = List("com.sankuai.zc.fy.orderjob")
    appkeys.foreach{
      appkey =>
        val path = s"/mns/sankuai/prod/${appkey}"
          println(path)
        ZkClient.deleteWithChildren(path)
    }
  }
  test("zk"){
    var i=0;
    while(true){
      val path = s"/mns/sankuai/prod/com.sankuai.inf.msgp"
      val data = ZkClient.exist(path)
      println(s"$data,$i")
      i+=1
      Thread.sleep(1000)
    }
  }

  test("zkpath"){
    val path = "/mns/sankuai/prod/com.sankuai.travel.dsg.gisplus /quota/DomesticCityService.selectOpenCityAll  "
    val result = ZkClient.exist(path.trim)
    println(result)

    val zkPathTemplate = "/mns/sankuai/%s/%s/quota/%s"
  }
}

