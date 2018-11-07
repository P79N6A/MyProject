package com.sankuai.octo

import com.ning.http.client.cookie.Cookie
import com.sankuai.msgp.common.model.ServiceModels
import com.sankuai.msgp.common.service.org.OrgSerivce
import com.sankuai.msgp.common.utils.HttpUtil
import com.sankuai.msgp.common.utils.helper.JsonHelper
import com.sankuai.octo.msgp.domain.{AppkeyDesc, AppkeyUser, DescUser}
import com.sankuai.octo.msgp.serivce.data.DataQuery
import dispatch.{Http, as, url}
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{BeforeAndAfter, FunSuite}
import org.springframework.test.context.web.WebAppConfiguration
import org.springframework.test.context.{ActiveProfiles, ContextConfiguration}
import play.api.libs.json.{JsArray, Json}

import scala.collection.JavaConverters._
import scala.collection.parallel.ForkJoinTaskSupport
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, duration}
/**
 * Created by zava on 16/7/15.
 */
@RunWith(classOf[JUnitRunner])
@ContextConfiguration(locations = Array(
  "classpath*:webmvc-config.xml",
  "classpath*:applicationContext.xml",
  "classpath*:applicationContext-*.xml",
  "classpath*:mybatis*.xml"))
@WebAppConfiguration
@ActiveProfiles(Array("test"))
class AppkeySuite extends FunSuite with BeforeAndAfter {

//    val hostname = "octo.test.sankuai.com"
  val hostname = "octo.st.sankuai.com"

  implicit val timeout = Duration.create(30, duration.SECONDS)
  /**
   * 读取所有的appkey,判定 业务线是否合法
   *
   */
  test("appkey") {
    val user_str = "zhangyangong"
    val map = user_str.split(",").map {
      x =>
        OrgSerivce.employee(x)
    }.filter(_.isDefined).map {
      x =>
        val employ = x.get
        val descUser = new DescUser()
        descUser.setId(employ.getId)
        descUser.setLogin(employ.getLogin)
        descUser.setName(employ.getName)
        descUser
    }
    val users = map

    val appkeys = getAppkey
    appkeys.foreach {
      desc =>
        val appkeyUser = new AppkeyUser()
        appkeyUser.setUsername("hanjiancheng")
        appkeyUser.setAppkey(desc.appkey)
//


        val new_owners =  users.toList.asJava
        val appkeyDesc = new AppkeyDesc()
        appkeyDesc.setAppkey(desc.appkey)
        appkeyDesc.setName(desc.name)
        appkeyDesc.setBaseApp(desc.baseApp.getOrElse(""))
        appkeyDesc.setIntro(desc.intro)
        appkeyDesc.setCategory(desc.category)
        appkeyDesc.setBusiness(desc.business.getOrElse(0))
        appkeyDesc.setGroup(desc.group.getOrElse(""))
        appkeyDesc.setBase(desc.base.getOrElse(0))
        appkeyDesc.setOwt(desc.owt.getOrElse(""))
        appkeyDesc.setPdl( desc.pdl.getOrElse(""))
        appkeyDesc.setLevel(desc.level.getOrElse(0))
        appkeyDesc.setTags(desc.tags)
        appkeyDesc.setRegLimit(desc.regLimit)
        val time =desc.createTime.getOrElse(System.currentTimeMillis())
        appkeyDesc.setCreateTime(time)
        appkeyDesc.setOwners(new_owners)
        updateService(desc.appkey, JsonHelper.jsonStr(appkeyDesc));
    }
  }

  def updateService(appkey: String, json: String): Unit = {
    val url = s"http://$hostname/service/$appkey/desc"
    println(s"$appkey,$json")
    val postReq = addHeaderAndCookie(url).addHeader("Content-Type", "application/json;charset=utf-8").PUT << json
    val feature = Http(postReq > as.String)
    val content = Await.result(feature, timeout)
    if(content.startsWith("<!DOCTYPE html>")){
      print(appkey)
      println(content)
    }
  }

  /*
  PUT http://octo.sankuai.com/service/com.meituan.xg.client.d.shop/desc
{"name":"","appkey":"com.meituan.xg.client.d.shop","category":"thrift","intro":"d.shop","tags":"","owt":"xianguo","pdl":"","base":"0","regLimit":"0","createTime":1441016941,"owners":[{"id":508,"login":"zhangdongxiao","name":"张冬晓"}],"observers":[]}

   */


