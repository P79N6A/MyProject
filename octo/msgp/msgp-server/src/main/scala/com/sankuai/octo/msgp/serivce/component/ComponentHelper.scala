package com.sankuai.octo.msgp.serivce.component

import com.sankuai.meituan.config.listener.IConfigChangeListener
import com.sankuai.msgp.common.config.MsgpConfig
import com.sankuai.msgp.common.model.{Base, Business}
import com.sankuai.msgp.common.service.org.{BusinessOwtService, OpsService}
import com.sankuai.octo.msgp.serivce.component.ComponentService.SimpleArtifactConfig
import com.sankuai.octo.msgp.serivce.service.ServiceCommon
import com.sankuai.octo.msgp.dao.component.ComponentDAO.{AppDescDomain, SimpleArtifact}
import com.sankuai.octo.msgp.dao.component._
import com.sankuai.octo.msgp.domain.Dependency
import com.sankuai.octo.msgp.model._
import com.sankuai.msgp.common.utils.client.Messager.{Alarm, MODE}
import com.sankuai.msgp.common.utils.{HttpUtil, StringUtil}
import com.sankuai.msgp.common.utils.helper.CommonHelper
import com.sankuai.msgp.common.utils.client.Messager
import com.sankuai.octo.statistic.util.ExecutionContextFactory
import org.slf4j.LoggerFactory
import play.api.libs.json.Json

import scala.collection.JavaConversions._
import scala.collection.JavaConverters._
import scala.concurrent.Future


/**
  * Created by yves on 16/8/31.
  */
object ComponentHelper {

  private val logger = LoggerFactory.getLogger(ComponentHelper.getClass)

  private val linkURL = s"https://123.sankuai.com/km/page/28097109"

  case class CheckResult(artifactConfigs: List[SimpleArtifactConfig], artifacts: List[SimpleArtifact])

  case class CheckResultWrapper(appDesc: AppDescDomain, brokenCheckResults: CheckResult, requiredBrokenCheckResults: CheckResult)

  implicit val checkResultReads = Json.reads[CheckResult]
  implicit val checkResultWrites = Json.writes[CheckResult]

  implicit val checkResultWrapperReads = Json.reads[CheckResultWrapper]
  implicit val checkResultWrapperWrites = Json.writes[CheckResultWrapper]

  val OCTO_URL = if (CommonHelper.isOffline) {
    "http://octo.test.sankuai.com"
  } else {
    "http://octo.sankuai.com"
  }

  private val TTT_API_HOST: String = "http://api.cmdb.dp/api/v0.1/cmdb"


  val CMPT_FREQUNTLY_USED = Set(("com.meituan.inf", "xmd-common-log4j2"),
    ("com.meituan.inf", "xmd-log4j2"),
    ("com.sankuai", "inf-bom"),
    ("com.sankuai", "xmd-bom"),
    ("com.sankuai.octo", "idl-common"),
    ("com.meituan.service.mobile", "mtthrift"),
    ("com.sankuai.octo", "mns-invoker"),
    ("com.sankuai.meituan", "mtconfig-client"),
    ("com.meituan.mtrace", "mtrace-http"),
    ("com.meituan.mtrace", "mtrace"),
    ("com.meituan", "jmonitor"),
    ("com.meituan.image", "client"),
    ("com.meituan.service.inf", "kms-java-client"),
    ("com.taobao.tair", "tair3-client"),
    ("com.dianping.squirrel", "squirrel-client"),
    ("com.meituan.cache", "redis-cluster-client"),
    ("com.dianping.zebra", "zebra-api"),
    ("com.meituan.mafka", "mafka-client"),
    ("com.dianping.dpsf", "dpsf-net"),
    ("com.dianping.cat", "cat-client"),
    ("com.cip.crane", "crane-client"),
    ("com.dianping.lion", "lion-client"))

  var CMPT_RECOMMENDED: Set[(String, String, String)] = {
    val value = MsgpConfig.get("cpmt.recommended", "")
    MsgpConfig.addListener("cpmt.recommended", new IConfigChangeListener() {
      def changed(key: String, oldValue: String, newValue: String) = {
        logger.info("cpmt.recommended:{}", newValue)
        CMPT_RECOMMENDED = initRecommendedComponent(newValue)
      }
    })
    initRecommendedComponent(value)
  }

