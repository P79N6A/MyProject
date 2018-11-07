package com.sankuai.octo.msgp.serivce.component

import java.sql.Date

import com.sankuai.msgp.common.utils.StringUtil
import com.sankuai.msgp.common.utils.helper.CommonHelper
import com.sankuai.octo.msgp.serivce.component.ComponentService.AppCount
import com.sankuai.octo.msgp.dao.component.{ActivenessDAO, ComponentDAO}
import com.sankuai.octo.msgp.dao.component.ComponentDAO.AppDescDomain
import org.joda.time.DateTime

/**
  * Created by yves on 16/9/27.
  *
  * MSGP 中 组件活跃度统计。
  *
  */
object ActivenessService {

  private val debug = false

  case class AppActiveness(apps: List[String], counts: List[Int])

  def getAppActiveness(base: String, business: String, owt: String, pdl: String) = {
    val result = if (CommonHelper.isOffline && !debug) {
      AppActiveness(List[String](), List[Int]())
    } else {
      val startDate = new DateTime().withDayOfWeek(1)
      val endDate = startDate.plusDays(6)

      val rawData = ActivenessDAO.getAppActiveness(base, business, owt, pdl,
        new Date(startDate.getMillis), new Date(endDate.getMillis))
      val groupResult = rawData.groupBy(_.appArtifactId)

      val count = groupResult.map {
        case (appName, dataList) =>
          (appName, dataList.map(_.count).sum)
      }.toList.sortBy(_._2)

      val topApp = if (count.size > 10) {
        count.takeRight(10)
      } else {
        count
      }
      AppActiveness(topApp.map(_._1), topApp.map(_._2))
    }
    result
  }

  def insertOrUpdate(appDesc: AppDescDomain, upLoadTime: Long) = {
    val date = new Date(upLoadTime)
    ActivenessDAO.insertOrUpdate(appDesc.base, appDesc.business, appDesc.owt, appDesc.pdl, appDesc.app,
      appDesc.groupId, appDesc.artifactId, appDesc.version, appDesc.appkey, date)
  }

  /**
    * 获取应用分布
    *
    */

  def getAppCount(base: String, business: String, owt: String, pdl: String) = {
    val result = if (CommonHelper.isOffline && !debug) {
      AppCount
    } else {
      val appsDesc = ComponentDAO.getAllUser(base, business, owt, pdl)

      val groupResult = if (StringUtil.isBlank(business)) {
        ("事业群", appsDesc.groupBy(_.business))
      } else if (StringUtil.isNotBlank(business) && StringUtil.isBlank(owt) && StringUtil.isBlank(pdl)) {
        (business, appsDesc.groupBy(_.owt))
      } else if (StringUtil.isNotBlank(business) && !StringUtil.isBlank(owt) && StringUtil.isBlank(pdl)) {
        (owt, appsDesc.groupBy(_.pdl))
      } else if (StringUtil.isNotBlank(business) && !StringUtil.isBlank(owt) && !StringUtil.isBlank(pdl)) {
        (pdl, appsDesc.groupBy(_.pdl))
      } else {
        (business, appsDesc.groupBy(_.business))
      }
      val data = groupResult._2.map {
        case (key, value) =>
          (key, value.size)
      }.toList
      AppCount(data.map(_._1), data.map(_._2))
    }
    result
  }
}
