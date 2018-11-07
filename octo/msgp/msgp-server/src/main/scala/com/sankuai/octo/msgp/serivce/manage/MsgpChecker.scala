package com.sankuai.octo.msgp.serivce.manage

import com.sankuai.msgp.common.model.{Env, Path}
import com.sankuai.msgp.common.config.db.msgp.Tables.SchedulerCostRow
import com.sankuai.octo.msgp.dao.self.OctoJobDao
import com.sankuai.octo.msgp.model.MScheduler
import com.sankuai.octo.msgp.serivce.service.{AppkeyProviderService, ServiceDesc, ServiceGroup}
import com.sankuai.octo.msgp.utils.client.ZkClient
import org.joda.time.DateTime
import org.slf4j.{Logger, LoggerFactory}

import scala.collection.JavaConverters._

object MsgpChecker {
  val LOG: Logger = LoggerFactory.getLogger(MsgpChecker.getClass)

  private val sankuaiPath = "/mns/sankuai"
  case class checkDesc(desc: String, detail: List[String], other: String )

  def monitorCheck = {
    val cost = OctoJobDao.selectCost(MScheduler.monitorSchedule.toString)
    getCheckDesc(cost, "msgp监控报警执行情况")
  }

  def dataSyncCheck = {
    val cost = OctoJobDao.selectCost(MScheduler.dataSyncSchedule.toString)
    getCheckDesc(cost, "msgp性能数据同步执行情况")
  }

  def sgAgentScannerCheck = {
    val cost = OctoJobDao.selectCost(MScheduler.sgAgentScannerSchedule.toString)
    getCheckDesc(cost, "agent重复注册检测执行情况")
  }

  def getCheckDesc(cost: List[SchedulerCostRow], title: String) = {
    //在不抛出异常的情况下取出前两个元素
    val recently = cost.applyOrElse(0, List(null))
    val subRecently = cost.applyOrElse(1, List(null, null))
    val result = if( recently == null ) {
      List("最近没有执行过监控报警")
    } else {
      if( subRecently == null) {
        List("最近一次执行",
          s"时间: ${new DateTime(recently.sTime).toString("yyyy-MM-dd HH:mm:ss")}, 耗时: ${(recently.eTime - recently.sTime)/1000}s")
      } else {
        List("最近两次执行",
          s"时间: ${new DateTime(recently.sTime).toString("yyyy-MM-dd HH:mm:ss")}, 耗时: ${(recently.eTime - recently.sTime)/1000}s",
          s"时间: ${new DateTime(subRecently.sTime).toString("yyyy-MM-dd HH:mm:ss")}, 耗时: ${(subRecently.eTime - subRecently.sTime)/1000}s")
      }
    }
    checkDesc(title, result, "")
  }

  def scheduleCheck() = {
    List(monitorCheck, dataSyncCheck, sgAgentScannerCheck)
  }

  /** 1、是否开启默认分组 2、开启的是否有风险*/
  case class RouteMsg(appkey: String, envId: Int, desc: String)
  def routeCheck(envId: Int) = {
    val apps = ServiceDesc.appsName()
    var routeMsgs:List[RouteMsg] = List()
    apps.foreach{
      appkey =>
        val list = ZkClient.children(List(sankuaiPath, Env.apply(envId), appkey, Path.route).mkString("/")).asScala.map(
          node => {
            val nodePath = List(sankuaiPath, Env.apply(envId), appkey, Path.route, node).mkString("/")
            ServiceGroup.getGroup(nodePath).getOrElse(null)
          }).toList
        val defaultList = list.filter(_.category.getOrElse(0) == 1)
        if (!defaultList.isEmpty) {
          val head = defaultList.head
          if(head.status == 1) { //1 启用
            //校验默认分组
            val result = verifyDefault(appkey, envId)
            if(result != null) {
              routeMsgs = routeMsgs ++ List(RouteMsg(appkey, envId, result))
            }
          }
        }
    }
    routeMsgs
  }

  def verifyDefault(appkey: String, envId: Int) = {
    val providerList = AppkeyProviderService.providerNode(appkey, envId)
    var result: String = null

    val ipGroup = providerList.groupBy(x => {
      if (x.startsWith("10.64.")) {
        "廊坊"
      } else if (x.startsWith("10.32.")) {
        "大兴"
      } else {
        "其他"
      }
    }).map(x => (x._1, x._2.length)).toList

    //当ipGroup为空时，没有provider，开启分组也无风险
    if (ipGroup.length == 1) {
      result = s"'${ipGroup.head._1}'单机房部署,启用默认分组有风险"
    } else {
      val list = ipGroup.foldLeft(List[String]()) {
        (list, x) =>
          if (x._2 < 2)
            list ++ List(s"${x._1}机房部署机器数少于两台")
          else
            list
      }
      if (!list.isEmpty) {
        result = list.mkString(",") + "启用默认分组有风险"
      }
    }
    result
  }

  def main(args: Array[String]): Unit = {

  }
}