  private implicit val ec = ExecutionContextFactory.build(1)

  /**
    * 上传依赖
    *
    * @param text
    */
  def uploadDependency(text: String) = {
    Future {
      val requestOpt = Json.parse(text).validate[ComponentDAO.DependencyUpload]
      //同一个应用,上传一次的时间是一致的
      val now = System.currentTimeMillis()
      requestOpt.map {
        request =>
          val appDesc = request.appDesc
          logger.info(s"${appDesc.app}, ${appDesc.appkey}, ${appDesc.groupId}:${appDesc.artifactId}, uploadTime: ${now}")
          val packaging = appDesc.packaging

          //执行依赖检查
          val simpleArtifact = SimpleArtifact(appDesc.groupId, appDesc.artifactId, appDesc.version)
          val isNotDependency = !checkIsDependency(simpleArtifact)

          //如果该项目是别人的依赖, 则删除该项目所有信息
          if (!isNotDependency) {
            ComponentDAO.deleteArtifact(simpleArtifact)
            ActivenessDAO.deleteArtifact(simpleArtifact)
          }

          //不是依赖包才插入
          if (isNotDependency && !packaging.equalsIgnoreCase("pom") && !packaging.equalsIgnoreCase("maven-plugin")) {
            val business = if (StringUtil.isBlank(appDesc.business)) {
              if (StringUtil.isNotBlank(appDesc.owt)) {
                BusinessOwtService.getBusiness(appDesc.base, appDesc.owt)
              } else {
                //do something
                Business.other.toString
              }
            } else {
              appDesc.business
            }
            //TODO temporary
            val appDescNew = appDesc.copy(business = doSomeAdjustmentForBusiness(business))
            logger.info(s"valid app: ${appDesc.app}, appkey: ${appDesc.appkey}, ${appDesc.groupId}:${appDesc.artifactId}, artifacts size: ${request.artifacts.size}")
            request.artifacts.foreach {
              artifact =>
                val category = getCategoryOfComponent(artifact)
                ComponentDAO.insertOrUpdate(appDescNew, artifact, category, now)
            }

            //删除废旧组件
            val appsUploadTime = ComponentDAO.getUploadTime(appDescNew.groupId, appDescNew.artifactId)
            if (appsUploadTime.size > 1) {
              //do the delete action
              val uploadTimeList = appsUploadTime.sortWith(_ > _)
              val oldUploadTimeList = uploadTimeList.drop(1)
              ComponentDAO.deleteDeprecatedDependencies(appDescNew.groupId, appDescNew.artifactId, oldUploadTimeList)
            }

            //每上传一次,活跃度增加1
            ActivenessService.insertOrUpdate(appDescNew, now)
          } else {
            logger.info(s"invalid app: ${appDesc.app}, appkey: ${appDesc.appkey}, ${appDesc.groupId}:${appDesc.artifactId}, uploadTime: ${now}")
          }
      }
    }
  }

  def doSomeAdjustmentForBusiness(business: String) = {
    val businessDesc = if (business.equalsIgnoreCase("猫眼文化")) {
      "猫眼电影"
    } else if (business.equalsIgnoreCase("企业平台研发部")) {
      "企业平台"
    } else if (business.equalsIgnoreCase("到店餐饮事业群")) {
      "到店餐饮"
    } else if (business.equalsIgnoreCase("外卖配送事业群")) {
      "外卖配送"
    } else {
      business
    }
    businessDesc
  }

