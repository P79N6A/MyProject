package com.sankuai.msgp.common.utils.client

import java.util.{Date, UUID}

import com.sankuai.meituan.auth.util.UserUtils
import com.sankuai.meituan.auth.vo.User
import com.sankuai.meituan.borp.impl.BorpServiceImpl
import com.sankuai.meituan.borp.vo._
import com.sankuai.msgp.common.model.EntityType.entityTypeEnum
import com.sankuai.msgp.common.model.{Page, _}
import com.sankuai.msgp.common.utils.ExecutionContextFactory
import com.sankuai.msgp.common.utils.helper.{CommonHelper, JsonHelper}
import org.apache.commons.lang3.StringUtils
import org.slf4j.{Logger, LoggerFactory}
import play.api.libs.json.Json

import scala.collection.JavaConverters._
import scala.concurrent.Future

object BorpClient {

  val LOG: Logger = LoggerFactory.getLogger(BorpClient.getClass)

  private val appkey = "msgp"
  private val secret = "b535efb74b52d3d202cb96d2e239b454"
  private val scannerApp = "com.sankuai.octo.scanner"
  private val specialOperatorID = "-1024"

  private implicit val ec = ExecutionContextFactory.build(4)

  private val borpHostUrl = {
    if (CommonHelper.isOffline) {
      "http://release.borp.test.sankuai.info"
    } else {
      "http://api.borp-in.sankuai.com"
    }
  }
  private val service = new BorpServiceImpl(borpHostUrl, appkey, secret)

  case class operationRow(operationId: String, operationType: String = "", operationDesc: String = "", operatorId: String = "",
                          operatorType: Int = 1, operatorName: String = "", addTime: Date = new Date(), remark: String = "") {

    def setOperation(operation: Operation) = {
      operation.setOperationId(operationId)
      operation.setOperationType(operationType)
      operation.setOperationDesc(operationDesc)
      operation.setOperatorId(operationId)
      operation.setOperatorName(operatorName)
      operation.setOperatorType(operatorType)
      operation.setAddTime(addTime)
      operation.setRemark(remark)
      //operation.setExtend(Map("" -> ""))
    }
  }

  case class actionRow(actionId: String, actionType: Int, operationId: String = "", operationType: String = "", operatorId: String,
                       operatorType: Int = 1, operatorName: String, addTime: Date = new Date(), entityType: String, entityId: String) {

    def setAction() = {
      val action = new Action()
      action.setActionId(actionId)
      action.setActionType(actionType)
      action.setOperationId(operationId)
      action.setOperationType(operationType)
      action.setOperatorId(operatorId)
      action.setOperatorType(operatorType)
      action.setOperatorName(operatorName)
      action.setAddTime(addTime)
      action.setEntityType(entityType)
      action.setEntityId(entityId)
      //action.setExtend()
      action

    }
  }

  case class detailRow(detailId: String, actionId: String = "", entityType: String, entityId: String, fieldName: String, oldValue: String = "",
                       newValue: String = "", addTime: Date = new Date()) {
    def setDetail() = {
      val detail = new Detail()
      detail.setDetailId(detailId)
      detail.setActionId(actionId)
      detail.setEntityType(entityType)
      detail.setEntityId(entityId)
      detail.setFieldName(fieldName)
      detail.setOldValue(oldValue)
      detail.setNewValue(newValue)
      detail.setAddTime(addTime)
      //detail.setExtend()
      detail
    }
  }

  case class operationDisplay(appkey: String, actionType: String, operatorName: String, entityType: String, fieldName: String, time: Date, oldValue: String, newValue: String, desc: List[Any])

