package com.sankuai.octo.oswatch.db
// AUTO-GENERATED Slick data model
/** Stand-alone Slick data model for immediate use */
object Tables extends {
  val profile = scala.slick.driver.MySQLDriver
} with Tables

/** Slick data model trait for extension, choice of backend or usage in the cake pattern. (Make sure to initialize this late.) */
trait Tables {
  val profile: scala.slick.driver.JdbcProfile
  import profile.simple._
  import scala.slick.model.ForeignKeyAction
  // NOTE: GetResult mappers for plain SQL are only generated for tables where Slick knows how to map the types of all columns.
  import scala.slick.jdbc.{GetResult => GR}
  
  /** DDL for all tables. Call .create to execute. */
  lazy val ddl = OswatchMonitorPolicy.ddl
  
  /** Entity class storing rows of table OswatchMonitorPolicy
   *  @param id Database column id DBType(BIGINT), AutoInc, PrimaryKey
   *  @param appkey Database column appkey DBType(VARCHAR), Length(128,true), Default()
   *  @param idc Database column idc DBType(VARCHAR), Length(128,true), Default(None)
   *  @param env Database column env DBType(INT), Default(0)
   *  @param gtetype Database column gteType DBType(INT), Default(0)
   *  @param watchperiod Database column watchPeriod DBType(INT), Default(0)
   *  @param monitorType Database column monitor_type DBType(INT)
   *  @param monitorvalue Database column monitorValue DBType(DOUBLE)
   *  @param spanName Database column span_name DBType(VARCHAR), Length(256,true), Default(None)
   *  @param responseurl Database column responseUrl DBType(VARCHAR), Length(512,true), Default()
   *  @param providerCountSwitch Database column provider_count_switch DBType(INT), Default(0) */
  case class OswatchMonitorPolicyRow(id: Long, appkey: String = "", idc: Option[String] = None, env: Int = 0, gtetype: Int = 0, watchperiod: Int = 0, monitorType: Int, monitorvalue: Double, spanName: Option[String] = None, responseurl: String = "", providerCountSwitch: Int = 0)
  /** GetResult implicit for fetching OswatchMonitorPolicyRow objects using plain SQL queries */
  implicit def GetResultOswatchMonitorPolicyRow(implicit e0: GR[Long], e1: GR[String], e2: GR[Option[String]], e3: GR[Int], e4: GR[Double]): GR[OswatchMonitorPolicyRow] = GR{
    prs => import prs._
    OswatchMonitorPolicyRow.tupled((<<[Long], <<[String], <<?[String], <<[Int], <<[Int], <<[Int], <<[Int], <<[Double], <<?[String], <<[String], <<[Int]))
  }
  /** Table description of table oswatch_monitor_policy. Objects of this class serve as prototypes for rows in queries. */
  class OswatchMonitorPolicy(_tableTag: Tag) extends Table[OswatchMonitorPolicyRow](_tableTag, "oswatch_monitor_policy") {
    def * = (id, appkey, idc, env, gtetype, watchperiod, monitorType, monitorvalue, spanName, responseurl, providerCountSwitch) <> (OswatchMonitorPolicyRow.tupled, OswatchMonitorPolicyRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (id.?, appkey.?, idc, env.?, gtetype.?, watchperiod.?, monitorType.?, monitorvalue.?, spanName, responseurl.?, providerCountSwitch.?).shaped.<>({r=>import r._; _1.map(_=> OswatchMonitorPolicyRow.tupled((_1.get, _2.get, _3, _4.get, _5.get, _6.get, _7.get, _8.get, _9, _10.get, _11.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))
    
    /** Database column id DBType(BIGINT), AutoInc, PrimaryKey */
    val id: Column[Long] = column[Long]("id", O.AutoInc, O.PrimaryKey)
    /** Database column appkey DBType(VARCHAR), Length(128,true), Default() */
    val appkey: Column[String] = column[String]("appkey", O.Length(128,varying=true), O.Default(""))
    /** Database column idc DBType(VARCHAR), Length(128,true), Default(None) */
    val idc: Column[Option[String]] = column[Option[String]]("idc", O.Length(128,varying=true), O.Default(None))
    /** Database column env DBType(INT), Default(0) */
    val env: Column[Int] = column[Int]("env", O.Default(0))
    /** Database column gteType DBType(INT), Default(0) */
    val gtetype: Column[Int] = column[Int]("gteType", O.Default(0))
    /** Database column watchPeriod DBType(INT), Default(0) */
    val watchperiod: Column[Int] = column[Int]("watchPeriod", O.Default(0))
    /** Database column monitor_type DBType(INT) */
    val monitorType: Column[Int] = column[Int]("monitor_type")
    /** Database column monitorValue DBType(DOUBLE) */
    val monitorvalue: Column[Double] = column[Double]("monitorValue")
    /** Database column span_name DBType(VARCHAR), Length(256,true), Default(None) */
    val spanName: Column[Option[String]] = column[Option[String]]("span_name", O.Length(256,varying=true), O.Default(None))
    /** Database column responseUrl DBType(VARCHAR), Length(512,true), Default() */
    val responseurl: Column[String] = column[String]("responseUrl", O.Length(512,varying=true), O.Default(""))
    /** Database column provider_count_switch DBType(INT), Default(0) */
    val providerCountSwitch: Column[Int] = column[Int]("provider_count_switch", O.Default(0))
  }
  /** Collection-like TableQuery object for table OswatchMonitorPolicy */
  lazy val OswatchMonitorPolicy = new TableQuery(tag => new OswatchMonitorPolicy(tag))
}