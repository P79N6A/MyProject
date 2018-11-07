package com.sankuai.octo

import com.sankuai.meituan.auth.vo.User
import com.sankuai.msgp.common.service.org.OpsService
import com.sankuai.octo.msgp.serivce.AppkeyAuth
import com.sankuai.octo.msgp.utils.Auth
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{BeforeAndAfter, FunSuite}

import scala.collection.JavaConverters._

/**
 * Created by zava on 16/2/1.
 */
@RunWith(classOf[JUnitRunner])
class appkeyAuthSuite extends FunSuite with BeforeAndAfter {

  test("auth") {
    val user = new User
    user.setId(43864)
    user.setLogin("wangdongsheng")
    user.setName("王东升")
    val a = AppkeyAuth.hasAuth("com.sankuai.ktv.sinai", Auth.Level.READ.getValue, user)
    println(a)
    //    val b = appkeyAuth.getAppkeysByAuth(user)
    //    println(b)
  }

  test("getOwtsbyUsername"){
    val users = List("xujianguo02","caojiguang","daimao","zhangzhitong","wangsiyu02");
    users.foreach{
      user =>
        val owtList = OpsService.getOwtsbyUsername(user)
        println(owtList)
    }
  }
  test("getowner"){
    val data = AppkeyAuth.getAppkeyOwner("com.sankuai.inf.msgp")
  println(data)
  }

}
