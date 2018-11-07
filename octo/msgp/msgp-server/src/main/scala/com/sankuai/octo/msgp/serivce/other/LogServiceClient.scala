package com.sankuai.octo.msgp.serivce.other

import com.sankuai.octo.errorlog.service.LogService
import org.slf4j.LoggerFactory
import org.springframework.beans.BeansException
import org.springframework.beans.factory.{FactoryBean, InitializingBean}
import org.springframework.context.{ApplicationContext, ApplicationContextAware}

object LogServiceClient {
  private val logger = LoggerFactory.getLogger(this.getClass)

  private var client: LogService = null

  def getInstance: LogService = {
    logger.info(s"LogService getInstance ${client}")
    client
  }
}

class LogServiceClient extends FactoryBean[AnyRef] with ApplicationContextAware with InitializingBean {
  private var clientProxy: AnyRef = null

  @throws(classOf[BeansException])
  def setApplicationContext(applicationContext: ApplicationContext) {
  }

  @throws(classOf[Exception])
  def getObject: AnyRef = {
    this
  }

  def getObjectType: Class[_] = {
    this.getClass
  }

  def isSingleton: Boolean = {
    true
  }

  @throws(classOf[Exception])
  def afterPropertiesSet {
    initInstance
  }

  def getClientProxy: AnyRef = {
    clientProxy
  }

  def setClientProxy(clientProxy: AnyRef) {
    this.clientProxy = clientProxy
  }

  private def initInstance {
    LogServiceClient.client = clientProxy.asInstanceOf[LogService]
  }
}