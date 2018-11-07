package com.sankuai.octo.msgp.utils.client

import com.sankuai.octo.msgp.utils.FreeMarkerTemplate
import org.slf4j.{Logger, LoggerFactory}
import org.springframework.beans.BeansException
import org.springframework.beans.factory.{FactoryBean, InitializingBean}
import org.springframework.context.{ApplicationContext, ApplicationContextAware}

/**
 * Created by zava on 16/4/27.
 */
object FreeMarkerClient {
  private val log: Logger = LoggerFactory.getLogger(FreeMarkerClient.getClass)

  private var freeMarkerTemplate: FreeMarkerTemplate = null

  def getInstance: FreeMarkerTemplate = {
    freeMarkerTemplate
  }
}

class FreeMarkerClient extends FactoryBean[AnyRef] with ApplicationContextAware with InitializingBean {
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
    FreeMarkerClient.freeMarkerTemplate = clientProxy.asInstanceOf[FreeMarkerTemplate]
  }
}
