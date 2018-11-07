package com.sankuai.octo.mworth.service

import java.sql.Date

import com.sankuai.msgp.common.model.{Business, Page}
import com.sankuai.msgp.common.service.org.{BusinessOwtService, OpsService, OrgSerivce}
import com.sankuai.msgp.common.utils.StringUtil
import com.sankuai.msgp.common.utils.client.TairClient
import com.sankuai.octo.mworth.common.model.Worth.Model
import com.sankuai.octo.mworth.dao.worthEventCountDaily
import com.sankuai.octo.mworth.dao.worthEventCountDaily.{WBusinessCount, WCoverageCount, WDailyCount, WModuleCount}
import com.sankuai.octo.mworth.model.CountType
import com.sankuai.octo.mworth.util.DateTimeUtil
import com.sankuai.octo.statistic.util.ExecutionContextFactory
import org.joda.time.DateTime
import org.slf4j.{Logger, LoggerFactory}

import scala.collection.JavaConverters._
import scala.concurrent.Future

/**
  * Created by yves on 16/7/7.
  */
object mWorthDailyService {

  private val LOG: Logger = LoggerFactory.getLogger(mWorthDailyService.getClass)

  private implicit val ec = ExecutionContextFactory.build(5)
  private val rootOrg = List(1, 2, 12, 101, 532, 100046, 100171, 102368, 102942, 103100, 2003386, 2003515)
  private val keyOrg = List(5, 97, 99, 103, 877, 1819, 1829, 3848, 4416, 4698, 100173, 100174, 100175, 100176, 100300, 101724, 101725, 102035, 102338, 102369, 102481, 102482, 102505, 102509, 102859, 1021463, 2003025)
  //private val subOrgID = List(4, 5, 97, 99, 103, 150, 1828, 4416, 4698, 102338, 1819, 1829, 3848, 4358, 4728, 5183, 100047, 100125, 100126, 100129, 100130, 100131, 100132, 100173, 100174, 100175, 100176, 100420, 100425, 101724, 101725, 102035, 102348, 102369, 102394, 102433, 102481, 102482, 102505, 102509, 102597, 1021463, 2003025, 3547)
  private val devPosList = List("算法开发", "系统开发", "前端开发", "后台开发", "运维", "QA")
  private val tairPeakKey = "mworth.peak"
  private val peakPattern = "^[0-9]{4}-[0-9]{2}-[0-9]{2}\\|\\d+\\|[0-9]{4}-[0-9]{2}-[0-9]{2}\\|\\d+$"


  case class DayCount(dates: List[String], counts: List[Int], max: Map[String, WDailyCount])

  case class BCountUser(username: List[String], usercount: List[Int])

  case class BCount(business: String, countBusiness: List[Int], bcountuser: List[BCountUser])

  case class BusinessCount(bcount: List[BCount], dates: List[String])

  case class MCount(module: String, countModule: List[Int], countDesc: Map[String, List[Int]])

  case class ModuleCount(mcount: List[MCount], dates: List[String])

  case class ICount(business: String, username: String, module: String, functionDesc: String, count: Int)

  case class IntegratedCount(icount: List[ICount])

  case class CCount(orgname: String, rates: List[Double], devtotal: Int, devnum: List[Int])

  case class CoverageCount(ccount: List[CCount], dates: List[String])

  def queryTotal(start: Date, end: Date, dataType: Int): DayCount = {
    val startTime = new DateTime(start.getTime).withTimeAtStartOfDay()
    val endTime = new DateTime(end.getTime).withTimeAtStartOfDay()
    val day = (endTime.getMillis - startTime.getMillis) / DateTimeUtil.DAY_TIME
    val dates = (day.toInt to 0 by -1).map {
      x =>
        endTime.minusDays(x).toString("yyyy-MM-dd")
    }.toList

    val startDate = new java.sql.Date(startTime.getMillis)
    val endDate = new java.sql.Date(endTime.getMillis)

    val dayMap = worthEventCountDaily.queryTotal(startDate, endDate, dataType).groupBy {
      x => DateTimeUtil.format(new Date(x.date.getTime), "yyyy-MM-dd")
    }

    val counts = dates.map {
      date =>
        dayMap.getOrElse(date, List[WDailyCount]()).map(_.count).sum
    }
    val peak = getPeak
    val max = Map("pv" -> peak._1, "uv" -> peak._2)
    DayCount(dates, counts, max)
  }

