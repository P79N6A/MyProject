package com.sankuai.octo.msgp.serivce.manage

import java.util.concurrent.TimeUnit

import com.google.common.cache.{CacheBuilder, CacheLoader}
import com.sankuai.msgp.common.model.{Env, ServiceModels}
import com.sankuai.msgp.common.service.org.OpsService
import com.sankuai.octo.msgp.dao.appkey.AppkeyDescDao
import com.sankuai.octo.msgp.model.{Appkeys, Echart}
import com.sankuai.octo.msgp.serivce.service.{AppkeyProviderService, ServiceCommon, ServiceDesc}
import com.sankuai.octo.msgp.utils.client.MnsCacheClient
import com.sankuai.sgagent.thrift.model.SGService
import org.slf4j.{Logger, LoggerFactory}

import scala.collection.JavaConverters._

object ThriftChecker {
  val LOG: Logger = LoggerFactory.getLogger(ThriftChecker.getClass)
  val thriftTypeList = List("mtthrift", "cthrift")

  case class ProvideGroupByVersion(version: String, providerList: List[ServiceModels.ProviderNode])

  case class thriftVersionCount(thriftType: String, pieData: Echart.Pie)

//  val expiredTime = 60l
//  val appsCache = CacheBuilder.newBuilder().expireAfterWrite(expiredTime, TimeUnit.MINUTES)
//    .build(new CacheLoader[String, List[ServiceModels.ProviderNode]]() {
//      def load(key: String) = {
//        try {
//          getAllAppProvideDesc(key)
//        }
//        catch {
//          case e: Exception => LOG.error(s"key $key 获取数据失败",e)
//            List[ServiceModels.ProviderNode]()
//        }
//      }
//    })

  private def getAllAppProvideDesc(thriftType: String) = {
    // 获取所有appkey,过滤掉sg_agent
    val sgagentStr = Appkeys.sgagent.toString
    val allApps = ServiceDesc.appsName.filterNot(_.equals(sgagentStr))
    // 根据appkey并发获取所有的provide的desc
    val mnsc = MnsCacheClient.getInstance
    val list = allApps.par.flatMap {
      appkey =>
        val mnscRet = Env.values.flatMap {
          env =>
            try {
              mnsc.getMNSCache(appkey, "0", env.toString).getDefaultMNSCache.asScala
            }
            catch {
              case e: Exception => LOG.error(s"getMNSCache appkey:${appkey},env:${env.toString} error",e)
                None
            }
        }
        mnscRet.filter(_.version.startsWith(thriftType))
    }.toList
    list.map(ServiceModels.SGService2ProviderNode)
  }

  def getThriftVersionCount(business: Int, owt: String, pdl: String) = {
    thriftTypeList.map{
      thriftType=>
        val originalList = AppkeyProviderService.appsCache.get(thriftType)
        val filteredList = if (business == -1) {
          originalList
        } else {
          val appkeys = AppkeyDescDao.appkeys(business, owt, pdl)
          originalList.filter(x => appkeys.contains(x.appkey))
        }
        /** 构造饼状图 */
        val pieSeriesData = filteredList.groupBy(_.version).map(x => Echart.PieSeriesData(x._1, x._2.length)).toList
        val pieCount = Echart.Pie(pieSeriesData.map(_.name).sortWith(_ < _), pieSeriesData)
        thriftVersionCount(thriftType, pieCount)
    }
  }


  def provideGroupByVersion(thriftType: String, version: String) = {
    val list = AppkeyProviderService.appsCache.get(thriftType)
    val result = list.filter(_.version == version).map(x => x.copy(name = Some(x.appkey + " " + OpsService.ipToHost(x.ip))))
    ProvideGroupByVersion(version, result)
  }
}
