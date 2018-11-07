package com.sankuai.octo.msgp.serivce

import java.util

import com.sankuai.meituan.auth.vo.User
import com.sankuai.msgp.common.config.{DbConnection, MsgpConfig}
import com.sankuai.msgp.common.config.db.msgp.Tables.{AppkeyAuth2, _}
import com.sankuai.msgp.common.model.{Pdl, ServiceModels}
import com.sankuai.msgp.common.model.ServiceModels.Desc
import com.sankuai.msgp.common.service.org.{OpsService, OrgSerivce, SsoService}
import com.sankuai.msgp.common.utils.StringUtil
import com.sankuai.msgp.common.utils.helper.{AuthorityHelper, JsonHelper}
import com.sankuai.octo.msgp.domain.AppkeyReg
import com.sankuai.octo.msgp.serivce.service.ServiceCommon
import com.sankuai.octo.msgp.utils.Auth
import org.apache.commons.lang3.StringUtils
import org.slf4j.{Logger, LoggerFactory}
import play.api.libs.json.Reads._
import play.api.libs.json._

import scala.collection.JavaConverters._
import scala.collection.mutable.ListBuffer
import scala.slick.driver.MySQLDriver.simple._
import scala.util.parsing.json.JSONArray

object AppkeyAuth {
  val LOG: Logger = LoggerFactory.getLogger(AppkeyAuth.getClass)

  private val db = DbConnection.getPool()

  private val AdminOwts = Set("inf", "ep")

  case class MyUser(id: Integer, name: String)

  case class AppkeyUser(appkey: String, users: Set[MyUser])

  /**
    * 1:msgp管理员 拥有最高权限
    * 2:用户所在的部门 与appkey 业务线匹配自动获取 read 权限
    * 3:判定权限是否配置
    */
  def hasAuth(appkey: String, level: Int, user: User): Boolean = {
    if (level < Auth.Level.OBSERVER.getValue) {
      return true;
    }
    if (AuthorityHelper.isAdmin(user.getLogin) || StringUtil.isBlank(appkey) ||
      (level == Auth.Level.LOGIN.getValue && user != null)) {
      return true
    }

    val appkeyDesc = ServiceCommon.desc(appkey)
    //    if (isOwtAdmin(appkeyDesc, user.getLogin)) {
    //      return true
    //    }

    //根据level和Appkey获取这个Appkey所有有此权限的用户
    val map = getAppkeyAuth(appkey, level)
    val usersIdSet = map.getOrElse("user", List[AppkeyAuth2Row]()).map(_.userId).toSet

    // 1、当前用户在appkey的权限用户中 2、当前用户的部门与appkey部门一致
    val owtList = OpsService.getOwtsbyUsername(user.getLogin)
    if (usersIdSet.contains(user.getId)
      || (owtList.contains(appkeyDesc.owt.getOrElse("")) && level < Auth.Level.OBSERVER.getValue)
    ) {
      true
    }
    else {
      false
    }
  }

  def isQArole(user: User): Boolean = {
    "QA".equalsIgnoreCase(OrgSerivce.getRoleName(user.getLogin)) || AuthorityHelper.isAdmin(user.getLogin)
  }

  /**
    * 业务线管理员
    */
  def isOwtAdmin(desc: ServiceModels.Desc, login: String): Boolean = {
    val pdl = new Pdl(desc.owt.getOrElse(""), desc.pdl.getOrElse(""))
    val owners = OpsService.owtOwnerCache.get(pdl)
    if (owners.isEmpty) {
      LOG.info(s"没有服务负责人:appkey: ${desc.appkey} ${pdl}")
    }
    owners.filter(_.login.eq(login)).nonEmpty
  }

  /**
    * 管理员才可以删除服务
    *
    * @param appkey
    * @param login
    * @return
    */
  def hasAdminAuth(appkey: String, login: String): Boolean = {
    if (AuthorityHelper.isAdmin(login)) {
      return true
    }
    val map = AppkeyAuth.getAppkeyAuth(appkey, Auth.Level.ADMIN.getValue)
    val usersIdSet = map.getOrElse("user", List[AppkeyAuth2Row]()).map(_.userId).toSet
    val userId = SsoService.getUser(login).get.getId
    if (usersIdSet.contains(userId.toLong))
      true
    else
      false
  }

  def insertAuth(appkey: String, level: Int = Auth.Level.READ.getValue, users: List[ServiceModels.User]) = {
    /* 根据用户获取直属组，存储写权限**/
    val f = users.map(x => MyUser(x.id, x.name)).toSet
    insertToMysql(List(AppkeyUser(appkey, f)), level)
  }

  /* 现在只区分读写权限，默认给负责人的组都加上写权限**/
  def insertToMysql(appkeyUsers: List[AppkeyUser], level: Int = Auth.Level.READ.getValue) = {
    val update_time = new java.sql.Timestamp(System.currentTimeMillis())
    db withSession {
      implicit session: Session =>
        appkeyUsers.foreach {
          Self =>
            val AppkeyAuthRows =
              Self.users.map(user => AppkeyAuth2Row(0, user.id.toLong, Self.appkey, level, user.name, update_time))
            AppkeyAuth2 ++= AppkeyAuthRows
        }
    }
  }

