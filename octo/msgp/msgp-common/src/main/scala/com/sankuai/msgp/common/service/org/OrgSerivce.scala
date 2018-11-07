package com.sankuai.msgp.common.service.org

import com.sankuai.meituan.org.remote.service.{RemoteEmployeeService, RemoteOrgService, RemoteOrgTreeService}
import com.sankuai.meituan.org.remote.vo.builder.SearchBuilder
import com.sankuai.meituan.org.remote.vo.search.SearchType
import com.sankuai.meituan.org.remote.vo.{EmpSimpleVo, EmployeeInfo, OrgTreeNodeVo}
import org.apache.commons.lang.StringUtils
import org.slf4j.{Logger, LoggerFactory}

import scala.annotation.tailrec
import scala.collection.JavaConverters._
import scala.collection.mutable.ListBuffer


object OrgSerivce {
  val LOG: Logger = LoggerFactory.getLogger(OrgSerivce.getClass)

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


  //  private var employeeBasics: Option[List[EmployeeBasic]] = None

  case class EmployeeBasic(id: Int, name: String, showName: String)

  case class EmployeePos(posID: Int, posName: String)


  def employee(id: Int) = {
    remoteEmployeeService.getEmployeeInfo(id)
  }


  def employee(login: String): Option[EmployeeInfo] = {
    val employees = remoteEmployeeService.getEmployeeListByLogin(login)
    if (!employees.isEmpty) {
      Some(employees.get(0))
    } else {
      //兼容虚拟账号
      val fuzzyEmployee = getSingelEmployeeByKeyWord(login)
      if (fuzzyEmployee.isDefined) {
        fuzzyEmployee
      } else {
        LOG.info(s"无法获取雇佣信息 : $login")
        None
      }
    }
  }


  def filterLeftEmployeeById(users: Seq[Int]) = {
    users.map(uid => employee(uid)).filter(_.getStatus.equals(0))
  }

  def filterLeftEmployeeByName(users: Seq[String]) = {
    val filteredUser = users.flatMap {
      user =>
        val employeeInfoOpt = employee(user)
        employeeInfoOpt match {
          case Some(employeeInfo) =>
            if (employeeInfo.getStatus.equals(0)) {
              Some(employeeInfo.getLogin)
            } else {
              None
            }
          case None =>
            None
        }
    }
    filteredUser
    //    users.map(user=>employee(user).getOrElse(new EmployeeInfo())).filter(_.getStatus.equals(0)).map(_.getLogin).toSeq
  }


