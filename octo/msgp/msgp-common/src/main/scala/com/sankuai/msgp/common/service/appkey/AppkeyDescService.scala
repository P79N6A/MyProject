package com.sankuai.msgp.common.service.appkey

import com.sankuai.msgp.common.model.ServiceModels
import com.sankuai.msgp.common.config.db.msgp.Tables._
import com.sankuai.msgp.common.dao.appkey.AppkeyDescDao
import com.sankuai.msgp.common.model.ServiceModels.{Desc, User}
import play.api.libs.json.Json

object AppkeyDescService {
  def getAppkeyDesc(appkey: String): Desc = {
    val data = AppkeyDescDao.getAppkeyDesc(appkey)
    if (data.isDefined) {
      toDesc(data.get)
    } else {
      null
    }
  }

  def getOwners(appkey: String) = {
    val data = AppkeyDescDao.getAppkeyDesc(appkey)
    if (data.isDefined) {
      val owners = Json.parse(data.get.owners).asOpt[List[User]]
      owners.getOrElse(List[User]())
    } else {
      List[User]()
    }
  }

  def toDesc(data: AppkeyDesc#TableElementType): ServiceModels.Desc = {
    val owners = Json.parse(data.owners).asOpt[List[ServiceModels.User]]
    val observers = Json.parse(data.observers).asOpt[List[ServiceModels.User]]
    ServiceModels.Desc(data.name, data.appkey, Some(data.baseapp), owners.getOrElse(List()),
      observers, data.intro, data.category, Some(data.business), None,
      Some(data.base), Some(data.owt), Some(data.pdl), None, data.tags, data.reglimit, Some(data.createTime))
  }
}