  /**
    * 上传Bom信息
    *
    * @param text
    */
  def uploadBomInformation(text: String) = {
    Future {
      val requestOpt = Json.parse(text).validate[AppBomInfoDAO.AppBomInfoUpload]
      val now = System.currentTimeMillis()
      requestOpt.map {
        request =>
          val appDesc = request.appDesc
          val businessDesc = if (StringUtil.isBlank(appDesc.business)) {
            if (StringUtil.isNotBlank(appDesc.owt)) {
              BusinessOwtService.getBusiness(appDesc.base, appDesc.owt)
            } else {
              //do something
              Business.other.toString
            }
          } else {
            appDesc.business
          }
          val record = AppBomInfoDAO.AppBomInfoDomain(doSomeAdjustmentForBusiness(businessDesc), appDesc.owt, appDesc.pdl, appDesc.app, appDesc.appkey,
            appDesc.base, appDesc.groupId, appDesc.artifactId, appDesc.version, appDesc.packaging,
            request.infBomUsed, request.infBomVersion, request.xmdBomUsed, request.xmdBomVersion, now, now)
          AppBomInfoDAO.insertOrUpdate(record)
      }
    }
  }

  def uploadBrokenInformation(text: String) = {
    Future {
      logger.info(text)
      doNotificationAction(text)
    }
  }

  def doNotificationAction(text: String) = {
    Json.parse(text).validate[CheckResultWrapper].fold(
      { errorInfo =>
        logger.error(s"parsing the checking result of artifacts failed. $errorInfo")
      }, {
        checkResult =>
          val appDesc = checkResult.appDesc
          val brokenCheckResults = checkResult.brokenCheckResults
          //TODO do not handle this condition
          //val requiredBrokenCheckResults = checkResult.requiredBrokenCheckResults

          val usernames = getUsernameByAppDesc(appDesc)

          val (brokenList, waringList) = brokenCheckResults.artifactConfigs.zip(brokenCheckResults.artifacts).partition(
            x =>
              x._1.action.equalsIgnoreCase(ComponentAction.BROKEN.toString)
          )
          if (usernames.nonEmpty) {
            if (brokenList.nonEmpty) {
              notifyOwner(appDesc, usernames, brokenList, ComponentAction.BROKEN.toString)
            }
            if (waringList.nonEmpty) {
              notifyOwner(appDesc, usernames, waringList, ComponentAction.WARNING.toString)
            }
          } else {
            val message = s"app owner is empty. appkey: ${appDesc.appkey}, app: ${appDesc.app}, base:${appDesc.base}. groupId: ${appDesc.artifactId}, artifactId: ${appDesc.artifactId}"
            val alarm = Alarm("app owner is empty", message)
            Messager.sendSingleMessage("tangye03", alarm, Seq(MODE.XM))
            logger.info(s"app owner is empty. appkey: ${appDesc.appkey}, app: ${appDesc.app}, artifactId: ${appDesc.artifactId}")
          }
      })
  }

  /**
    * 通知项目负责人
    *
    * @param appDesc 项目描述
    * @param users   负责人列表
    * @param records 通知内容
    * @param action  通知类型
    */
  def notifyOwner(appDesc: AppDescDomain, users: List[String], records: List[(SimpleArtifactConfig, SimpleArtifact)], action: String) = {
    val messageSubject = "组件低版本提醒"
    val prefixStr = if (action.equalsIgnoreCase(ComponentAction.BROKEN.toString)) {
      s"发布中的项目(${appDesc.groupId} : ${appDesc.artifactId}) 存在组件版本不满足要求的情况: \n"
    } else {
      s"发布中的项目(${appDesc.groupId} : ${appDesc.artifactId}) 存在组件版本使用过低的情况: \n"
    }
    val messageBody = if (action.equalsIgnoreCase(ComponentAction.BROKEN.toString)) {
      records.zipWithIndex.map {
        case ((artifactConfig, artifact), index) =>
          val appIdentity = if (artifact.groupId.equalsIgnoreCase("com.meituan.image") && artifact.artifactId.equalsIgnoreCase("client")) {
            s"${artifact.groupId} : ${artifact.artifactId}"
          } else {
            artifact.artifactId
          }
          s"${index + 1}, $appIdentity(要求版本: ${getVersionDesc(artifactConfig.version)}, 实际版本: ${artifact.version})"
      }.mkString("\n")
    } else {
      records.zipWithIndex.map {
        case ((artifactConfig, artifact), index) =>
          val appIdentity = if (artifact.groupId.equalsIgnoreCase("com.meituan.image") && artifact.artifactId.equalsIgnoreCase("client")) {
            s"${artifact.groupId} : ${artifact.artifactId}"
          } else {
            artifact.artifactId
          }
          val recVersion = getRecommendedComponent(artifact.groupId, artifact.artifactId).getOrElse(("", "", ""))._3
          s"${index + 1}, $appIdentity(要求版本: ${getVersionDesc(recVersion)}, 实际版本: ${artifact.version})"
      }.mkString("\n")
    }
    val suffixStr = if (action.equalsIgnoreCase(ComponentAction.BROKEN.toString)) {
      s"\n由于上述组件不满足最低版本要求，本次发布已终止。请参考[ 基础架构组件推荐清单 | $linkURL]升级至推荐的版本。如有更多疑问, 请咨询OCTO技术支持。"
    } else {
      s"\n低版本组件将逐渐停止维护, 后期不符合版本要求的服务将禁止发布。请参照[ 基础架构组件推荐清单 | $linkURL] 升至推荐的版本, 以体验更丰富和稳定的功能。\n如有更多疑问, 请咨询OCTO技术支持。"
    }
    val message = prefixStr + messageBody + suffixStr
    users.foreach {
      username =>
        val alarm = Alarm(messageSubject, message)
        Messager.sendSingleMessage(username, alarm, Seq(MODE.XM))
    }
  }


