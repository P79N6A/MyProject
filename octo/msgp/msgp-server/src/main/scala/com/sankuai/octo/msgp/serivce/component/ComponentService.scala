package com.sankuai.octo.msgp.serivce.component

import com.meituan.jmonitor.LOG
import com.sankuai.meituan.auth.util.UserUtils
import com.sankuai.msgp.common.model.Page
import com.sankuai.msgp.common.utils.StringUtil
import com.sankuai.msgp.common.utils.helper.CommonHelper
import com.sankuai.octo.msgp.dao.component.ComponentDAO
import com.sankuai.octo.msgp.domain.{ComponentUsed, Dependency}
import com.sankuai.msgp.common.utils.client.Messager
import com.sankuai.msgp.common.utils.client.Messager.{Alarm, MODE}
import com.sankuai.octo.statistic.util.ExecutionContextFactory
import org.apache.maven.artifact.versioning.{DefaultArtifactVersion, VersionRange}
import org.joda.time.DateTime
import play.api.libs.json.Json

import scala.collection.JavaConverters._
import scala.concurrent.Future
import scala.util.Random

/**
  * MSGP 中 组件覆盖度、组件版本分布、组件趋势、组件提醒功能。
  */
object ComponentService {

  private implicit val ec = ExecutionContextFactory.build(10)

  private val MSGP_OWNER = List("caojiguang", "yangrui08", "tangye03")

  case class ComponentCoverageDetails(base: String,business: String, owt: String, pdl: String, appkey: String, appGroupId: String, appArtifactId: String, isUsed: Int, version: String)

  case class ComponentCoverageOutline(dept: String, typeNames: List[String], typeValues: List[Int], total_count_names: List[Int], total_count_values: List[Int])

  case class ComponentCoverage(outline: ComponentCoverageOutline, details: List[ComponentCoverageDetails])

  case class SimpleArtifact(groupId: String, artifactId: String, version: String)

  implicit val simpleArtifactReads = Json.reads[SimpleArtifact]
  implicit val simpleArtifactWrites = Json.writes[SimpleArtifact]

  case class SimpleArtifactConfig(groupId: String, artifactId: String, version: String, action: String)

  implicit val simpleArtifactConfigReads = Json.reads[SimpleArtifactConfig]
  implicit val simpleArtifactConfigWrites = Json.writes[SimpleArtifactConfig]

  case class AppkeyDependency(appkey: String, appGroupId: String, appArtifactId: String, groupId: String, artifactId: String, version: String)

  case class AppkeyComponent(cmpts: List[AppkeyDependency], countTotal: Int, count: Int)

  case class ComponentDetails(base: String, business: String, owt: String, pdl: String, appkey: String, appGroupId: String, appArtifactId: String, groupId: String, artifactId: String, version: String, uploadTime: String)

  case class App(app_group_id: String = "", app_artifact_id: String = "", app_version: String = "")

  case class AppCount(names: List[String], appCounts: List[Int])

  case class DependencyApp(appGroupId: String, appArtifactId: String, currentDependencies: List[Dependency], recommendedDependencies: List[Dependency])

  case class DependencyUser(username: String, dependencyApps: List[DependencyApp])

  case class DependencyMessage(username: String, prefixStr: String, appDependency: List[String], suffixStr: String)

  case class DependencyAlarm(username: String, alarm: Alarm)

  private val debug = false

  /**
    * 查询组件在线版本及其对应的数量
    *
    */
  def getComponentVersionCount(groupId: String, artifactId: String, base: String, business: String, owt: String, pdl: String) = {
    val result = if (CommonHelper.isOffline && !debug) {
      List[ComponentDAO.ComponentVersionCount]()
    } else {
      ComponentDAO.getComponentVersionCount(groupId, artifactId, base, business, owt, pdl)
    }
    result
  }

  /**
    * 根据组件具体版本,查询使用情况
    *
    */
  def getComponentVersionDetails(groupId: String, artifactId: String, version: String, base: String, business: String, owt: String, pdl: String, page: Page) = {
    val result = if (CommonHelper.isOffline && !debug) {
      List[ComponentDAO.ComponentVersionDetails]()
    } else {
      val list = ComponentDAO.getComponentVersionDetails(groupId, artifactId, version, base, business, owt, pdl)
      page.setTotalCount(list.length)
      list.slice(page.getStart, page.getStart + page.getPageSize)
    }
    result
  }

