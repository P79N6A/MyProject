package com.sankuai.octo.msgp.appkey

import com.sankuai.msgp.common.model.Path
import com.sankuai.msgp.common.model.ServiceModels.ProviderEdit
import com.sankuai.octo.msgp.dao.appkey.AppkeyDescDao
import com.sankuai.octo.msgp.serivce.data.DataQuery
import com.sankuai.octo.msgp.serivce.service.{AppkeyProviderService, ServiceCommon, ServiceConsumer}
import org.joda.time.DateTime
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{BeforeAndAfter, FunSuite}

@RunWith(classOf[JUnitRunner])
class ServiceSuite extends FunSuite with BeforeAndAfter {

  test("apps") {
    val appkey = "com.sankuai.cos.mtconfig"
    val env = "prod"
    println(ServiceCommon.loadByUser(64137))
    println(ServiceCommon.apps)
    val start_time: Int = ((new DateTime).minusDays(7).getMillis / 1000).toInt
    val end_time: Int = (System.currentTimeMillis / 1000).toInt
    println(DataQuery.getAppLocalhost(appkey, env, "server", start_time, end_time))
  }
  test("tags") {
    val appkey = "com.sankuai.cos.mtconfig"
    val env = "prod"
    val time = System.currentTimeMillis()
    val start_time: Int = ((new DateTime).minusDays(7).getMillis / 1000).toInt
    val end_time: Int = (System.currentTimeMillis / 1000).toInt
    println(DataQuery.getAppLocalhost(appkey, env, "server", start_time, end_time))
    println(System.currentTimeMillis() - time)
  }

  /*test("provider") {
    println(service.provider("testThriftServer", 0).length)
    println(serviceController.service.provider("testThriftServer", 1).length)
    println(serviceController.service.provider("testThriftServer", 2).length)
    println(serviceController.service.provider("testThriftServer", 3).length)
    println(serviceController.service.provider("testThriftServer").length)
    println(serviceController.service.provider("testThriftServer", 0, new Page()).length)
  }*/

