package com.sankuai.octo

import java.util.concurrent.TimeUnit

import com.google.common.collect.Lists
import com.sankuai.msgp.common.utils.HttpUtil
import com.sankuai.msgp.common.utils.helper.{CommonHelper, JsonHelper}
import com.sankuai.octo.msgp.domain.{AppSpan, AppsKpiReq}
import com.sankuai.octo.msgp.serivce.data.{DataQuery, PublicQuery}
import com.sankuai.octo.mworth.util.DateTimeUtil
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{BeforeAndAfter, FunSuite}

import scala.collection.JavaConversions._
import scala.concurrent.duration.Duration


@RunWith(classOf[JUnitRunner])
class DataQuerySuite extends FunSuite with BeforeAndAfter {
  private implicit val timeout = Duration.create(20000, TimeUnit.MILLISECONDS)

  test("falconMinuteData") {
    val appkey = "com.sankuai.inf.msgp"
    val env = "prod"
    val start = 1505702315
    val end = 1505723915
    val source = "server"
    val group = "span"
    val spanname = "all"
    val localhost = "*"
    val remoteAppkey = "*"
    val remoteHost = null
    val dataSource = "hbase"
    val unit = "Minute"
    val data = DataQuery.getDataRecord(appkey, start, end, null, source, null, env, unit, group, spanname, localhost, remoteAppkey, remoteHost, dataSource)
    println(data)
    val data_http = DataQuery.getDataRecord(appkey, start, end, null, source, null, env, unit, group, spanname, localhost, remoteAppkey, remoteHost, dataSource)
    println(data_http)

  }
  test("getDailyStatistic") {
    val appkey = "waimai_api"
    val env = "prod"
    val dateTime = new DateTime().withTimeAtStartOfDay()
    val source = "server"
    val data = DataQuery.getDailyStatistic(appkey, env, dateTime, source)
    val data_http = DataQuery.getDailyStatistic(appkey, env, dateTime, source)
    println(data)
    println(data_http)
  }

  test("getDailyStatistic2") {
    val urlStr = s"http://data.octo.vip.sankuai.com/api/history/data"
    val map = Map("appkey" -> "mobile-groupapi"
      , "start" -> "1482307200"
      , "end" -> "1482310800"
      , "protocolType" -> "thrift"
      , "role" -> "server"
      , "dataType" -> null
      , "env" -> "prod"
      , "unit" -> "Minute"
      , "group" -> "spanLocalhost"
      , "spanname" -> "*"
      , "localhost" -> "*"
      , "remoteAppkey" -> "all"
      , "remoteHost" -> "all"
      , "dataSource" -> "hbase")
//    val head = Map("Accept-Encoding" -> "application/json")
    val head = Map("Accept-Encoding" -> "application/gzip")
    val text = HttpUtil.httpGetRequest(urlStr, head, map)
    println(text.size)


  }

  test("appskpi") {
    val start = "2016-09-11 00:00:00"
    val end = "2016-09-11 15:01:21"
    val source = "server"
    val appsKpiReq = new AppsKpiReq()
    appsKpiReq.setStart(DateTimeUtil.parse(start, DateTimeUtil.DATE_TIME_FORMAT))
    appsKpiReq.setEnd(DateTimeUtil.parse(end, DateTimeUtil.DATE_TIME_FORMAT))
    val appSpan = new AppSpan()
    val appSpan2 = new AppSpan()
    appSpan.setAppkey("com.sankuai.waimai.poi")
    appSpan.setSpanname("WmPoiTagThriftService.getWmPoiTagDicTreeByLeafId")
    appSpan2.setAppkey("com.sankuai.inf.msgp")
    appSpan2.setSpanname("APIController.report")
    val list = Lists.newArrayList(appSpan, appSpan2)
    appsKpiReq.setSpanList(list)
    appsKpiReq.setSource(source)
    println(PublicQuery.appskpi(appsKpiReq))
  }
  test("falconMinuteData_SpanRemoteApp") {
    val appkey = "com.sankuai.inf.logCollector"
    val env = "prod"
    val start = 1482221615
    val end = 1482222215
    val source = "server"
    val group = "spanRemoteapp"
    val spanname = "all"
    val localhost = "all"
    val remoteAppkey = "unknownService"
    val remoteHost = "all"

    println(DataQuery.getDataRecord(appkey, start, end, null, source, null, env, null, group, spanname, localhost, remoteAppkey, remoteHost, null))
  }

