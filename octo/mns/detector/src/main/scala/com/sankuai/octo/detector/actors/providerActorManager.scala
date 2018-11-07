package com.sankuai.octo.detector.actors

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

import akka.actor.{Props, ActorRef, ActorSystem}
import org.slf4j.{Logger, LoggerFactory}


object providerActorManager {
  private val LOG: Logger = LoggerFactory.getLogger(providerActorManager.getClass)
  private val system = ActorSystem("providerActorSystem")
  val actorMap = new ConcurrentHashMap[String, ActorRef]()

  def check(env: String, appKey: String, providerPath: String, providersDir: String, scanRoundCounter: AtomicInteger) {
    if (actorMap.get(providerPath) == null) {
      val providerActor = system.actorOf(Props(classOf[providerCheckActor], env, appKey, providerPath, providersDir, scanRoundCounter).withDispatcher("my-dispatcher"))
      actorMap.put(providerPath, providerActor)
      providerActor ! scanRoundCounter.get()
      LOG.info("actorMap size:" + actorMap.size + " insert new provider:" + providerPath)
    } else {
      val providerActor = actorMap.get(providerPath)
      providerActor ! scanRoundCounter.get()
    }
  }
}
