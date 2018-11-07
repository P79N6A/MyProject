package com.sankuai.octo.errorlog.db
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
  lazy val ddl = ErrorLog.ddl ++ ErrorLogDayReport.ddl ++ ErrorLogFilter.ddl ++ ErrorLogFilterRlt.ddl ++ ErrorLogPartition.ddl ++ ErrorLogServiceStatus.ddl ++ ErrorLogStatistic.ddl ++ LogAlarmConfig.ddl ++ LogAlarmSeverityConfig.ddl
  
  /** Entity class storing rows of table ErrorLog
   *  @param id Database column id DBType(BIGINT), AutoInc, PrimaryKey
   *  @param rowkey Database column rowkey DBType(VARCHAR), Length(128,true), Default()
   *  @param appkey Database column appkey DBType(VARCHAR), Length(64,true), Default()
   *  @param logTime Database column log_time DBType(DATETIME), Default(None)
   *  @param level Database column level DBType(VARCHAR), Length(32,true)
   *  @param host Database column host DBType(VARCHAR), Length(128,true)
   *  @param ip Database column ip DBType(VARCHAR), Length(64,true)
   *  @param action Database column action DBType(VARCHAR), Length(255,true), Default(None)
   *  @param location Database column location DBType(VARCHAR), Length(255,true), Default(None)
   *  @param message Database column message DBType(VARCHAR), Length(5120,true)
   *  @param exception Database column exception DBType(VARCHAR), Length(1024,true)
   *  @param exceptionMessage Database column exception_message DBType(VARCHAR), Length(8000,true)
   *  @param exceptionLocation Database column exception_location DBType(VARCHAR), Length(1024,true)
   *  @param traceId Database column trace_id DBType(VARCHAR), Length(20,true), Default() */
  case class ErrorLogRow(id: Long, rowkey: String = "", appkey: String = "", logTime: Option[java.sql.Timestamp] = None, level: String, host: String, ip: String, action: Option[String] = None, location: Option[String] = None, message: String, exception: String, exceptionMessage: String, exceptionLocation: String, traceId: String = "")
  /** GetResult implicit for fetching ErrorLogRow objects using plain SQL queries */
  implicit def GetResultErrorLogRow(implicit e0: GR[Long], e1: GR[String], e2: GR[Option[java.sql.Timestamp]], e3: GR[Option[String]]): GR[ErrorLogRow] = GR{
    prs => import prs._
    ErrorLogRow.tupled((<<[Long], <<[String], <<[String], <<?[java.sql.Timestamp], <<[String], <<[String], <<[String], <<?[String], <<?[String], <<[String], <<[String], <<[String], <<[String], <<[String]))
  }
  /** Table description of table error_log. Objects of this class serve as prototypes for rows in queries. */
  class ErrorLog(_tableTag: Tag) extends Table[ErrorLogRow](_tableTag, "error_log") {
    def * = (id, rowkey, appkey, logTime, level, host, ip, action, location, message, exception, exceptionMessage, exceptionLocation, traceId) <> (ErrorLogRow.tupled, ErrorLogRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (id.?, rowkey.?, appkey.?, logTime, level.?, host.?, ip.?, action, location, message.?, exception.?, exceptionMessage.?, exceptionLocation.?, traceId.?).shaped.<>({r=>import r._; _1.map(_=> ErrorLogRow.tupled((_1.get, _2.get, _3.get, _4, _5.get, _6.get, _7.get, _8, _9, _10.get, _11.get, _12.get, _13.get, _14.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))
    
    /** Database column id DBType(BIGINT), AutoInc, PrimaryKey */
    val id: Column[Long] = column[Long]("id", O.AutoInc, O.PrimaryKey)
    /** Database column rowkey DBType(VARCHAR), Length(128,true), Default() */
    val rowkey: Column[String] = column[String]("rowkey", O.Length(128,varying=true), O.Default(""))
    /** Database column appkey DBType(VARCHAR), Length(64,true), Default() */
    val appkey: Column[String] = column[String]("appkey", O.Length(64,varying=true), O.Default(""))
    /** Database column log_time DBType(DATETIME), Default(None) */
    val logTime: Column[Option[java.sql.Timestamp]] = column[Option[java.sql.Timestamp]]("log_time", O.Default(None))
    /** Database column level DBType(VARCHAR), Length(32,true) */
    val level: Column[String] = column[String]("level", O.Length(32,varying=true))
    /** Database column host DBType(VARCHAR), Length(128,true) */
    val host: Column[String] = column[String]("host", O.Length(128,varying=true))
    /** Database column ip DBType(VARCHAR), Length(64,true) */
    val ip: Column[String] = column[String]("ip", O.Length(64,varying=true))
    /** Database column action DBType(VARCHAR), Length(255,true), Default(None) */
    val action: Column[Option[String]] = column[Option[String]]("action", O.Length(255,varying=true), O.Default(None))
    /** Database column location DBType(VARCHAR), Length(255,true), Default(None) */
    val location: Column[Option[String]] = column[Option[String]]("location", O.Length(255,varying=true), O.Default(None))
    /** Database column message DBType(VARCHAR), Length(5120,true) */
    val message: Column[String] = column[String]("message", O.Length(5120,varying=true))
    /** Database column exception DBType(VARCHAR), Length(1024,true) */
    val exception: Column[String] = column[String]("exception", O.Length(1024,varying=true))
    /** Database column exception_message DBType(VARCHAR), Length(8000,true) */
    val exceptionMessage: Column[String] = column[String]("exception_message", O.Length(8000,varying=true))
    /** Database column exception_location DBType(VARCHAR), Length(1024,true) */
    val exceptionLocation: Column[String] = column[String]("exception_location", O.Length(1024,varying=true))
    /** Database column trace_id DBType(VARCHAR), Length(20,true), Default() */
    val traceId: Column[String] = column[String]("trace_id", O.Length(20,varying=true), O.Default(""))
    
    /** Index over (appkey,logTime) (database name idx_appkey_time) */
    val index1 = index("idx_appkey_time", (appkey, logTime))
    /** Index over (logTime) (database name idx_time) */
    val index2 = index("idx_time", logTime)
    /** Index over (traceId) (database name index_trace_id) */
    val index3 = index("index_trace_id", traceId)
  }
  /** Collection-like TableQuery object for table ErrorLog */
  lazy val ErrorLog = new TableQuery(tag => new ErrorLog(tag))
  
  /** Entity class storing rows of table ErrorLogDayReport
   *  @param id Database column id DBType(BIGINT), AutoInc, PrimaryKey
   *  @param appkey Database column appkey DBType(VARCHAR), Length(64,true)
   *  @param dt Database column dt DBType(DATE)
   *  @param logCount Database column log_count DBType(INT), Default(Some(0))
   *  @param updateTime Database column update_time DBType(TIMESTAMP) */
  case class ErrorLogDayReportRow(id: Long, appkey: String, dt: java.sql.Date, logCount: Option[Int] = Some(0), updateTime: java.sql.Timestamp)
  /** GetResult implicit for fetching ErrorLogDayReportRow objects using plain SQL queries */
  implicit def GetResultErrorLogDayReportRow(implicit e0: GR[Long], e1: GR[String], e2: GR[java.sql.Date], e3: GR[Option[Int]], e4: GR[java.sql.Timestamp]): GR[ErrorLogDayReportRow] = GR{
    prs => import prs._
    ErrorLogDayReportRow.tupled((<<[Long], <<[String], <<[java.sql.Date], <<?[Int], <<[java.sql.Timestamp]))
  }
  /** Table description of table error_log_day_report. Objects of this class serve as prototypes for rows in queries. */
  class ErrorLogDayReport(_tableTag: Tag) extends Table[ErrorLogDayReportRow](_tableTag, "error_log_day_report") {
    def * = (id, appkey, dt, logCount, updateTime) <> (ErrorLogDayReportRow.tupled, ErrorLogDayReportRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (id.?, appkey.?, dt.?, logCount, updateTime.?).shaped.<>({r=>import r._; _1.map(_=> ErrorLogDayReportRow.tupled((_1.get, _2.get, _3.get, _4, _5.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))
    
    /** Database column id DBType(BIGINT), AutoInc, PrimaryKey */
    val id: Column[Long] = column[Long]("id", O.AutoInc, O.PrimaryKey)
    /** Database column appkey DBType(VARCHAR), Length(64,true) */
    val appkey: Column[String] = column[String]("appkey", O.Length(64,varying=true))
    /** Database column dt DBType(DATE) */
    val dt: Column[java.sql.Date] = column[java.sql.Date]("dt")
    /** Database column log_count DBType(INT), Default(Some(0)) */
    val logCount: Column[Option[Int]] = column[Option[Int]]("log_count", O.Default(Some(0)))
    /** Database column update_time DBType(TIMESTAMP) */
    val updateTime: Column[java.sql.Timestamp] = column[java.sql.Timestamp]("update_time")
    
    /** Uniqueness Index over (appkey,dt) (database name uc_appkey_dt) */
    val index1 = index("uc_appkey_dt", (appkey, dt), unique=true)
  }
  /** Collection-like TableQuery object for table ErrorLogDayReport */
  lazy val ErrorLogDayReport = new TableQuery(tag => new ErrorLogDayReport(tag))
  
  /** Entity class storing rows of table ErrorLogFilter
   *  @param id Database column id DBType(INT), AutoInc, PrimaryKey
   *  @param name Database column name DBType(VARCHAR), Length(256,true), Default()
   *  @param appkey Database column appkey DBType(VARCHAR), Length(64,true), Default()
   *  @param terminate Database column terminate DBType(BIT), Default(true)
   *  @param sortNum Database column sort_num DBType(BIGINT), Default(999999)
   *  @param rules Database column rules DBType(VARCHAR), Length(10000,true), Default(Some())
   *  @param ruleCondition Database column rule_condition DBType(INT), Default(Some(0))
   *  @param enabled Database column enabled DBType(BIT), Default(true)
   *  @param status Database column status DBType(INT)
   *  @param createTime Database column create_time DBType(DATETIME), Default(None)
   *  @param updateTime Database column update_time DBType(DATETIME), Default(None)
   *  @param operatorId Database column operator_id DBType(INT), Default(None)
   *  @param alarm Database column alarm DBType(BIT), Default(true)
   *  @param threhold Database column threhold DBType(INT), Default(Some(1))
   *  @param thresholdMin Database column threshold_min DBType(INT), Default(1) */
  case class ErrorLogFilterRow(id: Int, name: String = "", appkey: String = "", terminate: Boolean = true, sortNum: Long = 999999L, rules: Option[String] = Some(""), ruleCondition: Option[Int] = Some(0), enabled: Boolean = true, status: Int, createTime: Option[java.sql.Timestamp] = None, updateTime: Option[java.sql.Timestamp] = None, operatorId: Option[Int] = None, alarm: Boolean = true, threhold: Option[Int] = Some(1), thresholdMin: Int = 1)
  /** GetResult implicit for fetching ErrorLogFilterRow objects using plain SQL queries */
  implicit def GetResultErrorLogFilterRow(implicit e0: GR[Int], e1: GR[String], e2: GR[Boolean], e3: GR[Long], e4: GR[Option[String]], e5: GR[Option[Int]], e6: GR[Option[java.sql.Timestamp]]): GR[ErrorLogFilterRow] = GR{
    prs => import prs._
    ErrorLogFilterRow.tupled((<<[Int], <<[String], <<[String], <<[Boolean], <<[Long], <<?[String], <<?[Int], <<[Boolean], <<[Int], <<?[java.sql.Timestamp], <<?[java.sql.Timestamp], <<?[Int], <<[Boolean], <<?[Int], <<[Int]))
  }
  /** Table description of table error_log_filter. Objects of this class serve as prototypes for rows in queries. */
  class ErrorLogFilter(_tableTag: Tag) extends Table[ErrorLogFilterRow](_tableTag, "error_log_filter") {
    def * = (id, name, appkey, terminate, sortNum, rules, ruleCondition, enabled, status, createTime, updateTime, operatorId, alarm, threhold, thresholdMin) <> (ErrorLogFilterRow.tupled, ErrorLogFilterRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (id.?, name.?, appkey.?, terminate.?, sortNum.?, rules, ruleCondition, enabled.?, status.?, createTime, updateTime, operatorId, alarm.?, threhold, thresholdMin.?).shaped.<>({r=>import r._; _1.map(_=> ErrorLogFilterRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6, _7, _8.get, _9.get, _10, _11, _12, _13.get, _14, _15.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))
    
    /** Database column id DBType(INT), AutoInc, PrimaryKey */
    val id: Column[Int] = column[Int]("id", O.AutoInc, O.PrimaryKey)
    /** Database column name DBType(VARCHAR), Length(256,true), Default() */
    val name: Column[String] = column[String]("name", O.Length(256,varying=true), O.Default(""))
    /** Database column appkey DBType(VARCHAR), Length(64,true), Default() */
    val appkey: Column[String] = column[String]("appkey", O.Length(64,varying=true), O.Default(""))
    /** Database column terminate DBType(BIT), Default(true) */
    val terminate: Column[Boolean] = column[Boolean]("terminate", O.Default(true))
    /** Database column sort_num DBType(BIGINT), Default(999999) */
    val sortNum: Column[Long] = column[Long]("sort_num", O.Default(999999L))
    /** Database column rules DBType(VARCHAR), Length(10000,true), Default(Some()) */
    val rules: Column[Option[String]] = column[Option[String]]("rules", O.Length(10000,varying=true), O.Default(Some("")))
    /** Database column rule_condition DBType(INT), Default(Some(0)) */
    val ruleCondition: Column[Option[Int]] = column[Option[Int]]("rule_condition", O.Default(Some(0)))
    /** Database column enabled DBType(BIT), Default(true) */
    val enabled: Column[Boolean] = column[Boolean]("enabled", O.Default(true))
    /** Database column status DBType(INT) */
    val status: Column[Int] = column[Int]("status")
    /** Database column create_time DBType(DATETIME), Default(None) */
    val createTime: Column[Option[java.sql.Timestamp]] = column[Option[java.sql.Timestamp]]("create_time", O.Default(None))
    /** Database column update_time DBType(DATETIME), Default(None) */
    val updateTime: Column[Option[java.sql.Timestamp]] = column[Option[java.sql.Timestamp]]("update_time", O.Default(None))
    /** Database column operator_id DBType(INT), Default(None) */
    val operatorId: Column[Option[Int]] = column[Option[Int]]("operator_id", O.Default(None))
    /** Database column alarm DBType(BIT), Default(true) */
    val alarm: Column[Boolean] = column[Boolean]("alarm", O.Default(true))
    /** Database column threhold DBType(INT), Default(Some(1)) */
    val threhold: Column[Option[Int]] = column[Option[Int]]("threhold", O.Default(Some(1)))
    /** Database column threshold_min DBType(INT), Default(1) */
    val thresholdMin: Column[Int] = column[Int]("threshold_min", O.Default(1))
  }
  /** Collection-like TableQuery object for table ErrorLogFilter */
  lazy val ErrorLogFilter = new TableQuery(tag => new ErrorLogFilter(tag))
  
  /** Entity class storing rows of table ErrorLogFilterRlt
   *  @param id Database column id DBType(BIGINT), AutoInc, PrimaryKey
   *  @param logId Database column log_id DBType(BIGINT), Default(None)
   *  @param filterId Database column filter_id DBType(INT)
   *  @param appkey Database column appkey DBType(VARCHAR), Length(64,true), Default(None)
   *  @param logTime Database column log_time DBType(DATETIME), Default(None) */
  case class ErrorLogFilterRltRow(id: Long, logId: Option[Long] = None, filterId: Int, appkey: Option[String] = None, logTime: Option[java.sql.Timestamp] = None)
  /** GetResult implicit for fetching ErrorLogFilterRltRow objects using plain SQL queries */
  implicit def GetResultErrorLogFilterRltRow(implicit e0: GR[Long], e1: GR[Option[Long]], e2: GR[Int], e3: GR[Option[String]], e4: GR[Option[java.sql.Timestamp]]): GR[ErrorLogFilterRltRow] = GR{
    prs => import prs._
    ErrorLogFilterRltRow.tupled((<<[Long], <<?[Long], <<[Int], <<?[String], <<?[java.sql.Timestamp]))
  }
  /** Table description of table error_log_filter_rlt. Objects of this class serve as prototypes for rows in queries. */
  class ErrorLogFilterRlt(_tableTag: Tag) extends Table[ErrorLogFilterRltRow](_tableTag, "error_log_filter_rlt") {
    def * = (id, logId, filterId, appkey, logTime) <> (ErrorLogFilterRltRow.tupled, ErrorLogFilterRltRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (id.?, logId, filterId.?, appkey, logTime).shaped.<>({r=>import r._; _1.map(_=> ErrorLogFilterRltRow.tupled((_1.get, _2, _3.get, _4, _5)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))
    
    /** Database column id DBType(BIGINT), AutoInc, PrimaryKey */
    val id: Column[Long] = column[Long]("id", O.AutoInc, O.PrimaryKey)
    /** Database column log_id DBType(BIGINT), Default(None) */
    val logId: Column[Option[Long]] = column[Option[Long]]("log_id", O.Default(None))
    /** Database column filter_id DBType(INT) */
    val filterId: Column[Int] = column[Int]("filter_id")
    /** Database column appkey DBType(VARCHAR), Length(64,true), Default(None) */
    val appkey: Column[Option[String]] = column[Option[String]]("appkey", O.Length(64,varying=true), O.Default(None))
    /** Database column log_time DBType(DATETIME), Default(None) */
    val logTime: Column[Option[java.sql.Timestamp]] = column[Option[java.sql.Timestamp]]("log_time", O.Default(None))
    
    /** Index over (appkey,logTime) (database name idx_appkey_time) */
    val index1 = index("idx_appkey_time", (appkey, logTime))
    /** Index over (logId) (database name idx_log_id) */
    val index2 = index("idx_log_id", logId)
    /** Index over (logTime) (database name idx_time) */
    val index3 = index("idx_time", logTime)
  }
  /** Collection-like TableQuery object for table ErrorLogFilterRlt */
  lazy val ErrorLogFilterRlt = new TableQuery(tag => new ErrorLogFilterRlt(tag))
  
  /** Entity class storing rows of table ErrorLogPartition
   *  @param id Database column id DBType(INT), AutoInc
   *  @param rowkey Database column rowkey DBType(VARCHAR), Length(128,true), Default()
   *  @param appkey Database column appkey DBType(VARCHAR), Length(64,true), Default()
   *  @param logTime Database column log_time DBType(DATETIME), Default(None)
   *  @param level Database column level DBType(VARCHAR), Length(32,true)
   *  @param host Database column host DBType(VARCHAR), Length(128,true)
   *  @param ip Database column ip DBType(VARCHAR), Length(64,true)
   *  @param action Database column action DBType(VARCHAR), Length(255,true), Default(None)
   *  @param location Database column location DBType(VARCHAR), Length(255,true), Default(None)
   *  @param message Database column message DBType(VARCHAR), Length(5120,true)
   *  @param exception Database column exception DBType(VARCHAR), Length(1024,true)
   *  @param exceptionMessage Database column exception_message DBType(VARCHAR), Length(8000,true)
   *  @param exceptionLocation Database column exception_location DBType(VARCHAR), Length(1024,true) */
  case class ErrorLogPartitionRow(id: Int, rowkey: String = "", appkey: String = "", logTime: Option[java.sql.Timestamp] = None, level: String, host: String, ip: String, action: Option[String] = None, location: Option[String] = None, message: String, exception: String, exceptionMessage: String, exceptionLocation: String)
  /** GetResult implicit for fetching ErrorLogPartitionRow objects using plain SQL queries */
  implicit def GetResultErrorLogPartitionRow(implicit e0: GR[Int], e1: GR[String], e2: GR[Option[java.sql.Timestamp]], e3: GR[Option[String]]): GR[ErrorLogPartitionRow] = GR{
    prs => import prs._
    ErrorLogPartitionRow.tupled((<<[Int], <<[String], <<[String], <<?[java.sql.Timestamp], <<[String], <<[String], <<[String], <<?[String], <<?[String], <<[String], <<[String], <<[String], <<[String]))
  }
  /** Table description of table error_log_partition. Objects of this class serve as prototypes for rows in queries. */
  class ErrorLogPartition(_tableTag: Tag) extends Table[ErrorLogPartitionRow](_tableTag, "error_log_partition") {
    def * = (id, rowkey, appkey, logTime, level, host, ip, action, location, message, exception, exceptionMessage, exceptionLocation) <> (ErrorLogPartitionRow.tupled, ErrorLogPartitionRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (id.?, rowkey.?, appkey.?, logTime, level.?, host.?, ip.?, action, location, message.?, exception.?, exceptionMessage.?, exceptionLocation.?).shaped.<>({r=>import r._; _1.map(_=> ErrorLogPartitionRow.tupled((_1.get, _2.get, _3.get, _4, _5.get, _6.get, _7.get, _8, _9, _10.get, _11.get, _12.get, _13.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))
    
    /** Database column id DBType(INT), AutoInc */
    val id: Column[Int] = column[Int]("id", O.AutoInc)
    /** Database column rowkey DBType(VARCHAR), Length(128,true), Default() */
    val rowkey: Column[String] = column[String]("rowkey", O.Length(128,varying=true), O.Default(""))
    /** Database column appkey DBType(VARCHAR), Length(64,true), Default() */
    val appkey: Column[String] = column[String]("appkey", O.Length(64,varying=true), O.Default(""))
    /** Database column log_time DBType(DATETIME), Default(None) */
    val logTime: Column[Option[java.sql.Timestamp]] = column[Option[java.sql.Timestamp]]("log_time", O.Default(None))
    /** Database column level DBType(VARCHAR), Length(32,true) */
    val level: Column[String] = column[String]("level", O.Length(32,varying=true))
    /** Database column host DBType(VARCHAR), Length(128,true) */
    val host: Column[String] = column[String]("host", O.Length(128,varying=true))
    /** Database column ip DBType(VARCHAR), Length(64,true) */
    val ip: Column[String] = column[String]("ip", O.Length(64,varying=true))
    /** Database column action DBType(VARCHAR), Length(255,true), Default(None) */
    val action: Column[Option[String]] = column[Option[String]]("action", O.Length(255,varying=true), O.Default(None))
    /** Database column location DBType(VARCHAR), Length(255,true), Default(None) */
    val location: Column[Option[String]] = column[Option[String]]("location", O.Length(255,varying=true), O.Default(None))
    /** Database column message DBType(VARCHAR), Length(5120,true) */
    val message: Column[String] = column[String]("message", O.Length(5120,varying=true))
    /** Database column exception DBType(VARCHAR), Length(1024,true) */
    val exception: Column[String] = column[String]("exception", O.Length(1024,varying=true))
    /** Database column exception_message DBType(VARCHAR), Length(8000,true) */
    val exceptionMessage: Column[String] = column[String]("exception_message", O.Length(8000,varying=true))
    /** Database column exception_location DBType(VARCHAR), Length(1024,true) */
    val exceptionLocation: Column[String] = column[String]("exception_location", O.Length(1024,varying=true))
    
    /** Index over (id) (database name id) */
    val index1 = index("id", id)
    /** Index over (appkey,logTime) (database name idx_appkey_time) */
    val index2 = index("idx_appkey_time", (appkey, logTime))
    /** Index over (logTime) (database name idx_time) */
    val index3 = index("idx_time", logTime)
  }
  /** Collection-like TableQuery object for table ErrorLogPartition */
  lazy val ErrorLogPartition = new TableQuery(tag => new ErrorLogPartition(tag))
  
  /** Entity class storing rows of table ErrorLogServiceStatus
   *  @param id Database column id DBType(INT), AutoInc, PrimaryKey
   *  @param appkey Database column appkey DBType(VARCHAR), Length(64,true), Default()
   *  @param serviceStatus Database column service_status DBType(ENUM), Length(8,false), Default(UNKNOWN)
   *  @param updateTime Database column update_time DBType(TIMESTAMP) */
  case class ErrorLogServiceStatusRow(id: Int, appkey: String = "", serviceStatus: String = "UNKNOWN", updateTime: java.sql.Timestamp)
  /** GetResult implicit for fetching ErrorLogServiceStatusRow objects using plain SQL queries */
  implicit def GetResultErrorLogServiceStatusRow(implicit e0: GR[Int], e1: GR[String], e2: GR[java.sql.Timestamp]): GR[ErrorLogServiceStatusRow] = GR{
    prs => import prs._
    ErrorLogServiceStatusRow.tupled((<<[Int], <<[String], <<[String], <<[java.sql.Timestamp]))
  }
  /** Table description of table error_log_service_status. Objects of this class serve as prototypes for rows in queries. */
  class ErrorLogServiceStatus(_tableTag: Tag) extends Table[ErrorLogServiceStatusRow](_tableTag, "error_log_service_status") {
    def * = (id, appkey, serviceStatus, updateTime) <> (ErrorLogServiceStatusRow.tupled, ErrorLogServiceStatusRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (id.?, appkey.?, serviceStatus.?, updateTime.?).shaped.<>({r=>import r._; _1.map(_=> ErrorLogServiceStatusRow.tupled((_1.get, _2.get, _3.get, _4.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))
    
    /** Database column id DBType(INT), AutoInc, PrimaryKey */
    val id: Column[Int] = column[Int]("id", O.AutoInc, O.PrimaryKey)
    /** Database column appkey DBType(VARCHAR), Length(64,true), Default() */
    val appkey: Column[String] = column[String]("appkey", O.Length(64,varying=true), O.Default(""))
    /** Database column service_status DBType(ENUM), Length(8,false), Default(UNKNOWN) */
    val serviceStatus: Column[String] = column[String]("service_status", O.Length(8,varying=false), O.Default("UNKNOWN"))
    /** Database column update_time DBType(TIMESTAMP) */
    val updateTime: Column[java.sql.Timestamp] = column[java.sql.Timestamp]("update_time")
    
    /** Uniqueness Index over (appkey) (database name appkey) */
    val index1 = index("appkey", appkey, unique=true)
  }
  /** Collection-like TableQuery object for table ErrorLogServiceStatus */
  lazy val ErrorLogServiceStatus = new TableQuery(tag => new ErrorLogServiceStatus(tag))
  
  /** Entity class storing rows of table ErrorLogStatistic
   *  @param id Database column id DBType(BIGINT), AutoInc
   *  @param time Database column time DBType(INT), Default(0)
   *  @param appkey Database column appkey DBType(VARCHAR), Length(128,true), Default()
   *  @param hostSet Database column host_set DBType(VARCHAR), Length(200,true), Default(No)
   *  @param host Database column host DBType(VARCHAR), Length(128,true), Default()
   *  @param filterId Database column filter_id DBType(INT), Default(0)
   *  @param exceptionName Database column exception_name DBType(VARCHAR), Length(200,true), Default()
   *  @param count Database column count DBType(INT), Default(0)
   *  @param duplicateCount Database column duplicate_count DBType(INT), Default(0) */
  case class ErrorLogStatisticRow(id: Long, time: Int = 0, appkey: String = "", hostSet: String = "No", host: String = "", filterId: Int = 0, exceptionName: String = "", count: Int = 0, duplicateCount: Int = 0)
  /** GetResult implicit for fetching ErrorLogStatisticRow objects using plain SQL queries */
  implicit def GetResultErrorLogStatisticRow(implicit e0: GR[Long], e1: GR[Int], e2: GR[String]): GR[ErrorLogStatisticRow] = GR{
    prs => import prs._
    ErrorLogStatisticRow.tupled((<<[Long], <<[Int], <<[String], <<[String], <<[String], <<[Int], <<[String], <<[Int], <<[Int]))
  }
  /** Table description of table error_log_statistic. Objects of this class serve as prototypes for rows in queries. */
  class ErrorLogStatistic(_tableTag: Tag) extends Table[ErrorLogStatisticRow](_tableTag, "error_log_statistic") {
    def * = (id, time, appkey, hostSet, host, filterId, exceptionName, count, duplicateCount) <> (ErrorLogStatisticRow.tupled, ErrorLogStatisticRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (id.?, time.?, appkey.?, hostSet.?, host.?, filterId.?, exceptionName.?, count.?, duplicateCount.?).shaped.<>({r=>import r._; _1.map(_=> ErrorLogStatisticRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6.get, _7.get, _8.get, _9.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))
    
    /** Database column id DBType(BIGINT), AutoInc */
    val id: Column[Long] = column[Long]("id", O.AutoInc)
    /** Database column time DBType(INT), Default(0) */
    val time: Column[Int] = column[Int]("time", O.Default(0))
    /** Database column appkey DBType(VARCHAR), Length(128,true), Default() */
    val appkey: Column[String] = column[String]("appkey", O.Length(128,varying=true), O.Default(""))
    /** Database column host_set DBType(VARCHAR), Length(200,true), Default(No) */
    val hostSet: Column[String] = column[String]("host_set", O.Length(200,varying=true), O.Default("No"))
    /** Database column host DBType(VARCHAR), Length(128,true), Default() */
    val host: Column[String] = column[String]("host", O.Length(128,varying=true), O.Default(""))
    /** Database column filter_id DBType(INT), Default(0) */
    val filterId: Column[Int] = column[Int]("filter_id", O.Default(0))
    /** Database column exception_name DBType(VARCHAR), Length(200,true), Default() */
    val exceptionName: Column[String] = column[String]("exception_name", O.Length(200,varying=true), O.Default(""))
    /** Database column count DBType(INT), Default(0) */
    val count: Column[Int] = column[Int]("count", O.Default(0))
    /** Database column duplicate_count DBType(INT), Default(0) */
    val duplicateCount: Column[Int] = column[Int]("duplicate_count", O.Default(0))
    
    /** Primary key of ErrorLogStatistic (database name error_log_statistic_PK) */
    val pk = primaryKey("error_log_statistic_PK", (id, time))
    
    /** Index over (exceptionName) (database name idx_exception_name) */
    val index1 = index("idx_exception_name", exceptionName)
    /** Index over (filterId) (database name idx_filter_id) */
    val index2 = index("idx_filter_id", filterId)
    /** Index over (host) (database name idx_host) */
    val index3 = index("idx_host", host)
    /** Index over (time) (database name idx_time) */
    val index4 = index("idx_time", time)
    /** Uniqueness Index over (appkey,host,filterId,exceptionName,time) (database name unique_key) */
    val index5 = index("unique_key", (appkey, host, filterId, exceptionName, time), unique=true)
  }
  /** Collection-like TableQuery object for table ErrorLogStatistic */
  lazy val ErrorLogStatistic = new TableQuery(tag => new ErrorLogStatistic(tag))
  
  /** Entity class storing rows of table LogAlarmConfig
   *  @param id Database column id DBType(INT), AutoInc, PrimaryKey
   *  @param appkey Database column appkey DBType(VARCHAR), Length(64,true), Default()
   *  @param host Database column host DBType(VARCHAR), Length(255,true)
   *  @param trapper Database column trapper DBType(VARCHAR), Length(255,true)
   *  @param gapSeconds Database column gap_seconds DBType(INT), Default(Some(3600))
   *  @param enabled Database column enabled DBType(BIT), Default(Some(false))
   *  @param taskOperType Database column task_oper_type DBType(ENUM), Length(7,false), Default(NO)
   *  @param updateTime Database column update_time DBType(TIMESTAMP) */
  case class LogAlarmConfigRow(id: Int, appkey: String = "", host: String, trapper: String, gapSeconds: Option[Int] = Some(3600), enabled: Option[Boolean] = Some(false), taskOperType: String = "NO", updateTime: java.sql.Timestamp)
  /** GetResult implicit for fetching LogAlarmConfigRow objects using plain SQL queries */
  implicit def GetResultLogAlarmConfigRow(implicit e0: GR[Int], e1: GR[String], e2: GR[Option[Int]], e3: GR[Option[Boolean]], e4: GR[java.sql.Timestamp]): GR[LogAlarmConfigRow] = GR{
    prs => import prs._
    LogAlarmConfigRow.tupled((<<[Int], <<[String], <<[String], <<[String], <<?[Int], <<?[Boolean], <<[String], <<[java.sql.Timestamp]))
  }
  /** Table description of table log_alarm_config. Objects of this class serve as prototypes for rows in queries. */
  class LogAlarmConfig(_tableTag: Tag) extends Table[LogAlarmConfigRow](_tableTag, "log_alarm_config") {
    def * = (id, appkey, host, trapper, gapSeconds, enabled, taskOperType, updateTime) <> (LogAlarmConfigRow.tupled, LogAlarmConfigRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (id.?, appkey.?, host.?, trapper.?, gapSeconds, enabled, taskOperType.?, updateTime.?).shaped.<>({r=>import r._; _1.map(_=> LogAlarmConfigRow.tupled((_1.get, _2.get, _3.get, _4.get, _5, _6, _7.get, _8.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))
    
    /** Database column id DBType(INT), AutoInc, PrimaryKey */
    val id: Column[Int] = column[Int]("id", O.AutoInc, O.PrimaryKey)
    /** Database column appkey DBType(VARCHAR), Length(64,true), Default() */
    val appkey: Column[String] = column[String]("appkey", O.Length(64,varying=true), O.Default(""))
    /** Database column host DBType(VARCHAR), Length(255,true) */
    val host: Column[String] = column[String]("host", O.Length(255,varying=true))
    /** Database column trapper DBType(VARCHAR), Length(255,true) */
    val trapper: Column[String] = column[String]("trapper", O.Length(255,varying=true))
    /** Database column gap_seconds DBType(INT), Default(Some(3600)) */
    val gapSeconds: Column[Option[Int]] = column[Option[Int]]("gap_seconds", O.Default(Some(3600)))
    /** Database column enabled DBType(BIT), Default(Some(false)) */
    val enabled: Column[Option[Boolean]] = column[Option[Boolean]]("enabled", O.Default(Some(false)))
    /** Database column task_oper_type DBType(ENUM), Length(7,false), Default(NO) */
    val taskOperType: Column[String] = column[String]("task_oper_type", O.Length(7,varying=false), O.Default("NO"))
    /** Database column update_time DBType(TIMESTAMP) */
    val updateTime: Column[java.sql.Timestamp] = column[java.sql.Timestamp]("update_time")
    
    /** Uniqueness Index over (appkey) (database name appkey) */
    val index1 = index("appkey", appkey, unique=true)
  }
  /** Collection-like TableQuery object for table LogAlarmConfig */
  lazy val LogAlarmConfig = new TableQuery(tag => new LogAlarmConfig(tag))
  
  /** Entity class storing rows of table LogAlarmSeverityConfig
   *  @param id Database column id DBType(INT), AutoInc, PrimaryKey
   *  @param appkey Database column appkey DBType(VARCHAR), Length(64,true), Default()
   *  @param ok Database column ok DBType(INT), Default(Some(0))
   *  @param warning Database column warning DBType(INT), Default(Some(5))
   *  @param error Database column error DBType(INT), Default(Some(10))
   *  @param disaster Database column disaster DBType(INT), Default(Some(15)) */
  case class LogAlarmSeverityConfigRow(id: Int, appkey: String = "", ok: Option[Int] = Some(0), warning: Option[Int] = Some(5), error: Option[Int] = Some(10), disaster: Option[Int] = Some(15))
  /** GetResult implicit for fetching LogAlarmSeverityConfigRow objects using plain SQL queries */
  implicit def GetResultLogAlarmSeverityConfigRow(implicit e0: GR[Int], e1: GR[String], e2: GR[Option[Int]]): GR[LogAlarmSeverityConfigRow] = GR{
    prs => import prs._
    LogAlarmSeverityConfigRow.tupled((<<[Int], <<[String], <<?[Int], <<?[Int], <<?[Int], <<?[Int]))
  }
  /** Table description of table log_alarm_severity_config. Objects of this class serve as prototypes for rows in queries. */
  class LogAlarmSeverityConfig(_tableTag: Tag) extends Table[LogAlarmSeverityConfigRow](_tableTag, "log_alarm_severity_config") {
    def * = (id, appkey, ok, warning, error, disaster) <> (LogAlarmSeverityConfigRow.tupled, LogAlarmSeverityConfigRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (id.?, appkey.?, ok, warning, error, disaster).shaped.<>({r=>import r._; _1.map(_=> LogAlarmSeverityConfigRow.tupled((_1.get, _2.get, _3, _4, _5, _6)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))
    
    /** Database column id DBType(INT), AutoInc, PrimaryKey */
    val id: Column[Int] = column[Int]("id", O.AutoInc, O.PrimaryKey)
    /** Database column appkey DBType(VARCHAR), Length(64,true), Default() */
    val appkey: Column[String] = column[String]("appkey", O.Length(64,varying=true), O.Default(""))
    /** Database column ok DBType(INT), Default(Some(0)) */
    val ok: Column[Option[Int]] = column[Option[Int]]("ok", O.Default(Some(0)))
    /** Database column warning DBType(INT), Default(Some(5)) */
    val warning: Column[Option[Int]] = column[Option[Int]]("warning", O.Default(Some(5)))
    /** Database column error DBType(INT), Default(Some(10)) */
    val error: Column[Option[Int]] = column[Option[Int]]("error", O.Default(Some(10)))
    /** Database column disaster DBType(INT), Default(Some(15)) */
    val disaster: Column[Option[Int]] = column[Option[Int]]("disaster", O.Default(Some(15)))
    
    /** Uniqueness Index over (appkey) (database name appkey) */
    val index1 = index("appkey", appkey, unique=true)
  }
  /** Collection-like TableQuery object for table LogAlarmSeverityConfig */
  lazy val LogAlarmSeverityConfig = new TableQuery(tag => new LogAlarmSeverityConfig(tag))
}