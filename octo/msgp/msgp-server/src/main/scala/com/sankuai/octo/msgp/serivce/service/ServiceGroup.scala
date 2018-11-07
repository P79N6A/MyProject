package com.sankuai.octo.msgp.serivce.service

import java.util.{Date, UUID}

import com.sankuai.meituan.auth.util.UserUtils
import com.sankuai.meituan.borp.vo.ActionType
import com.sankuai.msgp.common.model._
import com.sankuai.msgp.common.service.org.OpsService
import com.sankuai.msgp.common.utils.StringUtil
import com.sankuai.msgp.common.utils.client.BorpClient
import com.sankuai.msgp.common.utils.helper.JsonHelper
import com.sankuai.octo.msgp.domain.AppkeyGroup
import com.sankuai.octo.msgp.model.MnsRoutes
import com.sankuai.octo.msgp.serivce.service.AppkeyProviderService.{OCTO_URL, env_desc}
import com.sankuai.octo.msgp.utils.client.ZkClient
import com.sankuai.octo.mworth.util.DateTimeUtil
import org.slf4j.{Logger, LoggerFactory}
import play.api.libs.json.Json

import scala.collection.JavaConverters._

object ServiceGroup {
  val LOG: Logger = LoggerFactory.getLogger(ServiceGroup.getClass)
  val sankuaiPath = Path.sankuaiPath
  val prodPath = List(sankuaiPath, Env.prod).mkString("/")
  val stagePath = List(sankuaiPath, Env.stage).mkString("/")
  val testPath = List(sankuaiPath, Env.test).mkString("/")
  val rootPaths = List(prodPath, stagePath, testPath)

  private val defaultIdcID = "default-idc"
  private val defaultMultiCenterID1 = "default-multicenter_1"
  private val defaultMultiCenterID2 = "default-multicenter_2"
  private val defaultMultiCenterIDSH = "default-multicenter_sh"
  private val defaultCeneter = "default-center"

  private val defaultGroupName = "同机房优先"
  private val defaultMultiCenterGroupName = "同中心优先"
  private val multiCenterCategory = 3
  private val idcCategory = 1

  def getGroup(nodePath: String) = {
    try {
      val data = ZkClient.getData(nodePath)
      Json.parse(data).validate[ServiceModels.Group].asOpt
    } catch {
      case e: Exception =>
        LOG.error("获取group失败，$nodePath", e)
        None
    }
  }


  /**
    * 按环境查询服务分组
    *
    * @param appkey
    * @param envId
    * @param page
    * @return
    */
  def group(appkey: String, envId: Int, page: Page): List[ServiceModels.Group] = {
    val list = (envId match {
      case 0 =>
        Env.values.filter(_.id != 0).flatMap(x => group(appkey, x.id)).toList
      case _ =>
        group(appkey, envId)
    }).sortBy(r => (r.env, -r.priority))
    page.setTotalCount(list.length)
    list.slice(page.getStart, page.getStart + page.getPageSize)
  }

  private def getAllGroupFromZk(appkey: String, envId: Int, isIpToHost: Boolean, routePath: String) = {
    ZkClient.children(List(sankuaiPath, Env.apply(envId), appkey, routePath).mkString("/")).asScala.flatMap(node => {
      val nodePath = List(sankuaiPath, Env.apply(envId), appkey, routePath, node).mkString("/")
      // 转换为hostname展示
      if (isIpToHost) {
        getGroup(nodePath).map {
          groupParam =>
            val param = groupParam.copy(consumer = groupParam.consumer.copy(ips = groupParam.consumer.ips.map(OpsService.ipToHost)), provider = groupParam.provider.map {
              x =>
                if (x.contains(":")) {
                  val arrays = x.split(":")
                  OpsService.ipToHost(arrays.apply(0)) + ":" + arrays.apply(1)
                } else {
                  x
                }
            })
            param
        }
      } else {
        getGroup(nodePath)
      }
    }).toList.map(x => x.copy(consumer = x.consumer.copy(idcs = if (x.consumer.idcs.isDefined) {
      x.consumer.idcs
    } else {
      Some(List())
    })))
  }

