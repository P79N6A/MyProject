package com.sankuai.octo.detector.actors.http

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

import akka.actor.{ActorRef, ActorSystem, Props}
import com.sankuai.octo.detector.actors.providerCheckActor
import org.slf4j.{Logger, LoggerFactory}

/**
 * Copyright (C) 2015 Meituan
 * All rights reserved
 * User: gaosheng
 * Date: 16-7-4
 * Time: 下午5:05
 */
object HttpProviderActorManager {
  private val LOG: Logger = LoggerFactory.getLogger(HttpProviderActorManager.getClass)
  private val system = ActorSystem("httpProviderActorSystem")
  val actorMap = new ConcurrentHashMap[String, ActorRef]()

  def check(env: String, appKey: String, providerPath: String, providersDir: String, checkUrl: String, scanRoundCounter: AtomicInteger) {
    val httpCheckMessage = new HttpCheckMessage(scanRoundCounter.get(), checkUrl)
    if (actorMap.get(providerPath) == null) {
      val providerActor = system.actorOf(Props(classOf[HttpProviderCheckActor], env, appKey, providerPath, providersDir, scanRoundCounter))
      actorMap.put(providerPath, providerActor)
      providerActor ! httpCheckMessage
      LOG.info("http actorMap size:" + actorMap.size + " insert new provider:" + providerPath)
    } else {
      val providerActor = actorMap.get(providerPath)
      providerActor ! httpCheckMessage
    }
  }
}