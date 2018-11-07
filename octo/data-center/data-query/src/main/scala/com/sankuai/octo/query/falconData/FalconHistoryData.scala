package com.sankuai.octo.query.falconData

import java.util.Locale
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

import com.sankuai.octo.query.TagQueryHandler
import com.sankuai.octo.query.helper.QueryCondition
import com.sankuai.octo.query.model.HistoryData.{DataRecord, Point, ResponseTag}
import com.sankuai.octo.statistic.constant.Constants
import com.sankuai.octo.statistic.helper.api
import com.sankuai.octo.statistic.model.{PerfProtocolType, StatEnv, StatGroup, StatSource}
import com.sankuai.octo.statistic.util.{ExecutionContextFactory, common}
import dispatch._
import org.joda.time.DateTime
import org.slf4j.{Logger, LoggerFactory}
import play.api.libs.json.Json

import scala.collection.immutable.TreeMap
import scala.concurrent.Await
import scala.concurrent.duration.Duration

object FalconHistoryData {
  val LOG: Logger = LoggerFactory.getLogger(FalconHistoryData.getClass)

  private implicit val ec = ExecutionContextFactory.build(Runtime.getRuntime.availableProcessors)

  val falconQueryHost = if (common.isOffline) {
    url("http://10.5.233.113:9966/graph/history")
  } else {
    url("http://query.falcon.vip.sankuai.com:9966/graph/history")
  }
  val postReq = falconQueryHost.POST
  val historyReadCount = new AtomicInteger(0)

  val time = Duration.create(15000, TimeUnit.MILLISECONDS)

  //counter格式为"metric/tags"
  case class EndpointCounter(endpoint: String, counter: String)

  case class DataSeries(timestamp: Option[Int], value: Option[Double])

  implicit val var1 = Json.reads[DataSeries]
  implicit val var2 = Json.writes[DataSeries]

  case class FalconDataResponse(Values: Option[List[DataSeries]], counter: Option[String], dstype: Option[String], endpoint: Option[String], step: Option[Int])

  implicit val var3 = Json.reads[FalconDataResponse]
  implicit val var4 = Json.writes[FalconDataResponse]

  /**
   * spanname、localhost、remoteAppkey、remoteHost只有一个为*
   * 对值为*的去查询tags并展开
    *
    * @param appkey    应用的appKey
    * @param env       环境的标识
    * @param start     秒为单位的起始时间
    * @param end       秒为单位的终止时间
    * @param role      数据来源
    * @param condition 聚合的查询条件
    * @param sortKey   排序的key,"qps","tp50","tp90","tp99"
   * @return  计算出来的运算结果
   */
  def historyData(appkey: String, env: String, start: Int, end: Int, role: String, condition: QueryCondition, sortKey: String) = {
    //  将String转换为相应的enum
    val statEnv = StatEnv.getInstance(env)
    val statSource = TagQueryHandler.sourceToStatSource(role)
    val statGroup = condition.statGroup

    val spanname = condition.spanname
    val localhost = condition.localhost
    val remoteHost = condition.remoteHost
    val remoteAppkey = condition.remoteAppkey
    val list = historyDataFromFalcon(appkey, start, end, statEnv, statSource, statGroup, spanname, localhost, remoteAppkey, remoteHost, includeDrop = true)
    val res = sortDataRecordsByKey(list, sortKey)
    res
  }


  private def falconParam(start: Long, end: Long, cf: String = "AVERAGE", endpointCounters: List[EndpointCounter]) = {
    Map(
      "start" -> start,
      "end" -> end,
      "cf" -> cf,
      "endpoint_counters" -> endpointCounters)
  }

