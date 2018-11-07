package com.sankuai.octo.export.impl

import java.util.concurrent.ConcurrentHashMap

import akka.actor.{ActorSystem, Props}
import com.meituan.jmonitor.JMonitor
import com.meituan.service.mobile.mtthrift.util.ClientInfoUtil
import com.sankuai.octo.export.histogram.DailyHistogramProcessor
import com.sankuai.octo.export.histogram.DailyHistogramProcessor.DailyHistogram
import com.sankuai.octo.export.instance.InstanceManager
import com.sankuai.octo.log.constant.RTLogConstant
import com.sankuai.octo.statistic.domain._
import com.sankuai.octo.statistic.metrics.{SimpleCountHistogram2, SimpleCountReservoir2}
import com.sankuai.octo.statistic.service.LogExportService
import com.typesafe.config.ConfigFactory
import org.apache.thrift.TException
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class LogExportServiceImpl extends LogExportService {
  private val logger = LoggerFactory.getLogger(this.getClass)
  private val system = startActorSystem()
  private val instanceManager = system.actorOf(Props[InstanceManager]().withDispatcher("custom.tag-dispatcher"), "instanceManager")
  private val dailyProcessor = system.actorOf(Props[DailyHistogramProcessor]().withDispatcher("custom.tag-dispatcher"), "dailyHistogramProcessor")

  /**
   * 接受天粒度的数据，并聚合
   * @param appkey
   * @param name key的名字
   * @param histogram 参数
   */
  @throws(classOf[TException])
  override def sendDailyData(appkey: String, name: String, histogram: SimpleCountHistogram3) = {
    JMonitor.kpiForCount("dataExport.minuter.daily")
    val histogram2 = asHistogram2(histogram)
    val clientIp = ClientInfoUtil.getClientIp
    dailyProcessor ! new DailyHistogram(appkey, clientIp,name, histogram2)
  }

  /**
   * 接受，分钟，小时粒度的数据，聚合
   * @param instance
   */
  @throws(classOf[TException])
  override def sendGroupRangeData(instance: Instance3) = {
    JMonitor.kpiForCount("dataExport.minuter.groupRange")
    val clientIp = ClientInfoUtil.getClientIp
    val instance2 = asInstance2(instance,clientIp)
    instanceManager ! instance2
  }


  private def startActorSystem() = {
    //     load conf
    val conf = ConfigFactory.parseString("akka.remote.netty.tcp.hostname = " + RTLogConstant.localIP).
      withFallback(ConfigFactory.load())
    val system = ActorSystem("LogExport", conf)
    system
  }

  def asHistogram2(histogram: SimpleCountHistogram3): SimpleCountHistogram2 = {
    val reservoir3 = histogram.getReservoir
    val values = new ConcurrentHashMap[Integer, java.lang.Long](1000)
    values.putAll(reservoir3.getValues)
    val reservoir2 = new SimpleCountReservoir2(reservoir3.getMax, values)
    new SimpleCountHistogram2(histogram.getCount, histogram.getSuccessCount,
      histogram.getExceptionCount, histogram.getTimeoutCount, histogram.getDropCount,
      histogram.getHTTP2XXCount, histogram.getHTTP3XXCount, histogram.getHTTP4XXCount, histogram.getHTTP5XXCount,
      histogram.getVersion, histogram.getCreateTime, histogram.getUpdateTime, reservoir2)
  }

  def asInstance2(instance: Instance3,clientIp:String): Instance2 = {
    val instanceKey3 = instance.getKey
    val groupKey3 = instance.getGroupKey
    val statTag3 = groupKey3.getStatTag
    val statTag: StatTag = new StatTag(statTag3.getSpanname.intern(), statTag3.getLocalHost.intern(),
      statTag3.getRemoteHost.intern(), statTag3.getRemoteAppKey.intern(),
      statTag3.getInfraName.intern())

    val key: InstanceKey2 = InstanceKey2(instanceKey3.getAppKey, instanceKey3.getEnv, instanceKey3.getSource, instanceKey3.getPerfProtocolType)
    val groupKey: GroupKey = GroupKey(groupKey3.getTs, groupKey3.getRange, groupKey3.getGroup, statTag)
    val histogram: SimpleCountHistogram2 = asHistogram2(instance.getHistogram)

    new Instance2(key, groupKey,histogram,clientIp)
  }



}