  def getPeak = {
    val tairPeakValue = getPeakCache
    if (tairPeakValue.isDefined && tairPeakValue.get.matches(peakPattern)) {
      // tairValue  = dateOfMaxPV|maxPV|dateOfMaxUV|maxUV
      val peakValue = tairPeakValue.get.split("\\|")
      val dateOfMaxPV = new Date(DateTimeUtil.parse(peakValue.apply(0), DateTimeUtil.DATE_DAY_FORMAT).getTime)
      val maxPVValue = peakValue.apply(1).toInt
      val dateOfMaxUV = new Date(DateTimeUtil.parse(peakValue.apply(2), DateTimeUtil.DATE_DAY_FORMAT).getTime)
      val maxUVValue = peakValue.apply(3).toInt
      val maxPV = WDailyCount(dateOfMaxPV, maxPVValue)
      val maxUV = WDailyCount(dateOfMaxUV, maxUVValue)
      (maxPV, maxUV)
    } else {
      savePeak
    }
  }

  def savePeak = {
    val tairPeakValue = getPeakCache
    val startDate = if (tairPeakValue.isDefined && tairPeakValue.get.matches(peakPattern)) {
      val peakValue = tairPeakValue.get.split("\\|")
      val dateOfMaxPV = new Date(DateTimeUtil.parse(peakValue.apply(0), DateTimeUtil.DATE_DAY_FORMAT).getTime)
      val dateOfMaxUV = new Date(DateTimeUtil.parse(peakValue.apply(2), DateTimeUtil.DATE_DAY_FORMAT).getTime)
      (dateOfMaxPV, dateOfMaxUV)
    } else {
      val dateOfMaxPV = new Date(DateTimeUtil.parse(DateTimeUtil.START_DATE_OF_UNIX, DateTimeUtil.DATE_DAY_FORMAT).getTime)
      val dateOfMaxUV = new Date(DateTimeUtil.parse(DateTimeUtil.START_DATE_OF_UNIX, DateTimeUtil.DATE_DAY_FORMAT).getTime)
      (dateOfMaxPV, dateOfMaxUV)
    }
    val maxPVOpt = worthEventCountDaily.queryMax(CountType.pv.id, startDate._1).headOption
    val maxUVOpt = worthEventCountDaily.queryMax(CountType.uv.id, startDate._2).headOption

    if (maxPVOpt.isDefined && maxUVOpt.isDefined) {
      val maxPV = maxPVOpt.get
      val maxUV = maxUVOpt.get
      val peakString = s"${DateTimeUtil.format(maxPV.date, DateTimeUtil.DATE_DAY_FORMAT)}|${maxPV.count}|" +
        s"${DateTimeUtil.format(maxUV.date, DateTimeUtil.DATE_DAY_FORMAT)}|${maxUV.count}"
      TairClient.put(tairPeakKey, peakString)
      (maxPV, maxUV)
    } else {
      val peakString = s"${DateTimeUtil.START_DATE_OF_UNIX}|0|${DateTimeUtil.START_DATE_OF_UNIX}|0"
      TairClient.put(tairPeakKey, peakString)
      val emptyRecord = WDailyCount(new Date(DateTimeUtil.parse(DateTimeUtil.START_DATE_OF_UNIX, DateTimeUtil.DATE_DAY_FORMAT).getTime), 0)
      (emptyRecord, emptyRecord)
    }
  }

  def getPeakCache = {
    try {
      TairClient.get(tairPeakKey)
    }
    catch {
      case e: Exception =>
        LOG.error(s"get peak from tail failed", e)
        None
    }
  }

