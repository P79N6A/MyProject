package com.sankuai.octo.msgp.serivce.graph

import java.util.concurrent.{Executors, TimeUnit}

import com.sankuai.msgp.common.model.{Env, ServiceModels}
import com.sankuai.msgp.common.service.org.OpsService
import com.sankuai.msgp.common.utils.client.TairClient
import com.sankuai.msgp.common.utils.helper.{CommonHelper, JsonHelper}
import com.sankuai.octo.msgp.model.Appkeys
import com.sankuai.octo.msgp.serivce.data.DataQuery
import com.sankuai.octo.msgp.serivce.data.DataQuery.DataSeries
import com.sankuai.octo.msgp.serivce.graph.ServiceModel._
import com.sankuai.octo.msgp.serivce.graph.ViewDefine.Graph
import com.sankuai.octo.msgp.serivce.service
import com.sankuai.octo.msgp.serivce.service.AppkeyProviderService
import org.apache.commons.lang3.StringUtils
import org.joda.time.{DateTime, LocalDate}
import org.slf4j.{Logger, LoggerFactory}
import play.api.libs.json.Json

import scala.collection.JavaConverters._
import scala.collection.parallel.ForkJoinTaskSupport

object ViewCache {
  private val LOG: Logger = LoggerFactory.getLogger(ViewCache.getClass)

  val scheduler = Executors.newScheduledThreadPool(3)
  private val refreshThreadPool = new ForkJoinTaskSupport(new scala.concurrent.forkjoin.ForkJoinPool(4))

  val gap = 60l
  var listService = service.ServiceCommon.listService

  def start() {
    val now = System.currentTimeMillis() / 1000
    val init = gap - (now % gap)
    LOG.info(s"start cache loop $init $gap")
    scheduler.scheduleWithFixedDelay(new Runnable {
      def run(): Unit = {
        listService = service.ServiceCommon.listService
      }
    }, 0, gap, TimeUnit.SECONDS)

    scheduler.scheduleWithFixedDelay(new Runnable {
      def run(): Unit = {
        try {
          LOG.info(s"refresh all service 1m data")
          refreshAppNodeCache
        } catch {
          case e: Exception => LOG.error(s"refresh ServiceCache fail", e)
        }
      }
    }, init, gap, TimeUnit.SECONDS)

    scheduler.scheduleAtFixedRate(new Runnable {
      def run(): Unit = {
        try {
          LOG.info(s"refresh newSpannames")
          newSpannames
          LOG.info(s"refresh perfWorst")
          perfWorst
        } catch {
          case e: Exception => LOG.error(s"refresh ServiceCache fail ${e.getMessage}", e)
        }
      }
    }, init, gap * 12, TimeUnit.MINUTES)
  }

  def main(args: Array[String]): Unit = {
    refreshAppNodeCache()
  }

  def refreshAppNodeCache() {
    try {
      val services = listService.filter { x =>
        x.appkey != Appkeys.sgagent.toString && x.appkey != Appkeys.kmsagent.toString
      }
      val servicePar = services.par
      servicePar.tasksupport = refreshThreadPool
      servicePar.foreach {
        x =>
          try {
            LOG.debug(s"begin refresh IDC appkey ${x.appkey}")
            buildAppNodeByIDC(x)
          } catch {
            case e: Exception =>
              LOG.error(s"refresh $x fail", e)
          }
      }

      val graphs = ViewDefine.Graph.values.par
      graphs.tasksupport = refreshThreadPool
      graphs.foreach {
        x =>
          try {
            updateGraphDataIDC(x.id, services)
          } catch {
            case e: Exception =>
              LOG.error(s"refresh graph $x fail", e)
          }
      }
    } catch {
      case e: Exception =>
        LOG.error(s"refresh failed", e)
    }
  }


