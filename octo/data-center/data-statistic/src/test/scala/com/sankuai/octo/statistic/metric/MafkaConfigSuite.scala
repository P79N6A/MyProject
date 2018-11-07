package com.sankuai.octo.statistic.metric

import com.sankuai.octo.statistic.{MafkaConfig, Bootstrap}
import org.scalatest.{BeforeAndAfter, FunSuite}
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.web.WebAppConfiguration

@ContextConfiguration(
  locations = Array("classpath:applicationContext.xml")
)
@WebAppConfiguration
class MafkaConfigSuite extends FunSuite with BeforeAndAfter{

  //  new TestContextManager(this.getClass).prepareTestInstance(this)

  before {
    Bootstrap.init()
  }

  test("istaskhost"){
    println(MafkaConfig.isTaskHost)
  }
}
