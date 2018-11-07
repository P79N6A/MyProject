package com.sankuai.octo.msgp.serivce.mcc

import org.springframework.beans.BeansException
import org.springframework.beans.factory.{FactoryBean, InitializingBean}
import org.springframework.context.{ApplicationContext, ApplicationContextAware}

/**
 * Created by lhmily on 02/29/2016.
 */
object MtConfigService{
  private var client: com.sankuai.octo.config.service.MtConfigService.Iface = null

  def getInstance: com.sankuai.octo.config.service.MtConfigService.Iface = {
    client
  }
}
class MtConfigService extends FactoryBean[AnyRef] with ApplicationContextAware with InitializingBean{
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
    MtConfigService.client = clientProxy.asInstanceOf[com.sankuai.octo.config.service.MtConfigService.Iface]
  }
}
