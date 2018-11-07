package com.sankuai.msgp.common.config

import com.dianping.zebra.group.jdbc.GroupDataSource
import com.mchange.v2.c3p0.ComboPooledDataSource
import com.sankuai.inf.octo.mns.model.HostEnv
import com.sankuai.inf.octo.mns.util.ProcessInfoUtil
import com.sankuai.msgp.common.utils.helper.CommonHelper
import org.slf4j.LoggerFactory

import scala.slick.driver.MySQLDriver.simple._

object DbConnection {
  private val driver = "com.mysql.jdbc.Driver"
  private val logger = LoggerFactory.getLogger(this.getClass)

  private val master = {
    MsgpConfig.get("mysql.master", "10.20.116.73:5002")
  }

  private val auth = {
    MsgpConfig.get("mysql.auth", "user=dbadmin&password=dbpasswd")
  }

  private val jdbcRef = if (CommonHelper.isOffline) {
    if (ProcessInfoUtil.getHostEnv == HostEnv.TEST) {
      "inf_msgp_test"
    } else {
      "inf_msgp_dev"
    }
  } else {
//    "msgp_product"
    "msgp_msgp_product"
  }

  val databaseConnectionPool = {
    val ds = new GroupDataSource
    ds.setJdbcRef(jdbcRef)
    ds.setDriverClass(driver)
    ds.setMinPoolSize(2)
    ds.setTestConnectionOnCheckin(true)
    ds.setTestConnectionOnCheckout(true)
    ds.setIdleConnectionTestPeriod(18000)
    ds.setMaxIdleTime(7200)
    if (CommonHelper.isOffline) {
      ds.setMaxPoolSize(10)
    } else {
      ds.setMaxPoolSize(100)
    }
    ds.init()
    Database.forDataSource(ds)
  }

  def getPool() = {
    databaseConnectionPool
  }

  private val mworthJdbcRef = {
    if (CommonHelper.isOffline) {
      if (ProcessInfoUtil.getHostEnv == HostEnv.TEST) {
        "inf_mworth_test"
      } else {
        "inf_mworth_dev"
      }
    } else {
      // 暂时还没有
      "inf_mworth_product"
    }
  }
  val worthUrl = s"jdbc:mysql://$master/mworth?$auth&useUnicode=true&characterEncoding=UTF-8"
  val databaseWorthConnectionPool = if(!CommonHelper.isOffline){
    val ds = new ComboPooledDataSource
    ds.setDriverClass(driver)
    ds.setJdbcUrl(worthUrl)
    ds.setInitialPoolSize(2)
    ds.setMinPoolSize(2)
    ds.setTestConnectionOnCheckin(true)
    ds.setTestConnectionOnCheckout(true)
    ds.setIdleConnectionTestPeriod(18000)
    ds.setMaxIdleTime(7200)
    ds.setMaxPoolSize(100)
    ds.setAcquireIncrement(2)
    Database.forDataSource(ds)
  }else{
    val ds = new GroupDataSource
    ds.setJdbcRef(mworthJdbcRef)
    ds.setDriverClass(driver)
    ds.setMinPoolSize(2)
    ds.setTestConnectionOnCheckin(true)
    ds.setTestConnectionOnCheckout(true)
    ds.setIdleConnectionTestPeriod(18000)
    ds.setMaxIdleTime(7200)
    ds.setMaxPoolSize(10)
    ds.init()
    Database.forDataSource(ds)
  }

  def getWorthPool() = {
    databaseWorthConnectionPool
  }

  private val errorLogJdbcRef = {
    if (CommonHelper.isOffline) {
      if (ProcessInfoUtil.getHostEnv == HostEnv.TEST) {
        "inf_errorlog_offline_test"
      } else {
        "inf_errorlog_offline_dev"
      }
    } else {
      "notify2_meituansg_product"
    }
  }
  private val errorLogDBConnectionPool = {
    val ds = new GroupDataSource
    ds.setJdbcRef(errorLogJdbcRef)
    ds.setDriverClass(driver)
    ds.setMinPoolSize(2)
    ds.setTestConnectionOnCheckin(true)
    ds.setTestConnectionOnCheckout(true)
    ds.setIdleConnectionTestPeriod(18000)
    ds.setMaxIdleTime(7200)
    ds.setPoolType("druid")
    if (CommonHelper.isOffline) {
      ds.setMaxPoolSize(10)
      ds.setConnectionInitSql("set session transaction isolation level read committed")
    } else {
      ds.setMaxPoolSize(100)
    }
    ds.init()
    Database.forDataSource(ds)
  }

  def getErrorLogPool() = {
    errorLogDBConnectionPool
  }
}
