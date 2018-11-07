package com.sankuai.octo.msgp.serivce.component

import com.sankuai.meituan.auth.util.UserUtils
import com.sankuai.msgp.common.config.MsgpConfig
import com.sankuai.msgp.common.service.org.BusinessOwtService
import com.sankuai.msgp.common.utils.StringUtil
import com.sankuai.octo.msgp.dao.component.AppConfigDAO
import com.sankuai.octo.msgp.dao.component.AppConfigDAO.{AppConfigDomain, AppConfigSimple}
import com.sankuai.octo.statistic.constant.Constants
import play.api.libs.json.Json

/**
  * Created by yves on 16/11/21.
  */
object AppConfigService {

  case class Org(base: String, business: String, owt: String, pdl: String)

  val ADD_CONFIG_SUCCESS = "配置添加成功"
  val ADD_CONFIG_FAILED = "配置添加失败"
  val DELETE_CONFIG_FAILED = "配置删除失败"
  val DELETE_CONFIG_SUCCESS = "配置删除成功"
  val ILLEGAL_ARGUMENT = "参数错误"

  //配置信息(提供给插件)
  def getBlackListConfig(base: String, owt: String, pdl: String, app: String) = {
    val business = BusinessOwtService.getBusiness(base, owt)
    val allBlackListConfigs = AppConfigDAO.getAllBlackListConfig
    val allBusinessBlackListConfigs = allBlackListConfigs.filter(x=> x.base == base && x.business == Constants.ALL && x.owt == Constants.ALL && x.pdl == Constants.ALL)
    val businessBlackListConfigs = allBlackListConfigs.filter(x=> x.base == base && x.business == business && x.owt == Constants.ALL && x.pdl == Constants.ALL)
    val owtBlackListConfigs = allBlackListConfigs.filter(x=> x.base == base && x.business == business && x.owt == owt && x.pdl == Constants.ALL)
    val pdlBlackListConfigs = allBlackListConfigs.filter(x=> x.base == base && x.business == business && x.owt == owt && x.pdl == pdl)

    //所有的黑名单
    val configs = allBusinessBlackListConfigs ::: businessBlackListConfigs ::: owtBlackListConfigs ::: pdlBlackListConfigs

    val simpleConfigs = if (configs.nonEmpty) {
      configs.map {
        x =>
          val appConfigId = x.id
          //该条黑名单下的白名单
          val allWhiteLisConfigs = AppConfigDAO.getWhiteListConfig(appConfigId)
          val businessFilter = allWhiteLisConfigs.filter(x=> x.business == business && x.owt == Constants.ALL && x.pdl == Constants.ALL && x.app == Constants.ALL)
          val owtFilter = allWhiteLisConfigs.filter(x=> x.business == business && x.owt == owt && x.pdl == Constants.ALL && x.app == Constants.ALL)
          val pdlFilter = allWhiteLisConfigs.filter(x=> x.business == business && x.owt == owt && x.pdl == pdl && x.app == Constants.ALL)
          val appFilter = allWhiteLisConfigs.filter(x=> x.business == business && x.owt == owt && x.pdl == pdl && x.app == app)

          //只要一个筛选不为空,说明位于白名单中
          if(businessFilter.nonEmpty || owtFilter.nonEmpty || pdlFilter.nonEmpty || appFilter.nonEmpty){
            None
          }else{
            Some(AppConfigDAO.AppConfigSimple(x.groupId, x.artifactId, x.version, x.action))
          }
      }.filter(_.isDefined).map(_.get)
    } else {
      List[AppConfigSimple]()
    }
    simpleConfigs.distinct
  }

  def getWhiteListConfig(appConfigId: Int) = {
    AppConfigDAO.getWhiteListConfig(appConfigId)
  }

  //提供给配置平台
  def getRichBlackListConfig(groupId: String, artifactId: String, base: String, business: String, owt: String, pdl: String, action: String) = {
    AppConfigDAO.getRichConfig(toOptionParam(groupId), toOptionParam(artifactId), toOptionParam(base), toOptionParam(business), toOptionParam(owt), toOptionParam(pdl), toOptionParam(action))
  }

  def toOptionParam(param: String) = {
    //如果是ALL 则包含该部门下所有的配置都取出来
    if (StringUtil.isBlank(param) || Constants.ALL.equalsIgnoreCase(param)) {
      None
    } else {
      Some(param)
    }
  }

