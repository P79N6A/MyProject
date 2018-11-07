/*
package com.sankuai.octo

import com.sankuai.octo.statistic.helper.api
import com.sankuai.octo.statistic.model._
import com.sankuai.octo.statistic.store.AbstractStatStore
import org.scalatest.{BeforeAndAfter, FunSuite}

import scala.collection.JavaConverters._

class StoreSuite extends FunSuite with BeforeAndAfter {

  test("tair store") {
    val instance: AbstractStatStore = null
    val appkey = "test"
    val ts = (System.currentTimeMillis() / 1000).toInt
    val env = StatEnv.Prod
    val source = StatSource.Server
    val range = StatRange.Day
    val group = StatGroup.Span

    val data = new StatData()
    data.setAppkey(appkey)
    data.setTs(ts)
    data.setEnv(env)
    data.setSource(source)
    data.setRange(range)
    data.setGroup(group)
    data.setTags(Map("spanname" -> "all").asJava)
    instance.storeStat(data)

    val value = instance.getStat(appkey, ts, "all", env, source, range, group)
    println(value)
  }

  test("version upgrade") {
    val oldStat = """{"appkey":null,"ts":0,"category":null,"tags":null,"count":0,"qps":0.0,"costMin":0.0,"costMean":0.0,"cost50":0.0,"cost75":0.0,"cost90":0.0,"cost95":0.0,"cost98":0.0,"cost99":0.0,"cost999":0.0,"costMax":0.0,"datas":null,"updateTime":0,"updateFrom":null}"""
    val newStat = api.toObject(oldStat, classOf[StatData])
    println(newStat)
  }
}
*/
