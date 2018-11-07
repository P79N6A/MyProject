package com.sankuai.octo.log

import akka.actor.{ActorSystem, PoisonPill, Props}
import akka.cluster.singleton.{ClusterSingletonManager, ClusterSingletonManagerSettings}
import com.sankuai.octo.log.actor.{AppkeyRouter, WatcherManager}
import com.sankuai.octo.log.constant.RTLogConstant
import com.typesafe.config.ConfigFactory
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.context.annotation.{ComponentScan, Configuration}

@Configuration
@EnableAutoConfiguration
@ComponentScan(value = Array("com.sankuai.octo.log"))
object LogCluster extends App {

  {
    Bootstrap.init()
    start()
  }

  private def start() = {
    val system = startActorSystem()
    system.actorOf(
      ClusterSingletonManager.props(
        Props[AppkeyRouter](),
        PoisonPill,
        ClusterSingletonManagerSettings(system).withRole("")),
      "router")

    system.actorOf(Props[WatcherManager], "watcherManager")
  }

  def startActorSystem() = {
    //  load conf
    val conf = ConfigFactory.parseString("akka.remote.netty.tcp.hostname = " + RTLogConstant.localIP).
      withFallback(ConfigFactory.load())
    val system = ActorSystem("LogClusterSystem", conf)
    system
  }
}
