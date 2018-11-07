package com.sankuai.octo.msgp.utils.helper

import com.sankuai.msgp.common.model.ServiceModels
import com.sankuai.msgp.common.service.org.OrgSerivce
import com.sankuai.msgp.common.utils.StringUtil
import com.sankuai.octo.errorlog.dao.ErrorLogStatisticDao
import com.sankuai.octo.errorlog.dao.ErrorLogStatisticDao.AppkeyCount
import com.sankuai.octo.msgp.serivce.other.LogServiceClient
import com.sankuai.octo.msgp.serivce.service.{ServiceCommon, ServiceDesc}
import com.sankuai.octo.msgp.serivce.subscribe.ReportSubscribe
import com.sankuai.octo.msgp.serivce.AppkeyAlias
import com.sankuai.octo.statistic.util.ExecutionContextFactory
import org.slf4j.LoggerFactory
import play.api.libs.json.Json

import scala.collection.mutable.ListBuffer
import scala.collection.parallel.ForkJoinTaskSupport
import scala.concurrent.Future

object ReportHelper {
  private val logger = LoggerFactory.getLogger(this.getClass)

  private val tasksupport = new ForkJoinTaskSupport(new scala.concurrent.forkjoin.ForkJoinPool(6))
  private val logService = LogServiceClient.getInstance

  case class PdlOwner(owt: String, owners: String)

  implicit val pdlOwnerReads = Json.reads[PdlOwner]
  implicit val pdlOwnerWrites = Json.writes[PdlOwner]

  private implicit val ec = ExecutionContextFactory.build(3)


  /**
    * 返回 业务线 -> 服务appkey列表 的Map
    *
    * @return owt-> appkey list
    */

  def getOwtToAppkeyMap = {
    val owtAppkeyList = ServiceDesc.getAllAppkeyOwt
    owtAppkeyList.filterNot(_._1.isEmpty).groupBy(_._1).map {
      case (owt, list) =>
        owt -> list.map(_._2)
    }
  }

  def getAppkeyByOwt(owt: String) = {
    val allService = ServiceCommon.listService
    allService.filter { item =>
      item.owt.nonEmpty && item.owt.get.equalsIgnoreCase(owt)
    }.map(_.appkey)
  }

  def refreshUserAppkeyMapManually = {
    Future {
      refreshUserAppkeyMap
    }
  }

  def refreshUserAppkeyMap = {
    logger.info(s"刷新userappkey")
    val start = System.currentTimeMillis()
    val userAppkeyMap = scala.collection.mutable.Map[String, ListBuffer[String]]()
    val all = ServiceCommon.listService
    val appkeyPar = all.filterNot { item =>
      item.owt.isEmpty || item.owt.get.isEmpty
    }.par
    appkeyPar.tasksupport = tasksupport
    appkeyPar.foreach {
      app =>
        app.owners.foreach { owner =>
          updateUserAppkeyMap(userAppkeyMap, owner, app.appkey, true)
        }
        app.observers.getOrElse(List()).foreach { observer =>
          updateUserAppkeyMap(userAppkeyMap, observer, app.appkey, false)
        }
    }

    //写入数据到数据库里，并清空自己
    val userAppkeyMapPar = userAppkeyMap.par
    userAppkeyMapPar.tasksupport = tasksupport
    userAppkeyMapPar.foreach {
      case (username, appkeys) =>
        if (StringUtil.isNotBlank(username) && appkeys.nonEmpty) {
          ReportSubscribe.addReportSubscribe(username, appkeys.toList)
        }
    }
    userAppkeyMap.clear()
    logger.info(s"刷新userappkey完成,size:${userAppkeyMap.size},time ${System.currentTimeMillis() - start}")
  }

  def updateUserAppkeyMap(userAppkeyMap: scala.collection.mutable.Map[String, ListBuffer[String]], owner: ServiceModels.User, appkey: String, leader: Boolean) = {
    try {
      if (leader) {
        val heads = OrgSerivce.getHeadList(owner.id)
        //给leader添加appkey
        if (heads.size() > 3) {
          val head = OrgSerivce.employee(heads.get(0)).getLogin
          if (!owner.login.equals(head)) {
            updateUserAppkey(userAppkeyMap, head, appkey)
          }
        }
      }
      updateUserAppkey(userAppkeyMap, owner.login, appkey)
    }
    catch {
      case e: Exception => logger.error(s"update UserAppkey fail owner: $owner,appkey:$appkey", e)
    }
  }

  private def updateUserAppkey(userAppkeyMap: scala.collection.mutable.Map[String, ListBuffer[String]], username: String, appkey: String): Unit = {
    synchronized {
      val list = userAppkeyMap.getOrElseUpdate(username, ListBuffer[String]())
      if (!list.contains(appkey)) {
        list.append(appkey)
      }
    }
  }

  /**
    * 返回 业务线 -> 服务appkey列表 的Map
    *
    * @return owt-> appkey list
    */
  def getOwtToDescMap = {
    val all = ServiceCommon.listService
    val owtDescMap = all.filterNot { item =>
      item.owt.isEmpty || item.owt.get.isEmpty
    }.groupBy { item =>
      item.owt.get
    }.map { case (owt, ser2) =>
      (owt, ser2)
    }
    owtDescMap
  }

  /**
    * 返回 业务线 -> 服务appkey列表 的Map
    *
    * @return owt-> appkey list
    */
  def getAppkeyToDescMap = {
    val all = ServiceCommon.listService
    val owtAppkeyMap = all.filterNot { item =>
      item.owt.isEmpty || item.owt.get.isEmpty || item.appkey.isEmpty
    }.map { item =>
      (item.appkey, item.intro)
    }.toMap
    owtAppkeyMap
  }

  /**
    * 获取错误日志
    */
  def getDayErrorCount(appkey: String, start: Int, end: Int): Long = {
    val aliasAppkey = AppkeyAlias.aliasAppkey(appkey)
    val errorCount = if (null == logService) {
      0l
    } else {
      val count = logService.getErrorCount(aliasAppkey, new java.util.Date(start * 1000L), new java.util.Date(end * 1000L))
      if (null == count) {
        0l
      } else {
        count.toLong
      }
    }
    errorCount
  }

  /**
    * 统计 appkey 报错的总数
    */
  def errorCount(start: Int, end: Int) = {
    val errorCounts = ErrorLogStatisticDao.getErrorCount(new java.util.Date(start * 1000L), new java.util.Date(end * 1000L))
    val errors = errorCounts.map {
      errorcount =>
        //val appkey = AppkeyAlias.octoAppkey(errorcount.appkey)
        AppkeyCount(errorcount.appkey, errorcount.logCount)
    }.filter { error => StringUtil.isNotBlank(error.appkey) && error.logCount > 0 }
    errors
  }
}
