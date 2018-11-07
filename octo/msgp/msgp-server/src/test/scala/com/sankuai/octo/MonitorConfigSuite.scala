package com.sankuai.octo

import com.ning.http.client.cookie.Cookie
import com.sankuai.msgp.common.utils.helper.JsonHelper
import com.sankuai.octo.msgp.dao.monitor.ProviderTriggerDao
import com.sankuai.octo.msgp.domain.UserSubscribeMonitor
import com.sankuai.msgp.common.model.MonitorModels.AppkeyTriggerWithSubStatus
import com.sankuai.msgp.common.model.ServiceModels.{DescRich, User}
import dispatch.{Http, as, url}
import org.scalatest.{BeforeAndAfter, FunSuite}
import play.api.libs.json.{JsArray, Json}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, duration}


/**
 * Created by zava on 16/5/28.
 */
class MonitorConfigSuite extends FunSuite with BeforeAndAfter {

  case class PerfDayRow(id: Long, ts: Int = 0, mode: String = "", tags: String = "", appkey: String = "",
                        spanname: String = "", localhost: String = "", remoteApp: String = "", remoteHost: String = "",
                        status: Int = 0, count: Long = 0L, qps: Option[scala.math.BigDecimal] = None,
                        upper50: Option[scala.math.BigDecimal] = None, upper90: Option[scala.math.BigDecimal] = None,
                        upper95: Option[scala.math.BigDecimal] = None, upper99: Option[scala.math.BigDecimal] = None,
                        upper: Option[scala.math.BigDecimal] = None)

  implicit val perfDayRowReads = Json.reads[PerfDayRow]
  implicit val perfDayRowWrites = Json.writes[PerfDayRow]

  val hostname = "octo.st.sankuai.com"
  //  val hostname = "localhost:8080"
  implicit val timeout = Duration.create(30, duration.SECONDS)


  test("getProviderTrigger") {
    val appkey = "com.sankuai.inf.msgp"
    val triger = ProviderTriggerDao.getTrigger(appkey, "minute_change")
    println(triger)
  }

  test("appkeyDesc") {
    val desc = getDesc("com.sankuai.inf.logCollector")
    println(desc)
  }

  test("getTriggers") {
    val triggers = getTriggers("com.sankuai.inf.logCollector")
    println(triggers)
  }

  test("triggers") {
    //拿到有请求量的appkey,添加 monitor
    val appkeys = getAppkey();
    println(appkeys)
    val compare_json = List(
      "{\"side\":\"server\",\"spanname\":\"all\",\"item\":\"compare.$appkey.$sideCount.upper_90.day\",\"itemDesc\":\"90%耗时(分钟粒度)环比\",\"function\":\">\",\"functionDesc\":\"大于\",\"threshold\":50}",
      "{\"side\":\"server\",\"spanname\":\"all\",\"item\":\"compare.$appkey.$sideCount.upper_90.week\",\"itemDesc\":\"90%耗时(分钟粒度)同比\",\"function\":\">\",\"functionDesc\":\"大于\",\"threshold\":50}")
    appkeys.par.foreach {
      appkey =>
        compare_json.map {
          json =>
            addMonitor(appkey, json)
            //获取相关服务的RD,
            try {
              val descRich = get(appkey)
              //相关服务的Monitortrigger
              val triggers = getTriggers(appkey)
              triggers.foreach {
                trigger =>
                                  val users = List(User(28930, "wujinwu", "吴进武"), User(41081, "wangyanzhao", "王燕昭"),
                                    User(7655, "zhangxi", "张熙"), User(64137, "hanjiancheng", "韩建成"))

//                  val users = List(User(64137, "hanjiancheng", "韩建成"))
                  users.distinct.foreach {
                    user =>
                      val userSubscribeMonitor = new UserSubscribeMonitor(appkey, trigger.id, "xm", 0, user.id, user.login, user.name)
                      //给相关RD绑定注册服务
                      val monitorData = JsonHelper.jsonStr(userSubscribeMonitor)
                      addMonitorTrigger(appkey, monitorData)
                  }

              }
            }
            catch {
              case e: Exception =>
                println(s"appkey $appkey error,$e")
            }
        }
    }
  }

  test("addtriggers2") {
    //拿到有请求量的appkey,添加 monitor
    val appkeys = getAppkey();
    println(appkeys)
    appkeys.foreach {
      appkey =>
            //            addMonitor(appkey, json)
            //获取相关服务的RD,
            try {
              val descRich = get(appkey)
              //相关服务的Monitortrigger
              val triggers = getTriggers(appkey).filter(_.item.contains("upper_50"))
              triggers.par.foreach {
                trigger =>
                  val users = descRich.owners ::: descRich.observers.getOrElse(List())

                  users.distinct.foreach {
                    user =>
                      val userSubscribeMonitor = new UserSubscribeMonitor(appkey, trigger.id, "xm", 0, user.id, user.login, user.name)
                      //给相关RD绑定注册服务
                      val monitorData = JsonHelper.jsonStr(userSubscribeMonitor)
                      addMonitorTrigger(appkey, monitorData)
                  }

              }
            }
            catch {
              case e: Exception =>
                println(s"appkey $appkey error,$e")
            }





    }
  }

