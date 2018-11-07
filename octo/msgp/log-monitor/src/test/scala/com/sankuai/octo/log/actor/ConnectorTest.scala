package com.sankuai.octo.log.actor

import java.util.concurrent.TimeUnit._

import akka.actor.{ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit}
import com.sankuai.octo.log.Protocol.WatchInfo
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

import scala.concurrent.duration.{MINUTES => _, SECONDS => _, _}

/**
  * Created by wujinwu on 16/5/17.
  */
class ConnectorTest extends TestKit(ActorSystem("MySpec")) with ImplicitSender
  with WordSpecLike with Matchers with BeforeAndAfterAll {
  override def afterAll {
    TestKit.shutdownActorSystem(system)
  }


  "An Echo actor" must {

    "send back messages unchanged" in {
      var list = Seq[Int]()
      (1 to 3).foreach(i => list = i +: list)
      println(list)
      list = list.dropRight(1)
      println(list)
      val echo = system.actorOf(Props(classOf[Watcher], WatchInfo("testappkey", "/opt/logs/logs/test.log2", Set(" "), "10.4.237.168")))

      echo ! "hello world"
      //      expectMsg("hello world")
      while (true)
        Thread.sleep(10000)
    }

  }

  "Duration" must {

    "equal" in {
      val TIMEOUT = Duration(5, MINUTES)
      val SELF_CHECK_INTERVAL = Duration(2, SECONDS)
      val SELF_CHECK_COUNT = TIMEOUT / SELF_CHECK_INTERVAL
      println(SELF_CHECK_COUNT)
      println(TIMEOUT.toString())
    }

  }
}

