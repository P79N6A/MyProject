package com.sankuai.octo.msgp.serivce.service

import java.util.concurrent.{Executors, TimeUnit}

import com.sankuai.msgp.common.utils.helper.CommonHelper
import com.sankuai.msgp.common.config.db.msgp.Tables.{AppkeyProviderRow, SchedulerCostRow}
import com.sankuai.msgp.common.model.ServiceModels
import com.sankuai.msgp.common.service.org.OpsService
import com.sankuai.msgp.common.utils.StringUtil
import com.sankuai.octo.msgp.dao.appkey.{AppkeyDescDao, AppkeyProviderDao}
import com.sankuai.octo.msgp.dao.self.OctoJobDao
import com.sankuai.octo.msgp.model.{Appkeys, IdcName, MScheduler, ServiceCategory}
import org.joda.time.DateTime
import org.slf4j.{Logger, LoggerFactory}

import scala.collection.mutable.ListBuffer
import scala.collection.parallel.ForkJoinTaskSupport

object AppkeyDescService {
  private val LOG: Logger = LoggerFactory.getLogger(AppkeyDescService.getClass)

  private val scheduler = Executors.newSingleThreadScheduledExecutor()
  private val threadPool = new ForkJoinTaskSupport(new scala.concurrent.forkjoin.ForkJoinPool(4))

  private val MAX_SIZE = 200


  def refresh = {
    scheduler.scheduleAtFixedRate(new Runnable {
      def run(): Unit = {
        try {
          val start = DateTime.now
          // 更新服务列表
          LOG.info(s"start refresh service provider")
          val serviceList = ServiceDesc.appsName().filter(x => !x.equals(Appkeys.kmsagent.toString) && !x.equals(Appkeys.sgagent.toString))
          saveServiceProvider(serviceList)
          val end = DateTime.now
          OctoJobDao.insertCost(SchedulerCostRow(0, MScheduler.appkeyAndProviderRefresh.toString, start.getMillis, end.getMillis))
          val time = end.getMillis - start.getMillis
          LOG.info(s"end refresh service provider ${time}")
          //单独处理两个比较大的appkey
          val kms = List(Appkeys.kmsagent.toString)
          saveServiceProvider(kms)
          val sgagent = List(Appkeys.sgagent.toString)
          saveServiceProvider(sgagent)

        } catch {
          case e: Exception => LOG.error(s" refresh service provider failed", e)
        }
      }
    }, 60, 900, TimeUnit.SECONDS)
  }

  /**
    * 监听服务提供者
    */
  def saveServiceProvider(appkeys: List[String]) = {
    val appkeysPar = appkeys.par
    appkeysPar.tasksupport = threadPool
    appkeysPar.foreach {
      appkey =>
        AppkeyProviderDao.delete(appkey)
        val thrift_provider = AppkeyProviderService.provider(appkey)
        saveServiceProviderNode("thrift", thrift_provider)
        val http_provider = AppkeyProviderService.httpProvider(appkey)
        saveServiceProviderNode("http", http_provider)
        val isThrift = if (thrift_provider.nonEmpty) true else false
        val isHttp = if (http_provider.nonEmpty) true else false
        val category = if (isHttp && isThrift) {
          ServiceCategory.BOTH.toString
        } else if (isHttp) {
          ServiceCategory.HTTP.toString
        } else {
          ServiceCategory.THRIFT.toString
        }
        if (StringUtil.isNotBlank(category)) {
          updateAppkeyCategory(appkey, category)
        }
    }
  }

  /** *
    * 定时保存 服务提供者
    *
    * @param `type`
    * @param providerNodes
    * @return
    */
  private def saveServiceProviderNode(`type`: String, providerNodes: List[ServiceModels.ProviderNode]) = {
    val listBuffer = ListBuffer[AppkeyProviderRow]()
    providerNodes.foreach {
      node =>
        val idc = IdcName.getNameByIdc(CommonHelper.ip2IDC(node.ip))
        val hostname = OpsService.ipToHost(node.ip)
        val provider = node.toAppkeyProvider.copy(`type` = `type`, idc = idc, hostname = hostname)
        listBuffer.append(provider)
        if (listBuffer.size > MAX_SIZE) {
          try {
            AppkeyProviderDao.batchInsert(listBuffer.toList)
          }
          catch {
            case e: Exception =>
              LOG.error(s"saveServiceProviderNode  fail", e)
          }

          listBuffer.clear()
        }
    }
    if (listBuffer.nonEmpty) {
      try {
        AppkeyProviderDao.batchInsert(listBuffer.toList)
      }
      catch {
        case e: Exception =>
          LOG.error(s"saveServiceProviderNode  fail", e)
      }
    }
  }

  /**
    * 更新服务提供者的类型
    */
  def updateAppkeyCategory(appkey: String, category: String) = {
    AppkeyDescDao.updateCategory(appkey, category)
  }
}
