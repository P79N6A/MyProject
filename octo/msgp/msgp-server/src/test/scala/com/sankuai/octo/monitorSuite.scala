package com.sankuai.octo

import java.util.{Timer, TimerTask}

import com.sankuai.msgp.common.config.DbConnection
import com.sankuai.msgp.common.config.db.msgp.Tables.{AppkeyAuth2Row, AppkeyTriggerRow, TriggerSubscribeRow, _}
import com.sankuai.msgp.common.service.org.OrgSerivce
import com.sankuai.octo.msgp.dao.monitor.MonitorTriggerDAO
import com.sankuai.msgp.common.model.MonitorModels.{Trigger, _}
import com.sankuai.octo.msgp.model.SubStatus
import com.sankuai.octo.msgp.serivce.monitor.MonitorTrigger.MonitorData
import com.sankuai.octo.msgp.serivce.monitor.{MonitorConfig, MonitorTrigger, MonitorTriggerLeader}
import org.scalatest.{BeforeAndAfter, FunSuite}
import play.api.libs.json.Json

import scala.slick.driver.MySQLDriver.simple._

class monitorSuite extends FunSuite with BeforeAndAfter {
  val appkey = "com.sankuai.inf.msgp"
  val trigger = AppkeyTriggerRow(1, appkey, "server", "all", "minute.timers.$appkey.$sideCost.upper_50", "50%耗时(分钟粒度)", ">", "大于", 34, 1)
  val item = "minute.timers.$appkey.$sideCost.upper_50"
  test("notify") {
    val timer = new Timer(true)
    class Task extends TimerTask {
      def run() {
        println("start")
        MonitorTrigger.doTrigger(appkey, trigger, "5151")
      }
    }
    timer.schedule(new Task(), 0, 60000)
    while (true) {
    }
  }

  test("validate.get") {
    val json = "{\"side\":\"server\",\"spanname\":\"all\",\"item\":\"minute.timers.$appkey.$sideCost.upper_50\",\"itemDesc\":\"50%耗时(分钟粒度)\",\"function\":\">\",\"functionDesc\":\"大于\",\"threshold\":1234567}"
    println(json)
    val trigger = Json.parse(json).validate[Trigger].get
    println(trigger)
  }



  test("compare") {
    val triggers = List(AppkeyTriggerRow(2212, appkey, "server", "OpenPricingFailoverThriftIface.getFailoverPricingData", "minute.timers.$appkey.$sideCost.upper_99",
      "90%耗时(分钟粒度)", ">", "大于", 500, 0))
    //    val perfData = MonitorTrigger.getService("com.sankuai.mobile.group.sinai.spec", trigger, 1465307520)
    //    val cperfData = MonitorTrigger.getService("com.sankuai.mobile.group.sinai.spec", trigger, 1464789120)
    //    val c2perfData = MonitorTrigger.getService("com.sankuai.mobile.group.sinai.spec", trigger, 1465221120)
    //    println(MonitorTrigger.compareTPValue(perfData, cperfData))
    //    println(MonitorTrigger.compareQPSValue(perfData, cperfData))
    //
    //    println(MonitorTrigger.compareTPValue(perfData, c2perfData))
    //    println(MonitorTrigger.compareQPSValue(perfData, c2perfData))
    val createTime = System.currentTimeMillis() / 1000L / 60L
    MonitorTrigger.doMonitorAction(triggers, createTime)

  }

  test("checkItem") {
    val trigger = AppkeyTriggerRow(2212, "com.sankuai.inf.msgp", "server", "all", "today.timers.$appkey.$sideCost.upper_99", "99%耗时(今日)", ">", "大于", 200, 5)
    val createTime = System.currentTimeMillis() / 1000L / 60L
    MonitorTrigger.doMonitorAction(List(trigger), createTime)
  }
  test("checkItem1") {
    val appkey = "com.sankuai.lvyou.hermes"
    val trigger = AppkeyTriggerRow(2212, "com.sankuai.lvyou.hermes", "server", "TradeOrderController.mobileOrderCreateV1", "compare.counters.$appkey.$sideCount.day", "QPS(分钟粒度)环比", "<", "小于", 50, 1)
    //    MonitorTrigger.checkItem(appkey,trigger)
  }

  test("checkItem2") {
    val appkey = "com.sankuai.meilv.volga"
    val trigger = AppkeyTriggerRow(2212, "com.sankuai.meilv.volga", "server", "HotelCrossRecommendController.hotelDetailRecommend", "compare.counters.$appkey.$sideCount.day", "QPS(分钟粒度)环比", "<", "小于", 50, 1)
    //    MonitorTrigger.checkItem(appkey,trigger)
  }

  /**
    * 2016-08-18 16:21:00
    */
  test("checkItem3") {
    val appkey = "com.sankuai.meishi.merchant.api"
    val trigger = AppkeyTriggerRow(2212, "com.sankuai.meishi.merchant.api", "server", "HomepageController.poiList", "compare.counters.$appkey.$sideCount.week", "QPS(分钟粒度)环比", ">", "大于", 100, 1)
    //    MonitorTrigger.checkItem(appkey,trigger)
  }

