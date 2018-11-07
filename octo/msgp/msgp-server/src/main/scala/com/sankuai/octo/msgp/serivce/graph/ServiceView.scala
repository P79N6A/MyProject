package com.sankuai.octo.msgp.serivce.graph

import javax.servlet.http.Cookie

import com.sankuai.meituan.auth.util.UserUtils
import com.sankuai.msgp.common.config.MsgpConfig
import com.sankuai.msgp.common.service.org.OpsService
import com.sankuai.msgp.common.utils.helper.CommonHelper
import com.sankuai.octo.msgp.serivce.doc.DocDao
import com.sankuai.octo.msgp.serivce.graph.ServiceModel._
import com.sankuai.octo.msgp.serivce.graph.ViewDefine.Graph
import com.sankuai.octo.msgp.serivce.other.PerfApi
import com.sankuai.octo.msgp.serivce.service.{AppkeyProviderService, ServiceCommon}
import com.sankuai.octo.msgp.serivce.service
import org.joda.time.DateTime
import org.slf4j.{Logger, LoggerFactory}

object ServiceView {
  private val LOG: Logger = LoggerFactory.getLogger(ServiceView.getClass)
  private val defaultAdmin = "zhangxi,wangyanzhao,huangbinqiang,zhuhui02,yangjie17"

  case class MethodDesc(name: String, nameDesc: String, count: Long, qps: Double, upper50: Double, upper90: Double, upper95: Double, upper99: Double)

  case class InvokeDesc(from: String, fromDesc: String, to: String, toDesc: String, invokeDesc: List[MethodDesc])


  def getGraphAdmin = {
    MsgpConfig.get("msgp.graph.admin", defaultAdmin).split(",")
  }

  case class CacheKey(graphId: Int, unit: String)

  def getServerMsg(appkey: String) = {
    // get server desc
    val description = service.ServiceCommon.desc(appkey)
    // get hosts load
    val hosts = AppkeyProviderService.provider(appkey).map(_.ip).distinct.
      map(OpsService.ipToHost).map(getLoad).
      groupBy(Color.hostColor).map(x => (x._1.toString, x._2.length))
    val result = Map("introduction" -> description.intro,
      "owners" -> description.owners,
      "createTime" -> description.createTime,
      "machinesCount" -> hosts.values.sum,
      "machines" -> hosts)
    result
  }

  def getLoad(hostname: String) = {
    0.0
  }


  def getServerDesc(appkey: String, idc: String) = {
    // get server desc
    val description = service.ServiceCommon.desc(appkey)
    // get hosts load
    val ipList = AppkeyProviderService.provider(appkey).map(_.ip).distinct.filter {
      x =>
        if ("all".equals(idc)) {
          true
        } else {
          CommonHelper.ip2IDC(x).equals(idc)
        }
    }

    val hosts = ipList.map(OpsService.ipToHost).map(getLoad).
      groupBy(Color.hostColor).map(x => (x._1.toString, x._2.length))
    val result =
      Map("introduction" -> description.intro,
        "owners" -> description.owners,
        "createTime" -> description.createTime,
        "machinesCount" -> hosts.values.sum,
        "machines" -> hosts)
    result
  }

  def updateServerIntro(appkey: String, introduction: String, idc: String) = {
    val description = service.ServiceCommon.desc(appkey).copy(intro = introduction)
    val cookie = new Array[Cookie](0)
    ServiceCommon.saveService(UserUtils.getUser, description, cookie)
  }

  //按照事业群分的，也可以使用该函数。只需要在ViewDefine配置，和数据库中配置相应的数据即可
  def getIdcInfo(id: Int, idc: String) = {
    val q_idc = if (idc != "all") {
      idc.toUpperCase()
    } else {
      idc
    }
    val data = ViewCache.getGraphDataIDC(id, q_idc)
    data.map {
      x =>
        val user = UserUtils.getUser
        val permission = if (getGraphAdmin.contains(user.getLogin)) "write" else "read"
        val business = Graph(id).toString
        x.copy(auth = Some(permission), business = Some(business))
    }
  }


  def getInvokeDesc(from: String, to: String, idc: String) = {
    val server = PerfApi.getInvokeDescByIDC(from, to, idc)
    val result = if (server.isEmpty) {
      PerfApi.getInvokeDescByIDC(to, from, idc)
    } else {
      server
    }
    val appkey = if (server.isEmpty) {
      from
    } else {
      to
    }

    val descList = result.map {
      x =>
        val apiName = DocDao.getApiName(appkey, x.name).getOrElse("该方法暂无描述")
        MethodDesc(x.name, apiName, x.count, x.qps, x.upper50, x.upper90, x.upper95, x.upper99)
    }.filter(!_.name.equals("all")).sortBy(-_.qps)
    val fromDesc = ServiceCommon.desc(from)
    val toDesc = ServiceCommon.desc(to)
    InvokeDesc(from, fromDesc.intro, to, toDesc.intro, descList)
  }

  def getNewApps(id: Int, days: Int) = {
    val tsLimit = (new DateTime().minusDays(days).getMillis / 1000).toInt
    val data = service.ServiceCommon.listService.filter(x => x.createTime.getOrElse(0L) > tsLimit).filter(x => ViewDefine.isInGraph(id, x))
    data
  }

  // 获取过去2天内新增的接口，如果app是新增的那么所有的接口都是新增的
  def getNewSpannames(id: Int) = {
    ViewCache.getNewSpannames(id)
  }


  // 过去一天tp90性能倒序的20个接口
  def getPerfWorstAPI(id: Int, count: Int) = {
    ViewCache.getperfWorst(id).slice(0, count)
  }

}
