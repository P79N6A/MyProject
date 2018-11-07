package com.sankuai.octo.mnsc.test

import com.sankuai.octo.mnsc.idl.thrift.service.MNSCacheService
import org.apache.thrift.protocol.{TBinaryProtocol, TProtocol}
import org.apache.thrift.transport.{TFramedTransport, TSocket}
import org.joda.time.DateTime
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{BeforeAndAfter, FunSuite}

import scala.collection.parallel.ForkJoinTaskSupport

@RunWith(classOf[JUnitRunner])
class ServiceSuite extends FunSuite with BeforeAndAfter {

  test("service") {
    val timeout = 3000
    val ip = "127.0.0.1"
    val port = 8091
    val transport = new TFramedTransport(new TSocket(ip, port, timeout), 16384000)
    val protocol: TProtocol = new TBinaryProtocol(transport)
    val mnsc = new MNSCacheService.Client(protocol)
    val appkey = "com.sankuai.cos.mtconfi"
    val env = "prod"
    //    val path = List("/mns/sankuai", env, appkey, Path.provider).mkString("/")
    //    val version = zk.getNodeVersion(path)
    transport.open
    while (true) {
      val start = new DateTime().getMillis
      println(mnsc.getMNSCache(appkey, "0", env))
      val end = new DateTime().getMillis
      println(s"cost ${end - start}")
      Thread.sleep(2000)
    }
  }

  test("par") {
    val a = (0 to 30).toList.par
    a.tasksupport = new ForkJoinTaskSupport(new scala.concurrent.forkjoin.ForkJoinPool(1000))
    val b = (0 to 100000).toList.par
    b.tasksupport = new ForkJoinTaskSupport(new scala.concurrent.forkjoin.ForkJoinPool(1000))
    val result = a.map {
      x =>
        val at = Thread.currentThread().getName
        println(b.map(x => Thread.currentThread().getName).distinct.length)
        at
    }.distinct.length
    println("ss" + result)
  }

  test("httpPropertes") {
    val timeout = 3000
    val ip = "127.0.0.1"
    val port = 8091
    val transport = new TFramedTransport(new TSocket(ip, port, timeout), 16384000)
    val protocol: TProtocol = new TBinaryProtocol(transport)
    val mnsc = new MNSCacheService.Client(protocol)

    val appkey ="com.sankuai.hlb.rt"
    val env = "prod"
    val res = mnsc.getHttpPropertiesByAppkey(appkey, env)

    assert(200 == res.code)
  }
}