  test("query tags") {
    val appkey = "waimai_api"
    val start: Int = (new DateTime().minusDays(2).getMillis / 1000).toInt
    val end: Int = (new DateTime().getMillis / 1000).toInt
    val env: String = "prod"
    val source: String = "server"
    val tags: DataQuery.MetricsTags = DataQuery.tags(appkey, start, end, env, source)
    println(tags)
  }

  test("last data") {
    val appkey = "com.sankuai.travel.dsg.tripext"
    println(DataQuery.lastData(appkey, "prod", "server", "all"))
  }

  test("last data hbase") {
    val appkeys = "com.sankuai.tair.waimai.server,com.sankuai.dataapp.recsys.data,com.sankuai.tair.dataapp.server,com.sankuai.tair.banma,com.sankuai.inf.logCollector,com.sankuai.tair.data.uss,com.sankuai.tair.banma.staff,com.sankuai.tair.local.server,com.sankuai.tair.feedback.nearby.poiimgs,com.meituan.service.user,com.sankuai.tair.push.pushtoken,com.sankuai.waimai.poiquery,com.sankuai.banma.staff,com.sankuai.waimai.order.datamanager,com.sankuai.tair.mobile.mars,com.sankuai.tair.adp.public,com.sankuai.mobile.recsys.dealmodel,com.sankuai.tair.waimai.e.heartbeat,com.sankuai.tair.hotel.poi-mapper,com.sankuai.mobile.mars,com.sankuai.service.mobile.abtest,com.sankuai.tair.wpt.mars,com.sankuai.waimai.bizauth,com.sankuai.tair.dsp,com.sankuai.mobile.group.sinai,com.sankuai.tair.waimai.cbase,com.meituan.inf.tair.user,com.sankuai.waimai.lbs,com.sankuai.waimai.bizuser,com.sankuai.wpt.mars.service,com.sankuai.waimai.ucenter,com.sankuai.waimai.cbase,mobile-groupapi,com.sankuai.tair.pingtai.ugc,mobile.prometheus,com.sankuai.tair.web.aop.entry,com.sankuai.tair.data.ups,com.sankuai.tair.web.dealservicenew,com.sankuai.tair.hotel.portrait,com.sankuai.tair.waimai.search,com.sankuai.tair.web.msg,com.sankuai.web.campaign.api,com.sankuai.dataapp.userpref,com.sankuai.tair.rc.counter,com.sankuai.dataapp.search.server,waimai_e_api,waimai_api,mobile.groupdeal,com.sankuai.data.ups,com.sankuai.travel.osg.topsieve.online,com.sankuai.banma.staff.router,com.sankuai.inf.data.statistic,com.sankuai.travel.pandora.ruleengine,com.sankuai.tair.adp.profile,com.sankuai.banma.staff.admin,com.sankuai.cos.mtpoiop.api,com.sankuai.banma.package.admin,com.sankuai.hotel.risk.control,com.meituan.bpdata.spatial,com.sankuai.tair.adp.cache,com.sankuai.waimai.business,com.sankuai.web.deal.dealprice,com.meituan.banma.api,com.sankuai.banma.package,com.sankuai.tair.web.dealservice,com.sankuai.tair.search.featurevia,com.sankuai.data.dm.uss.sh,com.sankuai.hotel.cms,com.sankuai.tair.smartbox,com.sankuai.tair.waimai.hongbao,com.sankuai.tair.dataapp.ads,waimai_e,com.sankuai.data.uss,com.sankuai.mobile.mars.reverseAddress,com.sankuai.hotel.pandora,com.sankuai.tair.giant.map,com.sankuai.banma.lbs,com.sankuai.waimai.activity,com.sankuai.tair.waimai.crank,com.sankuai.tair.hbdata.datahub,com.sankuai.tair.web.azeroth,com.sankuai.tair.bp.ads.ms,com.sankuai.travel.campaign,com.sankuai.mobile.grouppoi,com.sankuai.wpt.op.entryop,com.sankuai.tair.deal.server,com.sankuai.waimai.productquery,com.sankuai.search.plateform.qs.tair.server,com.sankuai.hotel.noah.online,com.sankuai.tair.rc.server,com.sankuai.inf.mnsc,com.sankuai.retail.rec.hotsalerecnode,com.sankuai.retail.rec.recmanager,com.sankuai.sjst.erp.reservation,com.sankuai.travel.osg.pandoraadmin.stag,com.sankuai.zc.pos.settlecenter,com.sankuai.inf.octo.errorlog,com.sankuai.general.market,com.sankuai.inf.octo.log.monitor.frontend,com.sankuai.dataapp.search.dictionary,com.sankuai.adp.weibo.server,com.sankuai.cloudpublic.region.regionsrv,com.sankuai.ee.ufe.nginx,com.sankuai.data.ml.img,com.sankuai.sec.scanner.scanweb,com.sankuai.gct.apollo.activity,com.sankuai.data.rt.rtes,com.sankuai.inf.mafka.testappkey,com.sankuai.inf.octo.log.monitor,com.sankuai.movie.fe.fe,com.sankuai.data.dm.ussdumb,com.sankuai.ad.preference,com.sankuai.fd.fmis.trade.sg,com.sankuai.fd.fmis.account.sg,com.sankuai.dataapp.search.hotwordtool,com.sankuai.mingdian.basicUserInfo"
    appkeys.split(",").foreach {
      appkey =>
        print(appkey)
        println(DataQuery.lastData(appkey, "prod", "server", "all"))
    }

  }

