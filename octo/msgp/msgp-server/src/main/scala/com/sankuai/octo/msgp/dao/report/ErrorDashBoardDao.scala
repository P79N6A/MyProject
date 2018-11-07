package com.sankuai.octo.msgp.dao.report

import com.sankuai.msgp.common.config.DbConnection
import com.sankuai.msgp.common.config.db.msgp.Tables._

import scala.collection.mutable.ListBuffer
import scala.slick.driver.MySQLDriver.simple._
import scala.slick.jdbc.{GetResult, StaticQuery}

/**
 * Created by zava on 16/4/25.
 * 日报
 */
object ErrorDashBoardDao {
  private val db = DbConnection.getPool()

  case class ErrorDashBoardCount(owt: String = "", appkey: String, node: String = "", octoCount: Int = 0,falconCount: Int = 0,errorCount: Int = 0)

  implicit val getErrorDashBoardCountResult = GetResult(r => ErrorDashBoardCount(r.<<, r.<<, r.<<, r.<<, 0,0))

  def batchInsert(list: Seq[ErrorDashBoardDomain]) = {
    db withSession {
      implicit session: Session =>
        val seq = list.map(domain => {
          ErrorDashboardRow(0, domain.owt,domain.appkey, domain.node,domain.errorCount, domain.falconCount, domain.octoCount,domain.time, domain.createTime)
        })
        seq.foreach(ErrorDashboard.insertOrUpdate)
    }
  }

  def updateOctoCount(domain: ErrorDashBoardDomain) = {
    db withSession {
      implicit session: Session =>
        val statement = ErrorDashboard.filter(x => x.appkey === domain.appkey && x.time === domain.time).map(x => (x.octoCount))
        if (statement.exists.run) {
          statement.update(domain.octoCount)
        }else{
          val row = ErrorDashboardRow(0, domain.owt,domain.appkey, domain.node, domain.errorCount, domain.falconCount, domain.octoCount,domain.time, domain.createTime)
          ErrorDashboard.insert(row)
        }
    }
  }


  def updateFalconCount(domain: ErrorDashBoardDomain) = {
    db withSession {
      implicit session: Session =>
        val statement = ErrorDashboard.filter(x => x.appkey === domain.appkey && x.time === domain.time).map(x => (x.falconCount))
        if (statement.exists.run) {
          statement.update(domain.falconCount)
        }else{
          val row = ErrorDashboardRow(0, domain.owt,domain.appkey, domain.node,domain.errorCount, domain.falconCount, domain.octoCount,domain.time, domain.createTime)
          ErrorDashboard.insert(row)
        }
    }
  }

  def query(start:Int,end:Int,sort:String="errlog_count desc") ={
    db withSession {
      implicit session: Session =>
        val sql = s"select owt,appkey,node ,sum(octo_count) as octo_count " +
          s"from error_dashboard where  time  >=  ${start} and time <= ${end} group by appkey order by $sort "
        val data = ListBuffer[ErrorDashBoardCount]()
        StaticQuery.queryNA[ErrorDashBoardCount](sql) foreach{ c =>
          data.append(c)
        }
        data
    }
  }

  case class ErrorDashBoardDomain(owt:String,appkey: String,node:String,octoCount: Int=0,errorCount:Int=0,falconCount:Int=0,time:Long, createTime: Long = 0L)

}
