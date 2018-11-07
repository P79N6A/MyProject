package com.sankuai.octo.msgp.dao.appkey

import com.sankuai.msgp.common.config.DbConnection
import com.sankuai.msgp.common.config.db.msgp.Tables._
import com.sankuai.msgp.common.model.Page
import com.sankuai.msgp.common.utils.StringUtil
import com.sankuai.msgp.common.utils.helper.SqlParser

import scala.collection.mutable.ListBuffer
import scala.slick.driver.MySQLDriver.simple._
import scala.slick.jdbc.StaticQuery.interpolation
import scala.slick.jdbc.{GetResult, StaticQuery => Q}
import scala.slick.lifted.CanBeQueryCondition

object AppkeyProviderDao {

  private val db = DbConnection.getPool()

  implicit class QueryHelper[T, E](query: Query[T, E, Seq]) {
    def optionFilter[X, R: CanBeQueryCondition](name: Option[X])(f: (T, X) => R) =
      name.map(v => query.withFilter(f(_, v))).getOrElse(query)
  }

  case class appkeyIp(appkey: String, ip: String)

  case class IpPortName(ip: String, name: String, port: Int)

  implicit val getAppkeyIpResult = GetResult(r => appkeyIp(r.<<, r.<<))
  implicit val getIpPortNameResult = GetResult(r => IpPortName(r.<<, r.<<, r.<<))

  def appsbytype(nodeType: String, keyword: String = "") = {
    db withSession {
      implicit session: Session =>
        var sqlString = s"SELECT distinct appkey from appkey_provider where `type` = $nodeType"
        if (StringUtil.isNotBlank(keyword)) {
          sqlString += s" and appkey like  '%$keyword%' "
        }
        sql"""#${sqlString}""".as[String].list
    }
  }

  def apps() = {
    db withSession {
      implicit session: Session =>
        sql"SELECT distinct appkey from appkey_provider".as[String].list
    }
  }

  def appsProd() = {
    db withSession {
      implicit session: Session =>
        sql"SELECT distinct appkey from appkey_provider where env=3".as[String].list
    }
  }

  def apps(ips: List[String]) = {
    db withSession {
      implicit session: Session =>
        val str_ips = ips.mkString("','")
        val sqlString = s"SELECT  distinct appkey from appkey_provider where ip in ('$str_ips')"
        sql"""#${sqlString}""".as[String].list
    }
  }

  def appkeyProviderby(`nodeType`: String, env: Int) = {
    db withSession {
      implicit session: Session =>
        sql"SELECT  appkey,ip from appkey_provider where `type` = $nodeType and env = $env".as[appkeyIp].list
    }
  }


  def batchInsert(rows: List[AppkeyProviderRow]) = {
    db withSession {
      implicit session: Session =>
        val descList = ListBuffer[AppkeyProviderRow]()
        rows.foreach { row =>
          val serviceinfo = if (StringUtil.isNotBlank(row.serviceinfo) && row.serviceinfo.length > 1024) {
            row.serviceinfo.substring(0, 1023)
          } else {
            row.serviceinfo
          }
          //          val statement = AppkeyProvider.filter(x => x.appkey === row.appkey
          //            && x.`type` === row.`type` && x.env === row.env
          //            && x.ip === row.ip
          //            && x.port === row.port)
          //          if (statement.exists.run) {
          //            statement.map(x => (x.hostname, x.version, x.weight, x.fweight, x.status,
          //              x.enabled, x.lastupdatetime, x.extend, x.servertype, x.protocol, x.serviceinfo, x.heartbeatsupport, x.idc))
          //              .update(row.hostname, row.version, row.weight, row.fweight, row.status,
          //                row.enabled, row.lastupdatetime, row.extend, row.servertype, row.protocol, serviceinfo, row.heartbeatsupport, row.idc)
          //          } else {
          //            val edit_row = row.copy(serviceinfo = serviceinfo)
          //            descList.append(edit_row)
          //          }
          val edit_row = row.copy(serviceinfo = serviceinfo)
          descList.append(edit_row)
        }
        if (descList.nonEmpty) {
          AppkeyProvider ++= descList
        }
    }
  }


  def insert(row: AppkeyProviderRow) = {
    db withSession {
      implicit session: Session =>
        val statement = AppkeyProvider.filter(x => x.appkey === row.appkey
          && x.`type` === row.`type` && x.env === row.env
          && x.ip === row.ip
          && x.port === row.port)
        if (statement.exists.run) {
          statement.map(x => (x.version, x.weight, x.fweight, x.status,
            x.enabled, x.lastupdatetime, x.extend, x.servertype, x.protocol, x.serviceinfo, x.heartbeatsupport, x.idc))
            .update(row.version, row.weight, row.fweight, row.status,
              row.enabled, row.lastupdatetime, row.extend, row.servertype, row.protocol, row.serviceinfo, row.heartbeatsupport, row.idc)
        } else {
          AppkeyProvider += row
        }
    }
  }