  test("idc data") {
    val appkey = "com.sankuai.inf.logCollector"
    val start = 1482222215
    val end = 1482232215
    val protocolType = ""
    val role = "server"
    val dataType = ""
    val env = "prod"
    val unit = ""
    val group = "spanLocalhost"
    val spanname = ""
    val remoteAppkey = ""
    val remoteHost = ""
    val dataSource = ""
    val idc = "dx"
    var localhost = "*"
    val localhosts = List("10.32.176.234", "10.32.176.227", "10.32.176.224", "10.32.176.222",
      "10.32.176.221", "10.32.170.238", "10.32.170.213", "10.32.170.212", "10.32.170.202",
      "10.32.170.187", "10.32.170.185", "10.32.145.249", "10.32.145.142", "10.32.144.192",
      "10.32.144.173", "10.32.144.152", "10.32.120.210", "10.32.120.197", "10.32.120.158", "10.32.120.157",
      "10.32.120.156", "10.32.120.146", "10.32.120.145", "10.32.120.144", "10.32.120.143")
    println(DataQuery.getHistoryStatisticByHostList(appkey, start, end, protocolType, role, dataType, env, unit, group, spanname,
      localhosts, remoteAppkey, remoteHost, dataSource, "idc"))
    println(DataQuery.getHistoryStatisticByHostList(appkey, start, end, protocolType, role, dataType, env, unit, group, spanname,
      localhosts, remoteAppkey, remoteHost, dataSource, "idc"))
  }

  test("getIdc") {
    val list = List("10.32.176.234", "10.32.176.227", "10.32.176.224", "10.32.176.222",
      "10.32.176.221", "10.32.170.238", "10.32.170.213", "10.32.170.212", "10.32.170.202",
      "10.32.170.187", "10.32.170.185", "10.32.145.249", "10.32.145.142", "10.32.144.192",
      "10.32.144.173", "10.32.144.152", "10.32.120.210", "10.32.120.197", "10.32.120.158", "10.32.120.157",
      "10.32.120.156", "10.32.120.146", "10.32.120.145", "10.32.120.144", "10.32.120.143")
    list.foreach {
      x =>
        println(s"$x,${CommonHelper.ip2IDC(x)}")
    }
  }

  test("dayKpis") {
    val appkey = "com.sankuai.inf.logCollector"
    val env = "prod"
    val start = "2016-08-10"
    val end = "2016-08-17"
    val data = DataQuery.getDailyKpiTrends(appkey, "all", start, env, "server")
    println(JsonHelper.jsonStr(data))
  }
  test("hbase_minute") {
    val appkey = "com.sankuai.waimai.search.suggest"
    val env = "prod"
    val formatter = DateTimeFormat.forPattern("yyyy-MM-dd")
    val dateTime = formatter.parseLocalDate("2016-08-15")
    //    val text = DataQuery.HbaseData(appkey, start, end,null,source,null, env,unit, group, spanname, localhost, remoteAppkey, remoteHost)
  }

}
