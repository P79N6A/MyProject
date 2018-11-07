package com.sankuai.octo.log.utils

import com.sankuai.octo.log.constant.RTLogConstant
import com.sankuai.octo.statistic.helper.api
import com.sankuai.octo.statistic.util.{ExecutionContextFactory, tair}

import scala.concurrent.Future

/**
  * Created by wujinwu on 16/6/2.
  */
object IndicatorHelper {

  private val userNameTairKey = "RT_UserNames_" + RTLogConstant.localIP
  private val appkeyTairKey = "RT_Appkeys_" + RTLogConstant.localIP
  private val userAccessCountTairKey = "RT_UserAccessCount_" + RTLogConstant.localIP

  case class UserNames(set: Set[String] = Set())

  case class Appkeys(set: Set[String] = Set())

  case class UserAccessCount(count: Int = 0)

  private implicit val ec = ExecutionContextFactory.build(1)


  def getAppkeys = {
    //  从tair中回复数据,若无则新建
    Future {
      getAppkeyBytes match {
        case Some(bytes) => api.bytesToObject(bytes, classOf[Appkeys])
        case None => Appkeys()
      }
    }
  }

  private def getAppkeyBytes = {
    tair.getValue(appkeyTairKey)
  }

  def putAppkeys(appkeys: Appkeys) = {
    Future {
      tair.put(appkeyTairKey, appkeys)
    }
  }

  def getUserNames = {
    //  从tair中回复数据,若无则新建
    Future {
      getUserNameBytes match {
        case Some(bytes) => api.bytesToObject(bytes, classOf[UserNames])
        case None => UserNames()
      }
    }
  }

  private def getUserNameBytes = {
    tair.getValue(userNameTairKey)
  }

  def putUserNames(userNames: UserNames) = {
    Future {
      tair.put(userNameTairKey, userNames)
    }
  }

  def getUserAccessCount = {
    //  从tair中回复数据,若无则新建
    Future {
      getUserAccessCountBytes match {
        case Some(bytes) => api.bytesToObject(bytes, classOf[UserAccessCount])
        case None => UserAccessCount()
      }
    }
  }

  private def getUserAccessCountBytes = {
    tair.getValue(userAccessCountTairKey)
  }

  def putUserAccessCount(userAccessCount: UserAccessCount) = {
    Future {
      tair.put(userAccessCountTairKey, userAccessCount)
    }
  }
}