  /**
    * @param graphId
    * 根据 graphId获取对应的服务
    * @return
    */
  def updateGraphDataIDC(graphId: Int, services: List[ServiceModels.Desc]) = {
    try {
      // 从AppGraph中将所有符合条件的appkey、x、y检索出来
      val appGraphMap = ServiceLocation.getAppGraph(graphId).map(self => self.appkey -> (self.x, self.y)).toMap
      val graphData = services.filter(ViewDefine.isInGraph(graphId, _))
      val nodes = graphData.flatMap {
        desc =>
          val nodeOpt = getAppNodeIDC(desc.appkey)
          if (nodeOpt.nonEmpty) {
            val node = nodeOpt.get
            val appAxis = appGraphMap.applyOrElse(desc.appkey, Map(node.name -> (0, 0)))
            val idcList = (node.idcInMap.map(_._1).toList ::: node.idcOutMap.map(_._1).toList).distinct
            idcList.map {
              item =>
                val currentIn = node.idcInMap.getOrElse(item, List[AppCall]())
                val currentOut = node.idcOutMap.getOrElse(item, List[AppCall]())
                val hosts = node.hosts.getOrElse(item, Map())
                val appNode = AppNode(desc.appkey, desc.business.getOrElse(100), desc.level.getOrElse(0), hosts, currentIn, currentOut, appAxis._1, appAxis._2)
                (item, appNode)
            }
          } else {
            None
          }
      }.groupBy(_._1)
      val serviceMap = services.map(x => (x.appkey, x)).toMap
      val connectServices = scala.collection.mutable.HashSet[String]()
      val outNodes = (connectServices & serviceMap.keys.toSet).filterNot {
        x =>
          ViewDefine.isInGraph(graphId, serviceMap.apply(x))
      }.map {
        key =>
          val service = serviceMap.apply(key)
          val appAxis = appGraphMap.applyOrElse(key, Map(key -> (0, 0)))
          AppNode(service.appkey, service.business.getOrElse(100), service.level.getOrElse(0), Map(), List(), List(), appAxis._1, appAxis._2)
      }.toList

      val unknownNodes = (connectServices &~ serviceMap.keys.toSet).map {
        key =>
          val appAxis = appGraphMap.applyOrElse(key, Map(key -> (0, 0)))
          AppNode(key, 100, 0, Map(), List(), List(), appAxis._1, appAxis._2)
      }.toList
      LOG.info(s"graph unknownNodes ${unknownNodes.size}")

      nodes.foreach {
        item =>
          val graphData = GraphData(item._2.map(_._2), outNodes, unknownNodes)
          val key = graphKeyIDC(graphId, item._1)
          TairClient.put(key, graphData)
          LOG.debug(s"key $key,graphData$graphData")
      }
    } catch {
      case e: Exception => LOG.error(s"fail updateGraphDataIDC ${graphId} ", e); None
    }
  }


  def buildGraphData(graphId: Int) = {
    try {
      // 从AppGraph中将所有符合条件的appkey、x、y检索出来
      val appGraphMap = ServiceLocation.getAppGraph(graphId).map(self => self.appkey -> (self.x, self.y)).toMap
      LOG.info(s"graph node ${appGraphMap.size}")
      val serviceMap = listService.map(x => (x.appkey, x)).toMap
      val connectServices = scala.collection.mutable.HashSet[String]()

      val nodes = listService.filter(ViewDefine.isInGraph(graphId, _)).flatMap {
        desc =>
          val node = getAppNode(desc.appkey)
          node.map {
            self =>
              val appAxis = appGraphMap.applyOrElse(desc.appkey, Map(self.name -> (0, 0)))
              self.copy(x = appAxis._1, y = appAxis._2)
          }
      }
      LOG.info(s"graph nodes ${nodes.size}")

      val outNodes = (connectServices & serviceMap.keys.toSet).filterNot {
        x =>
          ViewDefine.isInGraph(graphId, serviceMap.apply(x))
      }.map {
        key =>
          val service = serviceMap.apply(key)
          val appAxis = appGraphMap.applyOrElse(key, Map(key -> (0, 0)))
          AppNode(service.appkey, service.business.getOrElse(100), service.level.getOrElse(0), Map(), List(), List(), appAxis._1, appAxis._2)
      }.toList
      LOG.info(s"graph outNodes ${outNodes.size}")

      val unknownNodes = (connectServices &~ serviceMap.keys.toSet).map {
        key =>
          val appAxis = appGraphMap.applyOrElse(key, Map(key -> (0, 0)))
          AppNode(key, 100, 0, Map(), List(), List(), appAxis._1, appAxis._2)
      }.toList
      LOG.info(s"graph unknownNodes ${unknownNodes.size}")

      val graphData = GraphData(nodes, outNodes, unknownNodes)
      LOG.info(s"finish graph data for $graphId")
      Some(graphData)
    } catch {
      case e: Exception => LOG.error(s"fail buildGraphData $graphId", e); None
    }
  }


