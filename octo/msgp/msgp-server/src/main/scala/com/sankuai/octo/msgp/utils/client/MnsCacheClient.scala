package com.sankuai.octo.msgp.utils.client

import com.sankuai.octo.mnsc.idl.thrift.service.MNSCacheService
import org.springframework.beans.BeansException
import org.springframework.beans.factory.{FactoryBean, InitializingBean}
import org.springframework.context.{ApplicationContext, ApplicationContextAware}

object MnsCacheClient {
  private var client: MNSCacheService.Iface = null

  def getInstance: MNSCacheService.Iface = {
    client
  }
}

class MnsCacheClient extends FactoryBean[AnyRef] with ApplicationContextAware with InitializingBean {
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
    MnsCacheClient.client = clientProxy.asInstanceOf[MNSCacheService.Iface]
  }
}