  def delete(appkey: String) = {
    db withSession {
      implicit session: Session =>
        val statement = AppkeyProvider.filter(_.appkey === appkey)
        if (statement.exists.run) {
          //存在也是新增一条,之前的数据,失效
          statement.delete
        } else {
          1L
        }
    }
  }

  def search(appkey: String, `type`: String = "thrift", env: Int = 2, status: Int = -1, ip: String, port: java.lang.Integer, sortby: String, page: Page) = {
    val ipOpt = Option(ip)
    val portOpt = if (port == null) {
      None
    } else {
      Some(port.toInt)
    }

    db withSession {
      implicit session: Session =>
        val statement = AppkeyProvider.filter(x => x.appkey === appkey && x.`type` === `type` && x.env === env && x.status === status)
          .optionFilter(ipOpt)(_.ip === _)
          .optionFilter(portOpt)(_.port === _)
        val limit = page.getPageSize
        val offset = page.getStart
        val count = statement.length.run
        page.setTotalCount(count)
        statement.drop(offset).take(limit).list
    }
  }

  def searchByVersion(appkey: String, env: Int = 2, version: String) = {

    db withSession {
      implicit session: Session =>
        val statement = AppkeyProvider.filter(x => x.appkey === appkey && x.env === env && x.version === version)
        statement.list
    }
  }