  test("kpi") {
    println(getKpi("com.sankuai.wmarch.search.comment"))
  }

  def getKpi(appkey: String) = {
    val url = s"http://$hostname/api/app/kpi2?appkey=${appkey}&start=2016-08-01%2020%3A50%3A30&end=2016-08-02%2021%3A52%3A30&unit=Hour"
    val getReq = addHeaderAndCookie(url)
    val feature = Http(getReq > as.String)
    val text = Await.result(feature, timeout)
    val data = (Json.parse(text) \ "data").asOpt[List[DataQuery.DataRecord]]
    data
  }

  def getAppkey(): List[ServiceModels.Desc] = {
    val url = s"http://$hostname/service/search?keyword=zhangdongxiao&pageNo=1&pageSize=1000"
    val getReq = addHeaderAndCookie(url)
    val feature = Http(getReq > as.String)
    val content = Await.result(feature, timeout)
    val appkeyDescs = (Json.parse(content) \ "data").asInstanceOf[JsArray].value.toSeq
    val appkeys = appkeyDescs.map {
      appkeyDesc =>
        (appkeyDesc).asOpt[ServiceModels.Desc].get
    }
    appkeys.toList
  }

  case class ProviderEdit2(appkey: String, ip: String, port: Int, name: String, env: Int, extend: String)

  implicit val providerEdit2Reads = Json.reads[ProviderEdit2]
  implicit val providerEdit2Writes = Json.writes[ProviderEdit2]

  test("deleteproviderOnline") {
    val appkey = "com.sankuai.inf.sg_agent"
    val list = "10.16.90.24,3;10.16.98.31,2;10.16.90.30,3;10.16.90.29,2;10.16.95.24,2;10.16.118.32,3;10.16.98.34,3;10.16.98.39,3;10.16.90.34,2;10.16.98.41,2;10.16.99.20,3;10.16.90.31,2;10.16.95.16,2;10.16.99.11,3;10.16.99.8,3;10.16.99.15,3;10.16.95.22,3;10.16.99.5,3;10.16.157.8,2;10.16.90.15,2;10.16.99.7,2;10.16.95.11,3;10.16.90.33,3;10.16.98.46,2;10.16.90.21,2;10.16.99.3,2;10.16.90.35,3;10.16.118.43,3;10.16.90.26,3;10.16.99.6,2;10.16.118.42,2;10.16.98.19,2;10.16.99.2,3;10.16.90.32,2;10.16.95.20,3;10.16.159.9,3;10.16.99.13,2;10.16.90.28,2;10.16.99.26,3;10.16.90.36,2;10.16.95.17,3;10.16.118.44,2;10.16.95.23,3;10.16.98.49,3;10.16.95.14,3;10.16.90.22,3;10.16.95.21,3;10.16.99.24,2;10.16.99.12,3;10.16.90.23,3"
    list.split(";").foreach {
      x =>
        val ip_evn = x.split(",")
        val ip = ip_evn.apply(0)
        val env = ip_evn.apply(1).toInt
        val edit = ProviderEdit2(appkey, ip, 5266, ip, env, "");
        val str = JsonHelper.jsonStr(edit)
        deleteProvider(str)
    }
  }

