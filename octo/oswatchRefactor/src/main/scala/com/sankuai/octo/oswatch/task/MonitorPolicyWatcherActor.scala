package com.sankuai.octo.oswatch.task

import java.util.concurrent.TimeUnit

import akka.actor.{ReceiveTimeout, ActorLogging, Actor}
import com.sankuai.octo.oswatch.db.Tables.{OswatchMonitorPolicyRow}
import com.sankuai.octo.oswatch.model.{WatchActorMail, ExecutorMail, OctoEnv, WatcherMail}
import com.sankuai.octo.oswatch.server.ActorManager
import com.sankuai.octo.oswatch.service.{FalconService, HarborClient, HttpService, LogCollectorService}
import com.sankuai.octo.oswatch.thrift.data.{GteType, MonitorType}
import com.sankuai.octo.oswatch.utils._
import scala.compat.Platform
import scala.concurrent.duration._
import scala.util.control.Breaks._

/**
 * Created by dreamblossom on 15/9/30.
 */
class MonitorPolicyWatcherActor(var oswatchMonitorPolicy: OswatchMonitorPolicyRow) extends Actor with ActorLogging {
  context.setReceiveTimeout(Duration(Common.checkInterval * Common.actorExitPeriod, SECONDS))
  var loop = false
  var lastCheckTimestamp = 0L

  def receive = {
    case WatcherMail.Start =>
      log.info("Watcher start")
      loop = true
      monitorTask()

    case WatcherMail.Stop =>
      log.info("Watcher stop")
      loop = false

    case WatcherMail.Update(mp) =>
      log.info("Update: " + mp)
      oswatchMonitorPolicy = mp
      monitorTask()

    case ReceiveTimeout =>
      if (loop) {
        log.info("Watcher exit")
        ActorManager.loader ! WatchActorMail.quit(self.path.name.toLong)
        context.stop(self)
      }
  }

  //enum MonitorType { CPU = 0, MEM = 1, QPS = 2, TP50 = 3, TP90 = 4, TP95 = 5, TP99 = 6, }
  def monitorTask(): Unit = {
    oswatchMonitorPolicy.monitorType match {
      case 0 => taskForMemOrCPU(0)
      case 1 => taskForMemOrCPU(1)
      case 2 => taskForQPS
      case _ => taskForMetrics
    }
  }

  def taskForQPS = {
    val currentTimestamp = Platform.currentTime
    val secondDiff = (currentTimestamp - lastCheckTimestamp) / Common.ONE_SECOND_IN_MS
    secondDiff > oswatchMonitorPolicy.watchperiod match {
      case false =>
      //未到监控周期 nothing to do
      case true =>
        //暂对qps对查询监测支持到appkey级别
        val spanname = oswatchMonitorPolicy.spanName match {
          case None => "all"
          case Some(tmpSpanname) => tmpSpanname
        }
        val qpsCheckPeriodInSecond = oswatchMonitorPolicy.watchperiod > Common.qpsMinWatchPeriodInSecond match {
          case true => oswatchMonitorPolicy.watchperiod
          case false => Common.qpsMinWatchPeriodInSecond
        }
        val qpsMap = LogCollectorService.getCurrentQPS(oswatchMonitorPolicy.appkey, spanname, OctoEnv.getEnv(oswatchMonitorPolicy.env), currentTimestamp, qpsCheckPeriodInSecond)
        val allQPS = qpsMap.foldLeft(0.0) { (total, pr) =>
          total + pr._2
        }.toLong
        log.info("qpsMap: " + qpsMap)

        var aliveNode = 0
        breakable {
          //循环查询aliveNode数量，直至成功退出
          for (i <- 0 until Common.httpFailQueryTimes) {
            aliveNode = HttpService.getAliveNode(oswatchMonitorPolicy)
            if (aliveNode != 0) break
            TimeUnit.SECONDS.sleep(Common.httpFailQueryWaitTime)
          }
        }
        if (aliveNode == 0) {
          log.info("alive node is zero, appkey: " + oswatchMonitorPolicy.appkey)
        } else {
          log.info("aliveNode: " + aliveNode)
          log.info("avgQPS: " + (allQPS / aliveNode).toInt)
        }
        //比较每个服务节点的平均QPS是否触发监控配置
        //enum GteType { GTE = 0, LTE = 1,}
        if ((oswatchMonitorPolicy.gtetype == 0 && allQPS > oswatchMonitorPolicy.monitorvalue * aliveNode) ||
          (oswatchMonitorPolicy.gtetype == 1 && allQPS < oswatchMonitorPolicy.monitorvalue * aliveNode)) {
          //触发监控，返回
          lastCheckTimestamp = currentTimestamp
          context.system.actorSelection("/user/executor") ! ExecutorMail.TellRegister(oswatchMonitorPolicy, allQPS)
        }
    }
  }

  //tag =0 cpu //tag = 1 mem
  def taskForMemOrCPU(tag:Int) = {
    log.info(s"taskForMemOrCPU is called ")
    val currentTimestamp = Platform.currentTime
    val secondDiff = (currentTimestamp - lastCheckTimestamp) / Common.ONE_SECOND_IN_MS
    secondDiff > oswatchMonitorPolicy.watchperiod match {
      case false =>
      //未到监控周期 nothing to do
      case true =>
        val containerNameList = HarborClient.getSGContainerName(oswatchMonitorPolicy.appkey,oswatchMonitorPolicy.idc.getOrElse(""),oswatchMonitorPolicy.env)
        containerNameList match {
          case  Some(containerList) =>
            containerList.length > 0 match {
              case true =>
                //scalingGroup 有实例
                val containerInfList = FalconService.getContainerInf(containerList)
                val falconDataSum = tag match{
                  case 0 =>
                    containerInfList.foldLeft(0.0) { (total, tmpContainerInf) => total + tmpContainerInf._2.cpu }
                  case 1 =>
                    containerInfList.foldLeft(0.0) { (total, tmpContainerInf) => total + tmpContainerInf._2.mem }
                }
                val falconDataAvg = falconDataSum/containerList.length
                log.info(s"${oswatchMonitorPolicy}: " + "sum cpu/mem:" +falconDataSum)
                log.info(s"containerLIst : ${ containerList}")
                log.info(s"falconDataAvg : ${ falconDataAvg}")
                if ((oswatchMonitorPolicy.gtetype == 0 && falconDataSum > oswatchMonitorPolicy.monitorvalue * containerList.length) ||
                  (oswatchMonitorPolicy.gtetype == 1 && falconDataSum < oswatchMonitorPolicy.monitorvalue * containerList.length)) {
                  //触发监控，返回
                  log.info(s"触发扩缩容 ")

                  lastCheckTimestamp = currentTimestamp
                  context.system.actorSelection("/user/executor") ! ExecutorMail.TellRegister(oswatchMonitorPolicy, f"$falconDataAvg%.2f".toDouble)
                }
              case false =>
                // scalingGroup 无实例 不触发监控
                log.error(s"taskForMem: getSGContainerName($oswatchMonitorPolicy) has no Container")
             }

          case None =>
           log.error(s"taskForMem: getSGContainerName($oswatchMonitorPolicy)")
        }
    }
  }

  def taskForMetrics = {

  }
}
