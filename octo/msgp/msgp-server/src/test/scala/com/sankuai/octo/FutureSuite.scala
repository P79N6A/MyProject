package com.sankuai.octo

import java.util.concurrent.TimeUnit

import com.sankuai.octo.statistic.util.ExecutionContextFactory
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{BeforeAndAfter, FunSuite}

import scala.concurrent._
import scala.concurrent.duration.Duration

/**
 * Created by zava on 16/8/27.
 */

@RunWith(classOf[JUnitRunner])
class FutureSuite extends FunSuite with BeforeAndAfter {

  private implicit val timeout = Duration.create(10, TimeUnit.SECONDS)
  private implicit val ec = ExecutionContextFactory.build(Runtime.getRuntime.availableProcessors)


  test("serialiseFutures") {
    val futureList = Future.traverse((1 to 10).toList) {
      x => Future {
        if (x / 3 == 0) {
          Some((0 to x).toList)
        } else {
          None
        }
      }
    }
    val result = Await.result(futureList, timeout)
    val map = result.flatten(_.getOrElse(List()))
    println(map)
  }
}