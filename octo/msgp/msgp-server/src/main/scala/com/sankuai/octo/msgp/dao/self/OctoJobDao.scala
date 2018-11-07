package com.sankuai.octo.msgp.dao.self

import com.sankuai.msgp.common.config.DbConnection
import com.sankuai.msgp.common.config.db.msgp.Tables._
import com.sankuai.octo.msgp.serivce
import org.slf4j.{Logger, LoggerFactory}

import scala.slick.driver.MySQLDriver.simple._


object OctoJobDao {
  val LOG: Logger = LoggerFactory.getLogger(serivce.AppkeyAuth.getClass)

  private val db = DbConnection.getPool()

  //insert into SchedulerCost
  def insertCost(value: SchedulerCostRow) = {
    LOG.info(s"insert cost value ${value}")
    db withSession {
      implicit session: Session =>
        val id = (SchedulerCost returning SchedulerCost.map(_.id)) += value
        id
    }
  }

  //insert or update OctoJob
  def inOrUpJob(value: OctoJobRow) = {
    db withSession {
      implicit session: Session =>
        val statement = OctoJob.filter(self =>
          self.appkey === value.appkey &&
            self.identifier === value.identifier &&
            self.job === value.job
        )
        if(statement.exists.run) { //update
          statement.map(self => (self.stime, self.cost, self.content)).update(value.stime, value.cost, value.content)
        } else { //insert
        val id = (OctoJob returning OctoJob.map(_.id)) += value
          id
        }
    }
  }

  //select from SchedulerCost
  def selectCost(name: String) = {
    val limit = 20
    db withSession {
      implicit session: Session =>
        SchedulerCost.filter(x => (x.name === name)).sortBy(x => x.sTime.desc).take(limit).list
    }
  }
}
