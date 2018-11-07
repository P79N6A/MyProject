package com.sankuai.octo.statistic.model


import org.scalatest.FunSuite

class PerfProtocalTypeTest extends FunSuite {
  test("getIntvalue") {
    println(PerfProtocolType.HTTP.getIntValue())
    println(StatSource.Client.getIntValue())
    println(StatSource.Server.getIntValue())
    println(StatSource.ServerFailure.getIntValue())
    println(StatEnv.Prod.getIntValue())
  }
}
