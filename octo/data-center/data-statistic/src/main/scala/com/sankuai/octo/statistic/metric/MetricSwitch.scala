package com.sankuai.octo.statistic.metric

import com.sankuai.meituan.config.listener.IConfigChangeListener
import com.sankuai.octo.statistic.model.StatGroup
import com.sankuai.octo.statistic.util.config
import org.slf4j.LoggerFactory

object MetricSwitch {
  private val logger = LoggerFactory.getLogger(this.getClass)

  private var groupSwitchMap = Map[StatGroup, Boolean]()

  private val ON = "on"
  private val OFF = "off"

  //  注册开关

  StatGroup.values().foreach {

    case group@StatGroup.SpanLocalHost =>
      val spanLocalHostSwitchStr = "switch.SpanLocalHost"
      val spanLocalHostSwitch = config.get(spanLocalHostSwitchStr, ON)
      groupSwitchMap += group -> (spanLocalHostSwitch == ON)

      config.addListener(spanLocalHostSwitchStr, new IConfigChangeListener() {
        def changed(key: String, oldValue: String, newValue: String) = {
          logger.info(s"change $key $oldValue $newValue")
          groupSwitchMap += group -> (newValue == ON)
        }
      })

    case group@StatGroup.SpanRemoteApp =>
      val spanRemoteAppSwitchStr = "switch.SpanRemoteApp"
      val spanRemoteAppSwitch = config.get(spanRemoteAppSwitchStr, ON)
      groupSwitchMap += group -> (spanRemoteAppSwitch == ON)

      config.addListener(spanRemoteAppSwitchStr, new IConfigChangeListener() {
        def changed(key: String, oldValue: String, newValue: String) = {
          logger.info(s"change $key $oldValue $newValue")
          groupSwitchMap += group -> (newValue == ON)
        }
      })
    case group@StatGroup.Span =>
      val spanSwitchStr = "switch.Span"
      val spanSwitch = config.get(spanSwitchStr, ON)
      groupSwitchMap += group -> (spanSwitch == ON)

      config.addListener(spanSwitchStr, new IConfigChangeListener() {
        def changed(key: String, oldValue: String, newValue: String) = {
          logger.info(s"change $key $oldValue $newValue")
          groupSwitchMap += group -> (newValue == ON)
        }
      })


    case group@StatGroup.SpanRemoteHost =>
      val spanRemoteHostSwitchStr = "switch.SpanRemoteHost"
      val spanRemoteHostSwitch = config.get(spanRemoteHostSwitchStr, ON)
      groupSwitchMap += group -> (spanRemoteHostSwitch == ON)

      config.addListener(spanRemoteHostSwitchStr, new IConfigChangeListener() {
        def changed(key: String, oldValue: String, newValue: String) = {
          logger.info(s"change $key $oldValue $newValue")
          groupSwitchMap += group -> (newValue == ON)
        }
      })


    case group@StatGroup.LocalHostRemoteHost =>
      groupSwitchMap += group -> false

    case group@StatGroup.LocalHostRemoteApp =>
      val localHostRemoteAppSwitchStr = "switch.LocalHostRemoteApp"
      val localHostRemoteAppSwitch = config.get(localHostRemoteAppSwitchStr, ON)
      groupSwitchMap += group -> (localHostRemoteAppSwitch == ON)

      config.addListener(localHostRemoteAppSwitchStr, new IConfigChangeListener() {
        def changed(key: String, oldValue: String, newValue: String) = {
          logger.info(s"change $key $oldValue $newValue")
          groupSwitchMap += group -> (newValue == ON)
        }
      })
    case group@StatGroup.RemoteAppRemoteHost =>
      val remoteAppRemoteHostSwitchStr = "switch.RemoteAppRemoteHost"
      val remoteAppRemoteHostSwitch = config.get(remoteAppRemoteHostSwitchStr, ON)
      groupSwitchMap += group -> (remoteAppRemoteHostSwitch == ON)

      config.addListener(remoteAppRemoteHostSwitchStr, new IConfigChangeListener() {
        def changed(key: String, oldValue: String, newValue: String) = {
          logger.info(s"change $key $oldValue $newValue")
          groupSwitchMap += group -> (newValue == ON)
        }
      })
  }

  def isOpen(group: StatGroup) = {
    groupSwitchMap.getOrElse(group, false)
  }


}
