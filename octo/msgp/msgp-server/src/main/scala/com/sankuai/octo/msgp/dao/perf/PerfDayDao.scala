package com.sankuai.octo.msgp.dao.perf

import java.text.DecimalFormat

import com.sankuai.msgp.common.config.DbConnection
import com.sankuai.msgp.common.config.db.msgp.Tables._
import org.joda.time.{DateTime, LocalDate}

import scala.slick.driver.MySQLDriver.simple._
import scala.slick.jdbc.GetResult
import scala.slick.jdbc.StaticQuery.interpolation

/**
 * Created by zava on 16/8/26.
 */
object PerfDayDao {
  private val db = DbConnection.getPool()

  private val tags = "spanname:*,localhost:all"
  private val logCollector = "com.sankuai.inf.logCollector"

  private val mode = "kpi"

  def updateOrInsertPerfDay(dataList: List[PerfDayRow]) = {
    db withSession {
      implicit session: Session =>
        dataList.foreach {
          data =>
            val dayOption = PerfDay.filter(x => x.ts === data.ts && x.mode === mode && x.tags === tags && x.appkey === data.appkey && x.spanname === data.spanname).firstOption
            dayOption.fold {
              PerfDay += data
            } {
              o =>
                PerfDay.filter(x => x.id === o.id).map(r => (r.appkeyCategory,r.count, r.qps, r.upper50, r.upper90, r.upper95, r.upper99, r.upper)).
                  update(data.appkeyCategory,data.count, data.qps, data.upper50, data.upper90, data.upper95, data.upper99, data.upper)
            }
        }
    }
  }

  def dailyKpi(date: LocalDate) = {
    val ts = (date.toDateTimeAtStartOfDay.getMillis / 1000).toInt
    val spanname = "all"
    db withSession {
      implicit session: Session =>
        val list = PerfDay.filter(x => x.ts === ts && x.mode === mode && x.spanname === spanname).list
        list.sortBy(-_.count)
    }
  }


  case class PerfCount(ts: Int, category: String, count: Long)

  implicit val getPerfCountResult = GetResult(r => PerfCount(r.<<, r.<<, r.<<))

  case class CategoryPerfCount(name:String,data:List[String])
  case class RequestCount(data:List[String],legend:List[String],series:List[CategoryPerfCount])

  def getReqCount = {
    db withSession {
      implicit session: Session =>
        val numFormat = new DecimalFormat("###,###")
        val format = new java.text.SimpleDateFormat("yyyy-MM-dd")
        val date = format.parse(new DateTime().toString("yyyy-MM-dd"))
        val dateTime = date.getTime/1000
        val twoWeekDays = 13
        val weekAgo = (1 to twoWeekDays).map {
          x=>
          (dateTime  - 3600 * 24 * x).toInt
        } mkString ","
        val sqlString = s"select ts,appkey_category as category,sum(count) from perf_day " +
          s"where  ts in (${weekAgo}) and mode= 'kpi' and tags = '${tags}' and   spanname='all' and localhost='all'" +
          s" and appkey <> '${logCollector}' and  appkey not like  '%.tair.%' GROUP  by ts,appkey_category ORDER  by ts "
        val requestCount =sql"""#${sqlString}""".as[PerfCount].list

        val categoryCount = requestCount.groupBy(_.category)
        val legend = categoryCount.keys.toList
        var data =  List[String]()
        val series = categoryCount.map {
          categoryMap =>
            val category = categoryMap._1
            val list = categoryMap._2
            if(data.isEmpty){
              data = list.map(self => format.format(new java.util.Date(self.ts * 1000L)))
            }
            val value = list.map(self => self.count.toString)
            CategoryPerfCount(category,value)
        }.toList
        val avgCount = requestCount.map(_.count).sum / twoWeekDays
        (numFormat.format(avgCount),RequestCount(data,legend,series))
    }
  }
}
