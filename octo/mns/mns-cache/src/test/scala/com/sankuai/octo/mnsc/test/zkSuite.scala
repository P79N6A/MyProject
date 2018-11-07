package com.sankuai.octo.mnsc.test

import com.sankuai.octo.mnsc.model.Env
import org.apache.curator.framework.api.CuratorWatcher
import com.sankuai.octo.mnsc.remote.zk
import org.apache.zookeeper.WatchedEvent
import org.junit.runner.RunWith
import org.scalatest.{BeforeAndAfter, FunSuite}
import org.scalatest.junit.JUnitRunner

/**
  * Created by lhmily on 10/31/2016.
  */
@RunWith(classOf[JUnitRunner])
class zkSuite extends FunSuite with BeforeAndAfter with CuratorWatcher {
  test("test zk") {
      zk.addDataWatcher("/mns/sankuai/prod/com.sankuai.octo.tmy/provider", this)
  }

  override def process(event: WatchedEvent): Unit = {
    println(event.toString)
    zk.addDataWatcher("/mns/sankuai/prod/com.sankuai.octo.tmy/provider", this)
  }
}
