package com.sankuai.octo.msgp.utils.client

import org.springframework.beans.BeansException
import org.springframework.beans.factory.{FactoryBean, InitializingBean}
import org.springframework.context.{ApplicationContext, ApplicationContextAware}

/**
  * Created by zmz on 2017/8/7.
  */
object PortraitClient {

  private var client: com.sankuai.inf.hulk.portrait.thrift.service.PortraitService.Iface = null

  def getInstance: com.sankuai.inf.hulk.portrait.thrift.service.PortraitService.Iface = {
    client
  }
}

class PortraitClient extends FactoryBean[AnyRef] with ApplicationContextAware with InitializingBean{
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
    PortraitClient.client = clientProxy.asInstanceOf[com.sankuai.inf.hulk.portrait.thrift.service.PortraitService.Iface]
  }
}