  def queryBusiness(start: Date, end: Date, business: Int, dataType: Int): BusinessCount = {
    /** 注: 本模块中使用了Business类,Value(9, "智能餐厅部") (6, "云计算部")  (8, "支付平台部") (3, "创新业务部") 废弃不用
      * */
    val startTime = new DateTime(start.getTime).withTimeAtStartOfDay()
    val endTime = new DateTime(end.getTime).withTimeAtStartOfDay()
    val day = (endTime.getMillis - startTime.getMillis) / DateTimeUtil.DAY_TIME
    val dates = (day.toInt to 0 by -1).map {
      x =>
        endTime.minusDays(x).toString("yyyy-MM-dd")
    }.toList

    val startDate = new java.sql.Date(startTime.getMillis)
    val endDate = new java.sql.Date(endTime.getMillis)

    var queryBusinessResultTemp = worthEventCountDaily.queryBusinessTotal(startDate, endDate, business, dataType).groupBy(_.business)

    val businessList = queryBusinessResultTemp.keys.toList

    val queryBusinessResult = if (business == -1 && businessList.size < Business.values.size) {
      //此时查询的是整个Business,因此需要补齐所有部门,避免数据库中有的部门数据不存在
      Business.values.map(_.getId) foreach (
        x =>
          if (!businessList.contains(x) && x != 3 && x != 6 && x != 8 && x != 9) {
            queryBusinessResultTemp += (x -> List[WBusinessCount]())
          }
        )
      queryBusinessResultTemp
    } else {
      queryBusinessResultTemp
    }
    val list = queryBusinessResult.map {
      bc =>
        val business = Business.getBusinessNameById(bc._1)
        val dateToData = bc._2.groupBy {
          x =>
            DateTimeUtil.format(new Date(x.date.getTime), "yyyy-MM-dd")
        }
        val countBusiness = dates.map {
          date =>
            dateToData.getOrElse(date, List[WBusinessCount]()).map(_.count).sum
        }
        //统计每天访问量前十的用户
        val countUser = if (queryBusinessResult.size == 1 && dataType == CountType.pv.id) {
          dates.map {
            date =>
              val userCountTemp = dateToData.getOrElse(date, List[WBusinessCount]()).map {
                x =>
                  x.username -> x.count
              }.toMap
              val userCount = userCountTemp.toList.sortBy(_._2).reverse
              val userCountLimited = if (userCount.size > 10) {
                userCount.take(10)
              } else {
                userCount
              }
              val usernameList = userCountLimited.map(_._1)
              val userCountList = userCountLimited.map(_._2)
              BCountUser(usernameList, userCountList)
          }
        }
        else {
          List[BCountUser]()
        }
        BCount(business, countBusiness, countUser)
    }.toList
    BusinessCount(list, dates)
  }

  def queryModule(start: Date, end: Date, module: Int, dataType: Int): ModuleCount = {
    val startTime = new DateTime(start.getTime).withTimeAtStartOfDay()
    val endTime = new DateTime(end.getTime).withTimeAtStartOfDay()
    val day = (endTime.getMillis - startTime.getMillis) / DateTimeUtil.DAY_TIME

    val dates = (day.toInt to 0 by -1).map {
      x =>
        endTime.minusDays(x).toString("yyyy-MM-dd")
    }.toList

    val startDate = new java.sql.Date(startTime.getMillis)
    val endDate = new java.sql.Date(endTime.getMillis)

    var queryResultTemp = worthEventCountDaily.queryModuleTotal(startDate, endDate, module, dataType).groupBy(_.module)
    //Maybe oneof module is not existing...so...
    val moduleList = queryResultTemp.keys.toList
    val queryResult = if (module == -1 && moduleList.size < Model.values().length) {
      Model.values().foreach {
        x =>
          val moduleName = x.getName
          if (!moduleList.contains(moduleName)) {
            queryResultTemp += (moduleName -> List[WModuleCount]())
          }
      }
      queryResultTemp
    } else {
      queryResultTemp
    }

    var list = queryResult.map {
      e =>
        val dateToData = e._2.groupBy {
          x =>
            DateTimeUtil.format(new Date(x.date.getTime), "yyyy-MM-dd")
        }
        val countModule = dates.map {
          date =>
            dateToData.getOrElse(date, List[WModuleCount]()).map(_.count).sum
        }
        val countDesc = if (queryResult.size == 1) {
          val descList = e._2.map(_.functionDesc).distinct
          descList.map {
            desc =>
              val desCount = dates.map {
                date =>
                  dateToData.getOrElse(date, List[WModuleCount]()).filter(_.functionDesc.equalsIgnoreCase(desc)).map(_.count).sum
              }
              desc -> desCount
          }.toMap
        } else {
          Map[String, List[Int]]()
        }
        MCount(e._1, countModule, countDesc)
    }.toList
    var serviceOpt = List[Int]()
    var exceptionMonitor = List[Int]()
    var serviceDetail = List[Int]()
    var serviceReport = List[Int]()
    list.foreach {
      x =>
        if (x.module.equals("服务分组") || x.module.equals("一键截流") || x.module.equals("访问控制") || x.module.equals("配置管理")) {
          if(serviceOpt.size == 0){
            serviceOpt = x.countModule
          }else{
            serviceOpt = serviceOpt.zip(x.countModule).map(t => t._1 + t._2)
          }
        }
        if (x.module.equals("异常监控") || x.module.equals("监控报警")) {
          if(exceptionMonitor.size == 0){
            exceptionMonitor = x.countModule
          }else{
            exceptionMonitor = exceptionMonitor.zip(x.countModule).map(t => t._1 + t._2)
          }
        }
        if (x.module.equals("命名服务") || x.module.equals("组件依赖")) {
          if(serviceDetail.size == 0){
            serviceDetail = x.countModule
          }else{
            serviceDetail = serviceDetail.zip(x.countModule).map(t => t._1 + t._2)
          }
        }
        if (x.module.equals("治理报告")) {
          if(serviceReport.size == 0){
            serviceReport = x.countModule
          }else{
            serviceReport = serviceReport.zip(x.countModule).map(t => t._1 + t._2)
          }
        }
    }
    val buildList = list.filterNot{
      m =>
      m.module.equals("服务分组") || m.module.equals("一键截流") ||
        m.module.equals("访问控制") || m.module.equals("配置管理") ||
        m.module.equals("异常监控") || m.module.equals("监控报警") ||
        m.module.equals("命名服务") || m.module.equals("组件依赖") ||
        m.module.equals("治理报告")
    }
    val temList = List[MCount](MCount("服务运营",serviceOpt,Map[String, List[Int]]()),MCount("异常监控",exceptionMonitor,Map[String, List[Int]]()),MCount("服务详情",serviceDetail,Map[String, List[Int]]()),MCount("服务报表",serviceReport,Map[String, List[Int]]()))
    val finalList = buildList.++(temList)
    ModuleCount(finalList, dates)
  }

