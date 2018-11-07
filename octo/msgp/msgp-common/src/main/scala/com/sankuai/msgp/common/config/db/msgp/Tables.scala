package com.sankuai.msgp.common.config.db.msgp

// AUTO-GENERATED Slick data model
/** Stand-alone Slick data model for immediate use */
object Tables extends {
  val profile = scala.slick.driver.MySQLDriver
} with Tables

/** Slick data model trait for extension, choice of backend or usage in the cake pattern. (Make sure to initialize this late.) */
trait Tables {
  val profile: scala.slick.driver.JdbcProfile
  import profile.simple._
  // NOTE: GetResult mappers for plain SQL are only generated for tables where Slick knows how to map the types of all columns.
  import scala.slick.jdbc.{GetResult => GR}

  /** DDL for all tables. Call .create to execute. */
  lazy val ddl = AgentChecker.ddl ++ AppActiveness.ddl ++ AppAdmin.ddl ++ AppAlias.ddl ++ AppBom.ddl ++ AppConfig.ddl ++ AppConfigExt.ddl ++ AppDependency.ddl ++ AppDesc.ddl ++ AppGraph.ddl ++ AppkeyAbandoned.ddl ++ AppkeyAuth.ddl ++ AppkeyAuth2.ddl ++ AppkeyDashboard.ddl ++ AppkeyDesc.ddl ++ AppkeyFavorite.ddl ++ AppkeyProvider.ddl ++ AppkeyProviderTrigger.ddl ++ AppkeySubscribe.ddl ++ AppkeyTrigger.ddl ++ AppkeyTriggerCount.ddl ++ AppkeyTriggerStatus.ddl ++ AppMethodDoc.ddl ++ AppQuota.ddl ++ AppScreen.ddl ++ AppScreenAuth.ddl ++ AppServer.ddl ++ AppSubscribe.ddl ++ AppTrend.ddl ++ AppTypeDoc.ddl ++ AppUpstream.ddl ++ AvailabilityData.ddl ++ AvailabilityDayDetail.ddl ++ AvailabilityDayReport.ddl ++ Banner.ddl ++ BusinessDash.ddl ++ BusinessMonitor.ddl ++ BusinessMonitorCount.ddl ++ BusinessTriggerSubscribe.ddl ++ ComponentCoverage.ddl ++ ConsumerQuota.ddl ++ ConsumerQuotaConfig.ddl ++ ErrorDashboard.ddl ++ Event.ddl ++ MnsapiAuth.ddl ++ Oauth2Client.ddl ++ Oauth2Token.ddl ++ OctoJob.ddl ++ OctoLog.ddl ++ OswatchLog.ddl ++ PerfDay.ddl ++ PerfHour.ddl ++ PerfIndicator.ddl ++ PluginConfig.ddl ++ ProviderTriggerCount.ddl ++ ProviderTriggerSubscribe.ddl ++ RealtimeLog.ddl ++ ReportDaily.ddl ++ ReportDailyMail.ddl ++ ReportDailyStatus.ddl ++ ReportDepend.ddl ++ ReportErrorlog.ddl ++ ReportIdcTraffic.ddl ++ ReportQps.ddl ++ ReportQpsPeak.ddl ++ ScannerLog.ddl ++ SchedulerCost.ddl ++ ServiceProvider.ddl ++ SgAgentLog.ddl ++ SpanKpiDay.ddl ++ Switchenv.ddl ++ TaskLog.ddl ++ TriggerEvent.ddl ++ TriggerSubscribe.ddl ++ UserShortcut.ddl

