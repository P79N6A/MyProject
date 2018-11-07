package com.sankuai.octo

import com.sankuai.meituan.org.remote.service.RemoteEmployeeService
import com.sankuai.msgp.common.service.org.OrgSerivce
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{BeforeAndAfter, FunSuite}

import scala.collection.JavaConverters._

@RunWith(classOf[JUnitRunner])
class OrgApiSuite extends FunSuite with BeforeAndAfter {

  test("employees") {
    //    println(orgapi.employees().length)
    val userAncestorOrgs = OrgSerivce.getUserAncestorOrgs(List(7497));
    println(userAncestorOrgs)
    val userAncestorOrgsIdSet = userAncestorOrgs.map(_.getId.toLong).toSet
    println(userAncestorOrgsIdSet)
  }

  test("all rd") {
    val orgHost = "http://api.org-in.sankuai.com"
    val service = new RemoteEmployeeService()
    service.setClientId("msgp")
    service.setSecret("b535efb74b52d3d202cb96d2e239b454")
    service.setHost(orgHost)
    val devOrgs = List(102, 1418, 1573, 1663, 1825, 3238, 3495, 5, 20, 103, 97, 877, 532, 1829,
      3848, 1819, 4416, 2935, 4698, 2003615, 2002959, 2003025, 1021868, 4962, 10206, 1021859)
    val list = devOrgs.flatMap(service.getEmployeeListByOrg(_).asScala)
    println(list.size)
    val flist = list.filter {
      x =>
        x.getCityName == "北京" && x.getPosName.contains("工程师")
    }.toList
    println(flist.size)
  }
  test("getDirectHeaderById") {
    val employ = OrgSerivce.employee("zhangkuiliang");
    println(employ)
    val id = employ.get.getId
    val head = OrgSerivce.getDirectHeader(id, "2016-09-12")
    val employHead = OrgSerivce.employee(head)
    println(employHead)
    //    println(orgapi.employee(orgapi.getDirectHeader(7655,"2016-05-12")))
    //    println(orgapi.employee(orgapi.getDirectHeader(34404,"2016-05-12")))
    //    println(orgapi.employee(orgapi.getDirectHeader(41924,"2016-05-12")))
    //    println(orgapi.employee(orgapi.getDirectHeader(41924,"2016-05-12")))
    OrgSerivce.getHeadList(employHead.getId).asScala.foreach {
      id =>
        println(OrgSerivce.employee(id));
    }
  }
  test("status") {
    val userids = "21023,21550,22708,23601,23838,25422,27038,27322,27404,27663,29170,31462,32336,33151,35340,35836,39459,42950,44132,44231,44895,47241,47242,47309,47712,48103,49908,50146,50184,50394,50667,50871,50974,53727,56876,58084,58784,58885,63417,71857,82464,85395,90993,104521,105126,1021316,1085119"
    userids.split(",").foreach {
      userid =>
        val employee = OrgSerivce.employee(userid.toInt)
        println(s"${employee.getLogin},${employee.getStatusDesc}")
    }
  }
  test("getEmpIdListByOrgIds") {
    val employ = OrgSerivce.employee("gaojiale")
    println(employ)
    println(OrgSerivce.employee(OrgSerivce.getDirectHeader(employ.get.getId, "2016-05-05")))

    //     println(orgapi.employee("limeng22"))
    //     println(orgapi.employee("caoshusheng"))
    //     println(orgapi.employee("yangdewu"))
    //     println(orgapi.employee("nini.li"))
    //status = 1 离职
    //    // 技术工程
    //      val list1 = orgapi.getEmployeeListByOrg(5)
    //      println(list1.size())
    //    // 平台业务
    //    val list2 = orgapi.service.getEmployeeListByOrg(20)
    //    println(list2.size())
    //    // 创新业务
    //    val list3 = orgapi.service.getEmployeeListByOrg(103)
    //    println(list3.size())
    //    // 外卖研发 产品：4
    //    val list4 = orgapi.service.getEmployeeListByOrg(1456)
    //    println(list4.size())
    //    // 酒店研发 酒店：101
    //    val list5 = orgapi.service.getEmployeeListByOrg(97)
    //    println(list5.size())
    //    // IT部
    //    val list6 = orgapi.service.getEmployeeListByOrg(877)
    //    println(list6.size())
    //    // dev
    //    println(orgapi.dev().size)
  }

