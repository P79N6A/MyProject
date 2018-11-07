package com.sankuai.octo.mworth.db
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
  lazy val ddl = WorthAppkeyCount.ddl ++ WorthBusinessDescCount.ddl ++ WorthBusinessDescCountCopy.ddl ++ WorthConfig.ddl ++ WorthEvent.ddl ++ WorthEventCount.ddl ++ WorthFunction.ddl ++ WorthModelCount.ddl ++ WorthValue.ddl ++ WorthValueCount.ddl

  /** Entity class storing rows of table WorthAppkeyCount
    *  @param id Database column id DBType(BIGINT), AutoInc, PrimaryKey
    *  @param owt Database column owt DBType(VARCHAR), Length(22,true), Default()
    *  @param appkey Database column appkey DBType(VARCHAR), Length(128,true), Default()
    *  @param model Database column model DBType(VARCHAR), Length(128,true), Default()
    *  @param day Database column day DBType(DATE)
    *  @param dtype Database column dtype DBType(INT)
    *  @param count Database column count DBType(INT)
    *  @param createTime Database column create_time DBType(BIGINT) */
  case class WorthAppkeyCountRow(id: Long, owt: String = "", appkey: String = "", model: String = "", day: java.sql.Date, dtype: Int, count: Int, createTime: Long)
  /** GetResult implicit for fetching WorthAppkeyCountRow objects using plain SQL queries */
  implicit def GetResultWorthAppkeyCountRow(implicit e0: GR[Long], e1: GR[String], e2: GR[java.sql.Date], e3: GR[Int]): GR[WorthAppkeyCountRow] = GR{
    prs => import prs._
      WorthAppkeyCountRow.tupled((<<[Long], <<[String], <<[String], <<[String], <<[java.sql.Date], <<[Int], <<[Int], <<[Long]))
  }
  /** Table description of table worth_appkey_count. Objects of this class serve as prototypes for rows in queries. */
  class WorthAppkeyCount(_tableTag: Tag) extends Table[WorthAppkeyCountRow](_tableTag, "worth_appkey_count") {
    def * = (id, owt, appkey, model, day, dtype, count, createTime) <> (WorthAppkeyCountRow.tupled, WorthAppkeyCountRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (id.?, owt.?, appkey.?, model.?, day.?, dtype.?, count.?, createTime.?).shaped.<>({r=>import r._; _1.map(_=> WorthAppkeyCountRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6.get, _7.get, _8.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id DBType(BIGINT), AutoInc, PrimaryKey */
    val id: Column[Long] = column[Long]("id", O.AutoInc, O.PrimaryKey)
    /** Database column owt DBType(VARCHAR), Length(22,true), Default() */
    val owt: Column[String] = column[String]("owt", O.Length(22,varying=true), O.Default(""))
    /** Database column appkey DBType(VARCHAR), Length(128,true), Default() */
    val appkey: Column[String] = column[String]("appkey", O.Length(128,varying=true), O.Default(""))
    /** Database column model DBType(VARCHAR), Length(128,true), Default() */
    val model: Column[String] = column[String]("model", O.Length(128,varying=true), O.Default(""))
    /** Database column day DBType(DATE) */
    val day: Column[java.sql.Date] = column[java.sql.Date]("day")
    /** Database column dtype DBType(INT) */
    val dtype: Column[Int] = column[Int]("dtype")
    /** Database column count DBType(INT) */
    val count: Column[Int] = column[Int]("count")
    /** Database column create_time DBType(BIGINT) */
    val createTime: Column[Long] = column[Long]("create_time")

    /** Index over (appkey) (database name idx_appkey) */
    val index1 = index("idx_appkey", appkey)
    /** Index over (createTime) (database name idx_create_time) */
    val index2 = index("idx_create_time", createTime)
    /** Index over (owt) (database name idx_owt) */
    val index3 = index("idx_owt", owt)
  }
  /** Collection-like TableQuery object for table WorthAppkeyCount */
  lazy val WorthAppkeyCount = new TableQuery(tag => new WorthAppkeyCount(tag))

  /** Entity class storing rows of table WorthBusinessDescCount
    *  @param id Database column id DBType(BIGINT), AutoInc, PrimaryKey
    *  @param business Database column business DBType(INT), Default(100)
    *  @param username Database column username DBType(VARCHAR), Length(22,true), Default()
    *  @param model Database column model DBType(VARCHAR), Length(128,true)
    *  @param functionDesc Database column function_desc DBType(VARCHAR), Length(256,true), Default()
    *  @param posid Database column posid DBType(INT)
    *  @param posname Database column posname DBType(VARCHAR), Length(128,true), Default()
    *  @param orgid Database column orgid DBType(INT)
    *  @param orgname Database column orgname DBType(VARCHAR), Length(128,true), Default()
    *  @param count Database column count DBType(INT)
    *  @param day Database column day DBType(DATE) */
  case class WorthBusinessDescCountRow(id: Long, business: Int = 100, username: String = "", model: String, functionDesc: String = "", posid: Int, posname: String = "", orgid: Int, orgname: String = "", count: Int, day: java.sql.Date)
  /** GetResult implicit for fetching WorthBusinessDescCountRow objects using plain SQL queries */
  implicit def GetResultWorthBusinessDescCountRow(implicit e0: GR[Long], e1: GR[Int], e2: GR[String], e3: GR[java.sql.Date]): GR[WorthBusinessDescCountRow] = GR{
    prs => import prs._
      WorthBusinessDescCountRow.tupled((<<[Long], <<[Int], <<[String], <<[String], <<[String], <<[Int], <<[String], <<[Int], <<[String], <<[Int], <<[java.sql.Date]))
  }
  /** Table description of table worth_business_desc_count. Objects of this class serve as prototypes for rows in queries. */
  class WorthBusinessDescCount(_tableTag: Tag) extends Table[WorthBusinessDescCountRow](_tableTag, "worth_business_desc_count") {
    def * = (id, business, username, model, functionDesc, posid, posname, orgid, orgname, count, day) <> (WorthBusinessDescCountRow.tupled, WorthBusinessDescCountRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (id.?, business.?, username.?, model.?, functionDesc.?, posid.?, posname.?, orgid.?, orgname.?, count.?, day.?).shaped.<>({r=>import r._; _1.map(_=> WorthBusinessDescCountRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6.get, _7.get, _8.get, _9.get, _10.get, _11.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id DBType(BIGINT), AutoInc, PrimaryKey */
    val id: Column[Long] = column[Long]("id", O.AutoInc, O.PrimaryKey)
    /** Database column business DBType(INT), Default(100) */
    val business: Column[Int] = column[Int]("business", O.Default(100))
    /** Database column username DBType(VARCHAR), Length(22,true), Default() */
    val username: Column[String] = column[String]("username", O.Length(22,varying=true), O.Default(""))
    /** Database column model DBType(VARCHAR), Length(128,true) */
    val model: Column[String] = column[String]("model", O.Length(128,varying=true))
    /** Database column function_desc DBType(VARCHAR), Length(256,true), Default() */
    val functionDesc: Column[String] = column[String]("function_desc", O.Length(256,varying=true), O.Default(""))
    /** Database column posid DBType(INT) */
    val posid: Column[Int] = column[Int]("posid")
    /** Database column posname DBType(VARCHAR), Length(128,true), Default() */
    val posname: Column[String] = column[String]("posname", O.Length(128,varying=true), O.Default(""))
    /** Database column orgid DBType(INT) */
    val orgid: Column[Int] = column[Int]("orgid")
    /** Database column orgname DBType(VARCHAR), Length(128,true), Default() */
    val orgname: Column[String] = column[String]("orgname", O.Length(128,varying=true), O.Default(""))
    /** Database column count DBType(INT) */
    val count: Column[Int] = column[Int]("count")
    /** Database column day DBType(DATE) */
    val day: Column[java.sql.Date] = column[java.sql.Date]("day")

    /** Index over (day,business,model,functionDesc) (database name day) */
    val index1 = index("day", (day, business, model, functionDesc))
    /** Index over (username) (database name idx_username) */
    val index2 = index("idx_username", username)
  }
  /** Collection-like TableQuery object for table WorthBusinessDescCount */
  lazy val WorthBusinessDescCount = new TableQuery(tag => new WorthBusinessDescCount(tag))

  /** Entity class storing rows of table WorthBusinessDescCountCopy
    *  @param id Database column id DBType(BIGINT), AutoInc, PrimaryKey
    *  @param business Database column business DBType(INT), Default(100)
    *  @param username Database column username DBType(VARCHAR), Length(22,true), Default()
    *  @param model Database column model DBType(VARCHAR), Length(128,true)
    *  @param functionDesc Database column function_desc DBType(VARCHAR), Length(256,true), Default()
    *  @param count Database column count DBType(INT)
    *  @param day Database column day DBType(DATE)
    *  @param posid Database column posid DBType(INT), Default(0)
    *  @param posname Database column posname DBType(VARCHAR), Length(128,true), Default()
    *  @param orgid Database column orgid DBType(INT)
    *  @param orgname Database column orgname DBType(VARCHAR), Length(128,true), Default() */
  case class WorthBusinessDescCountCopyRow(id: Long, business: Int = 100, username: String = "", model: String, functionDesc: String = "", count: Int, day: java.sql.Date, posid: Int = 0, posname: String = "", orgid: Int, orgname: String = "")
  /** GetResult implicit for fetching WorthBusinessDescCountCopyRow objects using plain SQL queries */
  implicit def GetResultWorthBusinessDescCountCopyRow(implicit e0: GR[Long], e1: GR[Int], e2: GR[String], e3: GR[java.sql.Date]): GR[WorthBusinessDescCountCopyRow] = GR{
    prs => import prs._
      WorthBusinessDescCountCopyRow.tupled((<<[Long], <<[Int], <<[String], <<[String], <<[String], <<[Int], <<[java.sql.Date], <<[Int], <<[String], <<[Int], <<[String]))
  }
  /** Table description of table worth_business_desc_count_copy. Objects of this class serve as prototypes for rows in queries. */
  class WorthBusinessDescCountCopy(_tableTag: Tag) extends Table[WorthBusinessDescCountCopyRow](_tableTag, "worth_business_desc_count_copy") {
    def * = (id, business, username, model, functionDesc, count, day, posid, posname, orgid, orgname) <> (WorthBusinessDescCountCopyRow.tupled, WorthBusinessDescCountCopyRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (id.?, business.?, username.?, model.?, functionDesc.?, count.?, day.?, posid.?, posname.?, orgid.?, orgname.?).shaped.<>({r=>import r._; _1.map(_=> WorthBusinessDescCountCopyRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6.get, _7.get, _8.get, _9.get, _10.get, _11.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id DBType(BIGINT), AutoInc, PrimaryKey */
    val id: Column[Long] = column[Long]("id", O.AutoInc, O.PrimaryKey)
    /** Database column business DBType(INT), Default(100) */
    val business: Column[Int] = column[Int]("business", O.Default(100))
    /** Database column username DBType(VARCHAR), Length(22,true), Default() */
    val username: Column[String] = column[String]("username", O.Length(22,varying=true), O.Default(""))
    /** Database column model DBType(VARCHAR), Length(128,true) */
    val model: Column[String] = column[String]("model", O.Length(128,varying=true))
    /** Database column function_desc DBType(VARCHAR), Length(256,true), Default() */
    val functionDesc: Column[String] = column[String]("function_desc", O.Length(256,varying=true), O.Default(""))
    /** Database column count DBType(INT) */
    val count: Column[Int] = column[Int]("count")
    /** Database column day DBType(DATE) */
    val day: Column[java.sql.Date] = column[java.sql.Date]("day")
    /** Database column posid DBType(INT), Default(0) */
    val posid: Column[Int] = column[Int]("posid", O.Default(0))
    /** Database column posname DBType(VARCHAR), Length(128,true), Default() */
    val posname: Column[String] = column[String]("posname", O.Length(128,varying=true), O.Default(""))
    /** Database column orgid DBType(INT) */
    val orgid: Column[Int] = column[Int]("orgid")
    /** Database column orgname DBType(VARCHAR), Length(128,true), Default() */
    val orgname: Column[String] = column[String]("orgname", O.Length(128,varying=true), O.Default(""))

    /** Index over (day,business,model,functionDesc) (database name day) */
    val index1 = index("day", (day, business, model, functionDesc))
  }
  /** Collection-like TableQuery object for table WorthBusinessDescCountCopy */
  lazy val WorthBusinessDescCountCopy = new TableQuery(tag => new WorthBusinessDescCountCopy(tag))

  /** Entity class storing rows of table WorthConfig
    *  @param id Database column id DBType(BIGINT), AutoInc, PrimaryKey
    *  @param functionId Database column function_id DBType(BIGINT)
    *  @param targetAppkey Database column target_appkey DBType(VARCHAR), Length(128,true), Default(None)
    *  @param worth Database column worth DBType(INT)
    *  @param primitiveCostTime Database column primitive_cost_time DBType(INT)
    *  @param fromTime Database column from_time DBType(BIGINT)
    *  @param toTime Database column to_time DBType(BIGINT), Default(None)
    *  @param coverd Database column coverd DBType(BIT), Default(false)
    *  @param effectived Database column effectived DBType(BIT), Default(false)
    *  @param deleted Database column deleted DBType(BIT), Default(false)
    *  @param createTime Database column create_time DBType(BIGINT) */
  case class WorthConfigRow(id: Long, functionId: Long, targetAppkey: Option[String] = None, worth: Int, primitiveCostTime: Int, fromTime: Long, toTime: Option[Long] = None, coverd: Boolean = false, effectived: Boolean = false, deleted: Boolean = false, createTime: Long)
  /** GetResult implicit for fetching WorthConfigRow objects using plain SQL queries */
  implicit def GetResultWorthConfigRow(implicit e0: GR[Long], e1: GR[Option[String]], e2: GR[Int], e3: GR[Option[Long]], e4: GR[Boolean]): GR[WorthConfigRow] = GR{
    prs => import prs._
      WorthConfigRow.tupled((<<[Long], <<[Long], <<?[String], <<[Int], <<[Int], <<[Long], <<?[Long], <<[Boolean], <<[Boolean], <<[Boolean], <<[Long]))
  }
  /** Table description of table worth_config. Objects of this class serve as prototypes for rows in queries. */
  class WorthConfig(_tableTag: Tag) extends Table[WorthConfigRow](_tableTag, "worth_config") {
    def * = (id, functionId, targetAppkey, worth, primitiveCostTime, fromTime, toTime, coverd, effectived, deleted, createTime) <> (WorthConfigRow.tupled, WorthConfigRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (id.?, functionId.?, targetAppkey, worth.?, primitiveCostTime.?, fromTime.?, toTime, coverd.?, effectived.?, deleted.?, createTime.?).shaped.<>({r=>import r._; _1.map(_=> WorthConfigRow.tupled((_1.get, _2.get, _3, _4.get, _5.get, _6.get, _7, _8.get, _9.get, _10.get, _11.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id DBType(BIGINT), AutoInc, PrimaryKey */
    val id: Column[Long] = column[Long]("id", O.AutoInc, O.PrimaryKey)
    /** Database column function_id DBType(BIGINT) */
    val functionId: Column[Long] = column[Long]("function_id")
    /** Database column target_appkey DBType(VARCHAR), Length(128,true), Default(None) */
    val targetAppkey: Column[Option[String]] = column[Option[String]]("target_appkey", O.Length(128,varying=true), O.Default(None))
    /** Database column worth DBType(INT) */
    val worth: Column[Int] = column[Int]("worth")
    /** Database column primitive_cost_time DBType(INT) */
    val primitiveCostTime: Column[Int] = column[Int]("primitive_cost_time")
    /** Database column from_time DBType(BIGINT) */
    val fromTime: Column[Long] = column[Long]("from_time")
    /** Database column to_time DBType(BIGINT), Default(None) */
    val toTime: Column[Option[Long]] = column[Option[Long]]("to_time", O.Default(None))
    /** Database column coverd DBType(BIT), Default(false) */
    val coverd: Column[Boolean] = column[Boolean]("coverd", O.Default(false))
    /** Database column effectived DBType(BIT), Default(false) */
    val effectived: Column[Boolean] = column[Boolean]("effectived", O.Default(false))
    /** Database column deleted DBType(BIT), Default(false) */
    val deleted: Column[Boolean] = column[Boolean]("deleted", O.Default(false))
    /** Database column create_time DBType(BIGINT) */
    val createTime: Column[Long] = column[Long]("create_time")

    /** Index over (createTime) (database name idx_create_time) */
    val index1 = index("idx_create_time", createTime)
    /** Index over (functionId,effectived,targetAppkey) (database name index_funtion_id) */
    val index2 = index("index_funtion_id", (functionId, effectived, targetAppkey))
  }
  /** Collection-like TableQuery object for table WorthConfig */
  lazy val WorthConfig = new TableQuery(tag => new WorthConfig(tag))

  /** Entity class storing rows of table WorthEvent
    *  @param id Database column id DBType(BIGINT), AutoInc, PrimaryKey
    *  @param project Database column project DBType(VARCHAR), Length(128,true), Default()
    *  @param model Database column model DBType(VARCHAR), Length(128,true), Default()
    *  @param functionName Database column function_name DBType(VARCHAR), Length(256,true), Default()
    *  @param functionDesc Database column function_desc DBType(VARCHAR), Length(256,true), Default()
    *  @param operationSourceType Database column operation_source_type DBType(INT)
    *  @param business Database column business DBType(INT), Default(-1)
    *  @param operationSource Database column operation_source DBType(VARCHAR), Length(22,true), Default()
    *  @param targetAppkey Database column target_appkey DBType(VARCHAR), Length(128,true), Default(None)
    *  @param appkeyOwt Database column appkey_owt DBType(VARCHAR), Length(22,true), Default(None)
    *  @param signid Database column signid DBType(VARCHAR), Length(32,true), Default(None)
    *  @param startTime Database column start_time DBType(BIGINT), Default(None)
    *  @param endTime Database column end_time DBType(BIGINT), Default(None)
    *  @param createTime Database column create_time DBType(BIGINT) */
  case class WorthEventRow(id: Long, project: String = "", model: String = "", functionName: String = "", functionDesc: String = "", operationSourceType: Int, business: Int = -1, operationSource: String = "", targetAppkey: Option[String] = None, appkeyOwt: Option[String] = None, signid: Option[String] = None, startTime: Option[Long] = None, endTime: Option[Long] = None, createTime: Long)
  /** GetResult implicit for fetching WorthEventRow objects using plain SQL queries */
  implicit def GetResultWorthEventRow(implicit e0: GR[Long], e1: GR[String], e2: GR[Int], e3: GR[Option[String]], e4: GR[Option[Long]]): GR[WorthEventRow] = GR{
    prs => import prs._
      WorthEventRow.tupled((<<[Long], <<[String], <<[String], <<[String], <<[String], <<[Int], <<[Int], <<[String], <<?[String], <<?[String], <<?[String], <<?[Long], <<?[Long], <<[Long]))
  }
  /** Table description of table worth_event. Objects of this class serve as prototypes for rows in queries. */
  class WorthEvent(_tableTag: Tag) extends Table[WorthEventRow](_tableTag, "worth_event") {
    def * = (id, project, model, functionName, functionDesc, operationSourceType, business, operationSource, targetAppkey, appkeyOwt, signid, startTime, endTime, createTime) <> (WorthEventRow.tupled, WorthEventRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (id.?, project.?, model.?, functionName.?, functionDesc.?, operationSourceType.?, business.?, operationSource.?, targetAppkey, appkeyOwt, signid, startTime, endTime, createTime.?).shaped.<>({r=>import r._; _1.map(_=> WorthEventRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6.get, _7.get, _8.get, _9, _10, _11, _12, _13, _14.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id DBType(BIGINT), AutoInc, PrimaryKey */
    val id: Column[Long] = column[Long]("id", O.AutoInc, O.PrimaryKey)
    /** Database column project DBType(VARCHAR), Length(128,true), Default() */
    val project: Column[String] = column[String]("project", O.Length(128,varying=true), O.Default(""))
    /** Database column model DBType(VARCHAR), Length(128,true), Default() */
    val model: Column[String] = column[String]("model", O.Length(128,varying=true), O.Default(""))
    /** Database column function_name DBType(VARCHAR), Length(256,true), Default() */
    val functionName: Column[String] = column[String]("function_name", O.Length(256,varying=true), O.Default(""))
    /** Database column function_desc DBType(VARCHAR), Length(256,true), Default() */
    val functionDesc: Column[String] = column[String]("function_desc", O.Length(256,varying=true), O.Default(""))
    /** Database column operation_source_type DBType(INT) */
    val operationSourceType: Column[Int] = column[Int]("operation_source_type")
    /** Database column business DBType(INT), Default(-1) */
    val business: Column[Int] = column[Int]("business", O.Default(-1))
    /** Database column operation_source DBType(VARCHAR), Length(22,true), Default() */
    val operationSource: Column[String] = column[String]("operation_source", O.Length(22,varying=true), O.Default(""))
    /** Database column target_appkey DBType(VARCHAR), Length(128,true), Default(None) */
    val targetAppkey: Column[Option[String]] = column[Option[String]]("target_appkey", O.Length(128,varying=true), O.Default(None))
    /** Database column appkey_owt DBType(VARCHAR), Length(22,true), Default(None) */
    val appkeyOwt: Column[Option[String]] = column[Option[String]]("appkey_owt", O.Length(22,varying=true), O.Default(None))
    /** Database column signid DBType(VARCHAR), Length(32,true), Default(None) */
    val signid: Column[Option[String]] = column[Option[String]]("signid", O.Length(32,varying=true), O.Default(None))
    /** Database column start_time DBType(BIGINT), Default(None) */
    val startTime: Column[Option[Long]] = column[Option[Long]]("start_time", O.Default(None))
    /** Database column end_time DBType(BIGINT), Default(None) */
    val endTime: Column[Option[Long]] = column[Option[Long]]("end_time", O.Default(None))
    /** Database column create_time DBType(BIGINT) */
    val createTime: Column[Long] = column[Long]("create_time")

    /** Index over (createTime) (database name idx_create_time) */
    val index1 = index("idx_create_time", createTime)
    /** Index over (project,functionName,signid) (database name idx_project_model_function_signid) */
    val index2 = index("idx_project_model_function_signid", (project, functionName, signid))
  }
  /** Collection-like TableQuery object for table WorthEvent */
  lazy val WorthEvent = new TableQuery(tag => new WorthEvent(tag))

  /** Entity class storing rows of table WorthEventCount
    *  @param id Database column id DBType(BIGINT), AutoInc, PrimaryKey
    *  @param business Database column business DBType(INT), Default(100)
    *  @param username Database column username DBType(VARCHAR), Length(22,true), Default()
    *  @param appkeyOwt Database column appkey_owt DBType(VARCHAR), Length(22,true)
    *  @param project Database column project DBType(VARCHAR), Length(128,true)
    *  @param model Database column model DBType(VARCHAR), Length(128,true)
    *  @param owt Database column owt DBType(VARCHAR), Length(22,true), Default()
    *  @param day Database column day DBType(DATE)
    *  @param dtype Database column dtype DBType(INT)
    *  @param count Database column count DBType(INT)
    *  @param createTime Database column create_time DBType(BIGINT) */
  case class WorthEventCountRow(id: Long, business: Int = 100, username: String = "", appkeyOwt: String, project: String, model: String, owt: String = "", day: java.sql.Date, dtype: Int, count: Int, createTime: Long)
  /** GetResult implicit for fetching WorthEventCountRow objects using plain SQL queries */
  implicit def GetResultWorthEventCountRow(implicit e0: GR[Long], e1: GR[Int], e2: GR[String], e3: GR[java.sql.Date]): GR[WorthEventCountRow] = GR{
    prs => import prs._
      WorthEventCountRow.tupled((<<[Long], <<[Int], <<[String], <<[String], <<[String], <<[String], <<[String], <<[java.sql.Date], <<[Int], <<[Int], <<[Long]))
  }
  /** Table description of table worth_event_count. Objects of this class serve as prototypes for rows in queries. */
  class WorthEventCount(_tableTag: Tag) extends Table[WorthEventCountRow](_tableTag, "worth_event_count") {
    def * = (id, business, username, appkeyOwt, project, model, owt, day, dtype, count, createTime) <> (WorthEventCountRow.tupled, WorthEventCountRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (id.?, business.?, username.?, appkeyOwt.?, project.?, model.?, owt.?, day.?, dtype.?, count.?, createTime.?).shaped.<>({r=>import r._; _1.map(_=> WorthEventCountRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6.get, _7.get, _8.get, _9.get, _10.get, _11.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id DBType(BIGINT), AutoInc, PrimaryKey */
    val id: Column[Long] = column[Long]("id", O.AutoInc, O.PrimaryKey)
    /** Database column business DBType(INT), Default(100) */
    val business: Column[Int] = column[Int]("business", O.Default(100))
    /** Database column username DBType(VARCHAR), Length(22,true), Default() */
    val username: Column[String] = column[String]("username", O.Length(22,varying=true), O.Default(""))
    /** Database column appkey_owt DBType(VARCHAR), Length(22,true) */
    val appkeyOwt: Column[String] = column[String]("appkey_owt", O.Length(22,varying=true))
    /** Database column project DBType(VARCHAR), Length(128,true) */
    val project: Column[String] = column[String]("project", O.Length(128,varying=true))
    /** Database column model DBType(VARCHAR), Length(128,true) */
    val model: Column[String] = column[String]("model", O.Length(128,varying=true))
    /** Database column owt DBType(VARCHAR), Length(22,true), Default() */
    val owt: Column[String] = column[String]("owt", O.Length(22,varying=true), O.Default(""))
    /** Database column day DBType(DATE) */
    val day: Column[java.sql.Date] = column[java.sql.Date]("day")
    /** Database column dtype DBType(INT) */
    val dtype: Column[Int] = column[Int]("dtype")
    /** Database column count DBType(INT) */
    val count: Column[Int] = column[Int]("count")
    /** Database column create_time DBType(BIGINT) */
    val createTime: Column[Long] = column[Long]("create_time")

    /** Index over (createTime) (database name idx_create_time) */
    val index1 = index("idx_create_time", createTime)
    /** Index over (username) (database name idx_username) */
    val index2 = index("idx_username", username)
  }
  /** Collection-like TableQuery object for table WorthEventCount */
  lazy val WorthEventCount = new TableQuery(tag => new WorthEventCount(tag))

  /** Entity class storing rows of table WorthFunction
    *  @param id Database column id DBType(BIGINT), AutoInc, PrimaryKey
    *  @param project Database column project DBType(VARCHAR), Length(128,true)
    *  @param model Database column model DBType(VARCHAR), Length(128,true)
    *  @param functionName Database column function_name DBType(VARCHAR), Length(256,true)
    *  @param functionDesc Database column function_desc DBType(VARCHAR), Length(256,true), Default()
    *  @param functionType Database column function_type DBType(INT)
    *  @param createTime Database column create_time DBType(BIGINT)
    *  @param deleted Database column deleted DBType(BIT), Default(false) */
  case class WorthFunctionRow(id: Long, project: String, model: String, functionName: String, functionDesc: String = "", functionType: Int, createTime: Long, deleted: Boolean = false)
  /** GetResult implicit for fetching WorthFunctionRow objects using plain SQL queries */
  implicit def GetResultWorthFunctionRow(implicit e0: GR[Long], e1: GR[String], e2: GR[Int], e3: GR[Boolean]): GR[WorthFunctionRow] = GR{
    prs => import prs._
      WorthFunctionRow.tupled((<<[Long], <<[String], <<[String], <<[String], <<[String], <<[Int], <<[Long], <<[Boolean]))
  }
  /** Table description of table worth_function. Objects of this class serve as prototypes for rows in queries. */
  class WorthFunction(_tableTag: Tag) extends Table[WorthFunctionRow](_tableTag, "worth_function") {
    def * = (id, project, model, functionName, functionDesc, functionType, createTime, deleted) <> (WorthFunctionRow.tupled, WorthFunctionRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (id.?, project.?, model.?, functionName.?, functionDesc.?, functionType.?, createTime.?, deleted.?).shaped.<>({r=>import r._; _1.map(_=> WorthFunctionRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6.get, _7.get, _8.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id DBType(BIGINT), AutoInc, PrimaryKey */
    val id: Column[Long] = column[Long]("id", O.AutoInc, O.PrimaryKey)
    /** Database column project DBType(VARCHAR), Length(128,true) */
    val project: Column[String] = column[String]("project", O.Length(128,varying=true))
    /** Database column model DBType(VARCHAR), Length(128,true) */
    val model: Column[String] = column[String]("model", O.Length(128,varying=true))
    /** Database column function_name DBType(VARCHAR), Length(256,true) */
    val functionName: Column[String] = column[String]("function_name", O.Length(256,varying=true))
    /** Database column function_desc DBType(VARCHAR), Length(256,true), Default() */
    val functionDesc: Column[String] = column[String]("function_desc", O.Length(256,varying=true), O.Default(""))
    /** Database column function_type DBType(INT) */
    val functionType: Column[Int] = column[Int]("function_type")
    /** Database column create_time DBType(BIGINT) */
    val createTime: Column[Long] = column[Long]("create_time")
    /** Database column deleted DBType(BIT), Default(false) */
    val deleted: Column[Boolean] = column[Boolean]("deleted", O.Default(false))

    /** Index over (createTime) (database name idx_create_time) */
    val index1 = index("idx_create_time", createTime)
    /** Index over (functionName) (database name idx_function_name) */
    val index2 = index("idx_function_name", functionName)
    /** Index over (project,model,functionName) (database name idx_project_model_function) */
    val index3 = index("idx_project_model_function", (project, model, functionName))
  }
  /** Collection-like TableQuery object for table WorthFunction */
  lazy val WorthFunction = new TableQuery(tag => new WorthFunction(tag))

  /** Entity class storing rows of table WorthModelCount
    *  @param id Database column id DBType(BIGINT), AutoInc, PrimaryKey
    *  @param functionName Database column function_name DBType(VARCHAR), Length(256,true), Default()
    *  @param project Database column project DBType(VARCHAR), Length(128,true)
    *  @param model Database column model DBType(VARCHAR), Length(128,true)
    *  @param day Database column day DBType(DATE)
    *  @param dtype Database column dtype DBType(INT)
    *  @param count Database column count DBType(INT)
    *  @param createTime Database column create_time DBType(BIGINT) */
  case class WorthModelCountRow(id: Long, functionName: String = "", project: String, model: String, day: java.sql.Date, dtype: Int, count: Int, createTime: Long)
  /** GetResult implicit for fetching WorthModelCountRow objects using plain SQL queries */
  implicit def GetResultWorthModelCountRow(implicit e0: GR[Long], e1: GR[String], e2: GR[java.sql.Date], e3: GR[Int]): GR[WorthModelCountRow] = GR{
    prs => import prs._
      WorthModelCountRow.tupled((<<[Long], <<[String], <<[String], <<[String], <<[java.sql.Date], <<[Int], <<[Int], <<[Long]))
  }
  /** Table description of table worth_model_count. Objects of this class serve as prototypes for rows in queries. */
  class WorthModelCount(_tableTag: Tag) extends Table[WorthModelCountRow](_tableTag, "worth_model_count") {
    def * = (id, functionName, project, model, day, dtype, count, createTime) <> (WorthModelCountRow.tupled, WorthModelCountRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (id.?, functionName.?, project.?, model.?, day.?, dtype.?, count.?, createTime.?).shaped.<>({r=>import r._; _1.map(_=> WorthModelCountRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6.get, _7.get, _8.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id DBType(BIGINT), AutoInc, PrimaryKey */
    val id: Column[Long] = column[Long]("id", O.AutoInc, O.PrimaryKey)
    /** Database column function_name DBType(VARCHAR), Length(256,true), Default() */
    val functionName: Column[String] = column[String]("function_name", O.Length(256,varying=true), O.Default(""))
    /** Database column project DBType(VARCHAR), Length(128,true) */
    val project: Column[String] = column[String]("project", O.Length(128,varying=true))
    /** Database column model DBType(VARCHAR), Length(128,true) */
    val model: Column[String] = column[String]("model", O.Length(128,varying=true))
    /** Database column day DBType(DATE) */
    val day: Column[java.sql.Date] = column[java.sql.Date]("day")
    /** Database column dtype DBType(INT) */
    val dtype: Column[Int] = column[Int]("dtype")
    /** Database column count DBType(INT) */
    val count: Column[Int] = column[Int]("count")
    /** Database column create_time DBType(BIGINT) */
    val createTime: Column[Long] = column[Long]("create_time")

    /** Index over (createTime) (database name idx_create_time) */
    val index1 = index("idx_create_time", createTime)
    /** Index over (functionName) (database name idx_functionname) */
    val index2 = index("idx_functionname", functionName)
  }
  /** Collection-like TableQuery object for table WorthModelCount */
  lazy val WorthModelCount = new TableQuery(tag => new WorthModelCount(tag))

  /** Entity class storing rows of table WorthValue
    *  @param id Database column id DBType(BIGINT), AutoInc, PrimaryKey
    *  @param functionId Database column function_id DBType(BIGINT)
    *  @param business Database column business DBType(INT), Default(100)
    *  @param owt Database column owt DBType(VARCHAR), Length(22,true), Default()
    *  @param project Database column project DBType(VARCHAR), Length(128,true)
    *  @param model Database column model DBType(VARCHAR), Length(128,true), Default()
    *  @param functionName Database column function_name DBType(VARCHAR), Length(256,true), Default()
    *  @param worth Database column worth DBType(INT)
    *  @param totalWorth Database column total_worth DBType(INT)
    *  @param primitiveTotalWorth Database column primitive_total_worth DBType(INT)
    *  @param costTime Database column cost_time DBType(BIGINT)
    *  @param worthTime Database column worth_time DBType(BIGINT)
    *  @param createTime Database column create_time DBType(BIGINT)
    *  @param deleted Database column deleted DBType(BIT), Default(false) */
  case class WorthValueRow(id: Long, functionId: Long, business: Int = 100, owt: String = "", project: String, model: String = "", functionName: String = "", worth: Int, totalWorth: Int, primitiveTotalWorth: Int, costTime: Long, worthTime: Long, createTime: Long, deleted: Boolean = false)
  /** GetResult implicit for fetching WorthValueRow objects using plain SQL queries */
  implicit def GetResultWorthValueRow(implicit e0: GR[Long], e1: GR[Int], e2: GR[String], e3: GR[Boolean]): GR[WorthValueRow] = GR{
    prs => import prs._
      WorthValueRow.tupled((<<[Long], <<[Long], <<[Int], <<[String], <<[String], <<[String], <<[String], <<[Int], <<[Int], <<[Int], <<[Long], <<[Long], <<[Long], <<[Boolean]))
  }
  /** Table description of table worth_value. Objects of this class serve as prototypes for rows in queries. */
  class WorthValue(_tableTag: Tag) extends Table[WorthValueRow](_tableTag, "worth_value") {
    def * = (id, functionId, business, owt, project, model, functionName, worth, totalWorth, primitiveTotalWorth, costTime, worthTime, createTime, deleted) <> (WorthValueRow.tupled, WorthValueRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (id.?, functionId.?, business.?, owt.?, project.?, model.?, functionName.?, worth.?, totalWorth.?, primitiveTotalWorth.?, costTime.?, worthTime.?, createTime.?, deleted.?).shaped.<>({r=>import r._; _1.map(_=> WorthValueRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6.get, _7.get, _8.get, _9.get, _10.get, _11.get, _12.get, _13.get, _14.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id DBType(BIGINT), AutoInc, PrimaryKey */
    val id: Column[Long] = column[Long]("id", O.AutoInc, O.PrimaryKey)
    /** Database column function_id DBType(BIGINT) */
    val functionId: Column[Long] = column[Long]("function_id")
    /** Database column business DBType(INT), Default(100) */
    val business: Column[Int] = column[Int]("business", O.Default(100))
    /** Database column owt DBType(VARCHAR), Length(22,true), Default() */
    val owt: Column[String] = column[String]("owt", O.Length(22,varying=true), O.Default(""))
    /** Database column project DBType(VARCHAR), Length(128,true) */
    val project: Column[String] = column[String]("project", O.Length(128,varying=true))
    /** Database column model DBType(VARCHAR), Length(128,true), Default() */
    val model: Column[String] = column[String]("model", O.Length(128,varying=true), O.Default(""))
    /** Database column function_name DBType(VARCHAR), Length(256,true), Default() */
    val functionName: Column[String] = column[String]("function_name", O.Length(256,varying=true), O.Default(""))
    /** Database column worth DBType(INT) */
    val worth: Column[Int] = column[Int]("worth")
    /** Database column total_worth DBType(INT) */
    val totalWorth: Column[Int] = column[Int]("total_worth")
    /** Database column primitive_total_worth DBType(INT) */
    val primitiveTotalWorth: Column[Int] = column[Int]("primitive_total_worth")
    /** Database column cost_time DBType(BIGINT) */
    val costTime: Column[Long] = column[Long]("cost_time")
    /** Database column worth_time DBType(BIGINT) */
    val worthTime: Column[Long] = column[Long]("worth_time")
    /** Database column create_time DBType(BIGINT) */
    val createTime: Column[Long] = column[Long]("create_time")
    /** Database column deleted DBType(BIT), Default(false) */
    val deleted: Column[Boolean] = column[Boolean]("deleted", O.Default(false))

    /** Index over (createTime) (database name idx_create_time) */
    val index1 = index("idx_create_time", createTime)
    /** Index over (project,model,functionName) (database name idx_project_model_function) */
    val index2 = index("idx_project_model_function", (project, model, functionName))
    /** Index over (business,worthTime) (database name index_worth__time) */
    val index3 = index("index_worth__time", (business, worthTime))
  }
  /** Collection-like TableQuery object for table WorthValue */
  lazy val WorthValue = new TableQuery(tag => new WorthValue(tag))

  /** Entity class storing rows of table WorthValueCount
    *  @param id Database column id DBType(BIGINT), AutoInc, PrimaryKey
    *  @param business Database column business DBType(INT), Default(100)
    *  @param owt Database column owt DBType(VARCHAR), Length(22,true), Default()
    *  @param project Database column project DBType(VARCHAR), Length(128,true)
    *  @param model Database column model DBType(VARCHAR), Length(128,true), Default()
    *  @param worth Database column worth DBType(INT)
    *  @param totalWorth Database column total_worth DBType(INT)
    *  @param primitiveTotalWorth Database column primitive_total_worth DBType(INT)
    *  @param costTime Database column cost_time DBType(BIGINT)
    *  @param day Database column day DBType(DATE)
    *  @param createTime Database column create_time DBType(BIGINT)
    *  @param deleted Database column deleted DBType(BIT), Default(false) */
  case class WorthValueCountRow(id: Long, business: Int = 100, owt: String = "", project: String, model: String = "", worth: Int, totalWorth: Int, primitiveTotalWorth: Int, costTime: Long, day: java.sql.Date, createTime: Long, deleted: Boolean = false)
  /** GetResult implicit for fetching WorthValueCountRow objects using plain SQL queries */
  implicit def GetResultWorthValueCountRow(implicit e0: GR[Long], e1: GR[Int], e2: GR[String], e3: GR[java.sql.Date], e4: GR[Boolean]): GR[WorthValueCountRow] = GR{
    prs => import prs._
      WorthValueCountRow.tupled((<<[Long], <<[Int], <<[String], <<[String], <<[String], <<[Int], <<[Int], <<[Int], <<[Long], <<[java.sql.Date], <<[Long], <<[Boolean]))
  }
  /** Table description of table worth_value_count. Objects of this class serve as prototypes for rows in queries. */
  class WorthValueCount(_tableTag: Tag) extends Table[WorthValueCountRow](_tableTag, "worth_value_count") {
    def * = (id, business, owt, project, model, worth, totalWorth, primitiveTotalWorth, costTime, day, createTime, deleted) <> (WorthValueCountRow.tupled, WorthValueCountRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (id.?, business.?, owt.?, project.?, model.?, worth.?, totalWorth.?, primitiveTotalWorth.?, costTime.?, day.?, createTime.?, deleted.?).shaped.<>({r=>import r._; _1.map(_=> WorthValueCountRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6.get, _7.get, _8.get, _9.get, _10.get, _11.get, _12.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id DBType(BIGINT), AutoInc, PrimaryKey */
    val id: Column[Long] = column[Long]("id", O.AutoInc, O.PrimaryKey)
    /** Database column business DBType(INT), Default(100) */
    val business: Column[Int] = column[Int]("business", O.Default(100))
    /** Database column owt DBType(VARCHAR), Length(22,true), Default() */
    val owt: Column[String] = column[String]("owt", O.Length(22,varying=true), O.Default(""))
    /** Database column project DBType(VARCHAR), Length(128,true) */
    val project: Column[String] = column[String]("project", O.Length(128,varying=true))
    /** Database column model DBType(VARCHAR), Length(128,true), Default() */
    val model: Column[String] = column[String]("model", O.Length(128,varying=true), O.Default(""))
    /** Database column worth DBType(INT) */
    val worth: Column[Int] = column[Int]("worth")
    /** Database column total_worth DBType(INT) */
    val totalWorth: Column[Int] = column[Int]("total_worth")
    /** Database column primitive_total_worth DBType(INT) */
    val primitiveTotalWorth: Column[Int] = column[Int]("primitive_total_worth")
    /** Database column cost_time DBType(BIGINT) */
    val costTime: Column[Long] = column[Long]("cost_time")
    /** Database column day DBType(DATE) */
    val day: Column[java.sql.Date] = column[java.sql.Date]("day")
    /** Database column create_time DBType(BIGINT) */
    val createTime: Column[Long] = column[Long]("create_time")
    /** Database column deleted DBType(BIT), Default(false) */
    val deleted: Column[Boolean] = column[Boolean]("deleted", O.Default(false))

    /** Index over (createTime) (database name idx_create_time) */
    val index1 = index("idx_create_time", createTime)
    /** Index over (business,day) (database name index_business_day) */
    val index2 = index("index_business_day", (business, day))
  }
  /** Collection-like TableQuery object for table WorthValueCount */
  lazy val WorthValueCount = new TableQuery(tag => new WorthValueCount(tag))
}