  def newSpannames() = {
    val startTime = new LocalDate().toDateTimeAtStartOfDay
    Graph.values.foreach {
      x =>
        val data = createNewSpanname(x.id, startTime, 2)
        val key = graphnewspanname(x.id)
        val jsonData = JsonHelper.jsonStr(data)
        TairClient.put(key, jsonData, 86400)
    }
  }

  def createNewSpanname(id: Int, startTime: DateTime, days: Int) = {
    val allApps = listService.filter(x => ViewDefine.isInGraph(id, x)).distinct
    val end = (startTime.getMillis / 1000).toInt
    val start = (startTime.minusDays(days).getMillis / 1000).toInt
    val origin = (startTime.minusDays(days + 1).getMillis / 1000).toInt
    val env = Env.prod.toString
    val source = "server"
    val group = "span"
    val appPar = allApps.par
    appPar.tasksupport = refreshThreadPool
    val ret = appPar.flatMap { x =>
      val appkey = x.appkey
      val recentlySpans = DataQuery.tags(appkey, start, end, env, source).spannames.filter(_ != "all")
      val originSpans = DataQuery.tags(appkey, origin, origin + 1, env, source).spannames.filter(_ != "all")

      val spannames = if (originSpans.isEmpty) {
        // 新增的app，发现接口增加过多，暂时去掉
        List()
      } else {
        // 新增的接口
        recentlySpans diff originSpans
      }
      spannames.map { spanname =>
        val lastData = DataQuery.lastData(appkey, env, source, spanname, group)
        lastData match {
          case Some(data) =>
            ServiceModel.AppPerf(appkey, spanname, data.count.getOrElse(DataSeries(0, 0)).value.toLong, data.qps.getOrElse(DataSeries(0, 0)).value, data.cost_50.getOrElse(DataSeries(0, 0)).value,
              data.cost_90.getOrElse(DataSeries(0, 0)).value, data.cost_95.getOrElse(DataSeries(0, 0)).value, data.cost_99.getOrElse(DataSeries(0, 0)).value)
          case None =>
            ServiceModel.AppPerf(appkey, spanname, 0, 0.0, 0.0, 0.0, 0.0, 0.0)
        }
      }
    }.toList
    ret
  }

  def getNewSpannames(id: Int) = {
    val key = graphnewspanname(id)
    val result = TairClient.get(key)
    result.flatMap {
      text =>
        Json.parse(text).validate[List[AppPerf]].asOpt
    }
  }

  def perfWorst() = {
    val startTime = new DateTime().minusDays(1).withTimeAtStartOfDay()
    Graph.values.foreach {
      x =>
        val data = perfWorset(x.id, startTime)
        val key = graphperfWorst(x.id)
        val jsonData = JsonHelper.jsonStr(data)
        TairClient.put(key, jsonData, 86400)
    }
  }

  def getperfWorst(id: Int) = {
    val key = graphperfWorst(id)
    val result = TairClient.get(key)
    result.flatMap {
      text =>
        Json.parse(text).validate[List[AppPerf]].asOpt
    }
  }