  /**
    * 根据组件,查询依赖它的使用服务
    *
    * @param groupId
    * @param artifactId
    * @return
    */
  def getComponentDetails(groupId: String, artifactId: String) = {
    val page = new Page(-1, 20000)
    ComponentService.getComponentVersionDetails(groupId, artifactId, null, "all", null, null, null, page).asJava
  }

  /**
    * 查询组件是否被使用
    *
    */
  def getComponentCoverage(base: String, business: String, owt: String, pdl: String, groupId: String, artifactId: String, version: String, matchingType: Int, page: Page) = {
    if (CommonHelper.isOffline && !debug) {
      val outline = ComponentCoverageOutline("", List[String](), List[Int](), List[Int](), List[Int]())
      val details = List[ComponentCoverageDetails]()
      ComponentCoverage(outline, details)
    } else {
      //所有版本
      val allVersions = ComponentDAO.getVersion(groupId, artifactId) //不存在为空的情况,前端过滤

      val versions = if (StringUtil.isBlank(version)) {
        List(version)
      } else {
        matchingType match {
          case 0 => List(version)
          case -1 => allVersions.sortWith(_ > _).dropWhile(!_.equalsIgnoreCase(version)).tail //小于此版本的所有版本
          case 1 => allVersions.sortWith(_ < _).dropWhile(!_.equalsIgnoreCase(version)).tail //大于此版本的所有版本
          case _ => List(version)
        }
      }

      //使用这些版本的
      val realAppsMap = ComponentDAO.getRealUser(groupId, artifactId, versions, base, business, owt, pdl).map {
        desc =>
          App(desc.groupId, desc.artifactId, "") -> desc
      }.toMap
      val realApps = realAppsMap.keys.toSet
      val realAppsDesc = realAppsMap.values.toList

      val allAppsMap = ComponentDAO.getAllUser(base, business, owt, pdl).map {
        desc =>
          App(desc.groupId, desc.artifactId, "") -> desc
      }.toMap
      val allApps = allAppsMap.keys.toSet

      val existingVersionsMap = ComponentDAO.getAppsVersion(groupId, artifactId, base, business, owt, pdl).map {
        desc =>
          //注意这里的version指的是组件的version, 而不是App本身的
          App(desc.groupId, desc.artifactId, "") -> desc
      }.toMap
      val existingVersionsApp = existingVersionsMap.keys.toSet
      LOG.info(s"allApps count ${allApps.size}")
      val list = allApps.toList.map {
        app =>
          val desc = allAppsMap.apply(app)
          val coverageDetail = ComponentCoverageDetails(desc.base,desc.business, desc.owt, desc.pdl, desc.appkey, desc.groupId, desc.artifactId, ComponentUsed.Unused.getIndex, "--")
          if (realApps.contains(app)) {
            if (StringUtil.isBlank(version)) {
              coverageDetail.copy(isUsed = ComponentUsed.Used.getIndex)
            } else {
              coverageDetail.copy(isUsed = ComponentUsed.CurrentVersion.getIndex, version = existingVersionsMap.apply(app).version)
            }
          } else {
            if (StringUtil.isNotBlank(version) && existingVersionsApp.contains(app)) {
              //该服务使用了该组件的其他版本
              coverageDetail.copy(isUsed = ComponentUsed.OtherVersion.getIndex, version = existingVersionsMap.apply(app).version)
            } else {
              //该服务的确未使用该组件
              coverageDetail
            }
          }

      }.sortWith(_.isUsed > _.isUsed)
      page.setTotalCount(list.length)

      //计算outline
      val outline = if (page.getPageNo == -1) {
        ComponentCoverageOutline("", List[String](), List[Int](), List[Int](), List[Int]())
      } else {
        //计算出outline
        val outlineData = list.groupBy(_.isUsed).map {
          case (isUsed, detailList) =>
            (isUsed, detailList.size)
        }.toList
        val total_count_names = outlineData.map(_._1)
        val total_count_values = outlineData.map(_._2)

        val groupResult = if (StringUtil.isBlank(business)) {
          ("事业群", realAppsDesc.groupBy(_.business))
        } else if (StringUtil.isNotBlank(business) && StringUtil.isBlank(owt) && StringUtil.isBlank(pdl)) {
          (business, realAppsDesc.groupBy(_.owt))
        } else if (StringUtil.isNotBlank(business) && !StringUtil.isBlank(owt) && StringUtil.isBlank(pdl)) {
          (owt, realAppsDesc.groupBy(_.pdl))
        } else if (StringUtil.isNotBlank(business) && !StringUtil.isBlank(owt) && !StringUtil.isBlank(pdl)) {
          (pdl, realAppsDesc.groupBy(_.pdl))
        } else {
          (business.toString, realAppsDesc.groupBy(_.business))
        }

        val data = groupResult._2.map {
          case (key, value) =>
            (key, value.size)
        }.toList

        ComponentCoverageOutline(groupResult._1, data.map(_._1), data.map(_._2), total_count_names, total_count_values)
      }
      val details = list.slice(page.getStart, page.getStart + page.getPageSize)

      ComponentCoverage(outline, details)
    }
  }

