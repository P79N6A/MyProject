package com.sankuai.octo.msgp.serivce.data

import com.sankuai.octo.msgp.utils.client.SquirrelClient
import org.slf4j.LoggerFactory
import play.api.libs.json.{Json, Reads, Writes}

object PortraitService {
  private val LOG = LoggerFactory.getLogger(this.getClass)

  private val qpsCategory = "portrait-qps"
  private val qpsMaxKey = "qpsMax"
  private val qpsMinKey = "qpsMin"
  private val qpsMaxPeaksKey = "qpsMaxPeaks"
  private val qpsMinPeaksKey = "qpsMinPeaks"
  private val qpsMatchingKey = "qpsMatching"
  private val peakHourKey = "peakHour"

  case class MatchingQPS(time: String, qps: Double)
  implicit val r: Reads[MatchingQPS] = Json.reads[MatchingQPS]
  implicit val w: Writes[MatchingQPS] = Json.writes[MatchingQPS]

  case class Load(time: String, load1min: Double)
  implicit val loadRead = Json.reads[Load]
  implicit val loadWrite = Json.writes[Load]

  case class extendTags(appkey: String, tags: Map[String, String])
  implicit val extendTagsRead = Json.reads[extendTags]
  implicit val extendTagsWrite = Json.writes[extendTags]

  case class propertyTags(appkey: String, tags: Map[String, String])
  implicit val propertyTagsRead = Json.reads[propertyTags]
  implicit val propertyTagsWrite = Json.writes[propertyTags]

  case class propertyTagsSome(appkey: String, tags: Map[String, String])
  implicit val propertyTagsSomeRead = Json.reads[propertyTagsSome]
  implicit val propertyTagsSomeWrite = Json.writes[propertyTagsSome]

  case class resourceTagsSome(appkey: String, tags: Map[String, String])
  implicit val resourceTagsSomeRead = Json.reads[resourceTagsSome]
  implicit val resourceTagsSomeWrite = Json.writes[resourceTagsSome]

  case class QPS(mm: Int, qps: Double)
  implicit val QPSRead = Json.reads[QPS]
  implicit val QPSWrite = Json.writes[QPS]


  def qpsPortrait(appkey: String, tag: String = "") = {
    val realQPSMatchingKey = "qps5Minutes" //qpsMatchingKey
    val realPeakHourKey = peakHourKey
    val matchingQps = SquirrelClient.hget(qpsCategory, realQPSMatchingKey, getQpsPortraitKey(appkey): _*)
    val peakHour = SquirrelClient.hget(qpsCategory, realPeakHourKey, getQpsPortraitKey(appkey): _*)
    LOG.info(s"matchingQps is $matchingQps")

    try {
      Json.parse(matchingQps).validate[List[MatchingQPS]].asOpt match {
        case None => Map()
        case Some(m) =>
          Json.parse(peakHour).validate[List[List[Int]]].asOpt match {
            case None => Map()
            case Some(p) =>
              Map("matchingQps" -> Map("xAxis" -> m.map(_.time), "series" -> m.map(_.qps)),
                "markZone" -> p)
          }
      }
    } catch {
      case e: Exception => LOG.error("qpsPortrait failed", e)
        Map()
    }
  }

  def extendTag(appkeys: List[String]) = {
    appkeys.foldLeft(List[extendTags]()) { (result, appkey) =>
      val value = SquirrelClient.hgetAll("extend", getQpsPortraitKey(appkey): _*)
      LOG.info(s"${appkey} extendTag is ${value}")
      result :+ extendTags(appkey, value)
    }
  }

//性能方面图片数据
  def propertyTagSome(appkeys: List[String]) = {
    // 获取性能展示数据
    appkeys.foldLeft(List[propertyTagsSome]()) { (result, appkey) =>
      val valuemax = SquirrelClient.hget("portrait-qps", "qpsMax", getQpsPortraitKey(appkey): _*)
      val valuemin = SquirrelClient.hget("portrait-qps", "qpsMin", getQpsPortraitKey(appkey): _*)
      val peakhour = SquirrelClient.hget("portrait-qps", "peakHour", getQpsPortraitKey(appkey): _*)
      result :+ propertyTagsSome(appkey, Map(("qpsMax"->valuemax), "qpsMin"->valuemin, "peakhour"->peakhour))
    }
  }

  def resourceTagSome(appkeys: List[String]) = {
    appkeys.foldLeft(List[resourceTagsSome]()) { (result, appkey) =>
      val value = SquirrelClient.hget("portrait-qps", "load.1minPerCPU", getQpsPortraitKey(appkey): _*)
      result :+ resourceTagsSome(appkey, Map("value"->value))
    }
  }

  def resourceTagOthers(appkeys: List[String]) = {
    appkeys.foldLeft(List[resourceTagsSome]()) { (result, appkey) =>
      val load = SquirrelClient.hget("portrait-qps", "load.1minPerCPU", getQpsPortraitKey(appkey): _*)
      val net_in = SquirrelClient.hget("portrait-qps", "net.if.in.bytes/iface=eth0", getQpsPortraitKey(appkey): _*)
      val net_out = SquirrelClient.hget("portrait-qps", "net.if.out.bytes/iface=eth0", getQpsPortraitKey(appkey): _*)
      val net_total = SquirrelClient.hget("portrait-qps", "net.if.total.bytes/iface=eth0", getQpsPortraitKey(appkey): _*)
      val jvm_count = SquirrelClient.hget("portrait-qps", "jvm.thread.count", getQpsPortraitKey(appkey): _*)
      val jvm_running = SquirrelClient.hget("portrait-qps", "jvm.thread.runnable.count", getQpsPortraitKey(appkey): _*)
      result :+ resourceTagsSome(appkey, Map("load"->load, "net_in"->net_in,
        "net_out"->net_out, "net_total"->net_total, "jvm_count"->jvm_count,  "jvm_running"->jvm_running))
    }
  }