  def group(appkey: String, envId: Int): List[ServiceModels.Group] = {
    upgradeOtherGroupPriority(appkey, envId)
    val list = getAllGroupFromZk(appkey, envId, true, Path.route.toString)

    val defaultIdc = list.filter(defaultIdcFilter)
    val defaultList = if (defaultIdc.nonEmpty) {
      val head = defaultIdc.head
      val defaultGroupItem = head.copy(id = Some(defaultIdcID), category = Some(1), name = defaultGroupName)
      list.filterNot(defaultIdcFilter) :+ defaultGroupItem
    } else {
      doDefaultGroup(appkey, envId, "disable")
      val now = System.currentTimeMillis()
      val defaultGroupItem = ServiceModels.Group(Some(defaultIdcID), defaultGroupName, Some(1), appkey, envId, 0, 0, ServiceModels.ConsumerGroup(List(), Some(List()), List()), List(), Some(now), Some(now), "")
      list :+ defaultGroupItem
    }


    val multiCenter = list.filter(multCenterFilter)
    if (multiCenter.nonEmpty) {
      defaultList.filterNot(multCenterFilter) :+ multiCenter.head
    } else {
      doDefaultMultiCenterGroup(appkey, envId, "disable")
      val multiCenterGroups = multiCenterGroup(appkey, envId)
      defaultList :+ multiCenterGroups.head.copy(status = 0)
    }
  }


  private def multCenterFilter(item: ServiceModels.Group) = item.category.getOrElse(0) == multiCenterCategory

  private def defaultIdcFilter(item: ServiceModels.Group) = item.category.getOrElse(0) == 1

  def saveGroup(appkeyGroup: AppkeyGroup): String = {
    try {
      val consumer = appkeyGroup.getConsumer
      val group = ServiceModels.Group(if (StringUtil.isBlank(appkeyGroup.getId)) {
        None
      } else {
        Some(appkeyGroup.getId)
      },
        appkeyGroup.getName,
        Some(appkeyGroup.getCategory), appkeyGroup.getAppkey,
        appkeyGroup.getEnv, appkeyGroup.getPriority, appkeyGroup.getStatus,
        ServiceModels.ConsumerGroup(consumer.getIps.asScala.toList, Some(consumer.getIdcs.asScala.toList), consumer.getAppkeys.asScala.toList),
        appkeyGroup.getProvider.asScala.toList,
        Some(appkeyGroup.getCreateTime), Some(appkeyGroup.getUpdateTime), appkeyGroup.getReserved)
      val ret = saveGroup(group)
      JsonHelper.dataJson(ret)
    } catch {
      case e: Exception => LOG.error(s"updateGroup失败,${appkeyGroup.toString}", e)
        JsonHelper.errorJson("内部异常: " + e.getMessage)
    }
  }

  /**
    * 修改或创建group
    *
    * @param groupParam
    * @return
    */
  def saveGroup(groupParam: ServiceModels.Group): ServiceModels.Group = {
    // 保存分组时必须将hostname转换为IP
    val param = groupParam.copy(consumer = groupParam.consumer.copy(ips = groupParam.consumer.ips.map(OpsService.host2ip)), provider = groupParam.provider.map {
      x =>
        if (x.contains(":")) {
          val arrays = x.split(":")
          OpsService.host2ip(arrays.apply(0)) + ":" + arrays.apply(1)
        } else {
          x
        }
    })
    val now = System.currentTimeMillis()
    val routePath = List(sankuaiPath, Env.apply(param.env), param.appkey, Path.route).mkString("/")
    val routeData = Json.prettyPrint(Json.toJson(ServiceModels.Route(param.appkey, now))).getBytes("utf-8")
    param.id.fold {
      val group = param.copy(id = Some(UUID.randomUUID().toString), createTime = Some(now), updateTime = Some(now))
      val path = List(sankuaiPath, Env.apply(group.env), group.appkey, Path.route, group.id.get).mkString("/")
      val data = Json.prettyPrint(Json.toJson(group)).getBytes("utf-8")
      ZkClient.client.inTransaction().create().forPath(path, data).and().setData().forPath(routePath, routeData).and().commit()
      //“增加自定义分组”操作日志
      BorpClient.saveOpt(actionType = ActionType.INSERT.getIndex, entityId = group.appkey, entityType = EntityType.createGroup, newValue = Json.toJson(group).toString)
      group
    } {
      x =>
        val path = List(sankuaiPath, Env.apply(param.env), param.appkey, Path.route, param.id.get).mkString("/")
        val oldGroup = getGroup(path)
        val createTime = if (oldGroup.get.createTime.isDefined) {
          oldGroup.get.createTime
        } else {
          Some(now)
        }
        val group = param.copy(updateTime = Some(now), createTime = createTime)
        val data = Json.prettyPrint(Json.toJson(group)).getBytes("utf-8")
        //"编辑自定义分组"
        BorpClient.saveOpt(actionType = ActionType.UPDATE.getIndex, entityId = group.appkey, entityType = EntityType.updateGroup, oldValue = Json.toJson(oldGroup).toString, newValue = Json.toJson(group).toString)
        ZkClient.client.inTransaction().setData().forPath(path, data).and().setData().forPath(routePath, routeData).and().commit()
        group
    }
  }

