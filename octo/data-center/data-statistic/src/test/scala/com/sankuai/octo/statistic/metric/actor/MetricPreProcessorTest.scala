package com.sankuai.octo.statistic.metric.actor

import akka.actor.Actor.Receive
import akka.actor.{Actor, ActorSystem, Props}
import com.typesafe.config.ConfigFactory
import org.scalatest.FunSuite

/**
  * Created by wujinwu on 16/6/25.
  */
class MetricPreProcessorTest extends FunSuite {

  test("testMailbox"){

    val actorSystem = ActorSystem("test",ConfigFactory.load())
    val actor = actorSystem.actorOf(Props[TestMailboxActor]()/*.withMailbox("custom.bounded-mailbox")*/)
    (1 to 10000000).foreach{i=>
      actor ! i
    }
  }

}

class TestMailboxActor() extends Actor{
  override def receive: Receive = {
    case i:Int => println(i)
  }
}