package com.sankuai.octo.oswatch.utils

import com.mchange.v2.c3p0.ComboPooledDataSource
import scala.slick.driver.MySQLDriver.simple._
/**
 * Created by dreamblossom on 15/9/29.
 */
object DBConnection {
  private val host = MTConfig.get("mysql.host","127.0.0.1")
  private val auth = MTConfig.get("mysql.auth","user=root&password=4404")
  private val url = s"jdbc:mysql://$host/inf_oswatch?$auth&useUnicode=true&characterEncoding=UTF-8"

  def getPool = {
    val ds = new ComboPooledDataSource
    ds.setJdbcUrl(url)
    Database.forDataSource(ds)
  }
}