  /**
    *
    * @param user
    * @param actionType 添加 1,更新 2,删除 3
    * @param entityId
    * @param entityType
    * @param fieldName
    * @param oldValue
    * @param newValue
    */
  def saveOpt(user: User = UserUtils.getUser, actionType: Int, entityId: String, entityType: entityTypeEnum, fieldName: String = "default", oldValue: String = "", newValue: String = "") = {
    Future {
      try {
        val defaultUser = if (null == user) {
          val user_new = new User
          user_new.setLogin("default")
          user_new.setName("default")
          user_new.setId(1)
          user_new
        } else {
          user
        }
        val userId = defaultUser.getId.toString

        val uuid = UUID.randomUUID().toString
        val action =
          actionRow(
            actionId = uuid,
            operationId = uuid,
            actionType = actionType,
            operatorId = userId,
            operatorName = defaultUser.getName,
            operatorType = 1,
            entityId = entityId,
            entityType = entityType.toString)
        val detail =
          detailRow(
            detailId = uuid,
            actionId = uuid,
            entityId = entityId,
            entityType = entityType.toString,
            fieldName = fieldName,
            oldValue = oldValue,
            newValue = newValue)

        val actionObject = action.setAction()
        val detailObject = detail.setDetail()
        actionObject.setDetails(List(detailObject).asJava)
        service.save(actionObject)
      } catch {
        case e: Exception => LOG.error(s"save borp operation excetion", e)
      }
    }
  }

  /**
    * scanner操作记录读取接口
    *
    * @param userId
    * @param startTime
    * @param endTime
    * @param page
    * @return
    */
  def getOptLogByOperatorId(userId: String, startTime: Date, endTime: Date, page: Page): List[operationDisplay] = {
    val borpRequest = BorpRequest.builder.
      mustEq("operatorId", userId).
      beginDate(startTime).
      endDate(endTime).
      from(page.getStart).
      size(page.getPageSize).build
    val borpResponse = service.getBorpResponse(borpRequest, classOf[Action])
    if (null == borpResponse) {
      return null
    }
    val actions = borpResponse.getResult.asScala.toList
    val borpResponsePage = borpResponse.getPage
    page.setPageNo(borpResponsePage.getPageNo)
    page.setPageSize(borpResponsePage.getPageSize)
    page.setTotalCount(borpResponsePage.getTotalCount)
    val listRe = actions.flatMap(
      x => {
        val details = service.getDetailByActionId(x.getActionId).asScala.toList
        if (details.isEmpty) {
          List()
        }
        else {
          val detail = details(0)
          List(operationDisplay(x.getEntityId, ActionType.getName(x.getActionType), x.getOperatorName, x.getEntityType, detail.getFieldName, detail.getAddTime, detail.getOldValue, detail.getNewValue, List()))
        }
      }
    ).sortWith((x, nextX) => x.time.compareTo(nextX.time) > 0)

    val ret = listRe.map {
      x =>
        //根据不同的entityType字段构造operationDisplay的desc字段，当出现withName异常或者validate异常时直接返回x，此时desc字段为空列表
        try {
          EntityType.withName(x.entityType) match {
            case EntityType.updateWeight => x.copy(desc = {
              List(x.fieldName + " " + x.oldValue + " -> " + x.newValue)
            })
            case EntityType.updateStatus => x.copy(desc = {
              List(x.fieldName + " " + getStatusDescription(x.oldValue.toInt) + " -> " + getStatusDescription(x.newValue.toInt))
            })
            case EntityType.deleteProvider => x.copy(desc = {
              List(x.fieldName)
            })
          }
        } catch {
          case e: Exception =>
            x
        }
    }
    return ret
  }

  def getOptLogByEntityTypeFieldName(entityType: String, startTime: Date, endTime: Date, page: Page): List[operationDisplay] = {
    val borpRequest =
      BorpRequest.builder.
        mustNotIn("operatorName", List(scannerApp).asJava).
        mustEq("entityType", entityType).
        beginDate(startTime).
        endDate(endTime).
        from(page.getStart).
        size(page.getPageSize).build
    val borpResponse = service.getBorpResponse(borpRequest, classOf[Action])
    if (null == borpResponse) {
      return null
    }
    val actions = borpResponse.getResult.asScala.toList
    val borpResponsePage = borpResponse.getPage
    page.setPageNo(borpResponsePage.getPageNo)
    page.setPageSize(borpResponsePage.getPageSize)
    page.setTotalCount(borpResponsePage.getTotalCount)
    val listRe = actions.flatMap(
      x => {
        val details = service.getDetailByActionId(x.getActionId).asScala.toList
        if (details.isEmpty) {
          List()
        }
        else {
          val detail = details(0)
          List(operationDisplay(x.getEntityId, ActionType.getName(x.getActionType), x.getOperatorName, x.getEntityType, detail.getFieldName, detail.getAddTime, detail.getOldValue, detail.getNewValue, List()))
        }
      }
    ).sortWith((x, nextX) => x.time.compareTo(nextX.time) > 0)
    val ret = listRe.map {
      x =>
        x.copy(desc = {
          List(x.oldValue)
        })
    }
    return ret
  }


