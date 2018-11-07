package com.sankuai.octo.errorlog.dao

import java.util.Date

import com.sankuai.msgp.common.config.DbConnection
import com.sankuai.octo.errorlog.db.Tables._
import com.sankuai.octo.statistic.helper.TimeProcessor

import scala.slick.driver.MySQLDriver.simple._
import scala.slick.jdbc.{StaticQuery => Q}
import scala.slick.lifted.CanBeQueryCondition

object ErrorLogStatisticDao {
  private val db = DbConnection.getErrorLogPool()

  implicit class QueryHelper[T, E](query: Query[T, E, Seq]) {
    def optionFilter[X, R: CanBeQueryCondition](name: Option[X])(f: (T, X) => R) =
      name.map(v => query.withFilter(f(_, v))).getOrElse(query)
  }

  case class HostSetCount(hostSet: String, count: Int)

  def groupBySet(appkey: String, stime: Int, etime: Int) = {
    db withSession {
      implicit session: Session =>

        // 将范围查询放到最后可以提升性能
        val ret = ErrorLogStatistic.filter(x => x.appkey === appkey).filter(x => x.time >= stime && x.time <= etime)
          .groupBy(x => x.hostSet).map {
          case (hostSet, list) =>
            (hostSet, list.map(_.count).sum)
        }.list

        List(HostSetCount("All", ret.map(_._2.getOrElse(0)).sum)) ++ ret.map { x =>
          HostSetCount(x._1, x._2.getOrElse(0))
        }.sortBy(-_.count)
    }
  }

  case class HostCount(host: String, count: Int)

  def groupByHost(appkey: String, stime: Int, etime: Int, hostSet: String, filterId: Int, exceptionName: String) = {
    db withSession {
      implicit session: Session =>
        val hostSetOpt = if (hostSet.equalsIgnoreCase("All")) {
          None
        } else {
          Some(hostSet)
        }

        // -1表示不检索filterId
        val filterIdOpt = if (filterId == -1) {
          None
        } else {
          Some(filterId)
        }

        // 在filterId为0时才检索exceptionName
        val exceptionNameOpt = if (filterId == 0) {
          Some(exceptionName)
        } else {
          None
        }

        // 将范围查询放到最后可以提升性能
        val ret = ErrorLogStatistic.filter(x => x.appkey === appkey)
          .optionFilter(hostSetOpt)(_.hostSet === _)
          .optionFilter(filterIdOpt)(_.filterId === _)
          .optionFilter(exceptionNameOpt)(_.exceptionName === _)
          .filter(x => x.time >= stime && x.time <= etime)
          .groupBy(x => x.host).map {
          case (host, list) =>
            (host, list.map(_.count).sum)
        }.list

        List(HostCount("All", ret.map(_._2.getOrElse(0)).sum)) ++ ret.map { x =>
          HostCount(x._1, x._2.getOrElse(0))
        }.sortBy(-_.count)
    }
  }

  case class FilterCount(id: Int, name: String, count: Int, alarm: Boolean)

  def groupByFilterId(appkey: String, stime: Int, etime: Int, hostSet: String, host: String) = {
    db withSession {
      implicit session: Session =>
        val hostSetOpt = if (hostSet.equalsIgnoreCase("All")) {
          None
        } else {
          Some(hostSet)
        }

        val hostOpt = if (host.equalsIgnoreCase("All")) {
          None
        } else {
          Some(host)
        }

        // 根据filterId划分
        val q1 = ErrorLogStatistic.filter(x => x.appkey === appkey && x.filterId =!= 0)
          .optionFilter(hostSetOpt)(_.hostSet === _)
          .optionFilter(hostOpt)(_.host === _)
          .filter(x => x.time >= stime && x.time <= etime)
        val retByFilterId = for {
          a <- q1.groupBy(_.filterId).map {
            case (filterId, list) =>
              (filterId, list.map(_.count).sum)
          }
          b <- ErrorLogFilter if a._1 === b.id
        } yield (b.id, b.name, a._2, b.alarm)

        // 根据exceptionName划分
        val q2 = ErrorLogStatistic.filter(x => x.appkey === appkey && x.filterId === 0)
          .optionFilter(hostSetOpt)(_.hostSet === _)
          .optionFilter(hostOpt)(_.host === _)
          .filter(x => x.time >= stime && x.time <= etime)
        val retByException = q2.groupBy(_.exceptionName).map {
          case (exceptionName, list) =>
            (0, exceptionName, list.map(_.count).sum, true)
        }

        val total_count = retByFilterId.list.map(_._3.getOrElse(0)).sum + retByException.list.map(_._3.getOrElse(0)).sum

        List(FilterCount(-1, "All", total_count, false)) ++ (retByFilterId.list ++ retByException.list).map { x =>
          FilterCount(x._1, x._2, x._3.getOrElse(0), x._4)
        }.sortBy(-_.count)
    }
  }