  /**
    * 根据groupId和关键字获取artifactId
    *
    * @param groupId
    * @param keyword
    * @param limitNumber
    * @return
    */
  def getArtifactIdByKeyword(groupId: String, keyword: String, limitNumber: Int) = {
    val result = ComponentDAO.getArtifactIdByKeyword(groupId, keyword).distinct
    if (result.length > limitNumber) {
      result.take(limitNumber)
    } else {
      result
    }
  }

  /**
    * 根据关键字获取groupId
    *
    * @param keyword
    * @param limitNumber
    * @return
    */
  def getGroupIdByKeyword(keyword: String, limitNumber: Int) = {
    val result = ComponentDAO.getGroupIdByKeyword(keyword).distinct
    if (result.length > limitNumber) {
      result.take(limitNumber)
    } else {
      result
    }
  }

  /**
    * 推荐的应用
    *
    * @param groupId
    * @param artifactId
    * @return
    */
  def getRecommendedComponent(groupId: String, artifactId: String) = {
    CMPT_RECOMMENDED.find { x =>
      x._1.equalsIgnoreCase(groupId) && x._2.equalsIgnoreCase(artifactId)
    }
  }

  def initRecommendedComponent(value: String) = {
    if (StringUtil.isNotBlank(value)) {
      val configs = value.trim.split(";")
      configs.map {
        config =>
          val configArray = config.trim.split(":")
          (configArray.apply(0), configArray.apply(1), configArray.apply(2))
      }.toSet
    } else {
      Set[(String, String, String)]()
    }
  }

  def getDefaultComponent = {
    CMPT_FREQUNTLY_USED.map {
      x =>
        ComponentDAO.SimpleArtifact(x._1, x._2, "")
    }
  }

  /**
    * 获取组件的所有版本(不含数量)
    *
    * @param groupId
    * @param artifactId
    * @return
    */
  def getVersion(groupId: String, artifactId: String) = {
    ComponentDAO.getVersion(groupId, artifactId)
  }

  /**
    * 初始化Owt和Pdl
    *
    * @return
    */

  def getOwt(business: String) = {
    ComponentDAO.getOwt(business).asJava
  }

  def getPdl(owt: String) = {
    ComponentDAO.getPdl(owt).filter(x => !StringUtil.isBlank(x)).sorted.asJava
  }

  def getApp(owt: String, pdl: String) = {
    ComponentDAO.getApp(owt, pdl).filter(x => !StringUtil.isBlank(x))
  }

  def getBusinessByOwt(owt: String) = {
    BusinessOwtService.getBusiness(owt)
  }