  def getOptLogById(appkey: String, startTime: Date, endTime: Date, page: Page): List[operationDisplay] = {
    val borpRequest =
      BorpRequest.builder.
        mustNotIn("operatorName", List(scannerApp).asJava).
        mustEq("entityId", appkey).
        beginDate(startTime).
        endDate(endTime).
        from(page.getStart).
        size(page.getPageSize).build
    val borpResponse = service.getBorpResponse(borpRequest, classOf[Action])
    if (null == borpResponse) {
      return null
    }
    val actions = borpResponse.getResult.asScala.toList
    val borpResponsePage = borpResponse.getPage
    page.setPageNo(borpResponsePage.getPageNo)
    page.setPageSize(borpResponsePage.getPageSize)
    page.setTotalCount(borpResponsePage.getTotalCount)
    val listRe = actions.flatMap(
      x => {
        val details = service.getDetailByActionId(x.getActionId).asScala.toList
        if (details.isEmpty) {
          List()
        }
        else {
          val detail = details(0)
          List(operationDisplay(x.getEntityId, ActionType.getName(x.getActionType), x.getOperatorName, x.getEntityType, detail.getFieldName, detail.getAddTime, detail.getOldValue, detail.getNewValue, List()))
        }
      }
    ).sortWith((x, nextX) => x.time.compareTo(nextX.time) > 0)

    val result = operationPaser(listRe)
    return result
  }

  /**
    * 根据操作类型和操作人员查询日志
    *
    * @param appkey
    * @param entityType 操作类型
    * @param operator   操作人员
    * @param startTime
    * @param endTime
    * @param page
    * @return
    */
  def getOptLog(appkey: String, entityType: String, operator: String, startTime: Date, endTime: Date, page: Page): List[operationDisplay] = {

    val borpRequestBuilder = if (StringUtils.isNotBlank(entityType)) {
      val entityTypeList = if (entityType == "环境切换") {
        List("修改agent配置并且重启agent", "环境切换删除节点", "环境切换增加节点").asJava
      } else {
        List(entityType).asJava
      }
      if (StringUtils.isNotBlank(operator)) {
        BorpRequest.builder.mustIn("entityType", entityTypeList).mustEq("operatorName", operator)
      } else {
        BorpRequest.builder.mustIn("entityType", entityTypeList)
      }
    } else {
      if (StringUtils.isNotBlank(operator)) {
        BorpRequest.builder.mustEq("operatorName", operator)
      } else {
        BorpRequest.builder
      }
    }

    val borpRequest = borpRequestBuilder.mustEq("entityId", appkey).beginDate(startTime).from(page.getStart).size(page.getPageSize).endDate(endTime).build
    val borpResponse = service.getBorpResponse(borpRequest, classOf[Action])
    val borpResponsePage = borpResponse.getPage
    page.setPageNo(borpResponsePage.getPageNo)
    page.setPageSize(borpResponsePage.getPageSize)
    page.setTotalCount(borpResponsePage.getTotalCount)
    if (null == borpResponse) {
      return null
    }
    //val actions = borpResponse.getResult.asScala.toList.filter(_.getOperatorId != specialOperatorID)
    val actions = borpResponse.getResult.asScala.toList
    val listRe = actions.flatMap(
      x => {
        val details = service.getDetailByActionId(x.getActionId).asScala.toList
        if (details.isEmpty) {
          List()
        }
        else {
          val detail = details.head
          List(operationDisplay(x.getEntityId, ActionType.getName(x.getActionType), x.getOperatorName, x.getEntityType, detail.getFieldName, detail.getAddTime, detail.getOldValue, detail.getNewValue, List()))
        }
      }
    ).sortWith((x, nextX) => x.time.compareTo(nextX.time) > 0)
    operationPaser(listRe)
  }