  case class TimeCount(time: Int, count: Int)

  def groupByTime(appkey: String, stime: Int, etime: Int, hostSet: String, host: String, filterId: Int, exceptionName: String) = {
    db withSession {
      implicit session: Session =>
        val hostSetOpt = if (hostSet.equalsIgnoreCase("All")) {
          None
        } else {
          Some(hostSet)
        }

        val hostOpt = if (host.equalsIgnoreCase("All")) {
          None
        } else {
          Some(host)
        }

        // -1表示不检索filterId
        val filterIdOpt = if (filterId == -1) {
          None
        } else {
          Some(filterId)
        }

        // 在filterId为0时才检索exceptionName
        val exceptionNameOpt = if (filterId == 0) {
          Some(exceptionName)
        } else {
          None
        }

        // 将范围查询放到最后可以提升性能
        val ret = ErrorLogStatistic.filter(x => x.appkey === appkey)
          .optionFilter(hostSetOpt)(_.hostSet === _)
          .optionFilter(hostOpt)(_.host === _)
          .optionFilter(filterIdOpt)(_.filterId === _)
          .optionFilter(exceptionNameOpt)(_.exceptionName === _)
          .filter(x => x.time >= stime && x.time <= etime)
          .groupBy(x => x.time).map {
          case (time, list) =>
            (time, list.map(_.count).sum)
        }.list
        ret.map { x =>
          TimeCount(x._1, x._2.getOrElse(0))
        }.sortBy(_.time)
    }
  }

  def getErrorCount(appkey: String, startTime: Date, stopTime: Date) = {
    db withSession {
      implicit session: Session =>
        val count = ErrorLogStatistic.filter(x => x.appkey === appkey && x.time >= TimeProcessor.getMinuteStart((startTime.getTime / 1000).toInt)
          && x.time <= TimeProcessor.getMinuteStart((stopTime.getTime / 1000).toInt)).map(_.count).sum.run
        if (count.nonEmpty) count.get else 0
    }
  }

  case class AppkeyCount(appkey: String, logCount: Int)

  def getErrorCount(startTime: Date, stopTime: Date) = {
    db withSession {
      implicit session: Session =>
        val q = ErrorLogStatistic.filter(x => x.time >= TimeProcessor.getMinuteStart((startTime.getTime / 1000).toInt)
          && x.time <= TimeProcessor.getMinuteStart((stopTime.getTime / 1000).toInt))
        val tmp = q.groupBy(_.appkey).map {
          case (appkey, list) =>
            (appkey, list.map(_.count).sum)
        }.list
        tmp.map { x =>
          AppkeyCount(x._1, x._2.getOrElse(0))
        }
    }
  }

  def main(args: Array[String]) {
    println(groupByHost("com.sankuai.inf.logCollector", 1473077640, 1473238980, "All", 1177, "java.net.URISyntaxException"))
    println(groupByFilterId("com.sankuai.inf.logCollector", 1473077640, 1473238980, "All", "All"))
    println(groupByTime("com.sankuai.inf.logCollector", 1473077640, 1473238980, "All", "All", -1, ""))
  }
}
