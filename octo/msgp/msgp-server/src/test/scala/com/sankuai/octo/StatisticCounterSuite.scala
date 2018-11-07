package com.sankuai.octo

import com.sankuai.msgp.common.utils.{DateTimeUtil, HttpUtil}
import com.sankuai.octo.msgp.serivce.data.{DataQuery, FalconQuery}
import org.apache.http.HttpHost
import org.apache.http.client.config.RequestConfig
import org.apache.http.client.methods.{HttpGet, HttpPost}
import org.apache.http.entity.{ContentType, StringEntity}
import org.apache.http.util.EntityUtils
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{BeforeAndAfter, FunSuite}
import play.api.libs.json.Json

import scala.collection.mutable
import scala.collection.parallel.ForkJoinTaskSupport

/**
 *
 * 指定appkey  获取appkey 的
 *
 * localAppKey
 * spanname，localHost，remoteAppKey，remoteHost
 *
 *
StatGroup.Span：spanname = 101
StatGroup.SpanRemoteApp：spanname*remoteAppKey     = 202
StatGroup.SpanLocalHost：spanname * localHost   = 1111
StatGroup.SpanRemoteHost：spanname * remoteHost    = 1111
StatGroup.LocalHostRemoteApp：localHost * remoteAppKey  = 22

 * 更具这些这些信息获取相关维度数据
 */
@RunWith(classOf[JUnitRunner])
class StatisticCounterSuite extends FunSuite with BeforeAndAfter {

  case class HostCount(host: String, count: Double, total: Double)

  case class AppCount(appkey: String, count: Int)

  case class TagCount(spanname: Int, remoteAppKey: Int, localHost: Int, remoteHost: Int)

  case class Appkeys(ts: Int, appkeys: List[String])

  implicit val appkeysReads = Json.reads[Appkeys]
  implicit val appkeysWrites = Json.writes[Appkeys]

  def getTags(appkey: String, start: Int, end: Int, source: String = "service", env: String = "prod") = {
    val tags = DataQuery.tags(appkey, start, end, env, source)
    val tagCount = TagCount(tags.spannames.size, tags.remoteAppKeys.size, tags.localHosts.size, tags.remoteHosts.size)
    tagCount.spanname * (1 + tagCount.localHost + tagCount.remoteHost) + tagCount.localHost + tagCount.remoteAppKey
  }

  def getTotalCount(appkey: String, start: Int, end: Int) = {
    val roles = List("service", "client")
    roles.map {
      role =>
        val count = getTags(appkey, start, end, role)
        count
    }.sum
  }

  def getappkeyList(ips: List[String]) = {
    val data = ips.map {
      ip =>
        val url = s"http://$ip:8950/appkey/list"
        val result = httpGetByProxy(url)
        (ip, result)
    }
    data
  }

  test("apkeys") {
    val ips = List("10.32.209.142", "10.32.210.218", "10.32.217.252", "10.32.203.227", "10.32.210.209", "10.32.210.201", "10.32.209.137")
    val results = getappkeyList(ips)
    results.foreach {
      result =>
        val ip = result._1
        val url_result = result._2
        val appkeyDataOpt = (Json.parse(url_result) \ "data").asOpt[List[Appkeys]]
        val appkeyData = appkeyDataOpt.get.apply(0)
        val appkeys = appkeyData.appkeys
        println("====分隔符======")
        println(ip)
        val total = getAppkeyCount(appkeys)
        println(s"total:" + total)
    }

  }
  val start = 1491274800
  val end = 1491362400

  test("count") {
    val appkeys = List("com.sankuai.waimai.infra",
      "com.sankuai.waimai.promotion",
      "com.sankuai.mobile.recsys.recommend.otherscene",
      "com.sankuai.web.opt.groupapi")
    val list = appkeys.par.map {
      appkey =>
        val count = getTotalCount(appkey, start, end)
        AppCount(appkey, count)
    }.toList
    list.sortBy(-_.count).foreach(println)
  }

  def getAppkeyCount(appkeys: List[String]) = {
    val list = appkeys.par.map {
      appkey =>
        val count = getTotalCount(appkey, start, end)
        AppCount(appkey, count)
    }.toList
    val total = list.map(_.count).sum
    list.filter(_.count > 1000).sortBy(-_.count).foreach(println)
    total
  }


