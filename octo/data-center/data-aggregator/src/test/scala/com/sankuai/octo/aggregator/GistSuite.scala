package com.sankuai.octo.aggregator

import com.ning.http.client.ProxyServer
import dispatch.url
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{BeforeAndAfter, FunSuite}

@RunWith(classOf[JUnitRunner])
class GistSuite extends FunSuite with BeforeAndAfter {

  test("ip") {
    val t1 = System.currentTimeMillis()
    (1 to 100000).foreach {
      x =>
        match1("10.64.1.1")
        match1("11.64.1.1")
        match1("192.168.1.2")
        match1("122.168.1.2")
        match1("inf-mns01")
    }
    val t2 = System.currentTimeMillis()
    println(t2 - t1)
    println(match1(null))
    println(match1("10.64.1.1"))
    println(match1("11.64.1.1"))
    println(match1("192.168.1.2"))
    println(match1("122.168.1.2"))
    println(match1("127.0.0.1"))
    println(match1("inf-mns01"))
  }

  def match1(ip: String) = {
    val p = """^([0-9]{1,3}[\.]){3}[0-9]{1,3}$""".r
    ip match {
      case null => "unknownHost"
      case `ip` if ip.startsWith("10.64.") => ip
      case `ip` if ip.startsWith("192.168.") => ip
      case `ip` if ip.startsWith("127.0.0.1") => ip
      case p(n) => "external"
      case _: String => ip
    }
  }

  test("clear") {
    val key = 399136
    val hosts = List("10.32.38.120", "10.32.38.119", "10.32.57.138", "10.32.26.246", "10.64.33.248", "10.64.40.223", "10.64.29.155", "10.64.33.134")
    //val hosts = List("10.32.38.120")
    val proxy = new ProxyServer("10.64.35.229", 80)
    hosts.flatMap {
      host =>
        val request = s"http://$host:8930/clear?hour=$key"
        http.execute(url(request).setProxyServer(proxy))
    }
  }



//  test("async") {
//    val count = 1000
//    val tcount = 10
//    val sum = new AtomicLong()
//    val latch = new CountDownLatch(count * tcount)
//    def f(x: Int) {
//      Thread.sleep(1)
//      sum.addAndGet(x)
//      latch.countDown()
//    }
//    val asyncTester = AsyncProcessor(f)
//    val start = System.currentTimeMillis()
//    (1 to tcount).foreach {
//      t =>
//        new Thread() {
//          override def run() {
//            (1 to count).foreach {
//              x =>
//                asyncTester.put(x)
//            }
//          }
//        }.start()
//    }
//    val end1 = System.currentTimeMillis()
//    latch.await()
//    val end2 = System.currentTimeMillis()
//    println(s"$sum ${end1 - start} ${end2 - start}")
//  }
}
