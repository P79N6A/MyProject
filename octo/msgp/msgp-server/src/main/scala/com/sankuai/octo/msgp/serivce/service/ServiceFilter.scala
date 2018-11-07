package com.sankuai.octo.msgp.serivce.service

import java.util.concurrent.{CountDownLatch, Executors}

import com.sankuai.msgp.common.model.{Page, Pdl, ServiceModels}
import com.sankuai.msgp.common.service.org.{BusinessOwtService, OpsService, OrgSerivce, SsoService}
import com.sankuai.octo.msgp.dao.appkey.{AppkeyDescDao, AppkeyFavoriteDao, AppkeyProviderDao}
import com.sankuai.octo.msgp.utils.Auth
import org.apache.commons.lang3.StringUtils
import play.api.libs.json.Json
import com.sankuai.msgp.common.config.db.msgp.Tables._

import scala.collection.JavaConverters._
import scala.collection.mutable.ListBuffer
import scala.collection.parallel.ForkJoinTaskSupport


/**
 * Created by yves on 16/8/3.
 */
object ServiceFilter {

  private val POOL_SIZE = 8
  private val executorthreadPool = Executors.newFixedThreadPool(POOL_SIZE)
  private val threadPool = new ForkJoinTaskSupport(new scala.concurrent.forkjoin.ForkJoinPool(12))

  def serviceByType(page: Page, queryType: Int, user: ServiceModels.User, business: Int) = {
    val data = queryType match {
      case 0 => serviceByBusiness(user, business, page)
      case 1 => serviceByObserver(user, page)
      case 2 => serviceByUserOwt(user, page)
      case 3 => serviceByOwner(user, page)
      case 4 => serviceTotal(page)
      case _ => serviceByOwner(user, page)
    }
    data
  }

  def applist(username: String, `type`: String,keyword:String) = {
    if (StringUtils.isNotBlank(username)) {
      val userInfo= SsoService.getUser(username)
      userInfo match {
        case None =>
          List()
        case Some(employeeInfo) =>
          AppkeyDescDao.appsbyuser(employeeInfo.getId,keyword)
      }
    } else if (StringUtils.isNotBlank(`type`)) {
      AppkeyProviderDao.appsbytype(`type`,keyword)
    } else {
      AppkeyDescDao.apps(keyword)
    }
  }

  def serviceByCategory(category: String) = {
    val list = AppkeyDescDao.appsByCategory(category)
    val hasProviderList = AppkeyProviderDao.appsProd();
    val filterList = list.filter(x => hasProviderList.contains(x))
    filterList
  }

  /**
   * 获取事业群服务
   *
   * @return
   */
  def serviceByBusiness(user: ServiceModels.User, businessGroup: Int, page: Page) = {
    val owts = if (businessGroup > -1) {
      val pdls = BusinessOwtService.businessMap.getOrElse(businessGroup, List[Pdl]())
      pdls.map {
        x =>
          x.getOwt
      }
    } else {
      OpsService.getOwtsbyUsername(user.login).asScala
    }.toList
    val list = AppkeyDescDao.search(None, Some(owts), None, page)
    toDescs(list)
  }

  /**
   * 获取关注的服务
   *
   * @return
   */
  def serviceByObserver(user: ServiceModels.User, page: Page) = {
    val list = AppkeyDescDao.searchByUser(user, Auth.Level.OBSERVER.getValue, page)
    toDescs(list)
  }

  /**
   * 获取负责的服务
   *
   * @return
   */
  def serviceByOwner(user: ServiceModels.User, page: Page) = {
    val list = AppkeyDescDao.searchByUser(user, Auth.Level.ADMIN.getValue, page)
    toDescs(list).distinct
  }

  def serviceByOwner(user: String) = {
    val employeeOpt = OrgSerivce.employee(user)
    if(employeeOpt.isDefined) {
      val e = employeeOpt.get
      val page = new Page
      page.setPageSize(10000)
      val list = AppkeyDescDao.searchByUser(ServiceModels.User(e.getId,e.getLogin,e.getName), Auth.Level.ADMIN.getValue, page)
      list.map(_.appkey).distinct.asJava
    }else{
      List[String]().asJava
    }
  }


