package com.sankuai.octo.statistic

import com.meituan.service.mobile.mtthrift.client.model.ServerConn
import com.meituan.service.mobile.mtthrift.client.route.AbstractLoadBalancer
import com.sankuai.octo.statistic.model.Metric
import org.aopalliance.intercept.MethodInvocation
import org.apache.commons.lang.ArrayUtils
import org.slf4j.LoggerFactory
import org.springframework.util.CollectionUtils

import scala.collection.JavaConversions._
import scala.util.Random

class ConsistentHashLoadBalancer extends AbstractLoadBalancer {
  private val logger = LoggerFactory.getLogger(this.getClass)

  //通过计算的方式获取最合适的value
  private val NumberOfReplicas = 61

  private var consistentHash: ConsistentHash = null
  private var connSet: Set[(String, Int)] = null

  override def doSelect(serverList: java.util.List[ServerConn], methodInvocation: MethodInvocation): ServerConn = {
    //  未启动0：DEAD（默认），1：STARTING, 正常2：ALIVE，3：STOPPING，禁用4：STOPPED，5：WARNING
    val aliveServerList = serverList.filter(_.getServer.getStatus == 2).toList
    if (logger.isDebugEnabled) {
      for (server <- aliveServerList) {
        logger.debug("server:{}", server)
      }
    }
    if (CollectionUtils.isEmpty(aliveServerList)) {
      throw new RuntimeException("aliveServerList empty,ERROR")
    }
    val start = System.currentTimeMillis()
    if (consistentHash == null || !nodesEqual(aliveServerList)) {
      constuctConsistentHashCircle(aliveServerList)
    }
    val methodName = methodInvocation.getMethod.getName
    if (!ArrayUtils.isEmpty(methodInvocation.getArguments)) {
      val arg = methodInvocation.getArguments()(0)
      val param = if ("sendMetrics" == methodName) {
        arg match {
          case metric: Metric =>
            metric.key.appkey
          case metrics: java.util.List[Metric@unchecked] =>
            if (CollectionUtils.isEmpty(metrics)) {
              throw new IllegalArgumentException("sendMetrics argument must not be empty")
            } else {
              val metric = metrics.get(0)
              metric.key.appkey
            }
          case _ =>
            arg.toString
        }
      } else {
        arg.toString
      }
      val serverConn = consistentHash.get(param)
      logger.debug(s"methodName:$methodName param:$param serverConn:${serverConn.getServer} cost:${System.currentTimeMillis() - start} ms")
      serverConn
    } else {
      aliveServerList.get(Random.nextInt(aliveServerList.size))
    }
  }

  private def constuctConsistentHashCircle(serverList: java.util.List[ServerConn]): Unit = {
    /** 初始化一致性hash环 */
    consistentHash = new ConsistentHash(NumberOfReplicas, serverList)
    connSet = (for (server <- serverList) yield {
      (server.getServer.getIp, server.getServer.getPort)
    }).toSet
  }

  private def nodesEqual(serverList: java.util.List[ServerConn]): Boolean = {
    val currentServerSet = (for (server <- serverList) yield {
      (server.getServer.getIp, server.getServer.getPort)
    }).toSet
    currentServerSet == connSet
  }

}