  def searchByIp(appkey: String, ip_type: String = "thrift", env: Int = 2, ip: String, hostname: String, sortby: String, page: Page) = {

    var from_sql_str = s"  from appkey_provider where appkey ='$appkey' and type = '$ip_type' and env = $env"

    val ipOpt = if (ip != null) {
      Some(s" ip like '${ip}%'")
    } else {
      None
    }
    val hostOpt = if (hostname != null) {
      Some(s"hostname like '${hostname}%'")
    } else {
      None
    }
    val ip_host = if (ipOpt.isDefined && hostOpt.isDefined) {
      s"(${ipOpt.get} or ${hostOpt.get} )"
    } else if (ipOpt.isDefined) {
      s"${ipOpt.get}  "
    } else if (hostOpt.isDefined) {
      s"${hostOpt.get}  "
    }
    from_sql_str += s" and ${ip_host}"
    db withSession {
      implicit session: Session =>

        val sqlCountString = "select count(*)" + from_sql_str
        val count = sql"""#${sqlCountString}""".as[Int].list.headOption
        page.setTotalCount(count.getOrElse(0))

        val sqlString = "select * " + from_sql_str + s" order by lastUpdateTime desc limit ${page.getStart} , ${page.getPageSize} "
        sql"""#${sqlString}""".as[AppkeyProvider#TableElementType].list

    }
  }


  case class ProviderCount(appkey: String, status: Int, idc: String, nodeCount: Int, hostCount: Int)

  implicit val getProviderCountResult = GetResult(r => ProviderCount(r.<<, r.<<, r.<<, r.<<, r.<<))

  def statusSearch(appkey: String, `type`: String, env: java.lang.Integer, status: java.lang.Integer, page: Page) = {
    val parmMap = Map("appkey" -> SqlParser.ValueExpress(if (StringUtil.isNotBlank(appkey)) {
      s"${appkey}%"
    } else {
      null
    }, "like"),
      "type" -> SqlParser.ValueExpress(`type`),
      "env" -> SqlParser.ValueExpress(String.valueOf(env)),
      "`status`" -> SqlParser.ValueExpress(String.valueOf(status)),
      "appkey " -> SqlParser.ValueExpress("com.sankuai.inf.sg_agent' and appkey <> 'com.sankuai.inf.kms_agent", "<>")
    )
    db withSession {
      implicit session: Session =>
        val sqlCountString = SqlParser.sqlParser("select count(*) from (select appkey,`status` from appkey_provider  where 1=1 ", parmMap, "group by appkey,`status` ) as t")
        val appkeyCount = sql"""#${sqlCountString}""".as[Int].list.headOption
        page.setTotalCount(appkeyCount.getOrElse(0))
        val groupby = " group by appkey,`status`"

        val sqlString = SqlParser.sqlParser("select appkey,`status`,idc, count(*) as nodeCount, count(distinct(ip)) as hostCount from appkey_provider where 1 =1 ", parmMap, s" $groupby  order by nodeCount desc limit ${page.getStart} , ${page.getPageSize} ")
        sql"""#${sqlString}""".as[ProviderCount].list
    }
  }

  def idcSearch(appkey: String, `type`: String, env: java.lang.Integer, status: java.lang.Integer, idc: String, page: Page) = {
    val parmMap = Map("appkey" -> SqlParser.ValueExpress(if (StringUtil.isNotBlank(appkey)) {
      s"${appkey}%"
    } else {
      null
    }, "like"),
      "type" -> SqlParser.ValueExpress(`type`),
      "env" -> SqlParser.ValueExpress(String.valueOf(env)),
      "`status`" -> SqlParser.ValueExpress(String.valueOf(status)),
      "idc" -> SqlParser.ValueExpress(idc),
      "appkey " -> SqlParser.ValueExpress("com.sankuai.inf.sg_agent' and appkey <> 'com.sankuai.inf.kms_agent", "<>")
    )
    db withSession {
      implicit session: Session =>
        val sqlCountString = SqlParser.sqlParser("select count(*) from (select appkey, idc from appkey_provider where 1=1 ", parmMap, " group by appkey,`idc` ) as t")
        val appkeyCount = sql"""#${sqlCountString}""".as[Int].list.headOption
        page.setTotalCount(appkeyCount.getOrElse(0))
        val groupby = " group by appkey,idc"
        val sqlString = SqlParser.sqlParser("select appkey,`status`,idc, count(*) as nodeCount, count(distinct(ip)) as hostCount from appkey_provider where 1 =1 ", parmMap, s" $groupby  order by nodeCount desc limit ${page.getStart} , ${page.getPageSize} ")
        sql"""#${sqlString}""".as[ProviderCount].list
    }
  }


  case class StatusCount(status: Int, count: Int)

  implicit val getStatusCountResult = GetResult(r => StatusCount(r.<<, r.<<))

  case class EnvCount(env: Int, count: Int)

  implicit val getEnvCountResult = GetResult(r => EnvCount(r.<<, r.<<))

  case class TypeCount(`type`: String, count: Int)

  implicit val getTypeCountResult = GetResult(r => TypeCount(r.<<, r.<<))

  case class IDCCount(idc: String, count: Int)

  implicit val getIDCCountResult = GetResult(r => IDCCount(r.<<, r.<<))


  def providerStatusCount(`type`: String, env: java.lang.Integer, status: java.lang.Integer) = {
    db withSession {
      implicit session: Session =>
        val paramMap = Map("type" -> SqlParser.ValueExpress(`type`),
          "env" -> SqlParser.ValueExpress(String.valueOf(env)),
          "`status`" -> SqlParser.ValueExpress(String.valueOf(status))
        )

        val sqlEnvCountString = SqlParser.sqlParser("select env,count(*) from appkey_provider where 1=1 ", paramMap, " group by env ")
        val envCounts = sql"""#${sqlEnvCountString}""".as[EnvCount].list

        val sqlTypeCountString = SqlParser.sqlParser("select type,count(*) from appkey_provider where 1=1 ", paramMap, " group by type ")
        val typeCounts = sql"""#${sqlTypeCountString}""".as[TypeCount].list

        val sqlStatusCountString = SqlParser.sqlParser("select `status`,count(*) from appkey_provider where 1=1 ", paramMap, " group by `status`")
        val statusCounts = sql"""#${sqlStatusCountString}""".as[StatusCount].list

        (typeCounts, envCounts, statusCounts, List[IDCCount]())

    }
  }

  def providerIdcCount(`type`: String, env: java.lang.Integer, status: java.lang.Integer, idc: String) = {
    db withSession {
      implicit session: Session =>

        val paramMap = Map("type" -> SqlParser.ValueExpress(`type`),
          "env" -> SqlParser.ValueExpress(String.valueOf(env)),
          "`status`" -> SqlParser.ValueExpress(String.valueOf(status)),
          "idc" -> SqlParser.ValueExpress(String.valueOf(idc))
        )

        val sqlEnvCountString = SqlParser.sqlParser("select env,count(*) from appkey_provider where 1=1 ", paramMap, " group by env ")
        val envCounts = sql"""#${sqlEnvCountString}""".as[EnvCount].list

        val sqlTypeCountString = SqlParser.sqlParser("select type,count(*) from appkey_provider where 1=1 ", paramMap, " group by type ")
        val typeCounts = sql"""#${sqlTypeCountString}""".as[TypeCount].list

        val sqlIdcCountString = SqlParser.sqlParser("select idc,count(*) from appkey_provider where 1=1 ", paramMap, " group by idc ")
        val idcCounts = sql"""#${sqlIdcCountString}""".as[IDCCount].list

        (typeCounts, envCounts, List[StatusCount](), idcCounts)
    }
  }

  def appProviderCount(appkey: String) = {
    db withSession {
      implicit session: Session =>
        AppkeyProvider.filter(x => x.appkey === appkey).groupBy(x => x.`type`).map {
          case (providerType, list) =>
            (providerType, list.length)
        }.list
    }
  }

  def provdiers(appkey: String, env: Int) = {
    db withSession {
      implicit session: Session =>
        sql"SELECT ip, hostname as `name`, port from appkey_provider where `appkey` = $appkey and env=$env".as[IpPortName].list
    }
  }

  def ipListByType(nodeType: String) = {
    db withSession {
      implicit session: Session =>
        sql"SELECT distinct ip from appkey_provider where `type` = $nodeType".as[String].list
    }
  }
}
