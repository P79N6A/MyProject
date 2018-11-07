package com.sankuai.octo.service.actors

import java.text.SimpleDateFormat
import java.util.Date

import akka.actor.Actor
import com.sankuai.inf.octo.mns.ProcessInfoUtil
import com.sankuai.octo.service._
import com.sankuai.octo.service.messager.{Alarm, MODE}
import com.sankuai.octo.scanner.Common
import com.sankuai.octo.scanner.falcon.FalconItem
import com.sankuai.octo.scanner.model.Provider
import com.sankuai.octo.scanner.model.report.ScannerReport
import com.sankuai.octo.scanner.service.{AgentDetector, SendReport}
import com.sankuai.octo.scanner.service.impl.ScanServiceImpl
import com.sankuai.octo.scanner.util.OpsUtils
import com.sankuai.sgagent.thrift.model.fb_status
import org.slf4j.{Logger, LoggerFactory}


/**
 * Created by jiguang on 15/6/4.
 */

class agentCheckActor(provider: Provider) extends Actor {

  private val LOG: Logger = LoggerFactory.getLogger(agentCheckActor.this.getClass)
  var unReachableBeginTime = 0L
  var socketTimeoutNum = 0
  var statusInfo: String = "ALIVE"

  def receive = {
    case Common.checkStatus => {

      statusInfo = "ALIVE"
      FalconItem.totalNum.incrementAndGet();
      var status: fb_status = fb_status.DEAD
      if (ScanServiceImpl.appCheck)
        status = AgentDetector.detect(provider)
      else
        status = detector.detect(provider)
      if (status.equals(fb_status.DEAD)) {
        exceptionHandler(provider)
        statusInfo = "DEAD"
      } else {
        unReachableBeginTime = 0L
        socketTimeoutNum = 0
        statusInfo = "ALIVE"
      }

      val env = provider.getEnv match {
        case 1 => "test"
        case 2 => "stage"
        case 3 => "prod"
      }
      LOG.info(s"check agent /mns/sankuai/$env/${provider.getAppkey}/provider/${provider.getIp}:${provider.getPort} $statusInfo")
    }
  }

  def exceptionHandler(provider: Provider) {

    val providerZKPath = provider.getServerPath
    val ip = provider.getIp
    var hostName = ""
    val env = provider.getEnv match {
      case 1 => "test"
      case 2 => "stage"
      case 3 => "prod"
    }
    var alarmContent = ""
    var rebootContent = ""

    // 可以 ping 通
    if (detector.isReachable(provider.getIp)) {
      val date = getDate();
      FalconItem.failNum.incrementAndGet();
      unReachableBeginTime = 0L
      if (Common.isOnline) {
        hostName = ProcessInfoUtil.getHostName(ip)
        //只有prod环境才重启
        if (provider.getExceptionMsg.contains("Read timed out") && env.equals("prod")) {
          socketTimeoutNum += 1
          if (socketTimeoutNum >= ScanServiceImpl.rebootTimeoutNum) {
            // 通过 ops 重启异常 sg_agent
            val reboot = OpsUtils.reboot(hostName)
            if (reboot)
              rebootContent = s"\n连续${socketTimeoutNum}次超时，可能是close_wait，重启sg_agent成功"
            else
              rebootContent = s"\n连续${socketTimeoutNum}次超时，可能是close_wait，重启sg_agent失败"
          }
        }
        alarmContent = s"${date}\n线上${env}:${hostName}\n${provider.getIp}\n${provider.getExceptionMsg}" + rebootContent
      } else {
        alarmContent = s"${date}\n线下${env}:\n${provider.getIp}\n${provider.getExceptionMsg}"
      }

      if (provider.getExceptionMsg.contains("Connection refused")) {
        LOG.warn(alarmContent)
        sendAlarm(alarmContent)
      }
      if (provider.getExceptionMsg.contains("Read timed out") && socketTimeoutNum >= ScanServiceImpl.rebootTimeoutNum) {
        socketTimeoutNum = 0;
        LOG.warn(alarmContent)
        sendAlarm(alarmContent)
      }
    } else {
      // ping 不通, 再判断连续 ping 不通的时间（线上：12小时，线下：1小时）, 则删除
      LOG.warn(s"unreachable agent: " + providerZKPath)
      FalconItem.pingFailNum.incrementAndGet();
      if (unReachableBeginTime == 0L) {
        unReachableBeginTime = System.currentTimeMillis
      } else if ((System.currentTimeMillis - unReachableBeginTime)
        > Common.unReachableTimeBeforeDelete) {
        if (Common.isOnline) {
          alarmContent = s"线上机器:连续12小时ping不到该主机,判断机器已下线:$providerZKPath"
        } else {
          alarmContent = s"线下机器:连续1小时ping不到该主机,判断机器已下线:$providerZKPath"
        }
        if (Common.allowUpdateZKData) {
          alarmContent += " 从mns里删除"
          zk.deleteWithChildren(providerZKPath)
          SendReport.send(new ScannerReport(1, "DeleteProvider",
            "Delete unReachable agent!", provider.getIdentifierString));
        }
        LOG.warn(alarmContent)
        sendAlarm(alarmContent)
      }
    }
  }

  def sendAlarm(content: String) {
    val alarm = Alarm(content, content, null)
    val modes = Seq(MODE.XM)
    messager.sendAlarm(scalaConstants.alarmList, alarm, modes)
  }

  def getDate(): String ={
    var now: Date = new Date()
    var format: SimpleDateFormat = new SimpleDateFormat("MM-dd HH:mm:ss")
    format.format(now)
  }
}
