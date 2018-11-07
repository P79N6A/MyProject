package com.sankuai.octo.msgp.serivce.monitor

import java.util.concurrent.{Executors, TimeUnit}

import com.sankuai.meituan.config.listener.IConfigChangeListener
import com.sankuai.msgp.common.config.MsgpConfig
import com.sankuai.msgp.common.utils.client.TairClient
import com.sankuai.octo.msgp.dao.monitor.MonitorTriggerDAO
import org.slf4j.{Logger, LoggerFactory}

/**
  * Created by yves on 16/12/1.
  *
  * 名称: 报警监控调度
  * 功能: 每分钟内将需要监控的监控项
  * 平均分配给MSGP其他机器
  *
  */
object MonitorTriggerLeader {

  val logger: Logger = LoggerFactory.getLogger(MonitorTriggerLeader.getClass)
  val scheduler = Executors.newScheduledThreadPool(2)

  //暂时屏蔽性能监控的开关
  var MONITOR_DISABLED: Boolean = {
    val value = MsgpConfig.get("monitor.disabled", "false")
    value.toBoolean
  }

  {
    MsgpConfig.addListener("monitor.disabled", new IConfigChangeListener() {
      def changed(key: String, oldValue: String, newValue: String) = {
        logger.info("monitor.disabled", newValue)
        MONITOR_DISABLED = newValue.toBoolean
      }
    })
  }

  val startOfEveryRoundOfMonitor = "startOfEveryRoundOfMonitor"

  //定时appkey任务
  def start() {
    val now = System.currentTimeMillis()
    //得到距离整分钟的秒数
    val init = (60000 - (now % 60000)) / 1000l
    scheduler.scheduleAtFixedRate(new Runnable {
      def run(): Unit = {
        logger.info(s"new round in leader is ${System.currentTimeMillis() / 1000l}")
        //新一轮执行的时间: 秒粒度
        val currentCreateTime = System.currentTimeMillis() / 1000L / 60L * 60l
        try {
          if (!MONITOR_DISABLED) {
            //插入新一轮的数据
            MonitorTriggerDAO.batchInsertTriggerStatus(currentCreateTime)
            TairClient.put(startOfEveryRoundOfMonitor, s"$currentCreateTime", 70)
            logger.info(s"currentCreateTime in leader is $currentCreateTime")
            //上报性统计数据至Falcon, 取三分钟之前数据
            val lastCreateTime = currentCreateTime - 60L * 3
            MonitorSelfCheck.uploadMonitorStatus(lastCreateTime)
          }
        } catch {
          case e: Exception =>
            logger.error(s"batch inset into TriggerStatus failed. currentCreateTime: $currentCreateTime")
        }
      }
    }, init, 60l, TimeUnit.SECONDS)

    //删除过期的项目
    scheduler.scheduleAtFixedRate(new Runnable {
      def run(): Unit = {
        //删除时间: 分钟粒度
        val deleteTime = System.currentTimeMillis() / 1000L / 60L * 60L - 10 * 60L
        try {
          //删除失效数据
          MonitorTriggerDAO.deleteTriggerStatus(deleteTime)
        } catch {
          case e: Exception =>
            logger.error(s"delete TriggerStatus failed. deleteTime: $deleteTime")
        }
      }
    }, init, 600l, TimeUnit.SECONDS)
  }
}
