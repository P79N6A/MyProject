package com.sankuai.octo

import java.net.InetAddress
import java.util.concurrent.TimeUnit

import com.ning.http.client.ProxyServer
import com.sankuai.meituan.common.security.Base64
import com.sankuai.meituan.org.remote.service.RemoteEmployeeService
import com.sankuai.msgp.common.model.Env
import com.sankuai.msgp.common.service.org.OpsService
import com.sankuai.msgp.common.utils.helper.{HttpHelper, JsonHelper}
import com.sankuai.octo.msgp.serivce.data.DataQuery
import com.sankuai.octo.statistic.model.StatRange
import dispatch.{Http, as, url}
import org.apache.commons.lang3.StringUtils
import org.eclipse.jetty.util.security.Credential.MD5
import org.joda.time.format.{DateTimeFormat, DateTimeFormatter}
import org.joda.time.{DateTime, LocalDate}
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{BeforeAndAfter, FunSuite}
import play.api.libs.json.Json

import scala.collection.JavaConverters._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.{Duration, FiniteDuration}
import scala.concurrent.{Await, duration}
import scala.util.Random

@RunWith(classOf[JUnitRunner])
class GistSuite extends FunSuite with BeforeAndAfter {

  test("env") {
    Env.values.map(x => {
      println(x.id + ":" + x)
    })

    println(Env.prod)
    println(List("ttt", Env.prod).mkString("/"))
    println(Env.values.map(_.toString).toList)
    Env.apply(1) match {
      case Env.`test` => println("test"); println(Env.test.toString)
      case Env.`stage` => println("stage")
      case Env.`prod` => println("prod")
    }
  }

  test("time") {
    val a = 1
    println(1 == a)
    val formatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
    println(formatter.parseDateTime("2014-10-22 19:31:06").toString("yyyy/MM/dd-HH:mm:ss"))
  }

  test("split") {
    val keyword = " aaa     bbb   ccc "
    keyword.trim.split("\\s+").foreach(println)
  }

  test("ts") {
    println(LocalDate.now().toDateTimeAtStartOfDay)
    val formatter: DateTimeFormatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss")
    val start = "2014-11-03 17:55:00"
    val startTime: DateTime = (if (start == null) new DateTime().minusDays(1) else formatter.parseDateTime(start))
    println(startTime.getMillis)
  }

  test("truncate") {
    val time = new DateTime().plusMinutes(10);
    println(truncatePer10m(time))
    println(time.hourOfDay().roundFloorCopy())
  }

  def truncatePer10m(time: DateTime) = {
    val minute = time.minuteOfHour().get
    println(minute)
    minute / 10 match {
      case 5 =>
        time.plusHours(1).withMinuteOfHour(0).withSecondOfMinute(0)
      case _ =>
        (minute / 10 + 1) * 10
        time.withMinuteOfHour((minute / 10 + 1) * 10).withSecondOfMinute(0)
    }
  }

  test("match") {
    pattern("1m")
    pattern("5m")
    pattern("10m")
    pattern("1h")
    pattern("6h")
    pattern("1d")
    pattern("7d")
  }

  def pattern(unit: String) = {
    val minutePattern = "(\\d+)m".r
    val hourPattern = "(\\d+)h".r
    val dayPattern = "(\\d+)d".r
    unit match {
      case minutePattern(m) => println(s"m $m")
      case hourPattern(h) => println(s"h $h")
      case dayPattern(d) => println(s"d $d")
      case _ => println("yyy")
    }
  }

  test("regex") {
    match1("cost.minute.mean")
    match1("cost.today.mean")
    match1("count.minute")
    match1("count.today")
  }

  def match1(str: String) {
    val costMeanPattern = "(cost.*).mean".r
    val countPattern = "(count.*)".r
    str match {
      case costMeanPattern(cost) => println(cost)
      case countPattern(count) => println(count)
      case x: String => println(x)
    }
  }

  test("base64") {
    val data = "xxxx=ccccc"
    //val aa = Base64.encodeBase64String(data.getBytes("utf-8"))
    val aa = scalaj.http.Base64.encodeString(data)
    //val aa = common.encode(data)
    println(aa + "dd")
    //val bb = new String(Base64.decodeBase64(aa), "utf-8")
    val bb = scalaj.http.Base64.decodeString(aa)
    println(bb)

    val m1 = MD5.digest("/opt/meituan/" + "test.txt").split(":").apply(1)
    println(m1)
  }