  /**
    * 通过内置的分类信息更新组件分类信息
    *
    * @param category
    * @return
    */
  def updateCategory(category: String) = {
    val categoryNameList = ComponentCategory.categoryNameMap.keys.toList
    if (!categoryNameList.contains(category)) {
    } else {
      val categoryName = ComponentCategory.getCategoryName(category)
      val dependencyList = ComponentCategoryMap.getDependencyListByCategory(ComponentCategory.withName(categoryName))
      if (dependencyList.isEmpty) {
        "category does not exist"
      } else {
        CategoryDAO.updateCategory(dependencyList, category)
        "update category successfully"
      }
    }
  }

  /**
    * 增加分类中的组件
    *
    * @param category
    * @return
    */
  def updateCategoryPlus(category: String, groupId: String, artifactId: String) = {
    val categoryNameList = ComponentCategory.categoryNameMap.keys.toList
    if (!categoryNameList.contains(category)) {
      "category does not exist"
    } else {
      val dependencyList = new Dependency(groupId, artifactId) :: List[Dependency]()
      CategoryDAO.updateCategory(dependencyList, category)
      "update category successfully"
    }
  }

  def getCategoryOfComponent(component: ComponentDAO.SimpleArtifact) = {
    val dependency = new Dependency(component.groupId, component.artifactId)
    ComponentCategoryMap.getCategoryByDependency(dependency)
  }

  /**
    * 根据项目描述信息得到项目负责人
    *
    */
  def getUsernameByAppDesc(appDesc: AppDescDomain) = {
    var usernames = List[String]()
    val appkey = appDesc.appkey
    if (StringUtil.isNotBlank(appkey)) {
      //首先从octo获取
      usernames = ServiceCommon.getOwnersLoginWhole(appkey)
    } else {
      if (appDesc.base.equalsIgnoreCase(Base.meituan.toString)) {
        //如果发布项没有配置appkey, 则通过发布项名获取
        val app = appDesc.app
        if (StringUtil.isNotBlank(app) && usernames.isEmpty) {
          //#1  app是appkey的情形
          usernames = ServiceCommon.getOwnersLoginWhole(app)

          //#2 从ops给的数据库查询
          if (usernames.isEmpty) {
            val usernameOpt = AppAdminDAO.getAdminsUsername(app)
            if (usernameOpt.isDefined) {
              val usernameString = usernameOpt.get
              usernames = usernameString.split(",").filter { x => StringUtil.isNotBlank(x) }.toList
            }
          }
          //#3 从ops获取, 上海侧服务不包括在内
          if (usernames.isEmpty) {
            val tagOpt = OpsService.getAppTag(app)
            if (tagOpt.isDefined) {
              usernames = OpsService.getRDAdmin(tagOpt.get).asScala.toList
            }
          }
        }
      } else {
        val map = Map("projects" -> appDesc.app, "type" -> "project")
        try {
          val text = HttpUtil.httpGetRequest(TTT_API_HOST, map)
          val rdsOpt = (Json.parse(text) \ "results" \ appDesc.app \ "project" \ "rd_duty").asOpt[String]
          if (rdsOpt.isDefined) {
            usernames = rdsOpt.get.split(",").toList
          }
        } catch {
          case e: Exception =>
            logger.error(s"get usernames error", e)
        }
      }
    }
    usernames
  }

  /**
    * 申请删除依赖
    */
  def deleteApplication(username: String, groupId: String, artifactId: String, reason: String) = {
    val domain = ServiceCommon.OCTO_URL
    val approveUrl = s"$domain/component/delete/artifact?username=$username&groupId=$groupId&artifactId=$artifactId"
    val rejectUrl = s"$domain/component/delete/reject?username=$username&groupId=$groupId&artifactId=$artifactId"
    val message = s"发布项删除申请\ngroupId: $groupId \nartifactId: $artifactId\n申请人员: $username \n申请理由: $reason\n[批准|$approveUrl]   [拒绝|$rejectUrl]"
    val application = Alarm("发布项删除申请", message, null)
    logger.info("发布项删除申请: " + application)
    val modes = Seq(MODE.XM)
    Messager.sendSingleMessage("zhanghui24", application, modes)
    Messager.sendSingleMessage("zhangyun16", application, modes)
  }