  private def upgradeOtherGroupPriority(appkey: String, env: Int) = {
    val list = getAllGroupFromZk(appkey, env, false, Path.route.toString)
    val isNeedToUpgrade = list.foldLeft(false) {
      (result, item) =>
        result || (!multCenterFilter(item) && !defaultIdcFilter(item) && item.priority == 1)
    }
    if (isNeedToUpgrade) {
      val routePath = List(sankuaiPath, Env.apply(env), appkey, Path.route).mkString("/")
      list.foreach {
        item =>
          if (!multCenterFilter(item) && !defaultIdcFilter(item)) {
            val upgradeItem = item.copy(priority = item.priority + 1)
            val groupPath = List(routePath, upgradeItem.id.get).mkString("/")
            try {
              if (ZkClient.exist(groupPath)) {
                val data = Json.prettyPrint(Json.toJson(upgradeItem))
                ZkClient.setData(groupPath, data)
              }
            } catch {
              case e: Exception =>
                LOG.error(s"fail to upgrade the route item of $appkey $env", e)
            }
          }
      }
    }
  }

  /**
    *
    * @param isIdc 标志位，支持同中心与同机房优先编辑
    * @param appkey
    * @param env 3/2/1
    * @param action enable or disable
    * @return
    */
  def editIdcOrCenterStatus(isIdc: Boolean, appkey: String, env: Int, action: String) = {
    val routePath = s"$sankuaiPath/${Env(env)}/$appkey/${Path.route}"
    val ret = try {

      // check appkey exists.
      if (!ZkClient.exist(routePath)) {
        JsonHelper.errorJson(s"path = $routePath don't exist.")
      }

      if(isIdc){
        doDefaultGroup(appkey, env, action)
      }else{
        doDefaultMultiCenterGroup(appkey, env, action)
      }
      JsonHelper.dataJson(s"$action path = $routePath")
    } catch {
      case e: Exception =>
        LOG.error(s"fail to edit route, path = $routePath", e)
        JsonHelper.errorJson(s"fail to edit route, msg = ${e.getMessage}")
    }
    ret
  }

