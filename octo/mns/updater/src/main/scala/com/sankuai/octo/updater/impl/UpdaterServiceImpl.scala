package com.sankuai.octo.updater.impl

import akka.actor.{Props, ActorSystem}
import com.sankuai.octo.updater.actor.{HttpProviderCheckActor, StatusChecker}
import com.sankuai.octo.updater.actor.StatusChecker.StatusInfo
import com.sankuai.octo.updater.thrift.{ProviderStatus, UpdaterService}
import com.typesafe.config.ConfigFactory
import org.springframework.stereotype.Service

/**
  * Created by wujinwu on 16/6/8.
  */

@Service("updaterService")
class UpdaterServiceImpl extends UpdaterService.Iface {

  private val system = {
    val config = ConfigFactory.load()
    ActorSystem("UpdaterService", config)
  }
  private val statusChecker = system.actorOf(Props[StatusChecker](), "statusChecker")

  override def doubleCheck(providerPath: String, status: ProviderStatus): Unit = {
    statusChecker ! StatusInfo(providerPath, status)

  }

  override def userDefinedHttpDoubleCheck(providerPath: String, status: ProviderStatus, checkUrl: String): Unit = {
    val providerActor = system.actorOf(Props(new HttpProviderCheckActor(providerPath, status)).withDispatcher("updater-dispatcher"))
    providerActor ! checkUrl
  }
}
