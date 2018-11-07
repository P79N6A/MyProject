package com.sankuai.octo.report

/**
 * Created by zava on 16/3/2.
 */

import java.util.concurrent.TimeUnit

import com.ning.http.client.cookie.Cookie
import com.sankuai.msgp.common.model.Business
import com.sankuai.msgp.common.model.ServiceModels.{Desc, User}
import com.sankuai.octo.msgp.serivce.data.DataQuery
import com.sankuai.octo.mworth.util.DateTimeUtil
import dispatch.{Http, as, url}
import org.joda.time.format.{DateTimeFormat, DateTimeFormatter}
import org.joda.time.{DateTime, Days, LocalDate}
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{BeforeAndAfter, FunSuite}
import play.api.libs.json.{JsArray, Json}

import scala.collection.concurrent.TrieMap
import scala.collection.mutable.ListBuffer
import scala.collection.parallel.ForkJoinTaskSupport
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.{Duration, FiniteDuration}
import scala.concurrent.{Await, duration}

@RunWith(classOf[JUnitRunner])
class ServiceReport extends FunSuite with BeforeAndAfter {

  private val smallThreadPool = new ForkJoinTaskSupport(new scala.concurrent.forkjoin.ForkJoinPool(80))

  // appkey ,top90,调用量,qps,服务节点数,单机QPS
  case class PerfData(qps: Double, tp90: Int, count: Long, providerd: Int = 0, sqps: Double = 0)

  case class ServicePerfData(desc: Desc, perfData: PerfData)

  case class DependCount(desc: Desc, count: Int)

  case class Kpi(business: String, qps: Double, count: Long, tp90: Double)

  //按照月份 统计最近3个月的数据 qps,tp90
  test("qps") {
    val appkeys = getAppkey()
    val months = getMonth("2016-02-01")
    val appkeyMap = appkeys.groupBy(_.business);
    val monthData = TrieMap[String, ListBuffer[Kpi]]()
    months.foreach {
      month =>
        val monthDay = getDayOfMonth(month) - 3
        val strMonth = DateTimeUtil.format(month.toDate, "yyyy年MM月");
        //.filter(_._1.getOrElse(100).equals(2))
        val businessValue = appkeyMap.map {
          x =>
            val business = x._1
            val descList = x._2
            //.filter(_.appkey.equals("com.meituan.movie.user"))
            val descPar = descList.par
            descPar.tasksupport = smallThreadPool
            val perfDatas = descPar.map {
              desc =>
                val monthPerf = getMonthPerf(desc.appkey, month)
                ServicePerfData(desc, monthPerf)
            }
            val count = perfDatas.map(_.perfData.count).sum

            val qps = count / (monthDay * 86400)
            val tp90 = if (count > 0) {
              perfDatas.map { x => x.perfData.count * x.perfData.tp90 }.sum / count
            } else {
              0
            }
            Kpi(Business.getBusinessNameById(business.getOrElse(100)), qps, count, tp90)
        }.toList
        // 获取所有的结果
        val count = businessValue.map(_.count).sum
        val qps = count / (monthDay * 86400)
        val allKpi = Kpi("总QPS", qps, count, 0)
        val listKpi = ListBuffer[Kpi]()
        listKpi.append(allKpi)
        listKpi.appendAll(businessValue)
        monthData.put(strMonth, listKpi)
    }

    val strMonth = DateTimeUtil.format(months.apply(0).toDate, "yyyy年MM月");
    val sortBusiness = monthData.getOrElse(strMonth, ListBuffer[Kpi]()).toList.sortBy(-_.qps);
    val headBusiness = sortBusiness.map(_.business).mkString("\t")
    println(s",$headBusiness")
    val strQps = sortBusiness.map(_.qps).mkString("\t")
    println(s"$strMonth,$strQps")
    (1 to months.length - 1).foreach {
      x =>
        val month = months.apply(x)
        val strMonth = DateTimeUtil.format(month.toDate, "yyyy年MM月");
        val kpiData = monthData.getOrElse(strMonth, ListBuffer[Kpi]()).toList
        val sortKpiData = sortBusiness.map {
          sortBus =>
            kpiData.filter(_.business.equals(sortBus.business)).apply(0)
        }
        val strQps = sortKpiData.map(_.qps).mkString("\t")
        println(s"$strMonth,$strQps")
    }


    //排除掉总的QPS 一项
    val sortTpBusiness = monthData.getOrElse(strMonth, ListBuffer[Kpi]()).toList.filter(!_.business.equals("总QPS")).sortBy(-_.tp90);
    val tpHead = sortTpBusiness.map(_.business).mkString("\t")
    println(s",$tpHead")
    val strTp = sortTpBusiness.map(_.tp90).mkString("\t")
    println(s"$strMonth,$strTp")
    (1 to months.length - 1).foreach {
      x =>
        val month = months.apply(x)
        val strMonth = DateTimeUtil.format(month.toDate, "yyyy年MM月");
        val tpKpi = monthData.getOrElse(strMonth, ListBuffer[Kpi]()).toList
        val sortTpKpiData = sortTpBusiness.map {
          sortTp =>
            tpKpi.filter(_.business.equals(sortTp.business)).apply(0)
        }
        val strTp90 = sortTpKpiData.map(_.tp90).mkString("\t")
        println(s"$strMonth,$strTp90")
    }
  }