  /**
    * 获取一段时间内日志记录的所有操作类型
    *
    * @param appkey
    * @param startTime
    * @param endTime
    * @return
    */
  def getAllEntityType(appkey: String, startTime: Date, endTime: Date): java.util.List[String] = {
    val borpRequest =
      BorpRequest.builder.
        mustNotIn("operatorName", List(scannerApp).asJava).
        mustEq("entityId", appkey).
        beginDate(startTime).
        endDate(endTime).build
    val borpResponse = service.getBorpResponse(borpRequest, classOf[Action])
    if (null == borpResponse) {
      return null
    }
    //val actions = borpResponse.getResult.asScala.toList.filter(_.getOperatorId != specialOperatorID)
    val actions = borpResponse.getResult.asScala.toList
    actions.map(_.getEntityType).distinct.map {
      x =>
        try {
          EntityType.withName(x) match {
            case EntityType.updateAndRestartAgent => "环境切换"
            case EntityType.switchEnvDelProvider => "环境切换"
            case EntityType.switchEnvNewProvider => "环境切换"
            case _ => x
          }
        } catch {
          case e: Exception =>
            x
        }
    }.distinct.asJava
  }

  /**
    * 获取一段时间内日志记录的所有操作人员
    *
    * @param appkey
    * @param startTime
    * @param endTime
    * @return
    */
  def getAllOperator(appkey: String, startTime: Date, endTime: Date): java.util.List[String] = {
    val borpRequest =
      BorpRequest.builder.
        mustNotIn("operatorName", List(scannerApp).asJava).
        mustEq("entityId", appkey).
        beginDate(startTime).
        endDate(endTime).build
    val borpResponse = service.getBorpResponse(borpRequest, classOf[Action])
    if (null == borpResponse) {
      return null
    }
    //val actions = borpResponse.getResult.asScala.toList.filter(_.getOperatorId != specialOperatorID)
    val actions = borpResponse.getResult.asScala.toList
    actions.map(_.getOperatorName).distinct.asJava
  }