  def deleteProvider(providerJson: String) = {
    val url = s"http://$hostname/service/com.sankuai.inf.sg_agent/provider/1/del"
    val paramStr = providerJson
    val postReq = addHeaderAndCookie(url).POST << paramStr
    val future = Http(postReq OK as.String)
    val result = Await.result(future, timeout)
    println(result)
  }

//批量增加一批
  test("user") {
//    val appkeys = "com.sankuai.pay.bankgw.cupshdebit ,com.sankuai.pay.bankgw.qqpayapp,com.sankuai.pay.bankgw.qqpayappnotify,com.sankuai.pay.bankgw.qqpaywapnotify,com.sankuai.pay.bankgw.qqpaywap,com.sankuai.pay.bankgw.poscibwxpay,com.sankuai.pay.bankgw.poscibalipay,com.sankuai.pay.bankgw.alipay ,com.sankuai.pay.bankgw.alipaynotify,com.sankuai.pay.bankgw.monitor,com.sankuai.pay.bankgw.wxpaynotify,com.sankuai.pay.bankgw.msbthdk,com.sankuai.pay.bankgw.wxpay,com.sankuai.pay.bankgw.sftp,com.sankuai.pay.bankgw.applecup,com.sankuai.pay.bankgw.payroute,com.sankuai.pay.bankgw.msbkhdk,com.sankuai.pay.bankgw.cupshcredit,com.sankuai.pay.bankgw.cupshcreditnotify ,com.sankuai.pay.bankgw.qqpayjs,com.sankuai.pay.bankgw.qqpayjsnotify ,com.sankuai.pay.bankgw.relay,com.sankuai.pay.bankgw.unionpaynotify,com.sankuai.pay.bankgw.tenpaynotify,com.sankuai.pay.bankgw.tenpay ,com.sankuai.pay.bankgw.unionpay,com.sankuai.pay.bankgw.abc ,com.sankuai.pay.bankgw.pos ,com.sankuai.pay.bankgw.hxb ,com.sankuai.pay.bankgw.icbc,com.sankuai.pay.bankgw.cup ,com.sankuai.pay.bankgw.ceb ,com.sankuai.pay.bankgw.cupshdebitnotify ,com.sankuai.pay.bankgw.spddebit,com.sankuai.pay.bankgw.spdcredit ,com.sankuai.pay.bankgw.msbcredit ,com.sankuai.pay.bankgw.icbcsz ,com.sankuai.pay.bankgw.enterpay,com.sankuai.pay.bankgw.commcredit ,com.sankuai.pay.bankgw.cmbdebit,com.sankuai.pay.bankgw.citiccredit,com.sankuai.pay.bankgw.cibdebit,com.sankuai.pay.bankgw.ccbdebit,com.sankuai.pay.bankgw.ccbcredit ,com.sankuai.pay.bankgw.bocdebit,com.sankuai.pay.bankgw.boccredit ,com.sankuai.pay.bankgw.applecupnotify,com.sankuai.pay.bankgw.banktest,com.sankuai.pay.bankgw.beanstalk ,com.sankuai.pay.bankgw.biz ,com.sankuai.pay.bankgw.biztest,com.sankuai.pay.bankgw.bypass ,com.sankuai.pay.bankgw.cibcredit ,com.sankuai.pay.bankgw.citicdebit ,com.sankuai.pay.bankgw.cmbcredit ,com.sankuai.pay.bankgw.cron,com.sankuai.pay.bankgw.file,com.sankuai.pay.bankgw.hxbfe,com.sankuai.pay.bankgw.pingancredit,com.sankuai.pay.bankgw.thirdtest ,com.sankuai.pay.bankgw.pingandebit,com.sankuai.pay.bankgw.ebpptest,com.sankuai.pay.bankgw.verifyszr ,com.sankuai.pay.bankgw.bankgate,com.sankuai.pay.bankgw.cupshdebdknotify ,com.sankuai.pay.bankgw.cupshdebdk ,com.sankuai.pay.bankgw.ebppceb,com.sankuai.pay.bankgw.paygate,com.sankuai.pay.bankgw.operation ,com.sankuai.pay.bankgw.psbocdebit ,com.sankuai.pay.bankgw.wxbiz,com.sankuai.pay.bankgw.commdebit ,com.sankuai.pay.bankgw.bankgateorder ,com.sankuai.pay.bankgw.gwfe,com.sankuai.pay.bankgw.wxpayrefundnotify ,com.sankuai.pay.bankgw.bankaccess ,com.sankuai.pay.bankgw.cupqdb ,com.sankuai.pay.bankgw.cupqdbnotify,com.sankuai.pay.bankgw.cgbcredit ,com.sankuai.pay.bankgw.netunion,com.sankuai.pay.bankgw.cuppuredk ,com.sankuai.pay.bankgw.cuppuredknotify,com.sankuai.pay.bankgw.pinganfront,com.sankuai.pay.bankgw.pinganpuredk,com.sankuai.pay.bankgw.szfesc ,com.sankuai.pay.bankgw.ccbdk,com.sankuai.pay.bankgw.phonecup,com.sankuai.pay.bankgw.phonecupnotify,com.sankuai.pay.bankgw.psboccredit,com.sankuai.pay.bankgw.cupqdbdk,com.sankuai.pay.bankgw.cupqdbdknotify"
    val appkeys = "com.sankuai.pay.fundstransfer.gwfecmbqdb ,com.sankuai.pay.fundstransfer.gwbocompay ,com.sankuai.pay.fundstransfer.gwfebocompay ,com.sankuai.pay.fundstransfer.gwfehxbpay ,com.sankuai.pay.fundstransfer.gwhxbpay ,com.sankuai.pay.fundstransfer.gwqdbpay ,com.sankuai.pay.fundstransfer.gwcebpay ,com.sankuai.pay.fundstransfer.gwfepinganpay ,com.sankuai.pay.fundstransfer.gwpinganpay ,com.sankuai.pay.fundstransfer.gwcmb ,com.sankuai.pay.fundstransfer.gwfebocpay ,com.sankuai.pay.fundstransfer.gwbocpay ,com.sankuai.pay.fundstransfer.gwfespdpay ,com.sankuai.pay.fundstransfer.gwcmbcpay ,com.sankuai.pay.fundstransfer.gwcmbcpay ,com.sankuai.pay.fundstransfer.gwspdpay ,com.sankuai.pay.fundstransfer.meetingmis ,com.sankuai.pay.fundstransfer.gwcibbj ,com.sankuai.pay.fundstransfer.gwccbhvbe ,com.sankuai.pay.fundstransfer.gwfepsbcbj ,com.sankuai.pay.fundstransfer.gwpsbcbj ,com.sankuai.pay.fundstransfer.gwabccs ,com.sankuai.pay.fundstransfer.gwfeabccs ,com.sankuai.pay.fundstransfer.gwfecib ,com.sankuai.pay.fundstransfer.channelservice ,com.sankuai.pay.fundstransfer.gwcib ,com.sankuai.pay.fundstransfer.gwcmbshagent ,com.sankuai.pay.fundstransfer.gwjob ,com.sankuai.pay.fundstransfer.gwmis ,com.sankuai.pay.fundstransfer.gwbocsh ,com.sankuai.pay.fundstransfer.gwccb ,com.sankuai.pay.fundstransfer.gwcmbshpay ,com.sankuai.pay.fundstransfer.gwicbcsz ,com.sankuai.pay.fundstransfer.gwcmbsh ,com.sankuai.pay.fundstransfer.gw ,com.sankuai.pay.fundstransfer.gwalipay";
    val user_str = "zhangyujing,chengjunbao,gelupeng,wangmin20,zhanglongyun,caimiaomiao"
    val map = user_str.split(",").map {
      x =>
        OrgSerivce.employee(x)
    }.filter(_.isDefined).map {
      x =>
        val employ = x.get
        (employ.getId, employ.getLogin, employ.getName)
    }
    val users = map.map(_._2)
    val appkey_arr = appkeys.split(",")
    appkey_arr.foreach {
      appkey =>
        val descOpt = getAppkeyDesc(appkey.trim())
        if (descOpt.isDefined) {
          val desc = descOpt.get
          val appkeyUser = new AppkeyUser()
          appkeyUser.setUsername("zhangyun16")
          appkeyUser.setAppkey(desc.appkey)
          val owner_arr = desc.owners.map {
            x =>
              x.login
          }
          val new_owners = owner_arr ::: users.toList
          appkeyUser.setOwners(new_owners.asJava)
          updateUser(appkeyUser)
        }

    }

  }