  /**
    * 服务详情-组件依赖:通过appkey得到该appkey下服务的组件使用分布
    *
    */
  def getDetailsByAppkey(appkey: String) = {
    val result = if (CommonHelper.isOffline && !debug) {
      AppkeyComponent(List[AppkeyDependency](), 0, 0)
    } else {
      val list = ComponentDAO.getDetialsByAppkey(appkey).map(
        x =>
          AppkeyDependency(x.appkey, x.appGroupId, x.appArtifactId, x.groupId, x.artifactId, x.version)
      ).distinct
      val count = list.map(
        x =>
          (x.groupId, x.artifactId)
      ).distinct.length
      AppkeyComponent(list, list.length, count)
    }
    result
  }

  /**
    * 从各个维度获取app_dependency数据
    */
  def getDetails(base: String, business: String, owt: String, pdl: String, groupId: String, artifactId: String, version: String, page: Page) = {
    val result = if (CommonHelper.isOffline && !debug) {
      List[ComponentDetails]()
    } else {
      val list = ComponentDAO.getDetails(base, business, owt, pdl, groupId, artifactId, version).map {
        x =>
          val uploadTime = new DateTime(x.uploadTime).toString("yyyy-MM-dd HH:mm:ss")
          val business = x.business
          val businessName = if (business.length > 4) business.substring(0, 4) else business
          ComponentDetails(x.base, businessName, x.owt, x.pdl, x.appkey, x.appGroupId, x.appArtifactId, x.groupId, x.artifactId, x.version, uploadTime)
      }
      page.setTotalCount(list.length)
      list.slice(page.getStart, page.getStart + page.getPageSize)
    }
    result
  }

  /**
    *
    * @param isTesting              是否是测试模式
    * @param subject                消息主题
    * @param option_type            操作类型
    * @param message_type           消息类型
    * @param dependencies           低版本组件List
    * @param recommend_dependencies 推荐版本组件List
    * @param wikis                  说明WIKI链接
    * @return 无
    */
  def sendMessage(isTesting: Int, subject: String, option_type: String, message_type: java.util.List[String],
                  dependencies: java.util.List[Dependency], recommend_dependencies: java.util.List[Dependency], wikis: java.util.List[String]) = {
    if (CommonHelper.isOffline && !debug) {
      "offline is not supported"
    } else {
      Future {
        //所有发布项
        val artifacts = dependencies.asScala.map(_.getArtifactId).toList

        //获得 组件 ——> List(发布项) 的映射关系
        val appDescMap = dependencies.asScala.map {
          dependency =>
            val appDesc = getAppVersionInRange(dependency.getGroupId, dependency.getArtifactId, dependency.getVersion)
            dependency -> appDesc
        }.toMap

        val appList = appDescMap.values.toList.flatten.distinct

        //TODO 获取服务负责人的方法不稳定
        // 发布项 -> List(负责人) 的映射关系
        val usernameMap = appList.map {
          appDesc =>
            appDesc -> ComponentHelper.getUsernameByAppDesc(appDesc)
        }.toMap.filter(_._2.nonEmpty)

        //获取所有需要发送用户的USERNAME
        val usernameList = usernameMap.values.toList.flatten.distinct

        val dependencyUser = usernameList.map {
          username =>
            //和该user相关的app(发布项)
            val relevantApps = usernameMap.filter {
              x =>
                val users = x._2.toSet
                users.contains(username)
            }.keys.toList.distinct

            val relevantDependency = relevantApps.map {
              appDesc =>
                //推荐使用的依赖
                val validDependencies = appDescMap.filter(x => x._2.contains(appDesc)).keys.toList
                val recommendedDependencies = validDependencies.flatMap {
                  dependency =>
                    recommend_dependencies.asScala.filter { x => x.getGroupId == dependency.getGroupId && x.getArtifactId == dependency.getArtifactId }
                }
                //应用实际使用的依赖
                val currentDependencies = recommendedDependencies.map(x => new Dependency(x.getGroupId, x.getArtifactId, appDesc.version))

                DependencyApp(appDesc.groupId, appDesc.artifactId, currentDependencies, recommendedDependencies)
            }.distinct

            //以app, appkey为粒度聚合数据
            val relevantDependencies = relevantDependency.groupBy(x => (x.appGroupId, x.appArtifactId)).map {
              case (key, value) =>
                val recommendedDependencies = value.flatMap(_.recommendedDependencies)
                val currentDependencies = value.flatMap(_.currentDependencies)
                DependencyApp(key._1, key._2, currentDependencies, recommendedDependencies)
            }.toList
            DependencyUser(username, relevantDependencies)
        }
        val messageList = assembleMessage(isTesting, subject, dependencyUser, artifacts, wikis.asScala.toList)

        val messageListToBeSent = if (isTesting == 1) {
          val currentUser = UserUtils.getUser
          val testingUsername = if (currentUser != null) (MSGP_OWNER :+ currentUser.getLogin).distinct else MSGP_OWNER
          val relevantMessageList = messageList.filter(x => testingUsername.contains(x.username))
          if (relevantMessageList.isEmpty) {
            testingUsername.map {
              username =>
                val randomItem = Random.shuffle(messageList).head
                DependencyAlarm(username, randomItem.alarm)
            }
          } else {
            relevantMessageList
          }
        } else {
          messageList
        }

        messageListToBeSent.foreach {
          message =>
            Messager.sendSingleMessage(message.username, message.alarm, Seq(MODE.XM))
        }
      }
      "success"
    }
  }