  /**
   * 获取业务线(owt)下的服务
   *
   * @return
   */
  def serviceByUserOwt(user: ServiceModels.User, page: Page) = {
    val owts = OpsService.getOwtsbyUsername(user.login).asScala.toList
    val list = AppkeyDescDao.search(None, Some(owts), None, page)
    toDescs(list)
  }

  def serviceByOwtPdl(owt: String, pdl: String, page: Page) = {
    val opt_pdl = if (StringUtils.isNotBlank(pdl)) {
      Some(List(pdl))
    } else {
      None
    }
    val list = AppkeyDescDao.search(None, Some(List(owt)), opt_pdl, page)
    toDescs(list)
  }

  def serviceAppkeyByOwtPdl(owt: String, pdl: String, page: Page) = {
    val optPdl = if (StringUtils.isNotBlank(pdl)) {
      Some(List(pdl))
    } else {
      None
    }
    val list = AppkeyDescDao.search(None, Some(List(owt)), optPdl, page)
    val appkeys = list.map(desc=>desc.appkey)
    appkeys
  }



  /**
   * 获取所有服务
   *
   * @return
   */
  def serviceTotal(page: Page) = {
    if (page.getPageSize == Integer.MAX_VALUE) {
      val maxSize = 1500
      page.setPageSize(maxSize)
      val list = AppkeyDescDao.search(None, None, None, page)
      val pageCount = page.getTotalPageCount
      var all_data = ListBuffer[AppkeyDesc#TableElementType]()
      all_data ++= list
      val latch = new CountDownLatch(pageCount - 1)
      for (i <- 2 to pageCount) {
        val n_page = new Page(i, maxSize)
        executorthreadPool.submit(new ThreadAppkeyDesc(all_data, n_page, latch))
      }
      latch.await()
      toDescs(all_data.toList)
    } else {
      val list = AppkeyDescDao.search(None, None, None, page)
      toDescs(list)
    }
  }

  class ThreadAppkeyDesc(all_data: ListBuffer[AppkeyDesc#TableElementType], page: Page, latch: CountDownLatch) extends Runnable {
    override def run() {
      var tmp_data = AppkeyDescDao.search(None, None, None, page)
      all_data ++= tmp_data
      latch.countDown()
    }
  }

  def toDescs(list: List[AppkeyDesc#TableElementType]): List[ServiceModels.Desc] = {
    val listPar = list.par
    listPar.tasksupport = threadPool
    listPar.map {
      data =>
        toDesc(data)
    }.toList
  }

  def toDesc(data: AppkeyDesc#TableElementType): ServiceModels.Desc = {
    val owners = Json.parse(data.owners).asOpt[List[ServiceModels.User]]
    val observers = Json.parse(data.observers).asOpt[List[ServiceModels.User]]
    ServiceModels.Desc(data.name, data.appkey, Some(data.baseapp), owners.getOrElse(List()),
      observers, data.intro, data.category, Some(data.business), None,
      Some(data.base), Some(data.owt), Some(data.pdl), None, data.tags, data.reglimit, Some(data.createTime))
  }


  /**
   * 获取自己感兴趣的服务
 *
   * @param user
   * @return
   */
  def getFavoriteAppkeys(user: ServiceModels.User) = {
    val appkeysSaved = AppkeyFavoriteDao.getAppkeys(user)
    val appkeys = if (appkeysSaved.isEmpty) {
      val page = new Page()
      page.setPageSize(1000)
      val list = serviceByOwner(user, page).map(_.appkey)
      if (list.nonEmpty) {
        AppkeyFavoriteDao.batchInsert(user, List(list.head))
        List(list.head)
      } else {
        List()
      }
    } else {
      appkeysSaved
    }
    appkeys.asJava
  }

  def addFavoriteAppkeys(user: ServiceModels.User, appkeys: java.util.List[String]) = {
    AppkeyFavoriteDao.batchInsert(user, appkeys.asScala.toList)
  }

  def deleteFavoriteAppkeys(user: ServiceModels.User, appkey: String) = {
    AppkeyFavoriteDao.delete(user, appkey)
  }
}
