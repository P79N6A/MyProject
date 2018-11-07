package com.sankuai.octo.msgp.serivce

import com.sankuai.msgp.common.config.DbConnection
import com.sankuai.msgp.common.config.db.msgp.Tables._
import org.slf4j.{Logger, LoggerFactory}

import scala.slick.driver.MySQLDriver.simple._

object AppkeyAlias {
  private val LOG: Logger = LoggerFactory.getLogger(this.getClass)
  private val db = DbConnection.getPool()

  def aliasAppkey(appkey: String) = {
    db withSession {
      implicit session: Session =>
        val aliasAppkey = AppAlias.filter(x => x.appkey === appkey).firstOption
        if(aliasAppkey.isEmpty){
          appkey
        } else {
          aliasAppkey.get.errorLogAppkey
        }
    }
  }

  def octoAppkey(errorLogAppkey: String) = {
    db withSession {
      implicit session: Session =>
        val aliasAppkey = AppAlias.filter(x => x.errorLogAppkey === errorLogAppkey).firstOption
        if(aliasAppkey.isEmpty){
          errorLogAppkey
        } else {
          aliasAppkey.get.appkey
        }
    }
  }
}