  /**
    * 获得版本条件下所有使用该组件的项目
    *
    * @param groupId
    * @param artifactId
    * @param version
    * @return
    */
  def getAppVersionInRange(groupId: String, artifactId: String, version: String) = {
    val targetVersion = if (!version.contains("[") && !version.contains("(")) {
      s"[$version,)"
    } else {
      version
    }
    val targetVersionRange = VersionRange.createFromVersionSpec(targetVersion)
    val allVersion = ComponentDAO.getVersion(groupId, artifactId)
    val versionsInRange = allVersion.filterNot {
      x =>
        targetVersionRange.containsVersion(new DefaultArtifactVersion(x))
    }
    //不存在为空的情况,前端过滤
    ComponentDAO.getAppVersionInRange(groupId, artifactId, versionsInRange).filter(StringUtil.isNotBlank(_))
  }

  def assembleMessage(isTesting: Int, subject: String, dependencyUserList: List[DependencyUser], artifacts: List[String], wikis: List[String]) = {
    val prefixStr = if (isTesting == 1) {
      s"$subject[测试消息]：\n\n"
    } else {
      s"$subject：\n\n"
    }
    val linkURL = s"https://123.sankuai.com/km/page/28097109"

    val dataItem = dependencyUserList.map {
      dependencyUser =>
        val dependencyApps = dependencyUser.dependencyApps
        val dependencyDetails = dependencyApps.map {
          item =>
            val prefix = s"[项目名]：${item.appGroupId} : ${item.appArtifactId}\n"
            val content = item.currentDependencies.zipWithIndex.map {
              case (dependency, index) =>
                val recommendedDependency = item.recommendedDependencies.apply(item.currentDependencies.indexOf(dependency))
                val appIdentity = if (dependency.getGroupId.equalsIgnoreCase("com.meituan.image") && dependency.getArtifactId.equalsIgnoreCase("client")) {
                  s"${dependency.getGroupId} : ${dependency.getArtifactId}"
                } else {
                  dependency.getArtifactId
                }
                s"${index + 1}, $appIdentity(使用版本: ${dependency.getVersion}, 推荐版本: ${recommendedDependency.getVersion})\n"
            }
            s"$prefix${content.mkString}"
        }
        DependencyMessage(dependencyUser.username, prefixStr, dependencyDetails, "")
    }

    dataItem.map {
      item =>
        val dependencyDesc = artifacts.zipWithIndex.map {
          case (artifact, index) =>
            s"${artifact}说明文档: [链接地址| ${wikis.apply(index)}]"
        }
        val footer = dependencyDesc.sortWith((a, b) => a.indexOf("|") < b.indexOf("|")) :+ s"基础架构组件推荐清单: [链接地址| $linkURL]"
        val alarmContent = s"${item.prefixStr}${item.appDependency.mkString("\n")}\n${footer.mkString("\n")}"
        DependencyAlarm(item.username, Alarm(subject, alarmContent))
    }
  }
}