  test("removeUser") {
    val userlogin = "zuopucun"
    val applist = getApplistByUserLogin(userlogin)
    val applistPar = applist.par
    applistPar.tasksupport = new ForkJoinTaskSupport(new scala.concurrent.forkjoin.ForkJoinPool(16))
    var count = 0
    applistPar.foreach{
      app =>
        val descOpt = getAppkeyDesc(app.trim)
        if(descOpt.isDefined){
          val desc = descOpt.get
          if(!"sre".equals(desc.owt.getOrElse(""))){
            val appkeyUser = new AppkeyUser()
            appkeyUser.setUsername("zhangyun16")
            appkeyUser.setAppkey(desc.appkey)
            val owners = desc.owners.filter(x=>x.login!=userlogin)
            appkeyUser.setOwners(owners.map(_.login).asJava)
            updateUser(appkeyUser)
            count += 1
            println(s"total size is ${applist.size} now size is $count")
          }
        }
    }
//    println(applist.size)
  }

  test("changeUser") {
    val userLogin  = "zhangyun16"
    val applist = getApplistByUserLogin(userLogin)
    val applistPar = applist.par
    applistPar.tasksupport = new ForkJoinTaskSupport(new scala.concurrent.forkjoin.ForkJoinPool(16))
    var count = 0
    applistPar.foreach{
      app =>
        val descOpt = getAppkeyDesc(app.trim)
        if(descOpt.isDefined){
          val desc = descOpt.get
          if(desc.owners.size == 1 && desc.owners.head.login.equals("zhangyun16")){
            val appkeyUser = new AppkeyUser
            appkeyUser.setUsername("zhangyun16")
            appkeyUser.setAppkey(desc.appkey)
            val newOwners = List[String]("inf.octo")
            appkeyUser.setOwners(newOwners.asJava)
            updateUser(appkeyUser)
            count += 1
            println(s"total size is ${applist.size} now size is $count")
          }
        }
    }
  }