  /**
    * 申请删除依赖
    */
  def deleteReject(username: String, groupId: String, artifactId: String) = {
    val message = s"发布项($groupId: $artifactId)删除申请未通过, 如有疑问请联系tangye03。"
    val application = Alarm("发布项删除申请结果", message, null)
    val modes = Seq(MODE.XM)
    Messager.sendSingleMessage(username, application, modes)
  }


  /**
    * 清除废弃的依赖
    */
  def deleteDeprecatedDependencies() = {
    /**
      * 1. fetching all app
      * 2. traverse all app
      */
    Future {
      val appsAndUploadTime = ComponentDAO.getAppAndUploadTime
      appsAndUploadTime.groupBy(x => (x._1, x._2)).foreach {
        case (app, list) =>
          if (list.size > 1) {
            //do the delete action
            val uploadTimeList = list.map(_._3).sortWith(_ > _)
            val oldUploadTimeList = uploadTimeList.drop(1)
            ComponentDAO.deleteDeprecatedDependencies(app._1, app._2, oldUploadTimeList)
          }
      }
    }
  }

  /**
    * 更新business下错误的owt, 更新至正确的值
    */
  def updateBusiness(business: String, owt: String) = {
    Future {
      if (StringUtil.isNotBlank(owt)) {
        ComponentDAO.updateBusiness(List(owt), business)
      } else {
        val owts = ComponentDAO.getOwt(business)
        logger.info(s"update owts: ${owts.mkString(",")}")
        val owtBusinessPair = owts.map {
          owt =>
            val _business = BusinessOwtService.getBusiness("", owt)
            (owt, _business)
        }.filter(!_._2.equalsIgnoreCase(Business.other.toString))

        owtBusinessPair.groupBy(_._2).foreach {
          case (_business, list) =>
            ComponentDAO.updateBusiness(list.map(_._1), _business)
            logger.info(s"update business: ${_business}, owts: ${list.map(_._1).mkString(",")}, successfully")
        }
      }
    }
  }

  /**
    * 更新正确的business
    */
  def updateBusinessWithCorrectValue(errorBusiness: String, correctBusiness: String) = {
    Future {
      ComponentDAO.updateBusinessWithCorrectValue(errorBusiness, correctBusiness)
    }
  }

  /**
    * 删除无效的发布项目
    * 判定规则是: 现在处于app_bom中的项目, 如果现在有被依赖的情况, 说明不是一个有效项目,执行删除操作
    */
  def deleteInvalidArtifact() = {
    Future {
      val allApp = AppBomInfoDAO.getAllApp
      allApp.foreach {
        artifact =>
          //check
          if (checkIsDependency(artifact)) {
            ComponentDAO.deleteArtifact(artifact)
            ActivenessDAO.deleteArtifact(artifact)
            AppBomInfoDAO.deleteArtifact(artifact)
          }
      }
    }
  }

  /**
    * 手动删除发布项目
    *
    */
  def deleteArtifact(username: String, groupId: String, artifactId: String) = {
    Future {
      val simpleArtifact = SimpleArtifact(groupId, artifactId, "")
      ComponentDAO.deleteArtifact(simpleArtifact)
      ActivenessDAO.deleteArtifact(simpleArtifact)
      AppBomInfoDAO.deleteArtifact(simpleArtifact)
      if (StringUtil.isNotBlank(username)) {
        val message = s"发布项($groupId: $artifactId)删除申请已通过, 项目已从统计结果中移除。"
        val application = Alarm("发布项删除申请结果", message, null)
        val modes = Seq(MODE.XM)
        Messager.sendSingleMessage(username, application, modes)
      }
    }
  }

  /**
    * 删除app_dependency表中一条数据
    * 原因是jumper登录mysql删除数据时需要审批
    *
    * @param id
    * @return
    */
  def deleteItem(id: Long) = {
    ComponentDAO.deleteItem(id)
  }

