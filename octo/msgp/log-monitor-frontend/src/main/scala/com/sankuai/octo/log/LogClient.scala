package com.sankuai.octo.log

import akka.actor.{ActorRef, ActorSystem, AddressFromURIString, Props, RootActorPath}
import akka.cluster.client.{ClusterClient, ClusterClientSettings}
import akka.japi.Util._
import com.sankuai.octo.log.actor.WebSocketServer
import com.sankuai.octo.log.constant.RTLogConstant
import com.typesafe.config.ConfigFactory
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.context.annotation.{ComponentScan, Configuration}

@Configuration
@EnableAutoConfiguration
@ComponentScan(value = Array("com.sankuai.octo.log"))
object LogClient extends App {

  {
    Bootstrap.init()
    start()
  }

  private def start() = {
    val system = startActorSystem()

    val clusterClient = startClusterClient(system)

    startWebSocketServer(system, clusterClient)
  }

  def startClusterClient(actorSystem: ActorSystem) = {
    val conf = ConfigFactory.load()
    val initialContacts = immutableSeq(conf.getStringList("contact-points")).map {
      case AddressFromURIString(addr) => RootActorPath(addr) / "system" / "receptionist"
    }.toSet

    val clusterClient = actorSystem.actorOf(
      ClusterClient.props(
        ClusterClientSettings(actorSystem)
          .withInitialContacts(initialContacts)),
      "clusterClient")
    clusterClient
  }

  def startActorSystem() = {
    //     load conf
    val conf = ConfigFactory.parseString("akka.remote.netty.tcp.hostname = " + RTLogConstant.localIP).
      withFallback(ConfigFactory.load())
    val system = ActorSystem("LogClientSystem", conf)
    system
  }

  def startWebSocketServer(system: ActorSystem, clusterClient: ActorRef) = {
    system.actorOf(Props(classOf[WebSocketServer], clusterClient), "webSocketServer")
  }


}
