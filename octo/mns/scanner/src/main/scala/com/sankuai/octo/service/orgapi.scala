package com.sankuai.octo.service

import com.sankuai.meituan.org.remote.service.{RemoteEmployeeService, RemoteOrgService, RemoteOrgTreeService}
import org.apache.commons.lang.StringUtils
import org.slf4j.{Logger, LoggerFactory}

import scala.collection.JavaConverters._

object orgapi {
  val LOG: Logger = LoggerFactory.getLogger(orgapi.getClass)

  //private def orgHost = "http://hr-vip.lf.sankuai.com/mtorg"
  private def orgHost = "http://api.org-in.sankuai.com"

  private val remoteOrgTreeService = {
    val service = new RemoteOrgTreeService()
    service.setClientId("msgp")
    service.setSecret("b535efb74b52d3d202cb96d2e239b454")
    service.setHost(orgHost)
    service
  }

  private val remoteOrgService = {
    val service = new RemoteOrgService()
    service.setClientId("msgp")
    service.setSecret("b535efb74b52d3d202cb96d2e239b454")
    service.setHost(orgHost)
    service
  }

  private val remoteEmployeeService = {
    val service = new RemoteEmployeeService()
    service.setClientId("msgp")
    service.setSecret("b535efb74b52d3d202cb96d2e239b454")
    service.setHost(orgHost)
    service
  }

  // 5 技术工程，20 平台业务，103 创新业务，1456 外卖研发，3559 外卖配送（4 产品）
  // 97 酒店研发（101 酒店），2018 酒店数据管理组，877 IT部
  // 1829 云计算 3848 金融发展 1819 支付平台
  private val devOrgs = List(5, 20, 103, 1456, 3559, 97, 2018, 877, 1829, 3848, 1819)
  private var employeeBasics: Option[List[EmployeeBasic]] = None

  case class EmployeeBasic(id: Int, name: String, showName: String)


  def employee(id: Int) = {
    remoteEmployeeService.getEmployeeInfo(id)
  }

  def employees(): List[EmployeeBasic] = {
    employeeBasics.getOrElse {
      employeeBasics = Some(dev())
      LOG.info("getAllEmployeeList real " + employeeBasics.get.length)
      employeeBasics.get
    }
  }

  def dev(): List[EmployeeBasic] = {
    devOrgs.flatMap(orgId => remoteEmployeeService.getAllEmployeeListByOrg(orgId).asScala.map {
      emp =>
        EmployeeBasic(emp.getId, emp.getName, emp.getName + "(" + emp.getLogin + ")")
    }.toList)
  }

  def orgTreeLevel(orgId: String, limitOrgIds: java.util.List[Integer]) = {
    if (StringUtils.isBlank(orgId)) {
      //处理第一次请求：因为第一次请求没有orgId参数
      if (limitOrgIds.isEmpty) remoteOrgTreeService.getOrgRootNode else remoteOrgTreeService.getOrgRootNode(limitOrgIds)
    } else {
      //展开某个节点请求
      val id = orgId.substring(1).toInt
      remoteOrgTreeService.getOrgSubNodes(id)
    }
  }

  def orgTreeSearch(keyWord: String, limitOrgIds: java.util.List[Integer]) = {
    if (limitOrgIds.isEmpty) {
      val org = remoteOrgTreeService.searchOnlineOrgTreeNodes(keyWord)
      val emp = remoteOrgTreeService.searchOnlineEmpTreeNodes(keyWord)
      val result = (org.asScala.toList ++ emp.asScala.toList).distinct
      result.asJava
    }
    else {
      val org = remoteOrgTreeService.searchOnlineOrgTreeNodes(keyWord, limitOrgIds)
      val emp = remoteOrgTreeService.searchOnlineEmpTreeNodes(keyWord, limitOrgIds)
      val result = (org.asScala.toList ++ emp.asScala.toList).distinct
      result.asJava
    }
  }

  /* 获取用户所有的上级组**/
  def getUserAncestorOrgs(userId: List[Integer]) = {
    val employeeMessage = remoteEmployeeService.getEmployeeList(userId.asJava).asScala
    val employeeAncestorOrgs = if (!employeeMessage.isEmpty) {
      remoteOrgService.getAncestorOrgs(employeeMessage(0).getOrgId).asScala
    } else {
      List()
    }
    employeeAncestorOrgs.toSet
  }

  /* 获取用户的直属组的信息**/
  def getUserOrg(userId: List[Integer]) = {
    /* 根据用户id获取相应的orgId列表**/
    val orgId = remoteEmployeeService.getEmployeeList(userId.asJava).asScala.map(_.getOrgId).distinct
    /* 根据orgId列表获取org具体信息**/
    remoteOrgService.getOrgsByIds(orgId.asJava).asScala
  }
}
