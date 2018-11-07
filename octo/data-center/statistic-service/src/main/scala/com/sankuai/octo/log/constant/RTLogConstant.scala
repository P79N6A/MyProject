package com.sankuai.octo.log.constant

import java.util.concurrent.TimeUnit._

import com.sankuai.inf.octo.mns.util.ProcessInfoUtil
import org.slf4j.LoggerFactory
import org.springframework.util.StringUtils

import scala.concurrent.duration.Duration

/**
  * Created by wujinwu on 16/4/21.
  */
object RTLogConstant {

  private val logger = LoggerFactory.getLogger(this.getClass)
  /**
    * true :线下环境
    * false: 线上环境
    */
  val isOffline = {
    val env = System.getProperty("env", "offline")
    !StringUtils.hasText(env) || env == "offline"
  }
  val LOG_SERVER_PORT = 8080

  val LOG_EVENT_NAME = "LogEvent"

  val LOG_AGENT_PORT = 3132

  //  64KB
  val MAX_FRAME_LENGTH = 64 * 1024

  val TIMEOUT = Duration(5, MINUTES)

  val LOG_BUFFER_THRESHOLD = 1000

  /*"http://localhost:8080"*/
  val MSGP_SERVER = if (isOffline) {
    "http://octo.test.sankuai.info"
  } else {
    "http://octo.sankuai.com"
  }

  val CLIENT_TOPIC = "client"

  val UUID_TOPIC = "uuid"

  val localIP = ProcessInfoUtil.getLocalIpV4

  logger.info(s"ip:$localIP")
}
