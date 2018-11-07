package com.sankuai.octo

import java.util

import com.meituan.service.mobile.mtthrift.client.model.{Server, ServerConn}
import com.sankuai.octo.statistic.ConsistentHash
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{BeforeAndAfter, FunSuite}

import scala.collection.JavaConversions._
import scala.collection.JavaConverters._

/**
 * Created by zava on 15/10/13.
 */
@RunWith(classOf[JUnitRunner])
class ConsistentHashSuit extends FunSuite with BeforeAndAfter {
  test("hash") {
    val a = "com.sankuai.data.12306.ver_collector,\ncom.sankuai.tair.test.batchdiff,\ncom.sankuai.pay.account,\ncom.sankuai.show.sell,\ncom.meituan.payment.fundstransfer,\ncom.sankuai.data.waimai_kefu,\ncom.sankuai.waimai.business,\ncom.meituan.web.groupuser.staging,\ncom.meituan.hotel.bootcamp2,\ncom.meituan.web.pointlock,\ncom.sankuai.ktv.ordercenter,\nwaimai_admin,\ncom.sankuai.tair.waimai.crank,\ncom.sankuai.tair.maiton.server,\ncom.sankuai.movie.gateway,\ncom.meituan.service.mobile.groupapi,\ncom.sankuai.dataapp.recommend.onlinelearning,\ncom.sankuai.feedback.unique_pic,\ncom.meituan.movie.activity.risk_staging,\nmobile.columbus,\ncom.sankuai.hotel.price.staging,\ncom.sankuai.maoyan.data,\ncom.meituan.mobile.ad.bs.staging,\ncom.sankuai.hotel.order.online,\ncom.sankuai.hotel.hprice.server,\ncom.meituan.movie.movieeye,\ncom.sankuai.inf.tair.hotel.image.server,\ncom.sankuai.inf.hulk.esm,\nxm.xai,\ncos.sankuai.cos.change2,\ncom.sankuai.sc.api.poisku,\ncom.sankuai.mobile.apollo.user,\ncom.sankuai.dataapp.userPro,\ncom.sankuai.tair.srq,\ncom.sankuai.waimai.rabbitmqplugin,\ncom.sankuai.hotel.goods.staging02,\ncom.sankuai.hotel.callcenter.staging,\ncom.sankuai.tair.poitest,\ncom.sankuai.waimai.risk.crawl,\ncom.meituan.xg.shop,\ncom.sankuai.hotel.sieve.index.staging,\ncom.sankuai.zaocan.activity,\ncom.sankuai.parttime,\ncom.sankuai.waimai.risk.web,\ncom.meituan.xg.demo,\ncom.meituan.movie.moviedata.moviematch,\ncom.sankuai.pay.route,\ncom.sankuai.ms,\ncom.meituan.srq.paiduitest,\ncom.meituan.xg.sku,\ncom.sankuai.banma.task,\ncom.sankuai.tair.fe.platform,\ncom.sankuai.hotel.ruleengine.server.staging,\ncom.meituan.movie.gateway-seat_staging,\ncom.sankui.hotel.hotelapi.staging.client,\ncom.meituan.hotel.mrs,\ncom.sankuai.hotel.antispider.staging,\ncom.sankuai.dataapp.userpref,\ncom.sankuai.automan.mutisearch,\ncom.sankuai.tair.web.dealservice,\ncom.sankuai.waimai.kuailv.mobile,\ncom.sankuai.cos.mtbase,\ncom.sankuai.web.finance,\ncom.sankuai.hotel.goods.staging,\ncom.sankuai.mobile.apollo.merchantplatform,\ncom.meituan.show.fixOrder,\ncom.meituan.cos.mtpbi-web,\ncom.sankuai.search.filter.qs.server,\ncom.meituan.service.mobile.junglepoi,\nwaimai_api,\ncom.meituan.movie.show_staging,\ncom.meituan.xg.customer,\ncom.sankuai.travel.discountingstorage,\ncom.sankuai.hotel.sc,\ncom.meituan.movie.machine,\ncom.meituan.movie.movieorder,\ncom.sankuai.pay.paypromo,\ncom.sankuai.banma.staff.admin,\ncom.meituan.movie.mbox-api,\ncom.sankuai.hotel.rp,\ncom.meituan.xg.promotion,\ncom.sankuai.travel.pandora.staging,\ncom.meituan.maiton,\ncom.sankuai.hotel.rssearch.staging,\ncom.sankuai.hotel.biz,\ncom.sankuai.cos.mtmop,\ncom.sankuai.hotel.ruleengine.staging02,\ncom.sankuai.mobile.group.sinai.miniflow,\ncom.meituan.hbdata.selectlog.server,\ncom.sankuai.hotel.hbdata.hotword.staging,\ncom.sankuai.piaofang.task,\nwaimai_e_api,\nwaimai_third_api,\ncom.sankuai.web.maiton,\ncom.meituan.inf.tair.feedback,\ncom.sankuai.pay.mcc,\ncom.meituan.movie.gateway.hall,\ncom.sankuai.cis.fetchparser,\ncom.sankuai.tair.pay,\ncom.sankuai.tair.it.saas,\ncom.meituan.movie.moviedata.boxoffice-staging,\ncom.meituan.movie.gateway.universalexchange_staging,\ncom.meituan.mock,\ncom.meituan.recsys.hotlist,\ncom.meituan.movie.sns.databus.order,\ncom.sankuai.mobile.apollo.touch,\ncom.meituan.hbdata.travelsearch.smartbox.rerank,\ncom.sankuai.rc.tairorderinfo,\ncom.sankuai.cos.mtupm,\ncom.sankuai.inf.tair.hotel.img.msg.client,\ncom.sankuai.movie.selllog,\ncom.sankuai.sc.api.sku,\ncom.sankuai.mobile.grouppoi.miniflow,\ncom.meituan.movie.show,\ncom.meituan.cos.ecom,\ncom.sankuai.octo.testMTthrift.client,\ncom.meituan.banma.open,\ncom.sankuai.maoyan.bigdata,\ncom.meituan.movie.sns.user,\ncom.sankuai.hotel.campaigns,\ncom.meituan.movie.sns.headline,\ncom.sankuai.fe.maiton,\ncom.meituan.movie.refund_staging,\ncom.meituan.payment.cardcenter,\ncom.sankuai.cos.mtlink,\ncom.sankuai.hotel.bizcenter,\ncom.sankuai.inf.data.statistic,\ncom.sankuai.hotel.ruleengine.staging,\ncom.sankuai.waimai.api.thrift.client,\ncom.sankuai.zaocan.supplier,\ncom.meituan.tair.piegon.message,\ncom.sankuai.cos.mtdiscount,\ncom.sankui.hotel.hotelapi,\ncom.meituan.traffic.dsp,\ncom.sankuai.waimai.promotion,\ncom.sankuai.hotel.ruleengine,\nwaimai_emergency,\ncom.sankuai.cos.photo,\ncom.sankuai.hotel.goods,\ncom.sankuai.zaocan.task,\ncom.meituan.mobile.touch,\ncom.sankuai.hotel.cms.staging,\ncom.meituan.hotel.ebooking.admin,\ncom.sankuai.banma.package,\nmobile.sievetrip,\ncom.sankui.hotel.hotelapi.client,\ncom.sankuai.tair.waimai.order,\ncom.sankuai.service.msg,\ncom.meituan.hotel.oms.staging,\ncom.meituan.show.core,\ncom.sankuai.kv.test,\ncom.sankuai.hotel.inventory.data,\ncom.sankuai.cos.mtcms,\ncom.maoyan.movie.fe.admincache,\ncom.sankuai.mobile.automan.prometheus,\ncom.meituan.movie.mmdb.notice,\ncom.meituan.pt.wedding.fe,\nwaimai_e_task,\ncom.meituan.movie.gateway.exchange,\ncom.meituan.movie.sale,\ncom.sankuai.inf.sg_agent,\ncom.sankuai.mobile.adcampaignupdate,\ncom.meituan.mobile.apollo.touch,\ncom.meituan.movie.gateway-seat,\ncom.sankuai.banma.monitor,\nmobile.groupdeal,\ncom.sankuai.inf.kms.nameStat,\ncom.meituan.hbdata.recommend.service,\ncom.sankuai.train.account,\ncom.sankuai.banma.staff,\ncom.sankuai.movie.groupordercenter.fix,\nwaimai_m_bumblebee,\ncom.sankuai.inf.cthrift.test,\ncom.sankuai.meilv.syr,\ncom.meituan.hbdata.rerank.service,\ncom.sankuai.xm.improxy,\ncom.meituan.movie.group.deal,\ncom.meituan.movie.sns.comment,\ncom.meituan.sc.web,\ncom.meituan.movie.movieshow_staging,\ncom.sankuai.inf.sgnotify,\ncom.sankuai.hotel.uts.staging,\ncom.meituan.web.combo,\ncom.sankuai.shangchao.order,\ncom.sankuai.mobile.apollo.artist,\nwaimai_service_product_server,\ncom.sankuai.hotel.admin.staging,\ncom.meituan.mx.mbox.token,\ncom.meituan.touch.mobile.tesla,\ncom.sankuai.waimai.order.trans,\ncom.meituan.web.pointlotterycurnumber,\ncom.meituan.movie.pay,\ncom.sankuai.cos.product2,\ncom.sankuai.banma.package.admin,\ncom.sankuai.shangchao.task,\ncom.sankuai.pay.bizcore,\ncom.meituan.dataapp.ads,\ncom.sankuai.data.ups_server,\ncom.sankuai.waimai.ucenter,\ncom.meituan.hotel.recommend.service,\ncom.sankuai.mobile.automan.celebrated,\ncom.meituan.train.ordercenter,\ncom.sankuai.web.orderfilter.tair,\ncom.sankuai.waimai.api.ordercenter,\ncom.sankuai.mobile.apollo.ordersearch,\ncom.sankuai.waimai.openmessagecenter,\ncom.sankuai.cis.fetchsaver,\ncolumbus.server,\ncom.sankuai.cos.mtgis,\ncom.meituan.movie.moviedata.source.cinema,\ncom.sankuai.cos.mtdeal,\ncom.sankuai.ktv.sinai,\ncom.meituan.hotel.omsrest.staging,\ncom.sankuai.waimai.risk.relevance.server,\ncom.sankuai.inf.sg_sentinel,\ncom.sankuai.pay.lottery,\ncom.sankuai.rc.aegisclient,\ncom.sankuai.tair.test.client,\ncom.meituan.service.mobile.movie.eye.pricefollow,\ncom.sankuai.merchant-ads.staging,\ncom.sankuai.traffic.dmp,\ncom.meituan.movie.antispam,\ncom.sankuai.hotel.rsindex,\ncom.sankuai.fd.ecif.credit,\ncom.sankuai.tair.maoyan.data.client,\ncom.sankuai.sr.srtable,\nmobile-groupapi-staging,\ncom.sankuai.fe.platform,\ncom.sankuai.tair.ecom,\ncom.sankuai.hotel.cos.rssync.staging01,\ncom.sankuai.mobile.apollo.order,\ncom.meituan.data.storm,\ncom.meituan.xg.client.b.order,\ncom.meituan.dataapp.ads.adsbilling,\ncom.meituan.movie.mmdb.movie.photos,\ncom.sankuai.tair.rc.server,\ncom.sankuai.pay.paytypeconfig,\ncom.sankuai.sc.client.poisku,\ncom.sankuai.web.pointlimit,\ncom.meituan.hbdata.recommend.server,\ncom.sankuai.waimai.dataindex,\ncom.sankuai.hotel.sieve.search,\ncom.sankuai.mobile.abtest,\ncom.sankuai.hotel.campaigns.staging,\ncom.sankuai.inf.dummy,\ncom.sankuai.tair.web.msg,\ncom.sankuai.hotel.cos.rsquery.staging01,\ncom.meituan.movie.geography,\ncom.sankuai.hotel.callcenter,\ncom.meituan.xg.client.d.shop,\ncom.sankuai.travel.pandora,\ncom.meituan.xg.logistics.region,\nprometheus-trip-dealserver,\ncom.sankuai.tair.inf.data.statistic,\ncom.sankuai.rc.kms,\ncom.meituan.hbdata.travelsearch,\ncom.sankuai.web.ordercenter.tair,\ncom.meituan.srq.paidui,\ncom.sankuai.inf.data.query,\ncom.sankuai.hotel.pandora.staging,\ncom.sankuai.octo.testMTthrift,\ncom.sankuai.pay.mpm,\ncom.meituan.movie.gateway.seat,\nwaimai_m_beekeeper,\ncom.sankuai.hotel.hprice.route.staging,\ncom.sankuai.tair.mbox,\nwaimai_risk,\ncom.sankuai.tair.zaocan,\ncom.meituan.movie.mmdb.movie.commentpop-offline,\ncom.sankuai.hotel.cos.rssync,\ncom.meituan.movie.mmdb.comment,\ncom.sankuai.web.orderfiltergeneral.tair,\ncom.sankuai.banma.monitor.task,\ncom.sankuai.inf.kms.name,\ncom.sankuai.travel.mschedule,\ncom.sankuai.meishi.vouchers,\ncom.sankuai.hotel.finance.staging,\ncom.sankuai.waimai.contract,\ncom.sankuai.cos.mtpoiop.web,\ncom.sankuai.sr.cms.test,\ncom.sankuai.web.aop,\ncom.meituan.travel.gtis,\ncom.sankuai.mobile.group.sinai.staging,\ncom.sankuai.rc.bm.server,\ncom.sankuai.hotel.travel.account,\ncom.sankuai.waimai.order.history,\ncom.sankuai.meilv.thames,\ncom.meituan.movie.mmdb.movie.update,\nwaimai_e_manager,\ncom.sankuai.zaocan.finance,\ncom.sankuai.movie.groupordercenter,\ncom.meituan.movie.degrade.staging,\ncom.sankuai.movie.salamander,\ncom.sankuai.pay.merchant,\ncom.sankuai.waimai.crank.tair,\ncom.meituan.movie.degrade,\ncom.meituan.maoyan.cinema_staging,\ncom.meituan.service.user,\ncom.meituan.movie.price_staging,\ncom.meituan.movie.eye,\ncom.sankuai.hotel.rsindex.staging,\ncom.sankuai.cos.mtcoop,\ncom.sankuai.waimai.infra,\ncom.sankuai.tair.dataapp.ads,\ncom.sankuai.inf.waimai_dialogue,\ncom.sankuai.tair.deal.server,\ncom.meituan.movie.gateway,\ncom.sankuai.tair.travel.gtis,\ncom.sankuai.xm.gim,\nmtupm,\ncom.sankuai.xm.security.db,\ncom.meituan.movie.bd,\ncom.sankuai.fd.rc.dc,\ncom.meituan.apollo.touch,\ncom.sankuai.hotel.pregoods.staging,\ncom.meituan.hotel.bizcenter.staging,\ncom.sankuai.zaocan.opendata,\ncom.sankuai.tair.waimai.product,\ncom.sankuai.meilv.rhone,\ncom.sankuai.deal.prefixtair,\ncom.sankuai.web.deal.rate_limit,\ncom.sankuai.cos.notify.http,\nwaimai_e,\ncom.sankuai.xm.search,\ncom.meituan.hotel.omsrest.online,\ncom.meituan.pointlottery.win.server,\ncom.meituan.xg.promotion.shoppromotionservice,\ncom.sankuai.hotel.travel.account.prod,\ncom.sankuai.dataapp.ml,\ncom.sankuai.tair.web.store,\ncom.sankuai.tair.waimai.server,\ncom.sankuai.sc.client.sku,\ncom.sankuai.waimai.open,\ncom.sankuai.hotel.antispider.product,\ncom.sankuai.cos.borp,\ncom.meituan.movie.bd_staging,\nwaimai_service_open_server,\ncom.sankuai.sc.client.property,\ncom.sankuai.hotel.order.staging03,\ncom.sankuai.hotel.order.staging02,\ncom.sankuai.waimai.order.datamanager,\nwaimai_in,\ncom.sankuai.cos.bizupm,\ncom.meituan.movie.touch,\ncom.sankuai.hotel.poi-mapper,\ncom.sankuai.inf.octo.oswatch,\ncom.sankuai.tair.offline.server,\ncom.sankuai.hotel.ruleengine.route.staging,\ncom.meituan.web.mfeedback,\ncom.meituan.service.mobile.movie.thrift.comment,\ncom.meituan.movie.fastfusion,\ncom.sankuai.tair.local.server,\ncom.sankuai.ep.hotelrsthrift,\ncom.sankuai.meilv.kiel,\ncom.sankuai.tair.image.server,\ncom.sankuai.zaocan.product,\ncom.meituan.service.mobile.movie.eye.pricefollow.staging,\ncom.sankuai.tair.test.groupvm,\ncom.meituan.movie.sale_staging,\ncom.sankuai.mobile.search.mergeservice,\ncom.sankuai.web.ordercenter,\ncom.sankuai.hotel.notify.staging,\ncom.sankuai.tair.deal.client,\ncom.meituan.payment.cardcenter.client,\ncom.sankuai.search.filter.qs,\ncom.sankuai.cos.mtpmc,\ncom.sankuai.banma.monitor.web,\ncom.sankuai.mobile.apollo.activity,\ncom.sankuai.hotel.rp.data,\ncom.sankuai.wamai.comment_classify,\ncom.sankuai.mobile.apollo.search,\ncom.sankuai.hotel.cos.rsquery,\ncom.sankuai.hotel.rp.staging,\ncom.sankuai.mobile.config,\ncom.sankuai.tair.waimai.api,\ncom.meituan.service.mobile.groupgeo,\ncom.sankuai.cis.urlmanager,\ncom.sankuai.movie.news_recomm,\ncom.sankuai.banma.finance,\ncom.sankuai.hotel.switch,\ncom.sankuai.ptorder.dealidordersync.tair,\ncom.sankuai.inf.testMtConfig,\ncom.sankuai.waimai.risk.relevance,\ncom.meituan.movie.moviedata.crawler,\nmobile.prometheus.dealsieve.staging,\ncom.sankuai.hotel.inventory.staging,\ncom.sankuai.cis.distributor,\nwaimai_e_kaidian,\ncom.sankuai.cos.mtpoiop.api,\ncom.sankuai.hotel.pandora,\ncom.sankuai.sc.search,\ncom.maoyan.movie.fe.adminsession,\ncom.sankuai.tair.fe.maiton,\ncom.sankuai.cos.mop,\ncom.sankuai.hotel.server,\ncom.meituan.movie.mmdb.wish,\ncom.sankuai.hotel.biz.staging,\nwaimai_reuse_product_api,\ncom.sankuai.inf.tair.hotel.img.msg.server,\ncom.sankuai.tair.dsp,\ncom.sankuai.rc.rcTairClient,\ncom.sankuai.cos.mtorgapi,\ncom.sankuai.rc.tairusertrust,\ncom.sankuai.travel.pandora.static,\ncom.meituan.mobile.spamcleaner,\ncom.sankuai.tair.maoyan.order,\ncom.sankuai.hotel.bizcenter.staging,\ncom.sankuai.hotel.bizapp.staging,\ncom.maoyan.movie.fe.admin,\ncom.meituan.banma.partner,\ncom.sankuai.hotel.poi-mapper.staging,\ncom.sankuai.meituan.waimai.waimai_e_dispatch_open,\ncom.sankuai.xm.shield,\ncom.meituan.zaocan.api,\ncom.meituan.movie.smaug.contractinfo.staging,\ncom.sankuai.hotel.ruleengine.online,\nwaimai_m_honeycomb,\ncom.meituan.mx.tair.mbox,\nmobile.prometheus,\ncom.sankuai.zaocan.machine,\ncom.sankuai.waimai.c.msg.processor,\ncom.meituan.sms.traffic-control,\nwaimai_e_dispatching,\ncom.meituan.xg.config.appversion,\ncom.meituan.hbdata.hotelsearch.smartbox.rerank,\nwaimai_i,\ncom.sankuai.xm.uinfo,\ncom.sankuai.hotel.inventory,\ncom.meituan.mx.mbox.tairstorage,\ncom.meituan.cis.urlmanager,\ncom.sankuai.cos.mtks,\ncom.sankuai.waimai.workflow,\ncom.sankuai.waimai.marketing,\ncom.sankuai.data.poiclass,\ncom.sankuai.tair.maoyan.backend,\ncom.sankuai.hotel.admin,\ncom.meituan.mtrace.test.MtraceTestD,\ncom.meituan.mtrace.test.MtraceTestE,\ncom.sankuai.tair.server,\ncom.meituan.hbdata.travelsearch.smartbox.server,\ncom.sankuai.dataapp.search,\ncom.sankuai.cos.mtgravity,\ncom.meituan.mtrace.test.MtraceTestA,\ncom.meituan.mtrace.test.MtraceTestB,\ncom.meituan.mtrace.test.MtraceTestC,\ncom.sankuai.orderfilter.tair,\ncom.sankuai.data.openplat,\ncom.sankuai.hotel.dealing,\ncom.sankuai.data.simnet,\ncom.sankuai.tair.web.sso.server,\ncom.meituan.xg.wms.outboundorder,\ncom.sankuai.hotel.hbdata.hotword.staging.client,\ncom.meituan.hbdata.travelsearch.smartbox.newserver,\ncom.meituan.web.sso,\ncom.sankuai.hotel.uts,\ncom.sankuai.hotel.uts.beta,\ncom.sankuai.promotion.tairinit,\ncom.meituan.zaocan.api.test,\ncom.meituan.movie.mmdb.movie,\ncom.meituan.hotel.lock.admin,\ncom.sankuai.dbus.tair.wmpoi,\ncom.sankuai.promotion.tair,\ncom.sankuai.hotel.finance,\ncom.sankuai.tair.hotel.poi-mapper,\ncom.sankuai.web.mdown,\ncom.meituan.movie.gateway-match,\ncom.sankuai.web.ordercenter.staging,\ncom.sankuai.inf.testagent,\ncom.sankuai.web.hotelPoiStatFilterClient,\ncom.sankuai.inf.test,\ncom.sankuai.xm.task,\ncom.sankuai.sc.supply,\ncom.sankuai.cos.mtmq,\ncom.meituan.movie.mmdb.movie-staging,\ncom.meituan.gct.crawl,\nwaimai_mt,\ncom.sankuai.maoyan.orderservice,\ncom.sankuai.hotel.superHearing,\ncom.sankuai.tair.image.client,\ncom.meituan.pay.paypromo,\ncom.sankuai.mobile.apollo.suppliermonitor,\ncom.sankuai.sc.task,\ncom.sankuai.srq.cms,\ncom.meituan.train.ordercenter.online,\ncom.sankuai.waimai.data,\ncom.sankuai.travel.pandora.static.staging,\ncom.sankuai.waimai.tair,\ncom.meituan.service.mobile.promotion.ml,\ncom.sankuai.hotel.superHearing.staging,\ncom.sankuai.cos.mtconfig.http,\ncom.meituan.zaocan.i,\nmtpoiop,\ncom.sankuai.dealmt.tair,\ncom.sankuai.hotel.bizapp.api-staging,\ncom.sankuai.mobile.apollo.pay,\ncom.sankuai.hotel.rssearch,\ncom.sankuai.sr.cms,\ncom.sankuai.mobile.apollo.artist-touch,\ncom.sankuai.sc.client.wm,\ncom.sankuai.web.grouporder,\ncom.sankuai.zaocan.order,\ncom.sankuai.web.hotelDealStatFilterClient,\ncom.sankuai.hotel.dealing.staging,\ncom.meituan.show.api,\ncom.meituan.xg.shopinfo,\ncom.meituan.movie.gateway-lockseat,\ncom.meituan.web.feedback,\ncom.sankuai.sc.client.category,\nwaimai_open_message_center,\ncom.sankuai.sr.ordertest,\ncom.sankuai.show.partner,\ncom.sankuai.cis.scheduler,\ncom.sankuai.shangchao.search,\ncom.meituan.waimai.m.kefu,\ncom.meituan.xg.logistics.staff,\ncom.meituan.mobile.ad.bs,\nwaimai_web,\ncom.meituan.movie.activity.risk,\ncom.sankuai.hotel.price,\ncom.sankuai.sr.queue.ordercenter.orderfix,\ncom.sankuai.web.couponindexsync,\ncom.sankuai.waimai.kaidian,\ncom.sankuai.hbdata.hotword.client,\ncom.sankuai.hotel.cms,\ncom.meituan.lvyou.ugc,\ncom.sankuai.pay.channelonline,\ncom.sankuai.hotel.server.staging,\nwaimai_open_developer,\ncom.sankuai.waimai.risk,\ncom.sankuai.movie.base,\ncom.meituan.movie.user,\ncom.sankuai.waimai.rank,\ncom.sankuai.mobile.group.sinai.spec,\ncom.sankuai.hotel.campaigns.dev,\nmobile-groupapi-miniflow,\nmobile.prometheus.staging,\ncom.sankuai.mobile.apollo.supplierplatform,\ncom.sankui.hotel.hotelapi.staging,\ncom.sankuai.waimai.poiquery,\ncom.meituan.movie.moviedata.MovieService,\ncom.sankuai.mobile.apollo.data,\ncom.sankuai.zaocan.pickup,\ncom.meituan.web.memento,\ncom.sankuai.tair.e.crm,\ncom.sankuai.vip.pay.misapi,\ncom.sankuai.deal.tair,\ncom.sankuai.hotel.pregoods,\ncom.sankuai.rc.aegis,\ncom.sankuai.pay.authkey,\ncom.meituan.movie.sns,\ncom.sankuai.hotel.ruleengine.route,\ncom.meituan.banma.api,\ncom.meituan.movie.base.tair,\ncom.sankuai.mobile.moviesearch-rerank,\ncom.sankuai.inf.sg,\ncom.meituan.movie.refund,\ncom.sankuai.merchant-ads,\ncom.sankuai.tair.movie.selllog,\ncom.sankuai.sc.api.poi,\ncom.meituan.movie.sns.group,\ncom.sankuai.mobile.apollo.stock,\ncom.sankuai.sc.yue,\ncom.sankuai.tair.web.cache,\ncom.sankuai.mobile.automan.alpha,\ncom.sankuai.ads.atp,\ncom.meituan.web.groupwallet,\ncom.meituan.hbdata.smartrerank.server,\ncom.meituan.xg.client.b,\ncom.meituan.hbdata.recommend.search.service,\ncom.meituan.xg.client.c,\ncom.sankuai.hotel.uts.test,\ncom.sankuai.web.dealidordersync.tair,\ncom.meituan.xg.client.a,\ncom.sankuai.hotel.admin.rest,\ncom.sankuai.search.plateform.qs,\ncom.sankuai.cos.price,\ncom.meituan.xg.client.d,\nwaimai_rp,\ncom.sankuai.cos.product,\ncom.meituan.tairprefix.deal,\ncom.sankuai.cos.mtct,\ncom.sankuai.dataapp.recsys.CelebrateRecommend,\nmobile-groupapi,\ncom.meituan.paidui.fe-tair,\ncom.meituan.hotel.oms.online,\ncom.sankuai.waimai.business.dispatch,\ncom.sankuai.cos.change2,\ncom.sankuai.fd.crm,\ncom.sankuai.hotel.campaigns.staging02,\ncom.sankuai.mtarm.web,\ncom.sankuai.pay.paycoupon,\ncom.meituan.cos.mtdeal2-web-sh,\ncom.meituan.web.meizhoukan,\ncom.meituan.hotel.pms.admin,\ncom.sankuai.hotel.crm,\ncom.meituan.hotel.daedalus.staging,\ncom.sankuai.zaocan.bd,\ncom.sankuai.hotel.notify,\ncom.sankuai.meilv.azeroth,\ncom.sankuai.cos.edit,\ncom.sankuai.hotel.bizapp,\ncom.sankuai.cos.ac,\ncom.meituan.movie.sns.topic,\ncom.sankuai.mobile.apollo.commonservice,\ncom.sankuai.pay.trade,\ncom.meituan.movie.crawler,\ncom.sankuai.web.deal.snapshot,\ncom.meituan.hbdata.rerank.select,\ncom.sankuai.mobile.seat.images,\ncom.meituan.hotel_admin,\nwaimai_m_sunflower,\ncom.meituan.movie.srv,\ncom.sankuai.waimai.cview,\ncom.meituan.movie.gateway.seat-staging,\ncom.sankuai.meilv.volga,\ncom.sankuai.tair.test.server,\ncom.meituan.service.mobile.movie.thrift.spamcleaner.TSpamWordService,\ncom.meituan.hotel.pms,\ncom.sankuai.waimai.audit,\ncom.meituan.thrift.IDLtest,\ncom.meituan.movie.activity,\ncom.meituan.movie.image,\ncom.sankuai.mobile.grouppoi.staging,\nmobile.prometheus.dealsieve,\ncom.sankuai.zaocan.supply,\ncom.sankuai.hotel.order,\ncom.sankuai.waimai.kuailv.admin,\ncom.sankuai.fd.rc.dc.stag,\ncom.meituan.movie.price,\ncom.sankuai.meilv.meilv,\ncom.sankuai.cos.content,\ncom.sankuai.waimai.bizauth,\ncom.sankuai.hotel.csg,\ncom.meituan.zaocan.light,\nmtsso,\nwaimai_e_dispatch_openapi,\ncom.meituan.hbdata.portrait.server,\ncom.meituan.movie.mmdb.celebrity,\ncom.sankuai.fe.mta.parser,\ncom.sankuai.waimai.money,\nbanma_jiaoma_pc,\ncom.sankuai.mobile.apollo.supplier,\ncom.meituan.banma.tongda.admin,\ncom.sankuai.inf.mtthrift.testServer,\ncom.sankuai.tair.maoyan.willhunter,\ncom.sankuai.banma.finance.admin,\ncom.meituan.movie.gateway.hall_staging,\ncom.sankuai.hotel.goods.data,\ncom.sankuai.cos.dc,\ncom.sankuai.mobile.group.sinai.spec.miniflow,\ncom.meituan.movie.recommend,\ncom.meituan.movie.movieshow,\ncom.sankuai.octo.testMTthriftClientPhp,\ncom.meituan.xg.shop.storeinfo,\ncom.sankuai.waimai.product,\ncom.sankuai.rc.bm.client,\ncom.sankuai.web.hotelStatFilter,\ncom.sankuai.inf.mnsc,\ncom.sankuai.inf.tairThriftProxy,\ncom.sankuai.zaocan.menu,\ncom.sankuai.cos.mtpbi-api,\ncom.meituan.movie.group.deal_staging,\ncom.sankuai.zaocan.poi,\ncom.meituan.pay.api,\ncom.meituan.hbdata.hotword.travel.server,\ncom.sankuai.cis.channel,\ncom.meituan.xg.shop.inventory,\ncom.meishi.dealapi.server,\ncom.sankuai.pay.paytair,\nwaimai_open,\ncom.sankuai.cos.change,\ncom.meituan.dataapp.recommend,\ncom.meituan.movie.smaug.contractinfo,\ncom.sankuai.hotel.galahad,\ncom.meituan.xg.order,\ncom.sankuai.fd.ecif.credit.stag,\ncom.meituan.movie.mmdb.movie.commentpop,\nwaimai_m_hummingbird,\ncom.sankuai.banma.admin,\ncom.sankuai.web.poisRpcClient,\ncom.meituan.mobilepay,\ncom.sankuai.octo.scanner,\ncom.meituan.movie.gateway.universalexchange,\ncom.sankuai.waimai.printerapi,\ncom.sankuai.movie.user_targeting,\ncom.sankuai.tair.merchant-ads,\ncom.sankuai.inf.kms_agent,\ncom.sankuai.dataapp.idensd,\ncom.meituan.payment.cardcenter.cardthrift,\ncom.sankuai.hotel.crm.staging,\ncom.sankuai.nuclearmq.dx,\ncom.meituan.xg.wx.client,\ncom.meituan.pic.imageproc.start,\ncom.sankuai.mobile.grouppoi,\ncom.sankuai.inf.tair.hotel.image.client,\ncom.sankuai.dbus.tair.wmorder,\ncom.meituan.hotel.daedalus.prod,\ncom.sankuai.waimai.api.ordercenter1,\nwaimai_m_bee,\nwaimai_e_liansuo,\ncom.sankuai.tair.maoyan.data,\ncom.meituan.inf.tair.user,\ncom.sankuai.hotel.order.staging,\ncom.sankuia.cos.mtupm,\ncom.sankuai.tair.share1,\ncom.meituan.hbdata.hotelsearch.smartbox.server,\ncom.sankuai.ktv.search_proxy_client,\ncom.sankuai.inf.msgp,\nwaimai_e_message_center,\ncom.sankuai.hotel.sc.staging,\ncom.sankuai.hotel.travel.account.staging,\ncom.meituan.movie.moviedata.boxoffice,\ncom.sankuai.cis.fetchserver,\ncom.sankuai.hotel.pregoods.staging02,\ncom.meituan.mobile.aopllo.touch.stage,\ncom.sankuai.sr.order,\ncom.sankuai.promotion.tairini,\nmobile.groupdeal.staging,\ncom.sankuai.hotel.hprice.route,\ncom.sankuai.mobile.automan.develop,\ncom.sankuai.waimai.activity,\ncom.meituan.hotel.bizcenter,\ncom.sankuai.web.orderversion.tair,\ncom.sankuai.vip.pay.openin,\ncom.sankuai.xm.ginfo,\ncom.sankuai.waimai.third,\ncom.sankuai.mobile.automan.online,\ncom.sankuai.tair.fd.ecif.credit,\ncom.sankuai.sc.order,\ncom.sankuai.ktv.search_proxy_server,\ncom.meituan.movie.seat.images,\ncom.meituan.movie.gateway-lockseat_staging,\ncom.sankuai.waimai.ugc,\ncom.sankuai.bp.sc.kms,\ncom.sankuai.cis.far,\ncom.sankuai.dataapp.poinearby,\ncom.sankuai.hotel.hprice.server.staging,\ncom.sankuai.cis.fetchserver_open,\ncom.sankuai.cos.groupbd,\ncom.sankuai.sc.client.poi,\ncom.meituan.lvyou.columbus.server,\ncom.meituan.hbdata.datahub.server,\ncom.meituan.maoyan.cinema,\ncom.meituan.movie.machine.staging,\ncom.meituan.service.staging.groupadmin,\ncom.sankuai.dataapp.ads.atp,\ncom.meituan.pay.mcc,\ncom.meituan.movie.mmdb.notice-staging,\ncom.sankuai.tair.ml,\ncom.meituan.movie.sns.spammer,\ncom.sankuai.web.useridcouponsync,\ncom.sankuai.mobile.seat.images.staging,\ncom.meituan.e.crm,\njiaoma_api,\ncom.sankuai.tair.maiton,\ncom.meituan.lvyou.mermaid.server,\ncom.meituan.movie.activity_staging,\ncom.sankuai.tair.mq,\ncom.sankuai.mtarm.web.External,\ncom.meituan.paidui.fe-node,\ncom.sankuai.pay.withdraw,\ncom.meituan.web.groupuser,\ncom.sankuai.sc.api.category,\ncom.sankuai.search.plateform.qs.tair.server,\ncom.sankuai.hotel.active,\ncom.sankuai.tair.remote.server,\ncom.sankuai.xm.udb,\ncom.sankuai.mobile.apollo.rating,\ncom.sankuai.hotel.travelprice,\ncom.sankuai.hotel.order.staing02,\ncom.sankuai.urlmanager.test,\ncom.sankuai.ktv.searchFilter,\ncom.sankuai.rc.offlinetairusertrust,\ncom.sankuai.xm.bizAccount,\ncom.sankuai.hotel.trainordercenter,\ncom.sankuai.srq.cmstest,\ncom.sankuai.meilv.rhein,\ncom.sankuai.cos.notify,\ncom.sankuai.it.saas,\ncom.sankuai.mobile.apollo.supplier.platform,\ncom.sankuai.sr.queue.mtthrift,\ncom.sankuai.orderfiltergeneral.tair,\ncom.sankuai.mobile.adcampaign,\ncom.sankuai.web.orderdealcates.tair,\ncom.sankuai.hotel.switch.task,\ncom.meituan.service.user.staging,\ncom.sankuai.cis.fetchserver_cis_exclusive,\ncom.sankuai.rc.tairsamehuman,\ncom.sankuai.tair.web.cache.client,\ncom.sankuai.tair.rc.counter,\ncom.meituan.hbdata.collector.fetcher,\ncom.sankuai.waimai.printer.server,\ncom.meituan.pic.imageproc,\ncom.sankuai.waimai.kuailv.vendor,\ncom.sankuai.waimai.cbase,\ncom.sankuai.zaocan.company,\ncom.meituan.movie.coupon,\ncom.meituan.movie.push,\ncom.meituan.movie.gateway.ticket,\ncom.meituan.movie.gateway.base,\ncom.meituan.movie.gateway.ticket_staging,\ncom.sankuai.rc.ruleplatform,\ncom.sankuai.hbdata.hotword,\ncom.sankuai.pay.config,\ncom.sankuai.mobile.apollo.item,\ncom.sankuai.travel.discountingstorage.staging,\ncom.sankuai.apollo.stock,\nwaimai_m_promotion,\ncom.sankuai.inf.octo.msgp,\ncom.sankuai.tair.banma,\ncom.meituan.movie.piegon.message,\nmtorgapi,\ncom.sankuai.inf.logCollector,\ncom.sankuai.data.storm,\ncom.sankuai.hotel.ruleengine.server,\ncom.meituan.xg.promotion.promotionruleapi,\ncom.meituan.xg.config.kvp,\ncom.meituan.movie.gateway.exchange_staging,\ncom.sankuai.ia.lock,\ncom.sankuai.databus.tair.test,\ncom.sankuai.inf.kmsnotify,\ncom.sankuai.zaocan.geo,\ncom.sankuai.cis.resource-manager,\ncom.sankuai.tair.rc,\ncom.sankuai.maiton.dealapi,\ncom.sankuai.inf.kms.pushLog,\ncom.sankuai.dataapp.search.server,\ncom.sankuai.hotel.sieve.search.staging,\ncom.meituan.xg.shop.credential,\ncom.sankuai.sc.ucenter,\ncom.sankuai.tair.dataapp.server,\ncom.meituan.movie.cinema,\ncom.meituan.mobile.apollo.order,\ncom.sankuai.cos.mtpt,\ncom.sankuai.travel.quark,\ncom.sankuai.mobile.group.sinai.spec.staging,\ncom.sankuai.mobile.apollo.finance,\ncom.sankuai.dbus.tair.waimai,\ncom.sankuai.sr.supplychain,\ncom.sankuai.data.test,\ncom.sankuai.service.mobile.abtest,\ncom.sankuai.hotel.sieve.index,\ncom.sankuai.sr.order.ffo.ordercenter,\ncom.sankuai.waimai.productquery,\ncom.sankuai.mtarm.core,\ncom.meituan.web.msgopt,\ncom.sankuai.banma.tair,\ncom.sankuai.tair.paidui.fe-tair,\ncom.sankuai.hotel.switch.staging,\ncom.sankuai.tair.cos,\ncom.sankuai.cos.mtsso,\ncom.meituan.hbdata.hotword.hotel.server,\ncom.sankuai.touch.tesla,\nweb.dealservice,\ncom.meituan.cos.ecom.kms,\ncom.sankuai.mobile.group.sinai,\ncom.sankuai.rc.tairusersafeinfo,\ncom.sankuai.meilv.azeroth.client,\ncom.sankuai.adam.example,\ncom.meituan.movie.seat.images.staging,\ncom.sankuai.lvyou.prometheus.trip.deal.server-staging,\ncom.sankuai.hotel.price.data,\ncom.meituan.hotel.notify,\ncom.sankuai.finance.tair,\ncom.sankuai.cos.mtconfig,\ncom.sankuai.cis.resourceManager,\ncom.sankuai.inf.jinluTestHTTP,\ncom.sankuai.rc.scorpio,\ncom.sankuai.inf.test.http,\ncom.meituan.movie.seat,\ncom.meituan.tair.travel.gtis,\ncom.meituan.lvyou.mermaid.app,\nnotify,\ncom.meituan.xg.shopgoods,\ncom.sankuai.maoyan.review,\ncom.sankuai.data.ups,\ncom.meituan.kms.zf.test2,\ncom.meituan.xg.promotion.couponbaseservice,\ncom.sankuai.waimai.poi,\ncom.meituan.paidui,\ncom.sankuai.waimai.order,\ncom.meituan.movie.cinema_staging,\ncom.sankuai.xm.token,\ncom.meituan.campaign.payment,\ncom.meituan.web.www,\nwaimai_order_asyn,\nwaimai_c_task,\ncom.sankuai.mobile.apollo.admin,\ncom.sankuai.rc.bm.server.test"
    val b = a.split(",\n")

    val serverList = Seq("10.4.38.88:8940", "10.4.38.89:8940", "10.4.38.90:8940", "10.32.92.201:8940"
      ,"10.32.96.229:8940"
      ,"10.32.94.102:8940"
      ,"10.32.92.202:8940"
      ,"10.32.94.103:8940"
      ,"10.32.62.124:8940"
      ,"10.32.94.104:8940"
      ,"10.32.94.101:8940"
      ,"10.32.93.202:8940"
      ,"10.32.95.169:8940",
      "10.32.94.89:8940","10.32.94.90:8940","10.32.94.87:8940","10.32.94.86:8940","10.32.94.88:8940","10.32.94.84:8940","10.32.74.70:8940","10.32.95.8:8940","10.32.94.85:8940","10.32.95.49:8940","10.4.54.237:8940","10.4.53.214:8940","10.4.52.210:8940","10.4.53.213:8940","10.4.51.201:8940"
    )

    val connList = serverList.map{ x=>
      val arr = x.split(":")
      val conn = new ServerConn()
      val server  = new Server(arr(0),arr(1).toInt)
      conn.setServer(server)
      conn
    }

    (3 to 101).foreach{
      i=>
        val cash  = new ConsistentHash(i, connList.asJava)
        var list = new util.ArrayList[Int]()
        b.map { x =>
          (cash.get(x), x)
        }.groupBy(_._1.getServer.getIp).foreach{
          x =>
            //             println(s"${x._1}  ${x._2.length}")
            list.add(x._2.length)
        }
        val vance =variance(list)
        println(s"$i\t$vance");
    }

  }

  def variance(datas: util.ArrayList[Int]) = {
    var sum = 0;
    datas.foreach(sum += _)
    //    println(s"sum:$sum")
    val avg = sum / datas.size() - 1;
    var variance = 0.0;
    datas.foreach{
      x =>
        variance += Math.pow(x - avg,2)
    }
    variance / datas.size() - 1
  }
}