  /*test("providerNode") {
    val list = serviceController.service.provider("testThriftServer")
    println(list)
    val node = ProviderNode(None, None, "testThriftServer", "1", "192.168.164.2", 9001, 10, Some(0), 0, Some(0), 1, 3, 1414051184, "",serviceInfo = None)
    node.copy(enabled =Some(0))
  }





  test("perf") {
    println(perfapi.queryTags("testthriftserver"))
  }

  test("tags-none") {
    println(perfapi.queryTags("rctairclient"))
  }

  test("group create & update") {
    val group = Group(None, "测试分组1", Some(1), "com.sankuai.inf.msgp", 3, 1, 0, ConsumerGroup(List("10.64.*"), List()), List("10.64.*:8910"), None, None, "")
    val group1 = serviceController.service.saveGroup(group.copy(priority = 222))
    println(group1)
    val group2 = serviceController.service.saveGroup(group1.copy(provider = List("*")))
    println(group2)
  }

  test("group list") {
    val list = serviceController.service.group("com.sankuai.inf.msgp", 3)
    println(list)
    val list0 = serviceController.service.group("com.sankuai.inf.msgp", 0)
    println(list0)
    val list1 = serviceController.service.group("com.sankuai.inf.msgp", 1)
    println(list1)
    val list2 = serviceController.service.group("com.sankuai.inf.msgp", 2)
    println(list2)
  }

  test("attributes") {
    val attributes = serviceController.service.groupAttributes("com.sankuai.inf.logCollector")
    println(attributes)
    println(api.dataJson(attributes))
  }

  test("monitor data") {
    println(tair.get("com.sankuai.inf.msgp.16388.count.today"))
    println(tair.get("com.sankuai.inf.msgp.23599185.count.minute"))

    println(tair.get("com.sankuai.inf.msgp.16388.cost.today"))
    println(tair.get("com.sankuai.inf.msgp.23599185.cost.minute"))
  }

  test("monitor sms") {
    println(tair.get("com.sankuai.inf.msgp.16388.count.today"))
    println(tair.get("msgp.monitor.event.com.sankuai.inf.msgp.count.today"))
    println(tair.put("msgp.monitor.event.com.sankuai.inf.msgp.count.today", "", 20))
    Thread.sleep(2000)
    println(tair.get("msgp.monitor.event.com.sankuai.inf.msgp.count.today"))
  }

  test("perm") {
    val user = new User()
    user.setId(20876)
    user.setLogin("wangshijun")
    UserUtils.bind(user)
    println(serviceController.service.listServiceByUser(true))
  }

  test("menu") {
    val m1 = Menu(11, "test", "test", List())
    println(m1)
    val m2 = Menu(12, "test", "test", List(m1))
    println(m2)
    val m3 = Menu("eee", "dd")
    println(m3)
    val m4 = Menu("eee", "dd", List(m3))
    println(m4)

    println(dashboardService.menus)
    println(dashboardService.menus)
  }

  test("exist") {
    println(serviceController.service.exist("com.sankuai.inf.msgp"))
    println(serviceController.service.exist("com.sankuai.inf.MSGP"))
    println(serviceController.service.exist("com.sankuai.inf.msg"))
  }

  test("fix weight") {
    List("prod", "stage", "test").foreach {
      env =>
        zk.children(s"/mns/sankuai/$env").asScala.foreach {
          appkey =>
            zk.children(s"/mns/sankuai/$env/$appkey/provider").asScala.foreach {
              node =>
                val nodePath = s"/mns/sankuai/$env/$appkey/provider/$node"
                val data = zk.getData(nodePath)
                Json.parse(data).validate[ProviderNode].asOpt.map {
                  provider =>
                    if ((provider.weight != 10)) {
                      println(s"$nodePath")
                      //println(Json.toJson(provider.copy(weight = 10, extend = "OCTO")).toString())
                      zk.setData(nodePath, Json.toJson(provider.copy(weight = 10, extend = provider.extend.split("\\|").apply(0))).toString())
                    }
                }
            }
        }
    }
  }

  test("nginx providers") {
    val list = serviceController.service.provider("com.sankuai.inf.msgp", 3, new Page())
    println(list)
  }

  test("correctness check for provider-http node") {
    val isOk = serviceController.service.correctnessCheck4Http()
    println(isOk.toString)
  }
  test("deleteService") {
    var appkeys = "com.sankuai.tair.push.pushtoken,tair.sankuai.mobile.sievetrip,com.sankuai.tair.vesta.rerank,com.sankuai.tair.vesta.qs,com.sankuai.tair.travel.mschedule,com.sankuai.dataapp.recapi,com.sankuai.push.pushtoken.tair,com.sankuai.tair.travel.quark,com.sankuai.inf.kms.zftest01,com.sankuai.tair.hotel.poi-mapper,com.sankuai.tair.waimai.product,com.sankuai.tair.maoyan.willhunter,com.sankuai.tair.paidui.fe-tair,com.sankuai.inf.tair.hotel.img.msg.server,com.sankuai.inf.tair.hotel.img.msg.client,com.sankuai.tair.maoyan.data,com.sankuai.search.plateform.qs.tair.server10:05,com.sankuai.search.plateform.qs,com.sankuai.waimai.risk.relevance.server,com.meituan.tair.piegon.message,com.sankuai.tair.poitest,com.sankuai.tair.maoyan.backend,com.sankuai.tair.movie.selllog,com.sankuai.inf.testagent,com.sankuai.tair.cos,com.sankuai.tair.web.dealservice,com.sankuai.tair.web.sso.server,com.sankuai.search.filter.qs.server,com.sankuai.search.filter.qs,com.sankuai.tair.maoyan.order,com.sankuai.nuclearmq.dx,com.sankuai.tair.mq,com.sankuai.tair.rc.counter,com.sankuai.tair.web.store,com.sankuai.tair.web.cache.client,com.sankuai.tair.web.cache,com.sankuai.tair.dataapp.ads10:09,com.sankuai.waimai.api.ordercenter,com.sankuai.tair.travel.gtis,com.sankuai.tair.inf.data.statistic,com.meituan.tair.travel.gtis,com.sankuai.inf.tair.hotel.image.client,com.sankuai.inf.tair.hotel.image.server,com.sankuai.tair.fe.platform,com.sankuai.tair.fe.maiton,com.sankuai.tair.share1,com.sankuai.tair.fd.ecif.credit,com.sankuai.tair.ecom,com.sankuai.tair.rc,com.sankuai.tair.pay,com.sankuai.tair.image.server,com.sankuai.tair.image.client,com.sankuai.tair.maiton.server,com.sankuai.tair.maiton,com.sankuai.tair.it.saas,com.sankuai.tair.merchant-ads,com.sankuai.tair.e.crm10:11,com.sankuai.tair.web.msg,com.sankuai.tair.mbox,com.meituan.mx.tair.mbox,com.meituan.kms.zf.test2,com.sankuai.dbus.tair.wmorder,com.meituan.pic.imageproc.start,com.meituan.pic.imageproc,com.sankuai.dataapp.userPro,com.sankuai.tair.ml,com.sankuai.dataapp.ml,com.sankuai.dbus.tair.wmpoi,com.sankuai.tair.srq,com.sankuai.dataapp.search.server,com.sankuai.dataapp.search,com.sankuai.tair.remote.server,com.sankuai.tair.local.server,com.sankuai.dbus.tair.waimai,com.sankuai.tair.waimai.order,com.sankuai.databus.tair.test,com.sankuai.tair.waimai.api10:15,com.sankuai.hotel.campaigns.staging,com.sankuai.cos.borp,com.sankuai.tair.test.groupvm,com.sankuai.tair.zaocan,com.sankuai.tair.banma,com.sankuai.tair.dsp,com.sankuai.inf.dummy,waimai_c_task,xm.xai,waimai_e_dispatching,waimai_e_task,com.sankuai.tair.deal.client,com.sankuai.tair.deal.server,com.sankuai.promotion.tairinit,com.sankuai.promotion.tairini,com.sankuai.promotion.tair,com.sankuai.tair.server,com.sankuai.rc.bm.server.test,com.sankuai.tair.rc.server,com.sankuai.rc.bm.client"
    var appkey_arr = appkeys.split(",")
    appkey_arr.foreach(
      appkey =>
        serviceDesc.delete(appkey)
    )
  }
  test("addSpace"){
    var appkeys = "com.sankuai.travel.campaign.prea";
    val cookies = new Array[Cookie](1)
    cookies(0) = service.getCookie("ssoid", "d722c5a90bfa401f8aeb5b7c1ec115fc")
    var appkey_arr = appkeys.split(",")
    appkey_arr.foreach(
      appkey =>
        serviceConfig.addSpace(appkey,cookies)
    )
  }

  test("deleteOwner"){
    val user = new com.sankuai.meituan.auth.vo.User()
    user.setLogin("hanjiancheng")
    user.setId(64137)
    user.setName("韩建成")
    service.deleteOwnerDesc(user)
  }*/


