package com.sankuai.octo.aggregator

import java.util.{Timer, TimerTask}

import com.meituan.mtrace.thrift.model.{Endpoint, StatusCode, ThriftSpan, ThriftSpanList}
import com.meituan.service.mobile.mtthrift.proxy.ThriftClientProxy
import com.sankuai.octo.aggregator.thrift.model.{CommonLog, SGLog}
import com.sankuai.octo.aggregator.thrift.service.LogCollectorService
import com.sankuai.octo.aggregator.util.Convert
import com.sankuai.octo.statistic.helper.Serializer
import com.sankuai.octo.statistic.model.PerfProtocolType
import org.joda.time.DateTime
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{BeforeAndAfter, FunSuite}

import scala.collection.JavaConversions._

@RunWith(classOf[JUnitRunner])
class ServiceSuite extends FunSuite with BeforeAndAfter {

  test("call") {
    val appkey = "com.sankuai.inf.msgp"
    val level = 1
    val client = init()
    val timer = new Timer(true)
    class Task extends TimerTask {
      def run() {
        println("start")
        val content = "ip:192.168.0.1|msg: sg_agent ERR|wyztest"
        val time = new DateTime().getMillis
        val log = new SGLog(appkey, time, level, content)
        (1 to 10).foreach {
          x =>
            try {
              val ret = client.uploadLog(log)
              println(ret)
            } catch {
              case e: Exception => println(e)
            }
        }
      }
    }
    timer.schedule(new Task(), 0, 30000)
    while (true) {
    }
  }

  def init() = {
    val proxy = new ThriftClientProxy
    proxy.setServiceInterface(classOf[LogCollectorService])
    proxy.setServerIpPorts("172.30.15.138:8920")
    //    proxy.setAppKey("com.sankuai.inf.msgp")
    //    proxy.setRemoteAppkey("com.sankuai.inf.sgnotify")
    //    proxy.setClusterManager("octo")
    proxy.setTimeout(3000)
    //proxy.setImplFacebookService(false)
    //proxy.setStrAgentUrl("192.168.12.176:5266")
    proxy.afterPropertiesSet()
    val a = proxy.getObject
    a.asInstanceOf[LogCollectorService.Iface]
  }

  test("uploadCommonLog") {
    val spanName: String = "3cjg.methodName"
    val localAppKey: String = "com.sankuai.inf.availability"
    val localIp: String = "172.30.26.212"
    val localPort: Int = 20
    val remoteAppKey: String = "xxx"
    val remoteIp: String = "172.30.26.213"
    val remotePort: Int = 80
    val infraName: String = "mtthrift"
    val version: String = "1.5.8"

    val local = new Endpoint(Convert.ipToInt(localIp), localPort.toShort, localAppKey)
    val remote = new Endpoint(Convert.ipToInt(remoteIp), remotePort.toShort, remoteAppKey)
    val now = System.currentTimeMillis()

    val list = (1 to 100000).map { _ =>
      val thriftSpan = new ThriftSpan
      thriftSpan.setTraceId(987654321)
      thriftSpan.setSpanId("sdsds")
      thriftSpan.setSpanName(spanName)
      thriftSpan.setLocal(local)
      thriftSpan.setRemote(remote)
      thriftSpan.setStart(now)
      thriftSpan.setDuration(99)
      // 确认type字段含义
      thriftSpan.setType(PerfProtocolType.THRIFT.toString)
      // 0是否是success
      thriftSpan.setStatus(StatusCode.SUCCESS)

      thriftSpan.setClientSide(false) //server
      thriftSpan
    }.toList

    val a = new ThriftSpanList()
    a.setVar1(23)
    a.setVar2("wangyanzhao")
    a.setSpans(list)

    val by = Serializer.toBytes(a)

    val commonlog = new CommonLog()
    commonlog.setCmd(6)
    commonlog.setContent(by)

    val LOCAL_APPKEY = "com.sankuai.inf.availabilityTest"
    val REMOTE_APPKEY = "com.sankuai.inf.logCollector"
    val ip = "10.4.232.94"
    val PORT = 8920

    val proxy: ThriftClientProxy = new ThriftClientProxy
    proxy.setAppKey(LOCAL_APPKEY)
    proxy.setRemoteAppkey(REMOTE_APPKEY)
    proxy.setServiceInterface(classOf[LogCollectorService])
    proxy.setTimeout(10000)
    proxy.setServerIpPorts(s"$ip:$PORT")
    proxy.afterPropertiesSet()
    val client = proxy.getObject.asInstanceOf[LogCollectorService.Iface]

    while (true) {
      client.uploadCommonLog(commonlog)
      Thread.sleep(1)
    }
  }
}