  test("user") {
    val appkeys = "com.sankuai.pay.bankgw.abc,com.sankuai.pay.bankgw.abchz,com.sankuai.pay.bankgw.alipay,com.sankuai.pay.bankgw.alipaynotify,com.sankuai.pay.bankgw.applecup,com.sankuai.pay.bankgw.applecupnotify,com.sankuai.pay.bankgw.bankgate,com.sankuai.pay.bankgw.banktest,com.sankuai.pay.bankgw.beanstalk,com.sankuai.pay.bankgw.biz,com.sankuai.pay.bankgw.biztest,com.sankuai.pay.bankgw.boccredit,com.sankuai.pay.bankgw.bocdebit,com.sankuai.pay.bankgw.bypass,com.sankuai.pay.bankgw.ccbcredit,com.sankuai.pay.bankgw.ccbdebit,com.sankuai.pay.bankgw.ceb,com.sankuai.pay.bankgw.cibcredit,com.sankuai.pay.bankgw.cibdebit,com.sankuai.pay.bankgw.citiccredit,com.sankuai.pay.bankgw.citicdebit,com.sankuai.pay.bankgw.cmbcredit,com.sankuai.pay.bankgw.cmbdebit,com.sankuai.pay.bankgw.commcredit,com.sankuai.pay.bankgw.cron,com.sankuai.pay.bankgw.cup,com.sankuai.pay.bankgw.cupshcredit,com.sankuai.pay.bankgw.cupshcreditnotify,com.sankuai.pay.bankgw.cupshdebdk,com.sankuai.pay.bankgw.cupshdebdknotify,com.sankuai.pay.bankgw.cupshdebit,com.sankuai.pay.bankgw.cupshdebitnotify,com.sankuai.pay.bankgw.ebppceb,com.sankuai.pay.bankgw.ebppgate,com.sankuai.pay.bankgw.ebppspd,com.sankuai.pay.bankgw.ebpptest,com.sankuai.pay.bankgw.enterpay,com.sankuai.pay.bankgw.file,com.sankuai.pay.bankgw.hxb,com.sankuai.pay.bankgw.hxbfe,com.sankuai.pay.bankgw.icbc," +
      "com.sankuai.pay.bankgw.icbcsz,com.sankuai.pay.bankgw.monitor,com.sankuai.pay.bankgw.msbcredit,com.sankuai.pay.bankgw.msbkhdk,com.sankuai.pay.bankgw.msbthdk,com.sankuai.pay.bankgw.nat,com.sankuai.pay.bankgw.operation,com.sankuai.pay.bankgw.paygate,com.sankuai.pay.bankgw.payroute,com.sankuai.pay.bankgw.pingancredit,com.sankuai.pay.bankgw.pingandebit,com.sankuai.pay.bankgw.pos,com.sankuai.pay.bankgw.posabc,com.sankuai.pay.bankgw.poscib,com.sankuai.pay.bankgw.poscibalipay,com.sankuai.pay.bankgw.poscibwxpay,com.sankuai.pay.bankgw.posqiandai,com.sankuai.pay.bankgw.qqpayapp,com.sankuai.pay.bankgw.qqpayappnotify,com.sankuai.pay.bankgw.qqpayjs,com.sankuai.pay.bankgw.qqpayjsnotify,com.sankuai.pay.bankgw.qqpaywap,com.sankuai.pay.bankgw.qqpaywapnotify,com.sankuai.pay.bankgw.relay,com.sankuai.pay.bankgw.routetest,com.sankuai.pay.bankgw.sftp,com.sankuai.pay.bankgw.spdcredit,com.sankuai.pay.bankgw.spddebit,com.sankuai.pay.bankgw.tenpay,com.sankuai.pay.bankgw.tenpaynotify,com.sankuai.pay.bankgw.thirdtest,com.sankuai.pay.bankgw.unionpay,com.sankuai.pay.bankgw.unionpaynotify,com.sankuai.pay.bankgw.verifyszr,com.sankuai.pay.bankgw.wxpay,com.sankuai.pay.bankgw.wxpaynotify"
    val user_str = "lihao05,zhangyujing,caimiaomiao,gelupeng,wujiazhi,xuhaoran04"
    val map = user_str.split(",").map{
      x=>
        OrgSerivce.employee(x)
//        if(){
//         val employ = employOpt.get
//          (employ.getId,employ.getLogin,employ.getName)
//        }
    }.filter(_.isDefined).map{
      x=>
        val employ = x.get
                (employ.getId,employ.getLogin,employ.getName)
    }
    map.map(println)
    appkeys.foreach{
      appkey=>

    }
  }
}