  def getApplistByUserLogin(userlogin: String) = {
    val urlString = s"http://$hostname/api/apps?username=$userlogin"
    val req = url(urlString)
    val result = Http(req > as.String)
    val text = Await.result(result, timeout)
    val data = (Json.parse(text)\"data").as[List[String]]
    data
  }


  def updateUser(appkeyUser: AppkeyUser) = {
    val url = s"http://$hostname/api/service/updateuser"
    val paramStr = JsonHelper.jsonStr(appkeyUser)
    val result = HttpUtil.httpPostRequest(url, paramStr)
    println(result)
  }

  def getAppkeyDesc(appkey: String) = {
    val url = s"http://$hostname/api/service/?appkey=$appkey"
    val getReq = addHeaderAndCookie(url)
    val feature = Http(getReq > as.String)
    val text = Await.result(feature, timeout)
    val data = (Json.parse(text) \ "data").asOpt[ServiceModels.Desc]
    data
  }

  def addHeaderAndCookie(urlString: String) = {
    var result = url(urlString)
    result = result.addCookie(new Cookie("skmtutc", "anXjmKK6IqOWzqWlwKU2MYHkBKBCvAVjRPLo+XhY3cGN5AblSMZAI/qD8mcxKB0P3mf/Xw7XvfCVRNcjv4/xhA==-n1AR+pA8p31RiXQggI0gPqZfVnE=",
      "anXjmKK6IqOWzqWlwKU2MYHkBKBCvAVjRPLo+XhY3cGN5AblSMZAI/qD8mcxKB0P3mf/Xw7XvfCVRNcjv4/xhA==-n1AR+pA8p31RiXQggI0gPqZfVnE=", "oct.sankuai.com", "/", -1, 2000, false, true))
      .addCookie(new Cookie("SID", "570th7p1bt26ksolu59n401164", "570th7p1bt26ksolu59n401164", "oct.sankuai.com", "/", -1, 2000, false, true))
      .addCookie(new Cookie("misId", "hanjiancheng", "hanjiancheng", "oct.sankuai.com", "/", -1, 2000, false, true))
      .addCookie(new Cookie("misId.sig", "R6t6zhbvKEhWCv0U1TVz6SuDRQk", "R6t6zhbvKEhWCv0U1TVz6SuDRQk", "oct.sankuai.com", "/", -1, 2000, false, true))
      .addCookie(new Cookie("userId", "64137", "64137", "oct.sankuai.com", "/", -1, 2000, false, true))
      .addCookie(new Cookie("userId.sig", "rTbO-Kqsm5A1-tTXf40dHyv7PiY", "rTbO-Kqsm5A1-tTXf40dHyv7PiY", "oct.sankuai.com", "/", -1, 2000, false, true))
      .addCookie(new Cookie("userName", "%E9%9F%A9%E5%BB%BA%E6%88%90", "%E9%9F%A9%E5%BB%BA%E6%88%90", "oct.sankuai.com", "/", -1, 2000, false, true))
      .addCookie(new Cookie("userName.sig", "oDdXTbNSdFWkL8A51ojyIOMsgmM", "oDdXTbNSdFWkL8A51ojyIOMsgmM", "oct.sankuai.com", "/", -1, 2000, false, true))
      .addCookie(new Cookie("__mta", "149770889.1445523886869.1445523886869.1452239177404.2", "149770889.1445523886869.1445523886869.1452239177404.2", "oct.sankuai.com", "/", -1, 2000, false, true))
      .addCookie(new Cookie("_ga", "GA1.2.1016736437.1445859649", "GA1.2.1016736437.1445859649", "oct.sankuai.com", "/", -1, 2000, false, true))
      .addCookie(new Cookie("ssoid", "278b5d34d2*34e2bb214c724e8bee736", "278b5d34d2*34e2bb214c724e8bee736", "oct.sankuai.com", "/", -1, 2000, false, true))
      .addCookie(new Cookie("JSESSIONID", "mvp5nhxm38ou697tuf1ib734", "mvp5nhxm38ou697tuf1ib734", "oct.sankuai.com", "/", -1, 2000, false, true))
    result
  }
}
