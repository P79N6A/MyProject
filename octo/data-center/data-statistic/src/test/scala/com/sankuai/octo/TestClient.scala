package com.sankuai.octo

//import com.meituan.mtrace.{Tracer, Span, TraceParam}

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{BeforeAndAfter, FunSuite}
import org.slf4j.LoggerFactory
import org.springframework.test.context.ContextConfiguration

@RunWith(classOf[JUnitRunner])
@ContextConfiguration(locations = Array("classpath*:statistic-service.xml"))
class TestClient extends FunSuite with BeforeAndAfter {
  private val logger = LoggerFactory.getLogger(classOf[TestClient])

  test("test init2") {
    logger.info("come here!!!")
  }

  test("uploadCommonLog") {
    (1 to 10).foreach { _ =>
      //      upload(Tracer.STATUS.DROP)
    }
    //
    //    (1 to 500).foreach { _ =>
    //      upload(Tracer.STATUS.EXCEPTION)
    //    }
    //
    //    (1 to 500).foreach { _ =>
    //      upload(Tracer.STATUS.SUCCESS)
    //    }
  }

  //  def upload(status: Tracer.STATUS): Unit = {
  //    val spanName: String = "ClassName.methodName"
  //    val localAppKey: String = "com.sankuai.inf.availability"
  //    val localIp: String = "127.0.0.1"
  //    val localPort: Int = 20
  //    val remoteAppKey: String = "xxx"
  //    val remoteIp: String = "127.0.0.2"
  //    val remotePort: Int = 80
  //    val infraName: String = "mtthrift"
  //    val version: String = "1.5.8"
  //    val size: Int = 1024
  //
  //    val param: TraceParam = new TraceParam(spanName)
  //    param.setLocal(localAppKey, localIp, localPort)
  //    param.setRemote(remoteAppKey, remoteIp, remotePort)
  //    param.setInfraName(infraName)
  //    param.setVersion(version)
  //    param.setPackageSize(size)
  //    val span: Span = Tracer.serverRecv(param)
  //    Tracer.serverSend(status)
  //  }
  //
  //  test("availability") {
  //    val LOCAL_APPKEY = "com.sankuai.inf.availability"
  //    val REMOTE_APPKEY = "com.sankuai.inf.data.statistic"
  //    val ip = "172.30.26.212"
  //    val PORT = 8940
  //
  //    val proxy: ThriftClientProxy = new ThriftClientProxy
  //    proxy.setAppKey(LOCAL_APPKEY)
  //    proxy.setRemoteAppkey(REMOTE_APPKEY)
  //    proxy.setServiceInterface(classOf[LogStatisticService])
  //    proxy.setTimeout(1000)
  //    proxy.setServerIpPorts(s"$ip:$PORT")
  //    proxy.afterPropertiesSet()
  //    val client: LogStatisticService = proxy.getObject.asInstanceOf[LogStatisticService]
  //
  //    val now = System.currentTimeMillis()
  //    val list = (1 to 1000).map {
  //      _ => new MetricData(now, 198, StatusCode.DROP)
  //    }.toList
  //    val metric = new Metric(new MetricKey("com.sankuai.inf.xxxx", "spanname333XXX", "172.30.26.212",
  //      "remoteAppKey", "172.30.26.212", StatSource.Server, PerfProtocolType.THRIFT), list)
  //    (1 to 500).foreach { _ =>
  //      try {
  //        client.sendMetrics(List(metric))
  //      } catch {
  //        case e: Exception => logger.error(s"exception $e")
  //      }
  //      Thread.sleep(1)
  //    }
  //    while (true) {
  //      Thread.sleep(10000)
  //    }
  //  }
}
