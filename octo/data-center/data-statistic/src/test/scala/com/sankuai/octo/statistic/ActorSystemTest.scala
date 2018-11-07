package com.sankuai.octo.statistic

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestActors, TestKit}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

/**
  * Created by wujinwu on 16/6/17.
  */
class ActorSystemTest extends TestKit(ActorSystem("MySpec")) with ImplicitSender
  with WordSpecLike with Matchers with BeforeAndAfterAll {
  override def afterAll {
    TestKit.shutdownActorSystem(system)
  }

  "An Echo actor" must {

    "send back messages unchanged" in {
      while (true) {
        val echo = system.actorOf(TestActors.echoActorProps)
        echo ! "hello world"
        expectMsg("hello world")
        Thread.sleep(1)
      }
    }

  }
}