  def doDefaultMultiCenterGroup(appkey: String, env: Int, action: String) = {
    try {
      val routePath = List(sankuaiPath, Env.apply(env), appkey, Path.route).mkString("/")
      upgradeOtherGroupPriority(appkey, env)
      var isModified = false
      multiCenterGroup(appkey, env).foreach {
        group =>
          val groupPath = List(routePath, group.id.get).mkString("/")
          val groupExist = ZkClient.exist(groupPath)

          val newStatus = if (action == "enable") {
            1
          } else {
            0
          }
          val data = Json.prettyPrint(Json.toJson(group.copy(status = newStatus)))
          if (groupExist) {
            val oldDefaultGroup = getGroup(groupPath)
            BorpClient.saveOpt(actionType = ActionType.UPDATE.getIndex, entityId = appkey, entityType = EntityType.updateGroup, oldValue = Json.toJson(oldDefaultGroup).toString, newValue = data)
            ZkClient.setData(groupPath, data)
            isModified = true
          } else {
            BorpClient.saveOpt(actionType = ActionType.INSERT.getIndex, entityId = appkey, entityType = EntityType.createGroup, newValue = data)
            ZkClient.client.create().forPath(groupPath, data.getBytes("utf-8"))
          }
      }

      if(isModified){
        sendDefaultRouteStatusUpdateMsgXM(appkey, action, false)
      }
      val now = System.currentTimeMillis()
      val routeData = Json.prettyPrint(Json.toJson(ServiceModels.Route(appkey, now)))
      ZkClient.setData(routePath, routeData)

    } catch {
      case e: Exception => LOG.error(s"defaultGroup $appkey $env $action", e)
    }
  }
  private def sendDefaultRouteStatusUpdateMsgXM(appkey:String,action:String, isIdc :Boolean)={
    val url = s"${OCTO_URL}/serverOpt/operation?appkey=${appkey}#routes"
    val eventTime = DateTimeUtil.format(new Date(), DateTimeUtil.DATE_TIME_FORMAT)
    val enableOrDisable = if("enable"==action){
      "开启"
    }else{
      "禁用"
    }
    val idcOrCenterMsg = if(isIdc){
      "同机房优先"
    }else{
      "同中心优先"
    }
    ServiceCommon.sendStatusMessage(appkey, s"OCTO服务分组监控(${env_desc})\n[${appkey}|$url] ${enableOrDisable}${idcOrCenterMsg}  \n操作时间: $eventTime\n操作用户: ${UserUtils.getUser.getLogin}")
  }

  def doDefaultGroup(appkey: String, env: Int, action: String) = {
    try {
      val routePath = List(sankuaiPath, Env.apply(env), appkey, Path.route).mkString("/")
      var isModified = false
      defaultGroupList(appkey, env).foreach {
        group =>
          val groupPath = List(routePath, group.id.get).mkString("/")
          val groupExist = ZkClient.exist(groupPath)
          var data: String = null
          if (action == "enable") {
            data = Json.prettyPrint(Json.toJson(group.copy(status = 1)))
          } else {
            data = Json.prettyPrint(Json.toJson(group.copy(status = 0)))
          }
          if (groupExist) {
            val oldDefaultGroup = getGroup(groupPath)
            BorpClient.saveOpt(actionType = ActionType.UPDATE.getIndex, entityId = appkey, entityType = EntityType.updateGroup, oldValue = Json.toJson(oldDefaultGroup).toString, newValue = data)
            ZkClient.setData(groupPath, data)
            isModified = true
          } else {
            BorpClient.saveOpt(actionType = ActionType.INSERT.getIndex, entityId = appkey, entityType = EntityType.createGroup, newValue = data)
            ZkClient.client.create().forPath(groupPath, data.getBytes("utf-8"))
          }
      }
      if(isModified){
        sendDefaultRouteStatusUpdateMsgXM(appkey, action, true)
      }

      getOldDefaultGroupList(appkey, env).foreach {
        group =>
          val groupPath = List(routePath, group.id.get).mkString("/")
          if (ZkClient.exist(groupPath)) {
            ZkClient.deleteWithChildren(groupPath)
          }
      }
      val now = System.currentTimeMillis()
      val routeData = Json.prettyPrint(Json.toJson(ServiceModels.Route(appkey, now)))
      ZkClient.setData(routePath, routeData)
    } catch {
      case e: Exception => LOG.error(s"defaultGroup $appkey $env $action", e)
    }
  }

