package com.sankuai.octo.msgp.dao.appkey

import com.sankuai.msgp.common.config.DbConnection
import com.sankuai.msgp.common.config.db.msgp.Tables._
import com.sankuai.msgp.common.model.{Page, ServiceModels}
import com.sankuai.msgp.common.service.cellar.CellarService
import com.sankuai.msgp.common.utils.StringUtil
import com.sankuai.msgp.common.utils.helper.SqlParser
import com.sankuai.octo.msgp.serivce.https.mqServer
import play.api.libs.json.Json

import scala.collection.mutable.ListBuffer
import scala.slick.driver.MySQLDriver.simple._
import scala.slick.jdbc.StaticQuery.interpolation
import scala.slick.jdbc.{StaticQuery => Q}
import scala.slick.lifted.CanBeQueryCondition

object AppkeyDescDao {
  private val db = DbConnection.getPool()

  def batchInsert(rows: List[AppkeyDescRow]) = {
    db withSession {
      implicit session: Session =>
        val descList = ListBuffer[AppkeyDescRow]()
        rows.foreach { row =>
          val statement = AppkeyDesc.filter(x => x.appkey === row.appkey)
          if (statement.exists.run) {
            // update
            statement.map(x => (x.name, x.base, x.appkey, x.baseapp, x.owners, x.observers, x.pdl, x.owt, x.intro, x.tags, x.business, x.category, x.createTime))
              .update(row.name, row.base, row.appkey, row.baseapp, row.owners, row.observers, row.pdl, row.owt, row.intro, row.tags, row.business, row.category, row.createTime)
          } else {
            //insert
            descList.append(row)
          }
        }
        if (descList.nonEmpty) {
          AppkeyDesc ++= descList
        }
    }
  }

  def insert(row: AppkeyDescRow) = {
    db withSession {
      implicit session: Session =>
        val statement = AppkeyDesc.filter(x => x.appkey === row.appkey)
        if (statement.exists.run) {
          // update
          statement.map(x => (x.name, x.base, x.appkey, x.baseapp, x.owners, x.observers, x.pdl, x.owt, x.reglimit, x.intro, x.tags, x.business, x.category, x.createTime))
            .update(row.name, row.base, row.appkey, row.baseapp, row.owners, row.observers, row.pdl, row.owt, row.reglimit, row.intro, row.tags, row.business, row.category, row.createTime)
        } else {
          //insert
          AppkeyDesc += row
        }
    }
  }

  def updateCategory(appkey: String, category: String) = {
    db withSession {
      implicit session: Session =>
        AppkeyDesc.filter(_.appkey === appkey).map(_.category).update(category)
    }
  }

  def getCategory(appkey: String) = {
    db withSession {
      implicit session: Session =>
        AppkeyDesc.filter(_.appkey === appkey).map(_.category).first
    }
  }

  /**
    * 获取表中appkey的信息
    *
    * @return
    */
  def getAllAppkeyOwt = {
    db withSession {
      implicit session: Session =>
        AppkeyDesc.map(x => (x.owt, x.appkey)).list
    }
  }

  def delete(appkey: String) = {
    db withSession {
      implicit session: Session =>
        val statement = AppkeyDesc.filter(_.appkey === appkey)
        if (statement.exists.run) {
          //存在也是新增一条,之前的数据,失效
          statement.delete
        } else {
          1L
        }
    }
  }