  test("addTriggers") {
    //拿到有请求量的appkey,添加 monitor
    val appkeys = getAppkey();
    println(appkeys)
    val compare_json = List(
      "{\"side\":\"server\",\"spanname\":\"all\",\"item\":\"compare.counters.$appkey.$sideCount.day\",\"itemDesc\":\"QPS(分钟粒度)环比\",\"function\":\">\",\"functionDesc\":\"大于\",\"threshold\":20}",
      "{\"side\":\"server\",\"spanname\":\"all\",\"item\":\"compare.counters.$appkey.$sideCount.week\",\"itemDesc\":\"QPS(分钟粒度)同比\",\"function\":\">\",\"functionDesc\":\"大于\",\"threshold\":20}")
    appkeys.foreach {
      appkey =>
        compare_json.map {
          json =>
            //            addMonitor(appkey,json)
            //获取相关服务的RD,
            val descRich = get(appkey)
            //相关服务的Monitortrigger
            val triggers = getTriggers(appkey)
            triggers.foreach {
              trigger =>
                val users = descRich.owners ::: descRich.observers.getOrElse(List())

                users.distinct.foreach {
                  user =>
                    val userSubscribeMonitor = new UserSubscribeMonitor(appkey, trigger.id, "xm", 0, user.id, user.login, user.name)
                    //给相关RD绑定注册服务
                    val monitorData = JsonHelper.jsonStr(userSubscribeMonitor)
                    addMonitorTrigger(appkey, monitorData)
                }

            }

        }
    }
  }

  def get(appkey: String) = {
    val url = s"http://$hostname/service/$appkey/desc"
    val getReq = addHeaderAndCookie(url)
    val feature = Http(getReq > as.String)
    val content = Await.result(feature, timeout)
    val descRich = (Json.parse(content) \ "data").asOpt[DescRich]
    descRich.get
  }

  def getTriggers(appkey: String) = {
    val url = s"http://$hostname/monitor/$appkey/triggers"
    val getReq = addHeaderAndCookie(url)
    val feature = Http(getReq > as.String)
    val content = Await.result(feature, timeout)
    val triggerArray = (Json.parse(content) \ "data").asInstanceOf[JsArray].value.toSeq
    val triggers = triggerArray.flatMap {
      triggerOpt =>
        (triggerOpt).asOpt[AppkeyTriggerWithSubStatus]
    }
    triggers.filter(_.item.startsWith("compare")).toList
  }


  def getDesc(appkey: String) = {
    val url = s"http://$hostname/service/$appkey/desc"
    val getReq = addHeaderAndCookie(url)
    val feature = Http(getReq > as.String)
    val content = Await.result(feature, timeout)
    val descRich = (Json.parse(content) \ "data").asOpt[DescRich]
    descRich
  }


  def getAppkey(): List[String] = {
    val url = s"http://$hostname/data/api/daily?day=2016-05-28"
    val getReq = addHeaderAndCookie(url)
    val feature = Http(getReq > as.String)
    val content = Await.result(feature, timeout)
    val appkeyDescs = (Json.parse(content) \ "data").asInstanceOf[JsArray].value.toSeq
    val appkeys = appkeyDescs.flatMap {
      appkeyDesc =>
        (appkeyDesc).asOpt[PerfDayRow]
    }
    appkeys.filter(_.count > 0).map(_.appkey).toList
  }

  def addMonitor(appkey: String, json: String): Unit = {
    val url = s"http://$hostname/monitor/$appkey/trigger"
    val postReq = addHeaderAndCookie(url).POST << json
    val feature = Http(postReq > as.String)
    val content = Await.result(feature, timeout)
    println(content)
  }

  def addMonitorTrigger(appkey: String, json: String): Unit = {
    println(s"${appkey},${json}")
    val url = s"http://$hostname/monitor/$appkey/trigger/subscribe2"
    val postReq = addHeaderAndCookie(url).setHeader("Content-Type", "application/json;charset=utf-8").setBody(json).POST
    val feature = Http(postReq OK as.String)
    val content = Await.result(feature, timeout)
    println(content)
  }


  private def addHeaderAndCookie(urlString: String) = {
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
      .addCookie(new Cookie("ssoid", "d722c5a90bfa401f8aeb5b7c1ec115fc", "d722c5a90bfa401f8aeb5b7c1ec115fc", "oct.sankuai.com", "/", -1, 2000, false, true))
      .addCookie(new Cookie("JSESSIONID", "15hytutkzkqyswfrme8hmssj8", "15hytutkzkqyswfrme8hmssj8", "oct.sankuai.com", "/", -1, 2000, false, true))
    result
  }
}