  test("regex path") {
    match2("2xxx-3")
    match2("xxx.xml")
    match2("xxx.^xml")
    match2("xxx^xml")
    match3("/")
    match3("/fsdfds.xml/fsdf")
    match3("/fsdfds.xml/fsdf/")
    match3("/fsdfds.xml/fs df/")
    match3("/fsdfds.xml /fsdf/")
    match3("/fsdfds-xml/12fsdf/")
    match3("/fsdfds.xml/fsdf/fdsfs/fds/fds/")
    match3("fsdfds.xml/fsdf/")
    match3("/fsdfds.xml/fsd.+f/")
  }

  def match2(str: String) {
    val p1 = """^([a-zA-Z0-9_-[\.]]+)$""".r
    str match {
      case p1(n) => println(s"$str match $n")
      case x: String => println(s"$str unmatch $x")
    }
  }

  def match3(str: String) {
    val p1 = """^(/[a-zA-Z0-9_-[\.]]+){0,}/$""".r
    println(str + " match " + p1.findAllIn(str).hasNext)
    //    println(p1.findAllIn(str))
    str match {
      case p1(n) => println(s"$str match $n")
      case x: String => println(s"$str unmatch $x")
    }
  }

  test("http") {
    val request = "http://performance.sankuai.com/api/query?access_token=5385cd607707a16953441ded&start=2014/12/19-10:55:01&end=2014/12/19-10:56:59&m=summarize(tagSeries(tagSeries('counters.mobile.sievetrip.clientCount','spanname','all'),'localhost','all'),'1m','sum')"
    implicit val timeout = Duration.create(60, duration.SECONDS)
    println(Thread.currentThread() + " 1:" + DateTime.now)
    val feature = Http(url(request) OK as.String)
    println(Thread.currentThread() + " 21:" + DateTime.now)
    val ret = Await.result(feature, timeout)
    println(Thread.currentThread() + " 22:" + DateTime.now)
    println(ret)
    println(Thread.currentThread() + " 3:" + DateTime.now)
  }

  test("ttt") {
    val tags = "localhost:all,spanname:all"
    val maps = tags.split(",").toList.map(x => (x.split(":").apply(0) -> x.split(":").apply(1))).toMap
    println(maps)
    println(maps.getOrElse("spanname", "*"))
  }

  test("date") {
    println(new DateTime(1423449614502L))
  }

  test("tair day") {
    //val day = "2015-03-09"
    val day = null
    val formatter: DateTimeFormatter = DateTimeFormat.forPattern("yyyy-MM-dd")
    val dayTime: DateTime = (if (StringUtils.isBlank(day)) new LocalDate().toDateTimeAtStartOfDay else formatter.parseDateTime(day))
    println(dayTime)
  }

  test("sa tags api") {
    val token = "Basic " + Base64.encodeToString("cos_api:fDJcetBDFiCIX8viuIb6th9".getBytes)
    val request = "http://ops.sankuai.com/api/stree/atomic/"
    implicit val timeout = Duration.create(60, duration.SECONDS)
    val feature = Http(url(request).setHeader("Authorization", token) OK as.String)
    val ret = Await.result(feature, timeout)
    println(ret)
  }

  test("ops restart") {
    //ops.refresh()
    "mobile-octo-scanner01,dx-mobile-octo-scanner01".split(",").foreach {
      host =>
        println(OpsService.restart(OpsService.ipToHost(host)))
    }
  }

  test("takeWhile") {
    var sum = 0
    (0 to 3).toStream.takeWhile(_ => sum < 1).foreach(i => {
      println(i);
      if (i > 0) sum = i
    })
    println(sum)
  }

  test("isReachable") {
    val timeout = 500
    val ip = "192.168.1.222"
    val address = InetAddress.getByName(ip)
    val result = address.isReachable(timeout)
    println(result)
  }

