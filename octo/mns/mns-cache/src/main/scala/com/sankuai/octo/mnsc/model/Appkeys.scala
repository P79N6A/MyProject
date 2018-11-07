package com.sankuai.octo.mnsc.model

/**
  * Created by lhmily on 10/28/2016.
  */
object Appkeys {
  val sgAgent = "com.sankuai.inf.sg_agent"
  val kmsAgent = "com.sankuai.inf.kms_agent"
  val noCacheAppkeys = List(sgAgent, kmsAgent)

  val largeAppkeys = Set("com.sankuai.banma.package", "com.sankuai.banma.rider", "com.sankuai.waimai.c.apigeneralserver","com.sankuai.waimai.product",
    "com.sankuai.hotel.goods.usercache", "com.sankuai.waimai.money", "com.sankuai.travel.quark", "com.sankuai.banma.package.admin",
  "com.sankuai.hotel.goods.user", "com.sankuai.waimai.c.marketing", "com.sankuai.waimai.contract", "com.sankuai.waimai.c.coupon", "com.sankuai.waimai.c.cbaser",
    "com.sankuai.waimai.cbase", "com.sankuai.waimai.c.apicoreserver", "com.sankuai.pay.mpm", "com.sankuai.banma.staff.admin", "com.sankuai.hotel.cos.rsquery",
    "com.sankuai.cos.mtpoiop.api", "com.sankuai.hotel.goods.usercachesh", "com.sankuai.banma.staff", "com.sankuai.banma.finance", "com.sankuai.hotel.goods.data",
    "com.sankuai.hotel.goods", "com.sankuai.banma.paas.doctor", "mobile.columbus", "com.sankuai.waimai.ugc", "com.sankuai.hotel.sw.api", "com.sankuai.banma.waybill.trans",
    "com.sankuai.rc.zeus", "com.sankuai.hotel.noah.online", "com.sankuai.hotel.vangogh.online", "com.meituan.grabticket.notify", "com.sankuai.zc.open.baseqrcode",
    "com.sankuai.banma.api.push", "com.sankuai.waimai.order.datamanager", "com.sankuai.zc.cos.freeway", "com.sankuai.hotel.biz.crm", "com.sankuai.banma.waybill.transquery",
    "com.sankuai.waimai.open", "com.sankuai.waimai.e.bizme", "com.sankuai.trip.trade.center", "com.sankuai.waimai.marketing", "com.sankuai.travel.osg.triggercore",
    "com.sankuai.hotel.order", "com.sankuai.travel.dsg.tripext", "com.meituan.pic.imageproc.start", "com.sankuai.waimai.c.mig", "com.sankuai.waimai.ad",
    "com.sankuai.dataapp.search.bs", "com.sankuai.dataapp.search.qs")

  val sgAgentPort = 5266
}
