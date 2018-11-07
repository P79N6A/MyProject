package com.sankuai.octo

import java.util.concurrent.{Executors, TimeUnit}

import com.sankuai.octo.statistic.model._
import com.sankuai.octo.export.util.{Falcon, TagUtil}
import org.joda.time.DateTime
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{BeforeAndAfter, FunSuite}
import org.springframework.test.context.{ContextConfiguration, TestContextManager}

import scala.util.Random

@RunWith(classOf[JUnitRunner])
@ContextConfiguration(locations = Array("classpath:applicationContext.xml"))
class FalconSuite extends FunSuite with BeforeAndAfter {

  new TestContextManager(this.getClass).prepareTestInstance(this)

  // 服务接口，全局性能，必须
  test("server: spanname */all") {
    val data = mockData(StatGroup.Span)
    data.sliding(100, 100).foreach(Falcon.send)
  }

  // 服务接口，不同主机的性能，必须
  test("server: spanname */all, localhost *") {
    val data = mockData(StatGroup.SpanLocalHost)
    data.sliding(100, 100).foreach(Falcon.send)
  }

  // 服务接口，不同来源服务的性能，必须
  test("server: spanname */all, remoteApp *") {
    val data = mockData(StatGroup.SpanRemoteApp)
    data.sliding(100, 100).foreach(Falcon.send)
  }

  // 服务接口，不同来源主机的性能，可选
  test("server: spanname */all, remoteHost *") {
    val data = mockData(StatGroup.SpanRemoteHost)
    data.sliding(100, 100).foreach(Falcon.send)
  }

  // 服务调用，不同接口的全局性能，建议
  test("client: spanname */all, remoteApp *") {
    val data = mockData(StatGroup.SpanRemoteApp, StatSource.Client)
    data.sliding(100, 100).foreach(Falcon.send)
  }

  test("testScheduler") {
    val count = 5
    val executor = Executors.newScheduledThreadPool(count)
    (1 to count).foreach(_ => {
      val r = new Runnable {
        override def run(): Unit = {
          println("id:" + Thread.currentThread().getId + ",name:" + Thread.currentThread().getName)
          try {
            throw new RuntimeException
          } catch {
            case e: Exception =>
          }
        }
      }
      executor.scheduleAtFixedRate(r, 1, 1, TimeUnit.SECONDS)
    })

    Thread.sleep(1000001)

  }

  def mockData(group: StatGroup = StatGroup.Span,
               source: StatSource = StatSource.Server) = {
    val start = new DateTime().minusHours(1).getMillis / 1000
    val end = new DateTime().getMillis / 1000
    val count = (end - start) / 60
    val data = (0 to count.toInt).flatMap {
      x =>
        val ts = start + x * 60
        Falcon.statToFalconData(randStat(ts.toInt, group = group, source = source))
    }
    println(data.size)
    data
  }

  test("crontab to falcon") {
    val scheduler =  Executors.newScheduledThreadPool(1)
    def job() = new Runnable {
      override def run(): Unit = {
        val ts = new DateTime().getMillis / 1000
        println(s"now is $ts")
        val data = Falcon.statToFalconData(randStat(ts.toInt, group = StatGroup.SpanLocalHost, source = StatSource.Server))
        Falcon.send(data)
      }
    }
    scheduler.scheduleAtFixedRate(job(), 1, 60, TimeUnit.SECONDS)
    while (true) {
      Thread.sleep(10000)
    }
  }

  def randStat(ts: Int, appkey: String = "com.sankuai.inf.test.falcon",
               group: StatGroup = StatGroup.Span, source: StatSource = StatSource.Server) = {
    val stat = new StatData()
    stat.setAppkey(appkey)
    stat.setTs(ts)
    stat.setEnv(StatEnv.Prod)
    stat.setSource(source)
    stat.setRange(StatRange.Minute)
    stat.setGroup(group)
    val tags = if (source == StatSource.Server) {
      val id = Random.nextInt(5)
      TagUtil.getStatTag("testMethod" + Random.nextInt(5), "dx-inf-octo-msgp01", "fromHost" + Random.nextInt(5), "com.sankuai.from" + id)
    } else {
      val id = Random.nextInt(5)
      TagUtil.getStatTag("toMethod" + Random.nextInt(5), "dx-inf-octo-msgp01", "toHost" + id + Random.nextInt(5), "com.sankuai.to" + id)
    }
    stat.setTags(tags)
    //val count = Random.nextInt(1000)
    stat.setCount(10000) //count
    stat.setQps(stat.getCount / 60)
    //10 + Random.nextInt(3)
    stat.setCostMean(ts % 59 + 10)
    //10 + Random.nextInt(5)
    stat.setCost50(ts % 59 + 20)
    //20 + Random.nextInt(5)
    stat.setCost90(ts % 59 + 30)
    //30 + Random.nextInt(5)
    stat.setCost95(ts % 59 + 40)
    //50 + Random.nextInt(5)
    stat.setCost99(ts % 59 + 50)
    stat
  }

}