  def queryDetails(date: Date, business: Int, username: String, module: Int, functionDesc: String, page: Page): IntegratedCount = {
    val startTime = new java.sql.Date(new DateTime(date.getTime).withTimeAtStartOfDay().getMillis)
    val businessReal = if (business == -1 && !StringUtil.isBlank(username)) {
      //对应未选部门,直接查用户
      -2
    } else {
      business
    }
    val list = worthEventCountDaily.queryDetails(startTime, businessReal, username, module, functionDesc).map {
      x =>
        ICount(Business.getBusinessNameById(x.business), x.username, x.module, x.functionDesc, x.count)
    }
    page.setTotalCount(list.length)
    IntegratedCount(list.slice(page.getStart, page.getStart + page.getPageSize))
  }

  def queryFunction(date: Date, module: Int) = {
    val startTime = new java.sql.Date(new DateTime(date.getTime).withTimeAtStartOfDay().getMillis)
    worthEventCountDaily.queryFunction(startTime, module).asJava
  }

  //刷新所有数据
  def refresh(date: Date) {
    Future {
      worthEventCountDaily.refresh(date)
    }
  }

  //刷新峰值数据
  def refreshPeak {
    Future {
      savePeak
    }
  }

  def orgTreeLevel(orgId: String, limitOrgIds: java.util.List[Integer]) = {
    val result = OrgSerivce.orgTreeLevel(orgId, limitOrgIds).asScala
    if (StringUtil.isBlank(orgId) || orgId.substring(1).toInt == 2) {
      result.filter(x => rootOrg.contains(x.getDataId.toInt)).asJava
    } else {
      result.asJava
    }
  }

  def orgTreeSearch(orgId: String, limitOrgIds: java.util.List[Integer]) = {
    val result = OrgSerivce.orgTreeSearch(orgId, limitOrgIds).asScala
    result.filter {
      orgNode =>
        rootOrg.contains(OrgSerivce.getOrgByOrgId(orgNode.getDataId).getTopOrgId)
    }.asJava
  }

