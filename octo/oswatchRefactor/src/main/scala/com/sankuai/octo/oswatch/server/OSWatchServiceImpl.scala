package com.sankuai.octo.oswatch.server

import akka.actor.{Props, ActorSystem}
import com.meituan.service.mobile.mtthrift.server.MTIface
import com.sankuai.octo.oswatch.dao.MonitorPolicyDAO
import com.sankuai.octo.oswatch.db.Tables.OswatchMonitorPolicyRow
import com.sankuai.octo.oswatch.model.{WatchActorMail, LoaderMail}
import com.sankuai.octo.oswatch.task.{MonitorPolicyWatcherActor, MonitorPolicyLoaderActor, MonitorPolicyExecutorActor}
import com.sankuai.octo.oswatch.thrift.data.{OswatchResponse, ErrorCode, MonitorPolicy}
import com.sankuai.octo.oswatch.thrift.service.OSWatchService
import org.slf4j.{LoggerFactory, Logger}

/**
 * Created by dreamblossom on 15/9/29.
 */
class OSWatchServiceImpl extends MTIface with OSWatchService.Iface {

  def init(): Unit ={
    ActorManager.loader ! LoaderMail.Start
  }

  def addMonitorPolicy(monitorPolicy: MonitorPolicy, responseUrl: String) = {
    println("addMonitorPolicy is called "+monitorPolicy)
    val ret = MonitorPolicyDAO.insertOrUpdate(toOswatchMonitorPolicyRow(monitorPolicy,responseUrl))
    ret._2 match {
      case Some(id) =>
        new OswatchResponse(ErrorCode.OK).setOswatchId(id)
      case None =>
        new OswatchResponse(ErrorCode.ERROR)
    }
  }

  def updateMonitorPolicy(oswatchId:Long, monitorPolicy: MonitorPolicy, responseUrl: String) = {
    println("updateMonitorPolicy is called "+monitorPolicy)
    val ret = MonitorPolicyDAO.update(oswatchId,toOswatchMonitorPolicyRow(monitorPolicy,responseUrl,oswatchId ))
    ret._2 match {
      case Some(id) =>
        new OswatchResponse(ErrorCode.OK).setOswatchId(id)
      case None =>
        new OswatchResponse(ErrorCode.ERROR)
    }
  }

  def delMonitorPolicy(oswatchId: Long) = {
    new OswatchResponse(MonitorPolicyDAO.delete(oswatchId))
  }

  private def toOswatchMonitorPolicyRow(monitorPolicy: MonitorPolicy, responseUrl: String, oswatchId:Long = 0)= {
    val oswatchMonitorPolicy=new OswatchMonitorPolicyRow(
      oswatchId match {
        case 0 => monitorPolicy.oswatchId
        case _ => oswatchId
      },
      monitorPolicy.appkey,
      monitorPolicy.isSetIdc match {
        case false => None
        case true => Some(monitorPolicy.idc)
      },
      monitorPolicy.env.getValue,
      monitorPolicy.gteType.getValue,
      monitorPolicy.watchPeriod,
      monitorPolicy.monitorType.getValue,
      monitorPolicy.value,
      monitorPolicy.isSetSpanName match {
        case false => None
        case true => Some(monitorPolicy.spanName)
      },
      responseUrl,
      monitorPolicy.isSetProviderCountSwitch match {
        case false => 0
        case true => monitorPolicy.providerCountSwitch
      }
    )
    oswatchMonitorPolicy
  }
}
