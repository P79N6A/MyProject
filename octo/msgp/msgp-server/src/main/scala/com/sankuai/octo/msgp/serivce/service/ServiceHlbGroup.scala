package com.sankuai.octo.msgp.serivce.service

import com.sankuai.meituan.borp.vo.ActionType
import com.sankuai.msgp.common.utils.helper.JsonHelper
import com.sankuai.octo.msgp.domain.HLBGroup
import com.sankuai.msgp.common.model.{EntityType, ServiceModels}
import com.sankuai.msgp.common.utils.client.BorpClient
import com.sankuai.octo.msgp.utils.client.ZkClient
import org.slf4j.{Logger, LoggerFactory}
import play.api.libs.json._

import scala.collection.JavaConverters._



object ServiceHlbGroup {

  val LOG: Logger = LoggerFactory.getLogger(ServiceHlbGroup.this.getClass)

  def getPath(env: String, appkey: String) = "/mns/sankuai/" + env + "/" + appkey


  def saveHlbGroup(hlbGroup: HLBGroup) = {
    try {
      val appkey = hlbGroup.getAppkey
      val appkeyPath = getPath(hlbGroup.getEnv, appkey)
      if (ZkClient.exist(appkeyPath)) {
        val group_name = hlbGroup.getGroup_name
        val providerList: List[String] = ZkClient.children(appkeyPath + "/provider-http").asScala.toList
        var success = 1
        hlbGroup.getServer.asScala.foreach {
          x =>
            if (!providerList.contains(x.getIp + ":" + x.getPort)) {
              success = 0
            }
        }

        if (success == 0) {
          Json.toJson(ServiceModels.AddResult(500, "group ip cannot be added"))
        } else {
          val data = JsonHelper.jsonStr(hlbGroup)
          val zkPath = s"$appkeyPath/groups/http/$group_name"
          val zkGroupPath = s"$appkeyPath/groups/http"
          if (ZkClient.exist(zkPath)) {
            //更新信息
            val oldData = ZkClient.getData(zkPath)
            ZkClient.setData(zkPath, data)
            BorpClient.saveOpt(actionType = ActionType.INSERT.getIndex, entityId = appkey, entityType = EntityType.updateHttpGroup, newValue = data,oldValue = oldData)
            ZkClient.setData(zkGroupPath, "")//更新父节点version, 使其自节点改变时触发watcher
            Json.toJson(ServiceModels.AddResult(200, s"group $group_name is updated successfully"))
          } else {
            //添加信息
            ZkClient.create(zkPath, data)
            BorpClient.saveOpt(actionType = ActionType.INSERT.getIndex, entityId = appkey, entityType = EntityType.createHttpGroup, newValue = data)
            ZkClient.setData(zkGroupPath, "")//更新父节点version, 使其自节点改变时触发watcher
            Json.toJson(ServiceModels.AddResult(200, s"group $group_name is created successfully"))
          }
        }
      }
      else {
        JsonHelper.errorJson("appkey不存在")
      }
    } catch {
      case e: Exception => LOG.error("添加http分组失败",e)
        JsonHelper.errorJson(e.getMessage)
    }
  }


  def deleteHlbGroup(env: String, appkey: String, group_name: String) = {
    val appkeyPath = getPath(env, appkey)
    val zkPath = appkeyPath + "/groups/http/" + group_name
    val zkGroupPath = s"$appkeyPath/groups/http"
    val result = if (!ZkClient.exist(zkPath)) {
      Json.toJson(ServiceModels.AddResult(500, s"group $group_name does not exist"))
    } else {
      val oldData = ZkClient.getData(zkPath)
      ZkClient.deleteWithChildren(zkPath)
      BorpClient.saveOpt(actionType = ActionType.DELETE.getIndex, entityId = appkey, entityType = EntityType.deleteHttpGroup,
        oldValue = oldData, newValue = "")
      ZkClient.setData(zkGroupPath, "")//更新父节点version, 使其自节点改变时触发watcher
      Json.toJson(ServiceModels.AddResult(200, s"group $group_name is delete successfully"))
    }
    result
  }

  def searchHlbGroup(env: String, appkey: String, group_name: String) = {
    val appkeyPath = getPath(env, appkey)
    val result = if (!ZkClient.exist(appkeyPath + "/groups/http/" + group_name)) {
      Json.toJson(ServiceModels.AddResult(500, s"group $group_name does not exist"))
    } else {
      ZkClient.getData(appkeyPath + "/groups/http/" + group_name)
    }
    result
  }


  def getHlbGroupByAppkey(env: String, appkey: String) = {
    val appkeyPath = getPath(env, appkey)
    val result = if (!ZkClient.exist(appkeyPath)) {
      Json.toJson(ServiceModels.AddResult(500, s"appkey $appkey does not exist"))
    } else {
      ZkClient.children(appkeyPath + "/groups/http")
    }
    result
  }
}