  test("config") {
    val config = "com.sankuai.inf.msgp,com.sankuai.inf.logCollector,com.sankuai.inf.data.statistic,unknownService:10.20.96.163;com.meituan.srq.paidui,com.sankuai.waimai.lbs,com.sankuai.waimai.e.bizadpub:10.20.97.116;com.sankuai.waimai.order.trans:10.32.209.142;com.sankuai.wpt.op.entryop,com.sankuai.web.info.pc,com.sankuai.hotel.order.platformforapic,com.sankuai.tdc.hubble:10.20.97.115;com.sankuai.hotel.oresund,com.sankuai.hotel.open.platform,com.sankuai.search.plateform.qs:10.32.209.137;com.meituan.movie.user,com.meituan.hbdata.hotelsearch.ranking,com.sankuai.waimai.c.coupon:10.20.53.200;com.sankuai.waimai.printer:10.32.217.167;com.sankuai.mobile.group.sinai:10.32.217.234;com.sankuai.banma.rider,com.sankuai.meishi.poiapi,com.meituan.hbdata.travel.recsys,com.sankuai.waimai.kefu,waimai_e_kaidian:10.32.210.218;com.sankuai.ads.as:10.20.53.202;com.meituan.pay.api,com.sankuai.cos.mtbase,com.meituan.hotel.oms.online,com.sankuai.hotel.goods.data:10.20.53.205;com.meituan.movie.pay,web.dealservice:10.32.208.70;com.sankuai.mtarm.web,com.sankuai.mobile.grouppoi,com.sankuai.waimai.data,mobile-groupapi:10.4.101.143;com.meituan.movie.gateway,com.sankuai.waimai.poiquery,com.sankuai.waimai.activity,com.sankuai.hotel.bill:10.32.210.201;com.sankuai.mobile.recsys.recommend,com.sankuai.it.bsi.ssosv:10.20.53.204;com.sankuai.banma.staff:10.32.210.235;com.sankuai.waimai.m.queenbee,com.sankuai.train,com.sankuai.service.mobile.abtest,com.sankuai.banma.inf.dcs:10.4.221.108;com.meituan.mobile.touch:10.20.92.222;com.sankuai.hotel.goods,com.sankuai.rc.tatooine:10.4.222.194;prometheus-trip-dealserver:10.20.97.157;waimai_api: 10.5.204.102;waimai_e_api:10.20.53.203;com.sankuai.banma.message:10.20.97.114;com.sankuai.waimai.money,com.meituan.hotel.daedalus.prod:10.32.203.169;com.sankuai.mobile.recsys.recommend.homepage,com.sankuai.data.ups,com.sankuai.hotel.goods.open:10.32.208.107;mobile.prometheus:10.32.210.162;com.sankuai.waimai.ucenter:10.32.223.204;com.sankuai.waimai.productquery:10.32.212.109;com.sankuai.banma.dispatch.engine,com.sankuai.tair.waimai.cache,com.sankuai.web.campaign.api,com.sankuai.ads.campaign,com.sankuai.tair.feedback.nearby.poiimgs:10.20.96.196;com.sankuai.banma.finance.account,com.sankuai.waimai.money.e.api,com.sankuai.web.refund,com.sankuai.movie.promotion.usertrait,com.sankuai.hotel.goods.oresundservershanghai:10.20.96.167;com.sankuai.tair.waimai.server,com.sankuai.wpt.jungle.junglepoi:10.32.204.141;com.meituan.movie.mmdb.movie,com.sankuai.hotel.noah.online,com.sankuai.web.deal.dealdetail:10.32.204.135;com.sankuai.tair.banma:10.32.204.133;com.sankuai.wpt.mars.service:10.32.217.141;mobile.columbus,com.sankuai.waimai.e.messagecenter,com.sankuai.hotel.cbs.productapi,com.sankuai.banma.lbs,com.sankuai.web.deal.dealprice,com.sankuai.hotel.cos.rsquery,relation-service:10.20.96.166;com.sankuai.waimai.infra,com.sankuai.waimai.promotion:10.32.210.187;com.sankuai.waimai.service.ordercachemanager,com.sankuai.wmarch.map.facade:10.32.210.164;com.sankuai.waimai.yunying:10.20.96.174;com.meituan.movie.marketing,com.sankuai.hotelapi.select,com.sankuai.wpt.jungle.groupapi.jungleapi,com.sankuai.waimai.service.orderasync,com.sankuai.train.train.shipapi,com.sankuai.web.deal.dpapi,travel-order-server,com.sankuai.hbdata.hotelrec.rec:10.32.208.149;com.sankuai.banma.package:10.32.217.252;,com.sankuai.pt.sim,com.sankuai.waimai.bizad.api:10.20.97.117;waimai_open,waimai_m_promotion:10.20.97.113;com.sankuai.meishi.op,waimai_rp,com.sankuai.unionid,com.sankuai.waimai.e.poijob:10.32.204.77;com.sankuai.waimai.product,com.sankuai.waimai.e.promotionreuseapi:10.32.208.164;com.sankuai.meilv.volga,waimai_m_hummingbird,com.sankuai.waimai.poilogistics,com.sankuai.banma.staff.sqs:10.32.207.144;com.sankuai.banma.biz.proxy,com.sankuai.hotel.dealing,com.sankuai.waimai.order.history:10.32.204.120;com.sankuai.waimai.cbase:10.32.203.217;com.sankuai.xm.udb,com.sankuai.cloud.cloudoffice.host,com.sankuai.tair.dataapp.server,com.sankuai.banma.package.admin,com.sankuai.mobile.recsys.dealmodel:10.20.97.118;com.sankuai.mobile.automan,com.meituan.cos.mtdeal2-web-sh,com.sankuai.trip.c.volgagt:10.32.203.201;com.sankuai.movie.emember,com.sankuai.travel.dsg.crmpc,com.sankuai.movie.pro.persona,com.meituan.movie.mmdb.web:10.32.201.130;com.sankuai.waimai.ugc,com.sankuai.waimai.service.poiflowline:10.32.217.169;com.sankuai.it.bsi.ssogw,com.sankuai.waimai.m.wdcinterfaceservicepoiportal:10.32.200.157;com.sankuai.banma.admin,waimai_i:10.32.209.179;com.meituan.service.user:10.32.209.173;waimai_e:10.32.210.174;com.sankuai.inf.mtsi:10.32.210.134;com.sankuai.wpt.groupapi.groupapi.pirlo,com.sankuai.waimai.m.contractweb:10.32.210.132;com.sankuai.banma.admin.dispatch:10.20.55.167;com.sankuai.web.poi.dpapi,com.sankuai.waimai.bizauth:10.20.97.112;com.sankuai.dataapp.recsys.data:10.4.120.144;com.meituan.meishi.groupapi:10.20.53.210;com.sankuai.waimai.business:10.20.66.83;com.sankuai.banma.staff.admin:10.20.96.165;com.sankuai.meishi.grouprerank:10.20.96.164;com.sankuai.mobile.recsys.recommend.otherscene:10.20.53.207;com.sankuai.web.opt.groupapi:10.20.53.219;com.meituan.banma.api:10.20.53.206;com.sankuai.waimai.contract:10.20.53.189;com.meituan.movie.srv:10.20.53.253;com.sankuai.waimai.m.beeapi,com.sankuai.meishi.customer:10.20.47.150;com.sankuai.tower.lvxing.groupapi,com.sankuai.waimai.bizdata,com.meituan.waimai.clog:10.20.53.208;com.sankuai.cos.mtupm:10.20.53.209;com.sankuai.rc.tatooine.event.groupb,com.sankuai.waimai.order.datamanager:10.20.95.88;com.sankuai.banma.operation,com.sankuai.inf.mafka.castlecommon,com.sankuai.travel.pandora.ruleengine:10.20.54.171;com.sankuai.dataapp.recsys.recapirecommend:10.20.92.115;com.sankuai.dataapp.search.intelli,com.meituan.mobile.groupapi-do:10.20.66.82;com.sankuai.banma.finance:10.20.66.81";

    val all_appkey = mutable.HashSet[String]()
    val app_ips = config.split(";")
    app_ips.foreach { app_ip
    =>
      val app_ip_arr = app_ip.split(":")
      val apps = app_ip_arr.apply(0).split(",");
      val ip = app_ip_arr.apply(1)
      apps.par.foreach {
        appkey =>
          if(!all_appkey.contains(appkey)){
            all_appkey.add(appkey)
          }else{
            println(s"$appkey 重复")
          }

      }

      //      val newlist = list.sortBy(-_.count).filter(_.count > 1000).map(_.appkey)
      //      if (newlist.nonEmpty) {
      //        println(s"${newlist.mkString(",")}:$ip")
      //      }

    }
  }