  //增加配置
  def addBlackListConfig(configJson: String) = {
    val admin = MsgpConfig.get("component.admin", "tangye03,yangrui08,zhangxi").split(",").toSet
    if (admin.contains(UserUtils.getUser.getLogin)) {
      val configValidation = Json.parse(configJson).validate[AppConfigDAO.AppConfigDomain]
      if (configValidation.isSuccess) {
        val config = configValidation.get
        val base = config.base
        val business = config.business
        val owt = config.owt
        val pdl = config.pdl
        val groupId = config.groupId
        val artifactId = config.artifactId
        val version = config.version

        if (StringUtil.isBlank(base) || StringUtil.isBlank(business) || StringUtil.isBlank(base)
          || StringUtil.isBlank(groupId) || StringUtil.isBlank(artifactId) || StringUtil.isBlank(version)) {
          throw new IllegalArgumentException(ILLEGAL_ARGUMENT)
        }
        //获取列表
        val orgList = getOwtPdlList(base, business, owt, pdl)
        if (orgList.nonEmpty) {
          val configList = orgList.map {
            org =>
              AppConfigDomain(org.business, org.owt, org.pdl, base, groupId, artifactId, version, config.action)
          }
          AppConfigDAO.batchInsert(configList)
          ADD_CONFIG_SUCCESS
        } else {
          throw new IllegalArgumentException(ILLEGAL_ARGUMENT)
        }
      } else {
        throw new IllegalArgumentException(ILLEGAL_ARGUMENT)
      }
    } else {
      throw new IllegalArgumentException(ILLEGAL_ARGUMENT)
    }
  }

  def addWhiteListConfig(configJson: String) = {
    val whiteListValidation = Json.parse(configJson).asOpt[AppConfigDAO.AppConfigExtDomain]
    whiteListValidation match {
      case Some(whiteList) =>
        AppConfigDAO.insertWhiteListConfig(whiteList)
      case None =>
        throw new IllegalArgumentException(ILLEGAL_ARGUMENT)
    }
  }

  //根据business, owt, pdl 得到org列表
  def getOwtPdlList(base: String, business: String, owt: String, pdl: String) = {
    //所有事业群
    if (Constants.ALL.equalsIgnoreCase(business)) {
      //所有事业群的
      List(Org(base, Constants.ALL, Constants.ALL, Constants.ALL))
    } else {
      //单个事业群下的
      if (Constants.ALL.equalsIgnoreCase(owt) && Constants.ALL.equalsIgnoreCase(pdl)) {
        List(Org(base, business, Constants.ALL, Constants.ALL))
      } else if (!Constants.ALL.equalsIgnoreCase(owt) && Constants.ALL.equalsIgnoreCase(pdl)) {
        //添加owt下所有pdl
        List(Org(base, business, owt, Constants.ALL))
      } else if (!Constants.ALL.equalsIgnoreCase(owt) && !Constants.ALL.equalsIgnoreCase(pdl)) {
        //添加单个pdl
        List(Org(base, business, owt, pdl))
      } else {
        List()
      }
    }
  }

  //删除配置
  def deleteBlackListConfig(configJson: String) = {
    val admin = MsgpConfig.get("component.admin", "tangye03,yangrui08,zhangxi").split(",").toSet
    if (admin.contains(UserUtils.getUser.getLogin)) {
      val configValidation = Json.parse(configJson).validate[List[AppConfigDAO.AppConfigDomain]]
      if (configValidation.isSuccess) {
        val configs = configValidation.get
        configs.foreach {
          config =>
            AppConfigDAO.deleteBlackListConfig(config)
        }
        DELETE_CONFIG_SUCCESS
      } else {
        throw new IllegalArgumentException(ILLEGAL_ARGUMENT)
      }
    } else {
      throw new IllegalArgumentException(ILLEGAL_ARGUMENT)
    }
  }

  def deleteWhiteListConfig(configJson: String) = {
    val admin = MsgpConfig.get("component.admin", "tangye03,yangrui08,caojiguang").split(",").toSet
    if (admin.contains(UserUtils.getUser.getLogin)) {
      val whiteListValidation = Json.parse(configJson).asOpt[AppConfigDAO.AppConfigExtDomain]
      whiteListValidation match {
        case Some(whiteList) =>
          AppConfigDAO.deleteWhiteListConfig(whiteList)
        case None =>
          throw new IllegalArgumentException(ILLEGAL_ARGUMENT)
      }
    } else {
      throw new IllegalArgumentException(ILLEGAL_ARGUMENT)
    }
  }
}