  test("appkeys") {
    val appkeys = getAppkey()
    println(appkeys)
  }
  //依赖最多服务,按照月份 统计最近3天的数据 client
  test("client") {
    val appkeys = getAppkey()
    val days = getDay("2016-03-03")
    //    val depenList = appkeys.filter(_.appkey.startsWith("com.sankuai.inf")).map {
    val depenList = appkeys.par.map {
      desc =>
        val metricsTags = getDependent(desc.appkey, days.apply(0), days.apply(days.length - 1), "client")
        DependCount(desc, metricsTags.remoteAppKeys.size)
    }.toList
    val clientDep = depenList.sortBy(-_.count).take(20)
    //    println(clientDep)
    //事业群	业务线	appkey	依赖服务数
    println("事业群\t业务线\tappkey\t依赖服务数");
    clientDep.foreach {
      client =>
        println(s"${Business.getBusinessNameById(client.desc.business.getOrElse(100))},${client.desc.owt.getOrElse("")},${client.desc.appkey},${client.count}")
    }
  }
  //被依赖最多服务,按照月份 统计最近3天的数据 source
  test("server") {
    val appkeys = getAppkey()
    val days = getDay("2016-03-03")
    //    val depenList = appkeys.filter(_.appkey.startsWith("com.sankuai.inf")).map {
    val depenList = appkeys.par.map {
      desc =>
        val metricsTags = getDependent(desc.appkey, days.apply(0), days.apply(days.length - 1), "server")
        DependCount(desc, metricsTags.remoteAppKeys.size)

    }.toList
    val sourceDep = depenList.sortBy(-_.count).take(20)
    //    println(sourceDep)
    println("事业群\t业务线\tappkey\t依赖服务数");
    sourceDep.foreach {
      source =>
        println(s"${Business.getBusinessNameById(source.desc.business.getOrElse(100))},${source.desc.owt.getOrElse("")},${source.desc.appkey},${source.count}")
    }
  }

  def getDayOfMonth(start: DateTime): Int = {
    val end = start.plusMonths(1).plusDays(-1)
    Days.daysBetween(start, end).getDays
  }


  /**
   * 统计最近1天的 1W 次以上调用量QPS的服务 ,性能最好,最差的服务
   * 时间范围 ?
   * appkey ,top90,调用量,qps,服务节点数,单机QPS
   */
  test("top10") {
    val appkeys = getAppkey()
    val day = "2016-03-03";
    val days = getDay(day).toList
    //    val topList = appkeys.filter(_.appkey.startsWith("com.sankuai.inf")).map {
    val topList = appkeys.par.map {
      desc =>
        val dayPerf = getDayPerf(desc.appkey, days)
        ServicePerfData(desc, dayPerf)

    }.toList
    val list = topList.filter(_.perfData.count > 10000).sortBy(_.perfData.tp90)
    val top_10 = list.take(20).map {
      top =>
        val depen = getDependent(top.desc.appkey, days.apply(0), days.apply(days.length - 1), "source")
        val length = depen.localHostList.filter(!_.equals("all")).length
        val perfData = top.perfData.copy(providerd = length, sqps = top.perfData.qps / length)
        top.copy(perfData = perfData)
    }
    val worst_top_10 = list.takeRight(20).map {
      top =>
        val depen = getDependent(top.desc.appkey, days.apply(0), days.apply(days.length - 1), "source")
        val length = depen.localHostList.filter(!_.equals("all")).length
        val perfData = top.perfData.copy(providerd = length, sqps = top.perfData.qps / length)
        top.copy(perfData = perfData)
    }.sortBy(-_.perfData.tp90)

    //事业群	业务线	appkey	tp90耗时	调用量	QPS	服务节点数	单机QPS
    println("事业群,业务线,appkey,tp90耗时,调用量,QPS,服务节点数,单机QPS");
    top_10.foreach {
      top =>
        val qps = f"${top.perfData.qps}%.1f"
        val sqps = f"${top.perfData.sqps}%.1f"
        println(s"${Business.getBusinessNameById(top.desc.business.getOrElse(100))},${top.desc.owt.getOrElse("")},${top.desc.appkey},${top.perfData.tp90},${top.perfData.count},${qps},${top.perfData.providerd},${sqps}")
    }
    println("");
    println("事业群,业务线,appkey,tp90耗时,调用量,QPS,服务节点数,单机QPS");
    worst_top_10.foreach {
      top =>
        val qps = f"${top.perfData.qps}%.1f"
        val sqps = f"${top.perfData.sqps}%.1f"
        println(s"${Business.getBusinessNameById(top.desc.business.getOrElse(100))},${top.desc.owt.getOrElse("")},${top.desc.appkey},${top.perfData.tp90},${top.perfData.count},${qps},${top.perfData.providerd},${sqps}")
    }
  }