  def httpGetByProxy(url: String): String = {
    val httpGet = new HttpGet(url)
    val proxy = new HttpHost("10.32.140.181", 80, "http");
    val config = RequestConfig.custom.setProxy(proxy)
      .setSocketTimeout(3000).setConnectTimeout(3000).setConnectionRequestTimeout(3000).build()
    httpGet.setConfig(config)
    val httpClient = HttpUtil.getHttpClient();
    val data = try {
      val response = httpClient.execute(httpGet)
      val entity = response.getEntity();
      if (entity != null) {
        val result = EntityUtils.toString(entity);
        result
      } else {
        ""
      }
    }
    catch {
      case e: Exception =>
        "error"
    }
    data
  }

  test("getFalconData") {
    val date = DateTimeUtil.parse("2017-04-08 14:00:00", DateTimeUtil.DATE_TIME_FORMAT)
    val start = (date.getTime / 1000).toInt
    val end = start + 4 * 60 * 60

    val data = getFalconData(start, end, List(
      "set-dx-inf-data-statistic01", "set-dx-inf-data-statistic02", "set-dx-inf-data-statistic03", "set-dx-inf-data-statistic04", "set-dx-inf-data-statistic05", "set-dx-inf-data-statistic06", "set-dx-inf-data-statistic07", "set-dx-inf-data-statistic08", "set-dx-inf-data-statistic09", "set-dx-inf-data-statistic10", "set-dx-inf-data-statistic11", "set-dx-inf-data-statistic12", "set-dx-inf-data-statistic13", "set-dx-inf-data-statistic14", "set-dx-inf-data-statistic15", "set-dx-inf-data-statistic16", "set-dx-inf-data-statistic17", "set-dx-inf-data-statistic18", "set-dx-inf-data-statistic19", "set-dx-inf-data-statistic20", "set-dx-inf-data-statistic21", "set-dx-inf-data-statistic22", "set-dx-inf-data-statistic23", "set-dx-inf-data-statistic24", "set-dx-inf-data-statistic25", "set-dx-inf-data-statistic26", "set-dx-inf-data-statistic27", "set-dx-inf-data-statistic28", "set-dx-inf-data-statistic29", "set-dx-inf-data-statistic30", "set-dx-inf-data-statistic31", "set-dx-inf-data-statistic32", "set-dx-inf-data-statistic33", "set-dx-inf-data-statistic34", "set-dx-inf-data-statistic35", "set-dx-inf-data-statistic36", "set-dx-inf-data-statistic37", "set-dx-inf-data-statistic38", "set-dx-inf-data-statistic39", "set-dx-inf-data-statistic40", "set-dx-inf-data-statistic41", "set-dx-inf-data-statistic42", "set-dx-inf-data-statistic43", "set-dx-inf-data-statistic44", "set-dx-inf-data-statistic45", "set-dx-inf-data-statistic46", "set-dx-inf-data-statistic47", "set-dx-inf-data-statistic48", "set-dx-inf-data-statistic49", "set-dx-inf-data-statistic50", "set-dx-inf-data-statistic51", "set-dx-inf-data-statistic52", "set-dx-inf-data-statistic53", "set-dx-inf-data-statistic54", "set-dx-inf-data-statistic55", "set-dx-inf-data-statistic56", "set-dx-inf-data-statistic57", "set-dx-inf-data-statistic58", "set-dx-inf-data-statistic59", "set-dx-inf-data-statistic60", "set-dx-inf-data-statistic61", "set-dx-inf-data-statistic62", "set-dx-inf-data-statistic63", "set-dx-inf-data-statistic64", "set-dx-inf-data-statistic65", "set-dx-inf-data-statistic66", "set-dx-inf-data-statistic67", "set-dx-inf-data-statistic68", "set-dx-inf-data-statistic69", "set-dx-inf-data-statistic70", "set-dx-inf-data-statistic71", "set-dx-inf-data-statistic72", "set-dx-inf-data-statistic73", "set-dx-inf-data-statistic74", "set-dx-inf-data-statistic75", "set-dx-inf-data-statistic76", "set-dx-inf-data-statistic77", "set-dx-inf-data-statistic78", "set-dx-inf-data-statistic79", "set-dx-inf-data-statistic80", "set-gh-inf-data-statistic01", "set-gh-inf-data-statistic02", "set-gh-inf-data-statistic03", "set-gh-inf-data-statistic04", "set-gh-inf-data-statistic05", "set-gh-inf-data-statistic06", "set-gh-inf-data-statistic07", "set-gh-inf-data-statistic08", "set-gh-inf-data-statistic09", "set-gh-inf-data-statistic10", "set-gh-inf-data-statistic11", "set-yf-inf-data-statistic02", "set-yf-inf-data-statistic04", "set-yf-inf-data-statistic05", "set-yf-inf-data-statistic06", "set-yf-inf-data-statistic13", "set-yf-inf-data-statistic15", "set-yf-inf-data-statistic16", "set-yf-inf-data-statistic17", "set-yf-inf-data-statistic19", "set-yf-inf-data-statistic20"

    ))

    val hostDatas = data.map {
      falconData =>
        val values = falconData.Values.getOrElse(List())
        if (values.nonEmpty) {
          val value = values.maxBy(_.value)
          val total = values.map(_.value.getOrElse(0.0)).sum
          val hostname = falconData.endpoint.getOrElse("")
          HostCount(hostname, value.value.getOrElse(0.0), total)
        } else {
          HostCount("", 0, 0)
        }
    }
    val sortDatas = hostDatas.sortBy(-_.count)
    println("====order by max====== ")
    sortDatas.foreach(x => println(s"${x.host}\t${x.count}\t${x.total}"))

  }

