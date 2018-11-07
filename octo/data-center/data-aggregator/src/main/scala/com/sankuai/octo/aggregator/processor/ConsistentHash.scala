package com.sankuai.octo.aggregator.processor

import org.slf4j.{Logger, LoggerFactory}

import scala.util.hashing.MurmurHash3

class ConsistentHash(numberOfReplicas: Int, nodes: List[String]) {

  private val logger: Logger = LoggerFactory.getLogger(this.getClass)

  /** 用来存储虚拟节点hash值 到真实node的映射 */
  private val circle = new java.util.TreeMap[Integer, String]()

  require(numberOfReplicas >= 1)
  nodes.foreach {
    node =>
      (1 to numberOfReplicas).foreach {
        i =>
          circle.put(MurmurHash3.stringHash(node + ":" + StatisticService.PORT + "#" + i), node)
      }
  }

  /**
   *
   * @param key 为给定键取Hash，取得顺时针方向上最近的一个虚拟节点对应的实际节点
   * @return 获得一个最近的顺时针节点
   */
  def get(key: String): String = {
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
