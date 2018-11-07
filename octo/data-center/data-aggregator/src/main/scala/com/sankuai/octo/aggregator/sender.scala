package com.sankuai.octo.aggregator

import java.util.concurrent.{Executors, LinkedBlockingQueue, TimeUnit}

import com.meituan.service.mobile.mtthrift.proxy.ThriftClientProxy
import com.sankuai.octo.parser.{Metric, Parser}
import com.sankuai.octo.statistic.util.{ExecutorFactory, StatThreadFactory}
import org.slf4j.{Logger, LoggerFactory}

object sender {
  val queue = new LinkedBlockingQueue[Metric](1000000)
  private val LOG: Logger = LoggerFactory.getLogger(sender.getClass)
  private val executor = Executors.newSingleThreadExecutor(StatThreadFactory.threadFactory(this.getClass))

  val asyncSender = new ExecutorFactory(sendMetrics, "sender.asyncSender", 4, 16, 20000)

  executor.submit(new Runnable {
    def run(): Unit = {
      while (!executor.isShutdown || !queue.isEmpty) {
        if (queue.isEmpty) {
          Thread.sleep(1)
        } else {
          try {
            val metricList = new java.util.ArrayList[Metric]()
            val start = System.currentTimeMillis()
            var continue = true
            while (continue) {
              val metric = queue.poll()
              if (metric != null) {
                metricList.add(metric)
              } else {
                Thread.sleep(1)
              }
              val end = System.currentTimeMillis()
              continue = metricList.size() < 200 && (end - start) < 100
            }
            if (!metricList.isEmpty) {
              LOG.debug(s"begin send $start ${metricList.size}")
              asyncSender.submit(metricList)
            }
          } catch {
            case e: Throwable => LOG.error("parser sender exception", e)
          }
          LOG.debug(s"finish parser with queue size ${queue.size()}")
        }
      }
    }
  })

  Runtime.getRuntime.addShutdownHook(new Thread() {
    override def run() {
      LOG.info(s"shutdown parser with queue size ${queue.size()}")
      executor.shutdown()
      executor.awaitTermination(10, TimeUnit.SECONDS)
    }
  })

  val client: Parser.Iface = {
    val proxy = new ThriftClientProxy
    proxy.setServiceInterface(classOf[Parser])
    proxy.setAppKey("com.sankuai.inf.logCollector")
    proxy.setRemoteAppkey("com.sankuai.fe.mta.parser")
    //proxy.setServerIpPorts("172.30.8.162:8890")
    //proxy.setServerIpPorts("192.168.60.244:8890")
    proxy.setClusterManager("octo")
    proxy.setTimeout(5000)
    proxy.afterPropertiesSet()
    val a = proxy.getObject
    a.asInstanceOf[Parser.Iface]
  }

  def sendMetrics(metrics: java.util.List[Metric]): Unit = {
    try {
      if (metrics != null && !metrics.isEmpty) {
        client.sendMetrics(metrics)
      }
    } catch {
      case e: Exception => LOG.error(s"send metric exception,data: $metrics", e)
    }
  }

  private def putQueue(metric: Metric) = {
    if (!executor.isShutdown) {
      queue.offer(metric)
    } else {
      LOG.error(s"parser already shutdown, drop metric $metric")
    }
  }

  def asyncSend(metric: Metric) = {
    putQueue(metric)
  }

  def asyncSendList(metrics: List[Metric]) = {
    metrics.foreach(putQueue)
  }

}