  test("listService") {
    val list = ServiceCommon.listService
    println(list.size)
    println(list)
  }


  test("provider") {
    //    val res1 = com.sankuai.octo.msgp.controller.serviceController.service.provider("com.sankuai.waimai.poiquery",3)
    //    val res2 = com.sankuai.octo.msgp.controller.serviceController.service.asyncProvider("com.sankuai.waimai.poiquery",3)
    //    println(res1)
    //    println(res2)
    println(AppkeyProviderService.provider("com.sankuai.inf.msgp", 3))
    println(AppkeyProviderService.provider("com.sankuai.inf.msgp", 3, Path.providerHttp))
  }


  test("deleteprovider") {
    val appkey = "com.sankuai.inf.sg_agent"
    val list = ",10.16.99.15,10.16.98.39,10.16.99.20,10.16.118.32,10.16.90.32,10.16.90.34,10.16.99.3,10.16.118.43,10.16.98.31,10.16.90.15,10.16.95.24,10.16.90.30,10.16.99.2,10.16.90.29,10.16.99.13,10.16.95.22,10.16.99.24,10.16.90.24,10.16.99.8,10.16.118.42,10.16.99.5,10.16.95.17,10.16.99.26,10.16.90.35,10.16.98.34,10.16.90.33,10.16.90.26,10.16.99.11,10.16.90.31,10.16.98.19,10.16.90.22,10.16.95.16,10.16.95.20,10.16.159.9,10.16.95.23,10.16.118.44,10.16.98.41,10.16.95.21,10.16.90.21,10.16.95.14,10.16.98.49,10.16.99.6,10.16.90.36,10.16.99.7,10.16.95.11,10.16.90.28,10.16.157.8,10.16.90.23,10.16.98.46,10.16.99.12"
    list.split(",").foreach {
      x =>
        val provider = ProviderEdit(appkey, x, 5266, 1, None, None, None, None, None,None,None, None,None)
        val providers = List(provider, provider.copy(env = 2), provider.copy(env = 3))
        providers.foreach {
          pro =>
            AppkeyProviderService.delProviderByType(appkey, 1, pro)
        }

    }
  }


  test("getUnknownServiceList") {
    ServiceConsumer.getUnknownServiceList("", "yesterday", "*", true)
    Thread.sleep(100000)
  }

  test("getAllHLBProvider") {
    val result = AppkeyProviderService.getAllHLBProvider
  }
  test("get category") {
    val data = AppkeyDescDao.getCategory("com.sankuai.inf.msgp")
    println(data)
  }




}