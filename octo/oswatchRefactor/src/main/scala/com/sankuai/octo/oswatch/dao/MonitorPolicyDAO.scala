package com.sankuai.octo.oswatch.dao

import com.sankuai.octo.oswatch.db.Tables._
import com.sankuai.octo.oswatch.thrift.data.{MonitorPolicy, ErrorCode}
import com.sankuai.octo.oswatch.utils.DBConnection
import org.slf4j.{LoggerFactory, Logger}
import scala.slick.driver.MySQLDriver.simple._

/**
 * Created by dreamblossom on 15/9/29.
 */
object MonitorPolicyDAO {
  private final val LOG: Logger = LoggerFactory.getLogger(MonitorPolicyDAO.getClass)
  val dbcp = DBConnection.getPool

  def insertOrUpdate(oswatchMonitorPolicy: OswatchMonitorPolicyRow) =
    try {
      dbcp withSession {
        implicit session: Session =>
          oswatchMonitorPolicy.id match {
            case 0 =>
              println("insert OswatchMonitorPolicy is called ")
              val id = (OswatchMonitorPolicy returning OswatchMonitorPolicy.map(_.id)) += oswatchMonitorPolicy
              (ErrorCode.OK, Some(id))
            case _ =>
              println("update OswatchMonitorPolicy is called ")
              OswatchMonitorPolicy.filter(x=>(x.id === oswatchMonitorPolicy.id)).update(oswatchMonitorPolicy)
              (ErrorCode.OK, Some(oswatchMonitorPolicy.id))
          }
      }
    } catch {
      case ex: Exception =>
        LOG.error("insert " + oswatchMonitorPolicy + " failed," + ex)
        (ErrorCode.ERROR, None)
    }

  def insert(oswatchMonitorPolicy: OswatchMonitorPolicyRow) =
    try {
      dbcp withSession {
        implicit session: Session =>
          println("insert OswatchMonitorPolicy is called ")
          val id = (OswatchMonitorPolicy returning OswatchMonitorPolicy.map(_.id)) += oswatchMonitorPolicy
          (ErrorCode.OK, Some(id))
      }
    } catch {
      case ex: Exception =>
        LOG.error("insert " + oswatchMonitorPolicy + " failed," + ex)
        (ErrorCode.ERROR, None)
    }

  def delete(id: Long) =
    try {
      dbcp withSession {
        implicit session: Session =>
          OswatchMonitorPolicy.filter(_.id === id).delete
          ErrorCode.OK
      }
    } catch {
      case ex: Exception =>
        LOG.error("delete monitorPolicyid" + id + " failed," + ex)
        ErrorCode.ERROR
    }

  def update(oswatchId:Long, oswatchMonitorPolicy: OswatchMonitorPolicyRow) =
    try{
      dbcp withSession {
        implicit  session: Session =>
          println("update OswatchMonitorPolicy is called ")
          OswatchMonitorPolicy.filter(_.id === oswatchId).update(oswatchMonitorPolicy)
          (ErrorCode.OK, Some(oswatchId))
      }
    } catch {
      case ex: Exception =>
        LOG.error("update " + oswatchMonitorPolicy + " failed," + ex)
        (ErrorCode.ERROR, None)
    }

  def getAll =
    dbcp withSession {
      implicit session: Session =>
        OswatchMonitorPolicy.list
    }

  def get(id: Long) =
    dbcp withSession {
      implicit session: Session =>
        OswatchMonitorPolicy.filter(_.id === id).list.headOption match {
          case Some(sg) => Some(sg)
          case None => None
        }
    }

  def exists(id: Long) =
    dbcp withSession {
      implicit session: Session =>
        OswatchMonitorPolicy.filter(_.id === id).list.nonEmpty
    }
}
