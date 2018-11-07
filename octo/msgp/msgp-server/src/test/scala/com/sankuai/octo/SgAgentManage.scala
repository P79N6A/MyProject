package com.sankuai.octo

import java.util.concurrent.{Executors, TimeUnit}

import com.sankuai.msgp.common.config.DbConnection
import com.sankuai.msgp.common.config.db.msgp.Tables._
import com.sankuai.octo.msgp.serivce.manage.AgentAvailability
import org.scalatest.{BeforeAndAfter, FunSuite}

import scala.slick.driver.MySQLDriver.simple._

/**
  * Created by yves on 17/2/17.
  */
class SgAgentManage  extends FunSuite with BeforeAndAfter {

  private val monitorExe = Executors.newScheduledThreadPool(2)
  private val db = DbConnection.getPool()

  test("check") {
    monitorExe.scheduleAtFixedRate(new Runnable {
      override def run(): Unit = {
        val checkList = getCheckJob.filter(_.id == 42)
        println(checkList)
        checkList.foreach {
          x => x.providers.split(",").toList.foreach {
            self => AgentAvailability.check(self, x.protocol, x.apps.split(",").toList)
          }
        }
      }
    }, 10, 60, TimeUnit.SECONDS)
    Thread.sleep(1000000)
  }


  def getCheckJob = {
    db withSession {
      implicit session: Session =>
        AgentChecker.list
    }
  }
}