  def orgTreeLevel(orgId: String, limitOrgIds: java.util.List[Integer]) = {
    if (StringUtils.isBlank(orgId)) {
      //处理第一次请求：因为第一次请求没有orgId参数
      val result = if (limitOrgIds.isEmpty) {
        remoteOrgTreeService.getOrgRootNode
      } else {
        remoteOrgTreeService.getOrgRootNode(limitOrgIds)
      }
      result
    } else {
      //展开某个节点请求
      val id = orgId.substring(1).toInt
      val result = remoteOrgTreeService.getOrgSubNodes(id)
      result
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
    val employeeAncestorOrgs = if (employeeMessage.nonEmpty) {
      remoteOrgService.getAncestorOrgs(employeeMessage.head.getOrgId).asScala
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

  def getAllEmployeesByOrg(orgId: Int) = {
    try {
      remoteEmployeeService.getAllEmployeeListByOrg(orgId).asScala.map {
        emp =>
          EmployeeBasic(emp.getId, emp.getLogin, emp.getName + "(" + emp.getLogin + ")")
      }
    }
    catch {
      case e: Exception =>
        LOG.info(s"getAllEmployeesByOrg error orgId: $orgId", e)
        ListBuffer()
    }
  }


  def getEmployeeListByKeyWord(keyWord: String) = {
    val searchCondition = SearchBuilder.create().searchName(keyWord).searchType(SearchType.EMP).includeVirtual().list().build()
    val vos = remoteOrgTreeService.search(searchCondition).asScala.toList
    vos.map {
      x =>
        val empSimpleVo = new EmpSimpleVo()
        empSimpleVo.setId(x.getDataId())
        empSimpleVo.setName(x.getName() + "(" + x.getEnName() + ")")
        empSimpleVo.setLogin(x.getEnName())
        empSimpleVo
    }
  }

  def getSingelEmployeeByKeyWord(keyWord: String): Option[EmployeeInfo] = {
    val searchCondition = SearchBuilder.create().searchName(keyWord).searchType(SearchType.EMP).includeVirtual().list().build()
    val vos = remoteOrgTreeService.search(searchCondition).asScala.toList
    val result = if (vos.length >= 1) {
      val orgTreeNodeVo = vos.head
      val employeeInfo = new EmployeeInfo()
      employeeInfo.setId(orgTreeNodeVo.getDataId())
      employeeInfo.setName(orgTreeNodeVo.getName())
      employeeInfo.setLogin(orgTreeNodeVo.getEnName())
      Some(employeeInfo)
    } else {
      None
    }
    result
  }


  def getEmployeePos(mis: String): Option[EmployeePos] = {
    val empList = remoteEmployeeService.getEmployeeListByLogin(mis)
    if (empList.size() == 1) {
      Some(EmployeePos(empList.get(0).getPosId, empList.get(0).getPosName))
    } else {
      None
    }
  }

  def getEmployeePosID(mis: String): Int = {
    val empList = remoteEmployeeService.getEmployeeListByLogin(mis)
    if (empList.size() == 1) {
      empList.get(0).getPosId
    } else {
      0
    }
  }

  def getEmployeePosName(mis: String) = {
    val empList = remoteEmployeeService.getEmployeeListByLogin(mis)
    if (empList.size() == 1) {
      empList.get(0).getPosName
    } else {
      "其他"
    }
  }

  def getEmployeeByUsername(usename: String): Option[EmployeeInfo] = {
    val employees = remoteEmployeeService.getEmployeeListByLogin(usename)
    if (!employees.isEmpty) {
      Some(employees.get(0))
    } else {
      None
    }
  }

  def getEmployeeOrgId(mis: String): Int = {
    val empList = remoteEmployeeService.getEmployeeListByLogin(mis)
    if (empList.size() == 1) {
      empList.get(0).getOrgId
    } else {
      1 //orgid = 1 -> 美团(orgname)
    }
  }

  def getEmployeeOrgName(mis: String) = {
    val employees = remoteEmployeeService.getEmployeeListByLogin(mis)
    if (employees.size() > 0) {
      getOrgByOrgId(employees.get(0).getOrgId).getName
    } else {
      "其他"
    }
  }

  def getEmployeeOrgName(orgId: Int) = {
    getOrgByOrgId(orgId).getName
  }

  def getEmployeeNumByOrg(orgId: Int) = {
    remoteEmployeeService.getAllEmployeeListByOrg(orgId).asScala.length
  }

  def getDevEmployeeNumofSubOrg(orgId: Int, devPosList: List[String]) = {
    remoteOrgTreeService.getOrgEmpSubNodes(orgId).asScala.map {
      x =>
        x.getDataId.toInt -> getDevEmployeeNumByOrg(x.getDataId, devPosList)
    }.toMap
  }

  def getSubOrg(orgId: Int) = {
    val result = remoteOrgTreeService.getOrgEmpSubNodes(orgId)
    result
  }

  def getDevEmployeeNumByOrg(orgId: Int, devPosList: List[String]) = {
    remoteEmployeeService.getAllEmployeeListByOrg(orgId).asScala.count(
      emp =>
        devPosList.exists(x => x.equals(emp.getPosName))
    )
  }

  def getOrgByOrgId(orgId: Int) = {
    remoteOrgService.getById(orgId)
  }

  def getTopOrgName(orgId: Int) = {
    getOrgByOrgId(orgId).getTopOrgName
  }

  @tailrec //使用尾递归后节省的时间大概在20%
  def getOrgAllSubNodes(orgId: List[Int], orgNodeList: List[OrgTreeNodeVo]): List[OrgTreeNodeVo] = {
    if (orgId.isEmpty) {
      return orgNodeList
    }
    val currentLevelOrgNode = orgId.flatMap {
      id =>
        remoteOrgTreeService.getOrgEmpSubNodes(id).asScala.toList.filter(_.getOrgCategory != null)
    }
    val orgNodeListNew = orgNodeList ::: currentLevelOrgNode
    val orgIdNew = currentLevelOrgNode.map(_.getDataId.toInt)
    getOrgAllSubNodes(orgIdNew, orgNodeListNew)
  }

  def getAllOrgIdOfEmployee(orgId: Int) = {
    remoteEmployeeService.getAllEmployeeListByOrg(orgId).asScala.par.map {
      x =>
        x.getOrgId.toInt
    }.toList.distinct
  }

  def getDirectHeader(userId: Integer, date: String) = {
    remoteEmployeeService.getDirectHeaderById(userId, date)
  }

  def getHeadList(userId: Integer) = {
    remoteEmployeeService.getHeaderEmpIdList(userId)
  }

  def getRoleName(login: String) = {
    getEmployeeByUsername(login) match {
      case Some(e) =>
        e.getPosName
      case None =>
        ""
    }
  }

}
