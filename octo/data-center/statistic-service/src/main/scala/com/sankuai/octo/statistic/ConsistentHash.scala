package com.sankuai.octo.statistic

import com.meituan.service.mobile.mtthrift.client.model.ServerConn
import org.slf4j.{Logger, LoggerFactory}

import scala.collection.JavaConversions._
import scala.util.hashing.MurmurHash3

class ConsistentHash(numberOfReplicas: Int, nodes: java.util.List[ServerConn]) {

  private val logger: Logger = LoggerFactory.getLogger(this.getClass)

  /** 用来存储虚拟节点hash值 到真实node的映射 */
  private val circle = new java.util.TreeMap[Integer, ServerConn]()

  require(numberOfReplicas >= 1)
  for (node <- nodes) {
    for (i <- 1 to numberOfReplicas)
      circle.put(MurmurHash3.stringHash(node.getServer.getIp + ":" + node.getServer.getPort + "#" + i), node)
  }

  /**
   *
   * @param key 为给定键取Hash，取得顺时针方向上最近的一个虚拟节点对应的实际节点
   * @return 获得一个最近的顺时针节点
   */
  def get(key: String): ServerConn = {
    if (circle.isEmpty) {
      logger.error("ConsistentHash get server Fail,key:{}", key)
      return null
    }
    var hash = MurmurHash3.stringHash(key)
    if (!circle.containsKey(hash)) {
      val tailMap = circle.tailMap(hash); ////返回此映射的部分视图，其键大于等于 hash
      hash = if (tailMap.isEmpty) circle.firstKey() else tailMap.firstKey()
    }
    circle.get(hash)
  }

}
