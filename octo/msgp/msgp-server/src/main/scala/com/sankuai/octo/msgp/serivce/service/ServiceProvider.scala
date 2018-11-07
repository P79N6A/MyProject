package com.sankuai.octo.msgp.serivce.service

import java.util.Date
import java.util.concurrent.{Executors, TimeUnit}

import com.google.common.cache.{CacheBuilder, CacheLoader}
import com.sankuai.msgp.common.model.{Env, Page, Path, ServiceModels}
import com.sankuai.msgp.common.utils.helper.{CommonHelper, HttpHelper}
import com.sankuai.octo.msgp.dao.appkey.AppkeyProviderDao
import com.sankuai.octo.msgp.domain.IdcIpprefix
import com.sankuai.octo.msgp.model.IdcName
import com.sankuai.octo.msgp.utils.client.{MnsCacheClient, ZkClient}
import com.sankuai.octo.mworth.util.DateTimeUtil
import dispatch.url
import org.slf4j.{Logger, LoggerFactory}
import play.api.libs.json.Json

import scala.collection.JavaConverters._
import scala.concurrent.ExecutionContext

object ServiceProvider {
  val LOG: Logger = LoggerFactory.getLogger(ServiceProvider.getClass)
  implicit val ec = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(8))

  case class idcNode(idc: String, nodeList: List[ServiceModels.ProviderNode])

  case class statusNode(status: Int, nodeList: List[ServiceModels.ProviderNode])

  case class providerOutline(idcList: List[String], iNode: List[idcNode], statusList: List[Int], sNode: List[statusNode])

  case class providerOutlineSimple(idcList: List[String], idcCount: List[Int], hostCount: List[Int], statusList: List[Int], statusCount: List[Int])

  private var ipListCache: Option[Map[String, List[String]]] = None

  val fourDecimalFormatter = "%.4f"

  /**
   * 返回provider的概要信息
   *
   * @param appkey
   * @param thriftHttp
   * @param envId
   * @param ip
   * @param status
   * @param page
   * @param sort
   * @return
   */
  def getOutlineOfProvider(appkey: String, thriftHttp: Int, envId: Int, ip: String, status: Int, page: Page, sort: Int = -8) = {
    val providerNodeList = AppkeyProviderService.getProviderByType(appkey, thriftHttp, Env(envId).toString, ip, status, page, sort)

    val idcHostPortCountMap = scala.collection.mutable.Map[String, Int]()
    val idcHostCountMap = scala.collection.mutable.Map[String, scala.collection.mutable.Set[String]]()
    val statusCountMap = scala.collection.mutable.Map[Int, Int]()
    providerNodeList.foreach { node =>
      val idcName = IdcName.getNameByIdc(CommonHelper.ip2IDC(node.ip))
      val status = node.status
      idcHostPortCountMap.put(idcName, idcHostPortCountMap.getOrElseUpdate(idcName, 0) + 1)
      statusCountMap.put(status, statusCountMap.getOrElseUpdate(status, 0) + 1)

      val hostSet = idcHostCountMap.getOrElseUpdate(idcName, scala.collection.mutable.Set())
      hostSet.add(node.ip)
      idcHostCountMap.put(idcName, hostSet)
    }

    val idcList = idcHostPortCountMap.keys.toList
    val idcCount = idcList.map(idc => idcHostPortCountMap.getOrElse(idc, 0))
    val hostCount = idcHostCountMap.keys.toList.map(idc => idcHostCountMap.getOrElse(idc, scala.collection.mutable.Set()).size)
    val statusList = statusCountMap.keys.toList
    val statusCount = statusList.map(stat => statusCountMap.getOrElse(stat, 0))

    providerOutlineSimple(idcList, idcCount, hostCount, statusList, statusCount)
  }

  /**
   *
   * @param appkey
   * @param thriftHttp
   * @param envId
   * @param ip
   * @param status
   * @param page
   * @param sort
   */
  def getIPListofProvider(appkey: String, thriftHttp: Int, envId: Int, ip: String, status: Int, page: Page, sort: Int = -8) = {
    val providerNodeList = AppkeyProviderService.getProviderByType(appkey, thriftHttp, Env(envId).toString, ip, status, page, sort)
    providerNodeList.map(_.ip).distinct
  }

  /**
   * 按照机房返回provider
   *
   * @param appkey
   * @param thriftHttp
   * @param envId
   * @param ip
   * @param status
   * @param page
   * @param sort
   * @param idcName
   * @return
   */
  def getProviderByIDC(appkey: String, thriftHttp: Int, envId: Int, ip: String, status: Int, page: Page, sort: Int = -8, idcName: String) = {
    val pageTemp = new Page()
    pageTemp.setPageSize(-1)
    val idc = IdcName.getIdcByName(idcName)
    val providerNodeList = AppkeyProviderService.getProviderByType(appkey, thriftHttp, Env(envId).toString, ip, status, pageTemp, sort)
    val list = if (idc == "OTHER") {
      val knownIdc = IdcName.idcNameMap.keys.toList.filter(_ != "OTHER")
      providerNodeList.filter {
        x =>
          !knownIdc.contains(CommonHelper.ip2IDC(x.ip))
      }
    } else {
      providerNodeList.filter {
        x =>
          CommonHelper.ip2IDC(x.ip).equals(idc)
      }
    }
    page.setTotalCount(list.length)
    list.slice(page.getStart, page.getStart + page.getPageSize)
  }

  /**
   * 获取机房IDC
   */
  def getServiceIdc(appkey: String, protocalType: Int) = {
    val envId = 3
    val providerNodeList = AppkeyProviderService.provider(appkey,envId,Path.provider)
    val httpProviderNodeList = AppkeyProviderService.provider(appkey,envId,Path.providerHttp)
    val nodeList = providerNodeList:::httpProviderNodeList
    val list = nodeList.filter(_.status==2).map {
      x =>
        val idc = CommonHelper.ip2IDC(x.ip)
        val ip_arr = x.ip.split("\\.")
        val prefix = s"${ip_arr(0)}.${ip_arr(1)}.*"
        new IdcIpprefix(idc,prefix)
    }
    list.toSet
  }

  def getOneProvider(appkey: String, envId: Int) = {
    val page = new Page()
    val providerList = getProviderByStatus(appkey, 2, envId, "", 2, page)
    var result = ""
    if(providerList.nonEmpty) {
      val firstProvider = providerList.head
      result = firstProvider.ip + ":" + firstProvider.port
      }
    result
  }

  def getProviderPort(appkey: String, envId: Int) = {
    val page = new Page()
    var httpPort = 0
    val providerList = getProviderByStatus(appkey, 2, envId, "", 2, page)
    if(providerList.isEmpty) {
      val providerDeadList = getProviderByStatus(appkey, 2, envId, "", 0, page)
      if(providerDeadList.isEmpty) {
        val providerStoppedList = getProviderByStatus(appkey, 2, envId, "", 4, page)
        if(providerStoppedList.isEmpty) {
          val providerStattingList = getProviderByStatus(appkey, 2, envId, "", 1, page)
          if(providerStattingList.nonEmpty) {
            httpPort = providerStattingList.head.port
          }
        } else {
          httpPort = providerStoppedList.head.port
        }
      } else {
        httpPort = providerDeadList.head.port
      }
    } else {
      httpPort = providerList.head.port
    }
    httpPort
  }
  /**
   * 按照状态返回provider
   *
   * @param appkey
   * @param thriftHttp
   * @param envId
   * @param ip
   * @param status
   * @param page
   * @param sort
   * @return
   */
  def getProviderByStatus(appkey: String, thriftHttp: Int, envId: Int, ip: String, status: Int, page: Page, sort: Int = -8) = {
    val pageTemp = new Page()
    pageTemp.setPageSize(-1)
    val providerNodeList = AppkeyProviderService.getProviderByType(appkey, thriftHttp, Env(envId).toString, ip, status, pageTemp, sort)
    val list = providerNodeList.filter(_.status == status)
    page.setTotalCount(list.length)
    list.slice(page.getStart, page.getStart + page.getPageSize)
  }

  val expiredTime = 60l
  val appsCache = CacheBuilder.newBuilder().expireAfterWrite(expiredTime, TimeUnit.MINUTES)
    .build(new CacheLoader[String, List[ServiceModels.ProviderNode]]() {
      def load(key: String) = {
        try {
          getAllAppProvideDesc(key)
        }
        catch {
          case e: Exception => LOG.error(s"key $key 获取数据失败", e)
            List[ServiceModels.ProviderNode]()
        }
      }
    })

  /**
   * 获取所有环境下 hlb、thrift的提供者节点信息
   *
   * @param nodeType
   * @return
   */
  private def getAllAppProvideDesc(nodeType: String) = {
    val allApps = ServiceDesc.appsName
    // 根据appkey并发获取所有的provide的desc
    val mnsc = MnsCacheClient.getInstance
    val list = allApps.par.flatMap {
      appkey =>
        val mnscRet = Env.values.flatMap {
          env =>
            try {
              if(ZkClient.children(List(Path.sankuaiPath,env, appkey).mkString("/")).size() <= 0){
                if (nodeType.equalsIgnoreCase("HLB")) {
                  mnsc.getMNSCache4HLB(appkey, "0", env.toString).getDefaultMNSCache.asScala
                } else {
                  mnsc.getMNSCache(appkey, "0", env.toString).getDefaultMNSCache.asScala
                }
              }else{
                None
              }
            }
            catch {
              case e: Exception => LOG.error(s"getMNSCache appkey:$appkey,env:${env.toString} error", e)
                None
            }
        }
        mnscRet.filter(_.version.startsWith(nodeType))
    }.toList
    list.map(ServiceModels.SGService2ProviderNode)
  }

  def getAllThriftProvider = {
    appsCache.get("thrift")
  }

  def getAllHLBProvider = {
    appsCache.get("HLB")
  }

  def getAllIpOfThriftNode(source: String) = {
    source match {
      //从数据库取数据
      case "mysql" => AppkeyProviderDao.ipListByType("thrift")
      //从MNSC取数据
      case _ => ServiceProvider.getAllThriftProvider.map(_.ip).filter(_.nonEmpty).distinct
    }
  }

  def getAllIpOfHLBNode(source: String) = {
    source match {
      //从数据库取数据
      case "mysql" => AppkeyProviderDao.ipListByType("http")
      //从MNSC取数据
      case _ => ServiceProvider.getAllHLBProvider.map(_.ip).filter(_.nonEmpty).distinct
    }
  }

  def getNodeTypeByIp(ip: String): String = {
    val ipMap = getIpListCache("mysql")
    val typeOption = ipMap.map {
      case (_type, _list) =>
        if (_list.contains(ip)) {
          Some(_type)
        } else {
          None
        }
    }

    val typeResult = typeOption.flatten
    val nodeType = typeResult.size match {
      case 2 => "thrift&http"
      case 1 => typeResult.head
      case _ => "other"
    }
    nodeType
  }

  def getIpListCache(source: String) = {
    val ret = if (ipListCache.isDefined) {
      ipListCache.get
    } else {
      val thriftIp = getAllIpOfThriftNode(source)
      val hlbIp = getAllIpOfHLBNode(source)
      Map("http" -> hlbIp, "thrift" -> thriftIp)
    }
    ipListCache = Some(ret)
    ret
  }


  /**
   * 获取某一天的服务存活率
   *
   * @param appkey
   * @param ts
   */
  def getAliveRatio(appkey: String, ts: Long) = {
    try {
      val date = DateTimeUtil.format(new Date(ts * 1000l), "yyyyMMdd")
      val request = s"http://scanner-report.sankuai.com/appkey/$date/$appkey"
      val msg = HttpHelper.execute(url(request))
      val httpAliveRatioOpt = (Json.parse(msg.get) \ appkey \ "http+prod" \ "aliverate").asOpt[Double]
      val thriftAliveRatioOpt = (Json.parse(msg.get) \ appkey \ "thrift+prod" \ "aliverate").asOpt[Double]
      val httpAliveRatio = if (httpAliveRatioOpt.isDefined) {
        if (httpAliveRatioOpt.get == 100.0) {
          "100%"
        } else {
          s"${fourDecimalFormatter.format(httpAliveRatioOpt.get)}%"
        }
      } else
        "NaN"
      val thriftAliveRatio = if (thriftAliveRatioOpt.isDefined) {
        if (thriftAliveRatioOpt.get == 100.0) {
          "100%"
        } else {
          s"${fourDecimalFormatter.format(thriftAliveRatioOpt.get)}%"
        }
      } else
        "NaN"
      Some(thriftAliveRatio, httpAliveRatio)
    } catch {
      case e: Exception =>
        LOG.error(s"get alive ratio of  $appkey failed", e)
        None
    }
  }
}
