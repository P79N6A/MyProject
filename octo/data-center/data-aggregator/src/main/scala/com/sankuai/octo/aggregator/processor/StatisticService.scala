package com.sankuai.octo.aggregator.processor

import com.meituan.service.mobile.mtthrift.proxy.ThriftClientProxy
import com.sankuai.meituan.config.listener.IConfigChangeListener
import com.sankuai.octo.aggregator.util.MyProxy
import com.sankuai.octo.statistic.service.LogStatisticService
import org.apache.commons.lang3.StringUtils
import org.slf4j.LoggerFactory

import scala.collection.concurrent.TrieMap

object StatisticService {
  private val LOG = LoggerFactory.getLogger(StatisticService.getClass)
  //statistic服务的map
  private val serverMap: TrieMap[String, LogStatisticService] = TrieMap()
  private val config = MyProxy.mcc

  private val LOCAL_APPKEY = "com.sankuai.inf.logCollector"
  private val REMOTE_APPKEY = "com.sankuai.inf.data.statistic"

  private val ALLOCATE = "allocate"
  private val SEVER_LIST = "server.list"
  //一个appkey对应多个IP
  private val STATISTIC_ALLOCATE = "statistic.allocate"

  //通过计算的方式获取最合适的一致性hash备份数目
  private val NumberOfReplicas = 61

  //指定appkey、host的map，key是appkey，value是ipx
  private var allocateMap: Map[String, List[String]] = constructAllocate(getAllocateHost)
  private var statisticAllocateMap: Map[String, List[String]] = constructStatisticAllocated(getStatisticAllocateHost)
  //一致性hash环
  private var consistentHash: ConsistentHash = constructHash(getServerList)

  val PORT = 8940

  {

    //指定定host构建
    config.addListener(ALLOCATE, new IConfigChangeListener() {
      def changed(key: String, oldValue: String, newValue: String) = {
        LOG.error(s"StatisticService allocate change $key oldValue: $oldValue newValue: $newValue")
        allocateMap = constructAllocate(newValue)
      }
    })

    //指定定host构建
    config.addListener(STATISTIC_ALLOCATE, new IConfigChangeListener() {
      def changed(key: String, oldValue: String, newValue: String) = {
        LOG.error(s"StatisticService allocate change $key oldValue: $oldValue newValue: $newValue")
        statisticAllocateMap = constructStatisticAllocated(newValue)
      }
    })

    try {
      //server list构建
      config.addListener(SEVER_LIST, new IConfigChangeListener() {
        def changed(key: String, oldValue: String, newValue: String) = {
          LOG.error(s"StatisticService server list change $key $oldValue $newValue")
          consistentHash = constructHash(newValue.split(",").toList)
        }
      })


      LOG.info("addListener success")
    }
    catch {
      case e: Exception => LOG.error("addListener failed", e)
    }
  }

 private def getIpByAppkey(appkey: String) = {
    //先从指定appkey、host的map中取ip，取不到再从一致性hash环中取ip
    allocateMap.getOrElse(appkey, List(consistentHash.get(appkey)))
  }


  /**
   * 根据appkey获取statistic服务
   * @param appkey
   * @return
   */
  def getStatisticServerList(appkey: String) = {
    statisticAllocateMap.getOrElse(appkey, getIpByAppkey(appkey))
  }

  def apply(paramAppkey: String) = {
    val serverList = getStatisticServerList(paramAppkey)
    val serverListIdentify = serverList.sortWith( _ > _).mkString(",")
    serverMap.getOrElseUpdate(serverListIdentify, constructService(serverList))
  }

  //构建mtthrift client proxy
  private def constructService(serverList: List[String]): LogStatisticService = {
    val ipAndPorts = serverList.map(ip => s"$ip:$PORT").mkString(",")
    try {
      val proxy: ThriftClientProxy = new ThriftClientProxy
      proxy.setAppKey(LOCAL_APPKEY)
      proxy.setRemoteAppkey(REMOTE_APPKEY)
      proxy.setServiceInterface(classOf[LogStatisticService])
      proxy.setServerIpPorts(ipAndPorts)
      proxy.afterPropertiesSet()
      proxy.setTimeout(500)
      val client: LogStatisticService = proxy.getObject.asInstanceOf[LogStatisticService]
      client
    } catch {
      case e: Exception =>
        LOG.error("StatisticServiceProxy init fail", e)
        throw new RuntimeException("StatisticServiceProxy init fail", e)
    }
  }

  //根据server list构建一致性hash
  private def constructHash(serverList: List[String]) = {
    new ConsistentHash(NumberOfReplicas, serverList)
  }

  //获取MCC的server list
  private def getServerList = {
    config.get(SEVER_LIST, "").split(",").toList
  }

  //获取MCC的指定appkey到host
  private def getAllocateHost = {
    config.get(ALLOCATE, "")
  }

  private def getStatisticAllocateHost = {
    config.get(STATISTIC_ALLOCATE, "")
  }

  //构建指定的host的map
  def constructAllocate(value: String) = {
    if (StringUtils.isBlank(value)) {
      Map[String, List[String]]()
    } else {
      try {
        value.split(";").flatMap {
          x =>
            val tmp = x.split(":")
            val keys = tmp(0).split(",").toSet
            val value = tmp(1)
            keys.map(_ -> List(value))
        }.toMap
      } catch {
        case e: Exception => LOG.error(s"$value 不符合格式，例子：" +
          s"com.sankuai.inf.logCollector,com.sankuai.inf.mnsc:10.4.232.74;com.sankuai.inf.msgp:10.4.232.76,value :${value}", e)
          Map[String, List[String]]()
      }
    }
  }

  /**
   * @return  appkey -> ipList
   */
  private def constructStatisticAllocated(value: String): Map[String, List[String]] = {
    if (StringUtils.isBlank(value)) {
      Map[String, List[String]]()
    } else {
      try {
        value.split(";").map {
          x =>
            val tmp = x.split(":")
            val key = tmp(0)
            val value = tmp(1).split(",").toList
            key -> value
        }.toMap

      } catch {
        case e: Exception => LOG.error(s"$value 不符合格式，例子：" +
          s"com.sankuai.inf.logCollector:10.4.232.74,10.4.232.76;com.sankuai.inf.msgp:10.4.232.74,10.4.232.76", e)
          Map[String, List[String]]()
      }
    }

  }

  def main(args: Array[String]) {
    def getAppkeyHost(appkey: String) = {
      println(consistentHash.get(appkey))
    }

    getAppkeyHost("com.sankuai.hotel.vangogh.online")
  }
}