  test("getMonth") {
    println(getMonth("2016-03-1"))
  }
  test("getDay") {
    println(getDay("2016-03-1"))
  }
  test("getMonthQps") {
    val months = getMonth("2016-02-01")
    val month = months.apply(0)
    println(getMonthPerf("com.meituan.movie.user", month))
  }

  test("delappkey") {
    val appkeys = getAppkey()
    val time = DateTimeUtil.parse("2016-04-21 11:00:00", DateTimeUtil.DATE_TIME_FORMAT)
    val user = User(44297, "wangjing25", "王静")
    val descs = appkeys.par.filter {
      desc =>
        desc.createTime.getOrElse(0L) > time.getTime/1000 && desc.owners.contains(user)
    }.toList
    descs.foreach { desc =>delAppkey(desc.appkey) }
  }

  /**
   * 还回 MetricsTags
   */
  private def getDependent(appkey: String, start: DateTime, end: DateTime, source: String) = {
    DataQuery.tags(appkey, (start.getMillis / 1000).toInt, (end.getMillis / 1000).toInt, "prod", source)
  }

  private val dataCenterHistoryDataTimeout: FiniteDuration = Duration.create(20L, TimeUnit.SECONDS)

  /**
   * 获取指定天的QPS
   */
  private def getDayPerf(appkey: String, days: List[DateTime]) = {
    val dayPerfs = days.map {
      day =>
        val allQpsValue = DataQuery.getDailyStatisticFormatted(appkey, "prod", day).filter(_.spanname.equals("all"))
        if (allQpsValue.nonEmpty) {
          val qpsValue = allQpsValue.apply(0);
          PerfData(
            qpsValue.qps,
            qpsValue.upper90.toInt,
            qpsValue.count)
        } else {
          PerfData(
            scala.math.BigDecimal(0.00).toDouble,
            0,
            0)
        }
    }
    val count = dayPerfs.map(_.count).sum
    val qps = count.toDouble / (days.length * 86400)

    val tp90 = if (count == 0) {
      dayPerfs.map { x => x.count * x.tp90 }.sum / 1
    } else {
      dayPerfs.map { x => x.count * x.tp90 }.sum / count
    }
    PerfData(qps, tp90.toInt, count)
  }

  /**
   * 获得指定月份的appkey qps
   **/
  private def getMonthPerf(appkey: String, dateTime: DateTime) = {
    val start = dateTime.withDayOfMonth(1).withMillisOfDay(0);
    val end = start.plusMonths(1).plusDays(-1);
    val days = Days.daysBetween(start, end).getDays();
    //    val days = 3;
    val qpsvalues = (0 to days).filter { x => x != 18 && x != 19 && x != 21 }.map {
      i =>
        val allQpsValue = DataQuery.getDailyStatisticFormatted(appkey, "prod", start.plusDays(i)).filter(_.spanname.equals("all"))
        if (allQpsValue.nonEmpty) {
          val qpsValue = allQpsValue.apply(0);
          //          println(s"$i\t$appkey\t${qpsValue.count}\t${qpsValue.upper90}")
          PerfData(
            0.0,
            qpsValue.upper90.toInt,
            qpsValue.count)
        } else {

          PerfData(
            0.0,
            0,
            0)
        }
    }
    val count = qpsvalues.map(_.count).sum
    val tp90 = if (count > 0) {
      qpsvalues.map { x => x.tp90 * x.count }.sum / count
    } else {
      0
    }

    PerfData(0.0, tp90.toInt, count)
  }

  private val formatter: DateTimeFormatter = DateTimeFormat.forPattern("yyyy-MM-dd")

  private def getDay(day: String, days: Int = 5) = {
    val date = (if (day == null) LocalDate.now else formatter.parseLocalDate(day))
    val nowDay = date.toDateTimeAtStartOfDay
    (days until 0 by -1).map {
      d =>
        val day = nowDay.plusDays(-d)
        day
    }
  }