  /**
    * 获取负责、关注的appkey
    * ADMIN(16)，OBSERVER(12),
    *
    */
  def searchByUser(user: ServiceModels.User, level: Integer, page: Page) = {
    val parmMap = Map(
      "aa.user_id" -> SqlParser.ValueExpress(String.valueOf(user.id)),
      "aa.level" -> SqlParser.ValueExpress(String.valueOf(level))
    )
    db withSession {
      implicit session: Session =>
        val sqlCountString = SqlParser.sqlParser("select count(*) from  appkey_desc  as ad  left join appkey_auth2 as aa on ad.appkey = aa.appkey  where 1=1 ", parmMap, "")
        val appkeyCount = sql"""#${sqlCountString}""".as[Int].list.headOption
        page.setTotalCount(appkeyCount.getOrElse(0))
        val sqlString = SqlParser.sqlParser("select ad.* from appkey_desc  as ad  left join appkey_auth2 as aa on ad.appkey = aa.appkey where 1 =1 ", parmMap, s" order by create_time desc limit ${page.getStart} , ${page.getPageSize} ")
        sql"""#${sqlString}""".as[AppkeyDesc#TableElementType].list
    }
  }

  def search(appkey: Option[String], owts: Option[List[String]], pdls: Option[List[String]], page: Page) = {
    db withSession {
      implicit session: Session =>
        val statement = AppkeyDesc.optionFilter(appkey)(_.appkey === _)
          .optionFilter(pdls)(_.pdl inSet _)
          .optionFilter(owts)(_.owt inSet _)
        val limit = page.getPageSize
        val offset = page.getStart
        val count = statement.length.run
        page.setTotalCount(count)
        statement.sortBy(_.createTime.desc).drop(offset).take(limit).list
    }
  }

  def apps(keyword: String = "") = {
    db withSession {
      implicit session: Session =>
        var sqlString = "select appkey from appkey_desc"
        if (StringUtil.isNotBlank(keyword)) {
          sqlString += s" and appkey like  '%$keyword%' "
        }
        sql"""#${sqlString}""".as[String].list
    }
  }

  def appsByCategory(category: String) = {
    db withSession {
      implicit session: Session =>
        var sqlString = "select appkey from appkey_desc"
        if (StringUtil.isNotBlank(category)) {
          sqlString += s" where category like  '%$category%' "
        }
        sql"""#${sqlString}""".as[String].list
    }
  }

  def search(keyword: String = "", page: Page) = {
    db withSession {
      implicit session: Session =>
        val statement = if (StringUtil.isNotBlank(keyword)) {
          AppkeyDesc.filter(desc => (desc.appkey like "%" + keyword + "%")
            || (desc.tags like "%" + keyword + "%")
            || (desc.intro like "%" + keyword + "%")
            || (desc.owners like "%" + keyword + "%")
          )
        } else {
          AppkeyDesc.sortBy(_.createTime.desc)
        }
        val limit = page.getPageSize
        val offset = page.getStart
        val count = statement.length.run
        page.setTotalCount(count)
        statement.drop(offset).take(limit).list

    }
  }


  def appsbyuser(user_id: Int, keyword: String = "") = {
    val parmMap = if (StringUtil.isNotBlank(keyword)) {
      Map(
        "aa.user_id" -> SqlParser.ValueExpress(String.valueOf(user_id)),
        "aa.appkey" -> SqlParser.ValueExpress(s"%$keyword%", "like")
      )
    } else {
      Map("aa.user_id" -> SqlParser.ValueExpress(String.valueOf(user_id)))
    }
    db withSession {
      implicit session: Session =>
        val sqlString = SqlParser.sqlParser("select ad.appkey from appkey_desc  as ad  left join appkey_auth2 as aa " +
          "on ad.appkey = aa.appkey where 1 =1 ", parmMap, s" group by appkey ")
        sql"""#${sqlString}""".as[String].list

    }
  }

  def appkeys(business: Int, owt: String, pdl: String) = {
    val realBusiness = if (business == -1) "" else business.toString
    val parameterMap = Map("business" -> SqlParser.ValueExpress(realBusiness, "="), "owt" -> SqlParser.ValueExpress(owt, "="), "pdl" -> SqlParser.ValueExpress(pdl, "="))
    val prefixSQL = "SELECT DISTINCT appkey FROM appkey_desc WHERE 1 = 1"
    val suffixSQL = ""
    val sqlStr = SqlParser.sqlParser(prefixSQL, parameterMap, suffixSQL)
    db withSession {
      implicit session: Session =>
        val appkeys = sql"""#$sqlStr""".as[String].list
        appkeys
    }
  }

  def isAppkeyExist(appkey: String) = {
    val sql = s"select 1 from appkey_desc where appkey = '$appkey' limit 1";
    db withSession {
      implicit session: Session =>
        val result = sql"""#$sql""".as[Int].list
        if (result.size > 0) {
          true
        } else {
          false
        }
    }
  }

  def appsbyowt(owt: Option[String]) = {
    db withSession {
      implicit session: Session =>
        AppkeyDesc.optionFilter(owt)(_.owt === _).groupBy(_.appkey).map {
          case (appkey, list) =>
            appkey
        }.list
    }
  }


  def get(appkey: String) = {
    db withSession {
      implicit session: Session =>
        val statement = AppkeyDesc.filter(x => x.appkey === appkey)
        statement.firstOption
    }
  }

  case class ServiceCategories(location: String, count: Int)

  implicit val reads = Json.reads[ServiceCategories]
  implicit val writes = Json.writes[ServiceCategories]

  def groupByLocation = {
    db withSession {
      implicit session: Session =>
        val ret = AppkeyDesc.groupBy(x => x.base).map {
          case (base, list) =>
            (base, list.length)
        }.list
        ret.map { x =>
          if (x._1 == 0) {
            ("北京", x._2)
          } else {
            ("上海", x._2)
          }
        }.toMap
    }
  }

  case class AppType(appkey: String, providerType: String)

  def groupByType = {
    db withSession {
      implicit session: Session =>
        val cellarRet = CellarService.cellarAppkeys()
        val mqRet = mqServer.mqAppkeys()

        val appsWithType = AppkeyDesc.list.par.map { x =>
          val appkey = x.appkey

          val providers = AppkeyProviderDao.appProviderCount(appkey)
          val typeMap = providers.map(_._1)
          if (typeMap.isEmpty) {
            var appTypeRet = AppType(appkey, "no provider")

            if (cellarRet.nonEmpty) {
              val cellarAppkeys = cellarRet.get.data.get
              if (cellarAppkeys.contains(appkey)) {
                appTypeRet = AppType(appkey, "cellar")
              }
            } else {
              if (mqRet.nonEmpty) {
                val mqAppkeys = mqServer.mqAppkeys().get.map(_.appkey)
                if (mqAppkeys.contains(appkey)) {
                  appTypeRet = AppType(appkey, "mq")
                }
              }
            }
            appTypeRet
          } else {
            if (typeMap.contains("thrift") && typeMap.contains("http")) {
              AppType(appkey, "thrift & http")
            } else if (typeMap.contains("thrift")) {
              AppType(appkey, "thrift")
            } else {
              AppType(appkey, "http")
            }
          }
        }.toList

        appsWithType.groupBy(_.providerType).map { x =>
          (x._1, x._2.length)
        }.toMap
    }
  }

  def getRegisterationLimited = {
    db withSession {
      implicit session: Session =>
        AppkeyDesc.filter(_.reglimit === 1).map(_.appkey).list
    }
  }


  implicit class QueryHelper[T, E](query: Query[T, E, Seq]) {
    def optionFilter[X, R: CanBeQueryCondition](name: Option[X])(f: (T, X) => R) =
      name.map(v => query.withFilter(f(_, v))).getOrElse(query)
  }

}
