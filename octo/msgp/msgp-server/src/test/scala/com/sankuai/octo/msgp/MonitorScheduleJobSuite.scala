package com.sankuai.octo.msgp

import com.sankuai.octo.msgp.serivce.service.ServiceCommon
import com.sankuai.octo.msgp.task.MonitorScheduleTask
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{BeforeAndAfter, FunSuite}

/**
 * Created by zava on 16/8/2.
 */
@RunWith(classOf[JUnitRunner])
class MonitorScheduleJobSuite extends FunSuite with BeforeAndAfter {

  test("getAppSre") {
    val list = List("com.sankuai.redis.search.dw","com.sankuai.redis.fifa","com.sankuai.redis.hotel.b","com.sankuai.web.receipt.mis","com.sankuai.waimai.service.orderreport","com.sankuai.data.cis.alipay","com.sankuai.tairpool","com.sankuai.meishi.scp.customer.api","com.sankuai.movie.mmdb.likedatasync","com.sankuai.wpt.op.citynews","com.sankuai.retail.m.act","com.sankuai.dwpool","com.sankuai.data.es.searchwaimai","com.sankuai.waimai.c.thirdadmin","com.sankuai.wpt.user.cron","com.sankuai.hotel.tmc.audit","com.sankuai.wpt.jungle.junglepoim","com.sankuai.wpt.jungle.junglepoijob","com.sankuai.wpt.jungle.jungle","com.sankuai.wpt.jungle.groupapifallback")
    list.foreach{
      appkey =>
        val desc = ServiceCommon.desc(appkey)
        val sres = MonitorScheduleTask.getAppSre(desc)
        println(sres)
    }
  }
}