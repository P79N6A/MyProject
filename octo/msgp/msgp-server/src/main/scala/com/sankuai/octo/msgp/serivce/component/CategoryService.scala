package com.sankuai.octo.msgp.serivce.component

import com.sankuai.msgp.common.config.db.msgp.Tables
import com.sankuai.msgp.common.utils.helper.CommonHelper
import com.sankuai.octo.msgp.dao.component.CategoryDAO
import com.sankuai.octo.msgp.domain.Dependency
import com.sankuai.octo.msgp.model.ComponentCategory

/**
  * Created by yves on 16/9/5.
  */
object CategoryService {

  private val debug = false

  case class Category(category: String, categoryName: String, components: List[Dependency], count: List[Int], appCount: Int)

  def getCategoryOutline(base: String, business: String, category: String) = {
    if (CommonHelper.isOffline  && !debug) {
      List[Category]()
    } else {
      val appCount = CategoryDAO.getAppCount(base, business)
      val appCountResult = CategoryDAO.getCategoryOutline(base, business, category).groupBy(_.category)
      val categoryNameList = ComponentCategory.values.toList.filter(_ != ComponentCategory.others).map(x => ComponentCategory.getCategoryVariableName(x))
      val result = categoryNameList.flatMap {
        key =>
          val categoryDetailList = appCountResult.getOrElse(key, List[Tables.AppDependency#TableElementType]())
          if (categoryDetailList.isEmpty) {
            None
          } else {
            val cmptCount = categoryDetailList.groupBy(x => (x.groupId, x.artifactId)).map {
              x =>
                (new Dependency(x._1._1, x._1._2), x._2.length)
            }.toList
            Some(Category(key, ComponentCategory.getCategoryName(key), cmptCount.map(_._1), cmptCount.map(_._2), appCount))
          }
      }
      result
    }
  }
}