  def getFalconData(start: Int, end: Int, hostnames: List[String]) = {
    FalconQuery.getMetrics(start, end, hostnames, "mem.memused.percent")
  }

  private val taskSuppertPool = new ForkJoinTaskSupport(new scala.concurrent.forkjoin.ForkJoinPool(128))

  test("logcollector") {
    var ips = List("10.12.50.251", "10.12.52.251", "10.12.75.134", "10.12.75.167", "10.20.53.221", "10.20.53.229", "10.20.53.245", "10.20.53.246", "10.20.92.209", "10.20.92.210", "10.20.92.212", "10.20.92.213", "10.20.92.253", "10.20.95.75", "10.32.120.126", "10.32.120.127", "10.32.120.156", "10.32.120.158", "10.32.120.210", "10.32.144.152", "10.32.145.142", "10.32.170.176", "10.32.170.202", "10.32.170.212", "10.32.176.221", "10.32.176.224", "10.32.176.227", "10.32.176.234", "10.32.200.158", "10.32.201.129", "10.32.201.217", "10.32.209.191", "10.32.209.192", "10.32.209.194", "10.32.209.217", "10.32.209.220", "10.32.209.240", "10.32.209.242", "10.32.210.142", "10.32.210.143", "10.32.210.144", "10.32.210.167", "10.32.210.179", "10.32.210.180", "10.32.210.181", "10.32.210.182", "10.32.210.184", "10.32.210.211", "10.32.210.219", "10.32.210.220", "10.32.210.221", "10.32.213.70", "10.32.217.132", "10.32.217.138", "10.32.217.166", "10.32.223.233", "10.32.223.234", "10.32.223.235", "10.32.223.237", "10.32.223.240", "10.32.224.207", "10.32.224.208", "10.32.224.209", "10.32.224.210", "10.32.224.218", "10.32.224.220", "10.32.224.222", "10.32.224.232", "10.32.224.237", "10.32.225.202", "10.32.225.203", "10.32.225.204", "10.32.225.205", "10.32.225.206", "10.32.225.207", "10.32.225.208", "10.32.225.209", "10.32.225.210", "10.32.225.211", "10.32.225.212", "10.32.226.219", "10.32.226.221", "10.32.226.225", "10.32.226.226", "10.4.100.249", "10.4.100.87", "10.4.101.145", "10.4.101.146", "10.4.102.135", "10.4.102.136", "10.4.102.137", "10.4.102.143", "10.4.102.218", "10.4.102.242", "10.4.104.240", "10.4.120.134", "10.4.209.100", "10.4.209.102", "10.4.209.177", "10.4.209.191", "10.4.209.197", "10.4.209.198", "10.4.209.20", "10.4.209.34", "10.4.209.68", "10.4.209.75", "10.4.217.100", "10.4.217.186", "10.4.217.187", "10.4.217.188", "10.4.217.189", "10.4.217.190", "10.4.217.197", "10.4.217.92", "10.4.217.93", "10.4.217.94", "10.4.217.95", "10.4.217.96", "10.4.217.97", "10.4.217.98", "10.4.217.99", "10.4.221.110", "10.4.221.111", "10.4.221.243", "10.4.221.55", "10.4.222.184", "10.4.222.214", "10.4.27.73", "10.4.27.74", "10.4.27.76", "10.4.39.143", "10.4.39.158", "10.4.39.159", "10.4.39.160", "10.4.56.194", "10.4.56.21", "10.4.56.22", "10.4.56.25", "10.5.202.105", "10.5.204.99", "10.5.210.223", "10.5.214.162", "10.5.214.163", "10.5.214.164", "10.5.214.165", "10.5.214.166", "10.5.214.167", "10.5.214.227", "10.5.214.228", "10.5.214.229", "10.5.214.230", "10.5.214.235", "10.5.216.204", "10.5.216.205", "10.5.216.228", "10.67.5.229", "10.67.5.230", "10.67.5.235", "10.67.5.243", "10.67.5.244", "10.67.5.246", "10.67.5.247");

    //     ips = List("10.4.39.160","10.4.221.111","10.4.56.194","10.4.209.191","10.32.217.132","10.32.224.220","10.32.225.209","10.5.216.205","10.67.5.244","10.4.209.34","10.32.224.210","10.4.209.75","10.32.224.208","10.32.226.221","10.32.224.232","10.4.209.100","10.4.39.158","10.4.217.99","10.4.56.22","10.5.214.167","10.5.204.99","10.5.214.229","10.32.226.226","10.32.213.70","10.5.210.223","10.32.225.211","10.32.225.208","10.32.217.138","10.4.221.243","10.4.217.98","10.5.202.105","10.5.216.204","10.5.216.228","10.32.201.129","10.32.225.207","10.67.5.229","10.32.225.212","10.32.225.210","10.4.217.94","10.32.226.219")
    val ipsPar = ips.par
    ipsPar.tasksupport = taskSuppertPool
    ipsPar.foreach {
      ip =>
        val data = getLogCollector(ip)
        if (data.indexOf("error") > -1) {
          println("error\t" + ip)
        } else {
          println("good\t" + ip)
        }

    }

  }