  test("order log") {
    val appkey = "com.sankuai.waimai.order"
    val span = "WmOrderThriftService.submit"
    val key = 398022
    val hosts = List("10.32.38.120", "10.32.38.119", "10.32.57.138", "10.32.26.246", "10.64.33.248", "10.64.40.223", "10.64.29.155", "10.64.33.134")
    //val hosts = List("10.32.38.120")
    val proxy = new ProxyServer("10.64.35.229", 80)
    val counts = hosts.flatMap {
      host =>
        val request = s"http://$host:8930/data"
        HttpHelper.execute(url(request).setProxyServer(proxy), {
          text =>
            val data = (Json.parse(text) \ "data").as[Map[String, Long]]
            data.get(s"$appkey|$span|$key")
        })
    }
    val total = counts.foldLeft(0L)(_ + _)
    println(total)
  }

  test("clear") {
    val key = 399136
    val hosts = List("10.32.38.120", "10.32.38.119", "10.32.57.138", "10.32.26.246", "10.64.33.248", "10.64.40.223", "10.64.29.155", "10.64.33.134")
    //val hosts = List("10.32.38.120")
    val proxy = new ProxyServer("10.64.35.229", 80)
    hosts.flatMap {
      host =>
        val request = s"http://$host:8930/clear?hour=$key"
        HttpHelper.execute(url(request).setProxyServer(proxy))
    }
  }

  test("refresh kpi") {
    //val proxy = new ProxyServer("10.64.35.229", 80)
    //val apps = zk.children("/mns/sankuai/prod").asScala.filter(x => x.startsWith("com.sankuai.waimai."))
    val apps = List("com.sankuai.waimai.poi")
    apps.foreach {
      appkey =>
        val request = s"http://octo.sankuai.com/data/init?day=1&appkey=${appkey}"
        println(request)
        HttpHelper.execute(url(request))(Duration.create(100, TimeUnit.MILLISECONDS))
    }
    Thread.sleep(1000)
  }

  test("par map") {
    val apps = Map(1 -> "aa", 2 -> "ffff", 3 -> "bbb", 4 -> "c", 5 -> "c", 6 -> "c", 7 -> "c", 8 -> "c", 9 -> "c")
    val start = System.currentTimeMillis()
    val results = apps.par.map {
      x => Thread.sleep(1000 * x._2.length); println(x); (x._1 -> x._2.toUpperCase)
    }.toMap.seq.asJava
    val end = System.currentTimeMillis()
    println(s"${end - start} $results")
  }

  test("xx") {
    val list = (1 to 5000 * 60).map {
      x =>
        ((Random.nextInt(10), Random.nextInt(10), Random.nextInt(10), Random.nextInt(10)), (Random.nextInt(5), Random.nextInt(500)))
    }.toList
    println(list.size)
    println(list.head)
    val merge = list.groupBy(_._1).mapValues(_.map(x => (x._2)).groupBy(_._1).mapValues(_.map(x => (x._2)).sum))
    println(merge.size)
    println(merge.head)
    //println(merge)
  }

  test("import subscribe") {
    val orgHost = "http://api.org-in.sankuai.com"
    val remoteEmployeeService = {
      val service = new RemoteEmployeeService()
      service.setClientId("msgp")
      service.setSecret("b535efb74b52d3d202cb96d2e239b454")
      service.setHost(orgHost)
      service
    }

    val users = List(5, 20, 103, 97, 877, 532, 1829, 3848, 1819, 4416, 2935, 4698).flatMap {
      orgId =>
        remoteEmployeeService.getAllEmployeeListByOrg(orgId).asScala.toList.filter {
          x =>
            x.getPosName.contains("技术") || x.getPosName.contains("工程") ||
              x.getPosName.contains("科学") || x.getPosName.contains("研究")
        }.toList
    }
    users.foreach(x => println(x.getEmail))
  }

  test("test dianping") {
    val orgHost = "http://api.org-in.sankuai.com"
    val remoteEmployeeService = {
      val service = new RemoteEmployeeService()
      service.setClientId("msgp")
      service.setSecret("b535efb74b52d3d202cb96d2e239b454")
      service.setHost(orgHost)
      service
    }

    val users = List(2003615).flatMap {
      orgId =>
        remoteEmployeeService.getAllEmployeeListByOrg(orgId).asScala.toList
    }
    users.foreach(x => println(x.getEmail))
  }

