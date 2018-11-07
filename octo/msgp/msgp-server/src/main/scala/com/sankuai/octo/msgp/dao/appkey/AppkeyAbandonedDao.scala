package com.sankuai.octo.msgp.dao.appkey

import com.sankuai.msgp.common.config.DbConnection
import com.sankuai.msgp.common.config.db.msgp.Tables.{AppkeyAbandonedRow, _}

import scala.slick.driver.MySQLDriver.simple._
import scala.slick.jdbc.{StaticQuery => Q}

/**
  * Created by yves on 17/5/18.
  * 存储废弃的appkey
  */
object AppkeyAbandonedDao {

  private val db = DbConnection.getPool()


  def insert(row: AppkeyAbandonedRow) = {
    db withSession {
      implicit session: Session =>
        val statement = AppkeyAbandoned.filter(x => x.appkey === row.appkey)
        if (statement.exists.run) {
          // update
          statement.map(x => (x.name, x.base, x.appkey, x.baseapp, x.owners, x.observers, x.pdl, x.owt, x.reglimit, x.intro, x.tags, x.business, x.category, x.operator, x.deleteTime))
            .update(row.name, row.base, row.appkey, row.baseapp, row.owners, row.observers, row.pdl, row.owt, row.reglimit, row.intro, row.tags, row.business, row.category, row.operator, row.deleteTime)
        } else {
          //insert
          AppkeyAbandoned += row
        }
    }
  }
}
