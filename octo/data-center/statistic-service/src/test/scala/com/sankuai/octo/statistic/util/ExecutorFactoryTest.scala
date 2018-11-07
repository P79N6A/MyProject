package com.sankuai.octo.statistic.util

import org.scalatest.FunSuite

import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.util.Random

/**
 * Created by wujinwu on 16/4/30.
 */
class ExecutorFactoryTest extends FunSuite {

  test("testExecute") {
    def test(t: String) = {
      println(t)
    }
    val executor = ExecutorFactory2(test)

    (1 to 1000).foreach { i =>
      val str = Random.nextString(100)
      executor.execute(str)
    }
    Thread.sleep(10000)
  }

  implicit lazy val ec: ExecutionContextExecutor = ExecutionContextFactory.build(20)

  test("future") {
    (1 to 100).foreach{
      i=>
      Future {
        Thread.sleep( 1000)
         println("helo")
      }
    }
  }

}