  def delete(appkey: String, level: Int = Auth.Level.READ.getValue) = {
    db withSession {
      implicit session: Session =>
        val statement = AppkeyAuth2.filter(x => x.appkey === appkey && x.level === level)
        statement.delete
    }
  }

  def delete(appkey: String, users: List[ServiceModels.User], level: Int) = {
    db withSession {
      implicit session: Session =>
        val usernames = users.map(_.name).distinct.toSet
        val statement = AppkeyAuth2.filter { x => x.appkey === appkey && x.level === level && (x.name inSet usernames) }
        if (statement.exists.run) {
          statement.delete
        }
    }
  }

  def deleteNoAuth(appkey: String, users: List[String], level: Int) = {
    db withSession {
      implicit session: Session =>
        val usernames = users.distinct.toSet
        val statement = AppkeyAuth2.filter { x => x.appkey === appkey && x.level === level && (x.name inSet usernames) }
        if (statement.exists.run) {
          statement.delete
        }
    }
  }

  def delete(appkey: String) = {
    db withSession {
      implicit session: Session =>
        val statement = AppkeyAuth2.filter(x => x.appkey === appkey)
        statement.delete
    }
  }

  /**
    *
    * @param appkey
    * @param level
    * 删除部门权限
    * @return
    */
  def deleteOwt(appkey: String, level: Int = Auth.Level.READ.getValue) = {
    db withSession {
      implicit session: Session =>
        val statement = AppkeyAuth2.filter(x => x.appkey === appkey && x.level === level && x.userId === 0L)
        statement.delete
    }
  }

  /* 将一个用户有读写权限的appkey都取出来**/
  case class UserAndOwt(userIdList: collection.mutable.ArrayBuffer[Long], owtList: collection.mutable.ArrayBuffer[String])

  def getAppkeysByAuth(user: User, pdled: Boolean) = {
    /* 获取用户所属的所有组信息**/
    val owts = OpsService.getOwtsbyUsername(user.getLogin).asScala.toSet
    /* 将数据库中所有的appkey和相应权限取出来**/
    val f = db withSession {
      implicit session: Session =>
        if (pdled) {
          AppkeyAuth2.filter(x => x.level >= Auth.Level.ANON.getValue
            && (x.userId === user.getId.toLong || (x.userId === 0L && x.name.inSet(owts)))).foldLeft(List[String]()) {
            (result, self) =>
              result :+ self.appkey
          }
        } else {
          AppkeyAuth2.filter(x => x.level >= Auth.Level.ANON.getValue
            && (x.userId === user.getId.toLong)).foldLeft(List[String]()) {
            (result, self) =>
              result :+ self.appkey
          }
        }
    }
    f.sorted.asJava
  }

  def getAppkeyAuth(appkey: String, level: Int) = {
    db withSession {
      implicit session: Session =>
        AppkeyAuth2.filter(x => x.appkey === appkey && x.level >= level).list.groupBy(x => {
          if (x.userId > 0)
            "user"
          else
            "owt"
        }
        )
    }
  }

  def getAppkeyOwner(appkey: String, level: Int = Auth.Level.ADMIN.getValue) = {
    db withSession {
      implicit session: Session =>
        AppkeyAuth2.filter(x => x.appkey === appkey && x.level >= level && x.userId > 0L).list.map(_.userId)
    }
  }

  def getAppkeyOwnerStr(appkey: String, level: Int = Auth.Level.ADMIN.getValue) = {
    db withSession {
      implicit session: Session =>
        AppkeyAuth2.filter(x => x.appkey === appkey && x.level >= level && x.userId > 0L).list.map(_.name).mkString(",")
    }
  }

  def getAppkeysByUserId(userId: Long) = {
    db withSession {
      implicit session: Session =>
        AppkeyAuth2.filter(x => x.userId === userId).map(_.appkey).list
    }
  }

  def getAllOwners() = {
    db withSession {
      implicit session: Session =>
        AppkeyAuth2.filter(x => x.level === 16).map(_.userId).list.distinct
    }
  }

  def getAppkeyByObservers(user: User) = {
    db withSession {
      implicit session: Session =>
        AppkeyAuth2.filter(x => x.level === 12 && x.name === user.getName).map(_.appkey).list.distinct
    }
  }


  def isInSpecialOwt(appkey: String): Boolean = {
    val appkeyDesc = ServiceCommon.desc(appkey)
    val specialString = MsgpConfig.get("mccSpecialOwts", "qdb,fin,cx,pay,fsp,fd,cbp,zc,conch,insurance,qianbao")
    val SpecialOwts = specialString.split(",");
    if (StringUtils.isNotEmpty(specialString) && null != SpecialOwts && SpecialOwts.nonEmpty) {
      SpecialOwts.foldRight[Boolean](false) {
        (item, result) =>
          result ||  item.equalsIgnoreCase(appkeyDesc.owt.get)
      }
    }
    else {
      false
    }
  }

  //  def getAppkeysNotInappkeydesc(): java.util.List[String] = {
  //    db withSession {
  //      implicit session: Session =>
  //        val authAppkeys = AppkeyAuth2.list.map(_.appkey).distinct
  //        val descAppkeys = AppkeyDesc.list.map(_.appkey)
  //        (authAppkeys.toSet -- descAppkeys.toSet).toList.asJava
  //    }
  //  }

}