  private def getOldDefaultGroupList(appkey: String, env: Int): List[ServiceModels.Group] = {
    val now = System.currentTimeMillis()
    // TODO get and update from sa api
    val lf = ServiceModels.Group(Some("default-lf"), defaultGroupName, Some(idcCategory), appkey, env, 0, 1, ServiceModels.ConsumerGroup(List("10.64.*"), Some(List()), List()), List("10.64.*"), Some(now), Some(now), "route_limit:0")
    val dx = ServiceModels.Group(Some("default-dx"), defaultGroupName, Some(idcCategory), appkey, env, 0, 1, ServiceModels.ConsumerGroup(List("10.32.*"), Some(List()), List()), List("10.32.*"), Some(now), Some(now), "route_limit:0")
    val yf = ServiceModels.Group(Some("default-yf"), defaultGroupName, Some(idcCategory), appkey, env, 0, 1, ServiceModels.ConsumerGroup(List("10.4.*"), Some(List()), List()), List("10.4.*"), Some(now), Some(now), "route_limit:0")
    val cq = ServiceModels.Group(Some("default-cq"), defaultGroupName, Some(idcCategory), appkey, env, 0, 1, ServiceModels.ConsumerGroup(List("10.12.*"), Some(List()), List()), List("10.12.*"), Some(now), Some(now), "route_limit:0")
    List(lf, dx, yf, cq)
  }


  private def multiCenterGroup(appkey: String, env: Int): List[ServiceModels.Group] = {
    val now = System.currentTimeMillis()

    val bj1 = ServiceModels.Group(Some(defaultMultiCenterID1), defaultMultiCenterGroupName, Some(multiCenterCategory), appkey, env, 1, 1, ServiceModels.ConsumerGroup(MnsRoutes.BEIJING_CENTER_1, Some(List()), List()), MnsRoutes.BEIJING_CENTER_1, Some(now), Some(now), "route_limit:0")
    val bj2 = ServiceModels.Group(Some(defaultMultiCenterID2), defaultMultiCenterGroupName, Some(multiCenterCategory), appkey, env, 1, 1, ServiceModels.ConsumerGroup(MnsRoutes.BEIJING_CENTER_2, Some(List()), List()), MnsRoutes.BEIJING_CENTER_2, Some(now), Some(now), "route_limit:0")
    val sh = ServiceModels.Group(Some(defaultMultiCenterIDSH), defaultMultiCenterGroupName, Some(multiCenterCategory), appkey, env, 1, 1, ServiceModels.ConsumerGroup(MnsRoutes.SH_CENTER, Some(List()), List()), MnsRoutes.SH_CENTER, Some(now), Some(now), "route_limit:0")
    List(bj1, bj2, sh)
  }

  def defaultGroupList(appkey: String, env: Int): List[ServiceModels.Group] = {
    val now = System.currentTimeMillis()
    // TODO get and update from sa api
    val default = ServiceModels.Group(Some(defaultIdcID), defaultGroupName, Some(idcCategory), appkey, env, 0, 1, ServiceModels.ConsumerGroup(List(""), Some(List()), List()), List(""), Some(now), Some(now), "route_limit:0")
    List(default)
  }

  def deleteGroup(appkey: String, id: String) = {
    if (id != "default") {
      rootPaths.foreach(rootPath => {
        val routePath = List(rootPath, appkey, Path.route).mkString("/")
        ZkClient.children(routePath).asScala.foreach {
          node =>
            if (node == id) {
              val now = System.currentTimeMillis()
              val routeData = Json.prettyPrint(Json.toJson(ServiceModels.Route(appkey, now))).getBytes("utf-8")
              LOG.info(s"deleteGroup $appkey $id in $routePath and update $routeData")
              val oldGroupData = ZkClient.getData(s"$routePath/$id")
              ZkClient.client.inTransaction().delete().forPath(s"$routePath/$id").and().setData().forPath(routePath, routeData).and.commit()
              BorpClient.saveOpt(actionType = ActionType.DELETE.getIndex, entityId = appkey, entityType = EntityType.deleteGroup,
                oldValue = oldGroupData, newValue = "")
            }
        }
      })
    }
  }