  /** Entity class storing rows of table AgentChecker
    *  @param id Database column id DBType(BIGINT), AutoInc, PrimaryKey
    *  @param name Database column name DBType(VARCHAR), Length(128,true), Default()
    *  @param protocol Database column protocol DBType(VARCHAR), Length(10,true), Default(thrift)
    *  @param providers Database column providers DBType(MEDIUMTEXT), Length(16777215,true)
    *  @param apps Database column apps DBType(MEDIUMTEXT), Length(16777215,true)
    *  @param status Database column status DBType(INT), Default(0)
    *  @param time Database column time DBType(BIGINT), Default(0) */
  case class AgentCheckerRow(id: Long, name: String = "", protocol: String = "thrift", providers: String, apps: String, status: Int = 0, time: Long = 0L)
  /** GetResult implicit for fetching AgentCheckerRow objects using plain SQL queries */
  implicit def GetResultAgentCheckerRow(implicit e0: GR[Long], e1: GR[String], e2: GR[Int]): GR[AgentCheckerRow] = GR{
    prs => import prs._
      AgentCheckerRow.tupled((<<[Long], <<[String], <<[String], <<[String], <<[String], <<[Int], <<[Long]))
  }
  /** Table description of table agent_checker. Objects of this class serve as prototypes for rows in queries. */
  class AgentChecker(_tableTag: Tag) extends Table[AgentCheckerRow](_tableTag, "agent_checker") {
    def * = (id, name, protocol, providers, apps, status, time) <> (AgentCheckerRow.tupled, AgentCheckerRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (id.?, name.?, protocol.?, providers.?, apps.?, status.?, time.?).shaped.<>({r=>import r._; _1.map(_=> AgentCheckerRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6.get, _7.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id DBType(BIGINT), AutoInc, PrimaryKey */
    val id: Column[Long] = column[Long]("id", O.AutoInc, O.PrimaryKey)
    /** Database column name DBType(VARCHAR), Length(128,true), Default() */
    val name: Column[String] = column[String]("name", O.Length(128,varying=true), O.Default(""))
    /** Database column protocol DBType(VARCHAR), Length(10,true), Default(thrift) */
    val protocol: Column[String] = column[String]("protocol", O.Length(10,varying=true), O.Default("thrift"))
    /** Database column providers DBType(MEDIUMTEXT), Length(16777215,true) */
    val providers: Column[String] = column[String]("providers", O.Length(16777215,varying=true))
    /** Database column apps DBType(MEDIUMTEXT), Length(16777215,true) */
    val apps: Column[String] = column[String]("apps", O.Length(16777215,varying=true))
    /** Database column status DBType(INT), Default(0) */
    val status: Column[Int] = column[Int]("status", O.Default(0))
    /** Database column time DBType(BIGINT), Default(0) */
    val time: Column[Long] = column[Long]("time", O.Default(0L))

    /** Uniqueness Index over (name) (database name name) */
    val index1 = index("name", name, unique=true)
  }
  /** Collection-like TableQuery object for table AgentChecker */
  lazy val AgentChecker = new TableQuery(tag => new AgentChecker(tag))

  /** Entity class storing rows of table AppActiveness
    *  @param id Database column id DBType(INT UNSIGNED), AutoInc, PrimaryKey
    *  @param base Database column base DBType(VARCHAR), Length(11,true), Default()
    *  @param business Database column business DBType(VARCHAR), Length(128,true), Default()
    *  @param owt Database column owt DBType(VARCHAR), Length(64,true), Default()
    *  @param pdl Database column pdl DBType(VARCHAR), Length(64,true), Default()
    *  @param app Database column app DBType(VARCHAR), Length(128,true), Default()
    *  @param appGroupId Database column app_group_id DBType(VARCHAR), Length(256,true), Default()
    *  @param appArtifactId Database column app_artifact_id DBType(VARCHAR), Length(128,true), Default()
    *  @param appVersion Database column app_version DBType(VARCHAR), Length(128,true), Default()
    *  @param appkey Database column appkey DBType(VARCHAR), Length(128,true), Default()
    *  @param count Database column count DBType(INT), Default(0)
    *  @param date Database column date DBType(DATE) */
  case class AppActivenessRow(id: Int, base: String = "", business: String = "", owt: String = "", pdl: String = "", app: String = "", appGroupId: String = "", appArtifactId: String = "", appVersion: String = "", appkey: String = "", count: Int = 0, date: java.sql.Date)
  /** GetResult implicit for fetching AppActivenessRow objects using plain SQL queries */
  implicit def GetResultAppActivenessRow(implicit e0: GR[Int], e1: GR[String], e2: GR[java.sql.Date]): GR[AppActivenessRow] = GR{
    prs => import prs._
      AppActivenessRow.tupled((<<[Int], <<[String], <<[String], <<[String], <<[String], <<[String], <<[String], <<[String], <<[String], <<[String], <<[Int], <<[java.sql.Date]))
  }
  /** Table description of table app_activeness. Objects of this class serve as prototypes for rows in queries. */
  class AppActiveness(_tableTag: Tag) extends Table[AppActivenessRow](_tableTag, "app_activeness") {
    def * = (id, base, business, owt, pdl, app, appGroupId, appArtifactId, appVersion, appkey, count, date) <> (AppActivenessRow.tupled, AppActivenessRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (id.?, base.?, business.?, owt.?, pdl.?, app.?, appGroupId.?, appArtifactId.?, appVersion.?, appkey.?, count.?, date.?).shaped.<>({r=>import r._; _1.map(_=> AppActivenessRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6.get, _7.get, _8.get, _9.get, _10.get, _11.get, _12.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id DBType(INT UNSIGNED), AutoInc, PrimaryKey */
    val id: Column[Int] = column[Int]("id", O.AutoInc, O.PrimaryKey)
    /** Database column base DBType(VARCHAR), Length(11,true), Default() */
    val base: Column[String] = column[String]("base", O.Length(11,varying=true), O.Default(""))
    /** Database column business DBType(VARCHAR), Length(128,true), Default() */
    val business: Column[String] = column[String]("business", O.Length(128,varying=true), O.Default(""))
    /** Database column owt DBType(VARCHAR), Length(64,true), Default() */
    val owt: Column[String] = column[String]("owt", O.Length(64,varying=true), O.Default(""))
    /** Database column pdl DBType(VARCHAR), Length(64,true), Default() */
    val pdl: Column[String] = column[String]("pdl", O.Length(64,varying=true), O.Default(""))
    /** Database column app DBType(VARCHAR), Length(128,true), Default() */
    val app: Column[String] = column[String]("app", O.Length(128,varying=true), O.Default(""))
    /** Database column app_group_id DBType(VARCHAR), Length(256,true), Default() */
    val appGroupId: Column[String] = column[String]("app_group_id", O.Length(256,varying=true), O.Default(""))
    /** Database column app_artifact_id DBType(VARCHAR), Length(128,true), Default() */
    val appArtifactId: Column[String] = column[String]("app_artifact_id", O.Length(128,varying=true), O.Default(""))
    /** Database column app_version DBType(VARCHAR), Length(128,true), Default() */
    val appVersion: Column[String] = column[String]("app_version", O.Length(128,varying=true), O.Default(""))
    /** Database column appkey DBType(VARCHAR), Length(128,true), Default() */
    val appkey: Column[String] = column[String]("appkey", O.Length(128,varying=true), O.Default(""))
    /** Database column count DBType(INT), Default(0) */
    val count: Column[Int] = column[Int]("count", O.Default(0))
    /** Database column date DBType(DATE) */
    val date: Column[java.sql.Date] = column[java.sql.Date]("date")

    /** Uniqueness Index over (business,owt,pdl,app,date) (database name app) */
    val index1 = index("app", (business, owt, pdl, app, date), unique=true)
  }
  /** Collection-like TableQuery object for table AppActiveness */
  lazy val AppActiveness = new TableQuery(tag => new AppActiveness(tag))

  /** Entity class storing rows of table AppAdmin
    *  @param id Database column id DBType(INT UNSIGNED), AutoInc, PrimaryKey
    *  @param appName Database column app_name DBType(VARCHAR), Length(128,true), Default()
    *  @param adminsName Database column admins_name DBType(VARCHAR), Length(256,true), Default()
    *  @param adminsId Database column admins_id DBType(VARCHAR), Length(256,true), Default() */
  case class AppAdminRow(id: Int, appName: String = "", adminsName: String = "", adminsId: String = "")
  /** GetResult implicit for fetching AppAdminRow objects using plain SQL queries */
  implicit def GetResultAppAdminRow(implicit e0: GR[Int], e1: GR[String]): GR[AppAdminRow] = GR{
    prs => import prs._
      AppAdminRow.tupled((<<[Int], <<[String], <<[String], <<[String]))
  }
  /** Table description of table app_admin. Objects of this class serve as prototypes for rows in queries. */
  class AppAdmin(_tableTag: Tag) extends Table[AppAdminRow](_tableTag, "app_admin") {
    def * = (id, appName, adminsName, adminsId) <> (AppAdminRow.tupled, AppAdminRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (id.?, appName.?, adminsName.?, adminsId.?).shaped.<>({r=>import r._; _1.map(_=> AppAdminRow.tupled((_1.get, _2.get, _3.get, _4.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id DBType(INT UNSIGNED), AutoInc, PrimaryKey */
    val id: Column[Int] = column[Int]("id", O.AutoInc, O.PrimaryKey)
    /** Database column app_name DBType(VARCHAR), Length(128,true), Default() */
    val appName: Column[String] = column[String]("app_name", O.Length(128,varying=true), O.Default(""))
    /** Database column admins_name DBType(VARCHAR), Length(256,true), Default() */
    val adminsName: Column[String] = column[String]("admins_name", O.Length(256,varying=true), O.Default(""))
    /** Database column admins_id DBType(VARCHAR), Length(256,true), Default() */
    val adminsId: Column[String] = column[String]("admins_id", O.Length(256,varying=true), O.Default(""))
  }
  /** Collection-like TableQuery object for table AppAdmin */
  lazy val AppAdmin = new TableQuery(tag => new AppAdmin(tag))

  /** Entity class storing rows of table AppAlias
    *  @param id Database column id DBType(BIGINT), AutoInc, PrimaryKey
    *  @param appkey Database column appkey DBType(VARCHAR), Length(128,true), Default()
    *  @param errorLogAppkey Database column error_log_appkey DBType(VARCHAR), Length(128,true), Default()
    *  @param desc Database column desc DBType(VARCHAR), Length(2048,true), Default(Some()) */
  case class AppAliasRow(id: Long, appkey: String = "", errorLogAppkey: String = "", desc: Option[String] = Some(""))
  /** GetResult implicit for fetching AppAliasRow objects using plain SQL queries */
  implicit def GetResultAppAliasRow(implicit e0: GR[Long], e1: GR[String], e2: GR[Option[String]]): GR[AppAliasRow] = GR{
    prs => import prs._
      AppAliasRow.tupled((<<[Long], <<[String], <<[String], <<?[String]))
  }
  /** Table description of table app_alias. Objects of this class serve as prototypes for rows in queries. */
  class AppAlias(_tableTag: Tag) extends Table[AppAliasRow](_tableTag, "app_alias") {
    def * = (id, appkey, errorLogAppkey, desc) <> (AppAliasRow.tupled, AppAliasRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (id.?, appkey.?, errorLogAppkey.?, desc).shaped.<>({r=>import r._; _1.map(_=> AppAliasRow.tupled((_1.get, _2.get, _3.get, _4)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id DBType(BIGINT), AutoInc, PrimaryKey */
    val id: Column[Long] = column[Long]("id", O.AutoInc, O.PrimaryKey)
    /** Database column appkey DBType(VARCHAR), Length(128,true), Default() */
    val appkey: Column[String] = column[String]("appkey", O.Length(128,varying=true), O.Default(""))
    /** Database column error_log_appkey DBType(VARCHAR), Length(128,true), Default() */
    val errorLogAppkey: Column[String] = column[String]("error_log_appkey", O.Length(128,varying=true), O.Default(""))
    /** Database column desc DBType(VARCHAR), Length(2048,true), Default(Some()) */
    val desc: Column[Option[String]] = column[Option[String]]("desc", O.Length(2048,varying=true), O.Default(Some("")))

    /** Uniqueness Index over (appkey) (database name appkey) */
    val index1 = index("appkey", appkey, unique=true)
    /** Index over (appkey) (database name idx_id) */
    val index2 = index("idx_id", appkey)
  }
  /** Collection-like TableQuery object for table AppAlias */
  lazy val AppAlias = new TableQuery(tag => new AppAlias(tag))

  /** Entity class storing rows of table AppBom
    *  @param id Database column id DBType(INT UNSIGNED), AutoInc, PrimaryKey
    *  @param business Database column business DBType(VARCHAR), Length(128,true), Default()
    *  @param owt Database column owt DBType(VARCHAR), Length(11,true), Default()
    *  @param pdl Database column pdl DBType(VARCHAR), Length(64,true), Default()
    *  @param app Database column app DBType(VARCHAR), Length(128,true), Default()
    *  @param appkey Database column appkey DBType(VARCHAR), Length(128,true), Default()
    *  @param base Database column base DBType(VARCHAR), Length(11,true), Default()
    *  @param appGroupId Database column app_group_id DBType(VARCHAR), Length(256,true), Default()
    *  @param appArtifactId Database column app_artifact_id DBType(VARCHAR), Length(128,true), Default()
    *  @param appVersion Database column app_version DBType(VARCHAR), Length(128,true), Default()
    *  @param appPackaging Database column app_packaging DBType(VARCHAR), Length(32,true), Default()
    *  @param infBomUsed Database column inf_bom_used DBType(INT), Default(0)
    *  @param infBomVersion Database column inf_bom_version DBType(VARCHAR), Length(128,true), Default()
    *  @param xmdBomUsed Database column xmd_bom_used DBType(INT), Default(0)
    *  @param xmdBomVersion Database column xmd_bom_version DBType(VARCHAR), Length(128,true), Default()
    *  @param createTime Database column create_time DBType(BIGINT)
    *  @param uploadTime Database column upload_time DBType(BIGINT) */
  case class AppBomRow(id: Int, business: String = "", owt: String = "", pdl: String = "", app: String = "", appkey: String = "", base: String = "", appGroupId: String = "", appArtifactId: String = "", appVersion: String = "", appPackaging: String = "", infBomUsed: Int = 0, infBomVersion: String = "", xmdBomUsed: Int = 0, xmdBomVersion: String = "", createTime: Long, uploadTime: Long)
  /** GetResult implicit for fetching AppBomRow objects using plain SQL queries */
  implicit def GetResultAppBomRow(implicit e0: GR[Int], e1: GR[String], e2: GR[Long]): GR[AppBomRow] = GR{
    prs => import prs._
      AppBomRow.tupled((<<[Int], <<[String], <<[String], <<[String], <<[String], <<[String], <<[String], <<[String], <<[String], <<[String], <<[String], <<[Int], <<[String], <<[Int], <<[String], <<[Long], <<[Long]))
  }
  /** Table description of table app_bom. Objects of this class serve as prototypes for rows in queries. */
  class AppBom(_tableTag: Tag) extends Table[AppBomRow](_tableTag, "app_bom") {
    def * = (id, business, owt, pdl, app, appkey, base, appGroupId, appArtifactId, appVersion, appPackaging, infBomUsed, infBomVersion, xmdBomUsed, xmdBomVersion, createTime, uploadTime) <> (AppBomRow.tupled, AppBomRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (id.?, business.?, owt.?, pdl.?, app.?, appkey.?, base.?, appGroupId.?, appArtifactId.?, appVersion.?, appPackaging.?, infBomUsed.?, infBomVersion.?, xmdBomUsed.?, xmdBomVersion.?, createTime.?, uploadTime.?).shaped.<>({r=>import r._; _1.map(_=> AppBomRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6.get, _7.get, _8.get, _9.get, _10.get, _11.get, _12.get, _13.get, _14.get, _15.get, _16.get, _17.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id DBType(INT UNSIGNED), AutoInc, PrimaryKey */
    val id: Column[Int] = column[Int]("id", O.AutoInc, O.PrimaryKey)
    /** Database column business DBType(VARCHAR), Length(128,true), Default() */
    val business: Column[String] = column[String]("business", O.Length(128,varying=true), O.Default(""))
    /** Database column owt DBType(VARCHAR), Length(11,true), Default() */
    val owt: Column[String] = column[String]("owt", O.Length(11,varying=true), O.Default(""))
    /** Database column pdl DBType(VARCHAR), Length(64,true), Default() */
    val pdl: Column[String] = column[String]("pdl", O.Length(64,varying=true), O.Default(""))
    /** Database column app DBType(VARCHAR), Length(128,true), Default() */
    val app: Column[String] = column[String]("app", O.Length(128,varying=true), O.Default(""))
    /** Database column appkey DBType(VARCHAR), Length(128,true), Default() */
    val appkey: Column[String] = column[String]("appkey", O.Length(128,varying=true), O.Default(""))
    /** Database column base DBType(VARCHAR), Length(11,true), Default() */
    val base: Column[String] = column[String]("base", O.Length(11,varying=true), O.Default(""))
    /** Database column app_group_id DBType(VARCHAR), Length(256,true), Default() */
    val appGroupId: Column[String] = column[String]("app_group_id", O.Length(256,varying=true), O.Default(""))
    /** Database column app_artifact_id DBType(VARCHAR), Length(128,true), Default() */
    val appArtifactId: Column[String] = column[String]("app_artifact_id", O.Length(128,varying=true), O.Default(""))
    /** Database column app_version DBType(VARCHAR), Length(128,true), Default() */
    val appVersion: Column[String] = column[String]("app_version", O.Length(128,varying=true), O.Default(""))
    /** Database column app_packaging DBType(VARCHAR), Length(32,true), Default() */
    val appPackaging: Column[String] = column[String]("app_packaging", O.Length(32,varying=true), O.Default(""))
    /** Database column inf_bom_used DBType(INT), Default(0) */
    val infBomUsed: Column[Int] = column[Int]("inf_bom_used", O.Default(0))
    /** Database column inf_bom_version DBType(VARCHAR), Length(128,true), Default() */
    val infBomVersion: Column[String] = column[String]("inf_bom_version", O.Length(128,varying=true), O.Default(""))
    /** Database column xmd_bom_used DBType(INT), Default(0) */
    val xmdBomUsed: Column[Int] = column[Int]("xmd_bom_used", O.Default(0))
    /** Database column xmd_bom_version DBType(VARCHAR), Length(128,true), Default() */
    val xmdBomVersion: Column[String] = column[String]("xmd_bom_version", O.Length(128,varying=true), O.Default(""))
    /** Database column create_time DBType(BIGINT) */
    val createTime: Column[Long] = column[Long]("create_time")
    /** Database column upload_time DBType(BIGINT) */
    val uploadTime: Column[Long] = column[Long]("upload_time")

    /** Index over (appGroupId,appArtifactId,appVersion) (database name app_idx) */
    val index1 = index("app_idx", (appGroupId, appArtifactId, appVersion))
  }
  /** Collection-like TableQuery object for table AppBom */
  lazy val AppBom = new TableQuery(tag => new AppBom(tag))

  /** Entity class storing rows of table AppConfig
    *  @param id Database column id DBType(INT UNSIGNED), AutoInc, PrimaryKey
    *  @param business Database column business DBType(VARCHAR), Length(128,true), Default()
    *  @param owt Database column owt DBType(VARCHAR), Length(11,true), Default()
    *  @param pdl Database column pdl DBType(VARCHAR), Length(64,true), Default()
    *  @param base Database column base DBType(VARCHAR), Length(11,true), Default()
    *  @param groupId Database column group_id DBType(VARCHAR), Length(256,true), Default()
    *  @param artifactId Database column artifact_id DBType(VARCHAR), Length(128,true), Default()
    *  @param version Database column version DBType(VARCHAR), Length(512,true), Default()
    *  @param action Database column action DBType(VARCHAR), Length(64,true), Default() */
  case class AppConfigRow(id: Int, business: String = "", owt: String = "", pdl: String = "", base: String = "", groupId: String = "", artifactId: String = "", version: String = "", action: String = "")
  /** GetResult implicit for fetching AppConfigRow objects using plain SQL queries */
  implicit def GetResultAppConfigRow(implicit e0: GR[Int], e1: GR[String]): GR[AppConfigRow] = GR{
    prs => import prs._
      AppConfigRow.tupled((<<[Int], <<[String], <<[String], <<[String], <<[String], <<[String], <<[String], <<[String], <<[String]))
  }
  /** Table description of table app_config. Objects of this class serve as prototypes for rows in queries. */
  class AppConfig(_tableTag: Tag) extends Table[AppConfigRow](_tableTag, "app_config") {
    def * = (id, business, owt, pdl, base, groupId, artifactId, version, action) <> (AppConfigRow.tupled, AppConfigRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (id.?, business.?, owt.?, pdl.?, base.?, groupId.?, artifactId.?, version.?, action.?).shaped.<>({r=>import r._; _1.map(_=> AppConfigRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6.get, _7.get, _8.get, _9.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id DBType(INT UNSIGNED), AutoInc, PrimaryKey */
    val id: Column[Int] = column[Int]("id", O.AutoInc, O.PrimaryKey)
    /** Database column business DBType(VARCHAR), Length(128,true), Default() */
    val business: Column[String] = column[String]("business", O.Length(128,varying=true), O.Default(""))
    /** Database column owt DBType(VARCHAR), Length(11,true), Default() */
    val owt: Column[String] = column[String]("owt", O.Length(11,varying=true), O.Default(""))
    /** Database column pdl DBType(VARCHAR), Length(64,true), Default() */
    val pdl: Column[String] = column[String]("pdl", O.Length(64,varying=true), O.Default(""))
    /** Database column base DBType(VARCHAR), Length(11,true), Default() */
    val base: Column[String] = column[String]("base", O.Length(11,varying=true), O.Default(""))
    /** Database column group_id DBType(VARCHAR), Length(256,true), Default() */
    val groupId: Column[String] = column[String]("group_id", O.Length(256,varying=true), O.Default(""))
    /** Database column artifact_id DBType(VARCHAR), Length(128,true), Default() */
    val artifactId: Column[String] = column[String]("artifact_id", O.Length(128,varying=true), O.Default(""))
    /** Database column version DBType(VARCHAR), Length(512,true), Default() */
    val version: Column[String] = column[String]("version", O.Length(512,varying=true), O.Default(""))
    /** Database column action DBType(VARCHAR), Length(64,true), Default() */
    val action: Column[String] = column[String]("action", O.Length(64,varying=true), O.Default(""))
  }
  /** Collection-like TableQuery object for table AppConfig */
  lazy val AppConfig = new TableQuery(tag => new AppConfig(tag))

  /** Entity class storing rows of table AppConfigExt
    *  @param id Database column id DBType(INT UNSIGNED), AutoInc, PrimaryKey
    *  @param appConfigId Database column app_config_id DBType(INT), Default(0)
    *  @param business Database column business DBType(VARCHAR), Length(128,true), Default()
    *  @param owt Database column owt DBType(VARCHAR), Length(11,true), Default()
    *  @param pdl Database column pdl DBType(VARCHAR), Length(64,true), Default()
    *  @param app Database column app DBType(VARCHAR), Length(128,true), Default() */
  case class AppConfigExtRow(id: Int, appConfigId: Int = 0, business: String = "", owt: String = "", pdl: String = "", app: String = "")
  /** GetResult implicit for fetching AppConfigExtRow objects using plain SQL queries */
  implicit def GetResultAppConfigExtRow(implicit e0: GR[Int], e1: GR[String]): GR[AppConfigExtRow] = GR{
    prs => import prs._
      AppConfigExtRow.tupled((<<[Int], <<[Int], <<[String], <<[String], <<[String], <<[String]))
  }
  /** Table description of table app_config_ext. Objects of this class serve as prototypes for rows in queries. */
  class AppConfigExt(_tableTag: Tag) extends Table[AppConfigExtRow](_tableTag, "app_config_ext") {
    def * = (id, appConfigId, business, owt, pdl, app) <> (AppConfigExtRow.tupled, AppConfigExtRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (id.?, appConfigId.?, business.?, owt.?, pdl.?, app.?).shaped.<>({r=>import r._; _1.map(_=> AppConfigExtRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id DBType(INT UNSIGNED), AutoInc, PrimaryKey */
    val id: Column[Int] = column[Int]("id", O.AutoInc, O.PrimaryKey)
    /** Database column app_config_id DBType(INT), Default(0) */
    val appConfigId: Column[Int] = column[Int]("app_config_id", O.Default(0))
    /** Database column business DBType(VARCHAR), Length(128,true), Default() */
    val business: Column[String] = column[String]("business", O.Length(128,varying=true), O.Default(""))
    /** Database column owt DBType(VARCHAR), Length(11,true), Default() */
    val owt: Column[String] = column[String]("owt", O.Length(11,varying=true), O.Default(""))
    /** Database column pdl DBType(VARCHAR), Length(64,true), Default() */
    val pdl: Column[String] = column[String]("pdl", O.Length(64,varying=true), O.Default(""))
    /** Database column app DBType(VARCHAR), Length(128,true), Default() */
    val app: Column[String] = column[String]("app", O.Length(128,varying=true), O.Default(""))
  }
  /** Collection-like TableQuery object for table AppConfigExt */
  lazy val AppConfigExt = new TableQuery(tag => new AppConfigExt(tag))

  /** Entity class storing rows of table AppDependency
    *  @param id Database column id DBType(BIGINT), AutoInc, PrimaryKey
    *  @param base Database column base DBType(VARCHAR), Length(11,true), Default()
    *  @param business Database column business DBType(VARCHAR), Length(128,true), Default()
    *  @param owt Database column owt DBType(VARCHAR), Length(64,true), Default()
    *  @param pdl Database column pdl DBType(VARCHAR), Length(64,true), Default()
    *  @param app Database column app DBType(VARCHAR), Length(128,true), Default()
    *  @param appGroupId Database column app_group_id DBType(VARCHAR), Length(256,true), Default()
    *  @param appArtifactId Database column app_artifact_id DBType(VARCHAR), Length(128,true), Default()
    *  @param appVersion Database column app_version DBType(VARCHAR), Length(128,true), Default()
    *  @param appkey Database column appkey DBType(VARCHAR), Length(128,true), Default()
    *  @param groupId Database column group_id DBType(VARCHAR), Length(256,true), Default()
    *  @param artifactId Database column artifact_id DBType(VARCHAR), Length(128,true), Default()
    *  @param version Database column version DBType(VARCHAR), Length(128,true), Default()
    *  @param category Database column category DBType(VARCHAR), Length(128,true), Default()
    *  @param createTime Database column create_time DBType(BIGINT), Default(0)
    *  @param uploadTime Database column upload_time DBType(BIGINT), Default(0) */
  case class AppDependencyRow(id: Long, base: String = "", business: String = "", owt: String = "", pdl: String = "", app: String = "", appGroupId: String = "", appArtifactId: String = "", appVersion: String = "", appkey: String = "", groupId: String = "", artifactId: String = "", version: String = "", category: String = "", createTime: Long = 0L, uploadTime: Long = 0L)
  /** GetResult implicit for fetching AppDependencyRow objects using plain SQL queries */
  implicit def GetResultAppDependencyRow(implicit e0: GR[Long], e1: GR[String]): GR[AppDependencyRow] = GR{
    prs => import prs._
      AppDependencyRow.tupled((<<[Long], <<[String], <<[String], <<[String], <<[String], <<[String], <<[String], <<[String], <<[String], <<[String], <<[String], <<[String], <<[String], <<[String], <<[Long], <<[Long]))
  }
  /** Table description of table app_dependency. Objects of this class serve as prototypes for rows in queries. */
  class AppDependency(_tableTag: Tag) extends Table[AppDependencyRow](_tableTag, "app_dependency") {
    def * = (id, base, business, owt, pdl, app, appGroupId, appArtifactId, appVersion, appkey, groupId, artifactId, version, category, createTime, uploadTime) <> (AppDependencyRow.tupled, AppDependencyRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (id.?, base.?, business.?, owt.?, pdl.?, app.?, appGroupId.?, appArtifactId.?, appVersion.?, appkey.?, groupId.?, artifactId.?, version.?, category.?, createTime.?, uploadTime.?).shaped.<>({r=>import r._; _1.map(_=> AppDependencyRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6.get, _7.get, _8.get, _9.get, _10.get, _11.get, _12.get, _13.get, _14.get, _15.get, _16.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id DBType(BIGINT), AutoInc, PrimaryKey */
    val id: Column[Long] = column[Long]("id", O.AutoInc, O.PrimaryKey)
    /** Database column base DBType(VARCHAR), Length(11,true), Default() */
    val base: Column[String] = column[String]("base", O.Length(11,varying=true), O.Default(""))
    /** Database column business DBType(VARCHAR), Length(128,true), Default() */
    val business: Column[String] = column[String]("business", O.Length(128,varying=true), O.Default(""))
    /** Database column owt DBType(VARCHAR), Length(64,true), Default() */
    val owt: Column[String] = column[String]("owt", O.Length(64,varying=true), O.Default(""))
    /** Database column pdl DBType(VARCHAR), Length(64,true), Default() */
    val pdl: Column[String] = column[String]("pdl", O.Length(64,varying=true), O.Default(""))
    /** Database column app DBType(VARCHAR), Length(128,true), Default() */
    val app: Column[String] = column[String]("app", O.Length(128,varying=true), O.Default(""))
    /** Database column app_group_id DBType(VARCHAR), Length(256,true), Default() */
    val appGroupId: Column[String] = column[String]("app_group_id", O.Length(256,varying=true), O.Default(""))
    /** Database column app_artifact_id DBType(VARCHAR), Length(128,true), Default() */
    val appArtifactId: Column[String] = column[String]("app_artifact_id", O.Length(128,varying=true), O.Default(""))
    /** Database column app_version DBType(VARCHAR), Length(128,true), Default() */
    val appVersion: Column[String] = column[String]("app_version", O.Length(128,varying=true), O.Default(""))
    /** Database column appkey DBType(VARCHAR), Length(128,true), Default() */
    val appkey: Column[String] = column[String]("appkey", O.Length(128,varying=true), O.Default(""))
    /** Database column group_id DBType(VARCHAR), Length(256,true), Default() */
    val groupId: Column[String] = column[String]("group_id", O.Length(256,varying=true), O.Default(""))
    /** Database column artifact_id DBType(VARCHAR), Length(128,true), Default() */
    val artifactId: Column[String] = column[String]("artifact_id", O.Length(128,varying=true), O.Default(""))
    /** Database column version DBType(VARCHAR), Length(128,true), Default() */
    val version: Column[String] = column[String]("version", O.Length(128,varying=true), O.Default(""))
    /** Database column category DBType(VARCHAR), Length(128,true), Default() */
    val category: Column[String] = column[String]("category", O.Length(128,varying=true), O.Default(""))
    /** Database column create_time DBType(BIGINT), Default(0) */
    val createTime: Column[Long] = column[Long]("create_time", O.Default(0L))
    /** Database column upload_time DBType(BIGINT), Default(0) */
    val uploadTime: Column[Long] = column[Long]("upload_time", O.Default(0L))

    /** Index over (appGroupId,appArtifactId,appVersion) (database name idx_app) */
    val index1 = index("idx_app", (appGroupId, appArtifactId, appVersion))
    /** Index over (appkey) (database name idx_appkey) */
    val index2 = index("idx_appkey", appkey)
    /** Index over (business,owt,pdl) (database name idx_business) */
    val index3 = index("idx_business", (business, owt, pdl))
    /** Index over (groupId,artifactId,version) (database name idx_component) */
    val index4 = index("idx_component", (groupId, artifactId, version))
    /** Index over (appGroupId,appArtifactId,uploadTime) (database name idx_group_time) */
    val index5 = index("idx_group_time", (appGroupId, appArtifactId, uploadTime))
  }
  /** Collection-like TableQuery object for table AppDependency */
  lazy val AppDependency = new TableQuery(tag => new AppDependency(tag))

  /** Entity class storing rows of table AppDesc
    *  @param id Database column id DBType(BIGINT), AutoInc, PrimaryKey
    *  @param name Database column name DBType(VARCHAR), Length(1024,true), Default()
    *  @param appkey Database column appkey DBType(VARCHAR), Length(128,true), Default()
    *  @param intro Database column intro DBType(VARCHAR), Length(2048,true), Default()
    *  @param category Database column category DBType(VARCHAR), Length(128,true), Default()
    *  @param business Database column business DBType(INT), Default(100)
    *  @param group Database column group DBType(VARCHAR), Length(1024,true), Default()
    *  @param level Database column level DBType(INT), Default(1)
    *  @param tags Database column tags DBType(VARCHAR), Length(1024,true), Default()
    *  @param createTime Database column create_time DBType(BIGINT), Default(0)
    *  @param xAxis Database column x_axis DBType(INT), Default(0)
    *  @param yAxis Database column y_axis DBType(INT), Default(0) */
  case class AppDescRow(id: Long, name: String = "", appkey: String = "", intro: String = "", category: String = "", business: Int = 100, group: String = "", level: Int = 1, tags: String = "", createTime: Long = 0L, xAxis: Int = 0, yAxis: Int = 0)
  /** GetResult implicit for fetching AppDescRow objects using plain SQL queries */
  implicit def GetResultAppDescRow(implicit e0: GR[Long], e1: GR[String], e2: GR[Int]): GR[AppDescRow] = GR{
    prs => import prs._
      AppDescRow.tupled((<<[Long], <<[String], <<[String], <<[String], <<[String], <<[Int], <<[String], <<[Int], <<[String], <<[Long], <<[Int], <<[Int]))
  }
  /** Table description of table app_desc. Objects of this class serve as prototypes for rows in queries. */
  class AppDesc(_tableTag: Tag) extends Table[AppDescRow](_tableTag, "app_desc") {
    def * = (id, name, appkey, intro, category, business, group, level, tags, createTime, xAxis, yAxis) <> (AppDescRow.tupled, AppDescRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (id.?, name.?, appkey.?, intro.?, category.?, business.?, group.?, level.?, tags.?, createTime.?, xAxis.?, yAxis.?).shaped.<>({r=>import r._; _1.map(_=> AppDescRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6.get, _7.get, _8.get, _9.get, _10.get, _11.get, _12.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id DBType(BIGINT), AutoInc, PrimaryKey */
    val id: Column[Long] = column[Long]("id", O.AutoInc, O.PrimaryKey)
    /** Database column name DBType(VARCHAR), Length(1024,true), Default() */
    val name: Column[String] = column[String]("name", O.Length(1024,varying=true), O.Default(""))
    /** Database column appkey DBType(VARCHAR), Length(128,true), Default() */
    val appkey: Column[String] = column[String]("appkey", O.Length(128,varying=true), O.Default(""))
    /** Database column intro DBType(VARCHAR), Length(2048,true), Default() */
    val intro: Column[String] = column[String]("intro", O.Length(2048,varying=true), O.Default(""))
    /** Database column category DBType(VARCHAR), Length(128,true), Default() */
    val category: Column[String] = column[String]("category", O.Length(128,varying=true), O.Default(""))
    /** Database column business DBType(INT), Default(100) */
    val business: Column[Int] = column[Int]("business", O.Default(100))
    /** Database column group DBType(VARCHAR), Length(1024,true), Default() */
    val group: Column[String] = column[String]("group", O.Length(1024,varying=true), O.Default(""))
    /** Database column level DBType(INT), Default(1) */
    val level: Column[Int] = column[Int]("level", O.Default(1))
    /** Database column tags DBType(VARCHAR), Length(1024,true), Default() */
    val tags: Column[String] = column[String]("tags", O.Length(1024,varying=true), O.Default(""))
    /** Database column create_time DBType(BIGINT), Default(0) */
    val createTime: Column[Long] = column[Long]("create_time", O.Default(0L))
    /** Database column x_axis DBType(INT), Default(0) */
    val xAxis: Column[Int] = column[Int]("x_axis", O.Default(0))
    /** Database column y_axis DBType(INT), Default(0) */
    val yAxis: Column[Int] = column[Int]("y_axis", O.Default(0))
  }
  /** Collection-like TableQuery object for table AppDesc */
  lazy val AppDesc = new TableQuery(tag => new AppDesc(tag))

  /** Entity class storing rows of table AppGraph
    *  @param id Database column id DBType(BIGINT), AutoInc, PrimaryKey
    *  @param graphId Database column graph_id DBType(INT), Default(100)
    *  @param appkey Database column appkey DBType(VARCHAR), Length(128,true), Default()
    *  @param x Database column x DBType(INT), Default(0)
    *  @param y Database column y DBType(INT), Default(0) */
  case class AppGraphRow(id: Long, graphId: Int = 100, appkey: String = "", x: Int = 0, y: Int = 0)
  /** GetResult implicit for fetching AppGraphRow objects using plain SQL queries */
  implicit def GetResultAppGraphRow(implicit e0: GR[Long], e1: GR[Int], e2: GR[String]): GR[AppGraphRow] = GR{
    prs => import prs._
      AppGraphRow.tupled((<<[Long], <<[Int], <<[String], <<[Int], <<[Int]))
  }
  /** Table description of table app_graph. Objects of this class serve as prototypes for rows in queries. */
  class AppGraph(_tableTag: Tag) extends Table[AppGraphRow](_tableTag, "app_graph") {
    def * = (id, graphId, appkey, x, y) <> (AppGraphRow.tupled, AppGraphRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (id.?, graphId.?, appkey.?, x.?, y.?).shaped.<>({r=>import r._; _1.map(_=> AppGraphRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id DBType(BIGINT), AutoInc, PrimaryKey */
    val id: Column[Long] = column[Long]("id", O.AutoInc, O.PrimaryKey)
    /** Database column graph_id DBType(INT), Default(100) */
    val graphId: Column[Int] = column[Int]("graph_id", O.Default(100))
    /** Database column appkey DBType(VARCHAR), Length(128,true), Default() */
    val appkey: Column[String] = column[String]("appkey", O.Length(128,varying=true), O.Default(""))
    /** Database column x DBType(INT), Default(0) */
    val x: Column[Int] = column[Int]("x", O.Default(0))
    /** Database column y DBType(INT), Default(0) */
    val y: Column[Int] = column[Int]("y", O.Default(0))
  }
  /** Collection-like TableQuery object for table AppGraph */
  lazy val AppGraph = new TableQuery(tag => new AppGraph(tag))

  /** Entity class storing rows of table AppkeyAbandoned
    *  @param id Database column id DBType(BIGINT), AutoInc, PrimaryKey
    *  @param name Database column name DBType(VARCHAR), Length(128,true), Default()
    *  @param base Database column base DBType(INT)
    *  @param appkey Database column appkey DBType(VARCHAR), Length(128,true), Default()
    *  @param baseapp Database column baseApp DBType(VARCHAR), Length(128,true), Default()
    *  @param owners Database column owners DBType(TEXT), Length(65535,true)
    *  @param observers Database column observers DBType(TEXT), Length(65535,true)
    *  @param pdl Database column pdl DBType(VARCHAR), Length(32,true), Default(0)
    *  @param owt Database column owt DBType(VARCHAR), Length(32,true), Default(0)
    *  @param intro Database column intro DBType(TEXT), Length(65535,true)
    *  @param tags Database column tags DBType(VARCHAR), Length(256,true), Default(0)
    *  @param business Database column business DBType(INT), Default(100)
    *  @param category Database column category DBType(VARCHAR), Length(128,true)
    *  @param reglimit Database column regLimit DBType(INT), Default(0)
    *  @param operator Database column operator DBType(VARCHAR), Length(128,true), Default()
    *  @param deleteTime Database column delete_time DBType(BIGINT), Default(0) */
  case class AppkeyAbandonedRow(id: Long, name: String = "", base: Int, appkey: String = "", baseapp: String = "", owners: String, observers: String, pdl: String = "0", owt: String = "0", intro: String, tags: String = "0", business: Int = 100, category: String, reglimit: Int = 0, operator: String = "", deleteTime: Long = 0L)
  /** GetResult implicit for fetching AppkeyAbandonedRow objects using plain SQL queries */
  implicit def GetResultAppkeyAbandonedRow(implicit e0: GR[Long], e1: GR[String], e2: GR[Int]): GR[AppkeyAbandonedRow] = GR{
    prs => import prs._
      AppkeyAbandonedRow.tupled((<<[Long], <<[String], <<[Int], <<[String], <<[String], <<[String], <<[String], <<[String], <<[String], <<[String], <<[String], <<[Int], <<[String], <<[Int], <<[String], <<[Long]))
  }
  /** Table description of table appkey_abandoned. Objects of this class serve as prototypes for rows in queries. */
  class AppkeyAbandoned(_tableTag: Tag) extends Table[AppkeyAbandonedRow](_tableTag, "appkey_abandoned") {
    def * = (id, name, base, appkey, baseapp, owners, observers, pdl, owt, intro, tags, business, category, reglimit, operator, deleteTime) <> (AppkeyAbandonedRow.tupled, AppkeyAbandonedRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (id.?, name.?, base.?, appkey.?, baseapp.?, owners.?, observers.?, pdl.?, owt.?, intro.?, tags.?, business.?, category.?, reglimit.?, operator.?, deleteTime.?).shaped.<>({r=>import r._; _1.map(_=> AppkeyAbandonedRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6.get, _7.get, _8.get, _9.get, _10.get, _11.get, _12.get, _13.get, _14.get, _15.get, _16.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id DBType(BIGINT), AutoInc, PrimaryKey */
    val id: Column[Long] = column[Long]("id", O.AutoInc, O.PrimaryKey)
    /** Database column name DBType(VARCHAR), Length(128,true), Default() */
    val name: Column[String] = column[String]("name", O.Length(128,varying=true), O.Default(""))
    /** Database column base DBType(INT) */
    val base: Column[Int] = column[Int]("base")
    /** Database column appkey DBType(VARCHAR), Length(128,true), Default() */
    val appkey: Column[String] = column[String]("appkey", O.Length(128,varying=true), O.Default(""))
    /** Database column baseApp DBType(VARCHAR), Length(128,true), Default() */
    val baseapp: Column[String] = column[String]("baseApp", O.Length(128,varying=true), O.Default(""))
    /** Database column owners DBType(TEXT), Length(65535,true) */
    val owners: Column[String] = column[String]("owners", O.Length(65535,varying=true))
    /** Database column observers DBType(TEXT), Length(65535,true) */
    val observers: Column[String] = column[String]("observers", O.Length(65535,varying=true))
    /** Database column pdl DBType(VARCHAR), Length(32,true), Default(0) */
    val pdl: Column[String] = column[String]("pdl", O.Length(32,varying=true), O.Default("0"))
    /** Database column owt DBType(VARCHAR), Length(32,true), Default(0) */
    val owt: Column[String] = column[String]("owt", O.Length(32,varying=true), O.Default("0"))
    /** Database column intro DBType(TEXT), Length(65535,true) */
    val intro: Column[String] = column[String]("intro", O.Length(65535,varying=true))
    /** Database column tags DBType(VARCHAR), Length(256,true), Default(0) */
    val tags: Column[String] = column[String]("tags", O.Length(256,varying=true), O.Default("0"))
    /** Database column business DBType(INT), Default(100) */
    val business: Column[Int] = column[Int]("business", O.Default(100))
    /** Database column category DBType(VARCHAR), Length(128,true) */
    val category: Column[String] = column[String]("category", O.Length(128,varying=true))
    /** Database column regLimit DBType(INT), Default(0) */
    val reglimit: Column[Int] = column[Int]("regLimit", O.Default(0))
    /** Database column operator DBType(VARCHAR), Length(128,true), Default() */
    val operator: Column[String] = column[String]("operator", O.Length(128,varying=true), O.Default(""))
    /** Database column delete_time DBType(BIGINT), Default(0) */
    val deleteTime: Column[Long] = column[Long]("delete_time", O.Default(0L))

    /** Index over (appkey) (database name idx_appkey) */
    val index1 = index("idx_appkey", appkey)
    /** Index over (owt) (database name idx_owt) */
    val index2 = index("idx_owt", owt)
    /** Index over (pdl) (database name idx_pdl) */
    val index3 = index("idx_pdl", pdl)
    /** Index over (tags) (database name idx_tags) */
    val index4 = index("idx_tags", tags)
  }
  /** Collection-like TableQuery object for table AppkeyAbandoned */
  lazy val AppkeyAbandoned = new TableQuery(tag => new AppkeyAbandoned(tag))

  /** Entity class storing rows of table AppkeyAuth
    *  @param id Database column id DBType(BIGINT), AutoInc, PrimaryKey
    *  @param flag Database column flag DBType(INT), Default(0)
    *  @param userOrgId Database column user_org_id DBType(BIGINT), Default(0)
    *  @param appkey Database column appkey DBType(VARCHAR), Length(128,true)
    *  @param level Database column level DBType(INT), Default(0)
    *  @param name Database column name DBType(VARCHAR), Length(128,true), Default() */
  case class AppkeyAuthRow(id: Long, flag: Int = 0, userOrgId: Long = 0L, appkey: String, level: Int = 0, name: String = "")
  /** GetResult implicit for fetching AppkeyAuthRow objects using plain SQL queries */
  implicit def GetResultAppkeyAuthRow(implicit e0: GR[Long], e1: GR[Int], e2: GR[String]): GR[AppkeyAuthRow] = GR{
    prs => import prs._
      AppkeyAuthRow.tupled((<<[Long], <<[Int], <<[Long], <<[String], <<[Int], <<[String]))
  }
  /** Table description of table appkey_auth. Objects of this class serve as prototypes for rows in queries. */
  class AppkeyAuth(_tableTag: Tag) extends Table[AppkeyAuthRow](_tableTag, "appkey_auth") {
    def * = (id, flag, userOrgId, appkey, level, name) <> (AppkeyAuthRow.tupled, AppkeyAuthRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (id.?, flag.?, userOrgId.?, appkey.?, level.?, name.?).shaped.<>({r=>import r._; _1.map(_=> AppkeyAuthRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id DBType(BIGINT), AutoInc, PrimaryKey */
    val id: Column[Long] = column[Long]("id", O.AutoInc, O.PrimaryKey)
    /** Database column flag DBType(INT), Default(0) */
    val flag: Column[Int] = column[Int]("flag", O.Default(0))
    /** Database column user_org_id DBType(BIGINT), Default(0) */
    val userOrgId: Column[Long] = column[Long]("user_org_id", O.Default(0L))
    /** Database column appkey DBType(VARCHAR), Length(128,true) */
    val appkey: Column[String] = column[String]("appkey", O.Length(128,varying=true))
    /** Database column level DBType(INT), Default(0) */
    val level: Column[Int] = column[Int]("level", O.Default(0))
    /** Database column name DBType(VARCHAR), Length(128,true), Default() */
    val name: Column[String] = column[String]("name", O.Length(128,varying=true), O.Default(""))
  }
  /** Collection-like TableQuery object for table AppkeyAuth */
  lazy val AppkeyAuth = new TableQuery(tag => new AppkeyAuth(tag))

  /** Entity class storing rows of table AppkeyAuth2
    *  @param id Database column id DBType(BIGINT), AutoInc, PrimaryKey
    *  @param userId Database column user_id DBType(BIGINT), Default(0)
    *  @param appkey Database column appkey DBType(VARCHAR), Length(128,true)
    *  @param level Database column level DBType(INT), Default(0)
    *  @param name Database column name DBType(VARCHAR), Length(128,true), Default()
    *  @param updateTime Database column update_time DBType(TIMESTAMP) */
  case class AppkeyAuth2Row(id: Long, userId: Long = 0L, appkey: String, level: Int = 0, name: String = "", updateTime: java.sql.Timestamp)
  /** GetResult implicit for fetching AppkeyAuth2Row objects using plain SQL queries */
  implicit def GetResultAppkeyAuth2Row(implicit e0: GR[Long], e1: GR[String], e2: GR[Int], e3: GR[java.sql.Timestamp]): GR[AppkeyAuth2Row] = GR{
    prs => import prs._
      AppkeyAuth2Row.tupled((<<[Long], <<[Long], <<[String], <<[Int], <<[String], <<[java.sql.Timestamp]))
  }
  /** Table description of table appkey_auth2. Objects of this class serve as prototypes for rows in queries. */
  class AppkeyAuth2(_tableTag: Tag) extends Table[AppkeyAuth2Row](_tableTag, "appkey_auth2") {
    def * = (id, userId, appkey, level, name, updateTime) <> (AppkeyAuth2Row.tupled, AppkeyAuth2Row.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (id.?, userId.?, appkey.?, level.?, name.?, updateTime.?).shaped.<>({r=>import r._; _1.map(_=> AppkeyAuth2Row.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id DBType(BIGINT), AutoInc, PrimaryKey */
    val id: Column[Long] = column[Long]("id", O.AutoInc, O.PrimaryKey)
    /** Database column user_id DBType(BIGINT), Default(0) */
    val userId: Column[Long] = column[Long]("user_id", O.Default(0L))
    /** Database column appkey DBType(VARCHAR), Length(128,true) */
    val appkey: Column[String] = column[String]("appkey", O.Length(128,varying=true))
    /** Database column level DBType(INT), Default(0) */
    val level: Column[Int] = column[Int]("level", O.Default(0))
    /** Database column name DBType(VARCHAR), Length(128,true), Default() */
    val name: Column[String] = column[String]("name", O.Length(128,varying=true), O.Default(""))
    /** Database column update_time DBType(TIMESTAMP) */
    val updateTime: Column[java.sql.Timestamp] = column[java.sql.Timestamp]("update_time")
  }
  /** Collection-like TableQuery object for table AppkeyAuth2 */
  lazy val AppkeyAuth2 = new TableQuery(tag => new AppkeyAuth2(tag))

  /** Entity class storing rows of table AppkeyDashboard
    *  @param id Database column id DBType(BIGINT), AutoInc, PrimaryKey
    *  @param owt Database column owt DBType(VARCHAR), Length(20,true), Default()
    *  @param appkey Database column appkey DBType(VARCHAR), Length(128,true), Default()
    *  @param count Database column count DBType(BIGINT), Default(0)
    *  @param qps Database column qps DBType(DOUBLE), Default(0.0)
    *  @param tp90 Database column tp90 DBType(INT), Default(0)
    *  @param errorCount Database column error_count DBType(INT), Default(0)
    *  @param highQps Database column high_qps DBType(DOUBLE), Default(0.0)
    *  @param higthHost Database column higth_host DBType(VARCHAR), Length(128,true), Default()
    *  @param lowQps Database column low_qps DBType(DOUBLE), Default(0.0)
    *  @param lowHost Database column low_host DBType(VARCHAR), Length(128,true), Default()
    *  @param qpsDiff Database column qps_diff DBType(DOUBLE), Default(0.0)
    *  @param time Database column time DBType(BIGINT)
    *  @param createTime Database column create_time DBType(BIGINT), Default(0) */
  case class AppkeyDashboardRow(id: Long, owt: String = "", appkey: String = "", count: Long = 0L, qps: Double = 0.0, tp90: Int = 0, errorCount: Int = 0, highQps: Double = 0.0, higthHost: String = "", lowQps: Double = 0.0, lowHost: String = "", qpsDiff: Double = 0.0, time: Long, createTime: Long = 0L)
  /** GetResult implicit for fetching AppkeyDashboardRow objects using plain SQL queries */
  implicit def GetResultAppkeyDashboardRow(implicit e0: GR[Long], e1: GR[String], e2: GR[Double], e3: GR[Int]): GR[AppkeyDashboardRow] = GR{
    prs => import prs._
      AppkeyDashboardRow.tupled((<<[Long], <<[String], <<[String], <<[Long], <<[Double], <<[Int], <<[Int], <<[Double], <<[String], <<[Double], <<[String], <<[Double], <<[Long], <<[Long]))
  }
  /** Table description of table appkey_dashboard. Objects of this class serve as prototypes for rows in queries. */
  class AppkeyDashboard(_tableTag: Tag) extends Table[AppkeyDashboardRow](_tableTag, "appkey_dashboard") {
    def * = (id, owt, appkey, count, qps, tp90, errorCount, highQps, higthHost, lowQps, lowHost, qpsDiff, time, createTime) <> (AppkeyDashboardRow.tupled, AppkeyDashboardRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (id.?, owt.?, appkey.?, count.?, qps.?, tp90.?, errorCount.?, highQps.?, higthHost.?, lowQps.?, lowHost.?, qpsDiff.?, time.?, createTime.?).shaped.<>({r=>import r._; _1.map(_=> AppkeyDashboardRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6.get, _7.get, _8.get, _9.get, _10.get, _11.get, _12.get, _13.get, _14.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id DBType(BIGINT), AutoInc, PrimaryKey */
    val id: Column[Long] = column[Long]("id", O.AutoInc, O.PrimaryKey)
    /** Database column owt DBType(VARCHAR), Length(20,true), Default() */
    val owt: Column[String] = column[String]("owt", O.Length(20,varying=true), O.Default(""))
    /** Database column appkey DBType(VARCHAR), Length(128,true), Default() */
    val appkey: Column[String] = column[String]("appkey", O.Length(128,varying=true), O.Default(""))
    /** Database column count DBType(BIGINT), Default(0) */
    val count: Column[Long] = column[Long]("count", O.Default(0L))
    /** Database column qps DBType(DOUBLE), Default(0.0) */
    val qps: Column[Double] = column[Double]("qps", O.Default(0.0))
    /** Database column tp90 DBType(INT), Default(0) */
    val tp90: Column[Int] = column[Int]("tp90", O.Default(0))
    /** Database column error_count DBType(INT), Default(0) */
    val errorCount: Column[Int] = column[Int]("error_count", O.Default(0))
    /** Database column high_qps DBType(DOUBLE), Default(0.0) */
    val highQps: Column[Double] = column[Double]("high_qps", O.Default(0.0))
    /** Database column higth_host DBType(VARCHAR), Length(128,true), Default() */
    val higthHost: Column[String] = column[String]("higth_host", O.Length(128,varying=true), O.Default(""))
    /** Database column low_qps DBType(DOUBLE), Default(0.0) */
    val lowQps: Column[Double] = column[Double]("low_qps", O.Default(0.0))
    /** Database column low_host DBType(VARCHAR), Length(128,true), Default() */
    val lowHost: Column[String] = column[String]("low_host", O.Length(128,varying=true), O.Default(""))
    /** Database column qps_diff DBType(DOUBLE), Default(0.0) */
    val qpsDiff: Column[Double] = column[Double]("qps_diff", O.Default(0.0))
    /** Database column time DBType(BIGINT) */
    val time: Column[Long] = column[Long]("time")
    /** Database column create_time DBType(BIGINT), Default(0) */
    val createTime: Column[Long] = column[Long]("create_time", O.Default(0L))

    /** Uniqueness Index over (owt,appkey,time) (database name index_owt_appkey_time) */
    val index1 = index("index_owt_appkey_time", (owt, appkey, time), unique=true)
    /** Index over (time) (database name index_time) */
    val index2 = index("index_time", time)
  }
  /** Collection-like TableQuery object for table AppkeyDashboard */
  lazy val AppkeyDashboard = new TableQuery(tag => new AppkeyDashboard(tag))

  /** Entity class storing rows of table AppkeyDesc
    *  @param id Database column id DBType(BIGINT), AutoInc, PrimaryKey
    *  @param name Database column name DBType(VARCHAR), Length(128,true), Default()
    *  @param base Database column base DBType(INT)
    *  @param appkey Database column appkey DBType(VARCHAR), Length(128,true), Default()
    *  @param baseapp Database column baseApp DBType(VARCHAR), Length(128,true), Default()
    *  @param owners Database column owners DBType(TEXT), Length(65535,true)
    *  @param observers Database column observers DBType(TEXT), Length(65535,true)
    *  @param pdl Database column pdl DBType(VARCHAR), Length(32,true), Default(0)
    *  @param owt Database column owt DBType(VARCHAR), Length(32,true), Default(0)
    *  @param intro Database column intro DBType(TEXT), Length(65535,true)
    *  @param tags Database column tags DBType(VARCHAR), Length(256,true), Default(0)
    *  @param business Database column business DBType(INT), Default(100)
    *  @param category Database column category DBType(VARCHAR), Length(128,true)
    *  @param reglimit Database column regLimit DBType(INT), Default(0)
    *  @param createTime Database column create_time DBType(BIGINT), Default(0) */
  case class AppkeyDescRow(id: Long, name: String = "", base: Int, appkey: String = "", baseapp: String = "", owners: String, observers: String, pdl: String = "0", owt: String = "0", intro: String, tags: String = "0", business: Int = 100, category: String, reglimit: Int = 0, createTime: Long = 0L)
  /** GetResult implicit for fetching AppkeyDescRow objects using plain SQL queries */
  implicit def GetResultAppkeyDescRow(implicit e0: GR[Long], e1: GR[String], e2: GR[Int]): GR[AppkeyDescRow] = GR{
    prs => import prs._
      AppkeyDescRow.tupled((<<[Long], <<[String], <<[Int], <<[String], <<[String], <<[String], <<[String], <<[String], <<[String], <<[String], <<[String], <<[Int], <<[String], <<[Int], <<[Long]))
  }
  /** Table description of table appkey_desc. Objects of this class serve as prototypes for rows in queries. */
  class AppkeyDesc(_tableTag: Tag) extends Table[AppkeyDescRow](_tableTag, "appkey_desc") {
    def * = (id, name, base, appkey, baseapp, owners, observers, pdl, owt, intro, tags, business, category, reglimit, createTime) <> (AppkeyDescRow.tupled, AppkeyDescRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (id.?, name.?, base.?, appkey.?, baseapp.?, owners.?, observers.?, pdl.?, owt.?, intro.?, tags.?, business.?, category.?, reglimit.?, createTime.?).shaped.<>({r=>import r._; _1.map(_=> AppkeyDescRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6.get, _7.get, _8.get, _9.get, _10.get, _11.get, _12.get, _13.get, _14.get, _15.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id DBType(BIGINT), AutoInc, PrimaryKey */
    val id: Column[Long] = column[Long]("id", O.AutoInc, O.PrimaryKey)
    /** Database column name DBType(VARCHAR), Length(128,true), Default() */
    val name: Column[String] = column[String]("name", O.Length(128,varying=true), O.Default(""))
    /** Database column base DBType(INT) */
    val base: Column[Int] = column[Int]("base")
    /** Database column appkey DBType(VARCHAR), Length(128,true), Default() */
    val appkey: Column[String] = column[String]("appkey", O.Length(128,varying=true), O.Default(""))
    /** Database column baseApp DBType(VARCHAR), Length(128,true), Default() */
    val baseapp: Column[String] = column[String]("baseApp", O.Length(128,varying=true), O.Default(""))
    /** Database column owners DBType(TEXT), Length(65535,true) */
    val owners: Column[String] = column[String]("owners", O.Length(65535,varying=true))
    /** Database column observers DBType(TEXT), Length(65535,true) */
    val observers: Column[String] = column[String]("observers", O.Length(65535,varying=true))
    /** Database column pdl DBType(VARCHAR), Length(32,true), Default(0) */
    val pdl: Column[String] = column[String]("pdl", O.Length(32,varying=true), O.Default("0"))
    /** Database column owt DBType(VARCHAR), Length(32,true), Default(0) */
    val owt: Column[String] = column[String]("owt", O.Length(32,varying=true), O.Default("0"))
    /** Database column intro DBType(TEXT), Length(65535,true) */
    val intro: Column[String] = column[String]("intro", O.Length(65535,varying=true))
    /** Database column tags DBType(VARCHAR), Length(256,true), Default(0) */
    val tags: Column[String] = column[String]("tags", O.Length(256,varying=true), O.Default("0"))
    /** Database column business DBType(INT), Default(100) */
    val business: Column[Int] = column[Int]("business", O.Default(100))
    /** Database column category DBType(VARCHAR), Length(128,true) */
    val category: Column[String] = column[String]("category", O.Length(128,varying=true))
    /** Database column regLimit DBType(INT), Default(0) */
    val reglimit: Column[Int] = column[Int]("regLimit", O.Default(0))
    /** Database column create_time DBType(BIGINT), Default(0) */
    val createTime: Column[Long] = column[Long]("create_time", O.Default(0L))

    /** Index over (appkey) (database name idx_appkey) */
    val index1 = index("idx_appkey", appkey)
    /** Index over (owt) (database name idx_owt) */
    val index2 = index("idx_owt", owt)
    /** Index over (pdl) (database name idx_pdl) */
    val index3 = index("idx_pdl", pdl)
    /** Index over (tags) (database name idx_tags) */
    val index4 = index("idx_tags", tags)
  }
  /** Collection-like TableQuery object for table AppkeyDesc */
  lazy val AppkeyDesc = new TableQuery(tag => new AppkeyDesc(tag))

  /** Entity class storing rows of table AppkeyFavorite
    *  @param id Database column id DBType(INT UNSIGNED), AutoInc, PrimaryKey
    *  @param username Database column username DBType(VARCHAR), Length(128,true), Default()
    *  @param appkey Database column appkey DBType(VARCHAR), Length(128,true), Default() */
  case class AppkeyFavoriteRow(id: Int, username: String = "", appkey: String = "")
  /** GetResult implicit for fetching AppkeyFavoriteRow objects using plain SQL queries */
  implicit def GetResultAppkeyFavoriteRow(implicit e0: GR[Int], e1: GR[String]): GR[AppkeyFavoriteRow] = GR{
    prs => import prs._
      AppkeyFavoriteRow.tupled((<<[Int], <<[String], <<[String]))
  }
  /** Table description of table appkey_favorite. Objects of this class serve as prototypes for rows in queries. */
  class AppkeyFavorite(_tableTag: Tag) extends Table[AppkeyFavoriteRow](_tableTag, "appkey_favorite") {
    def * = (id, username, appkey) <> (AppkeyFavoriteRow.tupled, AppkeyFavoriteRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (id.?, username.?, appkey.?).shaped.<>({r=>import r._; _1.map(_=> AppkeyFavoriteRow.tupled((_1.get, _2.get, _3.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id DBType(INT UNSIGNED), AutoInc, PrimaryKey */
    val id: Column[Int] = column[Int]("id", O.AutoInc, O.PrimaryKey)
    /** Database column username DBType(VARCHAR), Length(128,true), Default() */
    val username: Column[String] = column[String]("username", O.Length(128,varying=true), O.Default(""))
    /** Database column appkey DBType(VARCHAR), Length(128,true), Default() */
    val appkey: Column[String] = column[String]("appkey", O.Length(128,varying=true), O.Default(""))

    /** Uniqueness Index over (username,appkey) (database name user_appkey_uni_idx) */
    val index1 = index("user_appkey_uni_idx", (username, appkey), unique=true)
    /** Index over (username) (database name user_idx) */
    val index2 = index("user_idx", username)
  }
  /** Collection-like TableQuery object for table AppkeyFavorite */
  lazy val AppkeyFavorite = new TableQuery(tag => new AppkeyFavorite(tag))

  /** Entity class storing rows of table AppkeyProvider
    *  @param id Database column id DBType(BIGINT), AutoInc, PrimaryKey
    *  @param appkey Database column appkey DBType(VARCHAR), Length(128,true), Default()
    *  @param version Database column version DBType(VARCHAR), Length(64,true), Default()
    *  @param hostname Database column hostname DBType(VARCHAR), Length(64,true), Default()
    *  @param ip Database column ip DBType(VARCHAR), Length(15,true), Default()
    *  @param port Database column port DBType(INT), Default(0)
    *  @param `type` Database column type DBType(VARCHAR), Length(12,true), Default(thrift)
    *  @param weight Database column weight DBType(INT), Default(10)
    *  @param fweight Database column fweight DBType(DOUBLE), Default(10.0)
    *  @param status Database column status DBType(INT), Default(0)
    *  @param enabled Database column enabled DBType(INT), Default(0)
    *  @param role Database column role DBType(INT), Default(0)
    *  @param env Database column env DBType(INT), Default(0)
    *  @param lastupdatetime Database column lastUpdateTime DBType(BIGINT), Default(0)
    *  @param extend Database column extend DBType(VARCHAR), Length(255,true), Default()
    *  @param servertype Database column serverType DBType(INT), Default(0)
    *  @param protocol Database column protocol DBType(VARCHAR), Length(10,true), Default()
    *  @param swimlane Database column swimlane DBType(VARCHAR), Length(20,true), Default()
    *  @param serviceinfo Database column serviceInfo DBType(VARCHAR), Length(1024,true), Default()
    *  @param heartbeatsupport Database column heartbeatSupport DBType(INT), Default(0)
    *  @param idc Database column idc DBType(VARCHAR), Length(24,true), Default(OTHER) */
  case class AppkeyProviderRow(id: Long, appkey: String = "", version: String = "", hostname: String = "", ip: String = "", port: Int = 0, `type`: String = "thrift", weight: Int = 10, fweight: Double = 10.0, status: Int = 0, enabled: Int = 0, role: Int = 0, env: Int = 0, lastupdatetime: Long = 0L, extend: String = "", servertype: Int = 0, protocol: String = "", swimlane: String = "", serviceinfo: String = "", heartbeatsupport: Int = 0, idc: String = "OTHER")
  /** GetResult implicit for fetching AppkeyProviderRow objects using plain SQL queries */
  implicit def GetResultAppkeyProviderRow(implicit e0: GR[Long], e1: GR[String], e2: GR[Int], e3: GR[Double]): GR[AppkeyProviderRow] = GR{
    prs => import prs._
      AppkeyProviderRow.tupled((<<[Long], <<[String], <<[String], <<[String], <<[String], <<[Int], <<[String], <<[Int], <<[Double], <<[Int], <<[Int], <<[Int], <<[Int], <<[Long], <<[String], <<[Int], <<[String], <<[String], <<[String], <<[Int], <<[String]))
  }
  /** Table description of table appkey_provider. Objects of this class serve as prototypes for rows in queries.
    *  NOTE: The following names collided with Scala keywords and were escaped: type */
  class AppkeyProvider(_tableTag: Tag) extends Table[AppkeyProviderRow](_tableTag, "appkey_provider") {
    def * = (id, appkey, version, hostname, ip, port, `type`, weight, fweight, status, enabled, role, env, lastupdatetime, extend, servertype, protocol, swimlane, serviceinfo, heartbeatsupport, idc) <> (AppkeyProviderRow.tupled, AppkeyProviderRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (id.?, appkey.?, version.?, hostname.?, ip.?, port.?, `type`.?, weight.?, fweight.?, status.?, enabled.?, role.?, env.?, lastupdatetime.?, extend.?, servertype.?, protocol.?, swimlane.?, serviceinfo.?, heartbeatsupport.?, idc.?).shaped.<>({r=>import r._; _1.map(_=> AppkeyProviderRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6.get, _7.get, _8.get, _9.get, _10.get, _11.get, _12.get, _13.get, _14.get, _15.get, _16.get, _17.get, _18.get, _19.get, _20.get, _21.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id DBType(BIGINT), AutoInc, PrimaryKey */
    val id: Column[Long] = column[Long]("id", O.AutoInc, O.PrimaryKey)
    /** Database column appkey DBType(VARCHAR), Length(128,true), Default() */
    val appkey: Column[String] = column[String]("appkey", O.Length(128,varying=true), O.Default(""))
    /** Database column version DBType(VARCHAR), Length(64,true), Default() */
    val version: Column[String] = column[String]("version", O.Length(64,varying=true), O.Default(""))
    /** Database column hostname DBType(VARCHAR), Length(64,true), Default() */
    val hostname: Column[String] = column[String]("hostname", O.Length(64,varying=true), O.Default(""))
    /** Database column ip DBType(VARCHAR), Length(15,true), Default() */
    val ip: Column[String] = column[String]("ip", O.Length(15,varying=true), O.Default(""))
    /** Database column port DBType(INT), Default(0) */
    val port: Column[Int] = column[Int]("port", O.Default(0))
    /** Database column type DBType(VARCHAR), Length(12,true), Default(thrift)
      *  NOTE: The name was escaped because it collided with a Scala keyword. */
    val `type`: Column[String] = column[String]("type", O.Length(12,varying=true), O.Default("thrift"))
    /** Database column weight DBType(INT), Default(10) */
    val weight: Column[Int] = column[Int]("weight", O.Default(10))
    /** Database column fweight DBType(DOUBLE), Default(10.0) */
    val fweight: Column[Double] = column[Double]("fweight", O.Default(10.0))
    /** Database column status DBType(INT), Default(0) */
    val status: Column[Int] = column[Int]("status", O.Default(0))
    /** Database column enabled DBType(INT), Default(0) */
    val enabled: Column[Int] = column[Int]("enabled", O.Default(0))
    /** Database column role DBType(INT), Default(0) */
    val role: Column[Int] = column[Int]("role", O.Default(0))
    /** Database column env DBType(INT), Default(0) */
    val env: Column[Int] = column[Int]("env", O.Default(0))
    /** Database column lastUpdateTime DBType(BIGINT), Default(0) */
    val lastupdatetime: Column[Long] = column[Long]("lastUpdateTime", O.Default(0L))
    /** Database column extend DBType(VARCHAR), Length(255,true), Default() */
    val extend: Column[String] = column[String]("extend", O.Length(255,varying=true), O.Default(""))
    /** Database column serverType DBType(INT), Default(0) */
    val servertype: Column[Int] = column[Int]("serverType", O.Default(0))
    /** Database column protocol DBType(VARCHAR), Length(10,true), Default() */
    val protocol: Column[String] = column[String]("protocol", O.Length(10,varying=true), O.Default(""))
    /** Database column swimlane DBType(VARCHAR), Length(20,true), Default() */
    val swimlane: Column[String] = column[String]("swimlane", O.Length(20,varying=true), O.Default(""))
    /** Database column serviceInfo DBType(VARCHAR), Length(1024,true), Default() */
    val serviceinfo: Column[String] = column[String]("serviceInfo", O.Length(1024,varying=true), O.Default(""))
    /** Database column heartbeatSupport DBType(INT), Default(0) */
    val heartbeatsupport: Column[Int] = column[Int]("heartbeatSupport", O.Default(0))
    /** Database column idc DBType(VARCHAR), Length(24,true), Default(OTHER) */
    val idc: Column[String] = column[String]("idc", O.Length(24,varying=true), O.Default("OTHER"))

    /** Index over (appkey,`type`,env,status) (database name idx_appkey_env) */
    val index1 = index("idx_appkey_env", (appkey, `type`, env, status))
    /** Index over (appkey,`type`,env,ip,port) (database name idx_appkey_ip) */
    val index2 = index("idx_appkey_ip", (appkey, `type`, env, ip, port))
    /** Index over (appkey,`type`,env,lastupdatetime) (database name idx_appkey_time) */
    val index3 = index("idx_appkey_time", (appkey, `type`, env, lastupdatetime))
  }
  /** Collection-like TableQuery object for table AppkeyProvider */
  lazy val AppkeyProvider = new TableQuery(tag => new AppkeyProvider(tag))

  /** Entity class storing rows of table AppkeyProviderTrigger
    *  @param id Database column id DBType(BIGINT), AutoInc, PrimaryKey
    *  @param appkey Database column appkey DBType(VARCHAR), Length(128,true), Default()
    *  @param item Database column item DBType(VARCHAR), Length(255,true), Default()
    *  @param itemDesc Database column item_desc DBType(VARCHAR), Length(255,true), Default()
    *  @param function Database column function DBType(VARCHAR), Length(10,true), Default()
    *  @param functionDesc Database column function_desc DBType(VARCHAR), Length(255,true), Default()
    *  @param threshold Database column threshold DBType(INT), Default(0)
    *  @param createTime Database column create_time DBType(BIGINT), Default(0) */
  case class AppkeyProviderTriggerRow(id: Long, appkey: String = "", item: String = "", itemDesc: String = "", function: String = "", functionDesc: String = "", threshold: Int = 0, createTime: Long = 0L)
  /** GetResult implicit for fetching AppkeyProviderTriggerRow objects using plain SQL queries */
  implicit def GetResultAppkeyProviderTriggerRow(implicit e0: GR[Long], e1: GR[String], e2: GR[Int]): GR[AppkeyProviderTriggerRow] = GR{
    prs => import prs._
      AppkeyProviderTriggerRow.tupled((<<[Long], <<[String], <<[String], <<[String], <<[String], <<[String], <<[Int], <<[Long]))
  }
  /** Table description of table appkey_provider_trigger. Objects of this class serve as prototypes for rows in queries. */
  class AppkeyProviderTrigger(_tableTag: Tag) extends Table[AppkeyProviderTriggerRow](_tableTag, "appkey_provider_trigger") {
    def * = (id, appkey, item, itemDesc, function, functionDesc, threshold, createTime) <> (AppkeyProviderTriggerRow.tupled, AppkeyProviderTriggerRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (id.?, appkey.?, item.?, itemDesc.?, function.?, functionDesc.?, threshold.?, createTime.?).shaped.<>({r=>import r._; _1.map(_=> AppkeyProviderTriggerRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6.get, _7.get, _8.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id DBType(BIGINT), AutoInc, PrimaryKey */
    val id: Column[Long] = column[Long]("id", O.AutoInc, O.PrimaryKey)
    /** Database column appkey DBType(VARCHAR), Length(128,true), Default() */
    val appkey: Column[String] = column[String]("appkey", O.Length(128,varying=true), O.Default(""))
    /** Database column item DBType(VARCHAR), Length(255,true), Default() */
    val item: Column[String] = column[String]("item", O.Length(255,varying=true), O.Default(""))
    /** Database column item_desc DBType(VARCHAR), Length(255,true), Default() */
    val itemDesc: Column[String] = column[String]("item_desc", O.Length(255,varying=true), O.Default(""))
    /** Database column function DBType(VARCHAR), Length(10,true), Default() */
    val function: Column[String] = column[String]("function", O.Length(10,varying=true), O.Default(""))
    /** Database column function_desc DBType(VARCHAR), Length(255,true), Default() */
    val functionDesc: Column[String] = column[String]("function_desc", O.Length(255,varying=true), O.Default(""))
    /** Database column threshold DBType(INT), Default(0) */
    val threshold: Column[Int] = column[Int]("threshold", O.Default(0))
    /** Database column create_time DBType(BIGINT), Default(0) */
    val createTime: Column[Long] = column[Long]("create_time", O.Default(0L))

    /** Index over (appkey,item) (database name idx_appkey_item) */
    val index1 = index("idx_appkey_item", (appkey, item))
    /** Index over (createTime) (database name idx_time) */
    val index2 = index("idx_time", createTime)
  }
  /** Collection-like TableQuery object for table AppkeyProviderTrigger */
  lazy val AppkeyProviderTrigger = new TableQuery(tag => new AppkeyProviderTrigger(tag))

  /** Entity class storing rows of table AppkeySubscribe
    *  @param id Database column id DBType(INT UNSIGNED), AutoInc, PrimaryKey
    *  @param username Database column username DBType(VARCHAR), Length(128,true), Default()
    *  @param appkey Database column appkey DBType(VARCHAR), Length(128,true), Default()
    *  @param dailyReport Database column daily_report DBType(TINYINT UNSIGNED)
    *  @param weeklyReport Database column weekly_report DBType(TINYINT UNSIGNED)
    *  @param createTime Database column create_time DBType(BIGINT)
    *  @param updateTime Database column update_time DBType(BIGINT) */
  case class AppkeySubscribeRow(id: Int, username: String = "", appkey: String = "", dailyReport: Byte, weeklyReport: Byte, createTime: Long, updateTime: Long)
  /** GetResult implicit for fetching AppkeySubscribeRow objects using plain SQL queries */
  implicit def GetResultAppkeySubscribeRow(implicit e0: GR[Int], e1: GR[String], e2: GR[Byte], e3: GR[Long]): GR[AppkeySubscribeRow] = GR{
    prs => import prs._
      AppkeySubscribeRow.tupled((<<[Int], <<[String], <<[String], <<[Byte], <<[Byte], <<[Long], <<[Long]))
  }
  /** Table description of table appkey_subscribe. Objects of this class serve as prototypes for rows in queries. */
  class AppkeySubscribe(_tableTag: Tag) extends Table[AppkeySubscribeRow](_tableTag, "appkey_subscribe") {
    def * = (id, username, appkey, dailyReport, weeklyReport, createTime, updateTime) <> (AppkeySubscribeRow.tupled, AppkeySubscribeRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (id.?, username.?, appkey.?, dailyReport.?, weeklyReport.?, createTime.?, updateTime.?).shaped.<>({r=>import r._; _1.map(_=> AppkeySubscribeRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6.get, _7.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id DBType(INT UNSIGNED), AutoInc, PrimaryKey */
    val id: Column[Int] = column[Int]("id", O.AutoInc, O.PrimaryKey)
    /** Database column username DBType(VARCHAR), Length(128,true), Default() */
    val username: Column[String] = column[String]("username", O.Length(128,varying=true), O.Default(""))
    /** Database column appkey DBType(VARCHAR), Length(128,true), Default() */
    val appkey: Column[String] = column[String]("appkey", O.Length(128,varying=true), O.Default(""))
    /** Database column daily_report DBType(TINYINT UNSIGNED) */
    val dailyReport: Column[Byte] = column[Byte]("daily_report")
    /** Database column weekly_report DBType(TINYINT UNSIGNED) */
    val weeklyReport: Column[Byte] = column[Byte]("weekly_report")
    /** Database column create_time DBType(BIGINT) */
    val createTime: Column[Long] = column[Long]("create_time")
    /** Database column update_time DBType(BIGINT) */
    val updateTime: Column[Long] = column[Long]("update_time")

    /** Index over (username) (database name idx_username) */
    val index1 = index("idx_username", username)
    /** Index over (username,appkey) (database name ux_username_appkey) */
    val index2 = index("ux_username_appkey", (username, appkey))
  }
  /** Collection-like TableQuery object for table AppkeySubscribe */
  lazy val AppkeySubscribe = new TableQuery(tag => new AppkeySubscribe(tag))

  /** Entity class storing rows of table AppkeyTrigger
    *  @param id Database column id DBType(BIGINT), AutoInc, PrimaryKey
    *  @param appkey Database column appkey DBType(VARCHAR), Length(128,true), Default()
    *  @param side Database column side DBType(VARCHAR), Length(10,true), Default()
    *  @param spanname Database column spanname DBType(VARCHAR), Length(128,true), Default()
    *  @param item Database column item DBType(VARCHAR), Length(255,true), Default()
    *  @param itemDesc Database column item_desc DBType(VARCHAR), Length(255,true), Default()
    *  @param function Database column function DBType(VARCHAR), Length(10,true), Default()
    *  @param functionDesc Database column function_desc DBType(VARCHAR), Length(255,true), Default()
    *  @param threshold Database column threshold DBType(INT), Default(0)
    *  @param duration Database column duration DBType(INT), Default(1)
    *  @param createTime Database column create_time DBType(BIGINT), Default(0) */
  case class AppkeyTriggerRow(id: Long, appkey: String = "", side: String = "", spanname: String = "", item: String = "", itemDesc: String = "", function: String = "", functionDesc: String = "", threshold: Int = 0, duration: Int = 1, createTime: Long = 0L)
  /** GetResult implicit for fetching AppkeyTriggerRow objects using plain SQL queries */
  implicit def GetResultAppkeyTriggerRow(implicit e0: GR[Long], e1: GR[String], e2: GR[Int]): GR[AppkeyTriggerRow] = GR{
    prs => import prs._
      AppkeyTriggerRow.tupled((<<[Long], <<[String], <<[String], <<[String], <<[String], <<[String], <<[String], <<[String], <<[Int], <<[Int], <<[Long]))
  }
  /** Table description of table appkey_trigger. Objects of this class serve as prototypes for rows in queries. */
  class AppkeyTrigger(_tableTag: Tag) extends Table[AppkeyTriggerRow](_tableTag, "appkey_trigger") {
    def * = (id, appkey, side, spanname, item, itemDesc, function, functionDesc, threshold, duration, createTime) <> (AppkeyTriggerRow.tupled, AppkeyTriggerRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (id.?, appkey.?, side.?, spanname.?, item.?, itemDesc.?, function.?, functionDesc.?, threshold.?, duration.?, createTime.?).shaped.<>({r=>import r._; _1.map(_=> AppkeyTriggerRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6.get, _7.get, _8.get, _9.get, _10.get, _11.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id DBType(BIGINT), AutoInc, PrimaryKey */
    val id: Column[Long] = column[Long]("id", O.AutoInc, O.PrimaryKey)
    /** Database column appkey DBType(VARCHAR), Length(128,true), Default() */
    val appkey: Column[String] = column[String]("appkey", O.Length(128,varying=true), O.Default(""))
    /** Database column side DBType(VARCHAR), Length(10,true), Default() */
    val side: Column[String] = column[String]("side", O.Length(10,varying=true), O.Default(""))
    /** Database column spanname DBType(VARCHAR), Length(128,true), Default() */
    val spanname: Column[String] = column[String]("spanname", O.Length(128,varying=true), O.Default(""))
    /** Database column item DBType(VARCHAR), Length(255,true), Default() */
    val item: Column[String] = column[String]("item", O.Length(255,varying=true), O.Default(""))
    /** Database column item_desc DBType(VARCHAR), Length(255,true), Default() */
    val itemDesc: Column[String] = column[String]("item_desc", O.Length(255,varying=true), O.Default(""))
    /** Database column function DBType(VARCHAR), Length(10,true), Default() */
    val function: Column[String] = column[String]("function", O.Length(10,varying=true), O.Default(""))
    /** Database column function_desc DBType(VARCHAR), Length(255,true), Default() */
    val functionDesc: Column[String] = column[String]("function_desc", O.Length(255,varying=true), O.Default(""))
    /** Database column threshold DBType(INT), Default(0) */
    val threshold: Column[Int] = column[Int]("threshold", O.Default(0))
    /** Database column duration DBType(INT), Default(1) */
    val duration: Column[Int] = column[Int]("duration", O.Default(1))
    /** Database column create_time DBType(BIGINT), Default(0) */
    val createTime: Column[Long] = column[Long]("create_time", O.Default(0L))

    /** Index over (createTime) (database name idx_time) */
    val index1 = index("idx_time", createTime)
  }
  /** Collection-like TableQuery object for table AppkeyTrigger */
  lazy val AppkeyTrigger = new TableQuery(tag => new AppkeyTrigger(tag))

  /** Entity class storing rows of table AppkeyTriggerCount
    *  @param id Database column id DBType(BIGINT), AutoInc, PrimaryKey
    *  @param appkeyTriggerId Database column appkey_trigger_id DBType(BIGINT), Default(0)
    *  @param count Database column count DBType(INT), Default(0) */
  case class AppkeyTriggerCountRow(id: Long, appkeyTriggerId: Long = 0L, count: Int = 0)
  /** GetResult implicit for fetching AppkeyTriggerCountRow objects using plain SQL queries */
  implicit def GetResultAppkeyTriggerCountRow(implicit e0: GR[Long], e1: GR[Int]): GR[AppkeyTriggerCountRow] = GR{
    prs => import prs._
      AppkeyTriggerCountRow.tupled((<<[Long], <<[Long], <<[Int]))
  }
  /** Table description of table appkey_trigger_count. Objects of this class serve as prototypes for rows in queries. */
  class AppkeyTriggerCount(_tableTag: Tag) extends Table[AppkeyTriggerCountRow](_tableTag, "appkey_trigger_count") {
    def * = (id, appkeyTriggerId, count) <> (AppkeyTriggerCountRow.tupled, AppkeyTriggerCountRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (id.?, appkeyTriggerId.?, count.?).shaped.<>({r=>import r._; _1.map(_=> AppkeyTriggerCountRow.tupled((_1.get, _2.get, _3.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id DBType(BIGINT), AutoInc, PrimaryKey */
    val id: Column[Long] = column[Long]("id", O.AutoInc, O.PrimaryKey)
    /** Database column appkey_trigger_id DBType(BIGINT), Default(0) */
    val appkeyTriggerId: Column[Long] = column[Long]("appkey_trigger_id", O.Default(0L))
    /** Database column count DBType(INT), Default(0) */
    val count: Column[Int] = column[Int]("count", O.Default(0))

    /** Uniqueness Index over (appkeyTriggerId) (database name appkey_trigger_id) */
    val index1 = index("appkey_trigger_id", appkeyTriggerId, unique=true)
  }
  /** Collection-like TableQuery object for table AppkeyTriggerCount */
  lazy val AppkeyTriggerCount = new TableQuery(tag => new AppkeyTriggerCount(tag))

  /** Entity class storing rows of table AppkeyTriggerStatus
    *  @param id Database column id DBType(BIGINT UNSIGNED), AutoInc, PrimaryKey
    *  @param triggerId Database column trigger_id DBType(BIGINT)
    *  @param triggerStatus Database column trigger_status DBType(INT)
    *  @param createTime Database column create_time DBType(BIGINT)
    *  @param updateTime Database column update_time DBType(BIGINT) */
  case class AppkeyTriggerStatusRow(id: Long, triggerId: Long, triggerStatus: Int, createTime: Long, updateTime: Long)
  /** GetResult implicit for fetching AppkeyTriggerStatusRow objects using plain SQL queries */
  implicit def GetResultAppkeyTriggerStatusRow(implicit e0: GR[Long], e1: GR[Int]): GR[AppkeyTriggerStatusRow] = GR{
    prs => import prs._
      AppkeyTriggerStatusRow.tupled((<<[Long], <<[Long], <<[Int], <<[Long], <<[Long]))
  }
  /** Table description of table appkey_trigger_status. Objects of this class serve as prototypes for rows in queries. */
  class AppkeyTriggerStatus(_tableTag: Tag) extends Table[AppkeyTriggerStatusRow](_tableTag, "appkey_trigger_status") {
    def * = (id, triggerId, triggerStatus, createTime, updateTime) <> (AppkeyTriggerStatusRow.tupled, AppkeyTriggerStatusRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (id.?, triggerId.?, triggerStatus.?, createTime.?, updateTime.?).shaped.<>({r=>import r._; _1.map(_=> AppkeyTriggerStatusRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id DBType(BIGINT UNSIGNED), AutoInc, PrimaryKey */
    val id: Column[Long] = column[Long]("id", O.AutoInc, O.PrimaryKey)
    /** Database column trigger_id DBType(BIGINT) */
    val triggerId: Column[Long] = column[Long]("trigger_id")
    /** Database column trigger_status DBType(INT) */
    val triggerStatus: Column[Int] = column[Int]("trigger_status")
    /** Database column create_time DBType(BIGINT) */
    val createTime: Column[Long] = column[Long]("create_time")
    /** Database column update_time DBType(BIGINT) */
    val updateTime: Column[Long] = column[Long]("update_time")

    /** Index over (createTime) (database name create_time) */
    val index1 = index("create_time", createTime)
    /** Index over (updateTime) (database name idx_update_time) */
    val index2 = index("idx_update_time", updateTime)
    /** Uniqueness Index over (triggerId,createTime) (database name uni_id_time) */
    val index3 = index("uni_id_time", (triggerId, createTime), unique=true)
  }
  /** Collection-like TableQuery object for table AppkeyTriggerStatus */
  lazy val AppkeyTriggerStatus = new TableQuery(tag => new AppkeyTriggerStatus(tag))

  /** Entity class storing rows of table AppMethodDoc
    *  @param id Database column id DBType(BIGINT), AutoInc, PrimaryKey
    *  @param appkey Database column appkey DBType(VARCHAR), Length(128,true), Default()
    *  @param group Database column group DBType(VARCHAR), Length(128,true), Default(Some())
    *  @param api Database column api DBType(VARCHAR), Length(256,true), Default()
    *  @param name Database column name DBType(VARCHAR), Length(128,true), Default(Some())
    *  @param desc Database column desc DBType(VARCHAR), Length(512,true), Default(Some())
    *  @param params Database column params DBType(MEDIUMTEXT), Length(16777215,true), Default(None)
    *  @param result Database column result DBType(MEDIUMTEXT), Length(16777215,true), Default(None)
    *  @param exceptions Database column exceptions DBType(MEDIUMTEXT), Length(16777215,true), Default(None)
    *  @param permission Database column permission DBType(VARCHAR), Length(64,true), Default(Some())
    *  @param status Database column status DBType(VARCHAR), Length(32,true), Default(Some())
    *  @param version Database column version DBType(VARCHAR), Length(64,true), Default(Some())
    *  @param link Database column link DBType(VARCHAR), Length(256,true), Default(Some())
    *  @param author Database column author DBType(VARCHAR), Length(256,true), Default(Some())
    *  @param gsign Database column gsign DBType(VARCHAR), Length(32,true), Default()
    *  @param sign Database column sign DBType(VARCHAR), Length(32,true), Default()
    *  @param ctime Database column ctime DBType(BIGINT), Default(0) */
  case class AppMethodDocRow(id: Long, appkey: String = "", group: Option[String] = Some(""), api: String = "", name: Option[String] = Some(""), desc: Option[String] = Some(""), params: Option[String] = None, result: Option[String] = None, exceptions: Option[String] = None, permission: Option[String] = Some(""), status: Option[String] = Some(""), version: Option[String] = Some(""), link: Option[String] = Some(""), author: Option[String] = Some(""), gsign: String = "", sign: String = "", ctime: Long = 0L)
  /** GetResult implicit for fetching AppMethodDocRow objects using plain SQL queries */
  implicit def GetResultAppMethodDocRow(implicit e0: GR[Long], e1: GR[String], e2: GR[Option[String]]): GR[AppMethodDocRow] = GR{
    prs => import prs._
      AppMethodDocRow.tupled((<<[Long], <<[String], <<?[String], <<[String], <<?[String], <<?[String], <<?[String], <<?[String], <<?[String], <<?[String], <<?[String], <<?[String], <<?[String], <<?[String], <<[String], <<[String], <<[Long]))
  }
  /** Table description of table app_method_doc. Objects of this class serve as prototypes for rows in queries. */
  class AppMethodDoc(_tableTag: Tag) extends Table[AppMethodDocRow](_tableTag, "app_method_doc") {
    def * = (id, appkey, group, api, name, desc, params, result, exceptions, permission, status, version, link, author, gsign, sign, ctime) <> (AppMethodDocRow.tupled, AppMethodDocRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (id.?, appkey.?, group, api.?, name, desc, params, result, exceptions, permission, status, version, link, author, gsign.?, sign.?, ctime.?).shaped.<>({r=>import r._; _1.map(_=> AppMethodDocRow.tupled((_1.get, _2.get, _3, _4.get, _5, _6, _7, _8, _9, _10, _11, _12, _13, _14, _15.get, _16.get, _17.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id DBType(BIGINT), AutoInc, PrimaryKey */
    val id: Column[Long] = column[Long]("id", O.AutoInc, O.PrimaryKey)
    /** Database column appkey DBType(VARCHAR), Length(128,true), Default() */
    val appkey: Column[String] = column[String]("appkey", O.Length(128,varying=true), O.Default(""))
    /** Database column group DBType(VARCHAR), Length(128,true), Default(Some()) */
    val group: Column[Option[String]] = column[Option[String]]("group", O.Length(128,varying=true), O.Default(Some("")))
    /** Database column api DBType(VARCHAR), Length(256,true), Default() */
    val api: Column[String] = column[String]("api", O.Length(256,varying=true), O.Default(""))
    /** Database column name DBType(VARCHAR), Length(128,true), Default(Some()) */
    val name: Column[Option[String]] = column[Option[String]]("name", O.Length(128,varying=true), O.Default(Some("")))
    /** Database column desc DBType(VARCHAR), Length(512,true), Default(Some()) */
    val desc: Column[Option[String]] = column[Option[String]]("desc", O.Length(512,varying=true), O.Default(Some("")))
    /** Database column params DBType(MEDIUMTEXT), Length(16777215,true), Default(None) */
    val params: Column[Option[String]] = column[Option[String]]("params", O.Length(16777215,varying=true), O.Default(None))
    /** Database column result DBType(MEDIUMTEXT), Length(16777215,true), Default(None) */
    val result: Column[Option[String]] = column[Option[String]]("result", O.Length(16777215,varying=true), O.Default(None))
    /** Database column exceptions DBType(MEDIUMTEXT), Length(16777215,true), Default(None) */
    val exceptions: Column[Option[String]] = column[Option[String]]("exceptions", O.Length(16777215,varying=true), O.Default(None))
    /** Database column permission DBType(VARCHAR), Length(64,true), Default(Some()) */
    val permission: Column[Option[String]] = column[Option[String]]("permission", O.Length(64,varying=true), O.Default(Some("")))
    /** Database column status DBType(VARCHAR), Length(32,true), Default(Some()) */
    val status: Column[Option[String]] = column[Option[String]]("status", O.Length(32,varying=true), O.Default(Some("")))
    /** Database column version DBType(VARCHAR), Length(64,true), Default(Some()) */
    val version: Column[Option[String]] = column[Option[String]]("version", O.Length(64,varying=true), O.Default(Some("")))
    /** Database column link DBType(VARCHAR), Length(256,true), Default(Some()) */
    val link: Column[Option[String]] = column[Option[String]]("link", O.Length(256,varying=true), O.Default(Some("")))
    /** Database column author DBType(VARCHAR), Length(256,true), Default(Some()) */
    val author: Column[Option[String]] = column[Option[String]]("author", O.Length(256,varying=true), O.Default(Some("")))
    /** Database column gsign DBType(VARCHAR), Length(32,true), Default() */
    val gsign: Column[String] = column[String]("gsign", O.Length(32,varying=true), O.Default(""))
    /** Database column sign DBType(VARCHAR), Length(32,true), Default() */
    val sign: Column[String] = column[String]("sign", O.Length(32,varying=true), O.Default(""))
    /** Database column ctime DBType(BIGINT), Default(0) */
    val ctime: Column[Long] = column[Long]("ctime", O.Default(0L))

    /** Index over (ctime) (database name idx_time) */
    val index1 = index("idx_time", ctime)
  }
  /** Collection-like TableQuery object for table AppMethodDoc */
  lazy val AppMethodDoc = new TableQuery(tag => new AppMethodDoc(tag))

  /** Entity class storing rows of table AppQuota
    *  @param id Database column id DBType(BIGINT), AutoInc, PrimaryKey
    *  @param name Database column name DBType(VARCHAR), Length(128,true), Default()
    *  @param appkey Database column appkey DBType(VARCHAR), Length(128,true), Default()
    *  @param env Database column env DBType(INT), Default(0)
    *  @param method Database column method DBType(VARCHAR), Length(255,true), Default()
    *  @param qpsCapacity Database column qps_capacity DBType(BIGINT), Default(0)
    *  @param alarmStatus Database column alarm_status DBType(INT), Default(1)
    *  @param degradeStatus Database column degrade_status DBType(INT), Default(1)
    *  @param degradeend Database column degradeEnd DBType(INT), Default(1)
    *  @param watchPeriod Database column watch_period DBType(INT), Default(10)
    *  @param ctime Database column ctime DBType(BIGINT), Default(0)
    *  @param utime Database column utime DBType(BIGINT), Default(0)
    *  @param oswatchid Database column oswatchId DBType(BIGINT), Default(0)
    *  @param providerCountSwitch Database column provider_count_switch DBType(INT), Default(0)
    *  @param hostQpsCapacity Database column host_qps_capacity DBType(BIGINT), Default(0)
    *  @param clusterQpsCapacity Database column cluster_qps_capacity DBType(BIGINT), Default(0)
    *  @param testStatus Database column test_status DBType(INT), Default(0)
    *  @param ackStatus Database column ack_status DBType(INT), Default(0)
    *  @param ackUser Database column ack_user DBType(VARCHAR), Length(32,true), Default()
    *  @param ackTime Database column ack_time DBType(INT), Default(0) */
  case class AppQuotaRow(id: Long, name: String = "", appkey: String = "", env: Int = 0, method: String = "", qpsCapacity: Long = 0L, alarmStatus: Int = 1, degradeStatus: Int = 1, degradeend: Int = 1, watchPeriod: Int = 10, ctime: Long = 0L, utime: Long = 0L, oswatchid: Long = 0L, providerCountSwitch: Int = 0, hostQpsCapacity: Long = 0L, clusterQpsCapacity: Long = 0L, testStatus: Int = 0, ackStatus: Int = 0, ackUser: String = "", ackTime: Int = 0)
  /** GetResult implicit for fetching AppQuotaRow objects using plain SQL queries */
  implicit def GetResultAppQuotaRow(implicit e0: GR[Long], e1: GR[String], e2: GR[Int]): GR[AppQuotaRow] = GR{
    prs => import prs._
      AppQuotaRow.tupled((<<[Long], <<[String], <<[String], <<[Int], <<[String], <<[Long], <<[Int], <<[Int], <<[Int], <<[Int], <<[Long], <<[Long], <<[Long], <<[Int], <<[Long], <<[Long], <<[Int], <<[Int], <<[String], <<[Int]))
  }
  /** Table description of table app_quota. Objects of this class serve as prototypes for rows in queries. */
  class AppQuota(_tableTag: Tag) extends Table[AppQuotaRow](_tableTag, "app_quota") {
    def * = (id, name, appkey, env, method, qpsCapacity, alarmStatus, degradeStatus, degradeend, watchPeriod, ctime, utime, oswatchid, providerCountSwitch, hostQpsCapacity, clusterQpsCapacity, testStatus, ackStatus, ackUser, ackTime) <> (AppQuotaRow.tupled, AppQuotaRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (id.?, name.?, appkey.?, env.?, method.?, qpsCapacity.?, alarmStatus.?, degradeStatus.?, degradeend.?, watchPeriod.?, ctime.?, utime.?, oswatchid.?, providerCountSwitch.?, hostQpsCapacity.?, clusterQpsCapacity.?, testStatus.?, ackStatus.?, ackUser.?, ackTime.?).shaped.<>({r=>import r._; _1.map(_=> AppQuotaRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6.get, _7.get, _8.get, _9.get, _10.get, _11.get, _12.get, _13.get, _14.get, _15.get, _16.get, _17.get, _18.get, _19.get, _20.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id DBType(BIGINT), AutoInc, PrimaryKey */
    val id: Column[Long] = column[Long]("id", O.AutoInc, O.PrimaryKey)
    /** Database column name DBType(VARCHAR), Length(128,true), Default() */
    val name: Column[String] = column[String]("name", O.Length(128,varying=true), O.Default(""))
    /** Database column appkey DBType(VARCHAR), Length(128,true), Default() */
    val appkey: Column[String] = column[String]("appkey", O.Length(128,varying=true), O.Default(""))
    /** Database column env DBType(INT), Default(0) */
    val env: Column[Int] = column[Int]("env", O.Default(0))
    /** Database column method DBType(VARCHAR), Length(255,true), Default() */
    val method: Column[String] = column[String]("method", O.Length(255,varying=true), O.Default(""))
    /** Database column qps_capacity DBType(BIGINT), Default(0) */
    val qpsCapacity: Column[Long] = column[Long]("qps_capacity", O.Default(0L))
    /** Database column alarm_status DBType(INT), Default(1) */
    val alarmStatus: Column[Int] = column[Int]("alarm_status", O.Default(1))
    /** Database column degrade_status DBType(INT), Default(1) */
    val degradeStatus: Column[Int] = column[Int]("degrade_status", O.Default(1))
    /** Database column degradeEnd DBType(INT), Default(1) */
    val degradeend: Column[Int] = column[Int]("degradeEnd", O.Default(1))
    /** Database column watch_period DBType(INT), Default(10) */
    val watchPeriod: Column[Int] = column[Int]("watch_period", O.Default(10))
    /** Database column ctime DBType(BIGINT), Default(0) */
    val ctime: Column[Long] = column[Long]("ctime", O.Default(0L))
    /** Database column utime DBType(BIGINT), Default(0) */
    val utime: Column[Long] = column[Long]("utime", O.Default(0L))
    /** Database column oswatchId DBType(BIGINT), Default(0) */
    val oswatchid: Column[Long] = column[Long]("oswatchId", O.Default(0L))
    /** Database column provider_count_switch DBType(INT), Default(0) */
    val providerCountSwitch: Column[Int] = column[Int]("provider_count_switch", O.Default(0))
    /** Database column host_qps_capacity DBType(BIGINT), Default(0) */
    val hostQpsCapacity: Column[Long] = column[Long]("host_qps_capacity", O.Default(0L))
    /** Database column cluster_qps_capacity DBType(BIGINT), Default(0) */
    val clusterQpsCapacity: Column[Long] = column[Long]("cluster_qps_capacity", O.Default(0L))
    /** Database column test_status DBType(INT), Default(0) */
    val testStatus: Column[Int] = column[Int]("test_status", O.Default(0))
    /** Database column ack_status DBType(INT), Default(0) */
    val ackStatus: Column[Int] = column[Int]("ack_status", O.Default(0))
    /** Database column ack_user DBType(VARCHAR), Length(32,true), Default() */
    val ackUser: Column[String] = column[String]("ack_user", O.Length(32,varying=true), O.Default(""))
    /** Database column ack_time DBType(INT), Default(0) */
    val ackTime: Column[Int] = column[Int]("ack_time", O.Default(0))

    /** Uniqueness Index over (appkey,env,method) (database name appkey) */
    val index1 = index("appkey", (appkey, env, method), unique=true)
    /** Index over (utime) (database name idx_time) */
    val index2 = index("idx_time", utime)
  }
  /** Collection-like TableQuery object for table AppQuota */
  lazy val AppQuota = new TableQuery(tag => new AppQuota(tag))

  /** Entity class storing rows of table AppScreen
    *  @param id Database column id DBType(BIGINT), AutoInc, PrimaryKey
    *  @param category Database column category DBType(VARCHAR), Length(128,true), Default(appkey)
    *  @param appkey Database column appkey DBType(VARCHAR), Length(128,true), Default()
    *  @param endpoint Database column endpoint DBType(TEXT), Length(65535,true), Default(None)
    *  @param serverNode Database column server_node DBType(VARCHAR), Length(1024,true), Default()
    *  @param title Database column title DBType(VARCHAR), Length(1024,true), Default()
    *  @param metric Database column metric DBType(VARCHAR), Length(1024,true), Default()
    *  @param sampleMode Database column sample_mode DBType(VARCHAR), Length(128,true), Default(sum)
    *  @param auth Database column auth DBType(INT), Default(0)
    *  @param updateTime Database column update_time DBType(BIGINT), Default(0) */
  case class AppScreenRow(id: Long, category: String = "appkey", appkey: String = "", endpoint: Option[String] = None, serverNode: String = "", title: String = "", metric: String = "", sampleMode: String = "sum", auth: Int = 0, updateTime: Long = 0L)
  /** GetResult implicit for fetching AppScreenRow objects using plain SQL queries */
  implicit def GetResultAppScreenRow(implicit e0: GR[Long], e1: GR[String], e2: GR[Option[String]], e3: GR[Int]): GR[AppScreenRow] = GR{
    prs => import prs._
      AppScreenRow.tupled((<<[Long], <<[String], <<[String], <<?[String], <<[String], <<[String], <<[String], <<[String], <<[Int], <<[Long]))
  }
  /** Table description of table app_screen. Objects of this class serve as prototypes for rows in queries. */
  class AppScreen(_tableTag: Tag) extends Table[AppScreenRow](_tableTag, "app_screen") {
    def * = (id, category, appkey, endpoint, serverNode, title, metric, sampleMode, auth, updateTime) <> (AppScreenRow.tupled, AppScreenRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (id.?, category.?, appkey.?, endpoint, serverNode.?, title.?, metric.?, sampleMode.?, auth.?, updateTime.?).shaped.<>({r=>import r._; _1.map(_=> AppScreenRow.tupled((_1.get, _2.get, _3.get, _4, _5.get, _6.get, _7.get, _8.get, _9.get, _10.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id DBType(BIGINT), AutoInc, PrimaryKey */
    val id: Column[Long] = column[Long]("id", O.AutoInc, O.PrimaryKey)
    /** Database column category DBType(VARCHAR), Length(128,true), Default(appkey) */
    val category: Column[String] = column[String]("category", O.Length(128,varying=true), O.Default("appkey"))
    /** Database column appkey DBType(VARCHAR), Length(128,true), Default() */
    val appkey: Column[String] = column[String]("appkey", O.Length(128,varying=true), O.Default(""))
    /** Database column endpoint DBType(TEXT), Length(65535,true), Default(None) */
    val endpoint: Column[Option[String]] = column[Option[String]]("endpoint", O.Length(65535,varying=true), O.Default(None))
    /** Database column server_node DBType(VARCHAR), Length(1024,true), Default() */
    val serverNode: Column[String] = column[String]("server_node", O.Length(1024,varying=true), O.Default(""))
    /** Database column title DBType(VARCHAR), Length(1024,true), Default() */
    val title: Column[String] = column[String]("title", O.Length(1024,varying=true), O.Default(""))
    /** Database column metric DBType(VARCHAR), Length(1024,true), Default() */
    val metric: Column[String] = column[String]("metric", O.Length(1024,varying=true), O.Default(""))
    /** Database column sample_mode DBType(VARCHAR), Length(128,true), Default(sum) */
    val sampleMode: Column[String] = column[String]("sample_mode", O.Length(128,varying=true), O.Default("sum"))
    /** Database column auth DBType(INT), Default(0) */
    val auth: Column[Int] = column[Int]("auth", O.Default(0))
    /** Database column update_time DBType(BIGINT), Default(0) */
    val updateTime: Column[Long] = column[Long]("update_time", O.Default(0L))

    /** Index over (appkey) (database name index_appkey) */
    val index1 = index("index_appkey", appkey)
  }
  /** Collection-like TableQuery object for table AppScreen */
  lazy val AppScreen = new TableQuery(tag => new AppScreen(tag))

  /** Entity class storing rows of table AppScreenAuth
    *  @param id Database column id DBType(BIGINT), AutoInc, PrimaryKey
    *  @param appkey Database column appkey DBType(VARCHAR), Length(128,true), Default()
    *  @param metric Database column metric DBType(VARCHAR), Length(1024,true), Default()
    *  @param userLogin Database column user_login DBType(VARCHAR), Length(128,true), Default()
    *  @param userAuth Database column user_auth DBType(VARCHAR), Length(128,true), Default(write) */
  case class AppScreenAuthRow(id: Long, appkey: String = "", metric: String = "", userLogin: String = "", userAuth: String = "write")
  /** GetResult implicit for fetching AppScreenAuthRow objects using plain SQL queries */
  implicit def GetResultAppScreenAuthRow(implicit e0: GR[Long], e1: GR[String]): GR[AppScreenAuthRow] = GR{
    prs => import prs._
      AppScreenAuthRow.tupled((<<[Long], <<[String], <<[String], <<[String], <<[String]))
  }
  /** Table description of table app_screen_auth. Objects of this class serve as prototypes for rows in queries. */
  class AppScreenAuth(_tableTag: Tag) extends Table[AppScreenAuthRow](_tableTag, "app_screen_auth") {
    def * = (id, appkey, metric, userLogin, userAuth) <> (AppScreenAuthRow.tupled, AppScreenAuthRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (id.?, appkey.?, metric.?, userLogin.?, userAuth.?).shaped.<>({r=>import r._; _1.map(_=> AppScreenAuthRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id DBType(BIGINT), AutoInc, PrimaryKey */
    val id: Column[Long] = column[Long]("id", O.AutoInc, O.PrimaryKey)
    /** Database column appkey DBType(VARCHAR), Length(128,true), Default() */
    val appkey: Column[String] = column[String]("appkey", O.Length(128,varying=true), O.Default(""))
    /** Database column metric DBType(VARCHAR), Length(1024,true), Default() */
    val metric: Column[String] = column[String]("metric", O.Length(1024,varying=true), O.Default(""))
    /** Database column user_login DBType(VARCHAR), Length(128,true), Default() */
    val userLogin: Column[String] = column[String]("user_login", O.Length(128,varying=true), O.Default(""))
    /** Database column user_auth DBType(VARCHAR), Length(128,true), Default(write) */
    val userAuth: Column[String] = column[String]("user_auth", O.Length(128,varying=true), O.Default("write"))
  }
  /** Collection-like TableQuery object for table AppScreenAuth */
  lazy val AppScreenAuth = new TableQuery(tag => new AppScreenAuth(tag))

  /** Entity class storing rows of table AppServer
    *  @param id Database column id DBType(BIGINT), AutoInc, PrimaryKey
    *  @param ip Database column ip DBType(VARCHAR), Length(64,true), Default()
    *  @param port Database column port DBType(INT), Default(0)
    *  @param weight Database column weight DBType(INT), Default(10)
    *  @param status Database column status DBType(INT), Default(0)
    *  @param role Database column role DBType(INT), Default(0)
    *  @param category Database column category DBType(VARCHAR), Length(32,true), Default()
    *  @param appkey Database column appkey DBType(VARCHAR), Length(128,true), Default()
    *  @param env Database column env DBType(INT), Default(0)
    *  @param linkId Database column link_id DBType(BIGINT), Default(0)
    *  @param ctime Database column ctime DBType(INT), Default(0)
    *  @param utime Database column utime DBType(INT), Default(0)
    *  @param extend Database column extend DBType(VARCHAR), Length(128,true), Default() */
  case class AppServerRow(id: Long, ip: String = "", port: Int = 0, weight: Int = 10, status: Int = 0, role: Int = 0, category: String = "", appkey: String = "", env: Int = 0, linkId: Long = 0L, ctime: Int = 0, utime: Int = 0, extend: String = "")
  /** GetResult implicit for fetching AppServerRow objects using plain SQL queries */
  implicit def GetResultAppServerRow(implicit e0: GR[Long], e1: GR[String], e2: GR[Int]): GR[AppServerRow] = GR{
    prs => import prs._
      AppServerRow.tupled((<<[Long], <<[String], <<[Int], <<[Int], <<[Int], <<[Int], <<[String], <<[String], <<[Int], <<[Long], <<[Int], <<[Int], <<[String]))
  }
  /** Table description of table app_server. Objects of this class serve as prototypes for rows in queries. */
  class AppServer(_tableTag: Tag) extends Table[AppServerRow](_tableTag, "app_server") {
    def * = (id, ip, port, weight, status, role, category, appkey, env, linkId, ctime, utime, extend) <> (AppServerRow.tupled, AppServerRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (id.?, ip.?, port.?, weight.?, status.?, role.?, category.?, appkey.?, env.?, linkId.?, ctime.?, utime.?, extend.?).shaped.<>({r=>import r._; _1.map(_=> AppServerRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6.get, _7.get, _8.get, _9.get, _10.get, _11.get, _12.get, _13.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id DBType(BIGINT), AutoInc, PrimaryKey */
    val id: Column[Long] = column[Long]("id", O.AutoInc, O.PrimaryKey)
    /** Database column ip DBType(VARCHAR), Length(64,true), Default() */
    val ip: Column[String] = column[String]("ip", O.Length(64,varying=true), O.Default(""))
    /** Database column port DBType(INT), Default(0) */
    val port: Column[Int] = column[Int]("port", O.Default(0))
    /** Database column weight DBType(INT), Default(10) */
    val weight: Column[Int] = column[Int]("weight", O.Default(10))
    /** Database column status DBType(INT), Default(0) */
    val status: Column[Int] = column[Int]("status", O.Default(0))
    /** Database column role DBType(INT), Default(0) */
    val role: Column[Int] = column[Int]("role", O.Default(0))
    /** Database column category DBType(VARCHAR), Length(32,true), Default() */
    val category: Column[String] = column[String]("category", O.Length(32,varying=true), O.Default(""))
    /** Database column appkey DBType(VARCHAR), Length(128,true), Default() */
    val appkey: Column[String] = column[String]("appkey", O.Length(128,varying=true), O.Default(""))
    /** Database column env DBType(INT), Default(0) */
    val env: Column[Int] = column[Int]("env", O.Default(0))
    /** Database column link_id DBType(BIGINT), Default(0) */
    val linkId: Column[Long] = column[Long]("link_id", O.Default(0L))
    /** Database column ctime DBType(INT), Default(0) */
    val ctime: Column[Int] = column[Int]("ctime", O.Default(0))
    /** Database column utime DBType(INT), Default(0) */
    val utime: Column[Int] = column[Int]("utime", O.Default(0))
    /** Database column extend DBType(VARCHAR), Length(128,true), Default() */
    val extend: Column[String] = column[String]("extend", O.Length(128,varying=true), O.Default(""))

    /** Index over (utime) (database name idx_time) */
    val index1 = index("idx_time", utime)
  }
  /** Collection-like TableQuery object for table AppServer */
  lazy val AppServer = new TableQuery(tag => new AppServer(tag))

  /** Entity class storing rows of table AppSubscribe
    *  @param id Database column id DBType(BIGINT UNSIGNED), AutoInc, PrimaryKey
    *  @param appkey Database column appkey DBType(VARCHAR), Length(128,true)
    *  @param username Database column username DBType(VARCHAR), Length(128,true)
    *  @param userId Database column user_id DBType(BIGINT) */
  case class AppSubscribeRow(id: Long, appkey: String, username: String, userId: Long)
  /** GetResult implicit for fetching AppSubscribeRow objects using plain SQL queries */
  implicit def GetResultAppSubscribeRow(implicit e0: GR[Long], e1: GR[String]): GR[AppSubscribeRow] = GR{
    prs => import prs._
      AppSubscribeRow.tupled((<<[Long], <<[String], <<[String], <<[Long]))
  }
  /** Table description of table app_subscribe. Objects of this class serve as prototypes for rows in queries. */
  class AppSubscribe(_tableTag: Tag) extends Table[AppSubscribeRow](_tableTag, "app_subscribe") {
    def * = (id, appkey, username, userId) <> (AppSubscribeRow.tupled, AppSubscribeRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (id.?, appkey.?, username.?, userId.?).shaped.<>({r=>import r._; _1.map(_=> AppSubscribeRow.tupled((_1.get, _2.get, _3.get, _4.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id DBType(BIGINT UNSIGNED), AutoInc, PrimaryKey */
    val id: Column[Long] = column[Long]("id", O.AutoInc, O.PrimaryKey)
    /** Database column appkey DBType(VARCHAR), Length(128,true) */
    val appkey: Column[String] = column[String]("appkey", O.Length(128,varying=true))
    /** Database column username DBType(VARCHAR), Length(128,true) */
    val username: Column[String] = column[String]("username", O.Length(128,varying=true))
    /** Database column user_id DBType(BIGINT) */
    val userId: Column[Long] = column[Long]("user_id")

    /** Uniqueness Index over (appkey,userId) (database name idx_appkey_userid) */
    val index1 = index("idx_appkey_userid", (appkey, userId), unique=true)
    /** Index over (userId) (database name idx_user_id) */
    val index2 = index("idx_user_id", userId)
  }
  /** Collection-like TableQuery object for table AppSubscribe */
  lazy val AppSubscribe = new TableQuery(tag => new AppSubscribe(tag))

  /** Entity class storing rows of table AppTrend
    *  @param id Database column id DBType(INT UNSIGNED), AutoInc, PrimaryKey
    *  @param base Database column base DBType(VARCHAR), Length(11,true), Default()
    *  @param business Database column business DBType(VARCHAR), Length(128,true), Default()
    *  @param owt Database column owt DBType(VARCHAR), Length(64,true), Default()
    *  @param pdl Database column pdl DBType(VARCHAR), Length(64,true), Default()
    *  @param groupId Database column group_id DBType(VARCHAR), Length(256,true), Default()
    *  @param artifactId Database column artifact_id DBType(VARCHAR), Length(128,true), Default()
    *  @param version Database column version DBType(VARCHAR), Length(128,true), Default()
    *  @param coverage Database column coverage DBType(INT), Default(0)
    *  @param date Database column date DBType(DATE) */
  case class AppTrendRow(id: Int, base: String = "", business: String = "", owt: String = "", pdl: String = "", groupId: String = "", artifactId: String = "", version: String = "", coverage: Int = 0, date: java.sql.Date)
  /** GetResult implicit for fetching AppTrendRow objects using plain SQL queries */
  implicit def GetResultAppTrendRow(implicit e0: GR[Int], e1: GR[String], e2: GR[java.sql.Date]): GR[AppTrendRow] = GR{
    prs => import prs._
      AppTrendRow.tupled((<<[Int], <<[String], <<[String], <<[String], <<[String], <<[String], <<[String], <<[String], <<[Int], <<[java.sql.Date]))
  }
  /** Table description of table app_trend. Objects of this class serve as prototypes for rows in queries. */
  class AppTrend(_tableTag: Tag) extends Table[AppTrendRow](_tableTag, "app_trend") {
    def * = (id, base, business, owt, pdl, groupId, artifactId, version, coverage, date) <> (AppTrendRow.tupled, AppTrendRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (id.?, base.?, business.?, owt.?, pdl.?, groupId.?, artifactId.?, version.?, coverage.?, date.?).shaped.<>({r=>import r._; _1.map(_=> AppTrendRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6.get, _7.get, _8.get, _9.get, _10.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id DBType(INT UNSIGNED), AutoInc, PrimaryKey */
    val id: Column[Int] = column[Int]("id", O.AutoInc, O.PrimaryKey)
    /** Database column base DBType(VARCHAR), Length(11,true), Default() */
    val base: Column[String] = column[String]("base", O.Length(11,varying=true), O.Default(""))
    /** Database column business DBType(VARCHAR), Length(128,true), Default() */
    val business: Column[String] = column[String]("business", O.Length(128,varying=true), O.Default(""))
    /** Database column owt DBType(VARCHAR), Length(64,true), Default() */
    val owt: Column[String] = column[String]("owt", O.Length(64,varying=true), O.Default(""))
    /** Database column pdl DBType(VARCHAR), Length(64,true), Default() */
    val pdl: Column[String] = column[String]("pdl", O.Length(64,varying=true), O.Default(""))
    /** Database column group_id DBType(VARCHAR), Length(256,true), Default() */
    val groupId: Column[String] = column[String]("group_id", O.Length(256,varying=true), O.Default(""))
    /** Database column artifact_id DBType(VARCHAR), Length(128,true), Default() */
    val artifactId: Column[String] = column[String]("artifact_id", O.Length(128,varying=true), O.Default(""))
    /** Database column version DBType(VARCHAR), Length(128,true), Default() */
    val version: Column[String] = column[String]("version", O.Length(128,varying=true), O.Default(""))
    /** Database column coverage DBType(INT), Default(0) */
    val coverage: Column[Int] = column[Int]("coverage", O.Default(0))
    /** Database column date DBType(DATE) */
    val date: Column[java.sql.Date] = column[java.sql.Date]("date")

    /** Index over (business,owt,pdl) (database name idx_business) */
    val index1 = index("idx_business", (business, owt, pdl))
    /** Index over (groupId,artifactId,version) (database name idx_component) */
    val index2 = index("idx_component", (groupId, artifactId, version))
    /** Index over (date) (database name idx_date) */
    val index3 = index("idx_date", date)
  }
  /** Collection-like TableQuery object for table AppTrend */
  lazy val AppTrend = new TableQuery(tag => new AppTrend(tag))

  /** Entity class storing rows of table AppTypeDoc
    *  @param id Database column id DBType(BIGINT), AutoInc, PrimaryKey
    *  @param appkey Database column appkey DBType(VARCHAR), Length(128,true), Default()
    *  @param `type` Database column type DBType(VARCHAR), Length(512,true), Default()
    *  @param simpleType Database column simple_type DBType(VARCHAR), Length(128,true), Default()
    *  @param paramTypes Database column param_types DBType(MEDIUMTEXT), Length(16777215,true), Default(None)
    *  @param commentText Database column comment_text DBType(VARCHAR), Length(256,true), Default(Some())
    *  @param fields Database column fields DBType(MEDIUMTEXT), Length(16777215,true), Default(None)
    *  @param gsign Database column gsign DBType(VARCHAR), Length(32,true), Default()
    *  @param sign Database column sign DBType(VARCHAR), Length(32,true), Default()
    *  @param ctime Database column ctime DBType(BIGINT), Default(0) */
  case class AppTypeDocRow(id: Long, appkey: String = "", `type`: String = "", simpleType: String = "", paramTypes: Option[String] = None, commentText: Option[String] = Some(""), fields: Option[String] = None, gsign: String = "", sign: String = "", ctime: Long = 0L)
  /** GetResult implicit for fetching AppTypeDocRow objects using plain SQL queries */
  implicit def GetResultAppTypeDocRow(implicit e0: GR[Long], e1: GR[String], e2: GR[Option[String]]): GR[AppTypeDocRow] = GR{
    prs => import prs._
      AppTypeDocRow.tupled((<<[Long], <<[String], <<[String], <<[String], <<?[String], <<?[String], <<?[String], <<[String], <<[String], <<[Long]))
  }
  /** Table description of table app_type_doc. Objects of this class serve as prototypes for rows in queries.
    *  NOTE: The following names collided with Scala keywords and were escaped: type */
  class AppTypeDoc(_tableTag: Tag) extends Table[AppTypeDocRow](_tableTag, "app_type_doc") {
    def * = (id, appkey, `type`, simpleType, paramTypes, commentText, fields, gsign, sign, ctime) <> (AppTypeDocRow.tupled, AppTypeDocRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (id.?, appkey.?, `type`.?, simpleType.?, paramTypes, commentText, fields, gsign.?, sign.?, ctime.?).shaped.<>({r=>import r._; _1.map(_=> AppTypeDocRow.tupled((_1.get, _2.get, _3.get, _4.get, _5, _6, _7, _8.get, _9.get, _10.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id DBType(BIGINT), AutoInc, PrimaryKey */
    val id: Column[Long] = column[Long]("id", O.AutoInc, O.PrimaryKey)
    /** Database column appkey DBType(VARCHAR), Length(128,true), Default() */
    val appkey: Column[String] = column[String]("appkey", O.Length(128,varying=true), O.Default(""))
    /** Database column type DBType(VARCHAR), Length(512,true), Default()
      *  NOTE: The name was escaped because it collided with a Scala keyword. */
    val `type`: Column[String] = column[String]("type", O.Length(512,varying=true), O.Default(""))
    /** Database column simple_type DBType(VARCHAR), Length(128,true), Default() */
    val simpleType: Column[String] = column[String]("simple_type", O.Length(128,varying=true), O.Default(""))
    /** Database column param_types DBType(MEDIUMTEXT), Length(16777215,true), Default(None) */
    val paramTypes: Column[Option[String]] = column[Option[String]]("param_types", O.Length(16777215,varying=true), O.Default(None))
    /** Database column comment_text DBType(VARCHAR), Length(256,true), Default(Some()) */
    val commentText: Column[Option[String]] = column[Option[String]]("comment_text", O.Length(256,varying=true), O.Default(Some("")))
    /** Database column fields DBType(MEDIUMTEXT), Length(16777215,true), Default(None) */
    val fields: Column[Option[String]] = column[Option[String]]("fields", O.Length(16777215,varying=true), O.Default(None))
    /** Database column gsign DBType(VARCHAR), Length(32,true), Default() */
    val gsign: Column[String] = column[String]("gsign", O.Length(32,varying=true), O.Default(""))
    /** Database column sign DBType(VARCHAR), Length(32,true), Default() */
    val sign: Column[String] = column[String]("sign", O.Length(32,varying=true), O.Default(""))
    /** Database column ctime DBType(BIGINT), Default(0) */
    val ctime: Column[Long] = column[Long]("ctime", O.Default(0L))

    /** Index over (ctime) (database name idx_time) */
    val index1 = index("idx_time", ctime)
  }
  /** Collection-like TableQuery object for table AppTypeDoc */
  lazy val AppTypeDoc = new TableQuery(tag => new AppTypeDoc(tag))

  /** Entity class storing rows of table AppUpstream
    *  @param id Database column id DBType(BIGINT), AutoInc, PrimaryKey
    *  @param appkey Database column appkey DBType(VARCHAR), Length(128,true), Default()
    *  @param env Database column env DBType(INT), Default(0)
    *  @param name Database column name DBType(VARCHAR), Length(64,true), Default()
    *  @param nginx Database column nginx DBType(VARCHAR), Length(64,true), Default()
    *  @param commit Database column commit DBType(VARCHAR), Length(64,true), Default()
    *  @param idc Database column idc DBType(VARCHAR), Length(64,true), Default()
    *  @param ctime Database column ctime DBType(INT), Default(0)
    *  @param utime Database column utime DBType(INT), Default(0) */
  case class AppUpstreamRow(id: Long, appkey: String = "", env: Int = 0, name: String = "", nginx: String = "", commit: String = "", idc: String = "", ctime: Int = 0, utime: Int = 0)
  /** GetResult implicit for fetching AppUpstreamRow objects using plain SQL queries */
  implicit def GetResultAppUpstreamRow(implicit e0: GR[Long], e1: GR[String], e2: GR[Int]): GR[AppUpstreamRow] = GR{
    prs => import prs._
      AppUpstreamRow.tupled((<<[Long], <<[String], <<[Int], <<[String], <<[String], <<[String], <<[String], <<[Int], <<[Int]))
  }
  /** Table description of table app_upstream. Objects of this class serve as prototypes for rows in queries. */
  class AppUpstream(_tableTag: Tag) extends Table[AppUpstreamRow](_tableTag, "app_upstream") {
    def * = (id, appkey, env, name, nginx, commit, idc, ctime, utime) <> (AppUpstreamRow.tupled, AppUpstreamRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (id.?, appkey.?, env.?, name.?, nginx.?, commit.?, idc.?, ctime.?, utime.?).shaped.<>({r=>import r._; _1.map(_=> AppUpstreamRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6.get, _7.get, _8.get, _9.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id DBType(BIGINT), AutoInc, PrimaryKey */
    val id: Column[Long] = column[Long]("id", O.AutoInc, O.PrimaryKey)
    /** Database column appkey DBType(VARCHAR), Length(128,true), Default() */
    val appkey: Column[String] = column[String]("appkey", O.Length(128,varying=true), O.Default(""))
    /** Database column env DBType(INT), Default(0) */
    val env: Column[Int] = column[Int]("env", O.Default(0))
    /** Database column name DBType(VARCHAR), Length(64,true), Default() */
    val name: Column[String] = column[String]("name", O.Length(64,varying=true), O.Default(""))
    /** Database column nginx DBType(VARCHAR), Length(64,true), Default() */
    val nginx: Column[String] = column[String]("nginx", O.Length(64,varying=true), O.Default(""))
    /** Database column commit DBType(VARCHAR), Length(64,true), Default() */
    val commit: Column[String] = column[String]("commit", O.Length(64,varying=true), O.Default(""))
    /** Database column idc DBType(VARCHAR), Length(64,true), Default() */
    val idc: Column[String] = column[String]("idc", O.Length(64,varying=true), O.Default(""))
    /** Database column ctime DBType(INT), Default(0) */
    val ctime: Column[Int] = column[Int]("ctime", O.Default(0))
    /** Database column utime DBType(INT), Default(0) */
    val utime: Column[Int] = column[Int]("utime", O.Default(0))

    /** Index over (appkey,env) (database name idx_app) */
    val index1 = index("idx_app", (appkey, env))
    /** Index over (utime) (database name idx_time) */
    val index2 = index("idx_time", utime)
  }
  /** Collection-like TableQuery object for table AppUpstream */
  lazy val AppUpstream = new TableQuery(tag => new AppUpstream(tag))

  /** Entity class storing rows of table AvailabilityData
    *  @param id Database column id DBType(INT), AutoInc, PrimaryKey
    *  @param module Database column module DBType(VARCHAR), Length(45,true), Default()
    *  @param date Database column date DBType(DATETIME)
    *  @param env Database column env DBType(VARCHAR), Length(20,true)
    *  @param errorNum Database column error_num DBType(DOUBLE), Default(0.0)
    *  @param totalNum Database column total_num DBType(DOUBLE), Default(0.0)
    *  @param cost Database column cost DBType(DOUBLE), Default(0.0) */
  case class AvailabilityDataRow(id: Int, module: String = "", date: java.sql.Timestamp, env: String, errorNum: Double = 0.0, totalNum: Double = 0.0, cost: Double = 0.0)
  /** GetResult implicit for fetching AvailabilityDataRow objects using plain SQL queries */
  implicit def GetResultAvailabilityDataRow(implicit e0: GR[Int], e1: GR[String], e2: GR[java.sql.Timestamp], e3: GR[Double]): GR[AvailabilityDataRow] = GR{
    prs => import prs._
      AvailabilityDataRow.tupled((<<[Int], <<[String], <<[java.sql.Timestamp], <<[String], <<[Double], <<[Double], <<[Double]))
  }
  /** Table description of table availability_data. Objects of this class serve as prototypes for rows in queries. */
  class AvailabilityData(_tableTag: Tag) extends Table[AvailabilityDataRow](_tableTag, "availability_data") {
    def * = (id, module, date, env, errorNum, totalNum, cost) <> (AvailabilityDataRow.tupled, AvailabilityDataRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (id.?, module.?, date.?, env.?, errorNum.?, totalNum.?, cost.?).shaped.<>({r=>import r._; _1.map(_=> AvailabilityDataRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6.get, _7.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id DBType(INT), AutoInc, PrimaryKey */
    val id: Column[Int] = column[Int]("id", O.AutoInc, O.PrimaryKey)
    /** Database column module DBType(VARCHAR), Length(45,true), Default() */
    val module: Column[String] = column[String]("module", O.Length(45,varying=true), O.Default(""))
    /** Database column date DBType(DATETIME) */
    val date: Column[java.sql.Timestamp] = column[java.sql.Timestamp]("date")
    /** Database column env DBType(VARCHAR), Length(20,true) */
    val env: Column[String] = column[String]("env", O.Length(20,varying=true))
    /** Database column error_num DBType(DOUBLE), Default(0.0) */
    val errorNum: Column[Double] = column[Double]("error_num", O.Default(0.0))
    /** Database column total_num DBType(DOUBLE), Default(0.0) */
    val totalNum: Column[Double] = column[Double]("total_num", O.Default(0.0))
    /** Database column cost DBType(DOUBLE), Default(0.0) */
    val cost: Column[Double] = column[Double]("cost", O.Default(0.0))

    /** Index over (date) (database name idx_date) */
    val index1 = index("idx_date", date)
  }
  /** Collection-like TableQuery object for table AvailabilityData */
  lazy val AvailabilityData = new TableQuery(tag => new AvailabilityData(tag))

  /** Entity class storing rows of table AvailabilityDayDetail
    *  @param id Database column id DBType(BIGINT), AutoInc, PrimaryKey
    *  @param ts Database column ts DBType(INT), Default(0)
    *  @param appkey Database column appkey DBType(VARCHAR), Length(128,true), Default()
    *  @param spanname Database column spanname DBType(VARCHAR), Length(255,true), Default()
    *  @param remoteAppkey Database column remote_appkey DBType(VARCHAR), Length(128,true), Default()
    *  @param count Database column count DBType(BIGINT), Default(0)
    *  @param successCount Database column success_count DBType(BIGINT), Default(0)
    *  @param successCountPer Database column success_count_per DBType(DECIMAL), Default(None)
    *  @param failureCount Database column failure_count DBType(BIGINT), Default(0)
    *  @param failureCountPer Database column failure_count_per DBType(DECIMAL), Default(None)
    *  @param exceptionCount Database column exception_count DBType(BIGINT), Default(0)
    *  @param exceptionCountPer Database column exception_count_per DBType(DECIMAL), Default(None)
    *  @param timeoutCount Database column timeout_count DBType(BIGINT), Default(0)
    *  @param timeoutCountPer Database column timeout_count_per DBType(DECIMAL), Default(None)
    *  @param dropCount Database column drop_count DBType(BIGINT), Default(0)
    *  @param dropCountPer Database column drop_count_per DBType(DECIMAL), Default(None) */
  case class AvailabilityDayDetailRow(id: Long, ts: Int = 0, appkey: String = "", spanname: String = "", remoteAppkey: String = "", count: Long = 0L, successCount: Long = 0L, successCountPer: Option[scala.math.BigDecimal] = None, failureCount: Long = 0L, failureCountPer: Option[scala.math.BigDecimal] = None, exceptionCount: Long = 0L, exceptionCountPer: Option[scala.math.BigDecimal] = None, timeoutCount: Long = 0L, timeoutCountPer: Option[scala.math.BigDecimal] = None, dropCount: Long = 0L, dropCountPer: Option[scala.math.BigDecimal] = None)
  /** GetResult implicit for fetching AvailabilityDayDetailRow objects using plain SQL queries */
  implicit def GetResultAvailabilityDayDetailRow(implicit e0: GR[Long], e1: GR[Int], e2: GR[String], e3: GR[Option[scala.math.BigDecimal]]): GR[AvailabilityDayDetailRow] = GR{
    prs => import prs._
      AvailabilityDayDetailRow.tupled((<<[Long], <<[Int], <<[String], <<[String], <<[String], <<[Long], <<[Long], <<?[scala.math.BigDecimal], <<[Long], <<?[scala.math.BigDecimal], <<[Long], <<?[scala.math.BigDecimal], <<[Long], <<?[scala.math.BigDecimal], <<[Long], <<?[scala.math.BigDecimal]))
  }
  /** Table description of table availability_day_detail. Objects of this class serve as prototypes for rows in queries. */
  class AvailabilityDayDetail(_tableTag: Tag) extends Table[AvailabilityDayDetailRow](_tableTag, "availability_day_detail") {
    def * = (id, ts, appkey, spanname, remoteAppkey, count, successCount, successCountPer, failureCount, failureCountPer, exceptionCount, exceptionCountPer, timeoutCount, timeoutCountPer, dropCount, dropCountPer) <> (AvailabilityDayDetailRow.tupled, AvailabilityDayDetailRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (id.?, ts.?, appkey.?, spanname.?, remoteAppkey.?, count.?, successCount.?, successCountPer, failureCount.?, failureCountPer, exceptionCount.?, exceptionCountPer, timeoutCount.?, timeoutCountPer, dropCount.?, dropCountPer).shaped.<>({r=>import r._; _1.map(_=> AvailabilityDayDetailRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6.get, _7.get, _8, _9.get, _10, _11.get, _12, _13.get, _14, _15.get, _16)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id DBType(BIGINT), AutoInc, PrimaryKey */
    val id: Column[Long] = column[Long]("id", O.AutoInc, O.PrimaryKey)
    /** Database column ts DBType(INT), Default(0) */
    val ts: Column[Int] = column[Int]("ts", O.Default(0))
    /** Database column appkey DBType(VARCHAR), Length(128,true), Default() */
    val appkey: Column[String] = column[String]("appkey", O.Length(128,varying=true), O.Default(""))
    /** Database column spanname DBType(VARCHAR), Length(255,true), Default() */
    val spanname: Column[String] = column[String]("spanname", O.Length(255,varying=true), O.Default(""))
    /** Database column remote_appkey DBType(VARCHAR), Length(128,true), Default() */
    val remoteAppkey: Column[String] = column[String]("remote_appkey", O.Length(128,varying=true), O.Default(""))
    /** Database column count DBType(BIGINT), Default(0) */
    val count: Column[Long] = column[Long]("count", O.Default(0L))
    /** Database column success_count DBType(BIGINT), Default(0) */
    val successCount: Column[Long] = column[Long]("success_count", O.Default(0L))
    /** Database column success_count_per DBType(DECIMAL), Default(None) */
    val successCountPer: Column[Option[scala.math.BigDecimal]] = column[Option[scala.math.BigDecimal]]("success_count_per", O.Default(None))
    /** Database column failure_count DBType(BIGINT), Default(0) */
    val failureCount: Column[Long] = column[Long]("failure_count", O.Default(0L))
    /** Database column failure_count_per DBType(DECIMAL), Default(None) */
    val failureCountPer: Column[Option[scala.math.BigDecimal]] = column[Option[scala.math.BigDecimal]]("failure_count_per", O.Default(None))
    /** Database column exception_count DBType(BIGINT), Default(0) */
    val exceptionCount: Column[Long] = column[Long]("exception_count", O.Default(0L))
    /** Database column exception_count_per DBType(DECIMAL), Default(None) */
    val exceptionCountPer: Column[Option[scala.math.BigDecimal]] = column[Option[scala.math.BigDecimal]]("exception_count_per", O.Default(None))
    /** Database column timeout_count DBType(BIGINT), Default(0) */
    val timeoutCount: Column[Long] = column[Long]("timeout_count", O.Default(0L))
    /** Database column timeout_count_per DBType(DECIMAL), Default(None) */
    val timeoutCountPer: Column[Option[scala.math.BigDecimal]] = column[Option[scala.math.BigDecimal]]("timeout_count_per", O.Default(None))
    /** Database column drop_count DBType(BIGINT), Default(0) */
    val dropCount: Column[Long] = column[Long]("drop_count", O.Default(0L))
    /** Database column drop_count_per DBType(DECIMAL), Default(None) */
    val dropCountPer: Column[Option[scala.math.BigDecimal]] = column[Option[scala.math.BigDecimal]]("drop_count_per", O.Default(None))

    /** Index over (ts) (database name idx_time) */
    val index1 = index("idx_time", ts)
    /** Uniqueness Index over (ts,appkey,spanname,remoteAppkey) (database name ts) */
    val index2 = index("ts", (ts, appkey, spanname, remoteAppkey), unique=true)
  }
  /** Collection-like TableQuery object for table AvailabilityDayDetail */
  lazy val AvailabilityDayDetail = new TableQuery(tag => new AvailabilityDayDetail(tag))

  /** Entity class storing rows of table AvailabilityDayReport
    *  @param id Database column id DBType(BIGINT), AutoInc, PrimaryKey
    *  @param ts Database column ts DBType(INT), Default(0)
    *  @param appkey Database column appkey DBType(VARCHAR), Length(128,true), Default()
    *  @param spanname Database column spanname DBType(VARCHAR), Length(255,true), Default()
    *  @param count Database column count DBType(BIGINT), Default(0)
    *  @param successCount Database column success_count DBType(BIGINT), Default(0)
    *  @param successCountPer Database column success_count_per DBType(DECIMAL), Default(None)
    *  @param exceptionCount Database column exception_count DBType(BIGINT), Default(0)
    *  @param exceptionCountPer Database column exception_count_per DBType(DECIMAL), Default(None)
    *  @param timeoutCount Database column timeout_count DBType(BIGINT), Default(0)
    *  @param timeoutCountPer Database column timeout_count_per DBType(DECIMAL), Default(None)
    *  @param dropCount Database column drop_count DBType(BIGINT), Default(0)
    *  @param dropCountPer Database column drop_count_per DBType(DECIMAL), Default(None)
    *  @param http2xxCount Database column http2xx_count DBType(BIGINT), Default(0)
    *  @param http3xxCount Database column http3xx_count DBType(BIGINT), Default(0)
    *  @param http4xxCount Database column http4xx_count DBType(BIGINT), Default(0)
    *  @param http5xxCount Database column http5xx_count DBType(BIGINT), Default(0)
    *  @param tp50 Database column tp50 DBType(DOUBLE), Default(0.0)
    *  @param costMean Database column cost_mean DBType(DOUBLE), Default(0.0) */
  case class AvailabilityDayReportRow(id: Long, ts: Int = 0, appkey: String = "", spanname: String = "", count: Long = 0L, successCount: Long = 0L, successCountPer: Option[scala.math.BigDecimal] = None, exceptionCount: Long = 0L, exceptionCountPer: Option[scala.math.BigDecimal] = None, timeoutCount: Long = 0L, timeoutCountPer: Option[scala.math.BigDecimal] = None, dropCount: Long = 0L, dropCountPer: Option[scala.math.BigDecimal] = None, http2xxCount: Long = 0L, http3xxCount: Long = 0L, http4xxCount: Long = 0L, http5xxCount: Long = 0L, tp50: Double = 0.0, costMean: Double = 0.0)
  /** GetResult implicit for fetching AvailabilityDayReportRow objects using plain SQL queries */
  implicit def GetResultAvailabilityDayReportRow(implicit e0: GR[Long], e1: GR[Int], e2: GR[String], e3: GR[Option[scala.math.BigDecimal]], e4: GR[Double]): GR[AvailabilityDayReportRow] = GR{
    prs => import prs._
      AvailabilityDayReportRow.tupled((<<[Long], <<[Int], <<[String], <<[String], <<[Long], <<[Long], <<?[scala.math.BigDecimal], <<[Long], <<?[scala.math.BigDecimal], <<[Long], <<?[scala.math.BigDecimal], <<[Long], <<?[scala.math.BigDecimal], <<[Long], <<[Long], <<[Long], <<[Long], <<[Double], <<[Double]))
  }
  /** Table description of table availability_day_report. Objects of this class serve as prototypes for rows in queries. */
  class AvailabilityDayReport(_tableTag: Tag) extends Table[AvailabilityDayReportRow](_tableTag, "availability_day_report") {
    def * = (id, ts, appkey, spanname, count, successCount, successCountPer, exceptionCount, exceptionCountPer, timeoutCount, timeoutCountPer, dropCount, dropCountPer, http2xxCount, http3xxCount, http4xxCount, http5xxCount, tp50, costMean) <> (AvailabilityDayReportRow.tupled, AvailabilityDayReportRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (id.?, ts.?, appkey.?, spanname.?, count.?, successCount.?, successCountPer, exceptionCount.?, exceptionCountPer, timeoutCount.?, timeoutCountPer, dropCount.?, dropCountPer, http2xxCount.?, http3xxCount.?, http4xxCount.?, http5xxCount.?, tp50.?, costMean.?).shaped.<>({r=>import r._; _1.map(_=> AvailabilityDayReportRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6.get, _7, _8.get, _9, _10.get, _11, _12.get, _13, _14.get, _15.get, _16.get, _17.get, _18.get, _19.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id DBType(BIGINT), AutoInc, PrimaryKey */
    val id: Column[Long] = column[Long]("id", O.AutoInc, O.PrimaryKey)
    /** Database column ts DBType(INT), Default(0) */
    val ts: Column[Int] = column[Int]("ts", O.Default(0))
    /** Database column appkey DBType(VARCHAR), Length(128,true), Default() */
    val appkey: Column[String] = column[String]("appkey", O.Length(128,varying=true), O.Default(""))
    /** Database column spanname DBType(VARCHAR), Length(255,true), Default() */
    val spanname: Column[String] = column[String]("spanname", O.Length(255,varying=true), O.Default(""))
    /** Database column count DBType(BIGINT), Default(0) */
    val count: Column[Long] = column[Long]("count", O.Default(0L))
    /** Database column success_count DBType(BIGINT), Default(0) */
    val successCount: Column[Long] = column[Long]("success_count", O.Default(0L))
    /** Database column success_count_per DBType(DECIMAL), Default(None) */
    val successCountPer: Column[Option[scala.math.BigDecimal]] = column[Option[scala.math.BigDecimal]]("success_count_per", O.Default(None))
    /** Database column exception_count DBType(BIGINT), Default(0) */
    val exceptionCount: Column[Long] = column[Long]("exception_count", O.Default(0L))
    /** Database column exception_count_per DBType(DECIMAL), Default(None) */
    val exceptionCountPer: Column[Option[scala.math.BigDecimal]] = column[Option[scala.math.BigDecimal]]("exception_count_per", O.Default(None))
    /** Database column timeout_count DBType(BIGINT), Default(0) */
    val timeoutCount: Column[Long] = column[Long]("timeout_count", O.Default(0L))
    /** Database column timeout_count_per DBType(DECIMAL), Default(None) */
    val timeoutCountPer: Column[Option[scala.math.BigDecimal]] = column[Option[scala.math.BigDecimal]]("timeout_count_per", O.Default(None))
    /** Database column drop_count DBType(BIGINT), Default(0) */
    val dropCount: Column[Long] = column[Long]("drop_count", O.Default(0L))
    /** Database column drop_count_per DBType(DECIMAL), Default(None) */
    val dropCountPer: Column[Option[scala.math.BigDecimal]] = column[Option[scala.math.BigDecimal]]("drop_count_per", O.Default(None))
    /** Database column http2xx_count DBType(BIGINT), Default(0) */
    val http2xxCount: Column[Long] = column[Long]("http2xx_count", O.Default(0L))
    /** Database column http3xx_count DBType(BIGINT), Default(0) */
    val http3xxCount: Column[Long] = column[Long]("http3xx_count", O.Default(0L))
    /** Database column http4xx_count DBType(BIGINT), Default(0) */
    val http4xxCount: Column[Long] = column[Long]("http4xx_count", O.Default(0L))
    /** Database column http5xx_count DBType(BIGINT), Default(0) */
    val http5xxCount: Column[Long] = column[Long]("http5xx_count", O.Default(0L))
    /** Database column tp50 DBType(DOUBLE), Default(0.0) */
    val tp50: Column[Double] = column[Double]("tp50", O.Default(0.0))
    /** Database column cost_mean DBType(DOUBLE), Default(0.0) */
    val costMean: Column[Double] = column[Double]("cost_mean", O.Default(0.0))

    /** Uniqueness Index over (appkey,spanname,ts) (database name appkey_name_ts) */
    val index1 = index("appkey_name_ts", (appkey, spanname, ts), unique=true)
    /** Index over (ts) (database name idx_time) */
    val index2 = index("idx_time", ts)
  }
  /** Collection-like TableQuery object for table AvailabilityDayReport */
  lazy val AvailabilityDayReport = new TableQuery(tag => new AvailabilityDayReport(tag))

  /** Entity class storing rows of table Banner
    *  @param id Database column id DBType(INT UNSIGNED), AutoInc, PrimaryKey
    *  @param messageType Database column message_type DBType(INT), Default(0)
    *  @param messageTitle Database column message_title DBType(VARCHAR), Length(128,true), Default()
    *  @param messageBody Database column message_body DBType(VARCHAR), Length(1024,true), Default()
    *  @param operator Database column operator DBType(VARCHAR), Length(22,true), Default()
    *  @param expired Database column expired DBType(BIT), Default(false)
    *  @param creatTime Database column creat_time DBType(BIGINT), Default(0) */
  case class BannerRow(id: Int, messageType: Int = 0, messageTitle: String = "", messageBody: String = "", operator: String = "", expired: Boolean = false, creatTime: Long = 0L)
  /** GetResult implicit for fetching BannerRow objects using plain SQL queries */
  implicit def GetResultBannerRow(implicit e0: GR[Int], e1: GR[String], e2: GR[Boolean], e3: GR[Long]): GR[BannerRow] = GR{
    prs => import prs._
      BannerRow.tupled((<<[Int], <<[Int], <<[String], <<[String], <<[String], <<[Boolean], <<[Long]))
  }
  /** Table description of table banner. Objects of this class serve as prototypes for rows in queries. */
  class Banner(_tableTag: Tag) extends Table[BannerRow](_tableTag, "banner") {
    def * = (id, messageType, messageTitle, messageBody, operator, expired, creatTime) <> (BannerRow.tupled, BannerRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (id.?, messageType.?, messageTitle.?, messageBody.?, operator.?, expired.?, creatTime.?).shaped.<>({r=>import r._; _1.map(_=> BannerRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6.get, _7.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id DBType(INT UNSIGNED), AutoInc, PrimaryKey */
    val id: Column[Int] = column[Int]("id", O.AutoInc, O.PrimaryKey)
    /** Database column message_type DBType(INT), Default(0) */
    val messageType: Column[Int] = column[Int]("message_type", O.Default(0))
    /** Database column message_title DBType(VARCHAR), Length(128,true), Default() */
    val messageTitle: Column[String] = column[String]("message_title", O.Length(128,varying=true), O.Default(""))
    /** Database column message_body DBType(VARCHAR), Length(1024,true), Default() */
    val messageBody: Column[String] = column[String]("message_body", O.Length(1024,varying=true), O.Default(""))
    /** Database column operator DBType(VARCHAR), Length(22,true), Default() */
    val operator: Column[String] = column[String]("operator", O.Length(22,varying=true), O.Default(""))
    /** Database column expired DBType(BIT), Default(false) */
    val expired: Column[Boolean] = column[Boolean]("expired", O.Default(false))
    /** Database column creat_time DBType(BIGINT), Default(0) */
    val creatTime: Column[Long] = column[Long]("creat_time", O.Default(0L))
  }
  /** Collection-like TableQuery object for table Banner */
  lazy val Banner = new TableQuery(tag => new Banner(tag))

  /** Entity class storing rows of table BusinessDash
    *  @param id Database column id DBType(BIGINT), AutoInc, PrimaryKey
    *  @param owt Database column owt DBType(VARCHAR), Length(128,true), Default()
    *  @param screenId Database column screen_id DBType(BIGINT), Default(0) */
  case class BusinessDashRow(id: Long, owt: String = "", screenId: Long = 0L)
  /** GetResult implicit for fetching BusinessDashRow objects using plain SQL queries */
  implicit def GetResultBusinessDashRow(implicit e0: GR[Long], e1: GR[String]): GR[BusinessDashRow] = GR{
    prs => import prs._
      BusinessDashRow.tupled((<<[Long], <<[String], <<[Long]))
  }
  /** Table description of table business_dash. Objects of this class serve as prototypes for rows in queries. */
  class BusinessDash(_tableTag: Tag) extends Table[BusinessDashRow](_tableTag, "business_dash") {
    def * = (id, owt, screenId) <> (BusinessDashRow.tupled, BusinessDashRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (id.?, owt.?, screenId.?).shaped.<>({r=>import r._; _1.map(_=> BusinessDashRow.tupled((_1.get, _2.get, _3.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id DBType(BIGINT), AutoInc, PrimaryKey */
    val id: Column[Long] = column[Long]("id", O.AutoInc, O.PrimaryKey)
    /** Database column owt DBType(VARCHAR), Length(128,true), Default() */
    val owt: Column[String] = column[String]("owt", O.Length(128,varying=true), O.Default(""))
    /** Database column screen_id DBType(BIGINT), Default(0) */
    val screenId: Column[Long] = column[Long]("screen_id", O.Default(0L))

    /** Uniqueness Index over (owt,screenId) (database name owt) */
    val index1 = index("owt", (owt, screenId), unique=true)
    /** Uniqueness Index over (owt,screenId) (database name owt_2) */
    val index2 = index("owt_2", (owt, screenId), unique=true)
  }
  /** Collection-like TableQuery object for table BusinessDash */
  lazy val BusinessDash = new TableQuery(tag => new BusinessDash(tag))

  /** Entity class storing rows of table BusinessMonitor
    *  @param id Database column id DBType(BIGINT), AutoInc, PrimaryKey
    *  @param screenId Database column screen_id DBType(BIGINT), Default(0)
    *  @param strategy Database column strategy DBType(INT), Default(0)
    *  @param desc Database column desc DBType(VARCHAR), Length(128,true), Default(0)
    *  @param threshold Database column threshold DBType(INT), Default(0)
    *  @param duration Database column duration DBType(INT), Default(0) */
  case class BusinessMonitorRow(id: Long, screenId: Long = 0L, strategy: Int = 0, desc: String = "0", threshold: Int = 0, duration: Int = 0)
  /** GetResult implicit for fetching BusinessMonitorRow objects using plain SQL queries */
  implicit def GetResultBusinessMonitorRow(implicit e0: GR[Long], e1: GR[Int], e2: GR[String]): GR[BusinessMonitorRow] = GR{
    prs => import prs._
      BusinessMonitorRow.tupled((<<[Long], <<[Long], <<[Int], <<[String], <<[Int], <<[Int]))
  }
  /** Table description of table business_monitor. Objects of this class serve as prototypes for rows in queries. */
  class BusinessMonitor(_tableTag: Tag) extends Table[BusinessMonitorRow](_tableTag, "business_monitor") {
    def * = (id, screenId, strategy, desc, threshold, duration) <> (BusinessMonitorRow.tupled, BusinessMonitorRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (id.?, screenId.?, strategy.?, desc.?, threshold.?, duration.?).shaped.<>({r=>import r._; _1.map(_=> BusinessMonitorRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id DBType(BIGINT), AutoInc, PrimaryKey */
    val id: Column[Long] = column[Long]("id", O.AutoInc, O.PrimaryKey)
    /** Database column screen_id DBType(BIGINT), Default(0) */
    val screenId: Column[Long] = column[Long]("screen_id", O.Default(0L))
    /** Database column strategy DBType(INT), Default(0) */
    val strategy: Column[Int] = column[Int]("strategy", O.Default(0))
    /** Database column desc DBType(VARCHAR), Length(128,true), Default(0) */
    val desc: Column[String] = column[String]("desc", O.Length(128,varying=true), O.Default("0"))
    /** Database column threshold DBType(INT), Default(0) */
    val threshold: Column[Int] = column[Int]("threshold", O.Default(0))
    /** Database column duration DBType(INT), Default(0) */
    val duration: Column[Int] = column[Int]("duration", O.Default(0))

    /** Uniqueness Index over (screenId,strategy) (database name screen_id) */
    val index1 = index("screen_id", (screenId, strategy), unique=true)
  }
  /** Collection-like TableQuery object for table BusinessMonitor */
  lazy val BusinessMonitor = new TableQuery(tag => new BusinessMonitor(tag))

  /** Entity class storing rows of table BusinessMonitorCount
    *  @param id Database column id DBType(BIGINT), AutoInc, PrimaryKey
    *  @param businessMonitorId Database column business_monitor_id DBType(BIGINT), Default(0)
    *  @param count Database column count DBType(INT), Default(0) */
  case class BusinessMonitorCountRow(id: Long, businessMonitorId: Long = 0L, count: Int = 0)
  /** GetResult implicit for fetching BusinessMonitorCountRow objects using plain SQL queries */
  implicit def GetResultBusinessMonitorCountRow(implicit e0: GR[Long], e1: GR[Int]): GR[BusinessMonitorCountRow] = GR{
    prs => import prs._
      BusinessMonitorCountRow.tupled((<<[Long], <<[Long], <<[Int]))
  }
  /** Table description of table business_monitor_count. Objects of this class serve as prototypes for rows in queries. */
  class BusinessMonitorCount(_tableTag: Tag) extends Table[BusinessMonitorCountRow](_tableTag, "business_monitor_count") {
    def * = (id, businessMonitorId, count) <> (BusinessMonitorCountRow.tupled, BusinessMonitorCountRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (id.?, businessMonitorId.?, count.?).shaped.<>({r=>import r._; _1.map(_=> BusinessMonitorCountRow.tupled((_1.get, _2.get, _3.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id DBType(BIGINT), AutoInc, PrimaryKey */
    val id: Column[Long] = column[Long]("id", O.AutoInc, O.PrimaryKey)
    /** Database column business_monitor_id DBType(BIGINT), Default(0) */
    val businessMonitorId: Column[Long] = column[Long]("business_monitor_id", O.Default(0L))
    /** Database column count DBType(INT), Default(0) */
    val count: Column[Int] = column[Int]("count", O.Default(0))

    /** Uniqueness Index over (businessMonitorId) (database name business_monitor_id) */
    val index1 = index("business_monitor_id", businessMonitorId, unique=true)
  }
  /** Collection-like TableQuery object for table BusinessMonitorCount */
  lazy val BusinessMonitorCount = new TableQuery(tag => new BusinessMonitorCount(tag))

  /** Entity class storing rows of table BusinessTriggerSubscribe
    *  @param id Database column id DBType(BIGINT), AutoInc, PrimaryKey
    *  @param businessMonitorId Database column business_monitor_id DBType(BIGINT), Default(0)
    *  @param userId Database column user_id DBType(INT), Default(0)
    *  @param userLogin Database column user_login DBType(VARCHAR), Length(32,true), Default()
    *  @param userName Database column user_name DBType(VARCHAR), Length(32,true), Default()
    *  @param xm Database column xm DBType(TINYINT), Default(0)
    *  @param sms Database column sms DBType(TINYINT), Default(0)
    *  @param email Database column email DBType(TINYINT), Default(0) */
  case class BusinessTriggerSubscribeRow(id: Long, businessMonitorId: Long = 0L, userId: Int = 0, userLogin: String = "", userName: String = "", xm: Byte = 0, sms: Byte = 0, email: Byte = 0)
  /** GetResult implicit for fetching BusinessTriggerSubscribeRow objects using plain SQL queries */
  implicit def GetResultBusinessTriggerSubscribeRow(implicit e0: GR[Long], e1: GR[Int], e2: GR[String], e3: GR[Byte]): GR[BusinessTriggerSubscribeRow] = GR{
    prs => import prs._
      BusinessTriggerSubscribeRow.tupled((<<[Long], <<[Long], <<[Int], <<[String], <<[String], <<[Byte], <<[Byte], <<[Byte]))
  }
  /** Table description of table business_trigger_subscribe. Objects of this class serve as prototypes for rows in queries. */
  class BusinessTriggerSubscribe(_tableTag: Tag) extends Table[BusinessTriggerSubscribeRow](_tableTag, "business_trigger_subscribe") {
    def * = (id, businessMonitorId, userId, userLogin, userName, xm, sms, email) <> (BusinessTriggerSubscribeRow.tupled, BusinessTriggerSubscribeRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (id.?, businessMonitorId.?, userId.?, userLogin.?, userName.?, xm.?, sms.?, email.?).shaped.<>({r=>import r._; _1.map(_=> BusinessTriggerSubscribeRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6.get, _7.get, _8.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id DBType(BIGINT), AutoInc, PrimaryKey */
    val id: Column[Long] = column[Long]("id", O.AutoInc, O.PrimaryKey)
    /** Database column business_monitor_id DBType(BIGINT), Default(0) */
    val businessMonitorId: Column[Long] = column[Long]("business_monitor_id", O.Default(0L))
    /** Database column user_id DBType(INT), Default(0) */
    val userId: Column[Int] = column[Int]("user_id", O.Default(0))
    /** Database column user_login DBType(VARCHAR), Length(32,true), Default() */
    val userLogin: Column[String] = column[String]("user_login", O.Length(32,varying=true), O.Default(""))
    /** Database column user_name DBType(VARCHAR), Length(32,true), Default() */
    val userName: Column[String] = column[String]("user_name", O.Length(32,varying=true), O.Default(""))
    /** Database column xm DBType(TINYINT), Default(0) */
    val xm: Column[Byte] = column[Byte]("xm", O.Default(0))
    /** Database column sms DBType(TINYINT), Default(0) */
    val sms: Column[Byte] = column[Byte]("sms", O.Default(0))
    /** Database column email DBType(TINYINT), Default(0) */
    val email: Column[Byte] = column[Byte]("email", O.Default(0))

    /** Uniqueness Index over (businessMonitorId,userId) (database name business_monitor_id) */
    val index1 = index("business_monitor_id", (businessMonitorId, userId), unique=true)
  }
  /** Collection-like TableQuery object for table BusinessTriggerSubscribe */
  lazy val BusinessTriggerSubscribe = new TableQuery(tag => new BusinessTriggerSubscribe(tag))

  /** Entity class storing rows of table ComponentCoverage
    *  @param id Database column id DBType(INT UNSIGNED), AutoInc, PrimaryKey
    *  @param business Database column business DBType(INT), Default(100)
    *  @param owt Database column owt DBType(VARCHAR), Length(64,true), Default()
    *  @param pdl Database column pdl DBType(VARCHAR), Length(64,true), Default()
    *  @param groupId Database column group_id DBType(VARCHAR), Length(256,true), Default()
    *  @param artifactId Database column artifact_id DBType(VARCHAR), Length(128,true), Default()
    *  @param version Database column version DBType(VARCHAR), Length(128,true), Default()
    *  @param coverage Database column coverage DBType(INT), Default(0)
    *  @param date Database column date DBType(DATE) */
  case class ComponentCoverageRow(id: Int, business: Int = 100, owt: String = "", pdl: String = "", groupId: String = "", artifactId: String = "", version: String = "", coverage: Int = 0, date: java.sql.Date)
  /** GetResult implicit for fetching ComponentCoverageRow objects using plain SQL queries */
  implicit def GetResultComponentCoverageRow(implicit e0: GR[Int], e1: GR[String], e2: GR[java.sql.Date]): GR[ComponentCoverageRow] = GR{
    prs => import prs._
      ComponentCoverageRow.tupled((<<[Int], <<[Int], <<[String], <<[String], <<[String], <<[String], <<[String], <<[Int], <<[java.sql.Date]))
  }
  /** Table description of table component_coverage. Objects of this class serve as prototypes for rows in queries. */
  class ComponentCoverage(_tableTag: Tag) extends Table[ComponentCoverageRow](_tableTag, "component_coverage") {
    def * = (id, business, owt, pdl, groupId, artifactId, version, coverage, date) <> (ComponentCoverageRow.tupled, ComponentCoverageRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (id.?, business.?, owt.?, pdl.?, groupId.?, artifactId.?, version.?, coverage.?, date.?).shaped.<>({r=>import r._; _1.map(_=> ComponentCoverageRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6.get, _7.get, _8.get, _9.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id DBType(INT UNSIGNED), AutoInc, PrimaryKey */
    val id: Column[Int] = column[Int]("id", O.AutoInc, O.PrimaryKey)
    /** Database column business DBType(INT), Default(100) */
    val business: Column[Int] = column[Int]("business", O.Default(100))
    /** Database column owt DBType(VARCHAR), Length(64,true), Default() */
    val owt: Column[String] = column[String]("owt", O.Length(64,varying=true), O.Default(""))
    /** Database column pdl DBType(VARCHAR), Length(64,true), Default() */
    val pdl: Column[String] = column[String]("pdl", O.Length(64,varying=true), O.Default(""))
    /** Database column group_id DBType(VARCHAR), Length(256,true), Default() */
    val groupId: Column[String] = column[String]("group_id", O.Length(256,varying=true), O.Default(""))
    /** Database column artifact_id DBType(VARCHAR), Length(128,true), Default() */
    val artifactId: Column[String] = column[String]("artifact_id", O.Length(128,varying=true), O.Default(""))
    /** Database column version DBType(VARCHAR), Length(128,true), Default() */
    val version: Column[String] = column[String]("version", O.Length(128,varying=true), O.Default(""))
    /** Database column coverage DBType(INT), Default(0) */
    val coverage: Column[Int] = column[Int]("coverage", O.Default(0))
    /** Database column date DBType(DATE) */
    val date: Column[java.sql.Date] = column[java.sql.Date]("date")

    /** Index over (business,owt,pdl) (database name idx_business) */
    val index1 = index("idx_business", (business, owt, pdl))
    /** Index over (groupId,artifactId,version) (database name idx_component) */
    val index2 = index("idx_component", (groupId, artifactId, version))
    /** Index over (date) (database name idx_date) */
    val index3 = index("idx_date", date)
  }
  /** Collection-like TableQuery object for table ComponentCoverage */
  lazy val ComponentCoverage = new TableQuery(tag => new ComponentCoverage(tag))

  /** Entity class storing rows of table ConsumerQuota
    *  @param id Database column id DBType(BIGINT), AutoInc, PrimaryKey
    *  @param appQuotaId Database column app_quota_id DBType(BIGINT), Default(0)
    *  @param consumerAppkey Database column consumer_appkey DBType(VARCHAR), Length(128,true), Default()
    *  @param qpsRatio Database column qps_ratio DBType(DECIMAL)
    *  @param strategy Database column strategy DBType(INT), Default(0)
    *  @param redirect Database column redirect DBType(VARCHAR), Length(2048,true), Default(Some()) */
  case class ConsumerQuotaRow(id: Long, appQuotaId: Long = 0L, consumerAppkey: String = "", qpsRatio: scala.math.BigDecimal, strategy: Int = 0, redirect: Option[String] = Some(""))
  /** GetResult implicit for fetching ConsumerQuotaRow objects using plain SQL queries */
  implicit def GetResultConsumerQuotaRow(implicit e0: GR[Long], e1: GR[String], e2: GR[scala.math.BigDecimal], e3: GR[Int], e4: GR[Option[String]]): GR[ConsumerQuotaRow] = GR{
    prs => import prs._
      ConsumerQuotaRow.tupled((<<[Long], <<[Long], <<[String], <<[scala.math.BigDecimal], <<[Int], <<?[String]))
  }
  /** Table description of table consumer_quota. Objects of this class serve as prototypes for rows in queries. */
  class ConsumerQuota(_tableTag: Tag) extends Table[ConsumerQuotaRow](_tableTag, "consumer_quota") {
    def * = (id, appQuotaId, consumerAppkey, qpsRatio, strategy, redirect) <> (ConsumerQuotaRow.tupled, ConsumerQuotaRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (id.?, appQuotaId.?, consumerAppkey.?, qpsRatio.?, strategy.?, redirect).shaped.<>({r=>import r._; _1.map(_=> ConsumerQuotaRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id DBType(BIGINT), AutoInc, PrimaryKey */
    val id: Column[Long] = column[Long]("id", O.AutoInc, O.PrimaryKey)
    /** Database column app_quota_id DBType(BIGINT), Default(0) */
    val appQuotaId: Column[Long] = column[Long]("app_quota_id", O.Default(0L))
    /** Database column consumer_appkey DBType(VARCHAR), Length(128,true), Default() */
    val consumerAppkey: Column[String] = column[String]("consumer_appkey", O.Length(128,varying=true), O.Default(""))
    /** Database column qps_ratio DBType(DECIMAL) */
    val qpsRatio: Column[scala.math.BigDecimal] = column[scala.math.BigDecimal]("qps_ratio")
    /** Database column strategy DBType(INT), Default(0) */
    val strategy: Column[Int] = column[Int]("strategy", O.Default(0))
    /** Database column redirect DBType(VARCHAR), Length(2048,true), Default(Some()) */
    val redirect: Column[Option[String]] = column[Option[String]]("redirect", O.Length(2048,varying=true), O.Default(Some("")))

    /** Uniqueness Index over (appQuotaId,consumerAppkey) (database name app_quota_id) */
    val index1 = index("app_quota_id", (appQuotaId, consumerAppkey), unique=true)
    /** Index over (appQuotaId) (database name idx_id) */
    val index2 = index("idx_id", appQuotaId)
  }
  /** Collection-like TableQuery object for table ConsumerQuota */
  lazy val ConsumerQuota = new TableQuery(tag => new ConsumerQuota(tag))

  /** Entity class storing rows of table ConsumerQuotaConfig
    *  @param id Database column id DBType(BIGINT), AutoInc, PrimaryKey
    *  @param appQuotaId Database column app_quota_id DBType(BIGINT), Default(0)
    *  @param consumerAppkey Database column consumer_appkey DBType(VARCHAR), Length(128,true), Default()
    *  @param clusterQuota Database column cluster_quota DBType(INT)
    *  @param hostQuota Database column host_quota DBType(INT)
    *  @param strategy Database column strategy DBType(INT), Default(0)
    *  @param redirect Database column redirect DBType(VARCHAR), Length(2048,true), Default(Some())
    *  @param ackUser Database column ack_user DBType(VARCHAR), Length(32,true), Default()
    *  @param ackStatus Database column ack_status DBType(INT), Default(0)
    *  @param ackTime Database column ack_time DBType(INT), Default(0) */
  case class ConsumerQuotaConfigRow(id: Long, appQuotaId: Long = 0L, consumerAppkey: String = "", clusterQuota: Int, hostQuota: Int, strategy: Int = 0, redirect: Option[String] = Some(""), ackUser: String = "", ackStatus: Int = 0, ackTime: Int = 0)
  /** GetResult implicit for fetching ConsumerQuotaConfigRow objects using plain SQL queries */
  implicit def GetResultConsumerQuotaConfigRow(implicit e0: GR[Long], e1: GR[String], e2: GR[Int], e3: GR[Option[String]]): GR[ConsumerQuotaConfigRow] = GR{
    prs => import prs._
      ConsumerQuotaConfigRow.tupled((<<[Long], <<[Long], <<[String], <<[Int], <<[Int], <<[Int], <<?[String], <<[String], <<[Int], <<[Int]))
  }
  /** Table description of table consumer_quota_config. Objects of this class serve as prototypes for rows in queries. */
  class ConsumerQuotaConfig(_tableTag: Tag) extends Table[ConsumerQuotaConfigRow](_tableTag, "consumer_quota_config") {
    def * = (id, appQuotaId, consumerAppkey, clusterQuota, hostQuota, strategy, redirect, ackUser, ackStatus, ackTime) <> (ConsumerQuotaConfigRow.tupled, ConsumerQuotaConfigRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (id.?, appQuotaId.?, consumerAppkey.?, clusterQuota.?, hostQuota.?, strategy.?, redirect, ackUser.?, ackStatus.?, ackTime.?).shaped.<>({r=>import r._; _1.map(_=> ConsumerQuotaConfigRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6.get, _7, _8.get, _9.get, _10.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id DBType(BIGINT), AutoInc, PrimaryKey */
    val id: Column[Long] = column[Long]("id", O.AutoInc, O.PrimaryKey)
    /** Database column app_quota_id DBType(BIGINT), Default(0) */
    val appQuotaId: Column[Long] = column[Long]("app_quota_id", O.Default(0L))
    /** Database column consumer_appkey DBType(VARCHAR), Length(128,true), Default() */
    val consumerAppkey: Column[String] = column[String]("consumer_appkey", O.Length(128,varying=true), O.Default(""))
    /** Database column cluster_quota DBType(INT) */
    val clusterQuota: Column[Int] = column[Int]("cluster_quota")
    /** Database column host_quota DBType(INT) */
    val hostQuota: Column[Int] = column[Int]("host_quota")
    /** Database column strategy DBType(INT), Default(0) */
    val strategy: Column[Int] = column[Int]("strategy", O.Default(0))
    /** Database column redirect DBType(VARCHAR), Length(2048,true), Default(Some()) */
    val redirect: Column[Option[String]] = column[Option[String]]("redirect", O.Length(2048,varying=true), O.Default(Some("")))
    /** Database column ack_user DBType(VARCHAR), Length(32,true), Default() */
    val ackUser: Column[String] = column[String]("ack_user", O.Length(32,varying=true), O.Default(""))
    /** Database column ack_status DBType(INT), Default(0) */
    val ackStatus: Column[Int] = column[Int]("ack_status", O.Default(0))
    /** Database column ack_time DBType(INT), Default(0) */
    val ackTime: Column[Int] = column[Int]("ack_time", O.Default(0))

    /** Uniqueness Index over (appQuotaId,consumerAppkey) (database name app_quota_id) */
    val index1 = index("app_quota_id", (appQuotaId, consumerAppkey), unique=true)
    /** Index over (appQuotaId) (database name idx_id) */
    val index2 = index("idx_id", appQuotaId)
  }
  /** Collection-like TableQuery object for table ConsumerQuotaConfig */
  lazy val ConsumerQuotaConfig = new TableQuery(tag => new ConsumerQuotaConfig(tag))

  /** Entity class storing rows of table ErrorDashboard
    *  @param id Database column id DBType(BIGINT UNSIGNED), AutoInc, PrimaryKey
    *  @param owt Database column owt DBType(VARCHAR), Length(20,true), Default()
    *  @param appkey Database column appkey DBType(VARCHAR), Length(128,true)
    *  @param node Database column node DBType(VARCHAR), Length(256,true), Default()
    *  @param errlogCount Database column errlog_count DBType(INT), Default(0)
    *  @param falconCount Database column falcon_count DBType(INT), Default(0)
    *  @param octoCount Database column octo_count DBType(INT), Default(0)
    *  @param time Database column time DBType(BIGINT), Default(0)
    *  @param createTime Database column create_time DBType(BIGINT), Default(0) */
  case class ErrorDashboardRow(id: Long, owt: String = "", appkey: String, node: String = "", errlogCount: Int = 0, falconCount: Int = 0, octoCount: Int = 0, time: Long = 0L, createTime: Long = 0L)
  /** GetResult implicit for fetching ErrorDashboardRow objects using plain SQL queries */
  implicit def GetResultErrorDashboardRow(implicit e0: GR[Long], e1: GR[String], e2: GR[Int]): GR[ErrorDashboardRow] = GR{
    prs => import prs._
      ErrorDashboardRow.tupled((<<[Long], <<[String], <<[String], <<[String], <<[Int], <<[Int], <<[Int], <<[Long], <<[Long]))
  }
  /** Table description of table error_dashboard. Objects of this class serve as prototypes for rows in queries. */
  class ErrorDashboard(_tableTag: Tag) extends Table[ErrorDashboardRow](_tableTag, "error_dashboard") {
    def * = (id, owt, appkey, node, errlogCount, falconCount, octoCount, time, createTime) <> (ErrorDashboardRow.tupled, ErrorDashboardRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (id.?, owt.?, appkey.?, node.?, errlogCount.?, falconCount.?, octoCount.?, time.?, createTime.?).shaped.<>({r=>import r._; _1.map(_=> ErrorDashboardRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6.get, _7.get, _8.get, _9.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id DBType(BIGINT UNSIGNED), AutoInc, PrimaryKey */
    val id: Column[Long] = column[Long]("id", O.AutoInc, O.PrimaryKey)
    /** Database column owt DBType(VARCHAR), Length(20,true), Default() */
    val owt: Column[String] = column[String]("owt", O.Length(20,varying=true), O.Default(""))
    /** Database column appkey DBType(VARCHAR), Length(128,true) */
    val appkey: Column[String] = column[String]("appkey", O.Length(128,varying=true))
    /** Database column node DBType(VARCHAR), Length(256,true), Default() */
    val node: Column[String] = column[String]("node", O.Length(256,varying=true), O.Default(""))
    /** Database column errlog_count DBType(INT), Default(0) */
    val errlogCount: Column[Int] = column[Int]("errlog_count", O.Default(0))
    /** Database column falcon_count DBType(INT), Default(0) */
    val falconCount: Column[Int] = column[Int]("falcon_count", O.Default(0))
    /** Database column octo_count DBType(INT), Default(0) */
    val octoCount: Column[Int] = column[Int]("octo_count", O.Default(0))
    /** Database column time DBType(BIGINT), Default(0) */
    val time: Column[Long] = column[Long]("time", O.Default(0L))
    /** Database column create_time DBType(BIGINT), Default(0) */
    val createTime: Column[Long] = column[Long]("create_time", O.Default(0L))

    /** Uniqueness Index over (appkey,time) (database name idx_appkey_day) */
    val index1 = index("idx_appkey_day", (appkey, time), unique=true)
    /** Index over (time) (database name idx_time) */
    val index2 = index("idx_time", time)
  }
  /** Collection-like TableQuery object for table ErrorDashboard */
  lazy val ErrorDashboard = new TableQuery(tag => new ErrorDashboard(tag))

  /** Entity class storing rows of table Event
    *  @param id Database column id DBType(BIGINT), AutoInc, PrimaryKey
    *  @param appkey Database column appkey DBType(VARCHAR), Length(128,true), Default()
    *  @param side Database column side DBType(VARCHAR), Length(10,true), Default()
    *  @param spanname Database column spanname DBType(VARCHAR), Length(128,true), Default()
    *  @param item Database column item DBType(VARCHAR), Length(255,true), Default()
    *  @param status Database column status DBType(INT), Default(0)
    *  @param createTime Database column create_time DBType(BIGINT), Default(0)
    *  @param notifyStatus Database column notify_status DBType(INT), Default(1)
    *  @param ackTime Database column ack_time DBType(BIGINT), Default(0)
    *  @param ackUser Database column ack_user DBType(VARCHAR), Length(128,true), Default()
    *  @param message Database column message DBType(VARCHAR), Length(4096,true), Default() */
  case class EventRow(id: Long, appkey: String = "", side: String = "", spanname: String = "", item: String = "", status: Int = 0, createTime: Long = 0L, notifyStatus: Int = 1, ackTime: Long = 0L, ackUser: String = "", message: String = "")
  /** GetResult implicit for fetching EventRow objects using plain SQL queries */
  implicit def GetResultEventRow(implicit e0: GR[Long], e1: GR[String], e2: GR[Int]): GR[EventRow] = GR{
    prs => import prs._
      EventRow.tupled((<<[Long], <<[String], <<[String], <<[String], <<[String], <<[Int], <<[Long], <<[Int], <<[Long], <<[String], <<[String]))
  }
  /** Table description of table event. Objects of this class serve as prototypes for rows in queries. */
  class Event(_tableTag: Tag) extends Table[EventRow](_tableTag, "event") {
    def * = (id, appkey, side, spanname, item, status, createTime, notifyStatus, ackTime, ackUser, message) <> (EventRow.tupled, EventRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (id.?, appkey.?, side.?, spanname.?, item.?, status.?, createTime.?, notifyStatus.?, ackTime.?, ackUser.?, message.?).shaped.<>({r=>import r._; _1.map(_=> EventRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6.get, _7.get, _8.get, _9.get, _10.get, _11.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id DBType(BIGINT), AutoInc, PrimaryKey */
    val id: Column[Long] = column[Long]("id", O.AutoInc, O.PrimaryKey)
    /** Database column appkey DBType(VARCHAR), Length(128,true), Default() */
    val appkey: Column[String] = column[String]("appkey", O.Length(128,varying=true), O.Default(""))
    /** Database column side DBType(VARCHAR), Length(10,true), Default() */
    val side: Column[String] = column[String]("side", O.Length(10,varying=true), O.Default(""))
    /** Database column spanname DBType(VARCHAR), Length(128,true), Default() */
    val spanname: Column[String] = column[String]("spanname", O.Length(128,varying=true), O.Default(""))
    /** Database column item DBType(VARCHAR), Length(255,true), Default() */
    val item: Column[String] = column[String]("item", O.Length(255,varying=true), O.Default(""))
    /** Database column status DBType(INT), Default(0) */
    val status: Column[Int] = column[Int]("status", O.Default(0))
    /** Database column create_time DBType(BIGINT), Default(0) */
    val createTime: Column[Long] = column[Long]("create_time", O.Default(0L))
    /** Database column notify_status DBType(INT), Default(1) */
    val notifyStatus: Column[Int] = column[Int]("notify_status", O.Default(1))
    /** Database column ack_time DBType(BIGINT), Default(0) */
    val ackTime: Column[Long] = column[Long]("ack_time", O.Default(0L))
    /** Database column ack_user DBType(VARCHAR), Length(128,true), Default() */
    val ackUser: Column[String] = column[String]("ack_user", O.Length(128,varying=true), O.Default(""))
    /** Database column message DBType(VARCHAR), Length(4096,true), Default() */
    val message: Column[String] = column[String]("message", O.Length(4096,varying=true), O.Default(""))

    /** Index over (appkey,spanname,createTime) (database name idx_app) */
    val index1 = index("idx_app", (appkey, spanname, createTime))
    /** Index over (createTime) (database name idx_time) */
    val index2 = index("idx_time", createTime)
  }
  /** Collection-like TableQuery object for table Event */
  lazy val Event = new TableQuery(tag => new Event(tag))

  /** Entity class storing rows of table MnsapiAuth
    *  @param id Database column id DBType(INT), AutoInc, PrimaryKey
    *  @param username Database column username DBType(VARCHAR), Length(45,true), Default()
    *  @param token Database column token DBType(VARCHAR), Length(100,true), Default()
    *  @param owtPattern Database column owt_pattern DBType(VARCHAR), Length(1000,true), Default()
    *  @param appkeyPattern Database column pattern_pattern DBType(VARCHAR), Length(1000,true), Default()
    *  @param updateTime Database column create_time DBType(DATETIME) */
  case class MnsapiAuthRow(id: Int, username: String = "", token: String = "", owtPattern: String = "", appkeyPattern: String = "", updateTime: java.sql.Timestamp)
  /** GetResult implicit for fetching MnsapiAuthRow objects using plain SQL queries */
  implicit def GetResultMnsapiAuthRow(implicit e0: GR[Int], e1: GR[String], e2: GR[java.sql.Timestamp]): GR[MnsapiAuthRow] = GR{
    prs => import prs._
      MnsapiAuthRow.tupled((<<[Int], <<[String], <<[String], <<[String], <<[String], <<[java.sql.Timestamp]))
  }
  /** Table description of table mnsapi_auth. Objects of this class serve as prototypes for rows in queries. */
  class MnsapiAuth(_tableTag: Tag) extends Table[MnsapiAuthRow](_tableTag, "mnsapi_auth") {
    def * = (id, username, token, owtPattern, appkeyPattern, updateTime) <> (MnsapiAuthRow.tupled, MnsapiAuthRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (id.?, username.?, token.?, owtPattern.?, appkeyPattern.?, updateTime.?).shaped.<>({r=>import r._; _1.map(_=> MnsapiAuthRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id DBType(INT), AutoInc, PrimaryKey */
    val id: Column[Int] = column[Int]("id", O.AutoInc, O.PrimaryKey)
    /** Database column username DBType(VARCHAR), Length(45,true), Default() */
    val username: Column[String] = column[String]("username", O.Length(45,varying=true), O.Default(""))
    /** Database column token DBType(VARCHAR), Length(100,true), Default() */
    val token: Column[String] = column[String]("token", O.Length(100,varying=true), O.Default(""))
    /** Database column owt_pattern DBType(VARCHAR), Length(1000,true), Default() */
    val owtPattern: Column[String] = column[String]("owt_pattern", O.Length(1000,varying=true), O.Default(""))
    /** Database column appkey_pattern DBType(VARCHAR), Length(1000,true), Default() */
    val appkeyPattern: Column[String] = column[String]("appkey_pattern", O.Length(1000,varying=true), O.Default(""))
    /** Database column update_time DBType(DATETIME) */
    val updateTime: Column[java.sql.Timestamp] = column[java.sql.Timestamp]("update_time")
  }
  /** Collection-like TableQuery object for table MnsapiAuth */
  lazy val MnsapiAuth = new TableQuery(tag => new MnsapiAuth(tag))

  /** Entity class storing rows of table Oauth2Client
    *  @param id Database column id DBType(BIGINT), AutoInc, PrimaryKey
    *  @param name Database column name DBType(VARCHAR), Length(1024,true), Default()
    *  @param clientId Database column client_id DBType(VARCHAR), Length(128,true), Default()
    *  @param secret Database column secret DBType(VARCHAR), Length(128,true), Default()
    *  @param uri Database column uri DBType(VARCHAR), Length(1024,true), Default() */
  case class Oauth2ClientRow(id: Long, name: String = "", clientId: String = "", secret: String = "", uri: String = "")
  /** GetResult implicit for fetching Oauth2ClientRow objects using plain SQL queries */
  implicit def GetResultOauth2ClientRow(implicit e0: GR[Long], e1: GR[String]): GR[Oauth2ClientRow] = GR{
    prs => import prs._
      Oauth2ClientRow.tupled((<<[Long], <<[String], <<[String], <<[String], <<[String]))
  }
  /** Table description of table oauth2_client. Objects of this class serve as prototypes for rows in queries. */
  class Oauth2Client(_tableTag: Tag) extends Table[Oauth2ClientRow](_tableTag, "oauth2_client") {
    def * = (id, name, clientId, secret, uri) <> (Oauth2ClientRow.tupled, Oauth2ClientRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (id.?, name.?, clientId.?, secret.?, uri.?).shaped.<>({r=>import r._; _1.map(_=> Oauth2ClientRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id DBType(BIGINT), AutoInc, PrimaryKey */
    val id: Column[Long] = column[Long]("id", O.AutoInc, O.PrimaryKey)
    /** Database column name DBType(VARCHAR), Length(1024,true), Default() */
    val name: Column[String] = column[String]("name", O.Length(1024,varying=true), O.Default(""))
    /** Database column client_id DBType(VARCHAR), Length(128,true), Default() */
    val clientId: Column[String] = column[String]("client_id", O.Length(128,varying=true), O.Default(""))
    /** Database column secret DBType(VARCHAR), Length(128,true), Default() */
    val secret: Column[String] = column[String]("secret", O.Length(128,varying=true), O.Default(""))
    /** Database column uri DBType(VARCHAR), Length(1024,true), Default() */
    val uri: Column[String] = column[String]("uri", O.Length(1024,varying=true), O.Default(""))
  }
  /** Collection-like TableQuery object for table Oauth2Client */
  lazy val Oauth2Client = new TableQuery(tag => new Oauth2Client(tag))

  /** Entity class storing rows of table Oauth2Token
    *  @param id Database column id DBType(BIGINT), AutoInc, PrimaryKey
    *  @param code Database column code DBType(VARCHAR), Length(128,true), Default()
    *  @param token Database column token DBType(VARCHAR), Length(128,true), Default()
    *  @param userId Database column user_id DBType(BIGINT), Default(0)
    *  @param clientId Database column client_id DBType(VARCHAR), Length(128,true), Default()
    *  @param scope Database column scope DBType(VARCHAR), Length(128,true), Default()
    *  @param codeCreatetime Database column code_createtime DBType(BIGINT), Default(0)
    *  @param status Database column status DBType(INT), Default(0) */
  case class Oauth2TokenRow(id: Long, code: String = "", token: String = "", userId: Long = 0L, clientId: String = "", scope: String = "", codeCreatetime: Long = 0L, status: Int = 0)
  /** GetResult implicit for fetching Oauth2TokenRow objects using plain SQL queries */
  implicit def GetResultOauth2TokenRow(implicit e0: GR[Long], e1: GR[String], e2: GR[Int]): GR[Oauth2TokenRow] = GR{
    prs => import prs._
      Oauth2TokenRow.tupled((<<[Long], <<[String], <<[String], <<[Long], <<[String], <<[String], <<[Long], <<[Int]))
  }
  /** Table description of table oauth2_token. Objects of this class serve as prototypes for rows in queries. */
  class Oauth2Token(_tableTag: Tag) extends Table[Oauth2TokenRow](_tableTag, "oauth2_token") {
    def * = (id, code, token, userId, clientId, scope, codeCreatetime, status) <> (Oauth2TokenRow.tupled, Oauth2TokenRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (id.?, code.?, token.?, userId.?, clientId.?, scope.?, codeCreatetime.?, status.?).shaped.<>({r=>import r._; _1.map(_=> Oauth2TokenRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6.get, _7.get, _8.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id DBType(BIGINT), AutoInc, PrimaryKey */
    val id: Column[Long] = column[Long]("id", O.AutoInc, O.PrimaryKey)
    /** Database column code DBType(VARCHAR), Length(128,true), Default() */
    val code: Column[String] = column[String]("code", O.Length(128,varying=true), O.Default(""))
    /** Database column token DBType(VARCHAR), Length(128,true), Default() */
    val token: Column[String] = column[String]("token", O.Length(128,varying=true), O.Default(""))
    /** Database column user_id DBType(BIGINT), Default(0) */
    val userId: Column[Long] = column[Long]("user_id", O.Default(0L))
    /** Database column client_id DBType(VARCHAR), Length(128,true), Default() */
    val clientId: Column[String] = column[String]("client_id", O.Length(128,varying=true), O.Default(""))
    /** Database column scope DBType(VARCHAR), Length(128,true), Default() */
    val scope: Column[String] = column[String]("scope", O.Length(128,varying=true), O.Default(""))
    /** Database column code_createtime DBType(BIGINT), Default(0) */
    val codeCreatetime: Column[Long] = column[Long]("code_createtime", O.Default(0L))
    /** Database column status DBType(INT), Default(0) */
    val status: Column[Int] = column[Int]("status", O.Default(0))
  }
  /** Collection-like TableQuery object for table Oauth2Token */
  lazy val Oauth2Token = new TableQuery(tag => new Oauth2Token(tag))

  /** Entity class storing rows of table OctoJob
    *  @param id Database column id DBType(BIGINT), AutoInc, PrimaryKey
    *  @param appkey Database column appkey DBType(VARCHAR), Length(128,true), Default()
    *  @param identifier Database column identifier DBType(VARCHAR), Length(256,true), Default()
    *  @param job Database column job DBType(VARCHAR), Length(256,true), Default()
    *  @param stime Database column stime DBType(BIGINT), Default(0)
    *  @param cost Database column cost DBType(BIGINT), Default(0)
    *  @param content Database column content DBType(MEDIUMTEXT), Length(16777215,true) */
  case class OctoJobRow(id: Long, appkey: String = "", identifier: String = "", job: String = "", stime: Long = 0L, cost: Long = 0L, content: String)
  /** GetResult implicit for fetching OctoJobRow objects using plain SQL queries */
  implicit def GetResultOctoJobRow(implicit e0: GR[Long], e1: GR[String]): GR[OctoJobRow] = GR{
    prs => import prs._
      OctoJobRow.tupled((<<[Long], <<[String], <<[String], <<[String], <<[Long], <<[Long], <<[String]))
  }
  /** Table description of table octo_job. Objects of this class serve as prototypes for rows in queries. */
  class OctoJob(_tableTag: Tag) extends Table[OctoJobRow](_tableTag, "octo_job") {
    def * = (id, appkey, identifier, job, stime, cost, content) <> (OctoJobRow.tupled, OctoJobRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (id.?, appkey.?, identifier.?, job.?, stime.?, cost.?, content.?).shaped.<>({r=>import r._; _1.map(_=> OctoJobRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6.get, _7.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id DBType(BIGINT), AutoInc, PrimaryKey */
    val id: Column[Long] = column[Long]("id", O.AutoInc, O.PrimaryKey)
    /** Database column appkey DBType(VARCHAR), Length(128,true), Default() */
    val appkey: Column[String] = column[String]("appkey", O.Length(128,varying=true), O.Default(""))
    /** Database column identifier DBType(VARCHAR), Length(256,true), Default() */
    val identifier: Column[String] = column[String]("identifier", O.Length(256,varying=true), O.Default(""))
    /** Database column job DBType(VARCHAR), Length(256,true), Default() */
    val job: Column[String] = column[String]("job", O.Length(256,varying=true), O.Default(""))
    /** Database column stime DBType(BIGINT), Default(0) */
    val stime: Column[Long] = column[Long]("stime", O.Default(0L))
    /** Database column cost DBType(BIGINT), Default(0) */
    val cost: Column[Long] = column[Long]("cost", O.Default(0L))
    /** Database column content DBType(MEDIUMTEXT), Length(16777215,true) */
    val content: Column[String] = column[String]("content", O.Length(16777215,varying=true))
  }
  /** Collection-like TableQuery object for table OctoJob */
  lazy val OctoJob = new TableQuery(tag => new OctoJob(tag))

  /** Entity class storing rows of table OctoLog
    *  @param id Database column id DBType(BIGINT), AutoInc, PrimaryKey
    *  @param appkey Database column appkey DBType(VARCHAR), Length(128,true), Default()
    *  @param time Database column time DBType(BIGINT), Default(0)
    *  @param level Database column level DBType(INT), Default(0)
    *  @param category Database column category DBType(VARCHAR), Length(128,true), Default()
    *  @param content Database column content DBType(MEDIUMTEXT), Length(16777215,true) */
  case class OctoLogRow(id: Long, appkey: String = "", time: Long = 0L, level: Int = 0, category: String = "", content: String)
  /** GetResult implicit for fetching OctoLogRow objects using plain SQL queries */
  implicit def GetResultOctoLogRow(implicit e0: GR[Long], e1: GR[String], e2: GR[Int]): GR[OctoLogRow] = GR{
    prs => import prs._
      OctoLogRow.tupled((<<[Long], <<[String], <<[Long], <<[Int], <<[String], <<[String]))
  }
  /** Table description of table octo_log. Objects of this class serve as prototypes for rows in queries. */
  class OctoLog(_tableTag: Tag) extends Table[OctoLogRow](_tableTag, "octo_log") {
    def * = (id, appkey, time, level, category, content) <> (OctoLogRow.tupled, OctoLogRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (id.?, appkey.?, time.?, level.?, category.?, content.?).shaped.<>({r=>import r._; _1.map(_=> OctoLogRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id DBType(BIGINT), AutoInc, PrimaryKey */
    val id: Column[Long] = column[Long]("id", O.AutoInc, O.PrimaryKey)
    /** Database column appkey DBType(VARCHAR), Length(128,true), Default() */
    val appkey: Column[String] = column[String]("appkey", O.Length(128,varying=true), O.Default(""))
    /** Database column time DBType(BIGINT), Default(0) */
    val time: Column[Long] = column[Long]("time", O.Default(0L))
    /** Database column level DBType(INT), Default(0) */
    val level: Column[Int] = column[Int]("level", O.Default(0))
    /** Database column category DBType(VARCHAR), Length(128,true), Default() */
    val category: Column[String] = column[String]("category", O.Length(128,varying=true), O.Default(""))
    /** Database column content DBType(MEDIUMTEXT), Length(16777215,true) */
    val content: Column[String] = column[String]("content", O.Length(16777215,varying=true))

    /** Index over (time) (database name idx_time) */
    val index1 = index("idx_time", time)
  }
  /** Collection-like TableQuery object for table OctoLog */
  lazy val OctoLog = new TableQuery(tag => new OctoLog(tag))

  /** Entity class storing rows of table OswatchLog
    *  @param id Database column id DBType(BIGINT), AutoInc, PrimaryKey
    *  @param providerappkey Database column providerappkey DBType(VARCHAR), Length(128,true), Default()
    *  @param env Database column env DBType(INT), Default(0)
    *  @param method Database column method DBType(VARCHAR), Length(255,true), Default()
    *  @param qpsCapacity Database column qps_capacity DBType(BIGINT), Default(0)
    *  @param consumerappkey Database column consumerappkey DBType(VARCHAR), Length(128,true), Default()
    *  @param consumercurrentqps Database column consumerCurrentQPS DBType(BIGINT), Default(0)
    *  @param consumerquotaqps Database column consumerQuotaQPS DBType(BIGINT), Default(0)
    *  @param degradestrategy Database column degradeStrategy DBType(INT), Default(0)
    *  @param degradeend Database column degradeEnd DBType(INT), Default(0)
    *  @param status Database column status DBType(INT), Default(0)
    *  @param time Database column time DBType(BIGINT), Default(0) */
  case class OswatchLogRow(id: Long, providerappkey: String = "", env: Int = 0, method: String = "", qpsCapacity: Long = 0L, consumerappkey: String = "", consumercurrentqps: Long = 0L, consumerquotaqps: Long = 0L, degradestrategy: Int = 0, degradeend: Int = 0, status: Int = 0, time: Long = 0L)
  /** GetResult implicit for fetching OswatchLogRow objects using plain SQL queries */
  implicit def GetResultOswatchLogRow(implicit e0: GR[Long], e1: GR[String], e2: GR[Int]): GR[OswatchLogRow] = GR{
    prs => import prs._
      OswatchLogRow.tupled((<<[Long], <<[String], <<[Int], <<[String], <<[Long], <<[String], <<[Long], <<[Long], <<[Int], <<[Int], <<[Int], <<[Long]))
  }
  /** Table description of table oswatch_log. Objects of this class serve as prototypes for rows in queries. */
  class OswatchLog(_tableTag: Tag) extends Table[OswatchLogRow](_tableTag, "oswatch_log") {
    def * = (id, providerappkey, env, method, qpsCapacity, consumerappkey, consumercurrentqps, consumerquotaqps, degradestrategy, degradeend, status, time) <> (OswatchLogRow.tupled, OswatchLogRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (id.?, providerappkey.?, env.?, method.?, qpsCapacity.?, consumerappkey.?, consumercurrentqps.?, consumerquotaqps.?, degradestrategy.?, degradeend.?, status.?, time.?).shaped.<>({r=>import r._; _1.map(_=> OswatchLogRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6.get, _7.get, _8.get, _9.get, _10.get, _11.get, _12.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id DBType(BIGINT), AutoInc, PrimaryKey */
    val id: Column[Long] = column[Long]("id", O.AutoInc, O.PrimaryKey)
    /** Database column providerappkey DBType(VARCHAR), Length(128,true), Default() */
    val providerappkey: Column[String] = column[String]("providerappkey", O.Length(128,varying=true), O.Default(""))
    /** Database column env DBType(INT), Default(0) */
    val env: Column[Int] = column[Int]("env", O.Default(0))
    /** Database column method DBType(VARCHAR), Length(255,true), Default() */
    val method: Column[String] = column[String]("method", O.Length(255,varying=true), O.Default(""))
    /** Database column qps_capacity DBType(BIGINT), Default(0) */
    val qpsCapacity: Column[Long] = column[Long]("qps_capacity", O.Default(0L))
    /** Database column consumerappkey DBType(VARCHAR), Length(128,true), Default() */
    val consumerappkey: Column[String] = column[String]("consumerappkey", O.Length(128,varying=true), O.Default(""))
    /** Database column consumerCurrentQPS DBType(BIGINT), Default(0) */
    val consumercurrentqps: Column[Long] = column[Long]("consumerCurrentQPS", O.Default(0L))
    /** Database column consumerQuotaQPS DBType(BIGINT), Default(0) */
    val consumerquotaqps: Column[Long] = column[Long]("consumerQuotaQPS", O.Default(0L))
    /** Database column degradeStrategy DBType(INT), Default(0) */
    val degradestrategy: Column[Int] = column[Int]("degradeStrategy", O.Default(0))
    /** Database column degradeEnd DBType(INT), Default(0) */
    val degradeend: Column[Int] = column[Int]("degradeEnd", O.Default(0))
    /** Database column status DBType(INT), Default(0) */
    val status: Column[Int] = column[Int]("status", O.Default(0))
    /** Database column time DBType(BIGINT), Default(0) */
    val time: Column[Long] = column[Long]("time", O.Default(0L))
  }
  /** Collection-like TableQuery object for table OswatchLog */
  lazy val OswatchLog = new TableQuery(tag => new OswatchLog(tag))

  /** Entity class storing rows of table PerfDay
    *  @param id Database column id DBType(BIGINT), AutoInc, PrimaryKey
    *  @param ts Database column ts DBType(INT), Default(0)
    *  @param mode Database column mode DBType(VARCHAR), Length(32,true), Default()
    *  @param tags Database column tags DBType(VARCHAR), Length(64,true), Default()
    *  @param appkey Database column appkey DBType(VARCHAR), Length(128,true), Default()
    *  @param appkeyCategory Database column appkey_category DBType(VARCHAR), Length(8,true), Default(thrift)
    *  @param spanname Database column spanname DBType(VARCHAR), Length(255,true), Default()
    *  @param localhost Database column localhost DBType(VARCHAR), Length(128,true), Default()
    *  @param remoteApp Database column remote_app DBType(VARCHAR), Length(128,true), Default()
    *  @param remoteHost Database column remote_host DBType(VARCHAR), Length(128,true), Default()
    *  @param status Database column status DBType(INT), Default(0)
    *  @param count Database column count DBType(BIGINT), Default(0)
    *  @param qps Database column qps DBType(DECIMAL), Default(None)
    *  @param upper50 Database column upper_50 DBType(DECIMAL), Default(None)
    *  @param upper90 Database column upper_90 DBType(DECIMAL), Default(None)
    *  @param upper95 Database column upper_95 DBType(DECIMAL), Default(None)
    *  @param upper99 Database column upper_99 DBType(DECIMAL), Default(None)
    *  @param upper Database column upper DBType(DECIMAL), Default(None) */
  case class PerfDayRow(id: Long, ts: Int = 0, mode: String = "", tags: String = "", appkey: String = "", appkeyCategory: String = "thrift", spanname: String = "", localhost: String = "", remoteApp: String = "", remoteHost: String = "", status: Int = 0, count: Long = 0L, qps: Option[scala.math.BigDecimal] = None, upper50: Option[scala.math.BigDecimal] = None, upper90: Option[scala.math.BigDecimal] = None, upper95: Option[scala.math.BigDecimal] = None, upper99: Option[scala.math.BigDecimal] = None, upper: Option[scala.math.BigDecimal] = None)
  /** GetResult implicit for fetching PerfDayRow objects using plain SQL queries */
  implicit def GetResultPerfDayRow(implicit e0: GR[Long], e1: GR[Int], e2: GR[String], e3: GR[Option[scala.math.BigDecimal]]): GR[PerfDayRow] = GR{
    prs => import prs._
      PerfDayRow.tupled((<<[Long], <<[Int], <<[String], <<[String], <<[String], <<[String], <<[String], <<[String], <<[String], <<[String], <<[Int], <<[Long], <<?[scala.math.BigDecimal], <<?[scala.math.BigDecimal], <<?[scala.math.BigDecimal], <<?[scala.math.BigDecimal], <<?[scala.math.BigDecimal], <<?[scala.math.BigDecimal]))
  }
  /** Table description of table perf_day. Objects of this class serve as prototypes for rows in queries. */
  class PerfDay(_tableTag: Tag) extends Table[PerfDayRow](_tableTag, "perf_day") {
    def * = (id, ts, mode, tags, appkey, appkeyCategory, spanname, localhost, remoteApp, remoteHost, status, count, qps, upper50, upper90, upper95, upper99, upper) <> (PerfDayRow.tupled, PerfDayRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (id.?, ts.?, mode.?, tags.?, appkey.?, appkeyCategory.?, spanname.?, localhost.?, remoteApp.?, remoteHost.?, status.?, count.?, qps, upper50, upper90, upper95, upper99, upper).shaped.<>({r=>import r._; _1.map(_=> PerfDayRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6.get, _7.get, _8.get, _9.get, _10.get, _11.get, _12.get, _13, _14, _15, _16, _17, _18)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id DBType(BIGINT), AutoInc, PrimaryKey */
    val id: Column[Long] = column[Long]("id", O.AutoInc, O.PrimaryKey)
    /** Database column ts DBType(INT), Default(0) */
    val ts: Column[Int] = column[Int]("ts", O.Default(0))
    /** Database column mode DBType(VARCHAR), Length(32,true), Default() */
    val mode: Column[String] = column[String]("mode", O.Length(32,varying=true), O.Default(""))
    /** Database column tags DBType(VARCHAR), Length(64,true), Default() */
    val tags: Column[String] = column[String]("tags", O.Length(64,varying=true), O.Default(""))
    /** Database column appkey DBType(VARCHAR), Length(128,true), Default() */
    val appkey: Column[String] = column[String]("appkey", O.Length(128,varying=true), O.Default(""))
    /** Database column appkey_category DBType(VARCHAR), Length(8,true), Default(thrift) */
    val appkeyCategory: Column[String] = column[String]("appkey_category", O.Length(8,varying=true), O.Default("thrift"))
    /** Database column spanname DBType(VARCHAR), Length(255,true), Default() */
    val spanname: Column[String] = column[String]("spanname", O.Length(255,varying=true), O.Default(""))
    /** Database column localhost DBType(VARCHAR), Length(128,true), Default() */
    val localhost: Column[String] = column[String]("localhost", O.Length(128,varying=true), O.Default(""))
    /** Database column remote_app DBType(VARCHAR), Length(128,true), Default() */
    val remoteApp: Column[String] = column[String]("remote_app", O.Length(128,varying=true), O.Default(""))
    /** Database column remote_host DBType(VARCHAR), Length(128,true), Default() */
    val remoteHost: Column[String] = column[String]("remote_host", O.Length(128,varying=true), O.Default(""))
    /** Database column status DBType(INT), Default(0) */
    val status: Column[Int] = column[Int]("status", O.Default(0))
    /** Database column count DBType(BIGINT), Default(0) */
    val count: Column[Long] = column[Long]("count", O.Default(0L))
    /** Database column qps DBType(DECIMAL), Default(None) */
    val qps: Column[Option[scala.math.BigDecimal]] = column[Option[scala.math.BigDecimal]]("qps", O.Default(None))
    /** Database column upper_50 DBType(DECIMAL), Default(None) */
    val upper50: Column[Option[scala.math.BigDecimal]] = column[Option[scala.math.BigDecimal]]("upper_50", O.Default(None))
    /** Database column upper_90 DBType(DECIMAL), Default(None) */
    val upper90: Column[Option[scala.math.BigDecimal]] = column[Option[scala.math.BigDecimal]]("upper_90", O.Default(None))
    /** Database column upper_95 DBType(DECIMAL), Default(None) */
    val upper95: Column[Option[scala.math.BigDecimal]] = column[Option[scala.math.BigDecimal]]("upper_95", O.Default(None))
    /** Database column upper_99 DBType(DECIMAL), Default(None) */
    val upper99: Column[Option[scala.math.BigDecimal]] = column[Option[scala.math.BigDecimal]]("upper_99", O.Default(None))
    /** Database column upper DBType(DECIMAL), Default(None) */
    val upper: Column[Option[scala.math.BigDecimal]] = column[Option[scala.math.BigDecimal]]("upper", O.Default(None))

    /** Index over (ts) (database name idx_time) */
    val index1 = index("idx_time", ts)
    /** Index over (ts,mode,tags,spanname,localhost,appkey) (database name idx_ts_mode_tags_spanname) */
    val index2 = index("idx_ts_mode_tags_spanname", (ts, mode, tags, spanname, localhost, appkey))
  }
  /** Collection-like TableQuery object for table PerfDay */
  lazy val PerfDay = new TableQuery(tag => new PerfDay(tag))

  /** Entity class storing rows of table PerfHour
    *  @param id Database column id DBType(BIGINT), AutoInc, PrimaryKey
    *  @param ts Database column ts DBType(INT), Default(0)
    *  @param mode Database column mode DBType(VARCHAR), Length(32,true), Default()
    *  @param tags Database column tags DBType(VARCHAR), Length(64,true), Default()
    *  @param appkey Database column appkey DBType(VARCHAR), Length(128,true), Default()
    *  @param spanname Database column spanname DBType(VARCHAR), Length(255,true), Default()
    *  @param localhost Database column localhost DBType(VARCHAR), Length(128,true), Default()
    *  @param remoteApp Database column remote_app DBType(VARCHAR), Length(128,true), Default()
    *  @param remoteHost Database column remote_host DBType(VARCHAR), Length(128,true), Default()
    *  @param status Database column status DBType(INT), Default(0)
    *  @param count Database column count DBType(BIGINT), Default(0)
    *  @param qps Database column qps DBType(DECIMAL), Default(None)
    *  @param upper50 Database column upper_50 DBType(DECIMAL), Default(None)
    *  @param upper90 Database column upper_90 DBType(DECIMAL), Default(None)
    *  @param upper95 Database column upper_95 DBType(DECIMAL), Default(None)
    *  @param upper99 Database column upper_99 DBType(DECIMAL), Default(None)
    *  @param upper Database column upper DBType(DECIMAL), Default(None) */
  case class PerfHourRow(id: Long, ts: Int = 0, mode: String = "", tags: String = "", appkey: String = "", spanname: String = "", localhost: String = "", remoteApp: String = "", remoteHost: String = "", status: Int = 0, count: Long = 0L, qps: Option[scala.math.BigDecimal] = None, upper50: Option[scala.math.BigDecimal] = None, upper90: Option[scala.math.BigDecimal] = None, upper95: Option[scala.math.BigDecimal] = None, upper99: Option[scala.math.BigDecimal] = None, upper: Option[scala.math.BigDecimal] = None)
  /** GetResult implicit for fetching PerfHourRow objects using plain SQL queries */
  implicit def GetResultPerfHourRow(implicit e0: GR[Long], e1: GR[Int], e2: GR[String], e3: GR[Option[scala.math.BigDecimal]]): GR[PerfHourRow] = GR{
    prs => import prs._
      PerfHourRow.tupled((<<[Long], <<[Int], <<[String], <<[String], <<[String], <<[String], <<[String], <<[String], <<[String], <<[Int], <<[Long], <<?[scala.math.BigDecimal], <<?[scala.math.BigDecimal], <<?[scala.math.BigDecimal], <<?[scala.math.BigDecimal], <<?[scala.math.BigDecimal], <<?[scala.math.BigDecimal]))
  }
  /** Table description of table perf_hour. Objects of this class serve as prototypes for rows in queries. */
  class PerfHour(_tableTag: Tag) extends Table[PerfHourRow](_tableTag, "perf_hour") {
    def * = (id, ts, mode, tags, appkey, spanname, localhost, remoteApp, remoteHost, status, count, qps, upper50, upper90, upper95, upper99, upper) <> (PerfHourRow.tupled, PerfHourRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (id.?, ts.?, mode.?, tags.?, appkey.?, spanname.?, localhost.?, remoteApp.?, remoteHost.?, status.?, count.?, qps, upper50, upper90, upper95, upper99, upper).shaped.<>({r=>import r._; _1.map(_=> PerfHourRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6.get, _7.get, _8.get, _9.get, _10.get, _11.get, _12, _13, _14, _15, _16, _17)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id DBType(BIGINT), AutoInc, PrimaryKey */
    val id: Column[Long] = column[Long]("id", O.AutoInc, O.PrimaryKey)
    /** Database column ts DBType(INT), Default(0) */
    val ts: Column[Int] = column[Int]("ts", O.Default(0))
    /** Database column mode DBType(VARCHAR), Length(32,true), Default() */
    val mode: Column[String] = column[String]("mode", O.Length(32,varying=true), O.Default(""))
    /** Database column tags DBType(VARCHAR), Length(64,true), Default() */
    val tags: Column[String] = column[String]("tags", O.Length(64,varying=true), O.Default(""))
    /** Database column appkey DBType(VARCHAR), Length(128,true), Default() */
    val appkey: Column[String] = column[String]("appkey", O.Length(128,varying=true), O.Default(""))
    /** Database column spanname DBType(VARCHAR), Length(255,true), Default() */
    val spanname: Column[String] = column[String]("spanname", O.Length(255,varying=true), O.Default(""))
    /** Database column localhost DBType(VARCHAR), Length(128,true), Default() */
    val localhost: Column[String] = column[String]("localhost", O.Length(128,varying=true), O.Default(""))
    /** Database column remote_app DBType(VARCHAR), Length(128,true), Default() */
    val remoteApp: Column[String] = column[String]("remote_app", O.Length(128,varying=true), O.Default(""))
    /** Database column remote_host DBType(VARCHAR), Length(128,true), Default() */
    val remoteHost: Column[String] = column[String]("remote_host", O.Length(128,varying=true), O.Default(""))
    /** Database column status DBType(INT), Default(0) */
    val status: Column[Int] = column[Int]("status", O.Default(0))
    /** Database column count DBType(BIGINT), Default(0) */
    val count: Column[Long] = column[Long]("count", O.Default(0L))
    /** Database column qps DBType(DECIMAL), Default(None) */
    val qps: Column[Option[scala.math.BigDecimal]] = column[Option[scala.math.BigDecimal]]("qps", O.Default(None))
    /** Database column upper_50 DBType(DECIMAL), Default(None) */
    val upper50: Column[Option[scala.math.BigDecimal]] = column[Option[scala.math.BigDecimal]]("upper_50", O.Default(None))
    /** Database column upper_90 DBType(DECIMAL), Default(None) */
    val upper90: Column[Option[scala.math.BigDecimal]] = column[Option[scala.math.BigDecimal]]("upper_90", O.Default(None))
    /** Database column upper_95 DBType(DECIMAL), Default(None) */
    val upper95: Column[Option[scala.math.BigDecimal]] = column[Option[scala.math.BigDecimal]]("upper_95", O.Default(None))
    /** Database column upper_99 DBType(DECIMAL), Default(None) */
    val upper99: Column[Option[scala.math.BigDecimal]] = column[Option[scala.math.BigDecimal]]("upper_99", O.Default(None))
    /** Database column upper DBType(DECIMAL), Default(None) */
    val upper: Column[Option[scala.math.BigDecimal]] = column[Option[scala.math.BigDecimal]]("upper", O.Default(None))

    /** Index over (ts) (database name idx_time) */
    val index1 = index("idx_time", ts)
  }
  /** Collection-like TableQuery object for table PerfHour */
  lazy val PerfHour = new TableQuery(tag => new PerfHour(tag))

  /** Entity class storing rows of table PerfIndicator
    *  @param id Database column id DBType(BIGINT), AutoInc, PrimaryKey
    *  @param appkey Database column appkey DBType(VARCHAR), Length(128,true)
    *  @param `type` Database column type DBType(BIT)
    *  @param spanname Database column spanname DBType(VARCHAR), Length(128,true)
    *  @param count Database column count DBType(BIGINT), Default(0)
    *  @param successCount Database column success_count DBType(BIGINT), Default(0)
    *  @param exceptionCount Database column exception_count DBType(BIGINT), Default(0)
    *  @param timeoutCount Database column timeout_count DBType(BIGINT), Default(0)
    *  @param dropCount Database column drop_count DBType(BIGINT), Default(0)
    *  @param qps Database column qps DBType(DOUBLE), Default(0.0)
    *  @param tp50 Database column tp50 DBType(INT), Default(0)
    *  @param tp90 Database column tp90 DBType(INT), Default(0)
    *  @param tp95 Database column tp95 DBType(INT), Default(0)
    *  @param tp99 Database column tp99 DBType(INT), Default(0)
    *  @param costMax Database column cost_max DBType(INT), Default(0)
    *  @param date Database column date DBType(DATE)
    *  @param createTime Database column create_time DBType(BIGINT), Default(0) */
  case class PerfIndicatorRow(id: Long, appkey: String, `type`: Boolean, spanname: String, count: Long = 0L, successCount: Long = 0L, exceptionCount: Long = 0L, timeoutCount: Long = 0L, dropCount: Long = 0L, qps: Double = 0.0, tp50: Int = 0, tp90: Int = 0, tp95: Int = 0, tp99: Int = 0, costMax: Int = 0, date: java.sql.Date, createTime: Long = 0L)
  /** GetResult implicit for fetching PerfIndicatorRow objects using plain SQL queries */
  implicit def GetResultPerfIndicatorRow(implicit e0: GR[Long], e1: GR[String], e2: GR[Boolean], e3: GR[Double], e4: GR[Int], e5: GR[java.sql.Date]): GR[PerfIndicatorRow] = GR{
    prs => import prs._
      PerfIndicatorRow.tupled((<<[Long], <<[String], <<[Boolean], <<[String], <<[Long], <<[Long], <<[Long], <<[Long], <<[Long], <<[Double], <<[Int], <<[Int], <<[Int], <<[Int], <<[Int], <<[java.sql.Date], <<[Long]))
  }
  /** Table description of table perf_indicator. Objects of this class serve as prototypes for rows in queries.
    *  NOTE: The following names collided with Scala keywords and were escaped: type */
  class PerfIndicator(_tableTag: Tag) extends Table[PerfIndicatorRow](_tableTag, "perf_indicator") {
    def * = (id, appkey, `type`, spanname, count, successCount, exceptionCount, timeoutCount, dropCount, qps, tp50, tp90, tp95, tp99, costMax, date, createTime) <> (PerfIndicatorRow.tupled, PerfIndicatorRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (id.?, appkey.?, `type`.?, spanname.?, count.?, successCount.?, exceptionCount.?, timeoutCount.?, dropCount.?, qps.?, tp50.?, tp90.?, tp95.?, tp99.?, costMax.?, date.?, createTime.?).shaped.<>({r=>import r._; _1.map(_=> PerfIndicatorRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6.get, _7.get, _8.get, _9.get, _10.get, _11.get, _12.get, _13.get, _14.get, _15.get, _16.get, _17.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id DBType(BIGINT), AutoInc, PrimaryKey */
    val id: Column[Long] = column[Long]("id", O.AutoInc, O.PrimaryKey)
    /** Database column appkey DBType(VARCHAR), Length(128,true) */
    val appkey: Column[String] = column[String]("appkey", O.Length(128,varying=true))
    /** Database column type DBType(BIT)
      *  NOTE: The name was escaped because it collided with a Scala keyword. */
    val `type`: Column[Boolean] = column[Boolean]("type")
    /** Database column spanname DBType(VARCHAR), Length(128,true) */
    val spanname: Column[String] = column[String]("spanname", O.Length(128,varying=true))
    /** Database column count DBType(BIGINT), Default(0) */
    val count: Column[Long] = column[Long]("count", O.Default(0L))
    /** Database column success_count DBType(BIGINT), Default(0) */
    val successCount: Column[Long] = column[Long]("success_count", O.Default(0L))
    /** Database column exception_count DBType(BIGINT), Default(0) */
    val exceptionCount: Column[Long] = column[Long]("exception_count", O.Default(0L))
    /** Database column timeout_count DBType(BIGINT), Default(0) */
    val timeoutCount: Column[Long] = column[Long]("timeout_count", O.Default(0L))
    /** Database column drop_count DBType(BIGINT), Default(0) */
    val dropCount: Column[Long] = column[Long]("drop_count", O.Default(0L))
    /** Database column qps DBType(DOUBLE), Default(0.0) */
    val qps: Column[Double] = column[Double]("qps", O.Default(0.0))
    /** Database column tp50 DBType(INT), Default(0) */
    val tp50: Column[Int] = column[Int]("tp50", O.Default(0))
    /** Database column tp90 DBType(INT), Default(0) */
    val tp90: Column[Int] = column[Int]("tp90", O.Default(0))
    /** Database column tp95 DBType(INT), Default(0) */
    val tp95: Column[Int] = column[Int]("tp95", O.Default(0))
    /** Database column tp99 DBType(INT), Default(0) */
    val tp99: Column[Int] = column[Int]("tp99", O.Default(0))
    /** Database column cost_max DBType(INT), Default(0) */
    val costMax: Column[Int] = column[Int]("cost_max", O.Default(0))
    /** Database column date DBType(DATE) */
    val date: Column[java.sql.Date] = column[java.sql.Date]("date")
    /** Database column create_time DBType(BIGINT), Default(0) */
    val createTime: Column[Long] = column[Long]("create_time", O.Default(0L))

    /** Uniqueness Index over (appkey,`type`,date) (database name index_appkey_type_date) */
    val index1 = index("index_appkey_type_date", (appkey, `type`, date), unique=true)
  }
  /** Collection-like TableQuery object for table PerfIndicator */
  lazy val PerfIndicator = new TableQuery(tag => new PerfIndicator(tag))

  /** Entity class storing rows of table PluginConfig
    *  @param id Database column id DBType(BIGINT), AutoInc, PrimaryKey
    *  @param appkey Database column appkey DBType(VARCHAR), Length(128,true), Default()
    *  @param projectGroupId Database column project_group_id DBType(VARCHAR), Length(256,true), Default()
    *  @param projectArtifactId Database column project_artifact_id DBType(VARCHAR), Length(128,true), Default()
    *  @param groupId Database column group_id DBType(VARCHAR), Length(256,true), Default()
    *  @param artifactId Database column artifact_id DBType(VARCHAR), Length(128,true), Default()
    *  @param version Database column version DBType(VARCHAR), Length(128,true), Default()
    *  @param createTime Database column create_time DBType(BIGINT), Default(0)
    *  @param uploadTime Database column upload_time DBType(BIGINT), Default(0)
    *  @param requiredVersion Database column required_version DBType(VARCHAR), Length(128,true), Default()
    *  @param action Database column action DBType(VARCHAR), Length(32,true), Default(none) */
  case class PluginConfigRow(id: Long, appkey: String = "", projectGroupId: String = "", projectArtifactId: String = "", groupId: String = "", artifactId: String = "", version: String = "", createTime: Long = 0L, uploadTime: Long = 0L, requiredVersion: String = "", action: String = "none")
  /** GetResult implicit for fetching PluginConfigRow objects using plain SQL queries */
  implicit def GetResultPluginConfigRow(implicit e0: GR[Long], e1: GR[String]): GR[PluginConfigRow] = GR{
    prs => import prs._
      PluginConfigRow.tupled((<<[Long], <<[String], <<[String], <<[String], <<[String], <<[String], <<[String], <<[Long], <<[Long], <<[String], <<[String]))
  }
  /** Table description of table plugin_config. Objects of this class serve as prototypes for rows in queries. */
  class PluginConfig(_tableTag: Tag) extends Table[PluginConfigRow](_tableTag, "plugin_config") {
    def * = (id, appkey, projectGroupId, projectArtifactId, groupId, artifactId, version, createTime, uploadTime, requiredVersion, action) <> (PluginConfigRow.tupled, PluginConfigRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (id.?, appkey.?, projectGroupId.?, projectArtifactId.?, groupId.?, artifactId.?, version.?, createTime.?, uploadTime.?, requiredVersion.?, action.?).shaped.<>({r=>import r._; _1.map(_=> PluginConfigRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6.get, _7.get, _8.get, _9.get, _10.get, _11.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id DBType(BIGINT), AutoInc, PrimaryKey */
    val id: Column[Long] = column[Long]("id", O.AutoInc, O.PrimaryKey)
    /** Database column appkey DBType(VARCHAR), Length(128,true), Default() */
    val appkey: Column[String] = column[String]("appkey", O.Length(128,varying=true), O.Default(""))
    /** Database column project_group_id DBType(VARCHAR), Length(256,true), Default() */
    val projectGroupId: Column[String] = column[String]("project_group_id", O.Length(256,varying=true), O.Default(""))
    /** Database column project_artifact_id DBType(VARCHAR), Length(128,true), Default() */
    val projectArtifactId: Column[String] = column[String]("project_artifact_id", O.Length(128,varying=true), O.Default(""))
    /** Database column group_id DBType(VARCHAR), Length(256,true), Default() */
    val groupId: Column[String] = column[String]("group_id", O.Length(256,varying=true), O.Default(""))
    /** Database column artifact_id DBType(VARCHAR), Length(128,true), Default() */
    val artifactId: Column[String] = column[String]("artifact_id", O.Length(128,varying=true), O.Default(""))
    /** Database column version DBType(VARCHAR), Length(128,true), Default() */
    val version: Column[String] = column[String]("version", O.Length(128,varying=true), O.Default(""))
    /** Database column create_time DBType(BIGINT), Default(0) */
    val createTime: Column[Long] = column[Long]("create_time", O.Default(0L))
    /** Database column upload_time DBType(BIGINT), Default(0) */
    val uploadTime: Column[Long] = column[Long]("upload_time", O.Default(0L))
    /** Database column required_version DBType(VARCHAR), Length(128,true), Default() */
    val requiredVersion: Column[String] = column[String]("required_version", O.Length(128,varying=true), O.Default(""))
    /** Database column action DBType(VARCHAR), Length(32,true), Default(none) */
    val action: Column[String] = column[String]("action", O.Length(32,varying=true), O.Default("none"))
  }
  /** Collection-like TableQuery object for table PluginConfig */
  lazy val PluginConfig = new TableQuery(tag => new PluginConfig(tag))

  /** Entity class storing rows of table ProviderTriggerCount
   *  @param id Database column id DBType(INT), AutoInc, PrimaryKey
   *  @param triggerId Database column trigger_id DBType(BIGINT), Default(0)
   *  @param count Database column count DBType(INT), Default(0) */
  case class ProviderTriggerCountRow(id: Int, triggerId: Long = 0L, count: Int = 0)
  /** GetResult implicit for fetching ProviderTriggerCountRow objects using plain SQL queries */
  implicit def GetResultProviderTriggerCountRow(implicit e0: GR[Int], e1: GR[Long]): GR[ProviderTriggerCountRow] = GR{
    prs => import prs._
    ProviderTriggerCountRow.tupled((<<[Int], <<[Long], <<[Int]))
  }
  /** Table description of table provider_trigger_count. Objects of this class serve as prototypes for rows in queries. */
  class ProviderTriggerCount(_tableTag: Tag) extends Table[ProviderTriggerCountRow](_tableTag, "provider_trigger_count") {
    def * = (id, triggerId, count) <> (ProviderTriggerCountRow.tupled, ProviderTriggerCountRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (id.?, triggerId.?, count.?).shaped.<>({r=>import r._; _1.map(_=> ProviderTriggerCountRow.tupled((_1.get, _2.get, _3.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id DBType(INT), AutoInc, PrimaryKey */
    val id: Column[Int] = column[Int]("id", O.AutoInc, O.PrimaryKey)
    /** Database column trigger_id DBType(BIGINT), Default(0) */
    val triggerId: Column[Long] = column[Long]("trigger_id", O.Default(0L))
    /** Database column count DBType(INT), Default(0) */
    val count: Column[Int] = column[Int]("count", O.Default(0))
  }
  /** Collection-like TableQuery object for table ProviderTriggerCount */
  lazy val ProviderTriggerCount = new TableQuery(tag => new ProviderTriggerCount(tag))

  /** Entity class storing rows of table ProviderTriggerSubscribe
    *  @param id Database column id DBType(BIGINT), AutoInc, PrimaryKey
    *  @param appkey Database column appkey DBType(VARCHAR), Length(128,true), Default()
    *  @param triggerId Database column trigger_id DBType(BIGINT), Default(0)
    *  @param userId Database column user_id DBType(INT), Default(0)
    *  @param userLogin Database column user_login DBType(VARCHAR), Length(32,true), Default()
    *  @param userName Database column user_name DBType(VARCHAR), Length(32,true), Default()
    *  @param xm Database column xm DBType(TINYINT), Default(0)
    *  @param sms Database column sms DBType(TINYINT), Default(0)
    *  @param email Database column email DBType(TINYINT), Default(0) */
  case class ProviderTriggerSubscribeRow(id: Long, appkey: String = "", triggerId: Long = 0L, userId: Int = 0, userLogin: String = "", userName: String = "", xm: Byte = 0, sms: Byte = 0, email: Byte = 0)
  /** GetResult implicit for fetching ProviderTriggerSubscribeRow objects using plain SQL queries */
  implicit def GetResultProviderTriggerSubscribeRow(implicit e0: GR[Long], e1: GR[String], e2: GR[Int], e3: GR[Byte]): GR[ProviderTriggerSubscribeRow] = GR{
    prs => import prs._
      ProviderTriggerSubscribeRow.tupled((<<[Long], <<[String], <<[Long], <<[Int], <<[String], <<[String], <<[Byte], <<[Byte], <<[Byte]))
  }
  /** Table description of table provider_trigger_subscribe. Objects of this class serve as prototypes for rows in queries. */
  class ProviderTriggerSubscribe(_tableTag: Tag) extends Table[ProviderTriggerSubscribeRow](_tableTag, "provider_trigger_subscribe") {
    def * = (id, appkey, triggerId, userId, userLogin, userName, xm, sms, email) <> (ProviderTriggerSubscribeRow.tupled, ProviderTriggerSubscribeRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (id.?, appkey.?, triggerId.?, userId.?, userLogin.?, userName.?, xm.?, sms.?, email.?).shaped.<>({r=>import r._; _1.map(_=> ProviderTriggerSubscribeRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6.get, _7.get, _8.get, _9.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id DBType(BIGINT), AutoInc, PrimaryKey */
    val id: Column[Long] = column[Long]("id", O.AutoInc, O.PrimaryKey)
    /** Database column appkey DBType(VARCHAR), Length(128,true), Default() */
    val appkey: Column[String] = column[String]("appkey", O.Length(128,varying=true), O.Default(""))
    /** Database column trigger_id DBType(BIGINT), Default(0) */
    val triggerId: Column[Long] = column[Long]("trigger_id", O.Default(0L))
    /** Database column user_id DBType(INT), Default(0) */
    val userId: Column[Int] = column[Int]("user_id", O.Default(0))
    /** Database column user_login DBType(VARCHAR), Length(32,true), Default() */
    val userLogin: Column[String] = column[String]("user_login", O.Length(32,varying=true), O.Default(""))
    /** Database column user_name DBType(VARCHAR), Length(32,true), Default() */
    val userName: Column[String] = column[String]("user_name", O.Length(32,varying=true), O.Default(""))
    /** Database column xm DBType(TINYINT), Default(0) */
    val xm: Column[Byte] = column[Byte]("xm", O.Default(0))
    /** Database column sms DBType(TINYINT), Default(0) */
    val sms: Column[Byte] = column[Byte]("sms", O.Default(0))
    /** Database column email DBType(TINYINT), Default(0) */
    val email: Column[Byte] = column[Byte]("email", O.Default(0))

    /** Index over (appkey) (database name idx_appkey) */
    val index1 = index("idx_appkey", appkey)
    /** Index over (userId) (database name idx_user) */
    val index2 = index("idx_user", userId)
  }
  /** Collection-like TableQuery object for table ProviderTriggerSubscribe */
  lazy val ProviderTriggerSubscribe = new TableQuery(tag => new ProviderTriggerSubscribe(tag))

  /** Entity class storing rows of table RealtimeLog
    *  @param id Database column id DBType(BIGINT), AutoInc, PrimaryKey
    *  @param appkey Database column appkey DBType(VARCHAR), Length(128,true), Default()
    *  @param logPath Database column log_path DBType(VARCHAR), Length(256,true), Default()
    *  @param createTime Database column create_time DBType(BIGINT), Default(0) */
  case class RealtimeLogRow(id: Long, appkey: String = "", logPath: String = "", createTime: Long = 0L)
  /** GetResult implicit for fetching RealtimeLogRow objects using plain SQL queries */
  implicit def GetResultRealtimeLogRow(implicit e0: GR[Long], e1: GR[String]): GR[RealtimeLogRow] = GR{
    prs => import prs._
      RealtimeLogRow.tupled((<<[Long], <<[String], <<[String], <<[Long]))
  }
  /** Table description of table realtime_log. Objects of this class serve as prototypes for rows in queries. */
  class RealtimeLog(_tableTag: Tag) extends Table[RealtimeLogRow](_tableTag, "realtime_log") {
    def * = (id, appkey, logPath, createTime) <> (RealtimeLogRow.tupled, RealtimeLogRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (id.?, appkey.?, logPath.?, createTime.?).shaped.<>({r=>import r._; _1.map(_=> RealtimeLogRow.tupled((_1.get, _2.get, _3.get, _4.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id DBType(BIGINT), AutoInc, PrimaryKey */
    val id: Column[Long] = column[Long]("id", O.AutoInc, O.PrimaryKey)
    /** Database column appkey DBType(VARCHAR), Length(128,true), Default() */
    val appkey: Column[String] = column[String]("appkey", O.Length(128,varying=true), O.Default(""))
    /** Database column log_path DBType(VARCHAR), Length(256,true), Default() */
    val logPath: Column[String] = column[String]("log_path", O.Length(256,varying=true), O.Default(""))
    /** Database column create_time DBType(BIGINT), Default(0) */
    val createTime: Column[Long] = column[Long]("create_time", O.Default(0L))

    /** Uniqueness Index over (appkey) (database name index_appkey) */
    val index1 = index("index_appkey", appkey, unique=true)
  }
  /** Collection-like TableQuery object for table RealtimeLog */
  lazy val RealtimeLog = new TableQuery(tag => new RealtimeLog(tag))

  /** Entity class storing rows of table ReportDaily
    *  @param id Database column id DBType(BIGINT), AutoInc, PrimaryKey
    *  @param owt Database column owt DBType(VARCHAR), Length(20,true), Default()
    *  @param appkey Database column appkey DBType(VARCHAR), Length(128,true), Default()
    *  @param spanname Database column spanname DBType(VARCHAR), Length(255,true), Default()
    *  @param count Database column count DBType(BIGINT), Default(0)
    *  @param successRatio Database column success_ratio DBType(DECIMAL), Default(None)
    *  @param aliveRatio Database column alive_ratio DBType(VARCHAR), Length(128,true), Default()
    *  @param qps Database column qps DBType(DOUBLE), Default(0.0)
    *  @param topQps Database column top_qps DBType(DOUBLE), Default(0.0)
    *  @param topHostQps Database column top_host_qps DBType(DOUBLE), Default(0.0)
    *  @param avgHostQps Database column avg_host_qps DBType(DOUBLE), Default(0.0)
    *  @param tp999 Database column tp999 DBType(INT), Default(0)
    *  @param tp99 Database column tp99 DBType(INT), Default(0)
    *  @param tp95 Database column tp95 DBType(INT), Default(0)
    *  @param tp90 Database column tp90 DBType(INT), Default(0)
    *  @param tp50 Database column tp50 DBType(INT), Default(0)
    *  @param errorCount Database column error_count DBType(BIGINT), Default(0)
    *  @param perfAlertCount Database column perf_alert_count DBType(BIGINT), Default(0)
    *  @param isLoadBalance Database column is_load_balance DBType(INT), Default(0)
    *  @param hostCount Database column host_count DBType(INT), Default(0)
    *  @param day Database column day DBType(DATE)
    *  @param createTime Database column create_time DBType(BIGINT), Default(0) */
  case class ReportDailyRow(id: Long, owt: String = "", appkey: String = "", spanname: String = "", count: Long = 0L, successRatio: Option[scala.math.BigDecimal] = None, aliveRatio: String = "", qps: Double = 0.0, topQps: Double = 0.0, topHostQps: Double = 0.0, avgHostQps: Double = 0.0, tp999: Int = 0, tp99: Int = 0, tp95: Int = 0, tp90: Int = 0, tp50: Int = 0, errorCount: Long = 0L, perfAlertCount: Long = 0L, isLoadBalance: Int = 0, hostCount: Int = 0, day: java.sql.Date, createTime: Long = 0L)
  /** GetResult implicit for fetching ReportDailyRow objects using plain SQL queries */
  implicit def GetResultReportDailyRow(implicit e0: GR[Long], e1: GR[String], e2: GR[Option[scala.math.BigDecimal]], e3: GR[Double], e4: GR[Int], e5: GR[java.sql.Date]): GR[ReportDailyRow] = GR{
    prs => import prs._
      ReportDailyRow.tupled((<<[Long], <<[String], <<[String], <<[String], <<[Long], <<?[scala.math.BigDecimal], <<[String], <<[Double], <<[Double], <<[Double], <<[Double], <<[Int], <<[Int], <<[Int], <<[Int], <<[Int], <<[Long], <<[Long], <<[Int], <<[Int], <<[java.sql.Date], <<[Long]))
  }
  /** Table description of table report_daily. Objects of this class serve as prototypes for rows in queries. */
  class ReportDaily(_tableTag: Tag) extends Table[ReportDailyRow](_tableTag, "report_daily") {
    def * = (id, owt, appkey, spanname, count, successRatio, aliveRatio, qps, topQps, topHostQps, avgHostQps, tp999, tp99, tp95, tp90, tp50, errorCount, perfAlertCount, isLoadBalance, hostCount, day, createTime) <> (ReportDailyRow.tupled, ReportDailyRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (id.?, owt.?, appkey.?, spanname.?, count.?, successRatio, aliveRatio.?, qps.?, topQps.?, topHostQps.?, avgHostQps.?, tp999.?, tp99.?, tp95.?, tp90.?, tp50.?, errorCount.?, perfAlertCount.?, isLoadBalance.?, hostCount.?, day.?, createTime.?).shaped.<>({r=>import r._; _1.map(_=> ReportDailyRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6, _7.get, _8.get, _9.get, _10.get, _11.get, _12.get, _13.get, _14.get, _15.get, _16.get, _17.get, _18.get, _19.get, _20.get, _21.get, _22.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id DBType(BIGINT), AutoInc, PrimaryKey */
    val id: Column[Long] = column[Long]("id", O.AutoInc, O.PrimaryKey)
    /** Database column owt DBType(VARCHAR), Length(20,true), Default() */
    val owt: Column[String] = column[String]("owt", O.Length(20,varying=true), O.Default(""))
    /** Database column appkey DBType(VARCHAR), Length(128,true), Default() */
    val appkey: Column[String] = column[String]("appkey", O.Length(128,varying=true), O.Default(""))
    /** Database column spanname DBType(VARCHAR), Length(255,true), Default() */
    val spanname: Column[String] = column[String]("spanname", O.Length(255,varying=true), O.Default(""))
    /** Database column count DBType(BIGINT), Default(0) */
    val count: Column[Long] = column[Long]("count", O.Default(0L))
    /** Database column success_ratio DBType(DECIMAL), Default(None) */
    val successRatio: Column[Option[scala.math.BigDecimal]] = column[Option[scala.math.BigDecimal]]("success_ratio", O.Default(None))
    /** Database column alive_ratio DBType(VARCHAR), Length(128,true), Default() */
    val aliveRatio: Column[String] = column[String]("alive_ratio", O.Length(128,varying=true), O.Default(""))
    /** Database column qps DBType(DOUBLE), Default(0.0) */
    val qps: Column[Double] = column[Double]("qps", O.Default(0.0))
    /** Database column top_qps DBType(DOUBLE), Default(0.0) */
    val topQps: Column[Double] = column[Double]("top_qps", O.Default(0.0))
    /** Database column top_host_qps DBType(DOUBLE), Default(0.0) */
    val topHostQps: Column[Double] = column[Double]("top_host_qps", O.Default(0.0))
    /** Database column avg_host_qps DBType(DOUBLE), Default(0.0) */
    val avgHostQps: Column[Double] = column[Double]("avg_host_qps", O.Default(0.0))
    /** Database column tp999 DBType(INT), Default(0) */
    val tp999: Column[Int] = column[Int]("tp999", O.Default(0))
    /** Database column tp99 DBType(INT), Default(0) */
    val tp99: Column[Int] = column[Int]("tp99", O.Default(0))
    /** Database column tp95 DBType(INT), Default(0) */
    val tp95: Column[Int] = column[Int]("tp95", O.Default(0))
    /** Database column tp90 DBType(INT), Default(0) */
    val tp90: Column[Int] = column[Int]("tp90", O.Default(0))
    /** Database column tp50 DBType(INT), Default(0) */
    val tp50: Column[Int] = column[Int]("tp50", O.Default(0))
    /** Database column error_count DBType(BIGINT), Default(0) */
    val errorCount: Column[Long] = column[Long]("error_count", O.Default(0L))
    /** Database column perf_alert_count DBType(BIGINT), Default(0) */
    val perfAlertCount: Column[Long] = column[Long]("perf_alert_count", O.Default(0L))
    /** Database column is_load_balance DBType(INT), Default(0) */
    val isLoadBalance: Column[Int] = column[Int]("is_load_balance", O.Default(0))
    /** Database column host_count DBType(INT), Default(0) */
    val hostCount: Column[Int] = column[Int]("host_count", O.Default(0))
    /** Database column day DBType(DATE) */
    val day: Column[java.sql.Date] = column[java.sql.Date]("day")
    /** Database column create_time DBType(BIGINT), Default(0) */
    val createTime: Column[Long] = column[Long]("create_time", O.Default(0L))

    /** Uniqueness Index over (appkey,spanname,day) (database name index_appkey_spanname_day) */
    val index1 = index("index_appkey_spanname_day", (appkey, spanname, day), unique=true)
  }
  /** Collection-like TableQuery object for table ReportDaily */
  lazy val ReportDaily = new TableQuery(tag => new ReportDaily(tag))

  /** Entity class storing rows of table ReportDailyMail
    *  @param id Database column id DBType(BIGINT UNSIGNED), AutoInc, PrimaryKey
    *  @param appkey Database column appkey DBType(VARCHAR), Length(128,true)
    *  @param username Database column username DBType(VARCHAR), Length(128,true)
    *  @param day Database column day DBType(DATE)
    *  @param status Database column status DBType(INT), Default(0)
    *  @param sendTime Database column send_time DBType(BIGINT), Default(0)
    *  @param createTime Database column create_time DBType(BIGINT), Default(0) */
  case class ReportDailyMailRow(id: Long, appkey: String, username: String, day: java.sql.Date, status: Int = 0, sendTime: Long = 0L, createTime: Long = 0L)
  /** GetResult implicit for fetching ReportDailyMailRow objects using plain SQL queries */
  implicit def GetResultReportDailyMailRow(implicit e0: GR[Long], e1: GR[String], e2: GR[java.sql.Date], e3: GR[Int]): GR[ReportDailyMailRow] = GR{
    prs => import prs._
      ReportDailyMailRow.tupled((<<[Long], <<[String], <<[String], <<[java.sql.Date], <<[Int], <<[Long], <<[Long]))
  }
  /** Table description of table report_daily_mail. Objects of this class serve as prototypes for rows in queries. */
  class ReportDailyMail(_tableTag: Tag) extends Table[ReportDailyMailRow](_tableTag, "report_daily_mail") {
    def * = (id, appkey, username, day, status, sendTime, createTime) <> (ReportDailyMailRow.tupled, ReportDailyMailRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (id.?, appkey.?, username.?, day.?, status.?, sendTime.?, createTime.?).shaped.<>({r=>import r._; _1.map(_=> ReportDailyMailRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6.get, _7.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id DBType(BIGINT UNSIGNED), AutoInc, PrimaryKey */
    val id: Column[Long] = column[Long]("id", O.AutoInc, O.PrimaryKey)
    /** Database column appkey DBType(VARCHAR), Length(128,true) */
    val appkey: Column[String] = column[String]("appkey", O.Length(128,varying=true))
    /** Database column username DBType(VARCHAR), Length(128,true) */
    val username: Column[String] = column[String]("username", O.Length(128,varying=true))
    /** Database column day DBType(DATE) */
    val day: Column[java.sql.Date] = column[java.sql.Date]("day")
    /** Database column status DBType(INT), Default(0) */
    val status: Column[Int] = column[Int]("status", O.Default(0))
    /** Database column send_time DBType(BIGINT), Default(0) */
    val sendTime: Column[Long] = column[Long]("send_time", O.Default(0L))
    /** Database column create_time DBType(BIGINT), Default(0) */
    val createTime: Column[Long] = column[Long]("create_time", O.Default(0L))

    /** Uniqueness Index over (appkey,username,day) (database name idx_appkey_username_day) */
    val index1 = index("idx_appkey_username_day", (appkey, username, day), unique=true)
    /** Index over (username,day) (database name idx_username_day) */
    val index2 = index("idx_username_day", (username, day))
  }
  /** Collection-like TableQuery object for table ReportDailyMail */
  lazy val ReportDailyMail = new TableQuery(tag => new ReportDailyMail(tag))

  /** Entity class storing rows of table ReportDailyStatus
    *  @param id Database column id DBType(BIGINT), AutoInc, PrimaryKey
    *  @param owt Database column owt DBType(VARCHAR), Length(20,true), Default()
    *  @param appkey Database column appkey DBType(VARCHAR), Length(128,true), Default()
    *  @param isComputed Database column is_computed DBType(INT), Default(0)
    *  @param day Database column day DBType(DATE) */
  case class ReportDailyStatusRow(id: Long, owt: String = "", appkey: String = "", isComputed: Int = 0, day: java.sql.Date)
  /** GetResult implicit for fetching ReportDailyStatusRow objects using plain SQL queries */
  implicit def GetResultReportDailyStatusRow(implicit e0: GR[Long], e1: GR[String], e2: GR[Int], e3: GR[java.sql.Date]): GR[ReportDailyStatusRow] = GR{
    prs => import prs._
      ReportDailyStatusRow.tupled((<<[Long], <<[String], <<[String], <<[Int], <<[java.sql.Date]))
  }
  /** Table description of table report_daily_status. Objects of this class serve as prototypes for rows in queries. */
  class ReportDailyStatus(_tableTag: Tag) extends Table[ReportDailyStatusRow](_tableTag, "report_daily_status") {
    def * = (id, owt, appkey, isComputed, day) <> (ReportDailyStatusRow.tupled, ReportDailyStatusRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (id.?, owt.?, appkey.?, isComputed.?, day.?).shaped.<>({r=>import r._; _1.map(_=> ReportDailyStatusRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id DBType(BIGINT), AutoInc, PrimaryKey */
    val id: Column[Long] = column[Long]("id", O.AutoInc, O.PrimaryKey)
    /** Database column owt DBType(VARCHAR), Length(20,true), Default() */
    val owt: Column[String] = column[String]("owt", O.Length(20,varying=true), O.Default(""))
    /** Database column appkey DBType(VARCHAR), Length(128,true), Default() */
    val appkey: Column[String] = column[String]("appkey", O.Length(128,varying=true), O.Default(""))
    /** Database column is_computed DBType(INT), Default(0) */
    val isComputed: Column[Int] = column[Int]("is_computed", O.Default(0))
    /** Database column day DBType(DATE) */
    val day: Column[java.sql.Date] = column[java.sql.Date]("day")

    /** Index over (appkey,day) (database name idx_appkey_day) */
    val index1 = index("idx_appkey_day", (appkey, day))
  }
  /** Collection-like TableQuery object for table ReportDailyStatus */
  lazy val ReportDailyStatus = new TableQuery(tag => new ReportDailyStatus(tag))

  /** Entity class storing rows of table ReportDepend
    *  @param id Database column id DBType(BIGINT), AutoInc, PrimaryKey
    *  @param business Database column business DBType(INT), Default(100)
    *  @param owt Database column owt DBType(VARCHAR), Length(20,true), Default()
    *  @param appkey Database column appkey DBType(VARCHAR), Length(128,true), Default()
    *  @param `type` Database column type DBType(BIT), Default(false)
    *  @param count Database column count DBType(BIGINT), Default(0)
    *  @param startDay Database column start_day DBType(DATE)
    *  @param createTime Database column create_time DBType(BIGINT), Default(0) */
  case class ReportDependRow(id: Long, business: Int = 100, owt: String = "", appkey: String = "", `type`: Boolean = false, count: Long = 0L, startDay: java.sql.Date, createTime: Long = 0L)
  /** GetResult implicit for fetching ReportDependRow objects using plain SQL queries */
  implicit def GetResultReportDependRow(implicit e0: GR[Long], e1: GR[Int], e2: GR[String], e3: GR[Boolean], e4: GR[java.sql.Date]): GR[ReportDependRow] = GR{
    prs => import prs._
      ReportDependRow.tupled((<<[Long], <<[Int], <<[String], <<[String], <<[Boolean], <<[Long], <<[java.sql.Date], <<[Long]))
  }
  /** Table description of table report_depend. Objects of this class serve as prototypes for rows in queries.
    *  NOTE: The following names collided with Scala keywords and were escaped: type */
  class ReportDepend(_tableTag: Tag) extends Table[ReportDependRow](_tableTag, "report_depend") {
    def * = (id, business, owt, appkey, `type`, count, startDay, createTime) <> (ReportDependRow.tupled, ReportDependRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (id.?, business.?, owt.?, appkey.?, `type`.?, count.?, startDay.?, createTime.?).shaped.<>({r=>import r._; _1.map(_=> ReportDependRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6.get, _7.get, _8.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id DBType(BIGINT), AutoInc, PrimaryKey */
    val id: Column[Long] = column[Long]("id", O.AutoInc, O.PrimaryKey)
    /** Database column business DBType(INT), Default(100) */
    val business: Column[Int] = column[Int]("business", O.Default(100))
    /** Database column owt DBType(VARCHAR), Length(20,true), Default() */
    val owt: Column[String] = column[String]("owt", O.Length(20,varying=true), O.Default(""))
    /** Database column appkey DBType(VARCHAR), Length(128,true), Default() */
    val appkey: Column[String] = column[String]("appkey", O.Length(128,varying=true), O.Default(""))
    /** Database column type DBType(BIT), Default(false)
      *  NOTE: The name was escaped because it collided with a Scala keyword. */
    val `type`: Column[Boolean] = column[Boolean]("type", O.Default(false))
    /** Database column count DBType(BIGINT), Default(0) */
    val count: Column[Long] = column[Long]("count", O.Default(0L))
    /** Database column start_day DBType(DATE) */
    val startDay: Column[java.sql.Date] = column[java.sql.Date]("start_day")
    /** Database column create_time DBType(BIGINT), Default(0) */
    val createTime: Column[Long] = column[Long]("create_time", O.Default(0L))

    /** Index over (appkey,startDay) (database name idx_appkey_start_day) */
    val index1 = index("idx_appkey_start_day", (appkey, startDay))
    /** Uniqueness Index over (owt,appkey,startDay,`type`) (database name index_owt_appkey_start_day_type) */
    val index2 = index("index_owt_appkey_start_day_type", (owt, appkey, startDay, `type`), unique=true)
  }
  /** Collection-like TableQuery object for table ReportDepend */
  lazy val ReportDepend = new TableQuery(tag => new ReportDepend(tag))

  /** Entity class storing rows of table ReportErrorlog
    *  @param id Database column id DBType(BIGINT), AutoInc, PrimaryKey
    *  @param owt Database column owt DBType(VARCHAR), Length(20,true), Default()
    *  @param appkey Database column appkey DBType(VARCHAR), Length(128,true), Default()
    *  @param count Database column count DBType(BIGINT), Default(0)
    *  @param errorCount Database column error_count DBType(BIGINT), Default(0)
    *  @param ratio Database column ratio DBType(DOUBLE), Default(0.0)
    *  @param startDay Database column start_day DBType(DATE)
    *  @param createTime Database column create_time DBType(BIGINT), Default(0) */
  case class ReportErrorlogRow(id: Long, owt: String = "", appkey: String = "", count: Long = 0L, errorCount: Long = 0L, ratio: Double = 0.0, startDay: java.sql.Date, createTime: Long = 0L)
  /** GetResult implicit for fetching ReportErrorlogRow objects using plain SQL queries */
  implicit def GetResultReportErrorlogRow(implicit e0: GR[Long], e1: GR[String], e2: GR[Double], e3: GR[java.sql.Date]): GR[ReportErrorlogRow] = GR{
    prs => import prs._
      ReportErrorlogRow.tupled((<<[Long], <<[String], <<[String], <<[Long], <<[Long], <<[Double], <<[java.sql.Date], <<[Long]))
  }
  /** Table description of table report_errorlog. Objects of this class serve as prototypes for rows in queries. */
  class ReportErrorlog(_tableTag: Tag) extends Table[ReportErrorlogRow](_tableTag, "report_errorlog") {
    def * = (id, owt, appkey, count, errorCount, ratio, startDay, createTime) <> (ReportErrorlogRow.tupled, ReportErrorlogRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (id.?, owt.?, appkey.?, count.?, errorCount.?, ratio.?, startDay.?, createTime.?).shaped.<>({r=>import r._; _1.map(_=> ReportErrorlogRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6.get, _7.get, _8.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id DBType(BIGINT), AutoInc, PrimaryKey */
    val id: Column[Long] = column[Long]("id", O.AutoInc, O.PrimaryKey)
    /** Database column owt DBType(VARCHAR), Length(20,true), Default() */
    val owt: Column[String] = column[String]("owt", O.Length(20,varying=true), O.Default(""))
    /** Database column appkey DBType(VARCHAR), Length(128,true), Default() */
    val appkey: Column[String] = column[String]("appkey", O.Length(128,varying=true), O.Default(""))
    /** Database column count DBType(BIGINT), Default(0) */
    val count: Column[Long] = column[Long]("count", O.Default(0L))
    /** Database column error_count DBType(BIGINT), Default(0) */
    val errorCount: Column[Long] = column[Long]("error_count", O.Default(0L))
    /** Database column ratio DBType(DOUBLE), Default(0.0) */
    val ratio: Column[Double] = column[Double]("ratio", O.Default(0.0))
    /** Database column start_day DBType(DATE) */
    val startDay: Column[java.sql.Date] = column[java.sql.Date]("start_day")
    /** Database column create_time DBType(BIGINT), Default(0) */
    val createTime: Column[Long] = column[Long]("create_time", O.Default(0L))

    /** Uniqueness Index over (owt,appkey,startDay) (database name index_owt_appkey_start_day) */
    val index1 = index("index_owt_appkey_start_day", (owt, appkey, startDay), unique=true)
  }
  /** Collection-like TableQuery object for table ReportErrorlog */
  lazy val ReportErrorlog = new TableQuery(tag => new ReportErrorlog(tag))

  /** Entity class storing rows of table ReportIdcTraffic
    *  @param id Database column id DBType(BIGINT), AutoInc, PrimaryKey
    *  @param owt Database column owt DBType(VARCHAR), Length(20,true), Default()
    *  @param appkey Database column appkey DBType(VARCHAR), Length(128,true), Default()
    *  @param idc Database column idc DBType(VARCHAR), Length(20,true), Default(0)
    *  @param hostCount Database column host_count DBType(INT), Default(0)
    *  @param idcCount Database column idc_count DBType(BIGINT), Default(0)
    *  @param startDay Database column start_day DBType(DATE)
    *  @param createTime Database column create_time DBType(BIGINT), Default(0) */
  case class ReportIdcTrafficRow(id: Long, owt: String = "", appkey: String = "", idc: String = "0", hostCount: Int = 0, idcCount: Long = 0L, startDay: java.sql.Date, createTime: Long = 0L)
  /** GetResult implicit for fetching ReportIdcTrafficRow objects using plain SQL queries */
  implicit def GetResultReportIdcTrafficRow(implicit e0: GR[Long], e1: GR[String], e2: GR[Int], e3: GR[java.sql.Date]): GR[ReportIdcTrafficRow] = GR{
    prs => import prs._
      ReportIdcTrafficRow.tupled((<<[Long], <<[String], <<[String], <<[String], <<[Int], <<[Long], <<[java.sql.Date], <<[Long]))
  }
  /** Table description of table report_idc_traffic. Objects of this class serve as prototypes for rows in queries. */
  class ReportIdcTraffic(_tableTag: Tag) extends Table[ReportIdcTrafficRow](_tableTag, "report_idc_traffic") {
    def * = (id, owt, appkey, idc, hostCount, idcCount, startDay, createTime) <> (ReportIdcTrafficRow.tupled, ReportIdcTrafficRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (id.?, owt.?, appkey.?, idc.?, hostCount.?, idcCount.?, startDay.?, createTime.?).shaped.<>({r=>import r._; _1.map(_=> ReportIdcTrafficRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6.get, _7.get, _8.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id DBType(BIGINT), AutoInc, PrimaryKey */
    val id: Column[Long] = column[Long]("id", O.AutoInc, O.PrimaryKey)
    /** Database column owt DBType(VARCHAR), Length(20,true), Default() */
    val owt: Column[String] = column[String]("owt", O.Length(20,varying=true), O.Default(""))
    /** Database column appkey DBType(VARCHAR), Length(128,true), Default() */
    val appkey: Column[String] = column[String]("appkey", O.Length(128,varying=true), O.Default(""))
    /** Database column idc DBType(VARCHAR), Length(20,true), Default(0) */
    val idc: Column[String] = column[String]("idc", O.Length(20,varying=true), O.Default("0"))
    /** Database column host_count DBType(INT), Default(0) */
    val hostCount: Column[Int] = column[Int]("host_count", O.Default(0))
    /** Database column idc_count DBType(BIGINT), Default(0) */
    val idcCount: Column[Long] = column[Long]("idc_count", O.Default(0L))
    /** Database column start_day DBType(DATE) */
    val startDay: Column[java.sql.Date] = column[java.sql.Date]("start_day")
    /** Database column create_time DBType(BIGINT), Default(0) */
    val createTime: Column[Long] = column[Long]("create_time", O.Default(0L))

    /** Uniqueness Index over (appkey,idc,startDay) (database name index_appkey_idc_start_day) */
    val index1 = index("index_appkey_idc_start_day", (appkey, idc, startDay), unique=true)
    /** Index over (owt,startDay) (database name index_owt_start_day) */
    val index2 = index("index_owt_start_day", (owt, startDay))
  }
  /** Collection-like TableQuery object for table ReportIdcTraffic */
  lazy val ReportIdcTraffic = new TableQuery(tag => new ReportIdcTraffic(tag))

  /** Entity class storing rows of table ReportQps
    *  @param id Database column id DBType(BIGINT), AutoInc, PrimaryKey
    *  @param owt Database column owt DBType(VARCHAR), Length(20,true), Default()
    *  @param appkey Database column appkey DBType(VARCHAR), Length(128,true), Default()
    *  @param count Database column count DBType(BIGINT), Default(0)
    *  @param qps Database column qps DBType(DOUBLE), Default(0.0)
    *  @param tp90 Database column tp90 DBType(INT), Default(0)
    *  @param weekQps Database column week_qps DBType(VARCHAR), Length(256,true), Default()
    *  @param weekTp90 Database column week_tp90 DBType(VARCHAR), Length(256,true), Default()
    *  @param startDay Database column start_day DBType(DATE)
    *  @param createTime Database column create_time DBType(BIGINT), Default(0) */
  case class ReportQpsRow(id: Long, owt: String = "", appkey: String = "", count: Long = 0L, qps: Double = 0.0, tp90: Int = 0, weekQps: String = "", weekTp90: String = "", startDay: java.sql.Date, createTime: Long = 0L)
  /** GetResult implicit for fetching ReportQpsRow objects using plain SQL queries */
  implicit def GetResultReportQpsRow(implicit e0: GR[Long], e1: GR[String], e2: GR[Double], e3: GR[Int], e4: GR[java.sql.Date]): GR[ReportQpsRow] = GR{
    prs => import prs._
      ReportQpsRow.tupled((<<[Long], <<[String], <<[String], <<[Long], <<[Double], <<[Int], <<[String], <<[String], <<[java.sql.Date], <<[Long]))
  }
  /** Table description of table report_qps. Objects of this class serve as prototypes for rows in queries. */
  class ReportQps(_tableTag: Tag) extends Table[ReportQpsRow](_tableTag, "report_qps") {
    def * = (id, owt, appkey, count, qps, tp90, weekQps, weekTp90, startDay, createTime) <> (ReportQpsRow.tupled, ReportQpsRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (id.?, owt.?, appkey.?, count.?, qps.?, tp90.?, weekQps.?, weekTp90.?, startDay.?, createTime.?).shaped.<>({r=>import r._; _1.map(_=> ReportQpsRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6.get, _7.get, _8.get, _9.get, _10.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id DBType(BIGINT), AutoInc, PrimaryKey */
    val id: Column[Long] = column[Long]("id", O.AutoInc, O.PrimaryKey)
    /** Database column owt DBType(VARCHAR), Length(20,true), Default() */
    val owt: Column[String] = column[String]("owt", O.Length(20,varying=true), O.Default(""))
    /** Database column appkey DBType(VARCHAR), Length(128,true), Default() */
    val appkey: Column[String] = column[String]("appkey", O.Length(128,varying=true), O.Default(""))
    /** Database column count DBType(BIGINT), Default(0) */
    val count: Column[Long] = column[Long]("count", O.Default(0L))
    /** Database column qps DBType(DOUBLE), Default(0.0) */
    val qps: Column[Double] = column[Double]("qps", O.Default(0.0))
    /** Database column tp90 DBType(INT), Default(0) */
    val tp90: Column[Int] = column[Int]("tp90", O.Default(0))
    /** Database column week_qps DBType(VARCHAR), Length(256,true), Default() */
    val weekQps: Column[String] = column[String]("week_qps", O.Length(256,varying=true), O.Default(""))
    /** Database column week_tp90 DBType(VARCHAR), Length(256,true), Default() */
    val weekTp90: Column[String] = column[String]("week_tp90", O.Length(256,varying=true), O.Default(""))
    /** Database column start_day DBType(DATE) */
    val startDay: Column[java.sql.Date] = column[java.sql.Date]("start_day")
    /** Database column create_time DBType(BIGINT), Default(0) */
    val createTime: Column[Long] = column[Long]("create_time", O.Default(0L))

    /** Index over (appkey,startDay) (database name idx_appkey_start_day) */
    val index1 = index("idx_appkey_start_day", (appkey, startDay))
    /** Uniqueness Index over (owt,appkey,startDay) (database name index_owt_appkey_start_day) */
    val index2 = index("index_owt_appkey_start_day", (owt, appkey, startDay), unique=true)
  }
  /** Collection-like TableQuery object for table ReportQps */
  lazy val ReportQps = new TableQuery(tag => new ReportQps(tag))

  /** Entity class storing rows of table ReportQpsPeak
    *  @param id Database column id DBType(BIGINT), AutoInc, PrimaryKey
    *  @param owt Database column owt DBType(VARCHAR), Length(20,true), Default()
    *  @param appkey Database column appkey DBType(VARCHAR), Length(128,true), Default()
    *  @param count Database column count DBType(BIGINT), Default(0)
    *  @param hostCount Database column host_count DBType(INT), Default(0)
    *  @param avgQps Database column avg_qps DBType(DOUBLE), Default(0.0)
    *  @param maxHourQps Database column max_hour_qps DBType(DOUBLE), Default(0.0)
    *  @param minHourQps Database column min_hour_qps DBType(DOUBLE), Default(0.0)
    *  @param avgHostQps Database column avg_host_qps DBType(DOUBLE), Default(0.0)
    *  @param maxHourHostQps Database column max_hour_host_qps DBType(DOUBLE), Default(0.0)
    *  @param maxHostQps Database column max_host_qps DBType(DOUBLE), Default(0.0)
    *  @param startDay Database column start_day DBType(DATE)
    *  @param createTime Database column create_time DBType(BIGINT), Default(0) */
  case class ReportQpsPeakRow(id: Long, owt: String = "", appkey: String = "", count: Long = 0L, hostCount: Int = 0, avgQps: Double = 0.0, maxHourQps: Double = 0.0, minHourQps: Double = 0.0, avgHostQps: Double = 0.0, maxHourHostQps: Double = 0.0, maxHostQps: Double = 0.0, startDay: java.sql.Date, createTime: Long = 0L)
  /** GetResult implicit for fetching ReportQpsPeakRow objects using plain SQL queries */
  implicit def GetResultReportQpsPeakRow(implicit e0: GR[Long], e1: GR[String], e2: GR[Int], e3: GR[Double], e4: GR[java.sql.Date]): GR[ReportQpsPeakRow] = GR{
    prs => import prs._
      ReportQpsPeakRow.tupled((<<[Long], <<[String], <<[String], <<[Long], <<[Int], <<[Double], <<[Double], <<[Double], <<[Double], <<[Double], <<[Double], <<[java.sql.Date], <<[Long]))
  }
  /** Table description of table report_qps_peak. Objects of this class serve as prototypes for rows in queries. */
  class ReportQpsPeak(_tableTag: Tag) extends Table[ReportQpsPeakRow](_tableTag, "report_qps_peak") {
    def * = (id, owt, appkey, count, hostCount, avgQps, maxHourQps, minHourQps, avgHostQps, maxHourHostQps, maxHostQps, startDay, createTime) <> (ReportQpsPeakRow.tupled, ReportQpsPeakRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (id.?, owt.?, appkey.?, count.?, hostCount.?, avgQps.?, maxHourQps.?, minHourQps.?, avgHostQps.?, maxHourHostQps.?, maxHostQps.?, startDay.?, createTime.?).shaped.<>({r=>import r._; _1.map(_=> ReportQpsPeakRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6.get, _7.get, _8.get, _9.get, _10.get, _11.get, _12.get, _13.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id DBType(BIGINT), AutoInc, PrimaryKey */
    val id: Column[Long] = column[Long]("id", O.AutoInc, O.PrimaryKey)
    /** Database column owt DBType(VARCHAR), Length(20,true), Default() */
    val owt: Column[String] = column[String]("owt", O.Length(20,varying=true), O.Default(""))
    /** Database column appkey DBType(VARCHAR), Length(128,true), Default() */
    val appkey: Column[String] = column[String]("appkey", O.Length(128,varying=true), O.Default(""))
    /** Database column count DBType(BIGINT), Default(0) */
    val count: Column[Long] = column[Long]("count", O.Default(0L))
    /** Database column host_count DBType(INT), Default(0) */
    val hostCount: Column[Int] = column[Int]("host_count", O.Default(0))
    /** Database column avg_qps DBType(DOUBLE), Default(0.0) */
    val avgQps: Column[Double] = column[Double]("avg_qps", O.Default(0.0))
    /** Database column max_hour_qps DBType(DOUBLE), Default(0.0) */
    val maxHourQps: Column[Double] = column[Double]("max_hour_qps", O.Default(0.0))
    /** Database column min_hour_qps DBType(DOUBLE), Default(0.0) */
    val minHourQps: Column[Double] = column[Double]("min_hour_qps", O.Default(0.0))
    /** Database column avg_host_qps DBType(DOUBLE), Default(0.0) */
    val avgHostQps: Column[Double] = column[Double]("avg_host_qps", O.Default(0.0))
    /** Database column max_hour_host_qps DBType(DOUBLE), Default(0.0) */
    val maxHourHostQps: Column[Double] = column[Double]("max_hour_host_qps", O.Default(0.0))
    /** Database column max_host_qps DBType(DOUBLE), Default(0.0) */
    val maxHostQps: Column[Double] = column[Double]("max_host_qps", O.Default(0.0))
    /** Database column start_day DBType(DATE) */
    val startDay: Column[java.sql.Date] = column[java.sql.Date]("start_day")
    /** Database column create_time DBType(BIGINT), Default(0) */
    val createTime: Column[Long] = column[Long]("create_time", O.Default(0L))

    /** Index over (appkey,startDay) (database name idx_appkey_start_day) */
    val index1 = index("idx_appkey_start_day", (appkey, startDay))
    /** Index over (owt,startDay) (database name index_owt_start_day) */
    val index2 = index("index_owt_start_day", (owt, startDay))
    /** Uniqueness Index over (appkey,startDay) (database name indexappkey_start_day) */
    val index3 = index("indexappkey_start_day", (appkey, startDay), unique=true)
  }
  /** Collection-like TableQuery object for table ReportQpsPeak */
  lazy val ReportQpsPeak = new TableQuery(tag => new ReportQpsPeak(tag))

  /** Entity class storing rows of table ScannerLog
    *  @param id Database column id DBType(BIGINT), AutoInc, PrimaryKey
    *  @param appkey Database column appkey DBType(VARCHAR), Length(128,true), Default()
    *  @param env Database column env DBType(INT), Default(0)
    *  @param provider Database column provider DBType(VARCHAR), Length(64,true), Default()
    *  @param category Database column category DBType(VARCHAR), Length(128,true), Default()
    *  @param content Database column content DBType(MEDIUMTEXT), Length(16777215,true)
    *  @param time Database column time DBType(BIGINT), Default(0) */
  case class ScannerLogRow(id: Long, appkey: String = "", env: Int = 0, provider: String = "", category: String = "", content: String, time: Long = 0L)
  /** GetResult implicit for fetching ScannerLogRow objects using plain SQL queries */
  implicit def GetResultScannerLogRow(implicit e0: GR[Long], e1: GR[String], e2: GR[Int]): GR[ScannerLogRow] = GR{
    prs => import prs._
      ScannerLogRow.tupled((<<[Long], <<[String], <<[Int], <<[String], <<[String], <<[String], <<[Long]))
  }
  /** Table description of table scanner_log. Objects of this class serve as prototypes for rows in queries. */
  class ScannerLog(_tableTag: Tag) extends Table[ScannerLogRow](_tableTag, "scanner_log") {
    def * = (id, appkey, env, provider, category, content, time) <> (ScannerLogRow.tupled, ScannerLogRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (id.?, appkey.?, env.?, provider.?, category.?, content.?, time.?).shaped.<>({r=>import r._; _1.map(_=> ScannerLogRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6.get, _7.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id DBType(BIGINT), AutoInc, PrimaryKey */
    val id: Column[Long] = column[Long]("id", O.AutoInc, O.PrimaryKey)
    /** Database column appkey DBType(VARCHAR), Length(128,true), Default() */
    val appkey: Column[String] = column[String]("appkey", O.Length(128,varying=true), O.Default(""))
    /** Database column env DBType(INT), Default(0) */
    val env: Column[Int] = column[Int]("env", O.Default(0))
    /** Database column provider DBType(VARCHAR), Length(64,true), Default() */
    val provider: Column[String] = column[String]("provider", O.Length(64,varying=true), O.Default(""))
    /** Database column category DBType(VARCHAR), Length(128,true), Default() */
    val category: Column[String] = column[String]("category", O.Length(128,varying=true), O.Default(""))
    /** Database column content DBType(MEDIUMTEXT), Length(16777215,true) */
    val content: Column[String] = column[String]("content", O.Length(16777215,varying=true))
    /** Database column time DBType(BIGINT), Default(0) */
    val time: Column[Long] = column[Long]("time", O.Default(0L))

    /** Index over (appkey,env,provider,category) (database name idx_app) */
    val index1 = index("idx_app", (appkey, env, provider, category))
  }
  /** Collection-like TableQuery object for table ScannerLog */
  lazy val ScannerLog = new TableQuery(tag => new ScannerLog(tag))

  /** Entity class storing rows of table SchedulerCost
    *  @param id Database column id DBType(BIGINT), AutoInc, PrimaryKey
    *  @param name Database column name DBType(VARCHAR), Length(1024,true), Default()
    *  @param sTime Database column s_time DBType(BIGINT), Default(0)
    *  @param eTime Database column e_time DBType(BIGINT), Default(0) */
  case class SchedulerCostRow(id: Long, name: String = "", sTime: Long = 0L, eTime: Long = 0L)
  /** GetResult implicit for fetching SchedulerCostRow objects using plain SQL queries */
  implicit def GetResultSchedulerCostRow(implicit e0: GR[Long], e1: GR[String]): GR[SchedulerCostRow] = GR{
    prs => import prs._
      SchedulerCostRow.tupled((<<[Long], <<[String], <<[Long], <<[Long]))
  }
  /** Table description of table scheduler_cost. Objects of this class serve as prototypes for rows in queries. */
  class SchedulerCost(_tableTag: Tag) extends Table[SchedulerCostRow](_tableTag, "scheduler_cost") {
    def * = (id, name, sTime, eTime) <> (SchedulerCostRow.tupled, SchedulerCostRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (id.?, name.?, sTime.?, eTime.?).shaped.<>({r=>import r._; _1.map(_=> SchedulerCostRow.tupled((_1.get, _2.get, _3.get, _4.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id DBType(BIGINT), AutoInc, PrimaryKey */
    val id: Column[Long] = column[Long]("id", O.AutoInc, O.PrimaryKey)
    /** Database column name DBType(VARCHAR), Length(1024,true), Default() */
    val name: Column[String] = column[String]("name", O.Length(1024,varying=true), O.Default(""))
    /** Database column s_time DBType(BIGINT), Default(0) */
    val sTime: Column[Long] = column[Long]("s_time", O.Default(0L))
    /** Database column e_time DBType(BIGINT), Default(0) */
    val eTime: Column[Long] = column[Long]("e_time", O.Default(0L))

    /** Index over (name) (database name idx_name) */
    val index1 = index("idx_name", name)
  }
  /** Collection-like TableQuery object for table SchedulerCost */
  lazy val SchedulerCost = new TableQuery(tag => new SchedulerCost(tag))

  /** Entity class storing rows of table ServiceProvider
    *  @param id Database column id DBType(INT UNSIGNED), AutoInc, PrimaryKey
    *  @param name Database column name DBType(VARCHAR), Length(64,true), Default()
    *  @param appkey Database column appkey DBType(VARCHAR), Length(128,true), Default()
    *  @param version Database column version DBType(VARCHAR), Length(64,true), Default(None)
    *  @param thrifhttp Database column thrifhttp DBType(INT), Default(1)
    *  @param ip Database column ip DBType(VARCHAR), Length(20,true), Default()
    *  @param port Database column port DBType(INT), Default(None)
    *  @param weight Database column weight DBType(INT), Default(None)
    *  @param fweight Database column fweight DBType(DOUBLE), Default(None)
    *  @param status Database column status DBType(INT), Default(0)
    *  @param enabled Database column enabled DBType(INT), Default(0)
    *  @param role Database column role DBType(INT), Default(None)
    *  @param env Database column env DBType(INT), Default(0)
    *  @param lastupdatetime Database column lastUpdateTime DBType(BIGINT), Default(None)
    *  @param trace Database column trace DBType(INT), Default(None)
    *  @param extend Database column extend DBType(VARCHAR), Length(64,true), Default(Some())
    *  @param creattime Database column creatTime DBType(BIGINT) */
  case class ServiceProviderRow(id: Int, name: String = "", appkey: String = "", version: Option[String] = None, thrifhttp: Int = 1, ip: String = "", port: Option[Int] = None, weight: Option[Int] = None, fweight: Option[Double] = None, status: Int = 0, enabled: Int = 0, role: Option[Int] = None, env: Int = 0, lastupdatetime: Option[Long] = None, trace: Option[Int] = None, extend: Option[String] = Some(""), creattime: Long)
  /** GetResult implicit for fetching ServiceProviderRow objects using plain SQL queries */
  implicit def GetResultServiceProviderRow(implicit e0: GR[Int], e1: GR[String], e2: GR[Option[String]], e3: GR[Option[Int]], e4: GR[Option[Double]], e5: GR[Option[Long]], e6: GR[Long]): GR[ServiceProviderRow] = GR{
    prs => import prs._
      ServiceProviderRow.tupled((<<[Int], <<[String], <<[String], <<?[String], <<[Int], <<[String], <<?[Int], <<?[Int], <<?[Double], <<[Int], <<[Int], <<?[Int], <<[Int], <<?[Long], <<?[Int], <<?[String], <<[Long]))
  }
  /** Table description of table service_provider. Objects of this class serve as prototypes for rows in queries. */
  class ServiceProvider(_tableTag: Tag) extends Table[ServiceProviderRow](_tableTag, "service_provider") {
    def * = (id, name, appkey, version, thrifhttp, ip, port, weight, fweight, status, enabled, role, env, lastupdatetime, trace, extend, creattime) <> (ServiceProviderRow.tupled, ServiceProviderRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (id.?, name.?, appkey.?, version, thrifhttp.?, ip.?, port, weight, fweight, status.?, enabled.?, role, env.?, lastupdatetime, trace, extend, creattime.?).shaped.<>({r=>import r._; _1.map(_=> ServiceProviderRow.tupled((_1.get, _2.get, _3.get, _4, _5.get, _6.get, _7, _8, _9, _10.get, _11.get, _12, _13.get, _14, _15, _16, _17.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id DBType(INT UNSIGNED), AutoInc, PrimaryKey */
    val id: Column[Int] = column[Int]("id", O.AutoInc, O.PrimaryKey)
    /** Database column name DBType(VARCHAR), Length(64,true), Default() */
    val name: Column[String] = column[String]("name", O.Length(64,varying=true), O.Default(""))
    /** Database column appkey DBType(VARCHAR), Length(128,true), Default() */
    val appkey: Column[String] = column[String]("appkey", O.Length(128,varying=true), O.Default(""))
    /** Database column version DBType(VARCHAR), Length(64,true), Default(None) */
    val version: Column[Option[String]] = column[Option[String]]("version", O.Length(64,varying=true), O.Default(None))
    /** Database column thrifhttp DBType(INT), Default(1) */
    val thrifhttp: Column[Int] = column[Int]("thrifhttp", O.Default(1))
    /** Database column ip DBType(VARCHAR), Length(20,true), Default() */
    val ip: Column[String] = column[String]("ip", O.Length(20,varying=true), O.Default(""))
    /** Database column port DBType(INT), Default(None) */
    val port: Column[Option[Int]] = column[Option[Int]]("port", O.Default(None))
    /** Database column weight DBType(INT), Default(None) */
    val weight: Column[Option[Int]] = column[Option[Int]]("weight", O.Default(None))
    /** Database column fweight DBType(DOUBLE), Default(None) */
    val fweight: Column[Option[Double]] = column[Option[Double]]("fweight", O.Default(None))
    /** Database column status DBType(INT), Default(0) */
    val status: Column[Int] = column[Int]("status", O.Default(0))
    /** Database column enabled DBType(INT), Default(0) */
    val enabled: Column[Int] = column[Int]("enabled", O.Default(0))
    /** Database column role DBType(INT), Default(None) */
    val role: Column[Option[Int]] = column[Option[Int]]("role", O.Default(None))
    /** Database column env DBType(INT), Default(0) */
    val env: Column[Int] = column[Int]("env", O.Default(0))
    /** Database column lastUpdateTime DBType(BIGINT), Default(None) */
    val lastupdatetime: Column[Option[Long]] = column[Option[Long]]("lastUpdateTime", O.Default(None))
    /** Database column trace DBType(INT), Default(None) */
    val trace: Column[Option[Int]] = column[Option[Int]]("trace", O.Default(None))
    /** Database column extend DBType(VARCHAR), Length(64,true), Default(Some()) */
    val extend: Column[Option[String]] = column[Option[String]]("extend", O.Length(64,varying=true), O.Default(Some("")))
    /** Database column creatTime DBType(BIGINT) */
    val creattime: Column[Long] = column[Long]("creatTime")

    /** Index over (appkey,env) (database name appkey:env) */
    val index1 = index("appkey:env", (appkey, env))
    /** Index over (creattime) (database name creatTime) */
    val index2 = index("creatTime", creattime)
    /** Index over (ip) (database name ip) */
    val index3 = index("ip", ip)
    /** Index over (status) (database name status) */
    val index4 = index("status", status)
    /** Index over (thrifhttp) (database name thrifhttp) */
    val index5 = index("thrifhttp", thrifhttp)
  }
  /** Collection-like TableQuery object for table ServiceProvider */
  lazy val ServiceProvider = new TableQuery(tag => new ServiceProvider(tag))

  /** Entity class storing rows of table SgAgentLog
    *  @param id Database column id DBType(BIGINT), AutoInc, PrimaryKey
    *  @param appkey Database column appkey DBType(VARCHAR), Length(128,true), Default()
    *  @param time Database column time DBType(BIGINT), Default(0)
    *  @param level Database column level DBType(INT), Default(0)
    *  @param content Database column content DBType(MEDIUMTEXT), Length(16777215,true) */
  case class SgAgentLogRow(id: Long, appkey: String = "", time: Long = 0L, level: Int = 0, content: String)
  /** GetResult implicit for fetching SgAgentLogRow objects using plain SQL queries */
  implicit def GetResultSgAgentLogRow(implicit e0: GR[Long], e1: GR[String], e2: GR[Int]): GR[SgAgentLogRow] = GR{
    prs => import prs._
      SgAgentLogRow.tupled((<<[Long], <<[String], <<[Long], <<[Int], <<[String]))
  }
  /** Table description of table sg_agent_log. Objects of this class serve as prototypes for rows in queries. */
  class SgAgentLog(_tableTag: Tag) extends Table[SgAgentLogRow](_tableTag, "sg_agent_log") {
    def * = (id, appkey, time, level, content) <> (SgAgentLogRow.tupled, SgAgentLogRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (id.?, appkey.?, time.?, level.?, content.?).shaped.<>({r=>import r._; _1.map(_=> SgAgentLogRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id DBType(BIGINT), AutoInc, PrimaryKey */
    val id: Column[Long] = column[Long]("id", O.AutoInc, O.PrimaryKey)
    /** Database column appkey DBType(VARCHAR), Length(128,true), Default() */
    val appkey: Column[String] = column[String]("appkey", O.Length(128,varying=true), O.Default(""))
    /** Database column time DBType(BIGINT), Default(0) */
    val time: Column[Long] = column[Long]("time", O.Default(0L))
    /** Database column level DBType(INT), Default(0) */
    val level: Column[Int] = column[Int]("level", O.Default(0))
    /** Database column content DBType(MEDIUMTEXT), Length(16777215,true) */
    val content: Column[String] = column[String]("content", O.Length(16777215,varying=true))

    /** Index over (time) (database name idx_time) */
    val index1 = index("idx_time", time)
  }
  /** Collection-like TableQuery object for table SgAgentLog */
  lazy val SgAgentLog = new TableQuery(tag => new SgAgentLog(tag))

  /** Entity class storing rows of table SpanKpiDay
    *  @param id Database column id DBType(INT UNSIGNED), AutoInc, PrimaryKey
    *  @param dt Database column dt DBType(DATETIME)
    *  @param appkey Database column appkey DBType(VARCHAR), Length(64,true), Default(None)
    *  @param spanName Database column span_name DBType(VARCHAR), Length(256,true), Default(None)
    *  @param cnt Database column cnt DBType(INT), Default(None)
    *  @param sampleCnt Database column sample_cnt DBType(INT), Default(None)
    *  @param qps Database column qps DBType(DECIMAL), Default(None)
    *  @param sampleQps Database column sample_qps DBType(DECIMAL), Default(None)
    *  @param cost50th Database column cost_50th DBType(DECIMAL), Default(None)
    *  @param cost90th Database column cost_90th DBType(DECIMAL), Default(None)
    *  @param cost95th Database column cost_95th DBType(DECIMAL), Default(None)
    *  @param costMax Database column cost_max DBType(DECIMAL), Default(None) */
  case class SpanKpiDayRow(id: Int, dt: java.sql.Timestamp, appkey: Option[String] = None, spanName: Option[String] = None, cnt: Option[Int] = None, sampleCnt: Option[Int] = None, qps: Option[scala.math.BigDecimal] = None, sampleQps: Option[scala.math.BigDecimal] = None, cost50th: Option[scala.math.BigDecimal] = None, cost90th: Option[scala.math.BigDecimal] = None, cost95th: Option[scala.math.BigDecimal] = None, costMax: Option[scala.math.BigDecimal] = None)
  /** GetResult implicit for fetching SpanKpiDayRow objects using plain SQL queries */
  implicit def GetResultSpanKpiDayRow(implicit e0: GR[Int], e1: GR[java.sql.Timestamp], e2: GR[Option[String]], e3: GR[Option[Int]], e4: GR[Option[scala.math.BigDecimal]]): GR[SpanKpiDayRow] = GR{
    prs => import prs._
      SpanKpiDayRow.tupled((<<[Int], <<[java.sql.Timestamp], <<?[String], <<?[String], <<?[Int], <<?[Int], <<?[scala.math.BigDecimal], <<?[scala.math.BigDecimal], <<?[scala.math.BigDecimal], <<?[scala.math.BigDecimal], <<?[scala.math.BigDecimal], <<?[scala.math.BigDecimal]))
  }
  /** Table description of table span_kpi_day. Objects of this class serve as prototypes for rows in queries. */
  class SpanKpiDay(_tableTag: Tag) extends Table[SpanKpiDayRow](_tableTag, "span_kpi_day") {
    def * = (id, dt, appkey, spanName, cnt, sampleCnt, qps, sampleQps, cost50th, cost90th, cost95th, costMax) <> (SpanKpiDayRow.tupled, SpanKpiDayRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (id.?, dt.?, appkey, spanName, cnt, sampleCnt, qps, sampleQps, cost50th, cost90th, cost95th, costMax).shaped.<>({r=>import r._; _1.map(_=> SpanKpiDayRow.tupled((_1.get, _2.get, _3, _4, _5, _6, _7, _8, _9, _10, _11, _12)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id DBType(INT UNSIGNED), AutoInc, PrimaryKey */
    val id: Column[Int] = column[Int]("id", O.AutoInc, O.PrimaryKey)
    /** Database column dt DBType(DATETIME) */
    val dt: Column[java.sql.Timestamp] = column[java.sql.Timestamp]("dt")
    /** Database column appkey DBType(VARCHAR), Length(64,true), Default(None) */
    val appkey: Column[Option[String]] = column[Option[String]]("appkey", O.Length(64,varying=true), O.Default(None))
    /** Database column span_name DBType(VARCHAR), Length(256,true), Default(None) */
    val spanName: Column[Option[String]] = column[Option[String]]("span_name", O.Length(256,varying=true), O.Default(None))
    /** Database column cnt DBType(INT), Default(None) */
    val cnt: Column[Option[Int]] = column[Option[Int]]("cnt", O.Default(None))
    /** Database column sample_cnt DBType(INT), Default(None) */
    val sampleCnt: Column[Option[Int]] = column[Option[Int]]("sample_cnt", O.Default(None))
    /** Database column qps DBType(DECIMAL), Default(None) */
    val qps: Column[Option[scala.math.BigDecimal]] = column[Option[scala.math.BigDecimal]]("qps", O.Default(None))
    /** Database column sample_qps DBType(DECIMAL), Default(None) */
    val sampleQps: Column[Option[scala.math.BigDecimal]] = column[Option[scala.math.BigDecimal]]("sample_qps", O.Default(None))
    /** Database column cost_50th DBType(DECIMAL), Default(None) */
    val cost50th: Column[Option[scala.math.BigDecimal]] = column[Option[scala.math.BigDecimal]]("cost_50th", O.Default(None))
    /** Database column cost_90th DBType(DECIMAL), Default(None) */
    val cost90th: Column[Option[scala.math.BigDecimal]] = column[Option[scala.math.BigDecimal]]("cost_90th", O.Default(None))
    /** Database column cost_95th DBType(DECIMAL), Default(None) */
    val cost95th: Column[Option[scala.math.BigDecimal]] = column[Option[scala.math.BigDecimal]]("cost_95th", O.Default(None))
    /** Database column cost_max DBType(DECIMAL), Default(None) */
    val costMax: Column[Option[scala.math.BigDecimal]] = column[Option[scala.math.BigDecimal]]("cost_max", O.Default(None))

    /** Index over (dt) (database name idx_time) */
    val index1 = index("idx_time", dt)
  }
  /** Collection-like TableQuery object for table SpanKpiDay */
  lazy val SpanKpiDay = new TableQuery(tag => new SpanKpiDay(tag))

  /** Entity class storing rows of table Switchenv
    *  @param id Database column id DBType(BIGINT), AutoInc, PrimaryKey
    *  @param applyMisid Database column apply_misid DBType(VARCHAR), Length(45,true), Default(None)
    *  @param applyTime Database column apply_time DBType(BIGINT), Default(None)
    *  @param ip Database column ip DBType(VARCHAR), Length(20,true), Default(None)
    *  @param oldEnv Database column old_env DBType(VARCHAR), Length(5,true), Default(None)
    *  @param newEnv Database column new_env DBType(VARCHAR), Length(5,true), Default(None)
    *  @param comfirmMisid Database column comfirm_misid DBType(VARCHAR), Length(45,true), Default(None)
    *  @param comfirmTime Database column comfirm_time DBType(BIGINT), Default(None)
    *  @param flag Database column flag DBType(INT), Default(Some(0))
    *  @param note Database column note DBType(VARCHAR), Length(2000,true), Default(Some()) */
  case class SwitchenvRow(id: Long, applyMisid: Option[String] = None, applyTime: Option[Long] = None, ip: Option[String] = None, oldEnv: Option[String] = None, newEnv: Option[String] = None, comfirmMisid: Option[String] = None, comfirmTime: Option[Long] = None, flag: Option[Int] = Some(0), note: Option[String] = Some(""))
  /** GetResult implicit for fetching SwitchenvRow objects using plain SQL queries */
  implicit def GetResultSwitchenvRow(implicit e0: GR[Long], e1: GR[Option[String]], e2: GR[Option[Long]], e3: GR[Option[Int]]): GR[SwitchenvRow] = GR{
    prs => import prs._
      SwitchenvRow.tupled((<<[Long], <<?[String], <<?[Long], <<?[String], <<?[String], <<?[String], <<?[String], <<?[Long], <<?[Int], <<?[String]))
  }
  /** Table description of table switchEnv. Objects of this class serve as prototypes for rows in queries. */
  class Switchenv(_tableTag: Tag) extends Table[SwitchenvRow](_tableTag, "switchEnv") {
    def * = (id, applyMisid, applyTime, ip, oldEnv, newEnv, comfirmMisid, comfirmTime, flag, note) <> (SwitchenvRow.tupled, SwitchenvRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (id.?, applyMisid, applyTime, ip, oldEnv, newEnv, comfirmMisid, comfirmTime, flag, note).shaped.<>({r=>import r._; _1.map(_=> SwitchenvRow.tupled((_1.get, _2, _3, _4, _5, _6, _7, _8, _9, _10)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id DBType(BIGINT), AutoInc, PrimaryKey */
    val id: Column[Long] = column[Long]("id", O.AutoInc, O.PrimaryKey)
    /** Database column apply_misid DBType(VARCHAR), Length(45,true), Default(None) */
    val applyMisid: Column[Option[String]] = column[Option[String]]("apply_misid", O.Length(45,varying=true), O.Default(None))
    /** Database column apply_time DBType(BIGINT), Default(None) */
    val applyTime: Column[Option[Long]] = column[Option[Long]]("apply_time", O.Default(None))
    /** Database column ip DBType(VARCHAR), Length(20,true), Default(None) */
    val ip: Column[Option[String]] = column[Option[String]]("ip", O.Length(20,varying=true), O.Default(None))
    /** Database column old_env DBType(VARCHAR), Length(5,true), Default(None) */
    val oldEnv: Column[Option[String]] = column[Option[String]]("old_env", O.Length(5,varying=true), O.Default(None))
    /** Database column new_env DBType(VARCHAR), Length(5,true), Default(None) */
    val newEnv: Column[Option[String]] = column[Option[String]]("new_env", O.Length(5,varying=true), O.Default(None))
    /** Database column comfirm_misid DBType(VARCHAR), Length(45,true), Default(None) */
    val comfirmMisid: Column[Option[String]] = column[Option[String]]("comfirm_misid", O.Length(45,varying=true), O.Default(None))
    /** Database column comfirm_time DBType(BIGINT), Default(None) */
    val comfirmTime: Column[Option[Long]] = column[Option[Long]]("comfirm_time", O.Default(None))
    /** Database column flag DBType(INT), Default(Some(0)) */
    val flag: Column[Option[Int]] = column[Option[Int]]("flag", O.Default(Some(0)))
    /** Database column note DBType(VARCHAR), Length(2000,true), Default(Some()) */
    val note: Column[Option[String]] = column[Option[String]]("note", O.Length(2000,varying=true), O.Default(Some("")))
  }
  /** Collection-like TableQuery object for table Switchenv */
  lazy val Switchenv = new TableQuery(tag => new Switchenv(tag))

  /** Entity class storing rows of table TaskLog
    *  @param id Database column id DBType(BIGINT), AutoInc, PrimaryKey
    *  @param uuid Database column uuid DBType(VARCHAR), Length(64,true), Default()
    *  @param cmdType Database column cmd_type DBType(INT), Default(0)
    *  @param appkey Database column appkey DBType(VARCHAR), Length(128,true), Default()
    *  @param env Database column env DBType(INT), Default(1)
    *  @param status Database column status DBType(INT), Default(0)
    *  @param skey Database column skey DBType(VARCHAR), Length(255,true), Default()
    *  @param sdata Database column sdata DBType(MEDIUMTEXT), Length(16777215,true)
    *  @param sreturn Database column sreturn DBType(MEDIUMTEXT), Length(16777215,true)
    *  @param ctime Database column ctime DBType(INT), Default(0)
    *  @param ltime Database column ltime DBType(INT), Default(0) */
  case class TaskLogRow(id: Long, uuid: String = "", cmdType: Int = 0, appkey: String = "", env: Int = 1, status: Int = 0, skey: String = "", sdata: String, sreturn: String, ctime: Int = 0, ltime: Int = 0)
  /** GetResult implicit for fetching TaskLogRow objects using plain SQL queries */
  implicit def GetResultTaskLogRow(implicit e0: GR[Long], e1: GR[String], e2: GR[Int]): GR[TaskLogRow] = GR{
    prs => import prs._
      TaskLogRow.tupled((<<[Long], <<[String], <<[Int], <<[String], <<[Int], <<[Int], <<[String], <<[String], <<[String], <<[Int], <<[Int]))
  }
  /** Table description of table task_log. Objects of this class serve as prototypes for rows in queries. */
  class TaskLog(_tableTag: Tag) extends Table[TaskLogRow](_tableTag, "task_log") {
    def * = (id, uuid, cmdType, appkey, env, status, skey, sdata, sreturn, ctime, ltime) <> (TaskLogRow.tupled, TaskLogRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (id.?, uuid.?, cmdType.?, appkey.?, env.?, status.?, skey.?, sdata.?, sreturn.?, ctime.?, ltime.?).shaped.<>({r=>import r._; _1.map(_=> TaskLogRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6.get, _7.get, _8.get, _9.get, _10.get, _11.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id DBType(BIGINT), AutoInc, PrimaryKey */
    val id: Column[Long] = column[Long]("id", O.AutoInc, O.PrimaryKey)
    /** Database column uuid DBType(VARCHAR), Length(64,true), Default() */
    val uuid: Column[String] = column[String]("uuid", O.Length(64,varying=true), O.Default(""))
    /** Database column cmd_type DBType(INT), Default(0) */
    val cmdType: Column[Int] = column[Int]("cmd_type", O.Default(0))
    /** Database column appkey DBType(VARCHAR), Length(128,true), Default() */
    val appkey: Column[String] = column[String]("appkey", O.Length(128,varying=true), O.Default(""))
    /** Database column env DBType(INT), Default(1) */
    val env: Column[Int] = column[Int]("env", O.Default(1))
    /** Database column status DBType(INT), Default(0) */
    val status: Column[Int] = column[Int]("status", O.Default(0))
    /** Database column skey DBType(VARCHAR), Length(255,true), Default() */
    val skey: Column[String] = column[String]("skey", O.Length(255,varying=true), O.Default(""))
    /** Database column sdata DBType(MEDIUMTEXT), Length(16777215,true) */
    val sdata: Column[String] = column[String]("sdata", O.Length(16777215,varying=true))
    /** Database column sreturn DBType(MEDIUMTEXT), Length(16777215,true) */
    val sreturn: Column[String] = column[String]("sreturn", O.Length(16777215,varying=true))
    /** Database column ctime DBType(INT), Default(0) */
    val ctime: Column[Int] = column[Int]("ctime", O.Default(0))
    /** Database column ltime DBType(INT), Default(0) */
    val ltime: Column[Int] = column[Int]("ltime", O.Default(0))

    /** Index over (uuid,appkey,env,skey) (database name idx_app) */
    val index1 = index("idx_app", (uuid, appkey, env, skey))
    /** Index over (cmdType) (database name idx_cmdType) */
    val index2 = index("idx_cmdType", cmdType)
    /** Index over (ctime) (database name idx_ctime) */
    val index3 = index("idx_ctime", ctime)
  }
  /** Collection-like TableQuery object for table TaskLog */
  lazy val TaskLog = new TableQuery(tag => new TaskLog(tag))

  /** Entity class storing rows of table TriggerEvent
    *  @param id Database column id DBType(BIGINT), AutoInc, PrimaryKey
    *  @param triggerId Database column trigger_id DBType(BIGINT)
    *  @param status Database column status DBType(INT), Default(0)
    *  @param createTime Database column create_time DBType(BIGINT), Default(0)
    *  @param lastTime Database column last_time DBType(BIGINT), Default(0)
    *  @param count Database column count DBType(INT), Default(0)
    *  @param ackTime Database column ack_time DBType(BIGINT), Default(0)
    *  @param ackUser Database column ack_user DBType(VARCHAR), Length(128,true), Default()
    *  @param message Database column message DBType(VARCHAR), Length(4096,true), Default() */
  case class TriggerEventRow(id: Long, triggerId: Long, status: Int = 0, createTime: Long = 0L, lastTime: Long = 0L, count: Int = 0, ackTime: Long = 0L, ackUser: String = "", message: String = "")
  /** GetResult implicit for fetching TriggerEventRow objects using plain SQL queries */
  implicit def GetResultTriggerEventRow(implicit e0: GR[Long], e1: GR[Int], e2: GR[String]): GR[TriggerEventRow] = GR{
    prs => import prs._
      TriggerEventRow.tupled((<<[Long], <<[Long], <<[Int], <<[Long], <<[Long], <<[Int], <<[Long], <<[String], <<[String]))
  }
  /** Table description of table trigger_event. Objects of this class serve as prototypes for rows in queries. */
  class TriggerEvent(_tableTag: Tag) extends Table[TriggerEventRow](_tableTag, "trigger_event") {
    def * = (id, triggerId, status, createTime, lastTime, count, ackTime, ackUser, message) <> (TriggerEventRow.tupled, TriggerEventRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (id.?, triggerId.?, status.?, createTime.?, lastTime.?, count.?, ackTime.?, ackUser.?, message.?).shaped.<>({r=>import r._; _1.map(_=> TriggerEventRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6.get, _7.get, _8.get, _9.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id DBType(BIGINT), AutoInc, PrimaryKey */
    val id: Column[Long] = column[Long]("id", O.AutoInc, O.PrimaryKey)
    /** Database column trigger_id DBType(BIGINT) */
    val triggerId: Column[Long] = column[Long]("trigger_id")
    /** Database column status DBType(INT), Default(0) */
    val status: Column[Int] = column[Int]("status", O.Default(0))
    /** Database column create_time DBType(BIGINT), Default(0) */
    val createTime: Column[Long] = column[Long]("create_time", O.Default(0L))
    /** Database column last_time DBType(BIGINT), Default(0) */
    val lastTime: Column[Long] = column[Long]("last_time", O.Default(0L))
    /** Database column count DBType(INT), Default(0) */
    val count: Column[Int] = column[Int]("count", O.Default(0))
    /** Database column ack_time DBType(BIGINT), Default(0) */
    val ackTime: Column[Long] = column[Long]("ack_time", O.Default(0L))
    /** Database column ack_user DBType(VARCHAR), Length(128,true), Default() */
    val ackUser: Column[String] = column[String]("ack_user", O.Length(128,varying=true), O.Default(""))
    /** Database column message DBType(VARCHAR), Length(4096,true), Default() */
    val message: Column[String] = column[String]("message", O.Length(4096,varying=true), O.Default(""))

    /** Index over (createTime) (database name idx_time) */
    val index1 = index("idx_time", createTime)
  }
  /** Collection-like TableQuery object for table TriggerEvent */
  lazy val TriggerEvent = new TableQuery(tag => new TriggerEvent(tag))

  /** Entity class storing rows of table TriggerSubscribe
    *  @param id Database column id DBType(BIGINT), AutoInc, PrimaryKey
    *  @param appkey Database column appkey DBType(VARCHAR), Length(128,true), Default()
    *  @param triggerId Database column trigger_id DBType(BIGINT), Default(0)
    *  @param userId Database column user_id DBType(INT), Default(0)
    *  @param userLogin Database column user_login DBType(VARCHAR), Length(32,true), Default()
    *  @param userName Database column user_name DBType(VARCHAR), Length(32,true), Default()
    *  @param xm Database column xm DBType(TINYINT), Default(0)
    *  @param sms Database column sms DBType(TINYINT), Default(0)
    *  @param email Database column email DBType(TINYINT), Default(0) */
  case class TriggerSubscribeRow(id: Long, appkey: String = "", triggerId: Long = 0L, userId: Int = 0, userLogin: String = "", userName: String = "", xm: Byte = 0, sms: Byte = 0, email: Byte = 0)
  /** GetResult implicit for fetching TriggerSubscribeRow objects using plain SQL queries */
  implicit def GetResultTriggerSubscribeRow(implicit e0: GR[Long], e1: GR[String], e2: GR[Int], e3: GR[Byte]): GR[TriggerSubscribeRow] = GR{
    prs => import prs._
      TriggerSubscribeRow.tupled((<<[Long], <<[String], <<[Long], <<[Int], <<[String], <<[String], <<[Byte], <<[Byte], <<[Byte]))
  }
  /** Table description of table trigger_subscribe. Objects of this class serve as prototypes for rows in queries. */
  class TriggerSubscribe(_tableTag: Tag) extends Table[TriggerSubscribeRow](_tableTag, "trigger_subscribe") {
    def * = (id, appkey, triggerId, userId, userLogin, userName, xm, sms, email) <> (TriggerSubscribeRow.tupled, TriggerSubscribeRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (id.?, appkey.?, triggerId.?, userId.?, userLogin.?, userName.?, xm.?, sms.?, email.?).shaped.<>({r=>import r._; _1.map(_=> TriggerSubscribeRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6.get, _7.get, _8.get, _9.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id DBType(BIGINT), AutoInc, PrimaryKey */
    val id: Column[Long] = column[Long]("id", O.AutoInc, O.PrimaryKey)
    /** Database column appkey DBType(VARCHAR), Length(128,true), Default() */
    val appkey: Column[String] = column[String]("appkey", O.Length(128,varying=true), O.Default(""))
    /** Database column trigger_id DBType(BIGINT), Default(0) */
    val triggerId: Column[Long] = column[Long]("trigger_id", O.Default(0L))
    /** Database column user_id DBType(INT), Default(0) */
    val userId: Column[Int] = column[Int]("user_id", O.Default(0))
    /** Database column user_login DBType(VARCHAR), Length(32,true), Default() */
    val userLogin: Column[String] = column[String]("user_login", O.Length(32,varying=true), O.Default(""))
    /** Database column user_name DBType(VARCHAR), Length(32,true), Default() */
    val userName: Column[String] = column[String]("user_name", O.Length(32,varying=true), O.Default(""))
    /** Database column xm DBType(TINYINT), Default(0) */
    val xm: Column[Byte] = column[Byte]("xm", O.Default(0))
    /** Database column sms DBType(TINYINT), Default(0) */
    val sms: Column[Byte] = column[Byte]("sms", O.Default(0))
    /** Database column email DBType(TINYINT), Default(0) */
    val email: Column[Byte] = column[Byte]("email", O.Default(0))

    /** Index over (userId) (database name idx_user) */
    val index1 = index("idx_user", userId)
  }
  /** Collection-like TableQuery object for table TriggerSubscribe */
  lazy val TriggerSubscribe = new TableQuery(tag => new TriggerSubscribe(tag))

  /** Entity class storing rows of table UserShortcut
    *  @param id Database column id DBType(BIGINT), AutoInc, PrimaryKey
    *  @param login Database column login DBType(VARCHAR), Length(128,true), Default()
    *  @param title Database column title DBType(VARCHAR), Length(2048,true), Default()
    *  @param url Database column url DBType(VARCHAR), Length(2048,true), Default()
    *  @param appkey Database column appkey DBType(VARCHAR), Length(128,true), Default() */
  case class UserShortcutRow(id: Long, login: String = "", title: String = "", url: String = "", appkey: String = "")
  /** GetResult implicit for fetching UserShortcutRow objects using plain SQL queries */
  implicit def GetResultUserShortcutRow(implicit e0: GR[Long], e1: GR[String]): GR[UserShortcutRow] = GR{
    prs => import prs._
      UserShortcutRow.tupled((<<[Long], <<[String], <<[String], <<[String], <<[String]))
  }
  /** Table description of table user_shortcut. Objects of this class serve as prototypes for rows in queries. */
  class UserShortcut(_tableTag: Tag) extends Table[UserShortcutRow](_tableTag, "user_shortcut") {
    def * = (id, login, title, url, appkey) <> (UserShortcutRow.tupled, UserShortcutRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (id.?, login.?, title.?, url.?, appkey.?).shaped.<>({r=>import r._; _1.map(_=> UserShortcutRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id DBType(BIGINT), AutoInc, PrimaryKey */
    val id: Column[Long] = column[Long]("id", O.AutoInc, O.PrimaryKey)
    /** Database column login DBType(VARCHAR), Length(128,true), Default() */
    val login: Column[String] = column[String]("login", O.Length(128,varying=true), O.Default(""))
    /** Database column title DBType(VARCHAR), Length(2048,true), Default() */
    val title: Column[String] = column[String]("title", O.Length(2048,varying=true), O.Default(""))
    /** Database column url DBType(VARCHAR), Length(2048,true), Default() */
    val url: Column[String] = column[String]("url", O.Length(2048,varying=true), O.Default(""))
    /** Database column appkey DBType(VARCHAR), Length(128,true), Default() */
    val appkey: Column[String] = column[String]("appkey", O.Length(128,varying=true), O.Default(""))
  }
  /** Collection-like TableQuery object for table UserShortcut */
  lazy val UserShortcut = new TableQuery(tag => new UserShortcut(tag))
}