  /**
    * 2016-08-24 10:22:00
    */
  test("checkItem4") {
    val appkey = "com.sankuai.lvyou.hermes"
    val trigger = AppkeyTriggerRow(2212, "com.sankuai.lvyou.hermes", "server", "all", "compare.counters.$appkey.$sideCount.week", "QPS(分钟粒度)同比", "<", "小于", 75, 1)
    //    MonitorTrigger.checkItem(appkey,trigger)
    val trigger2 = AppkeyTriggerRow(2212, "com.sankuai.lvyou.hermes", "server", "all", "compare.counters.$appkey.$sideCount.day", "QPS(分钟粒度)环比", "<", "小于", 75, 1)
    //    MonitorTrigger.checkItem(appkey,trigger2)
  }


  test("checkItem5") {
    val appkey = "com.sankuai.waimai.order.datamanager"
    //    val trigger = AppkeyTriggerRow(2212,"com.sankuai.waimai.order.datamanager","server","all","compare.counters.$appkey.$sideCount.week","QPS(分钟粒度)同比","<","小于",75,1)
    //    MonitorTrigger.checkItem(appkey,trigger)
    val trigger2 = AppkeyTriggerRow(7656, "com.sankuai.waimai.order.datamanager", "server", "WmQueryOrderPayInfoThriftService.findLastModifyWmOrderRefundInfo", "compare.counters.$appkey.$sideCount.day", "QPS(分钟粒度)环比", "<", "小于", 10000, 1)
    //    MonitorTrigger.checkItem(appkey,trigger2)
  }

  test("checkItem6") {
    val appkey = "com.sankuai.inf.mnsc"
    val trigger2 = AppkeyTriggerRow(7656, "com.sankuai.inf.msgp", "server", "all", "minute.counters.$appkey.$sideCount", "QPS(分钟粒度)", ">", "大于", 10, 1)
    MonitorTrigger.doMonitorAction(List(trigger2), 1111)
  }

  test("testClient") {
    val appkey = "banma_service_callback_server"
    val trigger2 = AppkeyTriggerRow(2212, appkey, "client", "WmEDispatchOrderThriftService.autoCancelZbLogistics", "minute.timers.$appkey.$sideCost.upper_99", "99%耗时(分钟粒度)", ">", "大于", 5, 1)
    //    MonitorTrigger.checkItem(appkey,trigger2)
  }

  test("testTriggerLeader") {
    MonitorTriggerLeader.start()
    Thread.sleep(60000)
  }

  test("testTriggerAction") {
    //val msgp_provider = List("10.20.57.107", "10.20.33.98", "10.32.170.185", "10.4.56.63")

    MonitorTriggerLeader.start()
    MonitorTrigger.start()
    Thread.sleep(60000)
  }
  test("monitorAction") {
    MonitorTrigger.startMonitorAction(24677999L)
  }

  def authlist(): Map[String, Set[Long]] = {
    val db = DbConnection.getPool()
    val appOwners = db withSession {
      implicit session: Session =>
        val authList = AppkeyAuth2.filter(x => x.level === 16).list
        val temp: Map[String, List[AppkeyAuth2Row]] = authList.filter(_.userId != 0L).groupBy(_.appkey)
        val appOwners = temp.flatMap(x => Map(x._1 -> x._2.map(_.userId).toSet)).toMap
        appOwners
    }
    appOwners
  }

  def initSubscribes() {
    val db = DbConnection.getPool()
    val appOwners = authlist()
    val triggers = db withSession {
      implicit session: Session =>
        AppkeyTrigger.list
    }
    db withSession {
      implicit session: Session =>
        triggers.foreach {
          x =>
            appOwners.get(x.appkey).foreach {
              ao =>
                ao.foreach {
                  userId =>
                    val user = OrgSerivce.employee(userId.toInt)
                    TriggerSubscribe += TriggerSubscribeRow(0, x.appkey, x.id, user.getId, user.getLogin, user.getName, SubStatus.Sub.id.toByte, SubStatus.UnSub.id.toByte, SubStatus.UnSub.id.toByte)
                }
            }
        }
    }
  }

  test("getItemDescCount") {
    val a = MonitorConfig.getItemDescCount("com.sankuai.inf.logCollector", "50%耗时(分钟粒度)同比", "server")
    println(a)
  }

  test("doTrigger") {
    MonitorTrigger.doTrigger("com.sankuai.inf.msgp", trigger, "123")
  }

  test("delete trigger status") {
    MonitorTriggerDAO.deleteTriggerStatus(24677870)
  }

  /**
    * 检测当前监控的值是否为异常点
    */
  test("isOutlier") {
    val monitorData = MonitorData(0, 3025, 0, 0, 0, null)
    //只用到了side和spanname这两个
    val trigger = AppkeyTriggerRow(0, "com.sankuai.inf.leaf.service", "server", "all", "", "", "", "", 5, 1)

    //环比是否异常
    val isOutlier1 = MonitorTrigger.isOutlier(monitorData, "QPS", 1498189120 - 86400 - 600, 1498189120 - 86400, trigger)
    //同比是否异常
    val isOutlier2 = MonitorTrigger.isOutlier(monitorData, "QPS", 1498189120 - 604800 - 600, 1498189120 - 604800, trigger)

    println("环比异常检测: " + isOutlier1 + ", 同步异常检测: " + isOutlier2)

  }

  test("get None test") {
    try {
      val a = None
      a.get
    } catch {
      case e: Exception =>
        //expect: java.util.NoSuchElementException: None.get
        println(e)
    }
  }

}