  /**
   * spanname、localhost、remoteAppkey、remoteHost只有一个为*
   * 对值为*的去查询tags并展开
    *
    * @param appkey      应用的appKey
   * @param statEnv 环境的标识
   * @param start 秒为单位的起始时间
   * @param end 秒为单位的终止时间
   * @param statSource 数据来源
   * @param statGroup 数据分组
   * @param spanname 方法名
   * @param localHost 上报时的localhost
   * @param remoteAppkey 上报时的远程appKey
   * @param remoteHost 上报时的远程host
   * @return  计算出来的运算结果
   */
  def historyDataFromFalcon(appkey: String, start: Int, end: Int, statEnv: StatEnv, statSource: StatSource, statGroup: StatGroup, spanname: String = Constants.ALL,
                            localHost: String = Constants.ALL, remoteAppkey: String = Constants.ALL, remoteHost: String = Constants.ALL, includeDrop: Boolean = false) = {

    val tag = TagQueryHandler.tags(appkey, statEnv.toString, statSource.toString, (new DateTime(start * 1000L).minusDays(2).getMillis / 1000).toInt, end)

    val spannames = if (spanname == "*") tag.spannames else Set(spanname)
    val localHosts = if (localHost == "*") tag.localHosts else Set(localHost)
    val remoteAppkeys = if (remoteAppkey == "*") tag.remoteAppKeys else Set(remoteAppkey)
    val remoteHosts = if (remoteHost == "*") tag.remoteHosts else Set(remoteHost)

    //展开tag构造查询falcon查询所需endpoint、counter
    val endpointCountersWithTags = spannames.flatMap { spanname =>
      localHosts.flatMap { localHost =>
        remoteAppkeys.flatMap { remoteAppkey =>
          remoteHosts.map { remoteHost =>
            // 当localhost、remoteApp、remoteHost都为all时，从StatGroup.Span中查询
            val endpoint =
              if (Constants.ALL.equalsIgnoreCase(localHost) && Constants.ALL.equalsIgnoreCase(remoteAppkey) && Constants.ALL.equalsIgnoreCase(remoteHost)) {
                appkey
              } else {
                statGroup match {
                  case StatGroup.SpanLocalHost => localHost
                  case _ => appkey
                }
              }
            val metricPostfix =
              if (Constants.ALL.equalsIgnoreCase(localHost) && Constants.ALL.equalsIgnoreCase(remoteAppkey) && Constants.ALL.equalsIgnoreCase(remoteHost)) {
                spanname
              } else {
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
            // 将同一份tag的count、tp、qps请求放在一次http请求中
            val counterList = if (includeDrop) {
              val dropSource = statSource match {
                case StatSource.Server => StatSource.ServerDrop
                case StatSource.Client => StatSource.ClientDrop
                case StatSource.RemoteClient => StatSource.RemoteClientDrop
                case _ => StatSource.ServerDrop
              }
              val dropMetricPrefix = s"${statEnv}_${dropSource}_$metricPostfix"
              val dropQpsCounter = EndpointCounter(endpoint, s"${dropMetricPrefix}_qps/$tagStr")
              dropQpsCounter :: falconEndpointCounter(endpoint, metricPrefix, tagStr)
            } else {
              falconEndpointCounter(endpoint, metricPrefix, tagStr)
            }
            (counterList, tags)
          }
        }
      }
    }.toList

    val futureList = Future.traverse(endpointCountersWithTags) {
      x =>
        val endpointCounters = x._1
        val tags = x._2
        val dataTags = ResponseTag("thrift", statSource.toString.toLowerCase, null, statEnv.toString.toLowerCase, null, statGroup.toString,
          tags.get(Constants.SPAN_NAME), tags.get(Constants.LOCAL_HOST), tags.get(Constants.REMOTE_APPKEY), tags.get(Constants.REMOTE_HOST), Some(PerfProtocolType.THRIFT.toString))
        val params = falconParam(start, end, endpointCounters = endpointCounters)
        val data = query(params).recover { case e: Exception => Seq() }
        falconToDataRecord(appkey, data, statSource.toString.toLowerCase, dataTags)
    }

    val tmpList = futureList.map(result => result.filter(_.count.nonEmpty))
    try {
      Await.result(tmpList, time)
    } catch {
      case e: Exception =>
        LOG.error("historyDataFromFalcon fail", e)
        List()
    }

  }

  private def query(param: Map[String, Any]) = {
    val paramStr = api.jsonStr(param)
    historyReadCount.incrementAndGet()
    try {
      val future = Http(postReq << paramStr > as.String)
      future.map { result =>
        Json.parse(result).validate[Seq[FalconDataResponse]].fold({ error =>
          LOG.error(s"FalconDataResponse parse failed $error")
          Seq()
        }, {
          value => value
        })
      }
    } catch {
      case e: Exception => LOG.error(s"get data from falcon failed, param: $paramStr", e)
        Future {
          Seq()
        }
    }
  }

  //将falcon数据中tag相同的数据聚合在一起，即是同一指标的count、tp数据
  private def falconToDataRecord(appkey: String, data: Future[Seq[FalconDataResponse]], source: String, dataTags: ResponseTag) = {
    data.map { values =>
      var count, qps, tp50, tp90, tp99, dropQps = List[Point]()
      values.foreach { y =>
        val metric = y.counter.getOrElse("")
        metric match {
          case x if x.contains("_count/localHost=") =>
            count = convertResponseToPoints(y)
          case x if x.contains("_qps/localHost=") &&
            (x.contains(StatSource.ServerDrop.toString)
              || x.contains(StatSource.ClientDrop.toString)
              || x.contains(StatSource.RemoteClientDrop.toString)) =>
            dropQps = convertResponseToPoints(y)
          case x if x.contains("_qps/localHost=") =>
            qps = convertResponseToPoints(y)
          case x if x.contains("_tp50/localHost=") =>
            tp50 = convertResponseToPoints(y)
          case x if x.contains("_tp90/localHost=") =>
            tp90 = convertResponseToPoints(y)
          case x if x.contains("_tp99/localHost=") =>
            tp99 = convertResponseToPoints(y)
          case _ => //do nothing
        }
      }
      DataRecord(appkey, dataTags, count, qps = qps, tp50 = tp50, tp90 = tp90, tp99 = tp99, dropQps = dropQps)
    }
  }

  /**
   *
   * @param list 待排序的list
   * @param sortKey 排序的key
   * @return 排序后的list
   */
  private def sortDataRecordsByKey(list: List[DataRecord], sortKey: String) = {
    val retList = sortKey match {
      case "qps" => list.sortBy(-_.qps.map(_.y.getOrElse(0.0)).sum)
      case "tp50" => list.sortBy(-_.tp50.map(_.y.getOrElse(0.0)).sum)
      case "tp90" => list.sortBy(-_.tp90.map(_.y.getOrElse(0.0)).sum)
      case "tp99" => list.sortBy(-_.tp99.map(_.y.getOrElse(0.0)).sum)
      case _ => list.sortBy(-_.qps.map(_.y.getOrElse(0.0)).sum)
    }
    retList
  }

  def falconEndpointCounter(endpoint: String, metricPrefix: String, tags: String) = {
    val count = EndpointCounter(endpoint, s"${metricPrefix}_count/$tags")
    val qps = EndpointCounter(endpoint, s"${metricPrefix}_qps/$tags")
    val tp50 = EndpointCounter(endpoint, s"${metricPrefix}_tp50/$tags")
    val tp90 = EndpointCounter(endpoint, s"${metricPrefix}_tp90/$tags")
    val tp95 = EndpointCounter(endpoint, s"${metricPrefix}_tp95/$tags")
    val tp99 = EndpointCounter(endpoint, s"${metricPrefix}_tp99/$tags")
    List(count, qps, tp50, tp90, tp95, tp99)
  }

  private def convertResponseToPoints(response: FalconDataResponse) = {
    response.Values.getOrElse(List()).map { m =>
      Point(Some(new DateTime(m.timestamp.getOrElse(0) * 1000L).toString("MM-dd HH:mm, EEE", Locale.US)), m.value, m.timestamp)
    }
  }

  def getAndResetReadCount() = {
    val count = historyReadCount.get()
    historyReadCount.set(0)
    LOG.info(s"count is $count")
    count
  }
}