  test("getlog") {
    val data = getLogCollector("10.12.50.251")
    println(data)
  }

  def getLogCollector(ip: String) = {
    val url = s"http://$ip:8990/apps/statistic/node"
    val httpPost = new HttpPost(url)
    val proxy = new HttpHost("10.32.140.181", 80, "http");
    val config = RequestConfig.custom.setProxy(proxy)
      .setSocketTimeout(5000).setConnectTimeout(5000).setConnectionRequestTimeout(5000).build()
    httpPost.setConfig(config)
    val params = new StringEntity("{\"apps\":[\"com.sankuai.inf.msgp\"]}", ContentType.APPLICATION_JSON)
    httpPost.setEntity(params);
    httpPost.setHeader("Content-type", "application/json");
    val httpClient = HttpUtil.getHttpClient();
    val data = try {
      val response = httpClient.execute(httpPost)
      val entity = response.getEntity();
      if (entity != null) {
        val result = EntityUtils.toString(entity);
        result
      } else {
        ""
      }
    }
    catch {
      case e: Exception =>
        "error"
    }
    data
  }

  test("provider") {
    println(getAppkeyProvider("com.sankuai.inf.logCollector"))
  }

  def getAppkeyProvider(appkey: String) = {
    val url = s"http://octo.sankuai.com/api/provider/${appkey}?type=1&pageSize=10000"
    val httpGet = new HttpGet(url)
    val config = RequestConfig.custom
      .setSocketTimeout(50000).setConnectTimeout(5000).setConnectionRequestTimeout(5000).build()
    httpGet.setConfig(config)
    val httpClient = HttpUtil.getHttpClient();
    val data = try {
      val response = httpClient.execute(httpGet)
      val entity = response.getEntity();
      if (entity != null) {
        val result = EntityUtils.toString(entity);
        result
      } else {
        ""
      }
    }
    catch {
      case e: Exception => println(e)
        ""
    }
    data
  }


}