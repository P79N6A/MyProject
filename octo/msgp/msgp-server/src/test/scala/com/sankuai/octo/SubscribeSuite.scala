package com.sankuai.octo

import com.sankuai.msgp.common.model.{Env, Path, ServiceModels}
import com.sankuai.octo.msgp.serivce.AppSubscribeService
import com.sankuai.octo.msgp.utils.client.ZkClient
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{BeforeAndAfter, FunSuite}
import play.api.libs.json.Json

import scala.collection.JavaConverters._

@RunWith(classOf[JUnitRunner])
class SubscribeSuite extends FunSuite with BeforeAndAfter {

  test("query") {
    println(AppSubscribeService.query(Some("appkey556"),Some(1111L)).size)
    println(AppSubscribeService.query(Some("appkey556"),None).size)
    println(AppSubscribeService.query(None,Some(1111L)).size)
    println(AppSubscribeService.query(None,None).size)
  }

  test("delete") {
    AppSubscribeService.delete("appkey553",1111L)
    AppSubscribeService.delete("appkey554",null)
  }

  test("insert") {
    (1 to 1000).foreach(i => {
      val subscribe = AppSubscribeService.AppSubscribeDomain(0l,"appkey"+i,"zava",1111)
      AppSubscribeService.insert(subscribe)
    })
    (1 to 1000).foreach(i => {
      val subscribe = AppSubscribeService.AppSubscribeDomain(0l,"appkey","zava"+i,i)
      AppSubscribeService.insert(subscribe)
    })
  }
  val sankuaiPath = "/mns/sankuai"
  val prodPath = List(sankuaiPath, Env.prod).mkString("/")
  test("importfromZK"){
    val list = ZkClient.children(prodPath).asScala.map(desc).toList
    println(list.size)
//    println(list)
  }
  def desc(appkey: String): ServiceModels.Desc = {
    val data = ZkClient.getData(List(prodPath, appkey, Path.desc).mkString("/"))
    println(data)
    val result = Json.parse(data).validate[ServiceModels.Desc].get
    //写入app_subscribe
    val  owners = result.owners
    owners.foreach(owner => {
      val subscribe = AppSubscribeService.AppSubscribeDomain(0l,result.appkey,owner.name,owner.id)
      if(AppSubscribeService.query(Some(result.appkey),Some(owner.id)).size == 0){
        AppSubscribeService.insert(subscribe)
      }
    })
    result
  }

}