  def doDefaultHttpGroup(appkey: String, env: Int, action: String, groups: List[ServiceModels.Group]) = {
    try {
      val routeHttpPath = s"$sankuaiPath/${Env.apply(env)}/$appkey/${Path.routeHttp}"
      groups.foreach {
        group =>
          val groupPath = s"$routeHttpPath/${group.id.get}"
          val groupExist = ZkClient.exist(groupPath)
          var data: String = null
          if (action == "enable") {
            data = Json.prettyPrint(Json.toJson(group.copy(status = 1)))
          } else {
            data = Json.prettyPrint(Json.toJson(group.copy(status = 0)))
          }
          if (groupExist) {
            LOG.info(s"编辑Http分组 $appkey env=$env")
            val oldDefaultGroup = getGroup(groupPath)
            BorpClient.saveOpt(actionType = ActionType.UPDATE.getIndex, entityId = appkey, entityType = EntityType.updateGroup, oldValue = Json.toJson(oldDefaultGroup).toString, newValue = data)
            ZkClient.setData(groupPath, data)
          } else {
            LOG.info(s"创建Http分组 $appkey env=$env")
            BorpClient.saveOpt(actionType = ActionType.INSERT.getIndex, entityId = appkey, entityType = EntityType.createGroup, newValue = data)
            ZkClient.client.create().forPath(groupPath, data.getBytes("utf-8"))
          }
      }
      val now = System.currentTimeMillis()
      val routeData = Json.prettyPrint(Json.toJson(ServiceModels.Route(appkey, now)))
      ZkClient.setData(routeHttpPath, routeData)
    } catch {
      case e: Exception => LOG.error(s"defaultGroup $appkey $env $action", e)
    }
  }

  def defaultHttpGroupList(appkey: String, env: Int): List[ServiceModels.Group] = {
    val now = System.currentTimeMillis()
    val httpDefaultGroup = ServiceModels.Group(Some("default"), defaultGroupName, Some(idcCategory), appkey, env, 0, 0, ServiceModels.ConsumerGroup(List(), Some(List()), List()), List(), Some(now), Some(now), "route_limit:0")
    List(httpDefaultGroup)
  }

  private def httpMultiCenterGroup(appkey: String, env: Int): List[ServiceModels.Group] = {
    val now = System.currentTimeMillis()
    val httpCenterDefaultGroup = ServiceModels.Group(Some(defaultCeneter), defaultMultiCenterGroupName, Some(multiCenterCategory), appkey, env, 1, 0, ServiceModels.ConsumerGroup(List(), Some(List()), List()), List(), Some(now), Some(now), "route_limit:0")
    List(httpCenterDefaultGroup)
  }

  def editMultiCenter(appkey: String, text: String) = {
    val env = (Json.parse(text) \ "env").as[Int]
    val action = (Json.parse(text) \ "action").as[String]
    doDefaultMultiCenterGroup(appkey, env, action)
  }

  def defaultGroup(appkey: String, text: String) = {
    val env = (Json.parse(text) \ "env").as[Int]
    val action = (Json.parse(text) \ "action").as[String]
    doDefaultGroup(appkey, env, action)
  }

  //多中心强制、非强制
  def forceMultiCenterGroup(appkey: String, text: String) = {
    val env = (Json.parse(text) \ "env").as[Int]
    val reserved = (Json.parse(text) \ "reserved").as[String]
    doForceMultiCenterGroup(appkey, env, reserved)
  }

  def doForceMultiCenterGroup(appkey: String, env: Int, reserved: String) = {
    try {
      val routePath = List(sankuaiPath, Env.apply(env), appkey, Path.route).mkString("/")
      multiCenterGroup(appkey, env).foreach {
        group =>
          val groupPath = List(routePath, group.id.get).mkString("/")
          val groupExist = ZkClient.exist(groupPath)
          val data = Json.prettyPrint(Json.toJson(group.copy(reserved = reserved)))
          if (groupExist) {
            LOG.info("编辑分组")
            val oldDefaultGroup = getGroup(groupPath)
            BorpClient.saveOpt(actionType = ActionType.UPDATE.getIndex, entityId = appkey, entityType = EntityType.updateGroup, oldValue = Json.toJson(oldDefaultGroup).toString, newValue = data)
            ZkClient.setData(groupPath, data)
          }
      }
      val now = System.currentTimeMillis()
      val routeData = Json.prettyPrint(Json.toJson(ServiceModels.Route(appkey, now)))
      ZkClient.setData(routePath, routeData)
    } catch {
      case e: Exception => LOG.error(s"defaultGroup $appkey $env $reserved", e)
    }
  }

