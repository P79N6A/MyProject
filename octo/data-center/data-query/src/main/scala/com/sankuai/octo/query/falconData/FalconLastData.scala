package com.sankuai.octo.query.falconData

import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

import com.ning.http.client.ProxyServer
import com.sankuai.octo.query.TagQueryHandler
import com.sankuai.octo.query.falconData.FalconHistoryData.{EndpointCounter, DataSeries}
import com.sankuai.octo.statistic.constant.Constants
import com.sankuai.octo.statistic.helper.api
import com.sankuai.octo.statistic.model.{StatEnv, StatGroup, StatSource}
import com.sankuai.octo.statistic.util.common
import dispatch._
import org.slf4j.{Logger, LoggerFactory}
import play.api.libs.json.Json

import scala.collection.immutable.TreeMap
import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.collection.JavaConversions._

// 与history区别是：last查询最新上报的一个数据点信息，用于分钟级别报警
object FalconLastData {
  val LOG: Logger = LoggerFactory.getLogger(this.getClass)
  val proxy = new ProxyServer("10.32.140.181", 80)
  val host = if (common.isOffline) {
    url("http://10.12.22.249:9966/graph/last").setProxyServer(proxy)
  } else {
    url("http://query.falcon.vip.sankuai.com:9966/graph/last")
  }
  val postReq = host.POST
  val time = Duration.create(20000, TimeUnit.MILLISECONDS)
  val lastReadCount = new AtomicInteger(0)

  case class FalconDataResponse(value: Option[DataSeries], counter: Option[String], endpoint: Option[String])

  implicit val var1 = Json.reads[FalconDataResponse]
  implicit val var2 = Json.writes[FalconDataResponse]

  case class ResponseData(var count: Option[DataSeries], var qps: Option[DataSeries], var cost_50: Option[DataSeries], var cost_90: Option[DataSeries], var cost_95: Option[DataSeries], var cost_99: Option[DataSeries], var qps_drop: Option[DataSeries])

  private def query(param: AnyRef) = {
    val paramStr = api.jsonStr(param)
    lastReadCount.incrementAndGet()
    try {
      val future = Http(postReq << paramStr > as.String)
      val result = Await.result(future, time)
      Json.parse(result).validate[List[FalconDataResponse]].fold({ error =>
        LOG.error(s"FalconLastDataResponse parse failed $error"); None
      }, {
        value => Some(value)
      })
    } catch {
      case e: Exception => LOG.error(s"get data from falconLast failed, params: $paramStr", e); None
    }
  }

  /**
   * @param appkey 应用key
   * @param env 环境
   * @param source 来源
   * @param group 分组
   * @param spanname 方法名
   * @param localHost 本地主机
   * @param remoteAppkey 远程appKey
   * @param remoteHost 远程主机
   * @return last data
   *
   * @see Falcon.statToFalconData
   */
  def lastData(appkey: String, env: String, source: String, group: String = StatGroup.Span.toString, spanname: String = Constants.ALL,
               localHost: String = Constants.ALL, remoteAppkey: String = Constants.ALL, remoteHost: String = Constants.ALL) = {
    //  将String转换为相应的enum
    val statEnv = StatEnv.getInstance(env)
    val statSource = TagQueryHandler.sourceToStatSource(source)
    val statGroup = StatGroup.getInstance(group)

    val metricPostfix =
      if (Constants.ALL.equalsIgnoreCase(localHost) && Constants.ALL.equalsIgnoreCase(remoteAppkey) && Constants.ALL.equalsIgnoreCase(remoteHost)) {
        spanname
      } else {
        //  根据相应的查询维度,将endpoint中以外的信息包含进后缀中,便以前端处理
        statGroup match {
          case StatGroup.Span => spanname
          case StatGroup.SpanLocalHost => s"${appkey}_$spanname"
          case StatGroup.SpanRemoteApp => s"${remoteAppkey}_$spanname"
          case StatGroup.SpanRemoteHost => s"${remoteHost}_$spanname"
          case StatGroup.LocalHostRemoteHost => s"${remoteHost}_$localHost"
          case StatGroup.LocalHostRemoteApp => s"${remoteAppkey}_$localHost"
          case StatGroup.RemoteAppRemoteHost => s"${remoteAppkey}_$remoteHost"
        }
      }
    val metricPrefix = s"${statEnv}_${statSource}_$metricPostfix"
    val tags = TreeMap(Constants.SPAN_NAME -> spanname, Constants.LOCAL_HOST -> localHost, Constants.REMOTE_APPKEY -> remoteAppkey, Constants.REMOTE_HOST -> remoteHost)
    val tagStr = tags.map(x => s"${x._1}=${x._2}").mkString(",")
    val param = FalconHistoryData.falconEndpointCounter(appkey, metricPrefix, tagStr)

    val optDataList = query(param)

    optDataList match {
      case Some(dataList) =>
        val ret = ResponseData(None, None, None, None, None, None, None)
        dataList.foreach {
          data =>
            val metric = data.counter.getOrElse("")
            metric match {
              case x if x.contains("_count/localHost=") =>
                ret.count = data.value
              case x if x.contains("_qps/localHost=") &&
                (x.contains(StatSource.ServerDrop.toString)
                  || x.contains(StatSource.ClientDrop.toString)
                  || x.contains(StatSource.RemoteClientDrop.toString)) =>
                ret.qps_drop = data.value
              case x if x.contains("_qps/localHost=") =>
                ret.qps = data.value
              case x if x.contains("_tp50/localHost=") =>
                ret.cost_50 = data.value
              case x if x.contains("_tp90/localHost=") =>
                ret.cost_90 = data.value
              case x if x.contains("_tp95/localHost=") =>
                ret.cost_95 = data.value
              case x if x.contains("_tp99/localHost=") =>
                ret.cost_99 = data.value
              case _ => //do nothing
            }
        }
        ret
      case None => ResponseData(None, None, None, None, None, None, None)
    }
  }

  def getAndResetReadCount() = {
    val count = lastReadCount.get()
    lastReadCount.set(0)
    count
  }

  def lastData(endpoints: java.util.List[String], counters: java.util.List[String]) = {
    val endpointCounters = endpoints.flatMap { x =>
      counters.map { y =>
        EndpointCounter(x, y)
      }
    }
    query(endpointCounters)
  }
}