  def perfWorset(id: Int, startTime: DateTime) = {
    val allApps = listService.filter(x => ViewDefine.isInGraph(id, x))
    val env = Env.prod.toString
    val allAppsPar = allApps.par
    allAppsPar.tasksupport = refreshThreadPool
    val data = allAppsPar.flatMap { x =>
      DataQuery.getDailyStatistic(x.appkey, env, startTime)
    }.flatMap(x => x.asScala.toList).toList
    data.filterNot(x => x.getTags.getSpanname.equalsIgnoreCase("all"))
      .sortBy(_.getCost90)
      .slice(0, 100)
      .map { x =>
        val spananme = x.getTags.getSpanname
        val spannameFixed = if (StringUtils.isBlank(spananme)) "unknownSpan" else spananme
        ServiceModel.AppPerf(x.getAppkey, spannameFixed, x.getCount, x.getQps, x.getCost50, x.getCost90, x.getCost95, x.getCost99)
      }
  }

  def getAppNodeIDC(appkey: String) = {
    val result = TairClient.get(appNodeKeyIDC(appkey))
    result.flatMap {
      text =>
        Json.parse(text).validate[AppNodeIDC].asOpt
    }
  }

  def getAppNode(appkey: String) = {
    val result = TairClient.get(appNodeKey(appkey))
    result.flatMap {
      text =>
        Json.parse(text).validate[AppNode].asOpt
    }
  }

  def updateAppNode(appkey: String, node: AppNode) = {
    TairClient.put(appNodeKey(appkey), node)
  }

  def updateAppNodeIDC(appkey: String, node: AppNodeIDC) = {
    TairClient.put(appNodeKeyIDC(appkey), node)
  }


  def getGraphDataIDC(graphId: Int, idc: String) = {
    val result = TairClient.get(graphKeyIDC(graphId, idc))
    result.flatMap {
      text =>
        Json.parse(text).validate[GraphData].asOpt
    }
  }

  def updateGraphData(graphId: Int, graph: GraphData) = {
    TairClient.put(graphKey(graphId), graph)
  }

  def buildAppNodeByIDC(desc: ServiceModels.Desc) = {
    val allHosts = AppkeyProviderService.providerNode(desc.appkey, Env.prod.toString).distinct.map {
      host =>
        if (host.contains(":")) {
          host.split(":").apply(0)
        } else {
          host
        }
    }
    val idcMap = allHosts.groupBy(CommonHelper.ip2IDC).+("all" -> allHosts)

    val hosts = idcMap.map {
      idc =>
        val ipList = idc._2
        val colorMap = ipList.map(OpsService.ipToHost).map(getLoad).
          groupBy(Color.hostColor).map(x => (x._1.toString, x._2.length))
        idc._1 -> colorMap
    }
    val inAppCalls = DataQuery.getAppCallIDCFromDataCenter(desc.appkey, "server")
    val outAppCalls = DataQuery.getAppCallIDCFromDataCenter(desc.appkey, "client")
    // 坐标默认设为0，后根据Graph做reset
    val nodeIDC = AppNodeIDC(desc.appkey, desc.business.getOrElse(100), desc.level.getOrElse(0), hosts, inAppCalls, outAppCalls, 0, 0)
    updateAppNodeIDC(desc.appkey, nodeIDC)
  }

  def getLoad(hostname: String) = {
    0.0
  }

  def appNodeKey(appkey: String) = {
    s"graph.appnode.$appkey"
  }

  def appNodeKeyIDC(appkey: String) = {
    s"idc.graph.appnode.$appkey"
  }

  def graphKeyIDC(graphId: Int, idc: String) = {
    s"idc.graph.$graphId.$idc"
  }

  def graphKey(graphId: Int) = {
    s"graph.$graphId"
  }

  def graphnewspanname(id: Int) = {
    s"graph.newspanname.$id"
  }

  def graphperfWorst(id: Int) = {
    s"graph.perfWorst.$id"
  }
}