  //默认分组强制、非强制
  def forceDefaultGroup(appkey: String, text: String) = {
    val env = (Json.parse(text) \ "env").as[Int]
    val reserved = (Json.parse(text) \ "reserved").as[String]
    doForceDefaultGroup(appkey, env, reserved)
  }

  def doForceDefaultGroup(appkey: String, env: Int, reserved: String) = {
    try {
      val routePath = List(sankuaiPath, Env.apply(env), appkey, Path.route).mkString("/")
      defaultGroupList(appkey, env).foreach {
        group =>
          val groupPath = List(routePath, group.id.get).mkString("/")
          val groupExist = ZkClient.exist(groupPath)
          val data = Json.prettyPrint(Json.toJson(group.copy(reserved = reserved)))
          if (groupExist) {
            LOG.info("编辑分组")
            val oldDefaultGroup = getGroup(groupPath)
            BorpClient.saveOpt(actionType = ActionType.UPDATE.getIndex, entityId = appkey, entityType = EntityType.updateGroup, oldValue = Json.toJson(oldDefaultGroup).toString, newValue = data)
            ZkClient.setData(groupPath, data)
          }
      }
      val now = System.currentTimeMillis()
      val routeData = Json.prettyPrint(Json.toJson(ServiceModels.Route(appkey, now)))
      ZkClient.setData(routePath, routeData)
    } catch {
      case e: Exception => LOG.error(s"defaultGroup $appkey $env $reserved", e)
    }
  }


  /* http服务复用thrift自动分组功能 */
  def setHttpIDCOptimize(appkey: String, env: Int, isOn: Boolean): Boolean = {
    LOG.info(s"setHttpIDCOptimize $appkey env=$env isOn=$isOn")
    try {
      val now = System.currentTimeMillis()
      val routeHttpPath = s"$sankuaiPath/${Env.apply(env)}/$appkey/${Path.routeHttp}"
      val groupPath = s"$routeHttpPath/default"
      val oldDefaultGroup = getGroup(groupPath)
      oldDefaultGroup.foreach {
        group =>
          var data: String = null
          if (isOn) {
            data = Json.prettyPrint(Json.toJson(group.copy(status = 1, updateTime = Some(now))))
          } else {
            data = Json.prettyPrint(Json.toJson(group.copy(status = 0, updateTime = Some(now))))
          }
          ZkClient.setData(groupPath, data)
      }
      val routeData = Json.prettyPrint(Json.toJson(ServiceModels.Route(appkey, now)))
      //触发/route-http路径的watcher生效
      ZkClient.setData(routeHttpPath, routeData)
      true
    } catch {
      case e: Exception =>
        LOG.error(s"setHttpIDCOptimize $appkey $env $isOn", e)
        false
    }
  }

  def initHttpGroup(appkey: String, env: Int) = {
    try {
      val list = getAllGroupFromZk(appkey, env, false, Path.routeHttp.toString)
      val defaultIdc = list.filter(defaultIdcFilter)
      if (defaultIdc.isEmpty) {
        val idcGroup = defaultHttpGroupList(appkey, env)
        doDefaultHttpGroup(appkey, env, "disable", idcGroup)
      }


      val multiCenter = list.filter(multCenterFilter)
      if (multiCenter.isEmpty) {
        val centerGroup = httpMultiCenterGroup(appkey, env)
        doDefaultHttpGroup(appkey, env, "disable", centerGroup)
      }
      true
    } catch {
      case e: Exception =>
        LOG.error("fail to init http groups", e)
        false
    }
  }