  /**
    * 获取集团(orgid = 2) 下分布有开发者的Org
    *
    * @return
    */
  def getKeyRootOrg = {
    val rootOrgNode = OrgSerivce.getDevEmployeeNumofSubOrg(2, devPosList).filter(_._2 > 0)
    val topOrgMap = rootOrgNode.keys.toList.map(
      x =>
        x -> OrgSerivce.getOrgByOrgId(x).getTopOrgName
    ).toMap
    topOrgMap.keys.toList
  }

  /**
    * 获取各个事业群下分布有开发者的subOrg
    *
    * @return
    */
  def getKeyOrg = {
    getKeyRootOrg.flatMap {
      rootOrg =>
        OrgSerivce.getDevEmployeeNumofSubOrg(rootOrg, devPosList).filter(_._2 > 0).keys.toList
    }.sorted
  }

  def queryCoverage(start: Date, end: Date, orgId: Int, devPos: java.util.List[String]): CoverageCount = {
    val startTime = new DateTime(start.getTime).withTimeAtStartOfDay()
    val endTime = new DateTime(end.getTime).withTimeAtStartOfDay()
    val startDate = new java.sql.Date(startTime.getMillis)
    val endDate = new java.sql.Date(endTime.getMillis)
    val day = (endTime.getMillis - startTime.getMillis) / DateTimeUtil.DAY_TIME
    val dates = (day.toInt to 0 by -1).map {
      x =>
        endTime.minusDays(x).toString("yyyy-MM-dd")
    }.toList

    val devpos = devPos.asScala.toList
    val subOrgNode = OrgSerivce.getDevEmployeeNumofSubOrg(orgId, devPosList).filter(_._2 > 0)
    if (subOrgNode.isEmpty) {
      CoverageCount(List[CCount](), List[String]())
    } else {
      val ccount = subOrgNode.keys.par.map {
        subOrgId =>
          val orgNodeList = subOrgId :: OrgSerivce.getAllOrgIdOfEmployee(subOrgId)
          val resultTemp = worthEventCountDaily.queryCoverage(startDate, endDate, orgNodeList, devpos)
          val subDevEmp = subOrgNode(subOrgId).toDouble
          val orgName = OrgSerivce.getOrgByOrgId(subOrgId).getName
          val result = resultTemp.groupBy {
            x => DateTimeUtil.format(new Date(x.date.getTime), "yyyy-MM-dd")
          }
          val devNum = dates.map {
            x =>
              result.getOrElse(x, List[WCoverageCount]()).map(_.count).sum
          }
          val rates = devNum.map {
            x =>
              x.toDouble / subDevEmp
          }
          CCount(orgName, rates, subDevEmp.toInt, devNum)
      }.toList
      CoverageCount(ccount, dates)
    }
  }

  def updateOrgInfo() = {
    Future {
      val userList = worthEventCountDaily.getUserList
      val posName = userList.par.map {
        x =>
          OrgSerivce.getEmployeePosName(x)
      }.toList
      val posId = userList.par.map {
        x =>
          OrgSerivce.getEmployeePosID(x)
      }.toList
      val orgId = userList.par.map {
        x =>
          OrgSerivce.getEmployeeOrgId(x)
      }.toList
      val orgName = orgId.par.map {
        x =>
          OrgSerivce.getEmployeeOrgName(x)
      }.toList

      worthEventCountDaily.updateOrgInfo(posId, posName, orgId, orgName, userList)
    }
  }

  def updateBusinessInfoViaOrg() = {
    Future {
      val orgIdList = worthEventCountDaily.getOrgIdList
      val orgToBussiness = orgIdList.map {
        x =>
          val topOrgName = OrgSerivce.getTopOrgName(x)
          val id = Business.values.find(_.toString.equalsIgnoreCase(topOrgName)).getOrElse(Business.other).getId
          x -> id
      }.toMap
      worthEventCountDaily.updateBusinessInfoViaOrg(orgIdList, orgToBussiness)
    }
  }

  def updateBusinessInfoViaOps() = {
    Future {
      val userList = worthEventCountDaily.getUserList
      val userToBusiness = userList.map {
        username =>
          val owtList = OpsService.getOwtsbyUsername(username)
          val business = if (owtList.isEmpty) {
            100
          } else {
            BusinessOwtService.getBusiness(owtList.get(0))
          }
          username -> business
      }.toMap
      worthEventCountDaily.updateBusinessInfoViaOps(userList, userToBusiness)
    }
  }
}
