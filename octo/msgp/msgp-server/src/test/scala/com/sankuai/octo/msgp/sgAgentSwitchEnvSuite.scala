package com.sankuai.octo.msgp

import com.sankuai.meituan.auth.util.UserUtils
import com.sankuai.meituan.auth.vo.User
import com.sankuai.octo.msgp.serivce.sgagent.SgAgentSwitchEnv
import org.scalatest.{BeforeAndAfter, FunSuite}
import org.springframework.test.context.{ContextConfiguration, TestContextManager}

@ContextConfiguration(locations = Array(
  "classpath*:applicationContext-*.xml"))
class sgAgentSwitchEnvSuite extends FunSuite with BeforeAndAfter {
  new TestContextManager(this.getClass).prepareTestInstance(this)

  test("updateIpEnv") {
    val user: User = new User
    user.setId(64137)
    user.setLogin("hanjiancheng")
    user.setName("hanjiancheng")
    UserUtils.bind(user)
    val data = SgAgentSwitchEnv.updateIpEnv("10.4.231.48")
    println(data)
  }

  test("handleUpdateProviders") {
    val user: User = new User
    user.setId(64137)
    user.setLogin("hanjiancheng")
    user.setName("hanjiancheng")
    UserUtils.bind(user)
    val data = SgAgentSwitchEnv.updateProviders("10.4.231.48", "stage", "prod")
    println(data)
  }

}