  private def getMonth(day: String) = {
    val date = (if (day == null) LocalDate.now else formatter.parseLocalDate(day))
    val nowMonth = date.withDayOfMonth(1).toDateTimeAtStartOfDay
    val oneMonth = nowMonth.plusMonths(-1)
    val twoMonth = oneMonth.plusMonths(-1)
    //    List(twoMonth, oneMonth, nowMonth)
    List(oneMonth)
  }

  implicit val timeout = Duration.create(30L, duration.SECONDS)

  def delAppkey(appkey:String) ={
    println(s"$appkey");
    Thread.sleep(5000);
    val hostname = "octo.st.sankuai.com"
    val url = s"http://$hostname/service/$appkey?force=true"
    val getReq = addHeaderAndCookie(url)
    val feature = Http(getReq.DELETE > as.String)
    val content = Await.result(feature, timeout)
    println(s"$appkey,$content")
  }

  def getAppkey(): List[Desc] = {
    //    val hostname = "release.octo.test.sankuai.info"
    val hostname = "octo.st.sankuai.com"
    val url = s"http://$hostname/service/list?pageNo=1&pageSize=2000"
    val getReq = addHeaderAndCookie(url)
    val feature = Http(getReq > as.String)
    val content = Await.result(feature, timeout)
    val appkeyDescs = (Json.parse(content) \ "data").asInstanceOf[JsArray].value.toSeq
    val appkeys = appkeyDescs.map {
      appkeyDesc =>
        (appkeyDesc).asOpt[Desc].get
    }
    appkeys.toList
  }

  def addHeaderAndCookie(urlString: String) = {
    var result = url(urlString)
    result = result
//      .addCookie(new Cookie("skmtutc", "anXjmKK6IqOWzqWlwKU2MYHkBKBCvAVjRPLo+XhY3cGN5AblSMZAI/qD8mcxKB0P3mf/Xw7XvfCVRNcjv4/xhA==-n1AR+pA8p31RiXQggI0gPqZfVnE=",
//      "anXjmKK6IqOWzqWlwKU2MYHkBKBCvAVjRPLo+XhY3cGN5AblSMZAI/qD8mcxKB0P3mf/Xw7XvfCVRNcjv4/xhA==-n1AR+pA8p31RiXQggI0gPqZfVnE=", "oct.sankuai.com", "/", -1, 2000, false, true))
//      .addCookie(new Cookie("SID", "570th7p1bt26ksolu59n401164", "570th7p1bt26ksolu59n401164", "oct.sankuai.com", "/", -1, 2000, false, true))
//      .addCookie(new Cookie("misId", "hanjiancheng", "hanjiancheng", "oct.sankuai.com", "/", -1, 2000, false, true))
//      .addCookie(new Cookie("misId.sig", "R6t6zhbvKEhWCv0U1TVz6SuDRQk", "R6t6zhbvKEhWCv0U1TVz6SuDRQk", "oct.sankuai.com", "/", -1, 2000, false, true))
//      .addCookie(new Cookie("userId", "64137", "64137", "oct.sankuai.com", "/", -1, 2000, false, true))
//      .addCookie(new Cookie("userId.sig", "rTbO-Kqsm5A1-tTXf40dHyv7PiY", "rTbO-Kqsm5A1-tTXf40dHyv7PiY", "oct.sankuai.com", "/", -1, 2000, false, true))
//      .addCookie(new Cookie("userName", "%E9%9F%A9%E5%BB%BA%E6%88%90", "%E9%9F%A9%E5%BB%BA%E6%88%90", "oct.sankuai.com", "/", -1, 2000, false, true))
//      .addCookie(new Cookie("userName.sig", "oDdXTbNSdFWkL8A51ojyIOMsgmM", "oDdXTbNSdFWkL8A51ojyIOMsgmM", "oct.sankuai.com", "/", -1, 2000, false, true))
//      .addCookie(new Cookie("__mta", "149770889.1445523886869.1445523886869.1452239177404.2", "149770889.1445523886869.1445523886869.1452239177404.2", "oct.sankuai.com", "/", -1, 2000, false, true))
//      .addCookie(new Cookie("_ga", "GA1.2.1016736437.1445859649", "GA1.2.1016736437.1445859649", "oct.sankuai.com", "/", -1, 2000, false, true))
      .addCookie(new Cookie("ssoid", "f71183875ab54adaae04f648139e01d4", "f71183875ab54adaae04f648139e01d4", "oct.sankuai.com", "/", -1, 2000, false, true))
//      .addCookie(new Cookie("JSESSIONID", "15hytutkzkqyswfrme8hmssj8", "15hytutkzkqyswfrme8hmssj8", "oct.sankuai.com", "/", -1, 2000, false, true))
    result
  }
}