  /* http服务复用thrift自动分组功能 */
  def setHttpCenterOptimize(appkey: String, env: Int, isOn: Boolean): Boolean = {
    LOG.info(s"setHttpCenterOptimize $appkey env=$env isOn=$isOn")
    if (!initHttpGroup(appkey, env)) {
      return false
    }

    try {
      val now = System.currentTimeMillis()
      val routeHttpPath = s"$sankuaiPath/${Env.apply(env)}/$appkey/${Path.routeHttp}"
      val groupPath = s"$routeHttpPath/$defaultCeneter"
      val oldDefaultGroup = getGroup(groupPath)
      oldDefaultGroup.foreach {
        group =>
          val currentStatus = if (isOn) {
            1
          } else {
            0
          }
          val data = Json.prettyPrint(Json.toJson(group.copy(status = currentStatus, updateTime = Some(now))))
          ZkClient.setData(groupPath, data)
      }
      val routeData = Json.prettyPrint(Json.toJson(ServiceModels.Route(appkey, now)))
      //触发/route-http路径的watcher生效
      ZkClient.setData(routeHttpPath, routeData)
      true
    } catch {
      case e: Exception =>
        LOG.error(s"setHttpIDCOptimize $appkey $env $isOn", e)
        false
    }
  }


  def setHttpGroup(groupParam: ServiceModels.Group): ServiceModels.Group = {
    // 保存分组时必须将hostname转换为IP
    val param = groupParam.copy(consumer = groupParam.consumer.copy(ips = groupParam.consumer.ips.map(OpsService.host2ip)), provider = groupParam.provider.map {
      x =>
        if (x.contains(":")) {
          val arrays = x.split(":")
          OpsService.host2ip(arrays.apply(0)) + ":" + arrays.apply(1)
        } else {
          x
        }
    })
    val now = System.currentTimeMillis()
    val routePath = List(sankuaiPath, Env.apply(param.env), param.appkey, Path.route).mkString("/")
    val routeData = Json.prettyPrint(Json.toJson(ServiceModels.Route(param.appkey, now))).getBytes("utf-8")
    param.id.fold {
      val group = param.copy(id = Some(UUID.randomUUID().toString), createTime = Some(now), updateTime = Some(now))
      val path = List(sankuaiPath, Env.apply(group.env), group.appkey, Path.route, group.id.get).mkString("/")
      val data = Json.prettyPrint(Json.toJson(group)).getBytes("utf-8")
      ZkClient.client.inTransaction().create().forPath(path, data).and().setData().forPath(routePath, routeData).and().commit()
      //“增加自定义分组”操作日志
      BorpClient.saveOpt(actionType = ActionType.INSERT.getIndex, entityId = group.appkey, entityType = EntityType.createGroup, newValue = Json.toJson(group).toString)
      group
    } {
      x =>
        val group = param.copy(updateTime = Some(now))
        val path = List(sankuaiPath, Env.apply(group.env), group.appkey, Path.route, group.id.get).mkString("/")
        val data = Json.prettyPrint(Json.toJson(group)).getBytes("utf-8")
        //"编辑自定义分组"
        val oldGroup = getGroup(path)
        BorpClient.saveOpt(actionType = ActionType.UPDATE.getIndex, entityId = group.appkey, entityType = EntityType.updateGroup, oldValue = Json.toJson(oldGroup).toString, newValue = Json.toJson(group).toString)
        ZkClient.client.inTransaction().setData().forPath(path, data).and().setData().forPath(routePath, routeData).and().commit()
        group
    }
  }

  def getRouteDetail(appkey: String, env: Int, id: String) = {
    val envDesc = Env(env).toString
    val path = s"$sankuaiPath/$envDesc/$appkey/route/$id"
    val json = ZkClient.getData(path)
    JsonHelper.toObject(json, classOf[ServiceModels.Group])
  }


  def editRouteDetail(json: String): Boolean = {
    val currentTime = System.currentTimeMillis()
    val param: ServiceModels.Group = JsonHelper.toObject(json, classOf[ServiceModels.Group]).copy(updateTime = Option(currentTime))
    val parentPath = List(sankuaiPath, Env.apply(param.env), param.appkey, Path.route).mkString("/")
    val id = param.id.get
    val groupPath = s"$parentPath/$id"

    val groupData = Json.prettyPrint(Json.toJson(param))
    val routeData = Json.prettyPrint(Json.toJson(ServiceModels.AppkeyTs(param.appkey, currentTime)))
    try {
      ZkClient.setData(groupPath, groupData)
      ZkClient.setData(parentPath, routeData)
    } catch {
      case _: Exception => return false
    }
    true
  }


}
