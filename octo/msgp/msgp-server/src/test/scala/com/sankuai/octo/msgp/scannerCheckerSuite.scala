package com.sankuai.octo.msgp

import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.{CountDownLatch, Executors, TimeUnit}

import com.sankuai.octo.msgp.serivce.manage.ScannerChecker
import dispatch.Defaults._
import dispatch._
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{BeforeAndAfter, FunSuite}

import scala.concurrent.Await
import scala.concurrent.duration.Duration


/**
 * Created by zava on 16/8/2.
 */

@RunWith(classOf[JUnitRunner])
class scannerCheckerSuite extends FunSuite with BeforeAndAfter {

  val threadPool = Executors.newFixedThreadPool(1000)
  private implicit val timeout = Duration.create(10, TimeUnit.SECONDS)

  test("report") {
//    scannerChecker.report( """{"appkey":"com.sankuai.info.msgp","category":"UpdateStatus","level":0,"time":1433395169267,"content":"testwyz","identifier":"env:3|appkey:com.sankuai.inf.msgp|ip:192.168.0.1|port:8999","roundTime":50,"providerCount":121,"weight":10,"newWeight":30 ,"status":2,"newStatus":0}""")
//    scannerChecker.report( """{"appkey":"com.sankuai.info.msgp","category":"UpdateStatus","level":0,"time":1433395169267,"content":"testwyz","identifier":"env:3|appkey:com.sankuai.inf.msgp|ip:192.168.0.1|port:8999","roundTime":50,"providerCount":121,"weight":10,"newWeight":30 ,"status":0,"newStatus":2}""")
//    scannerChecker.report( """{"appkey":"com.sankuai.info.msgp","category":"UpdateStatus","level":0,"time":1433395169267,"content":"testwyz","identifier":"env:3|appkey:com.sankuai.inf.msgp|ip:192.168.0.1|port:8999","roundTime":50,"providerCount":121,"weight":10,"newWeight":30 ,"status":2,"newStatus":0}""")
//    scannerChecker.report( """{"appkey":"com.sankuai.info.msgp","category":"UpdateStatus","level":0,"time":1433395169267,"content":"testwyz","identifier":"env:3|appkey:com.sankuai.inf.msgp|ip:192.168.0.1|port:8999","roundTime":50,"providerCount":121,"weight":10,"newWeight":30 ,"status":2,"newStatus":0}""")
    ScannerChecker.report("""{"appkey":"com.sankuai.octo.scanner","category":"DuplicateRegistry","level":0,"time":1433395169267,"content":"[env:3|appkey:com.sankuai.inf.sg_agent|ip:10.5.242.86|port:5266, env:1|appkey:com.sankuai.inf.sg_agent|ip:10.5.242.86|port:5266]","identifier":"ip:10.5.242.86|port:5266","roundTime":50,"providerCount":121,"weight":10,"newWeight":30 ,"status":2,"newStatus":0}""")
    Thread.sleep(1000000)
  }
  private val counter = new AtomicInteger(0)
  test("bentchmarker") {
    val count = 4000
    val begin = new CountDownLatch(1)
    //对于整个比赛，所有运动员结束后才算结束
    val end = new CountDownLatch(count)
    (1 to count).foreach {
      x =>
        threadPool.execute(new Player(x, begin, end))
    }
    begin.countDown()
    end.await()
    threadPool.shutdown()
    println("ok")
    println(s"error:${counter.get()}")
  }

  class Player(id: Int, begin: CountDownLatch, end: CountDownLatch) extends Runnable {

    def run {
//      val urlString = s"http://octo.st.sankuai.com//api/scanner/report"
      val urlString = s"http://localhost:8910/api/scanner/report"
      val json = """{"appkey":"ttt-testt-web","category":"UpdateStatus","level":0,"time":1433395169267,"content":"testwyz","identifier":"env:3|appkey:com.sankuai.inf.msgp|ip:192.168.0.1|port:8999","roundTime":50,"providerCount":121,"weight":10,"newWeight":30 ,"status":2,"newStatus":0}"""
      try {
        begin.await
        val postReq = url(urlString)
//          .setProxyServer(new ProxyServer("10.32.140.181",80))
          .POST << json
        val feature = Http(postReq > as.String)
        val content = Await.result(feature, timeout)
        if (!content.contains("\"data\":\"ok\"")) {
          counter.incrementAndGet()
//          println(s"${System.nanoTime()},$id,$content")
        }
      }
      catch {
        case e: InterruptedException => {
          e.printStackTrace
        }
      } finally {
        end.countDown
      }
    }
  }

}