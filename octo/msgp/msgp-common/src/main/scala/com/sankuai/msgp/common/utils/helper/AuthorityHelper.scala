package com.sankuai.msgp.common.utils.helper

import java.util.concurrent.TimeUnit

import com.google.common.cache.{CacheBuilder, CacheLoader}
import com.sankuai.meituan.auth.vo.User
import com.sankuai.msgp.common.config.MsgpConfig
import com.sankuai.msgp.common.service.org.OrgSerivce
import com.sankuai.msgp.common.service.org.OrgSerivce.EmployeeBasic
import org.slf4j.{Logger, LoggerFactory}

object AuthorityHelper {
  private val LOG: Logger = LoggerFactory.getLogger(AuthorityHelper.getClass)

  val defaultAdminUsers = "zhangxi,huwei05,zhangjinlu,caojiguang," +
    "chenxi18,chenxin11,gaosheng,huangbinqiang,suchao02,wangsiyu02,wangziwu," +
    "wujinwu,xintao,xuzhangjian,yangjie17,gaosheng,zhangzhitong,gaosheng, " +
    "pengjunyu,wujinwu,yangrui08,zhangchi11,zhoufeng04,wangziyin,huangxin10,zhangcan02,zhoufeng04,fantaotao02,xiezhaodong"
  val whiteList = Set("static", "dashboard", "register", "perDenied")

  val expiredTime = 10l
  val JCZJEmployees = CacheBuilder.newBuilder().expireAfterWrite(expiredTime, TimeUnit.MINUTES)
    .build(new CacheLoader[String, List[EmployeeBasic]]() {
      def load(key: String) = {
        LOG.info("JCZJEmployees 未命中缓存")
        OrgSerivce.getAllEmployeesByOrg(JCZJOrgId).toList
      }
    })

  private val JCZJOrgId = 5174

  def getAdminUsers = {
    MsgpConfig.get("admins", defaultAdminUsers).split(",").toSet
  }

  def parseQueryStr(queryStr: String) = {
    val queryList = queryStr.split("&")
    queryList.map {
      x =>
        val splitArr = x.split("=")
        try {
          (splitArr.apply(0), splitArr.apply(1))
        }
        catch {
          case e: Exception => (null, null)
        }
    }
  }

  def getAppkey(queryStr: String): String = {
    val queryKey = parseQueryStr(queryStr)
    queryKey.foreach {
      x =>
        if (x._1 == "appkey") {
          return x._2
        }
    }
    return null
  }

  def isAdmin(login: String) = {
    val adminUsersGet = getAdminUsers
    adminUsersGet.contains(login)
  }

  def isWhiteUrl(url: String): Boolean = {
    whiteList.exists(_.startsWith(url))
  }


  // 判断是否有浏览管理页面的权限
  def hasMangagementAuth(user: User) = {
    val usersConfig = MsgpConfig.get("manage.view.auth", defaultAdminUsers)
    if (null != usersConfig) {
      usersConfig.split(",").toList.contains(user.getLogin)
    } else {
      false
    }
  }

}
