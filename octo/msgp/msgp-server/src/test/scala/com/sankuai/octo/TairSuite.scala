package com.sankuai.octo

import com.sankuai.msgp.common.utils.client.TairClient
import com.sankuai.octo.msgp.model.DashboardDomain.{Dashboard, Shortcut}
import com.sankuai.msgp.common.model.ServiceModels.User
import com.sankuai.octo.msgp.serivce.Setting
import com.sankuai.msgp.common.utils.client.TairClient._
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{BeforeAndAfter, FunSuite}
import play.api.libs.json.Json

import scala.util.Random

@RunWith(classOf[JUnitRunner])
class TairSuite extends FunSuite with BeforeAndAfter {

  test("tair") {
    println(get("test"))
    val user = User(1, "login" + Random.nextInt(10), "name" + Random.nextInt(10))
    println(put("test", user))
    println(get("test" + Random.nextInt(2)))
    println(get("test"))
    val value = getValue("test").map(Json.parse).get.validate[User].asOpt
    println(value)
  }

  test("incr") {
    val key = "tair.test.incr"
    println(TairClient.get(key))
    println(TairClient.incr(key,1,60))
    println(TairClient.incr(key,1,60))
    println(TairClient.incr(key,1,60))
    println(TairClient.get(key))
  }

  test("set") {
    val user = User(1, "login" + Random.nextInt(10), "name" + Random.nextInt(10))
    val key = "tair.test"
    val old = TairClient.get(key)
    println(old)
    TairClient.put(key, user)
    val value = TairClient.getValue(key).map(Json.parse).get.validate[User].asOpt
    println(value)
    //tair.del(key)
  }

  test("dashboard") {
    Setting.setDashboard("zhangxi", Dashboard("/"))
  }

  test("clear") {
    //setting.setShortcut("zhangxi", List())
    //setting.setShortcut("linye", List())
    println(Setting.getShortcuts("linye"))
    println(Setting.getShortcuts("zhangxi"))
    //setting.delShortut("zhangxi", "f5e08b43-496e-4c0f-b4da-5155d641d35e")
    println(Setting.getShortcuts("zhangxi"))
  }

  test("shortcuts") {
    val key = "zhangxi.shortcuts"

    println(get(key))
    val shortcut = Shortcut(Some("fdsfdsfs"), "", "", "")
    //setting.addShortcut("zhangxi", shortcut)
    //setting.setShortcut("zhangxi", List(shortcut))
    println(get(key))

    //setting.addShortcut("zhangxi", shortcut)
    println(get(key))
  }
}