  /**
    * 检查是否是依赖
    *
    * @param artifact 待检查发布项
    * @return true: 是一个依赖的jar包, false: 不是一个依赖的jar包
    */
  def checkIsDependency(artifact: SimpleArtifact) = {
    val checkResult = ComponentDAO.isDependency(artifact)
    if (checkResult.isDefined) true else false
  }

  def getVersionDesc(version: String): String = {
    val sb: StringBuilder = new StringBuilder
    val versions: Array[String] = version.split(";")
    var versionFixedString: String = ""
    if (versions.length > 1) {
      versionFixedString = versions(1)
    }
    if (StringUtil.isNotBlank(versionFixedString)) {
      sb.append("version = ")
      val versionFixedArray: Array[String] = versionFixedString.split(",")
      for (versionFixed <- versionFixedArray) {
        sb.append(versionFixed)
        sb.append(" or ")
      }
    }
    sb.append(getVersionRangeSpec(versions(0)))
    sb.toString
  }


  /**
    * 把版本区间 转换为不等式 表达式
    *
    * @param versionRange 版本区间
    * @return eg. 1.1.2 < version < 1.3.0
    */
  def getVersionRangeSpec(versionRange: String): String = {
    var result: String = ""
    val versionRangeArray = versionRange.split(",")
    if (versionRangeArray.length == 1) {
      if (!versionRange.contains(")") && !versionRange.contains("]")) {
        result = "version" + " >= " + versionRange
      }
      else {
        result = "version" + " == " + removeBracket(versionRangeArray(0))
      }
    }
    else if (versionRangeArray.length == 2) {
      val startVersion: String = versionRangeArray(0)
      val endVersion: String = versionRangeArray(1)
      val startVersionAdjusted: String = removeBracket(startVersion)
      val endVersionAdjusted: String = removeBracket(endVersion)
      if (startVersion.startsWith("(")) {
        if (startVersionAdjusted.isEmpty && !endVersionAdjusted.isEmpty) {
          if (endVersion.endsWith("]")) {
            result = "version" + " <= " + endVersionAdjusted
          }
          else if (endVersion.endsWith(")")) {
            result = "version" + " < " + endVersionAdjusted
          }
        }
        else if (!startVersionAdjusted.isEmpty && endVersionAdjusted.isEmpty) {
          result = "version" + " > " + startVersionAdjusted
        }
        else if (startVersionAdjusted.isEmpty && endVersionAdjusted.isEmpty) {
          result = ""
        }
        else {
          if (endVersion.endsWith("]")) {
            result = startVersionAdjusted + " < " + "version" + " <= " + endVersionAdjusted
          }
          else if (endVersion.endsWith(")")) {
            result = startVersionAdjusted + " < " + "version" + " < " + endVersionAdjusted
          }
        }
      }
      else if (startVersion.startsWith("[")) {
        if (startVersionAdjusted.isEmpty && !endVersionAdjusted.isEmpty) {
          if (endVersion.endsWith("]")) {
            result = "version" + " <= " + endVersionAdjusted
          }
          else if (endVersion.endsWith(")")) {
            result = "version" + " < " + endVersionAdjusted
          }
        }
        else if (!startVersionAdjusted.isEmpty && endVersionAdjusted.isEmpty) {
          result = "version" + " >= " + startVersionAdjusted
        }
        else if (startVersionAdjusted.isEmpty && endVersionAdjusted.isEmpty) {
          result = ""
        }
        else {
          if (endVersion.endsWith("]")) {
            result = startVersionAdjusted + " <= " + "version" + " <= " + endVersionAdjusted
          }
          else if (endVersion.endsWith(")")) {
            result = startVersionAdjusted + " <= " + "version" + " < " + endVersionAdjusted
          }
        }
      }
    }
    else if (versionRangeArray.length > 2) {
      val list = versionRangeArray.toList

      val current = list.subList(0, 2)
      val remainder = list.subList(2, list.size)
      result = getVersionRangeSpec(current.mkString(",")) + " or " + getVersionRangeSpec(remainder.mkString(","))
    }
    else {
      result = versionRange
    }
    result
  }

  def removeBracket(exp: String) = exp.replace("(", "").replace("[", "").replace(")", "").replace("]", "")

}