  def propertyQpsPicAllData(appkey: String): Map[String, List[Any]] = {
    // 优化后，只需要维护metricMap
    val metricMap = Map(
      "series" -> "qps5Minutes",
      "cq_series" -> "CQqps5Minutes",
      "dx_series" -> "DXqps5Minutes",
      "yf_series" -> "YFqps5Minutes",
      "gq_series" -> "GQqps5Minutes",
      "gh_series" -> "GHqps5Minutes"
    )

    var resourceMap =  metricMap.map{ case (k, v) => k -> SquirrelClient.hget(qpsCategory, v, getQpsPortraitKey(appkey): _*)}
    var A : Map[String, List[Any]] = Map()

    var flag:Int = 0;
    resourceMap.keys.foreach( i=>
      if (resourceMap(i) != null){
        flag = 1;
      }
    )

    if(flag==0){
      A
    }else {
      resourceMap.keys.foreach(i =>
        if (resourceMap(i) == null) {
          resourceMap -= (i)
        }
      )
      val loadMap = resourceMap.flatMap { case (key, resc) =>
        //resc 需要判断后再处理
        Json.parse(resc).validate[List[QPS]].asOpt match {
          case None => None
          case Some(m) => Some(key -> m)
        }
      }
      Map("xAxis" -> loadMap.values.map(_.map(_.mm)).head) ++ loadMap.map { case (k, v) => k -> v.map(_.qps) }
    }
  }

//  资源load图片
  def resourceLoadPicAllData(appkey: String): Map[String, List[Any]] = {
    // 优化后，只需要维护metricMap
    val metricMap = Map(
      "series" -> "all_load.1min_series",
      "cq_series" -> "cq_load.1min_series",
      "dx_series" -> "dx_load.1min_series",
      "yf_series" -> "yf_load.1min_series",
      "gq_series" -> "gq_load.1min_series",
      "gh_series" -> "gh_load.1min_series"
    )

    var resourceMap =  metricMap.map{ case (k, v) => k -> SquirrelClient.hget(qpsCategory, v, getQpsPortraitKey(appkey): _*)}
    var A : Map[String, List[Any]] = Map()

    var flag:Int = 0;
    resourceMap.keys.foreach( i=>
      if (resourceMap(i) != null){
        flag = 1;
      }
    )

    if(flag==0){
      A
    }else {
      resourceMap.keys.foreach(i =>
        if (resourceMap(i) == null) {
          resourceMap -= (i)
        }
      )
      val loadMap = resourceMap.flatMap { case (key, resc) =>
        //resc 需要判断后再处理
        Json.parse(resc).validate[List[Load]].asOpt match {
          case None => None
          case Some(m) => Some(key -> m)
        }
      }
      Map("xAxis" -> loadMap.values.map(_.map(_.time)).head) ++ loadMap.map { case (k, v) => k -> v.map(_.load1min) }
    }
  }

  def resourcesPortrait(appkey: String) = {
    val resource = SquirrelClient.hget(qpsCategory, "matching_load.1min", getQpsPortraitKey(appkey): _*)
    val dx_resource = SquirrelClient.hget(qpsCategory, "matching_dx_load.1min", getQpsPortraitKey(appkey): _*)
    try {
      Json.parse(resource).validate[List[Load]].asOpt match {
        case None => Map()
        case Some(m) =>
          Json.parse(dx_resource).validate[List[Load]].asOpt match {
            case None => Map()
            case Some(p) =>
              Map("xAxis" -> m.map(_.time),
                "series" -> m.map(_.load1min),
                "dx_series" -> p.map(_.load1min))
          }
      }
    } catch {
      case e: Exception => LOG.error("resource tag failed", e)
        Map()
    }
  }

  def changeFormatTestApi() = {
    // 为了展示数据时间序列图（测试tair导入数据是否成功，观察趋势）
    // 修改不同的input，在前端展示不同的数据。当前样例数据为 com.meituan.pic.imageproc.start	的5分钟粒度的部分时间基线值
    val input_all : String = "[{\"mm\":0,\"qps\":160.31},{\"mm\":5,\"qps\":164.52},{\"mm\":10,\"qps\":162.87}]"
    val input_dx : String = "[{\"mm\":0,\"qps\":102.15299065351486},{\"mm\":5,\"qps\":104.83569348335267},{\"mm\":10,\"qps\":103.78427788496018}]"
    val input_yf : String = "[{\"mm\":0,\"qps\":58.15700934648514},{\"mm\":5,\"qps\":59.68430651664734},{\"mm\":10,\"qps\":59.08572211503983}]"
    Json.parse(input_all).validate[List[QPS]].asOpt match {
      case None => Map()
      case Some(all) =>
        Json.parse(input_dx).validate[List[QPS]].asOpt match {
          case None => Map(
            "xAxis" -> all.map(_.mm),
            "series" -> all.map(_.qps)
          )
          case Some(dx) =>
            Json.parse(input_yf).validate[List[QPS]].asOpt match {
              case None => Map(
                "xAxis" -> all.map(_.mm),
                "series" -> all.map(_.qps),
                "dx_series" -> dx.map(_.qps)
              )
              case Some(yf) =>
                Map(
                  "xAxis" -> all.map(_.mm),
                  "series" -> all.map(_.qps),
                  "dx_series" -> dx.map(_.qps),
                  "yf_series" -> yf.map(_.qps)
                )
            }
        }
    }
  }

  private def getQpsPortraitKey(appkey: String, env: String = "prod") = {
    List(appkey, env, null)
  }
}