  test("test flow check") {
    OpsService.refresh()
    val appkey = "mobile.groupdeal"
    val start = "2016-01-06 09:00:01"
    val end = "2016-01-06 09:59:59"
    val result = DataQuery.validateFlow(appkey, start, end)
    println(result)
  }

  test("validate all") {
    OpsService.refresh()
    val start = "2016-01-06 09:00:01"
    val end = "2016-01-06 09:59:59"
    val result1 = DataQuery.validateAll(start, end, "client")
    println(result1)

    val result2 = DataQuery.validateAll(start, end, "server")
    println(result2)
  }

  test("banma dep") {
    // Note: 本地运行需修改DataQuery.scala 使用线上query、 第156行 "dataSource" -> "hbase" （疑似bug？）
    val timeout: FiniteDuration = Duration.create(20000, TimeUnit.MILLISECONDS)
    val start = 1482222215
    val startDate = new DateTime(1482222215*1000L)
    val end = 1482223215
    val group = "SpanRemoteApp"
    val localhost = "all"
    val remoteAppkey = "*"
    val remoteHost = "all"
    val unit = StatRange.Day.toString
    //val spanname = "BmUserThriftIface.getUserViewByAccountIdAndSourceFromCacheWithoutCheckLight"

    val apps = List(
      "banma_jiaoma_pc"
      , "com.meituan.banma.api"
      , "com.meituan.banma.api.xiniu"
      , "com.meituan.banma.data.api"
      , "com.meituan.banma.data.web"
      , "com.meituan.banma.open"
      , "com.sankuai.banma.admin"
      , "com.sankuai.banma.admin.dispatch"
      , "com.sankuai.banma.admin.finance"
      , "com.sankuai.banma.api.push"
      , "com.sankuai.banma.auth"
      , "com.sankuai.banma.biz.proxy"
      , "com.sankuai.banma.business"
      , "com.sankuai.banma.data"
      , "com.sankuai.banma.databus.sync"
      , "com.sankuai.banma.dispatch.engine"
      , "com.sankuai.banma.emp"
      , "com.sankuai.banma.finance"
      , "com.sankuai.banma.finance.admin"
      , "com.sankuai.banma.hoe"
      , "com.sankuai.banma.in"
      , "com.sankuai.banma.lbs"
      , "com.sankuai.banma.message"
      , "com.sankuai.banma.monitor"
      , "com.sankuai.banma.monitor.task"
      , "com.sankuai.banma.monitor.web"
      , "com.sankuai.banma.open"
      , "com.sankuai.banma.operation"
      , "com.sankuai.banma.operation.admin"
      , "com.sankuai.banma.paas.cache"
      , "com.sankuai.banma.package"
      , "com.sankuai.banma.package.admin"
      , "com.sankuai.banma.pricing"
      , "com.sankuai.banma.rider"
      , "com.sankuai.banma.rider.admin"
      , "com.sankuai.banma.staff"
      , "com.sankuai.banma.staff.admin"
      , "com.sankuai.banma.weather.admin"
      , "jiaoma_api"
    )
    val banmaList = apps.map {
      appkey =>
        val result = DataQuery.getDailyStatisticFormatted(appkey, "prod", startDate, "server")
        val spanQpsList = result.filter(x => x.appkey != "all" && x.qps >= 1.0).map(x => (x.spanname, x.qps)).toList
        //println(spanQpsList)

        val spanQpsRemoteAppQpsList = spanQpsList.map {
          spanQps =>
            val spanname = spanQps._1
            val dataOpt = DataQuery.getDataRecord(appkey, start, end, null, "server", null, "prod", unit,
              group, spanname, localhost, remoteAppkey, remoteHost, "hbase")
            val data = dataOpt.getOrElse(List())
            //println(data)

            val spanRemoteAppQpsList = data.filter(x => x.tags.remoteApp.getOrElse("all") != "all").
              map(x => (x.tags.spanname, x.tags.remoteApp, x.qps.head.y))
            //println(spanRemoteAppQpsList)
            (spanQps._1, spanQps._2, spanRemoteAppQpsList)
        }
        (appkey, spanQpsRemoteAppQpsList)
    }
    //println(banmaList)
    println(JsonHelper.jsonStr(banmaList))
  }

  test("kpi") {
    val list = OpsService.owtList()
    println(list)
  }
}
