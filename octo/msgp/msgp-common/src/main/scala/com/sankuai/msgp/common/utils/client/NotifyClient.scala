package com.sankuai.msgp.common.utils.client

import com.sankuai.meituan.notify.thrift.service.NotifyService
import org.springframework.beans.BeansException
import org.springframework.beans.factory.{FactoryBean, InitializingBean}
import org.springframework.context.{ApplicationContext, ApplicationContextAware}

/**
 * Created by zava on 16/4/28.
 */
object NotifyClient {

  private var notifyService: NotifyService.Iface = null

  def getInstance: NotifyService.Iface = {
    notifyService
  }
}

class NotifyClient extends FactoryBean[AnyRef] with ApplicationContextAware with InitializingBean {
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
    NotifyClient.notifyService = clientProxy.asInstanceOf[NotifyService.Iface]
  }
}
