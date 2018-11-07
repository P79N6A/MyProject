package com.sankuai.octo.msgp.dao.component

import com.sankuai.msgp.common.config.DbConnection
import com.sankuai.msgp.common.config.db.msgp.Tables._
import com.sankuai.octo.msgp.domain.Dependency
import com.sankuai.msgp.common.model.Base
import com.sankuai.msgp.common.utils.StringUtil

import scala.slick.driver.MySQLDriver.simple._
import scala.slick.jdbc.{StaticQuery => Q}


/**
  * Created by yves on 16/9/5.
  */
object CategoryDAO {

  private val db = DbConnection.getPool()

  def getCategoryOutline(base: String, business: String, category: String) = {
    db withSession {
      implicit session: Session =>
        if (base.equalsIgnoreCase(Base.all.getName)) {
          if (StringUtil.isBlank(category)) {
            AppDependency.filter(x => x.business === business && x.category =!= "").list
          } else {
            AppDependency.filter(x => x.business === business && x.category === category && x.category =!= "").list
          }
        } else {
          if (StringUtil.isBlank(category)) {
            AppDependency.filter(x => x.base === base && x.business === business && x.category =!= "").list
          } else {
            AppDependency.filter(x => x.base === base && x.business === business && x.category === category && x.category =!= "").list
          }
        }
    }

  }

  def updateCategory(dependencies: List[Dependency], category: String) = {
    db withSession {
      implicit session: Session =>
        dependencies.foreach {
          dependency =>
            AppDependency.filter(x => x.groupId === dependency.getGroupId && x.artifactId === dependency.getArtifactId)
              .map(_.category)
              .update(category)
        }
    }
  }

  def getAppCount(base: String, business: String) = {
    db withSession {
      implicit session: Session =>
        if(base.equalsIgnoreCase("all")){
          AppDependency.filter(x => x.business === business).list.map(x => (x.appGroupId, x.appArtifactId)).distinct.length
        }else{
          AppDependency.filter(x => x.base === base && x.business === business).list.map(x => (x.appGroupId, x.appArtifactId)).distinct.length
        }
    }
  }
}