  def operationPaser(data: List[BorpClient.operationDisplay]): List[BorpClient.operationDisplay] = {
    data.map {
      x =>
        //根据不同的entityType字段构造operationDisplay的desc字段，当出现withName异常或者validate异常时直接返回x，此时desc字段为空列表
        try {
          EntityType.withName(x.entityType) match {
            case EntityType.registerServer => x.copy(desc = {
              val newObj = Json.parse(x.newValue).validate[ServiceModels.Desc].asOpt.get
              List("唯一标识 : " + newObj.appkey, "负责人 : " + newObj.owner)
            })
            case EntityType.updateServer => x.copy(desc = {
              val oldObj = Json.parse(x.oldValue).validate[ServiceModels.Desc].asOpt.get
              val newObj = Json.parse(x.newValue).validate[ServiceModels.Desc].asOpt.get
              diffServer(oldObj, newObj)
            })
            case EntityType.increaseProvider => x.copy(desc = {
              val newObj = Json.parse(x.newValue).validate[ServiceModels.ProviderNode].asOpt.get
              List("主机ip : " + newObj.ip, "端口 : " + newObj.port)
            })
            case EntityType.updateProvider => x.copy(desc = {
              val oldObj = Json.parse(x.oldValue).validate[ServiceModels.ProviderNode].asOpt.get
              val newObj = Json.parse(x.newValue).validate[ServiceModels.ProviderNode].asOpt.get
              List("主机ip : " + newObj.ip, "端口 : " + newObj.port,
                "权重 : " + oldObj.weight + " -> " + newObj.weight,
                "状态 : " + {
                  getStatusDescription(oldObj.status).toString
                } + " -> " + {
                  getStatusDescription(newObj.status).toString
                }
              )
            })
            case EntityType.createGroup => x.copy(desc = {
              val newObj = Json.parse(x.newValue).validate[ServiceModels.Group].asOpt.get
              List("分组名 : " + newObj.name, "状态 : " + {
                if (newObj.status == 1) Status.ALIVE.toString else getStatusDescription(newObj.status).toString
              }, "环境 : " + Env.apply(newObj.env))
            })
            case EntityType.updateGroup => x.copy(desc = {
              val newObj = Json.parse(x.newValue).validate[ServiceModels.Group].asOpt.get
              List("分组名 : " + newObj.name, "状态 : " + {
                if (newObj.status == 1) Status.ALIVE.toString else getStatusDescription(newObj.status).toString
              }, "环境 : " + Env.apply(newObj.env))
            })
            case EntityType.createHttpGroup => x.copy(desc = {
              val newObj = Json.parse(x.newValue).validate[ServiceModels.HlbGroup].asOpt.get
              List("分组名 : " + newObj.group_name, "分组列表 : [" + {
                newObj.server.map {
                  ipport =>
                    ipport.ip + ":" + ipport.port
                }.mkString(",")
              } + "]", "环境 : " + newObj.env)
            })
            case EntityType.updateHttpGroup => x.copy(desc = {
              val newObj = Json.parse(x.newValue).validate[ServiceModels.HlbGroup].asOpt.get
              val oldObj = Json.parse(x.oldValue).validate[ServiceModels.HlbGroup].asOpt.get
              List("分组名 : " + newObj.group_name, "分组列表 : 从[" + {
                oldObj.server.map {
                  ipport =>
                    ipport.ip + ":" + ipport.port
                }.mkString(",")
              } + "]" + "\n变更为:[" + {
                newObj.server.map {
                  ipport =>
                    ipport.ip + ":" + ipport.port
                }.mkString(",")
              } + "]", "环境 : " + newObj.env)
            })
            case EntityType.updateSlowStart => x.copy(desc = List(x.newValue))
            case EntityType.updateHealthCheckConfig => x.copy(desc = List(x.newValue))
            case EntityType.updateLoadBalanceConfig => x.copy(desc = List(x.newValue))
            case EntityType.updateDomainConfig => x.copy(desc = List(x.newValue))
            case EntityType.updateSession => x.copy(desc = List(x.newValue))
            case EntityType.createTrigger => x.copy(desc = {
              val newObj = Json.parse(x.newValue).validate[MonitorModels.Trigger].asOpt.get
              List("服务与调用服务 : " + newObj.side,
                "接口名 : " + newObj.spanname,
                "指标名 : " + MonitorModels.itemList.find(_.name == newObj.item).getOrElse(MonitorModels.Item("", "")).desc,
                "阈值 : " + newObj.threshold)
            })
            case EntityType.updateTrigger => x.copy(desc = {
              val oldObj = Json.parse(x.oldValue).validate[MonitorModels.Trigger].asOpt.get
              val newObj = Json.parse(x.newValue).validate[MonitorModels.Trigger].asOpt.get
              List("服务与调用服务 : " + oldObj.side,
                "接口名 : " + oldObj.spanname,
                "指标名 : " + MonitorModels.itemList.find(_.name == oldObj.item).getOrElse(MonitorModels.Item("", "")).desc,
                "阈值 : " + oldObj.threshold + " -> " + newObj.threshold)
            })
            case EntityType.deleteTrigger => x.copy(desc = {
              val oldObj = Json.parse(x.oldValue).validate[MonitorModels.Trigger].asOpt.get
              List("服务与调用服务 : " + oldObj.side,
                "接口名 : " + oldObj.spanname,
                "指标名 : " + MonitorModels.itemList.find(_.name == oldObj.item).getOrElse(MonitorModels.Item("", "")).desc)
            })
            case EntityType.updateStatus => x.copy(desc = {
              List(x.fieldName + " " + getStatusDescription(x.oldValue.toInt) + " -> " + getStatusDescription(x.newValue.toInt))
            })
            case EntityType.updateWeight => x.copy(desc = {
              List(x.fieldName + " " + x.oldValue + " -> " + x.newValue)
            })
            case EntityType.deleteProvider => x.copy(desc = {
              List(x.fieldName + " " + x.oldValue + " -> " + x.newValue)
            })
            case EntityType.shutdownMachine => x.copy(desc = {
              List(x.newValue)
            })
            case EntityType.msgpDelProvider => x.copy(desc = {
              val oldObj = Json.parse(x.oldValue).validate[ServiceModels.ProviderNode].asOpt.get
              List("主机ip : " + oldObj.ip, "端口 : " + oldObj.port)
            })
            case EntityType.updateAndRestartAgent => x.copy(entityType = "环境切换", desc = {
              val ip = x.fieldName
              val oldEnv = x.oldValue
              val newEnv = x.newValue
              List(EntityType.updateAndRestartAgent.toString, s"主机ip:$ip", s"环境:$oldEnv -> $newEnv")
            })
            case EntityType.switchEnvDelProvider => x.copy(entityType = "环境切换", desc = {
              List("删除节点", x.fieldName)
            })
            case EntityType.switchEnvNewProvider => x.copy(entityType = "环境切换", desc = {
              List("新增节点", x.fieldName)
            })
            case EntityType.mtthriftInvoke => x.copy(desc = {
              List("调用服务 : " + x.fieldName, "参数 : " + x.oldValue, "结果 : " + x.newValue)
            })
            case EntityType.errorLogAddFilter => x.copy(desc = {
              List("新增过滤器 ： " + x.newValue)
            })
            case EntityType.errorLogUpdateFilter => x.copy(desc = {
              List("旧过滤器 : " + x.oldValue, "新过滤器 : " + x.newValue)
            })
            case EntityType.errorLogDeleteFilter => x.copy(desc = {
              List("删除过滤器 : " + x.oldValue)
            })
            case EntityType.cellSwitch => x.copy(desc = {
              List("cell状态调整 : " + x.newValue)
            })
            case EntityType.updateAppkeyAuthWhiteList => x.copy(desc = {
              List(s"环境：${x.fieldName}", "白名单：" + x.newValue)
            })
            case EntityType.updateAppkeyAuth => x.copy(desc = {
              List(s"环境：${x.fieldName}", "客户端名单：" + x.newValue)
            })
            case EntityType.updateSpanAuth => x.copy(desc = {
              val list = new collection.mutable.ListBuffer[Any]
              list += s"环境：${x.fieldName}"
              val spanAuthMap = JsonHelper.toObject(x.newValue, classOf[Map[String, List[String]]])
              spanAuthMap.foreach(entry => {
                val strBuilder = new StringBuilder
                strBuilder.append("接口: ").append(entry._1).append("   ")
                strBuilder.append("可访问该接口的服务: ").append(entry._2.asJava.toString)
                list += strBuilder.toString
              })
              list.toList
            })
          }
        }
        catch {
          case e: Exception =>
            x
        }
    }
  }

  def getStatusDescription(statusId: Int) = {
    Status.apply(statusId).toString
  }

  def diffServer(oldServer: ServiceModels.Desc, newServer: ServiceModels.Desc) = {
    var result: List[Any] = List()
    if (oldServer.owners != newServer.owners)
      result = result :+ ("负责人 : " + oldServer.owner + " -> " + newServer.owner)
    if (oldServer.category != newServer.category)
      result = result :+ ("类型 : " + oldServer.category + " -> " + newServer.category)
    if (oldServer.group != newServer.group)
      result = result :+ ("所属业务 : " + oldServer.group.getOrElse("") + " -> " + newServer.group.getOrElse(""))
    if (oldServer.level != newServer.level)
      result = result :+ ("层级 : " + Level.apply(oldServer.level.get) + " -> " + Level.apply(newServer.level.get))
    if (oldServer.intro != newServer.intro)
      result = result :+ ("服务描述 : " + oldServer.intro + " -> " + newServer.intro)
    if (oldServer.tags != newServer.tags)
      result = result :+ ("标签 : " + oldServer.tags + " -> " + newServer.tags)
    result
  }

}